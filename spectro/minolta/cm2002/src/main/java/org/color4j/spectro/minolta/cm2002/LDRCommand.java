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
 * LDRCommand.java
 *
 * Created on October 14, 2002, 3:27 PM
 */

package org.color4j.spectro.minolta.cm2002;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class LDRCommand implements SpectroCommand
{
    private String m_Colormode;
    private String[] m_Tolerance;

    /**
     * Creates a new instance of LDRCommand
     */
    public LDRCommand()
    {
    }

    public String construct()
    {
        return "LDR";
    }

    public String getName()
    {
        return "Limit Data Request Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        if( sTok.countTokens() < 2 )
        {
            CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
        else
        {
            try
            {

                String statusString = sTok.nextToken();
                CM2002Status status = CM2002Status.create( statusString );

                m_Colormode = sTok.nextToken();

                m_Tolerance = new String[ sTok.countTokens() ];

                for( int i = 0; i < sTok.countTokens(); i++ )
                {
                    m_Tolerance[ i ] = sTok.nextToken();
                }

                return new SpectroEvent( this, status );
            }
            catch( NoSuchElementException exception )
            {
                CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
                return new SpectroEvent( this, errstatus );
            }
        }
    }

    public String getColormode()
    {
        return m_Colormode;
    }

    public String[] getTolerance()
    {
        return m_Tolerance;
    }
}
