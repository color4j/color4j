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

package org.color4j.spectro.gretagmacbeth.ce7000;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
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

public class CE7000Spectro implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CE7000Spectro.class.getName() );
    }

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the instrument
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo; //Serial number of the instrument, none for the CE7000

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this

    protected int m_OpStatus; //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean startup;
    protected boolean reset; //Indicating waiting for a reset response
    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false;
    protected boolean measuremode;
    protected int noresult;
    protected int finalmeasure = 7;

    protected Thread m_Thread;

    //protected CustomLightFilter m_CustomLightFilter;
    protected MeasureCommand m_MeasureCommand = null;

    public CE7000Spectro()
    {
        m_Listeners = new Vector();
        m_Received = null;

        m_SerialNo = "";
        noresult = 0;
        m_OpStatus = OPERATIONAL_STATUS_IDLE;

        //m_CustomLightFilter = new CustomLightFilter ();

        initialize();
        notifyStatusChange( new SpectroEvent( this ) );
    }

    public void measure()
        throws SpectroException
    {
        try
        {
            if( m_MeasureCommand == null )
            {
                m_MeasureCommand = new MeasureCommand();
            }

            m_Outgoing.putObject( new GetMotorPositionCommand() );
            m_Outgoing.putObject( m_MeasureCommand );

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

            case 0: //Black Calibration
                m_Outgoing.putObject( new BlackTrapCommand() );
                break;

            case 1: //White Calibration
                m_Outgoing.putObject( new WhiteTileCommand() );
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
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
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
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    public void queryNoOfStoredStandards()
        throws SpectroException
    {
        throw new NotSupportedException( "MSG_STANDARDS_UNSUPPORTED" );
    }

    public SpectroSettings getSettings()
    {
        return m_Settings;
    }

    public void setSettings( SpectroSettings newSettings )
    {
        synchronized( this )
        {
            m_Logger.info( "CE7000 Spectro : Set settings called" );

            if( newSettings == null )
            {
                m_Logger.info( "CE7000 Spectro : Null settings enterred" );
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

                    m_Logger.info( "java.library.path : " + System.getProperty( "java.library.path" ) );

                    Properties prop = System.getProperties();

                    Iterator propList = prop.keySet().iterator();

                    while( propList.hasNext() )
                    {
                        Object o = propList.next();

                        m_Logger.info( o + " : " + prop.get( o ) );
                    }

                    String portname = (String) commParameters.get( "PORTNAME" );
                    String bitrate = (String) commParameters.get( "BITRATE" );

                    m_Logger.info( "Port name : " + portname );
                    m_Logger.info( "Bit rate : " + bitrate );

                    //Baudrate is set to 9600
                    m_CommDriver.openConnection( portname, 3000, 9600, CommDriver.FLOWCONTROL_XONXOFF );
                    //m_CommDriver.openConnection( portname, 3000, 9600 );

                    m_CommDriver.addCommDriverListener( this );

                    //Remove Startup junk
                    //startup = true;
                    //m_CommDriver.post( new String( "\r" ).getBytes(), this );

                    //Prepare to get the serial no
                    //reset = false;

                    try
                    {
                        m_Outgoing.putObject( new ResetCommand() );
                    }
                    catch( FifoFullException fullEx )
                    {
                    }

                    running = true;

                    m_Thread.start();

                    m_OpStatus = OPERATIONAL_STATUS_IDLE;
                    notifyStatusChange( new SpectroEvent( this ) );

                    m_Logger.info( "CE7000 Spectro : Comm Settings complete... return" );
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
                        m_Logger.info( "CE7000 Spectro : FAILURE TO OPEN CONNECTION -" + commEx );

                        commEx.printStackTrace( System.out );
                        m_Logger.info( "java.library.path : " + System.getProperty( "java.library.path" ) );

                        SpectroStatus status = CE7000Status.create( "OPEN_FAILED" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.info( "CE7000 Spectro : Comm Settings complete... return" );
                        return;
                    }

                    try
                    {
                        m_Outgoing.putObject( new ResetCommand() );
                    }
                    catch( FifoFullException fullEx )
                    {
                    }

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.info( "CE7000 Spectro : FAILURE TO OPEN CONNECTION -" + commEx );

                    commEx.printStackTrace( System.out );

                    m_Logger.info( "java.library.path : " + System.getProperty( "java.library.path" ) );

                    SpectroStatus status = CE7000Status.create( "ERROR_OPENING" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.info( "CE7000 Spectro : Unable to open port... return" );
                    return;
                }

                m_Logger.info( "CE7000 Spectro : Should not reach this return in set settings" );
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
                m_Logger.info( "CE7000 Spectro : Setting Specular " + ( newSettings.getSpecular() ? "Included" : "Excluded" ) );
                m_newSettings.setSpecular( newSettings.getSpecular() );

                try
                {
                    m_Outgoing.putObject( new SetSpecularCommand( new Boolean( m_newSettings.getSpecular() ) ) );
                }
                catch( FifoFullException fullEx )
                {
                    m_Logger.info( "CE7000 Spectro : Wait for fifo to be cleared." );
                }
            }

            m_Settings.setAperture( newSettings.getAperture() );

            m_Settings.setLensPosition( newSettings.getLensPosition() );

            m_Settings.setLightFilter( newSettings.getLightFilter() );

            m_Logger.info( "CE7000 Spectro : Set setings done" );
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

        m_Logger.info( "CE7000 Spectro : Dispose called" );

        stopThread();

        while( running )
        {
            //Wait for loop to end
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
            m_Logger.info( "CE7000 Spectro : Thread still running" );
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

    public void setCalibrationDataFiles( URI[] fileURLs )
    {
        //Not Required
    }

    protected void interpret( String message )
    {
        m_Logger.info( "CE7000 Spectro : Interpreting command" );
        m_Logger.info( "CE7000 Spectro : " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.info( "CE7000 Spectro : Interpreting > " + message );
            m_Logger.info( "CE7000 Spectro : Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.info( "CE7000 Spectro : Cancelling timeout at " + System.currentTimeMillis() );
                m_Logger.info( "CE7000 Spectro : Interpreting as a " + cmd.getName() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                if( cmd.getName().equals( "Insert Command" ) )
                {
                    // Do nothing
                }
                else
                {
                    m_Settings = createSettings( message );
                    notifySettingsChanged( new SpectroEvent( this ) );
                }

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    m_Logger.info( "CE7000 Spectro : Measure command " );
                    removeCommand();
                    notifyMeasured( evt );
                    return;
                }
                else if( cmd instanceof BlackTrapCommand )
                {
                    m_Logger.info( "CE7000 Spectro : Black Trap command " );
                    removeCommand();
                    m_Logger.info( "CE7000 Spectro : Notifying Calibration" );
                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof WhiteTileCommand )
                {
                    m_Logger.info( "CE7000 Spectro : White Tile command " );
                    removeCommand();
                    m_Logger.info( "CE7000 Spectro : Notifying Calibration" );
                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof GetMotorPositionCommand )
                {
                    m_Logger.info( "CE7000 Spectro : GetMotorPosition command " );
                    m_Logger.info( "CE7000 Spectro : Motor steps - " + ( (GetMotorPositionCommand) cmd ).getMotorsteps() );
                    //m_CustomLightFilter.setCutoff ( ( (GetMotorPositionCommand) cmd ).getMotorsteps() );
                    //m_MeasureCommand.setCustomLightfilter ( m_CustomLightFilter );
                    removeCommand();
                    m_Logger.info( "CE7000 Spectro : Notifying Settings Change" );
                    notifySettingsChanged( evt );
                    return;
                }
                else if( cmd instanceof SetSpecularCommand )
                {
                    m_Logger.info( "CE7000 Spectro : Settings command " );
                    removeCommand();
                    m_Logger.info( "CE7000 Spectro : Notifying Settings Change" );
                    notifySettingsChanged( evt );
                    return;
                }
                else if( cmd instanceof ResetCommand )
                {
                    m_Logger.info( "CE7000 Spectro : Reset command" );
                    removeCommand();
                    m_Logger.info( "CE7000 Spectro : Setting serial number" );

                    m_SerialNo = ( (ResetCommand) cmd ).getSerialNumber();
                    //notifyStatusChange ( evt );

                    SpectroStatus status = new CE7000Status();
                    status.addMessage( "MSG_SERIAL_NO" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    return;
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.info( "Unknown command" );
                }
            }
            else
            {
                //Unknown by the waiting command
                //guessInterpret( message );
                m_Logger.info( "CE7000 Spectro : Event is null" );
            }
        }

        //If the method hasn't returned then the command is not expected or unknown.
        //Guess interpret it.
        //guessInterpret( message );
    }

    protected void guessInterpret( String message )
    {
    }

    private void removeCommand()
    {
        SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject(); //Remove from fifo
        m_Logger.info( "Removing " + cmd.getName() + " from Incoming" );
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.info( "CE7000 Spectro : Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.info( "CE7000 Spectro : Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );
            String response = m_Received.toString();

            /*if( startup ) {
                //Grab received
                if ( response.matches ("[a-zA-z]{1}[0-9]{1}\\.{1}[0-9]{1}\\.{1}[0-9]{1}" ) ) {
                    //Assume it's the startup character
                    startup = false;
                    reset = true;
             
                    // Send the Serail No command
                    m_CommDriver.post( new String( "R\r" ).getBytes(), this );
                    return;
                }
            }*/

            /*if ( reset ) {
                // Get the Serial No
                m_SerialNo = response;
                reset = false;
                return;
            }*/

            interpret( response );

            //Clear out the buffer
            m_Received = null;

            m_Logger.info( "CE7000 Spectro : Buffer > " + m_Received );

            // Generate the status message
            SpectroStatus status = new CE7000Status();
            status.addMessage( "StatusPanel : RECEIVE" );
            // Generate event
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = CE7000Status.create( "TIMEOUT_ERROR" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            m_Logger.info( "CE7000 Spectro : Pre-empting " + cmd.getName() );

            m_Logger.info( "CE7000 Spectro : Timeout received for " + cmd.getName() );
            m_Logger.info( "CE7000 Spectro : Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.info( "CE7000 Spectro : Comm Driver Sending" );

            // Generate the status message
            SpectroStatus status = new CE7000Status();
            //status.addMessage ( "StatusPanel : SEND" );
            // Generate event
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.info( "CE7000 Spectro : Comm Driver Sent" );

            // Generate the status message
            SpectroStatus status = new CE7000Status();
            //status.addMessage ( "StatusPanel : EMPTY" );
            // Generate event
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else
        {

            //Should not happen : Unknown comm status event
            m_Logger.info( "CE7000 Spectro : Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.info( "CE7000 Spectro : Sent event from CommDriver" );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.info( "CE7000 Spectro : Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.info( "CE7000 Spectro : Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.info( "CE7000 Spectro : Settings Ack received" );
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
        m_Logger.info( "CE7000 Spectro : Status change " );
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
                m_Logger.info( "CE7000 Spectro : Thread still running" );
                m_Logger.info( "CE7000 Spectro : Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                m_Logger.info( "CE7000 Spectro : Constructing command" );

                if( cmd == null )
                {
                    continue;
                }

                String message = cmd.construct();

                if( !( cmd instanceof ResetCommand ) )
                {
                    message = message + "\r\n";
                }

                m_Logger.info( "CE7000 Spectro : Message constucted > " + message );

                m_Logger.info( "CE7000 Spectro : Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                m_Logger.info( "CE7000 Spectro : Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //m_Logger.info( "CE7000 Spectro : Spectro busy." );
            }

            try
            {
                m_Thread.sleep( 500 );
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
        m_Logger.info( "CE7000 Spectro : Thread stopped." );
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

        m_Logger.info( "CE7000 Spectro : Getting settings" );

        // Set the specular
        if( statusString.charAt( 3 ) == '1' )
        {
            m_Logger.info( "CE7000 Spectro : Specular set to Excluded" );
            m_Settings.setSpecular( false );
        }
        else if( statusString.charAt( 3 ) == '0' )
        {
            m_Logger.info( "CE7000 Spectro : Specular set to Included" );
            m_Settings.setSpecular( true );
        }

        // Set the UV Filter
        if( statusString.charAt( 4 ) == '1' )
        {
            m_Logger.info( "CE7000 Spectro : UV Excluded" );
            m_Settings.setLightFilter( new UVExcludedLightFilter() );
        }
        else if( statusString.charAt( 4 ) == '2' )
        {
            m_Logger.info( "CE7000 Spectro : Custom UV Filter" );
            m_Settings.setLightFilter( new UVD65LightFilter() );
        }

        // Check the Lens Position
        if( statusString.charAt( 5 ) == '0' )
        {
            m_Logger.info( "CE7000 Spectro : Large Area View" );
            m_Settings.setLensPosition( new LargeAreaView() );
        }
        else if( statusString.charAt( 5 ) == '1' )
        {
            m_Logger.info( "CE7000 Spectro : Medium Area View" );
            m_Settings.setLensPosition( new MediumAreaView() );
        }
        else if( statusString.charAt( 5 ) == '2' )
        {
            m_Logger.info( "CE7000 Spectro : Small Area View" );
            m_Settings.setLensPosition( new SmallAreaView() );
        }
        else if( statusString.charAt( 5 ) == '3' )
        {
            m_Logger.info( "CE7000 Spectro : Very Small Area View" );
            m_Settings.setLensPosition( new VerySmallAreaView() );
        }

        // Check for Aperture
        if( statusString.charAt( 6 ) == '0' )
        {
            m_Logger.info( "CE7000 Spectro : Large Aperture" );
            m_Settings.setAperture( new LargeAperture() );
        }
        else if( statusString.charAt( 6 ) == '1' )
        {
            m_Logger.info( "CE7000 Spectro : Very Small Aperture" );
            m_Settings.setAperture( new VerySmallAperture() );
        }
        else if( statusString.charAt( 6 ) == '2' )
        {
            m_Logger.info( "CE7000 Spectro : Medium Aperture" );
            m_Settings.setAperture( new MediumAperture() );
        }
        else if( statusString.charAt( 6 ) == '3' )
        {
            m_Logger.info( "CE7000 Spectro : Small Aperture" );
            m_Settings.setAperture( new SmallAperture() );
        }

        return m_Settings;
    }
}
