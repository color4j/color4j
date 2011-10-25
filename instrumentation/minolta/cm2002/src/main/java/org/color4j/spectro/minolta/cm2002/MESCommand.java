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
 * MESCommand.java
 *
 * Created on October 14, 2002, 3:29 PM
 */

package org.color4j.spectro.minolta.cm2002;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */

public class MESCommand implements SpectroCommand
{
    private String m_Type;

    private String[] m_Spectral = new String[ 31 ];
    private String m_DataID;
    private String m_Comment;

    /**
     * Creates a new instance of MESCommand
     */
    public MESCommand( String Type )
    {
        m_Type = Type;
    }

    public String construct()
    {
        return "MES";
    }

    public String getName()
    {
        return "Spectral Measurement Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        try
        {
            String statusString = sTok.nextToken();

            CM2002Status status = CM2002Status.create( statusString );

            if( "measure".equals( m_Type ) && status.isSuccess() )
            {
                for( int i = 0; i < 31; i++ )
                {
                    m_Spectral[ i ] = sTok.nextToken();
                }

                m_DataID = sTok.nextToken();
                m_Comment = sTok.nextToken();

                return new SpectroEvent( this, status );
            }
            else if( "calibrate".equals( m_Type ) )
            {
                return new SpectroEvent( this, status );
            }
            else
            {
                status.addError( "MSG_INVALID_RETURN" );
                return new SpectroEvent( this, status );
            }
        }
        catch( NoSuchElementException exception )
        {
            CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public String getType()
    {
        return m_Type;
    }

    public String[] getSpectral()
    {
        return m_Spectral;
    }

    public String getDataID()
    {
        return m_DataID;
    }

    public String getComment()
    {
        return m_Comment;
    }
}
