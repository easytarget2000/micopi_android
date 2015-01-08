package com.easytarget.micopi;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by michel on 08/01/15.
 *
 */
public class AssignContactImageService extends IntentService {

    private static final String LOG_TAG = AssignContactImageService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AssignContactImageService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @return TRUE if assignment was successful.
     */
//    public boolean assignImage(final String contactId) {
//        if (mAppContext == null || mBitmap == null) return false;
//
//        final Cursor rawContactCursor = mAppContext.getContentResolver().query(
//                ContactsContract.RawContacts.CONTENT_URI,
//                new String[]{ContactsContract.RawContacts._ID},
//                ContactsContract.RawContacts.CONTACT_ID + " = " + contactId,
//                null,
//                null
//        );
//
//        Uri rawContactUri = null;
//
//        if(rawContactCursor != null) {
//            if(rawContactCursor.moveToFirst()) {
//                final Uri contentUri = ContactsContract.RawContacts.CONTENT_URI;
//                final String rawPath = "" + rawContactCursor.getLong(0);
//                rawContactUri = contentUri.buildUpon().appendPath(rawPath).build();
//            }
//            rawContactCursor.close();
//        } else {
//            Log.e(LOG_TAG, "ERROR: rawContactCursor is null.");
//            return false;
//        }
//
//        // Create a byte stream from the generated image.
//        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        mBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
//        final byte[] photo = outputStream.toByteArray();
//
//        // Set the byte array as the raw contact's photo.
//        final ContentValues values = new ContentValues();
//        int photoRow = -1;
//
//        if(rawContactUri != null) {
//            Log.d(LOG_TAG,
//                    "parseId(): " + ContentUris.parseId(rawContactUri)
//                            + " rawContactUri: " + rawContactUri.toString()
//                            + " contact ID: " + contactId);
//
//            final String photoSelection = ContactsContract.Data.RAW_CONTACT_ID + " == " +
//                    ContentUris.parseId( rawContactUri ) + " AND " +
//                    ContactsContract.RawContacts.Data.MIMETYPE + "=='" +
//                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
//
//            final Cursor changePhotoCursor = mAppContext.getContentResolver().query(
//                    ContactsContract.Data.CONTENT_URI,
//                    null,
//                    photoSelection,
//                    null,
//                    null
//            );
//
//            if(changePhotoCursor != null) {
//                final int index = changePhotoCursor.getColumnIndex(ContactsContract.Data._ID);
//                if(index > 0 && changePhotoCursor.moveToFirst()) {
//                    photoRow = changePhotoCursor.getInt(index);
//                }
//                changePhotoCursor.close();
//            } else {
//                Log.e(LOG_TAG, "ERROR: changePhotoCursor is null.");
//                return false;
//            }
//
//            values.put(
//                    ContactsContract.Data.RAW_CONTACT_ID,
//                    ContentUris.parseId(rawContactUri)
//            );
//            values.put(
//                    ContactsContract.Data.IS_SUPER_PRIMARY,
//                    1
//            );
//            values.put(
//                    ContactsContract.CommonDataKinds.Photo.PHOTO,
//                    photo
//            );
//            values.put(
//                    ContactsContract.Data.MIMETYPE,
//                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
//            );
//        } else {
//            Log.e(LOG_TAG, "ERROR: rawContactUri is null.");
//            return false;
//        }
//
//        if(photoRow >= 0){
//            mAppContext.getContentResolver().update(
//                    ContactsContract.Data.CONTENT_URI,
//                    values,
//                    ContactsContract.Data._ID + " = " + photoRow,
//                    null
//            );
//        } else {
//            Log.i(LOG_TAG, "INFO: photoRow: " + photoRow);
//            mAppContext.getContentResolver().insert(
//                    ContactsContract.Data.CONTENT_URI,
//                    values
//            );
//        }
//
//        return true;
//    }
}
