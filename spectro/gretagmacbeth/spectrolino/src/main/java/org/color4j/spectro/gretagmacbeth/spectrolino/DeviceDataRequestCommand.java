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
 * DeviceDataRequestCommand.java
 *
 * Created on July 19, 2002, 4:00 PM
 */

package org.color4j.spectro.gretagmacbeth.spectrolino;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author chc
 */
public class DeviceDataRequestCommand implements SpectroCommand
{

    protected String serialno;

    static private Logger m_Logger = Logger.getLogger( SpectrolinoStatus.class.getName() );

    static
    {
    }

    /**
     * Creates a new instance of DeviceDataRequestCommand
     */
    public DeviceDataRequestCommand()
    {
    }

    public String getName()
    {
        return "Device Data Request Command";
    }

    public String construct()
    {
        String command;

        command = "; 181 ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String m_DeviceNameType = new String();
        String m_DeviceNumberType = new String();
        String m_ArticleNumberType = new String();
        String m_SerialNumberType = new String();
        String m_SoftwareReleaseType = new String();

        String response = new String( received );

        String rawresponse = response.replaceFirst( "\r\n", "" );

        SpectroStatus status = SpectrolinoStatus.create( "SUCCESS" );

        if( Pattern.matches( ": 182 .*", rawresponse ) )
        {
            StringTokenizer tokens = new StringTokenizer( rawresponse.replaceFirst( ": 182 ", "" ), " " );

            try
            {
                for( int i = 0; i < 18; i++ )
                {
                    m_DeviceNameType = m_DeviceNameType + (char) Integer.parseInt( tokens.nextToken() );
                }
                m_DeviceNumberType = tokens.nextToken();
                for( int i = 0; i < 8; i++ )
                {
                    m_ArticleNumberType = m_ArticleNumberType + (char) Integer.parseInt( tokens.nextToken() );
                }
                m_SerialNumberType = tokens.nextToken();
                for( int i = 0; i < 12; i++ )
                {
                    m_SoftwareReleaseType = m_SoftwareReleaseType + (char) Integer.parseInt( tokens.nextToken() );
                }

                m_Logger.info( "Device Name : " + m_DeviceNameType );
                m_Logger.info( "Device Name Number : " + m_DeviceNumberType );
                m_Logger.info( "Article Number : " + m_ArticleNumberType );
                m_Logger.info( "Software Release : " + m_SoftwareReleaseType );

                //int loSerialNo = Integer.parseInt( m_SerialNumberType.substring(0 , 2) );
                //int hiSerialNo = Integer.parseInt( m_SerialNumberType.substring(2 , 4) );

                //int tempserialno = hiSerialNo << 16 | loSerialNo;

                //serialno = ""+tempserialno;
                serialno = m_SerialNumberType;

                m_Logger.info( "Serial Number : " + serialno );

                // Now check whether is a SpectroLino or not !
                if( m_DeviceNumberType.equals( "32" ) )
                {

                }
                else
                {
                    SpectroStatus errstatus = SpectrolinoStatus.create( "NOT_VALID_SPECTROLINO" );
                    return new SpectroEvent( this, errstatus );
                }
            }
            catch( NoSuchElementException elEx )
            {
                //status = SpectrolinoStatus.create("UNKNOWN_RESPONSE");
                return null;
            }
            catch( IndexOutOfBoundsException boundEx )
            {
                //status = SpectrolinoStatus.create("UNKNOWN_RESPONSE");
                return null;
            }
        }
        else
        {
            //status = SpectrolinoStatus.create("UNKNOWN_RESPONSE");
            return null;
        }

        return new SpectroEvent( this, status );
    }

    public String getSerialNo()
    {
        return serialno;
    }
}
