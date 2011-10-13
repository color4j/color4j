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

package org.color4j.colorimetry;

/**
 * The CalcFunc is the util for doing color operations.
 * <p>It help ColorCalculator handles some math computations.</p>
 *
 * @stereotype place
 */

public class CalcFunc2
{
    private CalcFunc2()
    {
    }

    //use in colorConstancy
    public static double[] BFDChromaAdaptN10rgb( double x, double y, double z )
    {
        double[] rxyz = new double[ 3 ];
        rxyz[ 0 ] = 0.8951 * x / y + 0.2664 * y / y - 0.1614 * z / y;
        rxyz[ 1 ] = -0.7502 * x / y + 1.7135 * y / y + 0.0367 * z / y;
        rxyz[ 2 ] = 0.0389 * x / y - 0.0685 * y / y + 1.0296 * z / y;
        return rxyz;
    }

    //use in colorConstancy
    public static double[] BFDChromaAdaptN10xyz( double ry, double gy, double by )
    {
        double[] rxyz = new double[ 3 ];
        rxyz[ 0 ] = 0.9870 * ry - 0.1470 * gy + 0.1600 * by;
        rxyz[ 1 ] = 0.4323 * ry + 0.5184 * gy + 0.0493 * by;
        rxyz[ 2 ] = -0.0085 * ry + 0.0401 * gy + 0.9685 * by;
        return rxyz;
    }

    //use in colorConstancy
    public static double VkriesChromaAdaptFunc( double x, double y, double z )
    {
        double v;
        v = -0.46 * x + 1.36 * y + 0.1 * z;
        return ( v );
    }

    //use in colorConstancy
    public static double[] CieChromaAdaptNayrgb( double x, double y, double z )
    {
        double[] rgb = new double[ 3 ];
        rgb[ 0 ] = 0.40024 * x / y + 0.7076 * y / y - 0.08081 * z / y;
        rgb[ 1 ] = -0.2263 * x / y + 1.16532 * y / y + 0.0457 * z / y;
        rgb[ 2 ] = 0.91822 * z / y;
        return rgb;
    }

    //use in colorConstancy
    public static double[] CieChromaAdaptNargb( double x, double y, double z )
    {
        double[] rgb = new double[ 3 ];
        rgb[ 0 ] = 0.40024 * x + 0.7076 * y - 0.08081 * z;
        rgb[ 1 ] = -0.2263 * x + 1.16532 * y + 0.0457 * z;
        rgb[ 2 ] = 0.91822 * z;
        return rgb;
    }

    //use in colorConstancy
    public static double[] CieChromaAdaptNaxyz( double r, double g, double b )
    {
        double[] xyz = new double[ 3 ];
        xyz[ 0 ] = 1.85995 * r - 1.12939 * g + 0.21990 * b;
        xyz[ 1 ] = 0.36119 * r + 0.63881 * g;
        xyz[ 2 ] = 1.08906 * b;
        return xyz;
    }

    //use in colorConstancy
    public static double CieChromaAdaptFunc1( double var, double reflb, double rlux )
    {
        double varo, rv;
        varo = var * reflb * rlux / 3.14159;
        rv = ( 6.469 + 6.362 * Math.pow( varo, 0.4495 ) ) / ( 6.469 + Math.pow( varo, 0.4495 ) );
        return ( rv );
    }

    //use in colorConstancy
    public static double CieChromaAdaptFunc2( double var, double reflb, double rlux )
    {
        double varo, rv;

        varo = var * reflb * rlux / 3.14159;
        rv = 0.7844 * ( 8.414 + 8.091 * Math.pow( varo, 0.5128 ) ) / ( 8.414 + Math.pow( varo, 0.5128 ) );
        return ( rv );
    }

    /**
     * start_r>0 means start nm of reflectance is smaller than start nm of weight
     * like r_nm=340, w_nm=360
     * end_r>0 means ending nm of reflectance is bigger than end nm of weight
     * like r_nm=790, w_nm=780
     * this function is to make the reflectance start & ending same as weights
     */
    public static double[] getSameIntervalR( double[] r, int start_r, int end_r )
    {
        if( ( start_r == 0 ) && ( end_r == 0 ) )
        {
            return r;
        }
        double[] rt_value = new double[ r.length - start_r - end_r ];

        for( int i = 0; i < rt_value.length; i++ )
        {
            rt_value[ i ] = r[ i + start_r ];
        }
        return rt_value;
    }

    /**
     * this function make the weight start & ending same as reflectance
     */
    public static double[] getSameIntervalW( double[] w, int start_w, int end_w )
    {
        if( ( end_w == 0 ) && ( start_w == 0 ) )
        {
            return w;
        }
        int leng = w.length - start_w - end_w;

        double[] rt_value = new double[ leng ];
        rt_value[ 0 ] = 0.0;
        for( int i = 0; i <= start_w; i++ )
        {
            rt_value[ 0 ] += w[ i ];
        }
        rt_value[ leng - 1 ] = 0.0;
        for( int i = 0; i <= end_w; i++ )
        {
            rt_value[ leng - 1 ] += w[ start_w + leng - 1 + i ];
        }
        for( int i = 1; i < leng - 1; i++ )
        {
            rt_value[ i ] = w[ start_w + i ];
        }
        return rt_value;
    }

    //the Judd polynomial
    public static double calculateJuddAlgorithm( double v, double Y )
    {
        double rt = 1.2219 * v;
        rt = rt + ( -0.23111 ) * Math.pow( v, 2.0 );
        rt = rt + ( 0.23951 ) * Math.pow( v, 3.0 );
        rt = rt + ( -0.021009 ) * Math.pow( v, 4.0 );
        rt = rt + ( 0.0008404 ) * Math.pow( v, 5.0 );

        return ( Y - rt );
    }

    /**
     * bisector moethod for root finding,left right decided the interval,
     * xacc is the stop condition when interval range < xacc
     * Y is the trimulus value Y
     */
    public static double rtbis( double left, double right, double xacc, double y )
    {
        double dx = 0.0;
        double f = 0.0;
        double fmid = 0.0;
        double xmid = 0.0;
        double rtb = 0.0;
        double x1 = left;
        double x2 = right;
        // @TODO: Clean up. ML - 05/08/2003
//        double Y = y;
        int JMAX = 100;
        f = calculateJuddAlgorithm( x1, y );
        fmid = calculateJuddAlgorithm( x2, y );
        if( ( f * fmid ) >= 0.0 )
        {
            return Math.pow( y, 0.5 );
        }
        if( f < 0.0 )
        {
            dx = x2 - x1;
            rtb = x1;
        }
        else
        {
            dx = x1 - x2;
            rtb = x2;
        }
        for( int i = 1; i <= JMAX; i++ )
        {
            dx = dx * 0.5;
            xmid = rtb + dx;
            fmid = calculateJuddAlgorithm( xmid, y );
            if( fmid <= 0.0 )
            {
                rtb = xmid;
            }
            if( ( Math.abs( dx ) < xacc ) || fmid == 0.0 )
            {
                return rtb;
            }
        }
        return Math.pow( y, 0.5 );
    }

    /**
     * get geometry 3d distance
     */
    static double get3Distance( double[] x1, double[] x2 )
    {
        double d = 0.0;
        for( int i = 0; i < 3; i++ )
        {
            d = d + Math.pow( x1[ i ] - x2[ i ], 2 );
        }
        return Math.pow( d, 0.5 );
    }
}


