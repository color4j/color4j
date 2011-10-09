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
 *  ManualReaderFrame.java
 *
 *  Created on May 6, 2002, 9:44 AM
 */
package org.color4j.spectro.drivers.manual;

import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 * @author Choong Hong Cheng
 * @created May 8, 2002
 */
public class ManualReaderFrame
    extends javax.swing.JFrame
{
    /**
     * Description of the Class
     *
     * @author chc
     * @created May 9, 2002
     */
    class DoubleCompare
        implements Comparator
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

    /**
     * Creates new form ManualReaderFrame
     *
     * @param parent Description of the Parameter
     */
    public ManualReaderFrame()
    {
        //m_Parent = parent;

        m_fc = new javax.swing.JFileChooser();

        // Declaration of global var
        m_result_list = new java.util.TreeMap( new DoubleCompare() );

        m_listmodel = new javax.swing.DefaultListModel();

        m_Listeners = new Vector();

        // Init the values in the JList and JLabel
        generate_list( 360, 10 );
        refresh_list( 360, 10 );
        refresh_map( 360, 10 );

        // The appearance
        initComponents();

        // Make sure the first item in JList is selected
        m_in_wavelength.setSelectedIndex( 0 );
    }

    /**
     * Update the TreeMap result *
     *
     * @param start    Starting value
     * @param interval Increment. Last value is 760.
     */
    private void refresh_map( int start, int interval )
    {
        m_result_list.clear();

        for( int x = start; x <= m_last_value; x = x + interval )
        {
            m_result_list.put( new Double( new Integer( x ).toString() ), new Double( "0" ) );
        }
    }

    /**
     * Update the label list with initial 0 values*
     *
     * @param start    Starting value
     * @param interval Increment. Last value is 760.
     */
    private void refresh_list( int start, int interval )
    {
        m_label_text = "<html>";

        for( int x = start; x <= m_last_value; x = x + interval )
        {
            m_label_text = m_label_text + "<p><b>" + x + "&nbsp;nm</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0.0";
        }
    }

//	/**
//	 *  Update the label list using the TreeMap*
//	 *
//	 *@param  start     Starting value
//	 *@param  interval  Increment. Last value is 760.
//	 */
//	private void reload_list( int start, int interval )
//	{
//		Double m_value = new Double( "0" );
//		m_label_text = "<html>";
//		m_total_count = 0;
//
//		for ( int x = start; x <= m_last_value; x = x + interval )
//		{
//			m_value = ( Double ) m_result_list.get( new Double( new Integer( x ).toString() ) );
//			m_total_count = m_total_count + m_value.doubleValue();
//			m_label_text = m_label_text + "<p><b>" + x + "&nbsp;nm</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + m_value.toString();
//		}
//	}

    /**
     * Check the map. Adjust the TreeMap if one of the item is more than 3. If one
     * of the item is more than 3, all of the items will be divided by 100.
     */
    private void check_map()
    {
        boolean m_adjust = false;
        Double m_value = new Double( "0" );
        m_label_text = "<html>";

        java.util.Iterator keys = m_result_list.keySet().iterator();
        java.util.Iterator keys2 = m_result_list.keySet().iterator();

        while( keys.hasNext() )
        {
            Double m_input = new Double( keys.next().toString() );
            m_value = (Double) m_result_list.get( m_input );

            if( m_value.intValue() > 3 )
            {
                m_adjust = true;
                break;
            }
        }

        // Do adjusting if necessary
        if( m_adjust == true )
        {
            while( keys2.hasNext() )
            {
                Double m_input = new Double( keys2.next().toString() );
                m_value = (Double) m_result_list.get( m_input );

                m_result_list.put( m_input, new Double( m_value.doubleValue() / 100 ) );
            }
        }
    }

    /**
     * Update the label list using the TreeMap
     */
    private void reload_list()
    {
        Double m_value = new Double( "0" );
        m_label_text = "<html>";
        m_total_count = 0;

        java.util.Iterator keys = m_result_list.keySet().iterator();

        while( keys.hasNext() )
        {
            Double m_input = new Double( keys.next().toString() );
            m_value = (Double) m_result_list.get( m_input );

            m_total_count = m_total_count + m_value.doubleValue();
            m_label_text = m_label_text + "<p><b>" + m_input.intValue() + "&nbsp;nm</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + m_value
                .toString();
        }
    }

    /**
     * Generate a DefaultListModel based on starting value and increment*
     *
     * @param start    Starting value
     * @param interval Increment. Last value is 760.
     */
    private void generate_list( int start, int interval )
    {
        m_listmodel.clear();

        for( int x = start; x <= m_last_value; x = x + interval )
        {
            m_listmodel.addElement( new Integer( x ) );
        }
    }

    /**
     * Description of the Method
     *
     * @param readings Description of the Parameter
     * @param filename Description of the Parameter
     */
    private void update_import( java.util.SortedMap readings, String filename )
    {
        java.util.Iterator keys = readings.keySet().iterator();

        m_listmodel.clear();
        m_result_list.clear();

        m_label_text = "<html>";
        m_total_count = 0;

        while( keys.hasNext() )
        {
            String key = (String) keys.next();
            String reflectionValue = (String) readings.get( key );

            try
            {
                Double m_value = new Double( reflectionValue );
                // Set the map
                m_result_list.put( new Double( key ), m_value );
                // Set the modal
                m_listmodel.addElement( key.trim() );

                // Setup the label
                m_total_count = m_total_count + m_value.doubleValue();
                m_label_text = m_label_text + "<p><b>" + key + "&nbsp;nm</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + m_value
                    .toString();
            }
            catch( NumberFormatException e )
            {
                // show the error dailog box
                javax.swing.JFrame frame = new javax.swing.JFrame();
                javax.swing
                    .JOptionPane
                    .showMessageDialog( frame, "Invalid value entered in the import file [" + filename + "] for " + key, "Input Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            }
        }
    }

    /**
     * APPEARANCE. WARNING: Do NOT modify this code.
     */
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        m_main = new javax.swing.JPanel();
        m_in_interval = new javax.swing.JComboBox();
        m_panel_values = new javax.swing.JPanel();
        m_scroll_wavelength = new javax.swing.JScrollPane();
        m_label_wavelengthlist = new javax.swing.JLabel();
        m_label_total = new javax.swing.JLabel();
        m_in_start = new javax.swing.JSlider();
        m_start = new javax.swing.JLabel();
        m_scroll_in_wavelength = new javax.swing.JScrollPane();
        m_in_wavelength = new javax.swing.JList( m_listmodel );
        m_panel_enter = new javax.swing.JPanel();
        m_in_wavelengthvalue = new javax.swing.JTextField();
        m_ok = new javax.swing.JButton();
        m_label_start = new javax.swing.JLabel();
        m_nm = new javax.swing.JLabel();
        m_label_interval = new javax.swing.JLabel();
        m_values = new javax.swing.JLabel();
        m_apply = new javax.swing.JButton();
        m_import = new javax.swing.JButton();

        getContentPane().setLayout( new java.awt.FlowLayout( java.awt.FlowLayout.CENTER, 0, 0 ) );

        setTitle( "Manual Reader Setup" );
        setResizable( false );

        addWindowListener(
            new java.awt.event.WindowAdapter()
            {
                public void windowClosing( java.awt.event.WindowEvent evt )
                {
                    if( !m_Saved )
                    {
                        StringBuffer message = new StringBuffer();
                        message.append( "Data entered was not saved. \n" );
                        message.append( "Do you want to save the data? \n" );

                        int r = JOptionPane.showConfirmDialog( null, message, "Window Closing", JOptionPane.YES_NO_OPTION );

                        if( r == JOptionPane.YES_OPTION )
                        {
                            notifyCreation( new ManualReadingEvent( this, m_result_list ) );
                            //m_Parent.readingGenerated( m_result_list );
                        }
                    }

                    exitForm();
                }
            } );

        m_main.setLayout( null );

        m_main.setPreferredSize( new java.awt.Dimension( 489, 427 ) );
        m_in_interval.setModel( new javax.swing.DefaultComboBoxModel( new String[]{ "1", "2", "5", "10", "20" } ) );
        m_in_interval.setSelectedIndex( 3 );
        m_in_interval.addActionListener(
            new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent evt )
                {
                    m_in_intervalActionPerformed( evt );
                }
            } );

        m_main.add( m_in_interval );
        m_in_interval.setBounds( 20, 80, 170, 25 );

        m_panel_values.setLayout( new java.awt.BorderLayout() );

        m_panel_values.setBorder( new javax.swing.border.TitledBorder( null, "Wavelength Values", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION ) );
        m_scroll_wavelength.setVerticalScrollBarPolicy( javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        m_label_wavelengthlist.setFont( new java.awt.Font( "Dialog", 0, 12 ) );
        m_label_wavelengthlist.setHorizontalAlignment( javax.swing.SwingConstants.CENTER );
        m_label_wavelengthlist.setText( "1" );
        m_label_wavelengthlist.setText( m_label_text );
        m_scroll_wavelength.setViewportView( m_label_wavelengthlist );

        m_panel_values.add( m_scroll_wavelength, java.awt.BorderLayout.CENTER );

        m_label_total.setText( "Total :  0.0" );
        m_panel_values.add( m_label_total, java.awt.BorderLayout.NORTH );

        m_main.add( m_panel_values );
        m_panel_values.setBounds( 240, 60, 230, 305 );

        m_in_start.setMaximum( 460 );
        m_in_start.setMinimum( 360 );
        m_in_start.setMinorTickSpacing( 10 );
        m_in_start.setSnapToTicks( true );
        m_in_start.addChangeListener(
            new javax.swing.event.ChangeListener()
            {
                public void stateChanged( javax.swing.event.ChangeEvent evt )
                {
                    m_in_startStateChanged( evt );
                }
            } );

        m_in_start.addMouseListener(
            new java.awt.event.MouseAdapter()
            {
                public void mouseClicked( java.awt.event.MouseEvent evt )
                {
                    m_in_startMouseClicked( evt );
                }
            } );

        m_main.add( m_in_start );
        m_in_start.setBounds( 20, 30, 390, 16 );

        m_start.setText( "360 nm" );
        m_main.add( m_start );
        m_start.setBounds( 420, 30, 48, 16 );

        m_scroll_in_wavelength.setVerticalScrollBarPolicy( javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

        m_in_wavelength.addMouseListener(
            new java.awt.event.MouseAdapter()
            {
                public void mouseClicked( java.awt.event.MouseEvent evt )
                {
                    m_in_wavelengthMouseClicked( evt );
                }
            } );

        m_scroll_in_wavelength.setViewportView( m_in_wavelength );

        m_main.add( m_scroll_in_wavelength );
        m_scroll_in_wavelength.setBounds( 20, 130, 200, 120 );

        m_panel_enter.setLayout( new java.awt.GridBagLayout() );

        m_panel_enter.setBorder( new javax.swing.border.TitledBorder( "Enter Wavelength Value" ) );
        m_in_wavelengthvalue.setText( "0.0" );
        m_in_wavelengthvalue.addKeyListener(
            new java.awt.event.KeyAdapter()
            {
                public void keyPressed( java.awt.event.KeyEvent evt )
                {
                    m_in_wavelengthvalueKeyPressed( evt );
                }
            } );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets( 14, 17, 3, 13 );
        m_panel_enter.add( m_in_wavelengthvalue, gridBagConstraints );

        m_ok.setText( "OK" );
        m_ok.addActionListener(
            new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent evt )
                {
                    m_okActionPerformed( evt );
                }
            } );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets( 0, 17, 13, 13 );
        m_panel_enter.add( m_ok, gridBagConstraints );

        m_main.add( m_panel_enter );
        m_panel_enter.setBounds( 20, 260, 200, 100 );

        m_label_start.setText( "Start Value" );
        m_main.add( m_label_start );
        m_label_start.setBounds( 200, 10, 67, 16 );

        m_nm.setText( "nm" );
        m_main.add( m_nm );
        m_nm.setBounds( 200, 90, 20, 16 );

        m_label_interval.setText( "Interval" );
        m_main.add( m_label_interval );
        m_label_interval.setBounds( 20, 60, 47, 16 );

        m_values.setText( "Values (nm)" );
        m_main.add( m_values );
        m_values.setBounds( 20, 110, 73, 16 );

        m_apply.setText( "Apply" );
        m_apply.addActionListener(
            new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent evt )
                {
                    m_applyActionPerformed( evt );
                }
            } );

        m_main.add( m_apply );
        m_apply.setBounds( 50, 380, 140, 26 );

        m_import.setText( "Import Data" );
        m_import.addActionListener(
            new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent evt )
                {
                    m_importActionPerformed( evt );
                }
            } );

        m_main.add( m_import );
        m_import.setBounds( 290, 380, 140, 26 );

        getContentPane().add( m_main );

        pack();
    }

    /**
     * Description of the Method
     *
     * @param evt Description of the Parameter
     */
    private void m_in_wavelengthvalueKeyPressed( java.awt.event.KeyEvent evt )
    {
        if( evt.getKeyCode() == KeyEvent.VK_ENTER )
        {
            Double input;
//			String m_length = ( String ) m_in_interval.getSelectedItem();
            javax.swing.ListModel m_size = m_in_wavelength.getModel();

            // Validate the input. Make sure is in proper number format
            input = new Double( m_in_wavelengthvalue.getText() );
            m_result_list.put( new Double( m_in_wavelength.getSelectedValue().toString() ), input );

            //reload_list( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
            reload_list();
            m_label_wavelengthlist.setText( m_label_text );

            if( m_in_wavelength.getSelectedIndex() < ( m_size.getSize() - 1 ) )
            {
                // Increase the selected item by one
                m_in_wavelength.setSelectedIndex( m_in_wavelength.getSelectedIndex() + 1 );
            }
            else
            {
                // If we reached the last time in the JList, disable the OK button
                m_ok.setEnabled( false );
            }

            m_label_total.setText( "Total : " + new Double( m_total_count ).toString() );
        }
    }

    /**
     * Description of the Method
     *
     * @param evt Description of the Parameter
     */
    private void m_importActionPerformed( java.awt.event.ActionEvent evt )
    {
        reflectionReader = new ReflectionReader();

        int m_returnVal = m_fc.showOpenDialog( m_fc );

        if( m_returnVal == javax.swing.JFileChooser.APPROVE_OPTION )
        {
            try
            {
                java.io.File file = m_fc.getSelectedFile();
                String fileName = file.getPath().trim();
                int startIndex = -1;

                if( ( startIndex = ( fileName.toLowerCase() ).indexOf( "http:" ) ) >= 0 )
                {
                    fileName = fileName.substring( startIndex + 6 );
                    System.out.println( "File name : " + fileName );
                    m_result_list_temp = reflectionReader.importReadings( "http://" + fileName );
                }

                else if( ( startIndex = ( fileName.toLowerCase() ).indexOf( "ftp:" ) ) >= 0 )
                {
                    fileName = fileName.substring( startIndex + 5 );
                    System.out.println( "File name : " + fileName );
                    m_result_list_temp = reflectionReader.importReadings( "ftp://" + fileName );
                }
                else
                {
                    m_result_list_temp = reflectionReader.importReadings( "file:" + file.getAbsolutePath() );
                }

                //dataImported = true;
                //reflectionTextBox.createTextBox( reflections, DEF_INTERVAL );

                // Refresh the stuff
                update_import( m_result_list_temp, fileName );
                m_label_wavelengthlist.setText( m_label_text );
                m_in_wavelength.setModel( m_listmodel );
                m_in_wavelength.setSelectedIndex( 0 );
                m_label_total.setText( "Total : " + m_total_count );
            }
            catch( java.net.MalformedURLException ex )
            {
                System.out.println( "Bad URL - " + ex );
            }
            catch( java.io.IOException ex )
            {
                System.out.println( "File not found - " + ex );
            }
            catch( java.util.NoSuchElementException ex )
            {
                StringBuffer message = new StringBuffer();
                message.append( "Invalid file format." );
                JOptionPane.showMessageDialog( null, message, "Invalid File", JOptionPane.WARNING_MESSAGE );
            }
            catch( RuntimeException ex )
            {
                System.out.println( "Runtime exception - " + ex );
            }
        }
    }

    /**
     * When Apply button is click
     *
     * @param evt Description of the Parameter
     */
    private void m_applyActionPerformed( java.awt.event.ActionEvent evt )
    {
        check_map();
        reload_list();

        // For debugging purpose to see whether correctly adjust the TreeMap
        //label_wavelengthlist.setText( m_label_text );

        //Pass the map of values to the ManualReading Parent
        /*
		if ( m_Parent != null )
		{
			m_Parent.readingGenerated( m_result_list );
		}
         */
        notifyCreation( new ManualReadingEvent( this, m_result_list ) );

        m_Saved = true;
        exitForm();
    }

    /**
     * When OK button is click
     *
     * @param evt Description of the Parameter
     */
    private void m_okActionPerformed( java.awt.event.ActionEvent evt )
    {
        Double input;
        //String m_length = ( String ) m_in_interval.getSelectedItem();
        javax.swing.ListModel m_size = m_in_wavelength.getModel();

        // Validate the input. Make sure is in proper number format
        try
        {
            input = new Double( m_in_wavelengthvalue.getText() );
            m_result_list.put( new Double( m_in_wavelength.getSelectedValue().toString() ), input );

            reload_list();
            m_label_wavelengthlist.setText( m_label_text );

            if( m_in_wavelength.getSelectedIndex() < ( m_size.getSize() - 1 ) )
            {
                // Increase the selected item by one
                m_in_wavelength.setSelectedIndex( m_in_wavelength.getSelectedIndex() + 1 );
            }
            else
            {
                // If we reached the last time in the JList, disable the OK button
                m_ok.setEnabled( false );
            }

            m_label_total.setText( "Total : " + m_total_count );
        }
        catch( NumberFormatException e )
        {
            // show the error dailog box
            javax.swing.JFrame frame = new javax.swing.JFrame();
            javax.swing
                .JOptionPane
                .showMessageDialog( frame, "Invalid value entered", "Input Error", javax.swing.JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * When item is selected ni JList, update the value in the "Enter Wavelength
     * Value"
     *
     * @param evt Description of the Parameter
     */
    private void m_in_wavelengthMouseClicked( java.awt.event.MouseEvent evt )
    {
        // Enable back the OK button
        m_ok.setEnabled( true );
        Double value = new Double( "0" );
        value = (Double) m_result_list.get( new Double( m_in_wavelength.getSelectedValue().toString() ) );

        m_in_wavelengthvalue.setText( value.toString() );
    }

    /**
     * When the start value slider is click, increase it accordingly
     *
     * @param evt Description of the Parameter
     */
    private void m_in_startMouseClicked( java.awt.event.MouseEvent evt )
    {
        String m_length = (String) m_in_interval.getSelectedItem();
        m_in_start.setValue( new Integer( m_in_start.getValue() ).intValue() + new Integer( m_length ).intValue() );
    }

    /**
     * When the start value slider is change, refresh the list in the JList and
     * the JLabel
     *
     * @param evt Description of the Parameter
     */
    private void m_in_startStateChanged( javax.swing.event.ChangeEvent evt )
    {
        m_start.setText( new Integer( m_in_start.getValue() ).toString() + " nm" );

        String m_length = (String) m_in_interval.getSelectedItem();
        generate_list( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
        refresh_list( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
        refresh_map( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
        m_label_wavelengthlist.setText( m_label_text );
        m_in_wavelength.setModel( m_listmodel );
        m_in_wavelength.setSelectedIndex( 0 );
        m_label_total.setText( "Total : 0.0" );
    }

    /**
     * When the interval JComboList is click, refresh the list as well
     *
     * @param evt Description of the Parameter
     */
    private void m_in_intervalActionPerformed( java.awt.event.ActionEvent evt )
    {
        String m_length = (String) m_in_interval.getSelectedItem();
        m_in_start.setMinorTickSpacing( new Integer( m_length ).intValue() );
        m_in_start.setValue( 360 );

        generate_list( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
        refresh_list( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
        refresh_map( new Integer( m_in_start.getValue() ).intValue(), new Integer( m_length ).intValue() );
        m_label_wavelengthlist.setText( m_label_text );
        m_in_wavelength.setModel( m_listmodel );
        m_in_wavelength.setSelectedIndex( 0 );
        m_label_total.setText( "Total : 0.0" );
    }

    /**
     * Exit the Application
     */
    public void exitForm()
    {
        dispose();
    }

    /**
     * Description of the Method
     */
    public void pack()
    {
        super.pack();
        java.awt.Dimension m_dialogsize = getSize();
        java.awt.Dimension m_screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( ( m_screensize.width - m_dialogsize.width ) / 2, ( m_screensize.height - m_dialogsize.height ) / 2 );
    }

    public void addManualReadingListener( ManualReadingListener l )
    {
        m_Listeners.add( l );
    }

    public void removeManualReadingListener( ManualReadingListener l )
    {
        m_Listeners.remove( l );
    }

    public void notifyCreation( ManualReadingEvent evt )
    {
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (ManualReadingListener) list.next() ).manualReadingCreated( evt );
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main( String args[] )
    {
        new ManualReaderFrame().show();
    }

    // APPREARANCE
    // Variables declaration - do not modify
    private javax.swing.JScrollPane m_scroll_wavelength;
    private javax.swing.JLabel m_label_wavelengthlist;
    private javax.swing.JButton m_import;
    private javax.swing.JLabel m_values;
    private javax.swing.JButton m_ok;
    private javax.swing.JTextField m_in_wavelengthvalue;
    private javax.swing.JList m_in_wavelength;
    private javax.swing.JLabel m_label_total;
    private javax.swing.JButton m_apply;
    private javax.swing.JSlider m_in_start;
    private javax.swing.JScrollPane m_scroll_in_wavelength;
    private javax.swing.JLabel m_nm;
    private javax.swing.JLabel m_start;
    private javax.swing.JPanel m_panel_values;
    private javax.swing.JComboBox m_in_interval;
    private javax.swing.JPanel m_panel_enter;
    private javax.swing.JPanel m_main;
    private javax.swing.JLabel m_label_start;
    private javax.swing.JLabel m_label_interval;
    // End of variables declaration

    private javax.swing.DefaultListModel m_listmodel;

    // Variable which hold the JLabel text
    private String m_label_text;

    // Total count for checksum
    private double m_total_count = 0;

    // The TreeMap
    private java.util.Map m_result_list;
    private java.util.SortedMap m_result_list_temp;

    // Last value, modify here if want to change. Don't modify the functions.
    private int m_last_value = 760;

    //private ManualReading m_Parent;
    private boolean m_Saved = false;

    private javax.swing.JFileChooser m_fc;
    private ReflectionReader reflectionReader;

    private Vector m_Listeners;
}

