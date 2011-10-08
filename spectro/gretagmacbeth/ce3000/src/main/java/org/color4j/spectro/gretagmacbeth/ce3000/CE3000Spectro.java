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

package org.color4j.spectro.gretagmacbeth.ce3000;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
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
 * with a Gretag-MacBeth Color Eye 3000.
 *
 * The CE3000 Spectrophotometer:
 *
 * From the operational point of view, the CE3000 runs on the
 * the principal of queueing outgoing commands and expecting
 * incoming response for each command sent to be interpreted and
 * and passed up the chain via events.
 *
 * To this effect, the CE3000Spectro has two First In First Out (FIFO)
 * queues, one for outgoing commands and one for incoming commands.
 * There is a single thread that runs continuously while the CE3000Spectro
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
 * The other functions of the CE3000Spectro are all event driven. Procedures
 * are activated via events sent from the CommDriver. Reception of data from
 * the CommDriver is based on receiving the Data Available event. Upon triggerring
 * receive, the CE3000Spectro will retrieve available data from the CommDriver
 * and determine if the data returned is a complete response by locating the
 * terminator symbolized as a colon followed by a carriage return and a
 * linefeed ( ":<cr><lf>" ). Once a complete response is received, the
 * response is to be interpreted. ACK (*) and NAK (?) responses are also taken
 * note of. Assuming that each command sent requires a response, the command in
 * the Incoming FIFO is used to interpret the response.
 *
 * If successful, the command generates a SpectroEvent to be passed to listeners.
 * Otherwise, a null is returned and the CE3000Spectro would guess at interpreting
 * the response.
 *
 * Currently, the response is interpreted as two response; a Startup response or
 * or Serial Number response. The Startup response is a string of characters the
 * CE3000 returns upon restart. The CE3000Spectro assumes that the startup response
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
 * Differring from the other spectrophotomer objects, the CE3000Spectro has three
 * distinct operating modes; Normal, Reset and Recovery.
 *
 * The instrument upon restart takes a short period of time to complete its' internal
 * startup procedures. After which it returns a startup response to signify it is
 * to proceed. Between the time the instrument is switched on and the startup response
 * is returned, the instrument is unresponsive to commands. In Reset mode, the
 * CE3000Spectro attempts to determine if the instrument is currently in it's startup
 * procedure. The Reset mode consecutively sends Reset commands until a valid
 * response is received. Once a valid response is received, it is assumed that the
 * instrument has completed its' startup procedures and is ready to receive command;
 * the CE3000Spectro enters the Normal operating mode.
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
public abstract class CE3000Spectro
    implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CE3000Spectro.class.getName() );
    }

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings;
    //The current settings of the instrument
    protected SpectroSettings m_newSettings;
    // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo;
    //Serial number of the instrument, none for the CE3000

    protected Vector m_Listeners;
    //Collection of Spectrolisteners listening to this

    protected int m_OpStatus;
    //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean m_Startup;

    protected boolean m_Recovery; //Attempt to recover to a known state
    protected SpectroCommand m_LastCommand;

    protected boolean m_Running; // Switch for the running thread
    protected boolean m_StopRequest = false; //Stop signal
    protected boolean m_Measuremode;

    protected long m_lastReceived = 0;

    protected Thread m_Thread;

    public CE3000Spectro()
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
            m_Outgoing.putObject( new CalibrateCommand( step ) );

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
            m_Logger.finer( " Set settings called" );

            if( newSettings == null )
            {
                m_Logger.finer( " Null settings enterred" );
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
                    m_Logger.finer(
                        "SERIAL_PORT_LIST : "
                        + System.getProperty( "SERIAL_PORT_LIST" ) );
                }
                else
                {
                    m_Logger.finer( "SERIAL_PORT_LIST not set" );
                }

                m_CommDriver = new GenericCommDriver();

                try
                {
                    String portname = (String) commParameters.get( "PORTNAME" );
                    String bitrate = (String) commParameters.get( "BITRATE" );

                    m_Logger.finer( "Port name : " + portname );
                    m_Logger.finer( "Bit rate : " + bitrate );

                    openConnection( m_CommDriver, portname );
//                    m_CommDriver.openConnection(portname, 3000, 9600);

                    m_CommDriver.addCommDriverListener( this );

                    m_Running = true;
                    m_Startup = true;

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );

                    m_Logger.finer( " Comm Settings complete... return" );
                }
                catch( NumberFormatException numEx )
                {
                    //Try to recoved from exception and use a preset default
                    //bitrate
                    String portname = (String) commParameters.get( "PORTNAME" );

                    try
                    {
//                        m_CommDriver.openConnection(portname, 3000, 9600);
                        openConnection( m_CommDriver, portname );
                        m_CommDriver.addCommDriverListener( this );
                        m_Thread.start();
                    }
                    catch( CommDriverException commEx )
                    {
                        //Give up.... inform user that it is not possible
                        //to open connection
                        m_Logger.finer( " FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = new CE3000Status();
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.finer( " Comm Settings complete... return" );

                        if( System.getProperty( "SERIAL_PORT_LIST" ) != null )
                        {
                            m_Logger.finer(
                                "SERIAL_PORT_LIST : "
                                + System.getProperty( "SERIAL_PORT_LIST" ) );
                        }
                        else
                        {
                            m_Logger.finer( "SERIAL_PORT_LIST not set" );
                        }
                    }
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.finer( " FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = new CE3000Status();
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.finer( " Unable to open port... return" );
                }
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

            //Compare lensposition:
            m_Logger.finer( "lens :" + newSettings.getLensPosition() );
            m_Settings.setLensPosition( newSettings.getLensPosition() );
            m_Settings.setAperture( newSettings.getAperture() );
            m_Settings.setLightFilter( newSettings.getLightFilter() );
            m_Settings.setSpecular( newSettings.getSpecular() );

            //KH : should invoke status command and set things accordingly
            try
            {
                m_Outgoing.putObject( new StatusCommand() );
            }
            catch( FifoFullException fullEx )
            {
                m_Logger.finer( "SF300 Spectro : Wait for fifo to be cleared." );
            }

            m_Logger.finer( "Set settings done" );
        }
    }

    /**
     * subclasses that use a different baud rate...  not the good way, needs refactoring
     *
     * @param commDriver
     * @param portname
     */
    abstract protected void openConnection( CommDriver commDriver, String portname )
        throws CommDriverException;

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

        while( m_Running )
        {
            //Wait for end of runnig loop
        }

        m_Logger.finer( " Dispose called" );
        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;

        m_Incoming = null; //Dereference Incoming FIFO
        m_Outgoing = null; //Derefernce Outgoing FIFO

        m_LastStatus = null; //Dereference SpectroStatus
        m_Settings = null; //Dereference SpectroSettings
        m_SerialNo = null; //Derference String

        // ** WARNING : Thread is stull running ***
        if( m_Running )
        {
            m_Logger.finer( " Thread still running" );
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

        m_Logger.finer( " Interpreting command" );
        m_Logger.finer( " " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.finer( " Interpreting > " + message );
            m_Logger.finer( " Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.finer( " Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                //KH - status string not always sent with command response..
                StringTokenizer sTok = new StringTokenizer( message, "\n" );

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( " Removing Measure command from Incoming" );
                    modifySettings( sTok.nextToken() );
                    ( (CE3000Reading) evt.getReading() ).setSettings( getSettings() );
                    notifyMeasured( evt );
                }
                else if( cmd instanceof CalibrateCommand )
                {
                    SpectroStatus status = evt.getStatus();

                    Iterator msgList = status.getMessages().iterator();

                    while( msgList.hasNext() )
                    {
                        String msg = (String) msgList.next();

                        m_Logger.finer( "Message : " + msg );

                        if( msg.startsWith( CalibrateCommand.CALIBRATED ) )
                        {
                            m_Logger.finer( "CE3000 calibrated" );
                            m_Logger.finer( " Notifying Calibration" );
                            notifyCalibrated( evt );
                        }
                        else
                        {
                            m_Logger.finer( "Error calibrating CE3000" );
                        }
                    }

                    m_Logger.finer( cmd.getName() );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( " Removing " + cmd.getName() + " from Incoming" );
                    //KH needed to reflect changes in spectro
                    m_Logger.finer( "calling setSettings() " );
                    setSettings( m_Settings );
                }
                else if( cmd instanceof ResetCommand )
                {
                    SpectroStatus status = evt.getStatus();
                    Iterator msgList = status.getMessages().iterator();
                    while( msgList.hasNext() )
                    {
                        String msg = (String) msgList.next();

                        m_Logger.finer( "Message : " + msg );

                        if( msg.startsWith( "SERIAL:" ) )
                        {
                            m_SerialNo = msg.substring( msg.indexOf( "H" ) );
                            m_Logger.finer( "Serial Number : " + m_SerialNo );
                            m_Startup = false;
                        }
                        else if( msg.equals( "STARTUP" ) )
                        {
                            m_Logger.finer( "Startup received, sending reset command again" );
                            m_Startup = true;
                        }
                    }
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( "Removing " + cmd.getName() + " from Incoming" );
                }
                else if( cmd instanceof StatusCommand )
                {
                    m_Logger.finer( cmd.getName() );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( " Removing " + cmd.getName() + " from Incoming" );
                    modifySettings( sTok.nextToken() );
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.finer( "Unknown command" );
                    m_Incoming.removeNextObject(); //Remove from fifo
                    m_Logger.finer( " Removing " + cmd.getName() + " from Incoming" );
                }
            }
            else
            {
                //Unknown by the waiting command
                //guessInterpret( message );
            }
        }

        //If the method hasn't returned then the command is not expected or unknown.
        //Guess interpret it.
        //guessInterpret( message );
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
            m_Logger.log(
                Level.WARNING,
                "CE3000 Spectro: Unrecognized response received " + message );
        }
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.finer( " Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.finer( " Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.finer( "RECV : " + m_Received );

            String response = m_Received.toString();

            interpret( response );

            //Clear out the buffer
            m_Received = null;

            m_Logger.finer( " Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            if( !m_Recovery )
            {
                m_Recovery = true;
                //KH : shouldn't do this as it
//                m_Startup = true;  will short circuit the use of m_Recovery

                //Store last sent command
                m_LastCommand = (SpectroCommand) m_Incoming.removeNextObject();

                return;
            }
            else
            {
                //Give up

                //Generate error status:
                SpectroStatus status = new CE3000Status();

                //Assuming that the waiting command has been timed out

                //Cancel the timeout if not already cancelled
                m_CommDriver.cancelRespondTimeout();

                //Remove from FIFO
                SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

                m_Logger.finer( " Pre-empting " + cmd.getName() );

                m_Logger.finer( " Timeout received for " + cmd.getName() );
                m_Logger.finer( " Timeout received at " + System.currentTimeMillis() );

                //Insert error message
                status.addMessage( "MSG_TIMEOUT_ERROR" );

                //Notify time out
                notifyStatusChange( new SpectroEvent( this, status ) );
            }
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.finer( " Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.finer( " Comm Driver Sent" );
        }
        else
        {

            //Should not happen : Unknown comm status event
            m_Logger.finer( " Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.finer( " Sent event from CommDriver" );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.finer( " Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.finer( " Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.finer( " Settings Ack received" );
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
        m_Logger.finer( " Status change " );
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
        while( m_Running )
        {
            if( m_StopRequest )
            {
                m_Running = false;
                break;
            }

            SpectroCommand cmd = null;
            String message;

            //If the incoming FIFO is not full, send first command in outgoing queue
            if( !m_Incoming.isFull() )
            {
                if( System.currentTimeMillis() - m_lastReceived > 1000 )
                {
                    if( m_Startup )
                    {
                        cmd = new ResetCommand();

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
                        if( m_Recovery )
                        {
                            cmd = m_LastCommand;

                            m_Logger.finer( "Return from recovery" );
                            m_Logger.finer( "Resuming from last sent command" );

                            m_Logger.finer( "Constructing command" );
                            message = cmd.construct() + "\r";

                            m_Logger.finer( "Message constructd > " + message );
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

                            m_Recovery = false;
                            continue;
                        }

                        m_Logger.finer( " Thread still running" );
                        m_Logger.finer( " Retreiving Command from fifo" );

                        //Retrieve the command
                        cmd = (SpectroCommand) m_Outgoing.getNextObject();

                        if( cmd == null )
                        {
                            continue;
                        }

                        m_Logger.finer( "Sending command " + cmd.getName() );
                        m_Logger.finer( "Constructing command" );
                        message = cmd.construct() + "\r";

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

                        m_Logger.finer( "Message posted." );
                    }
                }
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                m_Logger.finer( "Incoming FIFO is full, spectro busy." );
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException irEx )
            {
                if( m_StopRequest )
                {
                    m_Running = false;
                    break;
                }
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.finer( " Thread stopped." );
    }

    private synchronized void stopThread()
    {
        m_StopRequest = true;
        m_Thread.interrupt();
    }

    private void modifySettings( String status )
    {
        m_Logger.finer( "status string :" + status );
        boolean modified = false;

        if( status.charAt( 2 ) == 'I' && !m_Settings.getSpecular() )
        {
            m_Settings.setSpecular( true );
            modified = true;
        }
        else if( status.charAt( 2 ) == 'E' && m_Settings.getSpecular() )
        {
            m_Settings.setSpecular( false );
            modified = true;
        }

        if( status.charAt( 3 ) == 'I' )//&& m_Settings.getLightFilter().getClass().equals( new UVIncludedLightFilter().getClass() ) )
        {
            m_Settings.setLightFilter( new UVIncludedLightFilter() );
            modified = true;
        }
        else if( status.charAt( 3 ) == 'O' )//&& m_Settings.getLightFilter().getClass().equals( new UVExcludedLightFilter().getClass() ) )
        {
            m_Settings.setLightFilter( new UVExcludedLightFilter() );
            modified = true;
        }

        if( status.charAt( 4 ) == 'S' && m_Settings.getLensPosition()
                                             .getFocusRadius() != new SmallAreaView().getFocusRadius() )
        {
            m_Settings.setLensPosition( new SmallAreaView() );
            modified = true;
        }
        else if( status.charAt( 4 ) == 'L' && m_Settings.getLensPosition()
                                                  .getFocusRadius() != new LargeAreaView().getFocusRadius() )
        {
            m_Settings.setLensPosition( new LargeAreaView() );
            modified = true;
        }

        if( modified )
        {
            notifySettingsChanged( new SpectroEvent( this ) );
        }
    }
}
