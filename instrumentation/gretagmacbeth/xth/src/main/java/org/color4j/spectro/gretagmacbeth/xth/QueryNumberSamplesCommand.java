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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class QueryNumberSamplesCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( QueryNumberSamplesCommand.class.getName() );

    /**
     * standard command name
     */
    public static final String NAME = "query number samples command";

    /**
     * standard white space
     */
    public static final char STANDARD_WS = ' ';

    public String getName()
    {
        return NAME;
    }

    public String construct()
    {
        return "GETTLIST";
    }

    public SpectroEvent interpret( byte[] response )
    {
        String string = convertWhiteSpace( response );
        m_Logger.finer( NAME + ": interpreting " + string );

        StringTokenizer tokens = new StringTokenizer( string );
        // KH Mar 22, 2004 - last token is some strange eof indicator
        int numberOfTokens = tokens.countTokens() - 1;

        m_Logger.finer( NAME + ": number of tokens == " + numberOfTokens );
        int[] indices = new int[ numberOfTokens ];
        try
        {
            for( int i = 0; i < numberOfTokens; i++ )
            {
                indices[ i ] = Integer.parseInt( tokens.nextToken() );
            }
        }
        catch( NumberFormatException nfe )
        {
            m_Logger.log( Level.SEVERE, NAME + ": error parsing integer", nfe );
        }

        if( numberOfTokens < 0 || indices[ 0 ] == -1 )
        {
            indices = new int[ 0 ];
        }

        // KH Mar 17, 2004 - response doesn't send a status, so creating an "all's good" status
        XTHStatus status = XTHStatus.create( "0000000000000000" );

        return new SpectroEvent( this, status, indices );
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
