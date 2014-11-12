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
import android.graphics.Paint;

import com.easytarget.micopi.Contact;

/**
 * Created by michel on 12/11/14.
 *
 */
public class StripeCandyGenerator {

    public static void generate(final Canvas fCanvas, final Contact fContact) {

        final float fImageSize = fCanvas.getWidth();

        final int fRows = 8;
        final int fColumns = fRows * fRows;
        final float fRowHeight = fImageSize / fRows;
        final float fColumnWidth = fImageSize / fColumns;

        final String fMd5String = fContact.getMD5EncryptedString();
        final int fMd5Length = fMd5String.length();
        int md5Pos = 0;

        fCanvas.rotate(45f);

        Paint fPaint = new Paint();
        fPaint.setStyle(Paint.Style.FILL);
        fPaint.setAntiAlias(true);

        for (int y = 0; y < fRows; y++) {
            for (int x = 0; x < fColumns; x++) {
                md5Pos++;
                if (md5Pos >= fMd5Length) md5Pos = 0;

                final float left = x * fColumnWidth;
                final float top = y * fRowHeight;

                fPaint.setColor(ColorCollection.getCandyColorForChar(fMd5String.charAt(md5Pos)));
                fCanvas.drawRect(left, top, left + fColumnWidth, top + fRowHeight, fPaint);
            }
        }

        fCanvas.rotate(-45f);
    }
}
