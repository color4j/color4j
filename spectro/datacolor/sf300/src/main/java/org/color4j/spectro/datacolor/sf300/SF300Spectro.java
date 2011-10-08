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

package org.color4j.spectro.datacolor.sf300;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.datacolor.common.BlackCalibrationCommand;
import org.color4j.spectro.datacolor.common.CutOff400LightFilter;
import org.color4j.spectro.datacolor.common.CutOff420LightFilter;
import org.color4j.spectro.datacolor.common.CutOff460LightFilter;
import org.color4j.spectro.datacolor.common.DCIReading;
import org.color4j.spectro.datacolor.common.ExtraLargeAperture;
import org.color4j.spectro.datacolor.common.ExtraLargeAreaView;
import org.color4j.spectro.datacolor.common.LargeAperture;
import org.color4j.spectro.datacolor.common.LargeAreaView;
import org.color4j.spectro.datacolor.common.MeasureCommand;
import org.color4j.spectro.datacolor.common.MediumAperture;
import org.color4j.spectro.datacolor.common.MediumAreaView;
import org.color4j.spectro.datacolor.common.ResponseDecoder;
import org.color4j.spectro.datacolor.common.SetSpecularCommand;
import org.color4j.spectro.datacolor.common.SmallAperture;
import org.color4j.spectro.datacolor.common.SmallAreaView;
import org.color4j.spectro.datacolor.common.UVIncludedLightFilter;
import org.color4j.spectro.datacolor.common.UltraSmallAperture;
import org.color4j.spectro.datacolor.common.UltraSmallAreaView;
import org.color4j.spectro.datacolor.common.WhiteEndCommand;
import org.color4j.spectro.spi.NotSupportedException;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.SpectroListener;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;
import org.color4j.spectro.spi.Spectrophotometer;
import org.color4j.spectro.spi.helpers.CommDriver;
import org.color4j.spectro.spi.helpers.CommDriverEvent;
import org.color4j.spectro.spi.helpers.CommDriverException;
import org.color4j.spectro.spi.helpers.CommDriverListener;
import org.color4j.spectro.spi.helpers.Fifo;
import org.color4j.spectro.spi.helpers.FifoFullException;
import org.color4j.spectro.spi.helpers.GenericCommDriver;

/**
 * Spectrophotometer object that supports the functionality of manually
 * entering reflectance data.
 *
 * The SF300 Spectrophotometer:
 *
 * From the operational point of view, the SF300 runs on the the principal of
 * queueing outgoing commands and expecting incoming response for each command
 * sent to be interpreted and and passed up the chain via events.
 *
 * To this effect, the SF300Spectro has two First In First Out (FIFO) queues,
 * one for outgoing commands and one for incoming commands. There is a single
 * thread that runs continuously while the SF300Spectro is active. The thread
 * is responsible for taking a queued command from the outgoing FIFO and
 * sending it to the instrument via the CommDriver. Once, the command is sent,
 * the command is placed into the incoming FIFO queue. However if the incoming
 * FIFO is full, the thread will not send anymore commands until the incoming
 * FIFO becomes available for more elements. If the Outgoing FIFO becomes full
 * due to this blocking mechanism, the SF300Spectro will throw exceptions
 * indicating that it is busy until the Outgoing FIFO is available for more
 * elements.
 *
 * For the purpose of keeping traffic to the instrument low, the Incoming FIFO
 * is kept low to a size of 1. And the Outgoing FIFO is set at a size of 5 to
 * facilitate multiple set settings commands to be placed in queue.
 *
 * To facilitate for a timeout; each command sent to the CommDriver notifies
 * the CommDriver to keep track of responses by setting a timer for a specified
 * time frame. At the end of each time frame, it is assumed that the instrument
 * has not responded and a time out event will be triggered. At this point, the
 * command in the Incoming FIFO is assumed to have timed out and is pre-empted
 * from the FIFO.
 *
 * The other functions of the SF300Spectro are all event driven. Procedures are
 * activated via events sent from the CommDriver. Reception of data from the
 * CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the SF300Spectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to
 * listeners. Otherwise, a null is returned and the SF300Spectro would guess at
 * interpreting the response. Currently, the response is interpreted as two
 * response; a Measure response or Calibration response and a settings
 * response. The distinction used to differentiate the two is the length of the
 * response. The longer is assumed to be a Calibration or Measure response
 * while the shorter is assume to be a Settings response.
 *
 * An average of the data values is used to identifiy a calibration from a
 * measurement. Currently, an average above 80% reflectance is assumed to be a
 * calibration and anything lower is assumed to be a measurement. While this
 * may work for most cases, the pitfall is that measurments of white or close
 * to white materials may be considerred to be a calibration.
 *
 * From a layer view of the spectro, there are three or four distinct layers.
 * The first layer is similar to the Data Layer handled by the Comm Driver;
 * which is the transfer of the raw bytes to and from the serial port. The
 * second layer is handled by the spectro where the ACK and NAK characters,
 * checksum and Terminator is checked and removed similar to the removal of
 * headers in data packets. Th third layer resides in the individual command'
 * classes that actually interprets the data content of the "messages". The
 * user interface in the Netbeans IDE could be considered as the fourth layer
 * that interacts with the user.
 */
