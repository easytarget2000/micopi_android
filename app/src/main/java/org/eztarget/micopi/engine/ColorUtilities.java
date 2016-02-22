/*
 * Copyright (C) 2014 Easy Target
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eztarget.micopi.engine;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Functional class containing utility methods for dialogs and saving files.
 *
 * Created by Michel on 19.01.14.
 */
public class ColorUtilities {

    private static final int STEP_SIZE_PIXEL = 6;

    /**
     * @param bitmap Bitmap that will be processed
     * @return The average color of the input bitmap
     */
    public static int getAverageColor(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("getAverageColor()", "ERROR: No bitmap generated to get average colour from.");
            return Color.BLACK;
        }

        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixelCount = 0;
        int c = 0;

        for (int y = 0; y < height; y += STEP_SIZE_PIXEL) {
            for (int x = 0; x < width; x += STEP_SIZE_PIXEL) {
                c = bitmap.getPixel(x, y);

                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
                pixelCount++;
            }
            redBucket += Color.red(c);
        }

        return Color.rgb(
                redBucket / pixelCount,
                greenBucket / pixelCount,
                blueBucket / pixelCount
        );
    }

    /**
     *
     * @param color
     * @return
     */
    public static int getDarkenedColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
