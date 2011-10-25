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
 * MDRCommand.java
 *
 * Created on October 14, 2002, 3:29 PM
 */

package org.color4j.spectro.minolta.cm3700d;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class MDRCommand implements SpectroCommand
{

    /**
     * Creates a new instance of MDRCommand
     */
    public MDRCommand()
    {
    }

    public String construct()
    {
        return "MDR\r\n&";
    }

    public String getName()
    {
        return "Measurement Data Output Request";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        if( sTok.countTokens() == 2 )
        {
            String line1 = sTok.nextToken();
            String line2 = sTok.nextToken();

            StringTokenizer line1comma = new StringTokenizer( line1, "," );
            StringTokenizer line2comma = new StringTokenizer( line2, "," );

            String line1status = line1comma.nextToken();
            String line2status = line2comma.nextToken();

            CM3700dStatus status1 = CM3700dStatus.create( line1status );
            CM3700dStatus status2 = CM3700dStatus.create( line2status );

            int wavelength = 400;

            if( status1.isSuccess() && status2.isSuccess() )
            {
                if( line1comma.countTokens() == 30 && line2comma.countTokens() == 9 )
                {

                    Map values = new TreeMap( new DoubleCompare() );

                    while( line1comma.hasMoreTokens() )
                    {
                        String data = line1comma.nextToken();

                        try
                        {
                            Double doubleData = new Double( data );

                            values.put( new Double( wavelength ), doubleData );
                            wavelength += 10;
                        }
                        catch( NumberFormatException numEx )
                        {
                            status1.addError( "MSG_DATA_NUMBER_FORMAT_ERROR" );
                        }
                    }

                    while( line2comma.hasMoreTokens() )
                    {
                        String data = line2comma.nextToken();

                        try
                        {
                            Double doubleData = new Double( data );

                            values.put( new Double( wavelength ), doubleData );
                            wavelength += 10;
                        }
                        catch( NumberFormatException numEx )
                        {
                            status1.addError( "MSG_DATA_NUMBER_FORMAT_ERROR" );
                        }
                    }

                    MinoltaReading reading = new MinoltaReading( status1, null, values );
                    return new SpectroEvent( this, reading );
                }
                else
                {
                    status1.addError( "MSG_UNKNOWN_STRING" );
                }
            }

            return new SpectroEvent( this, status1 );
        }
        else
        {
            CM3700dStatus errstatus = CM3700dStatus.create( "UNKNOWN_STRING" );
            return new SpectroEvent( this, errstatus );
        }
    }

    class DoubleCompare implements Comparator
    {
        /**
         * Description of the Method
         *
         * @param o1 Description of the Parameter
         * @param o2 Description of the Parameter
         *
         * @return Description of the Return Value
         *
         * @throws ClassCastException Description of the Exception
         */
        public int compare( Object o1, Object o2 )
        {
            Double d1 = (Double) o1;
            Double d2 = (Double) o2;

            return (int) ( d1.doubleValue() - d2.doubleValue() );
        }
    }
}
