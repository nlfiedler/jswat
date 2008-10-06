/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: VMStartAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.JVMArguments;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * Implements the Virtual Machine start action. It doens't do much
 * except activate the Session using the provided arguments.
 *
 * @author  Nathan Fiedler
 */
public class VMStartAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VMStartAction object with the default action
     * command string of "vmStart".
     */
    public VMStartAction() {
        super("vmStart");
    } // VMStartAction

    /**
     * Performs the virtual machine start action. Gets the name of the
     * class to be debugged and starts a virtual machine.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        final Frame topFrame = getFrame(event);
        final Session session = getSession(event);

        String javaHome = session.getProperty("javaHome");
        String jvmExecutable = session.getProperty("jvmExecutable");
        String jvmOptions = session.getProperty("jvmOptions");
        String mainClass = session.getProperty("mainClass");
        String suspended = session.getProperty("startSuspended");
        boolean startSuspended = false;
        if (suspended != null && suspended.equalsIgnoreCase("true")) {
            startSuspended = true;
        }
        // Use the launching connector for default values.
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map args = connector.defaultArguments();
        if (javaHome == null || javaHome.length() == 0) {
            javaHome = ((Connector.Argument) args.get("home")).value();
        }
        if (jvmExecutable == null || jvmExecutable.length() == 0) {
            jvmExecutable = ((Connector.Argument) args.get("vmexec")).value();
        }

        final JTextField optionsField = new JTextField(jvmOptions, 30);

        JButton button = new JButton(Bundle.getString("VMStart.setProps"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PropertiesDialog pd = new PropertiesDialog(
                        topFrame, session, optionsField);
                    pd.pack();
                    pd.setLocationRelativeTo(topFrame);
                    pd.setResizable(false);
                    pd.setVisible(true);
                }
            });

        // Ask the user for name of class to launch, along with
        // arguments to that class, and args to JVM.
        Object[] messages = {
            Bundle.getString("VMStart.javaHome"),
            new JTextField(javaHome, 30),
            Bundle.getString("VMStart.jvmExecutable"),
            new JTextField(jvmExecutable, 30),
            Bundle.getString("VMStart.jvmArgsField"),
            optionsField,
            button,
            Bundle.getString("VMStart.nameAndArgsField"),
            new JTextField(mainClass, 30),
            new JCheckBox(Bundle.getString("VMStart.startSuspended"),
                          startSuspended)
        };

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog to get user input.
            int response = JOptionPane.showOptionDialog(
                topFrame, messages,
                Bundle.getString("VMStart.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                // user cancelled
                return;
            }

            mainClass = ((JTextField) messages[8]).getText();
            if (mainClass == null || mainClass.length() == 0) {
                // Missing the classname to load.
                displayError(event, Bundle.getString("VMStart.missingClass"));
            } else {
                responseOkay = true;
            }
        }

        javaHome = ((JTextField) messages[1]).getText();
        jvmExecutable = ((JTextField) messages[3]).getText();
        jvmOptions = optionsField.getText();
        startSuspended = ((JCheckBox) messages[9]).isSelected();
        JVMArguments jvmArgs = new JVMArguments(jvmOptions + ' ' + mainClass);
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        String classpath = pathman.getClassPathAsString();
        jvmOptions = jvmArgs.normalizedOptions(classpath);
        // Could be getting a main class or jar file name here.
        mainClass = jvmArgs.stuffAfterOptions();

        if (session.isActive()) {
            // Deactivate current session.
            session.deactivate(false, this);
        }

        VMConnection connection = null;
        try {
            connection = VMConnection.buildConnection(
                javaHome, jvmExecutable, jvmOptions, mainClass);
        } catch (IllegalArgumentException iae) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                iae.getMessage());
            return;
        }

        // Display the options and classname that the launcher is
        // actually going to use.
        Log out = session.getStatusLog();
        out.writeln(connection.loadingString());

        // Save the values to the session settings for later reuse.
        session.setProperty("javaHome", javaHome);
        session.setProperty("jvmExecutable", jvmExecutable);
        session.setProperty("jvmOptions", jvmArgs.parsedOptions());
        session.setProperty("mainClass", mainClass);
        session.setProperty("startSuspended",
                            String.valueOf(startSuspended));

        // Show a busy cursor while we launch the debuggee.
        topFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean launched = connection.launchDebuggee(session, true);
        topFrame.setCursor(Cursor.getDefaultCursor());

        if (launched) {
            if (!startSuspended) {
                // Now that the Session has completely activated,
                // we may resume the debuggee VM.
                session.resumeVM(this, false, false);
            }
        } else {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                com.bluemarsh.jswat.Bundle.getString("vmLoadFailed"));
        }
    } // actionPerformed
} // VMStartAction

/**
 * Class PropertiesDialog acquires the parameters for launching the VM.
 *
 * @author  Nathan Fiedler
 */
