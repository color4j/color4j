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

package org.color4j.spectro.hunter.cqxe;

import org.color4j.spectro.hunter.common.Hex;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class StandardizeModeCommand implements SpectroCommand
{
    public final static char NO_CHANGE = 'F';

    public final static char REFLECTANCE = '0';
    public final static char TOTAL_TRANSMITTANCE = '1';
    public final static char REGULAR_TRANSMITTANCE = '2';
    public final static char FORTY_FIVE_ZERO = '3';

    public final static char UV_EXCLUDED = '0';
    public final static char UV_NOMINAL = '1';
    public final static char UV_CALIBRATED = '2';

    public final static char LAV = '1';
    public final static char SAV = '2';

    public final static char SPIN = '1';
    public final static char SPEX = '2';

    private char filter;
    private int port;
    private boolean specular;

    public StandardizeModeCommand( char uvFilter, String portPlate, boolean specular )
    {
        filter = uvFilter;
        try
        {
            port = Integer.parseInt( portPlate );
        }
        catch( NumberFormatException numEx )
        {
            port = -1;
        }

        this.specular = specular;
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

        //Measurement Type
        command.append( REFLECTANCE );

        //UV Settings
        command.append( "000" );
        command.append( filter );

        //UV Insertion
        command.append( "0000" );

        //Lens Position
        command.append( "000" );
        if( this.port == 1000 )
        {
            command.append( LAV );
        }
        else
        {
            command.append( SAV );
        }

        //Specular Component
        command.append( "000" );
        if( this.specular )
        {
            command.append( SPIN );
        }
        else
        {
            command.append( SPEX );
        }

        //PortPlate Settings
        command.append( Hex.intToHexString( port, 4 ) );
        command.append( Hex.intToHexString( port, 4 ) );

        return command.toString();
    }

    public SpectroEvent interpret( byte[] received )
    {
        CQXEStatus status = CQXEStatus.create( new String( received ) );

        String spotSize = new String( received, 9, 4 );
        String portPlate = new String( received, 13, 4 );
        String specular = new String( received, 18, 4 );

        if( received[ 9 ] != this.filter )
        {
            status.addWarning( "UNABLE_TO_SET_UV" );
        }

        if( this.specular )
        {
            if( received[ 21 ] != SPIN )
            {
                status.addWarning( "UNABLE_TO_SET_SPECULAR" );
            }
        }
        else
        {
            if( received[ 21 ] != SPEX )
            {
                status.addWarning( "UNABLE_TO_SET_SPECULAR" );
            }
        }

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