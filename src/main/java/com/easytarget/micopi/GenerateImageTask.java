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
        if (mContact == null) {
            Log.e("generateImageTask", "ERROR: Contact is null.");
            return sendBroadcast(false, 0);
        }

        long startTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "Starting image generator with image size " + params[0] + ".");
        Bitmap generatedBitmap = new ImageFactory(mContact, params[0]).generateBitmap();
        long endTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "FINISHED IMAGE GENERATOR: " + (endTime - startTime));
        sendProgressBroadcast(85);

        final int averageColor = ColorUtilities.getAverageColor(generatedBitmap);

        if (generatedBitmap == null) {
            Log.e(LOG_TAG, "Generated null bitmap.");
            return sendBroadcast(false, 0);
        }
        File tempFile = FileHelper.openTempFile(mAppContext);

        FileOutputStream stream;
        try {
            stream = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
            return sendBroadcast(false, 0);
        }
        startTime = System.currentTimeMillis();
        generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        endTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "FINISHED SAVING FILE: " + (endTime - startTime));

//        if (params[1] == 1) {
//            new AssignContactImageTask(mAppContext).execute(mContact.getId());
//        }

        return sendBroadcast(true, averageColor);
    }

    private Void sendBroadcast(final boolean didSucceed, final int color) {
        Intent finishBroadcast = new Intent(Constants.ACTION_FINISHED_GENERATE);
        finishBroadcast.putExtra(Constants.EXTRA_SUCCESS, didSucceed);
        finishBroadcast.putExtra(Constants.EXTRA_COLOR, color);
        mAppContext.sendBroadcast(finishBroadcast);
        return null;
    }
    
    private Intent mProgressBroadcast;

    private void sendProgressBroadcast(final int progress) {
        if (mProgressBroadcast == null) {
            mProgressBroadcast = new Intent(Constants.ACTION_UPDATE_PROGRESS);
        }
        mProgressBroadcast.putExtra(Constants.EXTRA_PROGRESS, progress);
        mAppContext.sendBroadcast(mProgressBroadcast);
    }


}