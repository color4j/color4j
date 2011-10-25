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
 * Created on Nov 12, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.color4j.spectro.drivers.xr8400;

import org.color4j.spectro.drivers.xr.CalibrateFunction;
import org.color4j.spectro.drivers.xr.DataAvailableFunction;
import org.color4j.spectro.drivers.xr.GetReflectanceFunction;
import org.color4j.spectro.drivers.xr.InitializeFunction;
import org.color4j.spectro.drivers.xr.MeasureFunction;
import org.color4j.spectro.drivers.xr.SetConfigurationFunction;
import org.color4j.spectro.drivers.xr.SmallAperture;
import org.color4j.spectro.drivers.xr.UVIncludedLightFilter;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroException;
import org.color4j.spectro.spi.SpectroFunction;
import org.color4j.spectro.spi.SpectroListener;

public class XR8000TestDriver
    implements SpectroListener
{
    public XR8400Spectro m_Spectro;

    public XR8000TestDriver()
    {
        m_Spectro = new XR8400Spectro();
        m_Spectro.addSpectroListener( this );
        m_Spectro.initialize();
    }

    public void measured( SpectroEvent event )
    {
        System.out.println( "Measured" );
    }

    public void calibrated( SpectroEvent event )
    {
        System.out.println( "Calibrated" );
    }

    public void settingsChanged( SpectroEvent event )
    {
        System.out.println( "Settings Changed" );
    }

    public void operationalStatusChanged( SpectroEvent event )
    {
        System.out.println( "Operational Status Changed" );
    }

    public static void main( String[] args )
        throws SpectroException
    {
        SpectroFunction function = new InitializeFunction();
        function.invoke();

        function = new SetConfigurationFunction( new SmallAperture(), new UVIncludedLightFilter(), false, true );

        function.invoke();

        function = new CalibrateFunction();
        function.invoke();

        function = new MeasureFunction();
        function.invoke();

        function = new DataAvailableFunction();
        function.invoke();

        function = new GetReflectanceFunction();
        function.invoke();

        function = new DataAvailableFunction();
        function.invoke();
    }

    public void retrievedStandard( SpectroEvent event )
    {
        // TODO Auto-generated method stub

    }

    public void retrievedSample( SpectroEvent event )
    {
        // TODO Auto-generated method stub

    }

    public void numberStandardsFound( int[] indices )
    {
        // TODO Auto-generated method stub

    }

    public void numberSamplesFound( int[] indices )
    {
        // TODO Auto-generated method stub

    }
}
