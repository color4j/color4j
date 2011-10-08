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

/*
 * Created on Nov 5, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.color4j.spectro.drivers.xr8200;

import java.util.Collection;
import java.util.Locale;
import org.color4j.spectro.drivers.xr.CutOff400LightFilter;
import org.color4j.spectro.drivers.xr.LargeAperture;
import org.color4j.spectro.drivers.xr.LargeAreaView;
import org.color4j.spectro.drivers.xr.MediumAperture;
import org.color4j.spectro.drivers.xr.MediumAreaView;
import org.color4j.spectro.drivers.xr.SmallAperture;
import org.color4j.spectro.drivers.xr.SmallAreaView;
import org.color4j.spectro.spi.Aperture;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.LightFilter;
import org.color4j.spectro.spi.SpectroDriver;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.Spectrophotometer;

public class XR8200Driver implements SpectroDriver
{
    private String m_Name;
    private String m_Manufacturer;

    private boolean m_RetrieveSamples;
    private boolean m_RetrieveStandards;
    private boolean m_Reflectance;
    private boolean m_Transmittance;
    private boolean m_SpecularInclusion;
    private boolean m_SpecularExclusion;
    private boolean m_SerialPorts;
    private boolean m_WhiteCalibration;
    private boolean m_BlackCalibration;
    private boolean m_BlackFirst;
    private boolean m_ApertureControl;
    private boolean m_SpecularControl;
    private boolean m_FilterControl;
    private boolean m_LensPositionControl;
    private boolean m_OfflineMeasure;

    private LightFilter[] m_Filters;
    private Aperture[] m_Apertures;
    private LensPosition[] m_LensPositions;

    private int m_StartingWavelength;
    private int m_EndingWavelength;
    private int m_Interval;

    public XR8200Driver()
    {
        m_Name = "XR8400";
        m_Manufacturer = "X-Rite";

        m_RetrieveSamples = false;
        m_RetrieveStandards = false;
        m_Reflectance = true;
        m_Transmittance = true;
        m_SpecularInclusion = true;
        m_SpecularExclusion = true;
        m_SerialPorts = false;
        m_WhiteCalibration = true;
        m_BlackCalibration = true;
        m_BlackFirst = true;
        m_ApertureControl = true;
        m_SpecularControl = true;
        m_FilterControl = true;
        m_LensPositionControl = false;
        m_OfflineMeasure = false;

        m_Filters = new LightFilter[]{ new CutOff400LightFilter() };

        m_Apertures = new Aperture[]{ new SmallAperture(), new MediumAperture(), new LargeAperture() };

        m_LensPositions = new LensPosition[]{ new SmallAreaView(), new MediumAreaView(), new LargeAreaView() };

        m_StartingWavelength = 360;
        m_EndingWavelength = 740;
        m_Interval = 10;
    }

    public String getName()
    {
        return m_Name;
    }

    public String getManufacturer()
    {
        return m_Manufacturer;
    }

    public boolean canRetrieveSamples()
    {
        return m_RetrieveSamples;
    }

    public boolean canRetrieveStandards()
    {
        return m_RetrieveStandards;
    }

    public boolean canMeasureReflectance()
    {
        return m_Reflectance;
    }

    public boolean canMeasureTransmittance()
    {
        return m_Transmittance;
    }

    public boolean canIncludeSpecular()
    {
        return m_SpecularInclusion;
    }

    public boolean canExcludeSpecular()
    {
        return m_SpecularExclusion;
    }

    public boolean usesSerialPorts()
    {
        return m_SerialPorts;
    }

    public LightFilter[] getFilters()
    {
        return m_Filters;
    }

    public int getStartingWavelength()
    {
        return m_StartingWavelength;
    }

    public int getEndingWavelength()
    {
        return m_EndingWavelength;
    }

    public int getInterval()
    {
        return m_Interval;
    }

    public double getWhiteTilePrecision()
    {
        return 1.0d;
    }

    public int getEstimatedAccuracy()
    {
        return 1;
    }

    public Aperture[] getApertures()
    {
        return m_Apertures;
    }

    public boolean canMeasureOffline()
    {
        return m_OfflineMeasure;
    }

    public int getNoOfOfflineMeasurements()
    {
        return 0;
    }

    public int getNoOfOfflineStandards()
    {
        return 0;
    }

    public void initialize()
        throws SpectroException
    {
    }

    public void dispose()
        throws SpectroException
    {
    }

    public Spectrophotometer createSpectrophotometer()
    {
        return null;
    }

    public Collection getParameterDefinitions()
    {
        return null;
    }

    public LensPosition[] getLensPositions()
    {
        return m_LensPositions;
    }

    public boolean doesBlackCalibrationFirst()
    {
        return m_BlackFirst;
    }

    public boolean canBlackCalibrate()
    {
        return m_BlackCalibration;
    }

    public boolean canWhiteCalibrate()
    {
        return m_WhiteCalibration;
    }

    public String[] getCalibrationSteps()
    {
        return null;
    }

    public String[] getCalibrationDataFiles()
    {
        return null;
    }

    public String getLocalizedText( String text )
    {
        // TODO: Mar 16, 2004 - not be null
        return null;
    }

    public String getLocalizedText( String text, Locale locale )
    {
        // TODO: Mar 16, 2004 - not be null
        return null;
    }

    public boolean canUIApertures()
    {
        return m_ApertureControl;
    }

    public boolean canUILensPositions()
    {
        return m_LensPositionControl;
    }

    public boolean canUISpecular()
    {
        return m_SpecularControl;
    }

    public boolean canUIUVFilters()
    {
        return m_FilterControl;
    }

    public boolean canQuerySamples()
    {
        return false;
    }

    public boolean canQueryStandards()
    {
        return false;
    }
}
