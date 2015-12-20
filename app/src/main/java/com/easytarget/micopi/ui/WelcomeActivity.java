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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.easytarget.micopi.R;

/**
 * First activity to be displayed after launch.
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    public static final int READ_CONTACTS_PERMISSION_CODE = 51;

    private static final String PAYPAL_DONATE_URL =
            "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VPWXDNB6P4QAJ";

    private boolean mDidPressSelectButton = false;

    private boolean mDidPressCrawlButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Set the status bar colour of this window on Android >= 5.0.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            int statusBarColor = getResources().getColor(R.color.primary_dark);
            window.setStatusBarColor(statusBarColor);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        if (grantResults.length < 1) return;

        if (requestCode == READ_CONTACTS_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) continueNavigation();
        }
    }

    public void selectButtonPressed(@SuppressWarnings("unused") View view) {
        mDidPressCrawlButton = false;
        mDidPressSelectButton = true;
        continueNavigation();
    }

    public void crawlButtonPressed(View view) {
        mDidPressSelectButton = false;
        mDidPressCrawlButton = true;
        continueNavigation();
    }

    private void continueNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int contactPerm = checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
            );

            if (contactPerm != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission to read contacts not given.");
                // Once the permission is given, the mail will be sent.
                // See onRequestPermissionsResult().
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACTS_PERMISSION_CODE
                );
                return;
            }
        }

        if (mDidPressSelectButton) {
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
        } else if (mDidPressCrawlButton) {
            startActivity(new Intent(this, BatchActivity.class));
        }
    }

    public void donateButtonPressed(View view) {
        final Intent payPal = new Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL_DONATE_URL));
        startActivity(payPal);
    }

}
