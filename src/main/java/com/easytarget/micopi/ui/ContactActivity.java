/*
 * Copyright (C) 2014 Easy Target
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.easytarget.micopi.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easytarget.micopi.AssignContactImageTask;
import com.easytarget.micopi.Constants;
import com.easytarget.micopi.Contact;
import com.easytarget.micopi.DeviceHelper;
import com.easytarget.micopi.FileHelper;
import com.easytarget.micopi.GenerateImageTask;
import com.easytarget.micopi.R;
import com.easytarget.micopi.engine.ColorUtilities;

import java.io.File;

/**
 * Activity that displays the generated image and all the options.
 *
 * Created by Michel on 03.02.14.
 */
public class ContactActivity extends ActionBarActivity {

    private static final String LOG_TAG = ContactActivity.class.getSimpleName();

    /** Key for Contact object, used for instance saving and restoration */
    private static final String STORED_CONTACT = "stored_contact";

    /** Key for boolean value, used for instance saving and restoration */
    private static final String STORED_PICKED = "stored_picked";

    /** This activity is the general Context */
    private Context mContext = this;

    /** Displays the contact name */
    private TextView mNameTextView;

    /** Displays a small description text */
    private TextView mDescriptionTextView;

    /** Displays the generated image */
    private ImageView mIconImageView;

    private ProgressBar mProgressbar;

    /** Currently handled contact */
    private Contact mContact;

    /** Will be set to false after first contact */
    private boolean mHasPickedContact = false;

    /**
     * Keeps the user from performing any input while performing a task such as generating an image
     */
    private boolean mGuiIsLocked = false;

