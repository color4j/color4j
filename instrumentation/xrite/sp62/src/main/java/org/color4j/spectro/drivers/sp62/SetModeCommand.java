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

public class SetModeCommand
    implements SpectroCommand
{
    public static final String MODE_MAIN_MENU = "00";
    public static final String MODE_QA = "01";
    public static final String MODE_ANALYZE = "02";
    public static final String MODE_COMPARE = "03";
    public static final String MODE_STRENGTH = "04";
    public static final String MODE_OPACITY = "05";
    public static final String MODE_CALIBRATION = "06";
    public static final String MODE_STANDARDS = "07";
    public static final String MODE_STANDARDS_MEASUREMENT = "08";
    public static final String MODE_PROJECT_MENU = "09";
    public static final String MODE_JOB_SELECTION_MENU = "10";
    public static final String MODE_CONFIGURATION_MENU = "11";

    private String m_Mode = "00";

    public SetModeCommand( String mode )
    {
        m_Mode = mode;
    }

    public String getName()
    {
        return "Set Mode Command";
    }

    public String construct()
    {
        return m_Mode + "01SM";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP62Status status = SP62Status.create( response );

        return new SpectroEvent( this, status );
    }
}
