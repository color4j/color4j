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
 * Created on October 14, 2002, 3:30 PM
 */

package org.color4j.spectro.minolta.cm3700d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.Aperture;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class STRCommand implements SpectroCommand
{
    private boolean m_ChargeStatus;
    private Aperture m_TargetMask;
    private boolean m_WhiteCalibration;
    private boolean m_ZeroCalibration;

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
            CM3700dStatus status = CM3700dStatus.create( sTok.nextToken() );

            if( status.isSuccess() )
            {
                String chargestatus = sTok.nextToken();
                String targetmask = sTok.nextToken();
                String whitecalibration = sTok.nextToken();
                String zerocalibration = sTok.nextToken();

                if( chargestatus.equals( "0" ) )
                {
                    m_ChargeStatus = true;
                }
                else if( chargestatus.equals( "1" ) )
                {
                    m_ChargeStatus = false;
                }

                if( targetmask.equals( "0" ) )
                {
                    m_TargetMask = new LargeAperture();
                }
                else if( targetmask.equals( "1" ) )
                {
                    m_TargetMask = new MediumAperture();
                }
                else if( targetmask.equals( "2" ) )
                {
                    m_TargetMask = new SmallAperture();
                }
                else if( targetmask.equals( "3" ) )
                {
                    m_TargetMask = null;
                }

                if( whitecalibration.equals( "0" ) )
                {
                    m_WhiteCalibration = false;
                }
                else if( whitecalibration.equals( "1" ) )
                {
                    m_WhiteCalibration = true;
                }

                if( zerocalibration.equals( "0" ) )
                {
                    m_ZeroCalibration = false;
                }
                else if( zerocalibration.equals( "1" ) )
                {
                    m_ZeroCalibration = true;
                }
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM3700dStatus errstatus = CM3700dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public boolean getChargeStatus()
    {
        return m_ChargeStatus;
    }

    public Aperture getTargetMask()
    {
        return m_TargetMask;
    }

    public boolean getWhiteCalibration()
    {
        return m_WhiteCalibration;
    }

    public boolean getZeroCalibration()
    {
        return m_ZeroCalibration;
    }
}
