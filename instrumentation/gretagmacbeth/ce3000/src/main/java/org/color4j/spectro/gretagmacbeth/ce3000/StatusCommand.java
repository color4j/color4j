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
 * MeasureCommand.java
 *
 * Created on July 11, 2002, 4:27 PM
 */

package org.color4j.spectro.gretagmacbeth.ce3000;

import java.util.Comparator;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author hywong
 */

public class StatusCommand
    implements SpectroCommand
{
    /**
     * Creates a new instance of MeasureCommand
     */
    public StatusCommand()
    {
    }

    public String getName()
    {
        return "Status Command";
    }

    public String construct()
    {
        return "s";
    }

    public SpectroEvent interpret( byte[] received )
    {
        SpectroStatus status = new CE3000Status( received );
//        return new SpectroEvent( this, new CE3000Reading(status, createSettings( new String(received)), null ) );
        return new SpectroEvent( this, status );
    }

    private SpectroSettings createSettings( String status )
    {
        SpectroSettings settings = new SpectroSettings();

        if( status.charAt( 2 ) == 'I' )
        {
            settings.setSpecular( true );
        }
        else if( status.charAt( 2 ) == 'E' )
        {
            settings.setSpecular( false );
        }

        if( status.charAt( 3 ) == 'I' )
        {
            settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else if( status.charAt( 3 ) == 'O' )
        {
            settings.setLightFilter( new UVExcludedLightFilter() );
        }

        if( status.charAt( 4 ) == 'S' )
        {
            //KH : status doesn't know about aperture size
//            settings.setAperture( new SmallAperture() );
            settings.setLensPosition( new SmallAreaView() );
        }
        else if( status.charAt( 4 ) == 'L' )
        {
            //KH : status doesn't know about aperture size
//            settings.setAperture( new LargeAperture() );
            settings.setLensPosition( new LargeAreaView() );
        }

        return settings;
    }

    class DoubleCompare implements Comparator
    {
        /**
         * Description of the Method
         *
         * @param o1 Description of the Parameter
         * @param o2 Description of the Parameter
         *
         * @return Description of the Return Value
         *
         * @throws ClassCastException Description of the Exception
         */
        public int compare( Object o1, Object o2 )
            throws ClassCastException
        {
            Double d1 = (Double) o1;
            Double d2 = (Double) o2;

            return (int) ( d1.doubleValue() - d2.doubleValue() );
        }
    }
}