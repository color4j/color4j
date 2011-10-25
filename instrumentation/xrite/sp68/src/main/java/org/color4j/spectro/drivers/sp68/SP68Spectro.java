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

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
 * The SP68 Spectrophotometer:
 *
 * From the operational point of view, the SP68 runs on the
 * the principal of queueing outgoing commands and expecting
 * incoming response for each command sent to be interpreted and
 * and passed up the chain via events.
 *
 * To this effect, the SP68Spectro has two First In First Out (FIFO)
 * queues, one for outgoing commands and one for incoming commands.
 * There is a single thread that runs continuously while the SP68Spectro
 * is active. The thread is responsible for taking a queued command from the
 * outgoing FIFO and sending it to the instrument via the CommDriver. Once,
 * the command is sent, the command is placed into the incoming FIFO queue.
 * However if the incoming FIFO is full, the thread will not send anymore
 * commands until the incoming FIFO becomes available for more elements.
 * If the Outgoing FIFO becomes full due to this blocking mechanism,
 * the SP68Spectro will throw exceptions indicating that it is busy until
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
 * The other functions of the SP68Spectro are all event driven. Procedures
 * are activated via events sent from the CommDriver. Reception of data from
 * the CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the SP68Spectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to listeners.
 * Otherwise, a null is returned and the SP68Spectro would guess at interpreting
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
public class SP68Spectro implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger = Logger.getLogger( SP68Spectro.class.getName() );

    static
    {
    }

    /**
     * Constructs and initialize the spectrophotometer.
     */
    public static final String TERMINATOR;

    static
    {
        TERMINATOR = "\r"; //Termniator : "<lf><cr>"
    }

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the instrument    
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo; //Serial number of the instrument, none for the SP68

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this

    protected int m_OpStatus; //The operational status of the spectro implementation
    protected int m_Stored; //The number of stored measurements the last time the instrument was queried
    protected int m_LastLoaded; //The last loaded stored measurement

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false;

    protected boolean measurementRequested = false; //Set when user has made a request to retrieve measurement

    protected Thread m_Thread;

    /**
     * Instantiates and initializes the spectrophotometer. The constructor does
     * not open the CommDriver until the first setSettings containing the CommParamaters
     * are passed in.
     */
    public SP68Spectro()
    {

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
            measurementRequested = true;
            m_Outgoing.putObject( new GetMeasurementStatusCommand() );
            //m_Outgoing.putObject( new MeasureCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
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
        throw new NotSupportedException( "MSG_MANUAL_CALIBRATION" );
    }

    public void retrieveStoredSamples()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_MEASURE" );
    }

    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_MEASURE" );
    }

    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS" );
    }

    public void retrieveStandards()
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new RetrieveStoredNumberCommand() );
        }
        catch( FifoFullException fifoFullEx )
        {
            throw new SpectroException( "Spectro busy" );
        }
