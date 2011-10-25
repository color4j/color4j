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

import org.color4j.colorimetry.ColorCalculator;
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;

/**
 * Value container for sRGB values.
 * <p>This class holds the RGB values.</p>
 * <p>The sole purpose of this class is to conveniently encapsulate the RGB values
 * into a manageable entity.</p>
 */
public class RGB extends ColorEncoding
{
    /**
     * Class constructor
     */
    static public RGB create( Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        XYZ xyz = ColorCalculator.computeXYZ( ill, refl, obs );
        XYZ whitepoint = ColorCalculator.computeWhitepoint( ill, obs );
        Illuminant illD65 = IlluminantImpl.create( "D65" );     //NOI18N
        XYZ wpD65 = ColorCalculator.computeWhitepoint( illD65, obs );

        return new RGB( xyz.toRGB( whitepoint, wpD65 ) );
    }

    static public RGB convert( ColorEncoding ce, XYZ whitepoint )
        throws UnsupportedConversionException
    {
        if( ce instanceof XYZ )
        {
            XYZ xyz = (XYZ) ce;
            return xyz.toRGB( whitepoint );
        }
        else
        {
            String message = "Unable to convert from " + ce.getClass().getName() + " to " + RGB.class.getName();
            throw new UnsupportedConversionException( message );     //NOI18N
        }
    }

    public RGB( Number[] rgb )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = rgb[ 0 ].doubleValue();
        m_Values[ 1 ] = rgb[ 1 ].doubleValue();
        m_Values[ 2 ] = rgb[ 2 ].doubleValue();
    }

    public RGB( Number r, Number g, Number b )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = r.doubleValue();
        m_Values[ 1 ] = g.doubleValue();
        m_Values[ 2 ] = b.doubleValue();
    }

    public RGB( double r, double g, double b )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = r;
        m_Values[ 1 ] = g;
        m_Values[ 2 ] = b;
    }

    public RGB( double[] rgb )
    {
        super();
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = rgb[ 0 ];
        m_Values[ 1 ] = rgb[ 1 ];
        m_Values[ 2 ] = rgb[ 2 ];
    }

    /**
     * @return Red value
     */
    public double getR()
    {
        return m_Values[ 0 ];
    }

    /**
     * @return Green value
     */
    public double getG()
    {
        return m_Values[ 1 ];
    }

    /**
     * @return Blue value
     */
    public double getB()
    {
        return m_Values[ 2 ];
    }

    public java.awt.Color toAWTColor()
    {
        return new java.awt.Color( (float) m_Values[ 0 ], (float) m_Values[ 1 ], (float) m_Values[ 2 ] );
    }
}
