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

package org.color4j.indexing;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * test whehter java.lang.Comparable interface implemented properly in ComparableReflectance
 *
 */
public class TestCaseComparableReflectance
{
    private Long[] m_uids;
    private Double[] m_values;
    private ComparableReflectance[] m_testData;

    @Before
    public void setUp()
    {

        m_uids = new Long[]{ 7564485698081449068L, 3131391933741695803L, -1483616520775186951L, -8324089644978763972L, -2266087794647526872L, 6086960707331982379L, -9189094816401459925L, 2573955665423686341L, 8997522547204770118L, -9134851795249449512L, -1580212341759796030L, 3069757967718047657L, -2718961150900460760L, 4467128427499567179L, -7927211919985343061L, 9095122547059473716L, 7640998240172820197L, -4404091917062801908L, -4906736683632611898L, 1648773681466038984L, -7258744946806494264L, 7640998240172820197L, 7322877380235653735L, 2328221205483966044L, 2332378757015776208L, -205964777955555652L, 4766472932210051800L, 7640998240172820197L, 7976935784579212847L, 7232222323470187357L, 3459979886067864277L, -3692376535219093702L, 3293473752371491276L, -6347807465778738254L, 7467478602062390542L, -8104993398037441580L, -6090098381356081215L, -6414576375344445976L, -4432983771641444952L, 451606358116366859L, -7720632321838051875L, -4825124634048711170L, -1695455129814016836L, -8865705548271527366L, 2249621569539328290L, 3339646084160654407L, -1957661798518030121L, 89367898475885133L, -8446541801751330744L, -8489118818922228758L, -6899418274406542983L, 5368174931656406086L, 7203180508194014777L, 7120449755449375077L, 159696294728769942L, -3878568183014722372L, 3682368816329155731L, -2484396583542071034L, 557960942815851032L, -3266505237445891753L, 6463592595590053417L, -4071579233753494159L, -1012821938359501734L, 1275485026344662869L, -5415441810746621566L, 1030906247021024797L, -3810130507392201554L, -3379624844255669752L, 4367059938974772056L, -5391389061703193130L, -1920084833077711655L, 5711841921289480534L, 4644414373446768923L, -6742863425613727428L, -6793718423142424942L, -6998826036681529288L, 1230295869421284204L, -1537327874826570429L, 3131391933741695803L, 3131391933741695803L, -2300037752195237726L, -7944389482100920910L, -3440332198318660521L, 3966403881281001218L, 6810253709021105992L, 1402282444797068196L, 9003543682290680344L, 7129876240983594689L, 3107946195241748637L, 7525085780849902601L, -97872439202436653L, -7183865558547625276L, 8206567594190170898L, -6493757456265392602L, 2080839401845412483L, -7845974618521051418L, 3897091670465715408L, -3958862317246330351L, 5349875811884571162L, 4090110482301305115L, -6840605365478578640L, -5077253234532159690L, -6427923097018470404L, -6825319135035731994L, 6309471289261605952L, 1132995201910659797L, -4777530704283938942L, 641045986157009123L, 5179825134316849989L, -5244164730840844671L, 257908835154119289L, -5938844656855390401L, -7228175948803794637L, 1654548754911800994L, -7549971835924978784L, -6728117112795864518L, 8647528829807794231L, 228112526073886964L, -8458884385015088851L };
        m_values = new Double[]{ 0.35322350951363024, 0.6700298828828554, 0.036398501787073934, 0.35322350951363024, 0.672876023962086, 0.4105896674352677, 0.732024357222085, 0.655057535573692, 0.2719636663818531, 0.6124746078777722, 0.213219545181348, 0.35322350951363024, 0.6728615520407887, 0.5782072460085348, 0.18932218332859985, 0.6486338392437555, 0.24730420523896113, 0.948007204915722, 0.7992211465162309, 0.565127159717659, 0.09654747876343628, 0.2687846936734082, 0.4164775760843975, 0.25496245091921743, 0.47472514381663067, 0.17719819563138017, 0.9355837625592152, 0.5966513755054547, 0.4000843199329095, 0.2792043537431512, 0.20992197938081958, 0.7367359395911464, 0.9396034095606811, 0.5946608369815377, 0.3559314750383903, 0.6321909581280073, 0.1495673888972766, 0.1279258286435203, 0.8381867757477812, 0.7218295605181309, 0.807995145384508, 0.7240304818272488, 0.606536334804768, 0.33794726913529893, 0.8843837715386803, 0.8748836121029707, 0.06914504850079894, 0.8024500939792614, 0.1580554139069481, 0.5511007294719886, 0.35423037124237167, 0.8909680973262865, 0.5184623173251781, 0.4524320722995486, 0.13847641111312914, 0.4939646984867382, 0.25912611157985166, 0.29339760829778727, 0.3015560643615237, 0.855609661829435, 0.1114575491004276, 0.8706191342772102, 0.8038862395529001, 0.7291912096585561, 0.8555236826963096, 0.6944730512677578, 0.4786870940150202, 0.15210069588414832, 0.0918989493543545, 0.9100697752510177, 0.8796106321492527, 0.3139313047758421, 0.1620578991702265, 0.08119870527130746, 0.9316503350359824, 0.16798966068128962, 0.2643368907281429, 0.16576055445408222, 0.8957380676489243, 0.019527345827104137, 0.5283126925811585, 0.6723415511472853, 0.07638013042611103, 0.31325679799736694, 0.18251631831939996, 0.5549666860264736, 0.776879466006899, 0.23822453713080882, 0.9787204608373011, 0.8767026063293578, 0.23813668311106218, 0.5698716164951624, 0.7206095185960575, 0.652662541322842, 0.6847174214068865, 0.37711953071350857, 0.0854612010289929, 0.5690996877599634, 0.14581921841205991, 0.8365777960968594, 0.9245393283540239, 0.46765709658955246, 0.41896624500002366, 0.4164765865437139, 0.8812175108460113, 0.5824758422932631, 0.4007054969801461, 0.11968617235767343, 0.4754237832510214, 0.5227455625653628, 0.021273713517432347, 0.30819274117369955, 0.7688167281746405, 0.9044143849805384, 0.18914884443224123, 0.8706573009583015, 0.347173641308009, 0.3521582127675482, 0.4765962283002555 };
        final int noOfData = m_uids.length;
        m_testData = new ComparableReflectance[ noOfData ];

        for( int i = 0; i < noOfData; i++ )
        {
            m_testData[ i ] = new ComparableReflectance( m_uids[ i ], m_values[ i ] );
        }
    }

