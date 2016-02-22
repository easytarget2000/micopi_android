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

import org.eztarget.micopi.Contact;

/**
 * Created by michel on 12/11/14.
 * <p/>
 * Image generator
 */
public class PlatesGenerator {

    private static final String TAG = PlatesGenerator.class.getSimpleName();

    /**
     * Fills the Canvas in the Painter with a lot of colourful circles
     * or polygon approximations of circles
     *
     * @param painter Paint the generated shape in this object
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     */
    public static void generate(final Painter painter, final Contact contact) {
        // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
        // there is a 2/3 chance that polygons will be painted instead of circles.
        final String md5String = contact.getMD5EncryptedString();


        final int imageSize = painter.getImageSize();
        float angleOffset = 0;
        float width;
        width = ((md5String.charAt(7) * md5String.charAt(19)) / imageSize) * (imageSize * 1.2f);

        final float smallestRadius = (imageSize / 300) * md5String.charAt(22);

        // Draw all the shapes.
        final int md5Length = md5String.length();
        int md5Pos = 0;
        float x = painter.getImageSize() * 0.5f;
        float y = x;

        // The amount of double shapes that will be painted:
        int numOfShapes = contact.getNumberOfLetters();
        if (numOfShapes < 3) {
            numOfShapes = 3;
        } else if (numOfShapes > 9) {
            numOfShapes = 10;
        } else {
            numOfShapes = contact.getNumberOfLetters();
        }

        // Some pictures have polygon approximations instead of actual circles.
        final boolean paintPolygon;
        boolean paintRoundedSquare = false;

        int numOfEdges = contact.getNameWord(0).length();
        if (md5String.charAt(15) % 4 != 0) {
            paintPolygon = true;

            if (numOfEdges < 3) {
                numOfEdges = 3;
            }  else if (numOfEdges > 10) {
                numOfEdges = 10;
            } else if (numOfEdges == 4 && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
                paintRoundedSquare = true;
            }
        } else {
            paintPolygon = false;
        }

        float extraDividend = md5String.charAt(23);
        int movementValue;

        for (int i = 0; i < numOfShapes; i++) {
            // Get the next character from the MD5 String.
            md5Pos++;
            if (md5Pos >= md5Length) md5Pos = 0;

            // Move the coordinates around.
            movementValue = md5String.charAt(md5Pos) + i * 3;
            switch (movementValue % 6) {
                case 0:
                    x += movementValue;
                    y -= movementValue * 2;
                    break;
                case 1:
                    x -= movementValue * 2;
                    y += movementValue;
                    break;
                case 2:
                    x += movementValue * 2;
                    break;
                case 3:
                    y += movementValue * 3;
                    break;
                case 4:
                    x -= movementValue * 2;
                    y -= movementValue;
                    break;
                default:
                    x -= movementValue;
                    y -= movementValue * 2;
                    break;
            }

            if (paintPolygon) {
                if (paintRoundedSquare && (movementValue % 3 == 0)) {
                    painter.paintRoundedSquare(
                            ColorCollection.getColor(md5String.charAt(md5Pos)),
                            x,
                            y,
                            width
                    );
                } else {
                    angleOffset += extraDividend / movementValue;

                    painter.paintPolygon(
                            ColorCollection.getColor(md5String.charAt(md5Pos)),
                            angleOffset,
                            numOfEdges,
                            x,
                            y,
                            width
                    );
                }

            } else {
                painter.paintCircle(
                        ColorCollection.getColor(md5String.charAt(md5Pos)),
                        x,
                        y,
                        width
                );
            }

            if (width < smallestRadius) width *= 3.1f;
            else width *= 0.6f;
        }
    }
}
