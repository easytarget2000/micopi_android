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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.easytarget.micopi.engine.ImageFactory;

import java.util.ArrayList;

/**
 * Based on com.germainz.identiconizer.services.IdenticonCreationService
 */
public class BatchService extends IntentService {

    private static final String LOG_TAG = BatchService.class.getSimpleName();

    private static final int SERVICE_NOTIFICATION_ID = 441444;

    private static final int ERROR_NOTIFICATION_ID = 4412669;

    private ArrayList<Contact> mInsertErrors = new ArrayList<>();
    private ArrayList<Contact> mUpdateErrors = new ArrayList<>();

    public BatchService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        startForeground(SERVICE_NOTIFICATION_ID, createNotification());

        final boolean doOverwrite =
                intent.getBooleanExtra(Constants.EXTRA_DO_OVERWRITE, false);
        final int screenWidthInPixels =
                intent.getIntExtra(Constants.EXTRA_IMAGE_SIZE, 1080);

        Log.d(LOG_TAG, "onHandleIntent: " + doOverwrite + ", " + screenWidthInPixels);
        processContacts(doOverwrite, screenWidthInPixels);

//        if (mUpdateErrors.size() > 0 || mInsertErrors.size() > 0) createNotificationForError();
//        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("CONTACTS_UPDATED"));
        getContentResolver().notifyChange(ContactsContract.Data.CONTENT_URI, null);
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

        while (cursor.moveToNext()) {
            final int rawContactId = cursor.getInt(idIndex);
            final String name = cursor.getString(nameIndex);
            final int photoId = cursor.getInt(photoIdIndex);
            if (!TextUtils.isEmpty(name)) {
//                final byte[] photo = getContactPhotoBlob(photoId);

                Contact contact = new Contact(getApplicationContext(), rawContactId + "");

                if (photoId <= 0 || doOverwrite) {
                    Log.d(LOG_TAG, "Generating image for " + contact.toString() + ".");
                    final Bitmap generatedBitmap =
                            new ImageFactory(contact, screenWidthInPixels).generateBitmap();

                    Log.d(LOG_TAG, "Assigning image to " + contact.toString() + ".");

//                    FileHelper.assignImage(getApplicationContext(), generatedBitmap, rawContactId);
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

//    private Notification createNotification() {
//        Intent intent = new Intent(this, IdenticonsSettings.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        @SuppressWarnings("deprecation")
//        Notification notice = new Notification.Builder(this)
//                .setAutoCancel(false)
//                .setOngoing(true)
//                .setContentTitle(getString(R.string.identicons_creation_service_running_title))
//                .setContentText(getString(R.string.identicons_creation_service_running_summary))
//                .setSmallIcon(R.drawable.ic_settings_identicons)
//                .setWhen(System.currentTimeMillis())
//                .setContentIntent(contentIntent)
//                .getNotification();
//        return notice;
//    }

//    private void updateNotification(String title, String text) {
//        Intent intent = new Intent(this, IdenticonsSettings.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        NotificationManager nm =
//                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        @SuppressWarnings("deprecation")
//        Notification notice = new Notification.Builder(this)
//                .setAutoCancel(false)
//                .setOngoing(true)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setSmallIcon(R.drawable.ic_settings_identicons)
//                .setWhen(System.currentTimeMillis())
//                .setContentIntent(contentIntent)
//                .getNotification();
//        nm.notify(SERVICE_NOTIFICATION_ID, notice);
//    }

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
}
