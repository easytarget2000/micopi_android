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
import com.easytarget.micopi.ui.WelcomeActivity;

import java.util.ArrayList;

/**
 * Based on com.germainz.identiconizer.services.IdenticonCreationService
 */
public class BatchService extends IntentService {

    private static final String LOG_TAG = BatchService.class.getSimpleName();

    private static final int SERVICE_NOTIFICATION_ID = 441444;

    private static final int ERROR_NOTIFICATION_ID = 4412669;

    private ArrayList<Contact> mInsertErrors;

    private ArrayList<Contact> mUpdateErrors;

    public BatchService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startForeground(SERVICE_NOTIFICATION_ID, createNotification("", 10, 0));

        final boolean doOverwrite =
                intent.getBooleanExtra(Constants.EXTRA_DO_OVERWRITE, false);
        final int screenWidthInPixels =
                intent.getIntExtra(Constants.EXTRA_IMAGE_SIZE, 1080);

        Log.d(LOG_TAG, "onHandleIntent: " + doOverwrite + ", " + screenWidthInPixels);
        mInsertErrors = new ArrayList<>();

        mUpdateErrors = new ArrayList<>();

        processContacts(doOverwrite, screenWidthInPixels);

//        if (mUpdateErrors.size() > 0 || mInsertErrors.size() > 0) createNotificationForError();
//        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("CONTACTS_UPDATED"));
        getContentResolver().notifyChange(ContactsContract.Data.CONTENT_URI, null);

        Intent progressBroadcast = new Intent(Constants.ACTION_FINISHED_GENERATE);
        sendBroadcast(progressBroadcast);
        stopForeground(true);
    }

    private void processContacts(final boolean doOverwrite, final int screenWidthInPixels) {
        Cursor cursor = getContacts();
        final int idIndex =
                cursor.getColumnIndex(ContactsContract.Contacts._ID);
        final int nameIndex =
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        final int photoIdIndex =
                cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID);

        final int maxProgress = cursor.getCount() * 2;
        int progress = 0;
        while (cursor.moveToNext()) {
            final String rawContactId = cursor.getString(idIndex);
            final String name = cursor.getString(nameIndex);
            final int photoId = cursor.getInt(photoIdIndex);
            if (!TextUtils.isEmpty(name)) {
//                final byte[] photo = getContactPhotoBlob(photoId);

                Contact contact = new Contact(getApplicationContext(), rawContactId + "");

                if (photoId <= 0 || doOverwrite) {
                    updateProgress(contact.getFullName(), maxProgress, progress++);
                    Log.d(LOG_TAG, "Generating image for " + contact.toString() + ".");
                    final Bitmap generatedBitmap =
                            new ImageFactory(contact, screenWidthInPixels).generateBitmap();

                    updateProgress(contact.getFullName(), maxProgress, progress++);

                    Log.d(LOG_TAG, "Assigning image to " + contact.toString() + ".");
                    FileHelper.assignImageToContact(
                            getApplicationContext(),
                            generatedBitmap,
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

    private Notification createNotification(final String text, int maxProgress, int progress) {
        PendingIntent contentIntent = getNotificationIntent();

        if (mLargeIcon == null) {
            mLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        }

        return new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(getResources().getString(R.string.batch_experimental))
                .setContentText(text != null ? text : "0")
                .setSmallIcon(R.drawable.ic_account_circle_white_24dp)
                .setLargeIcon(mLargeIcon)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setProgress(maxProgress, progress, false)
                .build();
    }

    private NotificationManager mNotMan;

    private void updateProgress(final String contactName, int maxProgress, int progress) {
        // Broadcast that will be received by the Activity:
        Intent progressBroadcast = new Intent(Constants.ACTION_UPDATE_PROGRESS);
        final int normalisedProgress = (int) (((float) progress / (float) maxProgress) * 100f);
        progressBroadcast.putExtra(Constants.EXTRA_PROGRESS, normalisedProgress);
        progressBroadcast.putExtra(Constants.EXTRA_CONTACT, contactName);
        sendBroadcast(progressBroadcast);

        Notification notification = createNotification(contactName, maxProgress, progress);

        if (mNotMan == null) {
            mNotMan = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotMan.notify(SERVICE_NOTIFICATION_ID, notification);
    }

//    private void createNotificationForError() {
//        Intent intent = new Intent(this, ErrorsListActivity.class);
//        intent.putParcelableArrayListExtra("insertErrors", mInsertErrors);
//        intent.putParcelableArrayListExtra("updateErrors", mUpdateErrors);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        String contentText = getString(R.string.sql_error_notification_text, mInsertErrors.size() + mUpdateErrors.size());
//        Notification notice = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_settings_identicons)
//                .setContentTitle(getString(R.string.sql_error_notification_title))
//                .setContentText(contentText)
//                .setContentIntent(contentIntent)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
//                .setAutoCancel(true)
//                .build();
//        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.notify(ERROR_NOTIFICATION_ID, notice);
//    }

    private PendingIntent getNotificationIntent() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
