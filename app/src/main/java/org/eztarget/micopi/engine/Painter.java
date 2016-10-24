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
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class containing the actual paint methods for generating a contact picture;
 * stores the Canvas and other often-used attributes;
 * to be accessed by different steps of the ImageFactory
 * <p/>
 * Created by Michel on 23.01.14.
 */
class Painter {

    private static final String TAG = Painter.class.getSimpleName();

    private static final int SHADOW_COLOR = 0xEE000000;

    private Canvas mCanvas;

    private int mImageSize;

    private float mShadowRadius;

    private Paint mPaint;

    private AssetManager mAssetMan;

    /**
     * Constructor
     */
    Painter(final Canvas canvas, final Context context) {
        if (canvas == null) {
            Log.e(TAG, "Null canvas.");
            return;
        }
        mCanvas = canvas;
        mImageSize = canvas.getWidth();
        mShadowRadius = mImageSize * 0.05f;
        mPaint = new Paint();
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setAntiAlias(true);

        mAssetMan = context.getAssets();

//        if (mGrainTextureBitmap == null) {
//            final InputStream inputStream;
//            try {
//                inputStream = assetManager.open("texture_noise.png");
//                mGrainTextureBitmap = BitmapFactory.decodeStream(inputStream);
//                inputStream.close();
//            } catch (IOException e) {
//                Log.e(TAG, e.toString());
//            }
//        }

    }

    /**
     * @return Side length of the square canvas
     */
    public int getImageSize() {
        return mImageSize;
    }

    /*
    Textures
     */

    static final int COLOR_UNCHANGED = -1;

    private static final int NUMBER_OF_TEXTURE_FILES = 17;

    private void setShader(int fileId, final int color) {

        fileId = fileId % NUMBER_OF_TEXTURE_FILES;

        if (fileId < 1) {
            mPaint.setColor(color);
            clearShader();
            return;
        }

        disableShadows();

        final String fileName = "texture_" + (fileId + 1) + ".bmp";

        Bitmap textureBitmap = null;
        final InputStream inputStream;
        try {
            inputStream = mAssetMan.open(fileName);
            textureBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (textureBitmap == null) {
            mPaint.setColor(color);
            clearShader();
            return;
        }

        Log.d(TAG, "Loaded " + fileName + ".");
        mPaint.setShader(
                new BitmapShader(textureBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR)
        );

        if (color != COLOR_UNCHANGED) {
            final ColorFilter filter = new LightingColorFilter(Color.GRAY, color);
            mPaint.setColorFilter(filter);
        }

    }

    private void clearShader() {
        mPaint.setColorFilter(null);
        mPaint.setShader(null);
    }

    void enableShadows() {
        mPaint.setShadowLayer(mShadowRadius, 0, 0, SHADOW_COLOR);
        mHasShadows = true;
    }

    private boolean mHasShadows = false;

    void setShadowLayer(
            final float radiusScale,
            final float offsetFactorX,
            final float offsetFactorY
    ) {
        mPaint.setShadowLayer(
                mShadowRadius * radiusScale,
                mShadowRadius * (((offsetFactorX % 40) / 40f)),
                mShadowRadius * (((offsetFactorY % 40) / 40f)),
                SHADOW_COLOR
        );
        mHasShadows = true;
    }

    void disableShadows() {
        if (mHasShadows) {
            mPaint.clearShadowLayer();
            mHasShadows = false;
        }
        clearShader();
    }

    /**
     * Paints a styled square onto the canvas
     */
    void paintSquare(
            final int color,
            final int textureId,
            final float x,
            final float y,
            final float size
    ) {
        final float offsetX = x * size;
        final float offsetY = y * size;

        if (textureId > 0) {
            setShader(textureId, color);
            mCanvas.drawRect(offsetX, offsetY, offsetX + size, offsetY + size, mPaint);
            clearShader();
        } else {
            mPaint.setColor(color);
            mCanvas.drawRect(offsetX, offsetY, offsetX + size, offsetY + size, mPaint);
        }

    }

    private static final float TWO_PI = 2f * (float) Math.PI;

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
    void paintPolygon(
            final int color,
            final int textureId,
            float angleOffset,
            final int numberOfEdges,
            final float centerX,
            final float centerY,
            float radius
    ) {
        enableShadows();

        final Path polygonPath = new Path();

        for (int edge = 1; edge <= numberOfEdges; edge++) {
            final double angle = TWO_PI * edge / numberOfEdges;
            final float x = (float) (centerX + radius * Math.cos(angle + angleOffset));
            final float y = (float) (centerY + radius * Math.sin(angle + angleOffset));

            if (edge == 1) {
                polygonPath.moveTo(x, y);
            } else {
                polygonPath.lineTo(x, y);
            }

        }

        polygonPath.close();

        if (textureId > 0) {
//            mCanvas.drawPath(polygonPath, mPaint);
            setShader(textureId, color);
            mCanvas.drawPath(polygonPath, mPaint);
            clearShader();
        } else {
            mPaint.setColor(color);
            mCanvas.drawPath(polygonPath, mPaint);
        }

    }

    void paintCircle(
            final int color,
            final int textureId,
            float centerX,
            float centerY,
            float radius
    ) {
        enableShadows();
        if (textureId > 0) {
            mCanvas.drawCircle(centerX, centerY, radius, mPaint);
            setShader(textureId, color);
            mCanvas.drawCircle(centerX, centerY, radius, mPaint);
            clearShader();
        } else {
            mPaint.setColor(color);
            mCanvas.drawCircle(centerX, centerY, radius, mPaint);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void paintRoundedSquare(
            final int color,
            final int textureId,
            final float centerX,
            final float centerY,
            final float width
    ) {

        enableShadows();

        final float cornerRadius = width / 5f;

        if (textureId > 0) {

            mCanvas.drawRoundRect(
                    centerX - width,
                    centerY - width,
                    centerX + width,
                    centerY + width,
                    cornerRadius,
                    cornerRadius,
                    mPaint
            );
            setShader(textureId, color);
            mCanvas.drawRoundRect(
                    centerX - width,
                    centerY - width,
                    centerX + width,
                    centerY + width,
                    cornerRadius,
                    cornerRadius,
                    mPaint
            );
            clearShader();

        } else {
            mPaint.setColor(color);
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

    }


    void paintChars(final String string, int color) {
        int count = string.length();
        if (count == 0) {
            return;
        }
        else if (count > 4) {
            count = 4;
        }

        disableShadows();
        clearShader();
        mPaint.setColor(color);

        // Typeface, size and alignment:

        mPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        mPaint.setTextSize((66f / (float) Math.sqrt(string.length())) * (mImageSize / 100f));
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

}
