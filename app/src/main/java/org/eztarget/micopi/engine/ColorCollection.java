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

import android.graphics.Color;

/**
 * Created by michel on 27/10/14.
 * <p/>
 * Contains the color definitions and getter-generators that will be used by ImageFactory
 */
public class ColorCollection {

    /**
     * Based on colour palette from google.com/design/spec/style/color.html
     * Excuse the colour names.
     * These are just used to make sure that colours "next to each other" are not too similar.
     */
    public static final int PALETTE[] = {
            0xFF00E676,
            0xFFFF9800,
            0xFFCDDC39,
            0xFFFFA000,
            0xFFFFAB40,
            0xFF7C4DFF,
            0xFF0288D1,
            0xFFD32F2F,
            0xFF29B6F6,
            0xFFFFF59D,
            0xFFFFC107,
            0xFF7C4DFF,
            0xFFF44336,
            0xFFFFEB3B,
            0xFF2196F3,
            0xFFFF4081,
            0xFF536DFE,
            0xFFC2185B,
            0xFFF57C00,
            0xFF4CAF50,
            0xFF303F9F,
            0xFF3F51B5,
            0xFF03A9F4,
            0xFFAD1457,
            0xFFE64A19,
            0xFFFFF9C4,
            0xFFFF5722,
            0xFF00796B,
            0xFF009688,
            0xFFBBDEFB,
            0xFF80CBC4,
            0xFFFF5252,
            0xFF1976D2
    };

    /**
     * Additional high-contrast colours and black & white
     */
    public static final int HARSH_PALETTE[] = {
            Color.WHITE,
            Color.RED,
            0xFF212121,
            0xFF009688,
    };

    /**
     * Goes through the candy palette c amount of times.
     * Capital and lower case letters get the same colours.
     *
     * @param c ASCII integer value of this character will be used as array index
     * @return Color from the candy palette
     */
    public static int getColor(char c) {
        // If the given character is between lower case a and z,
        // subtract the index difference to the upper case characters.
        if (c >= 'a' && c <= 'z') c -= 32;

        final int index = c % (PALETTE.length - 1);

        return PALETTE[index];
    }

    /**
     * Takes a color from the candy or harsh palette and, in some cases, modifies its value
     *
     * @return A color with alpha = 255
     */
    public static int generateColor(char c1, int i1, int i2) {
        int color;

        switch (i1 % 5) {
            case 0:
                final int harshIndex = i2 % (PALETTE.length - 1);
                color = PALETTE[harshIndex];
                break;
            case 1:
                color = getColor(c1);
                color = ColorUtilities.getDarkenedColor(color);
                break;
            default:
                color = getColor(c1);
        }

        return color | 0xff000000;
    }
}
