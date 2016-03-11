package org.eztarget.micopi.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.eztarget.micopi.ImageService;
import org.eztarget.micopi.R;
import org.eztarget.micopi.helper.DeviceHelper;


/**
 * Created by michel on 11/01/15.
 */
public class BatchActivity extends TaskActivity {


    public static final String ACTION_FINISHED_GENERATE = "finished_generating";

    public static final String ACTION_UPDATE_PROGRESS = "update_progress";

    private static final String TAG = BatchActivity.class.getSimpleName();

    private static final int PROGRESS_MAX = 100;

    private static final String SAVED_CONTACT = "contact";

    private static final String SAVED_PROGRESS = "progress";

    private String mContactName;

    private ImageService.CrawlMode mCrawlMode;

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

        if (savedInstanceState != null) {
            setProgress(
                    savedInstanceState.getString(SAVED_CONTACT),
                    savedInstanceState.getFloat(SAVED_PROGRESS)
            );
        } else {
            setProgress(null, 0f);
        }

        ((ProgressBar) findViewById(R.id.progress_batch)).setMax(PROGRESS_MAX);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_FINISHED_GENERATE);
        filter.addAction(ACTION_UPDATE_PROGRESS);
        registerReceiver(mReceiver, filter);

        if (ImageService.isRunning()) showControl();
        else setProgress(null, 0f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_PROGRESS, mContactName);
        outState.putFloat(
                SAVED_PROGRESS,
                ((ProgressBar) findViewById(R.id.progress_batch)).getProgress() / PROGRESS_MAX
        );
    }

    @Override
    protected void showSuccess() {
        super.showSuccess();
        hideControl();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_FINISHED_GENERATE:
                    showSuccess();
                    break;
                case ACTION_UPDATE_PROGRESS:
                    findViewById(R.id.progress_batch).setVisibility(View.VISIBLE);

                    setProgress(
                            intent.getStringExtra(ImageService.EXTRA_CONTACT_NAME),
                            intent.getFloatExtra(ImageService.EXTRA_PROGRESS, 0.5f)
                    );
                    break;
                default:
                    Log.e(TAG, "Unknown action received: " + action);
                    break;
            }
        }
    };

    private void showControl() {
        findViewById(R.id.group_batch_control).setVisibility(View.VISIBLE);
    }

    private void hideControl() {
        setProgress(null, 0f);
        fadeOutView(findViewById(R.id.group_batch_control));
    }

    private void setProgress(final String contactName, final float progress) {
        mContactName = contactName;
        if (progress > 0f) showControl();

        ((TextView) findViewById(R.id.text_contact_name)).setText(mContactName);
        ((ProgressBar) findViewById(R.id.progress_batch)).setProgress(
                (int) (progress * PROGRESS_MAX)
        );
    }

    public void generateAllPressed(View view) {
        showBatchConfirmDialog(ImageService.CrawlMode.All);
    }

    public void generateMissingPressed(View view) {
        showBatchConfirmDialog(ImageService.CrawlMode.Missing);
    }

    private void showBatchConfirmDialog(final ImageService.CrawlMode mode) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        if (mode == ImageService.CrawlMode.All) {
            dialog.setMessage(R.string.dialog_msg_all_contacts);
        } else {
            dialog.setTitle(R.string.dialog_title_please_note);
            dialog.setMessage(R.string.dialog_msg_missing);
        }

        // Alternatively react on pressing the OK button.
        dialog.setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        startCrawl(mode);
                    }
                }
        );

        dialog.setNegativeButton(
                android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mCrawlMode = null;
                    }
                }
        );

        dialog.show();
    }

    private void startCrawl(final ImageService.CrawlMode mode) {
        mCrawlMode = mode;

        if (hasStoragePermission()) {
            if (hasWriteContactsPermission()) {
                showControl();

                final Intent batchService = new Intent(this, ImageService.class);
                batchService.putExtra(ImageService.EXTRA_CRAWL_MODE, mCrawlMode);
                final int imageSize = DeviceHelper.getBestImageSize(this);
                batchService.putExtra(ImageService.EXTRA_IMAGE_SIZE, imageSize);
                startService(batchService);
            } else {
                requestWriteContactsPermission();
            }
        } else {
            requestStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            mCrawlMode = null;
            return;
        }

        switch (requestCode) {
            case WRITE_STORAGE_PERMISSION_CODE:
            case WRITE_CONTACTS_PERMISSION_CODE:
                if (mCrawlMode != null) startCrawl(mCrawlMode);
        }
    }

    public void cancelPressed(View view) {
        stopService(new Intent(this, ImageService.class));
        hideControl();
    }

}
