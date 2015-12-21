package com.easytarget.micopi.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.easytarget.micopi.BatchService;
import com.easytarget.micopi.Constants;
import com.easytarget.micopi.DeviceHelper;
import com.easytarget.micopi.R;

/**
 * Created by michel on 11/01/15.
 *
 */
public class BatchActivity extends TaskActivity {

    private static final String LOG_TAG = BatchActivity.class.getSimpleName();

    private static final String STORED_CONTACT = "stored_contact";

    private static final String STORED_PROGRESS = "stored_progress";

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

//        mProgressBar = (ProgressBar) findViewById(R.id.progress_batch);

        if (savedInstanceState != null) {
//            mContactName = savedInstanceState.getString(STORED_CONTACT);
            setContactName(savedInstanceState.getString(STORED_CONTACT));
//            mProgressBar.setProgress(savedInstanceState.getInt(STORED_PROGRESS));
        } else {
            setContactName(null);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_FINISHED_GENERATE);
        filter.addAction(Constants.ACTION_UPDATE_PROGRESS);
        registerReceiver(mReceiver, filter);

        if (!BatchService.isRunning()) {
//            mContactName = null;
            setContactName(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STORED_CONTACT, mContactName);

//        if (mProgressBar != null) {
//            outState.putInt(STORED_PROGRESS, mProgressBar.getProgress());
//            outState.putString(STORED_CONTACT, mContactName);
//        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Constants.ACTION_FINISHED_GENERATE:
                    findViewById(R.id.progress_batch).setVisibility(View.GONE);
                    setContactName(null);
                    ((TextView) findViewById(R.id.text_contact_name)).setText("\u2713");
                    break;
                case Constants.ACTION_UPDATE_PROGRESS:
                    findViewById(R.id.progress_batch).setVisibility(View.VISIBLE);
//                    final int progress = intent.getIntExtra(Constants.EXTRA_PROGRESS, 33);
//                    mProgressBar.setProgress(progress);
                    setContactName(intent.getStringExtra(Constants.EXTRA_CONTACT));
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown action received: " + action);
                    break;
            }
        }
    };


    private void setContactName(final String contactName) {
        mContactName = contactName;

        final TextView nameView = (TextView) findViewById(R.id.text_contact_name);
        final View controlGroup = findViewById(R.id.layout_control);

        if (TextUtils.isEmpty(mContactName)) {
            nameView.setVisibility(View.GONE);
            controlGroup.setVisibility(View.GONE);
        } else {
            nameView.setVisibility(View.VISIBLE);
            controlGroup.setVisibility(View.VISIBLE);
            nameView.setText(mContactName);
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

    public void cancelPressed(View view) {
        Intent batchService = new Intent(this, BatchService.class);
        stopService(batchService);
        setContactName(null);
    }

    public void skipPressed(View view) {
    }

}
