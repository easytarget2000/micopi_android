package org.eztarget.micopi.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by michel on 13/02/16.
 *
 */
public class BaseActivity extends AppCompatActivity {

    public void onBackButtonClicked(View view) {
        onBackPressed();
    }

    protected static final int WRITE_STORAGE_PERMISSION_CODE = 50;

    protected void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_STORAGE_PERMISSION_CODE
        );
    }

    protected boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int writePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return writePerm == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static final int READ_CONTACTS_PERMISSION_CODE = 49;

    protected void requestReadContactsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CONTACTS},
                READ_CONTACTS_PERMISSION_CODE
        );
    }

    protected boolean hasReadContactsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int writePerm = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            return writePerm == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    protected static final int WRITE_CONTACTS_PERMISSION_CODE = 51;

    protected void requestWriteContactsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_CONTACTS},
                WRITE_CONTACTS_PERMISSION_CODE
        );
    }

    protected boolean hasWriteContactsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int writePerm = checkSelfPermission(Manifest.permission.WRITE_CONTACTS);
            return writePerm == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
