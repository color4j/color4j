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
 * MDRCommand.java
 *
 * Created on October 14, 2002, 3:29 PM
 */

package org.color4j.spectro.minolta.cm3700d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class CPRCommand implements SpectroCommand
{
    private int m_AverageMeasurement;
    private boolean m_Specular;
    private LensPosition m_MeasureArea;
    private int m_UVAmount;
    private int m_MeasureMode;

    /**
     * Creates a new instance of MDRCommand
     */
    public CPRCommand()
    {
    }

    public String construct()
    {
        return "CPR";
    }

    public String getName()
    {
        return "Measurement Parameters Request Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );
        StringTokenizer sTok = new StringTokenizer( response, "," );

        if( sTok.countTokens() == 6 )
        {
            String statusString = sTok.nextToken();
            CM3700dStatus status = CM3700dStatus.create( statusString );

            if( status.isSuccess() )
            {
                m_AverageMeasurement = new Integer( sTok.nextToken() ).intValue();

                String Specular = sTok.nextToken();

                if( Specular.equals( "0" ) )
                {
                    m_Specular = true;
                }
                else if( Specular.equals( "1" ) )
                {
                    m_Specular = false;
                }

                String MeasureArea = sTok.nextToken();

                if( MeasureArea.equals( "0" ) )
                {
                    m_MeasureArea = new LargeAreaView();
                }
                else if( MeasureArea.equals( "1" ) )
                {
                    m_MeasureArea = new MediumAreaView();
                }
                else if( MeasureArea.equals( "2" ) )
                {
                    m_MeasureArea = new SmallAreaView();
                }

                m_UVAmount = new Integer( sTok.nextToken() ).intValue();

                m_MeasureMode = new Integer( sTok.nextToken() ).intValue();
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM3700dStatus errstatus = CM3700dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public int getAverageMeasurement()
    {
        return m_AverageMeasurement;
    }

    public boolean getSpecular()
    {
        return m_Specular;
    }

    public LensPosition getMeasureArea()
    {
        return m_MeasureArea;
    }

    public int getUVAmount()
    {
        return m_UVAmount;
    }

    public int getMeasureMode()
    {
        return m_MeasureMode;
    }
}
