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

package org.color4j.spectro.drivers.manual;

import java.util.Iterator;
import java.util.Vector;
import org.color4j.spectro.spi.NotSupportedException;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.SpectroListener;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;
import org.color4j.spectro.spi.Spectrophotometer;

/**
 * Spectrophotometer object that supports
 * the functionality of manually entering
 * reflectance data.
 */
public class ManualSpectro
    implements ManualReadingListener, Spectrophotometer
{
    /**
     * Constructs and initialize the spectrophotometer.
     */

    protected SpectroSettings m_Settings;
    protected SpectroStatus m_LastStatus;
    protected String m_SerialNo;

    //protected Vector m_PendingReadings;
    protected Vector m_Listeners;

    protected int m_OpStatus;

    public ManualSpectro()
    {
        m_Listeners = new Vector();
        //m_PendingReadings = new Vector();
        m_SerialNo = "ManualEmulator Ver 1.0";
        m_OpStatus = OPERATIONAL_STATUS_IDLE;

        initialize();
        notifyStatusChange( new SpectroEvent( this ) );
    }

    /**
     * Instantiate a new Manual Reading that would call up a pop-up for user to enter data
     *
     * @return SpectroReading containing the user enterred data.
     */
    public void measure()
        throws SpectroException
    {
        m_OpStatus = OPERATIONAL_STATUS_RECEIVING;
        notifyStatusChange( new SpectroEvent( this ) );

        //Constructor for ManualReading calls up the pop-up
        ManualReading newReading = new ManualReading( m_Settings );
        //m_PendingReadings.add( newReading );
        newReading.addManualReadingListener( this );

        m_OpStatus = OPERATIONAL_STATUS_IDLE;
        notifyStatusChange( new SpectroEvent( this ) );
    }

    public void calibrate( int step )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Calibration is not supported by the manual driver" );
    }

    public void retrieveStoredSamples()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Offline measurements are not supported by manual driver" );
    }

    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Offline measurements are not supported by manual driver" );
    }

    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Standards are not supported by manual driver" );
    }

    public void retrieveStandards()
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Standards are not supported by manual driver" );
    }

    public void retrieveStandard( int position )
        throws SpectroException
    {
        //Not supported
        throw new NotSupportedException( "Standards are not supported by manual driver" );
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
        m_Settings = newSettings;
    }

    public String getSerialNo()
    {
        return m_SerialNo;
    }

    public void initialize()
    {
        //Do nothing
        m_OpStatus = OPERATIONAL_STATUS_INITIALIZING;
        notifyStatusChange( new SpectroEvent( this ) );

        /*
        m_OpStatus = OPERATIONAL_STATUS_IDLE;
        notifyStatusChange( new SpectroEvent( this ) );
         */
    }

    public void dispose()
    {
        m_OpStatus = OPERATIONAL_STATUS_DISPOSED;
        m_LastStatus = null;
        m_Settings = null;
        m_SerialNo = null;
        System.gc();
    }

    public void removeSpectroListener( SpectroListener listener )
    {
        m_Listeners.remove( listener );
    }

    public void addSpectroListener( SpectroListener listener )
    {
        m_Listeners.add( listener );
    }

    public int getOperationalStatus()
    {
        return m_OpStatus;
    }

    public void manualReadingCreated( ManualReadingEvent evt )
    {
        if( evt == null )
        {
            notifyMeasured( null );
            return;
        }

        ManualReading newReading = (ManualReading) evt.getSource();

        notifyMeasured( new SpectroEvent( this, newReading ) );
    }

    public void notifyMeasured( SpectroEvent evt )
    {
        System.out.println( "Measurement received" );
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).measured( evt );
        }
    }

    public void notifyCalibrated( SpectroEvent evt )
    {
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).calibrated( evt );
        }
    }

    public void notifyStatusChange( SpectroEvent evt )
    {
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (SpectroListener) list.next() ).operationalStatusChanged( evt );
        }
    }

    public void setCalibrationDataFiles( java.net.URI[] input )
    {

    }
}
