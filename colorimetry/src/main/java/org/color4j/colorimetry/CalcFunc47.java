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

import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import org.color4j.colorimetry.observers.ObserverImpl;
import java.util.Collection;

public class CalcFunc47
{

    /**
     * Creates a new instance of CalcFunc47
     */
    public CalcFunc47()
    {
    }

    public static double getKS( double r )
    {
        if( r <= 0 )
        {
            r = 0.0001;
        }
        return ( 1 - r ) * ( 1 - r ) / ( 2 * r );
    }

    public static double[] convertConcToReflectance( double[][][] kos, double[][] con, double[] cp, double[] subks )
    {
        double ksp = 0.0;
        double fr = 0.0;
        int leng = subks.length;
        int leng2 = cp.length;
        double rp[] = new double[ leng ];

        for( int i = 0; i < leng; i++ )
        {
            ksp = 0.0;
            for( int j = 0; j < leng2; j++ )
            {
                fr = interpolateKS( j, i, kos, con, cp );
                ksp += fr;
            }
            ksp = ksp + subks[ i ];
            if( ksp <= 0.0 )
            {
                ksp = subks[ i ];
            }
            rp[ i ] = 1.0 + ksp - Math.sqrt( ksp * ( 2.0 + ksp ) );
            if( rp[ i ] <= 0.0 )
            {
                rp[ i ] = 0.001;
            }
            else if( rp[ i ] >= 1.0 )
            {
                rp[ i ] = 0.999;
            }
        }

        return rp;
    }

    public static double interpolateKS( int dye, int nm, double[][][] kos, double[][] con, double[] c )
    {
        double m = 0.0;
        double slope[] = getSlope( dye, nm, kos, con );
        int level = kos[ dye ].length;
        double fr = 0.0;

        if( c[ dye ] >= con[ dye ][ level - 1 ] )
        {
            m = slope[ level - 1 ];
            if( m < 0.0 )
            {
                if( level > 1 )
                {
                    m = ( m + slope[ level - 2 ] ) / 2.0;
                    if( m < 0.0 )
                    {
                        m = 0.0;
                    }
                }
            }
            fr = kos[ dye ][ level - 1 ][ nm ] + m * ( c[ dye ] - con[ dye ][ level - 1 ] );
        }

        else if( c[ dye ] >= 0.0 && c[ dye ] < con[ dye ][ 0 ] )
        {
            m = slope[ 0 ];
            if( m < 0.0 )
            {
                m = 0;
            }
            fr = m * c[ dye ];
        }

        else if( c[ dye ] >= con[ dye ][ 0 ] && c[ dye ] < con[ dye ][ level - 1 ] )
        {
            for( int i = 1; i < level; i++ )
            {
                if( c[ dye ] >= con[ dye ][ i - 1 ] && c[ dye ] < con[ dye ][ i ] )
                {
                    m = slope[ i ];
                    if( m < 0 )
                    {
                        m = 0.0;
                    }
                    fr = kos[ dye ][ i - 1 ][ nm ] + m * ( c[ dye ] - con[ dye ][ i - 1 ] );
                    break;
                }
            }
        }

        else if( c[ dye ] < 0.0 )
        {
            fr = c[ dye ] * slope[ 0 ];
        }

        return fr;
    }

    public static double[] getSlope( int dye, int nm, double[][][] kos, double[][] con )
    {
        int level = kos[ dye ].length;
        double slope[] = new double[ level ];

        for( int i = 0; i < level; i++ )
        {
            if( i == 0 )
            {
                slope[ i ] = kos[ dye ][ i ][ nm ] / con[ dye ][ i ];
            }
            else
            {
                slope[ i ] = ( kos[ dye ][ i ][ nm ] - kos[ dye ][ i - 1 ][ nm ] ) / ( con[ dye ][ i ] - con[ dye ][ i - 1 ] );
            }
        }
        return slope;
    }

