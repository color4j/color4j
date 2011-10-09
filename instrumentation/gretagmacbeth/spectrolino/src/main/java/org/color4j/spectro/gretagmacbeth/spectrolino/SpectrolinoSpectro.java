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

package org.color4j.spectro.gretagmacbeth.spectrolino;

import java.util.Iterator;
import java.util.Map;
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

public class SpectrolinoSpectro
    implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger = Logger.getLogger( SpectrolinoSpectro.class.getName() );

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the instrument
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected SpectroReading storedMeasurement;

    protected String m_SerialNo; //Serial number of the instrument, none for the Spectrolino

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this

    protected int m_OpStatus; //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false;

    protected int noresult;
    protected int finalmeasure = 7;

    protected Thread m_Thread;

    // Additional parameter for spectrolino
    protected String m_Density;
    protected String m_Illum;
    protected String m_Observer;
    protected String m_ActualFilter;

    public SpectrolinoSpectro()
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
            m_Outgoing.putObject( new ParameterRequestCommand() );
            m_Outgoing.putObject( new ExecMeasurementCommand() );

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

            case 0: //White Calibration
                //m_Logger.info ( "VALUES FOR ParamterDownloadCommand after : "+m_Density+","+m_Illum+","+ m_Observer );
                m_Outgoing.putObject( new ParameterDownloadCommand( m_Density, m_Illum, m_Observer ) );
                m_Outgoing.putObject( new ExecRefMeasurementCommand() );
                m_Outgoing.putObject( new ParameterRequestCommand() );

                m_Outgoing.putObject( new ExecRefMeasurementCommand() );
                m_Outgoing.putObject( new ParameterRequestCommand() );
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
            m_Logger.info( "Spectrolino Spectro : Set settings called" );

            if( newSettings == null )
            {
                m_Logger.info( "Spectrolino Spectro : Null settings enterred" );
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
                    m_CommDriver.openConnection( portname, 3000, 19200, CommDriver.FLOWCONTROL_XONXOFF );

                    m_CommDriver.addCommDriverListener( this );

                    try
                    {
                        m_Outgoing.putObject( new ResetStatusDownloadCommand() );
                        m_Outgoing.putObject( new ParameterRequestCommand() );
                        m_Outgoing.putObject( new DeviceDataRequestCommand() );
                        m_Outgoing.putObject( new SetMeasurementOutputCommand() );
                        // Now only set to reflectance mode
                        m_Outgoing.putObject( new MeasControlDownloadCommand( "155" ) );
                    }
                    catch( FifoFullException fullEx )
                    {
                        m_Logger.info( "Setting up not successfull" );
                        //throw new SpectroException ( "Spectrophotometer is currently busy." );
                    }

                    running = true;

                    m_Thread.start();

                    m_Logger.info( "Spectrolino Spectro : Comm Settings complete... return" );
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
                        m_Logger.info( "Spectrolino Spectro : FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = SpectrolinoStatus.create( "OPEN_FAILED" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.info( "Spectrolino Spectro : Comm Settings complete... return" );
                        return;
                    }

                    newSettings.setSpecular( true );

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.info( "Spectrolino Spectro : FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = SpectrolinoStatus.create( "ERROR_OPENING" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.info( "Spectrolino Spectro : Unable to open port... return" );
                    return;
                }

                m_Logger.info( "Spectrolino Spectro : Should not reach this return in set settings" );
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

            m_Settings.setSpecular( true );
            m_Settings.setLensPosition( new SmallAreaView() );
            m_Settings.setAperture( new SmallAperture() );

            m_Logger.info( "Spectrolino Spectro : Set setings done" );
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
        m_Logger.info( "Spectrolino Spectro : Dispose called" );

        stopThread();

        while( running )
        {
            // wait untill thread really stop
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
            m_Logger.info( "Spectrolino Spectro : Thread still running" );
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
    }

    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    protected void interpret( String message )
    {
        m_Logger.info( "Spectrolino Spectro : Interpreting command" );
        m_Logger.info( "Spectrolino Spectro : " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.info( "Spectrolino Spectro : Interpreting > " + message );
            m_Logger.info( "Spectrolino Spectro : Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.info( "Spectrolino Spectro : Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                m_Settings = createSettings( message );

                //Decide which listener method to notify:
                if( cmd instanceof DeviceDataRequestCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : DeviceDataRequest command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing DeviceDataRequest command from Incoming" );

                    // Now fetch the serial no out
                    m_SerialNo = ( (DeviceDataRequestCommand) cmd ).getSerialNo();

                    SpectroStatus status = SpectrolinoStatus.create( "SUCCESS" );
                    status.addMessage( "MSG_SERIAL_NO" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    //notifyStatusChange ( evt );
                    return;
                }
                else if( cmd instanceof ExecMeasurementCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : ExecMeasurement command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing ExecMeasurement command from Incoming" );

                    Map values = evt.getReading().getValues();
                    SpectroStatus status = evt.getReading().getStatus();

                    SpectrolinoReading newreading = new SpectrolinoReading( status, m_Settings, values );

                    SpectroEvent newevt = new SpectroEvent( this, newreading );
                    notifyMeasured( newevt );
                    return;
                }
                else if( cmd instanceof ExecRefMeasurementCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : ExecRefMeasurement command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing ExecRefMeasurement command from Incoming" );
                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof MeasControlDownloadCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : MeasControlDownload command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing MeasControlDownload command from Incoming" );

                    notifyStatusChange( evt );
                    return;
                }
                else if( cmd instanceof ParameterDownloadCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : ParameterDownload command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing ParameterDownload command from Incoming" );

                    notifyStatusChange( evt );
                    return;
                }
                else if( cmd instanceof ParameterRequestCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : ParameterRequest command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing ParameterRequest command from Incoming" );

                    // Now get the extra stuffs
                    m_Density = ( (ParameterRequestCommand) cmd ).getDensity();
                    m_Illum = ( (ParameterRequestCommand) cmd ).getIllum();
                    m_Observer = ( (ParameterRequestCommand) cmd ).getObserver();
                    m_ActualFilter = ( (ParameterRequestCommand) cmd ).getActualFilter();

                    m_Logger.info( "VALUES FOR ParamterDownloadCommand begin : " + m_Density + "," + m_Illum + "," + m_Observer + "," + m_ActualFilter );

                    if( m_ActualFilter.equals( "1" ) )
                    {
                        m_Settings.setLightFilter( new UVIncludedLightFilter() );
                    }
                    else if( m_ActualFilter.equals( "2" ) )
                    {
                        m_Settings.setLightFilter( new PolarizerLightFilter() );
                    }
                    else if( m_ActualFilter.equals( "3" ) )
                    {
                        m_Settings.setLightFilter( new D65LightFilter() );
                    }
                    else if( m_ActualFilter.equals( "5" ) )
                    {
                        m_Settings.setLightFilter( new UVExcludedLightFilter() );
                    }
                    else if( m_ActualFilter.equals( "6" ) )
                    {
                        m_Settings.setLightFilter( new CustomLightFilter() );
                    }

                    notifySettingsChanged( evt );

                    if( storedMeasurement != null )
                    {
                        SpectroEvent autoMeasurement = new SpectroEvent( this, new SpectrolinoReading( storedMeasurement
                                                                                                           .getStatus(), m_Settings, storedMeasurement
                            .getValues() ) );

                        notifyMeasured( autoMeasurement );

                        storedMeasurement = null;
                    }

                    //notifyStatusChange ( evt );
                    return;
                }
                else if( cmd instanceof ResetStatusDownloadCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : ResetStatusDownload command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing ResetStatusDownload command from Incoming" );

                    notifyStatusChange( evt );
                    return;
                }
                else if( cmd instanceof SetMeasurementOutputCommand )
                {
                    m_Logger.info( "Spectrolino Spectro : SetMeasurementOutput command " );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "Spectrolino Spectro : Removing SetMeasurementOutput command from Incoming" );
                    //notifyMeasured ( evt );
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
                try
                {
                    guessInterpret( message );
                }
                catch( SpectroException ep )
                {
                }
            }
        }

        //If the method hasn't returned then the command is not expected or unknown.
        //Guess interpret it.
        try
        {
            guessInterpret( message );
        }
        catch( SpectroException ep )
        {
        }
    }

    protected void guessInterpret( String message )
        throws SpectroException
    {
        SpectroEvent buttonmeasure = null;

        buttonmeasure = new ExecMeasurementCommand().interpret( message.getBytes() );

        if( buttonmeasure != null )
        {
            storedMeasurement = buttonmeasure.getReading();

            try
            {
                m_Outgoing.putObject( new ParameterRequestCommand() );

                m_OpStatus = OPERATIONAL_STATUS_SENDING;
                notifyStatusChange( new SpectroEvent( this ) );
            }
            catch( FifoFullException fullEx )
            {
                throw new SpectroException( "MSG_BUSY" );
            }
        }
    }

    public void received( CommDriverEvent evt )
    {
        m_Logger.info( "Spectrolino Spectro : Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.info( "Spectrolino Spectro : Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.info( "Spectrolino Spectro : Before clearing > " + m_Received );

            String response = m_Received.toString();

            if( m_Received.length() > 0 )
            {
                interpret( response );
            }

            //Clear out the buffer
            m_Received = null;

            m_Logger.info( "Spectrolino Spectro : Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = SpectrolinoStatus.create( "TIMEOUT_ERROR" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            //m_Logger.info ( "Spectrolino Spectro : Pre-empting " + cmd.getName () );

            m_Logger.info( "Spectrolino Spectro : Timeout received for " + cmd.getName() );
            m_Logger.info( "Spectrolino Spectro : Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.info( "Spectrolino Spectro : Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.info( "Spectrolino Spectro : Comm Driver Sent" );
        }
        else
        {
            //Should not happen : Unknown comm status event
            m_Logger.info( "Spectrolino Spectro : Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.info( "Spectrolino Spectro : Sent event from CommDriver" );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.info( "Spectrolino Spectro : Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.info( "Spectrolino Spectro : Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.info( "Spectrolino Spectro : Settings Ack received" );
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
        m_Logger.info( "Spectrolino Spectro : Status change " );
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
                m_Logger.info( "Spectrolino Spectro : Thread still running" );
                m_Logger.info( "Spectrolino Spectro : Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                m_Logger.info( "Spectrolino Spectro : Constructing command" );

                if( cmd == null )
                {
                    continue;
                }

                String message = cmd.construct();

                message = message + "\r\n";

                m_Logger.info( "Spectrolino Spectro : Message constucted > " + message );

                m_Logger.info( "Spectrolino Spectro : Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                m_Logger.info( "Spectrolino Spectro : Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //m_Logger.info( "Spectrolino Spectro : Spectro busy." );
            }

            try
            {
                m_Thread.sleep( 300 );
            }
            catch( InterruptedException inex )
            {
                if( stopRequest )
                {
                    running = false;
                    break;
                }
            }
        }

        //Indicate that the thread has stopped running
        m_Logger.info( "Spectrolino Spectro : Thread stopped." );
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

        m_Settings.setSpecular( true );
        m_Settings.setLensPosition( new SmallAreaView() );
        m_Settings.setAperture( new SmallAperture() );

        /*if ( statusString.charAt ( 3 ) == '1' )
        {
            m_Logger.info ( "Spectrolino Spectro : Specular set to Excluded" );
            m_Settings.setSpecular ( false );
        }
        else if ( statusString.charAt ( 3 ) == '0' )
        {
            m_Logger.info ( "Spectrolino Spectro : Specular set to Included" );
            m_Settings.setSpecular ( true );
        }*/

        return m_Settings;
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {

    }
}


