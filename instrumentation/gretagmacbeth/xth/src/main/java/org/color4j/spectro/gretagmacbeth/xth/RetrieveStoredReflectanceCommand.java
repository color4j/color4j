/*
 * Copyright (c) 2011 Niclas Hedhman.
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
 * Created on Mar 17, 2004
 *
 */
package org.color4j.spectro.gretagmacbeth.xth;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

/**
 * @author devteam
 *         this class retrieves BOTH trials and standards and interprets both exactly the same
 */
public class RetrieveStoredReflectanceCommand implements SpectroCommand
{
    private static Logger m_Logger = Logger.getLogger( RetrieveStoredReflectanceCommand.class.getName() );

    public static final int START_WL = 360;
    public static final int END_WL = 750;
    public static final int INTERVAL = 10;
    public static final String NAME = "retrieve stored reflectance command";
    public static final String TYPE_TRIAL = "trial";
    public static final String TYPE_STANDARD = "standard";

    /**
     * standard white space
     */
    public static final char STANDARD_WS = ' ';

    private int m_Position = 0;
    private String m_Type;
    private SpectroSettings m_SpectroSettings = null;

    /**
     * @param index - position of record in the XTH machine; can be either standard or trial
     * @param type  - should pick from the available types above
     */
    public RetrieveStoredReflectanceCommand( int index, String type )
    {
        m_Position = index;
        m_Type = type;
    }

    public String getName()
    {
        return NAME;
    }

    public String getReflectanceType()
    {
        return m_Type;
    }

    public String construct()
    {
        return "GETREC " + m_Position;
    }

    public SpectroSettings getSpectroSettings()
    {
        return m_SpectroSettings;
    }

    public SpectroEvent interpret( byte[] response )
    {
//        String string = convertWhiteSpace(response);

        String string = new String( response );
        StringTokenizer tokens = new StringTokenizer( string, "\r\n" );
        int numTokens = tokens.countTokens();
        m_Logger.finer( NAME + ": number of tokens == " + numTokens );

        // KH Mar 17, 2004 - burn the name token, should have tokens.countTokens - 1
        m_Logger.finer( NAME + ": burning token == " + tokens.nextToken() );

        //TODO: KH Mar 22, 2004 - should convert all white space to standard space and count tokens from there?
        // KH Mar 22, 2004 - inconsistencies in tokens...  should have 20, seems trials do not have an extra token between the name and the status
        if( numTokens == 20 )
        {
            m_Logger.finer( NAME + ": burning token == " + tokens.nextToken() );
        }

        // KH Mar 17, 2004 - get status
        String status = tokens.nextToken();
        SpectroSettings settings = new SpectroSettings();

        // KH Mar 17, 2004 - set settings        
        if( status.charAt( 3 ) == '0' )
        {
            settings.setSpecular( true );
        }
        else if( status.charAt( 3 ) == '1' )
        {
            settings.setSpecular( false );
        }

        if( status.charAt( 6 ) == '2' )
        {
            settings.setAperture( new MediumAperture() );
            settings.setLensPosition( new MediumAreaView() );
        }
        else if( status.charAt( 6 ) == '3' )
        {
            settings.setAperture( new SmallAperture() );
            settings.setLensPosition( new SmallAreaView() );
        }
        else
        {
            m_Logger.severe( "Invalid command generated" );
        }

        // KH Mar 17, 2004 - arbitrary?  no doc on this
        settings.setLightFilter( new UVIncludedLightFilter() );

        // KH Mar 17, 2004 - create status
        XTHStatus newStatus = XTHStatus.create( status );

        /* 
         * KH Mar 17, 2004 - next 10 tokens should be reflectance data
         * range is 360 to 750nm, 10nm intervals, 4 values each line, each value separated by a space
         */
        String line;
        StringTokenizer vals;
        Map refls = new TreeMap();
        double current = (double) START_WL;
        double data;
        m_Logger.finer( "wavelength    value" );
        for( int i = 0; i < 10; i++ )
        {
            if( !tokens.hasMoreTokens() )
            {
                m_Logger.severe( "unexpected parsing error; should have more data" );
                newStatus.addError( "unexpected parsing error; should have more data" );
            }

            line = tokens.nextToken();
            vals = new StringTokenizer( line, " " );
            for( int j = 0; j < 4; j++ )
            {
                if( !vals.hasMoreTokens() )
                {
                    m_Logger.severe( "unexpected parsing error; should have more data" );
                    newStatus.addError( "unexpected parsing error; should have more data" );
                }

                data = Double.parseDouble( vals.nextToken() ) / 100;
                m_Logger.finer( current + "    " + data );
                try
                {
                    refls.put( new Double( current ), new Double( data ) );
                }
                catch( NumberFormatException nfe )
                {
                    m_Logger.log( Level.SEVERE, "problems parsing data", nfe );
                    newStatus.addError( "problems parsing data" );
                }

                current += (double) INTERVAL;
            }
        }
        m_SpectroSettings = settings;
        // KH Mar 17, 2004 - has more tokens after reflectance data, but we don't care for it
        return new SpectroEvent( this, new XTHReading( newStatus, settings, refls ) );
    }

    /*
    * for converting all white space into "standard" white space for easier parsing
    */
    private String convertWhiteSpace( byte[] response )
    {
        StringBuffer toReturn = new StringBuffer( response.length );
        char temp;
        for( int i = 0; i < response.length; i++ )
        {
            temp = (char) response[ i ];
            m_Logger.finer( NAME + ": char at " + i + " == " + temp );
            if( Character.isWhitespace( temp ) )
            {
                toReturn.append( STANDARD_WS );
            }
            else
            {
                toReturn.append( temp );
            }
        }

        return toReturn.toString();
    }
}
