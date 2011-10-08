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

package org.color4j.spectro.hunter.lsxe;

import org.color4j.spectro.hunter.common.Hex;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class StandardizeModeCommand
    implements SpectroCommand
{
    private int filter;
    private int port;

    public StandardizeModeCommand( String uvFilter, String portPlate )
    {
        try
        {
            filter = Integer.parseInt( uvFilter );
        }
        catch( NumberFormatException numEx )
        {
            filter = -1;
        }

        try
        {
            port = Integer.parseInt( portPlate );
        }
        catch( NumberFormatException numEx )
        {
            port = -1;
        }
    }

    public String getName()
    {
        return "Standardize Mode Command";
    }

    public String construct()
    {
        StringBuffer command = new StringBuffer();

        command.append( "F" );
        command.append( "0000" );
        command.append( "0" );

        //UV Settings
        command.append( "000" );
        command.append( filter );

        //PortPlate Settings
        command.append( Hex.intToHexString( port, 4 ) );
        command.append( Hex.intToHexString( port, 4 ) );

        return command.toString();
    }

    public SpectroEvent interpret( byte[] received )
    {
        LSXEStatus status = LSXEStatus.create( new String( received ) );

        if( (int) received[ 8 ] != filter )
        {
            status.addWarning( "UNABLE_TO_SET_UV" );
        }

        String spotSize = new String( received, 9, 4 );
        String portPlate = new String( received, 13, 4 );

        if( Hex.hexStringToInt( spotSize ) != port )
        {
            status.addWarning( "UNABLE_TO_SET_LENS_FOCUS" );
        }
        else if( Hex.hexStringToInt( portPlate ) != port )
        {
            status.addWarning( "PORT_PLATE_LENS_FOCUS_MISMATCH" );
        }

        return new SpectroEvent( this, status );
    }
}
