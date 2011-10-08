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
 * MeasureCommand.java
 *
 */
package org.color4j.spectro.hunter.cqxe;

import java.util.Comparator;
import java.util.TreeMap;
import org.color4j.spectro.hunter.common.Binary;
import org.color4j.spectro.hunter.common.Hex;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author hywong
 */

public class MeasureCommand
    implements SpectroCommand
{
    private String command;

    /**
     * Creates a new instance of MeasureCommand
     */
    public MeasureCommand()
    {
        command = "H";
    }

    public String getName()
    {
        return "Measure Command";
    }

    public String construct()
    {
        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        int shortest = 400;
        int longest = 700;
        int interval = 10;

        //Remove header:
        String parameter = response.substring( 6 );

        TreeMap values = new TreeMap( new DoubleCompare() );
        int pointer = 0;
        StringBuffer buffer = new StringBuffer();

        for( int i = 0; i < 31; i++ )
        {
            for( int j = 0; j < 8; j++ )
            {
                Binary nibble = new Binary( Hex.hexToInt( parameter.charAt( pointer++ ) ) );
                nibble.extendTo( 4 );

                buffer.append( nibble.toString() );
            }

            Binary ieeeFloat = new Binary( buffer.toString() );
            buffer = new StringBuffer();

            double wv = (double) ( shortest + ( i * interval ) );
            double vl = (double) Float.intBitsToFloat( ieeeFloat.intValue() );

            values.put( new Double( wv ), new Double( vl ) );
        }

        CQXEReading reading = new CQXEReading( CQXEStatus.create( response ), null, values );

        return new SpectroEvent( this, reading );
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

            return (int) ( d1.intValue() - d2.intValue() );
        }
    }
}
