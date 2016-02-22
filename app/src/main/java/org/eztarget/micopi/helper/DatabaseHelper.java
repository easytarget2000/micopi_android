package org.eztarget.micopi.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import org.eztarget.micopi.Contact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by michel on 14/02/16.
 *
 */
public class DatabaseHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    /**
     * Constructs a contact by getting the contact ID from the Intent data
     */
    public static Contact buildContact(final ContentResolver contentResolver, final Intent intent) {
        // The received data contains the URI of the chosen contact.
        // The last part of the URI contains the contact's ID.

        return buildContact(contentResolver, Long.parseLong(intent.getData().getLastPathSegment()));
    }

    /**
     * Constructs a contact by querying the database for the given ID
     */
    public static Contact buildContact(
            final ContentResolver contentResolver,
            final long contactSqlId
    ) {

        final String[] contactsProjection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
        };

        final Cursor contactsCursor;
        try {
            contactsCursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    contactsProjection,
                    ContactsContract.Contacts._ID + "=?",
                    new String[]{String.valueOf(contactSqlId)},
                    null
            );
        } catch (SecurityException e) {
            Log.e(TAG, e.toString());
            return null;
        }

        if (contactsCursor == null) {
            Log.e(TAG, "ERROR: contactsCursor is null. Contact: " + contactSqlId);
            return null;
        }

        if (!contactsCursor.moveToFirst()) {
            Log.e(TAG, "ERROR: Cannot move contactsCursor. Contact: " + contactSqlId);
            contactsCursor.close();
            return null;
        }

        // Get the indices of the name and phone number.
        final String fullName = contactsCursor.getString(
                contactsCursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                )
        );

        final int hasPhoneNumber = contactsCursor.getInt(contactsCursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER
                )
        );

        contactsCursor.close();

        return new Contact(
                contactSqlId,
                fullName,
                hasPhoneNumber == 1 ? getPhoneNumber(contentResolver, contactSqlId) : "047",
                getEmailAddress(contentResolver, contactSqlId),
                getBirthday(contentResolver, contactSqlId),
                null
        );
    }

    private static String getPhoneNumber(final ContentResolver contentResolver, final long id) {

        String phoneNumber;

        // Query the phone DB only for the contact's phone number.
        final Cursor phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                null,
                null
        );

        if (phoneCursor == null) {
            // Something went wrong. The cursor is null after querying.
            Log.w(TAG, "WARNING: phoneCursor is null. Contact: " + id);
            phoneNumber = "1234567890";
        } else if (phoneCursor.moveToFirst()) {
            // The cursor moved into the first entry.
            // Get the phone number from the DATA/NUMBER column.
            final int phoneNumberIndex = phoneCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            );
            phoneNumber = phoneCursor.getString(phoneNumberIndex);
            phoneCursor.close();
        } else {
            // Something went wrong. The cursor is not null but cannot go into the first entry.
            phoneNumber = "1234567891";
            phoneCursor.close();
        }

        return phoneNumber;
    }

    private static String getEmailAddress(final ContentResolver contentResolver, final long id) {

        final Cursor emailCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.DATA},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id,
                null,
                null
        );

        String emailAddress = null;

        if (emailCursor == null) {
            Log.w(TAG, "WARNING: emailCursor is null. Contact: " + id);
        } else if (emailCursor.moveToFirst()) {
            final int emailIndex = emailCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.DATA
            );
            emailAddress = emailCursor.getString(emailIndex);

            emailCursor.close();
        } else {
            emailCursor.close();
        }

        return emailAddress;
    }

    private static String getBirthday(final ContentResolver contentResolver, final long id) {

        final String birthdayProjection[] = {
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.MIMETYPE,
        };

        final String birthdayQuery = ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + " and " +
                ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" +
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " +
                ContactsContract.Data.CONTACT_ID + " = " + id;

        final Cursor birthdayCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                birthdayProjection,
                birthdayQuery,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        );

        String birthday = null;

        if (birthdayCursor == null) {
            Log.w(TAG, "WARNING: birthdayCursor is null. Contact: " + id);
        } else if (birthdayCursor.moveToNext()) {
            final int birthdayIndex = birthdayCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Event.START_DATE
            );
            birthday = birthdayCursor.getString(birthdayIndex);

            birthdayCursor.close();
        } else {
            birthdayCursor.close();
        }

        return birthday;
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     */
    public static boolean assignImageToContact(
            final Context context,
            final Bitmap hiResBitmap,
            final Contact contact
    ) {
        final ContentResolver contentResolver = context.getContentResolver();

        final Cursor rawContactCursor = contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + "=" + contact.getId(),
                null,
                null
        );

        if (rawContactCursor == null) {
            Log.e(TAG, "rawContactCursor is null.");
            return false;
        }

        final Uri rawContactUri;

        if (rawContactCursor.moveToFirst()) {
            final Uri contentUri = ContactsContract.RawContacts.CONTENT_URI;
            final String rawPath = "" + rawContactCursor.getLong(0);
            rawContactUri = contentUri.buildUpon().appendPath(rawPath).build();
        } else {
            rawContactCursor.close();
            return false;
        }
        rawContactCursor.close();

        final String photoSelection = ContactsContract.Data.RAW_CONTACT_ID + "=="
                + ContentUris.parseId(rawContactUri)
                + " AND "
                + ContactsContract.RawContacts.Data.MIMETYPE + "=='"
                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";

        final Cursor existingPhotoCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                null,
                photoSelection,
                null,
                null
        );

        int photoId = -1;

        if (existingPhotoCursor != null) {
            final int index = existingPhotoCursor.getColumnIndex(ContactsContract.Data._ID);
            if (index > 0 && existingPhotoCursor.moveToFirst()) {
                photoId = existingPhotoCursor.getInt(index);
            }
            existingPhotoCursor.close();
        }

        final ContentValues values = new ContentValues();
        values.put(
                ContactsContract.Data.RAW_CONTACT_ID,
                ContentUris.parseId(rawContactUri)
        );
        values.put(
                ContactsContract.Data.IS_SUPER_PRIMARY,
                1
        );

        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                hiResBitmap,
                256,
                256,
                true
        );
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        values.put(
                ContactsContract.CommonDataKinds.Photo.PHOTO,
                outputStream.toByteArray()
        );

        values.put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
        );

        if (photoId >= 0) {
            contentResolver.update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    ContactsContract.Data._ID + "=" + photoId,
                    null
            );
        } else {
            contentResolver.insert(
                    ContactsContract.Data.CONTENT_URI,
                    values
            );
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        overwriteHiResPhoto(contentResolver, rawContactUri, hiResBitmap);

        return true;
    }

    private static void overwriteHiResPhoto(
            final ContentResolver contentResolver,
            final Uri contactUri,
            final Bitmap hiResBitmap
    ) {

        final Uri displayPhotoUri;
        displayPhotoUri = Uri.withAppendedPath(
                contactUri,
                ContactsContract.Contacts.Photo.DISPLAY_PHOTO
        );
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = contentResolver.openAssetFileDescriptor(displayPhotoUri, "w");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (descriptor != null) {
            OutputStream os;
            try {
                os = descriptor.createOutputStream();
                hiResBitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
