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

import java.awt.color.ICC_ColorSpace;
import org.color4j.colorimetry.CalcFunc;
import org.color4j.colorimetry.ColorCalculator;
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Spectrum;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.WeightsCache;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;

/**
 * Value container for XYZ values.
 * <p>This class holds the XYZ values, irrespective of the Standard Observer.</p>
 * <p>The sole purpose of this class is to conviniently encapsulate the XYZ values
 * into a manageable entity.</p>
 */
public class XYZ extends ColorEncoding
{
    public static final double D50X = 96.42;
    public static final double D50Y = 100.0;
    public static final double D50Z = 82.49;

    static public XYZ create( Illuminant ill, Reflectance refl, Observer obs )
        throws ColorException
    {
        if( ill == null )
        {
            throw new IllegalArgumentException( "An null parameter for Illuminant is not allowed." );   //NOI18N
        }
        if( obs == null )
        {
            throw new IllegalArgumentException( "An null parameter for Observer is not allowed." );     //NOI18N
        }
        if( refl == null )
        {
            throw new IllegalArgumentException( "An null parameter for Reflectance is not allowed." );  //NOI18N
        }

        // return value
        double rt_value[] = { 0.0, 0.0, 0.0 };
        //use to figure out the start & ending nm so that they can be same
        int start_r, end_r, start_w, end_w;
        //detect the reflectance interval
        Spectrum spectrum = refl.getSpectrum();
        if( spectrum == null )
        {
            throw new ColorException( "Reflectance " + refl.getName() + " does not have a Spectrum." );     //NOI18N
        }
        int interval = spectrum.getInterval();

        //get the weights of ill & observer given
        Weights w = WeightsCache.getInstance().getWeights( ill, obs, interval );

        //start nm of weights
        int w_S = w.getShortestWavelength();
        //ending nm of weights
        int w_E = w.getLongestWavelength();
        //start nm of reflectance
        int r_S = spectrum.getShortestWavelength();
        //ending nm of reflectance
        int r_E = spectrum.getLongestWavelength();

        if( r_S < w_S )
        {   //we need to take offset of amount 'start_r' of reflectance
            start_r = ( w_S - r_S ) / interval;
            start_w = 0;
        }
        else
        {   //we need to take offset of amount 'start_w' of weights
            start_r = 0;
            start_w = ( r_S - w_S ) / interval;
        }
        if( r_E < w_E )
        {   //we need to 'compound' ending of weight of amount end_w
            end_r = 0;
            end_w = ( w_E - r_E ) / interval;
        }
        else
        {   //we need to 'truncte' ending of reflectance of amount end_r
            end_w = 0;
            end_r = ( r_E - w_E ) / interval;
        }

        double[] r_xyz = CalcFunc.getSameIntervalR( refl.getSpectrum().getValues(), start_r, end_r );
        double[] w_x = CalcFunc.getSameIntervalW( w.getWeightsX(), start_w, end_w );
        double[] w_y = CalcFunc.getSameIntervalW( w.getWeightsY(), start_w, end_w );
        double[] w_z = CalcFunc.getSameIntervalW( w.getWeightsZ(), start_w, end_w );

        double length = ( w_x.length <= r_xyz.length ) ? w_x.length : r_xyz.length;
        for( int j = 0; j < length; j++ )
        {
            rt_value[ 0 ] += w_x[ j ] * r_xyz[ j ];
            rt_value[ 1 ] += w_y[ j ] * r_xyz[ j ];
            rt_value[ 2 ] += w_z[ j ] * r_xyz[ j ];
        }
        //return the XYZ object of deside value
        //return the XYZ object of deside value

        return new XYZ( rt_value );
    }

    static public XYZ convert( ColorEncoding ce )
        throws UnsupportedConversionException
    {
        if( ce instanceof XYZ )
        {
            return (XYZ) ce;
        }
        else
        {
            throw new UnsupportedConversionException( "Unable to convert from " + ce.getClass()
                .getName() + " to " + XYZ.class.getName() );     //NOI18N
        }
    }

