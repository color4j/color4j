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

package org.color4j.spectro.datacolor.common;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */

public class BlackCalibrationCommand
    implements SpectroCommand
{
    int m_MeasurementNum;
    private ResponseDecoder decoder;

    public BlackCalibrationCommand( Integer measurements, ResponseDecoder decoder )
    {
        this.decoder = decoder;
        m_MeasurementNum = measurements.intValue();
    }

    public String getName()
    {
        return "Black Calibration";
    }

    public String construct()
    {
        String command;

        command = "B1R ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SpectroStatus status = decoder.decode( response );

        if( response.charAt( 2 ) != 'B' )
        {
            return new SpectroEvent( this, status );
            //return null;
        }
        else if( response.charAt( 8 ) == 'x' )
        {
            return new SpectroEvent( this, status );
        }
        else
        {
            return null;
        }
    }
}