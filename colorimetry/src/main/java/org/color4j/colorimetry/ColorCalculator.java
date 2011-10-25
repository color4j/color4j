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

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.CIELuv;
import org.color4j.colorimetry.encodings.HunterLab;
import org.color4j.colorimetry.encodings.RGB;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import org.color4j.colorimetry.observers.ObserverImpl;
//import org.slf4j.Logger;

/**
 * The ColoCalculator is the central place for doing color operations.
 *
 * <p>The ColorCalculator handles all computations and conversions for
 *
 * color objects, including Colorspace conversions, match algorithm,
 *
 * metamerism index and many more color physics units.</p>
 */

public class ColorCalculator
{
    private static HashMap m_Encodings;

    public static XYZ m_WhitePointD65;
    private static ICC_ColorSpace m_ICCDisplayColorSpace;
    private static ICC_ColorSpace m_ICCPrinterColorSpace;

//    private static Logger m_Logger;

    public static final String ENCODING_XYZ = "XYZ";
    public static final String ENCODING_CIELAB = "CIELab";
    public static final String ENCODING_CIELUV = "CIELuv";
    public static final String ENCODING_RGB = "RGB";
    public static final String ENCODING_CMYK = "CMYK";
    public static final String ENCODING_HUNTERLAB = "HunterLab";
    public static float[][] m_RGBTristimulus;

    static
    {
        m_RGBTristimulus = getWeights();
        try
        {
            Illuminant d65 = IlluminantImpl.create( "D65" );  //NOI18N
            Observer obs = ObserverImpl.create( Observer.NAME_CIE1964 );
            m_WhitePointD65 = computeWhitepoint( d65, obs );
        }
        catch( Exception e )
        {
            e.printStackTrace(); // Shouldn't happen
            InternalError internalError = new InternalError( "Color system malfunction." );
            internalError.initCause( e );
            throw internalError;  //NOI18N
        }
    }

    /**
     * Weight is calculate by illuminant & observer to use in calculate XYZ
     *
     * <p>which reflectance is > 1nm
     */
    public static Weights computeWeights( Illuminant ill, Observer obs, int interval )
    {
        //get the weights of ill & observer given        
        //if no standard illuminant, then WeightsCache will call WeightsImpl to generate weight        
        Weights w = WeightsCache.getInstance().getWeights( ill, obs, interval );
        return w;
    }

    public static void setICCMonitorProfile( String fileName )
        throws IOException
    {
        if( fileName != null && fileName.length() > 0 )
        {
            ICC_Profile profile = ICC_Profile.getInstance( fileName );
            m_ICCDisplayColorSpace = new ICC_ColorSpace( profile );
        }
    }

    public static void setICCPrintingProfile( String fileName )
        throws IOException
    {
        if( fileName != null && fileName.length() > 0 )
        {
            ICC_Profile profile = ICC_Profile.getInstance( fileName );
            m_ICCPrinterColorSpace = new ICC_ColorSpace( profile );
        }
    }

    public static ICC_ColorSpace getICCPrintingProfile()
    {
        return m_ICCPrinterColorSpace;
    }

    public static ICC_ColorSpace getICCMonitorProfile()
    {
        return m_ICCDisplayColorSpace;
    }

    /**
     * whitepoint is the sum of weights factor
     */
    public static XYZ computeWhitepoint( Weights w )
    {
        double[] xyz_wp = new double[ 3 ];
        double[][] w_xyz =
            { w.getWeightsX(), w.getWeightsY(), w.getWeightsZ() };
        for( int i = 0; i < 3; i++ )
        {
            xyz_wp[ i ] = 0.0;
            for( int j = 0; j < w_xyz[ 0 ].length; j++ )
            {
                xyz_wp[ i ] += w_xyz[ i ][ j ];
            }
        }
        return new XYZ( xyz_wp );
    }

