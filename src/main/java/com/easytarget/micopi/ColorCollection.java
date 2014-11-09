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
 * Based on colour palette from google.com/design/spec/style/color.html
 */
public class ColorCollection {

    public static int palette[] = {
            0xFF3f51B5,
            0xFFE51C23,
            0xFFE91E63,
            0xFF9C27B0,
            0xFF673AB7,
            0xFF607D8B,
            0xFF5677fC,
            0xFF03A9f4,
            0xFF00BCD4,
            0xFF009688,
            0xFF259B24,
            0xFF8BC34A,
            0xFFCDDC39,
            0xFFffEB3B,
            0xFFffC107,
            0xFFff9800,
            0xFFff5722,
            0xFF795548,
    };

    public static int getColorForChar(char c) {

        // Capital and lower case letters get the same colours.
        // If the given character is between lower case a and z,
        // subtract the index difference to the upper case characters.
        if (c >= 'a' && c <= 'z') {
            c -= 32;
        }

        final int index = c % (palette.length - 1);

        int color = palette[index];
        //Log.d("getColorForChar()", c + ": " + index + ": " + Integer.toHexString(color));

        return color;
    }
    /**
     * Generates a color, based on the given input parameters.
     *
     * @param cFirstChar    First character of the contact's name
     * @param cFactor1  MD5 Character
     * @param cFactor2  MD5 Character
     * @param iNumberOfWords    Number of Words in the contact's name
     *
     * @return  Color with alpha=255
     */
    public static int generateColor(char c1, char c2, char c3, int factor) {

        int iGeneratedColor = Color.DKGRAY;
        if (c1 % 2 == 0) iGeneratedColor = getColorForChar(c2);

        iGeneratedColor *= c1 * -c2 * factor * c3;
        iGeneratedColor |= 0xff000000;

        return iGeneratedColor;
    }


}
