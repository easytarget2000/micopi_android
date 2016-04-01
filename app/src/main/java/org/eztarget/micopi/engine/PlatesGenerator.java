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

import android.os.Build;
import android.util.Log;

import org.eztarget.micopi.Contact;


/**
 * Created by michel on 12/11/14.
 *
 * Image generator
 * Fills the Canvas in the Painter with a lot of colourful circles
 * or polygon approximations of circles
 */
public class PlatesGenerator {

    private static final String TAG = PlatesGenerator.class.getSimpleName();

    private Painter mPainter;

    private Contact mContact;

    public PlatesGenerator(final Painter painter, final Contact contact) {
        mPainter = painter;
        mContact = contact;
    }

    public void paint() {
        // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
        // there is a 2/3 chance that polygons will be painted instead of circles.
        final String md5String = mContact.getMD5EncryptedString();

        final int imageSize = mPainter.getImageSize();
        float angleOffset = 0;
        final float factor = (float) md5String.charAt(7) / (float) md5String.charAt(19);
        float width;
        width = (factor * imageSize);

        final float minWidth = (imageSize / 300) * md5String.charAt(22);

        final int md5Length = md5String.length();
        float x = imageSize * 0.5f;
        float y = x;

        final int numberOfPlates = (md5String.charAt(28) % 6) + 2;

        Log.d(TAG, "width: " + width + " smallest: " + minWidth + " number: " + numberOfPlates);

        // Some pictures have polygon approximations instead of actual circles.
        final boolean paintPolygon;
        boolean paintRoundedSquares = false;

        int numberOfEdges = mContact.getNameWord(0).length();
        if (md5String.charAt(27) % 5 != 0) {
            paintPolygon = true;

            if (numberOfEdges < 3) {
                numberOfEdges = 3;
            }  else if (numberOfEdges > 10) {
                numberOfEdges = 10;
            } else if (numberOfEdges == 4 &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                paintRoundedSquares = true;
            }
        } else {
            paintPolygon = false;
        }

        float extraDividend = md5String.charAt(23);
        int md5Pos = 0;

        mPainter.enableShadows();

        for (int i = 0; i < numberOfPlates; i++) {

            // Get the next character from the MD5 String.
            md5Pos++;
            if (md5Pos >= md5Length) md5Pos = 0;

            // Move the coordinates around.
            final int md5Char = md5String.charAt(md5Pos) + i * 3;
            final Painter.Texture texture;

            switch (md5Char % 6) {
                case 0:
                    x += md5Char;
                    y -= md5Char * 2;
                    mPainter.setShadowLayer((((md5Char % 15) / 15f) + 0.7f), 2f, 2f);
                    texture = Painter.Texture.MARBLE;
                    break;
                case 1:
                    x -= md5Char * 2;
                    y += md5Char;
                    texture = Painter.Texture.NONE;
                    break;
                case 2:
                    x += md5Char * 2;
                    texture = Painter.Texture.GRAIN;
                    break;
                case 3:
                    y += md5Char * 3;
                    texture = Painter.Texture.TOWEL;
                    break;
                case 4:
                    x -= md5Char * 2;
                    y -= md5Char;
                    mPainter.setShadowLayer((((md5Char % 15) / 15f) + 1f), i + 1f, md5Char);
                    texture = Painter.Texture.GRAIN;
                    break;
                default:
                    x -= md5Char;
                    y -= md5Char * 2;
                    mPainter.setShadowLayer((((md5Char % 15) / 15) + 1.5f), 1f, 1f);
                    texture = Painter.Texture.NONE;
                    break;
            }


            if (paintPolygon) {
                if (paintRoundedSquares && (md5Char % 3 == 0)) {
                    mPainter.paintRoundedSquare(
                            ColorCollection.getColor(md5String.charAt(md5Pos)),
                            texture,
                            x,
                            y,
                            width
                    );
                } else {
                    angleOffset += extraDividend / md5Char;

                    mPainter.paintPolygon(
                            ColorCollection.getColor(md5String.charAt(md5Pos)),
                            texture,
                            angleOffset,
                            numberOfEdges,
                            x,
                            y,
                            width
                    );
                }

            } else {
                mPainter.paintCircle(
                        ColorCollection.getColor(md5String.charAt(md5Pos)),
                        texture,
                        x,
                        y,
                        width
                );
            }

            if (width < minWidth) width *= 3.1f;
            else width *= 0.6f;
        }

    }
}
