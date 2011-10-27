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
import org.color4j.colorimetry.math.Matrix;

/**
 * Value container for sRGB values.
 * <p>This class holds the RGB values.</p>
 * <p>The sole purpose of this class is to conveniently encapsulate the RGB values
 * into a manageable entity.</p>
 */
public class RGB
    implements ColorEncoding
{
    private final double r;
    private final double g;
    private final double b;

    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    protected boolean m_InGamut = true;

    public RGB( double r, double g, double b )
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * @return Red value
     */
    public double getR()
    {
        return r;
    }

    /**
     * @return Green value
     */
    public double getG()
    {
        return g;
    }

    /**
     * @return Blue value
     */
    public double getB()
    {
        return b;
    }

    public java.awt.Color toAWTColor()
    {
        return new java.awt.Color( (float) r, (float) g, (float) b );
    }

    public boolean isInGamut()
    {
        return m_InGamut;
    }

    public XYZ toXYZ()
    {
        double r = getR();
        double g = getG();
        double b = getB();

        if( r > 0.04045 )
        {
            r = Math.pow( ( r + 0.055 ) / 1.055, 2.4 );
        }
        else
        {
            r /= 12.92;
        }
        if( g > 0.04045 )
        {
            g = Math.pow( ( g + 0.055 ) / 1.055, 2.4 );
        }
        else
        {
            g /= 12.92;
        }
        if( b > 0.04045 )
        {
            b = Math.pow( ( b + 0.055 ) / 1.055, 2.4 );
        }
        else
        {
            b /= 12.92;
        }

        r *= 100;
        g *= 100;
        b *= 100;

        // 	KH - Aug 25, 2004 : should be the inverse of m_RGBTristumulus
        Matrix m = XYZ.getInvertedTristimulusMatrix();

        double x = ( r * m.m00 ) + ( g * m.m01 ) + ( b * m.m02 );
        double y = ( r * m.m10 ) + ( g * m.m11 ) + ( b * m.m12 );
        double z = ( r * m.m20 ) + ( g * m.m21 ) + ( b * m.m22 );

        return new XYZ( x, y, z );
    }
}

