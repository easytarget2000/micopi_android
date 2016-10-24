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

import android.util.Log;

import org.eztarget.micopi.Contact;


/**
 * Created by michel on 12/11/14.
 *
 * Image generator
 * Fills the Canvas in the Painter with a lot of colourful circles
 * or polygon approximations of circles
 */
class MaterialGenerator {

    private static final String TAG = MaterialGenerator.class.getSimpleName();

    private Painter mPainter;

    private Contact mContact;

    MaterialGenerator(final Painter painter, final Contact contact) {
        mPainter = painter;
        mContact = contact;
    }

    void paint() {

        mPainter.enableShadows();

        // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
        // there is a 2/3 chance that polygons will be painted instead of circles.
        final String md5String = mContact.getMD5EncryptedString();

        final float gridSize = mPainter.getImageSize() / 10f;

        final int md5Length = md5String.length();

        final int numberOfElements = (md5String.charAt(0) % 3) + 2;

//        final boolean paintRoundedSquares = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        final boolean paintRoundedSquares = false;

        final int shadowChar = md5String.charAt(2);
        switch (shadowChar % 6) {
            case 0:
                mPainter.setShadowLayer((((shadowChar % 15) / 15f) + 0.7f), 2f, 2f);
                break;
            case 1:
                mPainter.setShadowLayer((((shadowChar % 15) / 15f) + 1f), 3f, 3f);
                break;
            default:
                mPainter.setShadowLayer((((shadowChar % 15) / 15) + 1.5f), 1f, 1f);
                break;
        }

        for (int i = 1; i <= numberOfElements; i++) {

            // Get the next character from the MD5 String.
            final int md5Pos = md5Length % i;

            // Move the coordinates around.
            final int md5Char = md5String.charAt(md5Pos) + (i % 3) * 4;

            Log.d(TAG, "i: " + i + ", md5: " + md5Char + ", pos: " + md5Pos);

            final float widthUnits = ((md5Char * i) % (9 - i)) + (md5Char / 49f);
            Log.d(TAG, "Width rel: " + widthUnits);

            final float gridXPos = ((md5Char % 5) * 2f) + (md5Char / 51f);
            final float gridYPos = -1f + ((i % 5) * 2f) + (md5Char / 52f);
            Log.d(TAG, "x: " + gridXPos);
            Log.d(TAG, "y: " + gridYPos);

            final int shape = (((md5Char * i) * md5Char) % 7) - i;
            Log.d(TAG, "Shape: " + shape);

            if (shape == 0) {
                mPainter.paintCircle(
                        ColorCollection.getColor(md5String.charAt(md5Pos)),
                        1,
                        gridSize * gridXPos,
                        gridSize * gridYPos,
                        gridSize * widthUnits
                );

            } else if (shape < 5) {

                mPainter.paintPolygon(
                        ColorCollection.getColor(md5String.charAt(md5Pos)),
                        2,
                        md5Char / (i + 1f),
                        (md5Char + i + shape) % 5 + 3,
                        gridSize * gridXPos,
                        gridSize * gridYPos,
                        gridSize * widthUnits
                );
            } else {
                if (paintRoundedSquares && shape == 2) {
                    mPainter.paintRoundedSquare(
                            ColorCollection.getColor(md5Char),
                            1,
                            gridSize * gridXPos,
                            gridSize * gridYPos,
                            gridSize * widthUnits
                    );
                } else {
                    mPainter.paintSquare(
                            ColorCollection.getColor(md5Char),
                            2,
                            gridSize * gridXPos,
                            gridSize * gridYPos,
                            gridSize * widthUnits
                    );
                }
            }

        }

        mPainter.disableShadows();
    }
}
