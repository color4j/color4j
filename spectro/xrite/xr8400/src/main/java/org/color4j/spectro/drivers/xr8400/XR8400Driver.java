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
package org.color4j.spectro.drivers.xr8400;

import java.util.Collection;
import java.util.Locale;
import org.color4j.spectro.drivers.xr.CutOff400LightFilter;
import org.color4j.spectro.drivers.xr.CutOff420LightFilter;
import org.color4j.spectro.drivers.xr.LargeAperture;
import org.color4j.spectro.drivers.xr.LargeAreaView;
import org.color4j.spectro.drivers.xr.MediumAperture;
import org.color4j.spectro.drivers.xr.MediumAreaView;
import org.color4j.spectro.drivers.xr.SmallAperture;
import org.color4j.spectro.drivers.xr.SmallAreaView;
import org.color4j.spectro.drivers.xr.UVIncludedLightFilter;
import org.color4j.spectro.spi.Aperture;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.LightFilter;
import org.color4j.spectro.spi.SpectroDriver;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.Spectrophotometer;

/**
 * @author Administrator
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class XR8400Driver implements SpectroDriver
{
    String m_Name;
    String m_Manufacturer;

    boolean m_RetrieveSamples;
    boolean m_RetrieveStandards;
    boolean m_Reflectance;
    boolean m_Transmittance;
    boolean m_SpecularInclusion;
    boolean m_SpecularExclusion;
    boolean m_SerialPorts;
    boolean m_WhiteCalibration;
    boolean m_BlackCalibration;
    boolean m_BlackFirst;
    boolean m_ApertureControl;
    boolean m_SpecularControl;
    boolean m_FilterControl;
    boolean m_LensPositionControl;
    boolean m_OfflineMeasure;

    LightFilter[] m_Filters;
    Aperture[] m_Apertures;
    LensPosition[] m_LensPositions;

    int m_StartingWavelength;
    int m_EndingWavelength;
    int m_Interval;

    XR8400Spectro m_Spectro;

    public XR8400Driver()
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

        m_Filters = new LightFilter[]{ new UVIncludedLightFilter(), new CutOff400LightFilter(), new CutOff420LightFilter() };

        m_Apertures = new Aperture[]{ new SmallAperture(), new MediumAperture(), new LargeAperture() };

        m_LensPositions = new LensPosition[]{ new SmallAreaView(), new MediumAreaView(), new LargeAreaView() };

        m_StartingWavelength = 360;
        m_EndingWavelength = 740;
        m_Interval = 10;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getName()
     */
    public String getName()
    {
        return m_Name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getManufacturer()
     */
    public String getManufacturer()
    {
        return m_Manufacturer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canRetrieveReflectances()
     */
    public boolean canRetrieveSamples()
    {
        return m_RetrieveSamples;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canRetrieveStandards()
     */
    public boolean canRetrieveStandards()
    {
        return m_RetrieveStandards;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canMeasureReflectance()
     */
    public boolean canMeasureReflectance()
    {
        return m_Reflectance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canMeasureTransmittance()
     */
    public boolean canMeasureTransmittance()
    {
        return m_Transmittance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canIncludeSpecular()
     */
    public boolean canIncludeSpecular()
    {
        return m_SpecularInclusion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canExcludeSpecular()
     */
    public boolean canExcludeSpecular()
    {
        return m_SpecularExclusion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#usesSerialPorts()
     */
    public boolean usesSerialPorts()
    {
        return m_SerialPorts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getFilters()
     */
    public LightFilter[] getFilters()
    {
        return m_Filters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getStartingWavelength()
     */
    public int getStartingWavelength()
    {
        return m_StartingWavelength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getEndingWavelength()
     */
    public int getEndingWavelength()
    {
        return m_EndingWavelength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getInterval()
     */
    public int getInterval()
    {
        return m_Interval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getWhiteTilePrecision()
     */
    public double getWhiteTilePrecision()
    {
        return 1.0d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getEstimatedAccuracy()
     */
    public int getEstimatedAccuracy()
    {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getApertures()
     */
    public Aperture[] getApertures()
    {
        return m_Apertures;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canMeasureOffline()
     */
    public boolean canMeasureOffline()
    {
        return m_OfflineMeasure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getNoOfOfflineMeasurements()
     */
    public int getNoOfOfflineMeasurements()
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getNoOfOfflineStandards()
     */
    public int getNoOfOfflineStandards()
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#initialize()
     */
    public void initialize()
        throws SpectroException
    {
        if( m_Spectro != null )
        {
            m_Spectro = new XR8400Spectro();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#dispose()
     */
    public void dispose()
        throws SpectroException
    {
        if( m_Spectro != null )
        {
            m_Spectro.dispose();
            m_Spectro = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#createSpectrophotometer()
     */
    public Spectrophotometer createSpectrophotometer()
    {
        if( m_Spectro != null )
        {
            return m_Spectro;
        }
        else
        {
            m_Spectro = new XR8400Spectro();
            return m_Spectro;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getParameterDefinitions()
     */
    public Collection getParameterDefinitions()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getLensPositions()
     */
    public LensPosition[] getLensPositions()
    {
        return m_LensPositions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#doesBlackCalibrationFirst()
     */
    public boolean doesBlackCalibrationFirst()
    {
        return m_BlackFirst;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canBlackCalibrate()
     */
    public boolean canBlackCalibrate()
    {
        return m_BlackCalibration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canWhiteCalibrate()
     */
    public boolean canWhiteCalibrate()
    {
        return m_WhiteCalibration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getCalibrationSteps()
     */
    public String[] getCalibrationSteps()
    {
        return new String[]{ "CALIBRATION_INSTRUCTION" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getCalibrationDataFiles()
     */
    public String[] getCalibrationDataFiles()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getLocalizedText(java.lang.String)
     */
    public String getLocalizedText( String text )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#getLocalizedText(java.lang.String,
     *      java.util.Locale)
     */
    public String getLocalizedText( String text, Locale locale )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canUIApertures()
     */
    public boolean canUIApertures()
    {
        return m_ApertureControl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canUILensPositions()
     */
    public boolean canUILensPositions()
    {
        return m_LensPositionControl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canUISpecular()
     */
    public boolean canUISpecular()
    {
        return m_SpecularControl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.color4j.spectro.spi.SpectroDriver#canUIUVFilters()
     */
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
