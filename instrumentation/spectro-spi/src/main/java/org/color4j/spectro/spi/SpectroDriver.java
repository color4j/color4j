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

import java.util.Collection;
import java.util.Locale;

/**
 * Definition of a Spectrophotometer driver.
 * <p>The <code>SpectroDriver</code> implementation must properly
 * implement these methods, primarily for the user client software
 * to query for functionality and operational issues.</p>
 *
 */
public interface SpectroDriver
{
    String getName();

    String getManufacturer();

    boolean canQuerySamples();

    boolean canQueryStandards();

    boolean canRetrieveSamples();

    /**
     * if there is no separation between retrieval of standards or samples, use this as the default
     * retrieval mechanism
     *
     * @return if able to retrieveStandards
     */
    boolean canRetrieveStandards();

    boolean canMeasureReflectance();

    boolean canMeasureTransmittance();

    boolean canIncludeSpecular();

    boolean canExcludeSpecular();

    boolean usesSerialPorts();

    LightFilter[] getFilters();

    int getStartingWavelength();

    int getEndingWavelength();

    int getInterval();

    double getWhiteTilePrecision();

    int getEstimatedAccuracy();

    Aperture[] getApertures();

    boolean canMeasureOffline();

    int getNoOfOfflineMeasurements();

    int getNoOfOfflineStandards();

    void initialize()
        throws SpectroException;

    // TODO KH Mar 19, 2004 - NEVER GETS CALLED!?   
    void dispose()
        throws SpectroException;

    /**
     * Creates a new instance of a Spectrophotometer.
     * <p>The SpectroDriver implementation is responsible to initialize the Spectrophotometer instance to its initialized state, so it is then ready to be called by the <code>setSettings</code> method.</p>
     */
    Spectrophotometer createSpectrophotometer();

    /**
     * Returns the additional parameters required for the Driver.
     * <p>The key in the <code>java.util.Map</code> contains the name of the Parameter, the value is a <code>SpectroParameter</code> object.</p>
     */
    Collection getParameterDefinitions();

    LensPosition[] getLensPositions();

    boolean doesBlackCalibrationFirst();

    boolean canBlackCalibrate();

    boolean canWhiteCalibrate();

    String[] getCalibrationSteps();

    String[] getCalibrationDataFiles();

    String getLocalizedText( String text );

    String getLocalizedText( String text, Locale locale );

    // UI attributes, whether it can select Apertures, LensPositions, Specular and UVFilters
    boolean canUIApertures();

    boolean canUILensPositions();

    boolean canUISpecular();

    boolean canUIUVFilters();
}


