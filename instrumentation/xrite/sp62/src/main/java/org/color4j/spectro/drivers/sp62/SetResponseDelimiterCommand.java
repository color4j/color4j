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

package org.color4j.spectro.drivers.sp62;

import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class SetResponseDelimiterCommand
    implements SpectroCommand
{

    String m_Delimiter;
    String m_Param;

    public SetResponseDelimiterCommand( String delimiter )
    {
        m_Delimiter = delimiter;

        if( delimiter.equals( "\r" ) )
        {
            m_Param = "00";
        }
        else if( delimiter.equals( "\r\n" ) )
        {
            m_Param = "01";
        }
        else if( delimiter.equals( "\n" ) )
        {
            m_Param = "02";
        }
        else
        {
            Logger logger = Logger.getLogger( SetAveragingCommand.class.getName() );
            logger.warning( "Invalid delimiter parameter:  " + delimiter );
            m_Param = "01";
        }
    }

    public String getName()
    {
        return "Set Response Delimiter Command";
    }

    public String construct()
    {
        return m_Param + "08" + "CF";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP62Status status = SP62Status.create( response );

        status.addMessage( "DELIMITER:" + m_Delimiter );

        return new SpectroEvent( this, status );
    }
}
