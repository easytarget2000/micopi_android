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
 *
 * Contains the color definitions and getter-generators that will be used by ImageFactory
 *
 */
public class ColorCollection {

    /**
     * Based on colour palette from google.com/design/spec/style/color.html
     * Excuse the colour names.
     * These are just used to make sure that colours "next to each other" are not too similar.
     */
    public static final int CANDY_PALETTE[] = {
            0xFF3f51B5, // Navy blue
            0xFFAEEA00, // Neon yellow-green
            0xFFE91E63, // Salmon
            0xFFFFD600, // Cinnamon yellow
            0xFF9C27B0, // Purple
            0xFFDD2C00, // Dark deep-orange
            0xFF673AB7, // Blue-purple
            0xFFFFC107, // Pale orange
            0xFF84FFFF, // Light cyan
            0xFFFF9800, // Neon orange
            0xFF5677FC, // Blue
            0xFFAD1457, // Dark pink
            0xFFFFEB3B, // Pale yellow
            0xFF03A9f4, // Baby blue
            0xFFFFA726, // Orange
            0xFF259B24, // Woodruff
            0xFFCDDC39, // Spring green
            0xFF8BC34A, // Light green
            0xFFFF5722, // Neon red
            0xFFC6FF00, // Less-pale yellow
    };

    /**
     * Additional high-contrast colours and black & white
     */
    public static final int HARSH_PALETTE[] = {
            Color.WHITE,
            Color.RED,
            Color.BLACK,
            0xFF009688,
    };

    /**
     * Goes through the candy palette c amount of times.
     * Capital and lower case letters get the same colours.
     *
     * @param c ASCII integer value of this character will be used as array index
     * @return Color from the candy palette
     */
    public static int getCandyColorForChar(char c) {
        // If the given character is between lower case a and z,
        // subtract the index difference to the upper case characters.
        if (c >= 'a' && c <= 'z') c -= 32;

        final int index = c % (CANDY_PALETTE.length - 1);

        return CANDY_PALETTE[index];
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
                final int harshIndex = i2 % (HARSH_PALETTE.length - 1);
                color = HARSH_PALETTE[harshIndex];
                break;
            case 1:
                color = getCandyColorForChar(c1);
                color =  ColorUtilities.getDarkenedColor(color);
                break;
            default:
                color = getCandyColorForChar(c1);
        }

        return color | 0xff000000;
    }
}
