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

package org.color4j.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.ReflectanceImpl;
import org.color4j.colorimetry.Spectrum;

/**
 * The CalcFunc is the util for doing color operations.
 * <p>It help ColorCalculator handles some math computations.</p>
 *
 */
public class CalcFunc2
{
    private CalcFunc2()
    {
    }


    //return a Spectrum that contains KS value of a reflectance
    public static Spectrum getKS(Reflectance reflectance)
    {
        Spectrum s = reflectance.getSpectrum();
        double[] values = s.getValues();
//        int size = values.length;
        double[] data = convertToKS( values );
        return Spectrum.create( s.getShortestWavelength(), s.getInterval(), data );
    }

    private static double[] convertToKS(Reflectance reflectance)
    {
        SortedMap s = reflectance.getSpectrumMap();
        double[] data = new double[ s.size() ];
        Iterator itr = s.values().iterator();
        for( int i = 0; itr.hasNext(); i++ )
        {
            data[ i ] = convertToKS( ( (Number) itr.next() ).doubleValue() );
        }
        return data;
    }

    private static double[] convertToKS( double[] rf )
    {
        double[] ret = new double[ rf.length ];
        for( int i = 0; i < rf.length; i++ )
        {
            ret[ i ] = convertToKS( rf[ i ] );
        }
        return ret;
    }

    private static double convertToKS( double rf )
    {
        if( rf <= 0 )
        {
            rf = 0.0001;
        }
        double d = 1.0 - rf;
        d = ( d * d ) / ( 2 * rf );
        return d;
    }



    //put in the load factor map which getLoadFactorMap
    public static Collection returnBadLoadFactorMapIndex( Map loadFactorMap )
    {
//        Double[] conc = (Double[])loadFactorMap.keySet().toArray(new Double[0]);
        Double[] ks = (Double[]) loadFactorMap.values().toArray( new Double[ 0 ] );
        return checkIncreasing( ks );
    }

    //put in the dyeload map
    public static Map returnBadDyeLoadMap( Map dyeloadMap )
        throws ColorException
    {
        Map ret = new HashMap();
        Map dlmap = getSameMap( dyeloadMap );
        Double[] conc = (Double[]) dyeloadMap.keySet().toArray( new Double[ 0 ] );
        int[] index = new int[ dlmap.size() ];
        for( int i = 0; i < 16; i++ )
        {
            Map map = getLoadFactorMap( dlmap, new Integer( i ) );
            Collection c = returnBadLoadFactorMapIndex( map );

            if( c.size() > 0 )
            {
                for( int j = 0; j < c.size(); j++ )
                {
                    int k = ( (Integer) c.toArray()[ j ] ).intValue();
                    index[ k ]++;
                }
            }
        }

        for( int i = 0; i < dlmap.size(); i++ )
        {
            if( index[ i ] > 0 )
            {
                ret.put( dyeloadMap.get( conc[ i ] ), Boolean.FALSE );
            }
            else
            {
                ret.put( dyeloadMap.get( conc[ i ] ), Boolean.TRUE );
            }
        }

        return ret;
    }

    //get the wavelength that max k/s value in
    public static int getIndexNmOfMaxKS( Reflectance refl )
    {
        double[] values = convertToKS( refl );
        int k = 0;
        double value = values[ 0 ];
        for( int i = 1; i < values.length; i++ )
        {
            if( value < values[ i ] )
            {
                k = i;
                value = values[ i ];
            }
        }
        return k;
    }

    //get the ks vs conc. map from a dyeload
    public static Map getLoadFactorMap( Map map, Integer Index )
        throws ColorException
    {
        if( map == null || map.size() == 0 )
        {
            throw new ColorException( "Load Factor Map is null or empty" );
        }

        Map smap = getSameMap( map );
        int index;

        Double[] ds = (Double[]) smap.keySet().toArray( new Double[ 0 ] );
        Reflectance refl = (Reflectance) smap.get( ds[ 0 ] );
        if( refl == null )
        {
            throw new ColorException( "Reflctance in Load Factor Map is null" );
        }

        if( Index != null )
        {
            index = Index.intValue();
            if( index > refl.getSpectrumMap().size() - 1 )
            {
                index = getIndexNmOfMaxKS( refl );
            }
        }
        else
        {
            index = getIndexNmOfMaxKS( refl );
        }
        Map ret = new TreeMap();
        for( int i = 0; i < smap.size(); i++ )
        {
            ret.put( ds[ i ], getKS( (Reflectance) smap.get( ds[ i ] ), index ) );
        }
        return ret;
    }


    //the ks in the parameter must be a increasing array, otherwise the dyeload is bad
    static Collection checkIncreasing( Double[] ks )
    {
        Collection ret = new ArrayList();
        double first = ks[ 0 ].doubleValue();

        for( int i = 1; i < ks.length; i++ )
        {
            if( ( ks[ i ].doubleValue() < ks[ i - 1 ].doubleValue() ) || ( ks[ i ].doubleValue() < first ) )
            {
                ret.add( new Integer( i ) );
                ret.add( new Integer( i - 1 ) );
            }
        }

        return ret;
    }

    //modify the dyeload map from 400 to 700 20nm
    public static Map getSameMap( Map dyeloadMap )
        throws ColorException
    {
        Map ret = new TreeMap();
        Double[] conc = (Double[]) dyeloadMap.keySet().toArray( new Double[ 0 ] );
        for( int i = 0; i < dyeloadMap.size(); i++ )
        {
            ret.put( conc[ i ], getReflectance47( (Reflectance) dyeloadMap.get( conc[ i ] ) ) );
        }
        return ret;
    }

    //get a ks value at a nm of a reflectance
    static Double getKS( Reflectance refl, int index )
        throws ColorException
    {
        if( refl == null )
        {
            throw new ColorException( "Reflectance in Load Factor Map is null" );
        }

        return new Double( convertToKS( refl.getSpectrum().getValues()[ index ] ) );
    }

    static Reflectance getReflectance47( Reflectance refl )
        throws ColorException
    {
        double[] k = getPredictReflectanceF( refl, 20 );
        Spectrum s = Spectrum.create( 400, 20, k );
        return ReflectanceImpl.create( s );
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

    public static double[] getPredictReflectanceF( Reflectance refl, int interval )
    {
        double[] refl_d = getPredictReflectance( refl, interval );
        double[] ret = new double[ refl_d.length ];
        System.arraycopy( refl_d, 0, ret, 0, ret.length );
        return ret;
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

    //how many point within the [400-700] interval
    public static int getNoNm( int interval )
    {
        return 300 / interval + 1;
    }
}


