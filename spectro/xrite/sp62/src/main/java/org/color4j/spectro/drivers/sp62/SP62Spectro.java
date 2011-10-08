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

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * The SP62 Spectrophotometer:
 *
 * From the operational point of view, the SP62 runs on the the principal of
 * queueing outgoing commands and expecting incoming response for each command
 * sent to be interpreted and and passed up the chain via events.
 *
 * To this effect, the SP62Spectro has two First In First Out (FIFO) queues,
 * one for outgoing commands and one for incoming commands. There is a single
 * thread that runs continuously while the SP62Spectro is active. The thread is
 * responsible for taking a queued command from the outgoing FIFO and sending
 * it to the instrument via the CommDriver. Once, the command is sent, the
 * command is placed into the incoming FIFO queue. However if the incoming FIFO
 * is full, the thread will not send anymore commands until the incoming FIFO
 * becomes available for more elements. If the Outgoing FIFO becomes full due
 * to this blocking mechanism, the SP62Spectro will throw exceptions indicating
 * that it is busy until the Outgoing FIFO is available for more elements.
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
 * The other functions of the SP62Spectro are all event driven. Procedures are
 * activated via events sent from the CommDriver. Reception of data from the
 * CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the SP62Spectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to
 * listeners. Otherwise, a null is returned and the SP62Spectro would guess at
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
public class SP62Spectro
    implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger = Logger.getLogger( SP62Spectro.class.getName() );

    static
    {
    }

    /**
     * Constructs and initialize the spectrophotometer.
     */
    public static final String TERMINATOR;

    static
    {
        TERMINATOR = "\r\n"; //Termniator : "<lf><cr>"
    }

    protected CommDriver m_CommDriver;

    protected Fifo m_Incoming; // Expected incoming responses

    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the
    // instrument

    protected SpectroSettings m_newSettings; // The new settings for the
    // instrument

    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo; //Serial number of the instrument, none for
    // the SP62

    protected Vector m_Listeners; //Collection of Spectrolisteners listening
    // to this

    protected int m_OpStatus; //The operational status of the spectro
    // implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean m_IsRunning; // Switch for the running thread

    protected boolean m_IsRequestStopped = false;

    protected Thread m_Thread;

    /**
     * Instantiates and initializes the spectrophotometer. The constructor does
     * not open the CommDriver until the first setSettings containing the
     * CommParamaters are passed in.
     */
    public SP62Spectro()
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
            m_Outgoing.putObject( new MeasureCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
    }

    /**
     * will query the spectro for number of stored standards
     *
     * @throws SpectroException when the incoming queue is Blocked and no further commands
     *                          can be sent. The spectro is considerred as busy.
     */
    public void queryNoOfStoredStandards()
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new QueryNumberStandardsCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
    }

    /**
     * will retrieve a standard at a given position; should be used in conjunction with queryNoOfStoredStandards()
     *
     * @throws SpectroException when the incoming queue is Blocked and no further commands
     *                          can be sent. The spectro is considerred as busy.
     */
    public void retrieveStandard( int position )
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new RetrieveStandardReflectanceCommand( m_Settings.getSpecular(), position ) );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
    }

    public void queryNoOfStoredSamples()
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new QueryNumberSamplesCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
    }

    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new RetrieveSampleReflectanceCommand( m_Settings.getSpecular(), position ) );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
    }

    /**
     * will initiate a bulk retrieval of standards from the spectro
     */
    public void retrieveStandards()
        throws SpectroException
    {
        //TODO: KH Mar 12, 2004 - for bulk retrieval of standards in the spectro; no progress indication by UI
        //Not supported
//        throw new NotSupportedException("MSG_STANDARDS");
    }

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

    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS" );
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
        synchronized( this )
        {
            m_Logger.finer( "SP62 Spectro : Set settings called" );

            if( newSettings == null )
            {
                m_Logger.finer( "SP62 Spectro : Null settings enterred" );
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

                    //Opening using Default;
                    //m_CommDriver.openConnection( portname, 3000,
                    // Integer.parseInt( bitrate ) );
                    m_CommDriver.openConnection( portname, 3000, 9600 );

                    m_CommDriver.addCommDriverListener( this );

                    try
                    {
                        //Sent initialize signal
                        m_CommDriver.send( "\r\n".getBytes() );

                        //Set Baudrate
                        m_Outgoing.putObject(
                            new SetBaudRateCommand( new Integer( 9600 ) ) );

                        //Set Delimiter
                        m_Outgoing.putObject(
                            new SetResponseDelimiterCommand( "\r\n" ) );

                        //Set Read Switch
                        m_Outgoing.putObject(
                            new SetReadSwitchCommand( new Boolean( false ) ) );

                        //Set Averaging mode
                        m_Outgoing.putObject(
                            new SetAveragingCommand( new Integer( 1 ) ) );

                        //Get Serial Number
                        m_Outgoing.putObject( new GetSerialNumberCommand() );

                        //Set Read Switch
                        m_Outgoing.putObject(
                            new SetReadSwitchCommand( new Boolean( true ) ) );
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

                    m_IsRunning = true;

                    m_Thread.start();

//                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
//                    notifyStatusChange(new SpectroEvent(this));

                    m_Logger.finer(
                        "SP62 Spectro : Comm Settings complete... return" );
                    return;
                }
                catch( NumberFormatException numEx )
                {
                    //Try to recoved from exception and use a preset default
                    //bitrate
                    m_Logger.finer( "Invalid bitrate provided " );
                    m_Logger.finer( "Using default bitrate of 9600" );

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
                        m_Logger.finer(
                            "SP62 Spectro : FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status =
                            SP62Status.create( "EEEEEEEEEEEEEEEEEEEE" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.finer(
                            "SP62 Spectro : Comm Settings complete... return" );
                        return;
                    }

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.finer( "SP62 Spectro : FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = new SP62Status();
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.finer(
                        "SP62 Spectro : Unable to open port... return" );
                    return;
                }

                m_Logger.finer(
                    "SP62 Spectro : Should not reach this return in set settings" );
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
                m_Logger.finer(
                    "SP62 Spectro : Setting Specular "
                    + ( newSettings.getSpecular() ? "Included" : "Excluded" ) );
                m_newSettings.setSpecular( newSettings.getSpecular() );

                try
                {
                    m_Outgoing.putObject(
                        new SetSpecularCommand(
                            new Boolean( m_newSettings.getSpecular() ) ) );
                }
                catch( FifoFullException fullEx )
                {
                    m_Logger.finer(
                        "SP62 Spectro : Wait for fifo to be cleared." );
                }
            }

            if( m_Settings.getAperture() != newSettings.getAperture() )
            {
                m_Logger.finer(
                    "SP62 Spectro : Setting " + newSettings.getAperture() );
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

            m_Logger.finer( "SP62 Spectro : Set setings done" );
        }
    }

    /**
     * Returns the Serial number of the instrument. Not supported by SP62
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

        m_Outgoing = new Fifo( 10 );
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
        m_Logger.finer( "SP62 Spectro : Dispose called" );

        stopThread();

        while( m_IsRunning )
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

    public void setCalibrationDataFiles( URI[] fileURIs )
    {
        //TODO: KH Mar 24, 2004 - used to say TO DO... must implement
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
        m_Logger.finer( "SP62 Spectro : Interpreting command" );
        m_Logger.finer( "SP62 Spectro : " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            // KH Mar 10, 2004 - wait here for FIFOin to put object
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.finer( "SP62 Spectro : Interpreting > " + message );
            m_Logger.finer( "SP62 Spectro : Interpreting as a " + cmd.getName() + " command" );

            /*
             * Get the command to interpret the message according to its
             * expected data format 
             */
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            /*
             * If a spectroevent is returned then it is assumed that the
             * interpretation is complete without error
             */
            if( evt != null )
            {
                setSpectroIdle();
//                m_Incoming.removeNextObject(); 
                if( m_Incoming.isFull() )
                {
                    m_Logger.finer( "Command in incoming line..." );
                    m_Logger.finer( "Removing " + ( (SpectroCommand) m_Incoming.removeNextObject() ).getName() );
                }
                else if( m_Incoming.isEmpty() )
                {
                    m_Logger.severe( "No command waiting in line... should not be" );
                }

                //Decide which listener method to notify:
                /*KH Mar 10, 2004 - TODO
                 * this series of if else statements mainly prints logs 
                 * UNLESS some SpectroListener method needs to be called or a new command is to be placed onto the FIFO 
                 */
                if( cmd instanceof MeasureCommand )
                {
                    processMeasureCommand();
                }
                else if( cmd instanceof SetSpecularCommand )
                {
                    processSetSpecularCommand();
                    m_Settings.setSpecular( ( (SetSpecularCommand) cmd ).isIncluded() );
                    notifySettingsChanged( evt );
                }
                else if( cmd instanceof RetrieveMeasuredReflectanceCommand )
                {
                    processRetrieveReflectanceCommand( evt );
                    notifyMeasured( evt );
                }
                else if( cmd instanceof RetrieveStandardReflectanceCommand )
                {
                    processRetrieveReflectanceCommand( evt );
                    notifyStandardRetrieved( evt );
                }
                else if( cmd instanceof QueryNumberStandardsCommand )
                {
                    notifyFoundStandardNumber( (int[]) evt.getEventResult() );
                }
                else if( cmd instanceof QueryNumberSamplesCommand )
                {
                    notifyFoundSampleNumber( (int[]) evt.getEventResult() );
                }
                else if( cmd instanceof RetrieveSampleReflectanceCommand )
                {
                    processRetrieveReflectanceCommand( evt );
                    notifySampleRetrieved( evt );
                }
                else if( cmd instanceof SetAveragingCommand )
                {
                    processAveragingCommand( evt.getStatus().getMessages() );
                }
                else if( cmd instanceof SetBaudRateCommand )
                {
                    processSetBaudRateCommand( evt.getStatus().getMessages() );
                }
                else if( cmd instanceof SetDataDelimiterCommand )
                {
                    processSetDataDelimiterCommand( evt.getStatus().getMessages() );
                }
                else if( cmd instanceof SetReadSwitchCommand )
                {
                    processSetReadSwitchCommand( evt.getStatus().getMessages() );
                }
                else if( cmd instanceof SetResponseDelimiterCommand )
                {
                    processSetResponseDelimiterCommand( evt.getStatus().getMessages() );
                }
                else if( cmd instanceof GetSerialNumberCommand )
                {
                    processGetSerialNumberCommand( evt.getStatus().getMessages() );
                }
                else if( cmd instanceof SetModeCommand )
                {
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.severe( "Unknown command" );
                }
            }
            else
            {
                //Unknown by the waiting command
//                guessInterpret(message);
                m_Logger.severe( "Unknown command" );
            }
        }
        else
        {
            m_Logger.severe( "Unknown command" );//guessInterpret(message);
        }
    }

    /*
    *   KH Mar 10, 2004 - sends new command to spectro for reflectance retrieval
    */
    private void processMeasureCommand()
    {
        m_Logger.finer( "SP62 Spectro : Measure command " );

//        promptSpecular();

        RetrieveMeasuredReflectanceCommand command = new RetrieveMeasuredReflectanceCommand( m_Settings.getSpecular() );
        String msg = command.construct() + "\r\n";

        m_CommDriver.send( msg.getBytes() );

        try
        {
            m_Incoming.putObject( command );
        }
        catch( FifoFullException ex )
        {
            m_Logger.log( Level.SEVERE, "Fifo should not be full cause we just emptied it", ex );
        }
    }

    /*
    * will set the aperture, lightfilter, and lens position for the spectro event
    */
    private void processRetrieveReflectanceCommand( SpectroEvent evt )
    {
        m_Logger.finer( "SP62 RetrieveReflectanceCommand" );

//        if (m_Incoming.isFull())
//        {
//            m_Logger.finer("Command in incoming line...");
//            m_Logger.finer( "Removing " + ((SpectroCommand)m_Incoming.removeNextObject()).getName());
//        }
//        else if (m_Incoming.isEmpty())
//        {
//            m_Logger.error("No command waiting in line... should not be");
//        }

        m_Logger.finer( "Setting reading settings..." );
        m_Logger.finer( "Settings : " + m_Settings );
        m_Logger.finer( "Settings Aperture     : " + m_Settings.getAperture() );
        m_Logger.finer( "Settings Light Filter : " + m_Settings.getLightFilter() );
        m_Logger.finer( "Settings Lens Position: " + m_Settings.getLensPosition() );
        m_Logger.finer( "Setting aperture : " + m_Settings.getAperture().getName() );
        m_Logger.finer( "Setting light filter : " + m_Settings.getLightFilter().getName() );
        m_Logger.finer( "Settings lens position : " + m_Settings.getLensPosition().getName() );

        // KH Mar 10, 2004 - main meat of this code
        evt.getReading().getSettings().setAperture( m_Settings.getAperture() );
        evt.getReading().getSettings().setLightFilter( m_Settings.getLightFilter() );
        evt.getReading().getSettings().setLensPosition( m_Settings.getLensPosition() );
    }

    private void processGetSerialNumberCommand( Collection collection )
    {
        Iterator messages = collection.iterator();
        String message;
        while( messages.hasNext() )
        {
            message = (String) messages.next();

            if( message.indexOf( "SERIAL:" ) > 0 )
            {
                m_Logger.finer(
                    "SP62 Spectro : Serial Number "
                    + message.substring(
                        message.indexOf( "SERIAL:" ) ) );

                m_SerialNo =
                    message.substring( message.indexOf( "SERIAL:" ) );
            }
        }
    }

    private void processSetResponseDelimiterCommand( Collection collection )
    {
        Iterator messages = collection.iterator();
        String message;
        while( messages.hasNext() )
        {
            message = (String) messages.next();

            if( message.indexOf( "DELIMITER:" ) > 0 )
            {
                m_Logger.finer(
                    "SP62 Spectro : Response delimiter set to "
                    + message.substring(
                        message.indexOf( "DELIMITER:" ) ) );
            }
        }
    }

    private void processSetReadSwitchCommand( Collection collection )
    {
        Iterator messages = collection.iterator();
        String message;
        while( messages.hasNext() )
        {
            message = (String) messages.next();

            if( message.indexOf( "READSWITCH:" ) > 0 )
            {
                m_Logger.finer(
                    "SP62 Spectro : Readswitch set to "
                    + message.substring(
                        message.indexOf( "READSWITCH:" ) ) );
            }
        }
        m_OpStatus = OPERATIONAL_STATUS_IDLE;
        notifyStatusChange( new SpectroEvent( this ) );
    }

    private void setSpectroIdle()
    {
        //Stop timeout timer
        m_CommDriver.cancelRespondTimeout();
        m_Logger.finer( "SP62 Spectro : Cancelling timeout at " + System.currentTimeMillis() );
        m_OpStatus = OPERATIONAL_STATUS_IDLE;
        notifyStatusChange( new SpectroEvent( this ) );
    }

    private void processSetDataDelimiterCommand( Collection collection )
    {
        Iterator messages = collection.iterator();
        String message;
        while( messages.hasNext() )
        {
            message = (String) messages.next();

            if( message.indexOf( "DELIMITER:" ) > 0 )
            {
                m_Logger.finer(
                    "SP62 Spectro : Data delimiter set to "
                    + message.substring(
                        message.indexOf( "DELIMITER:" ) ) );
            }
        }
    }

    private void processSetBaudRateCommand( Collection collection )
    {
        Iterator messages = collection.iterator();
        String message;
        while( messages.hasNext() )
        {
            message = (String) messages.next();

            if( message.indexOf( "BAUDRATE:" ) > 0 )
            {
                m_Logger.finer(
                    "SP62 Spectro : Baud rate set to "
                    + message.substring(
                        message.indexOf( "BAUDRATE:" ) ) );
            }
        }
    }

    private void processAveragingCommand( Collection col )
    {
        Iterator messages = col.iterator();

        String message;
        while( messages.hasNext() )
        {
            message = (String) messages.next();

            if( message.indexOf( "AVERAGE:" ) > 0 )
            {
                m_Logger.finer(
                    "SP62 Spectro : Averaging number : "
                    + message.substring(
                        message.indexOf( "AVERAGE:" ) ) );
            }
        }
    }

    private void processSetSpecularCommand()
    {
        m_Logger.finer( "SP62 Spectro : Settings command " );
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
    private void guessInterpret( String message )
    {
        m_Logger.finer( "SP62 Spectro : Guess Interpret" );

        m_Logger.finer( "Assuming to be a measurement" );

        //Assume it's an auto transmit measurement

        promptSpecular();

        if( priorityCommand( new RetrieveMeasuredReflectanceCommand( m_Settings
                                                                         .getSpecular() ) ) )
        {

            if( m_Incoming.isFull() )
            {
                m_Logger.finer( "Awaiting incoming messages to be cleard" );
            }
            else
            {
                m_Logger.finer( "No incoming messages" );
                m_Logger.finer(
                    "Fifo head : "
                    + ( (SpectroCommand) m_Outgoing.getNextObject() ).getName() );
            }
        }

        /*
         * StringTokenizer sTok = new StringTokenizer( message, "\r\n" );
         * 
         * if( sTok.countTokens() == 1 ) { //It's some status from an
         * unregistered command SP62Status status = SP62Status.create(
         * sTok.nextToken() );
         * 
         * notifyStatusChange( new SpectroEvent( this, status ) ); return; }
         * else if( sTok.countTokens() > 1 ) { sTok.nextToken();
         * 
         * if( sTok.nextToken().startsWith( "XYZ" ) ) { m_Logger.finer(
         * "Assuming to be a measurement" );
         * 
         * //Assume it's an auto transmit measurement
         * 
         * promptSpecular();
         * 
         * if( priorityCommand( new RetrieveMeasuredReflectanceCommand(
         * m_Settings.getSpecular() ) ) ) {
         * 
         * if( m_Incoming.isFull() ) { m_Logger.finer( "Awaiting incoming
         * messages to be cleard" ); } else { m_Logger.finer( "No incoming
         * messages" ); m_Logger.finer( "Fifo head : " + ( (SpectroCommand)
         * m_Outgoing.peakFifo() ).getName() ); } } }
         * 
         * return;
         * 
         * 
         * MeasureCommand cmd = new MeasureCommand();
         * 
         * SpectroEvent evt = cmd.interpret( message.getBytes() );
         * 
         * if( evt == null ) { //Give up m_Logger.log( Level.WARN, "Unknown
         * response from SP62 : " + message ); return; } else { notifyMeasured(
         * evt ); return; } } else { //Give up m_Logger.log( Level.WARN,
         * "Unknown response from SP62 : " + message ); return; }
         */
    }

    public boolean priorityCommand( SpectroCommand cmd )
    {
        if( !m_Incoming.isFull() )
        {
            m_Logger.finer( "Sending priority command " + cmd.getName() );
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

    private void promptSpecular()
    {
        String message =
            "Retrieve specular included reflectance? \n ( Selecting no would retrieve specular excluded reflectance )";
        String title = "Reflectance specular";
        int type = javax.swing.JOptionPane.YES_NO_OPTION;

        switch( javax
            .swing
            .JOptionPane
            .showConfirmDialog( null, message, title, type ) )
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
        m_Logger.finer( "SP62 Spectro : Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.finer( "SP62 Spectro : Comm Driver Received" );

            interpret( new String( m_CommDriver.receive() ) );

            /*
             * if( m_Received == null ) { m_Received = new StringBuffer(); }
             * 
             * //Append available data to the local buffer
             * m_Received.append(new String( m_CommDriver.receive() ) );
             * 
             * //Determine if the Terminator is present [\\r\\n]* <\\d\\d>.*" ) ) {
             * //Message is complete. //Interpret command now String response =
             * m_Received.toString(); m_Logger.finer( "SP62 Spectro : " +
             * response );
             * 
             * //Interpret response interpret( response );
             * 
             * m_Received = null; } else { m_Logger.finer( "SP62 Spectro :
             * Buffer > " + m_Received ); }
             */
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = new SP62Status();

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO 
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            m_Logger.finer( "SP62 Spectro : Pre-empting " + cmd.getName() );

            m_Logger.finer(
                "SP62 Spectro : Timeout received for " + cmd.getName() );
            m_Logger.finer(
                "SP62 Spectro : Timeout received at "
                + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.finer( "SP62 Spectro : Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.finer( "SP62 Spectro : Comm Driver Sent" );
        }
        else
        {

            //Should not happen : Unknown comm status event
            m_Logger.finer( "SP62 Spectro : Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.finer( "SP62 Spectro : Sent event from CommDriver" );
    }

    //===================== Notify Spectrolistener
    // =============================

    private void notifyFoundStandardNumber( int[] indices )
    {
        m_Logger.finer( "SP62 Spectro : Number of Standards received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).numberStandardsFound( indices );
        }
    }

    /**
     * Notifies registered listeners of a measurement event
     *
     * @param evt The event to be sent or triggerred
     */
    private void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.finer( "SP62 Spectro : Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    private void notifyStandardRetrieved( SpectroEvent evt )
    {
        m_Logger.finer( "SP62 Spectro : Standard received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).retrievedStandard( evt );
        }
    }

    private void notifySampleRetrieved( SpectroEvent evt )
    {
        m_Logger.finer( "SP62 Spectro : Sample received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).retrievedSample( evt );
        }
    }

    private void notifyFoundSampleNumber( int[] indices )
    {
        m_Logger.finer( "SP62 Spectro : Number of Samples received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).numberSamplesFound( indices );
        }
    }

    /**
     * Notifies registered listeners of setting changes
     *
     * @param evt The event to be triggered
     */
    private void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.finer( "SP62 Spectro : Settings Ack received" );
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
    private void notifyStatusChange( SpectroEvent evt )
    {
        m_Logger.finer( "SP62 Spectro : Status change " );
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

    //========================== Thread method
    // =================================

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
        while( m_IsRunning )
        {
            if( m_IsRequestStopped )
            {
                m_IsRunning = false;
                break;
            }

            //If the incoming FIFO is not full, send first command in outgoing
            // queue
            if( !m_Incoming.isFull() )
            {
                m_Logger.finer( "SP62 Spectro : Thread still running" );
                m_Logger.finer( "SP62 Spectro : Retreiving Command from fifo" );

                /*
                 *  KH Mar 10, 2004 - calls wait on this thread when empty.  sits here until something elsewhere puts something in the fifo bufffer
                 * putting something in the buffer will call notifyAll() and continue the thread action
                 */
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                if( cmd == null )
                {
                    continue;
                }

                m_Logger.finer( "SP62 Spectro : Constructing command" );
                String message = cmd.construct();

                message = message + "\r\n";

                m_Logger.finer( "SP62 Spectro : Message constucted > " + message );

                m_Logger.finer(
                    "SP62 Spectro : Sending "
                    + cmd.getName()
                    + " at "
                    + System.currentTimeMillis() );
                // KH Mar 10, 2004 - sends a command to the spectro; triggers a serialevent in m_CommDriver                        
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

                // KH Mar 10, 2004 - command is sent, removing object from fifo and will make the thread wait in the next iteration if empty
                m_Outgoing.removeNextObject();

                m_Logger.finer( "SP62 Spectro : Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                m_Logger.finer( "SP62 Spectro : Spectro busy." );
            }

            try
            {
                m_Thread.sleep( 1000 );
            }
            catch( InterruptedException irEx )
            {
                if( m_IsRequestStopped )
                {
                    m_IsRunning = false;
                    break;
                }
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.finer( "SP62 Spectro : Thread stopped." );
    }

    private synchronized void stopThread()
    {
        m_IsRequestStopped = true;
        m_Thread.interrupt();
    }
}
