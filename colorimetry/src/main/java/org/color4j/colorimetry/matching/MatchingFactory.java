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

package org.color4j.colorimetry.matching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MatchingFactory
{
    public final static String CMC11 = "CMC 1:1";          //NOI18N
    public final static String CMC21 = "CMC 2:1";          //NOI18N
    public final static String BFD = "BFD 1:1";             //NOI18N
    public final static String LUVDE = "CIELuv DE";        //NOI18N
    public final static String CIE94DE = "CIE 94";          //NOI18N
    public final static String LABDE = "CIELab DE";        //NOI18N
    public final static String LCHDE = "CIELch DE";        //NOI18N
    public final static String HUNTERDE = "HunterLab DE";  //NOI18N
    public final static String CIE2000DE = "CIE 2000";      //NOI18N
    public final static String JPC79 = "JPC 79";            //NOI18N
    public final static String DIN99 = "DIN 99";            //NOI18N

    static private MatchingFactory m_Instance;

    private final Map<String, DifferenceAlgorithm> m_Algorithms;
    private DifferenceAlgorithm m_DefaultAlgorithm;

    static public MatchingFactory getInstance()
    {
        if( m_Instance == null )
        {
            synchronized( MatchingFactory.class )
            {
                if( m_Instance == null )
                {
                    m_Instance = new MatchingFactory();
                }
            }
        }
        return m_Instance;
    }

    private MatchingFactory()
    {
        m_Algorithms = new HashMap<String, DifferenceAlgorithm>();
        initializeAlgorithms();
    }

    public DifferenceAlgorithm getAlgorithm( String name )
    {
        synchronized( m_Algorithms )
        {
            DifferenceAlgorithm algo = m_Algorithms.get( name );
            if( algo != null )
            {
                return algo;
            }
            throw new RuntimeException( "No algorithm found for " + name );
        }
    }

    public String[] getAlgorithmNames()
    {
        synchronized( m_Algorithms )
        {
            String[] names = new String[ m_Algorithms.size() ];
            Iterator list = m_Algorithms.keySet().iterator();
            for( int i = 0; list.hasNext(); i++ )
            {
                names[ i ] = (String) list.next();
            }
            return names;
        }
    }

    public DifferenceAlgorithm getDefaultAlgorithm()
    {
        return m_DefaultAlgorithm;
    }

    public void setDefaultAlgorithm( String name )
    {
        m_DefaultAlgorithm = m_Algorithms.get( name );
    }

    public void register( String name, DifferenceAlgorithm algorithm )
    {
        synchronized( m_Algorithms )
        {
            m_Algorithms.put( name, algorithm );
        }
    }

    private void initializeAlgorithms()
    {
        register( CMC11, new CMC( 1.0 ) );
        register( CMC21, m_DefaultAlgorithm );
        register( BFD, new BFD() );
        //register( LUVDE, new CIELuvDE( ) );
        register( CIE94DE, new CIE94() );
        register( LABDE, new CIELabDE() );
        register( LCHDE, new CIELchDE() );
        //register( HUNTERDE, new HunterLabDE() );
        register( CIE2000DE, new CIE2000() );
        register( JPC79, new JPC79() );
        register( DIN99, new Din99( 1.0, 1.0 ) );
        setDefaultAlgorithm( CMC21 );
    }
}
