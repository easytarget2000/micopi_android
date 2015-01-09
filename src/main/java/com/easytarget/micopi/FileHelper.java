package com.easytarget.micopi;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by michel on 27/10/14.
 *
 */
public class FileHelper implements MediaScannerConnection.MediaScannerConnectionClient{

    private static final String LOG_TAG = FileHelper.class.getSimpleName();

    private MediaScannerConnection mConnection;

    private static Context mAppContext;

    private String mFileName;

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param context
     * @param tempImageFile
     * @param contactId
     * @return TRUE if assignment was successful.
     */
    public static boolean assignImage(Context context, File tempImageFile, final String contactId) {
        // Create a byte stream from the file.
        byte[] image = new byte[(int) tempImageFile.length()];
        try {
            new FileInputStream(tempImageFile).read(image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return assignImage(context, image, contactId);
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param context
     * @param bitmap
     * @param contactId
     * @return TRUE if assignment was successful.
     */
    public static boolean assignImage(Context context, final Bitmap bitmap, final String contactId) {
        // Create a byte stream from the generated image.
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        final byte[] image = outputStream.toByteArray();

        return assignImage(context, image, contactId);
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param context
     * @param image
     * @param contactId
     * @return TRUE if assignment was successful.
     */
    public static boolean assignImage(Context context, final byte[] image, final String contactId) {
        mAppContext = context.getApplicationContext();
        if (mAppContext == null || TextUtils.isEmpty(contactId)) return false;

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

        // Set the byte array as the raw contact's image.
        final ContentValues values = new ContentValues();
        int photoRow = -1;

        if(rawContactUri != null) {
//            Log.d(LOG_TAG,
//                    "parseId(): " + ContentUris.parseId(rawContactUri)
//                            + " rawContactUri: " + rawContactUri.toString()
//                            + " contact ID: " + contactId);

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
                    image
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

        return true;
    }

    /**
     * Saves the generated image to a file.
     */
    public String saveContactImageFile(
            Context context,
            @NonNull Bitmap bitmap,
            @NonNull final String name,
            final char appendix
    ) {
        mAppContext = context.getApplicationContext();
        if (mAppContext == null) return null;

        String strFileName = name.replace( ' ', '_' ) + "-" + appendix + ".png";

        // Files will be stored in the /sdcard/micopi dir.
        File micopiFolder = new File( Environment.getExternalStorageDirectory() + "/micopi/" );
        if( micopiFolder.mkdirs() ) Log.i("New directory created", micopiFolder.getPath());
        else Log.i( "New directory created", "false" );

        // The file name is "FirstName_LastName-x.png".
        File file = new File( micopiFolder.getAbsolutePath(), strFileName );
        FileOutputStream fileOutStream;

        try {
            fileOutStream = new FileOutputStream( file );
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutStream);
            fileOutStream.close();
            performMediaScan( file );
        } catch ( Exception e ) {
            e.printStackTrace();
            return "";
        }
        return strFileName;
    }

    /**
     * Makes the saved picture appear in Android's gallery.
     * @param file  Scan this file for media content
     */
    private void performMediaScan(@NonNull File file) {
        if (mAppContext == null) return;
        if (mConnection == null) return;

        mFileName = file.getAbsolutePath();
        mConnection = new MediaScannerConnection(mAppContext, this);
        mConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mConnection.scanFile(mFileName, null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mConnection.disconnect();
    }
}
