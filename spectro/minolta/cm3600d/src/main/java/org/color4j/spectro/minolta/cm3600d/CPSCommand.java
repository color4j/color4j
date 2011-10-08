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
 * CPSCommand.java
 *
 * Created on March 18, 2007, 10:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm3600d;

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author Robin Lee
 */
public class CPSCommand
    implements SpectroCommand, CommandStruc
{
    private int m_AverageMeasurement;
    private boolean m_Specular;
    private LensPosition m_MeasureArea;
    private int m_UVAmount;
    private int m_MeasureMode;

    /**
     * Creates a new instance of CPSCommand
     */
    public CPSCommand(
        Integer AverageMeasurement, Boolean Specular, LensPosition MeasureArea,
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
        StringBuffer command = new StringBuffer( "CPS" );

        command.append( "," );
        command.append( m_AverageMeasurement );

        if( m_Specular )
        {
            command.append( ",0" );
        }
        else
        {
            command.append( ",4" );
        }

        if( m_MeasureArea instanceof LargeAreaView )
        {
            command.append( ",0" );
        }
        else if( m_MeasureArea instanceof MediumAreaView )
        {
            command.append( ",1" );
        }
        else if( m_MeasureArea instanceof SmallAreaView )
        {
            command.append( ",2" );
        }

        DecimalFormat dfmt = new DecimalFormat( "000" );
        command.append( "," );
        command.append( dfmt.format( (long) m_UVAmount ) );

        command.append( "," );
        command.append( m_MeasureMode );

        command.append( DELIM );

        String cmd = command.toString();
        command = null;

        return cmd;
    }

    public String getName()
    {
        return "Measurement Parameters Prepare Command";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 1 )
        {
            String returnCode = sTok.nextToken();

            CM3600dStatus status = CM3600dStatus.create( returnCode );

            return new SpectroEvent( this, status );
        }
        else
        {
            CM3600dStatus errstatus = CM3600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }
}
