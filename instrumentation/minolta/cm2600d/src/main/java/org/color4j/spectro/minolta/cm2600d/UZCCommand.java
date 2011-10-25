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
 * UZCCommand.java
 *
 * Created on October 14, 2002, 3:05 PM
 */

package org.color4j.spectro.minolta.cm2600d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class UZCCommand
    implements SpectroCommand, CommandStruc
{

    /**
     * Creates a new instance of UZCCommand
     */
    public UZCCommand()
    {
    }

    public String construct()
    {
        return "UZC" + DELIM;
    }

    public String getName()
    {
        return "User Zero Calibration Command";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 1 )
        {
            String returnCode = sTok.nextToken();

            CM2600dStatus status = CM2600dStatus.create( returnCode );

            return new SpectroEvent( this, status );
        }
        else
        {
            CM2600dStatus errstatus = CM2600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }
}
