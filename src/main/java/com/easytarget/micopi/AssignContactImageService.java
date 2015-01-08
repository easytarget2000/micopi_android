package com.easytarget.micopi;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by michel on 08/01/15.
 *
 */
public class AssignContactImageService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AssignContactImageService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