    public XYZ( CIELab lab, XYZ whitepoint )
    {
        m_Values = ColorCalculator.convertLabToXYZ( lab, whitepoint );
    }

    public XYZ( XYZ xyz )
    {
        m_Values[ 0 ] = xyz.getX();
        m_Values[ 1 ] = xyz.getY();
        m_Values[ 2 ] = xyz.getZ();
    }

    public XYZ( CIELuv luv, XYZ whitepoint )
    {
        m_Values = ColorCalculator.convertLuvToXYZ( luv, whitepoint );
    }

    /**
     * Constructor using indivdual Numbers.
     */
    public XYZ( Number x, Number y, Number z )
    {
        super();
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = x.doubleValue();
        m_Values[ 1 ] = y.doubleValue();
        m_Values[ 2 ] = z.doubleValue();
    }

    /**
     * Constructor using indivdual doubles.
     */
    public XYZ( double x, double y, double z )
    {
        super();
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = x;
        m_Values[ 1 ] = y;
        m_Values[ 2 ] = z;
    }

    /**
     * Constructor using an array of Numbers.
     */
    public XYZ( Number[] xyz )
    {
        super();
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = xyz[ 0 ].doubleValue();
        m_Values[ 1 ] = xyz[ 1 ].doubleValue();
        m_Values[ 2 ] = xyz[ 2 ].doubleValue();
    }

    /**
     * Constructor using indivdual doubles.
     */
    public XYZ( double[] xyz )
    {
        super();
        m_Values = new double[ 3 ];
        m_Values[ 0 ] = xyz[ 0 ];
        m_Values[ 1 ] = xyz[ 1 ];
        m_Values[ 2 ] = xyz[ 2 ];
    }

    /**
     * returns X value
     */
    public double getX()
    {
        return m_Values[ 0 ];
    }

    /**
     * returns Y value
     */
    public double getY()
    {
        return m_Values[ 1 ];
    }

    /**
     * returns Z value
     */
    public double getZ()
    {
        return m_Values[ 2 ];
    }

    /**
     * Computes the CIELab value for the XYZ under the Illuminant.
     *
     * <p>This is a convinience method equivalent to;</code><pre>
     *
     * return (CIELab) convert( xyz, ill, CIELab.class );
     *
     * </pre></code>
     */

    public double[] toLab( XYZ whitepoint )
    {
        double[] lab = new double[ 3 ];
        double[] FN = new double[ 3 ];
        double XYZ_v[] =
            { getX(), getY(), getZ() };
        double ILL[] =
            { whitepoint.getX(), whitepoint.getY(), whitepoint.getZ() };
        for( int i = 0; i < 3; i++ )
        {
            if( XYZ_v[ i ] / ILL[ i ] < 0.008856 )
            {
                FN[ i ] = 7.787 * XYZ_v[ i ] / ILL[ i ] + 16.0 / 116.0;
            }
            else
            {
                FN[ i ] = Math.pow( XYZ_v[ i ] / ILL[ i ], 1.0 / 3.0 );
            }
        }
        lab[ 0 ] = 116.0 * FN[ 1 ] - 16.0;
        lab[ 1 ] = 500.0 * ( FN[ 0 ] - FN[ 1 ] );
        lab[ 2 ] = 200.0 * ( FN[ 1 ] - FN[ 2 ] );
        return ( lab );
    }

    public double[] toLch( XYZ whitepoint )
    {
        double[] lch = new double[ 3 ];
        double[] lab = toLab( whitepoint );
        lch[ 0 ] = lab[ 0 ];
        double d1 = Math.pow( lab[ 1 ], 2.0 );
        double d2 = Math.pow( lab[ 2 ], 2.0 );
        lch[ 1 ] = Math.sqrt( d1 + d2 );
        lch[ 2 ] = ColorCalculator.atan( lab[ 1 ], lab[ 2 ] );
        return ( lch );
    }