    private int mColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.w("MainActivity: onCreate()", "ONCREATE");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);

        mNameTextView           = (TextView) findViewById(R.id.nameTextView);
        mDescriptionTextView    = (TextView) findViewById(R.id.descriptionTextView);
        mIconImageView          = (ImageView) findViewById(R.id.iconImageView);
        mProgressbar            = (ProgressBar) findViewById(R.id.progressBar);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null && !mGuiIsLocked) {
            mColor                  = savedInstanceState.getInt(Constants.EXTRA_COLOR);
            mContact                = savedInstanceState.getParcelable(STORED_CONTACT);
            mHasPickedContact       = savedInstanceState.getBoolean(STORED_PICKED);
            new ShowContactDataTask().execute();
        }

        // Immediately show the contact picker if no contact has been selected, yet.
        if(!mHasPickedContact) pickContact();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent intent = new Intent(this, WelcomeActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_FINISHED_ASSIGN);
        filter.addAction(Constants.ACTION_FINISHED_GENERATE);
        filter.addAction(Constants.ACTION_UPDATE_PROGRESS);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            final boolean didSucceed = intent.getBooleanExtra(Constants.EXTRA_SUCCESS, false);

            switch (action) {
                case Constants.ACTION_FINISHED_GENERATE:
                    showProgress(false);
                    if (didSucceed) {
                        mColor = intent.getIntExtra(
                                Constants.EXTRA_COLOR,
                                0
                        );
                        new ShowContactDataTask().execute();
                    } else {
                        mContact = null;
                        mNameTextView.setText(R.string.no_contact_selected);
                        if (getApplicationContext() != null) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources().getString(R.string.error_generating),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                    break;
                case Constants.ACTION_FINISHED_ASSIGN:
                    showProgress(false);
                    if (didSucceed) {
                        Toast.makeText(getApplicationContext(),
                                String.format(
                                        getResources().getString(R.string.success_applying_image),
                                        mContact.getFullName()),
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                getResources().getString(R.string.error_assign),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    break;
                case Constants.ACTION_UPDATE_PROGRESS:
                    final int progress = intent.getIntExtra(Constants.EXTRA_PROGRESS, 33);
                    mProgressbar.setProgress(progress);
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown action received: " + action);
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        if(!mGuiIsLocked) pickContact();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!mGuiIsLocked) {
            // Handle presses on the action bar items.
            switch (item.getItemId()) {
                case R.id.action_assign:
                    confirmAssignContactImage();
                    return true;
                case R.id.action_previous_image:
                    mContact.modifyRetryFactor(false);
                    startGenerateImageTask();
                    return true;
                case R.id.action_next_image:
                    mContact.modifyRetryFactor(true);
                    startGenerateImageTask();
                    return true;
                case R.id.action_search:
                    pickContact();
                    return true;
                case R.id.action_save:
                    new SaveImageTask().execute();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.EXTRA_COLOR, mColor);
        savedInstanceState.putParcelable(STORED_CONTACT, mContact);
        savedInstanceState.putBoolean(STORED_PICKED, mHasPickedContact);
//        savedInstanceState.putInt(STORED_PROGRESS, mProgressbar.getProgress());
    }

    /*
    GUI ACTION
     */

    private void showProgress(boolean doShow) {
        mGuiIsLocked = doShow;
        if (mProgressbar == null) return;

        if (doShow) mProgressbar.setVisibility(View.VISIBLE);
        else mProgressbar.setVisibility(View.GONE);
    }

    /**
     * Return code of contact picker
     */
    private static final int PICK_CONTACT = 1;

    /**
     * Opens the contact picker and allows the user to chose a contact.
     * onActivityResult will be called when returning to MainActivity.
     */
    public void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    /**
     * Method called by contact picker when a person was chosen.
     *
     * @param reqCode Request code (1 when coming from contact picker)
     * @param resultCode Result code
     * @param data Contact data
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        // Close the app if the back button was pressed on first contact picker.
        if(!mHasPickedContact && resultCode != RESULT_OK) finish();

        // Check if the activity result is ok and check the request code.
        // The latter should be 1 indicating a picked contact.
        if(resultCode == RESULT_OK && reqCode == PICK_CONTACT) {
            mHasPickedContact = true;
            mContact = new Contact(mContext, data);
            startGenerateImageTask();
        }
    }

    private void startGenerateImageTask() {
        // Reset the ProgressBar.
        mProgressbar.setProgress(5);

        showProgress(true);
        mIconImageView.setImageDrawable(null);

        // Hide all text views.
        mNameTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);

        // Reset the activity colours.
        int defaultColor = getResources().getColor(R.color.primary);
        setColor(defaultColor);

        final int imageSize = DeviceHelper.getBestImageSize(this);
        mProgressbar.setProgress(25);
        new GenerateImageTask(mContext, mContact).execute(imageSize , 0);
    }

    /**
     * Opens a YES/NO dialog for the user to confirm that the contact's image will be overwritten.
     */
    public void confirmAssignContactImage() {
        if (mContact == null) return;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mGuiIsLocked = true;
                    new AssignContactImageTask(mContext).execute(mContact.getId());
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                String.format(
                        getResources().getString(R.string.overwrite_dialog),
                        mContact.getFullName())
        );
        builder.setNegativeButton(android.R.string.no, dialogClickListener);
        builder.setPositiveButton(android.R.string.yes, dialogClickListener);
        builder.show();
    }

    /*
    THREADS
     */

    private class ShowContactDataTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            if (mContact == null) return null;
            File tempFile = FileHelper.openTempFile(getApplicationContext());
            if (tempFile == null) return null;
            Log.d(LOG_TAG, "Loading bitmap from temp file:" + tempFile.getAbsolutePath());
            return BitmapFactory.decodeFile(tempFile.getAbsolutePath());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mProgressbar.setProgress(99);
            super.onPostExecute(bitmap);
            if (bitmap == null)  {
                Log.e(LOG_TAG, "Could not load image from file.");
                mColor = getResources().getColor(R.color.primary);
                setColor(mColor);
                return;
            }
            setColor(mColor);
            Drawable generatedDrawable = new BitmapDrawable(getResources(), bitmap);
            mProgressbar.setVisibility(View.GONE);
            mIconImageView.setImageDrawable(generatedDrawable);

            mNameTextView.setText(mContact.getFullName());
            mNameTextView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Save the generated image to a file on the device
     */
    private class SaveImageTask extends AsyncTask< Void, Void, String > {

        @Override
        protected void onPreExecute() {
            mGuiIsLocked = true;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (mContact == null) return null;

            FileHelper fileHandler = new FileHelper();
            return fileHandler.copyTempFileToPublicDir(
                    mContext,
                    mContact.getFullName(),
                    mContact.getMD5EncryptedString().charAt(0)
            );
        }

        @Override
        protected void onPostExecute(String fileName) {
            mGuiIsLocked = false;

            if (!TextUtils.isEmpty(fileName)) {
                Toast.makeText(
                        mContext,
                        String.format(
                                getResources().getString(R.string.success_saving_image),
                                fileName),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                        mContext,
                        String.format(
                                getResources().getString(R.string.error_saving),
                                fileName),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    /**
     * Changes the color of the action bar and status bar
     *
     * @param color ARGB Color to apply
     */
    private void setColor(int color) {
        View mainView = findViewById(R.id.layout_contact);
        if (mainView == null) {
            Log.e("MainActivity:setColor()", "WARNING: Did not find root view.");
        } else {
            mainView.setBackgroundColor(color);
        }

        /*
        Set the action bar colour to the average colour of the generated image and
        the status bar colour for Android Version >= 5.0 accordingly.
        */

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Get the window through a reference to the activity.
            Activity parent = (Activity) mContext;
            Window window = parent.getWindow();
            // Set the status bar colour of this window.
            int statusColor = ColorUtilities.getDarkenedColor(color);
            window.setStatusBarColor(statusColor);
        }
    }
}
