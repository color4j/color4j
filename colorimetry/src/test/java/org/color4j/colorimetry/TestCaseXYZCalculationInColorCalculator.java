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
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TestCaseXYZCalculationInColorCalculator extends TestCase
{
    /**
     * The maximum allowed difference for each component
     * (x, Y, Z) between the prepared XYZ and the computed
     * XYZ from Reflectance data.
     */
    private static final double ALLOWED_XYZ_DEVIANCE = 0.1;

    private ReflectanceCase[] m_TestReflectances;

    public TestCaseXYZCalculationInColorCalculator( String name )
    {
        super( name );
        initialize();
    }

    public void initialize()
    {
        // Nothing generic to initialize, now.
    }

    public void testReflectanceCasesXYZ()
        throws Exception
    {
        initReflectanceCases();
        for( int i = 0; i < m_TestReflectances.length; i++ )
        {
            Illuminant ill = m_TestReflectances[ i ].getIlluminant();
            Observer obs = m_TestReflectances[ i ].getObserver();
            Reflectance refl = m_TestReflectances[ i ].getReflectance();

            XYZ xyz = ColorCalculator.computeXYZ( ill, refl, obs );
            m_TestReflectances[ i ].validate( xyz );
        }
    }

    private class ReflectanceCase
    {
        private Reflectance m_Reflectance;
        private Illuminant m_Illuminant;
        private Observer m_Observer;
        private XYZ m_ExpectedResult;

        ReflectanceCase( int start,
                         int interval,
                         float[] data,
                         Illuminant ill,
                         Observer obs,
                         XYZ expected
        )
            throws ColorException
        {
            Spectrum s = new Spectrum( start, interval, data );
            m_Reflectance = ReflectanceImpl.create( s );
            m_ExpectedResult = expected;
            m_Illuminant = ill;
            m_Observer = obs;
        }

        void validate( XYZ result )
        {
            double[] set1 = result.getColorValues();
            double[] set2 = m_ExpectedResult.getColorValues();
//			@TODO: Clean up. ML - 05/08/2003
//            boolean equality = true;
            for( int i = 0; i < set1.length; i++ )
            {
                if( Math.abs( set1[ i ] - set2[ i ] ) > ALLOWED_XYZ_DEVIANCE )
                {
                    Assert.fail( "ColorValue " + i + " is not within limit. Expected " + result + " got " + m_ExpectedResult );
                }
            }
        }

        Reflectance getReflectance()
        {
            return m_Reflectance;
        }

        Observer getObserver()
        {
            return m_Observer;
        }

        Illuminant getIlluminant()
        {
            return m_Illuminant;
        }
    }

    private void initReflectanceCases()
        throws ColorException
    {
        Illuminant d65 = IlluminantImpl.create( "D65" );
        Illuminant A = IlluminantImpl.create( "A" );
        Illuminant F11 = IlluminantImpl.create( "F11" );
        Observer obs10 = ObserverImpl.create( Observer.NAME_CIE1964 );
//		@TODO: Clean up. ML - 05/08/2003
//        float[] r =
//        {
//                0.091f,0.010f, 0.121f,0.138f,    //360-390
//                0.161f,0.197f, 0.248f,0.320f,    //400-430
//                0.350f,0.317f, 0.262f,0.211f,    //440-470
//                0.168f,0.130f, 0.095f,0.075f,    //480-510
//                0.058f,0.046f, 0.036f,0.031f,    //520-550
//                0.029f,0.027f, 0.025f,0.024f,    //560-590
//                0.024f,0.024f, 0.024f,0.023f,    //600-630
//                0.023f,0.025f, 0.032f,0.056f,    //640-670
//                0.109f,0.197f, 0.308f,0.423f,    //680-710
//                0.538f,0.634f, 0.709f,0.763f,    //720-750
//        };
        float[] r6015054 = {
            0.10131f, 0.09800f, 0.09491f, 0.09372f, 0.09397f,
            0.09718f, 0.10186f, 0.10587f, 0.10955f, 0.11277f,
            0.11574f, 0.11742f, 0.12077f, 0.12439f, 0.12420f,
            0.12445f, 0.13631f, 0.16285f, 0.19183f, 0.20874f,
            0.21453f, 0.21592f, 0.21700f, 0.22142f, 0.23517f,
            0.26340f, 0.31061f, 0.37752f, 0.46697f, 0.56203f,
            0.66642f
        };
        float[] r6015072 = {
            0.10318f, 0.09924f, 0.09566f, 0.09406f, 0.09408f,
            0.09747f, 0.10310f, 0.10833f, 0.11443f, 0.11995f,
            0.12632f, 0.13154f, 0.13777f, 0.14310f, 0.14358f,
            0.14340f, 0.15426f, 0.17828f, 0.20334f, 0.21709f,
            0.22118f, 0.22195f, 0.22290f, 0.22720f, 0.24125f,
            0.27022f, 0.31857f, 0.38647f, 0.47682f, 0.57108f,
            0.67343f
        };

        float[] rorangeb = {
            0.04268f, 0.04436f, 0.04515f, 0.04561f, 0.04461f,
            0.04345f, 0.04164f, 0.039f, 0.0352f, 0.03133f,
            0.02889f, 0.02756f, 0.02681f, 0.02645f, 0.02659f,
            0.02808f, 0.03086f, 0.03823f, 0.05609f, 0.10211f,
            0.18918f, 0.32378f, 0.47457f, 0.59408f, 0.67707f,
            0.71244f, 0.73941f, 0.76852f, 0.79762f, 0.81946f,
            0.83325f, 0.83767f, 0.83805f
        };

        float[] rorange64 = {
            0.72786f, 0.50664f, 0.24846f, 0.08339f, 0.04372f,
            0.03615f, 0.03494f, 0.0341f, 0.03257f, 0.02957f,
            0.0268f, 0.02551f, 0.02551f, 0.02563f, 0.02721f,
            0.03160f, 0.04174f, 0.05861f, 0.0798f, 0.11745f,
            0.19014f, 0.30765f, 0.44784f, 0.5864f, 0.70951f,
            0.7887f, 0.83256f, 0.85749f, 0.87648f, 0.89065f,
            0.88982f, 0.8903f, 0.89331f
        };

        float[] ro6420nm = {
            0.72786f, 0.24846f, 0.04372f, 0.03494f, 0.03257f,
            0.0268f, 0.02551f, 0.02721f, 0.04174f, 0.0798f,
            0.19014f, 0.44784f, 0.70951f, 0.83256f, 0.87648f,
            0.88982f, 0.89331f
        };
        float[] rniclas1 = {
            0.736f, 0.7787f, 0.8014f, 0.784f, 0.7455f, 0.7071f, 0.6551f,
            0.6594f, 0.6548f, 0.7959f, 0.9222f, 0.9462f, 0.949f, 0.9529f, 0.9663f, 0.9654f
        };

        float[] rniclas2 = {
            0.0878f, 0.0819f, 0.0866f, 0.1002f, 0.1186f, 0.1409f, 0.1625f,
            0.1550f, 0.1567f, 0.1326f, 0.1485f, 0.1324f, 0.1833f, 0.3918f, 0.6671f, 0.8762f
        };

        float[] next1 = {
            0.26563f, 0.26342f, 0.26170f, 0.25900f, 0.25796f,
            0.25792f, 0.25816f, 0.25454f, 0.24666f, 0.24025f,
            0.23437f, 0.22178f, 0.20848f, 0.20207f, 0.19647f,
            0.18675f, 0.18124f, 0.18921f, 0.20669f, 0.22171f,
            0.22942f, 0.23319f, 0.23814f, 0.25057f, 0.27495f,
            0.31482f, 0.37080f, 0.43946f, 0.51971f, 0.59971f,
            0.67833f
        };

        ReflectanceCase rc1 = new ReflectanceCase
            (
                400, 10, r6015054,
                d65, obs10,
                new XYZ( 16.659, 15.29, 10.746 )
            );
        ReflectanceCase rc2 = new ReflectanceCase
            (
                400, 10, r6015072,
                d65, obs10,
                new XYZ( 17.505, 16.58, 10.938 )
            );

        ReflectanceCase rc3 = new ReflectanceCase
            (
                380, 10, rorangeb, d65, obs10, new XYZ( 28.5, 16.73, 4.01 )
            );
        ReflectanceCase rc4 = new ReflectanceCase
            (
                380, 10, rorangeb, A, obs10, new XYZ( 45.76, 24.88, 1.27 )
            );

        ReflectanceCase rc5 = new ReflectanceCase
            (
                380, 10, rorange64, d65, obs10, new XYZ( 29.63, 17.57, 3.87 )
            );

        ReflectanceCase rc6 = new ReflectanceCase
            (
                380, 10, rorange64, A, obs10, new XYZ( 47.79, 26.06, 1.2 )
            );
        ReflectanceCase rc7 = new ReflectanceCase
            (
                380, 20, ro6420nm, d65, obs10, new XYZ( 29.63, 17.57, 3.87 )
            );
        ReflectanceCase rc8 = new ReflectanceCase
            (
                380, 20, ro6420nm, A, obs10, new XYZ( 47.79, 26.06, 1.2 )
            );
        ReflectanceCase rc9 = new ReflectanceCase
            (
                400, 20, rniclas2, d65, obs10, new XYZ( 13.91, 14.98, 10.53 )
            );
        ReflectanceCase rc10 = new ReflectanceCase
            (
                400, 20, rniclas1, d65, obs10, new XYZ( 77.82, 74.56, 83.49 )
            );
        ReflectanceCase rc11 = new ReflectanceCase
            (
                400, 20, rniclas1, F11, obs10, new XYZ( 87.16, 75.75, 51.38 )
            );
        ReflectanceCase rc12 = new ReflectanceCase
            (
                400, 10, next1, d65, obs10, new XYZ( 21.928, 21.533, 27.413 )
            );

        m_TestReflectances = new ReflectanceCase[]
            {
                rc12, rc1, rc2, rc3, rc4, rc5, rc6, rc7, rc8, rc9, rc11, rc10
            };
    }
}
