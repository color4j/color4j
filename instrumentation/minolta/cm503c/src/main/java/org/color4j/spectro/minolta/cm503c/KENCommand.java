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
 * KENCommand.java
 *
 * Created on October 28, 2002, 2:43 PM
 */
package org.color4j.spectro.minolta.cm503c;

import java.util.Vector;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class KENCommand
    implements SpectroCommand
{
    String command;

    /**
     * Creates a new instance of KENCommand
     */
    public KENCommand()
    {
        command = "KEN";
    }

    public String construct()
    {
        return command;
    }

    public String getName()
    {
        return "Key Entered Commnand";
    }

    public SpectroEvent interpret( byte[] values )
    {
        Vector msg = new Vector();

        String response = new String( values );

        if( "BTN01".equals( response ) )
        {
            msg.add( "TARGET_KEY" );
        }
        else if( "BTN02".equals( response ) )
        {
            msg.add( "DISPLAY_KEY" );
        }
        else if( "BTN03".equals( response ) )
        {
            msg.add( "MENU_KEY" );
        }
        else if( "BTN04".equals( response ) )
        {
            msg.add( "CURSOR_KEY" );
        }
        else if( "BTN05".equals( response ) )
        {
            msg.add( "DOWN_KEY" );
        }
        else if( "BTN06".equals( response ) )
        {
            msg.add( "UP_KEY" );
        }
        else if( "BTN08".equals( response ) )
        {
            msg.add( "MEASURE_BUTTON" );
        }
        else if( "BTN09".equals( response ) )
        {
            msg.add( "DELETE_BUTTON" );
        }
        else if( "BTN10".equals( response ) )
        {
            msg.add( "PRINT_BUTTON" );
        }

        return new SpectroEvent( this, new CM503cStatus( msg, new Vector(), new Vector() ) );
    }
}
