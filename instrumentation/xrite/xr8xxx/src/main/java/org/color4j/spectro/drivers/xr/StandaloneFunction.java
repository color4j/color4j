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
 * Created on Nov 4, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.color4j.spectro.drivers.xr;

import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroFunction;
import org.color4j.spectro.spi.SpectroStatus;

public class StandaloneFunction
    implements SpectroFunction
{
    public SpectroEvent invoke()
    {
        SpectroStatus status = new XR8000Status();
        Xr8000Library.INSTANCE.Instrument_Standalone();
        return new SpectroEvent( this, status );
    }
}
