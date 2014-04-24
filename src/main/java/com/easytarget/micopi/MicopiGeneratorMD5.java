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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

import java.util.ArrayList;

/**
 * Functional class containing the methods to generate a seemingly random image
 * out of given contact values, such as name and telephone number.
 *
 * Only the public static method getGeneratedBitmap may be called.
 *
 * Created by Michel on 14.01.14.
 */
public class MicopiGeneratorMD5 {
    //private static final float CONTACT_ICON_SIZE = 1080f;
    private float mImageSize = 1080f;
    private String mMd5String, mContactName, mContactFirstName;
    private Bitmap mGeneratedBitmap = null;
    private Canvas mCanvas;
    private float mCenterX, mCenterY;

    /**
     * Constructor method
     * @param contactName    Name of the contact
     * @param md5String    Encrypted contact data
     */
    public MicopiGeneratorMD5( String contactName, String md5String ) {

        Log.d("MicopiGeneratorMD5,Constructor", contactName + " " + md5String);

        // Assign the instance variables.
        mContactName = contactName;
        if ( md5String.length() > 32 ) this.mMd5String = md5String;
        else this.mMd5String = md5String + contactName;

        // Older Android versions probably run on slower devices with a lower resolution.
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ) mImageSize = 720f;

        // Set up the bitmap and the canvas.
        mGeneratedBitmap = Bitmap.createBitmap(
                (int) mImageSize,
                (int) mImageSize,
                Bitmap.Config.RGB_565
        );

