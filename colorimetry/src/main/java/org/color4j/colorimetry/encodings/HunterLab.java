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

/**
 * This class stores L, a and b value
 *
 * <p>CIELab allows the specification of color perceptions in terms of a three-dimensional space.
 * The L value represents the lightness and extends from 0 (black) to 100 (white).
 * a and b represent redness-greeness and yellowness-blueness respectively.</p>
 */
public class HunterLab extends ColorEncoding
{
    static public HunterLab create( Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        XYZ xyz = ColorCalculator.computeXYZ( ill, refl, obs );
        XYZ whitepoint = ColorCalculator.computeWhitepoint( ill, obs );
        return new HunterLab( ColorCalculator.computeHunterLab( xyz, whitepoint ) );
    }

    static public HunterLab convert( ColorEncoding ce, XYZ whitepoint )
        throws UnsupportedConversionException
    {
        if( ce instanceof XYZ )
        {
            return new HunterLab( (XYZ) ce, whitepoint );
        }
        else
        {
            String message = "Unable to convert from " + ce.getClass().getName() + " to " + HunterLab.class.getName();
            throw new UnsupportedConversionException( message );   //NOI18N
        }
    }

    private HunterLab( XYZ xyz, XYZ whitepoint )
    {

        m_Values = ColorCalculator.computeHunterLab( xyz, whitepoint );
    }

    public HunterLab( Number[] values )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = values[ 0 ].doubleValue();
        m_Values[ 1 ] = values[ 1 ].doubleValue();
        m_Values[ 2 ] = values[ 2 ].doubleValue();
    }

    public HunterLab( Number L, Number a, Number b )
    {
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = L.doubleValue();
        m_Values[ 1 ] = a.doubleValue();
        m_Values[ 2 ] = b.doubleValue();
    }

    public HunterLab( double[] values )
    {

        m_Values = new double[ 3 ];
        m_Values[ 0 ] = values[ 0 ];
        m_Values[ 1 ] = values[ 1 ];
        m_Values[ 2 ] = values[ 2 ];
    }

    public HunterLab( double L, double a, double b )
    {

        m_Values = new double[ 3 ];
        m_Values[ 0 ] = L;
        m_Values[ 1 ] = a;
        m_Values[ 2 ] = b;
    }

    public double getL()
    {
        return m_Values[ 0 ];
    }

    public double geta()
    {
        return m_Values[ 1 ];
    }

    public double getb()
    {
        return m_Values[ 2 ];
    }

    public double getc()
    {
        double d1 = Math.pow( m_Values[ 1 ], 2.0 );
        double d2 = Math.pow( m_Values[ 2 ], 2.0 );
        return Math.sqrt( d1 + d2 );
    }

    public double geth()
    {
        return ColorCalculator.atan( m_Values[ 1 ], m_Values[ 2 ] );
    }
}
