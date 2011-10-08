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
 * IDRCommand.java
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
 * @author Robin Lee
 */
public class IDRCommand
    implements SpectroCommand, CommandStruc
{
    private String m_ProductType;
    private String m_ROMVersion;
    private String m_BodyNumber;
    private String m_Geometry;
    private int m_MinWaveLength;
    private int m_MaxWaveLength;
    private int m_WaveLengthPitch;

    /**
     * Creates a new instance of IDRCommand
     */
    public IDRCommand()
    {
    }

    public String construct()
    {
        return "IDR" + DELIM;
    }

    public String getName()
    {
        return "Read Device Identification Command";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 8 )
        {
            String returnCode = sTok.nextToken();

            CM2600dStatus status = CM2600dStatus.create( returnCode );
            String str = sTok.nextToken();
            if( str.equals( "03" ) )
            {
                m_ProductType = "CM-2600d";
            }
            else if( str.equals( "13" ) )
            {
                m_ProductType = "CM-2500d";
            }

            str = sTok.nextToken();
            if( str.length() == 3 )
            {
                m_ROMVersion = "" + str.charAt( 0 ) + "." + str.substring( 1 );
            }

            m_BodyNumber = sTok.nextToken();

            str = sTok.nextToken(); // Remove the "0" from the result list, which
            m_Geometry = "d/8";   /// means the geometer which is fixed.

            m_MinWaveLength = Integer.parseInt( sTok.nextToken() );
            m_MaxWaveLength = Integer.parseInt( sTok.nextToken() );
            m_WaveLengthPitch = Integer.parseInt( sTok.nextToken() );

            return new SpectroEvent( this, status );
        }
        else
        {
            CM2600dStatus errstatus = CM2600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public String getProductType()
    {
        return m_ProductType;
    }

    public String getRomVersion()
    {
        return m_ROMVersion;
    }

    public String getBodyNumber()
    {
        return m_BodyNumber;
    }

    public String getGeometry()
    {
        return m_Geometry;
    }

    public int getMinWaveLength()
    {
        return m_MinWaveLength;
    }

    public int getMaxWaveLength()
    {
        return m_MaxWaveLength;
    }

    public int getWaveLengthPitch()
    {
        return m_WaveLengthPitch;
    }
}
