package com.easytarget.micopi.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easytarget.micopi.BatchService;
import com.easytarget.micopi.Constants;
import com.easytarget.micopi.DeviceHelper;
import com.easytarget.micopi.R;

// TODO: Use fragments.

/**
 * Created by michel on 11/01/15.
 *
 */
public class BatchActivity extends AppCompatActivity {

    private static final String LOG_TAG = BatchActivity.class.getSimpleName();

    private static final String STORED_CONTACT = "stored_contact";

    private static final String STORED_PROGRESS = "stored_progress";

    private ProgressBar mProgressBar;

    private String mContactName;

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

        mProgressBar = (ProgressBar) findViewById(R.id.progress_batch);

        if (savedInstanceState != null) {
            mContactName = savedInstanceState.getString(STORED_CONTACT);
            mProgressBar.setProgress(savedInstanceState.getInt(STORED_PROGRESS));
        }

        showContactName();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int contactPerm = checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
            );

            if (contactPerm != PackageManager.PERMISSION_GRANTED) onBackPressed();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_FINISHED_GENERATE);
        filter.addAction(Constants.ACTION_UPDATE_PROGRESS);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mProgressBar != null) {
            outState.putInt(STORED_PROGRESS, mProgressBar.getProgress());
            outState.putString(STORED_CONTACT, mContactName);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Constants.ACTION_FINISHED_GENERATE:
                    mProgressBar.setVisibility(View.GONE);
                    mContactName = null;
                    showContactName();
                    mContactView.setText("\u2713");
                    break;
                case Constants.ACTION_UPDATE_PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    final int progress = intent.getIntExtra(Constants.EXTRA_PROGRESS, 33);
                    mProgressBar.setProgress(progress);
                    mContactName = intent.getStringExtra(Constants.EXTRA_CONTACT);
                    showContactName();
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown action received: " + action);
                    break;
            }
        }
    };

    private TextView mContactView;

    private LinearLayout mControlLayout;

    private void showContactName() {
        if (mContactView == null) {
            mContactView = (TextView) findViewById(R.id.text_contact_name);
        }

        if (mControlLayout == null) {
            mControlLayout = (LinearLayout) findViewById(R.id.layout_control);
        }

        if (TextUtils.isEmpty(mContactName)) {
            mContactView.setVisibility(View.GONE);
            mControlLayout.setVisibility(View.GONE);
        } else {
            mContactView.setVisibility(View.VISIBLE);
            mControlLayout.setVisibility(View.VISIBLE);
            mContactView.setText(mContactName);
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
            dialog.setTitle(R.string.please_note);
            dialog.setMessage(R.string.auto_experimental_long);
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
    }

    private NotificationManager mNotMan;

    public void cancelPressed(View view) {

        Intent batchService = new Intent(this, BatchService.class);
        stopService(batchService);
        mContactName = null;
        showContactName();
    }

    public void skipPressed(View view) {
    }

}
