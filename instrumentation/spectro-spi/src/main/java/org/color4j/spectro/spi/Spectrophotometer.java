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

package org.color4j.spectro.spi;

import java.net.URI;

/**
 */
public interface Spectrophotometer
{
    static int OPERATIONAL_STATUS_UNKNOWN = 0;
    static int OPERATIONAL_STATUS_INITIALIZING = 1;
    static int OPERATIONAL_STATUS_IDLE = 2;
    static int OPERATIONAL_STATUS_SENDING = 3;
    static int OPERATIONAL_STATUS_RECEIVING = 4;
    static int OPERATIONAL_STATUS_DISPOSED = 5;

    void measure()
        throws SpectroException;

    void calibrate( int step )
        throws SpectroException;

    SpectroSettings getSettings();

    void setSettings( SpectroSettings newSettings );

    String getSerialNo();

    void retrieveStoredSamples()
        throws SpectroException;

    void retrieveStoredSample( int position )
        throws SpectroException;

    void setStandard( int position, SpectroReading reading )
        throws SpectroException;

    void retrieveStandards()
        throws SpectroException;

    void retrieveStandard( int position )
        throws SpectroException;

    void queryNoOfStoredSamples()
        throws SpectroException;

    void queryNoOfStoredStandards()
        throws SpectroException;

    void initialize();

    void dispose();

    void removeSpectroListener( SpectroListener listener );

    void addSpectroListener( SpectroListener listener );

    int getOperationalStatus();

    void setCalibrationDataFiles( URI[] fileURIs );
}


