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
 * Created on Mar 12, 2004
 *
 */
package org.color4j.spectro.drivers.sp62;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class QueryNumberSamplesCommand implements SpectroCommand
{
    public static final String NAME = "query number of samples command";
    static private Logger m_Logger = Logger.getLogger( QueryNumberSamplesCommand.class.getName() );

    public String getName()
    {
        return NAME;
    }

    public String construct()
    {
        return "00GM";
    }

    public SpectroEvent interpret( byte[] response )
    {
        String recv = new String( response );
        m_Logger.finer( "got number of samples. response == " + recv );

        String statusString = recv.substring( recv.length() - 6 );
        SP62Status status = SP62Status.create( statusString );
        Integer number;
        try
        {
            number = new Integer( recv.substring( 0, recv.length() - 6 ) );
        }
        catch( NumberFormatException nfe )
        {
            m_Logger.log( Level.SEVERE, "number format exception!  change substring ranges!", nfe );
            number = new Integer( 0 );
        }

        int[] indices = new int[ number.intValue() ];
        for( int i = 0; i < number.intValue(); i++ )
        {
            indices[ i ] = ( i + 1 );
        }

        return new SpectroEvent( this, status, indices );
    }
}
