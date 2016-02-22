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

package org.eztarget.micopi;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class queries and stores contact data from a given intent.
 * <p/>
 * Created by Michel on 03.02.14.
 */
public class Contact implements Parcelable {
    /**
     * General log tag for Contact class
     */
    private final static String TAG = Contact.class.getSimpleName();

    private long mId;

    private String[] mNameParts;

    private Uri mPhotoUri;

    private String mFullName = "";

    private int mNumOfLetters = 1;

    private String mPhoneNumber = "555";

    private String mEmailAddress = "NE";

    private String mBirthday = "NB";

    private int mRetryFactor = 0;

    private String mMd5String = null;

    public Contact(
            final long id,
            final String fullName,
            final String phoneNumber,
            final String emailAddress,
            final String birthday,
            final Uri photoUri
    ) {
        mId = id;
        setName(fullName);
        mPhoneNumber = phoneNumber;
        mEmailAddress = emailAddress;
        mBirthday = birthday;
        mPhotoUri = photoUri;
    }

    public String toString() {
        return mId + ": " + mFullName;
    }

    /*
    PARCELABLE INTERFACE IMPLEMENTATION
     */

    public Contact(Parcel in) {
        String[] data = new String[8];

        in.readStringArray(data);

        mId = Long.parseLong(data[0]);
        mFullName = data[1];
        mPhoneNumber = data[2];
        mEmailAddress = data[3];
        mBirthday = data[4];
        mRetryFactor = Integer.parseInt(data[5]);
        mMd5String = data[6];
//        mPhotoUri = Uri.parse(data[7]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        final String[] dataStrings = new String[]{
                String.valueOf(mId),
                mFullName,
                mPhoneNumber,
                mEmailAddress,
                mBirthday,
                mRetryFactor + "",
                mMd5String,
                null
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

    public long getId() {
        return mId;
    }

    public void setName(final String name) {
        if (TextUtils.isEmpty(name)) {
            mFullName = " ";
            mNameParts = new String[]{" "};
            mNumOfLetters = 1;
            return;
        }

        mFullName = name;

        mNameParts = mFullName.split(" ");

        // Count all the letters in the name that are not spaces.
        mNumOfLetters = 0;
        for (char c : mFullName.toCharArray()) {
            if (c != ' ') mNumOfLetters++;
        }

        // If the splitting didn't result in anything, just use the full name as one name part.
        if (mNameParts.length == 0) mNameParts = new String[]{mFullName};
    }

    /**
     * Returns a certain word from the name
     *
     * @param index Array index
     * @return A part of the contact's name
     */
    public String getNameWord(final int index) {
        if (mNameParts == null) {
            Log.v(TAG, toString() + ": Array of name parts is null.");
            return getFullName();
        }

        if (index < mNameParts.length) {
            return mNameParts[index];
        } else {
            Log.e(TAG, "Name does not contain part number " + index + ".");
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
        if (mMd5String != null) return mMd5String;

        final String combinedInfo;
        combinedInfo = mFullName + mEmailAddress + mPhoneNumber + mBirthday + mRetryFactor;

        try {
            // Initialise and perform MD5 encryption.
            final MessageDigest fDigest = MessageDigest.getInstance("MD5");
            byte[] combinedBytes = combinedInfo.getBytes(Charset.forName("ISO-8859-1"));
//            Log.d(TAG, "Generating new MD5 from " + combinedInfo);

            // MD5-Encryption:
            fDigest.update(combinedBytes, 0, combinedInfo.length());
            mMd5String = new BigInteger(1, fDigest.digest()).toString(16);

            while (mMd5String.length() < 32) mMd5String += mFullName;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Now the new MD5 value is returned, so it is no longer new.
        return mMd5String;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    /**
     * Definition of step sized to be used by modifyRetryFactor()
     */
    private static final int RETRY_STEP = 9;

    /**
     * Alters the retry factor, so the next MD5 string will change significantly
     * Can be used to get back to last picture by setting forward boolean to false
     *
     * @param doMoveForward If true, increase x steps. If false, decrease x steps.
     */
    public void modifyRetryFactor(final boolean doMoveForward) {
        mMd5String = null;
        if (doMoveForward) mRetryFactor += RETRY_STEP;
        else mRetryFactor -= RETRY_STEP;
    }

    public String getFileName() {
        return mId + "___" + mFullName
                .replace(" - ", "-")
                .replace(' ', '_')
                .replace('\'', '_')
                .replace('\"', '_')
                .replace('/', '-')
                .replace('.', '_') + ".png";
    }
}
