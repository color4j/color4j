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

package org.color4j.spectro.gretagmacbeth.xth;

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

public class XTHSpectro implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger = Logger.getLogger( XTHSpectro.class.getName() );

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings;
    //The current settings of the instrument
    protected SpectroSettings m_newSettings;
    // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected String m_SerialNo;
    //Serial number of the instrument, none for the XTH

    protected Vector m_Listeners;
    //Collection of Spectrolisteners listening to this

    protected int m_OpStatus;
    //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean m_Startup;

    protected boolean m_Recovery; //Attempt to recover to a known state
    protected SpectroCommand m_LastCommand;

    protected boolean running; // Switch for the running thread
    protected boolean m_StopRequest = false;

    protected boolean measuremode;

    protected long m_lastReceived = 0;

    protected Thread m_Thread;

    public XTHSpectro()
    {
        m_Listeners = new Vector();
        m_Received = null;

        m_SerialNo = "";
        m_OpStatus = OPERATIONAL_STATUS_IDLE;

        initialize();
//        notifyStatusChange(new SpectroEvent(this));
    }

    public void measure()
        throws SpectroException
    {
        try
        {
            m_Logger.finer( "Creating Measure Command" );
//            m_Outgoing.putObject(new TriggerMeasureCommand());

            if( m_Settings == null )
            {
                m_Outgoing.putObject( new MeasureCommand() );
            }
            else
            {
                m_Outgoing.putObject( new MeasureCommand( m_Settings.getAperture(), m_Settings.getSpecular() ) );
            }

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
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
                m_Outgoing.putObject( new WhiteCalibrationCommand() );
                break;

            case 1: //Black Calibration
                m_Outgoing.putObject( new BlackCalibrationCommand() );
                break;

            default:
                /*  Either the driver is instantiating the wrong spectrophotometer
                *  implmentation or the driver has an erroneous calibration procedure
                *  list.
                */
                throw new SpectroException( "Unrecognized calibration procedure." );
            }

            m_OpStatus = OPERATIONAL_STATUS_SENDING;

            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
    }

    public void retrieveStoredSamples()
        throws SpectroException
    {
        //        Not supported
        throw new NotSupportedException( "Offline measurements are not supported by XTH Driver" );
    }

    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        try
        {
            m_Logger.finer( "creating query # samples Command" );
            m_Outgoing.putObject( new RetrieveStoredReflectanceCommand( position, RetrieveStoredReflectanceCommand.TYPE_TRIAL ) );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
//        throw new NotSupportedException("MSG_STANDARDS_UNSUPPORTED");
    }

    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Standards are not supported by XTH Driver" );
    }

    public void retrieveStandards()
        throws SpectroException
    {
//        try
//        {
//            m_Outgoing.putObject(new GetStandardListCommand());
//        }
//        catch (FifoFullException fifoFullEx)
//        {
//            throw new SpectroException("Spectrophotometer is currently busy.");
//        }
//
        //        //Not supported
        throw new NotSupportedException( "getting all Standards are not supported by XTH Driver" );
    }

    public void retrieveStandard( int position )
        throws SpectroException
    {
        try
        {
            m_Logger.finer( "creating query # standards Command" );
            m_Outgoing.putObject( new RetrieveStoredReflectanceCommand( position, RetrieveStoredReflectanceCommand.TYPE_STANDARD ) );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
//        throw new NotSupportedException("MSG_STANDARDS_UNSUPPORTED");
    }

    public void queryNoOfStoredSamples()
        throws SpectroException
    {
        try
        {
            m_Logger.finer( "creating query # samples Command" );
            m_Outgoing.putObject( new QueryNumberSamplesCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
//        throw new NotSupportedException("MSG_STANDARDS_UNSUPPORTED");
    }

    public void queryNoOfStoredStandards()
        throws SpectroException
    {
        try
        {
            m_Logger.finer( "creating query # standards Command" );
            m_Outgoing.putObject( new QueryNumberStandardsCommand() );

            m_OpStatus = OPERATIONAL_STATUS_SENDING;
            notifyStatusChange( new SpectroEvent( this ) );
        }
        catch( FifoFullException fullEx )
        {
            throw new SpectroException( "Spectrophotometer is currently busy." );
        }
//        throw new NotSupportedException("MSG_STANDARDS_UNSUPPORTED");
    }

    public SpectroSettings getSettings()
    {
        m_Logger.finer( "Returning settings" );
        return m_Settings;
    }

    public void setSettings( SpectroSettings newSettings )
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

            m_CommDriver = new GenericCommDriver();

            try
            {
                String portname = (String) commParameters.get( "PORTNAME" );
                String bitrate = (String) commParameters.get( "BITRATE" );

                m_Logger.finer( "Port name : " + portname );
                m_Logger.finer( "Bit rate : " + bitrate );

                //Baudrate is set to 9600
                m_CommDriver.openConnection( portname, 3000, 38400, CommDriver.FLOWCONTROL_RTSCTS );

                m_CommDriver.addCommDriverListener( this );

                running = true;

                try
                {
                    m_Outgoing.putObject( new ResetCommand() );
                }
                catch( FifoFullException FifoFullEx )
                {
                    m_Logger.log( Level.SEVERE, "incoming fifo should not be full: " + FifoFullEx.getMessage(), FifoFullEx );
                }

                //Attempt to synchronise and store new settings
//                if ( newSettings == null)
//                {
//                    m_Settings = new SpectroSettings();
//                    m_Settings.setAperture(new MediumAperture());
//                    m_Settings.setLightFilter(new UVIncludedLightFilter());
//                    m_Settings.setLensPosition(new MediumAreaView());
//                }
//                else
//                {
//                    m_Settings = newSettings;
//                }

                m_Thread.start();

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                m_Logger.finer( " Comm Settings complete... return" );
                return;
            }
            catch( NumberFormatException numEx )
            {
                //Try to recoved from exception and use a preset default
                //bitrate
                String portname = (String) commParameters.get( "PORTNAME" );

                try
                {
                    m_CommDriver.openConnection( portname, 3000, 38400, CommDriver.FLOWCONTROL_RTSCTS );

                    m_CommDriver.addCommDriverListener( this );
                }
                catch( CommDriverException commEx )
                {
                    //Give up.... inform user that it is not possible
                    //to open connection
                    m_Logger.finer( " FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = XTHStatus.create( "OPEN_FAILED" );
                    status.addMessage( "ERROR_OPENING " + portname );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.finer( " Comm Settings complete... return" );
                    return;
                }

                m_Thread.start();
            }
            catch( CommDriverException commEx )
            {
                //Give up... inform user that it is not possible to
                //open connection.

                m_Logger.finer( " FAILURE TO OPEN CONNECTION" );

                SpectroStatus status = XTHStatus.create( "ERROR_OPENING" );
                status.addMessage( "ERROR_OPENING " + commParameters.get( "PORTNAME" ) );

                notifyStatusChange( new SpectroEvent( this, status ) );

                m_Logger.finer( " Unable to open port... return" );
                return;
            }

            m_Logger.finer( " Should not reach this return in set settings" );
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
            m_Settings.setLightFilter( new UVIncludedLightFilter() );
            m_Settings.setLensPosition( new MediumAreaView() );
            return;
        }

        //Compare specular setting:
        m_Settings.setSpecular( newSettings.getSpecular() );

        m_Settings.setAperture( newSettings.getAperture() );

        m_Settings.setLensPosition( newSettings.getLensPosition() );

        m_Settings.setLightFilter( newSettings.getLightFilter() );

        m_Logger.finer( " Set setings done" );
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
        m_Logger.finer( " Dispose called" );

        stopThread();

        while( running )
        {
            // Wait untill thread really stop
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
            m_Incoming.removeNextObject();
            m_Logger.finer( " Interpreting > " + message );
            m_Logger.finer( " Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                m_Logger.finer( "Clearing received buffer" );
                m_Received = null;

                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.finer( " Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Decide which listener method to notify:
                if( cmd instanceof MeasureCommand )
                {
                    //Retrieve current settings of the instrument from the status string
                    m_Settings = createSettings( message );
                    notifySettingsChanged( new SpectroEvent( this ) );
                    m_Logger.finer( "Measure command " );
                    notifyMeasured( evt );
                }
                else if( cmd instanceof BlackCalibrationCommand )
                {
                    m_Logger.finer( cmd.getName() );
                    m_Logger.finer( " Notifying Calibration" );
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof WhiteCalibrationCommand )
                {
                    m_Logger.finer( cmd.getName() );
                    m_Logger.finer( " Notifying Calibration" );
                    notifyCalibrated( evt );
                }
                else if( cmd instanceof QueryNumberSamplesCommand )
                {
                    m_Logger.finer( "notifying number of samples" );
                    notifyNumberSamples( evt );
                }
                else if( cmd instanceof QueryNumberStandardsCommand )
                {
                    m_Logger.finer( "notifying number of standards" );
                    notifyNumberStandards( evt );
                }
                else if( cmd instanceof RetrieveStoredReflectanceCommand )
                {
                    m_Settings = ( (RetrieveStoredReflectanceCommand) cmd ).getSpectroSettings();
                    notifySettingsChanged( new SpectroEvent( this ) );
                    m_Logger.finer( "RetrieveStoredReflectanceCommand" );
                    if( RetrieveStoredReflectanceCommand.TYPE_STANDARD
                        .equals( ( (RetrieveStoredReflectanceCommand) cmd ).getReflectanceType() ) )
                    {
                        notifyStandardRetrieved( evt );
                    }
                    else if( RetrieveStoredReflectanceCommand.TYPE_TRIAL
                        .equals( ( (RetrieveStoredReflectanceCommand) cmd ).getReflectanceType() ) )
                    {
                        notifySampleRetrieved( evt );
                    }
                    else
                    {
                        m_Logger.severe( "wrong type chosen at command construction" );
                    }
                }
                else if( cmd instanceof ResetCommand )
                {
                    SpectroStatus status = evt.getStatus();
                    processResetCommand( status.getMessages() );
                }
                else
                {
                    //Otherwise assume an unknown response was received.
                    m_Logger.severe( "Unknown command" );
                }
            }
            else
            {
                m_Logger.warning( "event returned from command is null" );
            }
        }
        else
        {
            m_Logger.warning( "m_Incoming fifo empty" );
        }
    }

    private void processResetCommand( Collection collection )
    {
        Iterator msgList = collection.iterator();

        while( msgList.hasNext() )
        {
            String msg = (String) msgList.next();

            m_Logger.finer( "Message : " + msg );

            if( msg.matches( "SERIAL:.*" ) )
            {
                m_SerialNo = msg.substring( msg.indexOf( ":" ) + 1 );
                m_Logger.finer( "Serial Number : " + m_SerialNo );
                m_Startup = false;
                break;
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
            m_Logger.warning( "XTH Spectro: Unrecognized response received " + message );
        }
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.finer( " Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.finer( " ***************Comm Driver Received**********************" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            //System.out.println( "RECV : " + m_Received );

            String response = m_Received.toString();

            interpret( response );

            //Clear out the buffer
            m_Received = null;
            m_Logger.finer( " Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired
            m_Logger.finer( "Driver timed-out event recieved" );
            if( !m_Recovery )
            {
                m_Logger.finer( "not m_Recovery" );
                m_Recovery = true;
                m_Startup = true;

                //Store last sent command
                m_Logger.finer( "storing last command" );
                m_LastCommand = (SpectroCommand) m_Incoming.removeNextObject();
                m_Logger.finer( "last command stored, returning" );

                return;
            }
            else
            {
                //Give up

                //Generate error status:
                SpectroStatus status = XTHStatus.create( "TIMEOUT_ERROR" );

                //Assuming that the waiting command has been timed out

                //Cancel the timeout if not already cancelled
                m_CommDriver.cancelRespondTimeout();

                //Remove from FIFO
                SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

                m_Logger.finer( " Pre-empting " + cmd.getName() );

                m_Logger.finer( " Timeout received for " + cmd.getName() );
                m_Logger.finer( " Timeout received at " + System.currentTimeMillis() );

                //Insert error message
                status.addMessage( "TIMEOUT_ERROR " + cmd.getName() );

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

    private void notifyStandardRetrieved( SpectroEvent evt )
    {
        m_Logger.finer( "standard received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).retrievedStandard( evt );
        }
    }

    private void notifyNumberSamples( SpectroEvent evt )
    {
        m_Logger.finer( "sample #s received" );
        int[] indices = (int[]) evt.getEventResult();
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).numberSamplesFound( indices );
        }
    }

    private void notifyNumberStandards( SpectroEvent evt )
    {
        m_Logger.finer( "sample #s received" );
        int[] indices = (int[]) evt.getEventResult();
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).numberStandardsFound( indices );
        }
    }

    private void notifySampleRetrieved( SpectroEvent evt )
    {
        m_Logger.finer( "sample received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).retrievedSample( evt );
        }
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
        if( evt == null )
        {
            return;
        }

        m_Logger.finer( "Enter Notfiy Settings Changed" );

        Vector listeners = (Vector) m_Listeners.clone();
        Iterator list = listeners.iterator();

        while( list.hasNext() )
        {
            m_Logger.finer( "Notifying listeners" );
            m_Logger.finer( "No. of Listeners : " + m_Listeners.size() );
            ( (SpectroListener) list.next() ).settingsChanged( evt );
        }

        m_Logger.finer( "Leaving Notfiy Settings Changed" );
    }

    /**
     * Notifies registered listeners of status changes and errors
     *
     * @param evt The event or error to be triggered
     */
    public void notifyStatusChange( SpectroEvent evt )
    {
        //System.out.println( "Status change" );
        m_Logger.finer( " Status change " );//, new Exception() );
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
            SpectroCommand cmd = null;
            String message;

            if( m_StopRequest )
            {
                running = false;
                break;
            }

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

                            m_Logger.finer( "Message constructed > " + message );
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

                        m_Logger.finer( " Retreiving Command from fifo" );

                        //Retrieve the command
                        cmd = (SpectroCommand) m_Outgoing.getNextObject();

                        if( cmd == null )
                        {
                            continue;
                        }

                        //System.out.println( "Sending command " + cmd.getName() );
                        m_Logger.finer( " Constructing command" );
                        message = cmd.construct() + "\r";

                        m_Logger.finer( " Message constucted > " + message );

                        m_Logger.finer( " Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                        m_Logger.finer( " Message posted." );
                    }
                }
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                m_Logger.finer( " Spectro busy." );
                m_Logger.finer( " Command : " + m_Incoming.getNextObject() );
            }

            try
            {
                m_Thread.sleep( 300 );
            }
            catch( InterruptedException irEx )
            {
                if( m_StopRequest )
                {
                    running = false;
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

    private SpectroSettings createSettings( String statusString )
    {
        //Assuming that the status string is correct

        if( m_Settings == null )
        {
            m_Settings = new SpectroSettings();
            m_Settings.setAperture( new MediumAperture() );
            m_Settings.setLensPosition( new MediumAreaView() );
            m_Settings.setLightFilter( new UVIncludedLightFilter() );
        }

        if( statusString == null )
        {
            return m_Settings;
        }

        m_Logger.finer( " Getting settings" );

        if( statusString.charAt( 3 ) == '1' )
        {
            m_Logger.finer( " Specular set to Excluded" );
            m_Settings.setSpecular( false );
        }
        else if( statusString.charAt( 3 ) == '0' )
        {
            m_Logger.finer( " Specular set to Included" );
            m_Settings.setSpecular( true );
        }

        if( statusString.charAt( 6 ) == '2' )
        {
            m_Logger.finer( " Aperture set to MAV" );
            m_Settings.setAperture( new MediumAperture() );
            m_Settings.setLensPosition( new MediumAreaView() );
        }
        else if( statusString.charAt( 6 ) == '3' )
        {
            m_Logger.finer( " Aperture set to SAV" );
            m_Settings.setAperture( new SmallAperture() );
            m_Settings.setLensPosition( new SmallAreaView() );
        }

        return m_Settings;
    }
}
