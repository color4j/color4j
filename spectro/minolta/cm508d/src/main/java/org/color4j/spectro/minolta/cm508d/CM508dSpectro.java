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

package org.color4j.spectro.minolta.cm508d;

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

public class CM508dSpectro implements Spectrophotometer, CommDriverListener, Runnable
{
    static private Logger m_Logger = Logger.getLogger( CM508dSpectro.class.getName() );

    static
    {
    }

    protected CommDriver m_CommDriver;
    protected Fifo m_Incoming; // Expected incoming responses
    protected Fifo m_Outgoing; // Queued commands to be sent

    protected SpectroSettings m_Settings; //The current settings of the instrument
    protected SpectroSettings m_newSettings; // The new settings for the instrument
    protected SpectroStatus m_LastStatus; // The last received status

    protected SpectroReading storedMeasurement;

    protected String m_SerialNo; //Serial number of the instrument, none for the CM508d

    protected Vector m_Listeners; //Collection of Spectrolisteners listening to this

    protected int m_OpStatus; //The operational status of the spectro implementation

    protected StringBuffer m_Received; //The buffer of received bytes

    protected boolean running; // Switch for the running thread
    protected boolean stopRequest = false;

    protected int noresult;
    protected int finalmeasure = 7;

    protected Thread m_Thread;

    // Additional parameter for CM508d

    public CM508dSpectro()
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
            m_Outgoing.putObject( new STRCommand() );
            m_Outgoing.putObject( new MESCommand() );

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

            case 0: //Zero Calibration
                m_Outgoing.putObject( new STRCommand() );
                m_Outgoing.putObject( new UZCCommand() );
                break;

            case 1: //White Calibration
                m_Outgoing.putObject( new STRCommand() );
                m_Outgoing.putObject( new CALCommand() );
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
            m_Logger.info( "CM508d Spectro : Set settings called" );

            if( newSettings == null )
            {
                m_Logger.info( "CM508d Spectro : Null settings enterred" );
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
                    m_CommDriver.openConnection( portname, 3000, 9600, CommDriver.FLOWCONTROL_XONXOFF );

                    m_CommDriver.addCommDriverListener( this );

                    try
                    {
                        // Check whether is a Minolta CM508d or not
                        m_Outgoing.putObject( new IDRCommand() );

                        m_Outgoing.putObject( new STRCommand() );
                    }
                    catch( FifoFullException fullEx )
                    {
                        m_Logger.info( "Setting up not successfull" );
                        //throw new SpectroException ( "Spectrophotometer is currently busy." );
                    }

                    running = true;

                    m_Thread.start();

                    m_Logger.info( "CM508d Spectro : Comm Settings complete... return" );
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
                        m_Logger.info( "CM508d Spectro : FAILURE TO OPEN CONNECTION" );

                        SpectroStatus status = CM508dStatus.create( "OPEN_FAILED" );
                        status.addMessage( "MSG_ERROR_OPENING_PORT" );

                        notifyStatusChange( new SpectroEvent( this, status ) );

                        m_Logger.info( "CM508d Spectro : Comm Settings complete... return" );
                        return;
                    }

                    //newSettings.setSpecular ( true );

