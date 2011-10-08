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
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.color4j.spectro.hunter.cqxe;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.color4j.spectro.hunter.common.Hex;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author devteam
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CapabilityCommand implements SpectroCommand
{
    public static final int SENSOR_FIRMWARE_NAME = 0;
    public static final int SERIAL_NUMBER = 1;
    public static final int SPECTRAL_DATA_RANGE = 2;
    public static final int POLYTUBE_INSTALLATION = 3;
    public static final int LCD_RESOLUTION = 4;

    protected static Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CapabilityCommand.class.getName() );
    }

    protected int m_Type;
    protected String m_Command;
    protected String m_Name;

    public CapabilityCommand( int type )
    {
        m_Type = type;
        switch( m_Type )
        {
        case SENSOR_FIRMWARE_NAME:
            m_Command = "#0000";
            m_Name = "Sensor and Firmware Name Capability Command";
            break;
        case SERIAL_NUMBER:
            m_Command = "#0001";
            m_Name = "Serial Number Capability Command";
            break;
        case SPECTRAL_DATA_RANGE:
            m_Command = "#0010";
            m_Name = "Spectral Data Range Capability Command";
            break;
        case POLYTUBE_INSTALLATION:
            m_Command = "#0020";
            m_Name = "Polytube Installation Capability Command";
            break;
        case LCD_RESOLUTION:
            m_Command = "#0030";
            m_Name = "LCD Resolution Capabilirt Command";
            break;
        }
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroCommand#getName()
      */
    public String getName()
    {
        return m_Name;
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroCommand#construct()
      */
    public String construct()
    {
        return m_Command;
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroCommand#interpret(byte[])
      */
    public SpectroEvent interpret( byte[] arg0 )
    {
        SpectroStatus status = new CQXEStatus();
        if( arg0.length >= 5 )
        {
            String response = new String( arg0 ).substring( 5 );

            switch( m_Type )
            {
            case SENSOR_FIRMWARE_NAME:
                interpretSensorFirmwareName( response, status );
                break;
            case SERIAL_NUMBER:
                interpretSerialNumber( response, status );
                break;
            case SPECTRAL_DATA_RANGE:
                interpretSpectralDataRange( response, status );
                break;
            case POLYTUBE_INSTALLATION:
                if( response.charAt( 0 ) == '0' )
                {
                    status.addMessage( "POLYTUBE INSTALLED" );
                }
                else if( response.charAt( 0 ) == '1' )
                {
                    status.addMessage( "POLYTUBE NOT INSTALLED" );
                }
                break;
            case LCD_RESOLUTION:
                interpretLCDResoluction( response, status );
                break;
            }
        }
        else
        {
            status.addError( "Invalid response" );
        }

//		javax.swing.JOptionPane.showMessageDialog( null, "Returning spectroevent " );
        return new SpectroEvent( this, status );
    }

    /**
     * @param response
     * @param status
     */
    private void interpretLCDResoluction( String response, SpectroStatus status )
    {
        String[] data = processInteger( response );
        if( data.length >= 2 )
        {
            status.addMessage( "COLUMNS:" + Hex.hexStringToInt( data[ 0 ] ) );
            status.addMessage( "ROWS:" + Hex.hexStringToInt( data[ 1 ] ) );
        }
    }

    private void interpretSpectralDataRange( String response, SpectroStatus status )
    {
        String[] data = processInteger( response );
        for( int i = 0; i < data.length; i++ )
        {
            switch( i )
            {
            case 0:
                status.addMessage( "LOW WAVELENGTH:" + Hex.hexStringToInt( data[ i ] ) );
                break;
            case 1:
                status.addMessage( "HI WAVELENGTH:" + Hex.hexStringToInt( data[ i ] ) );
                break;
            case 2:
                status.addMessage( "SPECTRAL POINTS: " + Hex.hexStringToInt( data[ i ] ) );
                break;
            case 3:
                status.addMessage( "INTERVAL:" + Hex.hexStringToInt( data[ i ] ) );
                break;
            }
        }
    }

    private void interpretSerialNumber( String response, SpectroStatus status )
    {
        String[] serialNumber = processStrings( response );
        if( serialNumber.length >= 1 )
        {
            m_Logger.info( "SERIAL:" + serialNumber[ 0 ] );
            status.addMessage( "SERIAL:" + serialNumber[ 0 ] );
        }
        else
        {
            m_Logger.info( "NO SERIAL FOUND" + response );
            status.addMessage( "NO SERIAL FOND" );
        }
    }

    private void interpretSensorFirmwareName( String response, SpectroStatus status )
    {
        String[] sensor_firmware = processStrings( response );
        if( sensor_firmware.length >= 2 )
        {
            status.addMessage( "SENSOR NAME:" + sensor_firmware[ 0 ] );
            status.addMessage( "FIRMWARE VER:" + sensor_firmware[ 1 ] );
        }
    }

    private String[] processStrings( String target )
    {
        ArrayList storage = new ArrayList();

        int pointer = 0;
        StringBuffer length = new StringBuffer();
        int strLength = 0;

        while( pointer < target.length() )
        {
            for( int i = 0; i < 3; i++ )
            {
                length.append( target.charAt( pointer++ ) );
            }

            strLength = Hex.hexStringToInt( length.toString() );

            StringBuffer str = new StringBuffer();
            for( int i = 0; ( i < strLength ) && ( pointer < target.length() ); i++ )
            {
                str.append( target.charAt( pointer++ ) );
            }

            storage.add( str.toString() );
        }

        String[] result = new String[ 0 ];
        result = (String[]) storage.toArray( result );

        return result;
    }

    private String[] processInteger( String target )
    {
        ArrayList storage = new ArrayList();

        StringBuffer strBuffer = new StringBuffer();
        for( int i = 0; i < target.length(); i++ )
        {
            if( i % 4 != 0 )
            {
                strBuffer.append( target.charAt( i ) );

                if( ( i + 1 ) >= target.length() )
                {
                    storage.add( strBuffer.toString() );
                    strBuffer = new StringBuffer();
                }
            }
            else
            {
                storage.add( strBuffer.toString() );
                strBuffer = new StringBuffer();
            }
        }

        String[] result = new String[ 0 ];
        return (String[]) storage.toArray( result );
    }
}
