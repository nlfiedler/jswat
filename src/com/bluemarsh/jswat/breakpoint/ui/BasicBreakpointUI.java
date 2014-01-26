/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: BasicBreakpointUI.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.breakpoint.ResolvableBreakpoint;
import com.sun.jdi.request.EventRequest;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Class BasicBreakpointUI is an adapter for building the user interface
 * components to represent a the common properties of all breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class BasicBreakpointUI implements BreakpointUI {
    /** Breakpoint that we are working against. */
    protected Breakpoint targetBreakpoint;
    /** Panel that contains all of our properties, in a gridbag layout.
     * Subclasses can add their own components to this panel as needed. */
    protected JPanel propertiesPanel;
    /** Layout for the properties panel. */
    protected GridBagLayout gbl;
    /** Contraints object for the panel layout. */
    protected GridBagConstraints gbc;
    /** Original enabled value. */
    protected boolean originalEnabled;
    /** Checkbox for the breakpoint's enabledness. */
    protected JCheckBox enabledCheckbox;
    /** Original skip count value. */
    protected int originalSkipcount;
    /** Breakpoint's skip count text field. */
    protected JTextField skipcountTextfield;
    /** Original expire count value. */
    protected int originalExpirecount;
    /** Breakpoint's expire count text field. */
    protected JTextField expirecountTextfield;
    /** Original group the breakpoint belonged to. */
    protected BreakpointGroup originalGroup;
    /** Combo box showing the available breakpoint groups. */
    protected JComboBox groupCombo;
    /** Original suspend policy value. */
    protected int originalSuspendPolicy;
    /** Radio button group for the suspend policy. */
    protected ButtonGroup suspendGroup;
    /** Original list of class filters. */
    protected String originalClassFilters;
    /** Breakpoint's class filters text field. */
    protected JTextField classFiltersTextfield;
    /** Original list of thread filters. */
    protected String originalThreadFilters;
    /** Breakpoint's thread filters text field. */
    protected JTextField threadFiltersTextfield;

    /**
     * Create a BasicBreakpointUI that will operate on the given breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public BasicBreakpointUI(Breakpoint bp) {
        targetBreakpoint = bp;
        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 1.0;

        propertiesPanel = new JPanel(gbl);

        // Allow enabling and disabling of the breakpoint.
        enabledCheckbox = new JCheckBox(Bundle.getString("enabledLabel"));
        originalEnabled = bp.isEnabled();
        enabledCheckbox.setSelected(originalEnabled);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(enabledCheckbox, gbc);
        propertiesPanel.add(enabledCheckbox);

        // Allow changing the thread suspend policy.
        originalSuspendPolicy = bp.getSuspendPolicy();
        Box suspendBox = new Box(BoxLayout.X_AXIS);
        JLabel label = new JLabel(Bundle.getString("suspendPolicyLabel"));
        suspendBox.add(label);
        suspendBox.add(Box.createHorizontalStrut(10));
        gbl.setConstraints(suspendBox, gbc);
        propertiesPanel.add(suspendBox);

        // Suspend none of the threads.
        suspendGroup = new ButtonGroup();
        JRadioButton radioButton = new JRadioButton(
            Bundle.getString("suspendNoneLabel"));
        suspendGroup.add(radioButton);
        radioButton.setActionCommand("none");
        if (originalSuspendPolicy == EventRequest.SUSPEND_NONE) {
            radioButton.setSelected(true);
        }
        suspendBox.add(radioButton);

        // Suspend the event thread.
        radioButton = new JRadioButton(Bundle.getString("suspendEventLabel"));
        suspendGroup.add(radioButton);
        radioButton.setActionCommand("event");
        if (originalSuspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
            radioButton.setSelected(true);
        }
        suspendBox.add(radioButton);

        // Suspend all threads.
        radioButton = new JRadioButton(Bundle.getString("suspendAllLabel"));
        suspendGroup.add(radioButton);
        radioButton.setActionCommand("all");
        if (originalSuspendPolicy == EventRequest.SUSPEND_ALL) {
            radioButton.setSelected(true);
        }
        suspendBox.add(radioButton);

        if (bp instanceof ResolvableBreakpoint) {
            // If the breakpoint is resolvable, it has a class name.
            ResolvableBreakpoint rbp = (ResolvableBreakpoint) bp;
            label = new JLabel(Bundle.getString("classidLabel"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            propertiesPanel.add(label);

            JTextField tf = new JTextField(
                rbp.getReferenceTypeSpec().getIdentifier(), 20);
            tf.setEnabled(false);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbl.setConstraints(tf, gbc);
            propertiesPanel.add(tf);
        }

        // Allow changing the skip count.
        label = new JLabel(Bundle.getString("skipcountLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        skipcountTextfield = new JTextField(5);
        skipcountTextfield.setDocument(new NumericDocument());
        originalSkipcount = bp.getSkipCount();
        skipcountTextfield.setText(String.valueOf(originalSkipcount));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(skipcountTextfield, gbc);
        propertiesPanel.add(skipcountTextfield);

        // Allow changing the expire count.
        label = new JLabel(Bundle.getString("expirecountLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        expirecountTextfield = new JTextField(5);
        expirecountTextfield.setDocument(new NumericDocument());
        originalExpirecount = bp.getExpireCount();
        expirecountTextfield.setText(String.valueOf(originalExpirecount));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(expirecountTextfield, gbc);
        propertiesPanel.add(expirecountTextfield);

        // Look for the default breakpoint group by traversing upwards.
        BreakpointGroup group = bp.getBreakpointGroup();
        while (group.getParent() != null) {
            group = group.getParent();
        }
        originalGroup = bp.getBreakpointGroup();
        // Build the breakpoint group combo box.
        label = new JLabel(Bundle.getString("parentGroupLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        groupCombo = buildGroupList(group);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(groupCombo, gbc);
        propertiesPanel.add(groupCombo);
        findAndSelectGroup(groupCombo, bp);
    } // BasicBreakpointUI

    /**
     * Add the text input field for setting the class filters.
     */
    public void addClassFilter() {
        // Create the text input field to change the class filters.
        JLabel label = new JLabel(Bundle.getString("classFiltersLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        classFiltersTextfield = new JTextField(20);
        originalClassFilters = targetBreakpoint.getClassFilters();
        classFiltersTextfield.setText(originalClassFilters);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(classFiltersTextfield, gbc);
        propertiesPanel.add(classFiltersTextfield);
    } // addClassFilter

    /**
     * Add the text input field for setting the thread filters.
     */
    public void addThreadFilter() {
        // Create the text input field to change the thread filters.
        JLabel label = new JLabel(Bundle.getString("threadFiltersLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        threadFiltersTextfield = new JTextField(20);
        originalThreadFilters = targetBreakpoint.getThreadFilters();
        threadFiltersTextfield.setText(originalThreadFilters);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(threadFiltersTextfield, gbc);
        propertiesPanel.add(threadFiltersTextfield);
    } // addThreadFilter

    /**
     * Builds a combo box to display all the available breakpoint groups.
     * Each group is represented using its name plus the names of all of
     * its parents, separated by periods.
     *<p>
     * To retrieve the selected breakpoint group from the combo box,
     * pass the combo box to the <code>getSelectedGroup()</code> method.
     *
     * @param  defaultGroup  the "default" breakpoint group, usually
     *                       retrieved from the breakpoint manager.
     * @return  combo box widget that uses special objects to represent
     *          the elements of the list.
     */
    public static JComboBox buildGroupList(BreakpointGroup defaultGroup) {
        // List of breakpoint names.
        Vector names = new Vector();

        // Stack holds breakpoint group/composite name pairs.
        Stack stack = new Stack();
        stack.push(defaultGroup);
        stack.push(null);
        while (!stack.empty()) {
            // Get next composite name and breakpoint group.
            String name = (String) stack.pop();
            BreakpointGroup group = (BreakpointGroup) stack.pop();
            if (name != null) {
                name = name + "." + group.getName();
            } else {
                // This is the first time around, with default group.
                name = group.getName();
            }
            // Add this group's name to the list.
            names.add(new GroupNamePair(group, name));

            // Visit this group's subgroups.
            Iterator iter = group.groups();
            while (iter.hasNext()) {
                BreakpointGroup subgroup = (BreakpointGroup) iter.next();
                stack.push(subgroup);
                stack.push(name);
            }
        }

        return new JComboBox(names);
    } // buildGroupList

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    public void commit() {
        // Save the ui component values to the breakpoint.
        targetBreakpoint.setEnabled(false);

        ButtonModel bm = suspendGroup.getSelection();
        String cmd = bm.getActionCommand();
        int policy = -1;
        if (cmd.equals("none")) {
            policy = EventRequest.SUSPEND_NONE;
        } else if (cmd.equals("event")) {
            policy = EventRequest.SUSPEND_EVENT_THREAD;
        } else if (cmd.equals("all")) {
            policy = EventRequest.SUSPEND_ALL;
        }
        targetBreakpoint.setSuspendPolicy(policy);

        String str;
        if (classFiltersTextfield != null) {
            str = classFiltersTextfield.getText();
            targetBreakpoint.setClassFilters(str);
        }
        if (threadFiltersTextfield != null) {
            str = threadFiltersTextfield.getText();
            targetBreakpoint.setThreadFilters(str);
        }

        str = skipcountTextfield.getText();
        int n = Integer.parseInt(str);
        targetBreakpoint.setSkipCount(n);

        str = expirecountTextfield.getText();
        n = Integer.parseInt(str);
        targetBreakpoint.setExpireCount(n);

        // Set the breakpoint's parent group.
        BreakpointGroup newParent = getSelectedGroup(groupCombo);
        BreakpointGroup oldParent = targetBreakpoint.getBreakpointGroup();
        if (newParent != oldParent) {
            oldParent.removeBreakpoint(targetBreakpoint);
            newParent.addBreakpoint(targetBreakpoint);
        }

        targetBreakpoint.setEnabled(enabledCheckbox.isSelected());
    } // commit

    /**
     * Using the given combo box, previously built using
     * <code>buildGroupList()</code>, set one of the breakpoint groups
     * in the combo box as selected according to the parent group of
     * the given breakpoint.
     *
     * @param  box  combo box in which to select a group.
     * @param  bp   breakpoint whose parent group should be selected.
     */
    public static void findAndSelectGroup(JComboBox box, Breakpoint bp) {
        int count = box.getItemCount();
        BreakpointGroup parent = bp.getBreakpointGroup();
        for (int i = 0; i < count; i++) {
            GroupNamePair pair = (GroupNamePair) box.getItemAt(i);
            if (pair.breakpointGroup == parent) {
                box.setSelectedIndex(i);
                break;
            }
        }
    } // findAndSelectGroup

    /**
     * Retrieves the selected BreakpointGroup from the given combo box.
     * The combo box must have been built using the
     * <code>buildGroupList()</code> method.
     *
     * @param  box  combo box that lists breakpoint groups.
     * @return  selected breakpoint group, or null if none.
     */
    public static BreakpointGroup getSelectedGroup(JComboBox box) {
        try {
            GroupNamePair grpname = (GroupNamePair) box.getSelectedItem();
            if (grpname != null) {
                return grpname.breakpointGroup;
            } else {
                return null;
            }
        } catch (ClassCastException cce) {
            return null;
        }
    } // getSelectedGroup

    /**
     * Return a reference to the user interface element that this
     * adapter uses to graphically represent the breakpoint, condition,
     * or monitor. This may be a container that has several user
     * interface elements inside it.
     *
     * @return  user interface ocmponent.
     */
    public Component getUI() {
        return propertiesPanel;
    } // getUI

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     */
    public void undo() {
        targetBreakpoint.setEnabled(false);

        targetBreakpoint.setSkipCount(originalSkipcount);
        targetBreakpoint.setExpireCount(originalExpirecount);
        targetBreakpoint.setSuspendPolicy(originalSuspendPolicy);
        targetBreakpoint.setClassFilters(originalClassFilters);
        targetBreakpoint.setThreadFilters(originalThreadFilters);

        // Set the breakpoint's parent group.
        BreakpointGroup newParent = targetBreakpoint.getBreakpointGroup();
        if (newParent != originalGroup) {
            newParent.removeBreakpoint(targetBreakpoint);
            originalGroup.addBreakpoint(targetBreakpoint);
        }
        targetBreakpoint.setEnabled(originalEnabled);
    } // undo

    /**
     * Implements a text document that only accepts digits.
     *
     * @author  Nathan Fiedler
     */
    protected class NumericDocument extends PlainDocument {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Insert a string into the document.
         *
         * @param  offs  offset in which to insert.
         * @param  str   string to insert.
         * @param  a     attribute set.
         */
        public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

            if (str == null) {
                return;
            }
            char[] num = str.toCharArray();
            int j = 0;
            for (int i = 0; i < num.length; i++) {
                if (Character.isDigit(num[i])) {
                    // copy the digit to new location
                    num[j] = num[i];
                    // character is okay, advance count
                    j++;
                }
            }
            super.insertString(offs, new String(num, 0, j), a);
        } // insertString
    } // NumericDocument

    /**
     * Class GroupNamePair represents a breakpoint group/composite name pair.
     * This is used in the combo box that displays the breakpoint groups.
     *
     * @author  Nathan Fiedler
     */
    protected static class GroupNamePair {
        /** Breakpoint group. */
        public BreakpointGroup breakpointGroup;
        /** Group's composite name. */
        public String compositeName;

        /**
         * Constructs a GroupNamePair object.
         *
         * @param  group  breakpoint group.
         * @param  name   composite name.
         */
        public GroupNamePair(BreakpointGroup group, String name) {
            breakpointGroup = group;
            compositeName = name;
        } // GroupNamePair

        /**
         * Returns the string representation of this object.
         *
         * @return  string of this object.
         */
        public String toString() {
            return compositeName;
        } // toString
    } // GroupNamePair
} // BasicBreakpointUI
