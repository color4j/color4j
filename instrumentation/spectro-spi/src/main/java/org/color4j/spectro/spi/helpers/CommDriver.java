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

/**
 * Helper interface for Serial Communication devices. <p>
 *
 * This interface is not part of the SPI specification and may be used or not
 * used by the SpectroDriver upon their on discretion. However, the
 * GenericCommDriver, which implements this interface, is probably sufficient
 * for a majority of Spectrophotometer drivers, hence the importance of this
 * interface.</p>
 *
 * @see <{GenericCommDriver}>
 */
public interface CommDriver
{
    public final static int FLOWCONTROL_NONE = 0;
    public final static int FLOWCONTROL_RTSCTS = 1;
    public final static int FLOWCONTROL_XONXOFF = 2;

//    /**
//     *  Sends the bytes to the SerialPort. <p>
//     *
//     *  Sends the bytes to the configured SerialPort, and waits until all bytes has
//     *  been sent.
//     *
//     *@param  send  Description of the Parameter
//     *@return       true if all bytes has been sent successfully.
//     */
//    boolean send( byte[] send );

    /**
     * Description of the Method
     *
     * @param send Description of the Parameter
     */
    boolean send( byte[] send );

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    byte[] receive();

    /**
     * Adds a feature to the CommDriverListener attribute of the CommDriver object
     *
     * @param listener The feature to be added to the CommDriverListener attribute
     */
    void addCommDriverListener( CommDriverListener listener );

    /**
     * Description of the Method
     *
     * @param listener Description of the Parameter
     */
    void removeCommDriverListener( CommDriverListener listener );

    /**
     * Sets the receiveTimeout attribute of the CommDriver object
     *
     * @param timeout The new receiveTimeout value
     */
    void setReceiveTimeout( int timeout );

    /**
     * Gets the receiveTimeout attribute of the CommDriver object
     *
     * @return The receiveTimeout value
     */
    int getReceiveTimeout();

    /**
     * Sets the sendTimeout attribute of the CommDriver object
     *
     * @param timeout The new sendTimeout value
     */
    void setSendTimeout( int timeout );

    /**
     * Gets the sendTimeout attribute of the CommDriver object
     *
     * @return The sendTimeout value
     */
    int getSendTimeout();

    /**
     * Description of the Method
     *
     * @throws CommDriverException Description of the Exception
     */
    public void closeConnection()
        throws CommDriverException;

    /**
     * Description of the Method
     *
     * @param portname
     * @param timeout  Description of the Parameter
     * @param baudrate Description of the Parameter
     *
     * @throws CommDriverException Description of the Exception
     */
    public void openConnection( String portname, int timeout, int baudrate )
        throws CommDriverException;

    /**
     * Reports the open status of the port.
     *
     * @return true if port is open, false if port is closed.
     */
    public boolean isOpen();

    /**
     * Description of the Method
     *
     * @param event Description of the Parameter
     */
    public void received( CommDriverEvent event );

    /**
     * Description of the Method
     *
     * @param event Description of the Parameter
     */
    public void sent( CommDriverEvent event );

    /**
     * Opens a serial communication port.
     *
     * @param portname    The name of the port to be opened.
     * @param timeout     The timeout to use for communication on the port.
     * @param bitrate     The bitrate to use on the for the connection.
     * @param flowcontrol The flow control to be used by the driver in the connection.
     *
     * @throws CommDriverException
     */
    public void openConnection( String portname, int timeout, int bitrate, int flowcontrol )
        throws CommDriverException;

    public void setRespondTimeout( int timeout );

    public void cancelRespondTimeout();

    /*
    *  #CommStatusListener lnkCommStatusListener;
    */
    /*
     *  #CommStatusEvent lnkCommStatusEvent;
     */
    /*
     *  #CommDriverEvent lnkCommDriverEvent;
     */
    /*
     *  #CommDriverListener lnkCommDriverListener;
     */
}
