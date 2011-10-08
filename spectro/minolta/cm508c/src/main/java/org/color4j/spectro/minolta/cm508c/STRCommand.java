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
 * SDRCommand.java
 *
 * Created on October 14, 2002, 3:30 PM
 */

package org.color4j.spectro.minolta.cm508c;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class STRCommand implements SpectroCommand
{
    private boolean m_ChargeStatus;
    private boolean m_WhiteCalibration;
    private boolean m_BatteryLow;
    private boolean m_Specular;

    /**
     * Creates a new instance of SDRCommand
     */
    public STRCommand()
    {

    }

    public String construct()
    {
        return "STR";
    }

    public String getName()
    {
        return "Request Status Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );

        response = response.replaceAll( "\r\n", "" );
        StringTokenizer sTok = new StringTokenizer( response, "," );

        if( sTok.countTokens() == 5 )
        {
            CM508cStatus status = CM508cStatus.create( sTok.nextToken() );

            if( status.isSuccess() )
            {
                String chargestatus = sTok.nextToken();
                String whitecalibration = sTok.nextToken();
                String battery = sTok.nextToken();
                String specular = sTok.nextToken();

                if( chargestatus.equals( "0" ) )
                {
                    m_ChargeStatus = true;
                }
                else if( chargestatus.equals( "1" ) )
                {
                    m_ChargeStatus = false;
                }

                if( whitecalibration.equals( "0" ) )
                {
                    m_WhiteCalibration = true;
                }
                else if( whitecalibration.equals( "1" ) )
                {
                    m_WhiteCalibration = false;
                }

                if( battery.equals( "0" ) )
                {
                    m_BatteryLow = false;
                }
                else if( battery.equals( "1" ) )
                {
                    m_BatteryLow = true;
                }

                if( specular.equals( "0" ) )
                {
                    m_Specular = true;
                }
                else if( specular.equals( "1" ) )
                {
                    m_Specular = false;
                }
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM508cStatus errstatus = CM508cStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public boolean getChargeStatus()
    {
        return m_ChargeStatus;
    }

    public boolean getWhiteCalibration()
    {
        return m_WhiteCalibration;
    }

    public boolean isBatteryLow()
    {
        return m_BatteryLow;
    }

    public boolean getSpecular()
    {
        return m_Specular;
    }
}
