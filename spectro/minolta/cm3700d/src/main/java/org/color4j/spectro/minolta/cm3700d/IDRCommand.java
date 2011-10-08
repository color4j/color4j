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
 * OISCommand.java
 *
 * Created on October 14, 2002, 3:12 PM
 */

package org.color4j.spectro.minolta.cm3700d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class IDRCommand implements SpectroCommand
{
    private String m_model;
    private String m_rom;
    private String m_serialnumber;

    /**
     * Creates a new instance of OISCommand
     */
    public IDRCommand()
    {
    }

    public String construct()
    {
        return "IDR";
    }

    public String getName()
    {
        return "Instrument Identification Request Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );

        response = response.replaceAll( "\r\n", "" );
        StringTokenizer sTok = new StringTokenizer( response, "," );

        if( sTok.countTokens() == 8 )
        {
            CM3700dStatus status = CM3700dStatus.create( sTok.nextToken() );

            if( status.isSuccess() )
            {
                m_model = sTok.nextToken();

                if( m_model.equals( " 1" ) )
                {
                    // This is a CM3700d
                }
                else
                {
                    CM3700dStatus errstatus = CM3700dStatus.create( "NOT_VALID_CM500" );
                    return new SpectroEvent( this, errstatus );
                }

                m_rom = sTok.nextToken() + "00";
                m_serialnumber = sTok.nextToken();
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM3700dStatus errstatus = CM3700dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public String getModel()
    {
        return m_model;
    }

    public String getROM()
    {
        return m_rom;
    }

    public String getSerialNumber()
    {
        return m_serialnumber;
    }
}
