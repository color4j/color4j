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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.Spectrum;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.DefaultEncodingFactory;
import org.color4j.colorimetry.encodings.EncodingFactory;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.matching.ColorDifference;
import org.color4j.colorimetry.matching.DifferenceAlgorithm;
import org.color4j.colorimetry.matching.MatchingFactory;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.exports.ReflectanceExporter;
import org.color4j.exports.qtx.ExporterBatchedQTX;
import org.color4j.exports.qtx.ExporterQTX;
import org.color4j.imports.TextFileReflectanceImporter;
import org.color4j.imports.qtx.ImporterQTX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseImportExport extends TestCase
{
    private static final double MAXIMUM_DELTAE_TOLERANCE = 0.005;

    private EncodingFactory factory = new DefaultEncodingFactory();
    private ReflectanceExporter m_Exporter;
    private TextFileReflectanceImporter m_Importer;
    private static Logger m_Logger;

    static
    {
        m_Logger = LoggerFactory.getLogger( TestCaseImportExport.class );
    }

    private static TextFileReflectanceImporter[] m_AvailableImporters =
        {
            new ImporterQTX(),
            new ImporterQTX()
        };

    private static ReflectanceExporter[] m_AvailableExporters =
        {
            new ExporterQTX(),
            new ExporterBatchedQTX()
        };

    public TestCaseImportExport( String name,
                                 TextFileReflectanceImporter importer,
                                 ReflectanceExporter exporter
    )
    {
        super( name );
        m_Exporter = exporter;
        m_Importer = importer;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        for( int i = 0; i < m_AvailableImporters.length && i < m_AvailableExporters.length; i++ )
        {
            TextFileReflectanceImporter imp = m_AvailableImporters[ i ];
            ReflectanceExporter exp = m_AvailableExporters[ i ];
            suite.addTest( new TestCaseImportExport( "test_FirstColor", imp, exp ) );
            suite.addTest( new TestCaseImportExport( "test_SecondColor", imp, exp ) );
            suite.addTest( new TestCaseImportExport( "test_BothColors", imp, exp ) );
        }
        return suite;
    }

    public void test_FirstColor()
        throws Exception
    {
        doSingleTest( 0 );
    }

    public void test_SecondColor()
        throws Exception
    {
        doSingleTest( 1 );
    }

    public void test_BothColors()
        throws Exception
    {
        Reflectance r1 = createReflectance( 0 );
        Reflectance r2 = createReflectance( 1 );
        InputStream in = save( new Reflectance[]{ r1, r2 } );
        Reflectance[] refls = retrieve( in );
        compare( r1, refls[ 0 ] );
        compare( r2, refls[ 1 ] );
    }

    private void doSingleTest( int reflNo )
        throws Exception
    {
        Reflectance r1 = createReflectance( reflNo );
        InputStream in = save( new Reflectance[]{ r1 } );
        Reflectance[] refls = retrieve( in );
        assertEquals( 1, refls.length );
        Reflectance r2 = refls[ 0 ];
        compare( r1, r2 );
    }

    private Reflectance createReflectance( int reflNo )
        throws Exception
    {
        Spectrum spectrum = Spectrum.create( m_Start[ reflNo ], m_Interval[ reflNo ], m_Spectra[ reflNo ] );
        Reflectance refl = new MiniReflectanceImpl( "Test Reflectance" + reflNo, spectrum );
        return refl;
    }

    private InputStream save( Reflectance[] refls )
        throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        m_Exporter.doExport( refls, baos );

        byte[] bytes = baos.toByteArray();

        m_Logger.debug( "QTX File.\n" + new String( bytes ) );
        ByteArrayInputStream result = new ByteArrayInputStream( bytes );

        return result;
    }

    private void compare( Reflectance r1, Reflectance r2 )
        throws Exception
    {
        Illuminant ill = IlluminantImpl.create( "D65" );
        Observer obs = ObserverImpl.create( Observer.NAME_CIE1964 );

        XYZ xyz1 = factory.create( XYZ.class, ill, r1, obs );
        XYZ xyz2 = factory.create( XYZ.class, ill, r2, obs );

        XYZ wp = factory.createWhitePoint( ill, obs );

        CIELab lab1 = xyz1.toCIELab( wp );
        CIELab lab2 = xyz2.toCIELab( wp );

        DifferenceAlgorithm algo = MatchingFactory.getInstance().getDefaultAlgorithm();

        ColorDifference cdiff = algo.compute( lab1, lab2 );

        double diff = cdiff.getValue( "DeltaE" );
        assertTrue( "Delta E is " + diff, diff < MAXIMUM_DELTAE_TOLERANCE );
    }

    private Reflectance[] retrieve( InputStream in )
        throws Exception
    {
        Reflectance[] refls = m_Importer.doImport( in, new HashMap<String, String>() );
        return refls;
    }

    public class MiniReflectanceImpl
        implements Reflectance
    {
        private String m_Name;
        private Spectrum m_Spectrum;
        private Map m_Conditions;
        private Map m_Properties;
        private Number m_UID;
        private String m_Type;

        private MiniReflectanceImpl( String name, Spectrum spectrum )
        {
            m_Name = name;
            m_Spectrum = spectrum;
            m_Conditions = new HashMap();
            m_Properties = new HashMap();
            m_UID = null;
        }

        public String getName()
        {
            return m_Name;
        }

        public Spectrum getSpectrum()
        {
            return m_Spectrum;
        }

        public SortedMap getSpectrumMap()
        {
            return null;
        }

        public Map getConditions()
        {
            return m_Conditions;
        }

        public void setConditions( Map map )
        {

        }

        public Object getProperty( String name )
        {
            return m_Properties.get( name );
        }

        @Override
        public boolean hasProperty( String key )
        {
            return m_Properties.containsKey( key );
        }

        @Override
        public void setName( String name )
        {
            this.m_Name = name;
        }

        public void setSpectrum( Spectrum spectrum )
        {
            m_Spectrum = spectrum;
        }

        public void setSpectrumMap( SortedMap map )
        {

        }

        public Date getCreationDate()
        {
            return new Date();
        }

        @Override
        public void setProperty( String key, Object value )
        {
            m_Properties.put( key, value );
        }

        public String getType()
        {
            return m_Type;
        }

        public void setType( String type )
        {
            m_Type = type;
        }
    }

    private float[][] m_Spectra =
        {
            {
                0.7360f, 0.7787f, 0.8014f, 0.7840f, 0.7455f, 0.7071f, 0.6551f, 0.6594f,
                0.6548f, 0.7959f, 0.9222f, 0.9462f, 0.9490f, 0.9529f, 0.9663f, 0.9654f
            },

            {
                0.0878f, 0.0819f, 0.0866f, 0.1002f, 0.1186f, 0.1409f, 0.1625f, 0.1550f,
                0.1567f, 0.1326f, 0.1485f, 0.1324f, 0.1833f, 0.3918f, 0.6671f, 0.8762f
            }
        };

    private int[] m_Start =
        {
            400, 400
        };

    private int[] m_Interval =
        {
            20, 20
        };
}
