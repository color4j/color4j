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
 * TestCaseImportMIF.java
 * JUnit based test
 *
 * Created on May 17, 2003, 2:19 AM
 */

package org.color4j.tester;

import java.util.HashMap;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.imports.TextFileReflectanceImporter;
import org.color4j.imports.mif.MIFParserContext;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class TestCaseImportMIF extends TestCase
{
    private static final String TEST_FILE = "org/color4j/importer/mif/G7_P1w08.mif";

    public TestCaseImportMIF(java.lang.String testName)
    {
        super(testName);
    }
    
    public static void main(java.lang.String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestCaseImportMIF.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    public void testGetReflectances() throws Exception
    {
        System.out.println( "Testing getReflectances():Reflectance[].." );
        ClassLoader cl = TestCaseImportMIF.class.getClassLoader();
        InputStream is = cl.getResourceAsStream( TEST_FILE );
        assertNotNull( "Test file not found.", is );
        TextFileReflectanceImporter importer = new MIFParserContext();
        Reflectance[] refls = importer.doImport( is, new HashMap<String, String>( ) );
        //expect to have 8 reflectances
        int len = refls.length;
        final int expected = 11;
        assertEquals( "Comparing lengths", expected, len );
        //they are called  Wrist
        for( int i= 0; i < len; i ++ )
        {
            assertEquals( "Expected name did not match for sample " + i, EXPECTED_NAMES[i],refls[ i ].getName() );
        }
    }
    
    
    
    private static final String[] EXPECTED_NAMES = { "G7-B-1-w", "G7-C-1-w","G7-G-1-w", "G7-HC-1-w", "G7-HR-1-w",
    "G7-K-1-w", "G7-M-1-w", "G7-R-1-w", "G7-SC-1-w", "G7-W-1-w", "G7-Y-1-w"};

}
