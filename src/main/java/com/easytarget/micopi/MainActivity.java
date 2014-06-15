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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays the generated image and all the options.
 */
public class MainActivity extends Activity {
    private Context mContext = this;
    private static final int PICK_CONTACT = 1;      // Return code of contact picker
    private MicopiUtil mUtilities = new MicopiUtil(this);    // Utility class
    private TextView mNameTextView, mDescriptionTextView, mSeparatorView, mSeparatorView2;
    private ImageView mIconImageView;
    private Contact mContact;
    private boolean mIsFirstContactPicker = true;   // Will be set to false after first contact
    private Bitmap mGeneratedBitmap = null;         // Stores the generated image
    private boolean mGuiIsLocked = false;           // Keeps the user from performing input

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare GUI.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mNameTextView           = (TextView) findViewById(R.id.nameTextView);
        mDescriptionTextView    = (TextView) findViewById(R.id.descriptionTextView);
        mSeparatorView          = (TextView) findViewById(R.id.separator);
        mSeparatorView2         = (TextView) findViewById(R.id.separator2);
        mIconImageView          = (ImageView) findViewById(R.id.iconImageView);
        changeGui(false);

//         // Ad-Banner:
//        adView = (AdView) findViewById(R.id.adView);
//        adView.loadAd(new AdRequest.Builder().build());

        if(mIsFirstContactPicker || mContact == null || mGeneratedBitmap == null)
            pickContact();

    }

    @Override
    public void onBackPressed() {
        if(!mGuiIsLocked) pickContact();
        else finish();
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
                case R.id.action_retry:
                    mContact.modifyRetryFactor();
                    new generateImageTask().execute();
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

    /**
     * Locks / unlocks the GUI through boolean field and
     * hides / shows the progress bar.
     *
     * @param isBusy Will be applied to mGuiIsLocked field
     */
    private void changeGui(boolean isBusy) {
        mGuiIsLocked = isBusy;
        ProgressBar mLoadingCircle = (ProgressBar) findViewById(R.id.progressBar);
        if(isBusy) mLoadingCircle.setVisibility(View.VISIBLE);
        else mLoadingCircle.setVisibility(View.GONE);
    }

    /**
     * Opens the contact picker and allows the user to chose a contact.
     * onActivityResult will be called when returning to MainActivity.
     */
    public void pickContact() {
        if(mIsFirstContactPicker) {
            mNameTextView.setVisibility(View.GONE);
            mDescriptionTextView.setVisibility(View.GONE);
            mSeparatorView.setVisibility(View.GONE);
            mSeparatorView2.setVisibility(View.GONE);
        }

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

        // Close the app if the back button was pressed on first contact picker.
        if(mIsFirstContactPicker && resultCode != RESULT_OK) finish();

        // Check if the activity result is ok and check the request code.
        // The latter should be 1 indicating a picked contact.
        if(resultCode == RESULT_OK && reqCode == PICK_CONTACT) {
            mContact = new Contact(mContext, data);
            new generateImageTask().execute();
            mIsFirstContactPicker = false;
        }

        super.onActivityResult(reqCode, resultCode, data);

    }


    /**
     * Opens a YES/NO dialog for the user to confirm that the contact's image will be overwritten.
     */
    public void confirmAssignContactImage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int iButton) {
                if(iButton == DialogInterface.BUTTON_POSITIVE) {
                    new AssignContactImageTask().execute();
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

    //Threads that could block the GUI

    /**
     * Constructs a contact from the given Intent data.
     */
    private class generateImageTask extends AsyncTask< Void, Void, Bitmap > {

        // Show a blank GUI while generating an image.
        protected void onPreExecute() {
            changeGui(true);
            mIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.micopi_logo));

            // Show the GUI.
            mNameTextView.setText(mContact.getFullName());
            mNameTextView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setVisibility(View.VISIBLE);
            mSeparatorView.setVisibility(View.VISIBLE);
            mSeparatorView2.setVisibility(View.VISIBLE);
        }

        // Attempt to query the contact from the DB and
        // if the contact object contains a name, generate a bitmap.
        protected Bitmap doInBackground(Void... params) {

            if(mContact != null) {
                MicopiGeneratorMD5 mgen = new MicopiGeneratorMD5(mContact);
                return mgen.generateBitmap();
            } else {
                return null;
            }
        }

        // If a complete bitmap was generated, display it,
        // otherwise the Contact could not be generated and
        protected void onPostExecute(Bitmap generatedBitmap) {
            changeGui(false);

            // If a new bitmap was generated, store it in the field,
            // display it and show the contact name.
            if(generatedBitmap != null) {
                mGeneratedBitmap = generatedBitmap;

                Drawable generatedDrawable = new BitmapDrawable(
                        getResources(), generatedBitmap);
                mIconImageView.setImageDrawable(generatedDrawable);

            } else {
                Log.e("ConstructContactAndGenerateImageTask", "generatedBitmap is null.");
                mNameTextView.setText(R.string.no_contact_selected);
                if(getApplicationContext() != null) {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.error_generating),
                            Toast.LENGTH_LONG
                   ).show();
               }
            }
        }

    }

    /**
     * Assigns the bitmap to the contact.
     */
    private class AssignContactImageTask extends AsyncTask< Void, Void, Boolean > {

        protected void onPreExecute() {
            changeGui(true);
        }

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        @Override
        protected Boolean doInBackground(Void... params) {
            return mGeneratedBitmap != null && mContact.assignImage(mGeneratedBitmap);
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Boolean didSuccessfully) {
            changeGui(false);

            if(didSuccessfully && getApplicationContext() != null) {
                Toast.makeText(getApplicationContext(),
                        String.format(
                                getResources().getString(R.string.success_applying_image),
                                mContact.getFullName()),
                        Toast.LENGTH_LONG
               ).show();
            } else if(!didSuccessfully && getApplicationContext() != null) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(R.string.error_assign),
                        Toast.LENGTH_LONG
               ).show();
            } else {
                Log.e("AssignContactImageTask",
                        "Could not assign the image AND applicationContext is null.");
            }
        }
    }

    /**
     * Save the image to a file.
     */
    private class SaveImageTask extends AsyncTask< Void, Void, String > {

        protected void onPreExecute() {
            changeGui(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            if(mGeneratedBitmap != null && mContact != null) {
                return mUtilities.saveContactImageFile(mGeneratedBitmap, mContact.getFullName(),
                        mContact.getMD5EncryptedString().charAt(0));
            } else return "";
        }

        protected void onPostExecute(String fileName) {
            changeGui(false);

            if(fileName.length() > 1) {
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
}
