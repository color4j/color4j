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

import org.color4j.imports.TextFileReflectanceImporter;
import org.color4j.imports.TextFileReflectanceImporterManager;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.color4j.colorimetry.Reflectance;

/**
 */
public class TestCaseImportMTF extends TestCase
{
    private static String[] m_FileNames = null;

    static
    {
        try
        {
            URL url = TestCaseImportMTF.class.getClassLoader().getResource( "org/color4j/importer/mtf" );
            String path = url.toString();
            int pos = path.indexOf( "/" );
            path = path.substring( pos + 1 );
            System.out.println( "Path : " + path );
            File file = new File( path );
            if( file.isDirectory() )
            {
                File[] files = file.listFiles( new FileFilter()
                {
                    public boolean accept( File file )
                    {
                        return file.getName().toLowerCase().lastIndexOf( ".mtf" ) > 0;
                    }
                } );
                m_FileNames = new String[ files.length ];
                for( int i = 0; i < m_FileNames.length; i++ )
                {
                    m_FileNames[ i ] = files[ i ].getAbsolutePath();
                    System.out.println( "FileName " + m_FileNames[ i ] );
                }
            }
            else
            {
                System.out.println( "File is not a directory " + file );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    static public Test suite()
    {
        // Creates a Test suite out of the testXXX methods in
        // this class, and execute them in ALPHABETICAL ORDER.
        try
        {

            TestSuite suite = new TestSuite();
            TreeMap m = new TreeMap();
            Method[] methods = TestCaseImportMTF.class.getMethods();
            for( int i = 0; i < methods.length; i++ )
            {
                if( methods[ i ].getName().startsWith( "test" ) )
                {
                    m.put( methods[ i ].getName(), methods[ i ] );
                }
            }
            Iterator list = m.keySet().iterator();
            while( list.hasNext() )
            {
                String methodname = (String) list.next();
                suite.addTest( new TestCaseImportMTF( methodname ) );
            }
            return suite;
        }
        catch( Exception e )
        {
            throw new UndeclaredThrowableException( e );
        }
    }

    /**
     * Constructor for TestCaseImportMTFTest.
     *
     * @param methodName
     */
    public TestCaseImportMTF( String methodName )
    {
        super( methodName );
    }

    public void test200_ParseFile()
        throws Exception
    {
        TextFileReflectanceImporterManager importerManager = TextFileReflectanceImporterManager.getInstance();
        TextFileReflectanceImporter importer = importerManager.getReflectanceImporter( "MTF" );
        if( m_FileNames != null )
        {
            for( int i = 0; i < m_FileNames.length; i++ )
            {
                Reflectance[] refls = importer.doImport( new File( m_FileNames[ i ] ), null );
                assertNotNull( refls );
                assertTrue( refls.length == 1 );
                System.out.println( "Reflectance name " + refls[ 0 ].getName() );
                System.out.println( "Reflectance spectrum " + refls[ 0 ].getSpectrumMap() );
            }
        }
    }
}
