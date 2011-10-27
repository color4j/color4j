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

package org.color4j.tester;

import java.util.HashMap;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.EncodingFactory;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.imports.TextFileReflectanceImporter;
import org.color4j.imports.qtx.ImporterQTX;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseImportQTX extends TestCase
{
    private static final double MAXIMUM_DELTAE_TOLERANCE = 0.12;

    private EncodingFactory factory;
    private TextFileReflectanceImporter m_Importer;
    private InputStream m_Content;
    private String m_Filename;
    private String m_IlluminantName;
    private String m_ObserverName;
    private int m_ColorsInFile;

    private static Logger m_Logger;

    private static String[] m_SingleColorQTXFile =
    {
        "org/color4j/importer/qtx/file1.qtx",
        "org/color4j/importer/qtx/file2.qtx",
        "org/color4j/importer/qtx/file3.qtx",
        "org/color4j/importer/qtx/file4.qtx",
        "org/color4j/importer/qtx/file5.qtx",
        "org/color4j/importer/qtx/file6.qtx",
        "org/color4j/importer/qtx/file7.qtx",
        "org/color4j/importer/qtx/file8.qtx",
        "org/color4j/importer/qtx/file9.qtx",
        "org/color4j/importer/qtx/file10.qtx",
        "org/color4j/importer/qtx/332hs628.qtx",
        "org/color4j/importer/qtx/CARTER1.qtx",
        "org/color4j/importer/qtx/CARTER2.qtx",
        "org/color4j/importer/qtx/CARTER3.qtx",
        "org/color4j/importer/qtx/CARTER TEST.qtx",
        "org/color4j/importer/qtx/CTMAIL3.QTX",
        "org/color4j/importer/qtx/CTMAIL4.qtx",
        "org/color4j/importer/qtx/CTMAIL7.qtx",
        "org/color4j/importer/qtx/CTMAIL.QTX",
        "org/color4j/importer/qtx/color4j.qtx",
        "org/color4j/importer/qtx/next2.qtx",
        "org/color4j/importer/qtx/NEXT_IM.qtx",
        "org/color4j/importer/qtx/nyk test.qtx"
    };

    private static String[] m_IlluminantUsed =
    {
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F11",
        "F2",
        "F2",
        "F2",
        "D65",
        "D65",
        "D65",
        "D65",
        "D65",
        "D65",
        "D65",
        "D65",
        "D65"
    };

    private static String[] m_ObserverUsed =
    {
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964,
        Observer.NAME_CIE1964
    };

    private static int[] m_ExpectedNoOfColors =
    {
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        14,
        2,
        2,
        2,
        1,
        9,
        1,
        12,
        8,
        435,
        13,
        12,
        2
    };

    public TestCaseImportQTX(String name, InputStream content, String illuminant, String observer, int colorsInFile, String filename)
    {
        super( name );
        m_Importer = new ImporterQTX();
        m_Content = content;
        m_IlluminantName = illuminant;
        m_ObserverName = observer;
        m_ColorsInFile = colorsInFile;
        m_Filename = filename;
    }

    public static Test suite()
    {
        ClassLoader cl = TestCaseImportQTX.class.getClassLoader();
        m_Logger = LoggerFactory.getLogger( TestCaseImportQTX.class );
        TestSuite suite = new TestSuite();

        for( int i=0 ; i < m_SingleColorQTXFile.length ; i++ )
        {
            InputStream in = cl.getResourceAsStream( m_SingleColorQTXFile[i] );
            String ill = m_IlluminantUsed[i];
            String obs = m_ObserverUsed[i];
            int colorsInFile = m_ExpectedNoOfColors[i];
            suite.addTest( new TestCaseImportQTX( "test_ImportColors", in, ill, obs, colorsInFile, m_SingleColorQTXFile[i] ) );
        }
        return suite;
    }

    public void test_ImportColors()
        throws Exception
    {
        if( m_Content == null )
            fail( "Could not find the Test QTX file." );

        Reflectance[] refls = m_Importer.doImport( m_Content, new HashMap<String, String>() );
        assertEquals( "Wrong numbers of Colors parsed in file:" + m_Filename, m_ColorsInFile, refls.length );

        Observer obs = ObserverImpl.create( m_ObserverName );
        Illuminant ill = IlluminantImpl.create( m_IlluminantName );
        XYZ wp = factory.createWhitePoint( ill, obs );
        XYZ expected;
        for( Reflectance r : refls )
        {
            XYZ xyz = factory.createXYZ( ill, r, obs );

            CIELab lab_sample = factory.createCIELab(ill, r, obs );
            try
            {
                if( r.hasProperty( "STD_X" ) )
                {
                    double x = getValue( r, "STD_X" );
                    double y = getValue( r, "STD_Y" );
                    double z = getValue( r, "STD_Z" );
                    expected = new XYZ( x, y, z );
                }
                else
                {
                    double x = getValue( r, "BAT_X" );
                    double y = getValue( r, "BAT_Y" );
                    double z = getValue( r, "BAT_Z" );
                    expected = new XYZ( x, y, z );
                }

                CIELab lab_ref = expected.toCIELab( wp );

                DifferenceAlgorithm algo = MatchingFactory.getInstance().getDefaultAlgorithm();

                ColorDifference cdiff = algo.compute( lab_sample, lab_ref );
                double diff = cdiff.getValue( "DeltaE" );
                m_Logger.debug( "DeltaE=" + diff );
                assertTrue( "DeltaE [" + diff + "] is larger than allowed for '" + r.getName() + "' in: " + m_Filename + "  XYZ:" + xyz
                    .toString(), diff < MAXIMUM_DELTAE_TOLERANCE );
            }
            catch( NoSuchFieldException e )
            {
                // QTX file does not contain XYZ values. Ignore
            }
        }
    }

//    private Reflectance retrieveSingle( InputStream in )
//        throws Exception
//    {
//        Reflectance[] refls = m_Importer.doImport( in );
//        assertEquals( 1, refls.length );
//        return refls[0];
//    }

    private double getValue( Reflectance r, String name )
        throws Exception
    {
        String s = (String) r.getProperty( name );
        if( s == null )
            throw new NoSuchFieldException();
        int pos1 = 0;
        for( int i=0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt(i);
            if(  ch <= 57 && ch >= 48 )
            {
                pos1 = i;
                break;
            }
        }
        s = s.substring( pos1 );
        int pos2 = s.length()-1;
        for( int i=pos1 ; i < s.length() ; i++ )
        {
            if( s.charAt(i) > 57 && s.charAt(i) < 48 )
            {
                pos2 = i;
                break;
            }
        }
        s = s.substring( 0, pos2 );
        return Double.parseDouble( s );
    }
}
