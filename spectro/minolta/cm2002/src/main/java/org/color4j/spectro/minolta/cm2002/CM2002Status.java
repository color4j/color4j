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

package org.color4j.spectro.minolta.cm2002;

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
public class CM2002Status implements SpectroStatus
{
    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static CM2002Status create( String response )
    {
        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        response = response.replaceAll( "\r\n", "" );
        // Split the message first
        String[] result = response.split( " " );

        //Check error codes:

        try
        {
            // Check the energy level
            if( result[ 1 ].equals( "N" ) )
            {
                errorMsg.add( "MSG_LOW_BATTERY" );
            }
            else if( result[ 1 ].equals( "Y" ) )
            {
                // Check the error first
                if( result[ 0 ].charAt( 0 ) == 'E' )
                {
                    if( result[ 0 ].charAt( 1 ) == '0' && result[ 0 ].charAt( 2 ) == '0' )
                    {
                        errorMsg.add( "MSG_COMMAND_ERROR" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '0' && result[ 0 ].charAt( 2 ) == '2' )
                    {
                        errorMsg.add( "MSG_ILLUMINATION_TIMEOUT" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '0' && result[ 0 ].charAt( 2 ) == '5' )
                    {
                        errorMsg.add( "MSG_XENON" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '1' && result[ 0 ].charAt( 2 ) == '0' )
                    {
                        errorMsg.add( "MSG_NO_DATA" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '1' && result[ 0 ].charAt( 2 ) == '1' )
                    {
                        errorMsg.add( "MSG_CALIBRATION_ERROR" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '1' && result[ 0 ].charAt( 2 ) == '3' )
                    {
                        errorMsg.add( "MSG_CIRCUITS_ERROR" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '1' && result[ 0 ].charAt( 2 ) == '8' )
                    {
                        errorMsg.add( "MSG_EEPROM_ERROR" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '1' && result[ 0 ].charAt( 2 ) == '9' )
                    {
                        errorMsg.add( "MSG_MEMORY_ERROR" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '2' && result[ 0 ].charAt( 2 ) == '1' )
                    {
                        errorMsg.add( "MSG_MEMORYWRITING_ERROR" );
                    }
                    else
                    {
                        errorMsg.add( "MSG_UNKNOWN_STRING" );
                    }
                }

                // Then check warning
                if( result[ 0 ].charAt( 0 ) == 'W' )
                {
                    if( result[ 0 ].charAt( 1 ) == '0' && result[ 0 ].charAt( 2 ) == '0' )
                    {
                        warningMsg.add( "MSG_LIGHT_WARNING" );
                    }
                    else if( result[ 0 ].charAt( 1 ) == '0' && result[ 0 ].charAt( 2 ) == '1' )
                    {
                        warningMsg.add( "MSG_LIGHT_WARNING" );
                    }
                    else
                    {
                        errorMsg.add( "MSG_UNKNOWN_STRING" );
                    }
                }
            }
            else
            {
                errorMsg.add( "MSG_UNKNOWN_STRING" );
            }

            return new CM2002Status( msg, warningMsg, errorMsg );
        }
        catch( IndexOutOfBoundsException arrayEx )
        {
            errorMsg.add( "MSG_UNKNOWN_STRING" );

            return new CM2002Status( msg, warningMsg, errorMsg );
        }
        /*
        finally
        {
            errorMsg.add( "Error reading status string" );
            
            return new CM2002Status( msg, warningMsg, errorMsg );
        }
         */
    }

    private CM2002Status()
    {
    }

    CM2002Status( Collection messages, Collection warnings, Collection errors )
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