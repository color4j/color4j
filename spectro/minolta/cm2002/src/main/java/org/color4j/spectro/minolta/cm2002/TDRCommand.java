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
 * TDRCommand.java
 *
 * Created on October 14, 2002, 3:23 PM
 */

package org.color4j.spectro.minolta.cm2002;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class TDRCommand
    implements SpectroCommand
{
    private String m_Spectral;
    private String m_DataID;
    private String m_Comment;
    private String[] m_Colormode;

    /**
     * Creates a new instance of TDRCommand
     */
    public TDRCommand()
    {
    }

    public String construct()
    {
        return "TDR";
    }

    public String getName()
    {
        return "Target Data Request Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        try
        {
            String statusString = sTok.nextToken();

            CM2002Status status = CM2002Status.create( statusString );

            if( status.isSuccess() )
            {
                m_Spectral = sTok.nextToken();
                m_DataID = sTok.nextToken();
                m_Comment = sTok.nextToken();

                for( int i = 3; i < sTok.countTokens(); i++ )
                {
                    m_Colormode[ i - 3 ] = sTok.nextToken();
                }
            }

            return new SpectroEvent( this, status );
        }
        catch( NoSuchElementException exception )
        {
            CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public String getSpectral()
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

    public String[] getColormode()
    {
        return m_Colormode;
    }
}
