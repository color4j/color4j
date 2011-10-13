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

package org.color4j.colorimetry.entities;

import org.color4j.colorimetry.Spectrum;
import java.util.Map;
import java.util.SortedMap;

/**
 * The Reflectance interface (R).
 * <p>The Reflectance interface represents a measurement of a reflected light off a material. The Reflectance is a power spectrum of light, ranging from 0-1, representing at each wavelength, the percentage returned.<p>
 * <p>Closely related to the Reflectance is the <i>conditions</i> under which the Reflectance was captured.
 */
public interface Reflectance
{
    /**
     * The Key name in the Conditions table for Aperture description.
     * <p>The value field is represents the aperture description used
     * by the spectrophotometer maker for that model.</p>
     */
    String CONDITION_APERTURE = "Aperture";             //NOI18N
    String CONDITION_LIGHTFILTER = "Light Filter";      //NOI18N
    String CONDITION_SPECULAR = "Specular Inclusion";   //NOI18N
    String CONDITION_MODE = "Mode";                     //NOI18N

    /**
     * The Key name in the Conditions table for the ambient
     * temperature when the measurement was made.
     */
    String CONDITION_AMBIENTTEMPERATURE = "Ambient Temperature";    //NOI18N
    String CONDITION_AMBIENTHUMIDITY = "Ambient Humidity";          //NOI18N
    String CONDITION_LENSPOSITION = "Lens Position";                //NOI18N
    String CONDITION_LAMPUSED = "Lamp Used";                        //NOI18N
    String CONDITION_FLASHES = "No Of Flashes";                     //NOI18N

    String CONDITION_MEASUREMENTS = "No Of Measurements";           //NOI18N
    String CONDITION_AVERAGEALGORITHM = "Average Algorithm";        //NOI18N

    String TYPE_MEASURED = "Measured"; //NOI18N
    String TYPE_NEWLY_RECEIVED = "Newly Received"; //NOI18N
    String TYPE_RECEIVED = "Received"; //NOI18N
    String TYPE_RECIPE = "DefaultRecipeReflectanceDataset"; //NOI18N
    String TYPE_PROFILED = "Profiled"; //NOI18N    

    /**
     * @return the Spectrum of the Observation.
     */
    Spectrum getSpectrum();

    void setSpectrum( Spectrum spectrum );

    SortedMap getSpectrumMap();

    void setSpectrumMap( SortedMap spectrum );

    Map getConditions();

    void setConditions( Map conditions );

    Spectro getSpectro();

    void setSpectro( Spectro spectro );

    String getType();

    void setType( String type );

    String getName();
}