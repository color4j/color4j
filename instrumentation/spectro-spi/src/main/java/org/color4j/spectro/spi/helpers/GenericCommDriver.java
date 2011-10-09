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
package org.color4j.spectro.spi.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.comm.CommPortIdentifier;
import javax.comm.CommPortOwnershipListener;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

/**
 * Generic CommDriver for SerialPorts.
 * <p/>
 * <p/>
 * The GenericCommDriver is a generic implementation of the CommDriver interface, available to Spectrophotometers who
 * follow this simple and straight forward communication style. </p>
 *
 * @author chc @created May 30, 2002
 */
public class GenericCommDriver implements CommDriver, SerialPortEventListener,
                                          CommPortOwnershipListener
{
    private static Logger m_Logger = Logger.getLogger( GenericCommDriver.class.getName() );

    private OutputStream m_os;

    private InputStream m_is;

    private CommPortIdentifier m_portId;

    private SerialPort m_sPort;

    private boolean m_open = false;

    private String m_result = "empty";

    // Default settings for timout
    private int m_ReceiveTO;

    private int m_SendTO;

    private ArrayList m_Listeners;

    private Timer m_timer;

    public GenericCommDriver()
    {
        m_ReceiveTO = 1000;
        m_SendTO = 1000;
    }

    public boolean send( byte[] send )
    {
        try
        {
            // KH Mar 10, 2004 - triggers a serialevent
            m_Logger.finer( "calling send: command == " + new String( send ) );//, new Exception());
            m_os.write( send );
            fireCommDriverEventReceived( new CommDriverEvent( this,
                                                              CommDriverEvent.COMM_DRIVER_SENDING ) );
            return true;
        }
        catch( IOException e )
        {
            m_Logger.log( Level.SEVERE, e.getMessage(), e );
            return false;
        }
    }

    public byte[] receive()
    {
        return m_result.getBytes();
    }

    /**
     * Adds a feature to the CommDriverListener attribute of the GenericCommDriver object
     *
     * @param listener The feature to be added to the CommDriverListener attribute
     */
    public void addCommDriverListener( CommDriverListener listener )
    {
        synchronized( this )
        {
            ArrayList v;
            if( m_Listeners == null )
            {
                v = new ArrayList();
            }
            else
            {
                v = (ArrayList) m_Listeners.clone();
            }
            v.add( listener );
            m_Listeners = v;
        }
    }

    public void removeCommDriverListener( CommDriverListener listener )
    {
        if( m_Listeners == null )
        {
            return;
        }
        synchronized( this )
        {
            ArrayList v = (ArrayList) m_Listeners.clone();
            v.remove( listener );
            m_Listeners = v;

            /*
             * if( v.size() == 0 ) m_Listeners = null; else m_Listeners = v;
             */
        }
    }

    private void fireCommDriverEventReceived( CommDriverEvent event )
    {
        m_Logger.finer( "firing event recieved" );//, new Exception() );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (CommDriverListener) list.next() ).received( event );
        }
    }

    private void fireCommDriverEventSent( CommDriverEvent event )
    {
        m_Logger.finer( "firing event sent" );//, new Exception() );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (CommDriverListener) list.next() ).sent( event );
        }
    }

    /**
     * Sets the receiveTimeout attribute of the GenericCommDriver object
     *
     * @param timeout The new receiveTimeout value
     */
    public void setReceiveTimeout( int timeout )
    {
        m_ReceiveTO = timeout;
    }

    /**
     * Gets the receiveTimeout attribute of the GenericCommDriver object
     *
     * @return The receiveTimeout value
     */
    public int getReceiveTimeout()
    {
        return m_ReceiveTO;
    }

    /**
     * Sets the sendTimeout attribute of the GenericCommDriver object
     *
     * @param timeout The new sendTimeout value
     */
    public void setSendTimeout( int timeout )
    {
        m_SendTO = timeout;
    }

    /**
     * Gets the sendTimeout attribute of the GenericCommDriver object
     *
     * @return The sendTimeout value
     */
    public int getSendTimeout()
    {
        return m_SendTO;
    }

    public void openConnection( String portname, int timeout, int baudrate )
        throws CommDriverException
    {
        // Obtain a CommPortIdentifier object for the port you want to open.
        try
        {
            m_portId = CommPortIdentifier.getPortIdentifier( portname );
        }
        catch( NoSuchPortException e )
        {
            throw new CommDriverException( e.toString() );
        }

        // Open the port represented by the CommPortIdentifier object. Give
        // the open call a relatively long timeout of m_tinmeout seconds to
        // allow
        // a different application to reliquish the port if the user
        // wants to.
        try
        {
            m_sPort = (SerialPort) m_portId.open( "Spectro", timeout );
        }
        catch( PortInUseException e )
        {
            throw new CommDriverException( e.toString() );
        }

        // Set connection parameters
        try
        {
            m_sPort.setSerialPortParams( baudrate, SerialPort.DATABITS_8,
                                         SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
        }
        catch( UnsupportedCommOperationException e )
        {
            m_sPort.close();
            throw new CommDriverException( e.toString() );
        }

        // Open the input and output streams for the connection. If they won't
        // open, close the port before throwing an exception.
        try
        {
            m_os = m_sPort.getOutputStream();
            m_is = m_sPort.getInputStream();
        }
        catch( IOException e )
        {
            m_sPort.close();
            throw new CommDriverException( "Error opening i/o streams" );
        }

        // Add this object as an event listener for the serial port.
        try
        {
            m_sPort.addEventListener( this );
        }
        catch( TooManyListenersException e )
        {
            m_sPort.close();
            throw new CommDriverException( "too many listeners added" );
        }

        // Set notifyOnDataAvailable to true to allow event driven input.
        m_sPort.notifyOnDataAvailable( true );
        m_sPort.notifyOnOutputEmpty( true );

        // Set receive timeout to allow breaking out of polling loop during
        // input handling.
        try
        {
            m_sPort.enableReceiveTimeout( m_ReceiveTO );
        }
        catch( UnsupportedCommOperationException e )
        {
        }

        // Add ownership listener to allow ownership event handling.
        m_portId.addPortOwnershipListener( this );

        m_open = true;
    }

    /**
     * Close the port connection
     *
     * @throws CommDriverException Description of the Exception
     */
    public void closeConnection()
        throws CommDriverException
    {
        // If port is alread closed just return.
        if( !m_open )
        {
            return;
        }

        // Check to make sure sPort has reference to avoid a NPE.
        if( m_sPort != null )
        {
            try
            {
                // close the i/o streams.
                m_os.close();
                m_is.close();
            }
            catch( IOException e )
            {
                throw new CommDriverException( e.toString() );
            }

            // Close the port.
            m_sPort.close();

            // Remove the ownership listener.
            m_portId.removePortOwnershipListener( this );
        }

        m_open = false;
    }

    /**
     * Reports the open status of the port.
     *
     * @return true if port is open, false if port is closed.
     */
    public boolean isOpen()
    {
        return m_open;
    }

    //////// SerialPortListener interface

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void serialEvent( SerialPortEvent e )
    {
        m_Logger.finer( "serial event recieved: " + e.getEventType() );//, new Exception() );
        // Create a StringBuffer and int to receive input data.
        StringBuffer m_inputBuffer = new StringBuffer();
        int m_newData = 0;

        // Determine type of event.
        switch( e.getEventType() )
        {
        // Read data until -1 is returned. If \r is received substitute
        // \n for correct newline handling.
        case SerialPortEvent.DATA_AVAILABLE:
            while( m_newData != -1 )
            {
                try
                {
                    m_newData = m_is.read();
                    if( m_newData == -1 )
                    {
                        break;
                    }
                    m_inputBuffer.append( (char) m_newData );
                }
                catch( IOException ex )
                {
                    m_Logger.log( Level.SEVERE, ex.getMessage(), ex );
                    return;
                }
            }

            // Append received data to messageAreaIn.

            m_result = new String( m_inputBuffer ).trim();
            fireCommDriverEventReceived( new CommDriverEvent( this,
                                                              CommDriverEvent.COMM_DRIVER_RECEIVED ) );
            break;

        case SerialPortEvent.BI:
            break;

        case SerialPortEvent.CD:
            //Handle a carrier detect... probably never used
            break;

        case SerialPortEvent.CTS:
            //Handle a clear to send event
            break;

        case SerialPortEvent.DSR:
            //Handle a data set ready event
            break;

        case SerialPortEvent.FE:
            //Handle a Framing error event
            break;

        case SerialPortEvent.OE:
            //Handle a overrun error event
            //System.out.println( "Overrun Error." );
            break;

        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            //Handle an output buffer empty event
            fireCommDriverEventSent( new CommDriverEvent( this,
                                                          CommDriverEvent.COMM_DRIVER_SENT ) );
            break;

        case SerialPortEvent.PE:
            //Handle a parity error event... possibly error recovery?
            break;

        case SerialPortEvent.RI:
            //Handle a ring indicator event... probably never user.
            break;
        }
    }

    public void ownershipChange( int type )
    {
        if( type == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED )
        {
            try
            {
                closeConnection();
            }
            catch( Exception e )
            {
            }
        }
    }

    // Received and sent method for unknown prupose.
    public void received( CommDriverEvent event )
    {
    }

    public void sent( CommDriverEvent event )
    {
    }

    public void openConnection(
        String portname, int timeout,
        int baudrate, int flowcontrol
    )
        throws CommDriverException
    {
        // Obtain a CommPortIdentifier object for the port you want to open.
        try
        {
            m_portId = CommPortIdentifier.getPortIdentifier( portname );
        }
        catch( NoSuchPortException e )
        {
            throw new CommDriverException( e.toString() );
        }

        // Open the port represented by the CommPortIdentifier object. Give
        // the open call a relatively long timeout of m_tinmeout seconds to
        // allow
        // a different application to reliquish the port if the user
        // wants to.
        try
        {
            m_sPort = (SerialPort) m_portId.open( "Spectro", timeout );
        }
        catch( PortInUseException e )
        {
            throw new CommDriverException( e.toString() );
        }

        // Set connection parameters
        try
        {
            m_sPort.setSerialPortParams( baudrate, SerialPort.DATABITS_8,
                                         SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
        }
        catch( UnsupportedCommOperationException e )
        {
            m_sPort.close();
            throw new CommDriverException( e.toString() );
        }

        // set up the flow control
        setupFlowControl( flowcontrol );

        // Open the input and output streams for the connection. If they won't
        // open, close the port before throwing an exception.
        try
        {
            m_os = m_sPort.getOutputStream();
            m_is = m_sPort.getInputStream();
        }
        catch( IOException e )
        {
            m_sPort.close();
            throw new CommDriverException( "Error opening i/o streams" );
        }

        // Add this object as an event listener for the serial port.
        try
        {
            m_sPort.addEventListener( this );
        }
        catch( TooManyListenersException e )
        {
            m_sPort.close();
            throw new CommDriverException( "too many listeners added" );
        }

        // Set notifyOnDataAvailable to true to allow event driven input.
        m_sPort.notifyOnDataAvailable( true );
        m_sPort.notifyOnOutputEmpty( true );

        // Set receive timeout to allow breaking out of polling loop during
        // input handling.
        try
        {
            m_sPort.enableReceiveTimeout( m_ReceiveTO );
        }
        catch( UnsupportedCommOperationException e )
        {
        }

        // Add ownership listener to allow ownership event handling.
        m_portId.addPortOwnershipListener( this );

        m_open = true;
    }

    private void setupFlowControl( int flowcontrol )
        throws CommDriverException
    {
        if( flowcontrol < 0 )
        {
            return;
        }
        try
        {
            if( flowcontrol == CommDriver.FLOWCONTROL_NONE )
            {
                m_sPort.setFlowControlMode( SerialPort.FLOWCONTROL_NONE );
            }
            else if( flowcontrol == CommDriver.FLOWCONTROL_RTSCTS )
            {
                m_sPort.setFlowControlMode( SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT );
            }
            else if( flowcontrol == CommDriver.FLOWCONTROL_XONXOFF )
            {
                m_sPort.setFlowControlMode( SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT );
            }
            else
            {
                throw new CommDriverException( "Invalid flow control type" );
            }
        }
        catch( UnsupportedCommOperationException e )
        {
            throw new CommDriverException( e.toString() );
        }
    }

    // Implementation of Timer to timeout
    public void setRespondTimeout( int timeout )
    {
        m_timer = new Timer();
        m_timer.schedule( new RespondTimeOutTask(), timeout );
    }

    public void cancelRespondTimeout()
    {
        if( m_timer != null )
        {
            m_timer.cancel();
        }
    }

    class RespondTimeOutTask extends TimerTask
    {
        public void run()
        {
            m_timer.cancel();
            fireCommDriverEventReceived( new CommDriverEvent( this,
                                                              CommDriverEvent.COMM_DRIVER_TIMEOUT ) );
        }
    }
}
