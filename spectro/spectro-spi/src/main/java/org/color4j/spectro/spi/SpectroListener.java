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

import java.util.EventListener;

/**
 * Listener interface for Spectrophotometer activity.
 *
 * @author Niclas Hedhman
 */
public interface SpectroListener extends EventListener
{
    void measured( SpectroEvent event );

    void calibrated( SpectroEvent event );

    void settingsChanged( SpectroEvent event );

    void operationalStatusChanged( SpectroEvent event );

    /*
     * KH March 11, 2k4 - added to accommodate stored reflectance retrieval
     */

    /**
     * hopefully, this will be a generic-enough way to retrieve stored reflectances
     *
     * @param indices - should be an array containing the indices of the standards positions in the spectro
     */
    void numberStandardsFound( int[] indices );

    /**
     * called when spectroimpl has processed data from spectro machine
     *
     * @param event - should contain a reading
     */
    void retrievedStandard( SpectroEvent event );

    /**
     * hopefully, this will be a generic-enough way to retrieve stored reflectances
     *
     * @param indices - should be an array containing the indices of the samples positions in the spectro
     */
    void numberSamplesFound( int[] indices );

    /**
     * called when spectroimpl has processed data from spectro machine
     *
     * @param event - should contain a reading
     */
    void retrievedSample( SpectroEvent event );

}
