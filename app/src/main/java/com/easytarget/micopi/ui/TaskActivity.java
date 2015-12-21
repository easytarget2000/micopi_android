package com.easytarget.micopi.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by michel on 21/12/15.
 *
 * Super class for Activities in which images are being generated
 */
public class TaskActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int contactPerm = checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
            );

            if (contactPerm != PackageManager.PERMISSION_GRANTED) onBackPressed();
        }
    }

    private void showErrorToast() {

    }

    public void onBackButtonClicked(View view) {
        onBackPressed();
    }
}
