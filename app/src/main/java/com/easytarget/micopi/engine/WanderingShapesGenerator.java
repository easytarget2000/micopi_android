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

import com.easytarget.micopi.Contact;

/**
 * Created by michel on 12/11/14.
 * <p/>
 * Image generator
 */
public class WanderingShapesGenerator {

    private static final String TAG = WanderingShapesGenerator.class.getSimpleName();

    /**
     * Fills the Canvas in the Painter with a lot of colourful circles
     * or polygon approximations of circles
     *
     * @param painter Paint the generated shape in this object
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     */
    public static void generate(Painter painter, final Contact contact) {
        // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
        // there is a 2/3 chance that polygons will be painted instead of circles.
        final String md5String = contact.getMD5EncryptedString();


        final int imageSize = painter.getImageSize();
//            float extra = (float) (md5String.charAt(18) / painter.getImageSize()) * 10f;
        float extra = 0;
        float radius;
        radius = ((md5String.charAt(7) * md5String.charAt(19)) / imageSize) * (imageSize * 0.8f);

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
        final int paintMode;
        int numOfEdges = contact.getNameWord(0).length();

        if (md5String.charAt(15) % 2 == 0) {
            paintMode = Painter.MODE_POLYGON;

            if (numOfEdges < 3) numOfEdges = 3;
            else if (numOfEdges > 10) numOfEdges = 10;
        } else {
            paintMode = Painter.MODE_CIRCLE;
        }

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

//            Log.d(
//                    TAG,
//                    paintMode + ", " + extra + ", " + numOfEdges + ", "  + x + ", " + y + ", " + radius
//            );

            painter.paintShape(
                    paintMode,
                    ColorCollection.getColor(md5String.charAt(md5Pos)),
                    255,
                    extra,
                    numOfEdges,
                    x,
                    y,
                    radius
            );
            radius *= 0.5f;
        }
    }
}
