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

package org.color4j.spectro.gretagmacbeth.ce2180;

import java.net.URI;
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
 * Spectrophotometer object that supports
 * the functionality of measuring reflectance data
 * with a Gretag-MacBeth Color Eye 2180.
 *
 * The CE2180 Spectrophotometer:
 *
 * From the operational point of view, the CE2180 runs on the
 * the principal of queueing outgoing commands and expecting
 * incoming response for each command sent to be interpreted and
 * and passed up the chain via events.
 *
 * To this effect, the CE2180Spectro has two First In First Out (FIFO)
 * queues, one for outgoing commands and one for incoming commands.
 * There is a single thread that runs continuously while the CE2180Spectro
 * is active. The thread is responsible for taking a queued command from the
 * outgoing FIFO and sending it to the instrument via the CommDriver. Once,
 * the command is sent, the command is placed into the incoming FIFO queue.
 * However if the incoming FIFO is full, the thread will not send anymore
 * commands until the incoming FIFO becomes available for more elements.
 * If the Outgoing FIFO becomes full due to this blocking mechanism,
 * the SF600Spectro will throw exceptions indicating that it is busy until
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
 * The other functions of the CE2180Spectro are all event driven. Procedures
 * are activated via events sent from the CommDriver. Reception of data from
 * the CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the CE2180Spectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to listeners.
 * Otherwise, a null is returned and the CE2180Spectro would guess at interpreting
 * the response.
 *
 * Currently, the response is interpreted as two response; a Startup response or
 * or Serial Number response. The Startup response is a string of characters the
 * CE2180 returns upon restart. The CE2180Spectro assumes that the startup response
 * is in the form of 1 alpha character followed by a numeric digit and punctuation
 * another numeric digit then another punctutation followed be another numeric digit.
 * For example, S1.0.9. The Serail Number response is currently recognized to be
 * 2 alpha numberic characters followed by four numeric digits. Eg, SU0252
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
 *
 * Differring from the other spectrophotomer objects, the CE2180Spectro has three
 * distinct operating modes; Normal, Reset and Recovery.
 *
 * The instrument upon restart takes a short period of time to complete its' internal
 * startup procedures. After which it returns a startup response to signify it is
 * to proceed. Between the time the instrument is switched on and the startup response
 * is returned, the instrument is unresponsive to commands. In Reset mode, the
 * CE2180Spectro attempts to determine if the instrument is currently in it's startup
 * procedure. The Reset mode consecutively sends Reset commands until a valid
 * response is received. Once a valid response is received, it is assumed that the
 * instrument has completed its' startup procedures and is ready to receive command;
 * the CE2180Spectro enters the Normal operating mode.
 *
 * The Normal operating mode operates like the above mentioned whereby commands are sent
 * and responses received are interpreted and the appropriate events are generated.
 *
 * The Recovery mode is triggered when a timeout is received for a command. Once the
 * Recovery mode is triggered it saves the timedout command and suspends commands to be
 * sent out of the outgoing queue. A Reset command is then sent to ensure that the
 * instrument is not in a startup procedure. If a second timeout is received for the
 * reset command, a Timeout error is propagated to the user. Upon receipt of a valid
 * response, it is assumed that the instrument is still online and is accepting commands
 * The saved timeout command is re-sent and operations returns to Normal mode.
 * The Recovery mode is introduced to address the event where the instrument is
 * switched off and on in the middle of a measurement process.
 */