    /**
     * Test of getUID method, of class org.color4j.indexing.ComaparableReflectance.
     */
    @Test
    public void test100_GetUID()
    {
        int noOfData = m_uids.length;
        for( int i = 0; i < noOfData; i++ )
        {
            assertThat( m_testData[ i ].getUID(), new IsEqual<Long>( m_uids[ i ] ) );
        }
    }

    /**
     * Test of getValue method, of class org.color4j.indexing.ComaparableReflectance.
     */
    public void test200_GetValue()
    {
        int noOfData = m_values.length;
        for( int i = 0; i < noOfData; i++ )
        {
            assertThat( m_testData[ i ].getValue(), new IsEqual<Double>( m_values[ i ] ) );
        }
    }

    /**
     * Test of compareTo method, of class org.color4j.indexing.ComaparableReflectance.
     * See java.lang.Comaparable api docs for specs.
     */
    public void test300_CompareTo()
    {
        int noOfData = m_testData.length;
        // test sgn(x.compareTo(y)) == -sgn(y.compareTo(x)) for all x and y
        for( int i = 0; i < noOfData - 2; i += 2 )
        {
            assertThat( m_testData[ i ].compareTo( m_testData[ i + 1 ] ),
                        new IsEqual<Integer>( - 1 * m_testData[ i + 1 ].compareTo( m_testData[ i ] ) ) );
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
