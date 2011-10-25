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
 * CALCommand.java
 *
 * Created on October 29, 2002, 5:14 PM
 */

package org.color4j.spectro.minolta.cm508c;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class CALCommand
    implements SpectroCommand
{
    String command;

    /**
     * Creates a new instance of CALCommand
     */
    public CALCommand()
    {
        command = "CAL";
    }

    public String construct()
    {
        return command;
    }

    public String getName()
    {
        return "White Calibration Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM508cStatus.create( new String( values ) ) );
    }
}