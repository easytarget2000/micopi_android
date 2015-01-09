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

package com.easytarget.micopi;

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

import com.easytarget.micopi.engine.ColorUtilities;

import java.util.Date;

/**
 * Activity that displays the generated image and all the options.
 *
 * Created by Michel on 03.02.14.
 */
public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Key for Contact object, used for instance saving and restoration */
    private static final String STORED_CONTACT = "stored_contact";

    /** Key for boolean object, used for instance saving and restoration */
    private static final String STORED_PICKED = "stored_picked";

    /** This activity is the general Context */
    private Context mContext = this;

    /** Displays the contact name */
    private TextView mNameTextView;

    /** Displays a small description text */
    private TextView mDescriptionTextView;

    /** Displays the generated image */
    private ImageView mIconImageView;

    /** Currently handled contact */
    private Contact mContact;

    /** Will be set to false after first contact */
    private boolean mHasPickedContact = false;

    /**
     * Keeps the user from performing any input while performing a task such as generating an image
     */
    private boolean mGuiIsLocked = false;

    /** Last time the back button was pressed */
    private Date backButtonDate;

    /** Horizontal resolution of portrait mode */
//    private int mScreenWidthPixels;

    private String mImagePath;

    private int mColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.w("MainActivity: onCreate()", "ONCREATE");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNameTextView           = (TextView) findViewById(R.id.nameTextView);
        mDescriptionTextView    = (TextView) findViewById(R.id.descriptionTextView);
        mIconImageView          = (ImageView) findViewById(R.id.iconImageView);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            Log.d("MainActivity", "onRestoreInstanceState()");
            // Always call the superclass so it can restore the view hierarchy
            super.onRestoreInstanceState(savedInstanceState);

            // Restore state members from saved instance
//            byte[] imageBytes = savedInstanceState.getByteArray(STORED_IMAGE_PATH);
//            if (imageBytes != null) {
//                Log.d("MainActivity", "Restoring bitmap.");
//                mGeneratedBitmap        = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//            }
            mColor                  = savedInstanceState.getInt(Constants.EXTRA_COLOR);
            mImagePath              = savedInstanceState.getString(Constants.EXTRA_FILE_PATH);
            mContact                = savedInstanceState.getParcelable(STORED_CONTACT);
            mHasPickedContact       = savedInstanceState.getBoolean(STORED_PICKED);
//            mScreenWidthPixels = savedInstanceState.getInt(STORED_WIDTH);

//            if (mHasPickedContact && mContact != null && mGeneratedBitmap != null) {
//                Log.d("Restoring generated bitmap", mGeneratedBitmap.getHeight() + "");
//                Log.d("Restoring contact object", mContact.getFullName());
//                showContactData();
//            }
            new ShowContactDataTask().execute();
        }

        // Immediately show the contact picker if no contact has been selected, yet.
        if(!mHasPickedContact) pickContact();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_FINISHED_ASSIGN);
        filter.addAction(Constants.ACTION_FINISHED_GENERATE);
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
            setGuiIsBusy(false);

            final String action = intent.getAction();
            final boolean didSucceed = intent.getBooleanExtra(Constants.EXTRA_SUCCESS, false);

            switch (action) {
                case Constants.ACTION_FINISHED_GENERATE:
                    if (didSucceed) {
                        mImagePath = intent.getStringExtra(Constants.EXTRA_FILE_PATH);
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
                default:
                    Log.e(LOG_TAG, "Unknown action received: " + action);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (backButtonDate == null) {
            backButtonDate = new Date();
        } else if (backButtonDate.getTime() - System.currentTimeMillis() <= 4000) {
            finish();
        }

        backButtonDate.setTime(System.currentTimeMillis());
        if(!mGuiIsLocked) pickContact();
    }

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
        savedInstanceState.putInt(Constants.EXTRA_COLOR, mColor);
        savedInstanceState.putString(Constants.EXTRA_FILE_PATH, mImagePath);
        savedInstanceState.putParcelable(STORED_CONTACT, mContact);
        savedInstanceState.putBoolean(STORED_PICKED, mHasPickedContact);
//        savedInstanceState.putInt(STORED_WIDTH, mScreenWidthPixels);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        Log.d("MainActivity", "onSaveInstanceState()");
    }

    /*
    GUI ACTION
     */

    /**
     * Locks / unlocks the GUI through boolean field and
     * hides / shows the progress bar.
     *
     * @param isBusy Will be applied to mGuiIsLocked field
     */
    private void setGuiIsBusy(boolean isBusy) {
        mGuiIsLocked = isBusy;
        ProgressBar mLoadingCircle = (ProgressBar) findViewById(R.id.progressBar);
        if(isBusy) mLoadingCircle.setVisibility(View.VISIBLE);
        else mLoadingCircle.setVisibility(View.GONE);
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
            backButtonDate = null;
            mHasPickedContact = true;
            mContact = new Contact(mContext, data);
            startGenerateImageTask();
        }
    }

    private void startGenerateImageTask() {
        setGuiIsBusy(true);
        mIconImageView.setImageDrawable(null);

        // Hide all text views.
        mNameTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);

        // Reset the activity colours.
        int defaultColor = getResources().getColor(R.color.primary);
        setColor(defaultColor);

        final int imageSize = DeviceHelper.getBestImageSize(this);
        new GenerateImageTask(mContext, mContact).execute(imageSize , 0);
    }

    /**
     * Opens a YES/NO dialog for the user to confirm that the contact's image will be overwritten.
     */
    public void confirmAssignContactImage() {
        if (mContact == null || mImagePath == null) return;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    setGuiIsBusy(true);
                    new AssignContactImageTask(mContext).execute(mContact.getId(), mImagePath);
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
            if (mImagePath == null || mContact == null) return null;
            return BitmapFactory.decodeFile(mImagePath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null)  {
                Log.e(LOG_TAG, "Could not load image from file.");
                mColor = getResources().getColor(R.color.primary);
                setColor(mColor);
                return;
            }
            setColor(mColor);
            Drawable generatedDrawable = new BitmapDrawable(getResources(), bitmap);
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
            setGuiIsBusy(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (mImagePath == null) return null;
            Bitmap bitmap = BitmapFactory.decodeFile(mImagePath);

            if(bitmap != null && mContact != null) {
                FileHelper fileHandler = new FileHelper();
                return fileHandler.saveContactImageFile(
                        mContext,
                        bitmap,
                        mContact.getFullName(),
                        mContact.getMD5EncryptedString().charAt(0)
                );
            }

            return null;
        }

        @Override
        protected void onPostExecute(String fileName) {
            setGuiIsBusy(false);

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
        View mainView = findViewById(R.id.rootView);
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