class PropertiesDialog extends JDialog implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Associated Session instance. */
    private Session session;
    /** Field from which properties come and go. */
    private JTextField optionsField;
    /** Properties builder. */
    private PropertiesBuilder builder;

    /**
     * Constructs a PropertiesDialog.
     *
     * @param  owner    owner of this dialog.
     * @param  session  associated Session.
     * @param  field    text field from which to get properties,
     *                  and to which the properties are saved.
     */
    public PropertiesDialog(Frame owner, Session session,
                            JTextField field) {
        super(owner, Bundle.getString("VMStartProps.title"), true);
        this.session = session;
        optionsField = field;

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        Container contentPane = getContentPane();
        contentPane.setLayout(gbl);
        gbc.insets = new Insets(10, 10, 10, 10);
        JPanel panel = new JPanel(new GridBagLayout());
        gbl.setConstraints(panel, gbc);
        contentPane.add(panel);
        contentPane = panel;
        gbl = (GridBagLayout) panel.getLayout();

        gbc.insets = new Insets(2, 2, 2, 2);

        builder = new PropertiesBuilder();
        builder.setProperties(field.getText());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(builder, gbc);
        contentPane.add(builder);

        JPanel buttonPane = new JPanel();
        gbc.insets = new Insets(5, 3, 0, 3);
        gbl.setConstraints(buttonPane, gbc);
        contentPane.add(buttonPane);

        JButton button = new JButton(
            Bundle.getString("VMStartProps.okButton"));
        button.setActionCommand("ok");
        button.addActionListener(this);
        buttonPane.add(button);

        buttonPane.add(Box.createHorizontalStrut(5));

        button = new JButton(Bundle.getString("VMStartProps.cancelButton"));
        button.setActionCommand("cancel");
        button.addActionListener(this);
        buttonPane.add(button);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    } // PropertiesDialog

    /**
     * Invoked when a button has been pressed.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        AbstractButton src = (AbstractButton) e.getSource();
        if (src.getActionCommand().equals("ok")) {
            String props = builder.getProperties();
            optionsField.setText(props);
        }
        dispose();
    } // actionPerformed
} // PropertiesDialog

/**
 * The PropertiesBuilder is a component that allows the user to define a
 * set of Java properties for the debuggee.
 *
 * @author  Nathan Fiedler
 */
