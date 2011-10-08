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

package org.color4j.spectro.hunter.lsxe;

import org.color4j.spectro.hunter.common.Hex;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class CalibrateCommand
    implements SpectroCommand
{
    public static final int BLACK_CALIBRATION = 0;
    public static final int WHITE_CALIBRATION = 1;

    private String command;
    private int currentPortPlate;

    public CalibrateCommand( Integer type, Integer portPlate )
    {
        switch( type.intValue() )
        {
        case 0:
            command = "I0";
            break;
        case 1:
            command = "I1";
            break;
        }

        currentPortPlate = portPlate.intValue();
    }

    public String construct()
    {
        return command;
    }

    public String getName()
    {
        return "Calibrate Command";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String statusString = new String( received );

        LSXEStatus status = LSXEStatus.create( new String( received ) );

        String portPlateStr = statusString.substring( 2 );

        if( !portPlateStr.equals( Hex.intToHexString( currentPortPlate, 4 ) ) )
        {
            status = new LSXEStatus();
            status.addError( "MSG_PORT_PLATE_MISMATCH" );
        }

        return new SpectroEvent( this, status );
    }
}
