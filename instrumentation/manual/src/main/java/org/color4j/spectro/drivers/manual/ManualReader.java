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

package org.color4j.spectro.drivers.manual;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

/**
 * An openable window available to the IDE's window manager.
 *
 */
//public class ManualReader extends CloneableTopComponent implements TableModelListener
public class ManualReader extends TopComponent implements TableModelListener, FocusListener
{
    public ManualReader()
    {
        setName( NbBundle.getMessage( ManualReader.class, "LBL_Name" ) );
        initComponents();
        init_table();
        //Default to 20nm intervals
        m_interval.setSelectedItem( "20" );

        // Create radio group
        ButtonGroup group = new ButtonGroup();
        group.add( m_range_1 );
        group.add( m_range_100 );

        setCloseOperation( CLOSE_LAST ); // or CLOSE_EACH

        // Add in listener to give warning
        m_Listeners = new Vector();

        // Set up the map
        m_result_list = new java.util.TreeMap( new DoubleCompare() );
    }

    public boolean canClose( Workspace ws, boolean last )
    {
        // You might want to prompt the user first and maybe return false:
        if( !m_Saved )
        {
            String message = NbBundle.getMessage( ManualReader.class, "MSG_DATA_NOT_SAVED" );
            String title = NbBundle.getMessage( ManualReader.class, "TITLE_WINDOW_CLOSING" );

            org.openide.NotifyDescriptor m_confirm = new org.openide.NotifyDescriptor.Confirmation( message, title, org.openide.NotifyDescriptor.YES_NO_OPTION );

            Object m_result = DialogDisplayer.getDefault().notify( m_confirm );

            if( m_result == org.openide.NotifyDescriptor.YES_OPTION )
            {
                notifyCreation( null );
                m_Saved = false;
                m_first_checking = false;
                reset_table();
                return true;
            }
            else if( m_result == org.openide.NotifyDescriptor.NO_OPTION )
            {
                return false;
            }
            else
            {
                return false;
            }
        }
        else
        {
            // Reset stuffs
            m_Saved = false;
            m_first_checking = false;
            reset_table();
            return true;
        }
    }

    private void init_table()
    {
        int m_temp = new Integer( m_interval.getSelectedItem().toString() ).intValue();

        int m_tempwavelength = 400;
        int m_loop = ( ( 700 - 400 ) / m_temp ) + 1;

        Object[][] m_values = new Object[ m_loop ][ m_loop ];

        for( int i = 0; i < m_values.length; i++ )
        {
            Object[][] m_tempvalue = new Object[][]
                {
                    { new String( m_tempwavelength + " nm" ), null }
                };
            System.arraycopy( m_tempvalue, 0, m_values, i, 1 );

            m_tempwavelength = m_tempwavelength + m_temp;
        }

        String[] titles = new String[ 2 ];
        titles[ 0 ] = NbBundle.getMessage( ManualReader.class, "LBL_WAVELENGTH" );//"Wavelength";
        titles[ 1 ] = NbBundle.getMessage( ManualReader.class, "LBL_VALUES" );//"Value";

        m_table_input.setModel( new javax.swing.table.DefaultTableModel( m_values, titles )
        {
            Class[] types = new Class[]
                {
                    java.lang.Object.class, java.lang.Double.class
                };
            boolean[] canEdit = new boolean[]
                {
                    false, true
                };

            public Class getColumnClass( int columnIndex )
            {
                return types[ columnIndex ];
            }

            public boolean isCellEditable( int rowIndex, int columnIndex )
            {
                return canEdit[ columnIndex ];
            }
        } );

        m_tablemodel = (DefaultTableModel) m_table_input.getModel();
        m_tablemodel.addTableModelListener( this );

        //m_label_total.setText("Checksum : 0.0");
        m_label_total.setText( NbBundle.getMessage( ManualReader.class, "LBL_CHECKSUM" ) + " 0.0" );

        m_table_input.addFocusListener( this );

        m_table_input.getTableHeader().setReorderingAllowed( false );
    }

