/*
 * Copyright (c) 2000-2011 Niclas Hedhman.
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

package org.color4j.colorimetry.entities;


public interface Spectro
{
    final static String PROP_MANUFACTURER = "Manufacturer"; //NOI18N
    final static String PROP_LOCATION = "Location";         //NOI18N
    final static String PROP_MODEL = "Model";               //NOI18N
    final static String PROP_SERIALNUMBER = "SerialNumber"; //NOI18N
    final static String PROP_DRIVERCLASSNAME = "DriverClassName";   //NOI18N

    String getManufacturer();

    String getLocation();

    String getModel();

    String getSerialNumber();

    String getDriverClassName();

    void setLocation( String location );

    void setSerialNumber( String serialNumber );

    void setManufacturer( String manufacturer );

    void setDriverClassName( String name );

    void setModel( String modelname );
}
