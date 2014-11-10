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
     *
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     * @param screenWidthInPixels Width of device screen in pixels; height in landscape mode
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
                Bitmap.Config.ARGB_8888
        );

        // Set up a new canvas
        // and fill the background with the color for this contact's first letter.
        Canvas canvas = new Canvas(generatedBitmap);
        final char fFirstChar = contact.getFullName().charAt(0);
        final int fBackgroundColor = ColorCollection.getCandyColorForChar(fFirstChar);
        canvas.drawColor(fBackgroundColor);

        // The contact's current MD5 encoded string will be referenced a lot.
        final String md5String = contact.getMD5EncryptedString();

        /*
        MAIN PATTERN
        */

        switch (md5String.charAt(20) % 2) {
            case 1:
                generateWanderingShapes(canvas, contact);
                break;
//            case 2:
//                final int fColor3 = ColorCollection.generateColor(
//                        fFirstChar,
//                        md5String.charAt(0),
//                        md5String.charAt(12),
//                        contact.getFullName().length()
//                );
//                MicopiPainter.paintSpyro(
//                        canvas,
//                        Color.WHITE,
//                        ColorCollection.getCandyColorForChar(md5String.charAt(28)),
//                        fColor3,
//                        255 - md5String.charAt(18),
//                        (0.5f - (float) fFirstChar / 255f),
//                        (0.5f - (float) md5String.charAt(25) / 255f),
//                        (0.5f - (float) md5String.charAt(26) / 255f),
//                        Math.max(5, md5String.charAt(27) >> 2)
//                );
//                break;
            default:
                generateCircleMatrix(canvas, contact);
                break;
        }

        /*
        ADDITIONAL SHAPES
         */

        final int numberOfWords = contact.getNumberOfNameParts();
        final float centerX = imageSize * (md5String.charAt(9) / 128f);
        final float centerY = imageSize * (md5String.charAt(3) / 128f);
        final float offset  = md5String.charAt(18) * 2f;
        final float radiusFactor = imageSize * 0.4f;
        Log.d("Geometric Addition Center", centerX + " " + centerY);

        switch (md5String.charAt(20) % 4) {
            case 0:     // Paint circles depending on the number of words.
                for (int i = 0; i < numberOfWords; i++) {
                    int alpha = (int) (((i + 1f) / numberOfWords) * 120f);

                    MicopiPainter.paintDoubleShape(
                            canvas,
                            MicopiPainter.MODE_CIRCLE,
                            Color.WHITE,
                            alpha,
                            ((numberOfWords / (i + 1f)) * 80f), // Stroke Width
                            0,                              // No edges
                            0f,                             // No arc start angle
                            0f,                             // No arc end angle
                            centerX + (i * offset),
                            centerY - (i * offset),
                            ((numberOfWords / (i + 1f)) * radiusFactor)
                    );
                    //Log.d("Word Circles", alpha + "");
                }
                break;
            case 1:
                MicopiPainter.paintSpyro(
                        canvas,
                        Color.WHITE,
                        Color.YELLOW,
                        Color.BLACK,
                        255 - (md5String.charAt(19) * 2),
                        (0.3f - (float) fFirstChar / 255f),
                        (0.3f - (float) md5String.charAt(25) / 255f),
                        (0.3f - (float) md5String.charAt(26) / 255f),
                        Math.max(5, md5String.charAt(27) >> 2)
                );
                break;
            case 2:
                MicopiPainter.paintMicopiBeams(
                        canvas,
                        fBackgroundColor,
                        md5String.charAt(17) / 5,       // Alpha
                        md5String.charAt(12) % 4,       // Paint Mode
                        centerX,
                        centerY,
                        md5String.charAt(13) * 3,       // Density
                        md5String.charAt(5) * 0.6f,     // Line Length
                        md5String.charAt(14) * 0.15f,   // Angle
                        md5String.charAt(20) % 2 == 0,  // Large Delta Angle
                        md5String.charAt(21) % 2 == 0   // Wide Strokes
                );
                break;
        }

        /*
        INITIAL LETTER ON CIRCLE
         */

        MicopiPainter.paintCentralCircle(canvas, fBackgroundColor, (255 - md5String.charAt(27) * 2));
        MicopiPainter.paintChars(canvas, new char[]{fFirstChar}, Color.WHITE);

        return generatedBitmap;
    }

    /**
     * Fills a canvas with a lot of colourful circles or polygon approximations of circles
     * Uses MicopiPainter
     *
     * @param canvas Canvas to draw on
     * @param contact Data from this Contact object will be used to generate the shapes
     */
    private static void generateWanderingShapes(Canvas canvas, Contact contact) {
        // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
        // there is a 2/3 chance that polygons will be painted instead of circles.
        final int numOfEdges = contact.getNamePart(0).length();
        final String md5String = contact.getMD5EncryptedString();

        // Some pictures have polygon approximations instead of actual circles.
        boolean paintPolygon = false;
        if (md5String.charAt(15) % 3 != 0 && numOfEdges > 2 && numOfEdges < 7) paintPolygon = true;

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
        if (paintFilled) alpha /= 2;
        //Log.i("Circle Scape", "Alpha: " + alpha + " paintFilled: " + paintFilled);
        // This was 0.1
        float shapeWidth = (float) md5String.charAt(7) * 2f;

        // Determine if to paint occasional arcs or not.
        boolean paintArc = true;
        final float endAngle = md5String.charAt(8) * 2;
        if (md5String.charAt(1) % 2 == 0) paintArc = false;

        // Draw all the shapes.
        final int md5Length  = md5String.length();
        int md5Pos = 0;
        float x = canvas.getWidth() * 0.5f;
        float y = x;

        // The amount of double shapes that will be painted; at least 10, no more than 25.
        int numberOfShapes = contact.getFullName().length() * 4;
        numberOfShapes = Math.min(numberOfShapes, 25);
        while (numberOfShapes < 10) numberOfShapes *= 2;
        Log.d("Number of Circle Scape shapes", contact.getFullName() + " " + numberOfShapes);

        for (int i = 0; i < numberOfShapes; i++) {
            // Get the next character from the MD5 String.
            md5Pos++;
            if (md5Pos >= md5Length) md5Pos = 0;

            // Move the coordinates around.
            final int md5Int = md5String.charAt(md5Pos) + i;
            switch (md5Int % 6) {
                case 0:
                    x += md5Int;
                    y += md5Int;
                    break;
                case 1:
                    x -= md5Int;
                    y -= md5Int;
                    break;
                case 2:
                    x += md5Int * 2;
                    break;
                case 3:
                    y += md5Int * 2;
                    break;
                case 4:
                    x -= md5Int * 2;
                    y -= md5Int;
                    break;
                default:
                    x -= md5Int;
                    y -= md5Int * 2;
                    break;
            }

            int paintMode = MicopiPainter.MODE_CIRCLE;
            if (paintArc && md5Int % 2 == 0) paintMode = MicopiPainter.MODE_ARC;
            else if (paintPolygon) paintMode = MicopiPainter.MODE_POLYGON;
            //if (paintFilled) paintMode += MicopiPainter.MODE_CIRCLE_FILLED;

            // The new coordinates have been generated. Paint something.
            MicopiPainter.paintDoubleShape(
                    canvas,
                    paintMode,
                    ColorCollection.generateColor(colorChar1, colorChar2, md5Int, i + 1),
                    alpha,
                    shapeWidth,
                    numOfEdges,
                    md5Int * 2,            // Start Angle of Arc
                    endAngle,
                    x,
                    y,
                    i * md5String.charAt(2) // Radius
            );
            shapeWidth += .05f;
        }
    }

    /**
     * Fills the image with circles in a grid;
     * The grid size is always "number of letters in the first name ^ 2".
     *
     * @param canvas Canvas to draw on
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     */
    private static void generateCircleMatrix(Canvas canvas, Contact contact) {
        // Prepare painting values based on image size and contact data.
        final float fImageSize = canvas.getWidth();
        final String fMd5String = contact.getMD5EncryptedString();
        final int fFirstNameLength = contact.getNamePart(0).length();
        final float fStrokeWidth = fMd5String.charAt(19) * 2f;
        final float circleDistance = (fImageSize / fFirstNameLength)
                + (fImageSize / (fFirstNameLength * 2f));

        // Contact names with just one word will not get coloured circles.
        final boolean fDoGenerateColor = contact.getNumberOfNameParts() > 1;

        int md5Pos = 0;
        for (int y = 0; y < fFirstNameLength; y++) {
            for (int x = 0; x < fFirstNameLength; x++) {

                md5Pos++;
                if (md5Pos >= fMd5String.length()) md5Pos = 0;
                final char fMd5Char = fMd5String.charAt(md5Pos);

                int color = Color.WHITE;
                if (fDoGenerateColor) color = ColorCollection.getCandyColorForChar(fMd5Char);

                final int fIndex = y * fFirstNameLength + x;
                float radius;
                if ((fIndex & 1) == 0) radius = fMd5Char * 2f;
                else radius = fMd5Char * 3f;

                MicopiPainter.paintDoubleShape(
                        canvas,
                        MicopiPainter.MODE_CIRCLE_FILLED,
                        color,
                        200 - fMd5String.charAt(md5Pos) + fIndex,
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
