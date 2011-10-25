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
 * STRCommand.java
 *
 * Created on March 18, 2007, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm2600d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class STRCommand
    implements SpectroCommand, CommandStruc
{
    private boolean m_ChargeStatus;
    private String m_VoltageStatus;
    private boolean m_WhiteCalibration;
    private boolean m_ZeroCalibration;
    private int m_TargetCount;
    private int m_DataCount;
    private int m_DataCapacity;

    /**
     * Creates a new instance of STRCommand
     */
    public STRCommand()
    {

    }

    public String construct()
    {
        return "STR" + DELIM;
    }

    public String getName()
    {
        return "Request Status Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );

        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 7 )
        {
            CM2600dStatus status = CM2600dStatus.create( sTok.nextToken() );

            if( status.isSuccess() )
            {
                String chargestatus = sTok.nextToken();
                String voltageStatus = sTok.nextToken();
                String calibrationStatus = sTok.nextToken();
                if( chargestatus.equals( "0" ) )
                {
                    m_ChargeStatus = true;
                }
                else if( chargestatus.equals( "1" ) )
                {
                    m_ChargeStatus = false;
                }

                if( voltageStatus.equals( "0" ) )
                {
                    m_VoltageStatus = "NORM";
                }
                else if( voltageStatus.equals( "1" ) )
                {
                    m_VoltageStatus = "LOW";
                }

                if( calibrationStatus.equals( "0" ) )
                {
                    m_ZeroCalibration = false;
                    m_WhiteCalibration = false;
                }
                else if( calibrationStatus.equals( "1" ) )
                {
                    m_ZeroCalibration = true;
                    m_WhiteCalibration = false;
                }
                else if( calibrationStatus.equals( "2" ) )
                {
                    m_ZeroCalibration = true;
                    m_WhiteCalibration = true;
                }
                else if( calibrationStatus.equals( "3" ) )
                {
                    m_ZeroCalibration = true;
                    m_WhiteCalibration = true;
                }
                String dataCapacity = sTok.nextToken();
                m_DataCapacity = Integer.parseInt( dataCapacity );
                String dataCount = sTok.nextToken();
                m_DataCount = Integer.parseInt( dataCount );
                String targetCount = sTok.nextToken();
                m_TargetCount = Integer.parseInt( targetCount );
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM2600dStatus errstatus = CM2600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public boolean getChargeStatus()
    {
        return m_ChargeStatus;
    }

    public String getVoltageStatus()
    {
        return m_VoltageStatus;
    }

    public boolean getWhiteCalibration()
    {
        return m_WhiteCalibration;
    }

    public boolean getZeroCalibration()
    {
        return m_ZeroCalibration;
    }

    public int getTargetCount()
    {
        return m_TargetCount;
    }

    public int getDataCount()
    {
        return m_DataCount;
    }

    public int getDataCapacity()
    {
        return m_DataCapacity;
    }
}
