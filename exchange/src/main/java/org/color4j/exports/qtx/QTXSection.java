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

package org.color4j.exports.qtx;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.color4j.colorimetry.Spectrum;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.exports.ExportException;

class QTXSection
{
    public static final String STANDARD_DATA_INIT = "STANDARD_DATA 0";
    public static final String BATCH_DATA = "BATCH_DATA";
    private static Map<String, String> m_Filters;
    private static Map<String, String> m_Apertures;

    private String m_Header;
    private String m_Standard;
    private boolean m_Batch;
    private Reflectance m_Reflectance;
    private HashMap<String, String> m_Attributes = new HashMap<String, String>();

    private PrintWriter m_Out;

    static
    {
        m_Filters = new HashMap<String, String>();
        m_Filters.put( "UV Inc", "UV Inc" );
        m_Filters.put( "UV Cal", "UV Cal" );
        //m_Filters.put( "360 nm", "360 nm");
        //m_Filters.put( "380 nm", "380 nm");
        m_Filters.put( "400 nm", "400 nm" );
        m_Filters.put( "420 nm", "420 nm" );
        //m_Filters.put( "440 nm", "440 nm");
        m_Filters.put( "460 nm", "460 nm" );
        //m_Filters.put( "480 nm", "480 nm");
        m_Filters.put( "Custom", "Custom" );
        m_Filters.put( "UVAD1", "UVAD1" );
        m_Filters.put( "UVAD2", "UVAD2" );
        m_Filters.put( "P", "P" );
        m_Filters.put( "U", "U" );
        m_Filters.put( "D65", "D65" );

        /*
        m_Filters.put( "360 nm", "UV 360");
        m_Filters.put( "380 nm", "UV 380");
        m_Filters.put( "400 nm", "UV 400");
        m_Filters.put( "420 nm", "UV 420");
        m_Filters.put( "440 nm", "UV 440");
        m_Filters.put( "460 nm", "UV 460");
        m_Filters.put( "480 nm", "UV 480");
        */
        m_Apertures = new HashMap<String, String>();
        m_Apertures.put( "XUSAV", "XUSAV" );
        m_Apertures.put( "USAV", "USAV" );
        m_Apertures.put( "VSAV", "VSAV" );
        m_Apertures.put( "SAV", "SAV" );
        m_Apertures.put( "MAV", "MAV" );
        m_Apertures.put( "LAV", "LAV" );
        m_Apertures.put( "XLAV", "XLAV" );
    }

    QTXSection( String standardname, Reflectance refl, String header )
    {
        m_Standard = standardname;
        m_Batch = m_Standard != null;
        header = header.trim();

        if( !m_Batch &&
            !header.toLowerCase().contains( "standard" ) ) // Check if Standard data
        {
            throw new IllegalArgumentException( "If a standardname is given, the header must be a STANDARD header." );
        }

        if( !header.startsWith( "[" ) )
        {
            header = "[" + header + "]";
        }

        m_Header = header;
        m_Reflectance = refl;
    }

//    QTXSection( String standardname, AttributedReflectance refl, String header )
//    {
//        this( standardname, refl.getReflectance(), header );
//        m_Attributes = refl.getEntityAttributes();
//    }

    void doExport( OutputStream stream )
        throws ExportException
    {
        m_Out = new PrintWriter( stream, true );
        m_Out.println( m_Header );
        Spectrum spectrum = m_Reflectance.getSpectrum();
        if( spectrum == null )
        {
            throw new ExportException( "Reflectance contains a null Spectrum." );
        }
        double[] v = spectrum.getValues();

        if( m_Batch )
        {
            //Note: This can not use the out() method, since it is
            // a STD_ key, even though it is in a Batch color.
            m_Out.print( "STD_NAME=" );
            m_Out.println( m_Standard );
        }
        Date createDate = m_Reflectance.getCreationDate();
        if( createDate == null ) // only for local FS case
        {
            createDate = new Date();
        }
        out( "DATETIME", createDate.getTime() / 1000 );
        out( "NAME", m_Reflectance.getName() );
        out( "REFLINTERVAL", spectrum.getInterval() );
        out( "REFLLOW", spectrum.getShortestWavelength() );
        out( "REFLPOINTS", v.length );

        String viewing = "%R ";

        Map cond = m_Reflectance.getConditions();
        if( cond == null )
        {
            cond = new HashMap();
        }

        String aperture = m_Apertures.get( cond.get( Reflectance.CONDITION_APERTURE ) );
        if( aperture == null )
        {
            aperture = "SAV";
        }
        viewing = viewing + aperture + " ";
        if( "SCI".equals( cond.get( Reflectance.CONDITION_SPECULAR ) ) ) //"true".equals( cond.get( Reflectance.CONDITION_SPECULAR ) ) ||
        {
            viewing = viewing + "SCI ";
        }
        else
        {
            viewing = viewing + "SCE ";
        }

        String rawFilter = (String) cond.get( Reflectance.CONDITION_LIGHTFILTER );
        String filter = m_Filters.get( rawFilter );
        if( filter == null )
        {
            viewing = viewing + "UV Inc";
        }
        else
        {
            viewing = viewing + filter;
        }
        out( "VIEWING", viewing );

        StringBuffer refldata = new StringBuffer();

        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setMinimumFractionDigits( 2 );
        df.setMaximumFractionDigits( 4 );
        df.setMinimumIntegerDigits( 1 );
        df.setMaximumIntegerDigits( 3 );

        for( int i = 0; i < v.length; i++ )
        {
            if( i > 0 )
            {
                refldata.append( "," );
            }
            refldata.append( df.format( ( v[ i ] * 100 ) ) );
        }
        out( "R", refldata.toString() );

//        exportProperties();
        exportAttributes();
    }
//	@TODO: Clean up. ML - 05/08/2003
//    private void out( String key, long value )
//    {
//        out( key, new Long( value ).toString() );
//    }

    private void out( String key, String value )
    {
        if( m_Batch && !m_Header.equals( "[" + STANDARD_DATA_INIT + "]" ) )
        {
            m_Out.print( "BAT_" );
        }
        else
        {
            m_Out.print( "STD_" );
        }
        m_Out.print( key );
        m_Out.print( "=" );
        m_Out.print( value );
        m_Out.println();
    }

    private void out( String key, Number value )
    {
        if( m_Batch && !m_Header.equals( "[" + STANDARD_DATA_INIT + "]" ) )
        {
            m_Out.print( "BAT_" );
        }
        else
        {
            m_Out.print( "STD_" );
        }
        m_Out.print( key );
        m_Out.print( "=" );
        m_Out.print( value );
        if( value != null )
        {
            m_Out.print( "," );
        }
        m_Out.println();
    }

    private void exportAttributes()
    {
        if( m_Attributes != null )
        {

            for( Map.Entry<String, String> entry : m_Attributes.entrySet() )
            {
                String key = entry.getKey();
                String value = entry.getValue();
                out( key, value );
            }
        }
    }
}
