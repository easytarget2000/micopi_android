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

package org.eztarget.micopi.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eztarget.micopi.Contact;
import org.eztarget.micopi.R;
import org.eztarget.micopi.engine.ColorUtilities;
import org.eztarget.micopi.engine.ImageFactory;
import org.eztarget.micopi.helper.DatabaseHelper;
import org.eztarget.micopi.helper.DeviceHelper;
import org.eztarget.micopi.helper.FileHelper;


/**
 * Activity that displays the generated image and all the options.
 * <p/>
 * Created by Michel on 03.02.14.
 */
public class ContactActivity extends TaskActivity {

    private enum PhotoAction {
        Assign, Store
    }

    private static final String TAG = ContactActivity.class.getSimpleName();

    public static final int WRITE_STORAGE_PERMISSION_CODE = 50;

    public static final int WRITE_CONTACTS_PERMISSION_CODE = 52;

    /**
     * Key for Contact object, used for instance saving and restoration
     */
    private static final String KEY_CONTACT = "contact";

    /**
     * Currently handled contact
     */
    private Contact mContact;

    /**
     * Keeps the user from performing any input while performing a task such as generating an image
     */
    private boolean mLocked = false;

    private PhotoAction mAction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_CONTACT)) {
            setContact((Contact) savedInstanceState.getParcelable(KEY_CONTACT));
        }

        // Immediately show the contact picker if no contact has been selected, yet.
        if (mContact == null) pickContact();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            mAction = null;
            return;
        }

        switch (requestCode) {
            case WRITE_STORAGE_PERMISSION_CODE:
            case WRITE_CONTACTS_PERMISSION_CODE:
                if (mAction != null) startPhotoAction(mAction);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mLocked) super.onBackPressed();
    }

    public void onButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.button_assign:
                showAssignConfirmationDialog();
                return;

            case R.id.button_search:
                pickContact();
                return;

            case R.id.button_prev:
                generateNew(false);
                return;

            case R.id.button_next:
                generateNew(true);
                return;

            case R.id.button_save:
                startPhotoAction(PhotoAction.Store);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_CONTACT, mContact);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void generateNew(final boolean moveForward) {
        mContact.modifyRetryFactor(moveForward);
        generateImage();
    }

    /*
    GUI ACTION
     */

    private void setBusy() {
        mLocked = true;
        findViewById(R.id.group_contact_progress).setVisibility(View.VISIBLE);
    }

    private void setReady() {
        mLocked = false;
        fadeOutView(findViewById(R.id.group_contact_progress));
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
        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    /**
     * Method called by contact picker when a person was chosen.
     *
     * @param reqCode    Request code (1 when coming from contact picker)
     * @param resultCode Result code
     * @param data       Contact data
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        // Close the app if the back button was pressed on first contact picker.
        if (mContact == null && resultCode != RESULT_OK) finish();

        // Check if the activity result is ok and check the request code.
        // The latter should be 1 indicating a picked contact.
        if (resultCode == RESULT_OK && reqCode == PICK_CONTACT) {
            setContact(DatabaseHelper.buildContact(getContentResolver(), data));
        }
    }

    private void setContact(final Contact contact) {
        mContact = contact;

        final TextView nameView = (TextView) findViewById(R.id.text_contact_name);
        final View descriptionView = findViewById(R.id.text_description);

        if (mContact == null) {
            Log.e("generateImageTask", "ERROR: Contact is null.");
            nameView.setText("");
            descriptionView.setVisibility(View.GONE);
            onBackPressed();
            return;
        } else {
            nameView.setText(mContact.getFullName());
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setVisibility(View.VISIBLE);
        }

        generateImage();
    }

    private void generateImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.image_contact);
        imageView.setImageDrawable(null);


        final Bitmap generatedBitmap;
        generatedBitmap = ImageFactory.bitmapFrom(
                this,
                mContact,
                DeviceHelper.getBestImageSize(this)
        );
        imageView.setImageBitmap(generatedBitmap);

        if (generatedBitmap == null) {
            Log.e(TAG, "Generated null bitmap.");
            setColor(getResources().getColor(R.color.primary));
        } else {
            setColor(ColorUtilities.getAverageColor(generatedBitmap));
        }

    }

    /**
     * Opens a YES/NO dialog for the user to confirm that the contact's image will be overwritten.
     */
    public void showAssignConfirmationDialog() {
        if (mContact == null) return;

        final DialogInterface.OnClickListener dialogClickListener;
        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    startPhotoAction(PhotoAction.Assign);
                }

            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                String.format(
                        getResources().getString(R.string.want_to_assign),
                        mContact.getFullName()
                ) + getResources().getString(R.string.backup_not_activated)
        );
        builder.setNegativeButton(android.R.string.no, dialogClickListener);
        builder.setPositiveButton(android.R.string.yes, dialogClickListener);
        builder.show();
    }

    private void startPhotoAction(final PhotoAction action) {
        mAction = action;
        if (hasStoragePermission()) {
            if (PhotoAction.Assign == action && !hasWriteContactsPermission()) {
                requestWriteContactsPermission();
                return;
            }
            new SaveImageTask().execute();
        } else {
            requestStoragePermission();
        }
    }

    private class SaveImageTask extends AsyncTask<Void, Void, Boolean> {

        String mFileName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setBusy();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final Bitmap bitmap = ImageFactory.bitmapFrom(
                    ContactActivity.this,
                    mContact,
                    DeviceHelper.getBestImageSize(ContactActivity.this)
            );
            if (mAction == PhotoAction.Assign) {
                return DatabaseHelper.assignImageToContact(
                        ContactActivity.this,
                        bitmap,
                        mContact
                );
            } else if (mAction == PhotoAction.Store) {
                mFileName = mContact.getFileName();
                return new FileHelper().storeImage(
                        ContactActivity.this,
                        bitmap,
                        FileHelper.SUB_FOLDER_NEW,
                        mFileName
                ) != null;
            } else {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            setReady();

            if (mAction == PhotoAction.Assign) {
                showAssignImageToast(succeeded);
            } else if (mAction == PhotoAction.Store) {
                showSaveImageToast(succeeded, mFileName);
            }

            mAction = null;
        }
    }

    private void showAssignImageToast(final boolean succeeded) {
        if (succeeded) {
            showSuccess();
            Toast.makeText(
                    ContactActivity.this,
                    String.format(
                            getString(R.string.got_new_picture),
                            mContact.getFullName()
                    ),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    ContactActivity.this,
                    R.string.error_assign,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showSaveImageToast(final boolean succeeded, final String fileName) {
        if (succeeded) {
            Toast.makeText(
                    ContactActivity.this,
                    String.format(
                            getResources().getString(R.string.saved_picture_as),
                            fileName
                    ),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    ContactActivity.this,
                    String.format(
                            getResources().getString(R.string.error_saving),
                            fileName
                    ),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Changes the color of the action bar and status bar
     *
     * @param color ARGB Color to apply
     */
    private void setColor(int color) {
//        mColor = color;
        final View mainView = findViewById(R.id.layout_contact);
        if (mainView == null) {
            Log.e("MainActivity:setColor()", "WARNING: Did not find root view.");
        } else {
            mainView.setBackgroundColor(color);
        }

    }
}
