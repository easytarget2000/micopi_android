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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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
public class Contact {
    private final static String DEBUG_TAG = "Contact";

    private Context mContext;
    private Uri mContactUri = null;
    private String[] mNameParts;
    private String mFullName = "";
    private String mPhoneNumber = "555";
    private String mEmailAddress = "NE";
    private String mBirthday = "NB";
    private String mTimesContacted = "0";
    private int mRetryFactor = 0;
    private boolean mMd5IsNew = true;
    private String mMd5String = "000000000000000000000000000";

    /**
     * Constructor Method
     *
     * @param c Context
     * @param data Data from people list (device contacts)
     */
    public Contact(Context c, Intent data) {
        this.mContext = c;
        this.mMd5IsNew = true;
        String contactId, query;
        int iContactNameIdx, iContactPhoneNumberIdx, iContactTimesContactedIdx, iContactEmailIdx;
        Cursor contactCursor;

        // The received data contains the URI of the chosen contact.
        mContactUri = data.getData();
        // The last part of the URI contains the contact's ID.
        if( mContactUri != null ) contactId = mContactUri.getLastPathSegment();
        else return;

        // Point the cursor at the DB query.
        contactCursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                new String[] { contactId },
                null
        );

        if( contactCursor == null ) return;

        // START - QUERIES

        // Attempt to move the cursor to the first entry with a phone number.
        if( contactCursor.moveToFirst() ) {
            // Get the indices of the name and phone number.
            iContactNameIdx = contactCursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME );
            iContactPhoneNumberIdx = contactCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DATA);
            iContactTimesContactedIdx = contactCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED
            );
            mFullName = contactCursor.getString( iContactNameIdx );
            mPhoneNumber = contactCursor.getString( iContactPhoneNumberIdx );
            mTimesContacted = contactCursor.getString(
                    iContactTimesContactedIdx
            );
        } else {
            // If the contact does not have a phone number,
            // perform a different query.

            // Point the cursor at the DB query.
            contactCursor = mContext.getContentResolver().query(
                    mContactUri, null, null, null, null
            );

            // Get the index of the display name.
            if( contactCursor != null ) {
                iContactNameIdx = contactCursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                );

                // Attempt to move the cursor to the first entry of the query cursor.
                if( contactCursor.moveToFirst() )
                    mFullName = contactCursor.getString( iContactNameIdx );
            }
            else return;

        }

        /*
         * E-Mail Address
         */
        contactCursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[] { contactId },
                null
        );

        if( contactCursor != null ) {
            if( contactCursor.moveToFirst() ) {
                iContactEmailIdx = contactCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Email.DATA );
                mEmailAddress = contactCursor.getString( iContactEmailIdx );
            }
        }

        /*
         * Birthday
         */
        String mColumns[] = {
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.MIMETYPE,
        };

        query = ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + " and "+
                ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" +
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " +
                ContactsContract.Data.CONTACT_ID + " = " + contactId;

        contactCursor = mContext.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                mColumns,
                query,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        );

        if (contactCursor != null) {
            if (contactCursor.moveToNext()) {
                mBirthday = contactCursor.getString(
                        contactCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Event.START_DATE
                        )
                );
            }
        }

        // Split the name into its parts.
        mNameParts = mFullName.split(" ");

        // If the splitting didn't result in anything, just use the full name as one name part.
        if (mNameParts.length == 0) {
            mNameParts = new String[1];
            mNameParts[0] = mFullName;
        }
    }

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

        String combinedString = mFullName + mEmailAddress + mPhoneNumber
                + mBirthday + mTimesContacted + mRetryFactor;


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

//            for (byte b : combinedBytes) {
//                Log.d(DEBUG_TAG, "Byte: " + (char) b);
//            }

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