                    m_Thread.start();
                }
                catch( CommDriverException commEx )
                {
                    //Give up... inform user that it is not possible to
                    //open connection.

                    m_Logger.info( "CM508d Spectro : FAILURE TO OPEN CONNECTION" );

                    SpectroStatus status = CM508dStatus.create( "ERROR_OPENING" );
                    status.addMessage( "MSG_ERROR_OPENING_PORT" );

                    notifyStatusChange( new SpectroEvent( this, status ) );

                    m_Logger.info( "CM508d Spectro : Unable to open port... return" );
                    return;
                }

                m_Logger.info( "CM508d Spectro : Should not reach this return in set settings" );
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

            //m_Settings.setSpecular ( true );
            //m_Settings.setLensPosition ( new SmallAreaView () );
            //m_Settings.setAperture ( new SmallAperture () );

            m_Logger.info( "CM508d Spectro : Set setings done" );
        }
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
        m_Logger.info( "CM508d Spectro : Dispose called" );

        stopThread();

        while( running )
        {
            // Wait untill thread stop
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
            m_Logger.info( "CM508d Spectro : Thread still running" );
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
        m_Logger.info( "CM508d Spectro : Interpreting command" );
        m_Logger.info( "CM508d Spectro : " + message );

        //Assuming there's a command standing by for a response
        if( !m_Incoming.isEmpty() )
        {
            SpectroCommand cmd = (SpectroCommand) m_Incoming.getNextObject();

            m_Logger.info( "CM508d Spectro : Interpreting > " + message );
            m_Logger.info( "CM508d Spectro : Interpreting as a " + cmd.getName() + " command" );

            //Get the command to interpret the message according to it's expected data
            //format
            SpectroEvent evt = cmd.interpret( message.getBytes() );

            //If a spectroevent is returned then it is assumed that the interpretation
            //is complete without error
            if( evt != null )
            {
                //Stop timeout timer
                m_CommDriver.cancelRespondTimeout();
                m_Logger.info( "CM508d Spectro : Cancelling timeout at " + System.currentTimeMillis() );

                m_OpStatus = OPERATIONAL_STATUS_IDLE;
                notifyStatusChange( new SpectroEvent( this ) );

                //Retrieve current settings of the instrument from the status string
                m_Settings = createSettings( message );

                //Decide which listener method to notify:
                if( cmd instanceof MESCommand )
                {
                    m_Logger.info( "CM508d Spectro : Measure Command" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM508d Spectro : Removing Measure Command from Incoming" );

                    notifyMeasured( evt );
                    return;
                }
                else if( cmd instanceof UZCCommand )
                {
                    m_Logger.info( "CM508d Spectro : User Zero Calibration Command" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM508d Spectro : Removing User Zero Calibration Command from Incoming" );

                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof CALCommand )
                {
                    m_Logger.info( "CM508d Spectro : White Calibration Command" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM508d Spectro : Removing White Calibration Command from Incoming" );

                    notifyCalibrated( evt );
                    return;
                }
                else if( cmd instanceof IDRCommand )
                {
                    m_Logger.info( "CM508d Spectro : Instrument Identification Request Command" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM508d Spectro : Instrument Identification Request Command from Incoming" );

                    //notifyStatusChange ( evt );
                    return;
                }
                else if( cmd instanceof STRCommand )
                {
                    m_Logger.info( "CM508d Spectro : Request Status Command" );
                    m_Incoming.removeNextObject();
                    m_Logger.info( "CM508d Spectro : Request Status Command from Incoming" );

                    m_Settings.setSpecular( ( (STRCommand) cmd ).getSpecular() );

                    notifySettingsChanged( evt );
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
                /*try
                {
                    guessInterpret ( message );
                } catch ( SpectroException ep )
                {
                }*/
            }
        }

        //If the method hasn't returned then the command is not expected or unknown.
        //Guess interpret it.
        /*try
        {
            guessInterpret ( message );
        } catch ( SpectroException ep )
        {
        }*/
    }

    /*protected void guessInterpret ( String message ) throws SpectroException
    {
        SpectroEvent buttonmeasure = null;
        
        buttonmeasure = new ExecMeasurementCommand ().interpret ( message.getBytes () );
        
        if ( buttonmeasure != null )
        {
            storedMeasurement = buttonmeasure.getReading ();
            
            try
            {
                m_Outgoing.putObject ( new ParameterRequestCommand () );
                
                m_OpStatus = OPERATIONAL_STATUS_SENDING;
                notifyStatusChange ( new SpectroEvent ( this ) );
            }
            catch( FifoFullException fullEx )
            {
                throw new SpectroException ( "MSG_SPECTRO_BUSY" );
            }
        }
    }*/

    public void received( CommDriverEvent evt )
    {
        m_Logger.info( "CM508d Spectro : Received event from CommDriver" );

        //Indication of Data Available in the input stream
        if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_RECEIVED )
        {
            m_Logger.info( "CM508d Spectro : Comm Driver Received" );

            if( m_Received == null )
            {
                m_Received = new StringBuffer();
            }

            //Append available data to the local buffer
            m_Received.append( new String( m_CommDriver.receive() ) );

            m_Logger.info( "CM508d Spectro : Before clearing > " + m_Received );

            String response = m_Received.toString();

            if( m_Received.length() > 0 )
            {
                interpret( response );
            }

            //Clear out the buffer
            m_Received = null;

            m_Logger.info( "CM508d Spectro : Buffer > " + m_Received );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_TIMEOUT )
        {
            //Timeout period for the waiting command has expired

            //Generate error status:
            SpectroStatus status = CM508dStatus.create( "TIMEOUT_ERROR" );

            //Assuming that the waiting command has been timed out

            //Cancel the timeout if not already cancelled
            m_CommDriver.cancelRespondTimeout();

            //Remove from FIFO
            SpectroCommand cmd = (SpectroCommand) m_Incoming.removeNextObject();

            //m_Logger.info ( "CM508d Spectro : Pre-empting " + cmd.getName () );

            m_Logger.info( "CM508d Spectro : Timeout received for " + cmd.getName() );
            m_Logger.info( "CM508d Spectro : Timeout received at " + System.currentTimeMillis() );

            //Insert error message
            status.addMessage( "MSG_TIMEOUT_ERROR" );

            //Notify time out
            notifyStatusChange( new SpectroEvent( this, status ) );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENDING )
        {
            //Notify sending command -- Command written to output buffer
            m_Logger.info( "CM508d Spectro : Comm Driver Sending" );
        }
        else if( evt.getStatus() == CommDriverEvent.COMM_DRIVER_SENT )
        {
            //Notify sent command -- Output buffer/stream is empty
            m_Logger.info( "CM508d Spectro : Comm Driver Sent" );
        }
        else
        {
            //Should not happen : Unknown comm status event
            m_Logger.info( "CM508d Spectro : Unrecognized Comm Driver Event" );
        }
    }

    public void sent( CommDriverEvent evt )
    {
        m_Logger.info( "CM508d Spectro : Sent event from CommDriver" );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        m_Logger.info( "CM508d Spectro : Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        m_Logger.info( "CM508d Spectro : Calibration received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    public void notifySettingsChanged( SpectroEvent evt )
    {
        m_Logger.info( "CM508d Spectro : Settings Ack received" );
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
        m_Logger.info( "CM508d Spectro : Status change " );
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
                m_Logger.info( "CM508d Spectro : Thread still running" );
                m_Logger.info( "CM508d Spectro : Retreiving Command from fifo" );

                //Retrieve the command
                SpectroCommand cmd = (SpectroCommand) m_Outgoing.getNextObject();

                if( cmd == null )
                {
                    continue;
                }

                m_Logger.info( "CM508d Spectro : Constructing command" );
                String message = cmd.construct();

                message = message + "\r\n";

                m_Logger.info( "CM508d Spectro : Message constucted > " + message );

                m_Logger.info( "CM508d Spectro : Sending " + cmd.getName() + " at " + System.currentTimeMillis() );
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

                m_Logger.info( "CM508d Spectro : Message posted." );
            }
            else
            {
                //Incoming FIFO is full, spectro is currently busy
                //m_Logger.info( "CM508d Spectro : Spectro busy." );
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
        m_Logger.info( "CM508d Spectro : Thread stopped." );
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

        m_Settings.setAperture( new StandardAperture() );
        m_Settings.setLensPosition( new StandardAreaView() );
        m_Settings.setLightFilter( new UVIncludedLightFilter() );

        return m_Settings;
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {

    }

    public String getSerialNo()
    {
        return m_SerialNo;
    }
}