public class SF300Spectro
    implements Spectrophotometer, CommDriverListener, Runnable, ResponseDecoder
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( SF300Spectro.class.getName() );
    }

    /**
     * Constructs and initialize the spectrophotometer.
     */
    public static final String TERMINATOR;

    public static final char STARTUP_CHAR = (char) -4;

    static
    {
        TERMINATOR = ":\r\n"; //Termniator : ":<cr><lf>"
    }

    protected CommDriver m_CommDriver;

    protected Fifo m_Incoming; // Expected incoming responses

    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the

    // instrument

    protected SpectroSettings m_newSettings; // The new settings for the

    // instrument

    protected SpectroStatus m_LastStatus; // The last received status

    protected SpectroReading m_WhiteTileSPI; // Calibration white tile

    protected SpectroReading m_WhiteTileSPE; // Calibration white tile

    protected SpectroReading m_WhiteTileDataSPI; // White Tile Data loaded from

    // Data file

    protected SpectroReading m_WhiteTileDataSPE; // White Tile Data Loaded from

    // Data file

    protected TreeMap m_ScaleFactorSPI; //Scale factors for Spec Included

    protected TreeMap m_ScaleFactorSPE; //Scale factors for Spec Excluded

    protected String m_SerialNo; //Serial number of the instrument, none for

    // the SF300

    protected Vector m_Listeners; //Collection of Spectrolisteners listening

    // to this

    protected int m_OpStatus; //The operational status of the spectro

    // implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean sync; //Indicating waiting for a sync response

    protected boolean ack; // Indicating waiting for an ACK response

    protected boolean running; // Switch for the running thread

    protected boolean stopRequest = false;

    protected Thread m_Thread;

    /**
     * Instantiates and initializes the spectrophotometer. The constructor does
     * not open the CommDriver until the first setSettings containing the
     * CommParamaters are passed in.
     */
    public SF300Spectro()
    {

        sync = false;
        ack = false;

        m_Listeners = new Vector();
        m_Received = null;

        m_SerialNo = "";
        m_OpStatus = OPERATIONAL_STATUS_IDLE;

        initialize();
        notifyStatusChange( new SpectroEvent( this ) );
    }

    //=================== Public methods ==================================

    /**
     * Initiates a measure command. The method places a measure command in the
     * outgoing FIFO.
     *
     * @throws SpectroException when the incoming queue is Blocked and no further commands
     *                          can be sent. The spectro is considerred as busy.
     */
    public void measure()
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new MeasureCommand( 1, this ) );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "MSG_SPECTRO_BUSY" );
        }
    }

    /**
     * Initiates a calibration command. The method places a calibration command
     * in the outgoing FIFO.
     *
     * @param step - indicates the calibration procedure step
     *
     * @throws SpectroException when the incoming queue is Blocked and no further commands
     *                          can be sent. The spectro is considerred as busy. Also thrown
     *                          when an unrecognized calibration procedure step is passed
     *                          in.
     */
    public void calibrate( int step )
        throws SpectroException
    {
        try
        {
            switch( step )
            {

            case 0:
                //Black Calibration
                m_Outgoing.putObject( new BlackCalibrationCommand( 1, this ) );
                break;

            case 1:
                //White Calibration
                m_Outgoing.putObject( new WhiteEndCommand( 1, this ) );
                break;

            default:
                /*
                * Either the driver is instantiating the wrong
                * spectrophotometer implmentation or the driver has an
                * erroneous calibration procedure list.
                */
                throw new SpectroException( "MSG_UNKNOWN_CALIBRATION" );
            }

            m_OpStatus = OPERATIONAL_STATUS_SENDING;

            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "MSG_SPECTRO_BUSY" );
        }
    }

    public void retrieveStoredSamples()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    /*
     * Not supported by the SF300 Spectros
     * 
     * @throws NotSupportectExcetpion This is not supported by the SF300
     * Spectros
     */
    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    /*
     * Not supported by the SF300 Spectros
     * 
     * @throws NotSupportectExcetpion This is not supported by the SF300
     * Spectros
     */
    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    public void retrieveStandards()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*
     * Not supported by the SF300 Spectros
     * 
     * @throws NotSupportectExcetpion This is not supported by the SF300
     * Spectros
     */
    public void retrieveStandard( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*
     * Not supported by the SF300 Spectros
     * 
     * @throws NotSupportectExcetpion This is not supported by the SF300
     * Spectros
     */
    public void queryNoOfStoredSamples()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*
     * Not supported by the SF300 Spectros
     * 
     * @throws NotSupportectExcetpion This is not supported by the SF300
     * Spectros
     */
    public void queryNoOfStoredStandards()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*
     * Returns the current Spectro settings of the instrument
     * 
     * @return SpectroSetting the current settings of the instrument.
     */
    public SpectroSettings getSettings()
    {
        return m_Settings;
    }

    /**
     * Set the instrument to a new set of specified settings. When called for
     * the first time with the Comm Parameters included, the Serial Port
     * connection is also opened with this method. The set settings method
     * attempts to synchronize the current settings with the new settings. A
     * set command is sent for each parameter that differs from the current
     * settings to avoid unneccesary comm traffic. Each call overwrites
     * parameters that differ from the previous settings, and set commands are
     * sent for only those parameters.
     *
     * @param newSettings The new settings for the instrument.
     */
    public void setSettings( SpectroSettings newSettings )
    {
        //System.out.println( "Set settings called" );
        synchronized( this )
        {

            if( newSettings == null )
            {
                return;
            }

            //If comm driver is not initialized
            //Assume this is the first initial settings
            //And opens the connection with the new Comm Parameters
            if( m_CommDriver == null )
            {
                Map commParameters = newSettings.getCommParameters();

                m_CommDriver = new GenericCommDriver();

                try
                {
                    String portname = (String) commParameters.get( "PORTNAME" );
                    String bitrate = (String) commParameters.get( "BITRATE" );

                    m_Logger.finer( "Port name : " + portname );
                    m_Logger.finer( "Bit rate : " + bitrate );

                    m_CommDriver.openConnection( portname, 3000, 19200 );

                    m_CommDriver.addCommDriverListener( this );

                    //Send a sync command:
                    sync = true;
                    m_CommDriver.send( new String( ":\r\n" ).getBytes() );

                    //Wake instrument from unknown state:
                    // - Previous measure command fail to ACK or NAK the
                    // measurement.
                    ack = true;
                    m_CommDriver.send( new String( "*" ).getBytes() );

                    running = true;

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );
                    return;
                }
                catch( NumberFormatException numEx )
                {
                    //Try to recoved from exception and use a preset default
                    //bitrate
                    String portname = (String) commParameters.get( "PORTNAME" );

                    try
                    {
                        m_CommDriver.openConnection( portname, 3000, 19200 );

                        m_CommDriver.addCommDriverListener( this );
                    }
                    catch( CommDriverException commEx )
                    {
                        SpectroStatus status = SF300Status.create( "EEEEEEEEEEEEEEEEEEEE" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );
                        return;
                    }

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    SpectroStatus status = SF300Status.create( "EEEEEEEEEEEEEEEEEEEE" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    return;
                }
                return;
            }

            //Attempt to synchronise and store new settings
            if( m_newSettings == null )
            {
                m_newSettings = newSettings;
            }

            //Compare specular setting:
            if( m_Settings == null || m_Settings.getSpecular() != newSettings.getSpecular() )
            {
                m_newSettings.setSpecular( newSettings.getSpecular() );

                try
                {
                    SetSpecularCommand command = new SetSpecularCommand( m_newSettings.getSpecular(), this );
                    m_Outgoing.putObject( command );
                }
                catch( FifoFullException fullEx )
                {
                    m_Logger.finer( "SF300 Spectro : Wait for fifo to be cleared." );
                }
            }

            m_Logger.finer( "SF300 Spectro : Set setings done" );
        }
    }

    /**
     * Returns the Serial number of the instrument. Not supported by SF300
     * Spectros The method currently returns a null string.
     *
     * @return String null String since the instrument does not provide a
     *         serial number
     */
    public String getSerialNo()
    {
        return m_SerialNo;
    }

    /**
     * Initializes the spectrophotomer; but does not open connection to the
     * serial port The initialization process covers the instantiation of the
     * FIFO queues but does not open the connection and start the send thread.
     */
    public void initialize()
    {
        m_CommDriver = null;

        m_Outgoing = new Fifo( 5 );
        m_Incoming = new Fifo( 1 );

        m_Thread = new Thread( this );

        //Do nothing
        m_OpStatus = OPERATIONAL_STATUS_INITIALIZING;
        notifyStatusChange( new SpectroEvent( this ) );
    }

    /**
     * Dispose of resources held by this Spectrophotometer implementation Upon
     * invocation, closes connection held to the serial port. Dereference
     * pointers to objects and call garbage collection
     */
    public void dispose()
    {
        m_Logger.finer( "SF300 Spectro : Dispose called" );

        stopThread();

        while( running )
        {
            // wait untill it fully stop
        }

        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;

        m_Incoming = null; //Dereference Incoming FIFO
        m_Outgoing = null; //Derefernce Outgoing FIFO

        m_LastStatus = null; //Dereference SpectroStatus
        m_Settings = null; //Dereference SpectroSettings
        m_SerialNo = null; //Derference String

        // ** WARNING : Thread is stull running ***
        if( running )
        {
            m_Logger.warning( "SF300 Spectro : Thread still running" );
        }

        if( m_CommDriver != null )
        {
            try
            {
                m_CommDriver.cancelRespondTimeout();
                m_CommDriver.removeCommDriverListener( this );

                m_CommDriver.closeConnection();
            }
            catch( CommDriverException commDriverEx )
            {
                m_Logger.finer( " FAILURE TO CLOSE CONNECTION" );
            }
        }

        m_CommDriver = null; //Dereference Comm Driver

        //Garbage collection
        System.gc();
    }

    /**
     * Returns the current operation state of the spectrophotometer
     * implementation
     *
     * @return int The operational state of the Spectrophotometer :
     *         OPERATIONAL_STATUS_IDLE, OPERATIONAL_STATUS_INITIALIZING
     *         OPERATIONAL_STATUS_SENDING OPERATIONAL_STATUS_RECEIVING
     */
    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    /**
     * Interprets the response from the instrument. The methods uses the
     * command waiting in the Incoming FIFO to interpret the response according
     * to the expected response format of the command. If successful, the
     * SpectroEvent created by the command is fired to the appropriate
     * listeners. Upon failure to interpret using the command, a guessInterpret
     * is made.
     *
     * @param message Complete response from the instrument.
     */
    protected void interpret( String message )
    {
        m_Logger.finer( "Interpreting command" );
        m_Logger.finer( message );

        //Remove ACK character if any
        if( message.indexOf( "*" ) >= 0 )
        {
            //Assuming that an ACK returns before any response...
            //We take everything from the ACK character onwards to be
            //a response
            message = message.substring( message.indexOf( "*" ) + 1 );
        }

        //Validate the check sum returned with the response
        if( !validate( message ) )
        {
            m_Logger.warning( "Checksum Error" );

            SpectroStatus status = SF300Status.create( "EEEEEEEEEEEEEEEEEEEE" );
            status.addMessage( "MSG_CHECKSUM_ERROR" );

            //Notify Checksum Error
            notifyStatusChange( new SpectroEvent( this, status ) );
            return;
        }
        else
        {
            //Checksum validation passed.
            //Remove checksum section from the response
            message = message.substring( 0, message.length() - 4 );
        }

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.finer( "Interpreting > " + message );
            m_Logger.finer( "Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's
            // expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the
            // interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.finer( "Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                SpectroEvent emptyEvent = new SpectroEvent( this );
                notifyStatusChange( emptyEvent );

                // Retrieve current settings of the instrument from the status
                // string
                m_Settings = createSettings( message );

                notifySettingsChanged( emptyEvent );

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    //ACK measurement
                    m_Logger.finer( "Acknowledging measurement" );
                    ack = true;
                    m_CommDriver.send( new String( "*" ).getBytes() );

                    m_Logger.finer( "Measure command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.finer( "Removing Measure command from Incoming" );
                    notifyMeasured( evt );
                }
                else if( cmd instanceof BlackCalibrationCommand )
                {
                    m_Logger.finer( "Black Calibration command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.finer( "Removing Black Calibration command from Incoming" );
                    m_Logger.finer( "Notifying Calibration" );
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof WhiteEndCommand )
                {
                    m_Logger.finer( "White Calibration command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.finer( "Removing White Calibration command from Incoming" );
                    m_Logger.finer( "Notifying Calibration" );
                    notifyCalibrated( evt );

                    if( m_Settings.getSpecular() )
                    {
                        m_WhiteTileSPI = evt.getReading();
                    }
                    else
                    {
                        m_WhiteTileSPE = evt.getReading();
                    }

                    computeScaleFactors();
                }
                else if( cmd instanceof SetSpecularCommand )
                {
                    m_Logger.finer( "Settings command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.finer( "Removing " + cmd.getName() + " from Incoming" );
                    m_Logger.finer( "Notifying Settings Change" );
                }
                else
                {
                    m_Logger.warning( "Unknown command" );
                    //Otherwise assume an unknown response was received.
                    guessInterpret( message );
                }
            }
            else
            {
                //Unknown by the waiting command
                guessInterpret( message );
            }
        }
        else
        {
            //If the method hasn't returned then the command is not expected or
            // unknown.
            //Guess interpret it.
            guessInterpret( message );
        }
    }

    /**
     * Attempt to interpret the response based on the data structure and
     * format. The method currently distinct response into two distinct types;
     * a measurement or calibration response or settings response. The
     * measurement or calibration response is differentiated by length of the
     * response indicating data values returned. The shorter response is
     * assumed to be a stray settings response. If succesfully interpreted as a
     * measurement or calibration response, the averaged value of the data
     * values is used to distinct a calibration from a measurement. A higher
     * average value indicates a white colour typically returned by a white
     * calibration command and is assumed to be a white tile reflectance data.
     * Lower average values are assumed to be a measurement WARNING: A
     * measurement of a White material may be overlooked as a white calibration
     * tile.
     *
     * @param message The unknown response received
     */
    protected void guessInterpret( String message )
    {
        //For SF300... there are no auto transmits...
        //shouldn't happen.

        m_Logger.finer( "Guess Interpret" );

        StringTokenizer sTok = new StringTokenizer( message, "\r\n" );

        //Create status:
        //First line is status string
        SpectroSettings settings = new SpectroSettings();

        String status = sTok.nextToken();

        //Parse status Errors
        SF300Status newStatus = SF300Status.create( status );

        //Parse settings
        settings = createSettings( status );

        //Count the number of lines containing data
        if( sTok.countTokens() == 9 )
        {
            //Longer response is assumed to be a calibration or measurement
            //response.

            //Start parsing reflectance data:
            // Specification states that there are 40 values
            // 8 lines of 5 reflectance starting from 360 to 750
            // at 10nm interval
            Map values = new HashMap();

            int currentWavelength = 360;
            int interval = 10;
            double sum = 0.0;
            int count = 0;

            for( int i = 0; i < 8; i++ )
            {
                String line = sTok.nextToken();

                StringTokenizer dataTok = new StringTokenizer( line, "," );

                for( int j = 0; j < 5; j++ )
                {
                    String data = dataTok.nextToken();

                    try
                    {
                        sum += new Double( data ).doubleValue();
                        count++;

                        values.put( new Double( currentWavelength ), new Double( data ) );
                    }
                    catch( NumberFormatException numEx )
                    {
                    }

                    currentWavelength += interval;
                }
            }

            sum = sum / count;

            DCIReading reading = new DCIReading( newStatus, settings, values );

            if( sum >= 0.80 )
            {
                //Assume it's a white calibration tile
                //Pitfall : measurement is a white material

                //Notify a white calibration response
                notifyCalibrated( new SpectroEvent( this, reading ) );
            }
            else
            {
                //Assume it's a normal measurement
                //Pitfall : bad white calibration

                m_Logger.finer( "Guessing a measurement" );
                m_Logger.finer( "ACK measurement" );

                ack = true;
                m_CommDriver.send( new String( "*" ).getBytes() );

                //Notify a measurment repsonse
                notifyMeasured( new SpectroEvent( this, reading ) );
            }
        }
        else
        {
            boolean error = false;

            //Assume it's a setting message:
            if( settings.getSpecular() != m_Settings.getSpecular() )
            {
                error = true;
            }
            else if( settings.getAperture() != m_Settings.getAperture() )
            {
                error = true;
            }
            else if( settings.getLightFilter() != m_Settings.getLightFilter() )
            {
                error = true;
            }
            else
            {
                error = false;
            }

            if( error )
            {
                SpectroStatus unknown_status = SF300Status.create( "EEEEEEEEEEEEEEEEEEEEEE" );

                unknown_status.addMessage( "MSG_UNKNOWN_STATUS" );

                notifyStatusChange( new SpectroEvent( this, unknown_status ) );
            }
        }
    }

    /**
     * Triggers a data reception from the CommDriver The method draws available
     * data from the CommDriver and determine if a complete response is
     * received by the indicating Terminator string. The method also checks for
     * ACK and NAK responses as well as the character sent when the instrument
     * is switched on [ (char) -4 ].
     *
     * A complete response is then interpreted.
     *
     * @param evt The triggerring Comm Driver Event.
     */
    public void received( CommDriverEvent evt )
    {
        m_Logger.finer( "Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {

            m_Logger.finer( "Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            //Determine if the Terminator is present
            if( m_Received.indexOf( ":" ) >= 0 )
            {
                //Message is complete.
                //Interpret command now
                String response = m_Received.toString();

                response = response.substring( 0, response.indexOf( ":" ) );

                //If an ACK character is present assume anything between the
                //ACK character and the Terminator is the data returned.
                if( response.indexOf( "*" ) >= 0 )
                {
                    response = response.substring( response.indexOf( "*" ) + 1 );
                }

                m_Logger.finer( response );

                //Interpret response
                interpret( response );

                //Remove the interpreted section from the buffer
                m_Received = new StringBuffer( m_Received.substring( m_Received.indexOf( ":" ) + 1 ) );
            }
            else if( m_Received.indexOf( "?" ) >= 0 )
            {
                //A NAK is received.
                m_Logger.finer( "NAK received" );
                m_Logger.finer( "Buffer > " + m_Received );

                //If syncing, a NAK is expected; ignore the NAK
                if( sync )
                {
                    m_Logger.finer( "NAK response to Sync command" );
                    sync = false;
                    return;
                }
                else if( ack )
                {
                    //If acknowledging a measurement; expecting a NAK, ignore
                    // it
                    m_Logger.finer( "NAK response to Measure ACK" );
                    ack = false;
                    return;
                }

                SpectroStatus status = SF300Status.create( "EEEEEEEEEEEEEEEEEEEE" );

                //NAK received: assuming the previously sent command was
                // NAK-ed
                //Pre-empt the waiting command and assume a response will not
                // be sent
                SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject(); //Remove
                // from
                // Incoming
                // FIFO

                m_Logger.finer( "Pre-empting " + cmd.getName() );

                status.addMessage( "MSG_NAK" );

                notifyStatusChange( new SpectroEvent( this, status ) );
            }
            else if( m_Received.indexOf( "*" ) >= 0 )
            {
                //ACK Received
                m_Logger.finer( "ACK received." );
                m_Logger.finer( "Buffer > " + m_Received );
            }
            else if( m_Received.indexOf( String.valueOf( STARTUP_CHAR ) ) >= 0 )
            {
                //Startup character receivd : Recevied when the instrument is
                // just turned on
                m_Logger.finer( "Instrument was just turned on." );
            }
            else
            {
                m_Logger.finer( "Buffer > " + m_Received );
            }
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = SF300Status.create( "EEEEEEEEEEEEEEEEEEEE" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            m_Logger.finer( "Pre-empting " + cmd.getName() );

            m_Logger.finer( "Timeout received for " + cmd.getName() );
            m_Logger.finer( "Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.finer( "Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.finer( "Comm Driver Sent" );
        }
        else
        {
            //Should not happen : Unknown comm status event
            m_Logger.finer( "Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.finer( "Sent event from CommDriver" );
    }

    /**
     * Notifies registered listeners of a measurement event
     *
     * @param evt The event to be sent or triggerred
     */
    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.finer( "Measurement received" );
        m_Logger.finer( "Applying calibration correction" );
        // KH - Jan 20, 2006 : we will directly manipulate the map
        correctSpectrum( evt.getReading().getValues() );

        Iterator list = m_Listeners.iterator();
        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    private void correctSpectrum( Map values )
    {
        Map factorTree;
        if( m_Settings.getSpecular() && m_ScaleFactorSPI != null )
        {
            factorTree = m_ScaleFactorSPI;
        }
        else if( m_ScaleFactorSPE != null )
        {
            factorTree = m_ScaleFactorSPE;
        }
        else
        {
            // KH - Jan 21, 2006 : do not correct if no scalefactor is available
            return;
        }

        float[] r_values = new float[ values.size() ];

        int start = 0;
        int interval = 0;
        int i = 0;

        Iterator list = values.keySet().iterator();

        while( list.hasNext() )
        {
            Object obj = list.next();

            if( i == 0 )
            {
                start = ( (Double) obj ).intValue();
                m_Logger.finer( "Start : " + start );
            }
            else if( i == 1 )
            {
                interval = ( (Double) obj ).intValue() - start;
                m_Logger.finer( "Next nm : " + obj );
            }
            m_Logger.finer( "value == " + ( (Double) values.get( obj ) ).floatValue() );
            r_values[ i++ ] = ( (Double) values.get( obj ) ).floatValue();
        }

        m_Logger.finer( "Interval calculated " + interval );

        // KH - Jan 20, 2006 : directly modifying map
        int currWave = start;
        for( Iterator iter = values.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry e = (Map.Entry) iter.next();
            double old = ( (Double) e.getValue() ).doubleValue();
            double scale = ( (Double) factorTree.get( new Double( "" + currWave ) ) ).doubleValue();
            double newVal = old * scale;
            e.setValue( new Double( newVal ) );
            m_Logger.finer( "scaling wave: " + currWave + ", old: " + old + ", new: " + newVal );
            currWave += interval;
        }
    }

    /**
     * Notifies registered listeners of a calibration event
     *
     * @param evt The event to be triggerred
     */
    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.finer( "Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    /**
     * Notifies registered listeners of setting changes
     *
     * @param evt The event to be triggered
     */
    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.finer( "Settings Ack received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).settingsChanged( evt );
        }
    }

    /**
     * Notifies registered listeners of status changes and errors
     *
     * @param evt The event or error to be triggered
     */
    public void notifyStatusChange( SpectroEvent evt )
    {
        m_Logger.finer( "Status change " );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).operationalStatusChanged( evt );
        }
    }

    /**
     * Unregister a spectrolistener
     *
     * @param listener The listener to unregister
     */
    public void removeSpectroListener( SpectroListener listener )
    {
        m_Listeners.remove( listener );
    }

    /**
     * Register a spectrolistener
     *
     * @param listener the listener to register
     */
    public void addSpectroListener( SpectroListener listener )
    {
        m_Listeners.add( listener );
    }

    /**
     * The thread that takes a command from the outgoing FIFO and sends it.
     * This thread is responsible for taking the constructed command, computing
     * the appropriate checksum and terminating it then send it.
     *
     * If the incoming FIFO is full, the thread would not send commands until
     * the incoming FIFO is available for more elements to be added.
     */
    public void run()
    {
        while( running )
        {
            if( stopRequest )
            {
                running = false;
                break;
            }

            //If the incoming FIFO is not full, send first command in outgoing
            // queue
            if( !m_Incoming.isFull() )
            {
                m_Logger.finer( "Thread still running" );
                m_Logger.finer( "Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                m_Logger.finer( "Constructing command" );
                if( cmd == null )
                {
                    continue;
                }

                String message = cmd.construct();

                message = message + computeChecksum( message );

                message = message + ":\r\n";

                m_Logger.finer( "Message constucted > " + message );

                m_Logger.finer( "Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
                m_CommDriver.send( message.getBytes() );

                m_CommDriver.setRespondTimeout( 15000 );

                try
                {
                    m_Incoming.putObject( cmd );
                }
                catch( FifoFullException fifoFullEx )
                {
                    continue;
                }

                //Command is sent, remove from outgoing FIFO
                m_Outgoing.removeNextObject();

                m_Logger.finer( "Message posted" );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //System.out.println( "SF300 Spectro : Spectro busy." );
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException irEx )
            {
                if( stopRequest )
                {
                    running = false;
                    break;
                }
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.finer( "Thread stopped." );
    }

    private synchronized void stopThread()
    {
        stopRequest = true;
        m_Thread.interrupt();
    }

    /**
     * Parses from the status string returned the current settings of the
     * instrument
     *
     * @param statusString The status string returned from the instrument
     *
     * @return SpectroSettings The settings parsed from the status string
     */
    private SpectroSettings createSettings( String statusString )
    {
        SpectroSettings settings = null;

        //System.out.println( "Status String : " + statusString );
        //System.out.println( "M_Settings changed" );
        //Assuming that the status string is correct

        if( m_Settings == null )
        {
            settings = new SpectroSettings();
        }
        else
        {
            settings = m_Settings;
        }

        m_Logger.finer( "Getting settings" );

        switch( statusString.charAt( 0 ) )
        {
        case 'I':
            //System.out.println( "M_Settings specular set to included" );
            m_Logger.finer( "Specular set to included" );
            settings.setSpecular( true );
            break;

        case 'E':
            //System.out.println( "M_Settings specular set to excluded" );
            m_Logger.finer( "Specular set to excluded" );
            settings.setSpecular( false );
            break;
        }

        switch( statusString.charAt( 1 ) )
        {
        case 'X':
            m_Logger.finer( "Aperture set to Extra Large" );
            settings.setAperture( new ExtraLargeAperture() );
            settings.setLensPosition( new ExtraLargeAreaView() );
            break;

        case 'N':
            m_Logger.finer( "Aperture set to Large" );
            settings.setAperture( new LargeAperture() );
            settings.setLensPosition( new LargeAreaView() );
            break;

        case 'M':
            m_Logger.finer( "Aperture set to Medium" );
            settings.setAperture( new MediumAperture() );
            settings.setLensPosition( new MediumAreaView() );
            break;

        case 'S':
            m_Logger.finer( "Aperture set to Small" );
            settings.setAperture( new SmallAperture() );
            settings.setLensPosition( new SmallAreaView() );
            break;

        case 'U':
            m_Logger.finer( "Aperture set to Ultra Small" );
            settings.setAperture( new UltraSmallAperture() );
            settings.setLensPosition( new UltraSmallAreaView() );
            break;
        }

        if( statusString.substring( 3, 6 ).equals( "000" ) )
        {
            m_Logger.finer( "Light Filter set to UV included" );
            settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else if( statusString.substring( 3, 6 ).equals( "001" ) )
        {
            m_Logger.finer( "Light Filter set to Cut Off 400" );
            settings.setLightFilter( new CutOff400LightFilter() );
        }
        else if( statusString.substring( 3, 6 ).equals( "002" ) )
        {
            m_Logger.finer( "Light Filter set to Cut Off 420" );
            settings.setLightFilter( new CutOff420LightFilter() );
        }
        else if( statusString.substring( 3, 6 ).equals( "003" ) )
        {
            m_Logger.finer( "Light Filter set to Cut Off 460" );
            settings.setLightFilter( new CutOff460LightFilter() );
        }

        return settings;
    }

    /**
     * Compute the checksum of the string
     *
     * @param command The string to compute the checksum for
     *
     * @return String of checksum bytes
     */
    private String computeChecksum( String command )
    {
        int sum = 0;
        for( int i = 0; i < command.length(); i++ )
        {
            sum = sum + command.charAt( i );
        }
        sum = sum & 0xFFFF;
        String s = "0000" + Integer.toHexString( sum );
        s = s.toUpperCase().substring( s.length() - 4 );
        return s;
    }

    /**
     * Validates the checksum of a string
     *
     * @return boolean true if checksum is valid false if checksum is invalid
     *
     * @oaram response String with the last four bytes the checksum
     */
    private boolean validate( String response )
    {
        String sentChecksum = response.substring( response.length() - 4 );
        m_Logger.finer( "Checksum received > " + sentChecksum );
        if( sentChecksum == null || sentChecksum.length() <= 0 )
        {
            m_Logger.warning( "Message withouth checksum:\n" + response );
        }
        String calcChecksum = computeChecksum( response.substring( 0, response.length() - 4 ) );
        m_Logger.finer( "Checksum calculated > " + calcChecksum );
        return sentChecksum.equals( calcChecksum );
    }

    private SpectroReading processData( File dataFile )
        throws IOException
    {
        /*
         * Assuming the following format:
         * 
         * E 0123456789F 
         * 000.000 100.000 200.000 300.000 400.000 500.00 600.00
         * 700.00 800.00 900.00 010.000 110.000 210.000 310.000 410.000 510.00
         * 610.00 710.00 810.00 910.00 020.000 120.000 220.000 320.000 420.000
         * 520.00 620.00 720.00 820.00 920.00 030.000 130.000 230.000 330.000
         * 430.000 530.00 630.00 730.00 830.00 930.00
         */

        FileInputStream fis = new FileInputStream( dataFile );

        byte[] bytes = new byte[ fis.available() ];

        for( int i = 0; i < bytes.length; i++ )
        {
            bytes[ i ] = (byte) fis.read();
        }

        String data = new String( bytes );
        m_Logger.finer( "CDF:\n" + data );
        StringTokenizer lineTokenizer = new StringTokenizer( data, "\n" );

        if( lineTokenizer.countTokens() < 5 )
        {
            m_Logger.warning( "Data contains less than expected lines" );
        }

        int line = 0;
        int currentWavelength = 360;
        int interval = 10;

        TreeMap values = new TreeMap();

        while( lineTokenizer.hasMoreTokens() )
        {
            String currentLine = lineTokenizer.nextToken();

            if( line == 0 )
            {
                m_Logger.finer( "Reference : " + currentLine );
            }
            else
            {
                StringTokenizer dataTokenizer = new StringTokenizer( currentLine, " " );

                if( dataTokenizer.countTokens() < 10 )
                {
                    m_Logger.warning( "Data contain less than expected values" );
                }

                while( dataTokenizer.hasMoreTokens() )
                {
                    String dataValues = dataTokenizer.nextToken();

                    try
                    {
                        values.put( new Double( currentWavelength ), new Double( dataValues ) );
                        currentWavelength += interval;
                    }
                    catch( NumberFormatException numEx )
                    {
                        m_Logger.warning( "Unreadable data, unable to convert to double" );
                    }
                }
            }
            line++;
        }

        return new DCIReading( null, null, values );
    }

    // KH - Jan 21, 2006 : should be called whenever the calibration files are changed
    // or when there is some white tile calibration event
    private void computeScaleFactors()
    {
        if( m_WhiteTileDataSPI != null && m_WhiteTileSPI != null )
        {
            m_ScaleFactorSPI = computeScaleFactors( m_WhiteTileSPI, m_WhiteTileDataSPI );
        }

        if( m_WhiteTileDataSPE != null && m_WhiteTileSPE != null )
        {
            m_ScaleFactorSPE = computeScaleFactors( m_WhiteTileSPE, m_WhiteTileDataSPE );
        }
    }

    private TreeMap computeScaleFactors( SpectroReading tile, SpectroReading dataFile )
    {
        TreeMap factors = new TreeMap();

        Map tileValues = tile.getValues();
        Map fileValues = dataFile.getValues();

        Iterator wavelengths = tileValues.keySet().iterator();

        while( wavelengths.hasNext() )
        {
            try
            {
                Double currentWavelength = (Double) wavelengths.next();

                Double tileValue = (Double) tileValues.get( currentWavelength );
                Double fileValue = (Double) fileValues.get( currentWavelength );

                if( tileValue != null && fileValue != null )
                {
                    Double scale = new Double( fileValue.doubleValue() / tileValue.doubleValue() );
                    // KH - Feb 7, 2006 : scale factor should never be zero,
                    // if so, then apply no scale
                    if( scale.doubleValue() <= 0.0 )
                    {
                        scale = new Double( 1.0 );
                    }

                    factors.put( currentWavelength, scale );
                }
            }
            catch( ClassCastException ccEx )
            {
                m_Logger.warning( "Unable to cast data from map" );
            }
        }

        return factors;
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {
        if( input == null )
        {
            return;
        }
        try
        {
            if( input[ 0 ] != null )
            {
                //Process Whitese file
                File whiteseFile = new File( input[ 0 ] );
                m_WhiteTileDataSPE = processData( whiteseFile );
            }

            if( input[ 1 ] != null )
            {
                //Process Whitesi file
                File whitesiFile = new File( input[ 1 ] );
                m_WhiteTileDataSPI = processData( whitesiFile );
            }

            computeScaleFactors();
        }
        catch( NullPointerException nullEx )
        {
            m_Logger.log( Level.SEVERE, nullEx.getMessage(), nullEx );
        }
        catch( IllegalArgumentException illArgEx )
        {
            m_Logger.log( Level.SEVERE, illArgEx.getMessage(), illArgEx );
        }
        catch( IOException ioEx )
        {
            m_Logger.log( Level.SEVERE, ioEx.getMessage(), ioEx );
        }
        if( m_ScaleFactorSPE != null )
        {
            StringBuffer sb = new StringBuffer( 1000 );
            for( Iterator iter = m_ScaleFactorSPE.entrySet().iterator(); iter.hasNext(); )
            {
                Map.Entry me = (Map.Entry) iter.next();
                sb.append( "Key, Value: " ).append( me.getKey() ).append( ", " ).append( me.getValue() ).append( "/n" );
            }
            m_Logger.finer( sb.toString() );
        }
        if( m_ScaleFactorSPI != null )
        {
            StringBuffer sb = new StringBuffer( 1000 );
            for( Iterator iter = m_ScaleFactorSPI.entrySet().iterator(); iter.hasNext(); )
            {
                Map.Entry me = (Map.Entry) iter.next();
                sb.append( "Key, Value: " ).append( me.getKey() ).append( ", " ).append( me.getValue() ).append( "/n" );
            }
            m_Logger.finer( sb.toString() );
        }
    }

    public SpectroStatus decode( String resultString )
    {
        return SF300Status.create( resultString );
    }
}
