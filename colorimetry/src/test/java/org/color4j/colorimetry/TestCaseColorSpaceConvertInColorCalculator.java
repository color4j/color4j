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

import junit.framework.TestCase;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.CIELuv;
import org.color4j.colorimetry.encodings.HunterLab;
import org.color4j.colorimetry.encodings.XYZ;

public class TestCaseColorSpaceConvertInColorCalculator extends TestCase
{
    /**
     * The maximum allowed difference for each component
     * (x, Y, Z) between the prepared XYZ and the computed
     * XYZ from Reflectance data.
     */
    private static final double ALLOWED_XYZ_DEVIANCE = 0.1;

    private CIELabColorSpaceCase[] m_TestCIELabColorSpaces;
    private CIELuvColorSpaceCase[] m_TestCIELuvColorSpaces;
    private HunterColorSpaceCase[] m_TestHunterColorSpaces;

    public TestCaseColorSpaceConvertInColorCalculator( String name )
    {
        super( name );
        initialize();
    }

    public void initialize()
    {
        // Nothing generic to initialize, now.
    }

    public void testColorSpaceConvert()
        throws Exception
    {
        initColorSpaceCases();
//		@TODO: Clean up. ML - 05/08/2003
//        XYZ whiteD6510d = new XYZ(94.811,100.0,107.304);
        for( int i = 0; i < m_TestCIELabColorSpaces.length; i++ )
        {
            XYZ temp = m_TestCIELabColorSpaces[ i ].getXYZ();
            XYZ white = m_TestCIELabColorSpaces[ i ].getwhite();
            CIELab lab;
            //use difference method to test Lab
            if( i < 3 )
            {
                lab = temp.toCIELab( white );
            }
            else
            {
                lab = temp.toCIELab( white );
                assertTrue( "ColorEncoding unable to create CIELab from text.", lab != null );
            }
            //use difference method to test Luv
            m_TestCIELabColorSpaces[ i ].validate( lab );
        }

        for( int i = 0; i < m_TestCIELuvColorSpaces.length; i++ )
        {
            XYZ temp = m_TestCIELuvColorSpaces[ i ].getXYZ();
            XYZ white = m_TestCIELuvColorSpaces[ i ].getwhite();
            CIELuv luv;
            //use difference method to test Lab
            if( i < 3 )
            {
                luv = temp.toCIELuv( white );
            }
            else
            {
                luv = temp.toCIELuv( white );
                assertTrue( "ColorEncoding unable to create CIELuv from text.", luv != null );
            }
            //use difference method to test Luv

            m_TestCIELuvColorSpaces[ i ].validate( luv );
        }

        for( int i = 0; i < m_TestHunterColorSpaces.length; i++ )
        {
            XYZ temp = m_TestHunterColorSpaces[ i ].getXYZ();
            XYZ white = m_TestHunterColorSpaces[ i ].getwhite();
            HunterLab hlab;
            //use difference method to test Lab
            if( i < 3 )
            {
                hlab = temp.toHunterLab( white );
            }
            else
            {
                hlab = temp.toHunterLab( white );
                assertTrue( "ColorEncoding unable to create HunterLab from text.", hlab != null );
            }
            //use difference method to test HunterLab

            m_TestHunterColorSpaces[ i ].validate( hlab );
        }
    }

    private class HunterColorSpaceCase
    {
        private XYZ m_xyz;
        private XYZ m_white;
        private HunterLab m_Lab;

        HunterColorSpaceCase( XYZ test, XYZ white, HunterLab elab )
        {
            m_xyz = test;
            m_white = white;
            m_Lab = elab;
        }

