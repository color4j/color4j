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
 * Created on 2003/7/7
 *
 */
package org.color4j.imports.mdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.ReflectanceImpl;
import org.color4j.colorimetry.Spectrum;
import org.color4j.imports.DBReflectanceImporter;
import org.color4j.imports.ImportException;

/**
 */
public class MDBImporter implements DBReflectanceImporter
{
    private static final String NAME = "MDB";
    private static final String EXT = "mdb";
    //    private static Logger m_Logger = Logger.getLogger( MDBImporter.class );
    private static final String STD_QUERY;
    private static final String TRIAL_QUERY;
    private static final int INTERVAL = 10;
    private static final int START = 360;
    private static final int POINTS = 40;

    static
    {
        StringBuffer query = new StringBuffer( 2000 );
        query.append( " SELECT *" );
        query.append( " FROM \"Standard Configuration Parameters\" STD" );
        query.append( ", \"Spectral Data\" SD" );
        query.append( " WHERE STD.SpectralOrColorimetricDataId=SD.\"Spectral Data ID\"" );
        STD_QUERY = query.toString();
        query = new StringBuffer( 2000 );
        query.append( " SELECT *" );
        query.append( " FROM \"Trial Configuration Parameters\" T" );
        query.append( ", \"Spectral Data\" SD" );
        query.append( " WHERE T.SpectralOrColorimetricDataId=SD.\"Spectral Data ID\"" );
        TRIAL_QUERY = query.toString();
    }

    public String getName()
    {
        return NAME;
    }

    public String getDefaultExtension()
    {
        return EXT;
    }

    public Reflectance[] doImport( String user, String password, String url, Map<String, String> template )
        throws IOException, ImportException
    {
        MDBDataSource mds = null;

        try
        {
            if( user == null && password == null )
            {
                mds = new MDBDataSource( url );
            }
            else
            {
                mds = new MDBDataSource( url, user, password );
            }

//            ColorDBExtractor extractor = mds.getColorDBExtractor();
//            return buildReflectance(extractor);
            return buildReflectances( mds.getConnection() );
        }
        catch( Exception e )
        {
            throw new ImportException( e.getMessage(), e );
        }
        finally
        {
            if( mds != null )
            {
                mds.close();
            }
        }
    }

//    /**
//     * @param extractor
//     * @return
//     */
//    private Reflectance[] buildReflectance(ColorDBExtractor extractor)
//        throws Exception
//    {
//        Collection reflectances = new ArrayList();
//
//        String[] stdIds = extractor.getStandardIDs();
//        for (int i = 0; i < stdIds.length; i++)
//        {
//            Reflectance rfl = new ReflectanceImpl();
//            String stdID = stdIds[i];
//            rfl.setName(extractor.getStandardName(stdID));
//            rfl.setDescription(extractor.getStandardDescription(stdID));
//            rfl.setProperties(extractor.getStandardAttributes(stdID));
//            //rfl.setSpectrumMap();
//
//            SortedMap sm = extractor.getStandardSpectrum(stdID);
//            double[] d = new double[sm.size()];
//            int j = 0;
//            for (Iterator iter = sm.values().iterator(); iter.hasNext();)
//            {
//                Double element = (Double) iter.next();
//                d[j++] = element.doubleValue();
//            }
//
//            Spectrum spectrum = Spectrum.create(360, 10, d);
//            rfl.setSpectrum(spectrum);
//
//            Map condition = new HashMap();
//            condition.put(
//                Reflectance.CONDITION_APERTURE,
//                extractor.getStandardAperture(stdID));
//            condition.put(
//                Reflectance.CONDITION_LIGHTFILTER,
//                extractor.getStandardLightFilter(stdID));
//            condition.put(
//                Reflectance.CONDITION_SPECULAR,
//                extractor.getStandardSpecular(stdID));
//            rfl.setConditions(condition);
//            if( rfl.getName() == null || rfl.getName().equals( "" ) )
//            {
//                m_Logger.debug( "Reflectance with no name found, id = " + stdID );
//            }
//            reflectances.add(rfl);
//        }
//
//        String[] batIds = extractor.getBatchIDs();
//        for (int i = 0; i < batIds.length; i++)
//        {
//            Reflectance rfl = new ReflectanceImpl();
//            String batID = batIds[i];
//            rfl.setName(extractor.getBatchName(batID));
//            rfl.setDescription(extractor.getBatchDescription(batID));
//            rfl.setProperties(extractor.getBatchAttributes(batID));
//            //rfl.setSpectrumMap(extractor.getBatchSpectrum(batches[i]));
//
//            SortedMap sm = extractor.getBatchSpectrum(batID);
//            double[] d = new double[sm.size()];
//            int j = 0;
//            for (Iterator iter = sm.values().iterator(); iter.hasNext();)
//            {
//                Double element = (Double) iter.next();
//                d[j++] = element.doubleValue();
//            }
//
//            Spectrum spectrum = Spectrum.create(360, 10, d);
//            rfl.setSpectrum(spectrum);
//
//            Map condition = new HashMap();
//            condition.put(
//                Reflectance.CONDITION_APERTURE,
//                extractor.getBatchAperture(batID));
//            condition.put(
//                Reflectance.CONDITION_LIGHTFILTER,
//                extractor.getBatchLightFilter(batID));
//            condition.put(
//                Reflectance.CONDITION_SPECULAR,
//                extractor.getBatchSpecular(batID));
//            rfl.setConditions(condition);
//
//            reflectances.add(rfl);
//        }
//
//        return (Reflectance[]) reflectances.toArray(new Reflectance[0]);
//    }

