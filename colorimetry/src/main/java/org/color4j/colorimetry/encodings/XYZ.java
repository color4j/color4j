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
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.math.Matrix;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.colorimetry.weights.WeightsCache;

/**
 * Value container for XYZ values.
 * <p>This class holds the XYZ values, irrespective of the Standard Observer.</p>
 * <p>The sole purpose of this class is to conveniently encapsulate the XYZ values
 * into a manageable entity.</p>
 */
public class XYZ
    implements ColorEncoding
{
    public static final double D50X = 96.42;
    public static final double D50Y = 100.0;
    public static final double D50Z = 82.49;

    public static XYZ WHITEPOINT_D65_10;


    private final double x;
    private final double y;
    private final double z;

    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    protected boolean m_InGamut = true;

    /**
     * Constructor using indivdual doubles.
     * @param x The X value
     * @param y The Y value
     * @param z The Z value
     */
    public XYZ( double x, double y, double z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return X value
     */
    public double getX()
    {
        return x;
    }

    /**
     * @return Y value
     */
    public double getY()
    {
        return y;
    }

    /**
     * @return Z value
     */
    public double getZ()
    {
        return z;
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

    public CIELab toCIELab( XYZ whitepoint )
    {
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
        double lStar = 116.0 * FN[ 1 ] - 16.0;
        double aStar = 500.0 * ( FN[ 0 ] - FN[ 1 ] );
        double bStar = 200.0 * ( FN[ 1 ] - FN[ 2 ] );
        return new CIELab( lStar, aStar, bStar );
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
            return toRGB( whitepoint, WHITEPOINT_D65_10 );
        }
    }

    public HunterLab toHunterLab( XYZ whitepoint )
    {
        double fnX = getX() / whitepoint.getX();
        double fnY = getY() / whitepoint.getY();
        double fnZ = getZ() / whitepoint.getZ();

        double l = 100.0 * Math.pow( fnY, .5 );
        double a = 175.0 * ( fnX - fnY ) * Math.pow( .0102 * whitepoint.getX() / fnY, .5 );
        double b = .4 * 175.0 * ( fnY - fnZ ) * Math.pow( .00847 * whitepoint.getZ() / fnY, .5 );
        return new HunterLab( l, a, b );
    }

    public RGB toRGB( XYZ whitepoint, XYZ whitepointD65 )//XYZ whitepointD50
    {
        double temp[] = new double[ 3 ];
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
        double tempX = getX() * whitepointD65.getX() / whitepoint.getX();
        double tempY = getY() * whitepointD65.getY() / whitepoint.getY();
        double tempZ = getZ() * whitepointD65.getZ() / whitepoint.getZ();

        // KH - Aug 25, 2004 : adapting to use static matrix
        double r = RGBTristimulus[ 0 ][ 0 ] * tempX + RGBTristimulus[ 0 ][ 1 ] * tempY + RGBTristimulus[ 0 ][ 2 ] * tempZ;
        r = r / 100.0;
        double g = RGBTristimulus[ 1 ][ 0 ] * tempX + RGBTristimulus[ 1 ][ 1 ] * tempY + RGBTristimulus[ 1 ][ 2 ] * tempZ;
        g = g / 100.0;
        double b = RGBTristimulus[ 2 ][ 0 ] * tempX + RGBTristimulus[ 2 ][ 1 ] * tempY + RGBTristimulus[ 2 ][ 2 ] * tempZ;
        b = b / 100.0;

        // KH - Dec 26, 2004 : obviously, if rgb[i] ends up to be > 1, it is out of gamut
        // if it is less than 0
        r = checkBoundaries( r );
        g = checkBoundaries( g );
        b = checkBoundaries( b );
        return new RGB( r, g, b );
    }

    private double checkBoundaries( double value )
    {
        if( value < 0.0 )
        {
            value = 0.0;
        }
        else if( value > 1.0 )
        {
            value = 1.0;
        }

        if( value <= 0.0031308 )  // HanSoong said; 0.00304
        {
            value = 12.92 * value;
        }
        else
        {
            value = ( 1.055 * Math.pow( value, 1.0 / 2.4 ) - 0.055 );
        }
        return value;
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
    public CIELuv toCIELuv( XYZ whitepoint )
    {
        //the return value
        double IU, IV, denom, D, SU, SV;

        denom = whitepoint.getX() + 15.0 * whitepoint.getY() + 3.0 * whitepoint.getZ();

        IU = 4.0 * whitepoint.getX() / denom;
        IV = 9.0 * whitepoint.getY() / denom;
        D = getX() + 15.0 * getY() + 3.0 * getZ();
        SU = 4.0 * getX() / D;
        SV = 9.0 * getY() / D;
        double l = getY() / whitepoint.getY();

        if( l <= 0.008856 )
        {
            l = 903.292 * l;
        }
        else
        {
            l = 116.0 * Math.pow( l, 1.0 / 3.0 ) - 16.0;
        }

        double u = 13.0 * l * ( SU - IU );
        double v = 13.0 * l * ( SV - IV );
        return new CIELuv( l,  u, v);
    }

    public CMYK toCMYK( XYZ whitepoint, ICC_ColorSpace printerColorSpace )
    {
        RGB rgb = toRGB( whitepoint );
        if( printerColorSpace != null )
        {
            float[] f = printerColorSpace.fromRGB( new float[]{ (float) rgb.getR(), (float) rgb.getG(), (float) rgb.getB() } );
            if( f != null && f.length == 4 )
            {
                return new CMYK( f[ 0 ], f[ 1 ], f[ 2 ], f[ 3 ] );
            }
        }

        double c = 1.0 - rgb.getR();
        double m = 1.0 - rgb.getG();
        double y = 1.0 - rgb.getB();
        double min = Math.min( Math.min( c, m ), y );

        c = ( c - min ) / ( 1 - min );
        m = ( m - min ) / ( 1 - min );
        y = ( y - min ) / ( 1 - min );

        return new CMYK( c, m, y, min );
    }

    public boolean isInGamut()
    {
        return m_InGamut;
    }

    private static float[][] RGBTristimulus;

    private static final Matrix INVERTED_TRISTIMULUS;

    static
    {
        RGBTristimulus = new float[][]
            {
                new float[]{ 3.2406f, -1.5372f, -0.4986f, 0 },
                new float[]{ -0.9689f, 1.8758f, 0.0415f, 0 },
                new float[]{ 0.0557f, -0.204f, 1.057f, 0 },
                new float[]{ 0, 0, 0, 1 }
            };
        INVERTED_TRISTIMULUS = new Matrix( RGBTristimulus ).inverse();
        try
        {
            Illuminant d65 = IlluminantImpl.create( "D65" );  //NOI18N
            Observer obs = ObserverImpl.create( Observer.NAME_CIE1964 );
            Weights weights = WeightsCache.getInstance().getWeights( d65, obs );
            WHITEPOINT_D65_10 = weights.toWhitePoint();
        }
        catch( Exception e )
        {
            e.printStackTrace(); // Shouldn't happen
            InternalError internalError = new InternalError( "Color system malfunction." );
            internalError.initCause( e );
            throw internalError;  //NOI18N
        }

    }

    public static Matrix getInvertedTristimulusMatrix()
    {
        return INVERTED_TRISTIMULUS;
    }
}
