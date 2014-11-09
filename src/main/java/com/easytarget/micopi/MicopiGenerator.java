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
import android.graphics.Paint;
import android.util.FloatMath;
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

        // The contact's current MD5 encoded string will be referenced a lot.
        final String md5String = contact.getMD5EncryptedString();

//        /*
//        ROTATION
//         */
//
//        final boolean doRotate = (md5String.charAt(26) % 3 == 0);
//        float rotationSign = 1f;
//        if (md5String.charAt(21) % 2 == 0) rotationSign = -1f;
//
//        final float rotationAngle = md5String.charAt(22) * 0.3f * rotationSign;
//        if (doRotate) canvas.rotate(rotationAngle);

        /*
        MAIN PATTERN
        */

        final boolean isOneWordName = (contact.getNumberOfNameParts() == 1);

        if (isOneWordName || md5String.charAt(20) % 4 == 0) generateOddCircleMatrix(canvas, contact);
        else generateCircleScape(canvas, contact);

        /*
        ADDITIONAL SHAPES
         */

        // A name with three or more words is more likely to get the circles.
        // The higher the number the less likely circles are.
        int circleProbFactor = 4;
        if (contact.getNumberOfNameParts() > 2) circleProbFactor = 2;

        final int numberOfWords = contact.getNumberOfNameParts();
        final float centerX = imageSize * (md5String.charAt(9) / 128f);
        final float centerY = imageSize * (md5String.charAt(3) / 128f);
        final float offset  = md5String.charAt(18) * 2f;
        final float radiusFactor = imageSize * 0.4f;
        Log.d("Geometric Addition Center", centerX + " " + centerY);

        switch (md5String.charAt(20) % circleProbFactor) {
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
            default:    // Paint that flower.
                MicopiPainter.paintMicopiBeams(
                        canvas,
                        backgroundColor,
                        md5String.charAt(17) / 4,           // Alpha
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

        /*
        INITIAL LETTER
         */

        // Rotate back first, if needed.
//        if (doRotate) canvas.rotate(-rotationAngle);

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
        float shapeWidth = (float) md5String.charAt(7) * 2f;

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
                    md5Char * 2,            // Start Angle of Arc
                    endAngle,
                    x,
                    y,
                    i * md5String.charAt(2) // Radius
            );
            shapeWidth += .05f;
        }
    }

    private static final String DEBUG_TAG_ODD = "generateOddCircleMatrix()";

    private static void generateOddCircleMatrix(Canvas canvas, Contact contact) {
        final int numOfCircles  = contact.getNamePart(0).length();

        // If the number of circles is even, draw a different mid-section.
        final boolean isEvenNumbered = (numOfCircles % 2 == 0);

        // Determine which one is the middle circle.
        // For an even number of circles, there are two middle circles.
        // This value determines the first one in a sequence of two middle circles.
        int midCircle = (numOfCircles / 2);
        if (isEvenNumbered) midCircle--;

        // To work with radii right away, divide the canvas size by half.
        final float imageSizeHalf = canvas.getWidth() * 0.5f;

        Log.d(DEBUG_TAG_ODD, "Num: " + numOfCircles + " Mid: " + midCircle);

        // Calculate the sum of the weights of each circle relative to each other.
        float circleWeight = 1f;
        float sumOfCircleWeights = 0f;

        // Start with one,
        // double the value up to the middle circle, then half the values again,
        // add the new value to the old one.
        for (int i = 0; i < numOfCircles; i++) {
            sumOfCircleWeights += circleWeight;

            //Log.d(DEBUG_TAG_ODD, i + " CW: " + circleWeight + " TotW: " + sumOfCircleWeights );
            if (i < midCircle) circleWeight *= 2f;
            else if (!isEvenNumbered && i == midCircle) circleWeight /= 2f;
            else if (i > midCircle) circleWeight /= 2f;
        }
        // This results in the sum of circle "weights".
        // Take the inverse of the entire weight and multiply it with the image radius.
        final float smallestRadius = (1f / sumOfCircleWeights) * imageSizeHalf;
        Log.d(DEBUG_TAG_ODD, "Image Size Half: " + imageSizeHalf + " Radius 1: " + smallestRadius);

        /*
        CALCULATE RADIUS SERIES
         */

        float[] radii = new float[numOfCircles];
        float radius = smallestRadius;
        for (int circleIndex = 0; circleIndex <= midCircle; circleIndex++) {
            //Log.d(DEBUG_TAG_ODD, circleIndex + " " + radius + " " + (numOfCircles - circleIndex));
            radii[circleIndex] = radius;

            // Duplicate the middle circle for names with an even amount of characters. .oOOo.
            if (circleIndex == midCircle) {
                if (isEvenNumbered) {
                    radii[circleIndex + 1] = radius;
                    break;
                }
            } else {
                // As long as we haven't reached a middle circle,
                // write the current radius at the other side. .oOo.
                radii[numOfCircles - (circleIndex + 1)] = radius;
            }

            // Double the radius for the next circle of the ascending circle sequence. .oO
            radius *= 2;
        }

        /*
        PAINT HORIZONTAL ROW OF CIRCLES
         */

        String md5String = contact.getMD5EncryptedString();

        //final boolean useRadiusSweep = md5String.charAt(28) % 2 == 0;
        float xPos = 0f;
        float yPos = imageSizeHalf;

        // Use a light alpha for the horizontal line of circles.
        int alpha = 160 - md5String.charAt(6);
        float strokeWidth = md5String.charAt(19) * 2f;

        // Go through the radius succession and paint the circles.
        for (float r : radii) {
            xPos += r;
            MicopiPainter.paintDoubleShape(
                    canvas,
                    MicopiPainter.MODE_CIRCLE,
                    Color.WHITE,
                    alpha,
                    strokeWidth,       // Stroke width
                    0,
                    0f,
                    0f,
                    xPos,
                    yPos,
                    radius
            );
            xPos += r;
        }

        /*
        PAINT VERTICAL ROW OF CIRCLES
         */

        // Paint the vertical row in the horizontal centre.
        xPos = imageSizeHalf;
        yPos = 0f;

        // Use a generated color.
        final int yColor = ColorCollection.generateColor(
                md5String.charAt(23),
                md5String.charAt(24),
                md5String.charAt(25),
                4
        );

        // Use a stronger alpha for the vertical row of circles.
        alpha = 200 - md5String.charAt(6);

        strokeWidth = md5String.charAt(28) * 1.5f;

        // Go through the radius succession again and paint them from top to bottom.
        for (float r : radii) {
            yPos += r;
            if (yPos != imageSizeHalf) {
                MicopiPainter.paintDoubleShape(
                        canvas,
                        MicopiPainter.MODE_CIRCLE,
                        yColor,
                        alpha,
                        strokeWidth,
                        0,
                        0f,
                        0f,
                        xPos,
                        yPos,
                        radius
                );
            }
            yPos += r;
        }

    }
}
