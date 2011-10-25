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

package org.color4j.spectro.drivers.sp62;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

public abstract class RetrieveReflectanceCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( RetrieveReflectanceCommand.class.getName() );

    private boolean m_Spec;

    public RetrieveReflectanceCommand( boolean spec )
    {
        m_Spec = spec;
    }

    public abstract String getName();

    public abstract String construct();

    /**
     * allows subclasses to access information on specular inclusion
     *
     * @return
     */
    protected boolean specularInc()
    {
        return m_Spec;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String recv = new String( received );
        // KH Mar 12, 2004 -  filtering out unreadable characters
        StringBuffer buffer = new StringBuffer( recv.length() );
        for( int i = 0; i < recv.length(); i++ )
        {
            char curr = recv.charAt( i );
//            m_Logger.debug( "character " + i + " == " + curr );
            if( !Character.isWhitespace( curr ) && curr != ',' )
            {
                buffer.append( curr );
            }
            else
            {
                buffer.append( " " );
            }
        }
        recv = buffer.toString();
        m_Logger.finer( "recv == " );
        m_Logger.finer( recv );

        String statusString = recv.substring( recv.length() - 6 );
        SP62Status status = SP62Status.create( statusString );
        SpectroSettings settings = new SpectroSettings();
        settings.setSpecular( m_Spec );

        String data = recv.substring( 0, recv.length() - 6 );
        StringTokenizer tokens = new StringTokenizer( data );

        String current;
        double interval = 10;
        Map values = new TreeMap();
        int dataCount = 0;

        for( double wavelength = 400; tokens.hasMoreTokens(); wavelength += interval )
        {
            current = tokens.nextToken();
            try
            {
                double value = Double.parseDouble( current );
                value /= 100.00;

                m_Logger.finer( "Values : " + wavelength + ", " + value );
                values.put( new Double( wavelength ), new Double( value ) );
            }
            catch( NumberFormatException numEx )
            {
                m_Logger.log( Level.SEVERE, getName() + " : Invalid data value ", numEx );
                status.addError( "invalid data value" );
            }

            dataCount++;

            if( dataCount > 31 )
            {
                m_Logger.severe( "more than 31 values" );
                break;
            }
        }

        m_Logger.info( "Data retrieval complete" );

        SP62Reading reading = new SP62Reading( status, settings, values );

        return new SpectroEvent( this, reading );
    }
}
