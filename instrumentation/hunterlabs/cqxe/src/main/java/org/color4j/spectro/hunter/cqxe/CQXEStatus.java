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

package org.color4j.spectro.hunter.cqxe;

import java.util.Collection;
import java.util.Vector;
import org.color4j.spectro.hunter.common.Binary;
import org.color4j.spectro.hunter.common.Hex;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * Meta-information of an operation with the spectrophotometer.
 * <p>The SpectroStatus carries information of the SpectroReading, especially
 * about the result of a measurement, success or failure. If the the
 * <code>isSuccess</code> method, the <code>getErrors</code> should return
 * an empty Collection.</p>
 *
 */
public class CQXEStatus
    implements SpectroStatus
{
    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static CQXEStatus create( String response )
    {
        Vector msg = new Vector();
        Vector warningMsg = new Vector();
        Vector errorMsg = new Vector();

        switch( response.charAt( 0 ) )
        {
        case 'H':
            checkMeasureStatus( response, msg, warningMsg, errorMsg );
            break;
        case 'I':
            checkCalibrationStatus( response, msg, warningMsg, errorMsg );
            break;
        case 'F':
            checkStandardizationStatus( response, msg, warningMsg, errorMsg );
            break;
        case 'Z':
            checkErrorStatus( response, msg, warningMsg, errorMsg );
            break;
        default:
            errorMsg.add( "UNRECOGNIZED_RESPONSE" );
        }

        return new CQXEStatus( msg, warningMsg, errorMsg );
    }

    public CQXEStatus()
    {
        m_ErrorMessages = new Vector();
        m_WarningMessages = new Vector();
        m_Messages = new Vector();
    }

    CQXEStatus( Collection messages, Collection warnings, Collection errors )
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

    public static void checkMeasureStatus( String response,
                                           final Collection msg,
                                           final Collection warningMsg,
                                           final Collection errorMsg
    )
    {
        if( '0' == response.charAt( 1 ) )
        {
            errorMsg.add( "MSG_MEASUREMENT_ERROR" );
            return;
        }
    }

    public static void checkCalibrationStatus( String response,
                                               final Collection msg,
                                               final Collection warningMsg,
                                               final Collection errorMsg
    )
    {
        if( '0' == response.charAt( 1 ) )
        {
            errorMsg.add( "MSG_CALIBRATION_ERROR" );
        }
    }

    public static void checkStandardizationStatus( String response,
                                                   final Collection msg,
                                                   final Collection warningMsg,
                                                   final Collection errorMsg
    )
    {
        if( response.length() < 36 )
        {
            warningMsg.add( "MSG_INVALID_STATUS_LENGTH" );
        }
    }

    public static void checkErrorStatus( String response,
                                         final Collection msg,
                                         final Collection warningMsg,
                                         final Collection errorMsg
    )
    {
        Binary mirrorError = new Binary( Hex.hexStringToInt( response.substring( 1, 5 ) ) );
        Binary UVFilterError = new Binary( Hex.hexStringToInt( response.substring( 5, 9 ) ) );
        Binary apertureError = new Binary( Hex.hexStringToInt( response.substring( 9, 13 ) ) );
        Binary zoomLensError = new Binary( Hex.hexStringToInt( response.substring( 13, 17 ) ) );
        Binary tileError = new Binary( Hex.hexStringToInt( response.substring( 17, 20 ) ) );
        Binary measurementError = new Binary( Hex.hexStringToInt( response.substring( 20, 24 ) ) );
        Binary hostMessageError = new Binary( Hex.hexStringToInt( response.substring( 24, 28 ) ) );
        Binary adsp2181Error = new Binary( Hex.hexStringToInt( response.substring( 28 ) ) );

        processMirrorError( mirrorError, errorMsg );
        processUVFilterError( UVFilterError, errorMsg );
        processApertureError( apertureError, errorMsg );
        processZoomLensError( zoomLensError, errorMsg );
        processTileError( tileError, errorMsg );
        processMeasurementError( measurementError, errorMsg );
        processHostMessageError( hostMessageError, errorMsg );
        processADSP2181Error( adsp2181Error, errorMsg );
    }

    public static void processMirrorError( Binary mirrorError, final Collection errorMsg )
    {
        //Check mirrorError
        if( mirrorError.getBit( 0 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_RETRACTED_POSITION_SENSOR_FAIL" );
        }

        if( mirrorError.getBit( 1 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_INSERTED_POSITION_SENSOR_FAIL" );
        }

        if( mirrorError.getBit( 2 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_MECHANISM_DRAG_STICK" );
        }

        if( mirrorError.getBit( 10 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_UNABLE_TO_LOCATE_RETRACTED_NOTCH" );
        }

        if( mirrorError.getBit( 11 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_INSERTED_NOTCH_MEASURE_FAIL" );
        }

        if( mirrorError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_UNABLE_TO_LOCATE_INSERTED_NOTCH" );
        }

        if( mirrorError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_RETRACTED_NOTCH_MEASURE_FAIL" );
        }

        if( mirrorError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_UNABLE_TO_LOCATE_REFERENCE_POINT" );
        }

        if( mirrorError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_MIRROR_UNABLE_TO_LOCATE_REFERENCE_POINT2" );
        }
    }

    public static void processUVFilterError( Binary filterError, final Collection errorMsg )
    {
        if( filterError.getBit( 0 ) > 0 )
        {
            errorMsg.add( "MSG_UV_REFERENCE_POSTITION_SENSOR_FAIL" );
        }

        if( filterError.getBit( 2 ) > 0 )
        {
            errorMsg.add( "MSG_UV_MECHANISM_DRAG_STICK" );
        }

        if( filterError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_UV_UNABLE_TO_LOCATE_POSITION_SENSOR" );
        }

        if( filterError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_UV_UNABLE_TO_MEASURE_HYSTERESIS" );
        }

        if( filterError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_UV_UNABLE_TO_MEASURE_HYSTERESIS2" );
        }

        if( filterError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_UV_UNABLE_TO_LOCATE_POSITION_SENSOR" );
        }
    }

    public static void processApertureError( Binary apertureError, final Collection errorMsg )
    {
        if( apertureError.getBit( 0 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_RETRACTED_POSITION_SENSOR_FAIL" );
        }

        if( apertureError.getBit( 1 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_INSERTED_POSITION_SENSOR_FAIL" );
        }

        if( apertureError.getBit( 2 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_MECHANISM_DRAG_STICK" );
        }

        if( apertureError.getBit( 10 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_UNABLE_TO_LOCATE_RETRACTED_NOTCH" );
        }

        if( apertureError.getBit( 11 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_INSERTED_NOTCH_MEASURE_FAIL" );
        }

        if( apertureError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_UNABLE_TO_LOCATE_INSERTED_NOTCH" );
        }

        if( apertureError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_RETRACTED_NOTCH_MEASURE_FAIL" );
        }

        if( apertureError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_UNABLE_TO_LOCATE_REFERENCE_POINT" );
        }

        if( apertureError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_APERTURE_UNABLE_TO_LOCATE_REFERENCE_POINT2" );
        }
    }

    public static void processZoomLensError( Binary zoomLensError, final Collection errorMsg )
    {
        if( zoomLensError.getBit( 0 ) > 0 )
        {
            errorMsg.add( "MSG_LAV_POSITION_SENSOR_FAIL" );
        }

        if( zoomLensError.getBit( 1 ) > 0 )
        {
            errorMsg.add( "MSG_SAV_POSITION_SENSOR_FAIL" );
        }

        if( zoomLensError.getBit( 2 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_MECHANISM_DRAG_STICK" );
        }

        if( zoomLensError.getBit( 6 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_LAV_PHYSICAL_LIMIT_MEASURE_FAIL" );
        }

        if( zoomLensError.getBit( 7 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_BAD_LAV_LENS_ASSEMBLY" );
        }

        if( zoomLensError.getBit( 8 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_MEASURE_LAV_HYSTERESIS" );
        }

        if( zoomLensError.getBit( 9 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_MEASURE_LAV_SWITCH_DISTANCE" );
        }

        if( zoomLensError.getBit( 9 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_MEASURE_SWITCH_DISTANCE" );
        }

        if( zoomLensError.getBit( 10 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_SAV_PHYSICAL_LIMIT_MEASURE_FAIL" );
        }

        if( zoomLensError.getBit( 11 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_BAD_SAV_LENS_ASSEMBLY" );
        }

        if( zoomLensError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_MEASURE_SAV_HYSTERESIS" );
        }

        if( zoomLensError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_MEASURE_SAV_SWITCH_DISTANCE" );
        }

        if( zoomLensError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_LOCATE_REFERENCE_POINT" );
        }

        if( zoomLensError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_LENS_UNABLE_TO_LOCATE_REFERENCE_POINT2" );
        }
    }

    public static void processTileError( Binary tileError, final Collection errorMsg )
    {
        if( tileError.getBit( 0 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_RETRACTED_POSITION_SENSOR_FAIL" );
        }

        if( tileError.getBit( 1 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_INSERTED_POSITION_SENSOR_FAIL" );
        }

        if( tileError.getBit( 2 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_MECHANISM_DRAG_STICK" );
        }

        if( tileError.getBit( 9 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_MEASURE_RETRACTED_NOTCH_CLOSE" );
        }

        if( tileError.getBit( 10 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_MEASURE_RETRACTED_NOTCH_OPEN" );
        }

        if( tileError.getBit( 11 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_MEASURE_RETRACTED_NOTCH_OPEN" );
        }

        if( tileError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_MEASURE_PHYSICAL_LIMIT_OFFSET" );
        }

        if( tileError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_LOCATE_INSERTED_NOTCH" );
        }

        if( tileError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_LOCATE_REFERENCE_POINT_CLOSE" );
        }

        if( tileError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_TILE_UNABLE_TO_LOCATE_REFRENCE_POINT_OPEN" );
        }
    }

    public static void processMeasurementError( Binary measurementError, final Collection errorMsg )
    {
        if( measurementError.getBit( 0 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_LOW_MONITOR_SIGNAL" );
        }

        if( measurementError.getBit( 1 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_SATURATED_SAMPLE_AND_OR_MONITOR" );
        }

        if( measurementError.getBit( 2 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_HI_SIGNAL_LEVEL_FOR_BLACK_GLASS" );
        }

        if( measurementError.getBit( 3 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_LO_SIGNAL_LEVEL_FOR_WHITE_TILE" );
        }

        if( measurementError.getBit( 8 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_BLACK_GLASS_NOT_READ" );
        }

        if( measurementError.getBit( 9 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_PRIMARY_WHITE_TILE_NOT_READ" );
        }

        if( measurementError.getBit( 10 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_STANDARDIZATION_INCOMPLETE" );
        }

        if( measurementError.getBit( 11 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_PORT_PLATE_MISMATCH" );
        }

        if( measurementError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_MEASUREMENT_DSP_CONFIG_ERROR" );
        }
    }

    public static void processHostMessageError( Binary hostError, final Collection errorMsg )
    {
        if( hostError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_HOST_UNEXPECTED_END_OF_MESSAGE" );
        }

        if( hostError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_HOST_BAD_CHECKSUM" );
        }

        if( hostError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_HOST_BAD_MESSGE_FORMAT" );
        }

        if( hostError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_HOST_BAD_MESSAGE_NUMBER" );
        }
    }

    public static void processADSP2181Error( Binary adspError, final Collection errorMsg )
    {
        if( adspError.getBit( 12 ) > 0 )
        {
            errorMsg.add( "MSG_ADSP_UNEXPECTED_END_OF_MESSAGE" );
        }

        if( adspError.getBit( 13 ) > 0 )
        {
            errorMsg.add( "MSG_ADSP_BAD_CHECKSUM" );
        }

        if( adspError.getBit( 14 ) > 0 )
        {
            errorMsg.add( "MSG_ADSP_BAD_MESSAGE_FORMAT" );
        }

        if( adspError.getBit( 15 ) > 0 )
        {
            errorMsg.add( "MSG_ADSP_DSP_FAILED_TO_RESPOND" );
        }
    }
}
