/*
 * Copyright (C) 2014 Easy Target
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.easytarget.micopi;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Created by michel on 06/11/14.
 *
 * Utility class used for stepping through the entire database of contacts.
 */
public class ContactCrawler {

    private static String DEBUG_TAG = "ContactCrawler";

    public static Cursor allContacts(Context context) {
        final String projection[] = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.PHOTO_ID,
        };

        return context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

//        while (cursor.moveToNext()) {
//            final String contactId = cursor.getString(0);
//            final int photoId   = cursor.getInt(1);
//
//            Contact contact = new Contact(context, contactId);
//
//            if (photoId <= 0 || doGenerateAll) {
//                Log.d(DEBUG_TAG, "Automatically generating a new picture.");
//            }
//        }
    }
}