    public CMYK toCMYK( XYZ whitepoint )
    {
        return toCMYK( whitepoint, null );
    }

    /*
    return the srgb value of xyz given
    */
    public RGB toRGB( XYZ whitepoint )//XYZ whitepointD50
    {
        return toRGB( whitepoint, (ICC_ColorSpace) null );
    }

    public RGB toRGB( XYZ whitepoint, ICC_ColorSpace displayColorSpace )//XYZ whitepointD50
    {

        if( displayColorSpace != null )
        {
            float[] icc_xyz = displayColorSpace.fromCIEXYZ( computePCSXYZ( whitepoint ) );
            float[] icc_rgb = displayColorSpace.toRGB( icc_xyz );
            return new RGB( icc_rgb[ 0 ], icc_rgb[ 1 ], icc_rgb[ 2 ] );
        }
        else
        {
            return new RGB( toRGB( whitepoint, ColorCalculator.m_WhitePointD65 ) );
        }
    }

    public double[] toRGB( XYZ whitepoint, XYZ whitepointD65 )//XYZ whitepointD50
    {
        double temp[] = new double[ 3 ];
        double rgb[] = new double[ 3 ];
        double D65[] =
            { whitepointD65.getX(), whitepointD65.getY(), whitepointD65.getZ() };

        double illum[] =
            { whitepoint.getX(), whitepoint.getY(), whitepoint.getZ() };

        double xyzv[] =
            { getX(), getY(), getZ() };

        for( int i = 0; i < 3; i++ )
        {
            temp[ i ] = xyzv[ i ] * D65[ i ] / illum[ i ];
        }
/*
        // KH - Aug 25, 2004 : moved hard-coded values into m_RGBTristimulus matrix

        //the mark up part is another RGB calculation by Adobe under D50
        //rgb[0] = 1.8241 * temp[0] - .5048 * temp[1] - 0.308 * temp[2];
        rgb[0] = 3.2406 * temp[0] - 1.5372 * temp[1] - 0.4986 * temp[2];
        rgb[0] = rgb[0] / 100.0;
        //rgb[1] = -0.9935 * temp[0] + 1.9228 * temp[1] + 0.0426 * temp[2];
        rgb[1] = -0.9689 * temp[0] + 1.8758 * temp[1] + 0.0415 * temp[2];
        rgb[1] = rgb[1] / 100.0;
        //rgb[2] = 0.0184 * temp[0] - 0.1616 * temp[1] + 1.3864 * temp[2];
        rgb[2] = 0.0557 * temp[0] - 0.204 * temp[1] + 1.057 * temp[2];
        rgb[2] = rgb[2] / 100.0;
*/
        // KH - Aug 25, 2004 : adapting to use static matrix
        rgb[ 0 ] = ColorCalculator.m_RGBTristimulus[ 0 ][ 0 ] * temp[ 0 ] + ColorCalculator.m_RGBTristimulus[ 0 ][ 1 ] * temp[ 1 ] + ColorCalculator.m_RGBTristimulus[ 0 ][ 2 ] * temp[ 2 ];
        rgb[ 0 ] = rgb[ 0 ] / 100.0;
        rgb[ 1 ] = ColorCalculator.m_RGBTristimulus[ 1 ][ 0 ] * temp[ 0 ] + ColorCalculator.m_RGBTristimulus[ 1 ][ 1 ] * temp[ 1 ] + ColorCalculator.m_RGBTristimulus[ 1 ][ 2 ] * temp[ 2 ];
        rgb[ 1 ] = rgb[ 1 ] / 100.0;
        rgb[ 2 ] = ColorCalculator.m_RGBTristimulus[ 2 ][ 0 ] * temp[ 0 ] + ColorCalculator.m_RGBTristimulus[ 2 ][ 1 ] * temp[ 1 ] + ColorCalculator.m_RGBTristimulus[ 2 ][ 2 ] * temp[ 2 ];
        rgb[ 2 ] = rgb[ 2 ] / 100.0;

        // KH - Dec 26, 2004 : obviously, if rgb[i] ends up to be > 1, it is out of gamut
        // if it is less than 0
        for( int i = 0; i < 3; i++ )
        {
            if( rgb[ i ] < 0.0 )
            {
                rgb[ i ] = 0.0;
            }
            else if( rgb[ i ] > 1.0 )
            {
                rgb[ i ] = 1.0;
            }

            if( rgb[ i ] <= 0.0031308 )  // HanSoong said; 0.00304
            {
                rgb[ i ] = 12.92 * rgb[ i ];
            }
            else
            {
                rgb[ i ] = ( 1.055 * Math.pow( rgb[ i ], 1.0 / 2.4 ) - 0.055 );
            }
        }
        return rgb;
    }

