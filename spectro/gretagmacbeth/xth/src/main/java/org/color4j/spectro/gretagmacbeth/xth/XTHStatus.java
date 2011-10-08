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

package org.color4j.spectro.gretagmacbeth.xth;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * Meta-information of an operation with the spectrophotometer.
 * <p>The SpectroStatus carries information of the SpectroReading, especially
 * about the result of a measurement, success or failure. If the the
 * <code>isSuccess</code> method, the <code>getErrors</code> should return
 * an empty Collection.</p>
 *
 */
public class XTHStatus implements SpectroStatus
{
    static private Logger m_Logger = Logger.getLogger( XTHStatus.class.getName() );

    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static XTHStatus create( String response )
    {
        boolean m_unknown = false;

        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        try
        {
            if( response.charAt( 0 ) == '1' )
            {
                m_Logger.info( "Status - White Calibration successful" );
                msg.add( "MSG_SUCCESS_WHITE_CALIBRATION" );
            }
            else if( response.charAt( 0 ) == '9' )
            {
                m_Logger.info( "Error - White Calibration failed" );
                errorMsg.add( "MSG_FAILURE_WHITE_CALIBRATION" );
            }

            if( response.charAt( 1 ) == '1' )
            {
                m_Logger.info( "Status - Black Calibration successful" );
                msg.add( "MSG_SUCCESS_BLACK_CALIBRATION" );
            }
            else if( response.charAt( 1 ) == '9' )
            {
                m_Logger.info( "Error - Black Calibration failed" );
                errorMsg.add( "MSG_FAILURE_BLACK_CALIBRATION" );
            }

            if( response.charAt( 7 ) == '0' )
            {
                m_Logger.info( "Status - Floating Point OK" );
            }
            else if( response.charAt( 7 ) == 'J' )
            {
                m_Logger.info( "Error - Floating Point error" );
                errorMsg.add( "MSG_ERROR_FLOATING_POINT" );
            }

            if( response.charAt( 8 ) == '0' )
            {
                m_Logger.info( "Status - Light Limit OK" );
            }
            else if( response.charAt( 8 ) == '1' )
            {
                m_Logger.info( "Error - Light Limit too low" );
                errorMsg.add( "Light limit too low" );
            }

            if( response.charAt( 9 ) == '0' )
            {
                m_Logger.info( "Status - Dark Limit OK" );
            }
            else if( response.charAt( 9 ) == '1' )
            {
                m_Logger.info( "Error - Dark Limit too high" );
                errorMsg.add( "Dark limit too high" );
            }

            if( response.charAt( 10 ) == '0' )
            {
                m_Logger.info( "Status - Centroid Calculation OK" );
            }
            else if( response.charAt( 10 ) == '1' )
            {
                m_Logger.info( "Error - Centroid Calculation Failed" );
                errorMsg.add( "Centroid Calculation Failed" );
            }

            if( response.charAt( 12 ) == '0' )
            {
                m_Logger.info( "Status - Flash Voltage OK" );
            }
            else if( response.charAt( 12 ) == '9' )
            {
                m_Logger.info( "Error -Flash Voltage Error" );
                errorMsg.add( "Flash Voltage Error" );
            }
        }
        catch( IndexOutOfBoundsException arrayEx )
        {
            return null;
        }

        return new XTHStatus( msg, warningMsg, errorMsg );
    }

    public XTHStatus()
    {
        m_ErrorMessages = new Vector();
        m_WarningMessages = new Vector();
        m_Messages = new Vector();
    }

    XTHStatus( Collection messages, Collection warnings, Collection errors )
    {
        m_ErrorMessages = errors;
        m_WarningMessages = warnings;
        m_Messages = messages;
    }

    /**
     * Returns true if the Status reflects a successful operation.
     * <p>It is a SUCCESS if, and only if, there are no Errors
     * associated with the operation reflected by this SpectroStatus.
     * Conditions, states and actions that are not severe enough
     * to be considered as failures should be reported as Warnings.
     * </p>
     * <p>This method will ALWAYS return the negated value of the
     * <code>isFailure</code> method.</p>
     */
    public boolean isSuccess()
    {
        return m_ErrorMessages.size() == 0;
    }

    /**
     * Returns true if the Status reflects a failed operation.
     * <p>This method will ALWAYS return the negated value of the
     * <code>isSuccess</code> method.</p>
     */
    public boolean isFailure()
    {
        return m_ErrorMessages.size() > 0;
    }

    /**
     * Returns all the Warnings that are part of this SpectroStatus.
     * <p>Warnings are states and conditions that the human user should
     * be aware of, but that are not considered to be a failure.</p>
     * <p>The Collection contains <code>java.lang.String</code> objects, and
     * each of them are untranslated, in programmatic English.</p>
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
     * Returns all the errors that are part of this SpectroStatus.
     * <p>Errors are states, conditions and actions resulting in a failure
     * of the operation. Only if the returned Collection is empty, will
     * the <code>isSuccess</code> method return true.</p>
     * <p>The Collection contains <code>java.lang.String</code> objects, and
     * each of them are untranslated, in programmatic English.</p>
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
     * Returns all the messages that are part of this SpectroStatus.
     * <p>Messages are information to the human user, that can be of its
     * interest. This does NOT include debugging messages.</p>
     * <p>The Collection contains <code>java.lang.String</code> objects, and
     * each of them are untranslated, in programmatic English.</p>
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