    private Reflectance[] buildReflectances( Connection conn )
        throws Exception
    {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery( STD_QUERY );
        Collection<Reflectance> reflectances = new ArrayList<Reflectance>();
        createReflectances( rs, reflectances );
        rs.close();
        rs = st.executeQuery( TRIAL_QUERY );
        createReflectances( rs, reflectances );
        rs.close();
        st.close();
        return reflectances.toArray( new Reflectance[ reflectances.size() ] );
    }

    private void createReflectances( ResultSet rs, Collection<Reflectance> reflectances )
        throws Exception
    {
//        int count = 0;
        while( rs.next() )
        {
            Reflectance rfl = new ReflectanceImpl();
            String name = rs.getString( "Name" );
            if( name == null )
            {
                // ML - If name is null, use the id as the name. Dec 2, 2003
                name = "ID - " + rs.getString( 1 );
            }
//            rfl.setName( name );
            String measuredCondition = rs.getString( "Spec_MeasuredStatus" );
            Map<String,String> conditions = new HashMap<String, String>();
            conditions.put( Reflectance.CONDITION_APERTURE, getAperture( measuredCondition ) );
            conditions.put( Reflectance.CONDITION_SPECULAR, getSpecular( measuredCondition ) );
            conditions.put( Reflectance.CONDITION_LIGHTFILTER, getLightFilter( measuredCondition ) );
            rfl.setConditions( conditions );
            Date creationDate = new Date( rs.getDate( "Spec_DateMeasTaken" ).getTime() );
//            rfl.setCreationDate( creationDate );
            // ML - use creation date for last modified date also. Dec 2, 2003
//            rfl.setLastModifiedDate( creationDate );
            int interval = INTERVAL; //rs.getInt( "Wavelength_Interval" );
            int start = START; // rs.getInt( "Starting_Wavelength" );
            int points = POINTS; //rs.getInt( "Number_of_Points" );
            double[] values = new double[ points ];
            for( int i = 0; i < points; i++ )
            {
                String specNo;
                if( i >= 9 )
                {
                    specNo = "" + ( i + 1 );
                }
                else
                {
                    specNo = "0" + ( i + 1 );
                }
                values[ i ] = rs.getDouble( "SPECTRAL" + specNo );
            }
            Spectrum spectrum = Spectrum.create( start, interval, values );
            rfl.setSpectrum( spectrum );
            reflectances.add( rfl );
        }
    }

    /**
     * @param condition
     *
     * @return
     */
    private String getSpecular( String condition )
    {
        char c = condition.charAt( 2 );
        if( c == 'E' )
        {
            return "SCE";
        }
        // ML - Default to SCI. Dec 2, 2003
        return "SCI";
    }

    private String getAperture( String condition )
    {
        char c = condition.charAt( 5 );
        if( c == 'S' )
        {
            return "SAV";
        }
        // ML - Default to LAV. Dec 2, 2003
        return "LAV";
    }

    private String getLightFilter( String condition )
    {
        char c = condition.charAt( 3 );
        if( c == 'O' )
        {
            return "UV Exc";
        }
        // ML - Defaulting to Inc. Dec 2, 2003
        return "UV Inc";
    }
}
