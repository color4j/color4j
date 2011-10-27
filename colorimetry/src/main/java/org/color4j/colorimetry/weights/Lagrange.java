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

package org.color4j.colorimetry.weights;

import org.color4j.colorimetry.Interpolation;

public class Lagrange
    implements Interpolation
{
//	@TODO: Clean up. ML - 05/08/2003
//    private int step;
//    private int start;

    static public final double[] QUADRATIC_L0 =
        {
            0.92625f, 0.85500f, 0.78625f, 0.72000f, 0.65625f,
            0.59500f, 0.53625f, 0.48000f, 0.42675f, 0.37500f,
            0.32625f, 0.28000f, 0.23625f, 0.19500f, 0.15625f,
            0.12000f, 0.08625f, 0.05500f, 0.02625f, 0.0f
        };

    static public final double[] QUADRATIC_L1 =
        {
            0.0975f, 0.1900f, 0.2775f, 0.3600f, 0.4375f,
            0.5100f, 0.5775f, 0.6400f, 0.6975f, 0.7500f,
            0.7975f, 0.8400f, 0.8775f, 0.9100f, 0.9375f,
            0.9600f, 0.9775f, 0.9900f, 0.9975f, 1.0f
        };

    static public final double[] QUADRATIC_L2 =
        {
            -0.02375f, -0.04500f, -0.06375f, -0.08000f, -0.09375f,
            -0.10500f, -0.11375f, -0.12000f, -0.12375f, -0.12500f,
            -0.12375f, -0.12000f, -0.11375f, -0.10500f, -0.09375f,
            -0.08000f, -0.06375f, -0.04500f, -0.02375f, 0.0f
        };

    static public final double[] CUBIC_L0 =
        {
            -0.0154375f, -0.0285000f, -0.0393125f, -0.0480000f, -0.0546875f,
            -0.0595000f, -0.0625625f, -0.0640000f, -0.0639375f, -0.0625000f,
            -0.0598125f, -0.0560000f, -0.0511875f, -0.0455000f, -0.0390625f,
            -0.0320000f, -0.0244375f, -0.0165000f, -0.0083125f, 0.0f
        };

    static public final double[] CUBIC_L1 =
        {
            0.9725625f, 0.9405000f, 0.9041875f, 0.8640000f, 0.8203125f,
            0.7735000f, 0.7239375f, 0.6720000f, 0.6180625f, 0.5625000f,
            0.5056875f, 0.4480000f, 0.3898125f, 0.3315000f, 0.2734375f,
            0.2160000f, 0.1595625f, 0.1045000f, 0.0511875f, 0.0f
        };

    static public final double[] CUBIC_L2 =
        {
            0.0511875f, 0.1045000f, 0.1595625f, 0.2160000f, 0.2734375f,
            0.3315000f, 0.3898125f, 0.4480000f, 0.5056875f, 0.5625000f,
            0.6180625f, 0.6720000f, 0.7239375f, 0.7735000f, 0.8203125f,
            0.8640000f, 0.9041875f, 0.9405000f, 0.9725625f, 1.0f
        };

    static public final double[] CUBIC_L3 =
        {
            -0.0083125f, -0.0165000f, -0.0244375f, -0.0320000f, -0.0390625f,
            -0.0455000f, -0.0511875f, -0.0560000f, -0.0598125f, -0.0625000f,
            -0.0639375f, -0.0640000f, -0.0625625f, -0.0595000f, -0.0546875f,
            -0.0480000f, -0.0393125f, -0.0285000f, -0.0154375f, 0.0f
        };

    /**
     * Takes a double array of data, spaced at interval nm
     * and fills an output with the interpolated 'missing' data.
     */
    public double[] compute( double[] data, int interval )
    {
        if( interval == 1 )
        {
            return data;
        }

        double[] out = new double[ ( data.length - 1 ) * interval ];
        int outpos = 0;

        double[] first = quadratic( interval, data[ 0 ], data[ 1 ], data[ 2 ] );
        System.arraycopy( first, 0, out, outpos, first.length );
        outpos = outpos + first.length;

        for( int inpos = 1; inpos < data.length - 2; inpos++ )
        {
            double[] mid = cubic( interval, data[ inpos - 1 ], data[ inpos ], data[ inpos + 1 ], data[ inpos + 2 ] );
            System.arraycopy( mid, 0, out, outpos, mid.length );
            outpos = outpos + mid.length;
        }
        double[] last = quadratic( interval, data[ data.length - 1 ], data[ data.length - 2 ], data[ data.length - 3 ] );
        System.arraycopy( last, 0, out, outpos, last.length );
        return out;
    }

    /**
     * Compute the cubic Lagrange coefficient for a given missing
     * interval based on 4 points.
     * <code>Interval</code> is the number of nanometers between
     * each of m0, m1, m2 and m3.
     */
    private double[] cubic( int interval, double m0, double m1, double m2, double m3 )
    {
        int step = 20 / interval;
        double[] result = new double[ interval ];
        int index = -1;
        for( int i = 0; i < interval; i++ )
        {
            index = index + step;
            result[ i ] = CUBIC_L0[ i ] * m0 +
                          CUBIC_L1[ i ] * m1 +
                          CUBIC_L2[ i ] * m2 +
                          CUBIC_L3[ i ] * m3;
        }
        return result;
    }

    /**
     * Compute the quadratic Lagrange coefficient for a given missing
     * interval based on 3 points.
     * <code>Interval</code> is the number of nanometers between
     * each of m0, m1, and m2.
     */
    private double[] quadratic( int interval, double m0, double m1, double m2 )
    {
        int step = 20 / interval;
        double[] result = new double[ interval ];
        int index = -1;
        for( int i = 0; i < interval; i++ )
        {
            index = index + step;
            result[ i ] = QUADRATIC_L0[ i ] * m0 +
                          QUADRATIC_L1[ i ] * m1 +
                          QUADRATIC_L2[ i ] * m2;
        }
        return result;
    }
}
