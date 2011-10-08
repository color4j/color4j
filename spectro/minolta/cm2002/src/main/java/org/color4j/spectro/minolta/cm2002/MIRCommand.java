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
 * MIRCommand.java
 *
 * Created on October 14, 2002, 3:18 PM
 */

package org.color4j.spectro.minolta.cm2002;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class MIRCommand implements SpectroCommand
{
    private String m_Reference;
    private String m_Test;

    /**
     * Creates a new instance of MIRCommand
     */
    public MIRCommand()
    {
    }

    public String construct()
    {
        return "MIR";
    }

    public String getName()
    {
        return "Metamerism Illuminant Request Command";
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
                String ReferenceTest = sTok.nextToken();

                m_Reference = ReferenceTest.substring( 0, 2 );
                m_Test = ReferenceTest.substring( 2, 4 );
            }

            return new SpectroEvent( this, status );
        }
        catch( NoSuchElementException exception )
        {
            CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public String getReference()
    {
        return m_Reference;
    }

    public String getTest()
    {
        return m_Test;
    }
}