    /**
     * whitepoint is the sum of weights factor for XYZ, it is a combination of
     *
     * illuminant & observer. It is use to convert from XYZ to other color space
     *
     * like CIELab, CIELuv..
     */
    public static XYZ computeWhitepoint( Illuminant ill, Observer obs )
        throws ColorException
    {
        Weights w;
        w = WeightsCache.getInstance().getWeights( ill, obs );
        if( w == null )
        {
            throw new ColorException( "WeightCache.getWeights() return null" );
        }
        return computeWhitepoint( w );
    }

    /**
     * Color space XYZ is the combination of of illuminant, observer & reflectance
     *
     * illuminant: like daylight
     *
     * observer: simulate our eyes data
     *
     * reflectance:the power of the object reflect at certain wavelength
     */
    public static XYZ computeXYZ( Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        return XYZ.create( ill, refl, obs );
    }

    public static double[] computeDin99Lab( double[] lab, double Ke, double Kch )
    {
        double[] ret = new double[ 3 ];
        double dp = 3.14159 / 180.0;
        double e = lab[ 1 ] * Math.cos( 16 * dp ) + lab[ 2 ] * Math.sin( 16 * dp );
        double f = 0.7 * ( -lab[ 1 ] * Math.sin( 16 * dp ) + lab[ 2 ] * Math.cos( 16 * dp ) );

        double g = Math.sqrt( Math.pow( e, 2 ) + Math.pow( f, 2 ) );
        double hef = atan( f, e ) * dp;
        double c99 = Math.log( 1 + 0.045 * g ) / ( 0.045 * Ke * Kch );
        ret[ 0 ] = ( 105.51 / Ke ) * Math.log( 1 + 0.0158 * lab[ 0 ] );
        ret[ 1 ] = c99 * Math.cos( hef );
        ret[ 2 ] = c99 * Math.sin( hef );
        return ret;
    }

    public static double[] computeDin99Lch( double[] lab, double Ke, double Kch )
    {
        double[] ret = new double[ 3 ];
        double dp = 3.14159 / 180.0;
        double e = lab[ 1 ] * Math.cos( 16 * dp ) + lab[ 2 ] * Math.sin( 16 * dp );
        double f = 0.7 * ( -lab[ 1 ] * Math.sin( 16 * dp ) + lab[ 2 ] * Math.cos( 16 * dp ) );
        double g = Math.sqrt( Math.pow( e, 2 ) + Math.pow( f, 2 ) );
        double hef = atan( f, e );
        double c99 = Math.log( 1 + 0.045 * g ) / ( 0.045 * Ke * Kch );
        ret[ 0 ] = ( 105.51 / Ke ) * Math.log( 1 + 0.0158 * lab[ 0 ] );
        ret[ 1 ] = c99;
        ret[ 2 ] = hef;
        return ret;
    }

    public static double[] computeHunterLab( XYZ xyz, XYZ whitepoint )
    {
        double[] lab = new double[ 3 ];
        double[] FN =
            {
                xyz.getX() / whitepoint.getX(),
                xyz.getY() / whitepoint.getY(), xyz.getZ() / whitepoint.getZ()
            };
        lab[ 0 ] = 100.0 * Math.pow( FN[ 1 ], .5 );
        lab[ 1 ] = 175.0 * ( FN[ 0 ] - FN[ 1 ] ) * Math.pow( .0102 * whitepoint.getX() / FN[ 1 ], .5 );
        lab[ 2 ] = .4 * 175.0 * ( FN[ 1 ] - FN[ 2 ] ) * Math.pow( .00847 * whitepoint.getZ() / FN[ 1 ], .5 );
        return lab;
    }

    public static double[] convertLabToXYZ( CIELab Lab, XYZ whitepoint )
    {
        double[] lab =
            { Lab.getL(), Lab.geta(), Lab.getb() };
        double[] FN = new double[ 3 ];
        double[] ILL =
            { whitepoint.getX(), whitepoint.getY(), whitepoint.getZ() };

        double[] XYZ_v = new double[ 3 ];

        FN[ 1 ] = lab[ 0 ] / 116.0;
        FN[ 0 ] = FN[ 1 ] + lab[ 1 ] / 500.0;
        FN[ 2 ] = FN[ 1 ] - lab[ 2 ] / 200.0;
        for( int i = 0; i < 3; i++ )
        {
            if( FN[ 1 ] <= 0.068961672 )
            {
                XYZ_v[ i ] = ILL[ i ] * FN[ i ] / 7.787;
            }
            else
            {
                XYZ_v[ i ] = ILL[ i ] * Math.pow( FN[ i ] + 16.0 / 116.0, 3.0 );
            }
        }
        return ( XYZ_v );
    }

