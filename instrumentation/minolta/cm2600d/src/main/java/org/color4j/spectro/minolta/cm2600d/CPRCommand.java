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
 * CPRCommand.java
 *
 * Created on March 18, 2007, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm2600d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.LightFilter;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class CPRCommand
    implements SpectroCommand, CommandStruc
{
    private LensPosition m_MeasureArea;
    private boolean m_Specular;
    private LightFilter m_LightFilter;

    /**
     * Creates a new instance of CPRCommand
     */
    public CPRCommand()
    {
    }

    public String construct()
    {
        return "CPR" + DELIM;
    }

    public String getName()
    {
        return "Condition Parameters Request Command";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 3 )
        {
            String returnCode = sTok.nextToken();
            CM2600dStatus status = CM2600dStatus.create( returnCode );

            if( status.isSuccess() )
            {
                String measurementType = sTok.nextToken();
                if( measurementType.equals( "0" ) )
                {
                    m_MeasureArea = new MediumAreaView();
                }
                else if( measurementType.equals( "1" ) )
                {
                    m_MeasureArea = new SmallAreaView();
                }

                measurementType = sTok.nextToken();
                if( measurementType.equals( "0" ) )
                {
                    m_Specular = true;
                    m_LightFilter = new UVIncludedLightFilter();
                }
                else if( measurementType.equals( "1" ) )
                {
                    m_Specular = false;
                    m_LightFilter = new UVIncludedLightFilter();
                }
                else if( measurementType.equals( "2" ) )
                {
                    m_Specular = true;
                    m_LightFilter = new UVExcludedLightFilter();
                }
                else if( measurementType.equals( "3" ) )
                {
                    m_Specular = false;
                    m_LightFilter = new UVExcludedLightFilter();
                }
                else if( measurementType.equals( "4" ) )
                {
                    m_Specular = false;
                    m_LightFilter = new UVExcludedLightFilter();
                }
                else if( measurementType.equals( "5" ) )
                {
                    m_Specular = false;
                    m_LightFilter = new UVExcludedLightFilter();
                }
                else
                {
                    throw new RuntimeException( "Unknown Measurement Type received:" + measurementType );
                }
            }

            return new SpectroEvent( this, status );
        }
        else
        {
            CM2600dStatus errstatus = CM2600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public LensPosition getMeasureArea()
    {
        return m_MeasureArea;
    }

    public boolean getSpecular()
    {
        return m_Specular;
    }

    public LightFilter getLightFilter()
    {
        return m_LightFilter;
    }
}
