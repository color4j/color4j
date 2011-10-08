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

package org.color4j.spectro.minolta.cm508c;

import java.util.Properties;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroReading;

public class TDSCommand
    implements SpectroCommand
{
    public static final String TARGET_NUMBER = "TARGET NUMBER";

    public static final String TARGET_COLOR_DATA_TYPE = "TARGET COLOR DATA TYPE";
    public static final String TARGET_COLOR_SPECTRAL_DATA = "0";
    public static final String TARGET_COLOR_COLORIMETRIC_DATA = "1";

    public static final String MODE = "MODE";
    public static final String MODE_LAB_DE = "0";
    public static final String MODE_LCH_DE = "1";
    public static final String MODE_LCH_CMC = "2";
    public static final String MODE_HUNTER_LAB = "3";
    public static final String MODE_YXY_DE = "4";
    public static final String MODE_MUNSELL = "5";
    public static final String MODE_XYZ_DE = "8";
    public static final String MODE_WI_ASTM_E313 = "10";
    public static final String MODE_WI_CIE = "11";
    public static final String MODE_YI_ASTM_E313 = "12";
    public static final String MODE_YI_ASTM_D1925 = "13";
    public static final String MODE_B_ISO_2470 = "14";

    public static final String COMMENT = "COMMENT";

    public static final String OBSERVER = "OBSERVER";
    public static final String OBSERVER_2_DEG = "0";
    public static final String OBSERVER_10_DEG = "1";

    public static final String ILLUMINANT1 = "ILLUMINANT1";
    public static final String ILLUMINANT2 = "ILLUMINANT2";

    public static final String ILLUMINANT_D65 = "0";
    public static final String ILLUMINANT_D50 = "1";
    public static final String ILLUMINANT_C = "2";
    public static final String ILLUMINANT_A = "3";
    public static final String ILLUMINANT_F2 = "4";
    public static final String ILLUMINANT_F6 = "5";
    public static final String ILLUMINANT_F7 = "6";
    public static final String ILLUMINANT_F8 = "7";
    public static final String ILLUMINANT_F10 = "8";
    public static final String ILLUMINANT_F11 = "9";
    public static final String ILLUMINANT_F12 = "10";

    public static final String GEOMETRY = "GEOMETRY";
    public static final String GEOMETRY_D8 = "0";
    public static final String GEOMETRY_45_0 = "1";

    public static final String MEASUREMENT_AREA = "AREA";
    public static final String MEASUREMENT_8MM = "0";
    public static final String MEASUREMENT_3MM = "1";
    public static final String MEASUREMENT_25MM = "2";
    public static final String MEASUREMENT_50MM = "3";

    public static final String SPECULAR = "SPECULAR";
    public static final String SPECULAR_INCLUDED = "0";
    public static final String SPECULAR_EXCLUDED = "1";

    public static final String TEMPORARY_DELETED_STATE = "TEMP DELETE";
    public static final String TEMPORARY_DELETED = "0";
    public static final String TEMPORARY_NOT_DELETED = "1";

    public static final String ILLUMINANT1_TOLERANCE = "ILLUMINANT1 TOLERANCE";
    public static final String ILLUMINANT2_TOLERANCE = "ILLUMINANT2 TOLERANCE";

    public static final String METAMERISM = "METAMERISM";

    public static final String TARGET_COLOR_DATA = "DATA";

    public static final String DEFAULT_SPECTRAL_DATA = "000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00,000.00";
    public static final String DEFAULT_COLORIMETRIC_DATA = "0000.00,0000.00,0000.00";

    String command;
    Properties m_Props;
    SpectroReading m_Reading;

    public TDSCommand( Properties props, SpectroReading reading )
    {
        command = "TDS";

        m_Props = props;

        m_Reading = reading;
    }

    public String construct()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( command + "," );

        buffer.append( m_Props.getProperty( TARGET_NUMBER, "1" ) + "," );
        buffer.append( m_Props.getProperty( TARGET_COLOR_DATA_TYPE, TARGET_COLOR_SPECTRAL_DATA ) + "," );
        buffer.append( m_Props.getProperty( MODE, MODE_LAB_DE ) + "," );
        buffer.append( m_Props.getProperty( COMMENT, "No Comment" ).substring( 0, 16 ) + "," );
        buffer.append( m_Props.getProperty( OBSERVER, OBSERVER_2_DEG ) + "," );
        buffer.append( m_Props.getProperty( ILLUMINANT1, ILLUMINANT_D65 ) + "," );
        buffer.append( m_Props.getProperty( ILLUMINANT2, ILLUMINANT_D65 ) + "," );
        buffer.append( m_Props.getProperty( GEOMETRY, GEOMETRY_D8 ) + "," );
        buffer.append( m_Props.getProperty( TEMPORARY_DELETED_STATE, TEMPORARY_DELETED ) + "," );
        buffer.append( m_Props.getProperty( ILLUMINANT1_TOLERANCE, "0.00,0.00,0.00,0.00,0.00,0.00,0.00" ) + "," );
        buffer.append( m_Props.getProperty( ILLUMINANT2_TOLERANCE, "0.00,0.00,0.00,0.00,0.00,0.00,0.00" ) + "," );
        buffer.append( m_Props.getProperty( METAMERISM, "0.00" ) );

        if( TARGET_COLOR_SPECTRAL_DATA.equals( m_Props.getProperty( TARGET_COLOR_DATA_TYPE ) ) )
        {
            buffer.append( m_Props.getProperty( TARGET_COLOR_DATA, DEFAULT_SPECTRAL_DATA ) );
        }
        else if( TARGET_COLOR_COLORIMETRIC_DATA.equals( m_Props.getProperty( TARGET_COLOR_DATA_TYPE ) ) )
        {
            buffer.append( m_Props.getProperty( TARGET_COLOR_DATA, DEFAULT_COLORIMETRIC_DATA ) );
        }
        else
        {
            buffer.append( DEFAULT_SPECTRAL_DATA );
        }

        return buffer.toString();
    }

    public String getName()
    {
        return "Target Color Specification Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM508cStatus.create( new String( values ) ) );
    }
}
