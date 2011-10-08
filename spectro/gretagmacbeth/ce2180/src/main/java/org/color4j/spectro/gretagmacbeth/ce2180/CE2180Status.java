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

package org.color4j.spectro.gretagmacbeth.ce2180;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
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
public class CE2180Status
    implements SpectroStatus
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CE2180Status.class.getName() );
    }

    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static CE2180Status create( String response )
    {
        boolean m_unknown = false;

        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        if( response.equals( "ERROR_OPENING" ) )
        {
            errorMsg.add( "MSG_PORT_CONNECTION_FAILED" );
            //msg.add( "ERROR_OPENING" );
        }
        else if( response.equals( "TIMEOUT_ERROR" ) )
        {
            errorMsg.add( "MSG_TIMEOUT_ERROR" );
        }
        else
        {
            try
            {
                // Specular
                if( response.charAt( 3 ) == '0' )
                //msg.add( "Specular Included" );
                {
                    m_Logger.info( "Status - Specular Included" );
                }
                else if( response.charAt( 3 ) == '1' )
                //msg.add( "Specular Excluded" );
                {
                    m_Logger.info( "Status - Specular Excluded" );
                }
                else if( response.charAt( 3 ) == '2' )
                {
                    m_Logger.log( Level.WARNING, "Specular failure - port is in and out" );
                    errorMsg.add( "MSG_SPECULAR_FAILURE_IN_AND_OUT" );
                }
                else if( response.charAt( 3 ) == '9' )
                {
                    m_Logger.log( Level.WARNING, "Specular failure - port is not in and out" );
                    errorMsg.add( "MSG_SPECULAR_FAILURE_NOT_IN_AND_OUT" );
                }
                else if( response.charAt( 3 ) != '0' || response.charAt( 3 ) == '1' ||
                         response.charAt( 3 ) != '2' || response.charAt( 3 ) == '9' )
                {
                    m_unknown = true;
                }

                // Measurement interval
                if( response.charAt( 6 ) == '0' )
                //msg.add( "Interval 10 nm" );
                {
                    m_Logger.info( "Status - Interval 10 nm" );
                }
                else if( response.charAt( 6 ) == '2' )
                //msg.add( "Interval 2 nm" );
                {
                    m_Logger.info( "Status - Interval 2 nm" );
                }
                else if( response.charAt( 6 ) == '5' )
                //msg.add( "Interval 5 nm" );
                {
                    m_Logger.info( "Status - Interval 5 nm" );
                }
                else if( response.charAt( 6 ) != '0' || response.charAt( 6 ) == '2' ||
                         response.charAt( 6 ) != '5' )
                {
                    m_unknown = true;
                }

                // Floating point Error
                if( response.charAt( 7 ) == '0' )
                //msg.add( "Floating point success" );
                {
                    m_Logger.info( "Status - Floating point success" );
                }
                else if( response.charAt( 7 ) == 'J' )
                {
                    m_Logger.log( Level.WARNING, "Floating point failure" );
                    errorMsg.add( "MSG_FLOATIONG_PT_ERROR" );
                }
                else if( response.charAt( 7 ) != '0' || response.charAt( 7 ) == 'J' )
                {
                    m_unknown = true;
                }

                // Red and Blue limits
                if( response.charAt( 8 ) == '0' )
                //msg.add( "Read and Blue limits success" );
                {
                    m_Logger.info( "Status - Read and Blue limits success" );
                }
                else if( response.charAt( 8 ) == '1' )
                //msg.add( "Read and Blue limits low" );
                {
                    m_Logger.info( "Status - Read and Blue limits low" );
                }
                else if( response.charAt( 8 ) != '0' || response.charAt( 8 ) == '1' )
                {
                    m_unknown = true;
                }

                // Centroid calculation
                if( response.charAt( 10 ) == '0' )
                //msg.add( "Centroid calculation success" );
                {
                    m_Logger.info( "Status - Centroid calculation success" );
                }
                else if( response.charAt( 10 ) == '1' )
                {
                    m_Logger.log( Level.FINER, "Cetnroid calculation failure" );
                    errorMsg.add( "MSG_CENTROID_CALC_ERROR" );
                }
                else if( response.charAt( 10 ) != '0' || response.charAt( 10 ) == '1' )
                {
                    m_unknown = true;
                }

                // Flash voltage up
                if( response.charAt( 12 ) == '0' )
                //msg.add( "Flash success" );
                {
                    m_Logger.info( "Status - Flash success" );
                }
                else if( response.charAt( 12 ) == '9' )
                {
                    m_Logger.log( Level.FINER, "Flash failure" );
                    errorMsg.add( "MSG_FLASH_ERROR" );
                }
                else if( response.charAt( 12 ) != '0' || response.charAt( 12 ) == '9' )
                {
                    m_unknown = true;
                }

                // Beam switch position
                if( response.charAt( 13 ) == '0' )
                //msg.add( "Beam switch IN" );
                {
                    m_Logger.info( "Status - Beam switch IN" );
                }
                else if( response.charAt( 13 ) == '1' )
                //msg.add( "Beam switch out" );
                {
                    m_Logger.info( "Status - Beam switch out" );
                }
                else if( response.charAt( 13 ) != '0' || response.charAt( 13 ) == '1' )
                {
                    m_unknown = true;
                }

                if( m_unknown )
                {
                    m_Logger.log( Level.FINER, "Unrecognized status : " + response );
                    errorMsg.add( "MSG_UNKNOWN_STATUS" );
                }

                return new CE2180Status( msg, warningMsg, errorMsg );
            }
            catch( IndexOutOfBoundsException arrayEx )
            {
                m_Logger.log( Level.FINER, "Unrecognized status string : " + response );
                errorMsg.add( "MSG_UNKNOWN_STATUS" );
            }
        }

        return new CE2180Status( msg, warningMsg, errorMsg );
    }

    public CE2180Status()
    {
        m_ErrorMessages = new Vector();
        m_WarningMessages = new Vector();
        m_Messages = new Vector();
    }

    CE2180Status( Collection messages, Collection warnings, Collection errors )
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