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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class queries and stores contact data from a given intent.
 *
 * Created by Michel on 03.02.14.
 */
public class Contact implements Parcelable{
    private final static String DEBUG_TAG = "Contact";

    private Context mContext;
    private Uri mContactUri = null;
    private String[] mNameParts;
    private boolean mHasPhoto = false;
    private String mFullName = "";
    private String mPhoneNumber = "555";
    private String mEmailAddress = "NE";
    private String mBirthday = "NB";
    private String mTimesContacted = "0";
    private int mRetryFactor = 0;
    private boolean mMd5IsNew = true;
    private String mMd5String = "000000000000000000000000000";

//    /**
//     * Constructor Method
//     *
//     * @param c Context
//     * @param data Data from people list (device contacts)
//     */
//    public Contact(Context c, Intent data, String v) {
//        this.mContext = c;
//        this.mMd5IsNew = true;
//        String contactId, query;
//        Cursor contactCursor;
//
//        // The received data contains the URI of the chosen contact.
//        mContactUri = data.getData();
//        // The last part of the URI contains the contact's ID.
//        if(mContactUri != null) contactId = mContactUri.getLastPathSegment();
//        else return;
//
//        // Point the cursor at the DB query.
//        contactCursor = mContext.getContentResolver().query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
//                new String[] { contactId },
//                null
//        );
//
//        if(contactCursor == null) return;
//
//        /*
//        START CONTACT QUERY - PHONE NUMBER
//         */
//
//        // Attempt to move the cursor to the first entry with a phone number.
//        if(contactCursor.moveToFirst()) {
//            // Get the indices of the name and phone number.
//            final int displayNameIndex = contactCursor.getColumnIndex(
//                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
//            );
//            final int phoneNumberIndex = contactCursor.getColumnIndex(
//                    ContactsContract.CommonDataKinds.Phone.DATA
//            );
//            final int timesContactedIndex = contactCursor.getColumnIndex(
//                    ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED
//            );
//            mFullName = contactCursor.getString(displayNameIndex);
//            mPhoneNumber = contactCursor.getString(phoneNumberIndex);
//            mTimesContacted = contactCursor.getString(timesContactedIndex);
//        } else {
//            // If the contact does not have a phone number,
//            // perform a different query.
//
//            // Point the cursor at the DB query.
//            contactCursor = mContext.getContentResolver().query(
//                    mContactUri, null, null, null, null
//            );
//
//            // Get the index of the display name.
//            if(contactCursor != null) {
//                final int displayNameIndex = contactCursor.getColumnIndex(
//                        ContactsContract.Contacts.DISPLAY_NAME
//                );
//
//                // Attempt to move the cursor to the first entry of the query cursor.
//                if(contactCursor.moveToFirst())
//                    mFullName = contactCursor.getString(displayNameIndex);
//            }
//            else return;
//        }
//
//        /*
//         E-MAIL ADDRESS & CURRENT PHOTO
//         */
//
//        contactCursor = mContext.getContentResolver().query(
//                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
//                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
//                new String[] { contactId },
//                null
//        );
//
//        if(contactCursor != null) {
//            if(contactCursor.moveToFirst()) {
//                final int emailIndex = contactCursor.getColumnIndex(
//                        ContactsContract.CommonDataKinds.Email.DATA
//                );
//                final int photoIdIndex = contactCursor.getColumnIndex(
//                        ContactsContract.CommonDataKinds.Photo.PHOTO_ID
//                );
//
//                mEmailAddress = contactCursor.getString(emailIndex);
//                final int photoId = contactCursor.getInt(photoIdIndex);
//                Log.d("PhotoID", photoId + "");
//            }
//        }
//
//        /*
//         BIRTHDAY
//         */
//
//        final String birthdayCols[] = {
//                ContactsContract.CommonDataKinds.Event.START_DATE,
//                ContactsContract.CommonDataKinds.Event.TYPE,
//                ContactsContract.CommonDataKinds.Event.MIMETYPE,
//        };
//
//        query = ContactsContract.CommonDataKinds.Event.TYPE + "=" +
//                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + " and "+
//                ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" +
//                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " +
//                ContactsContract.Data.CONTACT_ID + " = " + contactId;
//
//        contactCursor = mContext.getContentResolver().query(
//                ContactsContract.Data.CONTENT_URI,
//                birthdayCols,
//                query,
//                null,
//                ContactsContract.Contacts.DISPLAY_NAME
//        );
//
//        if (contactCursor != null) {
//            if (contactCursor.moveToNext()) {
//                final int birthdayIndex = contactCursor.getColumnIndex(
//                        ContactsContract.CommonDataKinds.Event.START_DATE
//                );
//                mBirthday = contactCursor.getString(birthdayIndex);
//            }
//        }
//
//        splitFullName();
//    }

