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
 * CM26000dStatus.java
 *
 * Created on March 18, 2007, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm2600d;

import java.util.ArrayList;
import java.util.Collection;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * Meta-information of an operation with the spectrophotometer. <p>The SpectroStatus carries information of the
 * SpectroReading, especially about the result of a measurement, success or failure. If the the <code>isSuccess</code>
 * method, the <code>getErrors</code> should return an empty Collection.</p>
 *
 * @author Robin Lee
 */
public class CM2600dStatus
    implements SpectroStatus, CommandStruc
{
    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static CM2600dStatus create( String response )
    {
        ArrayList errorMsg = new ArrayList();
        ArrayList warningMsg = new ArrayList();
        ArrayList msg = new ArrayList();

        // Throw away any <CR>
        response = response.replaceAll( DELIM, "" );

        if( response.matches( "OK[0-9][0-9]" ) )
        {
            String result = response.replaceFirst( "OK", "" );

            if( result.equals( "00" ) )
            {
                msg.add( "MSG_OK" );
            }
            else if( result.equals( "02" ) )
            {
                warningMsg.add( "MSG_WARN_LIGHT" );
            }
            else if( result.equals( "03" ) )
            {
                warningMsg.add( "MSG_VOLTAGE_LOW" );
            }
            else if( result.equals( "04" ) )
            {
                warningMsg.add( "MSG_WARN_LIGHT" );
                warningMsg.add( "MSG_VOLTAGE_LOW" );
            }
            else if( result.equals( "05" ) )
            {
                warningMsg.add( "MSG_TARGET_COLOR_DATA_DIFFER" );
            }
            else
            {
                errorMsg.add( "MSG_UNKNOWN_STRING" );
            }
        }
        else if( response.matches( "ER[0-9][0-9]" ) )
        {
            String result = response.replaceFirst( "ER", "" );

            if( result.equals( "00" ) )
            {
                errorMsg.add( "MSG_COMMAND_ERROR" );
            }
            else if( result.equals( "02" ) )
            {
                errorMsg.add( "MSG_LOW_BATTERY" );
            }
            else if( result.equals( "05" ) )
            {
                errorMsg.add( "MSG_ERROR_LAMP1" );
            }
            else if( result.equals( "07" ) )
            {
                errorMsg.add( "MSG_ERROR_CALIBRATION1" );
            }
            else if( result.equals( "08" ) )
            {
                errorMsg.add( "MSG_ERROR_COMMUNICATION" );
            }
            else if( result.equals( "10" ) )
            {
                errorMsg.add( "MSG_ERROR_NO_DATA_AT_LOCATION" );
            }
            else if( result.equals( "11" ) )
            {
                errorMsg.add( "MSG_ERROR_CALIBRATION2" );
            }
            else if( result.equals( "13" ) )
            {
                errorMsg.add( "MSG_ERROR_CONVERTERS" );
            }
            else if( result.equals( "20" ) )
            {
                errorMsg.add( "MSG_MEMORYWRITING_ERROR" );
            }
            else if( result.equals( "24" ) )
            {
                errorMsg.add( "MSG_ERROR_WHITE_ERROR" );
            }
            else if( result.equals( "25" ) )
            {
                errorMsg.add( "MSG_ERROR_TARGET" );
            }
            else if( result.equals( "27" ) )
            {
                errorMsg.add( "MSG_LAMP_NOT_CHARGED" );
            }
            else
            {
                errorMsg.add( "MSG_UNKNOWN_STRING" );
            }
        }
        else
        {
            errorMsg.add( "MSG_UNKNOWN_STRING" );
        }

        return new CM2600dStatus( msg, warningMsg, errorMsg );
    }

    private CM2600dStatus( Collection messages, Collection warnings, Collection errors )
    {
        m_ErrorMessages = errors;
        m_WarningMessages = warnings;
        m_Messages = messages;
    }

    /**
     * Returns true if the Status reflects a successful operation. <p>It is a SUCCESS if, and only if, there are no
     * Errors associated with the operation reflected by this SpectroStatus. Conditions, states and actions that are not
     * severe enough to be considered as failures should be reported as Warnings. </p> <p>This method will ALWAYS return
     * the negated value of the <code>isFailure</code> method.</p>
     */
    public boolean isSuccess()
    {
        return m_ErrorMessages.size() == 0;
    }

    /**
     * Returns true if the Status reflects a failed operation. <p>This method will ALWAYS return the negated value of
     * the <code>isSuccess</code> method.</p>
     */
    public boolean isFailure()
    {
        return m_ErrorMessages.size() > 0;
    }

    /**
     * Returns all the Warnings that are part of this SpectroStatus. <p>Warnings are states and conditions that the
     * human user should be aware of, but that are not considered to be a failure.</p> <p>The Collection contains
     * <code>java.lang.String</code> objects, and each of them are untranslated, in programmatic English.</p>
     */
    public Collection getWarnings()
    {
        return m_WarningMessages;
    }

    public void addWarning( String warning )
    {
        m_WarningMessages.add( warning );
    }

    /**
     * Returns all the errors that are part of this SpectroStatus. <p>Errors are states, conditions and actions
     * resulting in a failure of the operation. Only if the returned Collection is empty, will the
     * <code>isSuccess</code> method return true.</p> <p>The Collection contains <code>java.lang.String</code> objects,
     * and each of them are untranslated, in programmatic English.</p>
     */
    public Collection getErrors()
    {
        return m_ErrorMessages;
    }

    public void addError( String error )
    {
        m_ErrorMessages.add( error );
    }

    /**
     * Returns all the messages that are part of this SpectroStatus. <p>Messages are information to the human user, that
     * can be of its interest. This does NOT include debugging messages.</p> <p>The Collection contains
     * <code>java.lang.String</code> objects, and each of them are untranslated, in programmatic English.</p>
     */
    public Collection getMessages()
    {
        return m_Messages;
    }

    public void addMessage( String message )
    {
        m_Messages.add( message );
    }
}
