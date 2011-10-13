/*
 * Copyright (c) 2011 Niclas Hedhman.
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

import org.color4j.colorimetry.ColorCalculator;
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import java.awt.Color;

/**
 */
public class CMYK extends ColorEncoding
{
    static public CMYK create( Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        XYZ xyz = ColorCalculator.computeXYZ( ill, refl, obs );
        XYZ whitepoint = ColorCalculator.computeWhitepoint( ill, obs );

        return xyz.toCMYK( whitepoint );
    }

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
     * expects an array.length == 4
     */
    public CMYK( Number[] cmyk )
        throws ColorException
    {
        if( cmyk.length != 4 )
        {
            throw new ColorException( "unexpected array size; expected length == 4, found " + cmyk.length );
        }
        m_Values = new double[ 4 ];
        m_Values[ 0 ] = cmyk[ 0 ].doubleValue();
        m_Values[ 1 ] = cmyk[ 1 ].doubleValue();
        m_Values[ 2 ] = cmyk[ 2 ].doubleValue();
        m_Values[ 3 ] = cmyk[ 3 ].doubleValue();
    }

    /**
     * Class constructor
     */
    public CMYK( Number c, Number m, Number y, Number k )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = c.doubleValue();
        m_Values[ 1 ] = m.doubleValue();
        m_Values[ 2 ] = y.doubleValue();
        m_Values[ 3 ] = k.doubleValue();
    }

    /**
     * Class constructor
     */
    public CMYK( double c, double m, double y, double k )
    {
        m_Values = new double[ 4 ];
        m_Values[ 0 ] = c;
        m_Values[ 1 ] = m;
        m_Values[ 2 ] = y;
        m_Values[ 3 ] = k;
    }

    public CMYK( double[] cmyk )
    {
        super();

        m_Values = new double[ 4 ];
        m_Values[ 0 ] = cmyk[ 0 ];
        m_Values[ 1 ] = cmyk[ 1 ];
        m_Values[ 2 ] = cmyk[ 2 ];
        m_Values[ 3 ] = cmyk[ 3 ];
    }

    public CMYK( float[] cmyk )
    {
        super();

        m_Values = new double[ 4 ];
        m_Values[ 0 ] = cmyk[ 0 ];
        m_Values[ 1 ] = cmyk[ 1 ];
        m_Values[ 2 ] = cmyk[ 2 ];
        m_Values[ 3 ] = cmyk[ 3 ];
    }

    public double getC()
    {
        return m_Values[ 0 ];
    }

    public double getM()
    {
        return m_Values[ 1 ];
    }

    public double getY()
    {
        return m_Values[ 2 ];
    }

    public double getK()
    {
        return m_Values[ 3 ];
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

}