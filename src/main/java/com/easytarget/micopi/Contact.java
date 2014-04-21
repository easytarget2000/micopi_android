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
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * a
 *
 * Created by Michel on 03.02.14.
 *
 */
public class Contact {
    private Context mContext;
    private Uri mContactUri = null;
    private String mContactName = "";
    private String mPhoneNumber = "555";
    private String mEmailAddress = "NE";
    private String mBirthday = "NB";
    private String mTimesContacted = "0";
    private int mRetryFactor = 0;

    public Contact( Context c, Intent data ) {
        this.mContext = c;
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
            mContactName = contactCursor.getString( iContactNameIdx );
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
                    mContactName = contactCursor.getString( iContactNameIdx );
            }
            else return;

        }

        /**
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

        /**
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

        if ( contactCursor != null ) {
            if ( contactCursor.moveToNext() ) {
                mBirthday = contactCursor.getString(
                        contactCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Event.START_DATE
                        )
                );
            }
        }

        // END - QUERIES

        /**
         * Combine all queried data.
         */


    }

    public String getName() {
        return mContactName;
    }

    /**
     * @return The MD5 encoded value as a String.
     */
    public String getMD5EncryptedString() {
        String combinedData = mContactName + mEmailAddress + mPhoneNumber
                + mBirthday + mTimesContacted + mRetryFactor;

        Log.d("combinedData:", combinedData);

        try {
            // Initialise and perform MD5 encyption.
            MessageDigest dEnc = MessageDigest.getInstance( "MD5" );
            dEnc.update( combinedData.getBytes(), 0, combinedData.length() );
            // Convert to String.
            return new BigInteger( 1, dEnc.digest() ).toString( 16 ) ;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
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

    public void modifyRetryFactor() {
        mRetryFactor ++;
    }
}
