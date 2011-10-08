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
 * Created on Sep 22, 2004
 *
 */
package org.color4j.spectro.spi.helpers;

/**
 * @author kevin
 */
public class SerialCommunicator
{
    GenericCommDriver m_Driver;

    public SerialCommunicator()
    {
        m_Driver = new GenericCommDriver();
    }

    public CommDriver getDriver()
    {
        return m_Driver;
    }

    public static void main( String[] args )
        throws Exception
    {
        SerialCommunicator s = new SerialCommunicator();
        SerialCommFrame frame = new SerialCommFrame();
        String portname;
        if( args.length == 0 )
        {
            portname = "COM3";
        }
        else
        {
            portname = args[ 0 ];
        }
        frame.setDriver( s.getDriver(), portname );
        frame.pack();
        frame.show();
        frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );
    }
}
