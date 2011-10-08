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
 * Created on March 18, 2007, 11:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm3600d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author Robin Lee
 */
public class STRCommand
    implements SpectroCommand, CommandStruc
{
    private boolean m_ChargeStatus;
    private boolean m_WhiteCalibration;

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

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 3 )
        {
            String returnCode = sTok.nextToken();
            CM3600dStatus status = CM3600dStatus.create( returnCode );

            String str = sTok.nextToken();
            if( str.equals( "0" ) )
            {
                m_ChargeStatus = true;
            }
            else if( str.equals( "1" ) )
            {
                m_ChargeStatus = false;
            }

            str = sTok.nextToken();
            if( str.equals( "0" ) )
            {
                m_WhiteCalibration = true;
            }
            else if( str.equals( "1" ) )
            {
                m_WhiteCalibration = false;
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM3600dStatus errstatus = CM3600dStatus.create( "INVALID_RETURN" );
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
}
