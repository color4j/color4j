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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 */
public class SerialCommFrame extends JFrame
    implements CommDriverListener
{

    private CommDriver m_Driver;
    private javax.swing.JPanel jContentPane = null;

    private JLabel m_lblSend = null;
    private JLabel m_lblRecieved = null;
    private JTextArea m_areaRecieved = null;
    private JTextField m_fldSend = null;
    private JScrollPane jScrollPane = null;

    /**
     * This is the default constructor
     */
    public SerialCommFrame()
    {
        super();
        initialize();
    }

    public void setDriver( CommDriver c, String portname )
        throws Exception
    {
        m_Driver = c;
        c.openConnection( portname, 30000, 9600 );
        c.addCommDriverListener( this );
//	    m_Driver.send( "R\r\n\u0004".getBytes() );
//	    c.send( "*".getBytes() );
//	    c.send("GI ****:\r\n".getBytes() );
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize()
    {
        this.setTitle( "SerialComm" );  // Generated
        this.setSize( 500, 300 );
        this.setContentPane( getJContentPane() );
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane()
    {
        if( jContentPane == null )
        {
            java.awt.GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            m_lblRecieved = new JLabel();
            m_lblSend = new JLabel();
            java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout( new GridBagLayout() );
            m_lblSend.setText( "Send" );  // Generated
            m_lblRecieved.setText( "Recieved" );  // Generated
            gridBagConstraints1.gridx = 0;  // Generated
            gridBagConstraints1.gridy = 0;  // Generated
            gridBagConstraints1.ipadx = 250;  // Generated
            gridBagConstraints1.insets = new java.awt.Insets( 0, 0, 0, 0 );  // Generated
            gridBagConstraints2.gridx = 0;  // Generated
            gridBagConstraints2.gridy = 3;  // Generated
            gridBagConstraints2.ipadx = 250;  // Generated
            gridBagConstraints2.insets = new java.awt.Insets( 0, 0, 0, 0 );  // Generated
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;  // Generated
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;  // Generated
            gridBagConstraints2.weightx = 1.0D;  // Generated
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;  // Generated
            gridBagConstraints1.weightx = 1.0D;  // Generated
            gridBagConstraints1.weighty = 0.0D;  // Generated
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;  // Generated
            gridBagConstraints21.gridx = 0;  // Generated
            gridBagConstraints21.gridy = 1;  // Generated
            gridBagConstraints21.weightx = 1.0;  // Generated
            gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;  // Generated
            gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTHWEST;  // Generated
            gridBagConstraints12.gridx = 0;  // Generated
            gridBagConstraints12.gridy = 4;  // Generated
            gridBagConstraints12.weightx = 1.0;  // Generated
            gridBagConstraints12.weighty = 1.0;  // Generated
            gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;  // Generated
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.NORTHWEST;  // Generated
            jContentPane.add( m_lblSend, gridBagConstraints1 );  // Generated
            jContentPane.add( m_lblRecieved, gridBagConstraints2 );  // Generated
            jContentPane.add( getM_fldSend(), gridBagConstraints21 );  // Generated
            jContentPane.add( getJScrollPane(), gridBagConstraints12 );  // Generated
        }
        return jContentPane;
    }

    public void received( CommDriverEvent event )
    {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( m_areaRecieved.getText() );
        sb.append( "Recieved:\n" );
        byte[] arr = m_Driver.receive();
        sb.append( "int :" );
        for( int i = 0; i < arr.length; i++ )
        {
            sb.append( new Byte( arr[ i ] ).intValue() );
            sb.append( "_" );
        }
        sb.append( "<end>\n" );
        sb.append( "hex :" );
        for( int i = 0; i < arr.length; i++ )
        {
            sb.append( Integer.toHexString( new Byte( arr[ i ] ).intValue() ) );
            sb.append( "_" );
        }
        sb.append( "<end>\n" );
        sb.append( "bin :" );
        for( int i = 0; i < arr.length; i++ )
        {
            sb.append( Integer.toBinaryString( new Byte( arr[ i ] ).intValue() ) );
            sb.append( "_" );
        }
        sb.append( "<end>\n" );

        sb.append( "string :" );
        sb.append( new String( arr ) );
        sb.append( "<end>\n" );

        m_areaRecieved.setText( sb.toString() );
        sb.setLength( 0 );
        sb = null;
    }

    public void sent( CommDriverEvent event )
    {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( m_areaRecieved.getText() );
        sb.append( "\nSent Command\n" );
        m_areaRecieved.setText( sb.toString() );
        sb.setLength( 0 );
        sb = null;
    }

    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextArea
     */
    private JTextArea getM_areaRecieved()
    {
        if( m_areaRecieved == null )
        {
            m_areaRecieved = new JTextArea();
            m_areaRecieved.setText( "" );  // Generated
        }
        return m_areaRecieved;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getM_fldSend()
    {
        if( m_fldSend == null )
        {
            m_fldSend = new JTextField();
            m_fldSend.addActionListener( new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent e )
                {
                    JTextField fld = (JTextField) e.getSource();
                    String send = fld.getText();
                    m_Driver.send( send.getBytes() );
//				    m_Driver.send( "\r\n\u0004".getBytes() );
                    m_Driver.send( "\r".getBytes() );
                    m_Driver.setRespondTimeout( 15000 );
                }
            } );
        }
        return m_fldSend;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane()
    {
        if( jScrollPane == null )
        {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView( getM_areaRecieved() );  // Generated
        }
        return jScrollPane;
    }
}
