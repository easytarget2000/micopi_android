package com.easytarget.micopi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.easytarget.micopi.engine.ColorUtilities;
import com.easytarget.micopi.engine.ImageFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by michel on 08/01/15.
 */
public class GenerateImageTask extends AsyncTask<Integer, Void, Void> {

    private static final String LOG_TAG = GenerateImageTask.class.getSimpleName();

    private Context mAppContext;

    private Contact mContact;

    public GenerateImageTask(Context context, final Contact contact) {
        if (context == null || contact == null) {
            return;
        }

        mAppContext = context.getApplicationContext();
        mContact = contact;
    }

    @Override
    protected Void doInBackground(Integer... params) {
        if (params.length < 2) {
            Log.e(LOG_TAG, "Wrong parameter length: " + params.length);
            return sendBroadcast(false, null, 0);
        }

        if (mContact == null) {
            Log.e("generateImageTask", "ERROR: Contact is null.");
            return sendBroadcast(false, null, 0);
        }

        Bitmap generatedBitmap = new ImageFactory(mContact, params[0]).generateBitmap();
        final int averageColor = ColorUtilities.getAverageColor(generatedBitmap);
        if (generatedBitmap == null) {
            Log.e(LOG_TAG, "Generated null bitmap.");
            return sendBroadcast(false, null, 0);
        }

        File tempFile = mContact.buildTempFile(mAppContext);
        try {
            generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(tempFile));
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
            return sendBroadcast(false, null, 0);
        }

        if (params[1] == 1) {
            new AssignContactImageTask(mAppContext)
                    .execute(mContact.getId(), tempFile.getAbsolutePath());
        }

        return sendBroadcast(true, tempFile.getAbsolutePath(), averageColor);
    }

    private Void sendBroadcast(final boolean didSucceed, final String filePath, final int color) {
        Intent finishBroadcast = new Intent(Constants.ACTION_FINISHED_GENERATE);
        finishBroadcast.putExtra(Constants.EXTRA_SUCCESS, didSucceed);
        finishBroadcast.putExtra(Constants.EXTRA_COLOR, color);
        if (filePath != null) {
            finishBroadcast.putExtra(Constants.EXTRA_FILE_PATH, filePath);
        }
        mAppContext.sendBroadcast(finishBroadcast);
        return null;
    }

}