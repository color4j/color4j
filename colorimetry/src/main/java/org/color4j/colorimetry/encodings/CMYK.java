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
import java.awt.Color;

/**
 */
public class CMYK
    implements ColorEncoding
{
    private final double c;
    private final double m;
    private final double y;
    private final double k;
    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    protected boolean m_InGamut = true;

    static public CMYK convert( ColorEncoding ce, XYZ whitepoint )
        throws UnsupportedConversionException
    {
        if( ce instanceof XYZ )
        {
            XYZ xyz = (XYZ) ce;
            return xyz.toCMYK( whitepoint );
        }
        String message = "Unable to convert from " + ce.getClass().getName() + " to " + CMYK.class.getName();
        throw new UnsupportedConversionException( message ); //NOI18N
    }

    /**
     * Class constructor
     */
    public CMYK( double c, double m, double y, double k )
    {
        this.c = c;
        this.m = m;
        this.y = y;
        this.k = k;
    }

    public double getC()
    {
        return c;
    }

    public double getM()
    {
        return m;
    }

    public double getY()
    {
        return y;
    }

    public double getK()
    {
        return k;
    }

    public Color toAWTColor()
    {
        double[] a = toRGB();
        return new Color( (float) a[ 0 ], (float) a[ 1 ], (float) a[ 2 ] );
    }

    public double[] toRGB()
    {
        double c = ( getC() * ( 1 - getK() ) + getK() );
        double m = ( getM() * ( 1 - getK() ) + getK() );
        double y = ( getY() * ( 1 - getK() ) + getK() );
        return new double[]{ 1.0 - c, 1.0 - m, 1.0 - y };
    }

    public boolean isInGamut()
    {
        return m_InGamut;
    }
}