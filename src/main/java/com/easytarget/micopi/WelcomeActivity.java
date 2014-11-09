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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;

/**
 * First activity to be displayed after launch.
 */
public class WelcomeActivity extends ActionBarActivity {

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

    public void startMainActivity( @SuppressWarnings("unused") View view ) {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity( intent );
    }

    public void startCrawl( @SuppressWarnings("unused") View view) {

    }

}
