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

package org.color4j.spectro.drivers.xr8200;

import java.net.URI;
import org.color4j.spectro.spi.NotSupportedException;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.SpectroListener;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.Spectrophotometer;

public class XR8200Spectro
    implements Spectrophotometer
{
    SpectroSettings m_Settings;

    public void measure()
        throws SpectroException
    {
        // TODO

    }

    public void calibrate( int step )
        throws SpectroException
    {
        // TODO

    }

    public SpectroSettings getSettings()
    {
        // TODO
        return null;
    }

    public void setSettings( SpectroSettings newSettings )
    {
        // TODO

    }

    public String getSerialNo()
    {
        // TODO
        return null;
    }

    public void retrieveStoredSamples()
        throws SpectroException
    {
        // TODO

    }

    public void retrieveStoredSample( int position )
        throws SpectroException
    {
        // TODO

    }

    public void setStandard( int position, SpectroReading reading )
        throws SpectroException
    {
        // TODO

    }

    public void retrieveStandards()
        throws SpectroException
    {
        // TODO

    }

    public void retrieveStandard( int position )
        throws SpectroException
    {
        // TODO

    }

    public void initialize()
    {
        // TODO

    }

    public void dispose()
    {
        // TODO

    }

    public void removeSpectroListener( SpectroListener listener )
    {
        // TODO

    }

    public void addSpectroListener( SpectroListener listener )
    {
        // TODO

    }

    public int getOperationalStatus()
    {
        return 0;
    }

    public void setCalibrationDataFiles( URI[] fileURIs )
    {
        //TODO: Mar 9, 2004 - found not done
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
}
