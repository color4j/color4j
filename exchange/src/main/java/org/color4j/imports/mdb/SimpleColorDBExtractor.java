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

package org.color4j.imports.mdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.color4j.imports.ImportException;

/**
 */
public class SimpleColorDBExtractor
    implements ColorDBExtractor
{
    private final static int LOW = 360;
    private final static int INTERVAL = 10;
    private final static int SPECTRAL_COUNT = 40;

    private final static DecimalFormat SPECTRAL_COLUMN =
        new DecimalFormat( "SPECTRAL00" );

    protected String[] m_standardIDs;
    protected Map<String, String> m_standardNames;
    protected Map<String, Date> m_standardDates;
    protected Map m_standardDescriptions;
    protected Map m_standardAttributes;
    protected Map<String, TreeMap<Integer, Object>> m_standardSpectrums;
    protected Map<String, String> m_standardApertures;
    protected Map<String, String> m_standardLights;
    protected Map<String, String> m_standardSpeculars;
    protected Map<String, String> m_standardMeasureStatus;

    protected String[] m_batchIDs;
    protected Map<String, String> m_batchNames;
    protected Map<String, Date> m_batchDates;
    protected Map m_batchDescriptions;
    protected Map m_batchAttributes;
    protected Map<String, TreeMap<Integer, Double>> m_batchSpectrums;
    protected Map<String, String> m_batchApertures;
    protected Map m_batchLights;
    protected Map<String, String> m_batchSpeculars;
    protected Map<String, String> m_batchMeasureStatus;

    Connection m_conn;

    public SimpleColorDBExtractor( Connection conn )
    {
        m_conn = conn;
    }

    public String[] getBatchIDs()
        throws ImportException
    {
        if( m_batchIDs != null )
        {
            return m_batchIDs;
        }

        Statement st = null;
        try
        {
            st = m_conn.createStatement();

            String query =
                " SELECT TrialID FROM \"Trial Configuration Parameters\"";

            ResultSet rs = st.executeQuery( query );

            List<String> names = new ArrayList<String>();
            while( rs.next() )
            {
                names.add( rs.getString( 1 ) );
            }

            m_batchIDs = names.toArray( new String[ names.size() ] );
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( st );
        }

        return m_batchIDs;
    }

    public String getBatchName( String id )
        throws ImportException
    {
        if( m_batchNames == null )
        {
            m_batchNames = new HashMap<String, String>();
        }

        String batchName;
        if( ( batchName = m_batchNames.get( id ) ) != null )
        {
            return batchName;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT NAME FROM \"Trial Configuration Parameters\""
                + " WHERE SpectralOrColorimetricDataId = ?";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                batchName = rs.getString( 1 );
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_batchNames.put( id, batchName );

        return batchName;
    }

    public String[] getStandardIDs()
        throws ImportException
    {
        if( m_standardIDs != null )
        {
            return m_standardIDs;
        }

        Statement st = null;
        try
        {
            st = m_conn.createStatement();

            String query =
                " SELECT StandardID FROM \"Standard Configuration Parameters\"";

            ResultSet rs = st.executeQuery( query );

            List<String> names = new ArrayList<String>();
            while( rs.next() )
            {
                names.add( rs.getString( 1 ) );
            }

            m_standardIDs = names.toArray( new String[ names.size() ] );
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( st );
        }

        return m_standardIDs;
    }

    public String getStandardName( String id )
        throws ImportException
    {
        if( m_standardNames == null )
        {
            m_standardNames = new HashMap<String, String>();
        }

        String stdName;
        if( ( stdName = m_standardNames.get( id ) ) != null )
        {
            return stdName;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT NAME FROM \"Standard Configuration Parameters\""
                + " WHERE SpectralOrColorimetricDataId = ?";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                stdName = rs.getString( 1 );
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_standardNames.put( id, stdName );

        return stdName;
    }

    public TreeMap getBatchSpectrum( String id )
        throws ImportException
    {
        if( m_batchSpectrums == null )
        {
            m_batchSpectrums = new HashMap<String, TreeMap<Integer, Double>>();
        }

        TreeMap<Integer, Double> batSpec;
        if( ( batSpec = m_batchSpectrums.get( id ) ) != null )
        {
            return batSpec;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT A.* FROM \"Spectral Data\" A, \"Trial Configuration Parameters\" B"
                + " WHERE B.TrialID = ?"
                + " AND A.\"Spectral Data ID\" = B.SpectralOrColorimetricDataId";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                batSpec = new TreeMap<Integer, Double>();
                for( int i = 0; i < SPECTRAL_COUNT; i++ )
                {
                    int wavelength = LOW + ( i * INTERVAL );
                    double reading = rs.getDouble( SPECTRAL_COLUMN.format( i + 1 ) );
                    batSpec.put( wavelength, reading );
                }
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_batchSpectrums.put( id, batSpec );

        return batSpec;
    }

    public TreeMap getStandardSpectrum( String id )
        throws ImportException
    {
        if( m_standardSpectrums == null )
        {
            m_standardSpectrums = new HashMap<String, TreeMap<Integer, Object>>();
        }

        TreeMap<Integer, Object> stdSpec = null;
        if( ( stdSpec = m_standardSpectrums.get( id ) ) != null )
        {
            return stdSpec;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT A.* FROM \"Spectral Data\" A, \"Standard Configuration Parameters\" B"
                + " WHERE B.StandardID = ?"
                + " AND A.\"Spectral Data ID\" = B.SpectralOrColorimetricDataId";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                stdSpec = new TreeMap<Integer, Object>();
                for( int i = 0; i < SPECTRAL_COUNT; i++ )
                {
                    int wavelength = LOW + ( i * INTERVAL );
                    double reading = rs.getDouble( SPECTRAL_COLUMN.format( i + 1 ) );
                    stdSpec.put(wavelength,reading );
                }
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_standardSpectrums.put( id, stdSpec );

        return stdSpec;
    }

    protected void closeStatement( Statement st )
        throws ImportException
    {
        try
        {
            if( st != null )
            {
                st.close();
            }
        }
        catch( SQLException e )
        {
        }
    }

    public String getBatchDescription( String name )
        throws ImportException
    {
        return null;
    }

    public String getStandardDescription( String name )
        throws ImportException
    {
        return null;
    }

    public Date getBatchCreationDate( String id )
        throws ImportException
    {
        if( m_batchDates == null )
        {
            m_batchDates = new HashMap<String, Date>();
        }

        Date batdate;
        if( ( batdate = m_batchDates.get( id ) ) != null )
        {
            return batdate;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT A.Spec_DateMeasTaken FROM \"Spectral Data\" A, \"Trial Configuration Parameters\" B"
                + " WHERE B.TrialID = ?"
                + " AND A.\"Spectral Data ID\"=B.SpectralOrColorimetricDataId";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                batdate = rs.getTimestamp( 1 );
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_batchDates.put( id, batdate );

        return batdate;
    }

    public Date getStandardCreationDate( String id )
        throws ImportException
    {
        if( m_standardDates == null )
        {
            m_standardDates = new HashMap<String, Date>();
        }

        Date stddate;
        if( ( stddate = m_standardDates.get( id ) ) != null )
        {
            return stddate;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT A.Spec_DateMeasTaken FROM \"Spectral Data\" A, \"Standard Configuration Parameters\" B"
                + " WHERE B.StandardID = ?"
                + " AND A.\"Spectral Data ID\"=B.SpectralOrColorimetricDataId";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                stddate = rs.getTimestamp( 1 );
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_standardDates.put( id, stddate );

        return stddate;
    }

    public String getBatchAperture( String id )
        throws ImportException
    {
        if( m_batchApertures == null )
        {
            m_batchApertures = new HashMap<String, String>();
        }

        String bataper;
        if( ( bataper = m_batchApertures.get( id ) ) != null )
        {
            return bataper;
        }

        bataper = getBatchMeasuredStatus( id );
        m_batchApertures.put( id, bataper );

        return extractAperture( bataper );
    }

    public Map getBatchAttributes( String name )
        throws ImportException
    {
        return new HashMap();
    }

    public String getBatchLightFilter( String name )
        throws ImportException
    {
        return "Inc";
    }

    public String getBatchSpecular( String id )
        throws ImportException
    {
        if( m_batchSpeculars == null )
        {
            m_batchSpeculars = new HashMap<String, String>();
        }

        String batspec;
        if( ( batspec = m_batchSpeculars.get( id ) ) != null )
        {
            return batspec;
        }

        batspec = getBatchMeasuredStatus( id );
        m_batchSpeculars.put( id, batspec );

        return extractSpecular( batspec );
    }

    public String getStandardAperture( String id )
        throws ImportException
    {
        if( m_standardApertures == null )
        {
            m_standardApertures = new HashMap<String, String>();
        }

        String stdaper;
        if( ( stdaper = m_standardApertures.get( id ) ) != null )
        {
            return stdaper;
        }

        stdaper = getBatchMeasuredStatus( id );
        m_standardApertures.put( id, stdaper );

        return extractAperture( stdaper );
    }

    public Map getStandardAttributes( String name )
        throws ImportException
    {
        return new HashMap();
    }

    public String getStandardLightFilter( String id )
        throws ImportException
    {
        if( m_standardLights == null )
        {
            m_standardLights = new HashMap<String, String>();
        }

        String stdlite;
        if( ( stdlite = m_standardApertures.get( id ) ) != null )
        {
            return stdlite;
        }

        stdlite = getBatchMeasuredStatus( id );
        m_standardLights.put( id, stdlite );

        return extractLightFilter( stdlite );
    }

    public String getStandardSpecular( String name )
        throws ImportException
    {
        if( m_standardSpeculars == null )
        {
            m_standardSpeculars = new HashMap<String, String>();
        }

        String stdspec;
        if( ( stdspec = m_standardSpeculars.get( name ) ) != null )
        {
            return stdspec;
        }

        stdspec = getStandardMeasuredStatus( name );
        m_standardSpeculars.put( name, stdspec );

        return extractSpecular( stdspec );
    }

    protected String getStandardMeasuredStatus( String id )
        throws ImportException
    {
        if( m_standardMeasureStatus == null )
        {
            m_standardMeasureStatus = new HashMap<String, String>();
        }

        String stdmeasurestatus;
        if( ( stdmeasurestatus = m_standardMeasureStatus.get( id ) ) != null )
        {
            return stdmeasurestatus;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT A.Spec_MeasuredStatus FROM \"Spectral Data\" A, \"Standard Configuration Parameters\" B"
                + " WHERE B.StandardID = ?"
                + " AND A.\"Spectral Data ID\"=B.SpectralOrColorimetricDataId";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                stdmeasurestatus = rs.getString( 1 );
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_standardMeasureStatus.put( id, stdmeasurestatus );

        return stdmeasurestatus;
    }

    protected String getBatchMeasuredStatus( String id )
        throws ImportException
    {
        if( m_batchMeasureStatus == null )
        {
            m_batchMeasureStatus = new HashMap<String, String>();
        }

        String batmeasurestatus;
        if( ( batmeasurestatus = m_batchMeasureStatus.get( id ) ) != null )
        {
            return batmeasurestatus;
        }

        PreparedStatement ps = null;
        try
        {
            String query =
                " SELECT A.Spec_MeasuredStatus FROM \"Spectral Data\" A, \"Trial Configuration Parameters\" B"
                + " WHERE B.TrialID = ?"
                + " AND A.\"Spectral Data ID\"=B.SpectralOrColorimetricDataId";

            ps = m_conn.prepareStatement( query );
            ps.setString( 1, id );

            ResultSet rs = ps.executeQuery();

            if( rs.next() )
            {
                batmeasurestatus = rs.getString( 1 );
            }
        }
        catch( SQLException e )
        {
            throw new ImportException( "", e );
        }
        finally
        {
            closeStatement( ps );
        }

        m_batchMeasureStatus.put( id, batmeasurestatus );

        return batmeasurestatus;
    }

    protected String extractLightFilter( String str )
    {
        if( str == null )
        {
            return null;
        }

        char c = str.charAt( 3 );

        if( c == 'O' )
        {
            return "UVE";
        }
        else if( c == 'P' )
        {
            return "UVI";
        }

        return null;
    }

    protected String extractSpecular( String str )
    {
        if( str == null )
        {
            return null;
        }

        char c = str.charAt( 2 );

        if( c == 'I' )
        {
            return "SCI";
        }
        else if( c == 'E' )
        {
            return "SCE";
        }

        return null;
    }

    protected String extractAperture( String str )
    {
        if( str == null )
        {
            return null;
        }

        char c = str.charAt( 5 );

        if( c == 'S' )
        {
            return "SAV";
        }
        else if( c == 'L' )
        {
            return "LAV";
        }

        return null;
    }

    public void refresh()
    {
        m_standardNames = null;
        m_standardDates = null;
        m_standardDescriptions = null;
        m_standardAttributes = null;
        m_standardSpectrums = null;
        m_standardApertures = null;
        m_standardLights = null;
        m_standardSpeculars = null;
        m_standardMeasureStatus = null;

        m_batchNames = null;
        m_batchDates = null;
        m_batchDescriptions = null;
        m_batchAttributes = null;
        m_batchSpectrums = null;
        m_batchApertures = null;
        m_batchLights = null;
        m_batchSpeculars = null;
        m_batchMeasureStatus = null;
    }
}
