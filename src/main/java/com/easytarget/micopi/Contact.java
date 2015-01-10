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
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

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
    /**
     * General log tag for Contact class
     */
    private final static String DEBUG_TAG = Contact.class.getSimpleName();

    private String mContactId;

    private String[] mNameParts;

    private boolean mHasPhoto = false;

    private String mFullName = "";

    private int mNumOfLetters = 1;

    private String mPhoneNumber = "555";

    private String mEmailAddress = "NE";

    private String mBirthday = "NB";

    private String mTimesContacted = "0";

    private int mRetryFactor = 0;

    private boolean mMd5IsNew = true;

    private String mMd5String = "000000000000000000000000001";

    private static final String DEBUG_TAG_QUERY = "Contact Query";

    /**
     * Constructs a contact by getting the contact ID from the Intent data
     *
     * @param context Context used to get ContentResolver
     * @param intent Contains picked contact URI
     */
    public Contact(Context context, final Intent intent) {
        // The received data contains the URI of the chosen contact.
        // The last part of the URI contains the contact's ID.
        this(context, intent.getData().getLastPathSegment());
    }

    public Contact(Context context, final long contactId) {
        this(context, contactId + "");
    }

    /**
     * Constructs a contact by querying the database for the given ID
     *
     * @param context Context used to get ContentResolver
     * @param contactSqlId Picked contact ID
     */
    public Contact(Context context, final String contactSqlId) {
        if (context == null || TextUtils.isEmpty(contactSqlId)) return;

        mMd5IsNew = true;
        mContactId = contactSqlId;

        /*
        CONTACTS DB QUERY
         */

        // Define which columns we want to query.
        final String[] contactsProjection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.TIMES_CONTACTED,
                ContactsContract.Contacts.PHOTO_ID
        };

        // Point the cursor at the DB query.
        final Cursor contactsCursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                contactsProjection,
                ContactsContract.Contacts._ID + "=?",
                new String[] {contactSqlId},
                null
        );

        if(contactsCursor == null) {
            Log.e(DEBUG_TAG_QUERY, "ERROR: contactsCursor is null. Contact: " + contactSqlId);
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
//            Log.i(DEBUG_TAG_QUERY, "Display Name: " + mFullName);

            hasPhoneNumber = contactsCursor.getInt(hasNumberIndex);
//            Log.i(DEBUG_TAG_QUERY, "Has Phone Number: " + hasPhoneNumber);

            mTimesContacted = contactsCursor.getString(timesContactedIndex);
//            Log.i(DEBUG_TAG_QUERY, "TimesContacted: " + mTimesContacted);

            final int photoId = contactsCursor.getInt(photoIdIndex);
//            Log.i(DEBUG_TAG_QUERY, "PhotoId: " + photoId);
            if (photoId > 0) mHasPhoto = true;
            contactsCursor.close();
        } else {
            Log.e(DEBUG_TAG_QUERY, "ERROR: Cannot move contactsCursor. Contact: " + contactSqlId);
            contactsCursor.close();
            return;
        }

        /*
        PHONE DB QUERY
         */

        // If the contacts DB query said that this contact has a phone number,
        // the Phone DB will now be queried.
        if (hasPhoneNumber == 1) {
            // Query the phone DB only for the contact's phone number.
            final Cursor phoneCursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[] {contactSqlId},
                    null
            );

            if (phoneCursor == null) {
                // Something went wrong. The cursor is null after querying.
                Log.w(DEBUG_TAG_QUERY, "WARNING: phoneCursor is null. Contact: " + contactSqlId);
                mPhoneNumber = "1234567890";
            } else if (phoneCursor.moveToFirst()) {
                // The cursor moved into the first entry.
                // Get the phone number from the DATA/NUMBER column.
                final int phoneNumberIndex = phoneCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                );
                mPhoneNumber = phoneCursor.getString(phoneNumberIndex);
//                Log.i(DEBUG_TAG_QUERY, "Display Name: " + mPhoneNumber);

                phoneCursor.close();
            } else {
                // Something went wrong. The cursor is not null but cannot go into the first entry.
//                Log.w(DEBUG_TAG_QUERY, "WARNING: phoneCursor cannot move. Contact: " + contactSqlId);
                mPhoneNumber = "1234567891";
                phoneCursor.close();
            }
        }

        /*
         E-MAIL DB QUERY
         */

        final Cursor emailCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] {ContactsContract.CommonDataKinds.Email.DATA},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[] {contactSqlId},
                null
        );

        if(emailCursor == null) {
            Log.w(DEBUG_TAG_QUERY, "WARNING: emailCursor is null. Contact: " + contactSqlId);
        } else if(emailCursor.moveToFirst()) {
            final int emailIndex = emailCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.DATA
            );
            mEmailAddress = emailCursor.getString(emailIndex);

