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

package org.color4j.spectro.drivers.sp68;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class SetModeCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( SetModeCommand.class.getName() );

    public final static Integer NORMAL = new Integer( 0 );
    public final static Integer STORAGE = new Integer( 1 );
    public final static Integer PASS_FAIL = new Integer( 2 );
    public final static Integer STORAGE_PASS_FAIL = new Integer( 4 );
    public final static Integer START_JOB = new Integer( 5 );

    protected int m_Mode = 0;

    public SetModeCommand( Integer mode )
    {
        m_Mode = mode.intValue();
    }

    public String getName()
    {
        return "Set Mode Command";
    }

    public String construct()
    {
        return m_Mode + "sm";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( "\\s(\\d\\d)\\s(\\d\\d)\\s(\\d\\d)\\s(\\d\\d)\\s(\\d\\d)\\s(\\d\\d)[\r\n]" );

            Matcher matcher = pattern.matcher( response );

            if( matcher.find() )
            {
                String op_mode = matcher.group( 1 );
                String user_mode = matcher.group( 2 );
                String analyze_mode = matcher.group( 3 );
                String delta_mode = matcher.group( 4 );
                String storage_operation = matcher.group( 5 );
                String averaging_mode = matcher.group( 6 );

                m_Logger.info( "Instrument Mode : " + op_mode + " ; " + user_mode + " : " + analyze_mode + " ; " +
                               delta_mode + " ; " + storage_operation + " ; " + averaging_mode );

                status.addMessage( "Instrument modes : " + op_mode + "; " + user_mode + "; " + analyze_mode + "; "
                                   + delta_mode + "; " + storage_operation + "; " + averaging_mode );
            }
            else
            {
                return null;
            }
        }
        catch( PatternSyntaxException patSynEx )
        {
            m_Logger.info( "Malformed Regular Expression" );
        }

        return new SpectroEvent( this, status );
    }
}
