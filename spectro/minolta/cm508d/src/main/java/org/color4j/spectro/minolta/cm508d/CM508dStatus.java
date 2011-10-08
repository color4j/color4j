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

package org.color4j.spectro.minolta.cm508d;

import java.util.Collection;
import java.util.Vector;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * Meta-information of an operation with the spectrophotometer.
 * <p>The SpectroStatus carries information of the SpectroReading, especially
 * about the result of a measurement, success or failure. If the the
 * <code>isSuccess</code> method, the <code>getErrors</code> should return
 * an empty Collection.</p>
 *
 */
public class CM508dStatus implements SpectroStatus
{
    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static CM508dStatus create( String response )
    {
        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        //Check error codes:

        try
        {
            if( response.equals( "NOT_VALID_CM500" ) )
            {
                errorMsg.add( "MSG_NOTVALID_SPECTROLINO" );
            }
            else if( response.equals( "TIMEOUT_ERROR" ) )
            {
                errorMsg.add( "MSG_TIMEOUT_ERROR" );
            }
            else if( response.equals( "INVALID_RETURN" ) )
            {
                errorMsg.add( "MSG_INVALID_RETURN" );
            }
            else if( response.equals( "UNKNOWN_STRING" ) )
            {
                errorMsg.add( "MSG_UNKNOWN_STRING" );
            }
            else if( response.startsWith( "OK" ) )
            {
                switch( response.charAt( 3 ) )
                {
                case '0':
                    break;
                case '1':
                    errorMsg.add( "MSG_WHITE_CALIBRATION_REQUIRED" );
                    break;
                case '2':
                    errorMsg.add( "MSG_LIGHT_ENERGY_LOW" );
                    break;
                case '3':
                    errorMsg.add( "MSG_VOLTAGE_LOW" );
                    break;
                case '4':
                    errorMsg.add( "MSG_LIGHT_ENERGY_LOW" );
                    errorMsg.add( "MSG_VOLTAGE_LOW" );
                    break;
                case '5':
                    errorMsg.add( "MSG_TARGET_COLOR_DATA_DIFFER" );
                    break;
                default:
                    errorMsg.add( "MSG_UNKNOWN_STRING" );
                }
            }
            else if( response.startsWith( "ER" ) )
            {
                response = response.substring( 2 );

                if( "00".equals( response ) )
                {
                    errorMsg.add( "MSG_INVALID_COMMAND" );
                }
                else if( "02".equals( response ) )
                {
                    errorMsg.add( "MSG_LAMP_NOT_CHARGED" );
                }
                else if( "05".equals( response ) )
                {
                    errorMsg.add( "MSG_LAMP_DID_NOT_FLASH" );
                }
                else if( "07".equals( response ) )
                {
                    errorMsg.add( "MSG_WHITE_CALIBRATION_DATA_CHANGED" );
                }
                else if( "11".equals( response ) )
                {
                    errorMsg.add( "MSG_WHITE_ZERO_CALIBRATION_INVALID" );
                }
                else if( "13".equals( response ) )
                {
                    errorMsg.add( "MSG_AD_CONVERTER_ERROR" );
                }
                else if( "18".equals( response ) )
                {
                    errorMsg.add( "MSG_EEPROM_DATA_DESTROYED" );
                }
                else if( "21".equals( response ) )
                {
                    errorMsg.add( "MSG_ERORR_WRITIG_CARD" );
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

            return new CM508dStatus( msg, warningMsg, errorMsg );
        }
        catch( IndexOutOfBoundsException arrayEx )
        {
            errorMsg.add( "MSG_UNKNOWN_STRING" );
        }

        return new CM508dStatus( msg, warningMsg, errorMsg );
        /*
        finally
        {
            errorMsg.add( "Error reading status string" );
         
            return new CM508dStatus( msg, warningMsg, errorMsg );
        }
         */
    }

    private CM508dStatus()
    {
        m_ErrorMessages = new Vector();
        m_WarningMessages = new Vector();
        m_Messages = new Vector();
    }

    CM508dStatus( Collection messages, Collection warnings, Collection errors )
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
