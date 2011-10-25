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

/**
 * This class stores L, u and v value
 * <p> L value represent lightness. u and v represents chrominancy</p>
 */
public class CIELuv extends ColorEncoding
{
    //private double m_Saturation;

    static public CIELuv create( Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        XYZ xyz = ColorCalculator.computeXYZ( ill, refl, obs );
        XYZ whitepoint = ColorCalculator.computeWhitepoint( ill, obs );
        return new CIELuv( xyz.toLuv( whitepoint ) );
    }

    static public CIELuv convert( ColorEncoding ce, XYZ whitepoint )
        throws UnsupportedConversionException
    {
        if( ce instanceof XYZ )
        {
            return new CIELuv( (XYZ) ce, whitepoint );
        }
        else
        {
            throw new UnsupportedConversionException( "Unable to convert from " + ce.getClass()
                .getName() + " to " + CIELuv.class.getName() );  //NOI18N
        }
    }

    private CIELuv( XYZ xyz, XYZ whitepoint )
    {

        m_Values = xyz.toLuv( whitepoint );
    }

    public CIELuv( Number[] values )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = values[ 0 ].doubleValue();
        m_Values[ 1 ] = values[ 1 ].doubleValue();
        m_Values[ 2 ] = values[ 2 ].doubleValue();
        /*if( values.length >= 4 )
            m_Saturation = values[3].doubleValue();
        else
            m_Saturation = Double.NaN;
        */
    }

    public CIELuv( Number l, Number u, Number v )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = l.doubleValue();
        m_Values[ 1 ] = u.doubleValue();
        m_Values[ 2 ] = v.doubleValue();
    }

    public CIELuv( double[] values )
    {
        this( values[ 0 ], values[ 1 ], values[ 2 ] );
    }

    public CIELuv( double l, double u, double v )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = l;
        m_Values[ 1 ] = u;
        m_Values[ 2 ] = v;
    }

    public CIELuv( double l, double u, double v, double s )
    {
        m_Values = new double[ 4 ];
        m_Values[ 0 ] = l;
        m_Values[ 1 ] = u;
        m_Values[ 2 ] = v;
        m_Values[ 3 ] = s;
    }

    public double getL()
    {
        return m_Values[ 0 ];
    }

    public double getu()
    {
        return m_Values[ 1 ];
    }

    public double getv()
    {
        return m_Values[ 2 ];
    }

    public double getCuv()
    {
        double d1 = Math.pow( m_Values[ 1 ], 2.0 );
        double d2 = Math.pow( m_Values[ 2 ], 2.0 );
        return Math.sqrt( d1 + d2 );
    }

    public double getHuv()
    {
        return ColorCalculator.atan( m_Values[ 1 ], m_Values[ 2 ] );
    }

    public double getSaturation()
    {
        return ( getCuv() / getL() );
        //return m_Saturation;
    }
}
