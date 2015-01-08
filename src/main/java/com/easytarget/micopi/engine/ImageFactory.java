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

    private static final String LOG_TAG = ImageFactory.class.getSimpleName();

    private Contact mContact;

    private int mScreenWidthPixels;

    /**
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     * @param screenWidthPixels Width of device screen in pixels; height in landscape mode
     */
    public ImageFactory(final Contact contact, final int screenWidthPixels) {
        mContact = contact;
        mScreenWidthPixels = screenWidthPixels;
    }

    /**
     * @return The completed, generated image as a bitmap to be used by the GUI and contact handler.
     */
    public Bitmap generateBitmap() {
        if (mContact == null) {
            Log.e(LOG_TAG, "ERROR: Contact object is null. Returning null image.");
            return null;
        }

        if (mContact.getFullName().length() < 1) {
            Log.e(LOG_TAG, "ERROR: Contact name < 1. Returning null image.");
            return null;
        }

        // Determine the image side length, roughly depending on the screen width.
        // Old devices should not be unnecessarily strained,
        // but if the user takes these account pictures to another device,
        // they shouldn't look too horribly pixelated.
        int imageSize = 1080;
        if (mScreenWidthPixels <= 480) imageSize = 480;
        else if (mScreenWidthPixels <= 600) imageSize = 640;
        else if (mScreenWidthPixels < 1000) imageSize = 720;
        else if (mScreenWidthPixels >= 1200) imageSize = 1440;

//        Log.d("Image Size", imageSize + "");

        // Set up the bitmap and the canvas.
        final Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);

        // Set up a new canvas
        // and fill the background with the color for this contact's first letter.
        final Canvas canvas = new Canvas(bitmap);
        final char firstChar = mContact.getFullName().charAt(0);
        final int bgColor = ColorCollection.getCandyColorForChar(firstChar);
        canvas.drawColor(bgColor);

        // The contact's current MD5 encoded string will be referenced a lot.
        final String md5String = mContact.getMD5EncryptedString();

        /*
        MAIN PATTERN
        */

//        StripeCandyGenerator.generate(canvas, mContact);

        final Painter painter = new Painter(canvas);

        switch (md5String.charAt(4) % 3) {
            case 1:
                WanderingShapesGenerator.generate(painter, mContact);
                break;
            case 2:
                SquareMatrixGenerator.generate(painter, mContact);
                break;
            default:
                CircleMatrixGenerator.generate(painter, mContact);
                break;
        }

        /*
        GRAIN
         */

        painter.grain();

        /*
        ADDITIONAL SHAPES
         */


        final int numOfWords = mContact.getNumberOfNameWords();
        final float centerX = imageSize * (md5String.charAt(9) / 128f);
        final float centerY = imageSize * (md5String.charAt(3) / 128f);
        final float centerOffset  = md5String.charAt(18) * 2f;
        final float radiusFactor = imageSize * 0.4f;

        switch (md5String.charAt(30) % 4) {
            case 0:     // Paint circles depending on the number of words.
                for (int i = 0; i < numOfWords; i++) {
                    int alpha = (int) (((i + 1f) / numOfWords) * 120f);

                    painter.paintDoubleShape(
                            Painter.MODE_CIRCLE,
                            Color.WHITE,
                            alpha,
                            ((numOfWords / (i + 1f)) * 80f), // Stroke Width
                            0,                              // No edges
                            0f,                             // No arc start angle
                            0f,                             // No arc end angle
                            centerX + (i * centerOffset),
                            centerY - (i * centerOffset),
                            ((numOfWords / (i + 1f)) * radiusFactor)
                    );
                    //Log.d("Word Circles", alpha + "");
                }
                break;
            case 1:
                painter.paintSpyro(
                        Color.WHITE,
                        Color.YELLOW,
                        Color.BLACK,
                        255 - (md5String.charAt(19) * 2),
                        (0.3f - (float) firstChar / 255f),
                        (0.3f - (float) md5String.charAt(23) / 255f),
                        (0.3f - (float) md5String.charAt(24) / 255f),
                        Math.max(5, md5String.charAt(25) >> 2)
                );
                break;
            case 2:
                painter.paintMicopiBeams(
                        bgColor,
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

        painter.paintCentralCircle(bgColor, (255 - md5String.charAt(29) * 2));
        painter.paintChars(new char[]{firstChar}, Color.WHITE);

        return bitmap;
    }
}