class PropertiesBuilder extends JPanel
    implements ActionListener, ListSelectionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The table that displays the elements. */
    private JTable elementTable;
    /** The table model that holds the elements. */
    private ElementTableModel elementModel;
    /** The button to add an element. */
    private JButton addElement;
    /** The button to remove an element. */
    private JButton removeElement;
    /** The button to move an element towards the front of the set. */
    private JButton moveUp;
    /** The button to move an element towards the end of the set. */
    private JButton moveDown;
    /** The elements of the set. */
    private Vector elements;
    /** String of the non-property JVM options. */
    private String nonproperties;

    /**
     * Constructs a default PropertiesBuilder.
     */
    public PropertiesBuilder() {
        super(new BorderLayout());

        elements = new Vector();
        elementModel = new ElementTableModel();

        addElement = new JButton(Bundle.getString("PropertiesBuilder.add"));
        addElement.addActionListener(this);

        removeElement = new JButton(
            Bundle.getString("PropertiesBuilder.remove"));
        removeElement.addActionListener(this);

        moveUp = new JButton(Bundle.getString("PropertiesBuilder.moveUp"));
        moveUp.addActionListener(this);

        moveDown = new JButton(Bundle.getString("PropertiesBuilder.moveDown"));
        moveDown.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addElement);
        buttonPanel.add(removeElement);
        buttonPanel.add(moveUp);
        buttonPanel.add(moveDown);
        add(buttonPanel, BorderLayout.SOUTH);

        elementTable = new JTable(elementModel);
        JScrollPane tableScroller = new JScrollPane(elementTable);
        tableScroller.setMinimumSize(new Dimension(100, 200));
        add(tableScroller, BorderLayout.CENTER);

        elementTable.getSelectionModel().addListSelectionListener(this);
    } // PropertiesBuilder

    /**
     * Invoked when one of the buttons has been pressed.
     *
     * @param  e  the action event.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(addElement)) {
            elementModel.add("", "");
            if (elements.size() == 1) {
                elementTable.setRowSelectionInterval(0, 0);
            }
        } else if (source.equals(removeElement)) {
            int row = elementTable.getSelectedRow();
            if (row >= 0) {
                elementModel.remove(row);
            }
        } else if (source.equals(moveUp)) {
            int row = elementTable.getSelectedRow();
            if (row >= 1) {
                elementModel.moveUp(row);
            }
        } else if (source.equals(moveDown)) {
            int row = elementTable.getSelectedRow();
            if (row < (elements.size() - 1)) {
                elementModel.moveDown(row);
            }
        }

        int tableSize = elements.size();
        if (tableSize < 1 && removeElement.isEnabled()) {
            removeElement.setEnabled(false);
        } else if (tableSize > 0 && !removeElement.isEnabled()) {
            removeElement.setEnabled(true);
        }

        // update the move up/down buttons
        valueChanged(null);
    } // actionPerformed

    /**
     * Returns the properties built using this PropertiesBuilder as a
     * single String.
     *
     * @return  the properties built using this PropertiesBuilder.
     */
    public String getProperties() {
        StringBuffer sb = new StringBuffer(nonproperties);
        // The non-properties will already have a space at the end.

        // Canonicalize the properties into a string.
        for (int ii = 0; ii < elements.size(); ii++) {
            sb.append("-D");
            sb.append(elementModel.getValueAt(ii, 0));
            sb.append('=');
            String val = (String) elementModel.getValueAt(ii, 1);
            if (val.length() > 0 && val.indexOf(' ') > -1
                && val.charAt(0) != '"') {
                val = '"' + val + '"';
            }
            sb.append(val);
            sb.append(' ');
        }

        // Remove the trailing space.
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(len - 1);
        }
        return sb.toString();
    } // getProperties

    /**
     * Set the properties to be displayed in the table. The options may
     * contain non-properties and in fact may not contain any properties
     * at all. Properties are denoted by the "-D" option, with a name
     * and value pair separated by an equals sign (=). Values containing
     * spaces must be enclosed in double-quotes.
     *
     * @param  options  all of the JVM options.
     */
    public void setProperties(String options) {
        int size = elements.size();
        elements.clear();
        if (size > 0) {
            elementModel.fireTableRowsDeleted(0, size - 1);
        }

        // Parse the options and add the properties to the table.

        StringBuffer nonprops = new StringBuffer();

        int strlen = options.length();
        int start = 0;
        int index = 0;
        byte state = 0;
        char ch = '\0';
        while (index < strlen) {
            char prevch = ch;
            ch = options.charAt(index);
            switch (state) {
            case 0:
                // Not inside a quoted string.
                if (ch == '"') {
                    state = 1;
                } else if (ch == '\'') {
                    state = 2;
                } else if (ch == '\\') {
                    state = 3;
                } else if (ch == ' ') {
                    // Grab the option and see if it is a property.
                    String opt = options.substring(start, index);
                    if (opt.startsWith("-D")) {
                        opt = opt.substring(2);
                        int eq = opt.indexOf('=');
                        String name = opt.substring(0, eq);
                        String value = opt.substring(eq + 1);
                        elementModel.add(name, value);
                    } else {
                        nonprops.append(opt);
                        nonprops.append(' ');
                    }
                    start = index + 1;
                }
                break;

            case 1:
                // Inside a double-quoted string.
                if (ch == '"') {
                    state = 0;
                } else if (ch == '\\') {
                    state = 4;
                }
                break;

            case 2:
                // Inside a single-quoted string.
                if (ch == '\'') {
                    state = 0;
                } else if (ch == '\\') {
                    state = 5;
                }
                break;

            case 3:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 0;
                break;

            case 4:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 1;
                break;

            case 5:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 2;
                break;
            default:
                throw new IllegalStateException("finite machine confused");
            }
            index++;
        }

        // Either we have processed nothing or processed all but the
        // last separated option. Both cases are happily handled in
        // exactly the same manner.
        String opt = options.substring(start);
        if (opt.startsWith("-D")) {
            opt = opt.substring(2);
            int eq = opt.indexOf('=');
            String name = opt.substring(0, eq);
            String value = opt.substring(eq + 1);
            elementModel.add(name, value);
        } else {
            nonprops.append(opt);
            nonprops.append(' ');
        }

        nonproperties = nonprops.toString();
    } // setProperties

    /**
     * Handle list selection events.
     *
     * @param  e  the list selection event.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e == null || !e.getValueIsAdjusting()) {
            int row = elementTable.getSelectedRow();
            int tableSize = elements.size();

            if (tableSize < 2) {
                moveUp.setEnabled(false);
                moveDown.setEnabled(false);
                return;
            }

            if (row < 1) {
                moveUp.setEnabled(false);
                if (tableSize > 1 && !moveDown.isEnabled()) {
                    moveDown.setEnabled(true);
                }
            } else if (row == (tableSize - 1)) {
                moveDown.setEnabled(false);
                if (!moveUp.isEnabled()) {
                    moveUp.setEnabled(true);
                }
            } else {
                moveUp.setEnabled(true);
                moveDown.setEnabled(true);
            }
        }
    } // valueChanged

    /**
     * A simple table model of the elementTable.
     */
    class ElementTableModel extends AbstractTableModel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Name column offset. */
        static final int NAME_COLUMN = 0;
        /** Value column offset. */
        static final int VALUE_COLUMN = 1;

        /**
         * Add an element to the table model.
         *
         * @param  name   property name.
         * @param  value  property value.
         */
        protected void add(String name, String value) {
            int rows = elements.size();
            NameValuePair nvp = new NameValuePair(name, value);
            elements.addElement(nvp);
            fireTableRowsInserted(rows, rows);
        } // add

        /**
         * Returns the column count.
         *
         * @return  column count.
         */
        public int getColumnCount() {
            return 2;
        } // getColumnCount

        /**
         * Returns the name of the nth column.
         *
         * @param  column  index of desired column.
         * @return  name of the column.
         */
        public String getColumnName(int column) {
            if (column == NAME_COLUMN) {
                return Bundle.getString("PropertiesBuilder.nameColumn");
            } else if (column == VALUE_COLUMN) {
                return Bundle.getString("PropertiesBuilder.valueColumn");
            } else {
                return "ERROR";
            }
        } // getColumnName

        /**
         * Returns the row count.
         *
         * @return  row count.
         */
        public int getRowCount() {
            return elements.size();
        } // getRowCount

        /**
         * Retrieves the value at the specified cell.
         *
         * @param  row     row of cell.
         * @param  column  column of cell.
         * @return  value in cell.
         */
        public Object getValueAt(int row, int column) {
            NameValuePair nvp = (NameValuePair) elements.elementAt(row);
            if (column == NAME_COLUMN) {
                return nvp.getName();
            } else if (column == VALUE_COLUMN) {
                return nvp.getValue();
            } else {
                return null;
            }
        } // getValueAt

        /**
         * Returns true if the cell at <code>rowIndex</code> and
         * <code>columnIndex</code> is editable.
         *
         * @param  rowIndex     the row whose value to be queried
         * @param  columnIndex  the column whose value to be queried
         * @return  true if the cell is editable
         */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // All of our columns are always editable.
            return true;
        } // isCellEditable

        /**
         * Move an element up (towards the front of) the set.
         *
         * @param  row  the element to be moved.
         */
        protected void moveUp(int row) {
            Object a = elements.elementAt(row);
            Object b = elements.elementAt(row - 1);
            elements.setElementAt(a, row - 1);
            elements.setElementAt(b, row);
            fireTableRowsUpdated(row - 1, row);
            elementTable.setRowSelectionInterval(row - 1, row - 1);
        } // moveUp

        /**
         * Move an element down (towards the end of) the set.
         *
         * @param  row  the element to be moved.
         */
        protected void moveDown(int row) {
            Object a = elements.elementAt(row);
            Object b = elements.elementAt(row + 1);
            elements.setElementAt(a, row + 1);
            elements.setElementAt(b, row);
            fireTableRowsUpdated(row, row + 1);
            elementTable.setRowSelectionInterval(row + 1, row + 1);
        } // moveDown

        /**
         * Remove an element from the table model.
         *
         * @param  row  the index of the element to remove.
         */
        protected void remove(int row) {
            elements.removeElementAt(row);
            fireTableRowsDeleted(row, row);
            if (elements.size() > 0) {
                if (elements.size() > row) {
                    elementTable.setRowSelectionInterval(row, row);
                } else {
                    row = elements.size() - 1;
                    elementTable.setRowSelectionInterval(row, row);
                }
            }
        } // remove

        /**
         * Sets a value for the record in the cell at 'col' and 'row'.
         * The 'val' is the new value. This notifies model listeners
         * that the cell data has changed.
         *
         * @param  val  the new value
         * @param  row  the row whose value is to be changed
         * @param  col  the column whose value is to be changed
         */
        public void setValueAt(Object val, int row, int col) {
            if (row < 0 || row >= elements.size()) {
                throw new ArrayIndexOutOfBoundsException(row);
            }
            NameValuePair nvp = (NameValuePair) elements.elementAt(row);
            if (col == NAME_COLUMN) {
                nvp.setName((String) val);
            } else if (col == VALUE_COLUMN) {
                nvp.setValue((String) val);
            }
            fireTableCellUpdated(row, col);
        } // setValueAt

        /**
         * A name/value pair object.
         */
        protected class NameValuePair {
            /** name */
            private String name;
            /** value */
            private String value;

            /**
             * Constructs a NameValuePair instance.
             *
             * @param  name   name.
             * @param  value  value.
             */
            public NameValuePair(String name, String value) {
                this.name = name;
                this.value = value;
            } // NameValuePair

            /**
             * Gets the name element.
             *
             * @return  name.
             */
            public String getName() {
                return name;
            } // getName

            /**
             * Gets the value element.
             *
             * @return  value.
             */
            public String getValue() {
                return value;
            } // getValue

            /**
             * Sets the name element to a new value.
             *
             * @param  name  new name value.
             */
            public void setName(String name) {
                this.name = name;
            } // setName

            /**
             * Sets the value element to a new value.
             *
             * @param  value  new value value.
             */
            public void setValue(String value) {
                this.value = value;
            } // setValue
        } // NameValuePair
    } // ElementTableModel
} // PropertiesBuilder