    private static final String DEBUG_TAG_QUERY = "Contact Query";

    /**
     * Constructor Method
     *
     * @param c Context
     * @param data Data from people list (device contacts)
     */
    public Contact(Context c, Intent data) {
        mContext = c;
        mMd5IsNew = true;

        /*
        CONTACTS DB QUERY
         */

        // The received data contains the URI of the chosen contact.
        mContactUri = data.getData();
        // The last part of the URI contains the contact's ID.
        final String contactId = mContactUri.getLastPathSegment();

        // Define which columns we want to query.
        final String[] contactsProjection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.TIMES_CONTACTED,
                ContactsContract.Contacts.PHOTO_ID
        };

        // Point the cursor at the DB query.
        final Cursor contactsCursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                contactsProjection,
                ContactsContract.Contacts._ID + "=?",
                new String[] {contactId},
                null
        );

        if(contactsCursor == null) {
            Log.e(DEBUG_TAG_QUERY, "ERROR: contactsCursor is null. Contact: " + contactId);
            return;
        }

        int hasPhoneNumber;

        if(contactsCursor.moveToFirst()) {
            // Get the indices of the name and phone number.
            final int displayNameIndex = contactsCursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME
            );
            final int hasNumberIndex = contactsCursor.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
            );
            final int timesContactedIndex = contactsCursor.getColumnIndex(
                    ContactsContract.Contacts.TIMES_CONTACTED
            );
            final int photoIdIndex = contactsCursor.getColumnIndex(
                    ContactsContract.Contacts.PHOTO_ID
            );

            mFullName = contactsCursor.getString(displayNameIndex);
            Log.i(DEBUG_TAG_QUERY, "Display Name: " + mFullName);

            hasPhoneNumber = contactsCursor.getInt(hasNumberIndex);
            Log.i(DEBUG_TAG_QUERY, "Has Phone Number: " + hasPhoneNumber);

            mTimesContacted = contactsCursor.getString(timesContactedIndex);
            Log.i(DEBUG_TAG_QUERY, "TimesContacted: " + mTimesContacted);

            final int photoId = contactsCursor.getInt(photoIdIndex);
            Log.i(DEBUG_TAG_QUERY, "PhotoId: " + photoId);
            if (photoId > 0) mHasPhoto = true;

            contactsCursor.close();
        } else {
            Log.e(DEBUG_TAG_QUERY, "ERROR: Cannot move contactsCursor. Contact: " + contactId);
            return;
        }

        /*
        PHONE DB QUERY
         */

        // If the contacts DB query said that this contact has a phone number,
        // the Phone DB will now be queried.
        if (hasPhoneNumber == 1) {
            // Query the phone DB only for the contact's phone number.
            final Cursor phoneCursor = mContext.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[] {contactId},
                    null
            );

            if (phoneCursor == null) {
                // Something went wrong. The cursor is null after querying.
                Log.w(DEBUG_TAG_QUERY, "WARNING: phoneCursor is null. Contact: " + contactId);
                mPhoneNumber = "1234567890";
            } else if (phoneCursor.moveToFirst()) {
                // The cursor moved into the first entry.
                // Get the phone number from the DATA/NUMBER column.
                final int phoneNumberIndex = phoneCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                );
                mPhoneNumber = phoneCursor.getString(phoneNumberIndex);
                Log.i(DEBUG_TAG_QUERY, "Display Name: " + mPhoneNumber);

                phoneCursor.close();
            } else {
                // Something went wrong. The cursor is not null but cannot go into the first entry.
                Log.w(DEBUG_TAG_QUERY, "WARNING: phoneCursor cannot move. Contact: " + contactId);
                mPhoneNumber = "1234567891";
                phoneCursor.close();
            }
        }

        /*
         E-MAIL DB QUERY
         */

        final Cursor emailCursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] {ContactsContract.CommonDataKinds.Email.DATA},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[] {contactId},
                null
        );

        if(emailCursor == null) {
            Log.w(DEBUG_TAG_QUERY, "WARNING: emailCursor is null. Contact: " + contactId);
        } else if(emailCursor.moveToFirst()) {
            final int emailIndex = emailCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.DATA
            );
            mEmailAddress = emailCursor.getString(emailIndex);

            Log.i(DEBUG_TAG_QUERY, "E-Mail Address: " + mEmailAddress);
            emailCursor.close();
        } else {
            Log.w(DEBUG_TAG_QUERY, "WARNING: emailCursor cannot move. Contact: " + contactId);
            emailCursor.close();
        }

        /*
         EVENT QUERY (BIRTHDAY START_DATE)
         */

        // Ask for the date and types of all stored events.
        final String birthdayProjection[] = {
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.MIMETYPE,
        };

        // Get the event of type birthday for the current user.
        final String birthdayQuery = ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + " and "+
                ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" +
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " +
                ContactsContract.Data.CONTACT_ID + " = " + contactId;

        final Cursor birthdayCursor = mContext.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                birthdayProjection,
                birthdayQuery,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        );

        // If the cursor found something, get START_DATE of the query result.
        if (birthdayCursor == null) {
            Log.w(DEBUG_TAG_QUERY, "WARNING: birthdayCursor is null. Contact: " + contactId);
        } else if (birthdayCursor.moveToNext()) {
            final int birthdayIndex = birthdayCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Event.START_DATE
            );
            mBirthday = birthdayCursor.getString(birthdayIndex);

            Log.i(DEBUG_TAG_QUERY, "Birthday: " + mBirthday);
            birthdayCursor.close();
        } else {
            Log.w(DEBUG_TAG_QUERY, "WARNING: birthdayCursor cannot move. Contact: " + contactId);
            birthdayCursor.close();
        }

        /*
        NAME SPLITTING
         */

        if (mFullName == null) {
            Log.e(DEBUG_TAG_QUERY, "ERROR: Didn't find user name. Contact ID: " + contactId);
            return;
        }
        mNameParts = mFullName.split(" ");

        // If the splitting didn't result in anything, just use the full name as one name part.
        if (mNameParts.length == 0) mNameParts = new String[] {mFullName};
    }

    /*
    PARCELABLE INTERFACE IMPLEMENTATION
     */

    private static final String TRUE_STRING = "TRUE";

    public Contact(Parcel in){
        String[] data = new String[9];

        in.readStringArray(data);
        mContactUri = Uri.parse(data[0]);
        mFullName = data[1];
        mPhoneNumber = data[2];
        mEmailAddress = data[3];
        mBirthday = data[4];
        mTimesContacted = data[5];
        mRetryFactor = Integer.parseInt(data[6]);
        mMd5IsNew = (data[7].equals(TRUE_STRING));
        mMd5String = data[8];
        mHasPhoto = (data[9].equals(TRUE_STRING));
    }

    //TODO: Figure out useful usage of describeContents & flags.

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String md5IsNewString = "FALSE";
        String hasPhotoString = "FALSE";
        if (mMd5IsNew) md5IsNewString = TRUE_STRING;
        if (mHasPhoto) hasPhotoString = TRUE_STRING;

        String[] dataStrings = new String[] {
                mContactUri.toString(),
                mFullName,
                mPhoneNumber,
                mEmailAddress,
                mBirthday,
                mTimesContacted,
                mRetryFactor + "",
                md5IsNewString,
                mMd5String,
                hasPhotoString
        };

        dest.writeStringArray(dataStrings);
    }

    /*
    GETTER / SETTER
     */

    /**
     *
     * @param partNumber Array index
     * @return A part of the contact's name
     */
    public String getNamePart(int partNumber) {
        if (mNameParts == null) {
            Log.e(DEBUG_TAG, "ERROR: Array of name parts is null.");
            return "";
        }

        if (partNumber < mNameParts.length) {
            Log.d(DEBUG_TAG, "Returning name part " + partNumber + " " + mNameParts[partNumber]);
            return mNameParts[partNumber];
        } else {
            Log.e(DEBUG_TAG, "ERROR: Name does not contain part number " + partNumber + ".");
            return "";
        }
    }

    /**
     *
     * @return The number of all words/name parts in the contact's name
     */
    public int getNumberOfNameParts() {
        if (mNameParts == null) return 0;
        else return mNameParts.length;
    }

    /**
     * @return The name string of this contact instance.
     */
    public String getFullName() {
        return mFullName;
    }

    /**
     * @return The MD5 encoded value as a String.
     */
    public String getMD5EncryptedString() {
        // If no other contact attribute changed, don't re-calculate the MD5 value.
        if (!mMd5IsNew) return mMd5String;

        String combinedString = mFullName + mEmailAddress + mPhoneNumber + mBirthday
                + mTimesContacted + mRetryFactor;


        try {
            // Initialise and perform MD5 encryption.
            MessageDigest dEnc = MessageDigest.getInstance("MD5");
            byte[] combinedBytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                combinedBytes = combinedString.getBytes(Charset.forName("ISO-8859-1"));
            } else {
                //TODO: Issue: This getBytes stops at special characters.
                combinedString = combinedString.replaceAll("[^\\x00-\\x7F]", "_");
                combinedBytes = combinedString.getBytes();
            }
            Log.d(DEBUG_TAG, "Generating new MD5 from " + combinedString);

            // MD5-Encryption:
            dEnc.update(combinedBytes, 0, combinedString.length());
            String md5EncString = new BigInteger(1, dEnc.digest()).toString( 16 );

            if (md5EncString.length() >= 32) mMd5String = md5EncString;
            else mMd5String = md5EncString + mFullName;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Now the new MD5 value is returned, so it is no longer new.
        mMd5IsNew = false;
        return mMd5String;
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param generatedBitmap This will be the new contact image.
     * @return TRUE if assignment was successful.
     */
    public boolean assignImage( Bitmap generatedBitmap ) {
        Uri rawContactUri = null;
        int photoRow = -1;
        int index;

        Cursor rawContactCursor =  mContext.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + " = " + mContactUri.getLastPathSegment(),
                null,
                null);

        if( rawContactCursor != null ) {
            if( !rawContactCursor.isAfterLast() ) {
                rawContactCursor.moveToFirst();
                rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendPath(
                        "" + rawContactCursor.getLong(0) ).build();
            }
            rawContactCursor.close();
        } else return false;

        // Create a byte stream from the generated image.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        generatedBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] photo = outputStream.toByteArray();

        // Set the byte array as the raw contact's photo.
        ContentValues values = new ContentValues();

        if( rawContactUri != null ) {
            String strQuery = ContactsContract.Data.RAW_CONTACT_ID + " == " +
                    ContentUris.parseId( rawContactUri ) + " AND " +
                    ContactsContract.RawContacts.Data.MIMETYPE + "=='" +
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";

            Cursor changePhotoCursor = mContext.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    strQuery,
                    null,
                    null );

            if( changePhotoCursor != null ) {
                index = changePhotoCursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
                if( changePhotoCursor.moveToFirst() )
                    photoRow = changePhotoCursor.getInt( index );
                changePhotoCursor.close();
            } else return false;

            values.put(ContactsContract.Data.RAW_CONTACT_ID,
                    ContentUris.parseId(rawContactUri));
            values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
            values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo);
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

        } else return false;


        if( photoRow >= 0 ){
            mContext.getContentResolver().update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    ContactsContract.Data._ID + " = " + photoRow, null );
        } else {
            mContext.getContentResolver().insert(
                    ContactsContract.Data.CONTENT_URI,
                    values );
        }

        return true;
    }

    /**
     * Alter the retry factor, so the next MD5 string will change significantly.
     */
    public void modifyRetryFactor() {
        mMd5IsNew = true;
        mRetryFactor++;
    }
}
