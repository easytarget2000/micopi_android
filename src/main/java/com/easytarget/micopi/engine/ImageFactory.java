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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.easytarget.micopi.Contact;

/**
 * Utility class that generates a seemingly random image out of given contact values, such as the
 * name and a hash string.
 *
 * Created by Michel on 14.01.14.
 */
public class ImageFactory {

    /**
     * Generate the entire image.
     *
     * @param fContact Data from this Contact object will be used to generate the shapes and colors
     * @param fScreenWidthInPixels Width of device screen in pixels; height in landscape mode
     * @return The completed, generated image as a bitmap to be used by the GUI and contact handler.
     */
    public static Bitmap generateBitmap(final Contact fContact, final int fScreenWidthInPixels) {
        if (fContact == null) {
            Log.e("ImageFactory", "ERROR: Contact object is null. Returning null image.");
            return null;
        }

        if (fContact.getFullName().length() < 1) {
            Log.e("ImageFactory", "ERROR: Contact name < 1. Returning null image.");
            return null;
        }

        // Determine the image side length, roughly depending on the screen width.
        // Old devices should not be unnecessarily strained,
        // but if the user takes these account pictures to another device,
        // they shouldn't look too horribly pixelated.
        int imageSize = 1080;
        if (fScreenWidthInPixels <= 480) imageSize = 480;
        else if (fScreenWidthInPixels <= 600) imageSize = 640;
        else if (fScreenWidthInPixels < 1000) imageSize = 720;
        else if (fScreenWidthInPixels >= 1200) imageSize = 1440;

        Log.d("Image Size", imageSize + "");

        // Set up the bitmap and the canvas.
        final Bitmap fBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);

        // Set up a new canvas
        // and fill the background with the color for this contact's first letter.
        final Canvas fCanvas = new Canvas(fBitmap);
        final char fFirstChar = fContact.getFullName().charAt(0);
        final int fBackgroundColor = ColorCollection.getCandyColorForChar(fFirstChar);
        fCanvas.drawColor(fBackgroundColor);

        // The contact's current MD5 encoded string will be referenced a lot.
        final String fMd5String = fContact.getMD5EncryptedString();

        /*
        MAIN PATTERN
        */

//        StripeCandyGenerator.generate(fCanvas, fContact);

        switch (fMd5String.charAt(4) % 3) {
            case 1:
                WanderingShapesGenerator.generate(fCanvas, fContact);
                break;
            case 2:
                SquareMatrixGenerator.generate(fCanvas, fContact);
                break;
            default:
                CircleMatrixGenerator.generate(fCanvas, fContact);
                break;
        }

        /*
        ADDITIONAL SHAPES
         */

        final int fNumOfWords = fContact.getNumberOfNameWords();
        final float fCenterX = imageSize * (fMd5String.charAt(9) / 128f);
        final float fCenterY = imageSize * (fMd5String.charAt(3) / 128f);
        final float fOffset  = fMd5String.charAt(18) * 2f;
        final float fRadiusFactor = imageSize * 0.4f;

        switch (fMd5String.charAt(30) % 4) {
            case 0:     // Paint circles depending on the number of words.
                for (int i = 0; i < fNumOfWords; i++) {
                    int alpha = (int) (((i + 1f) / fNumOfWords) * 120f);

                    Painter.paintDoubleShape(
                            fCanvas,
                            Painter.MODE_CIRCLE,
                            Color.WHITE,
                            alpha,
                            ((fNumOfWords / (i + 1f)) * 80f), // Stroke Width
                            0,                              // No edges
                            0f,                             // No arc start angle
                            0f,                             // No arc end angle
                            fCenterX + (i * fOffset),
                            fCenterY - (i * fOffset),
                            ((fNumOfWords / (i + 1f)) * fRadiusFactor)
                    );
                    //Log.d("Word Circles", alpha + "");
                }
                break;
            case 1:
                Painter.paintSpyro(
                        fCanvas,
                        Color.WHITE,
                        Color.YELLOW,
                        Color.BLACK,
                        255 - (fMd5String.charAt(19) * 2),
                        (0.3f - (float) fFirstChar / 255f),
                        (0.3f - (float) fMd5String.charAt(23) / 255f),
                        (0.3f - (float) fMd5String.charAt(24) / 255f),
                        Math.max(5, fMd5String.charAt(25) >> 2)
                );
                break;
            case 2:
                Painter.paintMicopiBeams(
                        fCanvas,
                        fBackgroundColor,
                        fMd5String.charAt(17) / 5,       // Alpha
                        fMd5String.charAt(12) % 4,       // Paint Mode
                        fCenterX,
                        fCenterY,
                        fMd5String.charAt(13) * 3,       // Density
                        fMd5String.charAt(5) * 0.6f,     // Line Length
                        fMd5String.charAt(14) * 0.15f,   // Angle
                        fMd5String.charAt(20) % 2 == 0,  // Large Delta Angle
                        fMd5String.charAt(21) % 2 == 0   // Wide Strokes
                );
                break;
        }

        /*
        INITIAL LETTER ON CIRCLE
         */

        Painter.paintCentralCircle(fCanvas, fBackgroundColor, (255 - fMd5String.charAt(29) * 2));
        Painter.paintChars(fCanvas, new char[]{fFirstChar}, Color.WHITE);

        return fBitmap;
    }
}
