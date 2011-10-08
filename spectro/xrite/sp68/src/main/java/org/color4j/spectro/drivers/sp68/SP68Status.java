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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * Meta-information of an operation with the spectrophotometer.
 * <p>The SpectroStatus carries information of the SpectroReading, especially
 * about the result of a measurement, success or failure. If the the
 * <code>isSuccess</code> method, the <code>getErrors</code> should return
 * an empty Collection.</p>
 *
 */
public class SP68Status
    implements SpectroStatus
{
    static private Logger m_Logger = Logger.getLogger( SP68Status.class.getName() );

    static
    {
    }

    public static HashMap statusCodes;

    static
    {
        statusCodes = new HashMap();

        statusCodes.put( "01", "MEMORY_CLEARED_OPERATOR" );
        statusCodes.put( "02", "INVALID_EPROM" );
        statusCodes.put( "03", "MEMORY_CLEARED_EPROM" );
        statusCodes.put( "04", "PRESETS_OUT_OF_RANGE" );
        statusCodes.put( "05", "MEMORY_CLEARED_RCI" );
        statusCodes.put( "06", "MICROCONTROLLER_FAILURE" );
        statusCodes.put( "07", "RAM_FAILURE" );
        statusCodes.put( "08", "ROM_FAILURE" );
        statusCodes.put( "09", "READ_SWITCH_EARLY_RELEASE" );

        statusCodes.put( "0A", "REFLECTANCE_LIMIT_EXCEEDED" );
        statusCodes.put( "0B", "BATTERY_LOW" );
        statusCodes.put( "0C", "RESET_DURING_READING" );
        statusCodes.put( "0D", "LIGHT_LEAKAGE_ERROR" );
        statusCodes.put( "0F", "LAMP_FAILURE" );
        statusCodes.put( "10", "BATTERIES_WEAK" );
        statusCodes.put( "11", "STORAGE_LIMIT_REACHED" );
        statusCodes.put( "12", "ACTIVE_GROUP_STORAGE_LIMIT_REACHED" );
        statusCodes.put( "13", "CALIBRATION_REQUIRED_MEMORY" );
        statusCodes.put( "14", "CALIBRATION_REQUIRED_TEMPERATURE" );
        statusCodes.put( "15", "CALIBRATION_REQUIRED_TIME" );
        statusCodes.put( "16", "UNUSED" );
        statusCodes.put( "17", "ZERO_CALIBRATION_REQUIRED" );
        statusCodes.put( "18", "CALIBRATION_ABORTED" );
        statusCodes.put( "19", "UNUSED" );
        statusCodes.put( "1A", "CALIBRATION_REQUIRED_LAMP" );
        statusCodes.put( "1B", "CALIBRATION_REQUIRED_RCI" );
        statusCodes.put( "1C", "ZERO_CALIBRATION_LIMIT_EXCEEDED" );
        statusCodes.put( "1D", "CALIBRATION_ERROR_TYPE_1" );
        statusCodes.put( "1E", "CALIRBATION_ERROR_TYPE_2" );
        statusCodes.put( "1F", "CALIBRATION_ERROR_TYPE_3" );
        statusCodes.put( "20", "CELL_LEVEL_HIGH" );
        statusCodes.put( "21", "CELL_LEVEL_LOW" );
        statusCodes.put( "22", "CELL_FAILURE" );
        statusCodes.put( "23", "RECEIVE_BUFFER_REACHED_LIMIT" );
        statusCodes.put( "24", "JOB_STACK_OVERFLOW_ERROR" );
        statusCodes.put( "30", "ZERO_CALIBRATION_CHANGED" );
    }

    Vector m_Warnings = null;
    Vector m_Errors = null;
    Vector m_Messages = null;

    public SP68Status()
    {
        m_Warnings = new Vector();
        m_Errors = new Vector();
        m_Messages = new Vector();
    }

    public SP68Status( Vector warnings, Vector errors, Vector messages )
    {
        m_Warnings = warnings;
        m_Errors = errors;
        m_Messages = messages;
    }

    public static SP68Status create( Vector errors )
    {
        SP68Status status = new SP68Status();

        for( Iterator errList = errors.iterator(); errList.hasNext(); )
        {
            Object key = errList.next();

            String message = (String) statusCodes.get( key );

            if( message != null )
            {
                status.addError( message );
                m_Logger.info( "ERROR : " + message );
            }
        }

        return status;
    }

    public static SP68Status create( String statusCode )
    {
        SP68Status status = new SP68Status();

        try
        {
            Pattern pattern = Pattern.compile( "(OK\\d\\d)[\r\n]*" );

            Matcher matcher = pattern.matcher( statusCode );

            if( matcher.find() )
            {
                String code = matcher.group( 1 ).substring( 2 );
                m_Logger.info( "Status code : " + code );

                if( "00".equals( code ) )
                {
                    status.addMessage( "STATUS_OK" );
                }
                else
                {
                    String message = (String) statusCodes.get( code );

                    if( message != null )
                    {
                        status.addError( message );
                        m_Logger.info( "ERROR: " + message );
                    }
                    else
                    {
                        status.addError( "UNRECOGNIZED_STATUS" );
                        m_Logger.info( "ERROR: UNRECOGNIZED_STATUS" );
                    }
                }
            }
            else
            {
                //status.addError( "UNRECOGNIZED_STATUS" );
            }
        }
        catch( PatternSyntaxException patSynEx )
        {
            m_Logger.info( "Malformed Regular Expression while creating new status" );
            return null;
        }

        return status;
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
        return m_Errors.isEmpty();
    }

    /**
     * Returns true if the Status reflects a failed operation.
     * <p>This method will ALWAYS return the negated value of the
     * <code>isSuccess</code> method.</p>
     */
    public boolean isFailure()
    {
        return !m_Errors.isEmpty();
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
        return m_Warnings;
    }

    public void addWarning( String warning )
    {
        m_Warnings.add( warning );
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
        return m_Errors;
    }

    public void addError( String error )
    {
        m_Errors.add( error );
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
