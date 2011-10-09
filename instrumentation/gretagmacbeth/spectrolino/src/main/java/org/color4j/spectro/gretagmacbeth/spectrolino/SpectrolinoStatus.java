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

package org.color4j.spectro.gretagmacbeth.spectrolino;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * Meta-information of an operation with the spectrophotometer.
 * <p>The SpectroStatus carries information of the SpectroReading, especially
 * about the result of a measurement, success or failure. If the the
 * <code>isSuccess</code> method, the <code>getErrors</code> should return
 * an empty Collection.</p>
 *
 */
public class SpectrolinoStatus implements SpectroStatus
{
    static private Logger m_Logger = Logger.getLogger( SpectrolinoStatus.class.getName() );

    Collection m_ErrorMessages;
    Collection m_WarningMessages;
    Collection m_Messages;

    public static SpectrolinoStatus create( String response )
    {
        //boolean m_unknown = false;

        Vector errorMsg = new Vector();
        Vector warningMsg = new Vector();
        Vector msg = new Vector();

        if( response.equals( "SUCCESS" ) )
        {
            return new SpectrolinoStatus( msg, warningMsg, errorMsg );
        }
        else if( response.equals( "ERROR_OPENING" ) )
        {
            errorMsg.add( "PORT CONNECTION FAILED" );
            //msg.add( "ERROR_OPENING" );
        }
        else if( response.equals( "TIMEOUT_ERROR" ) )
        {
            errorMsg.add( "TIMEOUT OCCURRED" );
        }
        else if( response.equals( "NOT_VALID_SPECTROLINO" ) )
        {
            errorMsg.add( "MSG_NOTVALID_SPECTROLINO" );
        }
        //else if ( response.equals( "UNKNOWN_RESPONSE" ) )
        //{
        //    errorMsg.add( "Unrecognized status" );
        //}
        else
        {
            // Remove the <CR><LF>
            String rawresponse = response.replaceFirst( "\r\n", "" );
            String result = new String();
            String tempresult = new String();

            try
            {
                // Check for RemoteErrorSet
                if( Pattern.matches( ": 31 .*", rawresponse ) )
                {
                    int no = Integer.parseInt( rawresponse.replaceFirst( ": 31 ", "" ) );
                    result = Integer.toBinaryString( no );

                    // Adjust the binary string
                    for( int i = 0; i < 16 - result.length(); i++ )
                    {
                        tempresult = tempresult + "0";
                    }
                    result = tempresult + result;

                    m_Logger.info( "Result of RemoteErrorSet : " + result );

                    if( result.charAt( 0 ) == '1' )
                    {
                        errorMsg.add( "MSG_SLOPE" );
                    }
                    if( result.charAt( 1 ) == '1' )
                    {
                        errorMsg.add( "MSG_DORL" );
                    }
                    if( result.charAt( 2 ) == '1' )
                    {
                        errorMsg.add( "MSG_REFLECTANCE" );
                    }
                    if( result.charAt( 3 ) == '1' )
                    {
                        errorMsg.add( "MSG_COLOR1" );
                    }
                    if( result.charAt( 4 ) == '1' )
                    {
                        errorMsg.add( "MSG_COLOR2" );
                    }
                    if( result.charAt( 5 ) == '1' )
                    {
                        errorMsg.add( "MSG_COLOR3" );
                    }
                    if( result.charAt( 6 ) == '1' )
                    {
                        errorMsg.add( "MSG_NSROR" );
                    }
                    if( result.charAt( 7 ) == '1' )
                    {
                        errorMsg.add( "MSG_VAL_REF" );
                    }
                    if( result.charAt( 8 ) == '1' )
                    {
                        errorMsg.add( "MSG_DSTD" );
                    }
                    if( result.charAt( 9 ) == '1' )
                    {
                        errorMsg.add( "MSG_WHITE" );
                    }
                    if( result.charAt( 10 ) == '1' )
                    {
                        errorMsg.add( "MSG_ILLUM" );
                    }
                    if( result.charAt( 11 ) == '1' )
                    {
                        errorMsg.add( "MSG_OBSERVER" );
                    }
                    if( result.charAt( 12 ) == '1' )
                    {
                        errorMsg.add( "MSG_LAMBDA" );
                    }
                    if( result.charAt( 13 ) == '1' )
                    {
                        errorMsg.add( "MSG_SPECT" );
                    }
                    if( result.charAt( 14 ) == '1' )
                    {
                        errorMsg.add( "MSG_COL_INDEX" );
                    }
                    if( result.charAt( 15 ) == '1' )
                    {
                        errorMsg.add( "MSG_CHAR" );
                    }

                    if( no > 32768 )
                    {
                        errorMsg.add( "MSG_UNKNOWN_STRING" );
                    }

                    // reset the filler
                    tempresult = "";
                }
                // Now Check for Error type during measurment
                else if( Pattern.matches( ": 37 .*", rawresponse ) )
                {
                    result = rawresponse.replaceFirst( ": 37 ", "" );

                    m_Logger.info( "Result of ErrorType : " + result );

                    if( result.equals( "1" ) )
                    {
                        errorMsg.add( "MSG_MEMORY" );
                    }
                    else if( result.equals( "2" ) )
                    {
                        errorMsg.add( "MSG_POWER" );
                    }
                    else if( result.equals( "4" ) )
                    {
                        errorMsg.add( "MSG_LAMP" );
                    }
                    else if( result.equals( "5" ) )
                    {
                        errorMsg.add( "MSG_HARDWARE" );
                    }
                    else if( result.equals( "6" ) )
                    {
                        errorMsg.add( "MSG_FILTER" );
                    }
                    else if( result.equals( "7" ) )
                    {
                        errorMsg.add( "MSG_DATA" );
                    }
                    else if( result.equals( "8" ) )
                    {
                        errorMsg.add( "MSG_DRIVE" );
                    }
                    else if( result.equals( "9" ) )
                    {
                        errorMsg.add( "MSG_MEASURE_ERROR" );
                    }
                    else if( result.equals( "10" ) )
                    {
                        errorMsg.add( "MSG_DENSITOMETIC" );
                    }
                    else if( result.equals( "13" ) )
                    {
                        errorMsg.add( "MSG_EPROM" );
                    }
                    else if( result.equals( "14" ) )
                    {
                        errorMsg.add( "MSG_WHITE_CALIBRATE_ERROR" );
                    }
                    else if( result.equals( "16" ) )
                    {
                        errorMsg.add( "MSG_CHECKSUM_MEMORY" );
                    }
                    else if( result.equals( "17" ) )
                    {
                        errorMsg.add( "MSG_MEMORY_FULL" );
                    }
                    else if( result.equals( "19" ) )
                    {
                        m_Logger.info( "White measurement OK" );
                    }
                    else if( result.equals( "21" ) )
                    {
                        errorMsg.add( "MSG_NOT_READY" );
                    }
                    else if( result.equals( "50" ) )
                    {
                        m_Logger.info( "White measurement warning" );
                    }
                    else if( result.equals( "51" ) )
                    {
                        m_Logger.info( "Reset done" );
                    }
                    else if( result.equals( "52" ) )
                    {
                        m_Logger.info( "Emission Calibration OK" );
                    }
                    else if( result.equals( "53" ) )
                    {
                        m_Logger.info( "Only for emission (not reflectance)" );
                    }
                    else if( result.equals( "54" ) )
                    {
                        errorMsg.add( "MSG_CHECKSUM_ERROR" );
                    }
                    else if( result.equals( "55" ) )
                    {
                        errorMsg.add( "MSG_NO_MEASURE" );
                    }
                    else if( result.equals( "56" ) )
                    {
                        errorMsg.add( "MSG_ERROR_BACKUP" );
                    }
                    else if( result.equals( "57" ) )
                    {
                        errorMsg.add( "MSG_ERROR_ROM" );
                    }

                    try
                    {
                        int no = Integer.parseInt( result );

                        if( no > 57 )
                        {
                            errorMsg.add( "MSG_UNKNOWN_STRING" );
                        }
                    }
                    catch( NumberFormatException noEx )
                    {
                        errorMsg.add( "MSG_UNKNOWN_STRING" );
                    }
                }
                else if( Pattern.matches( " 255 .*", rawresponse ) )
                {
                    int no = Integer.parseInt( rawresponse.replaceFirst( " 255 ", "" ) );
                    result = Integer.toBinaryString( no );

                    // Adjust the binary string
                    for( int i = 0; i < 16 - result.length(); i++ )
                    {
                        tempresult = tempresult + "0";
                    }
                    result = tempresult + result;

                    m_Logger.info( "Result of RemoteErrorSet : " + result );

                    if( result.charAt( 0 ) == '1' )
                    {
                        errorMsg.add( "MSG_SLOPE" );
                    }
                    if( result.charAt( 1 ) == '1' )
                    {
                        errorMsg.add( "MSG_DORL" );
                    }
                    if( result.charAt( 2 ) == '1' )
                    {
                        errorMsg.add( "MSG_REFLECTANCE" );
                    }
                    if( result.charAt( 3 ) == '1' )
                    {
                        errorMsg.add( "MSG_COLOR1" );
                    }
                    if( result.charAt( 4 ) == '1' )
                    {
                        errorMsg.add( "MSG_COLOR2" );
                    }
                    if( result.charAt( 5 ) == '1' )
                    {
                        errorMsg.add( "MSG_COLOR3" );
                    }
                    if( result.charAt( 6 ) == '1' )
                    {
                        errorMsg.add( "MSG_NSROR" );
                    }
                    if( result.charAt( 7 ) == '1' )
                    {
                        errorMsg.add( "MSG_VAL_REF" );
                    }
                    if( result.charAt( 8 ) == '1' )
                    {
                        errorMsg.add( "MSG_DSTD" );
                    }
                    if( result.charAt( 9 ) == '1' )
                    {
                        errorMsg.add( "MSG_WHITE" );
                    }
                    if( result.charAt( 10 ) == '1' )
                    {
                        errorMsg.add( "MSG_ILLUM" );
                    }
                    if( result.charAt( 11 ) == '1' )
                    {
                        errorMsg.add( "MSG_OBSERVER" );
                    }
                    if( result.charAt( 12 ) == '1' )
                    {
                        errorMsg.add( "MSG_LAMBDA" );
                    }
                    if( result.charAt( 13 ) == '1' )
                    {
                        errorMsg.add( "MSG_SPECT" );
                    }
                    if( result.charAt( 14 ) == '1' )
                    {
                        errorMsg.add( "MSG_COL_INDEX" );
                    }
                    if( result.charAt( 15 ) == '1' )
                    {
                        errorMsg.add( "MSG_CHAR" );
                    }

                    if( no > 32768 )
                    {
                        errorMsg.add( "MSG_UNKNOWN_STRING" );
                    }

                    // reset the filler
                    tempresult = "";
                }
                else
                {
                    errorMsg.add( "MSG_UNKNOWN_STRING" );
                }

                //if ( m_unknown )
                //errorMsg.add( "Unrecognized status" );

                return new SpectrolinoStatus( msg, warningMsg, errorMsg );
            }
            catch( IndexOutOfBoundsException arrayEx )
            {
                errorMsg.add( "MSG_UNKNOWN_STRING" );
            }
            catch( NumberFormatException noEx )
            {
                errorMsg.add( "MSG_UNKNOWN_STRING" );
            }
        }

        return new SpectrolinoStatus( msg, warningMsg, errorMsg );
    }

    public SpectrolinoStatus()
    {
        m_ErrorMessages = new Vector();
        m_WarningMessages = new Vector();
        m_Messages = new Vector();
    }

    SpectrolinoStatus( Collection messages, Collection warnings, Collection errors )
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
