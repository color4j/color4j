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

/*
 * TestCaseImportXTF.java
 *
 * Created on May 9, 2003, 3:16 PM
 */

package org.color4j.tester;

import java.util.HashMap;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.imports.ImportException;
import org.color4j.imports.TextFileReflectanceImporter;
import org.color4j.imports.xtf.XTFParserContext;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
//@TODO: Clean up. ML - 05/08/2003
public class TestCaseImportXTF extends TestCase
{
//    private static final double MAXIMUM_DELTAE_TOLERANCE = 0.12;

    private TextFileReflectanceImporter m_Importer;
    private InputStream m_Content;
    private String m_Filename;
    private int m_ColorsInFile;

    private static Logger m_Logger;

    private static String[] m_SingleColorXTFFile =
    {
        "org/color4j/importer/xtf/Gold.xtf"
    };

    private static String[] m_IlluminantUsed =
    {
        "D65"
    };

    private static String[] m_ObserverUsed =
    {
        Observer.NAME_CIE1964
    };

    private static int[] m_ExpectedNoOfColors =
    {
        2
    };

    public TestCaseImportXTF( String name,
                              InputStream content,
                              String illuminant,
                              String observer,
                              int colorsInFile,
                              String filename )
    {
        super( name );
        m_Importer = new XTFParserContext();
        m_Content = content;
        m_ColorsInFile = colorsInFile;
        m_Filename = filename;
    }

    
    public static Test suite()
    {
        ClassLoader cl = TestCaseImportXTF.class.getClassLoader();
        m_Logger = LoggerFactory.getLogger( TestCaseImportXTF.class );
        TestSuite suite = new TestSuite();

        for( int i=0 ; i < m_SingleColorXTFFile.length ; i++ )
        {
            InputStream in = cl.getResourceAsStream( m_SingleColorXTFFile[i] );
            String ill = m_IlluminantUsed[i];
            String obs = m_ObserverUsed[i];
            int colorsInFile = m_ExpectedNoOfColors[i];
            suite.addTest( new TestCaseImportXTF( "test_ImportColors", in, ill, obs, colorsInFile, m_SingleColorXTFFile[i] ) );
            suite.addTest( new TestCaseImportXTF( "test_ManyStandards", in, ill, obs, colorsInFile, m_SingleColorXTFFile[i] ) );
            suite.addTest( new TestCaseImportXTF( "test_AltStandard", in, ill, obs, colorsInFile, m_SingleColorXTFFile[i] ) );
        }
        return suite;
    }
    
    public static void main(java.lang.String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public void test_ImportColors()
        throws Exception
    {
        
        if( m_Content == null )
            fail( "Could not find the Test XTF file." );
            
        m_Logger.debug( "Found file" );
//        ColorCalculator2 cc = ColorCalculator2.getInstance();

        m_Logger.debug( "About to doImport" );        
        try
        {
            Reflectance[] refls = m_Importer.doImport( m_Content, new HashMap<String, String>(  ) );
            m_Logger.debug( "Imported refls" );        
            assertEquals( "Wrong numbers of Colors parsed in file:" + m_Filename, m_ColorsInFile, refls.length );

        }
        catch( ImportException iexp )
        {
            m_Logger.error( iexp.getMessage(), iexp );
        }
    }
    
    /**
     * Added test case to test for multiple standards
     * in one xtf file. 
     * 
     * Reported issue Jul-31
     *
     **/
    public void test_ManyStandards()
    throws Exception
    {
        m_Logger.debug( "test Many standards" );
        ClassLoader cl = TestCaseImportXTF.class.getClassLoader();
        InputStream in = cl.getResourceAsStream( "org/color4j/importer/xtf/manystd.xtf" );
        XTFParserContext ctx = new XTFParserContext();
        Reflectance[] refl = ctx.doImport( in, new HashMap<String, String>(  ) );
        assertEquals( "Wrong numbers of Colors parsed in file: ", 12, refl.length );
        
    }
    
    public void test_AltStandard()
    throws Exception
    {
        m_Logger.debug( "test Alt Standard" );
        ClassLoader cl = TestCaseImportXTF.class.getClassLoader();
        InputStream in = cl.getResourceAsStream( "org/color4j/importer/xtf/blue.xtf" );
        XTFParserContext ctx = new XTFParserContext();
        Reflectance[] refl = ctx.doImport( in, new HashMap<String, String>(  ) );
        assertEquals( "Wrong numbers of Colors parsed in file: ", 4, refl.length );
    }
     

//    private Reflectance retrieveSingle( InputStream in )
//        throws Exception
//    {
//        Reflectance[] refls = m_Importer.doImport( in );
//        assertEquals( 1, refls.length );
//        return refls[0];
//    }

//    private double getValue( Reflectance r, String name )
//        throws Exception
//    {
//        String s = (String)r.getProperties().get( name );
//        if( s == null )
//            throw new NoSuchFieldException();
//        int pos1 = 0;
//        for( int i=0 ; i < s.length() ; i++ )
//        {
//            char ch = s.charAt(i);
//            if(  ch <= 57 && ch >= 48 )
//            {
//                pos1 = i;
//                break;
//            }
//        }
//        s = s.substring( pos1 );
//        int pos2 = s.length()-1;
//        for( int i=pos1 ; i < s.length() ; i++ )
//        {
//            if( s.charAt(i) > 57 && s.charAt(i) < 48 )
//            {
//                pos2 = i;
//                break;
//            }
//        }
//        s = s.substring( 0, pos2 );
//        return Double.parseDouble( s );
//    }
}