    public static double[] getConc( double[] c )
    {
        int len = c.length;

        double[] pc = new double[ len ];
        for( int i = 0; i < len; i++ )
        {
            if( c[ i ] <= -0.02 )
            {
                pc[ i ] = c[ i ];
            }

            else if( c[ i ] > -0.02 && c[ i ] < 0 )
            {
                pc[ i ] = 0.00;
            }

            else
            {
                pc[ i ] = c[ i ];
            }
        }

        return pc;
    }

    /*get the subInterval of weight table of illuminants given*/
    public static double[][] getWeights( Collection illuminants, String sobs, int interval )
        throws ColorException
    {
        int k = illuminants.size();
        double[][] ret = new double[ 3 * k ][ getNoNm( interval ) ];
        Observer obs = ObserverImpl.create( sobs );
        for( int i = 0; i < k; i++ )
        {
            Illuminant ill = IlluminantImpl.create( (String) illuminants.toArray()[ i ] );
            Weights w = ColorCalculator.computeWeights( ill, obs, interval );
            int start_nm = w.getShortestWavelength();
            int end_nm = w.getLongestWavelength();
            int start_w = ( 400 - start_nm ) / interval;
            int end_w = ( end_nm - 700 ) / interval;
            ret[ i * 3 ] = CalcFunc2.getSameIntervalW( w.getWeightsX(), start_w, end_w );
            ret[ i * 3 + 1 ] = CalcFunc2.getSameIntervalW( w.getWeightsY(), start_w, end_w );
            ret[ i * 3 + 2 ] = CalcFunc2.getSameIntervalW( w.getWeightsZ(), start_w, end_w );
        }

        return ret;
    }

    //how many point within the [400-700] interval
    public static int getNoNm( int interval )
    {
        return 300 / interval + 1;
    }

    //get the [400-700] reflectance
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

    public static float[] getPredictReflectanceF( Reflectance refl, int interval )
    {
        double[] refl_d = getPredictReflectance( refl, interval );
        float[] ret = new float[ refl_d.length ];
        for( int i = 0; i < ret.length; i++ )
        {
            ret[ i ] = (float) refl_d[ i ];
        }
        return ret;
    }

    //Summation of weights to get whitepoint
    public static double[] getWhitepoint( double[][] weight )
    {
        double[] wp = new double[ 3 ];
        for( int i = 0; i < 3; i++ )
        {
            wp[ i ] = 0.0;
            for( int j = 0; j < weight[ i ].length; j++ )
            {
                wp[ i ] += weight[ i ][ j ];
            }
        }
        return wp;
    }

    //get the deltaE under a whitepoint
    public static double getDeltaE( String CDF, XYZ target, XYZ predict, XYZ whitepoint )
        throws ColorException
    {
        ColorEncoding ce_t = new CIELab( target.toLab( whitepoint ) );
        ColorEncoding ce_p = new CIELab( predict.toLab( whitepoint ) );

        DifferenceAlgorithm da = MatchingFactory.getInstance().getAlgorithm( CDF );

        ColorDifference cd = da.compute( ce_t, ce_p );
        return cd.getValue( ColorDifference.DELTA_E );
    }

