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
import org.color4j.colorimetry.math.Maths;

/**
 * This class stores L, u and v value
 * <p> L value represent lightness. u and v represents chrominancy</p>
 */
public class CIELuv
    implements ColorEncoding
{
    private final double lStar;
    private final double uStar;
    private final double vStar;

    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    protected boolean m_InGamut = true;
    //private double m_Saturation;

    public CIELuv( double l, double u, double v )
    {
        lStar = l;
        uStar = u;
        vStar = v;
    }

    public double getL()
    {
        return lStar;
    }

    public double getu()
    {
        return uStar;
    }

    public double getv()
    {
        return vStar;
    }

    public double getCuv()
    {
        double d1 = Math.pow( uStar, 2.0 );
        double d2 = Math.pow( vStar, 2.0 );
        return Math.sqrt( d1 + d2 );
    }

    public double getHuv()
    {
        return Maths.atan( uStar, vStar );
    }

    public double getSaturation()
    {
        return ( getCuv() / getL() );
        //return m_Saturation;
    }

    public boolean isInGamut()
    {
        return m_InGamut;
    }

    public XYZ toXYZ(XYZ whitepoint)
    {
        double y;
        if( ( getL() / 903.3 ) <= .008856 )
        {
            y = getL() * whitepoint.getY() / 903.3;
        }
        else
        {
            y = whitepoint.getY() * Math.pow( ( getL() + 16.0 ) / 116.0, 3.0 );
        }
        double denum = whitepoint.getX() + 15 * whitepoint.getY() + 3 * whitepoint.getZ();
        double uak = getu() + 13 * getL() * 4 * whitepoint.getX() / denum;
        double vak = getv() + 13 * getL() * 9 * whitepoint.getY() / denum;

        double x = 9 * y * uak / ( 4 * vak );
        double z = ( 4 * 13 * getL() * x - uak * ( x + 15 * y ) ) / ( 3 * uak );

        return new XYZ( x, y, z );
    }
}
