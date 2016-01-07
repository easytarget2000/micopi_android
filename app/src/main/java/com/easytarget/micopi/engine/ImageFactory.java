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

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.easytarget.micopi.Contact;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class that generates a seemingly random image out of given contact values, such as the
 * name and a hash string.
 *
 * Created by Michel on 14.01.14.
 */
public class ImageFactory {

    private static final String TAG = ImageFactory.class.getSimpleName();

    private static final String LOG_TAG_BM = TAG + ": Benchmark";

    private static final boolean BENCHMARK = false;

    private Contact mContact;

    private int mImageSize;

    private static Bitmap sGrainBitmap;

    /**
     * @param contact Data from this Contact object will be used to generate the shapes and colors
     * @param imageSize Width of device screen in pixels; height in landscape mode
     */
    private ImageFactory(final Contact contact, final int imageSize) {
        mContact = contact;
        mImageSize = imageSize;
    }

    public static Bitmap bitmapFrom(final Context context, final Contact contact, final int imageSize) {
        final ImageFactory factory = new ImageFactory(contact, imageSize);
        AssetManager assetManager = context.getAssets();

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

        return factory.generateBitmap();
    }

    public static Bitmap getGrainBitmap() {
        return sGrainBitmap;
    }

    /**
     * @return The completed, generated image as a bitmap to be used by the GUI and contact handler.
     */
    public Bitmap generateBitmap() {
        if (mContact == null) {
            Log.e(TAG, "ERROR: Contact object is null. Returning null image.");
            return null;
        }

        if (mContact.getFullName().length() < 1) {
            Log.e(TAG, "ERROR: Contact name < 1. Returning null image.");
            return null;
        }

        long startTime;

        if(BENCHMARK) startTime = System.currentTimeMillis();

        // Set up the bitmap and the canvas.
        final Bitmap bitmap = Bitmap.createBitmap(mImageSize, mImageSize, Bitmap.Config.ARGB_8888);

        // Set up a new canvas
        // and fill the background with the color for this contact's first letter.
        final Canvas canvas = new Canvas(bitmap);
        final char firstChar = mContact.getFullName().charAt(0);
        final int bgColor = ColorCollection.getColor(firstChar);
        canvas.drawColor(bgColor);

        if(BENCHMARK) {
            Log.d(LOG_TAG_BM, "10: " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }

        // The contact's current MD5 encoded string will be referenced a lot.
        final String md5String = mContact.getMD5EncryptedString();
//        if (mDoBroadcastProgress) sendProgressBroadcast(40);

        /*
        MAIN PATTERN
        */

        final Painter painter = new Painter(canvas);

        final int painterMode = md5String.charAt(5) % 4;
        if(BENCHMARK) {
            Log.d(LOG_TAG_BM, "11: " + (System.currentTimeMillis() - startTime) + ", " + painterMode);
            Log.d(LOG_TAG_BM, "Mode: " + painterMode);
            startTime = System.currentTimeMillis();
        }

        switch (painterMode) {
            case 1:
                CircleMatrixGenerator.generate(painter, mContact);
                break;
            case 2:
                SquareMatrixGenerator.generate(painter, mContact);
                break;
            default:
                WanderingShapesGenerator.generate(painter, mContact);
        }

        if(BENCHMARK) {
            Log.d(LOG_TAG_BM, "12: " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }

//        if (mDoBroadcastProgress) sendProgressBroadcast(70);
        painter.paintGrain();

        if(BENCHMARK) {
            Log.d(LOG_TAG_BM, "13: " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }

        /*
        INITIAL LETTER ON CIRCLE
         */

        painter.paintCentralCircle(bgColor, (220 - md5String.charAt(29)));
        painter.paintChars(
                String.valueOf(firstChar),
                // Experimental:
//                md5String.charAt(26) % 2 == 0 ?
//                        String.valueOf(firstChar).toLowerCase() :
//                        String.valueOf(firstChar).toUpperCase(),
                Color.WHITE
        );

//        if (mDoBroadcastProgress) sendProgressBroadcast(100);
        if(BENCHMARK) Log.d(LOG_TAG_BM, "15: " + (System.currentTimeMillis() - startTime));
        return bitmap;
    }

//    private Intent mProgressBroadcast;

//    private void sendProgressBroadcast(final int progress) {
//        if (mAppContext == null) return;
//
//        if (mProgressBroadcast == null) {
//            mProgressBroadcast = new Intent(Constants.ACTION_UPDATE_PROGRESS);
//        }
//        mProgressBroadcast.putExtra(Constants.EXTRA_PROGRESS, progress);
//        mAppContext.sendBroadcast(mProgressBroadcast);
//    }
}
