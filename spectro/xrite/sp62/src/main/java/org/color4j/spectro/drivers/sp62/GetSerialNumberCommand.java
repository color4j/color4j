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

package org.color4j.spectro.drivers.sp62;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class GetSerialNumberCommand
    implements SpectroCommand
{
    public GetSerialNumberCommand()
    {
    }

    public String getName()
    {
        return "Get Serial Number Command";
    }

    public String construct()
    {
        return "00SN";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP62Status status = SP62Status.create( response );

        status.addMessage( "SERIAL:" + response );

        return new SpectroEvent( this, status );
    }
}