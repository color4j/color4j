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
 * Created on Nov 5, 2003
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.color4j.spectro.drivers.xr;

import java.util.Map;
import java.util.TreeMap;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroFunction;
import org.color4j.spectro.spi.SpectroStatus;

public class GetReflectanceFunction
    implements SpectroFunction
{
    public SpectroEvent invoke()
    {
        SpectroStatus status = new XR8000Status();
        Map<Double, Double> values = new TreeMap<Double, Double>();
        float[] outdata = new float[ 39 ];
        Xr8000Library.INSTANCE.VB_Instrument_GetReflectances( outdata );
        double currentWavelength = 360.0d;
        double interval = 10.0d;
        for( float aResultArray : outdata )
        {
            System.out.println( currentWavelength + "," + aResultArray );
            values.put( currentWavelength, (double) aResultArray / 100 );
            currentWavelength += interval;
        }
        return new SpectroEvent( this, new XR8000Reading( status, null, values ) );
    }
}
