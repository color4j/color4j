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

package org.color4j.spectro.drivers.sp68;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class GetMeasurementStatusCommand implements SpectroCommand
{
    public GetMeasurementStatusCommand()
    {
    }

    public String getName()
    {
        return "Get Measurement Status Command";
    }

    public String construct()
    {
        return "me";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( "\\s*(\\d\\d).*[\r\n].*" );

            Matcher matcher = pattern.matcher( response );

            if( matcher.find() )
            {
                checkMeasurementStatus( matcher.group( 1 ), status );

                return new SpectroEvent( this, status );
            }
            else
            {
                return null;
            }
        }
        catch( PatternSyntaxException patSynEx )
        {
            Logger logger = Logger.getLogger( GetMeasurementStatusCommand.class.getName() );
            logger.info( "Malformed Regular Expression in " + getName() );
            return null;
        }
    }

    private void checkMeasurementStatus( String response, SP68Status status )
    {
        try
        {
            int statusCode = Integer.parseInt( response.trim() );

            switch( statusCode )
            {
            case 0:
                status.addMessage( "MEASUREMENT_OK_STATUS" );
                break;
            case 1:
                status.addError( "MEASUREMENT_RELEASED_TOO_SOON" );
                break;
            case 2:
                status.addError( "MEASUREMENT_REFLECTANCE_LIMIT_EXCEEDED" );
                break;
            case 3:
                status.addError( "MEASUREMENT_MUST_CHARGE" );
                break;
            case 4:
                status.addError( "MEASUREMENT_BATTERY_LOW" );
                break;
            case 5:
                status.addError( "MEASUREMENT_LIGHT_LEAKAGE" );
                break;
            default:
                status.addError( "UNRECOGNIZED_STATUS" );
                break;
            }
        }
        catch( NumberFormatException numEx )
        {
            status.addError( "UNRECOGNIZED_RESPONSE" );
        }
    }
}
