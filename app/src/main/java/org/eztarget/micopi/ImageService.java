package org.eztarget.micopi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import org.eztarget.micopi.engine.ImageFactory;
import org.eztarget.micopi.helper.DatabaseHelper;
import org.eztarget.micopi.ui.BatchActivity;

import java.util.ArrayList;

/**
 *
 */
public class ImageService extends IntentService {

    public enum CrawlMode {
        All, Missing
    }

    private static final boolean SIMULATION = false;

    private static final String TAG = ImageService.class.getSimpleName();

    public static final String EXTRA_CRAWL_MODE = "mode";

    public static final String EXTRA_IMAGE_SIZE = "image_size";

    public static final String EXTRA_CONTACT_NAME = "name";

    public static final String EXTRA_PROGRESS = "progress";

    private static boolean sIsRunning = false;

    public static final int SERVICE_NOTIFICATION_ID = 441444;

    private boolean mIsCancelled;

    private int mScreenWidthPixels;

    public ImageService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (sIsRunning) return;

        mIsCancelled = false;

        startForeground(SERVICE_NOTIFICATION_ID, createNotification("", 0, 1));

        final CrawlMode mode = (CrawlMode) intent.getSerializableExtra(EXTRA_CRAWL_MODE);
        mScreenWidthPixels = intent.getIntExtra(EXTRA_IMAGE_SIZE, 1080);

        sIsRunning = true;
        processContacts(mode);

        getContentResolver().notifyChange(ContactsContract.Data.CONTENT_URI, null);

        sendBroadcast(new Intent(BatchActivity.ACTION_FINISHED_GENERATE));

        // TODO: Show finish notification.

        stopForeground(true);
        sIsRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsCancelled = true;
        stopForeground(true);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    private void processContacts(final CrawlMode mode) {
        if (mScreenWidthPixels < 100) return;

        final boolean allContacts = (mode == CrawlMode.All);

        final Contact[] contacts = getContacts(allContacts);

        final int maxProgress = contacts.length * 2;
        int currentProgress = 0;

        for (final Contact contact : contacts) {
            if (mIsCancelled) return;

            updateProgress(contact.getFullName(), ++currentProgress, maxProgress);
            final Bitmap bitmap = ImageFactory.bitmapFrom(
                    getApplicationContext(),
                    contact,
                    mScreenWidthPixels
            );

            if (mIsCancelled) return;

            updateProgress(contact.getFullName(), ++currentProgress, maxProgress);
            if (SIMULATION) {
                Log.d(TAG, "Simulating: Assigning image to " + contact.getFullName() + ".");
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "Assigning image to " + contact.getFullName() + ".");
                DatabaseHelper.assignImageToContact(
                        getApplicationContext(),
                        bitmap,
                        contact
                );
            }

        }

    }

    private static final String[] PROJECTION_INITIAL_QUERY = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_ID
    };

    private Contact[] getContacts(final boolean allContacts) {
        final Uri uri = ContactsContract.Contacts.CONTENT_URI;

        final Cursor cursor = getContentResolver().query(
                uri,
                PROJECTION_INITIAL_QUERY,
                ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1",
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
        );

        if (cursor == null) return new Contact[0];

        final ArrayList<Contact> contactList = new ArrayList<>();

        final ContentResolver contentResolver = getContentResolver();

        while (cursor.moveToNext() && !mIsCancelled) {

            // If not all contacts are supposed to be queried,
            // only get the one without a valid Photo ID.
            if (cursor.getInt(2) <= 0 || allContacts) {
                // Make sure this contact has a name.
                if (!TextUtils.isEmpty(cursor.getString(1))) {
                    contactList.add(DatabaseHelper.buildContact(
                            contentResolver,
                            cursor.getLong(0))
                    );
                }
            }
        }

        cursor.close();

        final Contact[] contacts = new Contact[contactList.size()];
        contactList.toArray(contacts);
        return contacts;
    }

    private void updateProgress(final String name, final int progress, final int maxProgress) {
        if (mIsCancelled) return;

        // Broadcast that will be received by the Activity:
        final Intent progressBroadcast = new Intent(BatchActivity.ACTION_UPDATE_PROGRESS);
        progressBroadcast.putExtra(EXTRA_CONTACT_NAME, name);
        if (maxProgress > 0) {
            progressBroadcast.putExtra(EXTRA_PROGRESS, (progress / (float) maxProgress));
        }
        sendBroadcast(progressBroadcast);

        final Notification notification = createNotification(name, progress, maxProgress);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                SERVICE_NOTIFICATION_ID,
                notification
        );
    }

    private Bitmap mLargeIcon;

    private Notification createNotification(
            final String text,
            final int progress,
            final int maxProgress
    ) {
        PendingIntent contentIntent = getNotificationIntent();

        if (mLargeIcon == null) {
            mLargeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }

        return new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(getResources().getString(R.string.auto_mode))
                .setContentText(text != null ? text : "0")
                .setSmallIcon(R.drawable.ic_account_circle_white_24dp)
                .setLargeIcon(mLargeIcon)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setProgress(maxProgress, progress, false)
                .build();
    }

    private PendingIntent getNotificationIntent() {
        Intent intent = new Intent(this, BatchActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
