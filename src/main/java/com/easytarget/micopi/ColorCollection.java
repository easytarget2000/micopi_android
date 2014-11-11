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

package com.easytarget.micopi;

import android.graphics.Color;
import android.util.Log;

/**
 * Created by michel on 27/10/14.
 *
 * Contains the color definitions and getter-generators that will be used by MicopiGenerator
 *
 */
public class ColorCollection {

    /**
     * Based on colour palette from google.com/design/spec/style/color.html
     */
    public static final int CANDY_PALETTE[] = {
            0xFF3f51B5,
            0xFFE51C23,
            0xFFE91E63,
            0xFF9C27B0,
            0xFF673AB7,
            0xFF607D8B,
            0xFF5677fC,
            0xFF03A9f4,
            0xFF00BCD4,
            0xFF259B24,
            0xFFCDDC39,
            0xFF8BC34A,
            0xFFffEB3B,
            0xFFffC107,
            0xFFff9800,
            0xFFff5722,
            0xFF795548,
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

        int color = CANDY_PALETTE[index];
//        Log.d("getCandyColorForChar()", c + ": " + index + ": " + Integer.toHexString(color));

        return color;
    }

    /**
     * Generate a color from the candy or harsh palette.
     *
     * @param c1 ASCII integer value of this character will be used as factor 1
     * @param c2 ASCII integer value of this character will be used as factor 2
     * @param i1 Used as factor 3
     * @param i2 Used as factor 4
     * @return A color with alpha = 255, possibly with altered values
     */
    public static int generateColor(char c1, char c2, int i1, int i2) {
        int color;

        if (i1 % 4 != 0) {
            color = getCandyColorForChar(c2);
        } else {
            final int harshIndex = i2 % HARSH_PALETTE.length;
            color = HARSH_PALETTE[harshIndex];
        }

        if (i2 % 2 == 0) {
            color *= c1 * -c2 * i2 * i1;
        }

        return color | 0xff000000;
    }
}
