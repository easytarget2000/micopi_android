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

    import com.easytarget.micopi.Contact;

    /**
     * Created by michel on 12/11/14.
     *
     * Image generator
     */
    public class WanderingShapesGenerator {

        private static final String LOG_TAG = WanderingShapesGenerator.class.getSimpleName();

        private static final int DENSITY_FACTOR = 1;

        private static final int MIN_DENSITY = 4;

        /**
         * Fills the Canvas in the Painter with a lot of colourful circles
         * or polygon approximations of circles
         *
         * @param painter Paint the generated shape in this object
         * @param contact Data from this Contact object will be used to generate the shapes and colors
         */
        public static void generate(Painter painter, final Contact contact) {
            // If the first name has at least 3 (triangle) and no more than 6 (hexagon) letters,
            // there is a 2/3 chance that polygons will be painted instead of circles.
            final int numOfEdges = contact.getNameWord(0).length();
            final String md5String = contact.getMD5EncryptedString();

            // Some pictures have polygon approximations instead of actual circles.
            final boolean doPaintPolygon =
                    (md5String.charAt(15) % 2 == 0 && numOfEdges > 2 && numOfEdges < 9);

            // These characters will be used for color generating:
            final int colorFactor1 = contact.getFullName().charAt(0);

            // Determine if the shapes will be painted filled or stroked.
            boolean paintFilled = false;
            if (md5String.charAt(0) % 2 == 0) paintFilled = true;

            // Determine the alpha value to paint with.
            final int alpha  = 255 - (md5String.charAt(6) / 2);
            // Filled shapes have a smaller alpha value.
    //        if (paintFilled) alpha /= 2;
            float shapeWidth = (float) md5String.charAt(7) * 6f;

            // Draw all the shapes.
            final int md5Length  = md5String.length();
            int md5Pos = 0;
            float x = painter.getImageSize() * 0.5f;
            float y = x;

            // The amount of double shapes that will be painted:
            int numOfShapes = contact.getNumberOfLetters();
            if (numOfShapes < MIN_DENSITY) shapeWidth *= 2f;
    //        Log.d("Number of Circle Scape shapes", contact.getFullName() + " " + numOfShapes);

            final int paintMode;
    //        if (paintArc && ((md5Int % 2) == 0)) paintMode = Painter.MODE_ARC;
            if (doPaintPolygon) paintMode = Painter.MODE_POLYGON;
            else paintMode = Painter.MODE_CIRCLE;

            for (int i = 0; i < numOfShapes; i++) {
                // Get the next character from the MD5 String.
                md5Pos++;
                if (md5Pos >= md5Length) md5Pos = 0;

                // Move the coordinates around.
                final int md5Int = md5String.charAt(md5Pos) + i;
                switch (md5Int % 6) {
                    case 0:
                        x += md5Int;
                        y += md5Int;
                        break;
                    case 1:
                        x -= md5Int;
                        y -= md5Int;
                        break;
                    case 2:
                        x += md5Int * 2;
                        break;
                    case 3:
                        y += md5Int * 2;
                        break;
                    case 4:
                        x -= md5Int * 2;
                        y -= md5Int;
                        break;
                    default:
                        x -= md5Int;
                        y -= md5Int * 2;
                        break;
                }

                final int color = ColorCollection.getCandyColorForChar(md5String.charAt(md5Pos));
                final float radius = md5String.charAt(md5Pos) + md5String.charAt(2);
//                Log.d(
//                        LOG_TAG,
//                        paintMode + ", " + Integer.toHexString(color) + ", " + alpha + ", "
//                                + shapeWidth + ", " + numOfEdges + ", "
//                                + x + ", " + y + " ," + radius
//                );

                painter.paintShape(paintMode, color, alpha, shapeWidth, numOfEdges, x, y, radius);
                shapeWidth *= 1.05f;
            }
//            Log.d(LOG_TAG, "END");
        }
    }