    private void reset_table()
    {
        m_tablemodel.setRowCount( 0 );

        int m_Interval = new Integer( m_interval.getSelectedItem().toString() ).intValue();

        int m_tempwavelength = 400;
        int m_loop = ( ( 700 - 400 ) / m_Interval ) + 1;

        for( int i = 0; i < m_loop; i++ )
        {
            m_tablemodel.addRow( new String[]
                                 { m_tempwavelength + " nm", null } );
            m_tempwavelength = m_tempwavelength + m_Interval;
        }

        m_label_total.setText( NbBundle.getMessage( ManualReader.class, "LBL_CHECKSUM" ) + " 0.0" );
    }

    private boolean proccess_map( boolean flag_message )
    {
        // Check whether want to divide by 100
        //String message = "Please make sure these following wavelength contain values : \n";
        String message = NbBundle.getMessage( ManualReader.class, "MSG_NO_VALUES_ENTERED" );
        //message = ResourceBundle.getBundle( "Bundle.properties" ).getString( "MSG_NO_VALUES_ENTERED" );
        boolean return_value = true;

        if( m_range_100.isSelected() )
        {
            int m_total = m_tablemodel.getRowCount();
            int m_intervalvalue = new Integer( m_interval.getSelectedItem().toString() ).intValue();

            int m_tempwavelength = 400;

            Double m_value = new Double( "0" );

            for( int i = 0; i < m_total; i++ )
            {
                try
                {
                    Object m_tempvalue = m_tablemodel.getValueAt( i, 1 );

                    double temp = new Double( m_tempvalue.toString() ).doubleValue() / 100;

                    m_value = new Double( temp );
                }
                catch( NullPointerException e )
                {
                    return_value = false;

                    if( i == ( m_total - 1 ) )
                    {
                        message = message + m_tempwavelength + "nm.";
                    }
                    else
                    {
                        message = message + m_tempwavelength + "nm, ";
                    }
                }

                m_result_list.put( new Double( new Integer( m_tempwavelength ).toString() ), m_value );

                //javax.swing.JOptionPane.showMessageDialog (null,m_tempwavelength+" : "+m_value.toString ());

                // Increase the wavelength
                m_tempwavelength = m_tempwavelength + m_intervalvalue;
            }
        }
        else if( m_range_1.isSelected() )
        {
            // No need to divide by 100
            int m_total = m_tablemodel.getRowCount();
            int m_intervalvalue = new Integer( m_interval.getSelectedItem().toString() ).intValue();

            int m_tempwavelength = 400;

            Double m_value = new Double( "0" );

            for( int i = 0; i < m_total; i++ )
            {

                try
                {
                    Object m_tempvalue = m_tablemodel.getValueAt( i, 1 );
                    m_value = new Double( m_tempvalue.toString() );
                }
                catch( NullPointerException e )
                {
                    //m_value = new Double( "0" );
                    return_value = false;

                    if( i == ( m_total - 1 ) )
                    {
                        message = message + m_tempwavelength + "nm.";
                    }
                    else
                    {
                        message = message + m_tempwavelength + "nm, ";
                    }
                }

                m_result_list.put( new Double( new Integer( m_tempwavelength ).toString() ), m_value );

                //javax.swing.JOptionPane.showMessageDialog (null,m_tempwavelength+" : "+m_value.toString ());

                // Increase the wavelength
                m_tempwavelength = m_tempwavelength + m_intervalvalue;
            }
        }
        else
        {
            int m_total = m_tablemodel.getRowCount();
//            int m_intervalvalue = new Integer(m_interval.getSelectedItem().toString()).intValue();

            Double m_value = new Double( "0" );
            Double m_key = new Double( "0" );

            for( int i = 0; i < m_total; i++ )
            {

                try
                {
                    Object m_tempvalue = m_tablemodel.getValueAt( i, 1 );

                    m_value = new Double( m_tempvalue.toString() );
                }
                catch( NullPointerException e )
                {
                    //m_value = new Double( "0" );
                    return_value = false;
                }

                String m_tempkey = (String) m_tablemodel.getValueAt( i, 0 );

                m_key = new Double( m_tempkey.substring( 0, m_tempkey.length() - 2 ) );

                m_result_list.put( m_key, m_value );
            }
        }

        if( return_value == false && flag_message == true )
        {
            org.openide.NotifyDescriptor m_message = new org.openide.NotifyDescriptor.Message( message );
            DialogDisplayer.getDefault().notify( m_message );
        }

        return return_value;
    }

