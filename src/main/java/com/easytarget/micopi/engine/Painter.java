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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

import java.util.Random;

/**
 * Utility class containing the actual paint methods for generating a contact picture.
 *
 * Created by Michel on 23.01.14.
 */
public class Painter {

    private static final String LOG_TAG = Painter.class.getSimpleName();

    private Canvas mCanvas;

    private int mImageSize;

    private float mImageSizeHalf;

    private Paint mPaint;

    /** Constructor */
    public Painter(Canvas canvas) {
        if (canvas == null) {
            Log.e(LOG_TAG, "Null canvas.");
            return;
        }


        mCanvas = canvas;
        mImageSize = canvas.getWidth();
        mImageSizeHalf = mImageSize * 0.5f;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
//        paint.setFilterBitmap(true);
    }

    /**
     * @return Side length of the square canvas
     */
    public int getImageSize() {
        return mImageSize;
    }

    /** Adds paintGrain to the entire canvas */
    public void paintGrain() {
        Paint darkener = new Paint();
        darkener.setColor(Color.DKGRAY);
        darkener.setAlpha(15);
        Paint brightener = new Paint();
        brightener.setColor(Color.WHITE);
        brightener.setAlpha(20);
        final int grainDensity = mImageSize / 7;
        final Random random = new Random();
        float darkenerX;
        for (int y = 0; y < mImageSize; y++) {
            for (int i = 0; i < grainDensity; i++) {
                darkenerX = random.nextFloat() * mImageSize;
                mCanvas.drawPoint(darkenerX, y, darkener);
                mCanvas.drawPoint(mImageSize - darkenerX, y, darkener);
                mCanvas.drawPoint(random.nextFloat() * mImageSize, y, brightener);
            }
        }
    }

