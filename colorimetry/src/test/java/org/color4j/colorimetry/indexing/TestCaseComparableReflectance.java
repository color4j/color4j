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
 * ComaparableReflectanceTest.java
 * JUnit based test
 *
 * Created on July 18, 2002, 11:38 AM
 */

package org.color4j.colorimetry.indexing;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * test whehter java.lang.Comparable interface implemented properly in ComparableReflectance
 *
 */
public class TestCaseComparableReflectance extends TestCase
{

    private Long[] m_uids;
    private Double[] m_values;
    private ComparableReflectance[] m_testData;

    public void setUp()
    {

        m_uids = new Long[]{ new Long( 7564485698081449068L ), new Long( 3131391933741695803L ), new Long( -1483616520775186951L ), new Long( -8324089644978763972L ), new Long( -2266087794647526872L ), new Long( 6086960707331982379L ), new Long( -9189094816401459925L ), new Long( 2573955665423686341L ), new Long( 8997522547204770118L ), new Long( -9134851795249449512L ), new Long( -1580212341759796030L ), new Long( 3069757967718047657L ), new Long( -2718961150900460760L ), new Long( 4467128427499567179L ), new Long( -7927211919985343061L ), new Long( 9095122547059473716L ), new Long( 7640998240172820197L ), new Long( -4404091917062801908L ), new Long( -4906736683632611898L ), new Long( 1648773681466038984L ), new Long( -7258744946806494264L ), new Long( 7640998240172820197L ), new Long( 7322877380235653735L ), new Long( 2328221205483966044L ), new Long( 2332378757015776208L ), new Long( -205964777955555652L ), new Long( 4766472932210051800L ), new Long( 7640998240172820197L ), new Long( 7976935784579212847L ), new Long( 7232222323470187357L ), new Long( 3459979886067864277L ), new Long( -3692376535219093702L ), new Long( 3293473752371491276L ), new Long( -6347807465778738254L ), new Long( 7467478602062390542L ), new Long( -8104993398037441580L ), new Long( -6090098381356081215L ), new Long( -6414576375344445976L ), new Long( -4432983771641444952L ), new Long( 451606358116366859L ), new Long( -7720632321838051875L ), new Long( -4825124634048711170L ), new Long( -1695455129814016836L ), new Long( -8865705548271527366L ), new Long( 2249621569539328290L ), new Long( 3339646084160654407L ), new Long( -1957661798518030121L ), new Long( 89367898475885133L ), new Long( -8446541801751330744L ), new Long( -8489118818922228758L ), new Long( -6899418274406542983L ), new Long( 5368174931656406086L ), new Long( 7203180508194014777L ), new Long( 7120449755449375077L ), new Long( 159696294728769942L ), new Long( -3878568183014722372L ), new Long( 3682368816329155731L ), new Long( -2484396583542071034L ), new Long( 557960942815851032L ), new Long( -3266505237445891753L ), new Long( 6463592595590053417L ), new Long( -4071579233753494159L ), new Long( -1012821938359501734L ), new Long( 1275485026344662869L ), new Long( -5415441810746621566L ), new Long( 1030906247021024797L ), new Long( -3810130507392201554L ), new Long( -3379624844255669752L ), new Long( 4367059938974772056L ), new Long( -5391389061703193130L ), new Long( -1920084833077711655L ), new Long( 5711841921289480534L ), new Long( 4644414373446768923L ), new Long( -6742863425613727428L ), new Long( -6793718423142424942L ), new Long( -6998826036681529288L ), new Long( 1230295869421284204L ), new Long( -1537327874826570429L ), new Long( 3131391933741695803L ), new Long( 3131391933741695803L ), new Long( -2300037752195237726L ), new Long( -7944389482100920910L ), new Long( -3440332198318660521L ), new Long( 3966403881281001218L ), new Long( 6810253709021105992L ), new Long( 1402282444797068196L ), new Long( 9003543682290680344L ), new Long( 7129876240983594689L ), new Long( 3107946195241748637L ), new Long( 7525085780849902601L ), new Long( -97872439202436653L ), new Long( -7183865558547625276L ), new Long( 8206567594190170898L ), new Long( -6493757456265392602L ), new Long( 2080839401845412483L ), new Long( -7845974618521051418L ), new Long( 3897091670465715408L ), new Long( -3958862317246330351L ), new Long( 5349875811884571162L ), new Long( 4090110482301305115L ), new Long( -6840605365478578640L ), new Long( -5077253234532159690L ), new Long( -6427923097018470404L ), new Long( -6825319135035731994L ), new Long( 6309471289261605952L ), new Long( 1132995201910659797L ), new Long( -4777530704283938942L ), new Long( 641045986157009123L ), new Long( 5179825134316849989L ), new Long( -5244164730840844671L ), new Long( 257908835154119289L ), new Long( -5938844656855390401L ), new Long( -7228175948803794637L ), new Long( 1654548754911800994L ), new Long( -7549971835924978784L ), new Long( -6728117112795864518L ), new Long( 8647528829807794231L ), new Long( 228112526073886964L ), new Long( -8458884385015088851L ) };
        m_values = new Double[]{ new Double( 0.35322350951363024 ), new Double( 0.6700298828828554 ), new Double( 0.036398501787073934 ), new Double( 0.35322350951363024 ), new Double( 0.672876023962086 ), new Double( 0.4105896674352677 ), new Double( 0.732024357222085 ), new Double( 0.655057535573692 ), new Double( 0.2719636663818531 ), new Double( 0.6124746078777722 ), new Double( 0.213219545181348 ), new Double( 0.35322350951363024 ), new Double( 0.6728615520407887 ), new Double( 0.5782072460085348 ), new Double( 0.18932218332859985 ), new Double( 0.6486338392437555 ), new Double( 0.24730420523896113 ), new Double( 0.948007204915722 ), new Double( 0.7992211465162309 ), new Double( 0.565127159717659 ), new Double( 0.09654747876343628 ), new Double( 0.2687846936734082 ), new Double( 0.4164775760843975 ), new Double( 0.25496245091921743 ), new Double( 0.47472514381663067 ), new Double( 0.17719819563138017 ), new Double( 0.9355837625592152 ), new Double( 0.5966513755054547 ), new Double( 0.4000843199329095 ), new Double( 0.2792043537431512 ), new Double( 0.20992197938081958 ), new Double( 0.7367359395911464 ), new Double( 0.9396034095606811 ), new Double( 0.5946608369815377 ), new Double( 0.3559314750383903 ), new Double( 0.6321909581280073 ), new Double( 0.1495673888972766 ), new Double( 0.1279258286435203 ), new Double( 0.8381867757477812 ), new Double( 0.7218295605181309 ), new Double( 0.807995145384508 ), new Double( 0.7240304818272488 ), new Double( 0.606536334804768 ), new Double( 0.33794726913529893 ), new Double( 0.8843837715386803 ), new Double( 0.8748836121029707 ), new Double( 0.06914504850079894 ), new Double( 0.8024500939792614 ), new Double( 0.1580554139069481 ), new Double( 0.5511007294719886 ), new Double( 0.35423037124237167 ), new Double( 0.8909680973262865 ), new Double( 0.5184623173251781 ), new Double( 0.4524320722995486 ), new Double( 0.13847641111312914 ), new Double( 0.4939646984867382 ), new Double( 0.25912611157985166 ), new Double( 0.29339760829778727 ), new Double( 0.3015560643615237 ), new Double( 0.855609661829435 ), new Double( 0.1114575491004276 ), new Double( 0.8706191342772102 ), new Double( 0.8038862395529001 ), new Double( 0.7291912096585561 ), new Double( 0.8555236826963096 ), new Double( 0.6944730512677578 ), new Double( 0.4786870940150202 ), new Double( 0.15210069588414832 ), new Double( 0.0918989493543545 ), new Double( 0.9100697752510177 ), new Double( 0.8796106321492527 ), new Double( 0.3139313047758421 ), new Double( 0.1620578991702265 ), new Double( 0.08119870527130746 ), new Double( 0.9316503350359824 ), new Double( 0.16798966068128962 ), new Double( 0.2643368907281429 ), new Double( 0.16576055445408222 ), new Double( 0.8957380676489243 ), new Double( 0.019527345827104137 ), new Double( 0.5283126925811585 ), new Double( 0.6723415511472853 ), new Double( 0.07638013042611103 ), new Double( 0.31325679799736694 ), new Double( 0.18251631831939996 ), new Double( 0.5549666860264736 ), new Double( 0.776879466006899 ), new Double( 0.23822453713080882 ), new Double( 0.9787204608373011 ), new Double( 0.8767026063293578 ), new Double( 0.23813668311106218 ), new Double( 0.5698716164951624 ), new Double( 0.7206095185960575 ), new Double( 0.652662541322842 ), new Double( 0.6847174214068865 ), new Double( 0.37711953071350857 ), new Double( 0.0854612010289929 ), new Double( 0.5690996877599634 ), new Double( 0.14581921841205991 ), new Double( 0.8365777960968594 ), new Double( 0.9245393283540239 ), new Double( 0.46765709658955246 ), new Double( 0.41896624500002366 ), new Double( 0.4164765865437139 ), new Double( 0.8812175108460113 ), new Double( 0.5824758422932631 ), new Double( 0.4007054969801461 ), new Double( 0.11968617235767343 ), new Double( 0.4754237832510214 ), new Double( 0.5227455625653628 ), new Double( 0.021273713517432347 ), new Double( 0.30819274117369955 ), new Double( 0.7688167281746405 ), new Double( 0.9044143849805384 ), new Double( 0.18914884443224123 ), new Double( 0.8706573009583015 ), new Double( 0.347173641308009 ), new Double( 0.3521582127675482 ), new Double( 0.4765962283002555 ) };
        final int noOfData = m_uids.length;
        m_testData = new ComparableReflectance[ noOfData ];

        for( int i = 0; i < noOfData; i++ )
        {
            m_testData[ i ] = new ComparableReflectance( m_uids[ i ], m_values[ i ] );
        }
    }

