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

public class SetPrintoutFormatCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( SetPrintoutFormatCommand.class.getName() );

    public final static Integer NO_CHANGE = new Integer( 0 );

    public final static Integer COLORSPACE_FORMAT = new Integer( 1 );
    public final static Integer SPECTRAL_FORMAT = new Integer( 2 );

    public final static Integer HEADER_ON = new Integer( 2 );
    public final static Integer HEADER_OFF = new Integer( 1 );

    public final static Integer REFERENCE_ON = new Integer( 1 );
    public final static Integer REFERENCE_OFF = new Integer( 2 );

    protected Integer m_Format = new Integer( 0 );
    protected Integer m_Header = new Integer( 0 );
    protected Integer m_Reference = new Integer( 0 );

    public SetPrintoutFormatCommand( Integer format, Integer header, Integer reference )
    {
        if( format != null )
        {
            m_Format = format;
        }

        if( header != null )
        {
            m_Header = header;
        }

        if( reference != null )
        {
            m_Reference = reference;
        }
    }

    public String getName()
    {
        return "Set Printout Format Command";
    }

    public String construct()
    {
        return m_Format.toString() + m_Header.toString() + m_Reference.toString() + "sp";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( "\\s*(\\d)(\\d)(\\d)[\r\n]" );

            Matcher matcher = pattern.matcher( response );

            if( matcher.find() )
            {
                String format = matcher.group( 1 );
                String header = matcher.group( 2 );
                String reference = matcher.group( 3 );
            }
            else
            {
                m_Logger.info( "Incorrect response : " + response );
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
