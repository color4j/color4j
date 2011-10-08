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

package org.color4j.spectro.datacolor.sf600;

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
public class SF600Status
    implements SpectroStatus
{
    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static SF600Status create( String response )
    {
        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        //Check error codes:

        try
        {
            //Calibration error
            if( response.charAt( 8 ) == 'E' )
            {
                errorMsg.add( "MSG_CALIBRATION_ERROR" );
            }

            //Firmware error
            if( response.charAt( 9 ) == 'E' )
            {
                errorMsg.add( "MSG_FIRMWARE_DATA_ERROR" );
            }
            else if( response.charAt( 9 ) != 'x' )
            {
                errorMsg.add( "MSG_FIRMWARE_ERROR" );
            }

            //Viewer error
            if( response.charAt( 10 ) == '3' )
            {
                errorMsg.add( "MSG_VIEWER_INDETERMINATE" );
            }
            else if( response.charAt( 10 ) == '4' )
            {
                errorMsg.add( "MSG_VIEWER_SENSOR_ERROR" );
            }

            //Measurement error
            if( response.charAt( 11 ) == 'T' )
            {
                errorMsg.add( "MSG_MEASUREMENT_DATA_OUT_OF_RANGE" );
            }
            else if( response.charAt( 11 ) == 'E' )
            {
                errorMsg.add( "MSG_MEASUREMENT_ERROR" );
            }
            else if( response.charAt( 11 ) == 'F' )
            {
                errorMsg.add( "MSG_MEASUREMENT_ENERGY_LOW" );
            }

            //Specular port error
            if( response.charAt( 12 ) == 'T' )
            {
                errorMsg.add( "MSG_SPECULAR_MOVEMENT_TIMEOUT" );
            }
            else if( response.charAt( 12 ) == 'E' )
            {
                errorMsg.add( "MSG_SPECULAR_INVALID" );
            }

            //
            if( response.charAt( 13 ) == 'T' )
            {
                errorMsg.add( "MSG_APERTURE_MOVEMENT_TIMEOUT" );
            }
            else if( response.charAt( 13 ) == 'E' )
            {
                errorMsg.add( "MSG_APERTURE_INVALID" );
            }

            if( response.charAt( 14 ) == 'T' )
            {
                errorMsg.add( "MSG_FILTER_MOVEMENT_TIMEOUT" );
            }
            else if( response.charAt( 14 ) == 'E' )
            {
                errorMsg.add( "MSG_FILTER_INVALID" );
            }

            return new SF600Status( msg, warningMsg, errorMsg );
        }
        catch( IndexOutOfBoundsException arrayEx )
        {
            errorMsg.add( "MSG_UNKNOWN_STATUS" );

            return new SF600Status( msg, warningMsg, errorMsg );
        }
    }

    SF600Status( Collection messages, Collection warnings, Collection errors )
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