/*
 * Copyright (c) 2000-2011 Niclas Hedhman.
 *
 * Licensed  under the  Apache License, Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.color4j.exports;

/**
 * a marker interface for exclusive text file exporting; the names are stored here;
 * since the names are a key in a map, it'd be best if the names are in a central
 * location
 */
public interface TextFileReflectanceExporter extends ReflectanceExporter
{
    String ACO = "ACO (Photoshop Palette)";
    String XYZ_LAB = "XYZ/CIELab (Delimited XYZ and CIELab values)";
    String SRGB = "sRGB (Delimited sRGB values)";
    String AI = "AI (Illustrator/InDesign Palette)";
    String CXF = "CXF";
    String CMYK = "CMYK (Delimited CMYK values)";
    String QTX_BATCH = "QTX (Standard + Batches)";
    String QTX = "QTX (Standards Only)";
}
