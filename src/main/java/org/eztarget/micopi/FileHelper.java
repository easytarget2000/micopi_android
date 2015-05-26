package org.eztarget.micopi;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by michel on 27/10/14.
 *
 */
public class FileHelper implements MediaScannerConnection.MediaScannerConnectionClient{

    private static final String LOG_TAG = FileHelper.class.getSimpleName();

    private static final String DIR_MICOPI = "/micopi/";

    private static final String TEMP_FILE_NAME = ".tempfile.jpg";

    private MediaScannerConnection mConnection;

    private static Context mAppContext;

    private String mFileName;

    public static File openTempFile(Context context) {
        if (context == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    TEMP_FILE_NAME
            );
        } else {
            // Fallback: use the Micopi directory on the external storage.
            File micopiDir = prepareMicopiDir();
            return new File(micopiDir.getAbsolutePath(), TEMP_FILE_NAME);
        }
    }

    /** Creates a Micopi directory on the external storage */
    private static File prepareMicopiDir() {
        File micopiDir = new File(Environment.getExternalStorageDirectory() + DIR_MICOPI);
        micopiDir.mkdirs();
        return micopiDir;
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param context
     * @param contactId
     * @return TRUE if assignment was successful.
     */
    public static boolean assignTempFileToContact(
            Context context,
            final String contactId
    ) {
        if (context == null) return false;

        // Open the temporary file in which the image was stored.
        File tempImageFile = openTempFile(context);
        if (tempImageFile == null) return false;

        // Create a byte stream from the file.
        byte[] image = new byte[(int) tempImageFile.length()];
        try {
            new FileInputStream(tempImageFile).read(image);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return assignImageToContact(context, image, contactId);
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param context
     * @param bitmap
     * @param contactId
     * @return TRUE if assignment was successful.
     */
    public static boolean assignImageToContact(
            Context context,
            final Bitmap bitmap,
            final String contactId
    ) {
        // Create a byte stream from the generated image.
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final byte[] image = outputStream.toByteArray();

        return assignImageToContact(context, image, contactId);
    }

    /**
     * Finds the contact's image entry and replaces it with the generated data.
     *
     * @param context
     * @param image
     * @param contactId
     * @return TRUE if assignment was successful.
     */
    public static boolean assignImageToContact(
            Context context,
            final byte[] image,
            final String contactId
    ) {
        mAppContext = context.getApplicationContext();
        if (mAppContext == null || TextUtils.isEmpty(contactId)) return false;

        final Cursor rawContactCursor = mAppContext.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + " = " + contactId,
                null,
                null
        );


        if(rawContactCursor == null){
            Log.e(LOG_TAG, "ERROR: rawContactCursor is null.");
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

        // Set the byte array as the raw contact's image.
        final ContentValues values = new ContentValues();
        int photoRow = -1;

        if (rawContactUri == null) {
            Log.e(LOG_TAG, "ERROR: rawContactUri is null.");
            return false;
        }

        final String photoSelection =
                ContactsContract.Data.RAW_CONTACT_ID + "=="
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

        if(changePhotoCursor == null) {
            Log.e(LOG_TAG, "ERROR: changePhotoCursor is null.");
            return false;
        }
        final int index = changePhotoCursor.getColumnIndex(ContactsContract.Data._ID);
        if(index > 0 && changePhotoCursor.moveToFirst()) {
            photoRow = changePhotoCursor.getInt(index);
        }
        changePhotoCursor.close();

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
     * The last generated image is stored in a temporary file in the App directory.
     * This method copies the temporary file to a Micopi folder on the external storage,
     * so that it can be accessed by the user.
     * A media scan is performed, to have the file listed in gallery apps.
     *
     * @param context Context from which to get the temporary file from the app dir
     * @param fileName First part of the new file name, usually the full contact name
     * @param appendix Appendix character to separate files from the same contact
     * @return FirstName_LastName-appendix.png, if successfully copied the file
     */
    public String copyTempFileToPublicDir(
            Context context,
            final String fileName,
            final char appendix
    ) {
        if (context == null || TextUtils.isEmpty(fileName)) return null;
        mAppContext = context.getApplicationContext();

        // Files will be stored in the /sdcard/micopi dir.
        File micopiDir = prepareMicopiDir();

        // The file name is "FirstName_LastName-x.png".
        final String newName = fileName.replace(' ', '_') + "-" + appendix + ".png";
        File newFile = new File(micopiDir.getAbsolutePath(), newName);

        // Open the temporary file and copy the file to the new destination.
        File tempFile = openTempFile(context);
        if (tempFile == null) return null;
        try {
            copyFile(tempFile, newFile);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
            return null;
        }

        performMediaScan(newFile);
        return newName;
    }

    public void copyFile(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);

        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
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
