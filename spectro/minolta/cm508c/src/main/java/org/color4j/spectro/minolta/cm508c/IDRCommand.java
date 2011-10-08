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

package org.color4j.spectro.minolta.cm508c;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class IDRCommand implements SpectroCommand
{
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
        StringTokenizer sTok = new StringTokenizer( response, "," );

        if( sTok.countTokens() == 6 )
        {
            CM508cStatus status = CM508cStatus.create( sTok.nextToken() );

            if( status.isSuccess() )
            {
                String CM508cresult = sTok.nextToken();

                // If this is not CM508c return error
                if( CM508cresult.equals( " 1" ) )
                {
                    CM508cStatus errstatus = CM508cStatus.create( "NOT_VALID_CM500" );
                    return new SpectroEvent( this, errstatus );
                }
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM508cStatus errstatus = CM508cStatus.create( "MSG_INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }
}
