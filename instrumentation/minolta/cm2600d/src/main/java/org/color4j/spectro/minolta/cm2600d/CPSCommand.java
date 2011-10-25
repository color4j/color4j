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
public class CPSCommand
    implements SpectroCommand, CommandStruc
{
    private LensPosition m_MeasureArea;
    private boolean m_Specular;
    private LightFilter m_Filter;

    /**
     * Creates a new instance of CPSCommand
     *
     * @param measureArea The LensPosition to use.
     * @param specular    true if the Specular component is to be used.
     * @param filter      The LightFilter to use. Must only be UVIncluded or UVExcluded.
     */
    public CPSCommand( LensPosition measureArea, boolean specular, LightFilter filter )
    {
        m_MeasureArea = measureArea;
        m_Specular = specular;
        m_Filter = filter;
    }

    public String construct()
    {
        StringBuffer command = new StringBuffer( "CPS" );

        if( m_MeasureArea instanceof MediumAreaView )
        {
            command.append( ",0" );
        }
        else if( m_MeasureArea instanceof SmallAreaView )
        {
            command.append( ",1" );
        }

        if( m_Specular )
        {
            if( m_Filter instanceof UVIncludedLightFilter )
            {
                command.append( ",0" );
            }
            else
            {
                command.append( ",2" );
            }
        }
        else
        {
            if( m_Filter instanceof UVIncludedLightFilter )
            {
                command.append( ",1" );
            }
            else
            {
                command.append( ",3" );
            }
        }
        command.append( DELIM );
        String cmd = command.toString();
        command.setLength( 0 );
        return cmd;
    }

    public String getName()
    {
        return "Condition Parameter Set Command";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 1 )
        {
            String returnCode = sTok.nextToken();

            CM2600dStatus status = CM2600dStatus.create( returnCode );

            return new SpectroEvent( this, status );
        }
        else
        {
            CM2600dStatus errstatus = CM2600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }
}
