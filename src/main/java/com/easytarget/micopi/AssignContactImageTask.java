package com.easytarget.micopi;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by michel@easy-target.org on 08/01/15.
 *
 * Assigns the bitmap to the contact
 */
public class AssignContactImageTask extends AsyncTask<String, Void, Boolean> {

    private static final String LOG_TAG = AssignContactImageTask.class.getSimpleName();

    private Context mAppContext;

    public AssignContactImageTask(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (mAppContext == null) return false;

        final String contactId = params[0];
        if (TextUtils.isEmpty(contactId)) return false;

        if (params.length != 2) return false;
        final String imageFile = params[1];
        if (TextUtils.isEmpty(imageFile)) return false;
        final File tempFile = new File(imageFile);

        return assignImage(tempFile, contactId);
    }

    @Override
    protected void onPostExecute(Boolean didSuccessfully) {
        Intent finishBroadcast = new Intent(Constants.ACTION_FINISHED_ASSIGN);
        finishBroadcast.putExtra(Constants.EXTRA_SUCCESS, didSuccessfully);
        mAppContext.sendBroadcast(finishBroadcast);
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @return TRUE if assignment was successful.
     */
    public boolean assignImage(final File imageFile, final String contactId) {
        if (mAppContext == null) return false;

        final Cursor rawContactCursor = mAppContext.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + " = " + contactId,
                null,
                null
        );

        Uri rawContactUri = null;

        if(rawContactCursor != null) {
            if(rawContactCursor.moveToFirst()) {
                final Uri contentUri = ContactsContract.RawContacts.CONTENT_URI;
                final String rawPath = "" + rawContactCursor.getLong(0);
                rawContactUri = contentUri.buildUpon().appendPath(rawPath).build();
            }
            rawContactCursor.close();
        } else {
            Log.e(LOG_TAG, "ERROR: rawContactCursor is null.");
            return false;
        }

        // Create a byte stream from the generated image.
//        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageFile);
        byte[] photo = new byte[(int) imageFile.length()];
        try {
            new FileInputStream(imageFile).read(photo);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        final byte[] photo = outputStream.toByteArray();

        // Set the byte array as the raw contact's photo.
        final ContentValues values = new ContentValues();
        int photoRow = -1;

        if(rawContactUri != null) {
            Log.d(LOG_TAG,
                    "parseId(): " + ContentUris.parseId(rawContactUri)
                            + " rawContactUri: " + rawContactUri.toString()
                            + " contact ID: " + contactId);

            final String photoSelection = ContactsContract.Data.RAW_CONTACT_ID + "=="
                            + ContentUris.parseId(rawContactUri)
                            + " AND "
                            + ContactsContract.RawContacts.Data.MIMETYPE + "=='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";

            final Cursor changePhotoCursor = mAppContext.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    photoSelection,
                    null,
                    null
            );

            if(changePhotoCursor != null) {
                final int index = changePhotoCursor.getColumnIndex(ContactsContract.Data._ID);
                if(index > 0 && changePhotoCursor.moveToFirst()) {
                    photoRow = changePhotoCursor.getInt(index);
                }
                changePhotoCursor.close();
            } else {
                Log.e(LOG_TAG, "ERROR: changePhotoCursor is null.");
                return false;
            }

            values.put(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    ContentUris.parseId(rawContactUri)
            );
            values.put(
                    ContactsContract.Data.IS_SUPER_PRIMARY,
                    1
            );
            values.put(
                    ContactsContract.CommonDataKinds.Photo.PHOTO,
                    photo
            );
            values.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            );
        } else {
            Log.e(LOG_TAG, "ERROR: rawContactUri is null.");
            return false;
        }

        if(photoRow >= 0){
            mAppContext.getContentResolver().update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    ContactsContract.Data._ID + " = " + photoRow,
                    null
            );
        } else {
            Log.i(LOG_TAG, "INFO: photoRow: " + photoRow);
            mAppContext.getContentResolver().insert(
                    ContactsContract.Data.CONTENT_URI,
                    values
            );
        }

        imageFile.delete();
        return true;
    }
}
