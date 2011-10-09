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

package org.color4j.spectro.drivers.manual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.color4j.spectro.spi.Aperture;
import org.color4j.spectro.spi.DriverManager;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.LightFilter;
import org.color4j.spectro.spi.SpectroDriver;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.Spectrophotometer;

public class ManualDriver implements SpectroDriver
{
    static
    {
        try
        {
            DriverManager manager = DriverManager.getInstance();
            manager.registerDriver( ManualDriver.class );
        }
        catch( SpectroException ex )
        {
            System.err.println( "Unable to register Manual Driver" );
            ex.printStackTrace( System.err );
        }
    }

    private boolean m_RetrieveReflectances;
    private boolean m_RetrieveStandards;
    private boolean m_Reflectance;
    private boolean m_Transmittance;
    private boolean m_Specular;
    private boolean m_OfflineMeasure;
    private boolean m_BlackFirst;
    private boolean m_BlackCalibration;
    private boolean m_WhiteCalibration;

    private LightFilter[] m_Filters;
    private Aperture[] m_Apertures;
    private LensPosition[] m_LensPosition;
    private Collection m_Parameters;

    private int m_Shortest;
    private int m_Longest;
    private int m_Interval;
    private int m_Accuracy;
    private int m_Offlines;
    private int m_Standards;
    private double m_Precision;

    private String m_Name;
    private String m_Manufacturer;

    private String[] m_CalibrationSteps = new String[ 0 ];

    private boolean m_UIApertures;
    private boolean m_UILensPositions;
    private boolean m_UISpecular;
    private boolean m_UIUVFilters;

    public ManualDriver()
    {
        initialize();
    }

    public boolean canRetrieveSamples()
    {
        return m_RetrieveReflectances;
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
        return m_Specular;
    }

    public boolean canExcludeSpecular()
    {
        return m_Specular;
    }

    public boolean usesSerialPorts()
    {
        return false;
    }

    public boolean canMeasureOffline()
    {
        return m_OfflineMeasure;
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

    public LightFilter[] getFilters()
    {
        return m_Filters;
    }

    public Aperture[] getApertures()
    {
        return m_Apertures;
    }

    public LensPosition[] getLensPositions()
    {
        return m_LensPosition;
    }

    /**
     * Returns the additional parameters required for the Driver.
     * <p>The key in the <code>java.util.Map</code> contains the name of the Parameter, the value is a <code>SpectroParameter</code> object.</p>
     */
    public Collection getParameterDefinitions()
    {
        return m_Parameters;
    }

    /**
     * Creates a new instance of a Spectrophotometer.
     * <p>The SpectroDriver implementation is responsible to initialize the Spectrophotometer instance to its initialized state, so it is then ready to be called by the <code>setSettings</code> method.</p>
     */
    public Spectrophotometer createSpectrophotometer()
    {
        return new ManualSpectro();
    }

    public int getStartingWavelength()
    {
        return m_Shortest;
    }

    public int getEndingWavelength()
    {
        return m_Longest;
    }

    public int getInterval()
    {
        return m_Interval;
    }

    public int getEstimatedAccuracy()
    {
        return m_Accuracy;
    }

    public int getNoOfOfflineMeasurements()
    {
        return m_Offlines;
    }

    public int getNoOfOfflineStandards()
    {
        return m_Standards;
    }

    public double getWhiteTilePrecision()
    {
        return m_Precision;
    }

    public String getName()
    {
        return m_Name;
    }

    public String getManufacturer()
    {
        return m_Manufacturer;
    }

    public void initialize()
    {
        m_RetrieveReflectances = false;
        m_RetrieveStandards = false;
        m_Reflectance = true;
        m_Transmittance = false;
        m_Specular = true;
        m_OfflineMeasure = false;
        m_BlackFirst = true;
        m_BlackCalibration = false;
        m_WhiteCalibration = false;

        m_Filters = new LightFilter[]{ new UVIncludedLightFilter(), new CutOff400LightFilter(), new CutOff420LightFilter(), new CutOff460LightFilter(), };
        m_Apertures = new Aperture[]{ new UltraSmallAperture(), new SmallAperture(), new MediumAperture(), new LargeAperture(), new UltraLargeAperture() };

        m_LensPosition = new LensPosition[]{ new UltraSmallView(), new SmallView(), new MediumView(), new LargeView(), new UltraLargeView() };

        m_Parameters = new ArrayList();

        m_Shortest = 360;
        m_Longest = 700;
        m_Interval = 10;
        m_Accuracy = 1;
        m_Offlines = 0;
        m_Standards = 0;
        m_Precision = 1.0;

        m_Name = "Manual Driver";
        m_Manufacturer = "Color4j Community";

        m_UIApertures = true;
        m_UILensPositions = true;
        m_UISpecular = true;
        m_UIUVFilters = true;
    }

    public void dispose()
    {
        m_Filters = null;
        m_Apertures = null;
        m_LensPosition = null;
        m_Parameters = null;

        m_Name = null;
        m_Manufacturer = null;
        System.gc();
    }

    public String[] getCalibrationSteps()
    {
        return m_CalibrationSteps;
    }

    public String[] getCalibrationDataFiles()
    {
        return null;
    }

    public String getLocalizedText( String text )
    {
        return getLocalizedText( text, Locale.getDefault() );
    }

    public String getLocalizedText( String text, Locale locale )
    {
        try
        {
            return ResourceBundle.getBundle( "Bundle.properties", locale ).getString( text );
        }
        catch( MissingResourceException e )
        {
            return text;
        }
    }

    public boolean canUIApertures()
    {
        return m_UIApertures;
    }

    public boolean canUILensPositions()
    {
        return m_UILensPositions;
    }

    public boolean canUISpecular()
    {
        return m_UISpecular;
    }

    public boolean canUIUVFilters()
    {
        return m_UIUVFilters;
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
