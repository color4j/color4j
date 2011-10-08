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
 * CPSCommand.java
 *
 * Created on October 28, 2002, 2:56 PM
 */
package org.color4j.spectro.minolta.cm503c;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author hywong
 */
public class CPSCommand implements SpectroCommand
{
    /*public final static String DISPLAY = "DISPLAY";
    public final static String DISP_DIFF_AND_ABS="00";
    public final static String DISP_PASS_FAIL="01";
    public final static String DISP_COLOR_GRAPH="02";
    public final static String DISP_DENSITY_A="03";
    public final static String DISP_DENSITY_T="04";
    public final static String DISP_SPECTRAL_GRAPH="05";
    
    public final static String MODE = "MODE";
    public final static String MODE_LAB_DE = "00";
    public final static String MODE_LCH_DE = "01";
    public final static String MODE_LCH_CMC = "02";
    public final static String MODE_HUNTER_LAB = "03";
    public final static String MODE_YXY_DE = "04";
    public final static String MODE_MUNSELL = "05";
    public final static String MODE_XYZ_DE = "06";
    public final static String MODE_WI_ASTM_E313 = "10";
    public final static String MODE_WI_CIE = "11";
    public final static String MODE_YI_ASTM_E313 = "12";
    public final static String MODE_YI_ASTM_D1925 = "13";
    public final static String MODE_B_ISE_2470 = "14";
    
    public final static String I_OF_CMC = "I of CMC";
    
    public final static String C_OF_CMC = "c of CMC";
    
    public final static String AUTO_PRINT = "AUTO PRINT";
    public final static String AUTO_PRINT_OFF = "0";
    public final static String AUTO_PRINT_ON = "1";
    
    public final static String AUTO_AVERAGE = "AUTO AVERAGE";
    public final static String AUTO_AVERAGE_1 = "00";
    public final static String AUTO_AVERAGE_3 = "01";
    public final static String AUTO_AVERAGE_5 = "02";
    public final static String AUTO_AVERAGE_8 = "03";
    
    public final static String DELETE_OUTLIER = "DELETE OUTLIER";
    public final static String DELETE_OUTLIER_OFF = "0";
    public final static String DELETE_OUTLIER_ON = "1";
    
    public final static String AUTO_SELECT = "AUTO SELECT";
    public final static String AUTO_SELECT_OFF = "0";
    public final static String AUTO_SELECT_ON = "1";
    
    public final static String BUZZER = "BUZZER";
    public final static String BUZZER_OFF = "0";
    public final static String BUZZER_ON = "1";
    
    public final static String OBSERVER = "OBSERVER";
    public final static String OBSERVER_2_DEG = "00";
    public final static String OBSERVER_10_DEG = "01";
    
    public final static String ILLUMINANT_1 = "ILLUMINANT1";
    public final static String ILLUMINANT_2 = "ILLUMINANT2";
    
    public final static String D65 = "00";
    public final static String D50 = "01";
    public final static String C = "02";
    public final static String A = "03";
    public final static String F2 = "04";
    public final static String F6 = "05";
    public final static String F7 = "06";
    public final static String F8 = "07";
    public final static String F10 = "08";
    public final static String F11 = "09";
    public final static String F12 = "10";
    public final static String NONE = "11";
 
    public final static String TARGET_NUMBER = "TARGET NUMBER";
    
    String command;
    String disp;
    String mode;
    String i_cmc;
    String c_cmc;
    String autoPrint;
    String autoAverage;
    String deleteOutlier;
    String autoSelect;
    String buzzer;
    String observer;
    String ill1;
    String ill2;
    String targetNumber; */

    /**
     * Creates a new instance of CPSCommand
     */
    /*public CPSCommand( Properties props )
    {
        command = "CPS";
        
        disp = (String) props.get( this.DISPLAY );
        mode = (String) props.get( this.MODE );

        i_cmc = (String) props.get( this.I_OF_CMC );
        c_cmc = (String) props.get( this.C_OF_CMC );

        autoPrint = (String) props.get( this.AUTO_PRINT );
        autoAverage = (String) props.get( this.AUTO_AVERAGE );
        deleteOutlier = (String) props.get( this.DELETE_OUTLIER );
        autoSelect = (String) props.get( this.AUTO_SELECT );
        buzzer = (String) props.get( this.BUZZER );
        observer = (String) props.get( this.OBSERVER );
        ill1 = (String) props.get( this.ILLUMINANT_1 );
        ill2 = (String) props.get( this.ILLUMINANT_2 );
        targetNumber = (String) props.get( this.TARGET_NUMBER );
    }
    
    public String construct()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( command + "," );
        buffer.append( disp + "," );
        buffer.append( mode + "," );
        buffer.append( i_cmc + "," );
        buffer.append( c_cmc + "," );
        buffer.append( autoPrint + "," );
        buffer.append( autoAverage + "," );
        buffer.append( deleteOutlier + "," );
        buffer.append( autoSelect + "," );
        buffer.append( buzzer + "," );
        buffer.append( observer + "," );
        buffer.append( ill1 + "," );
        buffer.append( ill2 + "," );
        buffer.append( targetNumber );
        
        return buffer.toString ();
    }*/

    private String m_parameters;
    private String command;

    /**
     * Creates a new instance of CPSCommand
     */
    public CPSCommand( String parameters )
    {
        m_parameters = parameters;
        command = "CPS";
    }

    public String construct()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( command + "," + m_parameters );

        return buffer.toString();
    }

    public String getName()
    {
        return "Set Measurement Parameter Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM503cStatus.create( new String( values ) ) );
    }
}