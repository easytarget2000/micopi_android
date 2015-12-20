package com.easytarget.micopi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by michel@easy-target.org on 08/01/15.
 *
 * Assigns the content of the temporary image file to a given contact
 * and broadcasts a Finished signal onPostExecute
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
        if (TextUtils.isEmpty(contactId)) {
            Log.e(LOG_TAG, "No contact ID parameter given.");
            return false;
        }

        return FileHelper.assignTempFileToContact(mAppContext, contactId);
    }

    @Override
    protected void onPostExecute(Boolean didSuccessfully) {
        Intent finishBroadcast = new Intent(Constants.ACTION_FINISHED_ASSIGN);
        finishBroadcast.putExtra(Constants.EXTRA_SUCCESS, didSuccessfully);
        mAppContext.sendBroadcast(finishBroadcast);
    }

}
