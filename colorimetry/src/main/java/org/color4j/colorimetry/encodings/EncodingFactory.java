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

package org.color4j.colorimetry.encodings;

import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Reflectance;

public interface EncodingFactory
{
    <T extends ColorEncoding> T create( Class<T> encodingType, Illuminant illuminant, Reflectance reflectance, Observer observer );

    CIELab createCIELab( Illuminant illuminant, Reflectance reflectance, Observer observer );

    CIELuv createCIELuv( Illuminant illuminant, Reflectance reflectance, Observer observer );

    CMYK createCMYK( Illuminant illuminant, Reflectance reflectance, Observer observer );

    Din99Lab createDin99Lab( Illuminant illuminant, Reflectance reflectance, Observer observer, double ke, double kch );

    HunterLab createHunterLab( Illuminant illuminant, Reflectance reflectance, Observer observer );

    RGB createRGB( Illuminant illuminant, Reflectance reflectance, Observer observer );

    XYZ createXYZ( Illuminant illuminant, Reflectance reflectance, Observer observer );

    /**
     * whitepoint is the sum of weights factor for XYZ, it is a combination of
     * illuminant & observer. It is use to convert from XYZ to other color space
     * like CIELab, CIELuv..
     * @param illuminant
     * @param observer
     * @return The XYZ of the whitepoint.
     */
    XYZ createWhitePoint( Illuminant illuminant, Observer observer );

    Chromacity createChromacity( XYZ xyz );
}
