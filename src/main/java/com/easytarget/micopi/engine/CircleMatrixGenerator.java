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
public class CircleMatrixGenerator {

    /**
     * Fills the image with circles in a grid;
     * The grid size is always "number of letters in the first name ^ 2".
     *
     * @param canvas Canvas to draw on
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     */
    public static void generate(Painter painter, Contact contact) {
        // Prepare painting values based on image size and contact data.
        final String md5String = contact.getMD5EncryptedString();

        // Use the length of the first name as the number of shapes per row and column.
        int shapesPerRow = contact.getNameWord(0).length();
        // If the first name is too short, use the length of the entire name.
        if (shapesPerRow < 4) shapesPerRow = contact.getNumberOfLetters();
        // If the full name is too short, double the amount of letters.
        if (shapesPerRow < 5) shapesPerRow *= 2;

        final int imageSize = painter.getImageSize();
        final float fStrokeWidth = md5String.charAt(12) * 2f;
        final float circleDistance = (imageSize / shapesPerRow)
                + (imageSize / (shapesPerRow * 2f));

        // Contact names with just one word will not get coloured circles.
        final boolean doGenerateColor = contact.getNumberOfNameWords() > 1;

        int md5Pos = 0;
        int color = Color.WHITE;
        for (int y = 0; y < shapesPerRow; y++) {
            for (int x = 0; x < shapesPerRow; x++) {

                md5Pos++;
                if (md5Pos >= md5String.length()) md5Pos = 0;
                final char md5Char = md5String.charAt(md5Pos);

                if (doGenerateColor) color = ColorCollection.getCandyColorForChar(md5Char);

                final int index = y * shapesPerRow + x;
                final float radius;
                if ((index & 1) == 0) radius = md5Char * 2f;
                else radius = md5Char * 3f;

                painter.paintDoubleShape(
                        Painter.MODE_CIRCLE_FILLED,
                        color,
                        200 - md5String.charAt(md5Pos) + index,
                        fStrokeWidth,        // Stroke width
                        0,
                        0f,
                        0f,
                        x * circleDistance,
                        y * circleDistance,
                        radius
                );
            }
        }
    }

}
