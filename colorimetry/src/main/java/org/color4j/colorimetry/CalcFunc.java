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

package org.color4j.colorimetry;

import org.color4j.colorimetry.entities.Reflectance;

/**
 * The CalcFunc is the util for doing color operations.
 * <p>It help ColorCalculator handles some math computations.</p>
 *
 * @stereotype place
 */
public class CalcFunc
{
    private CalcFunc()
    {
    }

    public static double convertToKS( double rf )
    {
        if( rf <= 0 )
        {
            rf = 0.0001;
        }
        double d = 1.0 - rf;
        d = ( d * d ) / ( 2 * rf );
        return d;
    }

    public static double[] convertToKS( double[] rf )
    {
        double[] ret = new double[ rf.length ];
        for( int i = 0; i < rf.length; i++ )
        {
            ret[ i ] = convertToKS( rf[ i ] );
        }

        return ret;
    }

    //how many point within the [400-700] interval
    public static int getNoNm( int interval )
    {
        return 300 / interval + 1;
    }

    public static double[] getPredictReflectance( Reflectance refl, int interval )
    {
        int no_nm = getNoNm( interval );
        double[] ret = new double[ no_nm ];
        Spectrum spec = refl.getSpectrum();
        double[] ref = spec.getValues();

        int m_interval = spec.getInterval();
        int start = spec.getShortestWavelength();
        int offset2 = 0;
        if( start > 400 )
        {
            offset2 = ( start - 400 ) / interval;
            for( int i = 0; i < offset2; i++ )
            {
                ret[ i ] = 0.0;
            }
        }
        int step = interval / m_interval;
        int offset = ( 400 - start ) / m_interval;

        for( int i = offset2; i < no_nm; i++ )
        {
            ret[ i ] = ref[ offset + i * step ];
        }

        return ret;
    }

    public static double[] getPredictReflectanceF( Reflectance refl, int interval )
    {
        double[] refl_d = getPredictReflectance( refl, interval );
        double[] ret = new double[ refl_d.length ];
        for( int i = 0; i < ret.length; i++ )
        {
            ret[ i ] = refl_d[ i ];
        }
        return ret;
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
}