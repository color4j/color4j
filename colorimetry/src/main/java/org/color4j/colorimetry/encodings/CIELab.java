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
 * This class stores L, a and b value
 *
 * <p>CIELab allows the sepcification of color perceptions in terms of a three-dimensional space.
 * The L value represents the lightness and extends from 0 (black) to 100 (white).
 * a and b represent redness-greeness and yellowness-blueness respectively.</p>
 */
public class CIELab
    implements ColorEncoding
{
    private final double lStar;
    private final double aStar;
    private final double bStar;
    private double cStar;
    private double hStar;

    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    private final boolean m_InGamut = true;

    public CIELab( double L, double a, double b )
    {
        lStar = L;
        aStar = a;
        bStar = b;
        cStar = Math.sqrt( Math.pow( a, 2.0 ) + Math.pow( b, 2.0 ) );
        hStar = Maths.atan( a, b );
    }

    public double getL()
    {
        return lStar;
    }

    public double geta()
    {
        return aStar;
    }

    public double getb()
    {
        return bStar;
    }

    public double getc()
    {
        return cStar;
    }

    public double geth()
    {
        return hStar;
    }

    public double getDE( CIELab batch )
    {
        double k;
        double dL = batch.getL() - getL();
        double da = batch.geta() - geta();
        double db = batch.getb() - getb();
        double dc = batch.getc() - getc();
        double dh = batch.geth() - geth();

        if( dh > 0.0 )
        {
            k = 1.0;
        }
        else
        {
            k = -1.0;
        }
        dh = k * ( Math.pow( da, 2.0 ) + Math.pow( db, 2.0 ) - Math.pow( dc, 2.0 ) );
        return Math.sqrt( Math.pow( dL, 2.0 ) + Math.pow( dc, 2.0 ) + Math.pow( dh, 2.0 ) );
    }


    public boolean isInGamut()
    {
        return m_InGamut;
    }

    public XYZ toXYZ( XYZ whitepoint )
    {
        double fn1 = lStar / 116.0;
        double fn0 = fn1 + aStar / 500.0;
        double fn2 = fn1 - bStar / 200.0;
        double x = morphToXyz( whitepoint.getX(), fn0 );
        double y = morphToXyz( whitepoint.getY(), fn1 );
        double z = morphToXyz( whitepoint.getZ(), fn2 );
        return new XYZ( x, y, z );
    }

    private double morphToXyz( double whitepoint, double function )
    {
        double luminance = lStar / 116.0;
        double result;
        if( luminance <= 0.068961672 )
        {
            result = whitepoint * function / 7.787;
        }
        else
        {
            result = whitepoint * Math.pow( function + 16.0 / 116.0, 3.0 );
        }
        return result;
    }

    public Din99Lab toDin99Lab(double Ke, double Kch)
    {
        double dp = Math.PI / 180.0;
        double e = geta() * Math.cos( 16 * dp ) + getb() * Math.sin( 16 * dp );
        double f = 0.7 * ( -geta() * Math.sin( 16 * dp ) + getb() * Math.cos( 16 * dp ) );

        double g = Math.sqrt( Math.pow( e, 2 ) + Math.pow( f, 2 ) );
        double hef = Maths.atan( f, e ) * dp;
        double c99 = Math.log( 1 + 0.045 * g ) / ( 0.045 * Ke * Kch );
        double lStar = ( 105.51 / Ke ) * Math.log( 1 + 0.0158 * getL() );
        double aStar = c99 * Math.cos( hef );
        double bStar = c99 * Math.sin( hef );
        return new Din99Lab( lStar, aStar, bStar, c99, hef );
    }
}