    public TestCaseComparableReflectance( java.lang.String testName )
    {
        super( testName );
    }

    public static void main( java.lang.String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestCaseComparableReflectance.class );
        return suite;
    }

    /**
     * Test of getUID method, of class org.color4j.colorimetry.indexing.ComaparableReflectance.
     */
    public void test100_GetUID()
    {
        int noOfData = m_uids.length;
        for( int i = 0; i < noOfData; i++ )
        {
            assertEquals( m_testData[ i ].getUID(), m_uids[ i ] );
        }
    }

    /**
     * Test of getValue method, of class org.color4j.colorimetry.indexing.ComaparableReflectance.
     */
    public void test200_GetValue()
    {
        int noOfData = m_values.length;
        for( int i = 0; i < noOfData; i++ )
        {
            assertEquals( m_testData[ i ].getValue(), m_values[ i ] );
        }
    }

    /**
     * Test of compareTo method, of class org.color4j.colorimetry.indexing.ComaparableReflectance.
     * See java.lang.Comaparable api docs for specs.
     */
    public void test300_CompareTo()
    {
        int noOfData = m_testData.length;
        // test sgn(x.compareTo(y)) == -sgn(y.compareTo(x)) for all x and y
        for( int i = 0; i < noOfData - 2; i += 2 )
        {
            assertEquals( m_testData[ i ].compareTo( m_testData[ i + 1 ] ),
                          -1 * m_testData[ i + 1 ].compareTo( m_testData[ i ] )
            );
        }
        // test (x.compareTo(y)>0 && y.compareTo(z)>0) implies x.compareTo(z)>0
        for( int i = 0; i < noOfData - 3; i += 3 )
        {
            ComparableReflectance x = m_testData[ i ];
            ComparableReflectance y = m_testData[ i + 1 ];
            ComparableReflectance z = m_testData[ i + 2 ];
            if( ( x.compareTo( y ) > 0 ) &&
                ( y.compareTo( z ) > 0 ) )
            {
                assertTrue( x.compareTo( z ) > 0 );
            }
        }
        // test  x.compareTo(y)==0  implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.
        for( int i = 0; i < noOfData - 3; i += 3 )
        {
            Comparable x = m_testData[ i ];
            Comparable y = m_testData[ i + 1 ];
            Comparable z = m_testData[ i + 2 ];
            if( x.compareTo( y ) == 0 )
            {
                // should only check for sign. this works cos implementation reutrns 1, 0 or -1
                assertTrue( x.compareTo( z ) == y.compareTo( z ) );
            }
        }
    }

    public void test400_MapTest()
    {
        int noOfData = m_testData.length;
        int counter = 0;
        Map testMap = new TreeMap();
        for( int i = 0; i < noOfData; i++ )
        {
            counter = counter + 1;
            testMap.put( m_testData[ i ], m_testData[ i ].getValue() );
        }
        Collection testResult = testMap.values();
        Iterator it = testResult.iterator();
        Double lastValue = (Double) it.next();
        while( it.hasNext() )
        {
            Double nextValue = (Double) it.next();
            assertTrue( lastValue.doubleValue() <= nextValue.doubleValue() );
            lastValue = nextValue;
        }
    }

    public void test500_DuplicateDataTest()
    {
        final int noOfData = m_testData.length;
        Map testMap = new TreeMap();
        for( int i = 0; i < noOfData; i++ )
        {
            testMap.put( m_testData[ i ], m_testData[ i ].getValue() );
        }
        Collection testResult = testMap.values();
        Iterator it = testResult.iterator();
        Double lastValue = (Double) it.next();
        int counter = 0;
        while( it.hasNext() )
        {
            Double nextValue = (Double) it.next();
            assertTrue( lastValue.doubleValue() <= nextValue.doubleValue() );
            lastValue = nextValue;
            counter++;
        }
    }
}
