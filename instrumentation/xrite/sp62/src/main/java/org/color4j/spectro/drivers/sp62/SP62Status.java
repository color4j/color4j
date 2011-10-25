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

package org.color4j.spectro.drivers.sp62;

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
public class SP62Status
    implements SpectroStatus
{
    static private Logger m_Logger = Logger.getLogger( SP62Status.class.getName() );

    static
    {
    }

    public static final String NO_ERROR = "00"; //No errors
    public static final String MEASUREMENT_STATUS = "01"; //Measurement complete
    public static final String CALIBRATION_STATUS = "02"; //Calibration complete
    public static final String KEYPRESSED_STATUS = "03"; //Key was pressed
    public static final String DEFAULTS_LOADED_STATUS = "04"; //Default configuration loaded

    public static final String BAD_COMMAND = "11"; //Unrecognized command
    public static final String BAD_PARAMETERS = "12"; //One or more parameters missing or too many
    public static final String PRM_RANGE_ERROR = "13"; //One ore more of the parameters is out of range
    public static final String BUSY = "14"; //Instrumenet is currently busy, command ignored
    public static final String USER_ABORT_ERROR = "15"; //User aborted process
    public static final String DATA_SYNC_ERROR = "16"; //Command was expecting to receive data

    public static final String MEASUREMENT_ERROR = "20"; //General measurement error
    public static final String TIMEOUT = "21"; //Timeout

    public static final String BAD_READING = "27"; //Failed to accurately complete the reading
    public static final String NEEDS_CAL_ERROR = "28"; //Instrument requires a new calibration
    public static final String CAL_FAILURE_ERROR = "29"; //Calibration failed
    public static final String CAL_TIMEOUT_ERROR = "2a"; //Calibration timeout

    public static final String INSTRUMENT_ERROR = "30"; //General internal instrument error
    public static final String LAMP_ERROR = "31"; //Lamp is not functioning
    public static final String FILTER_ERROR = "32"; //A filter is not responding
    public static final String FILTER_MOTOR_ERROR = "33"; //Stepper mototr not functioning
    public static final String KEYPAD_ERROR = "35"; //Keypad test failed
    public static final String DISPLAY_ERROR = "36"; //Display test failed
    public static final String MEMORY_ERROR = "37"; //Memory test failed
    public static final String ADC_ERROR = "38"; //Analog to digital conversion failed
    public static final String PROCESSOR_ERROR = "39"; //Microcontroller test failed
    public static final String BATTERY_ERROR = "3a"; //Battery test failed
    public static final String BATTERY_LOW_ERROR = "3b"; //The charge level of the batteries is getting low
    public static final String INPUT_POWER_ERROR = "3c"; //Battery charger voltage too low or too high
    public static final String TEMPERATURE_ERROR = "3d"; //The temperature exceeds limit for proper operation

    public static final String BATTERY_ABSENT_ERROR = "3e"; //The battery pack is not in the instrument
    public static final String TRAN_LAMP_ERROR = "3f"; //Transmission lamp position failure. Measurement fails
    public static final String INVALID_COMMAND_ERROR = "40"; // Invalid command; not permitted under current mode

    public static final String TOLERANCES_ERROR = "41"; //The maximum tolerance per standard exceeded or none exist

    public static final String STANDARD_NAME_ERROR = "42"; //Too many characters in the standard name
    public static final String SAVING_STANDARD_ERROR = "43"; //No memory available to store standard
    public static final String SAVING_PROJECT_ERROR = "44"; //No memory available to store project or standard limit exceeded

    public static final String SAVING_TEXT_RECORD_ERROR = "45"; //No memory available to store text record
    public static final String SHADE_SORT_ERROR = "46"; //Invalid shade sort setup or does not exist
    public static final String INVALID_RECORD = "47"; //The database record does not exist
    public static final String BATTERY_OPEN_ERROR = "48"; //Battery reached its thermal cut off temperature    

    Vector m_Warnings = null;
    Vector m_Errors = null;
    Vector m_Messages = null;

    public SP62Status()
    {
        m_Warnings = new Vector();
        m_Errors = new Vector();
        m_Messages = new Vector();
    }

    public SP62Status( Vector warnings, Vector errors, Vector messages )
    {
        m_Warnings = warnings;
        m_Errors = errors;
        m_Messages = messages;
    }

    public static SP62Status create( String statusCode )
    {
        String sc = new String( statusCode );

        SP62Status status = new SP62Status();

        if( sc.equals( NO_ERROR ) )
        {
            m_Logger.info( "No error" );
        }
        else if( sc.equals( MEASUREMENT_STATUS ) )
        {
            m_Logger.info( "Measurement complete" );
        }
        else if( sc.equals( CALIBRATION_STATUS ) )
        {
            m_Logger.info( "Calibration complete" );
        }
        else if( sc.equals( KEYPRESSED_STATUS ) )
        {
            m_Logger.info( "Key pressed" );
        }
        else if( sc.equals( DEFAULTS_LOADED_STATUS ) )
        {
            m_Logger.info( "Defaults loaded" );
        }
        else if( sc.equals( BAD_COMMAND ) )
        {
            m_Logger.warning( "Bad Command" );
            status.m_Errors.add( "MSG_BAD_COMMAND" );
        }
        else if( sc.equals( BAD_PARAMETERS ) )
        {
            m_Logger.warning( "Bad Parameters" );
            status.m_Errors.add( "MSG_BAD_PARAMETERS" );
        }
        else if( sc.equals( PRM_RANGE_ERROR ) )
        {
            m_Logger.warning( "Parameters out of range" );
            status.m_Errors.add( "MSG_PARAMETER_OUT_RANGE" );
        }
        else if( sc.equals( BUSY ) )
        {
            m_Logger.warning( "Instrument busy" );
            status.m_Errors.add( "MSG_BUSY" );
        }
        else if( sc.equals( USER_ABORT_ERROR ) )
        {
            m_Logger.warning( "User aborted" );
            status.m_Errors.add( "MSG_ABORTED" );
        }
        else if( sc.equals( DATA_SYNC_ERROR ) )
        {
            m_Logger.warning( "Command expect to receive data" );
            status.m_Errors.add( "MSG_EXPECT_DATA" );
        }
        else if( sc.equals( MEASUREMENT_ERROR ) )
        {
            m_Logger.warning( "General measurement error" );
            status.m_Errors.add( "MSG_MEASURE_ERROR" );
        }
        else if( sc.equals( TIMEOUT ) )
        {
            m_Logger.warning( "Command time out" );
            status.m_Errors.add( "MSG_COMMAND_TIMEOUT" );
        }
        else if( sc.equals( BAD_READING ) )
        {
            m_Logger.warning( "Failed to accuratel complete reading" );
            status.m_Errors.add( "MSG_READING_FAILED" );
        }
        else if( sc.equals( NEEDS_CAL_ERROR ) )
        {
            m_Logger.warning( "Calibration required" );
            status.m_Errors.add( "MSG_CALIBRATE" );
        }
        else if( sc.equals( CAL_FAILURE_ERROR ) )
        {
            m_Logger.warning( "Calibration failed" );
            status.m_Errors.add( "MSG_CALIBRATE_FAILED" );
        }
        else if( sc.equals( CAL_TIMEOUT_ERROR ) )
        {
            m_Logger.warning( "Calibration timeout warning" );
            status.m_Errors.add( "MSG_CALIBRATE_TIMEOUT" );
        }
        else if( sc.equals( INSTRUMENT_ERROR ) )
        {
            m_Logger.warning( "General instrument error" );
            status.m_Errors.add( "MSG_SPECTRO_ERROR" );
        }
        else if( sc.equals( LAMP_ERROR ) )
        {
            m_Logger.warning( "Lamp not functioning" );
            status.m_Errors.add( "MSG_LAMP_ERROR" );
        }
        else if( sc.equals( FILTER_ERROR ) )
        {
            m_Logger.warning( "Invalid filter response" );
            status.m_Errors.add( "MSG_INVALID_FILTER" );
        }
        else if( sc.equals( FILTER_MOTOR_ERROR ) )
        {
            m_Logger.info( "Filter stepper motor not functioning" );
            status.m_Errors.add( "MSG_MOTOR_ERROR" );
        }
        else if( sc.equals( KEYPAD_ERROR ) )
        {
            m_Logger.warning( "Keypad test failed" );
            status.m_Errors.add( "MSG_KEYPAD_ERROR" );
        }
        else if( sc.equals( DISPLAY_ERROR ) )
        {
            m_Logger.warning( "Display test failed" );
            status.m_Errors.add( "MSG_DISPLAY_ERROR" );
        }
        else if( sc.equals( MEMORY_ERROR ) )
        {
            m_Logger.warning( "Memory test failed" );
            status.m_Errors.add( "MSG_MEMORY_ERROR" );
        }
        else if( sc.equals( ADC_ERROR ) )
        {
            m_Logger.warning( "ADC test failed" );
            status.m_Errors.add( "MSG_ADC_ERROR" );
        }
        else if( sc.equals( PROCESSOR_ERROR ) )
        {
            m_Logger.warning( "Microcontroller test failed" );
            status.m_Errors.add( "MSG_MICROCONTROLLER_ERROR" );
        }
        else if( sc.equals( BATTERY_ERROR ) )
        {
            m_Logger.warning( "Battery test failed" );
            status.m_Errors.add( "MSG_BATTERY_ERROR" );
        }
        else if( sc.equals( BATTERY_LOW_ERROR ) )
        {
            m_Logger.warning( "Charge level of batteries low" );
            status.m_Errors.add( "MSG_BATTERIES_LOW" );
        }
        else if( sc.equals( INPUT_POWER_ERROR ) )
        {
            m_Logger.warning( "Battery charger voltage too low or too high" );
            status.m_Errors.add( "MSG_CHARGER_ERROR" );
        }
        else if( sc.equals( TEMPERATURE_ERROR ) )
        {
            m_Logger.warning( "Temperature exceeds limit for proper operation" );
            status.m_Errors.add( "MSG_HIGH_TEMPERATURE" );
        }
        else if( sc.equals( BATTERY_ABSENT_ERROR ) )
        {
            m_Logger.warning( "Battery pack is not in instrument" );
            status.m_Errors.add( "MSG_NO_BATTERY" );
        }
        else if( sc.equals( TRAN_LAMP_ERROR ) )
        {
            m_Logger.warning( "Transmission lamp failure. Measurement failed" );
            status.m_Errors.add( "MSG_TRANSMISSION_FAILED" );
        }
        else if( sc.equals( INVALID_COMMAND_ERROR ) )
        {
            m_Logger.warning( "Invalid command for current configuration" );
            status.m_Errors.add( "MSG_INVALID_COMMAND" );
        }
        else if( sc.equals( TOLERANCES_ERROR ) )
        {
            m_Logger.warning( "The maximum number of tolerances has been exceeded" );
            status.m_Errors.add( "MSG_MAXIMUM_TOLERANCES" );
        }
        else if( sc.equals( STANDARD_NAME_ERROR ) )
        {
            m_Logger.warning( "Too many characters in standard name" );
            status.m_Errors.add( "MSG_TOO_MANY_CHARACTERS" );
        }
        else if( sc.equals( SAVING_STANDARD_ERROR ) )
        {
            m_Logger.warning( "No memory available to store standard" );
            status.m_Errors.add( "MSG_NO_MEMORY_STANDARD" );
        }
        else if( sc.equals( SAVING_PROJECT_ERROR ) )
        {
            m_Logger.warning( "No memory available to store project" );
            status.m_Errors.add( "MSG_NO_MEMORY_PROJECT" );
        }
        else if( sc.equals( SAVING_TEXT_RECORD_ERROR ) )
        {
            m_Logger.warning( "No memory available to store text record" );
            status.m_Errors.add( "MSG_NO_MEMORY_TEXT" );
        }
        else if( sc.equals( SHADE_SORT_ERROR ) )
        {
            m_Logger.warning( "Charge level of batteries low" );
            status.m_Errors.add( "MSG_CHARGE_LOW" );
        }
        else if( sc.equals( INVALID_RECORD ) )
        {
            m_Logger.warning( "The database record does not exist" );
            status.m_Errors.add( "MSG_INVALID_RECORD" );
        }
        else if( sc.equals( BATTERY_OPEN_ERROR ) )
        {
            m_Logger.warning( "Battery reached its thermal cutoff temperature" );
            status.m_Errors.add( "MSG_BATTERY_TEMPERATURE" );
        }
        /*
      else
      {
          m_Logger.warning( "Possibly multiple errors" );
          status.m_Errors.add( "ERRORS" );
      }
      */

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
