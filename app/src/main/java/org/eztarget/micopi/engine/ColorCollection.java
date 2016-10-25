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
            }
    };

    static int getColor(int paletteId, int colorIndex) {
//        final int index = c % (PALETTES.length - 1);
        final int[] palette = PALETTES[paletteId % PALETTES.length];
        return palette[colorIndex % (palette.length - 1)];
    }

}
