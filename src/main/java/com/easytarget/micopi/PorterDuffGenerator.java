package com.easytarget.micopi;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

/**
 * Created by michel on 10/11/14.
 */
public class PorterDuffGenerator {

    private static final int NUM_OF_MODES = PorterDuff.Mode.values().length - 1;


    public static PorterDuffXfermode getXfermode(int i) {

        if (i > NUM_OF_MODES) i = i % NUM_OF_MODES;

        final PorterDuff.Mode mode = PorterDuff.Mode.values()[i];

        Log.d("PorterDuffGenerator", i + " / " + NUM_OF_MODES + " " + mode.toString());

        return new PorterDuffXfermode(mode);
    }
}
