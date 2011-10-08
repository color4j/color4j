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

/**
 * Interface for LightFilter
 * <p>
 * LightFilters are often present in Spectrophotometers to change the
 * light reaching the sample under test. Experience shows that UV
 * filters are most common, but we have accommodated the specification to
 * handle any type of filter.</p>
 * <p>If the LightFilter is a Lowpass or a Highpass filter type, then
 * only the CutoffWavelength1 property is used. For the Notch and Bandpass
 * filter types, CutoffWavelength1 is always a lower wavelength
 * (higher frequency) than CutoffWavelength2.</p>
 *
 */

public interface LightFilter
{
    static int TYPE_LOWPASS = 1;
    static int TYPE_HIGHPASS = 2;
    static int TYPE_BANDPASS = 3;
    static int TYPE_NOTCH = 4;

    /**
     * Returns the Name of the Filter
     * <p>The Name is the "raw", untranslated name of the filter.</p>
     */
    String getName();

    String getDisplayName();

    int getCutoffWavelength2();

    int getCutoffWavelength1();

    boolean isBandpassFilter();

    boolean isLowpassFilter();

    boolean isHighpassFilter();

    boolean isNotchFilter();

    int getType();
}


