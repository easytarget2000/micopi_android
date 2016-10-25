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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import org.eztarget.micopi.Contact;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class that generates a seemingly random image out of given contact values, such as the
 * name and a hash string.
 * <p/>
 * Created by Michel on 14.01.14.
 */
public class ImageFactory {

    private static final String TAG = ImageFactory.class.getSimpleName();

    private static final String TAG_BENCHMARK = TAG + ": Benchmark";

    private static final boolean BENCHMARK = true;

    private Contact mContact;

    private int mImageSize;

    private static Bitmap sGrainBitmap;

    private int mInitialsSettings = 1;

    /**
     * @param contact   Data from this Contact object will be used to generate the shapes and colors
     * @param imageSize Width of device screen in pixels; height in landscape mode
     */
    private ImageFactory(final Contact contact, final int imageSize) {
        mContact = contact;
        mImageSize = imageSize;
    }

    public static Bitmap bitmapFrom(
            final Context context,
            final Contact contact,
            final int imageSize
    ) {
        final ImageFactory factory = new ImageFactory(contact, imageSize);
        final AssetManager assetManager = context.getAssets();

        if (sGrainBitmap == null) {
            Log.d(TAG, "Loading Grain Bitmap from Assets.");

            final InputStream inputStream;
            try {
                inputStream = assetManager.open("texture_noise.png");
                sGrainBitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        return factory.generateBitmap(context);
    }

    /**
     * @return The completed, generated image as a bitmap to be used by the GUI and contact handler.
     */
    private Bitmap generateBitmap(final Context context) {
        if (mContact == null) {
            Log.e(TAG, "ERROR: Contact object is null. Returning null image.");
            return null;
        }

        if (mContact.getFullName().length() < 1) {
            Log.e(TAG, "ERROR: Contact name < 1. Returning null image.");
            return null;
        }

        long startTime;

        if (BENCHMARK) {
            startTime = System.currentTimeMillis();
        }

        // Set up the bitmap and the canvas.
        final Bitmap bitmap = Bitmap.createBitmap(mImageSize, mImageSize, Bitmap.Config.ARGB_8888);

        // Set up a new canvas
        // and fill the background with the color for this contact's first letter.
        final Canvas canvas = new Canvas(bitmap);
        final char firstChar = mContact.getFullName().charAt(0);
        final int paletteId = firstChar;
        final int bgColor = ColorCollection.getColor(paletteId, 0);
        canvas.drawColor(bgColor);

        if (BENCHMARK) {
            Log.d(TAG_BENCHMARK, "ImageSize: " + mImageSize);
            Log.d(TAG_BENCHMARK, "10: " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }

        /*
        MAIN PATTERN
        */

        final Painter painter = new Painter(canvas, context);

        MaterialGenerator.paint(painter, mContact, paletteId);

        if (BENCHMARK) {
            Log.d(TAG_BENCHMARK, "12: " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }

        painter.disableShadows();

        /*
        INITIAL LETTER ON CIRCLE
         */

        painter.paintChars(String.valueOf(firstChar).toUpperCase(), Color.WHITE);

        if (BENCHMARK) {
            Log.d(TAG_BENCHMARK, "15: " + (System.currentTimeMillis() - startTime));
        }
        return bitmap;
    }

}