    private float[] computePCSXYZ( XYZ whitepoint )
    {
        double X = ( getX() * ( D50X / whitepoint.getX() ) ) / 100.0;
        double Y = ( getY() * ( D50Y / whitepoint.getY() ) ) / 100.0;
        double Z = ( getZ() * ( D50Z / whitepoint.getZ() ) ) / 100.0;
        return new float[]{ (float) X, (float) Y, (float) Z };
    }


    /**
     * this is the implementation of XYZ -> Luv
     */
    public double[] toLuv( XYZ whitepoint )
    {
        //the return value
        double[] luv = new double[ 3 ];
        double IU, IV, denom, D, SU, SV;
        double[] xyz_v =
            { getX(), getY(), getZ() };

        double[] xyz_wp =
            { whitepoint.getX(), whitepoint.getY(), whitepoint.getZ() };

        denom = xyz_wp[ 0 ] + 15.0 * xyz_wp[ 1 ] + 3.0 * xyz_wp[ 2 ];

        IU = 4.0 * xyz_wp[ 0 ] / denom;
        IV = 9.0 * xyz_wp[ 1 ] / denom;
        D = xyz_v[ 0 ] + 15.0 * xyz_v[ 1 ] + 3.0 * xyz_v[ 2 ];
        SU = 4.0 * xyz_v[ 0 ] / D;
        SV = 9.0 * xyz_v[ 1 ] / D;
        luv[ 0 ] = xyz_v[ 1 ] / xyz_wp[ 1 ];

        if( luv[ 0 ] <= 0.008856 )
        {
            luv[ 0 ] = 903.292 * ( luv[ 0 ] );
        }
        else
        {
            luv[ 0 ] = 116.0 * Math.pow( luv[ 0 ], 1.0 / 3.0 ) - 16.0;
        }

        luv[ 1 ] = 13.0 * ( luv[ 0 ] ) * ( SU - IU );
        luv[ 2 ] = 13.0 * ( luv[ 0 ] ) * ( SV - IV );
        return luv;
    }

    public CMYK toCMYK( XYZ whitepoint, ICC_ColorSpace printerColorSpace )
    {
        double[] rgb = toRGB( whitepoint ).getColorValues();
        if( printerColorSpace != null )
        {
            float[] f = printerColorSpace.fromRGB( new float[]{ (float) rgb[ 0 ], (float) rgb[ 1 ], (float) rgb[ 2 ] } );
            if( f != null && f.length == 4 )
            {
                return new CMYK( f );
            }
        }

        double c = 1.0 - rgb[ 0 ];
        double m = 1.0 - rgb[ 1 ];
        double y = 1.0 - rgb[ 2 ];
        double min = Math.min( Math.min( c, m ), y );

        c = ( c - min ) / ( 1 - min );
        m = ( m - min ) / ( 1 - min );
        y = ( y - min ) / ( 1 - min );

        return new CMYK( new double[]{ c, m, y, min } );
    }

}
