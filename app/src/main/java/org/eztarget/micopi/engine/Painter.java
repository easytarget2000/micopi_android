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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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

    private static final int SHADOW_COLOR = 0xBB000000;

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
        mShadowRadius = mImageSize * 0.1f;
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
        disableShadows();
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
    }

    private boolean mHasShadows = false;

    public void setShadowLayer(
            final float radiusScale,
            final float offsetFactorX,
            final float offsetFactorY
    ) {
        mPaint.setShadowLayer(
                mShadowRadius * radiusScale,
                mShadowRadius * (((offsetFactorX % 20) / 40f)),
                mShadowRadius * (((offsetFactorY % 20) / 40f)),
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
            final Texture texture,
            float angleOffset,
            final int numberOfEdges,
            final float centerX,
            final float centerY,
            float radius
    ) {

        double angle;
        float x, y;

        final Path polygonPath = new Path();
        // Use Path.moveTo() for first vertex.

        for (int edge = 1; edge <= numberOfEdges; edge++) {
            angle = TWO_PI * edge / numberOfEdges;
            x = (float) (centerX + radius * Math.cos(angle + angleOffset));
            y = (float) (centerY + radius * Math.sin(angle + angleOffset));

            if (edge == 1) polygonPath.moveTo(x, y);
            else  polygonPath.lineTo(x, y);
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
     * paintMicopiBeams() Paint Mode "Spiral"
     */
    public static final int BEAM_SPIRAL = 0;

    /**
     * paintMicopiBeams() Paint Mode "Solar"
     */
    public static final int BEAM_SOLAR = 1;

    /**
     * paintMicopiBeams() Paint Mode "Star"
     */
    public static final int BEAM_STAR = 2;

    /**
     * paintMicopiBeams() Paint Mode "Whirl"
     */
    public static final int BEAM_WHIRL = 3;

    /**
     * Paints many lines in beam-like ways
     *
     * @param color        Paint color
     * @param alpha        Paint alpha value
     * @param fPaintMode   Determines the shape to draw
     * @param centerX      X coordinate of the centre of the shape
     * @param centerY      Y coordinate of the centre of the shape
     * @param fDensity     Density of the beams
     * @param lineLength   Length of the beams
     * @param angle        Angle between single beams
     * @param fGroupAngle  Angle between beam groups
     * @param fDoPaintWide Wide beam groups
     */
    public void paintMicopiBeams(
            final int color,
            final int alpha,
            final int fPaintMode,
            float centerX,
            float centerY,
            final int fDensity,
            float lineLength,
            double angle,
            final boolean fGroupAngle,
            final boolean fDoPaintWide
    ) {
        final float lengthUnit = (mCanvas.getWidth() / 200f);
        lineLength *= lengthUnit;
        angle *= lengthUnit;

        // Define how the angle should change after every line.
        float deltaAngle;
        if (fGroupAngle) deltaAngle = 10f * lengthUnit;
        else deltaAngle = lengthUnit;

        // Configure paint:
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        mPaint.clearShadowLayer();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

        // Set wide or thin strokes.
        if (fDoPaintWide) mPaint.setStrokeWidth(24f);
        else mPaint.setStrokeWidth(8f);
        float lineStartX = centerX;
        float lineStartY = centerY;
        float lineEndX, lineEndY;

        for (int i = 0; i < fDensity; i++) {
            lineEndX = lineStartX + ((float) Math.cos(angle) * lineLength);
            lineEndY = lineStartY + ((float) Math.sin(angle) * lineLength);

            mCanvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, mPaint);

            angle += deltaAngle;
            lineLength += lengthUnit;

            switch (fPaintMode) {
                case BEAM_SPIRAL:
                    lineStartX = lineEndX;
                    lineStartY = lineEndY;
                    break;
                case BEAM_SOLAR:
                    lineStartX = centerX;
                    lineStartY = centerY;
                    break;
                case BEAM_STAR:
                    lineStartX = lineEndX;
                    lineStartY = lineEndY;
                    angle--;
                    break;
                default:
                    centerX += 2;
                    centerY -= 3;
                    lineStartX = centerX;
                    lineStartY = centerY;
            }
        }
    }

    /**
     * Distance between circle steps
     */
    private static final float PI_STEP_SIZE = (float) Math.PI / 50f;

    public void paintSpyro(
            final int color1,
            final int color2,
            final int color3,
            final int alpha,
            final float fPoint1Factor,
            final float fPoint2Factor,
            final float fPoint3Factor,
            final int revolutions
    ) {

        float innerRadius = mImageSizeHalf * 0.5f;
        double outerRadius = (innerRadius * 0.5f) + 1;
        double radiusSum = innerRadius + outerRadius;

        final float point1 = (float) (fPoint1Factor * (mImageSize - radiusSum));
        final float point2 = (float) (fPoint2Factor * (mImageSize - radiusSum));
        final float point3 = (float) (fPoint3Factor * (mImageSize - radiusSum));

        final Path pointPath1 = new Path();
        final Path pointPath2 = new Path();
        final Path pointPath3 = new Path();
        boolean moveTo = true;

        double t = 0;
        float x, y, x2, y2, x3, y3;
        do {
            x = (float) (radiusSum * Math.cos(t) +
                    point1 * Math.cos(radiusSum * t / outerRadius) + mImageSizeHalf);
            y = (float) (radiusSum * Math.sin(t) +
                    point1 * Math.sin(radiusSum * t / outerRadius) + mImageSizeHalf);
            x2 = (float) (radiusSum * Math.cos(t) +
                    point2 * Math.cos(radiusSum * t / outerRadius) + mImageSizeHalf);
            y2 = (float) (radiusSum * Math.sin(t) +
                    point2 * Math.sin(radiusSum * t / outerRadius) + mImageSizeHalf);
            x3 = (float) (radiusSum * Math.cos(t) +
                    point3 * Math.cos(radiusSum * t / outerRadius) + mImageSizeHalf);
            y3 = (float) (radiusSum * Math.sin(t) +
                    point3 * Math.sin(radiusSum * t / outerRadius) + mImageSizeHalf);

            if (moveTo) {
                pointPath1.moveTo(x, y);
                pointPath2.moveTo(x2, y2);
                pointPath3.moveTo(x3, y3);
                moveTo = false;
            } else {
                pointPath1.lineTo(x, y);
                pointPath2.lineTo(x2, y2);
                pointPath3.lineTo(x3, y3);
            }
            t += PI_STEP_SIZE;
        } while (t < TWO_PI * revolutions);

        mPaint.clearShadowLayer();
        mPaint.setStyle(Paint.Style.STROKE);

        // Draw the first path.
        mPaint.setColor(color1);
        mPaint.setAlpha(alpha);
        mCanvas.drawPath(pointPath1, mPaint);
        // Draw the second path.
        mPaint.setColor(color2);
        mPaint.setAlpha(alpha);
        mCanvas.drawPath(pointPath2, mPaint);
        // Draw the third path.
        mPaint.setColor(color3);
        mPaint.setAlpha(alpha);
        mPaint.setStrokeWidth(20 / revolutions);
        mCanvas.drawPath(pointPath3, mPaint);
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
        mPaint.clearShadowLayer();

        // Typeface, size and alignment:
        Typeface sansSerifLight = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mPaint.setTypeface(sansSerifLight);

        final int length = string.length();
        if (length == 1) {
            mPaint.setTextSize(75f * mImageSize / 100f);
        } else {
            mPaint.setTextSize(90f / string.length() * mImageSize / 100f);
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
    private static final float CIRCLE_SIZE = 0.66f;

    /**
     * Paints a circle in the middle of the image to enhance the visibility of the initial letter
     *
     * @param color Paint color
     * @param alpha Paint alpha value
     */
    public void paintCentralCircle(final int color, final int alpha, final boolean inverted) {
        mPaint.setStyle(Paint.Style.FILL);

        final float radius = mImageSizeHalf * CIRCLE_SIZE;
        mPaint.setColor(color);

        // TODO: Implement Inverted Mode.

        if (!inverted) {
            mPaint.setAlpha(alpha);
            mCanvas.drawCircle(mImageSizeHalf, mImageSizeHalf, radius, mPaint);
        } else {
            mCanvas.save();
            mCanvas.drawRect(new Rect(0, 0, mImageSize, mImageSize), mPaint);

            mPaint.setColor(Color.TRANSPARENT);
            // A out B http://en.wikipedia.org/wiki/File:Alpha_compositing.svg
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            mCanvas.drawCircle(mImageSizeHalf, mImageSizeHalf, radius, mPaint);
            mCanvas.restore();

        }

    }
}