    // APPEARANCE

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        java.awt.GridBagConstraints gridBagConstraints;

        m_panel1 = new javax.swing.JPanel();
        m_panel1a = new javax.swing.JPanel();
        m_label_inteval = new javax.swing.JLabel();
        m_interval = new javax.swing.JComboBox();
        m_label_nm = new javax.swing.JLabel();
        m_panel1c = new javax.swing.JPanel();
        m_range_1 = new javax.swing.JRadioButton();
        m_range_100 = new javax.swing.JRadioButton();
        m_scroll_input = new javax.swing.JScrollPane();
        m_table_input = new javax.swing.JTable();
        m_panel1b = new javax.swing.JPanel();
        m_reset = new javax.swing.JButton();
        m_reset.setMnemonic( KeyEvent.VK_R );
        m_apply = new javax.swing.JButton();
        m_apply.setMnemonic( KeyEvent.VK_A );
        m_label_total = new javax.swing.JLabel();

        setLayout( new java.awt.BorderLayout() );

        setMaximumSize( new java.awt.Dimension( 372, 600 ) );
        setMinimumSize( new java.awt.Dimension( 372, 400 ) );
        setPreferredSize( new java.awt.Dimension( 372, 480 ) );
        m_panel1.setLayout( new java.awt.GridBagLayout() );

        m_panel1.setMaximumSize( new java.awt.Dimension( 375, 550 ) );
        m_panel1.setMinimumSize( new java.awt.Dimension( 350, 550 ) );
        m_panel1.setPreferredSize( new java.awt.Dimension( 350, 550 ) );
        m_panel1a.setLayout( new java.awt.GridBagLayout() );

