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
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * Based on PlayfulJS Terrain Demo:
 * https://github.com/hunterloftis/playfuljs/blob/master/content/demos/terrain.html
 *
 * Created by Michel on 13.05.2014.
 */
public class MicopiTerrain {
    //private static final float ISO_FACTOR = 4f;
    private int mMax;
    private float[] mMap;
    private float mRoughness;
    private int mSize;

    public MicopiTerrain(int detail, float roughness) {
        mSize = (int) Math.pow(2, detail) + 1;
        mMax = mSize - 1;
        mMap = new float[mSize * mSize];
        mRoughness = roughness;

        this.generateMap();
    }

    private float getMapPoint(int x, int y) {
        if (x < 0 || x > mMax || y < 0 || y > mMax) return -1f;
        return mMap[x + mSize * y];
    }

    private void setMapPoint(int x, int y, float value) {
       mMap[x + mSize * y] = value;
    }

    private void generateMap() {

        this.setMapPoint(0, 0, mMax);
        this.setMapPoint(mMax, 0, mMax / 2);
        this.setMapPoint(mMax, mMax, 0);
        this.setMapPoint(0, mMax, mMax / 2);

        divideTerrain(mMax);
    }

    private void divideTerrain(int size) {
        int x, y;
        int half = size / 2;
        float scale = mRoughness * size;
        Log.d("Divide", "half: " + half);
        Log.d("Divide", "scale: " + scale);
        if (half < 1) return;

        for (y = half; y < mMax; y += size) {
            for (x = half; x < mMax; x += size) {
                float offset = (float) Math.random() * scale * 2f - scale;
                //Log.d("Divide", "square: offset: " + offset);
                //Log.d("Divide", "square: scale: " + scale);
                divideSquare(x, y, half, offset);
            }
        }
        for (y = 0; y <= mMax; y += half) {
            for (x = (y + half) % size; x <= mMax; x += size) {
                float offset = (float) Math.random() * scale * 2f - scale;
                divideDiamond(x, y, half, offset);
            }
        }
        divideTerrain(size / 2);
    }

    private float average(float[] values) {
        int numberOfValidValues = 0;
        float sumOfValidValues  = 0f;
        for (float f : values) {
            if (f != -1f) {
                Log.d("Average", "value: " + f);
                numberOfValidValues++;
                sumOfValidValues += f;
            }
        }

        Log.d("Average", "total: " + sumOfValidValues);

        float average = sumOfValidValues/numberOfValidValues;
        Log.d("Average", "return: " +  average);
        return average;
    }

    private void divideSquare(int x, int y, int size, float offset) {
        float points[] = {
                this.getMapPoint(x - size, y - size),   // upper left
                this.getMapPoint(x + size, y - size),   // upper right
                this.getMapPoint(x + size, y + size),   // lower right
                this.getMapPoint(x - size, y + size)    // lower left
        };
        float average = average(points);
        this.setMapPoint(x, y, average + offset);
    }

    private void divideDiamond(int x, int y, int size, float offset) {
        float points[] = {
            this.getMapPoint(x, y - size),      // top
            this.getMapPoint(x + size, y),      // right
            this.getMapPoint(x, y + size),      // bottom
            this.getMapPoint(x - size, y)       // left
        };
        float average = average(points);
        this.setMapPoint(x, y, average + offset);
    }

    public void drawMap(int width, int height, Canvas canvas) {

        float x0            = width * .5f;
        float y0            = height;
        float rectWidth     = width / mSize;
        float sizeFactor    = mSize *.5f;
        float waterVal      = mSize * .3f;

        for (int y = 0; y < mSize; y++) {
            for (int x = 0; x < mSize; x++) {
                float value = this.getMapPoint(x, y);
                Log.d("draw", x + " " + y + " " + value);
                Vertex top      = this.projectedPoint(x, y, value, x0, y0, sizeFactor, rectWidth);
                Vertex bottom   = this.projectedPoint(x + 1, y, 0, x0, y0, sizeFactor, rectWidth);
                Vertex water    = this.projectedPoint(x, y, waterVal, x0, y0, sizeFactor, rectWidth);
                int color = brightness(x, y, this.getMapPoint(x + 1, y) - value);

                Log.d("draw", "drawing");

                this.drawRect(top, bottom, color, canvas);
                this.drawRect(water, bottom, 0x443296C8, canvas);
            }
        }

    }

    public void drawRect(Vertex a, Vertex b, int color, Canvas canvas) {

        float right = b.y - a.y;
        float bottom = b.x - a.x;
        Log.d("drawRect", "a: " + a.x + " " + a.y );
        Log.d("drawRect", "b: " + b.x + " " + b.y );
        Log.d("drawRect", "right: " + right);
        Log.d("drawRect", "bottom: " + bottom);
        Log.d("drawRect", "color: " + Integer.toHexString(color));

        RectF rect = new RectF(a.x, a.y, b.x, b.y);

        if (b.y < a.y) {
            Log.i("drawRect", "b.y < a.y");
            return;
        }

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);

        //a.y += 500;
        //b.y += 500;

        canvas.drawRect(rect, paint);
    }

    private int brightness(int x, int y, float slope) {
        if (y == mMax || x == mMax) return 0;

        int slopeBrightness = (int) (slope * 50) + 128;

        int color = 0xFF000000;
        color += slopeBrightness;
        color += slopeBrightness << 8;
        color += slopeBrightness << 16;

        return color;
    }

    /*private Vertex isoPoint(int x, int y) {
        float isoX = ISO_FACTOR * (mSize + x - y);
        float isoY = ISO_FACTOR * (x + y);
        Log.d("Iso", x + " " + y + ", " + isoX + " " + isoY);
        return new Vertex(isoX, isoY);
    }*/

    private Vertex projectedPoint(int flatX,
                                  int flatY,
                                  float flatZ,
                                  float x0,
                                  float y0,
                                  float sizeFactor,
                                  float rectWidth) {
        //Vertex vertex = isoPoint(flatX, flatY);
        Vertex vertex = new Vertex(flatX, flatY);
        //float x0    = width * .5f;
        //float y0    = height * .2f;
        //float sizeFactor = mSize *.5f;
        float z     = sizeFactor - flatZ + vertex.y * .75f;
        float x     = (vertex.x - sizeFactor) * rectWidth;
        float y     = (mSize - vertex.y) * .005f + 1f;

        float projectedX = x0 + x / y;
        float projectedY = y0 + z / y;

        return new Vertex(projectedX, projectedY);
    }

    //float terrain = new Terrain(9);
    //.generate(0.7);
    //terrain.draw(ctx, width, height);

}
