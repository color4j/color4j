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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.hunter.common.DataControlMessage;
import org.color4j.spectro.hunter.common.HunterCommDriver;
import org.color4j.spectro.hunter.common.ResponseMessage;
import org.color4j.spectro.spi.LensPosition;
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

/**
 * Spectrophotometer object that supports
 * the functionality of manually entering
 * reflectance data.
 *
 * The LSXE Spectrophotometer:
 *
 * From the operational point of view, the LSXE runs on the
 * the principal of queueing outgoing commands and expecting
 * incoming response for each command sent to be interpreted and
 * and passed up the chain via events.
 *
 * To this effect, the CQXESpectro has two First In First Out (FIFO)
 * queues, one for outgoing commands and one for incoming commands.
 * There is a single thread that runs continuously while the CQXESpectro
 * is active. The thread is responsible for taking a queued command from the
 * outgoing FIFO and sending it to the instrument via the CommDriver. Once,
 * the command is sent, the command is placed into the incoming FIFO queue.
 * However if the incoming FIFO is full, the thread will not send anymore
 * commands until the incoming FIFO becomes available for more elements.
 * If the Outgoing FIFO becomes full due to this blocking mechanism,
 * the CQXESpectro will throw exceptions indicating that it is busy until
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
 * The other functions of the CQXESpectro are all event driven. Procedures
 * are activated via events sent from the CommDriver. Reception of data from
 * the CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the CQXESpectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to listeners.
 * Otherwise, a null is returned and the CQXESpectro would guess at interpreting
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
public class CQXESpectro implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger;
    static int instances;

    static
    {
        m_Logger = Logger.getLogger( CQXESpectro.class.getName() );
    }

    /**
     * Constructs and initialize the spectrophotometer.
     */

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the instrument
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected int m_CurrentPortPlate = 1000;

    protected String m_SerialNo; //Serial number of the instrument, none for the LSXE

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this

    protected int m_OpStatus; //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean whiteCalibration; //Indicate that calibration is in progress
    protected boolean specularState; //State before calibration is performed
    protected boolean calibrate; //Calibration flag

    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false; //Stop signal

    protected int m_ID;
    protected Thread m_Thread;

    /**
     * Instantiates and initializes the spectrophotometer. The constructor does
     * not open the CommDriver until the first setSettings containing the CommParamaters
     * are passed in.
     */
    public CQXESpectro()
    {
        m_ID = instances++;

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
            m_Outgoing.putObject( new MeasureCommand() );

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
            if( step > 2 )
            {
                throw new SpectroException( "MSG_UNKNOWN_CALIBRATION" );
            }

            m_Outgoing.putObject( new CalibrateCommand( new Integer( step ), new Integer( m_CurrentPortPlate ) ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "MSG_SPECTRO_BUSY" );
        }
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
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Set settings called" );

            if( newSettings == null )
            {
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Null settings enterred" );
                return;
            }

            //If comm driver is not initialized
            //Assume this is the first initial settings
            //And opens the connection with the new Comm Parameters
            if( m_CommDriver == null )
            {
                Map commParameters = newSettings.getCommParameters();

                m_CommDriver = new HunterCommDriver();
                try
                {
                    String portname = (String) commParameters.get( "PORTNAME" );
                    String bitrate = (String) commParameters.get( "BITRATE" );

                    m_Logger.info( Thread.currentThread().getName() + " " + m_ID + "Opening : " + portname );
                    m_Logger.info( Thread.currentThread().getName() + " " + m_ID + "Baudrate: " + bitrate );

                    m_CommDriver.openConnection( portname, 3000, 9600 ); //Integer.parseInt( bitrate ) );

                    m_CommDriver.addCommDriverListener( this );

                    running = true;

                    m_Settings = new SpectroSettings();

                    //DEBUG
                    m_Settings.setAperture( new LargeAperture() );
                    m_Settings.setLightFilter( new UVIncludedLightFilter() );
                    m_Settings.setLensPosition( new LargeAreaView() );
                    //DEBUG

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Comm Settings complete... return" );
                    return;
                }
                catch( NumberFormatException numEx )
                {
                    //Try to recoved from exception and use a preset default
                    //bitrate
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
                        m_Logger.warning( Thread.currentThread()
                                              .getName() + " " + m_ID + "FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = CQXEStatus.create( "EEEEEEEEEEEEEEEEEEEE" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.finer( Thread.currentThread()
                                            .getName() + " " + m_ID + "Comm Settings complete... return" );
                        return;
                    }

                    m_Settings = new SpectroSettings();

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.warning( Thread.currentThread().getName() + " " + m_ID + "FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = CQXEStatus.create( "EEEEEEEEEEEEEEEEEEEE" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Unable to open port... return" );
                    return;
                }

                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Should not reach this return in set settings" );
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

            char uvSettings = 'F';
            String apertureSettings = "1000";

            //Compare Aperture setting:
            if( newSettings.getLensPosition() != null )
            {
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Setting " + newSettings.getLensPosition().getName() );
                if( m_Settings.getLensPosition() == null || !m_Settings.getLensPosition()
                    .getName()
                    .equals( newSettings.getLensPosition().getName() ) )
                {
                    if( m_Settings.getLensPosition() != null )
                    {
                        m_Logger.finer( Thread.currentThread()
                                            .getName() + " " + m_ID + "Current Lens Position " + m_Settings.getLensPosition()
                            .getName() );
                        m_Logger.finer( Thread.currentThread()
                                            .getName() + " " + m_ID + "New Lens Position " + newSettings.getLensPosition()
                            .getName() );
                    }
                    else
                    {
                        m_Logger.finer( Thread.currentThread()
                                            .getName() + " " + m_ID + "New Lens Position " + newSettings.getLensPosition()
                            .getName() );
                    }

                    m_newSettings.setLensPosition( newSettings.getLensPosition() );

                    LensPosition newLensPosition = m_newSettings.getLensPosition();

                    if( newLensPosition instanceof LargeAreaView )
                    {
                        m_newSettings.setAperture( new LargeAperture() );
                        m_CurrentPortPlate = 1000;
                    }
                    else if( newLensPosition instanceof UltraSmallAreaView )
                    {
                        m_newSettings.setAperture( new UltraSmallAperture() );
                        m_CurrentPortPlate = 375;
                    }
                    else
                    {
                        m_CurrentPortPlate = 1000;
                    }
                }
                else
                {
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Settings unchanged" );
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Current Settings " + m_Settings.getLensPosition()
                        .getName() );
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "New     Settings " + newSettings.getLensPosition()
                        .getName() );
                }
            }

            if( newSettings.getLightFilter() != null )
            {
                if( newSettings.getLightFilter() instanceof UVIncludedLightFilter )
                {
                    m_newSettings.setLightFilter( new UVIncludedLightFilter() );
                    uvSettings = StandardizeModeCommand.UV_NOMINAL;
                }
                else if( newSettings.getLightFilter() instanceof CutOff420LightFilter )
                {
                    m_newSettings.setLightFilter( new CutOff420LightFilter() );
                    uvSettings = StandardizeModeCommand.UV_EXCLUDED;
                }
                else if( newSettings.getLightFilter() instanceof CutOff460LightFilter )
                {
                    m_newSettings.setLightFilter( new CutOff460LightFilter() );
                    uvSettings = StandardizeModeCommand.UV_EXCLUDED;
                }
            }
            else
            {
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + " Settings unchanged" );
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + " Current Settings : " + m_Settings.getLightFilter()
                    .getName() );
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + " New     Settings : " + newSettings.getLightFilter()
                    .getName() );
            }

            //added by lijen
            if( m_Settings == null || m_Settings.getSpecular() != newSettings.getSpecular() )
            {
                m_Logger.finer( "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY" );
                m_newSettings.setSpecular( newSettings.getSpecular() );
                //	lijen

                try
                {

                    m_Outgoing.putObject( new StandardizeModeCommand( uvSettings, String.valueOf( m_CurrentPortPlate ), m_newSettings
                        .getSpecular() ) );
                }
                catch( FifoFullException fullEx )
                {
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Wait for fifo to be cleared." );
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Command " + ( (SpectroCommand) m_Outgoing.getNextObject() )
                        .getName() );
                }
            } //lijen

            /*
                   //Compare Light Filter settings:
                   if( newSettings.getLightFilter() != null )
                   {
                       m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Setting " + newSettings.getLightFilter().getName() );
                       if( m_Settings.getLightFilter() == null || !m_Settings.getLightFilter().getName().equals( newSettings.getLightFilter().getName() ) )
                       {
                           m_newSettings.setLightFilter( newSettings.getLightFilter() );

                           try
                           {
                               m_Outgoing.putObject( new SetUVFilterCommand( m_newSettings.getLightFilter() ) );
                           }
                           catch( FifoFullException fullEx )
                           {
                               m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Wait for fifo to be cleared." );
                           }
                       }
                       else
                       {
                           m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Settings unchanged" );
                           m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Current Settings " + m_Settings.getLightFilter().getName() );
                           m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "New     Settings " + newSettings.getLightFilter().getName() );
                       }
                   }
                */

            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Settings called : " );
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Aperture : " + newSettings.getAperture() );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Lens     : " + newSettings.getLensPosition() );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "LightFil : " + newSettings.getLightFilter() );
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Specular : " + newSettings.getSpecular() );
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Set setings done" );
        }
    }

    public void retrieveStoredSamples()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    /*  Not supported by the LSXE Spectros
      *
      *  @throws NotSupportectExcetpion This is not supported by the LSXE Spectros
      */
    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    /*  Not supported by the LSXE Spectros
      *
      *  @throws NotSupportectExcetpion This is not supported by the LSXE Spectros
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

    /*  Not supported by the LSXE Spectros
      *
      *  @throws NotSupportectExcetpion This is not supported by the LSXE Spectros
      */
    public void retrieveStandard( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*  Not supported by the LSXE Spectros
      *
      *  @throws NotSupportectExcetpion This is not supported by the LSXE Spectros
      */
    public void queryNoOfStoredSamples()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    /*  Not supported by the LSXE Spectros
      *
      *  @throws NotSupportectExcetpion This is not supported by the LSXE Spectros
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
     * Returns the Serial number of the instrument. Not supported by LSXE Spectros
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
        m_Thread.setName( "CQXE Thread " + m_ID );
        m_Logger.finer( "Assigning : " + m_Thread.getName() );

        try
        {
            m_Outgoing.putObject( new CapabilityCommand( CapabilityCommand.SERIAL_NUMBER ) );
        }
        catch( FifoFullException e )
        {
            m_Logger.log( Level.SEVERE, e.getMessage(), e );
        }

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
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Dispose called" );

        stopThread();

        while( running )
        {
            //Wait for loop to stop;
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
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Thread still running" );
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
                m_Logger.info( Thread.currentThread().getName() + " " + m_ID + " FAILURE TO CLOSE CONNECTION" );
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
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Interpreting command" );
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + message );

        ResponseMessage response = new ResponseMessage( message );

        //Validate the check sum returned with the response
        if( !response.verifyChecksum() )
        {
            m_Logger.warning( Thread.currentThread().getName() + " " + m_ID + "Checksum Error" );

            SpectroStatus status = CQXEStatus.create( "E" );
            status.addMessage( "MSG_CHECKSUM_ERROR" );

            //Notify Checksum Error
            notifyStatusChange( new SpectroEvent( this, status ) );
            return;
        }
        /*
              else
              {
                  //Checksum validation passed.
                  //Remove checksum section from the response
                  m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Message without Checksum " + message );
              }
           */

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Interpreting > " + message );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( response.getParameter().getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + cmd.getName() );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Removing " + cmd.getName() + " from Incoming" );
                    ( (CQXEReading) evt.getReading() ).setSettings( m_Settings );

                    //DEBUG
                    if( evt.getReading().getSettings() != null )
                    {
                        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Settings set to reading" );
                    }
                    else
                    {
                        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Settings is null" );
                    }

                    notifyMeasured( evt );
                    return;
                }
                else if( cmd instanceof CalibrateCommand )
                {
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + cmd.getName() );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Removing " + cmd.getName() + " from Incoming" );
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Notifying Calibration" );
                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof StandardizeModeCommand )
                {
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + cmd.getName() );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Removing " + cmd.getName() + " from Incoming" );
                    m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Notifying Settings Change" );
                    m_Settings = m_newSettings;
                    notifySettingsChanged( evt );
                    return;
                }
                else if( cmd instanceof CapabilityCommand )
                {
                    m_Logger.info( cmd.getName() );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( Thread.currentThread()
                                        .getName() + " " + m_ID + "Removing " + cmd.getName() + " from Incoming" );

                    Iterator msgList = evt.getStatus().getMessages().iterator();
                    while( msgList.hasNext() )
                    {
                        String msg = (String) msgList.next();
                        if( msg.startsWith( "SERIAL:" ) )
                        {
                            m_SerialNo = msg.substring( msg.indexOf( ':' ) + 1 );
                        }
                    }
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
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "No command in incoming" );
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
        //For LSXE... there are no auto transmits...
        //shouldn't happen.
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Guess Interpret" );

        switch( message.charAt( 0 ) )
        {
        case 'M':
            switch( message.charAt( 1 ) )
            {
            case '0':
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Received Motor Speed Profile response" );
                break;
            case '1':
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Motor Control response" );
                break;
            case '3':
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Received Adjustable End-User Positions response" );
                break;
            case '4':
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Disable Device response" );
                break;
            default:
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Unknown Response" );
            }
            break;
        case 'Q':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Flash Lamp Settings response" );
            break;
        case 'V':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received UV Control Parameters response" );
            break;
        case 'A':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Sensor Parameters response" );
            break;
        case 'P':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Port Plate Settings response" );
            break;
        case 'T':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Production Self Test response" );
            break;
        case 'F':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Select Standardization Mode" );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Error : Should have been interpreted by command" );
            break;
        case 'W':
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Received Instrument Tile Values response" );
            break;
        case 'I':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Standardize response" );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Error : Should have been interpreted by command" );
            break;
        case 'H':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Recevied Photometric Data response" );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Error : Should have been interpreted by command" );
            break;
        case 'G':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Read Ganz Whiteness response" );
            break;
        case 'U':
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Received UV Calibration Procedure response" );
            break;
        case 'Z':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Error Codes response" );
            break;
        case 'X':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Control Status response" );
            break;
        case 'D':
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Received Calibrate Devices or Status of Sensor response" );
            break;
        case 'K':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Macro Button response" );
            break;
        case 'Y':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Serial Number response" );
            break;
        case '#':
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received Capability response" );
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
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Received : " + m_Received + " (" + m_Received.length() + ")" );

            for( int i = 0; i < m_Received.length(); i++ )
            {
                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Recv : " + (int) m_Received.charAt( i ) );
            }

            //Parse received message
            ResponseMessage response = new ResponseMessage( m_Received.toString() );

            //Determine if the message is complete
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Received : " + m_Received );
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Response : " + response.toString() );

            //Message is complete.
            //Interpret command now

            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + response );

            //Interpret response
            interpret( response.toString() );

            m_Received = new StringBuffer();
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = CQXEStatus.create( "EEEEEEEEEEEEEEEEEEEE" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Pre-empting " + cmd.getName() );

            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Timeout received for " + cmd.getName() );
            m_Logger.finer( Thread.currentThread()
                                .getName() + " " + m_ID + "Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "LSXE Spectro : Comm Driver Sent" );
        }
        else
        {
            //Should not happen : Unknown comm status event
            m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Sent event from CommDriver" );
    }

    //===================== Notify Spectrolistener =============================

    /**
     * Notifies registered listeners of a measurement event
     *
     * @param evt The event to be sent or triggerred
     */
    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Measurement received" );
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
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Calibration received" );
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
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Settings Ack received" );
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
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Status change " );
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
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Thread still running" );
                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                if( cmd == null )
                {
                    continue;
                }

                m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Constructing command" );

                DataControlMessage message = new DataControlMessage( 1, cmd.construct() );

                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Message constucted > " + message + " (" + message.toString()
                    .length() + ")" );

                m_Logger.finer( Thread.currentThread()
                                    .getName() + " " + m_ID + "Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
                m_CommDriver.send( message.toString().getBytes() );

                m_CommDriver.setRespondTimeout( 60000 );

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

                try
                {
                    Thread.sleep( 300 );
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
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //m_Logger( "LSXE Spectro : Spectro busy." );
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.finer( Thread.currentThread().getName() + " " + m_ID + "Thread stopped." );
    }

    private synchronized void stopThread()
    {
        stopRequest = true;
        m_Thread.interrupt();
    }

    public void setCalibrationDataFiles( java.net.URI[] datafiles )
    {
    }
}