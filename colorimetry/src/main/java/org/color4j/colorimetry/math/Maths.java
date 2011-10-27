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

package org.color4j.colorimetry.math;

public class Maths
{
    public static double computeDifferenceHue( double sth, double stc, double bth, double btc )
    {

        double dh = bth - sth;
        if( dh < -180.0 )
        {
            dh = dh + 360.0;
        }
        if( dh > 180.0 )
        {
            dh = dh - 360.0;
        }

        return Math.sqrt( btc * stc ) * 2 * Math.sin( ( dh / 2.0 ) * Math.PI / 180.0 );
    }

    /**
     * Math function arctan if tan45=1 then atan1=45
     */
    static public double atan( double a, double b )
    {
        if( a == 0.0 )
        {
            if( b > 0.0 )
            {
                return 90.0;
            }
            else
            {
                return 270.0;
            }
        }

        if( b == 0.0 )
        {
            if( a >= 0 )
            {
                return 0.0;
            }
            else
            {
                return 180.0;
            }
        }

        double h = Math.atan( b / a ) * 180.0 / Math.PI;
        if( a < 0.0 )
        {
            h = h + 180.0;
        }
        else if( b < 0.0 )
        {
            h = h + 360.0;
        }
        return h;
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
        System.arraycopy( r, start_r, rt_value, 0, rt_value.length );
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
        System.arraycopy( w, start_w + 1, rt_value, 1, leng - 1 - 1 );
        return rt_value;
    }
}
