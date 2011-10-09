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

package org.color4j.spectro.datacolor.sf450;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.color4j.spectro.datacolor.common.SetApertureCommand;
import org.color4j.spectro.datacolor.common.SetSpecularCommand;
import org.color4j.spectro.datacolor.common.SetUVFilterCommand;
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
 * Spectrophotometer object that supports
 * the functionality of manually entering
 * reflectance data.
 *
 * The SF450 Spectrophotometer:
 *
 * From the operational point of view, the SF450 runs on the
 * the principal of queueing outgoing commands and expecting
 * incoming response for each command sent to be interpreted and
 * and passed up the chain via events.
 *
 * To this effect, the SF450Spectro has two First In First Out (FIFO)
 * queues, one for outgoing commands and one for incoming commands.
 * There is a single thread that runs continuously while the SF450Spectro
 * is active. The thread is responsible for taking a queued command from the
 * outgoing FIFO and sending it to the instrument via the CommDriver. Once,
 * the command is sent, the command is placed into the incoming FIFO queue.
 * However if the incoming FIFO is full, the thread will not send anymore
 * commands until the incoming FIFO becomes available for more elements.
 * If the Outgoing FIFO becomes full due to this blocking mechanism,
 * the SF450Spectro will throw exceptions indicating that it is busy until
 * the Outgoing FIFO is available for more elements.
 *
 * For the purpose of keeping traffic to the instrument low, the Incoming
 * FIFO is kept low to a size of 1. And the Outgoing FIFO is set at a size
 * of 5 to facilitate multiple set settings commands to be placed in queue.
 *
 * To facilitate for a timeout; each command sent to the CommDriver
 * notifies the CommDriver to keep track of responses by setting a timer for a specified
 * time frame. At the end of each time frame, it is assumed that the instrument
 * has not responded and a time out event will be triggered. At this point,
 * the command in the Incoming FIFO is assumed to have timed out and is
 * pre-empted from the FIFO.
 *
 * The other functions of the SF450Spectro are all event driven. Procedures
 * are activated via events sent from the CommDriver. Reception of data from
 * the CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the SF450Spectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to listeners.
 * Otherwise, a null is returned and the SF450Spectro would guess at interpreting
 * the response. Currently, the response is interpreted as two response; a Measure
 * response or Calibration response and a settings response. The distinction used
 * to differentiate the two is the length of the response. The longer is assumed
 * to be a Calibration or Measure response while the shorter is assume to be a
 * Settings response.
 *
 * An average of the data values is used to identifiy a calibration from a
 * measurement. Currently, an average above 80% reflectance is assumed to be a
 * calibration and anything lower is assumed to be a measurement. While this may
 * work for most cases, the pitfall is that measurments of white or close to white
 * materials may be considerred to be a calibration.
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
public class SF450Spectro
    implements Spectrophotometer, CommDriverListener, Runnable, ResponseDecoder
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( SF450Spectro.class.getName() );
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

    protected SpectroSettings m_Settings; //The current settings of the instrument    
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo; //Serial number of the instrument, none for the SF450

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this    

    protected int m_OpStatus; //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean sync; //Indicating waiting for a sync response
    protected boolean ack; // Indicating waiting for an ACK response
    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false;

    protected Thread m_Thread;

    /**
     * Instantiates and initializes the spectrophotometer. The constructor does
     * not open the CommDriver until the first setSettings containing the CommParamaters
     * are passed in.
     */
    public SF450Spectro()
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
     * Initiates a measure command.
     * The method places a measure command in the outgoing FIFO.
     *
     * @throws SpectroException when the incoming queue is Blocked and no further
     *                          commands can be sent. The spectro is considerred as busy.
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
     * Initiates a calibration command.
     * The method places a calibration command in the outgoing FIFO.
     *
     * @param step - indicates the calibration procedure step
     *
     * @throws SpectroException when the incoming queue is Blocked and no further
     *                          commands can be sent. The spectro is considerred as busy. Also thrown
     *                          when an unrecognized calibration procedure step is passed in.
     */
    public void calibrate( int step )
        throws SpectroException
    {
        try
        {
            switch( step )
            {

            case 0: //Black Calibration
                m_Outgoing.putObject( new BlackCalibrationCommand( 1, this ) );
                break;

            case 1: //White Calibration
                m_Outgoing.putObject( new WhiteEndCommand( 1, this ) );
                break;

            default:
                /*  Either the driver is instantiating the wrong spectrophotometer
                *  implmentation or the driver has an erroneous calibration procedure
                *  list.
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

    /*  Not supported by the SF450 Spectros
     *
     *  @throws NotSupportectExcetpion This is not supported by the SF450 Spectros
     */
    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    /*  Not supported by the SF450 Spectros
     *
     *  @throws NotSupportectExcetpion This is not supported by the SF450 Spectros
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

    /*  Not supported by the SF450 Spectros
     *
     *  @throws NotSupportectExcetpion This is not supported by the SF450 Spectros
     */
    public void retrieveStandard( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*  Not supported by the SF450 Spectros
     *
     *  @throws NotSupportectExcetpion This is not supported by the SF450 Spectros
     */
    public void queryNoOfStoredSamples()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*  Not supported by the SF450 Spectros
     *
     *  @throws NotSupportectExcetpion This is not supported by the SF450 Spectros
     */
    public void queryNoOfStoredStandards()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*  Returns the current Spectro settings of the instrument
     *
     *  @return SpectroSetting the current settings of the instrument.
     */
    public SpectroSettings getSettings()
    {
        return m_Settings;
    }

    /**
     * Set the instrument to a new set of specified settings.
     * When called for the first time with the Comm Parameters included,
     * the Serial Port connection is also opened with this method.
     * The set settings method attempts to synchronize the current settings
     * with the new settings. A set command is sent for each parameter that
     * differs from the current settings to avoid unneccesary comm traffic.
     * Each call overwrites parameters that differ from the previous settings,
     * and set commands are sent for only those parameters.
     *
     * @param newSettings The new settings for the instrument.
     */
    public void setSettings( SpectroSettings newSettings )
    {
        synchronized( this )
        {
            m_Logger.log( Level.INFO, "Set settings called" );

            if( newSettings == null )
            {
                m_Logger.log( Level.INFO, "Null settings enterred" );
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

                    m_Logger.log( Level.INFO, "Port name : " + portname );
                    m_Logger.log( Level.INFO, "Bit rate : " + bitrate );

                    m_CommDriver.openConnection( portname, 3000, 19200 );

                    m_CommDriver.addCommDriverListener( this );

                    //Send a sync command:
                    sync = true;
                    m_CommDriver.send( new String( ":\r\n" ).getBytes() );

                    //Wake instrument from unknown state:
                    // - Previous measure command fail to ACK or NAK the measurement.
                    ack = true;
                    m_CommDriver.send( new String( "*" ).getBytes() );

                    running = true;

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );

                    m_Logger.log( Level.INFO, "Comm Settings complete... return" );
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
                        //Give up.... inform user that it is not possible
                        //to open connection
                        m_Logger.log( Level.INFO, "FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = SF450Status.create( "EEEEEEEEEEEEEEEEEEEE" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.log( Level.INFO, "Comm Settings complete... return" );
                        return;
                    }

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.log( Level.INFO, "FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = SF450Status.create( "EEEEEEEEEEEEEEEEEEEE" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.log( Level.INFO, "Unable to open port... return" );
                    return;
                }

                m_Logger.log( Level.INFO, "Should not reach this return in set settings" );
                return;
            }

            //Attempt to synchronise and store new settings
            if( m_newSettings == null )
            {
                m_newSettings = newSettings;
            }

            //If settings not created, create a new one.
            if( m_Settings == null )
            {
                m_Settings = new SpectroSettings();
            }

            //Compare and update new settings:

            //Compare specular setting:
            if( m_Settings.getSpecular() != newSettings.getSpecular() )
            {
                m_Logger.log( Level.INFO, "Setting Specular " + ( newSettings.getSpecular() ? "Included" : "Excluded" ) );
                m_newSettings.setSpecular( newSettings.getSpecular() );

                try
                {
                    SetSpecularCommand command = new SetSpecularCommand( m_newSettings.getSpecular(), this );
                    m_Outgoing.putObject( command );
                }
                catch( FifoFullException fullEx )
                {
                    m_Logger.log( Level.INFO, "Wait for fifo to be cleared." );
                }
            }

            //Compare LensPosition setting:
            if( newSettings.getLensPosition() != null )
            {
                m_Logger.log( Level.INFO, "Setting " + newSettings.getLensPosition().getName() );
                if( m_Settings.getLensPosition() == null || !m_Settings.getLensPosition()
                    .getName()
                    .equals( newSettings.getLensPosition().getName() ) )
                {
                    if( m_Settings.getLensPosition() != null )
                    {
                        m_Logger.log( Level.INFO, "Current Lens Position " + m_Settings.getLensPosition().getName() );
                        m_Logger.log( Level.INFO, "    New Lens Position " + newSettings.getLensPosition().getName() );
                    }
                    else
                    {
                        m_Logger.log( Level.INFO, "    New Lens Position " + newSettings.getLensPosition().getName() );
                    }

                    m_newSettings.setLensPosition( newSettings.getLensPosition() );

                    try
                    {
                        m_Outgoing.putObject( new SetApertureCommand( m_newSettings.getLensPosition(), this ) );
                    }
                    catch( FifoFullException fullEx )
                    {
                        m_Logger.log( Level.INFO, "Wait for fifo to be cleared." );
                        m_Logger.log( Level.INFO, "Command " + ( (SpectroCommand) m_Outgoing.getNextObject() ).getName() );
                    }
                }
                else
                {
                    m_Logger.log( Level.INFO, "Settings unchanged" );
                    m_Logger.log( Level.INFO, "Current Settings " + m_Settings.getLensPosition().getName() );
                    m_Logger.log( Level.INFO, "New     Settings " + newSettings.getLensPosition().getName() );
                }
            }

            //Compare Light Filter settings:
            if( newSettings.getLightFilter() != null )
            {
                m_Logger.log( Level.INFO, "Setting " + newSettings.getLightFilter().getName() );
                if( m_Settings.getLightFilter() == null || !m_Settings.getLightFilter()
                    .getName()
                    .equals( newSettings.getLightFilter().getName() ) )
                {
                    m_newSettings.setLightFilter( newSettings.getLightFilter() );

                    try
                    {
                        m_Outgoing.putObject( new SetUVFilterCommand( m_newSettings.getLightFilter(), this ) );
                    }
                    catch( FifoFullException fullEx )
                    {
                        m_Logger.log( Level.INFO, "Wait for fifo to be cleared." );
                    }
                }
                else
                {
                    m_Logger.log( Level.INFO, "Settings unchanged" );
                    m_Logger.log( Level.INFO, "Current Settings " + m_Settings.getLightFilter().getName() );
                    m_Logger.log( Level.INFO, "New     Settings " + newSettings.getLightFilter().getName() );
                }
            }

            m_Logger.log( Level.INFO, "Set setings done" );
        }
    }

    /**
     * Returns the Serial number of the instrument. Not supported by SF450 Spectros
     * The method currently returns a null string.
     *
     * @return String null String since the instrument does not provide a serial number
     */
    public String getSerialNo()
    {
        return m_SerialNo;
    }

    /**
     * Initializes the spectrophotomer; but does not open connection to the serial port
     * The initialization process covers the instantiation of the FIFO queues but
     * does not open the connection and start the send thread.
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
     * Dispose of resources held by this Spectrophotometer implementation
     * Upon invocation, closes connection held to the serial port. Dereference
     * pointers to objects and call garbage collection
     */
    public void dispose()
    {
        m_Logger.log( Level.INFO, "Dispose called" );
        stopThread();

        while( running )
        {
            // Wait until fully stop
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
            m_Logger.log( Level.INFO, "Thread still running" );
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
                m_Logger.info( " FAILURE TO CLOSE CONNECTION" );
            }
        }

        m_CommDriver = null; //Dereference Comm Driver

        //Garbage collection
        System.gc();
    }

    /**
     * Returns the current operation state of the spectrophotometer implementation
     *
     * @return int The operational state of the Spectrophotometer :
     *         OPERATIONAL_STATUS_IDLE,
     *         OPERATIONAL_STATUS_INITIALIZING
     *         OPERATIONAL_STATUS_SENDING
     *         OPERATIONAL_STATUS_RECEIVING
     */
    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    //========================== Protected Methods =============================

    /**
     * Interprets the response from the instrument.
     * The methods uses the command waiting in the Incoming FIFO to
     * interpret the response according to the expected response format of the
     * command. If successful, the SpectroEvent created by the command is fired
     * to the appropriate listeners. Upon failure to interpret using the command,
     * a guessInterpret is made.
     *
     * @param message Complete response from the instrument.
     */
    protected void interpret( String message )
    {
        m_Logger.log( Level.INFO, "Interpreting command" );
        m_Logger.log( Level.INFO, message );

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
            m_Logger.log( Level.WARNING, "Checksum Error" );

            SpectroStatus status = SF450Status.create( "EEEEEEEEEEEEEEEEEEEE" );
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

            m_Logger.log( Level.WARNING, "Message without Checksum " + message );
        }

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.log( Level.INFO, "Interpreting > " + message );
            m_Logger.log( Level.INFO, "Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();

                m_Logger.log( Level.INFO, "Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                m_Settings = createSettings( message );

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    //ACK measurement
                    m_Logger.log( Level.INFO, "Acknowledging measurement" );
                    ack = true;
                    m_CommDriver.send( new String( "*" ).getBytes() );

                    m_Logger.log( Level.INFO, "Measure command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.log( Level.INFO, "Removing Measure command from Incoming" );
                    notifyMeasured( evt );
                    return;
                }
                else if( cmd instanceof BlackCalibrationCommand )
                {
                    m_Logger.log( Level.INFO, "Black Calibration command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.log( Level.INFO, "Removing Black Calibration command from Incoming" );
                    m_Logger.log( Level.INFO, "Notifying Calibration" );
                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof WhiteEndCommand )
                {
                    m_Logger.log( Level.INFO, "White Calibration command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.log( Level.INFO, "Removing White Calibration command from Incoming" );
                    m_Logger.log( Level.INFO, "Notifying Calibration" );
                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof SetApertureCommand ||
                         cmd instanceof SetUVFilterCommand ||
                         cmd instanceof SetSpecularCommand )
                {
                    m_Logger.log( Level.INFO, "Settings command " );
                    m_Incoming.removeNextObject(); //Remove from fifo

                    m_Logger.log( Level.INFO, "Removing " + cmd.getName() + " from Incoming" );
                    m_Logger.log( Level.INFO, "Notifying Settings Change" );
                    notifySettingsChanged( evt );
                    return;
                }
                else
                {
                    //If an erroneous response is received for a waiting measure command
                    //a NAK is sent to get the instrument to re-send the measurement data.
                    if( cmd instanceof MeasureCommand )
                    {
                        m_Logger.log( Level.INFO, "Measure command waiting" );
                        m_Logger.log( Level.INFO, "NAK received and wait for retransmit" );

                        m_CommDriver.send( new String( "?" ).getBytes() );
                        return;
                    }

                    //Otherwise assume an unknown response was received.
                    m_Logger.log( Level.INFO, "Unknown command" );
                }
            }
            else
            {
                //Unknown by the waiting command
                guessInterpret( message );
            }
        }

        //If the method hasn't returned then the command is not expected or unknown.
        //Guess interpret it.        
        guessInterpret( message );
    }

    /**
     * Attempt to interpret the response based on the data structure and format.
     * The method currently distinct response into two distinct types; a measurement
     * or calibration response or settings response. The measurement or calibration
     * response is differentiated by length of the response indicating data values
     * returned. The shorter response is assumed to be a stray settings response.
     * If succesfully interpreted as a measurement or calibration response, the
     * averaged value of the data values is used to distinct a calibration from
     * a measurement. A higher average value indicates a white colour typically
     * returned by a white calibration command and is assumed to be a white
     * tile reflectance data. Lower average values are assumed to be a measurement
     * WARNING: A measurement of a White material may be overlooked as a white
     * calibration tile.
     *
     * @param message The unknown response received
     */
    protected void guessInterpret( String message )
    {
        //For SF450... there are no auto transmits...
        //shouldn't happen.

        m_Logger.log( Level.INFO, "Guess Interpret" );

        StringTokenizer sTok = new StringTokenizer( message, "\r\n" );

        //Create status:
        //First line is status string
        SpectroSettings settings = new SpectroSettings();

        String status = sTok.nextToken();

        //Parse status Errors
        SF450Status newStatus = SF450Status.create( status );

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

                m_Logger.log( Level.INFO, "Guessing a measurement" );
                m_Logger.log( Level.INFO, "ACK measurement" );

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
                SpectroStatus unknown_status = SF450Status.create( "EEEEEEEEEEEEEEEEEEEEEE" );

                unknown_status.addMessage( "MSG_UNKNOWN_STATUS" );

                notifyStatusChange( new SpectroEvent( this, unknown_status ) );
            }
        }
    }

    /**
     * Triggers a data reception from the CommDriver
     * The method draws available data from the CommDriver and determine if
     * a complete response is received by the indicating Terminator string.
     * The method also checks for ACK and NAK responses as well as the character
     * sent when the instrument is switched on [ (char) -4 ].
     *
     * A complete response is then interpreted.
     *
     * @param evt The triggerring Comm Driver Event.
     */
    public void received( CommDriverEvent evt )
    {
        m_Logger.log( Level.INFO, "Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.log( Level.INFO, "Comm Driver Received" );

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

                m_Logger.log( Level.INFO, response );

                //Interpret response
                interpret( response );

                //Remove the interpreted section from the buffer
                m_Received = new StringBuffer( m_Received.substring( m_Received.indexOf( ":" ) + 1 ) );
            }
            else if( m_Received.indexOf( "?" ) >= 0 )
            {
                //A NAK is received. 
                m_Logger.log( Level.INFO, "NAK received." );
                m_Logger.log( Level.INFO, "Buffer > " + m_Received );

                //If syncing, a NAK is expected; ignore the NAK
                if( sync )
                {
                    m_Logger.log( Level.INFO, "NAK response to Sync command" );
                    sync = false;
                    return;
                }
                else if( ack )
                {
                    //If acknowledging a measurement; expecting a NAK, ignore it
                    m_Logger.log( Level.INFO, "NAK response to Measure ACK" );
                    ack = false;
                    return;
                }

                SpectroStatus status = SF450Status.create( "EEEEEEEEEEEEEEEEEEEE" );

                //NAK received: assuming the previously sent command was NAK-ed
                //Pre-empt the waiting command and assume a response will not be sent                
                SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject(); //Remove from Incoming FIFO

                m_Logger.log( Level.INFO, "Pre-empting " + cmd.getName() );

                status.addMessage( "MSG_NAK" );

                notifyStatusChange( new SpectroEvent( this, status ) );
            }
            else if( m_Received.indexOf( "*" ) >= 0 )
            {
                //ACK Received
                m_Logger.log( Level.INFO, "ACK received." );
                m_Logger.log( Level.INFO, "Buffer > " + m_Received );
            }
            else if( m_Received.indexOf( String.valueOf( STARTUP_CHAR ) ) >= 0 )
            {
                //Startup character receivd : Recevied when the instrument is just turned on                
                m_Logger.log( Level.INFO, "Instrument was just turned on." );
            }
            else
            {
                m_Logger.log( Level.INFO, "Buffer > " + m_Received );
            }
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = SF450Status.create( "EEEEEEEEEEEEEEEEEEEE" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            m_Logger.log( Level.INFO, "Pre-empting " + cmd.getName() );

            m_Logger.log( Level.INFO, "Timeout received for " + cmd.getName() );
            m_Logger.log( Level.INFO, "Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.log( Level.INFO, "Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.log( Level.INFO, "Comm Driver Sent" );
        }
        else
        {

            //Should not happen : Unknown comm status event
            m_Logger.log( Level.INFO, "Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.log( Level.INFO, "Sent event from CommDriver" );
    }

    //===================== Notify Spectrolistener =============================

    /**
     * Notifies registered listeners of a measurement event
     *
     * @param evt The event to be sent or triggerred
     */
    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.log( Level.INFO, "Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    /**
     * Notifies registered listeners of a calibration event
     *
     * @param evt The event to be triggerred
     */
    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.log( Level.INFO, "Calibration received" );
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
        m_Logger.log( Level.INFO, "Settings Ack received" );
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
        m_Logger.log( Level.INFO, "Status change " );
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

    //========================== Thread method =================================

    /**
     * The thread that takes a command from the outgoing FIFO and sends it.
     * This thread is responsible for taking the constructed command,
     * computing the appropriate checksum and terminating it then send it.
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

            //If the incoming FIFO is not full, send first command in outgoing queue
            if( !m_Incoming.isFull() )
            {
                m_Logger.log( Level.INFO, "Thread still running" );
                m_Logger.log( Level.INFO, "Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                m_Logger.log( Level.INFO, "Constructing command" );

                if( cmd == null )
                {
                    continue;
                }

                String message = cmd.construct();

                message = message + computeChecksum( message );

                message = message + ":\r\n";

                m_Logger.log( Level.INFO, "Message constucted > " + message );

                m_Logger.log( Level.INFO, "Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                m_Logger.log( Level.INFO, "Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //System.out.println( "SF450 Spectro : Spectro busy." );
            }

            try
            {
                m_Thread.sleep( 300 );
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
        m_Logger.log( Level.INFO, "Thread stopped." );
    }

    private synchronized void stopThread()
    {
        stopRequest = true;
        m_Thread.interrupt();
    }

    //=========================== Checksum methods =============================

    /**
     * Parses from the status string returned the current settings of the instrument
     *
     * @param statusString The status string returned from the instrument
     *
     * @return SpectroSettings The settings parsed from the status string
     */
    private SpectroSettings createSettings( String statusString )
    {
        //Assuming that the status string is correct

        if( m_Settings == null )
        {
            m_Settings = new SpectroSettings();
        }

        m_Logger.log( Level.INFO, "Getting settings" );

        switch( statusString.charAt( 0 ) )
        {
        case 'I':
            m_Logger.log( Level.INFO, "Specular set to included" );
            m_Settings.setSpecular( true );
            break;

        case 'E':
            m_Logger.log( Level.INFO, "Specular set to excluded" );
            m_Settings.setSpecular( false );
            break;
        }

        switch( statusString.charAt( 1 ) )
        {
        case 'X':
            m_Logger.log( Level.INFO, "Aperture set to Extra Large" );
            m_Settings.setAperture( new ExtraLargeAperture() );
            m_Settings.setLensPosition( new ExtraLargeAreaView() );
            break;

        case 'N':
            m_Logger.log( Level.INFO, "Aperture set to Large" );
            m_Settings.setAperture( new LargeAperture() );
            m_Settings.setLensPosition( new LargeAreaView() );
            break;

        case 'M':
            m_Logger.log( Level.INFO, "Aperture set to Medium" );
            m_Settings.setAperture( new MediumAperture() );
            m_Settings.setLensPosition( new MediumAreaView() );
            break;

        case 'S':
            m_Logger.log( Level.INFO, "Aperture set to Small" );
            m_Settings.setAperture( new SmallAperture() );
            m_Settings.setLensPosition( new SmallAreaView() );
            break;

        case 'A':
            m_Logger.log( Level.INFO, "Aperture set to Ultra Small" );
            m_Settings.setAperture( new UltraSmallAperture() );
            m_Settings.setLensPosition( new UltraSmallAreaView() );
            break;
        }

        if( statusString.substring( 3, 6 ).equals( "000" ) )
        {
            m_Logger.log( Level.INFO, "Light Filter set to UV included" );
            m_Settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else if( statusString.substring( 3, 6 ).equals( "001" ) )
        {
            m_Logger.log( Level.INFO, "Light Filter set to Cut Off 400" );
            m_Settings.setLightFilter( new CutOff400LightFilter() );
        }
        else if( statusString.substring( 3, 6 ).equals( "002" ) )
        {
            m_Logger.log( Level.INFO, "Light Filter set to Cut Off 420" );
            m_Settings.setLightFilter( new CutOff420LightFilter() );
        }
        else if( statusString.substring( 3, 6 ).equals( "003" ) )
        {
            m_Logger.log( Level.INFO, "Light Filter set to Cut Off 460" );
            m_Settings.setLightFilter( new CutOff460LightFilter() );
        }

        return m_Settings;
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
     * @return boolean true if checksum is valid
     *         false if checksum is invalid
     *
     * @oaram response String with the last four bytes the checksum
     */
    private boolean validate( String response )
    {
        String sentChecksum = response.substring( response.length() - 4 );
        m_Logger.log( Level.INFO, "Checksum received > " + sentChecksum );
        String calcChecksum = computeChecksum( response.substring( 0, response.length() - 4 ) );
        m_Logger.log( Level.INFO, "Checksum calculated > " + calcChecksum );
        return sentChecksum.equals( calcChecksum );
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {
        // KH - Jan 20, 2006 : does nothing? should be like SF300/600

    }

    public SpectroStatus decode( String resultString )
    {
        return SF450Status.create( resultString );
    }
}