    public static double[] convertLuvToXYZ( CIELuv Luv, XYZ whitepoint )
    {
        double L, u, v, xn, yn, zn;
        double X, Y, Z;
        L = Luv.getL();
        u = Luv.getu();
        v = Luv.getv();
        xn = whitepoint.getX();
        yn = whitepoint.getY();
        zn = whitepoint.getZ();
        if( ( L / 903.3 ) <= .008856 )
        {
            Y = L * yn / 903.3;
        }
        else
        {
            Y = yn * Math.pow( ( L + 16.0 ) / 116.0, 3.0 );
        }
        double denum = xn + 15 * yn + 3 * zn;
        double uak = u + 13 * L * 4 * xn / denum;
        double vak = v + 13 * L * 9 * yn / denum;

        X = 9 * Y * uak / ( 4 * vak );
        Z = ( 4 * 13 * L * X - uak * ( X + 15 * Y ) ) / ( 3 * uak );

        double[] xyz = { X, Y, Z };
        return xyz;
    }

    //TODO KH - Aug 25, 2004 : using 4 x 4 matrix because our matrix operations
    //optimized to using 4 x 4 matrices.  should reoptimze to 3 x 3 matrix(?)
    private static float[][] getWeights()
    {
        return new float[][]
            {
                new float[]{ 3.2406f, -1.5372f, -0.4986f, 0 },
                new float[]{ -0.9689f, 1.8758f, 0.0415f, 0 },
                new float[]{ 0.0557f, -0.204f, 1.057f, 0 },
                new float[]{ 0, 0, 0, 1 }
            };
    }

    /*
     * taken from matrix class in ch.idx3d.engine3d.Matrix
     *
     */
    private static class Matrix
    {
        // M A T R I X   D A T A

        private float m00 = 1, m01 = 0, m02 = 0, m03 = 0;
        private float m10 = 0, m11 = 1, m12 = 0, m13 = 0;
        private float m20 = 0, m21 = 0, m22 = 1, m23 = 0;
        private float m30 = 0, m31 = 0, m32 = 0, m33 = 1;

        public Matrix()
        {

        }

        public Matrix( float[][] a )
        {
            setData( a );
        }

        public void setData( float[][] a )
        {
            m00 = a[ 0 ][ 0 ];
            m01 = a[ 0 ][ 1 ];
            m02 = a[ 0 ][ 2 ];
            m03 = a[ 0 ][ 3 ];
            m10 = a[ 1 ][ 0 ];
            m11 = a[ 1 ][ 1 ];
            m12 = a[ 1 ][ 2 ];
            m13 = a[ 1 ][ 3 ];
            m20 = a[ 2 ][ 0 ];
            m21 = a[ 2 ][ 1 ];
            m22 = a[ 2 ][ 2 ];
            m23 = a[ 2 ][ 3 ];
            m30 = a[ 3 ][ 0 ];
            m31 = a[ 3 ][ 1 ];
            m32 = a[ 3 ][ 2 ];
            m33 = a[ 3 ][ 3 ];
        }

