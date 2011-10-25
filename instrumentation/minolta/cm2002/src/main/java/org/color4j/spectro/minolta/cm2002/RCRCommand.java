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
 * RCRCommand.java
 *
 * Created on October 14, 2002, 3:17 PM
 */

package org.color4j.spectro.minolta.cm2002;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class RCRCommand implements SpectroCommand
{
    private String m_correction;

    /**
     * Creates a new instance of RCRCommand
     */
    public RCRCommand()
    {
    }

    public String construct()
    {
        return "RCR";
    }

    public String getName()
    {
        return "Reflectance Correction Factor Request Command";
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
                m_correction = sTok.nextToken();
            }

            return new SpectroEvent( this, status );
        }
        catch( NoSuchElementException exception )
        {
            CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public String getCorrection()
    {
        return m_correction;
    }
}