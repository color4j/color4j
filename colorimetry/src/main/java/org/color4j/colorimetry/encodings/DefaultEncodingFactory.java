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
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.Spectrum;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.math.Maths;
import org.color4j.colorimetry.weights.WeightsCache;
import org.color4j.colorimetry.illuminants.IlluminantImpl;

public class DefaultEncodingFactory
    implements EncodingFactory
{
    @SuppressWarnings( { "unchecked" } )
    @Override
    public <T extends ColorEncoding> T create( Class<T> encodingType,
                                               Illuminant illuminant,
                                               Reflectance reflectance,
                                               Observer observer
    )
    {
        if( encodingType.equals( CIELab.class ) )
        {
            return (T) createCIELab( illuminant, reflectance, observer );
        }
        else if( encodingType.equals( CIELuv.class ) )
        {
            return (T) createCIELuv( illuminant, reflectance, observer );
        }
        else if( encodingType.equals( CMYK.class ) )
        {
            return (T) createCMYK( illuminant, reflectance, observer );
        }
        else if( encodingType.equals( Din99Lab.class ) )
        {
            return (T) createDin99Lab( illuminant, reflectance, observer, 1.0, 0 );
        }
        else if( encodingType.equals( HunterLab.class ) )
        {
            return (T) createHunterLab( illuminant, reflectance, observer );
        }
        else if( encodingType.equals( RGB.class ) )
        {
            return (T) createRGB( illuminant, reflectance, observer );
        }
        else if( encodingType.equals( XYZ.class ) )
        {
            return (T) createXYZ( illuminant, reflectance, observer );
        }
        else
        {
            throw new ColorException( "Unsupported ColorEncoding type: " + encodingType.getName() );
        }
    }

    @Override
    public CIELab createCIELab( Illuminant illuminant, Reflectance reflectance, Observer observer )
    {
        XYZ xyz = createXYZ( illuminant, reflectance, observer );
        XYZ whitepoint = createWhitePoint( illuminant, observer );
        return xyz.toCIELab( whitepoint );
    }

    @Override
    public CIELuv createCIELuv( Illuminant illuminant, Reflectance reflectance, Observer observer )
    {
        XYZ xyz = createXYZ( illuminant, reflectance, observer );
        XYZ whitepoint = createWhitePoint( illuminant, observer );
        return xyz.toCIELuv( whitepoint );
    }

    @Override
    public CMYK createCMYK( Illuminant illuminant, Reflectance reflectance, Observer observer )
    {
        XYZ xyz = createXYZ( illuminant, reflectance, observer );
        XYZ whitepoint = createWhitePoint( illuminant, observer );
        return xyz.toCMYK( whitepoint );
    }

    @Override
    public Din99Lab createDin99Lab( Illuminant illuminant,
                                    Reflectance reflectance,
                                    Observer observer,
                                    double ke,
                                    double kch
    )
    {
        XYZ xyz = createXYZ( illuminant, reflectance, observer );
        XYZ whitepoint = createWhitePoint( illuminant, observer );
        return xyz.toCIELab( whitepoint ).toDin99Lab( ke, kch );
    }

    @Override
    public HunterLab createHunterLab( Illuminant illuminant, Reflectance reflectance, Observer observer )
    {
        XYZ xyz = createXYZ( illuminant, reflectance, observer );
        XYZ whitepoint = createWhitePoint( illuminant, observer );
        return xyz.toHunterLab( whitepoint );
    }

    @Override
    public RGB createRGB( Illuminant illuminant, Reflectance reflectance, Observer observer )
    {
        XYZ xyz = createXYZ( illuminant, reflectance, observer );
        XYZ whitepoint = createWhitePoint( illuminant, observer );
        Illuminant illD65 = IlluminantImpl.create( "D65" );     //NOI18N
        XYZ wpD65 = WeightsCache.getInstance().getWeights( illD65, observer ).toWhitePoint();
        return xyz.toRGB( whitepoint, wpD65 );
    }

    @Override
    public XYZ createXYZ( Illuminant illuminant, Reflectance reflectance, Observer observer )
    {
        if( illuminant == null )
        {
            throw new IllegalArgumentException( "A null parameter for Illuminant is not allowed." );   //NOI18N
        }
        if( observer == null )
        {
            throw new IllegalArgumentException( "A null parameter for Observer is not allowed." );     //NOI18N
        }
        if( reflectance == null )
        {
            throw new IllegalArgumentException( "A null parameter for Reflectance is not allowed." );  //NOI18N
        }

        // return value
        double rt_value[] = { 0.0, 0.0, 0.0 };
        //use to figure out the start & ending nm so that they can be same
        int start_r, end_r, start_w, end_w;
        //detect the reflectance interval
        Spectrum spectrum = reflectance.getSpectrum();
        if( spectrum == null )
        {
            throw new ColorException( "Reflectance " + reflectance.getName() + " does not have a Spectrum." );     //NOI18N
        }
        int interval = spectrum.getInterval();

        //get the weights of ill & observer given
        Weights w = WeightsCache.getInstance().getWeights( illuminant, observer, interval );

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

        double[] r_xyz = Maths.getSameIntervalR( reflectance.getSpectrum().getValues(), start_r, end_r );
        double[] w_x = Maths.getSameIntervalW( w.getWeightsX(), start_w, end_w );
        double[] w_y = Maths.getSameIntervalW( w.getWeightsY(), start_w, end_w );
        double[] w_z = Maths.getSameIntervalW( w.getWeightsZ(), start_w, end_w );

        double length = ( w_x.length <= r_xyz.length ) ? w_x.length : r_xyz.length;
        for( int j = 0; j < length; j++ )
        {
            rt_value[ 0 ] += w_x[ j ] * r_xyz[ j ];
            rt_value[ 1 ] += w_y[ j ] * r_xyz[ j ];
            rt_value[ 2 ] += w_z[ j ] * r_xyz[ j ];
        }
        return new XYZ( rt_value[ 0 ], rt_value[ 1 ], rt_value[ 2 ] );
    }

    @Override
    public XYZ createWhitePoint( Illuminant illuminant, Observer observer )
    {
        Weights weights = WeightsCache.getInstance().getWeights( illuminant, observer );
        return weights.toWhitePoint();
    }

    @Override
    public Chromacity createChromacity( XYZ xyz )
    {
        double x = xyz.getX();
        double y = xyz.getY();
        double z = xyz.getZ();
        double sum = x + y + z;
        if( sum == 0.0 )
        {
            x = 0.0;
            y = 0.0;
        }
        else
        {
            x = x / sum;
            y = y / sum;
        }
        return new Chromacity( x, y );
    }

}
