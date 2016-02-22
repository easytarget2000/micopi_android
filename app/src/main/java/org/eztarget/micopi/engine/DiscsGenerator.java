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
 *
 * Image generator
 */
public class DiscsGenerator {

    /**
     * Fills the Canvas in the Painter with circles in a grid
     *
     * @param painter Paint the generated shape in this object
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     */
    public static void generate(final Painter painter, final Contact contact) {
        if (painter == null || contact == null) return;

        // Prepare painting values based on image size and contact data.
        final String md5String = contact.getMD5EncryptedString();

        // Use the length of the first name to determine the number of shapes per row and column.
        final int firstNameLength = contact.getNameWord(0).length();

        final int shapesPerRow;
        if (firstNameLength < 3) {
            shapesPerRow = 3;
        } else if (firstNameLength > 8) {
            shapesPerRow = 8;
        } else {
            shapesPerRow = firstNameLength;
        }

        final int imageSize = painter.getImageSize();
        float circleDistance;
        circleDistance = (imageSize / shapesPerRow);

        int md5Pos = 0;
        int index;
        float radius;
        int color;
        char md5Char;
        for (int y = 0; y < shapesPerRow; y++) {
            for (int x = 0; x < shapesPerRow; x++) {

                md5Pos++;
                if (md5Pos >= md5String.length()) md5Pos = 0;
                md5Char = md5String.charAt(md5Pos);

                color = ColorCollection.generateColor(md5Char, firstNameLength, shapesPerRow);

                index = y * shapesPerRow + x;
                if ((index & 1) == 0) radius = md5Char * 6f;
                else radius = md5Char * 5f;

                painter.paintCircle(
                        color,
                        x * circleDistance,
                        y * circleDistance,
                        radius
                );
                circleDistance *= 1.1f;
            }
        }
    }

}
