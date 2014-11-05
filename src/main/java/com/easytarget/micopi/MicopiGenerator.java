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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

/**
 * Functional class containing the methods to generate a seemingly random image
 * out of given contact values, such as name and telephone number.
 *
 *
 * Created by Michel on 14.01.14.
 */
public class MicopiGenerator {

    /**
     * Generate the entire image.
     * @return The completed, generated image as a bitmap to be used by the GUI and contact handler.
     */
    public static Bitmap generateBitmap(Contact contact, int screenWidthInPixels) {
        // Determine the image side length, roughly depending on the screen width.
        // Old devices should not be unnecessarily strained,
        // but if the user takes these account pictures to another device,
        // they shouldn't look too horribly pixelated.
        int imageSize = 1080;
        if (screenWidthInPixels <= 600) imageSize = 640;
        else if (screenWidthInPixels < 1000) imageSize = 720;
        else if (screenWidthInPixels >= 1200) imageSize = 1440;

        Log.d("Image Size", imageSize + "");

        // Set up the bitmap and the canvas.
        Bitmap generatedBitmap = Bitmap.createBitmap(
                imageSize,
                imageSize,
                Bitmap.Config.RGB_565
        );

        Canvas canvas = new Canvas(generatedBitmap);

        final int backgroundColor = ColorCollection.getColorForChar(contact.getFullName().charAt(0));
        canvas.drawColor(backgroundColor);

        /*
        Most of the painting is done here:
        */
        generateCircleScape(canvas, contact);

        /*
        A name with three or more words is more likely to get the circles.
        The higher the number the less likely circles are.
        */
        int circleProbFactor = 4;
        if (contact.getNumberOfNameParts() > 2) circleProbFactor = 2;

        String md5String = contact.getMD5EncryptedString();
        final int numberOfWords = contact.getNumberOfNameParts();
        final float centerX = imageSize * (md5String.charAt(9) / 128f);
        final float centerY = imageSize * (md5String.charAt(3) / 128f);
        final float offset  = md5String.charAt(18) * 2f;
        final float radiusFactor = imageSize * 0.4f;
        Log.d("Geometric Addition Center", centerX + " " + centerY);

        switch (md5String.charAt(20) % circleProbFactor) {
            case 0:     // Paint circles depending on the number of words.
                for (int i = 0; i < numberOfWords; i++) {
                    int alpha = (int) (((i + 1f) / numberOfWords) * 150f);

                    MicopiPainter.paintDoubleShape(
                            canvas,
                            MicopiPainter.MODE_CIRCLE,
                            Color.WHITE,
                            alpha,
                            ((numberOfWords / (i + 1)) * 64), // Stroke Width
                            0,                              // No edges
                            0f,                             // No arc start angle
                            0f,                             // No arc end angle
                            centerX + (i * offset),
                            centerY - (i * offset),
                            ((numberOfWords / (i + 1f)) * radiusFactor)
                    );
                    Log.d("Word Circles", alpha + "");
                }
                break;
            default:    // Paint that flower.
                md5String = contact.getMD5EncryptedString();
                MicopiPainter.paintMicopiBeams(
                        canvas,
                        backgroundColor,
                        md5String.charAt(17) / 3,           // Alpha
                        md5String.charAt(12) % 4,       // Paint Mode
                        centerX,
                        centerY,
                        md5String.charAt(13) * 3,       // Density
                        md5String.charAt(5) * 0.6f,     // Line Length
                        md5String.charAt(14) * 0.15f,   // Angle
                        md5String.charAt(20) % 2 == 0,  // Large Delta Angle
                        md5String.charAt(21) % 2 == 0   // Wide Strokes
                );
        }

        // Write the initial(s).
        char[] initials = {contact.getFullName().charAt(0)};
        MicopiPainter.paintChars(canvas, initials, Color.WHITE);

        return generatedBitmap;
    }

    /**
     * Fills a canvas with a lot of colourful circles or polygons
     * Uses MicopiPainter
     */
    private static void generateCircleScape(Canvas canvas, Contact contact) {
        // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
        // there is a 3/4 chance that polygons will be painted instead of circles.
        final int numOfEdges = contact.getNamePart(0).length();
        final String md5String = contact.getMD5EncryptedString();
        final int numberOfShapes  = contact.getFullName().length() * 4;

        boolean paintPolygon = false;
        if (md5String.charAt(15) % 4 != 0 && numOfEdges > 2 && numOfEdges < 7) paintPolygon = true;

        // These characters will be used for color generating:
        final char colorChar1     = contact.getFullName().charAt(0);
        final int lastNamePart    = contact.getNumberOfNameParts() - 1;
        final char colorChar2     = contact.getNamePart(lastNamePart).charAt(0);

        // Determine if the shapes will be painted filled or stroked.
        boolean paintFilled = false;
        if (md5String.charAt(0) % 2 == 0) paintFilled = true;

        // Determine the alpha value to paint with.
        int alpha  = md5String.charAt(6) * 2;
        // Filled shapes have a smaller alpha value.
        if (paintFilled) alpha *=2;
        //Log.i("Circle Scape", "Alpha: " + alpha + " paintFilled: " + paintFilled);
        // This was 0.1
        float shapeWidth = (float) md5String.charAt(7);

        // Determine if to paint occasional arcs or not.
        boolean paintArc = true;
        float endAngle = md5String.charAt(8) * 2;
        if (md5String.charAt(1) % 2 == 0) paintArc = false;

        // Draw all the shapes.
        int md5Length       = md5String.length();
        int md5Pos          = 0;
        float x = canvas.getWidth() * 0.7f;
        float y = canvas.getHeight() * 0.3f;

        for (int i = 0; i < numberOfShapes; i++) {
            char md5Char = ' ';

            // Do the operation for the x- and y-coordinate.
            for (int axis = 0; axis < 2; axis++) {
                // Make sure we do not jump out of the MD5 String.
                if (md5Pos >= md5Length) md5Pos = 0;

                // Move the coordinates around.
                md5Char = md5String.charAt(md5Pos);
                if (md5Char % 2 == 0) {
                    if (axis == 0) x += md5Char;
                    else y -= md5Char;
                }
                else {
                    if (axis == 0) x -= md5Char;
                    else y += md5Char;
                }
                md5Pos++;
            }

            int paintMode = MicopiPainter.MODE_CIRCLE;
            if (paintArc && md5Char % 4 == 0) paintMode = MicopiPainter.MODE_ARC;
            else if (paintPolygon) paintMode = MicopiPainter.MODE_POLYGON;
            //if (paintFilled) paintMode += MicopiPainter.MODE_CIRCLE_FILLED;

            // The new coordinates have been generated. Paint something.
            MicopiPainter.paintDoubleShape(
                    canvas,
                    paintMode,
                    ColorCollection.generateColor(colorChar1, colorChar2, md5Char, i + 1),
                    alpha,
                    shapeWidth,
                    numOfEdges,
                    md5Char * 2,
                    endAngle,
                    x,
                    y,
                    i * md5String.charAt(2)         // Radius
            );
            shapeWidth += .05f;
        }
    }
}
