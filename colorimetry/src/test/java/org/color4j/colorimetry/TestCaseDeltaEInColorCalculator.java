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

import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.CIELuv;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestCaseDeltaEInColorCalculator extends TestCase
{
    /**
     * The maximum allowed difference for each component
     * (x, Y, Z) between the prepared XYZ and the computed
     * XYZ from Reflectance data.
     */
    public TestCaseDeltaEInColorCalculator( String name )
    {
        super( name );
        //initialize();
    }

    private static final double ALLOWED_XYZ_DEVIANCE = 0.1;

    private DeltaECase[] m_TestDeltaEs;

    public void initialize()
    {
        // Nothing generic to initialize, now.
    }

    public void testDeltaE()
        throws Exception
    {
        initDeltaECases();
        MatchingFactory fact = MatchingFactory.getInstance();
        //String[] md = {"CIE94","LABDE","CMC","LUVDE","CIE2000DE"}; 
        for( int i = 0; i < m_TestDeltaEs.length; i++ )
        {
            String s = m_TestDeltaEs[ i ].getcdf();
            XYZ target = m_TestDeltaEs[ i ].gettxyz();
            XYZ batch = m_TestDeltaEs[ i ].getbxyz();
            XYZ white = m_TestDeltaEs[ i ].getwhite();
            ColorDifference cd;
            DifferenceAlgorithm algo;
            if( i != 9 )
            {
                CIELab tlab = new CIELab( target.toLab( white ) );
                CIELab blab = new CIELab( batch.toLab( white ) );
                algo = fact.getAlgorithm( s );
                cd = algo.compute( tlab, blab );
            }
            else
            {
                CIELuv tluv = new CIELuv( target.toLuv( white ) );
                CIELuv bluv = new CIELuv( batch.toLuv( white ) );
                algo = fact.getAlgorithm( s );
                cd = algo.compute( tluv, bluv );
            }
            String[] names = cd.getAllValueNames();
            for( int k = 0; k < names.length; k++ )
            {
                System.out.println( s + " : method[" + k + "]=" + names[ k ] );
            }

            System.out.println();

            System.out.println( "Method = [ " + names[ 0 ] + " ] " + s + " = " + cd.getValue( names[ 0 ] ) );
            //+" dl = "+cd.getDeltaLuminance()+" dc = "+cd.getDeltaChroma());
            m_TestDeltaEs[ i ].validate( cd.getValue( names[ 0 ] ), m_TestDeltaEs[ i ].getde() );
        }
    }

    private class DeltaECase
    {
        private XYZ m_txyz;
        private XYZ m_bxyz;
        private XYZ m_white;
        private String m_cdf;
        private double m_de;
        private double m_ex;

        //target is the target color XYZ 
        //batch is the batch color XYZ
        //white is the whitepoint XYZ,like D65 10d observer
        //cdf is which cdf to use, 0=cie94,1=LabDE, 2=CMC, 3=LuvDE
        //extra is use for cmc(2 or 1) or cie94
        //deltaE is the expect deltaE
        DeltaECase( XYZ target, XYZ batch, XYZ white, String cdf, double extra, double deltaE )
            throws ColorException
        {
            m_txyz = target;
            m_bxyz = batch;
            m_cdf = cdf;
            m_de = deltaE;
            m_white = white;
            m_ex = extra;
        }

        void validate( double cv, double ev )
        {

            if( Math.abs( cv - ev ) > ALLOWED_XYZ_DEVIANCE )
            {
                Assert.fail( "DeltaE calculation is not within limit. Expected " + ev + " got " + cv );
            }
        }

        XYZ gettxyz()
        {
            return m_txyz;
        }

        XYZ getbxyz()
        {
            return m_bxyz;
        }

        double getde()
        {
            return m_de;
        }

        XYZ getwhite()
        {
            return m_white;
        }

        String getcdf()
        {
            return m_cdf;
        }

        double getextra()
        {
            return m_ex;
        }
    }

    private void initDeltaECases()
        throws ColorException
    {
        XYZ xyz6015054 = new XYZ( 16.659, 15.29, 10.746 );
        XYZ xyz6015054A10 = new XYZ( 22.381, 17.118, 3.583 );
        XYZ xyz6015072 = new XYZ( 17.505, 16.58, 10.938 );
        XYZ xyz6015072A10 = new XYZ( 23.402, 18.353, 3.671 );
        XYZ xyzOrangeB = new XYZ( 28.5, 16.73, 4.01 );
        XYZ xyzOrange46 = new XYZ( 29.63, 17.57, 3.87 );
        XYZ xyz2000t1 = new XYZ( 19.41, 28.41, 11.5766 );
        XYZ xyz2000b1 = new XYZ( 19.5525, 28.64, 10.5791 );
        XYZ xyz2000t2 = new XYZ( 4.96, 3.72, 19.59 );
        XYZ xyz2000b2 = new XYZ( 4.6651, 3.81, 17.7848 );
        XYZ white = new XYZ( 94.811, 100.0, 107.304 );
        XYZ whiteA10 = new XYZ( 111.144, 100.0, 35.2 );

        //LabDE case
        DeltaECase de1 = new DeltaECase
            (
                xyz6015054, xyz6015072, white, "CIELab DE", 1.0, 3.947
            );

        //cmc(2:1)
        DeltaECase de2 = new DeltaECase
            (
                xyz6015054, xyz6015072, white, "CMC 2:1", 2.0, 5.413
            );

        //cmc(2:1)
        DeltaECase de3 = new DeltaECase
            (
                xyzOrangeB, xyzOrange46, white, "CMC 2:1", 2.0, 1.65
            );
        //cie1994
        DeltaECase de4 = new DeltaECase
            (
                xyzOrangeB, xyzOrange46, white, "CIE 94", 1.0, 1.53
            );
        //cmc(1:1)
        DeltaECase de5 = new DeltaECase
            (
                xyzOrangeB, xyzOrange46, white, "CMC 1:1", 1.0, 1.86
            );
        //cie2000
        DeltaECase de6 = new DeltaECase
            (
                xyz2000t1, xyz2000b1, white, "CIE 2000", 1.0, 1.2644
            );
        //cie2000
        DeltaECase de7 = new DeltaECase
            (
                xyz2000t2, xyz2000b2, white, "CIE 2000", 1.0, 2.0373
            );
        DeltaECase de8 = new DeltaECase
            (
                xyz2000t2, xyz2000b2, white, "CMC 2:1", 1.0, 3.1547
            );
        //bfd
        DeltaECase de9 = new DeltaECase
            (
                xyz6015054A10, xyz6015072A10, whiteA10, "BFD 1:1", 1.0, 4.168
            );
        /*DeltaECase de10 =  new DeltaECase
                                   (
                                       xyz6015054,xyz6015072,white,"CIELuv DE",1.0,4.429
                                   );
         **/
        //all the cases testing
        m_TestDeltaEs = new DeltaECase[]
            {
                de1, de2, de3, de4, de5, de6, de7, de8, de9//,de10
            };
    }
}