        void validate( HunterLab vlab )
        {
            assertTrue( "HunterLab L is not within limit. Expected " + m_Lab.getL() + " got " + vlab.getL(), Math.abs( vlab.getL() - m_Lab
                .getL() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "HunterLab a is not within limit. Expected " + m_Lab.geta() + " got " + vlab.geta(), Math.abs( vlab.geta() - m_Lab
                .geta() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "HunterLab b is not within limit. Expected " + m_Lab.getb() + " got " + vlab.getb(), Math.abs( vlab.getb() - m_Lab
                .getb() ) < ALLOWED_XYZ_DEVIANCE );
        }

        XYZ getXYZ()
        {
            return m_xyz;
        }

        HunterLab getlab()
        {
            return m_Lab;
        }

        XYZ getwhite()
        {
            return m_white;
        }
    }

    private class CIELabColorSpaceCase
    {
        private XYZ m_xyz;
        private XYZ m_white;
        private CIELab m_Lab;

        /*test is the sample color XYZ
        *white is the whitepoint, like D65 10d observer
        *elab is the expected lab value of the test object
        *eluv is the expected luv value of the test object
        */
        CIELabColorSpaceCase( XYZ test, XYZ white, CIELab elab )
            throws ColorException
        {
            m_xyz = test;
            m_Lab = elab;
            m_white = white;
        }

        void validate( CIELab clab )
        {
            assertTrue( "CIELab L* is not within limit. Expected " + m_Lab.getL() + " got " + clab.getL(), Math.abs( clab.getL() - m_Lab
                .getL() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "CIELab a* is not within limit. Expected " + m_Lab.geta() + " got " + clab.geta(), Math.abs( clab.geta() - m_Lab
                .geta() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "CIELab b* is not within limit. Expected " + m_Lab.getb() + " got " + clab.getb(), Math.abs( clab.getb() - m_Lab
                .getb() ) < ALLOWED_XYZ_DEVIANCE );
        }

        XYZ getXYZ()
        {
            return m_xyz;
        }

        CIELab getlab()
        {
            return m_Lab;
        }

        XYZ getwhite()
        {
            return m_white;
        }
    }

    private class CIELuvColorSpaceCase
    {
        private XYZ m_xyz;
        private XYZ m_white;
        private CIELuv m_Luv;

        /*test is the sample color XYZ
        *white is the whitepoint, like D65 10d observer
        *elab is the expected lab value of the test object
        *eluv is the expected luv value of the test object
        */
        CIELuvColorSpaceCase( XYZ test, XYZ white, CIELuv eluv )
            throws ColorException
        {
            m_xyz = test;
            m_Luv = eluv;
            m_white = white;
        }

        void validate( CIELuv cluv )
        {
            assertTrue( "CIELuv L is not within limit. Expected " + m_Luv.getL() + " got " + cluv.getL(),
                        Math.abs( m_Luv.getL() - cluv.getL() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "CIELuv u is not within limit. Expected " + m_Luv.getu() + " got " + cluv.getu(),
                        Math.abs( m_Luv.getu() - cluv.getu() ) < ALLOWED_XYZ_DEVIANCE );
            assertTrue( "CIELuv v is not within limit. Expected " + m_Luv.getv() + " got " + cluv.getv(),
                        Math.abs( m_Luv.getv() - cluv.getv() ) < ALLOWED_XYZ_DEVIANCE );
        }

        XYZ getXYZ()
        {
            return m_xyz;
        }

        CIELuv getluv()
        {
            return m_Luv;
        }

        XYZ getwhite()
        {
            return m_white;
        }
    }

    private void initColorSpaceCases()
        throws ColorException
    {
        XYZ xyz6015054 = new XYZ( 16.659, 15.29, 10.746 );
        XYZ xyz6015072 = new XYZ( 17.505, 16.58, 10.938 );
        XYZ xyzOrange46D6510d = new XYZ( 29.63, 17.57, 3.87 );
        XYZ xyzOrange46A10d = new XYZ( 47.79, 26.06, 1.2 );
        XYZ xyzOrangeBD6510d = new XYZ( 28.5, 16.73, 4.01 );
        XYZ xyzOrangeBA10d = new XYZ( 45.76, 24.88, 1.27 );

        XYZ white = new XYZ( 94.811, 100.0, 107.304 );
        XYZ whiteA10 = new XYZ( 111.144, 100.0, 35.2 );

        CIELabColorSpaceCase cs0 = new CIELabColorSpaceCase
            (
                xyz6015054, white, new CIELab( 46.029, 12.68, 14.071 )
            );
        CIELabColorSpaceCase cs1 = new CIELabColorSpaceCase
            (
                xyz6015072, white, new CIELab( 47.727, 10.025, 16.447 )
            );
        CIELabColorSpaceCase cs2 = new CIELabColorSpaceCase
            (
                xyzOrange46D6510d, white, new CIELab( 48.97, 59.26, 45.96 )
            );
        CIELabColorSpaceCase cs3 = new CIELabColorSpaceCase
            (
                xyzOrange46A10d, whiteA10, new CIELab( 58.09, 58.0, 62.95 )
            );
        CIELabColorSpaceCase cs4 = new CIELabColorSpaceCase
            (
                xyzOrangeBD6510d, white, new CIELab( 47.91, 59.45, 43.31 )
            );
        CIELabColorSpaceCase cs5 = new CIELabColorSpaceCase
            (
                xyzOrangeBA10d, whiteA10, new CIELab( 56.96, 57.5, 59.7 )
            );

        m_TestCIELabColorSpaces = new CIELabColorSpaceCase[]
            {
                cs0, cs1, cs2, cs3, cs4, cs5
            };

        HunterColorSpaceCase hcs0 = new HunterColorSpaceCase
            (
                xyz6015054, white, new HunterLab( 39.103, 10.036, 9.004 )
            );
        HunterColorSpaceCase hcs1 = new HunterColorSpaceCase
            (
                xyz6015072, white, new HunterLab( 40.719, 7.956, 10.467 )
            );
        m_TestHunterColorSpaces = new HunterColorSpaceCase[]
            {
                hcs0, hcs1
            };

        CIELuvColorSpaceCase vcs0 = new CIELuvColorSpaceCase
            (
                xyz6015054, white, new CIELuv( 46.029, 24.904, 14.966 )
            );
        CIELuvColorSpaceCase vcs1 = new CIELuvColorSpaceCase
            (
                xyz6015072, white, new CIELuv( 47.727, 22.52, 18.292 )
            );
        CIELuvColorSpaceCase vcs2 = new CIELuvColorSpaceCase
            (
                xyzOrange46D6510d, white, new CIELuv( 48.97, 121.59, 31.36 )
            );
        CIELuvColorSpaceCase vcs3 = new CIELuvColorSpaceCase
            (
                xyzOrange46A10d, whiteA10, new CIELuv( 58.09, 130.83, 4.56 )
            );
        CIELuvColorSpaceCase vcs4 = new CIELuvColorSpaceCase
            (
                xyzOrangeBD6510d, white, new CIELuv( 47.91, 120.38, 29.29 )
            );
        CIELuvColorSpaceCase vcs5 = new CIELuvColorSpaceCase
            (
                xyzOrangeBA10d, whiteA10, new CIELuv( 56.96, 128.83, 4.0 )
            );
        m_TestCIELuvColorSpaces = new CIELuvColorSpaceCase[]
            {
                vcs0, vcs1, vcs2, vcs3, vcs4, vcs5
            };
    }
}