        public Matrix inverse()
        // Returns the inverse of this matrix
        // Code generated with MapleV and handoptimized
        {
            Matrix m = new Matrix();

            float q1 = m12;
            float q6 = m10 * m01;
            float q7 = m10 * m21;
            float q8 = m02;
            float q13 = m20 * m01;
            float q14 = m20 * m11;
            float q21 = m02 * m21;
            float q22 = m03 * m21;
            float q25 = m01 * m12;
            float q26 = m01 * m13;
            float q27 = m02 * m11;
            float q28 = m03 * m11;
            float q29 = m10 * m22;
            float q30 = m10 * m23;
            float q31 = m20 * m12;
            float q32 = m20 * m13;
            float q35 = m00 * m22;
            float q36 = m00 * m23;
            float q37 = m20 * m02;
            float q38 = m20 * m03;
            float q41 = m00 * m12;
            float q42 = m00 * m13;
            float q43 = m10 * m02;
            float q44 = m10 * m03;
            float q45 = m00 * m11;
            float q48 = m00 * m21;
            float q49 = q45 * m22 - q48 * q1 - q6 * m22 + q7 * q8;
            float q50 = q13 * q1 - q14 * q8;
            float q51 = 1 / ( q49 + q50 );

            m.m00 = ( m11 * m22 * m33 - m11 * m23 * m32 - m21 * m12 * m33 + m21 * m13 * m32 + m31 * m12 * m23 - m31 * m13 * m22 ) * q51;
            m.m01 = -( m01 * m22 * m33 - m01 * m23 * m32 - q21 * m33 + q22 * m32 ) * q51;
            m.m02 = ( q25 * m33 - q26 * m32 - q27 * m33 + q28 * m32 ) * q51;
            m.m03 = -( q25 * m23 - q26 * m22 - q27 * m23 + q28 * m22 + q21 * m13 - q22 * m12 ) * q51;
            m.m10 = -( q29 * m33 - q30 * m32 - q31 * m33 + q32 * m32 ) * q51;
            m.m11 = ( q35 * m33 - q36 * m32 - q37 * m33 + q38 * m32 ) * q51;
            m.m12 = -( q41 * m33 - q42 * m32 - q43 * m33 + q44 * m32 ) * q51;
            m.m13 = ( q41 * m23 - q42 * m22 - q43 * m23 + q44 * m22 + q37 * m13 - q38 * m12 ) * q51;
            m.m20 = ( q7 * m33 - q30 * m31 - q14 * m33 + q32 * m31 ) * q51;
            m.m21 = -( q48 * m33 - q36 * m31 - q13 * m33 + q38 * m31 ) * q51;
            m.m22 = ( q45 * m33 - q42 * m31 - q6 * m33 + q44 * m31 ) * q51;
            m.m23 = -( q45 * m23 - q42 * m21 - q6 * m23 + q44 * m21 + q13 * m13 - q38 * m11 ) * q51;

            return m;
        }
    }