//            Log.i(DEBUG_TAG_QUERY, "E-Mail Address: " + mEmailAddress);
            emailCursor.close();
        } else {
//            Log.w(DEBUG_TAG_QUERY, "WARNING: emailCursor cannot move. Contact: " + contactSqlId);
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
                ContactsContract.Data.CONTACT_ID + " = " + contactSqlId;

        final Cursor birthdayCursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                birthdayProjection,
                birthdayQuery,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        );

        // If the cursor found something, get START_DATE of the query result.
        if (birthdayCursor == null) {
            Log.w(DEBUG_TAG_QUERY, "WARNING: birthdayCursor is null. Contact: " + contactSqlId);
        } else if (birthdayCursor.moveToNext()) {
            final int birthdayIndex = birthdayCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Event.START_DATE
            );
            mBirthday = birthdayCursor.getString(birthdayIndex);

//            Log.i(DEBUG_TAG_QUERY, "Birthday: " + mBirthday);
            birthdayCursor.close();
        } else {
//            Log.w(DEBUG_TAG_QUERY, "WARNING: birthdayCursor cannot move. Contact: " + contactSqlId);
            birthdayCursor.close();
        }

        /*
        NAME SPLITTING
         */

        if (mFullName == null) {
            Log.e(DEBUG_TAG_QUERY, "Didn't find user name. Contact ID: " + contactSqlId);
            return;
        }
        mNameParts = mFullName.split(" ");

        // Count all the letters in the name that are not spaces.
        mNumOfLetters = 0;
        for (char c : mFullName.toCharArray()) {
            if (c != ' ') mNumOfLetters++;
        }

        // If the splitting didn't result in anything, just use the full name as one name part.
        if (mNameParts.length == 0) mNameParts = new String[] {mFullName};
    }

    public String toString() {
        return mContactId + ": " + mFullName;
    }

    /*
    PARCELABLE INTERFACE IMPLEMENTATION
     */

    /**
     * Definition for parceling of boolean values
     */
    private static final String TRUE_STRING = "TRUE";

    public Contact(Parcel in){
        String[] data = new String[9];

        in.readStringArray(data);
        mContactId = data[0];
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
                mContactId,
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

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    /*
    GETTER / SETTER
     */

    public String getId() {
        return mContactId;
    }

    /**
     * Returns a certain word from the name
     *
     * @param fWordIndex Array index
     * @return A part of the contact's name
     */
    public String getNameWord(final int fWordIndex) {
        if (mNameParts == null) {
            Log.v(DEBUG_TAG, toString() + ": Array of name parts is null.");
            return getFullName();
        }

        if (fWordIndex < mNameParts.length) {
//            Log.d(DEBUG_TAG, "Returning name part " + fWordIndex + " " + mNameParts[fWordIndex]);
            return mNameParts[fWordIndex];
        } else {
            Log.e(DEBUG_TAG, "Name does not contain part number " + fWordIndex + ".");
            return getFullName();
        }
    }

    /**
     * @return The number of all words/name parts in the contact's name
     */
    public int getNumberOfNameWords() {
        if (mNameParts == null) return 0;
        else return mNameParts.length;
    }

    /**
     * @return The name of this contact
     */
    public String getFullName() {
        if (mFullName == null) {
            if (mPhoneNumber != null) return mPhoneNumber;
            else return "Unknown";
        } else {
            if (mFullName.length() > 0) return mFullName;
            else return "Unknown";
        }
    }

    /**
     * Should be used instead of getFullName().length()
     *
     * @return The number of actual letters, spaces excluded
     */
    public int getNumberOfLetters() {
        return mNumOfLetters;
    }

    /**
     * @return The MD5 encoded value as a String.
     */
    public String getMD5EncryptedString() {
        // If no other contact attribute changed, don't re-calculate the MD5 value.
        if (!mMd5IsNew) return mMd5String;

        String combinedInfo = mFullName + mEmailAddress + mPhoneNumber + mBirthday
                + mTimesContacted + mRetryFactor;


        try {
            // Initialise and perform MD5 encryption.
            final MessageDigest fDigest = MessageDigest.getInstance("MD5");
            byte[] combinedBytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                combinedBytes = combinedInfo.getBytes(Charset.forName("ISO-8859-1"));
            } else {
                combinedInfo = combinedInfo.replaceAll("[^\\x00-\\x7F]", "_");
                combinedBytes = combinedInfo.getBytes();
            }
//            Log.d(DEBUG_TAG, "Generating new MD5 from " + combinedInfo);

            // MD5-Encryption:
            fDigest.update(combinedBytes, 0, combinedInfo.length());
            mMd5String = new BigInteger(1, fDigest.digest()).toString(16);

            while (mMd5String.length() < 32) mMd5String += mFullName;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Now the new MD5 value is returned, so it is no longer new.
        mMd5IsNew = false;
        return mMd5String;
    }

    /**
     * Log tag for assignTempFileToContact() method
     */
    private static final String DEBUG_TAG_ASSIGN = "assignTempFileToContact()";


    /**
     * Definition of step sized to be used by modifyRetryFactor()
     */
    private static final int RETRY_STEP = 9;

    /**
     * Alters the retry factor, so the next MD5 string will change significantly
     * Can be used to get back to last picture by setting forward boolean to false
     *
     * @param fDoMoveForward If true, increase x steps. If false, decrease x steps.
     */
    public void modifyRetryFactor(final boolean fDoMoveForward) {
        mMd5IsNew = true;
        if (fDoMoveForward) mRetryFactor += RETRY_STEP;
        else mRetryFactor -= RETRY_STEP;
    }

}
