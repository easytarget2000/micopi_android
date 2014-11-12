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

import android.graphics.Canvas;
import android.graphics.Color;

import com.easytarget.micopi.Contact;

/**
 * Created by michel on 12/11/14.
 *
 */
public class SquareMatrixGenerator {

    /**
     * Fills a canvas with retro-styled squares
     *
     * @param fCanvas Canvas to draw on
     * @param fContact Data from this Contact object will be used to generate the shapes and colors
     */
    public static void generate(final Canvas fCanvas, final Contact fContact) {

        final String fMd5String = fContact.getMD5EncryptedString();
        final int fMd5Length    = fMd5String.length();

        final int fColor1 = ColorCollection.generateColor(
                fContact.getFullName().charAt(0),
                fMd5String.charAt(10),
                fContact.getNumberOfNameWords(),
                fMd5String.charAt(11)
        );
        final int fColor2 = Color.WHITE;
        final int fColor3 = ColorCollection.getCandyColorForChar(fMd5String.charAt(16));

        int numOfSquares = 5;
        if (fContact.getNameWord(0).length() % 2 == 0) numOfSquares -= 1;
        final float fImageSizeDiv = fCanvas.getWidth() / numOfSquares;

        int md5Pos = 0;
        for (int y = 0; y < numOfSquares; y++) {
            for (int x = 0; x < 3; x++) {
                md5Pos++;
                if (md5Pos >= fMd5Length) md5Pos = 0;
                final char fMd5Char = fMd5String.charAt(md5Pos);

                if (isOddParity(fMd5Char)) {
                    Painter.paintSquare(
                            fCanvas,
                            true,
                            fColor1,
                            255,
                            x,
                            y,
                            fImageSizeDiv
                    );
                    if (x == 0) {
                        Painter.paintSquare(
                                fCanvas,
                                true,
                                fColor2,
                                200 - fMd5Char,
                                4,
                                y,
                                fImageSizeDiv
                        );
                    }
                    if (x == 1) {
                        Painter.paintSquare(
                                fCanvas,
                                true,
                                fColor3,
                                200 - fMd5Char,
                                3,
                                y,
                                fImageSizeDiv
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
