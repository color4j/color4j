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

package org.color4j.spectro.minolta.cm508d;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

public class MESCommand
    implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( MESCommand.class.getName() );

    static
    {
    }

    String command;

    public MESCommand()
    {
        command = "MES";
    }

    public String construct()
    {
        return command;
    }

    public String getName()
    {
        return "Measure Command";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );

        response = response.replaceAll( "\r\n", "" );
        StringTokenizer sTok = new StringTokenizer( response, "," );

        String statusString = sTok.nextToken();
        CM508dStatus status = CM508dStatus.create( statusString );

        SpectroSettings settings = new SpectroSettings();

        settings.setAperture( new StandardAperture() );
        settings.setLensPosition( new StandardAreaView() );
        settings.setLightFilter( new UVIncludedLightFilter() );

        try
        {
            if( sTok.countTokens() < 31 )
            {
                status.addError( "MSG_INVALID_RETURN" );
                return new SpectroEvent( this, status );
            }
            else
            {

                if( status.isSuccess() )
                {
                    Map values = new TreeMap( new DoubleCompare() );

                    int wavelength = 400;

                    while( sTok.hasMoreTokens() )
                    {
                        String data = sTok.nextToken().trim();

                        try
                        {
                            double doubleData = Double.parseDouble( data );

                            doubleData = doubleData / 100.00;

                            values.put( new Double( wavelength ), new Double( doubleData ) );
                            m_Logger.finer( wavelength + " -> " + data );
                            wavelength += 10;
                        }
                        catch( NumberFormatException numEx )
                        {
                            status.addError( "MSG_DATA_NUMBER_FORMAT_ERROR" );
                        }
                    }

                    MinoltaReading reading = new MinoltaReading( status, settings, values );

                    return new SpectroEvent( this, reading );
                }
            }
        }
        catch( NoSuchElementException exception )
        {
            status.addError( "MSG_INVALID_RETURN" );
            return new SpectroEvent( this, status );
        }

        return new SpectroEvent( this, status );
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
            throws ClassCastException
        {
            Double d1 = (Double) o1;
            Double d2 = (Double) o2;

            return (int) ( d1.doubleValue() - d2.doubleValue() );
        }
    }
}