//        //Not supported
//        throw new NotSupportedException("MSG_STANDARS");
    }

    public void retrieveStandard( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS" );
    }

    public void queryNoOfStoredSamples()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    public void queryNoOfStoredStandards()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
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
            m_Logger.info( "SP68 Spectro : Set settings called" );

            if( newSettings == null )
            {
                m_Logger.info( "SP68 Spectro : Null settings enterred" );
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

                    m_Logger.info( "Port name : " + portname );
                    m_Logger.info( "Bit rate : " + bitrate );

                    //Opening using Default;
                    //m_CommDriver.openConnection( portname, 3000, Integer.parseInt( bitrate ) );
                    m_CommDriver.openConnection( portname, 3000, 9600 );

                    m_CommDriver.addCommDriverListener( this );

                    try
                    {
                        //Sent initialize signal
                        //m_CommDriver.post( "\r\n".getBytes(), this );

                        //Set Baudrate
                        //m_Outgoing.putObject( new SetBaudRateCommand( SetBaudRateCommand.BAUD9600 ) );

                        //Set Printout Format
                        m_Outgoing.putObject( new SetPrintoutFormatCommand( SetPrintoutFormatCommand.SPECTRAL_FORMAT, SetPrintoutFormatCommand.HEADER_ON, SetPrintoutFormatCommand.NO_CHANGE ) );

                        //Set Averaging mode
                        m_Outgoing.putObject( new SetAveragingCommand( new Integer( 1 ) ) );

                        //Get Version Code
                        m_Outgoing.putObject( new GetVersionCommand() );
                    }
                    catch( FifoFullException fifoEx )
                    {
                        m_Logger.warning( "Warning: Unable to initialize settings for the Spectro" );
                    }

                    //If settings not created, create a new one.
                    if( m_Settings == null )
                    {
                        m_Settings = new SpectroSettings();
                        m_Settings.setAperture( new MediumAperture() );
                        m_Settings.setLightFilter( new DefaultLightFilter() );
                        m_Settings.setLensPosition( new MediumAreaView() );
                    }

                    running = true;

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );

                    m_Logger.info( "SP68 Spectro : Comm Settings complete... return" );
                    return;
                }
                catch( NumberFormatException numEx )
                {
                    //Try to recoved from exception and use a preset default
                    //bitrate
                    m_Logger.info( "Invalid bitrate provided " );
                    m_Logger.info( "Using default bitrate of 9600" );

                    String portname = (String) commParameters.get( "PORTNAME" );

                    try
                    {
                        m_CommDriver.openConnection( portname, 3000, 9600 );

                        m_CommDriver.addCommDriverListener( this );
                    }
                    catch( CommDriverException commEx )
                    {
                        //Give up.... inform user that it is not possible
                        //to open connection
                        m_Logger.info( "SP68 Spectro : FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = SP68Status.create( "EEEEEEEEEEEEEEEEEEEE" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.info( "SP68 Spectro : Comm Settings complete... return" );
                        return;
                    }

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.info( "SP68 Spectro : FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = new SP68Status();
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.info( "SP68 Spectro : Unable to open port... return" );
                    return;
                }

                m_Logger.info( "SP68 Spectro : Should not reach this return in set settings" );
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
                m_Settings.setAperture( new MediumAperture() );
                m_Settings.setLightFilter( new DefaultLightFilter() );
                m_Settings.setLensPosition( new MediumAreaView() );
            }

            //Compare and update new settings:

            //Compare specular setting:
            if( m_Settings.getSpecular() != newSettings.getSpecular() )
            {
                m_Logger.info( "SP68 Spectro : Setting Specular " + ( newSettings.getSpecular() ? "Included" : "Excluded" ) );
                m_Settings.setSpecular( newSettings.getSpecular() );
            }

            if( m_Settings.getAperture() != newSettings.getAperture() && newSettings.getAperture() != null )
            {
                m_Logger.info( "SP68 Spectro : Setting " + newSettings.getAperture() );
                m_Settings.setAperture( newSettings.getAperture() );

                if( "MAV".equals( m_Settings.getAperture().getName() ) )
                {
                    m_Settings.setLensPosition( new MediumAreaView() );
                }
                else if( "SAV".equals( m_Settings.getAperture().getName() ) )
                {
                    m_Settings.setLensPosition( new SmallAreaView() );
                }
                else if( "USAV".equals( m_Settings.getAperture().getName() ) )
                {
                    m_Settings.setLensPosition( new UltraSmallAreaView() );
                }
                else
                {
                    m_Settings.setLensPosition( new MediumAreaView() );
                }
            }

            m_Logger.info( "SP68 Spectro : Set setings done" );
        }
    }

    /**
     * Returns the Serial number of the instrument. Not supported by SP68 Spectros
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

        m_Outgoing = new Fifo( 10 );
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
        m_Logger.info( "SP68 Spectro : Dispose called" );

        stopThread();

        while( running )
        {
            //wait untill thread stop
        }

        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;

        m_Incoming = null; //Dereference Incoming FIFO
        m_Outgoing = null; //Derefernce Outgoing FIFO

        m_LastStatus = null; //Dereference SpectroStatus
        m_Settings = null; //Dereference SpectroSettings
        m_SerialNo = null; //Derference String

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

    public void setCalibrationDataFiles( URI[] fileURIs )
    {
        //TO DO
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
        m_Logger.info( "SP68 Spectro : Interpreting command" );
        m_Logger.info( "SP68 Spectro : " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.info( "SP68 Spectro : Interpreting > " + message );
            m_Logger.info( "SP68 Spectro : Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {

                if( evt.getStatus().isFailure() )
                {
                    try
                    {
                        m_Outgoing.putObject( new RetrieveErrorsCommand() );
                        m_Outgoing.putObject( new ClearErrorCommand() );
                    }
                    catch( FifoFullException fifoEx )
                    {
                        m_Logger.info( "Unable to clear errors... Fifo queue is fill" );
                    }
                }

                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.info( "SP68 Spectro : Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Decide which listener method to notify:
                if( cmd instanceof GetMeasurementStatusCommand )
                {
                    m_Logger.info( "SP68 Spectro : " + cmd.getName() );

                    RetrieveReflectanceCommand command = new RetrieveReflectanceCommand( new Boolean( m_Settings.getSpecular() ) );
                    String msg = command.construct() + "\r";

                    m_CommDriver.send( msg.getBytes() );

                    m_Incoming.removeNextObject(); //Remove from fifo

                    try
                    {
                        m_Incoming.putObject( command );
                    }
                    catch( Exception ex )
                    {
                    }

                    m_Logger.info( "SP68 Spectro : Removing Measure command from Incoming" );
                    return;
                }
                else if( cmd instanceof SetAveragingCommand )
                {
                    Iterator messages = evt.getStatus().getMessages().iterator();

                    while( messages.hasNext() )
                    {
                        message = (String) messages.next();

                        if( message.indexOf( "AVERAGE:" ) > 0 )
                        {
                            m_Logger.info( "SP68 Spectro : Averaging number : " + message.substring( message.indexOf( "AVERAGE:" ) ) );
                        }
                    }

                    m_Logger.info( "SP68 Spectro : Averaging set" );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( "SP68 Spectro : Removing " + cmd.getName() + " from Incoming" );
                    return;
                }
                else if( cmd instanceof SetBaudRateCommand )
                {
                    Iterator messages = evt.getStatus().getMessages().iterator();

                    while( messages.hasNext() )
                    {
                        message = (String) messages.next();

                        if( message.indexOf( "BAUDRATE:" ) > 0 )
                        {
                            m_Logger.info( "SP68 Spectro : Baud rate set to " + message.substring( message.indexOf( "BAUDRATE:" ) ) );
                        }
                    }
                    m_Logger.info( "SP68 Spectro : Baud rate set" );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( "SP68 Spectro : Removing " + cmd.getName() + " from Incoming" );
                    return;
                }
                else if( cmd instanceof GetVersionCommand )
                {
                    Iterator messages = evt.getStatus().getMessages().iterator();

                    while( messages.hasNext() )
                    {
                        message = (String) messages.next();

                        if( message.indexOf( "SERIAL:" ) > 0 )
                        {
                            m_Logger.info( "SP68 Spectro : Serial Number " + message.substring( message.indexOf( "SERIAL:" ) ) );

                            m_SerialNo = message.substring( message.indexOf( "SERIAL:" ) );
                        }
                    }
                    m_Logger.info( "SP68 Spectro : Response delimiter set" );
                    m_Incoming.removeNextObject(); //remove fifo;
                    m_Logger.info( "SP68 Spectro : Removing " + cmd.getName() + " from Incoming" );
                    return;
                }
                else if( cmd instanceof RetrieveReflectanceCommand )
                {
                    m_Logger.info( "SP68 RetrieveReflectanceCommand" );

                    if( m_LastLoaded <= m_Stored )
                    {
                        synchronized( m_Incoming )
                        {
                            SpectroCommand loadCmd = new LoadReflectanceCommand( new Integer( m_LastLoaded ) );
                            String loadMsg = loadCmd.construct() + "\r";

                            m_CommDriver.send( loadMsg.getBytes() );

                            m_Logger.info( "Removing " + ( (SpectroCommand) m_Incoming.removeNextObject() ).getName() );
                            m_Logger.info( "Loading reflectance at location " + m_LastLoaded );

                            try
                            {
                                m_Incoming.putObject( loadCmd );
                            }
                            catch( FifoFullException fifoFullEx )
                            {
                                m_Logger.info( "m_Incoming fifo queue full - Should not happen" );
                            }
                        }
                    }
                    else
                    {
                        evt.getStatus().addMessage( "NO STORED" );
                        m_Logger.info( "Removing " + ( (SpectroCommand) m_Incoming.removeNextObject() ).getName() );
                    }

                    /*
                    if( m_Incoming.isFull() )
                    {
                    m_Logger.info( "Command in incoming line..." );
                    m_Logger.info( "Removing " + ( (SpectroCommand) m_Incoming.getObject() ).getName() );
                    }
                    else if( m_Incoming.isEmpty() )
                    {
                    m_Logger.info( "No command waiting in line..." );
                    }
                    */

                    /*
                    SpectroReading reading = evt.getReading();
                    
                    m_Logger.info( "Setting reading settings..." );
                    m_Logger.info( "Settings : " + m_Settings );
                    m_Logger.info( "Settings Aperture     : " + m_Settings.getAperture() );
                    m_Logger.info( "Settings Light Filter : " + m_Settings.getLightFilter() );
                    m_Logger.info( "Settings Lens Position: " + m_Settings.getLensPosition() );
                    
                    m_Logger.info( "Setting aperture : " + m_Settings.getAperture().getName() );
                    reading.getSettings().setAperture( m_Settings.getAperture() );
                    
                    m_Logger.info( "Setting light filter : " + m_Settings.getLightFilter().getName() );
                    reading.getSettings().setLightFilter( m_Settings.getLightFilter() );
                    
                    m_Logger.info( "Settings lens position : " + m_Settings.getLensPosition().getName() );
                    reading.getSettings().setLensPosition( m_Settings.getLensPosition() );
                    
                    //m_Logger.info( "Reading aperture      : " + evt.getReading().getSettings().getAperture() );
                    //m_Logger.info( "Reading lightfilter   : " + evt.getReading().getSettings().getLightFilter() );
                    //m_Logger.info( "Reading lens position : " + evt.getReading().getSettings().getLensPosition() );
                    
                    notifyMeasured( new SpectroEvent( this, reading ) );
                    */

                    if( evt.getStatus().isSuccess() )
                    {
                        m_Settings.setSpecular( evt.getReading().getSettings().getSpecular() );
                        notifySettingsChanged( new SpectroEvent( this ) );

                        notifyMeasured( new SpectroEvent( this, new SP68Reading( evt.getStatus(), m_Settings, evt.getReading()
                            .getValues() ) ) );
                    }
                    else
                    {
                        notifyStatusChange( evt );
                    }

                    return;
                }
                else if( cmd instanceof RetrieveStoredNumberCommand )
                {
                    for( Iterator msgList = evt.getStatus().getMessages().iterator(); msgList.hasNext(); )
                    {
                        String msg = (String) msgList.next();

                        try
                        {
                            Pattern storedNumberPattern = Pattern.compile( ".*STORED\\sNUMBER\\s:\\s(\\d+).*" );

                            Matcher matcher = storedNumberPattern.matcher( msg );
                            if( matcher.find() )
                            {
                                try
                                {
                                    m_Stored = Integer.parseInt( matcher.group( 1 ) );
                                    m_Logger.info( "Noted stored : " + m_Stored );
                                }
                                catch( NumberFormatException numEx )
                                {
                                    m_Logger.info( "Error parsing stored number of reflectances; " + msg );
                                    m_Stored = 0;
                                }
                            }
                        }
                        catch( PatternSyntaxException patSynEx )
                        {
                            m_Logger.info( "Malformed Regex Pattern" );
                        }
                    }

                    if( m_Stored > 0 )
                    {
                        synchronized( m_Incoming )
                        {
                            m_LastLoaded = 1;
                            SpectroCommand loadCmd = new LoadReflectanceCommand( new Integer( m_LastLoaded ) );
                            String loadMessage = loadCmd.construct() + "\r";

                            m_CommDriver.send( loadMessage.getBytes() );

                            m_Incoming.removeNextObject(); //Clear previous command;

                            try
                            {
                                m_Incoming.putObject( loadCmd ); //Insert the load reflectance command
                            }
                            catch( FifoFullException fifoFullEx )
                            {
                                m_Logger.info( "Incoming fifo queue is full - Should not happend" );
                            }
                            return;
                        }
                    }
                    else
                    {
                        m_Logger.info( "Clearing " + cmd.getName() );
                        m_Incoming.removeNextObject(); //Clearing Command;
                        evt.getStatus().addMessage( "NO REFLECTANCES" );
                        notifyStatusChange( evt );
                        return;
                    }
                }
                else if( cmd instanceof LoadReflectanceCommand )
                {
                    for( Iterator msgList = evt.getStatus().getMessages().iterator(); msgList.hasNext(); )
                    {
                        m_Logger.info( msgList.next().toString() );
                    }

                    if( m_LastLoaded <= m_Stored )
                    {
                        synchronized( m_Incoming )
                        {
                            SpectroCommand retrieveCmd = new RetrieveReflectanceCommand( new Boolean( m_Settings.getSpecular() ) );

                            String retrieveMsg = retrieveCmd.construct() + "\r";
                            m_CommDriver.send( retrieveMsg.getBytes() );

                            m_Incoming.removeNextObject(); //Clear previous command;

                            try
                            {
                                m_Incoming.putObject( retrieveCmd );
                            }
                            catch( FifoFullException fifoFullEx )
                            {
                                m_Logger.info( "m_Incoming fifo queue full - Should not happend" );
                            }
                        }
                        m_LastLoaded++;
                        return;
                    }

                    m_Logger.info( "Clearing " + cmd.getName() );
                    m_Incoming.removeNextObject(); //Clearing Command
                    notifyStatusChange( evt );
                    return;
                }
                else
                {
                    m_Logger.info( "Clearing " + cmd.getName() );
                    m_Incoming.removeNextObject(); // Clearing Command
                    notifyStatusChange( evt );
                    return;
                }
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
        m_Logger.info( "SP68 Spectro : Guess Interpret" );

        try
        {
            Pattern labPattern = Pattern.compile( ".*[L].*[a].*[b].*" );

            Matcher labMatch = labPattern.matcher( message );

            if( labMatch.find() )
            {
                //Assume a measurement is made
                try
                {
                    m_Outgoing.putObject( new RetrieveReflectanceCommand( new Boolean( m_Settings.getSpecular() ) ) );
                }
                catch( FifoFullException fifoEx )
                {
                    m_Logger.info( "Fifo is full" );
                }
                return;
            }
            else
            {
                SpectroCommand cmd = new RetrieveReflectanceCommand( new Boolean( false ) );

                SpectroEvent evt = cmd.interpret( message.getBytes() );

                if( evt != null )
                {
                    m_Settings.setSpecular( evt.getReading().getSettings().getSpecular() );
                    notifySettingsChanged( new SpectroEvent( this ) );

                    notifyMeasured( new SpectroEvent( this, new SP68Reading( evt.getStatus(), m_Settings, evt.getReading()
                        .getValues() ) ) );

                    return;
                }
            }
        }
        catch( PatternSyntaxException synEx )
        {
            m_Logger.log( Level.FINER, "Incorrect Pattern Syntax", synEx );
        }
    }

    public boolean priorityCommand( SpectroCommand cmd )
    {
        if( !m_Incoming.isFull() )
        {
            m_Logger.info( "Sending priority command " + cmd.getName() );
            String mesg = cmd.construct() + "\r\n";

            m_CommDriver.send( mesg.getBytes() );

            try
            {
                m_Incoming.putObject( cmd );
                return true;
            }
            catch( FifoFullException fullEx )
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void promptSpecular()
    {
        String message = "Retrieve specular included reflectance? \n ( Selecting no would retrieve specular excluded reflectance )";
        String title = "Reflectance specular";
        int type = javax.swing.JOptionPane.YES_NO_OPTION;

        switch( javax.swing.JOptionPane.showConfirmDialog( null, message, title, type ) )
        {
        case javax.swing.JOptionPane.YES_OPTION:
            m_Settings.setSpecular( true );
            break;
        case javax.swing.JOptionPane.NO_OPTION:
            m_Settings.setSpecular( false );
            break;
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
        m_Logger.info( "SP68 Spectro : Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.info( "SP68 Spectro : Comm Driver Received" );

            interpret( new String( m_CommDriver.receive() ) );

            /*
                if( m_Received == null )
                {
                    m_Received = new StringBuffer();
                }
                
                //Append available data to the local buffer
                m_Received.append(new String( m_CommDriver.receive() ) );
            
                //Determine if the Terminator is present
                if( m_Received.toString().matches( ".*[\\r\\n]*<\\d\\d>.*" ) )
                {
                    //Message is complete.
                    //Interpret command now
                    String response = m_Received.toString();
                    m_Logger.info( "SP68 Spectro : " + response );
                                    
                    //Interpret response
                    interpret( response );               
            
                    m_Received = null;
                }
                else
                {
                    m_Logger.info( "SP68 Spectro : Buffer > " + m_Received );
                }
            */
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = new SP68Status();

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            m_Logger.info( "SP68 Spectro : Pre-empting " + cmd.getName() );

            m_Logger.info( "SP68 Spectro : Timeout received for " + cmd.getName() );
            m_Logger.info( "SP68 Spectro : Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.info( "SP68 Spectro : Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.info( "SP68 Spectro : Comm Driver Sent" );
        }
        else
        {

            //Should not happen : Unknown comm status event
            m_Logger.info( "SP68 Spectro : Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.info( "SP68 Spectro : Sent event from CommDriver" );
    }

    //===================== Notify Spectrolistener =============================

    /**
     * Notifies registered listeners of a measurement event
     *
     * @param evt The event to be sent or triggerred
     */
    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.info( "SP68 Spectro : Measurement received" );
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
        m_Logger.info( "SP68 Spectro : Calibration received" );
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
        m_Logger.info( "SP68 Spectro : Settings Ack received" );
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
        m_Logger.info( "SP68 Spectro : Status change " );
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
                m_Logger.info( "SP68 Spectro : Thread still running" );
                m_Logger.info( "SP68 Spectro : Retreiving Command from fifo" );

                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                if( cmd == null )
                {
                    continue;
                }

                m_Logger.info( "SP68 Spectro : Constructing command" );
                String message = cmd.construct();

                message = message + "\r";

                m_Logger.info( "SP68 Spectro : Message constucted > " + message );

                m_Logger.info( "SP68 Spectro : Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                m_Logger.info( "SP68 Spectro : Message posted." );
            }

            try
            {
                m_Thread.sleep( 1000 );
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
        m_Logger.info( "SP68 Spectro : Thread stopped." );
    }

    private synchronized void stopThread()
    {
        stopRequest = true;
        m_Thread.interrupt();
    }
}
