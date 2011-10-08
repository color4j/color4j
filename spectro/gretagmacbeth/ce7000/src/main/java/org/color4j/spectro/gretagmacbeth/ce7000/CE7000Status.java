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

package org.color4j.spectro.gretagmacbeth.ce7000;

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
public class CE7000Status implements SpectroStatus
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CE7000Status.class.getName() );
    }

    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static CE7000Status create( String response )
    {
        boolean m_unknown = false;

        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        m_Logger.info( "CE7000 Spectro : Actual status string, " + response );

        if( response.equals( "ERROR_OPENING" ) )
        {
            errorMsg.add( "MSG_ERROR_OPENING_PORT" );
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
                // Measurement mode
                if( response.charAt( 2 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro: Status - reflection mode" );
                }
                else if( response.charAt( 2 ) == '1' )
                {
                    m_Logger.info( "CE7000 Spectro: Status - transmission mode" );
                }
                else if( response.charAt( 2 ) != '0' || response.charAt( 2 ) != '1' )
                {
                    m_unknown = true;
                }

                // Specular
                if( response.charAt( 3 ) == '0' )
                //msg.add( "Specular Included" );
                {
                    m_Logger.info( "CE7000 Spectro : Status - Specular Included" );
                }
                else if( response.charAt( 3 ) == '1' )
                //msg.add( "Specular Excluded" );
                {
                    m_Logger.info( "CE7000 Spectro : Status - Specular Excluded" );
                }
                else if( response.charAt( 3 ) == '9' )
                {
                    errorMsg.add( "MSG_SPECULAR_FAILURE" );
                }
                else if( response.charAt( 3 ) != '0' || response.charAt( 3 ) == '1' ||
                         response.charAt( 3 ) == '9' )
                {
                    m_unknown = true;
                }

                // UV Filter Position
                if( response.charAt( 4 ) == '1' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - UV Filter in" );
                }
                else if( response.charAt( 4 ) == '2' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - UV Filter partial" );
                }
                //else if ( response.charAt( 4 ) == '9' )
                //    errorMsg.add( "UV Filter failure" );
                else if( response.charAt( 4 ) != '1' || response.charAt( 4 ) != '2' ||
                         response.charAt( 4 ) != '9' )
                {
                    m_unknown = true;
                }

                // Lens Position
                if( response.charAt( 5 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - LAV Lens Position" );
                }
                else if( response.charAt( 5 ) == '1' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - MAV Lens Position" );
                }
                else if( response.charAt( 5 ) == '2' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - SAV Lens Position" );
                }
                else if( response.charAt( 5 ) == '3' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - VSAV Lens Position" );
                }
                else if( response.charAt( 5 ) == '9' )
                {
                    errorMsg.add( "MSG_LENSPOSITION_FAILURE" );
                }
                else if( response.charAt( 5 ) != '0' || response.charAt( 5 ) != '1' ||
                         response.charAt( 5 ) != '2' || response.charAt( 5 ) != '3' ||
                         response.charAt( 5 ) != '9' )
                {
                    m_unknown = true;
                }

                // Aperture Position
                if( response.charAt( 6 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - LAV Aperture Position" );
                }
                else if( response.charAt( 6 ) == '1' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - VSAV Aperture Position" );
                }
                else if( response.charAt( 6 ) == '2' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - MAV Aperture Position" );
                }
                else if( response.charAt( 6 ) == '3' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - SAV Aperture Position" );
                }
                else if( response.charAt( 6 ) != '0' || response.charAt( 6 ) != '1' ||
                         response.charAt( 6 ) != '2' || response.charAt( 6 ) != '3' ||
                         response.charAt( 6 ) != '9' )
                {
                    m_unknown = true;
                }

                // Floating Point
                if( response.charAt( 7 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Floating Point success" );
                }
                else
                {
                    checkFloatingPointError( response.charAt( 7 ), errorMsg );
                }

                // Raw Light
                if( response.charAt( 8 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Raw Light success" );
                }
                else if( response.charAt( 8 ) == '1' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Raw Light low" );
                }
                else if( response.charAt( 8 ) == '2' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Raw Light high" );
                }
                else if( response.charAt( 8 ) != '0' || response.charAt( 8 ) != '1' ||
                         response.charAt( 8 ) != '2' )
                {
                    m_unknown = true;
                }

                // Raw Dark
                if( response.charAt( 9 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Raw Dark success" );
                }
                else if( response.charAt( 9 ) == '1' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Raw Dark low" );
                }
                else if( response.charAt( 9 ) == '2' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Raw Dark high" );
                }
                else if( response.charAt( 9 ) != '0' || response.charAt( 9 ) != '1' ||
                         response.charAt( 9 ) != '2' )
                {
                    m_unknown = true;
                }

                // 400 nM Ratio
                if( response.charAt( 10 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - 400 nM Ratio success" );
                }
                else if( response.charAt( 10 ) == '1' )
                {
                    errorMsg.add( "MSG_400NM_RATIO_FAILURE" );
                }
                else if( response.charAt( 10 ) != '0' || response.charAt( 10 ) != '1' )
                {
                    m_unknown = true;
                }

                // Repeatability
                if( response.charAt( 11 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Repeatability success" );
                }
                else if( response.charAt( 11 ) == '1' )
                {
                    errorMsg.add( "MSG_REPEATABILITY_FAILURE" );
                }
                else if( response.charAt( 11 ) == 'M' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Repeatability return M char" );
                }
                else if( response.charAt( 11 ) != '0' || response.charAt( 11 ) != '1' )
                {
                    m_unknown = true;
                }

                // Zero Calibration
                if( response.charAt( 12 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Zero Calibration success" );
                }
                else if( response.charAt( 12 ) == '1' || response.charAt( 12 ) == 'F' )
                {
                    errorMsg.add( "MSG_ZERO_CALIBRATION_FAILURE" );
                }
                else if( response.charAt( 12 ) != '0' || response.charAt( 12 ) != '1' )
                {
                    m_unknown = true;
                }

                // Last Lamp Flashed
                if( response.charAt( 13 ) == 'M' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Measure Lamp" );
                }
                else if( response.charAt( 13 ) == 'R' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Reference Lamp" );
                }
                else if( response.charAt( 13 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - Lamp has not been flash" );
                }
                else if( response.charAt( 13 ) != 'M' || response.charAt( 13 ) != 'R' || response.charAt( 13 ) != '0' )
                {
                    m_unknown = true;
                }

                // 550 nM Reference
                if( response.charAt( 14 ) == '0' )
                {
                    m_Logger.info( "CE7000 Spectro : Status - 550 nM Reference success" );
                }
                else if( response.charAt( 14 ) == '1' )
                {
                    errorMsg.add( "MSG_550NM_FAILURE" );
                }
                else if( response.charAt( 14 ) != '0' || response.charAt( 14 ) != '1' )
                {
                    m_unknown = true;
                }

                if( m_unknown )
                {
                    errorMsg.add( "MSG_UNKNOWN_STATUS" );
                }

                return new CE7000Status( msg, warningMsg, errorMsg );
            }
            catch( IndexOutOfBoundsException arrayEx )
            {
                errorMsg.add( "MSG_UNKNOWN_STATUS" );
            }
        }

        return new CE7000Status( msg, warningMsg, errorMsg );
    }

    public CE7000Status()
    {
        m_ErrorMessages = new Vector();
        m_WarningMessages = new Vector();
        m_Messages = new Vector();
    }

    CE7000Status( Collection messages, Collection warnings, Collection errors )
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

    private static void checkFloatingPointError( char input, Vector errorMsg )
    {
        int value = ( (int) input ) & 15;

        if( ( value & 0x01 ) == 0x01 )
        {
            errorMsg.add( "MSG_ZERO_DIV" );
        }

        if( ( value & 0x02 ) == 0x02 )
        {
            errorMsg.add( "MSG_UNDERFLOW" );
        }

        if( ( value & 0x04 ) == 0x04 )
        {
            errorMsg.add( "MSG_OVERFLOW" );
        }

        if( ( value & 0x08 ) == 0x08 )
        {
            errorMsg.add( "MSG_INVALID_OPERAND" );
        }
    }
}
