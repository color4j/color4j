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
 * BoundingSphereCalculatorTest.java
 * JUnit based test
 *
 * Created on July 8, 2002, 11:17 AM
 */

package org.color4j.indexing;

import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.encodings.CIELab;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class BoundingSphereCalculatorTest extends TestCase
{
    private ColorEncoding[] m_samples;

    public BoundingSphereCalculatorTest( java.lang.String testName )
    {
        super( testName );
    }

    public static void main( java.lang.String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( BoundingSphereCalculatorTest.class );

        return suite;
    }

    /** Test of createColorEncodingsFor method, of class org.color4j.indexing.BoundingSphereCalculator.
     public void testCreateColorEncodingsFor()
     {

     // Add your test code below by replacing the default call to fail.
     fail("The test case is empty.");    //NOI18N
     }
     */
    /**
     * Test of calculateCentrePoint method, of class org.color4j.indexing.BoundingSphereCalculator.
     */
    public void testCalculateCentrePoint()
    {
        //System.out.println("testCalculateCentrePoint"); //NOI18N
//        BoundingSphereCalculator bsc = new BoundingSphereCalculator();
//        double[] centre = bsc.calculateCentrePoint( m_samples );
        // Add your test code below by replacing the default call to fail.
        /*
        assertEquals( new Double( 0.0 ), new Double( centre[ 0 ] ) );
        assertEquals( new Double( 0.0 ), new Double( centre[ 1 ] ) );
        assertEquals( new Double( 0.0 ), new Double(centre[ 2 ] ) );
         **/
        //System.out.println( centre[0] + ", " + centre[1]+", " +centre[2] ); //NOI18N
        assertTrue( true );
    }

    /**
     * Test of findRadius method, of class org.color4j.indexing.BoundingSphereCalculator.
     */
    public void testFindRadius()
    {
        //System.out.println("testFindRadius ");  //NOI18N
//        BoundingSphereCalculator bsc = new BoundingSphereCalculator();
//        double[] centre = bsc.calculateCentrePoint( m_samples );
//        Double[] target = new Double[]{ new Double( centre[ 0 ] ), 
//                                        new Double( centre[ 1 ] ), 
//                                        new Double( centre[ 2 ] ) };
//        double rad = bsc.findRadius( target, m_samples );
        //System.out.println( "Radius is :" + rad );      //NOI18N
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");  //NOI18N
    }

    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}

    public void setUp()
    {
        m_samples = new ColorEncoding[]{// new CIELab( 100.0, 100.0, -100.0 ),
                                        // new CIELab( 100.0, -100.0, -100.0 ),
                                        //new CIELab( 100.0, 100.0, 100.0 ),
                                        // new CIELab( 100.0, -100.0, 100.0 ),
                                        //  new CIELab( -100.0, 100.0, -100.0 ),
                                        //  new CIELab( -100.0, -100.0, -100.0 ),
                                        //  new CIELab( -100.0, 100.0, 100.0 ),
                                        //  new CIELab( -100.0, -100.0, 100.0 )
                                        new CIELab( 52.149163730754935, 64.73875659418925, 52.36667431615823 ),
                                        new CIELab( 51.342905158208865, 63.7391853565768, 50.78984000422979 ),
                                        new CIELab( 50.1459611056933, 61.79508557701063, 48.42212537582506 ),
                                        new CIELab( 49.3084103532713, 60.1009479774387, 46.75432474112697 ),
                                        new CIELab( 51.39123099868962, 47.003211524445696, 50.56648771561958 ),
                                        new CIELab( 49.431446183188456, 57.74919611310836, 47.49599180873451 ),
                                        new CIELab( 54.20137157009292, 60.90360842366349, 55.86756680416125 ),
                                        new CIELab( 57.71769515131753, 58.682992432790016, 61.317473560845094 ),
                                        new CIELab( 58.59171474142332, 62.59664020534916, 63.95501152961815 ),
                                        new CIELab( 50.51740970667487, 59.18743784447689, 49.4289768386457 ),
                                        new CIELab( 57.71769515131753, 58.682992432790016, 61.317473560845094 ),
                                        new CIELab( 54.20683793856476, 60.953269677292084, 55.88324277518609 ),
                                        new CIELab( 57.71769515131753, 58.682992432790016, 61.317473560845094 ),
                                        new CIELab( 58.5931625381464, 62.59224431852062, 63.948124262471985 ),
                                        new CIELab( 50.52018375402625, 59.19036363931068, 49.47152369474899 ),
                                        new CIELab( 49.42709631780467, 57.78607585108375, 47.498938948552016 ),
                                        new CIELab( 51.39734647469692, 47.03807678968286, 50.591383664333755 ),
                                        new CIELab( 51.16008892501601, 61.50852707403631, 50.26430967545622 ),
                                        new CIELab( 50.28159163951746, 60.76704175310166, 48.53831730036703 ),
                                        new CIELab( 48.968232483002, 59.26117436352818, 45.960610941061255 ),
                                        new CIELab( 48.034983890874216, 57.94997277370256, 44.131839874203884 ),
                                        new CIELab( 50.91391398338767, 43.47096678545109, 49.245785275255415 ),
                                        new CIELab( 48.41682334793593, 55.02609203955483, 45.20311482176682 ),
                                        new CIELab( 53.893608432804115, 56.110871344397296, 54.945436807878245 ),
                                        new CIELab( 57.64654509516909, 53.45636702410583, 60.77162394445092 ),
                                        new CIELab( 58.0938214378903, 58.00240813988028, 62.950422744276736 ),
                                        new CIELab( 49.259314235113905, 57.16529036308565, 46.83646296512717 ),
                                        new CIELab( 57.64654509516909, 53.45636702410583, 60.77162394445092 ),
                                        new CIELab( 53.902790789638516, 56.13663967241006, 54.98721073044488 ),
                                        new CIELab( 57.64654509516909, 53.45636702410583, 60.77162394445092 ),
                                        new CIELab( 58.093743071679526, 58.004369810282896, 62.960081856724884 ),
                                        new CIELab( 49.26041037699882, 57.17586741252029, 46.84098833445252 ),
                                        new CIELab( 48.41161077468399, 55.05525084466895, 45.21693685496396 ),
                                        new CIELab( 50.91718062430638, 43.50276195018099, 49.277624601096306 )
                                        //new CIELab( 12.0, 67.0, 41.0 ),
                                        //new CIELab( 9.0, 45.0, 12.0 ),
                                        //new CIELab( 56.0, 78.0, 9.0 ),
                                        //new CIELab( 4.0, 8.0, 62.0 ),
                                        // new CIELab( 12.0, 65.0, 42.0 )
        };
    }
}
