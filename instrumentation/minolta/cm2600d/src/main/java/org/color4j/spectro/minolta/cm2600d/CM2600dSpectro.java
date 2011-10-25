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

/*
 * CM2600dSpectro.java
 *
 * Created on March 18, 2007, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.color4j.spectro.minolta.cm2600d;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.LightFilter;
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
 */
public class CM2600dSpectro
    implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( CM2600dSpectro.class.getName() );
    }

    private CommDriver m_CommDriver;
    private Fifo m_Incoming; // Expected incoming responses
    private Fifo m_Outgoing; // Queued commands to be sent

    //The current settings of the instrument
    private SpectroSettings m_Settings;
    private SpectroSettings m_newSettings;

    //Serial number of the instrument, none for the CM2600d
    private String m_SerialNo;

    //Collection of Spectrolisteners listening to this
    private Vector m_Listeners;

    //The operational status of the spectro implementation
    private int m_OpStatus;

    private StringBuffer m_Received; //The buffer of received bytes

    private boolean running; // Switch for the running thread

    private Thread m_Thread;

    // Additional parameter for CM2600d
    private boolean m_ChargeStatus;
    private boolean m_WhiteCalibration;
    private boolean m_ZeroCalibration;
    private int m_StoredSamples;
    private int m_CurrentSample;
    private int m_CurrentEnviron;

    public CM2600dSpectro()
    {
        m_Listeners = new Vector();
        m_Received = null;
        m_CurrentEnviron = 1;
        m_CurrentSample = 1;
        m_OpStatus = OPERATIONAL_STATUS_IDLE;
        initialize();
        notifyStatusChange( new SpectroEvent( this ) );
    }

    public void measure()
        throws SpectroException
    {
        try
        {
            // Ensure that the Charge Status is OK before trying to measure.
            while( !m_ChargeStatus )
            {
                m_Outgoing.putObject( new STRCommand() );
                Thread.sleep( 100 );
            }
            m_Outgoing.putObject( new MESCommand( m_Settings ) );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "MSG_BUSY" );
        }
        catch( InterruptedException e )
        {
            throw new SpectroException( "MSG_INTERRUPTED" );
        }
    }

    public void calibrate( int step )
        throws SpectroException
    {
        try
        {
            if( m_Settings == null )
            {
                m_Outgoing.putObject( new CPRCommand() );
                synchronized( this )
                {
                    wait();
                }
            }
            LensPosition lensPosition = m_Settings.getLensPosition();
            boolean specular = m_Settings.getSpecular();
            LightFilter filter = m_Settings.getLightFilter();
            CPSCommand command = new CPSCommand( lensPosition, specular, filter );
            switch( step )
            {
            case 0: //Zero Calibration
                m_Outgoing.putObject( command );
                m_Outgoing.putObject( new UZCCommand() );
                break;

            case 1: //White Calibration
                m_Outgoing.putObject( command );
                m_Outgoing.putObject( new CALCommand() );
                break;

            default:
                /*  Either the driver is instantiating the wrong spectrophotometer
                *  implementation or the driver has an erroneous calibration procedure
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
        catch( InterruptedException e )
        {
            // Ignore.
            m_Logger.finer( "Wait interrupted." );
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
        try
        {
            m_Outgoing.putObject( new SDRCommand( m_CurrentEnviron, m_CurrentSample ) );
        }
        catch( FifoFullException e )
        {
            throw new SpectroException( "MSG_BUSY" );
        }
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
        int[] indices = new int[ m_StoredSamples ];
        for( int i = 0; i < m_StoredSamples; i++ )
        {
            indices[ i ] = i;
        }
        m_CurrentSample = 1;
        m_CurrentEnviron = 1;
        notifySamplesFound( indices );
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
            m_Logger.finer( "Set settings called" );

            if( newSettings == null )
            {
                m_Logger.finer( "Null settings enterred" );
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
                    //m_CommDriver.openConnection (portname, 3000, Integer.parseInt (bitrate));
                    m_CommDriver.openConnection(
                        portname,
                        20000,  // 20 seconds is max time according to manual.
                        9600,
                        CommDriver.FLOWCONTROL_RTSCTS );

                    m_CommDriver.addCommDriverListener( this );

                    try
                    {
                        m_Outgoing.putObject( new IDRCommand() );
                        m_Outgoing.putObject( new SWECommand() );
                        m_Outgoing.putObject( new STRCommand() );
                    }
                    catch( FifoFullException fullEx )
                    {
                        m_Logger.log( Level.SEVERE, "Setting up not successful." );
                        //throw new SpectroException ("Spectrophotometer is currently busy.");
                    }

                    running = true;

                    m_Thread.start();

                    m_Logger.finer( "Comm Settings complete... return" );
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
                        m_Logger.severe( "FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = CM2600dStatus.create( "OPEN_FAILED" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.finer( "Comm Settings complete... return" );
                        return;
                    }

                    //newSettings.setSpecular (true);

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.severe( "FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = CM2600dStatus.create( "ERROR_OPENING" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.finer( "Unable to open port... return" );
                    return;
                }

                m_Logger.severe( "Should not reach this return in set settings" );
                return;
            }

            //Attempt to synchronise and store new settings
            m_newSettings = newSettings;
            try
            {
                m_Outgoing.putObject( new CPSCommand( newSettings.getLensPosition(), newSettings.getSpecular(), newSettings
                    .getLightFilter() ) );
                m_Outgoing.putObject( new CPRCommand() );
            }
            catch( FifoFullException e )
            {
                m_Logger.severe( "Setting up not successful." );
            }
            m_Logger.finer( "Set setings done" );
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
        m_Logger.finer( "Dispose called" );
        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;

        if( m_CommDriver != null )
        {
            try
            {
                m_CommDriver.closeConnection();
            }
            catch( CommDriverException commDriverEx )
            {
                m_Logger.severe( "FAILURE TO CLOSE CONNECTION" );
            }
        }

        m_CommDriver = null; //Dereference Comm Driver

        m_Incoming = null; //Dereference Incoming FIFO
        m_Outgoing = null; //Derefernce Outgoing FIFO
        running = false; //Turn Thread off

        m_Settings = null; //Dereference SpectroSettings
        m_SerialNo = null; //Derference String

        //Garbage collection
        System.gc();
    }

    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    private void interpret( String message )
    {
        m_Logger.finer( "Interpreting command" );
        m_Logger.finer( "" + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.finer( "Interpreting > " + message );
            m_Logger.finer( "Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.finer( "Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                createSettings( message );

                //Decide which listener method to notify:
                m_Logger.finer( cmd.getName() + " Command" );
                if( cmd instanceof STRCommand )
                {
                    m_Incoming.removeNextObject();
                    m_Logger.finer( "Removing " + cmd.getName() );

                    STRCommand strCommand = (STRCommand) cmd;
                    m_ChargeStatus = strCommand.getChargeStatus();
                    m_WhiteCalibration = strCommand.getWhiteCalibration();
                    m_ZeroCalibration = strCommand.getZeroCalibration();
                    m_StoredSamples = strCommand.getDataCount();
                    notifyStatusChange( evt );
                }
                else if( cmd instanceof CPRCommand )
                {
                    m_Incoming.removeNextObject();
                }
                else if( cmd instanceof MESCommand )
                {
                    m_Incoming.removeNextObject();
                    notifyMeasured( evt );
                }
                else if( cmd instanceof CPSCommand )
                {
                    m_Incoming.removeNextObject();
                }
                else if( cmd instanceof UZCCommand )
                {
                    m_Incoming.removeNextObject();
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof CALCommand )
                {
                    m_Incoming.removeNextObject();
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof IDRCommand )
                {
                    m_Incoming.removeNextObject();
                    m_SerialNo = ( (IDRCommand) cmd ).getBodyNumber();
                }
                else if( cmd instanceof SWECommand )
                {
                    m_Incoming.removeNextObject();
                }
                else if( cmd instanceof SDRCommand )
                {
                    m_Incoming.removeNextObject();
                    if( evt.getStatus().isSuccess() )
                    {
                        notifyRetrieved( evt );
                        m_CurrentSample = m_CurrentSample + 1;
                    }
                    else
                    {
                        if( evt.getStatus().getErrors().contains( "MSG_ERROR_NO_DATA_AT_LOCATION" ) )
                        {
                            if( m_CurrentEnviron <= 6 )
                            {
                                m_CurrentEnviron = m_CurrentEnviron + 1;
                                m_CurrentSample = 1;
                                try
                                {
                                    m_Outgoing.putObject( new SDRCommand( m_CurrentEnviron, m_CurrentSample ) );
                                }
                                catch( FifoFullException e )
                                {
                                    m_Logger.log( Level.SEVERE, "Spectro is too Busy.", e );
                                }
                            }
                            else
                            {
                                // We have retrieved all stored samples.
                                m_CurrentEnviron = 1;
                                m_CurrentSample = 1;
                            }
                        }
                        else
                        {
                            // Some form of unexpected error, try again.
                            try
                            {
                                m_Outgoing.putObject( new SDRCommand( m_CurrentEnviron, m_CurrentSample ) );
                            }
                            catch( FifoFullException e )
                            {
                                m_Logger.log( Level.SEVERE, "Spectro is too Busy.", e );
                            }
                        }
                    }
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.severe( "Unknown command: " + cmd.getName() );
                    m_Incoming.removeNextObject();
                }
            }
            else
            {
                m_Logger.severe( "Unable to guess interpret the response: " + message );
            }
        }
        else
        {
            // A spontanous measurement was made?
            SpectroEvent event = new MESCommand( m_Settings ).interpret( message.getBytes() );
            if( event.getStatus().isSuccess() )
            {
                notifyMeasured( event );
            }
        }
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.finer( "Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.finer( "Comm Driver Data Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.finer( "Before clearing > " + m_Received );

            String response = m_Received.toString();

            if( m_Received.length() > 0 )
            {
                interpret( response );
                //Clear out the buffer
                m_Received = null;
            }
            m_Logger.finer( "Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = CM2600dStatus.create( "TIMEOUT_ERROR" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            //m_Logger.finer("Pre-empting " + cmd.getName ());

            m_Logger.info( "Timeout received for " + cmd.getName() + " at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT" );

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

    private void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.finer( "Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            try
            {
                SpectroListener spectroListener = (SpectroListener) list.next();
                spectroListener.measured( evt );
            }
            catch( Throwable e )
            {
                m_Logger.log( Level.SEVERE, "Unexpected Exception.", e );
            }
        }
    }

    private void notifyRetrieved( SpectroEvent evt )
    {
        m_Logger.finer( "Measurement retrieved" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            try
            {
                SpectroListener spectroListener = (SpectroListener) list.next();
                spectroListener.retrievedSample( evt );
            }
            catch( Throwable e )
            {
                m_Logger.log( Level.SEVERE, "Unexpected Exception.", e );
            }
        }
    }

    private void notifySamplesFound( int[] indices )
    {
        m_Logger.finer( "Samples Found" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            try
            {
                SpectroListener spectroListener = (SpectroListener) list.next();
                spectroListener.numberSamplesFound( indices );
            }
            catch( Throwable e )
            {
                m_Logger.log( Level.SEVERE, "Unexpected Exception.", e );
            }
        }
    }

    private void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.finer( "Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            SpectroListener spectroListener = (SpectroListener) list.next();
            spectroListener.calibrated( evt );
        }
    }

    private void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.finer( "Settings Ack received" );
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
    private void notifyStatusChange( SpectroEvent evt )
    {
        m_Logger.finer( "Status change " );
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
     * The thread that takes a command from the outgoing FIFO and sends it. This thread is responsible for taking the
     * constructed command, computing the appropriate checksum and terminating it then send it.
     * <p/>
     * If the incoming FIFO is full, the thread would not send commands until the incoming FIFO is available for more
     * elements to be added.
     */
    public void run()
    {
        while( running )
        {
            //If the incoming FIFO is not full, send first command in outgoing queue
            if( !m_Incoming.isFull() )
            {
                m_Logger.finer( "Thread still running" );
                m_Logger.finer( "Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                m_Logger.finer( "Constructing command" );
                String message = cmd.construct();

                m_Logger.finer( "Message constructed > " + message );

                m_Logger.finer( "Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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
                    sleep( 100 );
                    continue;
                }

                //Command is sent, remove from outgoing FIFO
                m_Outgoing.removeNextObject();

                m_Logger.finer( "Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                m_Logger.finer( "Spectro busy." );
                int time = 500;
                sleep( time );
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.finer( "Thread stopped." );
    }

    private void sleep( int time )
    {
        try
        {
            Thread.sleep( time );
        }
        catch( InterruptedException e )
        {
            // Ignore...
        }
    }

    private void createSettings( String statusString )
    {
        CPRCommand cpr = new CPRCommand();
        SpectroEvent event = cpr.interpret( statusString.getBytes() );
        SpectroStatus status = event.getStatus();
        if( m_Settings == null )
        {
            m_Settings = new SpectroSettings();
        }
        if( status.isFailure() )
        {
            return;
        }
        m_Settings.setSpecular( cpr.getSpecular() );
        m_Settings.setLensPosition( cpr.getMeasureArea() );
        m_Settings.setAperture( new MediumAperture() );
        m_Settings.setLightFilter( cpr.getLightFilter() );
        synchronized( this )
        {
            notifyAll();
        }
        notifySettingsChanged( event );
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {
    }
}
