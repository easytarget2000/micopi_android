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
import android.graphics.Shader;
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
    public static void paintCanvasGradient( int baseColor, char xChar, char yChar,
                                            float imageSize, int currentLoop, Canvas canvas ) {
        Paint paint = new Paint();
        float fXPos = imageSize - ( (float) xChar * 3f );
        float fYPos = imageSize - ( (float) yChar * 3.5f );
        int iRadius = (int) ( imageSize * 1.5 );

        if ( currentLoop == 1 ) {
            iRadius *= 2;
            paint.setAlpha( 150 );
        }

        RadialGradient gradient = new RadialGradient(
                fXPos,
                fYPos,
                iRadius,
                baseColor,
                baseColor + 0x00222222,
                Shader.TileMode.MIRROR );

        paint.setDither( true );
        paint.setShader( gradient );

        canvas.drawCircle( fXPos, fYPos, iRadius, paint );
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
            ArrayList<Vertex> polygon,
            char cAlphaFactor,
            int iAlphaFactor2,
            boolean isFilled,
            int iIteration,
            float fTriangleA,
            Canvas canvas
    ) {
        Paint paint = new Paint( Color.WHITE );
        Path path = new Path();
        boolean isFirstVertex = true;       // path.isEmpty() does not seem to become false in API10
        int iAlpha = cAlphaFactor + iAlphaFactor2;
        int shaderColor1, shaderColor2;

        iAlpha /= iIteration + 1;

        // No extreme alpha values are allowed.
        while ( iAlpha > 80 )
            iAlpha -= cAlphaFactor;
        while ( iAlpha < 30 )
            iAlpha += cAlphaFactor;

        if ( cAlphaFactor % 2 == 0 ) {
            shaderColor1 = 0xAA555555;
            shaderColor2 = 0x77EEEEEE;
        } else {
            shaderColor1 = 0x99443344;
            shaderColor2 = 0x77FFFF99;
        }

        // Paint one transparent white triangle to alter the base color.
        paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.LIGHTEN ) );

        paint.setDither(true);
        paint.setShader( new LinearGradient(
                0,
                0,
                0,
                fTriangleA,
                shaderColor1,
                shaderColor2,
                Shader.TileMode.CLAMP)
        );

        // Some pictures have filled triangles, others just lines.
        if ( isFilled ) {
            paint.setStyle( Paint.Style.FILL );
        } else {
            paint.setStrokeWidth( (float) cAlphaFactor *.07f );
            //paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.LIGHTEN ) );
            paint.setStyle( Paint.Style.STROKE );
//            iAlpha /= 2;
        }
        paint.setAlpha( iAlpha );
        path.reset();

        for ( Vertex v : polygon ) {
            if ( isFirstVertex ) {
                path.moveTo( v.publicX, v.publicY );
                isFirstVertex = false;
            } else path.lineTo( v.publicX, v.publicY);
        }

        path.close();
        canvas.drawPath( path, paint );

    }

    /**
     * Draws two circles on top of each other. The second one is larger than the first one.
     * The first one is more visible though because it has the combined alpha values of both.
     *
     * @param currentNum     Current circle number
     * @param numOfShapes   Total number of circles to draw
     * @param centerX   X-coordinate of circle center
     * @param centerY   Y-coordinate of circle center
     * @param radiusFactor      Will be multiplied with generated radius
     * @param widthChar Character that determines the width of the lines
     * @param canvas    Canvas to draw on
     */
    public static void paintMicopiCircle(
            boolean paintPolygon,
            int numOfEdges,
            int currentNum,
            int numOfShapes,
            int addColor,
            float centerX,
            float centerY,
            float radiusFactor,
            char widthChar,
            float imageSize,
            Canvas canvas
    ) {

        float strokeWidth  = (imageSize * (float) widthChar * .0015f);
        float innerRadius = radiusFactor *
                ( strokeWidth + (strokeWidth * currentNum) + (strokeWidth * currentNum) );

        if (currentNum > 2) innerRadius -= strokeWidth;

        // Configure paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        switch(widthChar % 4) {
            case 0:
                paint.setColor(addColor);
                break;
            case 1:
                paint.setColor(Color.BLACK);
                break;
            case 2:
                paint.setColor(Color.WHITE);
                break;
            default:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
                paint.setColor(Color.RED);
        }

        int alpha = (int) (60 * ((double) (currentNum + 1) / (double) numOfShapes));
        paint.setAlpha(alpha);

        //Calculate the polygon values if needed.
        float edgeLength = 0f;   // Distance between two vertices.
        float deltaAlpha = 0f;   // The inner angle that will be added to the current path angle
        if (paintPolygon) {
            edgeLength = innerRadius * 2f * FloatMath.sin(pi/numOfEdges);
            float n = (float) numOfEdges;
            deltaAlpha = ((n-2f)/n) * pi;
        }

        // Draw two shapes of the same kind.
        for (int i = 0; i < 2; i++) {
            if (!paintPolygon) {
                canvas.drawCircle(centerX, centerY, innerRadius, paint);
            } else {
                Path polygonPath = new Path();
                float pathAlpha = 0f;

                // Estimate a lower left corner of the polygon from the center coordinates.
                float x = centerX - innerRadius;
                float y = centerY + innerRadius;
                polygonPath.moveTo(x,y);

                for (int j = 0; j < numOfEdges; j++) {
                    x +=  FloatMath.cos(pathAlpha) * edgeLength;
                    y -=  FloatMath.sin(pathAlpha) * edgeLength;
                    polygonPath.lineTo(x,y);
                    pathAlpha += deltaAlpha;
                }

                polygonPath.close();
                canvas.drawPath(polygonPath, paint);
            }

            // Draw the second shape differently.
            innerRadius -= strokeWidth * .32f;
        }

    }

    /**
     * Draws beams in a vortex-/flower-like manner.
     * @param factorChar1    MD5 character that determines the density of the beams
     * @param factorChar2     MD5 character that determines the length of the beams
     * @param factorChar4      MD5 character that determines the angle of the beams
     * @param canvas    Canvas to draw on
     */
    public static void paintMicopiBeams(char factorChar1, char factorChar2, char factorChar3,
                                        char factorChar4, float centerX, float centerY,
                                        float imageSize, Canvas canvas) {
        Paint paint = new Paint();
        int paintAlpha  = 40;
        int density     = factorChar1 * 3;
        int paintStyle  = 0;                    // Should only be 0, 1, 2 or 3
        float lengthUnit = ( imageSize / 200f );
        float lineLength = ( (float) factorChar2 * .6f ) * lengthUnit;
        double angle    = ( (float) factorChar4 * .15f ) * lengthUnit;
        double deltaAngle;
        float lineStartX = centerX;
        float lineStartY = centerY;
        float lineEndX, lineEndY;

        // Define how to paint.
        // P_n = 25%
        if ( factorChar1 % 2 == 0 ) {
            if ( factorChar4 % 2 == 0 ) paintStyle = 1;     // 1 :Solar Beams
            else paintStyle = 2;        // 2: Star
        } else {
            if ( factorChar2 % 2 == 0 ) paintStyle = 3;    // 3: Whirl
        }   // 0: Spiral

        // Define how the angle should change after every line.
        if ( factorChar3 % 3 != 0 ) deltaAngle = (double) ( 10f * lengthUnit );
        else deltaAngle = (double) lengthUnit;

//        Log.d("Beams", "PaintStyle: " + paintStyle + " deltaAngle: " + deltaAngle);

        // Configure paint
        paint.setAntiAlias( true );
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.ADD ) );
        } else {
            paintAlpha *= 4;
        }

        if ( factorChar3 % 3 == 0) paint.setColor( Color.RED );
        else paint.setColor(Color.WHITE );

        if ( factorChar3 % 2 == 0 )  paint.setStrokeWidth( 8f );
        else paint.setStrokeWidth( 24f );

        paint.setStyle( Paint.Style.STROKE );
        paint.setAlpha( paintAlpha );

        for ( int i = 0; i < density; i++ ) {
            lineEndX = lineStartX + ( (float) Math.cos( angle ) * lineLength );
            lineEndY = lineStartY + ( (float) Math.sin( angle ) * lineLength );

            canvas.drawLine( lineStartX, lineStartY, lineEndX, lineEndY ,paint );

            angle += deltaAngle;
            lineLength += lengthUnit;

            switch ( paintStyle ) {
                case 0:
                    lineStartX = lineEndX;
                    lineStartY = lineEndY;
                    break;
                case 1:
                    lineStartX = centerX;
                    lineStartY = centerY;
                    break;
                case 2:
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

//    public static void paintMicopiLines( char[] contactName, char widthChar, Canvas canvas ) {
//        Paint paint = new Paint();
//        float distUnit = ( CONTACT_ICON_SIZE / contactName.length ) * .75f;
//        float centerX = CONTACT_ICON_SIZE * .5f;
//        float lineY = distUnit * .5f;
//        float lineEndX;
//
//        paint.setColor( Color.WHITE );
//        paint.setAntiAlias( true );
//        paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.LIGHTEN ) );
//        paint.setAlpha( 50 );
//        paint.setStrokeWidth( distUnit );
//
//
//        for ( char currentChar : contactName ) {
//
//            if ( currentChar > 47 ) {
//                lineEndX = centerX + ( ( currentChar - 96 ) * 20 );
//                canvas.drawLine( centerX, lineY, lineEndX, lineY, paint );
//            }
//            lineY += distUnit;
//
//
//        }
//    }
}
