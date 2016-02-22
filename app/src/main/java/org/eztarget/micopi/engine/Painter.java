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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

/**
 * Utility class containing the actual paint methods for generating a contact picture;
 * stores the Canvas and other often-used attributes;
 * to be accessed by different steps of the ImageFactory
 * <p/>
 * Created by Michel on 23.01.14.
 */
public class Painter {

    private static final String LOG_TAG = Painter.class.getSimpleName();

    private static final int SHADOW_COLOR = 0xBB000000;

    private Canvas mCanvas;

    private int mImageSize;

    private float mImageSizeHalf;

    private float mShadowRadius;

    private Paint mPaint;

    /**
     * Constructor
     */
    public Painter(Canvas canvas) {
        if (canvas == null) {
            Log.e(LOG_TAG, "Null canvas.");
            return;
        }
        mCanvas = canvas;
        mImageSize = canvas.getWidth();
        mImageSizeHalf = mImageSize * 0.5f;
        mShadowRadius = mImageSize / 25f;
        mPaint = new Paint();
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setAntiAlias(true);
    }

    /**
     * @return Side length of the square canvas
     */
    public int getImageSize() {
        return mImageSize;
    }

    /**
     * Adds paintGrain to the entire canvas
     */
    public void paintGrain() {
        final Bitmap noise = ImageFactory.getGrainBitmap();
        if (noise != null) {
            mCanvas.drawBitmap(
                    noise,
                    null,
                    new Rect(0, 0, mCanvas.getWidth(), mCanvas.getHeight()),
                    null
            );
        }

    }

    /**
     * Paints a styled square onto the canvas
     *
     * @param doPaintFilled Filled or stroked painting
     * @param color         Paint color
     * @param alpha         Paint alpha value
     * @param x             X-coordinate of square centre
     * @param y             Y-coordinate of square centre
     * @param size          Side length
     */
    public void paintSquare(
            final int color,
            final int alpha,
            final float x,
            final float y,
            final float size
    ) {
        final float offsetX = x * size;
        final float offsetY = y * size;

        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        mPaint.setShadowLayer(0f, 0f, 0f, 0);

        mCanvas.drawRect(offsetX, offsetY, offsetX + size, offsetY + size, mPaint);
    }

    /**
     * Shape definition: full circle, stroked
     */
    public static final int MODE_CIRCLE = 0;

    /**
     * Shape definition: full circle, filled
     */
    public static final int MODE_CIRCLE_FILLED = 10;

    /**
     * Shape definition: polygon approximating a circle, stroked
     */
    public static final int MODE_POLYGON = 3;

    /**
     * Shape definition: polygon approximating a circle, filled
     */
    public static final int MODE_POLYGON_FILLED = 13;

    public static final float TWO_PI = 2f * (float) Math.PI;

    /**
     * Paints two shapes on top of each other with slightly different alpha,
     * size and stroke width values.
     *
     * @param color         Paint color
     * @param angleOffset   Offset that will be added to the angles of each edge
     * @param numberOfEdges Number of polygon edges
     * @param centerX       X coordinate of the centre of the shape
     * @param centerY       Y coordinate of the centre of the shape
     * @param radius        Also determines size of polygon approximations
     */
    public void paintPolygon(
            final int color,
            float angleOffset,
            final int numberOfEdges,
            final float centerX,
            final float centerY,
            float radius
    ) {

        // Configure paint:
        mPaint.setColor(color);
        mPaint.setShadowLayer(mShadowRadius, 0, 0, SHADOW_COLOR);

        double angle;
        float x, y;

        Path polygonPath = new Path();
        // Use Path.moveTo() for first vertex.
        boolean isFirstEdge = true;

        for (int edge = 1; edge <= numberOfEdges; edge++) {
            angle = TWO_PI * edge / numberOfEdges;
            x = (float) (centerX + radius * Math.cos(angle + angleOffset));
            y = (float) (centerY + radius * Math.sin(angle + angleOffset));

            if (isFirstEdge) {
                polygonPath.moveTo(x, y);
                isFirstEdge = false;
            } else {
                polygonPath.lineTo(x, y);
            }
        }

        polygonPath.close();

        mCanvas.drawPath(polygonPath, mPaint);
    }

    public void paintCircle(
            final int color,
            float centerX,
            float centerY,
            float radius
    ) {
        mPaint.setColor(color);
        mPaint.setShadowLayer(mShadowRadius, 0, 0, SHADOW_COLOR);
        mCanvas.drawCircle(centerX, centerY, radius, mPaint);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void paintRoundedSquare(
            final int color,
            final float centerX,
            final float centerY,
            final float width
    ) {
        mPaint.setColor(color);
        mPaint.setShadowLayer(mShadowRadius, 0, 0, SHADOW_COLOR);

        final float cornerRadius = width / 5f;

        mCanvas.drawRoundRect(
                centerX - width,
                centerY - width,
                centerX + width,
                centerY + width,
                cornerRadius,
                cornerRadius,
                mPaint
        );
    }

    /**
     * Alpha value of character that will be drawn on top of the picture
     */
    private static final char CHAR_ALPHA = 255;

    /**
     * Paints letters on top of the centre of a canvas - GMail style
     *
     * @param chars Characters to draw
     * @param color Paint color
     */
    public void paintChars(final String string, int color) {
        int count = string.length();
        if (count == 0) return;
        else if (count > 4) count = 4;

        mPaint.setColor(color);
        mPaint.setAlpha(CHAR_ALPHA);
        mPaint.setShadowLayer(0f, 0f, 0f, 0);

        // Typeface, size and alignment:
        Typeface sansSerifLight = Typeface.create("sans-serif", Typeface.BOLD);
        mPaint.setTypeface(sansSerifLight);


        final int length = string.length();
        if (length == 1) {
            mPaint.setTextSize(70f * mImageSize / 100f);
        } else {
            mPaint.setTextSize(100f / string.length() * mImageSize / 100f);
        }
        mPaint.setTextAlign(Paint.Align.CENTER);

        // Get the rectangle that the text fits into.
        final Rect rect = new Rect();
        mPaint.getTextBounds(string, 0, 1, rect);

        final float imageSizeHalf = mImageSize * 0.5f;

        mCanvas.drawText(
                string,
                0,
                count,
                imageSizeHalf,
                imageSizeHalf + (rect.bottom - rect.top) * 0.5f,
                mPaint
        );

    }

    /**
     * How much of the canvas the central circle is going to occupy
     */
    private static final float CIRCLE_SIZE = 0.8f;

    /**
     * Paints a circle in the middle of the image to enhance the visibility of the initial letter
     *
     * @param color Paint color
     * @param alpha Paint alpha value
     */
    public void paintCentralCircle(int color, int alpha) {
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        mPaint.setShadowLayer(0f, 0f, 0f, 0);
        mPaint.setStyle(Paint.Style.FILL);

        final float imageCenter = mImageSize * 0.5f;
        final float radius = imageCenter * CIRCLE_SIZE;

        mCanvas.drawCircle(imageCenter, imageCenter, radius, mPaint);
    }
}