        m_panel1.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
        m_label_inteval.setText( org.openide.util.NbBundle.getMessage( ManualReader.class, "LBL_Interval" ) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets( 0, 0, 0, 4 );
        m_panel1a.add( m_label_inteval, gridBagConstraints );

        m_interval.setModel( new javax.swing.DefaultComboBoxModel( new String[]{ "10", "20" } ) );
        m_interval.setMaximumSize( new java.awt.Dimension( 60, 25 ) );
        m_interval.setMinimumSize( new java.awt.Dimension( 60, 25 ) );
        m_interval.setPreferredSize( new java.awt.Dimension( 60, 25 ) );
        m_panel1a.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
        m_interval.addItemListener( new java.awt.event.ItemListener()
        {
            public void itemStateChanged( java.awt.event.ItemEvent evt )
            {
                m_intervalItemStateChanged( evt );
            }
        } );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets( 0, 4, 0, 4 );
        m_panel1a.add( m_interval, gridBagConstraints );

        m_panel1a.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
        m_label_nm.setText( org.openide.util.NbBundle.getMessage( ManualReader.class, "LBL_NM" ) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets( 0, 4, 0, 0 );
        m_panel1a.add( m_label_nm, gridBagConstraints );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        m_panel1.add( m_panel1a, gridBagConstraints );

        m_panel1c.setLayout( new java.awt.GridBagLayout() );

        m_panel1.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
        m_range_1.setText( org.openide.util.NbBundle.getMessage( ManualReader.class, "LBL_Value_0_1" ) );
        m_range_1.setActionCommand( "value1" );
        m_range_1.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent evt )
            {
                m_range_1ActionPerformed( evt );
            }
        } );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        m_panel1c.add( m_range_1, gridBagConstraints );

        m_panel1c.add( Box.createRigidArea( new Dimension( 30, 0 ) ) );
        m_range_100.setSelected( true );
        m_range_100.setText( org.openide.util.NbBundle.getMessage( ManualReader.class, "LBL_Value_1_100" ) );
        m_range_100.setActionCommand( "value100" );
        m_range_100.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent evt )
            {
                m_range_100ActionPerformed( evt );
            }
        } );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        m_panel1c.add( m_range_100, gridBagConstraints );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        m_panel1.add( m_panel1c, gridBagConstraints );

        m_panel1.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
        m_table_input.setModel( new javax.swing.table.DefaultTableModel(
            new Object[][]
            {

            },
            new String[]
            {

            }
        ) );
        m_table_input.setRowHeight( 20 );
        m_scroll_input.setViewportView( m_table_input );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        m_panel1.add( m_scroll_input, gridBagConstraints );

        m_panel1b.setLayout( new java.awt.GridBagLayout() );

        m_panel1.add( Box.createRigidArea( new Dimension( 0, 20 ) ) );
        m_reset.setText( org.openide.util.NbBundle.getMessage( ManualReader.class, "LBL_Reset" ) );
        m_reset.setMaximumSize( new java.awt.Dimension( 109, 26 ) );
        m_reset.setMinimumSize( new java.awt.Dimension( 109, 26 ) );
        m_reset.setPreferredSize( new java.awt.Dimension( 109, 26 ) );
        m_reset.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent evt )
            {
                m_resetActionPerformed( evt );
            }
        } );

        m_panel1b.add( m_reset, new java.awt.GridBagConstraints() );

        m_apply.setText( org.openide.util.NbBundle.getBundle( ManualReader.class ).getString( "LBL_Apply" ) );
        m_apply.setMaximumSize( new java.awt.Dimension( 109, 26 ) );
        m_apply.setMinimumSize( new java.awt.Dimension( 109, 26 ) );
        m_apply.setPreferredSize( new java.awt.Dimension( 109, 26 ) );
        m_apply.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent evt )
            {
                m_applyActionPerformed( evt );
            }
        } );

        m_panel1b.add( m_apply, new java.awt.GridBagConstraints() );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets( 2, 2, 2, 2 );
        m_panel1.add( m_panel1b, gridBagConstraints );

        m_label_total.setHorizontalAlignment( javax.swing.SwingConstants.TRAILING );
        m_label_total.setText( org.openide.util.NbBundle.getMessage( ManualReader.class, "LBL_CHECKSUM" ) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets( 2, 2, 2, 2 );
        m_panel1.add( m_label_total, gridBagConstraints );

        add( m_panel1, java.awt.BorderLayout.CENTER );
    }//GEN-END:initComponents

    private void m_range_100ActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_m_range_100ActionPerformed
    {//GEN-HEADEREND:event_m_range_100ActionPerformed
        if( evt.getActionCommand().equals( "value100" ) )
        {
            m_range_1.setSelected( false );
            m_range_100.setSelected( !m_range_1.isSelected() );

            m_range = 100;
        }
    }//GEN-LAST:event_m_range_100ActionPerformed

    private void m_range_1ActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_m_range_1ActionPerformed
    {//GEN-HEADEREND:event_m_range_1ActionPerformed
        if( evt.getActionCommand().equals( "value1" ) )
        {
            m_range_100.setSelected( false );
            m_range_1.setSelected( !m_range_100.isSelected() );

            m_range = 1;
        }
    }//GEN-LAST:event_m_range_1ActionPerformed

    private void m_applyActionPerformed( java.awt.event.ActionEvent evt )
    {//GEN-FIRST:event_m_applyActionPerformed
        if( proccess_map( true ) )
        {
            notifyCreation( new ManualReadingEvent( this, m_result_list ) );
            m_Saved = true;
            m_first_checking = false;

            reset_table();
            close();
        }
    }//GEN-LAST:event_m_applyActionPerformed

    private void m_resetActionPerformed( java.awt.event.ActionEvent evt )
    {//GEN-FIRST:event_m_resetActionPerformed
        m_range_100.setSelected( false );
        m_range_1.setSelected( false );

//        m_total = 0;
        m_first_checking = false;

        reset_table();
    }//GEN-LAST:event_m_resetActionPerformed

    private void m_intervalItemStateChanged( java.awt.event.ItemEvent evt )
    {//GEN-FIRST:event_m_intervalItemStateChanged
//        m_total = 0;

        reset_table();
    }//GEN-LAST:event_m_intervalItemStateChanged

    public void tableChanged( javax.swing.event.TableModelEvent tableModelEvent )
    {
        int row = tableModelEvent.getFirstRow();
        int column = tableModelEvent.getColumn();

        if( column < 0 || row < 0 )
        {
            return;
        }

        String columnName = m_tablemodel.getColumnName( column );

        if( columnName.equals( NbBundle.getMessage( ManualReader.class, "LBL_WAVELENGTH" ) ) )
        {
            return;
        }

        Object m_value = m_tablemodel.getValueAt( row, column );

        if( m_value == null )
        {
            computeChecksum();
            // Check whether is a -ve value
        }
        else if( Pattern.matches( "-[0-9].*", m_value.toString() ) )
        {
            notifyflag = true;
            org.openide.NotifyDescriptor m_message = new org.openide.NotifyDescriptor.Message( "Value entered is a negative value" );
            DialogDisplayer.getDefault().notify( m_message );

            m_tablemodel.setValueAt( null, row, column );
            notifyflag = false;
        }
        else
        {

            try
            {
                if( new Double( m_value.toString() ).compareTo( new Double( new String( "" + m_range ) ) ) > 0 )
                {
                    if( m_first_checking == false )
                    {
                        notifyflag = true;

                        org.openide.NotifyDescriptor.Confirmation m_confirm = new org.openide.NotifyDescriptor.Confirmation( "Value entered is greater than " + m_range, "Warning", org.openide.NotifyDescriptor.OK_CANCEL_OPTION );

                        if( DialogDisplayer.getDefault().notify( m_confirm ) == org.openide.NotifyDescriptor.OK_OPTION )
                        {
                            m_first_checking = true;
                        }

                        notifyflag = false;
                    }
                    //}
                }

                computeChecksum();
            }
            catch( NullPointerException e )
            {
                err.notify( ErrorManager.USER, new Exception( "Please enter a valid value" ) );
            }
        }
    }

    public void computeChecksum()
    {
        double checksum = 0.0;

        for( int i = 0; i < m_tablemodel.getRowCount(); i++ )
        {
            Double value = (Double) m_tablemodel.getValueAt( i, 1 );

            if( value != null )
            {
                checksum += value.doubleValue();
            }
        }

        java.text.DecimalFormat format = new java.text.DecimalFormat();
        format.setMaximumFractionDigits( 3 );
        format.setMinimumFractionDigits( 2 );

        m_label_total.setText( "Checksum : " + format.format( checksum ).toString() );
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
     * Invoked when a component gains the keyboard focus.
     */
    public void focusGained( FocusEvent e )
    {
    }

    /**
     * Invoked when a component loses the keyboard focus.
     */
    public void focusLost( FocusEvent e )
    {
        TableCellEditor cell = m_table_input.getCellEditor();

        if( cell != null && notifyflag == false )
        {
            cell.stopCellEditing();
            computeChecksum();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton m_apply;
    private javax.swing.JComboBox m_interval;
    private javax.swing.JLabel m_label_inteval;
    private javax.swing.JLabel m_label_nm;
    private javax.swing.JLabel m_label_total;
    private javax.swing.JPanel m_panel1;
    private javax.swing.JPanel m_panel1a;
    private javax.swing.JPanel m_panel1b;
    private javax.swing.JPanel m_panel1c;
    private javax.swing.JRadioButton m_range_1;
    private javax.swing.JRadioButton m_range_100;
    private javax.swing.JButton m_reset;
    private javax.swing.JScrollPane m_scroll_input;
    private javax.swing.JTable m_table_input;
    // End of variables declaration//GEN-END:variables
    private DefaultTableModel m_tablemodel;
    private int m_range = 100;

    //    private int m_total = 0;
    private java.util.Map m_result_list;
//    private java.util.SortedMap m_result_list_temp;

//    private javax.swing.JFileChooser m_fc;
//    private ReflectionReader reflectionReader;

    private Vector m_Listeners;

    private boolean m_Saved = false;
    private boolean m_first_checking = false;

    private boolean notifyflag = false;

    private ErrorManager err;

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
