package org.eztarget.micopi;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

/**
 * Created by michel on 09/01/15.
 *
 */
public class DeviceHelper {

    private static final int SMALLEST_IMAGE_SIZE = 480;

    public static int getBestImageSize(Context context) {

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) && context != null) {
            Configuration config = context.getResources().getConfiguration();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();

            // Store the height value as screen width, if in landscape mode.
            final float screenWidthInPixels;
            if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                screenWidthInPixels = config.screenWidthDp * dm.density;
            } else {
                screenWidthInPixels = config.screenHeightDp * dm.density;
            }

            // Determine the image side length, roughly depending on the screen width.
            // Old devices should not be unnecessarily strained,
            // but if the user takes these account pictures to another device,
            // they shouldn't look too horribly pixelated.
            if (screenWidthInPixels <= SMALLEST_IMAGE_SIZE) return SMALLEST_IMAGE_SIZE;
            else if (screenWidthInPixels <= 600) return 640;
            else if (screenWidthInPixels < 1000) return 720;
            else if (screenWidthInPixels >= 1200) return 1440;
            else return 1080;
        } else {
            // On old android versions, a generic, small screen resolution is assumed.
            return SMALLEST_IMAGE_SIZE;
        }
    }
}
