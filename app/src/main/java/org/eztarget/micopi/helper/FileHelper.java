package org.eztarget.micopi.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by michel on 27/10/14.
 *
 */
public class FileHelper implements MediaScannerConnection.MediaScannerConnectionClient {

    private static final String TAG = FileHelper.class.getSimpleName();

    private MediaScannerConnection mConnection;

    private String mFileName;

    /**
     * Creates a Micopi directory on the external storage
     */
    public static File prepareMicopiDir(final String subFolder) {

        final String path = Environment.getExternalStorageDirectory() +
                "/micopi/" +
                (TextUtils.isEmpty(subFolder) ? "" : subFolder + "/");

        final File micopiDir = new File(path);
        Log.d(TAG, "mkdirs returned " + micopiDir.mkdirs() + " on " + path + ".");
        return micopiDir;
    }

    public static final String SUB_FOLDER_NEW = "micopi_created";

    public String storeImage(
            final Context context,
            final Bitmap bitmap,
            final String subFolder,
            final String fileName
    ) {
        if (bitmap == null) {
            Log.d(TAG, "storeImage: Bitmap is null.");
            return null;
        }

        // Files will be stored in the /sdcard/micopi dir.

        final File outFile = new File(prepareMicopiDir(subFolder).getAbsolutePath(), fileName);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (SUB_FOLDER_NEW.equals(subFolder)) {
            mFileName = outFile.getAbsolutePath();
            mConnection = new MediaScannerConnection(context, this);
            mConnection.connect();
        }

        return outFile.getAbsolutePath();
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

