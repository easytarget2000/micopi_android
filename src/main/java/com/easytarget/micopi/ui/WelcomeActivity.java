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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;

import com.easytarget.micopi.BatchService;
import com.easytarget.micopi.Constants;
import com.easytarget.micopi.DeviceHelper;
import com.easytarget.micopi.R;

/**
 * First activity to be displayed after launch.
 */
public class WelcomeActivity extends ActionBarActivity {

    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // The welcome activity doesn't need to have an action bar.
        if(getSupportActionBar() != null) getSupportActionBar().hide();

        // Set the status bar colour of this window on Android >= 5.0.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            int statusBarColor = getResources().getColor(R.color.primary_dark);
            window.setStatusBarColor(statusBarColor);
        }
    }

    public void selectButtonPressed(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    public void crawlButtonPressed(View view) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setMessage(R.string.batch_question);

        // Alternatively react on pressing the OK button.
        dialog.setPositiveButton(R.string.generate_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showBatchConfirmDialog(true);
            }
        });

        dialog.setNeutralButton(R.string.generate_missing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showBatchConfirmDialog(false);
            }
        });

        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        dialog.show();
    }

    private void showBatchConfirmDialog(final boolean doOverwrite) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        if (doOverwrite) {
            dialog.setMessage(R.string.confirm_all);
        } else {
            dialog.setTitle(R.string.batch_experimental);
            dialog.setMessage(R.string.batch_experimental_warning);
        }

        // Alternatively react on pressing the OK button.
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                startCrawl(doOverwrite);
            }
        });

        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        dialog.show();
    }

    private void startCrawl(final boolean doOverwrite) {
        Intent batchService = new Intent(this, BatchService.class);
        batchService.putExtra(Constants.EXTRA_DO_OVERWRITE, doOverwrite);
        final int imageSize = DeviceHelper.getBestImageSize(this);
        batchService.putExtra(Constants.EXTRA_IMAGE_SIZE, imageSize);
        startService(batchService);
//
//        selectButtonPressed(view);
    }

}
