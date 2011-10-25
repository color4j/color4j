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

/*
 * ComaparableReflectance.java
 *
 * Created on July 18, 2002, 10:53 AM
 */

package org.color4j.colorimetry.indexing;

/**
 * Wrapper class to store reflectances ad deltas in a sorted map.
 *
 */
public class ComparableReflectance implements Comparable
{
    /* uid can be null */
    private Long m_uid;
    private Double m_value;

    /**
     * Creates a new instance of ComaparableReflectance
     */
    public ComparableReflectance( Long uid, Double delta )
        throws IllegalArgumentException
    {
        if( delta == null )
        {
            throw new IllegalArgumentException( "Argument delta can not be null" ); //NOI18N
        }
        m_uid = uid;
        m_value = delta;
    }

    public Long getUID()
    {
        return m_uid;
    }

    public Double getValue()
    {
        return m_value;
    }

    public int compareTo( Object obj )
    {
        ComparableReflectance that = (ComparableReflectance) obj;
        int ret = this.getValue().compareTo( that.getValue() );
        if( ret == 0 )
        {
            if( this.getUID() == null )
            {
                return -1;
            }
            else if( that.getUID() == null )
            {
                return 1;
            }

            ret = this.getUID().compareTo( that.getUID() );
        }
        return ret;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "ComparableReflectance[uid: " );    //NOI18N
        str.append( getUID().toString() );
        str.append( " double: " );                      //NOI18N
        str.append( getValue().toString() );
        str.append( "]" );                              //NOI18N
        return str.toString();
    }
}