    public static XYZ convertRGBtoXYZ( RGB rgb )
    {
        double r = rgb.getR();
        double g = rgb.getG();
        double b = rgb.getB();

        if( r > 0.04045 )
        {
            r = Math.pow( ( r + 0.055 ) / 1.055, 2.4 );
        }
        else
        {
            r /= 12.92;
        }
        if( g > 0.04045 )
        {
            g = Math.pow( ( g + 0.055 ) / 1.055, 2.4 );
        }
        else
        {
            g /= 12.92;
        }
        if( b > 0.04045 )
        {
            b = Math.pow( ( b + 0.055 ) / 1.055, 2.4 );
        }
        else
        {
            b /= 12.92;
        }

        r *= 100;
        g *= 100;
        b *= 100;

        // 	KH - Aug 25, 2004 : should be the inverse of m_RGBTristumulus 
        Matrix m = new Matrix( m_RGBTristimulus ).inverse();

        double x = ( r * m.m00 ) + ( g * m.m01 ) + ( b * m.m02 );
        double y = ( r * m.m10 ) + ( g * m.m11 ) + ( b * m.m12 );
        double z = ( r * m.m20 ) + ( g * m.m21 ) + ( b * m.m22 );

        return new XYZ( new double[]{ x, y, z } );
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

    /** Returns the distance an object must be viewed for its size.
     *
     * <p>The <code>objectsize</code> parameter is expressed in meters,
     *
     * and the returned distance is in meters.</p>
     *
     **/
//    public static double getViewDistance( Observer obs, double objectsize )
//    {
//        return 0;
//    }

    /**
     * Returns the size of the viewed object at a given distance.
     *
     * <p>This method computes how big the view circle should be at a
     *
     * given distance.</p>
     *
     * <p>The <code>distance</code> parameter is expressed in meters,
     *
     * and the returned object size is in meters.</p>
     */

//    public static double getObjectSize( Observer obs, double distance )
//    {
//        return 0;
//    }
    public static String[] getAlgorithmNames()
    {
        return MatchingFactory.getInstance().getAlgorithmNames();
    }

    public static DifferenceAlgorithm getAlgorithm( String name )
    {
        return MatchingFactory.getInstance().getAlgorithm( name );
    }

    public static ColorDifference computeDifference( DifferenceAlgorithm algorithm,
                                                     ColorEncoding standard,
                                                     ColorEncoding sample
    )
    {
        return algorithm.compute( standard, sample );
    }

    public static ColorEncoding create( Class encodingclass, Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        try
        {
            Class[] params = new Class[]
                { Illuminant.class, Reflectance.class, Observer.class };
            Method create = encodingclass.getMethod( "create", params );  //NOI18N

            Object[] args = new Object[]
                { ill, refl, obs };
            Object obj = create.invoke( null, args );
            return (ColorEncoding) obj;
        }
        catch( NoSuchMethodException e )
        {
            ColorException ce = new ColorException( e.getMessage() );
            ce.initCause( e );
            throw ce;
        }
        catch( IllegalAccessException e )
        {
            ColorException ce = new ColorException( e.getMessage() );
            ce.initCause( e );
            throw ce;
        }
        catch( IllegalArgumentException e )
        {
            ColorException ce = new ColorException( e.getMessage() );
            ce.initCause( e );
            throw ce;
        }
        catch( InvocationTargetException e )
        {
            ColorException ce = new ColorException( e.getMessage() );
            ce.initCause( e );
            throw ce;
        }
    }

    static public String[] getColorEncodingNames()
    {
        checkNull();
        return (String[]) m_Encodings.keySet().toArray( new String[ 0 ] );
    }

    static public Class getColorEncodingClass( String name )
    {
        checkNull();
        Class cls = (Class) m_Encodings.get( name );
        return cls;
    }

    private static void checkNull()
    {
        if( m_Encodings == null )
        {
            synchronized( ColorCalculator.class )
            {
                m_Encodings = new HashMap();
                m_Encodings.put( ENCODING_XYZ, XYZ.class );
                m_Encodings.put( ENCODING_CIELAB, CIELab.class );
                m_Encodings.put( ENCODING_CIELUV, CIELuv.class );
                m_Encodings.put( ENCODING_RGB, RGB.class );
                m_Encodings.put( ENCODING_HUNTERLAB, HunterLab.class );
            }
        }
    }

    //return a Spectrum that contains KS value of a reflectance
    public static Spectrum getKS( Reflectance refl )
        throws ColorException
    {
        Spectrum s = refl.getSpectrum();
        double[] values = s.getValues();
//        int size = values.length;
        double[] data = CalcFunc.convertToKS( values );
        return Spectrum.create( s.getShortestWavelength(), s.getInterval(), data );
    }

    private static double[] convertToKS( Reflectance refl )
    {
        SortedMap s = refl.getSpectrumMap();
        double[] data = new double[ s.size() ];
        Iterator itr = s.values().iterator();
        for( int i = 0; itr.hasNext(); i++ )
        {
            data[ i ] = CalcFunc.convertToKS( ( (Number) itr.next() ).doubleValue() );
        }
        return data;
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

    //get a ks value at a nm of a reflectance
    static Double getKS( Reflectance refl, int index )
        throws ColorException
    {
        if( refl == null )
        {
            throw new ColorException( "Reflectance in Load Factor Map is null" );
        }

        return new Double( CalcFunc.convertToKS( refl.getSpectrum().getValues()[ index ] ) );
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

    static Reflectance getReflectance47( Reflectance refl )
        throws ColorException
    {
        double[] k = CalcFunc.getPredictReflectanceF( refl, 20 );
        Spectrum s = Spectrum.create( 400, 20, k );
        return ReflectanceImpl.create( s );
    }

    //this lab values must calculate under D65 10degree observer or C 2degree observer
    //GreyscaleStaining
    double getGreyscaleA04( CIELab lab_s, CIELab lab_b )
    {
        double de, dl, da, db, degs, ssr;
        double[] lab_bat = lab_b.getColorValues();
        double[] lab_std = lab_s.getColorValues();

        dl = lab_bat[ 0 ] - lab_std[ 0 ];
        da = lab_bat[ 1 ] - lab_std[ 1 ];
        db = lab_bat[ 2 ] - lab_std[ 2 ];

        if( ( Math.pow( dl, 2 ) + Math.pow( da, 2 ) + Math.pow( db, 2 ) ) == 0.0 )
        {
            de = 0.0;
            ssr = 5.0;
            return ( ssr );
        }
        else
        {
            de = Math.sqrt( Math.pow( dl, 2 ) + Math.pow( da, 2 ) + Math.pow( db, 2 ) );
            degs = de - 0.4 * Math.sqrt( Math.pow( de, 2 ) - Math.pow( dl, 2 ) );
        }
        ssr = 6.1 - 1.45 * Math.log( degs );
        if( ssr > 4.0 )
        {
            ssr = 5 - 0.23 * degs;
        }
        if( ssr < 1.25 )
        {
            ssr = 1.0;
        }
        else if( ssr < 1.745 )
        {
            ssr = 1.5;
        }
        else if( ssr < 2.245 )
        {
            ssr = 2.0;
        }
        else if( ssr < 2.745 )
        {
            ssr = 2.5;
        }
        else if( ssr < 3.245 )
        {
            ssr = 3.0;
        }
        else if( ssr < 3.745 )
        {
            ssr = 3.5;
        }
        else if( ssr < 4.245 )
        {
            ssr = 4.0;
        }
        else if( ssr < 4.745 )
        {
            ssr = 4.5;
        }
        else if( ssr <= 5.00 )
        {
            ssr = 5.0;
        }
        return ssr;
    }

    //GreyscaleColourchange
    double getGreyscaleA05( CIELab lab_s, CIELab lab_b )
    {
        double isosws, dl, dc, dh, hm, cm, x, d, dhk, dck, dhf, dcf, def;
        double cm_PI = 3.14159;
        double l_std = lab_s.getL();
        double l_bat = lab_b.getL();
        double c_bat = lab_b.getc();
        double h_bat = lab_b.geth();
        double c_std = lab_s.getc();
        double h_std = lab_s.geth();
        hm = 0.0;
        dc = c_bat - c_std;
        dl = l_bat - l_std;
        dh = h_bat - h_std;

        if( dh < -180.0 )
        {
            dh += 360.0;
        }
        if( dh > 180.0 )
        {
            dh -= 360.0;
        }
        dh = 2.0 * Math.sqrt( c_std * c_bat ) * Math.sin( ( dh / 2.0 ) * cm_PI / 180.0 );
        if( Math.abs( h_bat - h_std ) <= 180.0 )
        {
            hm = ( h_bat - h_std ) / 2.0;
        }
        else if( Math.abs( h_bat - h_std ) > 180.0 && ( h_bat + h_std ) < 360.0 )
        {
            hm = 180.0 + ( h_std + h_bat ) / 2.0;
        }
        else if( Math.abs( h_bat - h_std ) > 180.0 && ( h_bat + h_std ) >= 360.0 )
        {
            hm = ( h_std + h_bat ) / 2.0 - 180.0;
        }
        cm = ( c_std + c_bat ) / 2.0;
        if( Math.abs( hm - 280.0 ) <= 180.0 )
        {
            x = Math.pow( ( hm - 280.0 ) / 30.0, 2 );
        }
        else
        {
            x = Math.pow( ( 360.0 - Math.abs( hm - 280.0 ) ) / 30.0, 2 );
        }
        d = cm * dc * Math.exp( -1.0 * x ) / 100.0;
        dhk = dh - d;
        dck = dc - d;
        dhf = dhk / ( 1.0 + Math.pow( 10.0 * cm / 1000.0, 2 ) );
        dcf = dck / ( 1.0 + Math.pow( 20.0 * cm / 1000.0, 2 ) );
        def = Math.sqrt( dl * dl + dcf * dcf + dhf * dhf );
        if( def < 0.4 )
        {
            isosws = 5.0;
        }
        else if( def < 1.25 )
        {
            isosws = 4.5;
        }
        else if( def < 2.10 )
        {
            isosws = 4.0;
        }
        else if( def < 2.95 )
        {
            isosws = 3.5;
        }
        else if( def < 4.10 )
        {
            isosws = 3.0;
        }
        else if( def < 5.80 )
        {
            isosws = 2.5;
        }
        else if( def < 8.20 )
        {
            isosws = 2.0;
        }
        else if( def < 11.60 )
        {
            isosws = 1.5;
        }
        else
        {
            isosws = 1.0;
        }

        return isosws;
    }

    double getGreyScaleA02( CIELab lab_s, CIELab lab_b )
    {
        double t;
        double st = getDE( lab_s, lab_b );

        if( st < 0.8 )
        {
            t = 5.0;
        }
        else if( st < 1.7 )
        {
            t = 4.5;
        }
        else if( st < 2.5 )
        {
            t = 4.0;
        }
        else if( st < 3.4 )
        {
            t = 3.5;
        }
        else if( st < 4.8 )
        {
            t = 3.0;
        }
        else if( st < 6.8 )
        {
            t = 2.5;
        }
        else if( st < 9.6 )
        {
            t = 2.0;
        }
        else if( st < 13.6 )
        {
            t = 1.5;
        }
        else
        {
            t = 1.0;
        }

        return t;
    }

    double getGreyScaleA03( CIELab lab_s, CIELab lab_b )
    {
        double u;
        double st = getDE( lab_s, lab_b );
        if( st < 2.2 )
        {
            u = 5.0;
        }
        else if( st < 4.3 )
        {
            u = 4.5;
        }
        else if( st < 6.0 )
        {
            u = 4.0;
        }
        else if( st < 8.5 )
        {
            u = 3.5;
        }
        else if( st < 12.0 )
        {
            u = 3.0;
        }
        else if( st < 16.9 )
        {
            u = 2.5;
        }
        else if( st < 24.0 )
        {
            u = 2.0;
        }
        else if( st < 34.1 )
        {
            u = 1.5;
        }
        else
        {
            u = 1.0;
        }
        return u;
    }

    double getDE( CIELab lab_s, CIELab lab_b )
    {
        double k;
        double dL = lab_b.getL() - lab_s.getL();
        double da = lab_b.geta() - lab_s.geta();
        double db = lab_b.getb() - lab_s.getb();
        double dc = lab_b.getc() - lab_s.getc();
        double dh = lab_b.geth() - lab_s.geth();

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

    private static double[] computeEllipse( double[] xy, double[] target, double deltaE, int degree )
    {
        // TODO:
        double dp = Math.PI / 180.0;

        double rad = degree * dp;
        double rot = target[ 2 ] * dp;
        //rot = Math.PI/2.0;
        //double[] xy = computeEllipseAB(target, deltaE);

        if( degree == 0 )
        {
            return rotate( xy[ 0 ], 0.0, rot );
        }
        else if( degree == 180 )
        {
            return rotate( -xy[ 0 ], 0.0, rot );
        }
        else if( degree == 90 )
        {
            return rotate( 0.0, xy[ 1 ], rot );
        }
        else if( degree == 270 )
        {
            return rotate( 0.0, -xy[ 1 ], rot );
        }
        else
        {
            double tn = Math.tan( rad );
            double k = tn * tn + xy[ 1 ] * xy[ 1 ] / ( xy[ 0 ] * xy[ 0 ] );
            double x = xy[ 1 ] / Math.pow( k, .5 );
            if( ( degree >= 90 ) && ( degree <= 270 ) )
            {
                x = -x;
            }
            double y = xy[ 1 ] * Math.pow( ( 1 - x * x / ( xy[ 0 ] * xy[ 0 ] ) ), .5 );
            y = ( degree > 180 ) ? -y : y;

            return rotate( x, y, rot );
        }
    }

    private static double[] rotate( double x0, double y0, double rad )
    {
        double[] xy = new double[ 2 ];
        xy[ 0 ] = x0 * Math.cos( rad ) - y0 * Math.sin( rad );
        xy[ 1 ] = x0 * Math.sin( rad ) + y0 * Math.cos( rad );

        return xy;
        //double rt = Math.pow( x*x + y*y , .5);
        //return rt;
    }

    public static double computeDeltaLBoundary( double[] target, double deltaE )
    {
        double lt;
        if( target[ 0 ] > 16.0 )
        {
            lt = 0.040975 * target[ 0 ] / ( 1 + 0.01765 * target[ 0 ] );
        }
        else
        {
            lt = 0.511;
        }

        return deltaE * lt;
    }

    //get the ellipse long [0] & short [1] value
    public static double[] computeEllipseAB( double[] target, double deltaE )
    {
        double dp = Math.PI / 180.0;
        double[] xy = new double[ 2 ];
        double ct, ht, gt, t;

        ct = 0.0638 * target[ 1 ] / ( 1 + 0.0131 * target[ 1 ] ) + 0.638;

        xy[ 0 ] = Math.abs( deltaE * ct );//Math.abs(deltaE*Math.pow(ct,.5));

        if( target[ 2 ] > 164 && target[ 2 ] < 345 )
        {
            ht = 0.56 + Math.abs( .2 * Math.cos( ( target[ 2 ] + 168 ) * dp ) );
        }
        else
        {
            ht = 0.36 + Math.abs( 0.4 * Math.cos( ( target[ 2 ] + 35 ) * dp ) );
        }

        gt = Math.sqrt( Math.pow( target[ 1 ], 4.0 ) / ( Math.pow( target[ 1 ], 4.0 ) + 1900 ) );

        t = ht * gt + 1 - gt;
        ht = t * ct;
        xy[ 1 ] = Math.abs( deltaE * ht );//Math.abs(deltaE*Math.pow(ht,.5));

        return xy;
    }

    //return array[360][2], which is the xy points from angle 0 to 360 degree
    //target is the CIELab value of the color, deltaE control the size of the ellipse
    public static double[][] getEllipsePoint( ColorEncoding target, double deltaE )
    {
        if( deltaE <= 0 )
        {
            deltaE = 1.0;
        }

        if( !( target instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIELabDE only accepts CIELab color encodings." );  //NOI18N
        }
        CIELab tlab = (CIELab) target;
        double[] lch = { tlab.getL(), tlab.getc(), tlab.geth() };
        double[][] ret = new double[ 360 ][ 2 ];

        double[] xy = computeEllipseAB( lch, deltaE );
        for( int i = 0; i < 360; i++ )
        {
            ret[ i ] = computeEllipse( xy, lch, deltaE, i );
        }

        return ret;
    }

    public static Reflectance getActualReflectance( Reflectance overWhite,
                                                    Reflectance overBlack,
                                                    Reflectance whiteBacking,
                                                    Reflectance blackBacking
    )
    {
        int interval = 20;
        double[] row = CalcFunc47.getPredictReflectance( overWhite, interval );
        double[] rob = CalcFunc47.getPredictReflectance( overBlack, interval );
        double[] bw = CalcFunc47.getPredictReflectance( whiteBacking, interval );
        double[] bb = CalcFunc47.getPredictReflectance( blackBacking, interval );
        double[] actual = CalcFunc47.getR8All( rob, row, bb, bw );
        Spectrum sp = new Spectrum( 400, 20, actual );
        return ReflectanceImpl.create( sp );
    }
}
