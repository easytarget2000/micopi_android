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
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

import java.util.ArrayList;

/**
 * Utility class containing the actual paint methods for generating a contact picture.
 *
 * Created by Michel on 23.01.14.
 */
public class MicopiPainter {
    // TODO: Is this accurate enough for the task or should I use (float) Math.PI?
    private static final float pi = 3.14159f;

    /**
     * Draws a circular gradient from the given color to a lighter one.
     * @param baseColor    Base color to draw with
     * @param xChar  MD5 character that determines the center's x-position
     * @param yChar  MD5 character that determines the cetner's y-position
     * @param currentLoop    Current drawing loop
     * @param canvas    Canvas to draw on
     */
    public static void paintCanvasGradient(
            int baseColor,
            char xChar,
            char yChar,
            float imageSize,
            int currentLoop,
            Canvas canvas
    ) {
        // Calculate the starting point and radius.
        float fXPos = imageSize - ((float) xChar * 3f);
        float fYPos = imageSize - ((float) yChar * 3.5f);
        int iRadius = (int) (imageSize * 1.5);

        // Set up the gradient.
        RadialGradient gradient = new RadialGradient(
                fXPos,
                fYPos,
                iRadius,
                baseColor,
                baseColor + 0x00222222,
                Shader.TileMode.MIRROR);

        // Configure paint.
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setShader(gradient);

        // Adjust the transparency and radius if this is the second loop.
        if (currentLoop == 1) {
            iRadius *= 2;
            paint.setAlpha(150);
        }

        canvas.drawCircle(fXPos, fYPos, iRadius, paint);
    }

    /**
     *
     * @param polygon   List of (three) vertices.
     * @param cAlphaFactor  MD5 character that determines the alpha value of this triangle
     * @param isFilled  MD5 character that determines the
     * @param iIteration    Iteration factor that determines the alpha value of this triangle
     * @param fTriangleA    Length of the base side of the triangle
     * @param canvas    Canvas to draw on
     */
    public static void paintMicopiPolygon(
            Canvas canvas,
            ArrayList<Vertex> polygon,
            char cAlphaFactor,
            int iAlphaFactor2,
            boolean isFilled,
            int iIteration,
            float fTriangleA
   ) {
        Paint paint = new Paint(Color.WHITE);
        Path path = new Path();
        boolean isFirstVertex = true;       // path.isEmpty() does not seem to become false in API10
        int iAlpha = cAlphaFactor + iAlphaFactor2;
        int shaderColor1, shaderColor2;

        iAlpha /= iIteration + 1;

        // No extreme alpha values are allowed.
        while (iAlpha > 80)
            iAlpha -= cAlphaFactor;
        while (iAlpha < 30)
            iAlpha += cAlphaFactor;

        if (cAlphaFactor % 2 == 0) {
            shaderColor1 = 0xAA555555;
            shaderColor2 = 0x77EEEEEE;
        } else {
            shaderColor1 = 0x99443344;
            shaderColor2 = 0x77FFFF99;
        }

        // Paint one transparent white triangle to alter the base color.
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));

        paint.setDither(true);
        paint.setShader(new LinearGradient(
                0,
                0,
                0,
                fTriangleA,
                shaderColor1,
                shaderColor2,
                Shader.TileMode.CLAMP)
       );

        // Some pictures have filled triangles, others just lines.
        if (isFilled) {
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setStrokeWidth((float) cAlphaFactor *.07f);
            //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
            paint.setStyle(Paint.Style.STROKE);
//            iAlpha /= 2;
        }
        paint.setAlpha(iAlpha);
        path.reset();

        for (Vertex v : polygon) {
            if (isFirstVertex) {
                path.moveTo(v.x, v.y);
                isFirstVertex = false;
            } else path.lineTo(v.x, v.y);
        }

        path.close();
        canvas.drawPath(path, paint);
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

    public static final float PI_DOUBLED_FLOAT = 2f * (float) Math.PI;

    /**
     * Paints two shapes on top of each other with slightly different alpha,
     * size and stroke width values.
     *
     * @param canvas Canvas to draw on
     * @param paintMode Determines the shape to draw
     * @param xfermode Paint PorterDuffMode
     * @param color Paint colour
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
            PorterDuffXfermode xfermode,
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

        if (xfermode != null) paint.setXfermode(xfermode);

        // All filled mode int have a value >= 10.
        if (paintMode >= MODE_CIRCLE_FILLED) paint.setStyle(Paint.Style.FILL);
        else paint.setStyle(Paint.Style.STROKE);

        //Calculate the polygon values if needed.
        float edgeLength = 0f;   // Distance between two vertices.
        float deltaAngle = 0f;   // The inner angle that will be added to the current path angle
        if (paintMode == MODE_POLYGON) {
            edgeLength = radius * 3f * FloatMath.sin(pi / numOfEdges);
            float n = (float) numOfEdges;
            deltaAngle = ((n-2f)/n) * pi;
        }

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
                        final float angle = PI_DOUBLED_FLOAT * i / numOfEdges;
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

    public static final int BEAM_SPIRAL = 0;

    public static final int BEAM_SOLAR = 1;

    public static final int BEAM_STAR = 2;

    public static final int BEAM_WHIRL = 3;

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

        Log.d("Painting Beams", paintMode + " Alpha: " + alpha);

        // Calculate the lengths and angles.
//        float lineLength    =  * lengthUnit;
//        double angle        = ((float) factorChar4 * 0.15f) * lengthUnit;

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

    /**
     * Alpha value of character that will be drawn on top of the picture
     */
    private static final char CHAR_ALPHA = 255;

    /**
     * Paints letters on top of the centre of a canvas - GMail style
     * @param canvas Canvas to draw on
     * @param chars Characters to draw
     * @param color Paint colour
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
     * How much of the canvas the circle is going to occupy
     */
    private static final float CIRCLE_SIZE = 0.8f;

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
