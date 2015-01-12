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
package com.easytarget.micopi.engine;

import android.graphics.Color;

import com.easytarget.micopi.Contact;

/**
 * Created by michel on 12/11/14.
 *
 */
public class SquareMatrixGenerator {

    /**
     * Number of squares per row (number of columns) and number of rows;
     * total number of painted squares is this value squared
     */
    private static final int NUM_OF_SQUARES = 5;

    /**
     * Fills a canvas with retro-styled squares
     *
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     */
    public static void generate(Painter painter, final Contact contact) {
        if (painter == null || contact == null) return;

        final String md5String = contact.getMD5EncryptedString();
        final int md5Length = md5String.length();

        final int color1 = ColorCollection.getCandyColorForChar(md5String.charAt(16));
        final int color2 = Color.WHITE;
        final int color3 = ColorCollection.getCandyColorForChar(md5String.charAt(17));

        int numOfSquares = NUM_OF_SQUARES;
        if (contact.getNameWord(0).length() % 2 == 0) numOfSquares -= 1;
        final float sideLength = painter.getImageSize() / numOfSquares;

        int md5Pos = 0;
        for (int y = 0; y < numOfSquares; y++) {
            for (int x = 0; x < 3; x++) {
                md5Pos++;
                if (md5Pos >= md5Length) md5Pos = 0;
                final char md5Char = md5String.charAt(md5Pos);

                if (isOddParity(md5Char)) {
                    painter.paintSquare(
                            true,       // doPaintFilled
                            color1,
                            255,        // Alpha
                            x,
                            y,
                            sideLength
                    );
                    if (x == 0) {
                        painter.paintSquare(
                                true,
                                color2,
                                200 - md5Char,
                                4,
                                y,
                                sideLength
                        );
                    }
                    if (x % 2 == 1) {
                        painter.paintSquare(
                                true,
                                color3,
                                200 - md5Char,
                                3,
                                y,
                                sideLength
                        );
                    }
                }
            }
        }
    }

    private static boolean isOddParity(final char fChar) {
        int bb = fChar;
        short bitCount = 0;
        for (int i = 0; i < 16; i++, bb >>= 1) {
            if ((bb & 1) != 0) bitCount++;
        }

        return (bitCount & 1) != 0;
    }
}
