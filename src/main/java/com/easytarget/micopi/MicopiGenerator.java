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
import android.util.FloatMath;
import android.util.Log;

import java.util.ArrayList;

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

        int backgroundColor = ColorCollection.getColorForChar(contact.getFullName().charAt(0));
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
        int numberOfWords = contact.getNumberOfNameParts();
        float centerX = imageSize * 0.5f;
        float centerY = imageSize * 0.5f;

        switch (md5String.charAt(20) % circleProbFactor) {
            case 0:     // Paint circles depending on the number of words.
                for (int i = 0; i < numberOfWords; i++)
                    MicopiPainter.paintMicopiCircle(
                            canvas,
                            false,
                            0,
                            i,
                            numberOfWords,
                            Color.WHITE,
                            centerX,
                            centerY,
                            1.6f,
                            md5String.charAt(11)
                    );
                break;
            default:    // Paint that flower.
                md5String = contact.getMD5EncryptedString();
                MicopiPainter.paintMicopiBeams(
                        md5String.charAt(17),
                        md5String.charAt(12),
                        md5String.charAt(13),
                        md5String.charAt(5),
                        centerX,
                        centerY,
                        imageSize,
                        canvas
                );
        }

        // Write the initial(s).
        char[] initials = {contact.getFullName().charAt(0)};
        MicopiPainter.paintChars(canvas, initials, Color.WHITE);

        return generatedBitmap;
    }

    /**
     * Generates a color, based on the given input parameters.
     *
     * @param firstLetter    First character of the contact's name
     * @param char1  MD5 Character
     * @param char2  MD5 Character
     * @param numOfWords    Number of Words in the contact's name
     *
     * @return  Color with alpha=255
     */
    private static int generateColor(char firstLetter, char char1, char char2, int numOfWords) {
        int iGeneratedColor = Color.DKGRAY;
        if (firstLetter % 2 == 0) iGeneratedColor = Color.YELLOW;

        iGeneratedColor *= firstLetter * -char1 * numOfWords * char2;
        iGeneratedColor |= 0xff000000;

        return iGeneratedColor;
    }

    /**
     * Fills the image with a lot of colourful circles.
     */
    private static void generateCircleScape(Canvas canvas, Contact contact) {
        /*
         If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
         there is a 3/4 chance that polygons will be painted instead of circles.
         */
        boolean paintPolygon = false;
        int numOfEdges = contact.getNamePart(0).length();
        String md5String = contact.getMD5EncryptedString();
        if (md5String.charAt(14) % 4 != 0 && numOfEdges > 2 && numOfEdges < 7) paintPolygon = true;

        // Draw all the shapes.
        int numberOfShapes  = contact.getFullName().length() * 4;
        int md5Length       = md5String.length();
        int md5Pos          = 0;
        float shapeWidth    = 0.1f;
        int shapeColor      = generateColor(
                md5String.charAt(5), md5String.charAt(6), md5String.charAt(7), 10);

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

            // The new coordinates have been generated. Paint something.
            MicopiPainter.paintMicopiCircle(
                    canvas,
                    paintPolygon,
                    numOfEdges,
                    1,
                    1,
                    shapeColor,
                    x,
                    y,
                    shapeWidth,
                    md5Char
            );
            shapeWidth += .05f;
        }
    }
}
