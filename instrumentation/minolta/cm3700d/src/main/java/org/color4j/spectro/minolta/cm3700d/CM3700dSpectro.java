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

package org.color4j.spectro.minolta.cm3700d;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import org.color4j.spectro.spi.Aperture;
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

public class CM3700dSpectro
    implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CM3700dSpectro.class.getName() );
    }

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings;
    //The current settings of the instrument
    protected SpectroSettings m_newSettings;
    // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected SpectroReading storedMeasurement;

    protected String m_SerialNo;
    //Serial number of the instrument, none for the CM3700d

    protected Vector m_Listeners;
    //Collection of Spectrolisteners listening to this

    protected int m_OpStatus;
    //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean running; // Switch for the running thread

    protected int noresult;
    protected int finalmeasure = 7;

    protected Thread m_Thread;

    // Additional parameter for CM3700d
    private boolean m_ChargeStatus;
    private Aperture m_TargetMask;
    private boolean m_WhiteCalibration;
    private boolean m_ZeroCalibration;

    public CM3700dSpectro()
    {

        m_Listeners = new Vector();
        m_Received = null;

        m_SerialNo = "";
        noresult = 0;
        m_OpStatus = OPERATIONAL_STATUS_IDLE;

        storedMeasurement = null;

        initialize();
        notifyStatusChange( new SpectroEvent( this ) );
    }

    public void measure()
        throws SpectroException
    {
        try
        {
            m_Outgoing.putObject( new MESCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "MSG_BUSY" );
        }
    }

    public void calibrate( int step )
        throws SpectroException
    {
        try
        {
            switch( step )
            {

            case 0: //Zero Calibration
                m_Outgoing.putObject(
                    new CPSCommand(
                        new Integer( 1 ),
                        Boolean.valueOf( m_Settings.getSpecular() ),
                        m_Settings.getLensPosition(),
                        new Integer( 999 ),
                        new Integer( 0 ) ) );
                m_Outgoing.putObject( new UZCCommand() );
                break;

            case 1: //White Calibration
                m_Outgoing.putObject(
                    new CPSCommand(
                        new Integer( 1 ),
                        Boolean.valueOf( m_Settings.getSpecular() ),
                        m_Settings.getLensPosition(),
                        new Integer( 999 ),
                        new Integer( 0 ) ) );
                m_Outgoing.putObject( new CALCommand() );
                break;

            default:
                /*  Either the driver is instantiating the wrong spectrophotometer
                *  implmentation or the driver has an erroneous calibration procedure
                *  list.
                */
                throw new SpectroException( "MSG_UNKNOWN_CALIBRATE" );
            }

            m_OpStatus = OPERATIONAL_STATUS_SENDING;

            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "MSG_BUSY" );
        }
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
        //Not supported
        throw new NotSupportedException( "MSG_STANDARDS" );
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
            m_Logger.info( "CM3700d Spectro : Set settings called" );

            if( newSettings == null )
            {
                m_Logger.info( "CM3700d Spectro : Null settings enterred" );
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

                    //Baudrate is set to 9600
                    //m_CommDriver.openConnection ( portname, 3000, Integer.parseInt ( bitrate ) );
                    m_CommDriver.openConnection(
                        portname,
                        3000,
                        9600,
                        CommDriver.FLOWCONTROL_XONXOFF );

                    m_CommDriver.addCommDriverListener( this );

                    try
                    {
                        m_Outgoing.putObject( new STRCommand() );
                    }
                    catch( FifoFullException fullEx )
                    {
                        m_Logger.info( "Setting up not successfull" );
                        //throw new SpectroException ( "Spectrophotometer is currently busy." );
                    }

                    running = true;

                    m_Thread.start();

                    m_Logger.info(
                        "CM3700d Spectro : Comm Settings complete... return" );
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
                        m_Logger.info(
                            "CM3700d Spectro : FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status =
                            CM3700dStatus.create( "OPEN_FAILED" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.info(
                            "CM3700d Spectro : Comm Settings complete... return" );
                        return;
                    }

                    //newSettings.setSpecular ( true );

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.info(
                        "CM3700d Spectro : FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status =
                        CM3700dStatus.create( "ERROR_OPENING" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.info(
                        "CM3700d Spectro : Unable to open port... return" );
                    return;
                }

                m_Logger.info(
                    "CM3700d Spectro : Should not reach this return in set settings" );
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

            // Set the stuffs
            //m_Settings.setAperture ( m_TargetMask );

            m_Logger.info( "CM3700d Spectro : Set setings done" );
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
        m_Logger.info( "CM3700d Spectro : Dispose called" );
        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;

        if( m_CommDriver != null )
        {
            try
            {
                m_CommDriver.closeConnection();
            }
            catch( CommDriverException commDriverEx )
            {
                m_Logger.info( "CM3700d Spectro : FAILURE TO CLOSE CONNECTION" );
            }
        }

        m_CommDriver = null; //Dereference Comm Driver

        m_Incoming = null; //Dereference Incoming FIFO
        m_Outgoing = null; //Derefernce Outgoing FIFO
        running = false; //Turn Thread off

        m_LastStatus = null; //Dereference SpectroStatus
        m_Settings = null; //Dereference SpectroSettings
        m_SerialNo = null; //Derference String

        //Garbage collection
        System.gc();
    }

    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    protected void interpret( String message )
    {
        m_Logger.info( "CM3700d Spectro : Interpreting command" );
        m_Logger.info( "CM3700d Spectro : " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.info( "CM3700d Spectro : Interpreting > " + message );
            m_Logger.info(
                "CM3700d Spectro : Interpreting as a "
                + cmd.getName()
                + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.info(
                    "CM3700d Spectro : Cancelling timeout at "
                    + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                m_Settings = createSettings( message );

                //Decide which listener method to notify:
                if( cmd instanceof STRCommand )
                {
                    m_Logger.info( "CM3700d Spectro : Request Status Command" );
                    m_Incoming.removeNextObject();
                    m_Logger.info(
                        "CM3700d Spectro : Removing Request Status Command from Incoming" );

                    m_ChargeStatus = ( (STRCommand) cmd ).getChargeStatus();
                    m_TargetMask = ( (STRCommand) cmd ).getTargetMask();
                    m_WhiteCalibration =
                        ( (STRCommand) cmd ).getWhiteCalibration();
                    m_ZeroCalibration = ( (STRCommand) cmd ).getZeroCalibration();

                    notifyStatusChange( evt );
                }
                else if( cmd instanceof MESCommand )
                {
                    m_Logger.info( "CM3700d Spectro : Measurement Data Output Request from Incoming" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM3700d Spectro : Removing Measurement Data Output Request from Incoming" );

                    notifyMeasured( evt );
                }
                else if( cmd instanceof CPSCommand )
                {
                    m_Logger.info( "CM3700d Spectro : Measurement Parameters Prepare Command from Incoming" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM3700d Spectro : Removing Measurement Parameters Prepare Command from Incoming" );
                }
                else if( cmd instanceof UZCCommand )
                {
                    m_Logger.info( "CM3700d Spectro : User Zero Calibration Command from Incoming" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM3700d Spectro : Removing User Zero Calibration Command from Incoming" );
                }
                else if( cmd instanceof CALCommand )
                {
                    m_Logger.info( "CM3700d Spectro : White Calibration Command from Incoming" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM3700d Spectro : Removing White Calibration Command from Incoming" );
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.info( "Unknown command" );
                }
            }
        }
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.info( "CM3700d Spectro : Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.info( "CM3700d Spectro : Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.info( "CM3700d Spectro : Before clearing > " + m_Received );

            String response = m_Received.toString();

            if( m_Received.length() > 0 )
            {
                interpret( response );
            }

            //Clear out the buffer
            m_Received = null;

            m_Logger.info( "CM3700d Spectro : Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = CM3700dStatus.create( "TIMEOUT_ERROR" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            //m_Logger.info ( "CM3700d Spectro : Pre-empting " + cmd.getName () );

            m_Logger.info( "CM3700d Spectro : Timeout received for " + cmd.getName() );
            m_Logger.info( "CM3700d Spectro : Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.info( "CM3700d Spectro : Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.info( "CM3700d Spectro : Comm Driver Sent" );
        }
        else
        {
            //Should not happen : Unknown comm status event
            m_Logger.info( "CM3700d Spectro : Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.info( "CM3700d Spectro : Sent event from CommDriver" );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.info( "CM3700d Spectro : Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            SpectroListener spectroListener = (SpectroListener) list.next();
            spectroListener.measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.info( "CM3700d Spectro : Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            SpectroListener spectroListener = (SpectroListener) list.next();
            spectroListener.calibrated( evt );
        }
    }

    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.info( "CM3700d Spectro : Settings Ack received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            SpectroListener spectroListener = (SpectroListener) list.next();
            spectroListener.settingsChanged( evt );
        }
    }

    /**
     * Notifies registered listeners of status changes and errors
     *
     * @param evt The event or error to be triggered
     */
    public void notifyStatusChange( SpectroEvent evt )
    {
        m_Logger.info( "CM3700d Spectro : Status change " );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            SpectroListener spectroListener = (SpectroListener) list.next();
            spectroListener.operationalStatusChanged( evt );
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
            //If the incoming FIFO is not full, send first command in outgoing queue
            if( !m_Incoming.isFull() )
            {
                m_Logger.info( "CM3700d Spectro : Thread still running" );
                m_Logger.info( "CM3700d Spectro : Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                m_Logger.info( "CM3700d Spectro : Constructing command" );
                String message = cmd.construct();

                message = message + "\r\n";

                m_Logger.info( "CM3700d Spectro : Message constucted > " + message );

                m_Logger.info( "CM3700d Spectro : Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
                m_CommDriver.send( message.getBytes() );

                m_OpStatus = OPERATIONAL_STATUS_SENDING;
                notifyStatusChange( new SpectroEvent( this ) );

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

                m_Logger.info( "CM3700d Spectro : Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //m_Logger.info( "CM3700d Spectro : Spectro busy." );
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.info( "CM3700d Spectro : Thread stopped." );
    }

    private SpectroSettings createSettings( String statusString )
    {
        //Assuming that the status string is correct

        if( m_Settings == null )
        {
            m_Settings = new SpectroSettings();
        }

        /*m_Settings.setSpecular ( true );
        m_Settings.setLensPosition ( new SmallAreaView () );
        m_Settings.setAperture ( new SmallAperture () );*/

        /*if ( statusString.charAt ( 3 ) == '1' )
        {
            m_Logger.info ( "CM3700d Spectro : Specular set to Excluded" );
            m_Settings.setSpecular ( false );
        }
        else if ( statusString.charAt ( 3 ) == '0' )
        {
            m_Logger.info ( "CM3700d Spectro : Specular set to Included" );
            m_Settings.setSpecular ( true );
        }*/

        return m_Settings;
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {
    }
}
