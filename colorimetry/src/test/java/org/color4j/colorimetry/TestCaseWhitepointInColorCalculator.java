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

import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import junit.framework.TestCase;
import org.color4j.colorimetry.weights.WeightsCache;

public class TestCaseWhitepointInColorCalculator extends TestCase
{
    /**
     * The maximum allowed difference for each component
     * (x, Y, Z) between the prepared XYZ and the computed
     * XYZ from Reflectance data.
     */
    private static final double ALLOWED_XYZ_DEVIANCE = 0.1;

    private WhitepointCase[] m_TestWhitepoints;

    public TestCaseWhitepointInColorCalculator( String name )
    {
        super( name );
        initialize();
    }

    public void initialize()
    {
        // Nothing generic to initialize, now.
    }

    public void testWhitepoint()
        throws Exception
    {
        initWhitepointCases();
        for( int i = 0; i < m_TestWhitepoints.length; i++ )
        {
            Illuminant ill = IlluminantImpl.create( m_TestWhitepoints[ i ].getill() );
            Observer obs = ObserverImpl.create( m_TestWhitepoints[ i ].getobs() );
            Weights weights = WeightsCache.getInstance().getWeights( ill, obs );
            XYZ whitepoint = weights.toWhitePoint();
            m_TestWhitepoints[ i ].validate( whitepoint, m_TestWhitepoints[ i ].getwp() );
        }
    }

    private class WhitepointCase
    {
        private String m_ill;
        private String m_obs;
        private XYZ m_wp;

        WhitepointCase( String ill, String obs, XYZ ewp )
            throws ColorException
        {
            m_ill = ill;
            m_obs = obs;
            m_wp = ewp;
        }

        void validate( XYZ cxyz, XYZ exyz )
        {
            assertTrue( "Whitepoint X is not within limit. Expected " + exyz.getX() + " got " + cxyz.getX(),  Math.abs( exyz.getX() - exyz.getX() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "Whitepoint Y is not within limit. Expected " + exyz.getY() + " got " + cxyz.getY(),  Math.abs( exyz.getY() - exyz.getY() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "Whitepoint Z is not within limit. Expected " + exyz.getZ() + " got " + cxyz.getZ(),  Math.abs( exyz.getZ() - exyz.getZ() ) < ALLOWED_XYZ_DEVIANCE );
        }

        String getill()
        {
            return m_ill;
        }

        String getobs()
        {
            return m_obs;
        }

        XYZ getwp()
        {
            return m_wp;
        }
    }

    private void initWhitepointCases()
        throws ColorException
    {
        XYZ A2d = new XYZ( 109.85, 100.0, 35.585 );
        XYZ A10d = new XYZ( 111.144, 100.0, 35.2 );
        XYZ C2d = new XYZ( 98.07, 100.0, 118.23 );
        XYZ C10d = new XYZ( 97.28, 100.0, 116.14 );
        XYZ D652d = new XYZ( 95.047, 100.0, 108.883 );
        XYZ D6510d = new XYZ( 94.811, 100.0, 107.304 );
        XYZ D502d = new XYZ( 96.422, 100.0, 82.521 );
        XYZ D5010d = new XYZ( 96.72, 100.0, 81.427 );
        XYZ F22d = new XYZ( 99.186, 100.0, 67.393 );
        XYZ F210d = new XYZ( 103.279, 100.0, 69.027 );

        WhitepointCase wp1 = new WhitepointCase
            (
                "A", Observer.NAME_CIE1931, A2d
            );
        //cmc(2:1)
        WhitepointCase wp2 = new WhitepointCase
            (
                "A", Observer.NAME_CIE1964, A10d
            );
        WhitepointCase wp3 = new WhitepointCase
            (
                "C", Observer.NAME_CIE1931, C2d
            );
        WhitepointCase wp4 = new WhitepointCase
            (
                "C", Observer.NAME_CIE1964, C10d
            );
        WhitepointCase wp5 = new WhitepointCase
            (
                "D65", Observer.NAME_CIE1931, D652d
            );
        WhitepointCase wp6 = new WhitepointCase
            (
                "D65", Observer.NAME_CIE1964, D6510d
            );
        WhitepointCase wp7 = new WhitepointCase
            (
                "D50", Observer.NAME_CIE1931, D502d
            );
        WhitepointCase wp8 = new WhitepointCase
            (
                "D50", Observer.NAME_CIE1964, D5010d
            );
        WhitepointCase wp9 = new WhitepointCase
            (
                "F2", Observer.NAME_CIE1931, F22d
            );
        WhitepointCase wp10 = new WhitepointCase
            (
                "F2", Observer.NAME_CIE1964, F210d
            );

        /*
        //cmc(2:1)
        WhitepointCase de3 =  new WhitepointCase
                                   (
                                       xyzOrangeB,xyzOrange46,white,2,2.0,1.65
                                   );
        //cie1994
        WhitepointCase de4 =  new WhitepointCase
                                   (
                                       xyzOrangeB,xyzOrange46,white,0,1.0,1.53
                                   );
        //cmc(1:1)
        WhitepointECase de5 =  new WhitepointCase
                                   (
                                       xyzOrangeB,xyzOrange46,white,2,1.0,1.86
                                   );
        */
        //all the cases testing
        m_TestWhitepoints = new WhitepointCase[]
            {
                wp1, wp2, wp3, wp4, wp5, wp6, wp7, wp8, wp9, wp10
            };
    }
}
