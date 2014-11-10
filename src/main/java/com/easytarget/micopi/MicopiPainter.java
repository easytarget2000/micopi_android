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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.FloatMath;

/**
 * Utility class containing the actual paint methods for generating a contact picture.
 *
 * Created by Michel on 23.01.14.
 */
public class MicopiPainter {

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
     * @param canvas Canvas to draw on
     * @param paintMode Determines the shape to draw
     * @param color Paint color
     * @param alpha Paint alpha value
     * @param strokeWidth Paint stroke width
     * @param numOfEdges Number of polygon edges
     * @param startAngle Start angle of an arc
     * @param endAngle End angle of an arc
     * @param centerX X coordinate of the centre of the shape
     * @param centerY Y coordinate of the centre of the shape
     * @param radius Also determines size of polygon approximations
     */
    public static void paintDoubleShape(
            Canvas canvas,
            int paintMode,
            int color,
            int alpha,
            float strokeWidth,
            int numOfEdges,
            float startAngle,
            float endAngle,
            float centerX,
            float centerY,
            float radius
    ) {
        // Configure paint:
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setStrokeWidth(strokeWidth);

        // All filled mode int have a value >= 10.
        if (paintMode >= MODE_CIRCLE_FILLED) paint.setStyle(Paint.Style.FILL);
        else paint.setStyle(Paint.Style.STROKE);

        // Draw two shapes of the same kind.
        for (int i = 0; i < 2; i++) {
            if (paintMode == MODE_ARC || paintMode == MODE_ARC_FILLED) {
                final RectF oval = new RectF();
                oval.set(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                );
                Path arcPath = new Path();
                arcPath.arcTo(oval, startAngle, endAngle, true);

            } else if (paintMode == MODE_POLYGON || paintMode == MODE_POLYGON_FILLED) {
                if (numOfEdges == 4) {
                    canvas.drawRect(
                            centerX - radius * 0.5f,
                            centerY - radius * 0.5f,
                            centerX + radius * 0.5f,
                            centerY + radius * 0.5f,
                            paint
                    );
                } else {
                    Path polygonPath = new Path();
                    // Use Path.moveTo() for first vertex.
                    boolean isFirstEdge = true;

                    for (i = 1; i <= numOfEdges; i++) {
                        final float angle = TWO_PI * i / numOfEdges;
                        final float x = centerX + radius * FloatMath.cos(angle);
                        final float y = centerY + radius * FloatMath.sin(angle);

                        if (isFirstEdge) {
                            polygonPath.moveTo(x,y);
                            isFirstEdge = false;
                        } else {
                            polygonPath.lineTo(x,y);
                        }
                    }

                    polygonPath.close();
                    canvas.drawPath(polygonPath, paint);
                }
            } else {
                canvas.drawCircle(centerX, centerY, radius, paint);
            }

            // Draw the second shape differently.
            radius -= strokeWidth * 0.5f;
            paint.setAlpha((int) (alpha * 0.75f));
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
     * @param canvas Canvas to draw on
     * @param color Paint color
     * @param alpha Paint alpha value
     * @param paintMode Determines the shape to draw
     * @param centerX X coordinate of the centre of the shape
     * @param centerY Y coordinate of the centre of the shape
     * @param density Density of the beams
     * @param lineLength Length of the beams
     * @param angle Angle between single beams
     * @param largeDeltaAngle Angle between beam groups
     * @param wideStrokes Wide beam groups
     */
    public static void paintMicopiBeams(
            Canvas canvas,
            int color,
            int alpha,
            int paintMode,
            float centerX,
            float centerY,
            int density,
            float lineLength,
            float angle,
            boolean largeDeltaAngle,
            boolean wideStrokes
    ) {
        final float lengthUnit = (canvas.getWidth() / 200f);
        lineLength *= lengthUnit;
        angle *= lengthUnit;

        // Define how the angle should change after every line.
        float deltaAngle;
        if (largeDeltaAngle) deltaAngle = 10f * lengthUnit;
        else deltaAngle = lengthUnit;

        // Configure paint:
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.STROKE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        }

        // Set wide or thin strokes.
        if (wideStrokes)  paint.setStrokeWidth(24f);
        else paint.setStrokeWidth(8f);
        float lineStartX = centerX;
        float lineStartY = centerY;
        float lineEndX, lineEndY;

        for (int i = 0; i < density; i++) {
            lineEndX = lineStartX + ((float) Math.cos(angle) * lineLength);
            lineEndY = lineStartY + ((float) Math.sin(angle) * lineLength);

            canvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY ,paint);

            angle += deltaAngle;
            lineLength += lengthUnit;

            switch (paintMode) {
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

    private static float PI_STEP_SIZE = (float) Math.PI / 50f;

    public static void paintSpyro(
            Canvas canvas,
            int color1,
            int color2,
            int color3,
            int alpha,
            float point1Relative,
            float point2Relative,
            float point3Relative,
            int revolutions
    ) {
        final float fImageSize = canvas.getWidth();
        final float fImageSizeHalf = fImageSize * 0.5f;
        float fInnerRadius  = fImageSizeHalf * 0.5f;
        float fOuterRadius  = (fInnerRadius * 0.5f) + 1;
        float fRadiusSum    = fInnerRadius + fOuterRadius;

        final float point1 = point1Relative * (fImageSize - fRadiusSum);
        final float point2 = point2Relative * (fImageSize - fRadiusSum);
        final float point3 = point3Relative * (fImageSize - fRadiusSum);

        Path point1Path = new Path();
        Path point2Path = new Path();
        Path point3Path = new Path();
        boolean moveTo = true;

        float t = 0f;
        float x, y, x2, y2, x3, y3;
        do {
            x = (float) (fRadiusSum * FloatMath.cos(t) +
                    point1 * Math.cos(fRadiusSum * t / fOuterRadius) + fImageSizeHalf);
            y = (float) (fRadiusSum * FloatMath.sin(t) +
                    point1 * Math.sin(fRadiusSum * t / fOuterRadius) + fImageSizeHalf);
            x2 = (float) (fRadiusSum * FloatMath.cos(t) +
                    point2 * Math.cos(fRadiusSum * t / fOuterRadius) + fImageSizeHalf);
            y2 = (float) (fRadiusSum * FloatMath.sin(t) +
                    point2 * Math.sin(fRadiusSum * t / fOuterRadius) + fImageSizeHalf);
            x3 = (float) (fRadiusSum * FloatMath.cos(t) +
                    point3 * Math.cos(fRadiusSum * t / fOuterRadius) + fImageSizeHalf);
            y3 = (float) (fRadiusSum * FloatMath.sin(t) +
                    point3 * Math.sin(fRadiusSum * t / fOuterRadius) + fImageSizeHalf);

            if (moveTo) {
                point1Path.moveTo(x, y);
                point2Path.moveTo(x2, y2);
                point3Path.moveTo(x3, y3);
                moveTo = false;
            } else {
                point1Path.lineTo(x, y);
                point2Path.lineTo(x2, y2);
                point3Path.lineTo(x3, y3);
            }
            t += PI_STEP_SIZE;
        } while (t < TWO_PI * revolutions);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        // draw the first path
        paint.setColor(color1);
        paint.setAlpha(alpha);
        canvas.drawPath(point1Path, paint);
        // draw the second path
        paint.setColor(color2);
        paint.setAlpha(alpha);
        canvas.drawPath(point2Path, paint);
        // draw the third path
        paint.setColor(color3);
        paint.setAlpha(alpha);
        paint.setStrokeWidth(2f);
        canvas.drawPath(point3Path, paint);
    }

    /**
     * Alpha value of character that will be drawn on top of the picture
     */
    private static final char CHAR_ALPHA = 255;

    /**
     * Paints letters on top of the centre of a canvas - GMail style
     * @param canvas Canvas to draw on
     * @param chars Characters to draw
     * @param color Paint color
     */
    public static void paintChars(Canvas canvas, char[] chars, int color) {
        int count = chars.length;
        if (count == 0) return;
        else if (count > 4) count = 4;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setAlpha(CHAR_ALPHA);


        // Typeface, size and alignment:
        Typeface sansSerifLight = Typeface.create("sans-serif-light", 0);
        paint.setTypeface(sansSerifLight);

        final float imageSize = canvas.getWidth();
        final int tileLetterFontSize = (int) (70f * imageSize / 100f);

        paint.setTextSize(tileLetterFontSize);
        paint.setTextAlign(Paint.Align.CENTER);

        // Get the rectangle that the text fits into.
        final Rect rect = new Rect();
        paint.getTextBounds(chars, 0, 1, rect);

        final float imageSizeHalf = imageSize * 0.5f;

        canvas.drawText(
                chars,
                0,
                count,
                imageSizeHalf,
                imageSizeHalf + (rect.bottom - rect.top) * 0.5f,
                paint
        );

    }

    /**
     * How much of the canvas the central circle is going to occupy
     */
    private static final float CIRCLE_SIZE = 0.8f;

    /**
     * Paints a circle in the middle of the image to enhance the visibility of the initial letter
     *
     * @param canvas Canvas to draw on
     * @param color Paint color
     * @param alpha Paint alpha value
     */
    public static void paintCentralCircle(Canvas canvas, int color, int alpha) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setAlpha(alpha);

        final float imageCenter = canvas.getWidth() * 0.5f;
        final float radius      = imageCenter * CIRCLE_SIZE;

        canvas.drawCircle(imageCenter, imageCenter, radius, paint);
    }
}
