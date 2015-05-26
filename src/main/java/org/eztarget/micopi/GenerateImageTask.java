package org.eztarget.micopi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.eztarget.micopi.engine.ColorUtilities;
import org.eztarget.micopi.engine.ImageFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by michel on 08/01/15.
 *
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

        ImageFactory factory = new ImageFactory(mContact, params[0]);
        Bitmap generatedBitmap = factory.generateBitmapBroadcasting(mAppContext);

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
//        generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        generatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        return sendBroadcast(true, averageColor);
    }

    private Void sendBroadcast(final boolean didSucceed, final int color) {
        Intent finishBroadcast = new Intent(Constants.ACTION_FINISHED_GENERATE);
        finishBroadcast.putExtra(Constants.EXTRA_SUCCESS, didSucceed);
        finishBroadcast.putExtra(Constants.EXTRA_COLOR, color);
        mAppContext.sendBroadcast(finishBroadcast);
        return null;
    }

}