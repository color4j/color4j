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
 * CDSCommand.java
 *
 * Created on October 29, 2002, 5:20 PM
 */

package org.color4j.spectro.minolta.cm503c;

import java.util.Map;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroReading;

/**
 * @author hywong
 */
public class CDSCommand
    implements SpectroCommand
{
    String command;
    SpectroReading m_whiteTile;

    /**
     * Creates a new instance of CDSCommand
     */
    public CDSCommand( SpectroReading whiteTile )
    {
        m_whiteTile = whiteTile;
        command = "CDS";
    }

    public String construct()
    {
        Map values = m_whiteTile.getValues();
        StringBuffer buffer = new StringBuffer();

        buffer.append( command + "," );

        /*Iterator entryList = values.entrySet().iterator();
        
        while( entryList.hasNext() )
        {
            Map.Entry e = (Map.Entry) entryList.next();
            buffer.append( ( (Double) e.getValue () ).toString () );
            
            if( entryList.hasNext() )
            {
                buffer.append( "," );
            }
        }*/

        // Extract wavelength from 400nm to 700 nm, 20 nm in between
        for( int i = 400; i <= 700; i = i + 20 )
        {
            buffer.append( ( (Double) values.get( new Double( i ) ) ).toString() );

            if( i != 700 )
            {
                buffer.append( "," );
            }
        }

        return buffer.toString();
    }

    public String getName()
    {
        return "Set White Calibration Data Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM503cStatus.create( new String( values ) ) );
    }
}
