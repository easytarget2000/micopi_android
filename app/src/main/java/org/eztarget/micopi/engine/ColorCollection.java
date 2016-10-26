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

/**
 * Created by michel on 27/10/14.
 * <p/>
 * Contains the color definitions and getter-generators that will be used by ImageFactory
 */
class ColorCollection {

    /**
     * Based on colour palette from google.com/design/spec/style/color.html
     * Excuse the colour names.
     * These are just used to make sure that colours "next to each other" are not too similar.
     */
    private static final int PALETTES[][] = {
            {
                    0xFF211C00,
                    0xFF574515,
                    0xFF6F5920,
                    0xFF8E8536,
                    0xFF8C8B48
            },
            {
                    0xFFC7CEB2,
                    0xFFFED5C1,
                    0xFFAA7155,
                    0xFF8D5138,
                    0xFF291615
            },
            {
                    0xFFBF2A23,
                    0xFFA6AD3C,
                    0xFFF0CE4E,
                    0xFFCF872E,
                    0xFF8A211D
            },
            {
                    0xFFCCCCCC,
                    0xFFDBCAD5,
                    0xFFE3D8E0,
                    0xFFE5E5E5,
                    0xFFFAFAFA
            },
            {
                    0xFFF4F4F4,
                    0xFF9BA657,
                    0xFFA68C78,
                    0xFF594433
            },
            {
                    0xFFFFFFFF,
                    0xFF912891,
                    0xFF858585,
                    0xFF232323,
                    0xFF7F2A83
            },
            {
                    0xFF0F2A25,
                    0xFF274640,
                    0xFF133942,
                    0xFF237085,
                    0xFF9DDADE
            },
            {
                    0xFF70803C,
                    0xFF4A3E3C,
                    0xFF664037,
                    0xFF88736F,
            },
            {
                    0xFFECD078,
                    0xFFD95B43,
                    0xFFC02942,
                    0xFF542437,
                    0xFF53777B
            },
            {
                    0xFF490A3D,
                    0xFFBD1550,
                    0xFFE97F02,
                    0xFFF8CA00,
                    0xFF8A9B0F
            },
            {
                    0xFFFFFFFF,
                    0xFFCBE86B,
                    0xFFF2E9E1,
                    0xFF1C140D,
                    0xFFCBE86B
            },
            {
                    0xFFFFFFFF,
                    0xFF111111,
                    0xFF999999,
                    0xFFFFD700
            },
            {
                    0xFF413E4A,
                    0xFF73626E,
                    0xFFB38184,
                    0xFFF0B49E,
                    0xFFF7E4BE
            },
            {
                    0xFF343838,
                    0xFF005F6B,
                    0xFF008C9E,
                    0xFF99B4CC,
                    0xFF00DFFC
            },
            {
                    0xFFFAD089,
                    0xFFFF9C5B,
                    0xFFF5634A,
                    0xFFED303C,
                    0xFF3B8183
            },
            {
                    0xFFFF4242,
                    0xFFF4FAD2,
                    0xFFD4EE5E,
                    0xFFE1EDB9,
                    0xFFF0F2EB
            },
            {
                    0xFFD1E751,
                    0xFFFFFFFF,
                    0xFF000000,
                    0xFF4DBCE9,
                    0xFF26ADE4
            },
            {
                    0xFF3E4147,
                    0xFFFFFEDF,
                    0xFFDFBA69,
                    0xFF5A2E2E,
                    0xFF2A2C31
            },
            {
                    0xFF1C0113,
                    0xFF6B0103,
                    0xFFA30006,
                    0xFFC21A01,
                    0xFFF03C02
            },
            {
                    0xFFEDF6EE,
                    0xFFD1C089,
                    0xFFB3204D,
                    0xFF412E28,
                    0xFF151101
            },
            {
                    0xFFF1F0CD,
                    0xFFB6C59B,
                    0xFFC59948,
                    0xFFC82D5F,
                    0xFF22124A
            },
            {
                    0xFFFC580C,
                    0xFFFC6B0A,
                    0xFFF8872E,
                    0xFFFFA927,
                    0xFFFDCA49
            }
    };

    static int getColor(int paletteId, int colorIndex) {
        final int[] palette = PALETTES[paletteId % PALETTES.length];
        return palette[colorIndex % (palette.length - 1)];
    }

}
