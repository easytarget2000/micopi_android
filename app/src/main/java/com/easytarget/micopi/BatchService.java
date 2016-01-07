package com.easytarget.micopi;

/*
 * Original work Copyright (C) 2013 The ChameleonOS Open Source Project
 * Modified work Copyright (C) 2013-2014 GermainZ@xda-developers.com
 * Additional modification (C) 2015 michel@easy-target.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.easytarget.micopi.engine.ImageFactory;
import com.easytarget.micopi.ui.BatchActivity;

import java.util.ArrayList;

/**
 * Based on com.germainz.identiconizer.services.IdenticonCreationService
 */
public class BatchService extends IntentService {

    private static final String LOG_TAG = BatchService.class.getSimpleName();

    private static boolean sIsRunning = false;

    public static final int SERVICE_NOTIFICATION_ID = 441444;

    private ArrayList<Contact> mInsertErrors;

    private ArrayList<Contact> mUpdateErrors;

    private boolean mIsCancelled;

    private Contact mContact;

    private int mProgress;

    private int mMaxProgress;

    private int mScreenWidthPixels;

    public BatchService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mProgress = 0;
        mMaxProgress = 0;
        mIsCancelled = false;
        mContact = null;

        startForeground(SERVICE_NOTIFICATION_ID, createNotification(""));

        final boolean doOverwrite =
                intent.getBooleanExtra(Constants.EXTRA_DO_OVERWRITE, false);
        mScreenWidthPixels = intent.getIntExtra(Constants.EXTRA_IMAGE_SIZE, 1080);

        Log.d(LOG_TAG, "onHandleIntent: " + doOverwrite + ", " + mScreenWidthPixels);
        mInsertErrors = new ArrayList<>();

        mUpdateErrors = new ArrayList<>();

        sIsRunning = true;
        processContacts(doOverwrite);

//        if (mUpdateErrors.size() > 0 || mInsertErrors.size() > 0) createNotificationForError();
//        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("CONTACTS_UPDATED"));
        getContentResolver().notifyChange(ContactsContract.Data.CONTENT_URI, null);

        Intent progressBroadcast = new Intent(Constants.ACTION_FINISHED_GENERATE);
        sendBroadcast(progressBroadcast);

        // TODO: Show finish notification.

        stopForeground(true);
        sIsRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsCancelled = true;
        stopForeground(true);
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    private void processContacts(final boolean doOverwrite) {
        if (mScreenWidthPixels < 100) return;

        // TODO: Specify photo ID selection to get right amount of maxProgress.
        Cursor cursor = getContacts();
        final int idIndex =
                cursor.getColumnIndex(ContactsContract.Contacts._ID);
        final int nameIndex =
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        final int photoIdIndex =
                cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID);

        mMaxProgress = cursor.getCount() * 2;
        while (cursor.moveToNext() && !mIsCancelled) {
            final String rawContactId = cursor.getString(idIndex);
            final String name = cursor.getString(nameIndex);
            final int photoId = cursor.getInt(photoIdIndex);
            if (!TextUtils.isEmpty(name)) {
                mContact = Contact.buildContact(
                        getApplicationContext(),
                        String.valueOf(rawContactId)
                );
//                mContact = new Contact(getApplicationContext(), rawContactId + "");

                if (photoId <= 0 || doOverwrite) {
                    updateProgress();
//                    Log.d(LOG_TAG, "Generating image for " + mContact.toString() + ".");
                    final Bitmap bitmap = ImageFactory.bitmapFrom(
                            getApplicationContext(),
                            mContact,
                            mScreenWidthPixels
                    );

                    updateProgress();

                    FileHelper.assignImageToContact(
                            getApplicationContext(),
                            bitmap,
                            rawContactId
                    );
                }
            }
        }

        cursor.close();
    }

    private Cursor getContacts() {
        final Uri uri = ContactsContract.Contacts.CONTENT_URI;

        final String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID
        };

        final String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1";
//        if (Config.getInstance(this).shouldIgnoreContactVisibility())
//            selection = null;
        final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return getContentResolver().query(uri, projection, selection, null, sortOrder);
    }

    private Bitmap mLargeIcon;

    private Notification createNotification(final String text) {
        PendingIntent contentIntent = getNotificationIntent();

        if (mLargeIcon == null) {
            mLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        }

        return new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(getResources().getString(R.string.auto_mode))
                .setContentText(text != null ? text : "0")
                .setSmallIcon(R.drawable.ic_account_circle_white_24dp)
                .setLargeIcon(mLargeIcon)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setProgress(mMaxProgress, mProgress, false)
                .build();
    }

    private NotificationManager mNotMan;

    private void updateProgress() {
        if (mIsCancelled) return;
        mProgress++;

        // Broadcast that will be received by the Activity:
        Intent progressBroadcast = new Intent(Constants.ACTION_UPDATE_PROGRESS);
        final int normalisedProgress = (int) (((float) mProgress / (float) mMaxProgress) * 100f);
        progressBroadcast.putExtra(Constants.EXTRA_PROGRESS, normalisedProgress);
        progressBroadcast.putExtra(Constants.EXTRA_CONTACT, mContact.getFullName());
        sendBroadcast(progressBroadcast);

        Notification notification = createNotification(mContact.getFullName());

        if (mNotMan == null) {
            mNotMan = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotMan.notify(SERVICE_NOTIFICATION_ID, notification);
    }

    private PendingIntent getNotificationIntent() {
        Intent intent = new Intent(this, BatchActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
