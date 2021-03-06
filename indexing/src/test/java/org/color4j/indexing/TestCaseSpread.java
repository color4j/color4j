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

package org.color4j.indexing;

import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.ReflectanceImpl;
import org.color4j.colorimetry.Spectrum;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.DefaultEncodingFactory;
import org.color4j.colorimetry.encodings.EncodingFactory;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestCaseSpread
{
    EncodingFactory factory = new DefaultEncodingFactory();

    @Test
    public void testOrange()
        throws Exception
    {
        doAnIlluminant( rorange64 );
    }

    private void doAnIlluminant( float[] data )
        throws Exception
    {
        String[] obsName = { Observer.NAME_CIE1931, Observer.NAME_CIE1964 };
        String[] illName = IlluminantImpl.getStandardIlluminantNames();
        Reflectance refl = createReflectance( data );
        double maxL = -200;
        double minL = 200;
        double maxa = -200;
        double mina = 200;
        double maxb = -200;
        double minb = 200;
        CIELab[] all = new CIELab[ obsName.length * illName.length ];
        for( int i = 0; i < obsName.length; i++ )
        {
            Observer obs = ObserverImpl.create( obsName[ i ] );
            for( int j = 0; j < illName.length; j++ )
            {
                Illuminant ill = IlluminantImpl.create( illName[ j ] );
                all[ j * 2 + i ] = factory.createCIELab( ill, refl, obs );
                CIELab lab = all[ j * 2 + i ];
                if( maxL < lab.getL() )
                {
                    maxL = lab.getL();
                }
                if( minL > lab.getL() )
                {
                    minL = lab.getL();
                }
                if( maxa < lab.geta() )
                {
                    maxa = lab.geta();
                }
                if( mina > lab.geta() )
                {
                    mina = lab.geta();
                }
                if( maxb < lab.getb() )
                {
                    maxb = lab.getb();
                }
                if( minb > lab.getb() )
                {
                    minb = lab.getb();
                }
            }
        }
        CIELab center = new CIELab( ( maxL - minL ) / 2 + minL, ( maxa - mina ) / 2 + mina, ( maxb - minb ) / 2 + minb );
        double maxRadius = 0;
        for( CIELab sample : all )
        {
            double dl = center.getL() - sample.getL();
            double da = center.geta() - sample.geta();
            double db = center.getb() - sample.getb();
            double radius = Math.sqrt( dl * dl + da * da + db * db );
            if( radius > maxRadius )
            {
                maxRadius = radius;
            }
        }

        String[] algoNames = MatchingFactory.getInstance().getAlgorithmNames();
        for( String algoName : algoNames )
        {
            if( algoName.equals( MatchingFactory.HUNTERDE ) )
            {
                continue;
            }
            if( algoName.equals( MatchingFactory.LUVDE ) )
            {
                continue;
            }
            DifferenceAlgorithm algo = MatchingFactory.getInstance().getAlgorithm( algoName );
            double maxDE = 0;
            for( CIELab sample : all )
            {
                ColorDifference cd = algo.compute( center, sample );
                double de = cd.getValue( ColorDifference.DELTA_E );
                if( de > maxDE )
                {
                    maxDE = de;
                }
            }
            assertTrue( "MaxDE = " + maxDE + " and maxRadius = " + maxRadius, maxDE <= maxRadius ); //NOI18N
        }
    }

    float[] rorange64 = {
        0.72786f, 0.50664f, 0.24846f, 0.08339f, 0.04372f,
        0.03615f, 0.03494f, 0.0341f, 0.03257f, 0.02957f,
        0.0268f, 0.02551f, 0.02551f, 0.02563f, 0.02721f,
        0.03160f, 0.04174f, 0.05861f, 0.0798f, 0.11745f,
        0.19014f, 0.30765f, 0.44784f, 0.5864f, 0.70951f,
        0.7887f, 0.83256f, 0.85749f, 0.87648f, 0.89065f,
        0.88982f, 0.8903f, 0.89331f
    };

    private Reflectance createReflectance( float[] spectra )
        throws Exception
    {
        Spectrum spectrum = Spectrum.create( 380, 10, spectra );
        return ReflectanceImpl.create( spectrum );
    }
}



