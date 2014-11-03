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

import android.util.Log;

/**
 * Created by michel on 27/10/14.
 *
 * Based on colour palette from google.com/design/spec/style/color.html
 */
public class ColorCollection {

    public static int palette[] = {
            0xFF000000,
            0xFFE51C23,
            0xFFE91E63,
            0xFF9C27B0,
            0xFF673AB7,
            0xFF3f51B5,
            0xFF5677fC,
            0xFF03A9f4,
            0xFF777777,
            0xFFAAAAAA,
            0xFF00BCd4,
            0xFF009688,
            0xFF259B24,
            0xFF8BC34A,
            0xFFCDDC39,
            0xFFffEB3B,
            0xFFffC107,
            0xFFff9800,
            0xFFff5722,
            0xFF795548,
            0xFF9E9E9E,
            0xFF607D8B,
            0xFFFFFFFF
    };

    public static int getColorForChar(char c) {
        int index = palette.length - 1;

        if (c >= 'A' && c <= 'Z') {
            index = c - 'A';
            index %= palette.length;
        } else if (c >= 'a' && c <= 'z') {
            index = c - 'a';
            index %= palette.length;
        }

        Log.d("ColorCollection: getColorForChar()", "Index: " + index + " Color: " + palette[index]);
        return palette[index];
    }


}
