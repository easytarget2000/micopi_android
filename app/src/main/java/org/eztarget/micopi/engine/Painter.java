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
public class Painter {

    private static final String TAG = Painter.class.getSimpleName();

    private static final int SHADOW_COLOR = 0xDD000000;

    private static final int SHADOW_COLOR_LIGHT = 0x77000000;

    private Canvas mCanvas;

    private int mImageSize;

    private float mImageSizeHalf;

    private float mShadowRadius;

    private Paint mPaint;

    private Bitmap mGrainTextureBitmap;

    private Bitmap mTowelTextureBitmap;

    private Bitmap mMarbleTexture;

    /**
     * Constructor
     */
    public Painter(final Canvas canvas, final Context context) {
        if (canvas == null) {
            Log.e(TAG, "Null canvas.");
            return;
        }
        mCanvas = canvas;
        mImageSize = canvas.getWidth();
        mImageSizeHalf = mImageSize * 0.5f;
        mShadowRadius = mImageSize * 0.05f;
        mPaint = new Paint();
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setAntiAlias(true);

        final AssetManager assetManager = context.getAssets();

        if (mGrainTextureBitmap == null) {
            final InputStream inputStream;
            try {
                inputStream = assetManager.open("texture_noise.png");
                mGrainTextureBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        if (mTowelTextureBitmap == null) {
            final InputStream inputStream;
            try {
                inputStream = assetManager.open("texture_towel.png");
                mTowelTextureBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        if (mMarbleTexture == null) {
            final InputStream inputStream;
            try {
                inputStream = assetManager.open("texture_marble.png");
                mMarbleTexture = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
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

    public enum Texture {
        NONE,
        GRAIN,
        TOWEL,
        MARBLE
    }

    private void setShader(final Texture texture) {
        switch (texture) {
            case GRAIN:
                mPaint.setShader(
                        new BitmapShader(
                                mGrainTextureBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR
                        )
                );
                break;
            case TOWEL:
                mPaint.setShader(
                        new BitmapShader(
                                mTowelTextureBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR
                        )
                );
                break;
            case MARBLE:
                mPaint.setShader(
                        new BitmapShader(
                                mMarbleTexture, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR
                        )
                );
                break;
            default:
                clearShader();
        }
    }

    private void clearShader() {
        mPaint.setShader(null);
    }

    public void enableShadows() {
        mPaint.setShadowLayer(mShadowRadius, 0, 0, SHADOW_COLOR);
        mHasShadows = true;
    }

    private boolean mHasShadows = false;

    public void setShadowLayer(
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

    public void disableShadows() {
        if (mHasShadows) {
            mPaint.clearShadowLayer();
            mHasShadows = false;
        }
    }

    /**
     * Paints a styled square onto the canvas
     */
    public void paintSquare(
            final int color,
            final Texture texture,
            final int alpha,
            final float x,
            final float y,
            final float size
    ) {
        final float offsetX = x * size;
        final float offsetY = y * size;

        mPaint.setColor(color);
        mPaint.setAlpha(alpha);

//        Log.d("square", x + ", " + y);

        mCanvas.drawRect(offsetX, offsetY, offsetX + size, offsetY + size, mPaint);
        if (texture != Texture.NONE) {
            setShader(texture);
            mCanvas.drawRect(offsetX, offsetY, offsetX + size, offsetY + size, mPaint);
            clearShader();
        }
    }

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
            final Texture texture,
            float angleOffset,
            final int numberOfEdges,
            final boolean hasCurvedEdge,
            final float centerX,
            final float centerY,
            float radius
    ) {
        float lastX = 0f;
        float lastY = 0f;

        final Path polygonPath = new Path();

        for (int edge = 1; edge <= numberOfEdges; edge++) {
            final double angle = TWO_PI * edge / numberOfEdges;
            final float x = (float) (centerX + radius * Math.cos(angle + angleOffset));
            final float y = (float) (centerY + radius * Math.sin(angle + angleOffset));

            if (edge == 1) {
                polygonPath.moveTo(x, y);
                if (hasCurvedEdge) {
                    lastX = x;
                    lastY = y;
                }

            } else if (hasCurvedEdge && edge == 2) {
                polygonPath.quadTo(
                        ((x * 2f) + lastX + centerX) / 4f,
                        ((y * 2f) + lastY + centerY) / 4f,
                        x,
                        y
                );
            } else {
                polygonPath.lineTo(x, y);
            }

        }

        polygonPath.close();

        mPaint.setColor(color);

        mCanvas.drawPath(polygonPath, mPaint);

        if (texture != Texture.NONE) {
            setShader(texture);
            mCanvas.drawPath(polygonPath, mPaint);
            clearShader();
        }
    }

    public void paintCircle(
            final int color,
            final Texture texture,
            float centerX,
            float centerY,
            float radius
    ) {
        mPaint.setColor(color);
        mCanvas.drawCircle(centerX, centerY, radius, mPaint);

        if (texture != Texture.NONE) {
            setShader(texture);
            mCanvas.drawCircle(centerX, centerY, radius, mPaint);
            clearShader();
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void paintRoundedSquare(
            final int color,
            final Texture texture,
            final float centerX,
            final float centerY,
            final float width
    ) {
        mPaint.setColor(color);

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

        if (texture != Texture.NONE) {
            setShader(texture);
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
        }

    }

    /**
     * Alpha value of character that will be drawn on top of the picture
     */
    private static final char CHAR_ALPHA = 255;

    public void paintChars(final String string, int color) {
        int count = string.length();
        if (count == 0) return;
        else if (count > 4) count = 4;

        mPaint.setColor(color);
        mPaint.setAlpha(CHAR_ALPHA);

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
