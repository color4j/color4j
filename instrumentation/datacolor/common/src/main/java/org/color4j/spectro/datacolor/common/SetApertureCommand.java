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
 * SetApertureCommand.java
 *
 * Created on June 26, 2002, 3:30 PM
 */

package org.color4j.spectro.datacolor.common;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */
public class SetApertureCommand
    implements SpectroCommand
{
    private LensPosition m_lensPosition;
    private ResponseDecoder decoder;
    private char m_setlensPosition;

    /**
     * Creates a new instance of SetApertureCommand
     */
    public SetApertureCommand( LensPosition lensPosition, ResponseDecoder decoder )
    {
        m_lensPosition = lensPosition;
        this.decoder = decoder;
    }

    public String getName()
    {
        return "Set Aperture Command";
    }

    public String construct()
    {
        String command = new String();

        if( m_lensPosition.getName().equals( "XUSAV" ) )
        {
            command = "AA  ";
            m_setlensPosition = 'A';
        }
        else if( m_lensPosition.getName().equals( "XLAV" ) )
        {
            command = "AX  ";
            m_setlensPosition = 'X';
        }
        else if( m_lensPosition.getName().equals( "LAV" ) )
        {
            command = "AN  ";
            m_setlensPosition = 'N';
        }
        else if( m_lensPosition.getName().equals( "MAV" ) )
        {
            command = "AM  ";
            m_setlensPosition = 'M';
        }
        else if( m_lensPosition.getName().equals( "SAV" ) )
        {
            command = "AS  ";
            m_setlensPosition = 'S';
        }
        else if( m_lensPosition.getName().equals( "USAV" ) )
        {
            command = "AU  ";
            m_setlensPosition = 'U';
        }

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        StringTokenizer sTok = new StringTokenizer( response, "\r" + "\n" );

        boolean warning = false;
        boolean error = false;

        if( sTok.countTokens() != 1 )
        {
            warning = true;
        }

        //Create status:
        //First line is status string
        String status = sTok.nextToken();

        // Check the status string
        if( status.charAt( 13 ) == 'T' )
        {
            // movement time-out
            error = true;
        }
        else if( status.charAt( 13 ) == 'E' )
        {
            // aperture not supported
            error = true;
        }
        else
        {
            // Now check whether the set up are correct
            if( status.charAt( 1 ) == m_setlensPosition )
            {

            }
            else
            {
                // Error setting
                error = true;
            }
        }

        SpectroStatus spectroStatus = decoder.decode( status );
        if( error )
        {
            return null;
        }

        return new SpectroEvent( this, spectroStatus );
    }
}