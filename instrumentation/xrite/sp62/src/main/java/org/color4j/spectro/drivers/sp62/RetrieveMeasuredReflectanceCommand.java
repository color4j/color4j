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
 * Created on Mar 12, 2004
 *
 */
package org.color4j.spectro.drivers.sp62;

/**
 * @author devteam
 */
public class RetrieveMeasuredReflectanceCommand
    extends RetrieveReflectanceCommand
{
    public static final String NAME = "Retrieve measured reflectance command";

    public RetrieveMeasuredReflectanceCommand( boolean specular )
    {
        super( specular );
    }

    public String getName()
    {
        return NAME;
    }

    public String construct()
    {
        return "0" + ( super.specularInc() ? "0" : "1" ) + "01GM";
    }
}
