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
package org.color4j.spectro.drivers.xr;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface Xr8000Library extends Library
{
    Xr8000Library INSTANCE = (Xr8000Library)
        Native.loadLibrary( ( Platform.isWindows() ? "xr8000" : "failOnNonWindows" ), Xr8000Library.class );

    boolean Instrument_Initialize();

    void Instrument_Configure();

    void Instrument_SetConfiguration( int aperture, int filter, float pctEnergy, int reflectance, int specular );

    void Instrument_GetConfigString( char[] outValue );

    boolean Instrument_Measure();

    void Instrument_Calibrate();

    void Instrument_GetModel( char[] outValue );

    int Instrument_GetLoWavelength();

    int Instrument_GetHiWavelength();

    void VB_Instrument_GetReflectances( float[] outdata );

    void Instrument_GetSerialNo( char[] outdata );

    boolean Instrument_IsReflectance();

    boolean Instrument_IsDataAvailable();

    boolean Instrument_Standalone();
}