    /**
     * Paints a styled square onto the canvas
     *
     * @param doPaintFilled Filled or stroked painting
     * @param color Paint color
     * @param alpha Paint alpha value
     * @param x X-coordinate of square centre
     * @param y Y-coordinate of square centre
     * @param size Side length
     */
    public void paintSquare(
            final boolean doPaintFilled,
            final int color,
            final int alpha,
            final float x,
            final float y,
            final float size
    ) {
        final float fOffsetX = x * size;
        final float fOffsetY = y * size;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setAlpha(alpha);
        if (doPaintFilled) paint.setStyle(Paint.Style.FILL);
        else paint.setStyle(Paint.Style.STROKE);

        mCanvas.drawRect(fOffsetX, fOffsetY, fOffsetX + size, fOffsetY + size, paint);
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
     * Shape definition: circle arc, stroked
     */
    public static final int MODE_ARC = 1;

    /**
     * Shape definition: circle arc, filled
     */
    public static final int MODE_ARC_FILLED = 11;

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
     * @param paintMode Determines the shape to draw
     * @param color Paint color
     * @param alpha Paint alpha value
     * @param strokeWidth Paint stroke width
     * @param numOfEdges Number of polygon edges
     * @param fArcStartAngle Start angle of an arc
     * @param fArcEndAngle End angle of an arc
     * @param fCenterX X coordinate of the centre of the shape
     * @param fCenterY Y coordinate of the centre of the shape
     * @param radius Also determines size of polygon approximations
     */
    public void paintDoubleShape(
            final int paintMode,
            final int color,
            final int alpha,
            final float strokeWidth,
            final int numOfEdges,
            final float fArcStartAngle,
            final float fArcEndAngle,
            final float fCenterX,
            final float fCenterY,
            float radius
    ) {
        // Configure paint:
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        mPaint.setStrokeWidth(strokeWidth);

        // All filled mode int have a value >= 10.
        if (paintMode >= MODE_CIRCLE_FILLED) mPaint.setStyle(Paint.Style.FILL);
        else mPaint.setStyle(Paint.Style.STROKE);

        // Draw two shapes of the same kind.
        for (int i = 0; i < 2; i++) {
            if (paintMode == MODE_ARC || paintMode == MODE_ARC_FILLED) {
                final RectF oval = new RectF();
                oval.set(
                        fCenterX - radius,
                        fCenterY - radius,
                        fCenterX + radius,
                        fCenterY + radius
                );
                final Path fArcPath = new Path();
                fArcPath.arcTo(oval, fArcStartAngle, fArcEndAngle, true);

            } else if (paintMode == MODE_POLYGON || paintMode == MODE_POLYGON_FILLED) {
                if (numOfEdges == 4) {
                    mCanvas.drawRect(
                            fCenterX - radius * 0.5f,
                            fCenterY - radius * 0.5f,
                            fCenterX + radius * 0.5f,
                            fCenterY + radius * 0.5f,
                            mPaint
                    );
                } else {
                    Path polygonPath = new Path();
                    // Use Path.moveTo() for first vertex.
                    boolean isFirstEdge = true;

                    for (i = 1; i <= numOfEdges; i++) {
                        final float angle = TWO_PI * i / numOfEdges;
                        final float x = fCenterX + radius * FloatMath.cos(angle);
                        final float y = fCenterY + radius * FloatMath.sin(angle);

                        if (isFirstEdge) {
                            polygonPath.moveTo(x,y);
                            isFirstEdge = false;
                        } else {
                            polygonPath.lineTo(x,y);
                        }
                    }

                    polygonPath.close();
                    mCanvas.drawPath(polygonPath, mPaint);
                }
            } else {
                mCanvas.drawCircle(fCenterX, fCenterY, radius, mPaint);
            }

            // Draw the second shape differently.
            radius -= strokeWidth * 0.5f;
            mPaint.setAlpha((int) (alpha * 0.75f));
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
     * @param color Paint color
     * @param alpha Paint alpha value
     * @param fPaintMode Determines the shape to draw
     * @param centerX X coordinate of the centre of the shape
     * @param centerY Y coordinate of the centre of the shape
     * @param fDensity Density of the beams
     * @param lineLength Length of the beams
     * @param angle Angle between single beams
     * @param fGroupAngle Angle between beam groups
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
            float angle,
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
        mPaint.setStyle(Paint.Style.STROKE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        }

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

    /** Distance between circle steps */
    private static final float PI_STEP_SIZE = (float) Math.PI / 50f;

    public void paintSpyro(
            final int fColor1,
            final int fColor2,
            final int fColor3,
            final int fAlpha,
            final float fPoint1Factor,
            final float fPoint2Factor,
            final float fPoint3Factor,
            final int fRevolutions
    ) {
        float fInnerRadius  = mImageSizeHalf * 0.5f;
        float fOuterRadius  = (fInnerRadius * 0.5f) + 1;
        float fRadiusSum    = fInnerRadius + fOuterRadius;

        final float point1 = fPoint1Factor * (mImageSize - fRadiusSum);
        final float point2 = fPoint2Factor * (mImageSize - fRadiusSum);
        final float point3 = fPoint3Factor * (mImageSize - fRadiusSum);

        final Path fPoint1Path = new Path();
        final Path fPoint2Path = new Path();
        final Path fPoint3Path = new Path();
        boolean moveTo = true;

        float t = 0f;
        float x, y, x2, y2, x3, y3;
        do {
            x = (float) (fRadiusSum * FloatMath.cos(t) +
                    point1 * Math.cos(fRadiusSum * t / fOuterRadius) + mImageSizeHalf);
            y = (float) (fRadiusSum * FloatMath.sin(t) +
                    point1 * Math.sin(fRadiusSum * t / fOuterRadius) + mImageSizeHalf);
            x2 = (float) (fRadiusSum * FloatMath.cos(t) +
                    point2 * Math.cos(fRadiusSum * t / fOuterRadius) + mImageSizeHalf);
            y2 = (float) (fRadiusSum * FloatMath.sin(t) +
                    point2 * Math.sin(fRadiusSum * t / fOuterRadius) + mImageSizeHalf);
            x3 = (float) (fRadiusSum * FloatMath.cos(t) +
                    point3 * Math.cos(fRadiusSum * t / fOuterRadius) + mImageSizeHalf);
            y3 = (float) (fRadiusSum * FloatMath.sin(t) +
                    point3 * Math.sin(fRadiusSum * t / fOuterRadius) + mImageSizeHalf);

            if (moveTo) {
                fPoint1Path.moveTo(x, y);
                fPoint2Path.moveTo(x2, y2);
                fPoint3Path.moveTo(x3, y3);
                moveTo = false;
            } else {
                fPoint1Path.lineTo(x, y);
                fPoint2Path.lineTo(x2, y2);
                fPoint3Path.lineTo(x3, y3);
            }
            t += PI_STEP_SIZE;
        } while (t < TWO_PI * fRevolutions);

        mPaint.setStyle(Paint.Style.STROKE);

        // Draw the first path.
        mPaint.setColor(fColor1);
        mPaint.setAlpha(fAlpha);
        mCanvas.drawPath(fPoint1Path, mPaint);
        // Draw the second path.
        mPaint.setColor(fColor2);
        mPaint.setAlpha(fAlpha);
        mCanvas.drawPath(fPoint2Path, mPaint);
        // Draw the third path.
        mPaint.setColor(fColor3);
        mPaint.setAlpha(fAlpha);
        mPaint.setStrokeWidth(2f);
        mCanvas.drawPath(fPoint3Path, mPaint);
    }

    /**
     * Alpha value of character that will be drawn on top of the picture
     */
    private static final char CHAR_ALPHA = 255;

    /**
     * Paints letters on top of the centre of a canvas - GMail style
     * @param chars Characters to draw
     * @param color Paint color
     */
    public void paintChars(char[] chars, int color) {
        int count = chars.length;
        if (count == 0) return;
        else if (count > 4) count = 4;

        mPaint.setColor(color);
        mPaint.setAlpha(CHAR_ALPHA);

        // Typeface, size and alignment:
        Typeface sansSerifLight = Typeface.create("sans-serif-light", 0);
        mPaint.setTypeface(sansSerifLight);

        final int tileLetterFontSize = (int) (70f * mImageSize / 100f);

        mPaint.setTextSize(tileLetterFontSize);
        mPaint.setTextAlign(Paint.Align.CENTER);

        // Get the rectangle that the text fits into.
        final Rect rect = new Rect();
        mPaint.getTextBounds(chars, 0, 1, rect);

        final float imageSizeHalf = mImageSize * 0.5f;

        mCanvas.drawText(
                chars,
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
        mPaint.setStyle(Paint.Style.FILL);

        final float imageCenter = mImageSize * 0.5f;
        final float radius      = imageCenter * CIRCLE_SIZE;

        mCanvas.drawCircle(imageCenter, imageCenter, radius, mPaint);
    }
}
