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

import java.util.Arrays;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.CIELuv;
import org.color4j.colorimetry.encodings.HunterLab;
import org.color4j.colorimetry.encodings.RGB;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The ColorCalculator is the central place for doing color operations.
 * <p>The ColorCalculator handles all computations and conversions for
 * color objects, including Colorspace conversions, match algorithm,
 * metamerism index and many more color physics units.</p>
 * <p>ColorCalculator2 is going to replace the ColorCalculator over
 * time since ColorCalculator contains a lot of "BAD" code, bad
 * separation of concerns, double[] return values and so on.</p>
 */

public class ColorCalculator2
{
    private static ColorCalculator2 m_Instance;

    static public ColorCalculator2 getInstance()
    {
        if( m_Instance != null )
        {
            return m_Instance;
        }
        synchronized( ColorCalculator2.class )
        {
            if( m_Instance == null )
            {
                m_Instance = new ColorCalculator2();
            }
        }
        return m_Instance;
    }

    private HashMap<String, Class<? extends ColorEncoding>> m_Encodings;

    private ColorCalculator2()
    {
        synchronized( ColorCalculator.class )
        {
            m_Encodings = new HashMap<String, Class<? extends ColorEncoding>>();
            // This can probably be done dynamically by
            // looping through the classes in encodings,
            // by ripping out the URLs of the classloader,
            // look for class resources in encodings, and load them,
            // check for implementing the right interface.
            m_Encodings.put( "XYZ", XYZ.class );            //NOI18N
            m_Encodings.put( "CIELab", CIELab.class );       //NOI18N
            m_Encodings.put( "CIELuv", CIELuv.class );       //NOI18N
            m_Encodings.put( "RGB", RGB.class );           //NOI18N
            m_Encodings.put( "HunterLab", HunterLab.class );    //NOI18N
        }
    }

    /**
     * Weight is calculate by illuminant & observer to use in calculate XYZ
     * <p>which reflectance is > 1nm
     */
    public Weights computeWeights( Illuminant ill, Observer obs, int interval )
    {
        //get the weights of ill & observer given
        //if no standard illuminant, then WeightsCache will call WeightsImpl to generate weight
        Weights w = WeightsCache.getInstance().getWeights( ill, obs, interval );
        return w;
    }

    /**
     * whitepoint is the sum of weights factor for XYZ, it is a combination of
     * illuminant & observer. It is use to convert from XYZ to other color space
     * like CIELab, CIELuv..
     */
    public XYZ computeWhitepoint( Illuminant ill, Observer obs )
    {
        Weights w;
        double[] xyz_wp = new double[ 3 ];

        w = WeightsCache.getInstance().getWeights( ill, obs );

        double[][] w_xyz = { w.getWeightsX(), w.getWeightsY(), w.getWeightsZ() };
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

    public String[] getAlgorithmNames()
    {
        return MatchingFactory.getInstance().getAlgorithmNames();
    }

    public DifferenceAlgorithm getAlgorithm( String name )
    {
        return MatchingFactory.getInstance().getAlgorithm( name );
    }

    public ColorEncoding create( Class encodingclass, Illuminant ill, Reflectance refl, Observer obs )
    {

        try
        {
            Class[] params = new Class[]{ Illuminant.class, Reflectance.class, Observer.class };
            Method create = encodingclass.getMethod( "create", params );    //NOI18N

            if( create == null )
            {
                throw new NoSuchMethodException(
                    "ColorEncoding class does not implement the required create() method." );   //NOI18N
            }
            Object[] args = new Object[]{ ill, refl, obs };
            Object obj = create.invoke( null, args );

            if( !( obj instanceof ColorEncoding ) )
            {
                throw new NoSuchMethodException(
                    "ColorEncoding class does not implement the required create() method." );   //NOI18N
            }
            return (ColorEncoding) obj;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public ColorEncoding convert( Class encodingclass, XYZ xyz, XYZ whitepoint )
    {
        try
        {
            Class[] params = new Class[]{ ColorEncoding.class, XYZ.class };
            Method create = encodingclass.getMethod( "convert", params );   //NOI18N

            if( create == null )
            {
                throw new NoSuchMethodException(
                    "ColorEncoding class does not implement the required create() method." );   //NOI18N
            }
            Object[] args = new Object[]{ xyz, whitepoint };
            Object obj = create.invoke( null, args );

            if( !( obj instanceof ColorEncoding ) )
            {
                throw new NoSuchMethodException(
                    "ColorEncoding class does not implement the required create() method." );   //NOI18N
            }
            return (ColorEncoding) obj;
        }
        catch( NoSuchMethodException e )
        {
            // The ColorEncoding class does not have the
            // convert( ColorEncoding ce, XYZ whitepoint )
            // method.
            e.printStackTrace();
            return null;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getColorEncodingNames()
    {
        // Niclas: Not thread-safe.
        String[] result = new String[m_Encodings.size()];
        m_Encodings.keySet().toArray(result);
        return result;
    }

    public Class getColorEncodingClass( String name )
    {
/*
        try
        {
*/
        Class<? extends ColorEncoding> cls = m_Encodings.get( name );
        return cls;
/* NH: 2002-07-05  Whoever added this needs to rethink their own code.
        }
        catch( Exception e )
        {
            return XYZ.class;
        }
*/
    }
}