    //change reflectance to reflectancePE
/*    
    public static ReflectancePE getReflectancePE( Reflectance refl ) 
    {
        int interval = refl.getSpectrum().getInterval();
        int start = refl.getSpectrum().getShortestWavelength();
        int end = refl.getSpectrum().getLongestWavelength();
        double[] tr = refl.getSpectrum().getValues();
        TreeMap spectrum = new TreeMap();
        
        int count = 0;
        
        for( int i = start; i <= end; i += interval )
        {
            spectrum.put( new Integer( i ), new Double( tr[count++] ) );
        }
        
        ReflectancePE reflpe = new ReflectancePE();
        reflpe.setSpectrumMap( spectrum );        
        return reflpe;
    }
*/
    //use in colorConstancy
    public static double[] getBFDCaf( XYZ xyz_ws, XYZ xyz_s, XYZ xyz_wd )
    {

        double kr, kg, kb;
        double[] power = { 0.0, 0.0, 0.083437 };
        double[] refln = { 0.0, 0.0, 0.0 };
        double pr, pg, pb, rb2, gb2, bb2, r2, g2, b2;//r1,g1,b1,;
//        double rill1,gill1,bill1,rill2,gill2,bill2,xy2,yy2,zy2;

        double[] rgbill1 = CalcFunc2.BFDChromaAdaptN10rgb( xyz_ws.getX(), xyz_ws.getY(), xyz_ws.getZ() );
        double[] rgbill2 = CalcFunc2.BFDChromaAdaptN10rgb( xyz_wd.getX(), xyz_wd.getY(), xyz_wd.getZ() );
        double[] rgb1 = CalcFunc2.BFDChromaAdaptN10rgb( xyz_s.getX(), xyz_s.getY(), xyz_s.getZ() );

        pr = Math.pow( ( rgbill1[ 0 ] / rgbill2[ 0 ] ), power[ 0 ] );
        pg = Math.pow( ( rgbill1[ 1 ] / rgbill2[ 1 ] ), power[ 1 ] );
        pb = Math.pow( ( rgbill1[ 2 ] / rgbill2[ 2 ] ), power[ 2 ] );

        kr = ( rgbill2[ 0 ] + refln[ 0 ] ) / Math.pow( ( rgbill1[ 0 ] + refln[ 0 ] ), pr );
        kg = ( rgbill2[ 1 ] + refln[ 1 ] ) / Math.pow( ( rgbill1[ 1 ] + refln[ 1 ] ), pg );
        kb = ( rgbill2[ 2 ] + refln[ 2 ] ) / Math.pow( ( rgbill1[ 2 ] + refln[ 2 ] ), pb );

        rb2 = kr * Math.pow( ( rgb1[ 0 ] + refln[ 0 ] ), pr ) - refln[ 0 ];
        gb2 = kg * Math.pow( ( rgb1[ 1 ] + refln[ 1 ] ), pg ) - refln[ 1 ];
        bb2 = kb * Math.pow( ( rgb1[ 2 ] + refln[ 2 ] ), pb ) - refln[ 2 ];

        r2 = rb2;
        g2 = gb2;
        b2 = bb2;

        double[] xyz_d = CalcFunc2.BFDChromaAdaptN10xyz( r2, g2, b2 );

        xyz_d[ 1 ] = xyz_d[ 1 ] * xyz_s.getY();
        xyz_d[ 0 ] = xyz_d[ 0 ] * xyz_s.getY();
        xyz_d[ 2 ] = xyz_d[ 2 ] * xyz_s.getY();

        return xyz_d;
    }

    public static double[] getR8All( double[] rob, double[] row, double[] bb, double[] wb )
    {
        int leng = rob.length;
        double[] ret = new double[ leng ];
        for( int i = 0; i < leng; i++ )
        {
            double b = getB( rob[ i ], row[ i ], bb[ i ], wb[ i ] );
            ret[ i ] = getR8( b );
        }
        return ret;
    }

    public static double getB( double rb, double rw, double rgb, double rgw )
    {
        double a1 = 1 + rb * rw;
        double a2 = rgw - rgb;
        double a3 = 1 + rgb * rgw;
        double a4 = rw - rb;
        double a5 = rb * rgw - rgb * rw;
        if( a5 == 0.0 )
        {
            return 1.0;
        }
        return ( a1 * a2 - a3 * a4 ) / ( a5 + a5 );
    }

    public static double getR8( double b )
    {
        if( b * b - 1 < 0.0 )
        {
            return 1.0;
        }
        return b - Math.pow( b * b - 1.0, 0.5 );
    }

    public static double[][] getR8Color( double[] wb, double[] bb, double[][] wow, double[][] wob )
    {
        int leng = wow.length;
        int leng2 = wow[ 0 ].length;
        double[][] ret = new double[ leng ][ leng2 ];
        for( int i = 0; i < leng; i++ )
        {
            ret[ i ] = getR8All( wob[ i ], wow[ i ], bb, wb );
        }
        return ret;
    }
}
