package org.eztarget.micopi.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

/**
 * Created by michel on 09/01/15.
 *
 */
public class DeviceHelper {

    public static int getBestImageSize(Context context) {

        final Configuration config = context.getResources().getConfiguration();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        // Store the height value as screen width, if in landscape mode.
        final int screenWidthInPixels;
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidthInPixels = (int) (config.screenWidthDp * dm.density);
        } else {
            screenWidthInPixels = (int) (config.screenHeightDp * dm.density);
        }

        // Determine the image side length, roughly depending on the screen width.
        // Old devices should not be unnecessarily strained,
        // but if the user takes these account pictures to another device,
        // they shouldn't look too horribly pixelated.
        if (screenWidthInPixels < 640) return 640;
        else if (screenWidthInPixels < 720) return 720;
        else if (screenWidthInPixels < 960) return 960;
        else if (screenWidthInPixels < 1200) return 1080;
        else return 1440;

    }
}