public class CE2180Spectro implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CE2180Spectro.class.getName() );
    }

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the instrument
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo; //Serial number of the instrument, none for the CE2180

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this

    protected int m_OpStatus; //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean startup;

    protected boolean recovery; //Attempt to recover to a known state
    protected SpectroCommand m_LastCommand;

    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false; //Stop signal
    protected boolean measuremode;

    protected long m_lastReceived = 0;

    protected Thread m_Thread;

    public CE2180Spectro()
    {
        m_Listeners = new Vector();
        m_Received = null;

        m_SerialNo = "";
        m_OpStatus = OPERATIONAL_STATUS_IDLE;

        initialize();
        notifyStatusChange( new SpectroEvent( this ) );
    }

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

    public void calibrate( int step )
        throws SpectroException
    {
        try
        {
            switch( step )
            {

            case 0: //White Calibration
                m_Outgoing.putObject( new WhiteTileCommand() );
                break;

            case 1: //Black Calibration
                m_Outgoing.putObject( new BlackTrapCommand() );
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
        //Not Supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_OFFLINE_UNSUPPORTED" );
    }

    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANTDARDS_UNSUPPORTED" );
    }

    public void retrieveStandards()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    public void retrieveStandard( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
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

    public SpectroSettings getSettings()
    {
        return m_Settings;
    }

    public void setSettings( SpectroSettings newSettings )
    {
        synchronized( this )
        {
            m_Logger.info( " Set settings called" );

            if( newSettings == null )
            {
                m_Logger.info( " Null settings enterred" );
                return;
            }

            //If comm driver is not initialized
            //Assume this is the first initial settings
            //And opens the connection with the new Comm Parameters
            if( m_CommDriver == null )
            {
                Map commParameters = newSettings.getCommParameters();

                if( System.getProperty( "SERIAL_PORT_LIST" ) != null )
                {
                    m_Logger.info( "SERIAL_PORT_LIST : " + System.getProperty( "SERIAL_PORT_LIST" ) );
                }
                else
                {
                    m_Logger.info( "SERIAL_PORT_LIST not set" );
                }

                m_CommDriver = new GenericCommDriver();

                try
                {
                    String portname = (String) commParameters.get( "PORTNAME" );
                    String bitrate = (String) commParameters.get( "BITRATE" );

                    m_Logger.info( "Port name : " + portname );
                    m_Logger.info( "Bit rate : " + bitrate );

                    //Baudrate is set to 9600
                    m_CommDriver.openConnection( portname, 3000, 9600 );

                    m_CommDriver.addCommDriverListener( this );

                    running = true;

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );

                    startup = true;

                    m_Logger.info( " Comm Settings complete... return" );
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
                        m_Logger.info( " FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = CE2180Status.create( "OPEN_FAILED" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.info( " Comm Settings complete... return" );

                        if( System.getProperty( "SERIAL_PORT_LIST" ) != null )
                        {
                            m_Logger.info( "SERIAL_PORT_LIST : " + System.getProperty( "SERIAL_PORT_LIST" ) );
                        }
                        else
                        {
                            m_Logger.info( "SERIAL_PORT_LIST not set" );
                        }

                        return;
                    }

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.info( " FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = CE2180Status.create( "ERROR_OPENING" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.info( " Unable to open port... return" );
                    return;
                }

                m_Logger.info( " Should not reach this return in set settings" );
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
                m_Logger.info( " Setting Specular " + ( newSettings.getSpecular() ? "Included" : "Excluded" ) );
                m_newSettings.setSpecular( newSettings.getSpecular() );

                try
                {
                    Boolean specular = Boolean.valueOf( m_newSettings.getSpecular() );
                    SetSpecularCommand command = new SetSpecularCommand( specular );
                    m_Outgoing.putObject( command );
                }
                catch( FifoFullException fullEx )
                {
                    m_Logger.info( " Wait for fifo to be cleared." );
                }
            }

            m_Settings.setAperture( newSettings.getAperture() );

            m_Settings.setLensPosition( newSettings.getLensPosition() );

            m_Settings.setLightFilter( newSettings.getLightFilter() );

            m_Logger.info( " Set setings done" );
        }
    }

    public String getSerialNo()
    {
        return m_SerialNo;
    }

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

    public void dispose()
    {

        stopThread();

        while( running )
        {
            //Wait for end of runnig loop
        }

        m_Logger.info( " Dispose called" );
        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;

        m_Incoming = null; //Dereference Incoming FIFO
        m_Outgoing = null; //Derefernce Outgoing FIFO

        m_LastStatus = null; //Dereference SpectroStatus
        m_Settings = null; //Dereference SpectroSettings
        m_SerialNo = null; //Derference String

        // ** WARNING : Thread is stull running ***
        if( running )
        {
            m_Logger.info( " Thread still running" );
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

    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    public void setCalibrationDataFiles( URI[] fileURIs )
    {
        //TO DO
    }

    protected void interpret( String message )
    {
        m_lastReceived = System.currentTimeMillis();

        m_Logger.info( " Interpreting command" );
        m_Logger.info( " " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.info( " Interpreting > " + message );
            m_Logger.info( " Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.info( " Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                m_Settings = createSettings( message );

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    m_Logger.info( " Measure command " );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( " Removing Measure command from Incoming" );
                    notifyMeasured( evt );
                }
                else if( cmd instanceof BlackTrapCommand )
                {
                    m_Logger.info( " Black Trap command " );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( " Removing Black Trap command from Incoming" );
                    m_Logger.info( " Notifying Calibration" );
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof WhiteTileCommand )
                {
                    m_Logger.info( " White Tile command " );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( " Removing White Tile command from Incoming" );
                    m_Logger.info( " Notifying Calibration" );
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof SetSpecularCommand )
                {
                    m_Logger.info( " Settings command " );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( " Removing " + cmd.getName() + " from Incoming" );
                    m_Logger.info( " Notifying Settings Change" );
                    notifySettingsChanged( evt );
                }
                else if( cmd instanceof ResetCommand )
                {
                    SpectroStatus status = evt.getStatus();

                    Iterator msgList = status.getMessages().iterator();

                    while( msgList.hasNext() )
                    {
                        String msg = (String) msgList.next();

                        m_Logger.info( "Message : " + msg );

                        if( msg.matches( "SERIAL_NO:.*" ) )
                        {
                            m_SerialNo = msg.substring( msg.indexOf( ":" ) + 1 );
                            m_Logger.info( "Serial Number : " + m_SerialNo );
                            startup = false;
                            break;
                        }

                        if( msg.equals( "STARTUP" ) )
                        {
                            m_Logger.info( "Startup received" );
                            startup = true;
                        }
                    }

                    m_Logger.info( "Reset command" );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.info( "Removing " + cmd.getName() + " from Incoming" );
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.info( "Unknown command" );
                }
            }
        }
    }

    protected void guessInterpret( String message )
    {
        if( message.matches( "SU[0-9]{4}" ) )
        {
            //Assume it's the Serial Number
            m_SerialNo = message;
        }
        else
        {
            m_Logger.log( Level.WARNING, "CE2180 Spectro: Unrecognized response received " + message );
        }
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.info( " Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.info( " Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.info( "RECV : " + m_Received );

            String response = m_Received.toString();

            interpret( response );

            //Clear out the buffer
            m_Received = null;

            m_Logger.info( " Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            if( !recovery )
            {
                recovery = true;
                startup = true;

                //Store last sent command
                m_LastCommand = (SpectroCommand) m_Incoming.removeNextObject();
            }
            else
            {
                //Give up

                //Generate error status:
                SpectroStatus status = CE2180Status.create( "TIMEOUT_ERROR" );

                //Assuming that the waiting command has been timed out

                //Cancel the timeout if not already cancelled
                m_CommDriver.cancelRespondTimeout();

                //Remove from FIFO
                SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

                m_Logger.info( " Pre-empting " + cmd.getName() );

                m_Logger.info( " Timeout received for " + cmd.getName() );
                m_Logger.info( " Timeout received at " + System.currentTimeMillis() );

                //Insert error message
                status.addMessage( "MSG_TIMEOUT_ERROR" );

                //Notify time out
                notifyStatusChange( new SpectroEvent( this, status ) );
            }
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.info( " Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.info( " Comm Driver Sent" );
        }
        else
        {

            //Should not happen : Unknown comm status event
            m_Logger.info( " Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.info( " Sent event from CommDriver" );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.info( " Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.info( " Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.info( " Settings Ack received" );
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
        m_Logger.log( Level.FINER, "Status change" );
        m_Logger.info( " Status change " );
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

            String message;

            //If the incoming FIFO is not full, send first command in outgoing queue
            if( !m_Incoming.isFull() )
            {
                if( System.currentTimeMillis() - m_lastReceived > 1000 )
                {
                    if( startup )
                    {
                        SpectroCommand cmd = new ResetCommand();

                        message = cmd.construct();

                        message = message + "\r";

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
                    }
                    else
                    {
                        if( recovery )
                        {
                            SpectroCommand cmd = m_LastCommand;

                            m_Logger.info( "Return from recovery" );
                            m_Logger.info( "Resuming from last sent command" );

                            m_Logger.info( "Constructing command" );
                            message = cmd.construct() + "\r";

                            m_Logger.info( "Message constructd > " + message );
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

                            recovery = false;
                            continue;
                        }

                        m_Logger.log( Level.FINER, " Thread still running" );
                        m_Logger.log( Level.FINER, " Retreiving Command from fifo" );

                        //Retrieve the command
                        SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                        if( cmd == null )
                        {
                            continue;
                        }

                        m_Logger.log( Level.FINER, "Sending command " + cmd.getName() );
                        m_Logger.log( Level.FINER, "Constructing command" );
                        message = cmd.construct() + "\r";

                        m_Logger.log( Level.FINER, "Message constucted > " + message );

                        m_Logger.log( Level.FINER, "Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                        m_Logger.log( Level.FINER, "Message posted." );
                    }
                }
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //m_Logger.info( " Spectro busy." );
            }

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

        //Indicate that the thread has stopped running
        m_Logger.info( " Thread stopped." );
    }

    private synchronized void stopThread()
    {
        stopRequest = true;
        m_Thread.interrupt();
    }

    private SpectroSettings createSettings( String statusString )
    {
        //Assuming that the status string is correct

        if( m_Settings == null )
        {
            m_Settings = new SpectroSettings();
        }

        m_Logger.info( " Getting settings" );

        if( statusString.charAt( 3 ) == '1' )
        {
            m_Logger.info( " Specular set to Excluded" );
            m_Settings.setSpecular( false );
        }
        else if( statusString.charAt( 3 ) == '0' )
        {
            m_Logger.info( " Specular set to Included" );
            m_Settings.setSpecular( true );
        }

        return m_Settings;
    }
}
