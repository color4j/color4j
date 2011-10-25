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
 * LDRCommand.java
 *
 * Created on October 14, 2002, 3:27 PM
 */

package org.color4j.spectro.minolta.cm3700d;

import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class CPSCommand implements SpectroCommand
{
    private int m_AverageMeasurement;
    private boolean m_Specular;
    private LensPosition m_MeasureArea;
    private int m_UVAmount;
    private int m_MeasureMode;

    /**
     * Creates a new instance of LDRCommand
     */
    public CPSCommand( Integer AverageMeasurement, Boolean Specular, LensPosition MeasureArea,
                       Integer UVAmount, Integer MeasureMode
    )
    {
        m_AverageMeasurement = AverageMeasurement.intValue();
        m_Specular = Specular.booleanValue();
        m_MeasureArea = MeasureArea;
        m_UVAmount = UVAmount.intValue();
        m_MeasureMode = MeasureMode.intValue();
    }

    public String construct()
    {
        String command = "CPS," + m_AverageMeasurement + ",";

        if( m_Specular == true )
        {
            command = command + "0,";
        }
        else if( m_Specular = false )
        {
            command = command + "1,";
        }

        if( m_MeasureArea instanceof LargeAreaView )
        {
            command = command + "0,";
        }
        else if( m_MeasureArea instanceof MediumAreaView )
        {
            command = command + "1,";
        }
        else if( m_MeasureArea instanceof SmallAreaView )
        {
            command = command + "2,";
        }

        command = command + m_UVAmount + "," + m_MeasureMode;

        return command;
    }

    public String getName()
    {
        return "Measurement Parameters Prepare Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM3700dStatus.create( new String( values ) ) );
    }
}