        mCanvas = new Canvas( mGeneratedBitmap );
    }

    public Bitmap generateBitmap() {

//        if ( mMd5String.charAt( 8 ) % 3 == 0 ) {
//            generateOldModeImage();
//        } else {
//            generateCircleScape();
//        }

        // Count the number of spaces/words in the contact's name
        int iNumberOfWords = 1;
        for (int i = 0; i < mContactName.length(); i++  ) {
            if (mContactName.charAt(i) == ' ' ) {
                iNumberOfWords++;
                // Store the first part of the name seperately.
                if (mContactFirstName == null && i > 0) {
                    mContactFirstName = mContactName.substring( 0, i);
                }
            }
        }
        if (mContactFirstName == null) mContactFirstName = mContactName;

        /*
        Most of the painting is done here:
        */
        generateCircleScape();

        /*
        A name with three or more words is more likely to get the circles.
        The higher the number the less likely circles are.
        */
        int circleProbFactor = 4;
        if ( iNumberOfWords > 2 ) circleProbFactor = 2;

        switch ( mMd5String.charAt( 20 ) % circleProbFactor ) {
            case 0:     // Paint circles depending on the number of words.
                for ( int i = 0; i < iNumberOfWords; i++ )
                    MicopiPainter.paintMicopiCircle(
                            false, 0, i, iNumberOfWords, Color.WHITE, mCenterX, mCenterY, 1.6f,
                            mMd5String.charAt(11), mImageSize, mCanvas
                    );
                break;
            default:    // Paint that flower.
                MicopiPainter.paintMicopiBeams(
                        mMd5String.charAt(17), mMd5String.charAt(12), mMd5String.charAt(13),
                        mMd5String.charAt(5), mCenterX, mCenterY, mImageSize, mCanvas
                );
        }


        return mGeneratedBitmap;
    }

    /**
     * Generates a color, based on the given input parameters.
     * @param cFirstChar    First character of the contact's name
     * @param cFactor1  MD5 Character
     * @param cFactor2  MD5 Character
     * @param iNumberOfWords    Number of Words in the contact's name
     * @return  Color with alpha=255
     */
    private static int generateColor(char cFirstChar, char cFactor1,
                                     char cFactor2, int iNumberOfWords) {

        int iGeneratedColor = Color.DKGRAY;
        if ( cFirstChar % 2 == 0 ) iGeneratedColor = Color.YELLOW;

        iGeneratedColor *= cFirstChar * -cFactor1 * iNumberOfWords * cFactor2;
        iGeneratedColor |= 0xff000000;

        return iGeneratedColor;
    }

    private void generateOldModeImage() {

        // The polygon density is determined by the length of the name and at least 6.
        int polygonDensity = 6;
        if ( mContactName.length() > 6 ) polygonDensity = (int) ( mContactName.length() * .7 );

        // Set the center coordinates of the geometric figures.
        mCenterX = ( mImageSize * (float) mMd5String.charAt( 2 ) * .008f );
        mCenterY = ( mImageSize * (float) mMd5String.charAt( 3 ) * .008f );

        /**
         *  Paint two images on top of each other.
         */
        for ( int i = 0; i < 2; i++ ) {

            if ( i == 1 ) mMd5String += mContactName;
            polygonDensity += i * 2;

            // Generate a base color.
            int iBaseColor = generateColor(
                    mContactName.charAt( 0 ),
                    mMd5String.charAt( 10 ),
                    mMd5String.charAt( 28 ),
                    2
            );

            MicopiPainter.paintCanvasGradient( iBaseColor, mMd5String.charAt( 23 ),
                    mMd5String.charAt( 22 ), mImageSize, i, mCanvas );
            generatePolygonMesh( i, polygonDensity );
        }

        /**
         * Decide on circles or beams and paint them.
         */
//        Log.d( "charAt20 % 4", "" + strMd5.charAt( 20 ) % circleProbFactor );


    }

    private void generateCircleScape() {

        // Starting with Android 3.0, the images should have a white background.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mCanvas.drawColor( Color.WHITE );
        }

        /*
        About half of images drawn with this method will use polygons instead of circles.
        The length of the first name determines the number of vertices.
         */
        boolean paintPolygon = false;
        int numOfEdges = mContactFirstName.length();
        if ( mMd5String.charAt(14) % 2 == 0 && numOfEdges > 2 ) paintPolygon = true;

        // Draw all the shapes.
        int numberOfShapes  = mContactName.length() * 4;
        int md5Length       = mMd5String.length();
        int md5Pos          = 0;
        float shapeWidth    = 0.1f;
        int shapeColor      = generateColor(
                mMd5String.charAt(5), mMd5String.charAt(6), mMd5String.charAt(7), 10);

        float x = mImageSize * .5f;
        float y = mImageSize * .5f;
        for (int i = 0; i < numberOfShapes; i++) {
            char md5Char = ' ';

            // Do the operation for the x- and y-coordinate.
            for (int axis = 0; axis < 2; axis++ ) {
                // Make sure we do not jump out of the MD5 String.
                if (md5Pos >= md5Length) md5Pos = 0;

                // Move the coordinates around.
                md5Char = mMd5String.charAt(md5Pos);
                if ( md5Char % 2 == 0 ) {
                    if ( axis == 0 ) x += md5Char;
                    else y += md5Char;
                }
                else {
                    if ( axis == 0 ) x -= md5Char;
                    else y-= md5Char;
                }
                md5Pos++;
            }

            // The new coordinates have been generated. Paint something.
            MicopiPainter.paintMicopiCircle(
                    paintPolygon,
                    numOfEdges,
                    1,
                    1,
                    shapeColor,
                    x,
                    y,
                    shapeWidth,
                    md5Char,
                    mImageSize,
                    mCanvas
            );
            shapeWidth += .05f;
        }
    }

    /**
     * Creates a bunch of vertices, turns them into polygons and
     * uses MicopiPainter to draw them.
     * @param iIteration    Amount of times this method has been called
     */
    private void generatePolygonMesh( int iIteration, int polygonDensity ) {
        if ( mMd5String == null ) return;

        // Vertex variables:
        ArrayList<Vertex> allVerticesList = new ArrayList<Vertex>();
        float fTriangleH    = ( mImageSize / (float) polygonDensity);
        float fTriangleA    = ( fTriangleH * 2f ) / FloatMath.sqrt( 3f );
        float fOffset       = ( mMd5String.charAt( 1 ) * iIteration * .2f );
        float fXCurrent, fYCurrent;
        boolean isEvenColumn = true;    // Used for VERTEX and POLYGON columns
        int iOddColumn;                 // Odd VERTEX columns have one vertex more at the bottom.

        /**
         * Set up the entire vertex mesh.
         */
        polygonDensity += iIteration * 2;
        for ( int x = 0; x <= polygonDensity; x++ ) {
            fXCurrent = ( fTriangleH * x ) - fOffset;

            if ( isEvenColumn )  iOddColumn = 0;
            else    iOddColumn = 1;

            for ( int y = 0; y < polygonDensity + iOddColumn; y++ ) {
                //Vertex v = new Vertex();

                fYCurrent = fTriangleA * y;
                if ( !isEvenColumn ) fYCurrent -= fTriangleA * .5f;

                fYCurrent -= fOffset;

                //v.publicX = fXCurrent;
                //v.publicY = fYCurrent;
                allVerticesList.add( new Vertex( fXCurrent, fYCurrent ) );
            }

            isEvenColumn = !isEvenColumn;
        }

        /**
         * Reshape the mesh.
         */
        allVerticesList = modifyVerticesMesh( allVerticesList, mCenterX, mCenterY);

        /**
         * Variable preparation for POLYGONS:
         */
        // Polygon variables:
        int iMD5Char = 0;
        int iFirstPolVertex = 0;    // First vertex of the current polygon
        int iFirstColVertex = 0;    // First vertex of the first polygon of this column
        int iNextPolVertex;
        int iAlphaFactor    = 10;   // To be used for painting
        int iVertexLimit    = allVerticesList.size();   // Store the count instead of looking it up.
        boolean isLeftArrow = true; // Is the current polygon/triangle essentially a left arrow?
        isEvenColumn = true; // Even or odd POLYGON column?

        // Decide if this mesh will be filled polygon faces or grid-lines.
        boolean isFilledMesh = ( mMd5String.charAt( 11 ) % 5 != 0 );

        /**
         * Draw the POLYGONS using the vertices list.
         */
        while ( iFirstPolVertex < iVertexLimit  ) {
            // Generate seemingly random alpha values.
            iAlphaFactor++;
            if ( isLeftArrow ) {
                iAlphaFactor -= 30;
                iMD5Char++;
            }
            else {
                iAlphaFactor += 25;
                iMD5Char += 2;
            }
            if ( iMD5Char >= mMd5String.length() ) iMD5Char = 0;

            // Move the path to the first vertex of this polygon.
            ArrayList<Vertex> polygon = new ArrayList<Vertex>();
            polygon.add( allVerticesList.get( iFirstPolVertex ) );

            // Find the second vertex of this polygon.
            if ( isEvenColumn )
                if ( isLeftArrow ) iNextPolVertex = iFirstPolVertex + polygonDensity;
                else iNextPolVertex = iFirstPolVertex + 1;
            else
                if ( isLeftArrow ) iNextPolVertex = iFirstPolVertex + 1;
                else iNextPolVertex = iFirstPolVertex - polygonDensity - 1;

            if ( iNextPolVertex >= iVertexLimit ) break;
            polygon.add( allVerticesList.get( iNextPolVertex ) );

            // Find the third vertex of this polygon.
            if ( isEvenColumn )
                iNextPolVertex = iFirstPolVertex + polygonDensity + 1;
            else
                iNextPolVertex = iFirstPolVertex - polygonDensity;

            if ( iNextPolVertex >= iVertexLimit ) break;
            polygon.add( allVerticesList.get( iNextPolVertex ) );

            // Three vertices were added to this polygon. Paint it.
            MicopiPainter.paintMicopiPolygon( polygon, mMd5String.charAt( iMD5Char ), iAlphaFactor,
                    isFilledMesh, iIteration, fTriangleA, mCanvas);

            // Check if a column could be finished.
            // TODO: Improve this programming style.
            if ( iFirstPolVertex == iFirstColVertex + polygonDensity - 1 ) {
                 // Check if a left pointing arrow in an odd column was found...
                if ( isEvenColumn && isLeftArrow ) {
                    isEvenColumn = false;
                    iFirstPolVertex += polygonDensity + 1;
                    iFirstColVertex = iFirstPolVertex + 1;
                } else if ( !isEvenColumn && !isLeftArrow ) { // or a right pointing one in an even col.
                    isEvenColumn = true;
                    iFirstPolVertex = iFirstColVertex - 1;
                }
                iAlphaFactor = iFirstColVertex;
            }

             // Go to the next triangle.
            isLeftArrow = !isLeftArrow;

            // The starting vertex changes on left-pointing triangles in even columns
            // and on right-pointing triangles in odd columns.
            if ( isEvenColumn == isLeftArrow ) iFirstPolVertex++;

        }
    }

    /**
     * Creates a bulge in the mesh.
     * @param verticesMesh  A list of vertices that are to be modified
     * @param fXCenter  X-coordinate of the center of the bulge
     * @param fYCenter  Y-coordinate of the center of the bulge
     * @return  Modified list of vertices
     */
    private ArrayList<Vertex> modifyVerticesMesh( ArrayList<Vertex> verticesMesh,
                                                         float fXCenter, float fYCenter ) {
        int iListLimit = verticesMesh.size();
        float fDistanceFactor =  1.5f * mImageSize;
        float fCenterDistance, fXCurrent, fYCurrent;
        Vertex v;

        for ( int i = 0; i < iListLimit; i++ ) {
            v = verticesMesh.get( i );
            fXCurrent = v.publicX;
            fYCurrent = v.publicY;

            // Calculate the distance of this vertex to the generated center point.
            // sqrt( x-distance^2 + y-distance^2 )
            fCenterDistance = ( fXCurrent - fXCenter ) * ( fXCurrent - fXCenter ) +
                    ( fYCurrent - fYCenter ) * ( fYCurrent - fYCenter );
            fCenterDistance = FloatMath.sqrt( fCenterDistance );
            fCenterDistance = fDistanceFactor / fCenterDistance;

            if ( fXCurrent - fXCenter < 0 ) v.publicX -= fCenterDistance;
            else v.publicX += fCenterDistance;

            if ( fYCurrent - fYCenter < 0 ) v.publicY -= fCenterDistance;
            else v.publicY += fCenterDistance;

            verticesMesh.set(i, v);
        }

        return  verticesMesh;
    }

}
