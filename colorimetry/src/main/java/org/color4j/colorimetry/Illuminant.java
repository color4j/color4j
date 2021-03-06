/*
 * Copyright (c) 2000-2011 Niclas Hedhman.
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

package org.color4j.colorimetry;

import org.color4j.colorimetry.Spectrum;

/**
 * Interface for Illuminant (S).
 * <p>Illuminants in color physics are light sources, often
 * with standardized characteristics, but this
 * colorimetry system allows for user defined (meaured) illuminants
 * to be used.</p>
 */

public interface Illuminant
{
    /**
     * @return the Name of the Illuminant
     */
    String getName();

    /**
     * @return the Power distribution of the light source.
     */

    Spectrum getSpectrum();
}
