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

import org.eztarget.micopi.Contact;

/**
 * Created by michel on 12/11/14.
 * <p/>
 * Fills a canvas with retro-styled squares
 */
public class PixelGenerator {

    /**
     * Number of squares per row (number of columns) and number of rows;
     * total number of painted squares is this value squared
     */

    private Painter mPainter;

    private Contact mContact;

    public PixelGenerator(final Painter painter, final Contact contact) {
        mPainter = painter;
        mContact = contact;
    }

    public void paint() {

        mPainter.disableShadows();

        final String md5String = mContact.getMD5EncryptedString();
        final int md5Length = md5String.length();

        final int color1 = ColorCollection.getColor(md5String.charAt(16));
        final int color2 = ColorCollection.getColor(md5String.charAt(17));

        int numberOfSquares = (md5String.charAt(15) % 10) + 15;
//        if (numberOfSquares % 2 == 0) ++numberOfSquares;

        final float sideLength = mPainter.getImageSize() / numberOfSquares;

        final boolean leftAligned = md5String.charAt(14) % 2 == 0;
        final boolean topAligned = md5String.charAt(13) % 2 == 0;

        int md5Index = 0;
        float x = 0f;
        float y = 0f;
        for (int i = 0; i < numberOfSquares; i++) {

            for (int j = 0; j < numberOfSquares; j++) {

                md5Index++;
                if (md5Index >= md5Length) md5Index = 0;
                final char md5Char = md5String.charAt(md5Index);

                if (x > 0 && y > 0) {
                    if (isOddParity(md5Char)) {
                        mPainter.paintSquare(
                                color1,
                                255 - md5Char % 100,
                                leftAligned ? (md5Char % y) : (numberOfSquares - (md5Char % y)),
                                topAligned ? (md5Char % x) : (numberOfSquares - (md5Char % x)),
                                sideLength
                        );
                    } else if (x % 2 == 0) {
                        mPainter.paintSquare(
                                color2,
                                255 - md5Char % 100,
                                leftAligned ? (md5Char % x) : (numberOfSquares - (md5Char % x)),
                                topAligned ? (md5Char % y) : (numberOfSquares - (md5Char % y)),
                                sideLength
                        );
                    }
                }
                ++y;
            }
            ++x;
            y = 0f;
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
