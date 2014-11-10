package com.easytarget.micopi;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by michel on 06/11/14.
 */
public class ContactCrawler {

    private static String DEBUG_TAG = "ContactCrawler";

    public static void crawl(Context context, boolean doGenerateAll, boolean doAsk) {
        final String projection[] = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.PHOTO_ID,
        };

        final Cursor cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            final String contactId = cursor.getString(0);
            final int photoId   = cursor.getInt(1);

            Contact contact = new Contact(context, contactId);

            if (photoId <= 0 || doGenerateAll) {
                Log.d(DEBUG_TAG, "Automatically generating a new picture.");
            }
        }
    }
}
