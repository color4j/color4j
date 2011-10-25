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
 * ParameterRequestCommand.java
 *
 * Created on July 19, 2002, 4:01 PM
 */

package org.color4j.spectro.gretagmacbeth.spectrolino;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */
public class ParameterRequestCommand implements SpectroCommand
{
    protected String Density;
    protected String Illum;
    protected String Observer;
    protected String ActualFilter;

    /**
     * Creates a new instance of ParameterRequestCommand
     */
    public ParameterRequestCommand()
    {
    }

    public String getName()
    {
        return "Parameter Request Command";
    }

    public String construct()
    {
        String command;

        command = "; 0";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        String rawresponse = response.replaceFirst( "\r\n", "" );

        SpectroStatus status = new SpectrolinoStatus().create( "SUCCESS" );

        if( Pattern.matches( ": 11 .*", rawresponse ) )
        {
            String result = rawresponse.replaceFirst( ": 11 ", "" );

            // Interpret the string
            try
            {
                StringTokenizer token = new StringTokenizer( result, " " );

                // Density
                Density = token.nextToken();

                // WBaseType
                token.nextToken();

                // Illum
                Illum = token.nextToken();

                // Observer
                Observer = token.nextToken();

                // ActualFilterType
                ActualFilter = token.nextToken();
            }
            catch( NoSuchElementException elEp )
            {
                return null;
            }
        }
        else
        {
            //status = new SpectrolinoStatus ().create ("UNKNOWN_RESPONSE");
            return null;
        }

        return new SpectroEvent( this, status );
    }

    public String getDensity()
    {
        return Density;
    }

    public String getIllum()
    {
        return Illum;
    }

    public String getObserver()
    {
        return Observer;
    }

    public String getActualFilter()
    {
        return ActualFilter;
    }
}
