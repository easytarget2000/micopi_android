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
 * Created by michel on 11/01/15.
 *
 */
public class BatchActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch);

        // Set the status bar colour of this window on Android >= 5.0.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            int statusBarColor = getResources().getColor(R.color.primary_dark);
            window.setStatusBarColor(statusBarColor);
        }
    }

    public void generateAllPressed(View view) {
        showBatchConfirmDialog(true);
    }

    public void generateMissingPressed(View view) {
        showBatchConfirmDialog(false);
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
