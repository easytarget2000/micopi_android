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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.eztarget.micopi.R;

/**
 * First activity to be displayed after launch.
 */
public class WelcomeActivity extends BaseActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();


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

        final TextView upgradeTeaser = (TextView) findViewById(R.id.text_upgrade_teaser);
        if (upgradeTeaser != null) {
            switch ((int) (Math.random() * 5)) {
                case 1:
                    upgradeTeaser.setText(R.string.upgrade_teaser_2);
                    break;
                case 2:
                    upgradeTeaser.setText(R.string.upgrade_teaser_3);
                    break;
                case 4:
                    upgradeTeaser.setText(R.string.upgrade_teaser_4);
                    break;
                case 5:
                    upgradeTeaser.setText(R.string.upgrade_teaser_5);
                    break;
                default:
                    upgradeTeaser.setText(R.string.upgrade_teaser_1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                requestCode == READ_CONTACTS_PERMISSION_CODE) {

            continueNavigation();

        } else {
            mDidPressSelectButton = false;
            mDidPressCrawlButton = false;
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
        if (!hasReadContactsPermission()) {
            requestReadContactsPermission();
            return;
        }

        if (mDidPressSelectButton) startActivity(new Intent(this, ContactActivity.class));
        else if (mDidPressCrawlButton) startActivity(new Intent(this, BatchActivity.class));

        mDidPressSelectButton = false;
        mDidPressCrawlButton = false;
    }

    private static final Uri UPGRADE_URI =
            Uri.parse("https://play.google.com/store/apps/details?id=org.eztarget.micopifull");

    public void settingsButtonPressed(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, UPGRADE_URI));
    }

}
