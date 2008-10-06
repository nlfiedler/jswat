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
 * $Id: EditorDialog.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.CommandMonitor;
import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.ExprCondition;
import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.breakpoint.ResolvableBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Class EditorDialog is responsible for allowing the user to edit the
 * properties of a particular breakpoint. This includes presenting the
 * user interface of the breakpoint as well as the interface for the
 * conditions and monitors of the breakpoint. This dialog allows the
 * user to add conditions and monitors to a breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class EditorDialog extends JDialog implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Breakpoint that we are editing. */
    private Breakpoint targetBreakpoint;
    /** UI adapter for the breakpoint we are editing. */
    private BreakpointUI uiAdapter;
    /** List showing the conditions, if any. */
    private JList conditionList;
    /** List showing the monitors, if any. */
    private JList monitorList;
    /** Button to add a condition. */
    private JButton addCondButton;
    /** Button to edit a condition. */
    private JButton editCondButton;
    /** Button to delete a condition. */
    private JButton deleteCondButton;
    /** Button to add a monitor. */
    private JButton addMonButton;
    /** Button to edit a monitor. */
    private JButton editMonButton;
    /** Button to delete a monitor. */
    private JButton deleteMonButton;
    /** Button to save breakpoint changes. */
    private JButton okayButton;
    /** Button to abandon breakpoint changes. */
    private JButton cancelButton;

    /**
     * Constructs the breakpoint managing dialog.
     *
     * @param  owner  parent component.
     * @param  bp     breakpoint to edit.
     */
    public EditorDialog(Frame owner, Breakpoint bp) {
        // Really must have a dialog owner or weird things happen.
        super(owner, Bundle.getString("EditorDialog.title"));
        Container pane = getContentPane();
        targetBreakpoint = bp;

        GridBagLayout gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Get the breakpoint's ui adapter and stuff it into the dialog.
        uiAdapter = bp.getUIAdapter();
        Component uicomp = uiAdapter.getUI();
        gbl.setConstraints(uicomp, gbc);
        pane.add(uicomp);

        JLabel label;
        JScrollPane scroller;

        label = new JLabel(Bundle.getString("conditions"));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbl.setConstraints(label, gbc);
        pane.add(label);

        conditionList = new JList(buildConditionList());
        conditionList.setVisibleRowCount(3);
        scroller = new JScrollPane(conditionList);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        // Add Condition button to create a new condition.
        addCondButton = new JButton(Bundle.getString("addCondLabel"));
        addCondButton.addActionListener(this);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.ipady = 0;
        gbc.weightx = 1.0;
        gbl.setConstraints(addCondButton, gbc);
        pane.add(addCondButton);

        // Edit Condition button to edit an existing condition.
        editCondButton = new JButton(Bundle.getString("editCondLabel"));
        editCondButton.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(editCondButton, gbc);
        pane.add(editCondButton);

        // Remove Condition button to remove selected condition.
        deleteCondButton = new JButton(Bundle.getString("delCondLabel"));
        deleteCondButton.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbl.setConstraints(deleteCondButton, gbc);
        pane.add(deleteCondButton);

        label = new JLabel(Bundle.getString("monitors"));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        gbl.setConstraints(label, gbc);
        pane.add(label);

        monitorList = new JList(buildMonitorList());
        monitorList.setVisibleRowCount(3);
        scroller = new JScrollPane(monitorList);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        // Add Monitor button to create a new monitor.
        addMonButton = new JButton(Bundle.getString("addMonLabel"));
        addMonButton.addActionListener(this);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.ipady = 0;
        gbc.weightx = 1.0;
        gbl.setConstraints(addMonButton, gbc);
        pane.add(addMonButton);

        // Edit Monitor button to edit an existing monitor.
        editMonButton = new JButton(Bundle.getString("editMonLabel"));
        editMonButton.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(editMonButton, gbc);
        pane.add(editMonButton);

        // Remove Monitor button to remove selected monitor.
        deleteMonButton = new JButton(Bundle.getString("delMonLabel"));
        deleteMonButton.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbl.setConstraints(deleteMonButton, gbc);
        pane.add(deleteMonButton);

        // Ok button to save the changes and close the dialog.
        okayButton = new JButton(Bundle.getString("okayLabel"));
        okayButton.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbl.setConstraints(okayButton, gbc);
        pane.add(okayButton);

        // Cancel button to close without saving.
        cancelButton = new JButton(Bundle.getString("cancelLabel"));
        cancelButton.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(cancelButton, gbc);
        pane.add(cancelButton);

        // Set up the dialog's closing procedure.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Size the dialog.
        pack();
        setResizable(false);
        // Let the caller position and display us.
    } // EditorDialog

    /**
     * User invoked one of the many buttons.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == addCondButton) {
            // Adding a new condition.
            String expr = editCondition(this, null);
            if (expr != null && expr.length() > 0) {
                // Construct the new ExprCondition.
                ExprCondition ec = new ExprCondition(expr);
                DefaultListModel model = (DefaultListModel)
                    conditionList.getModel();
                model.addElement(ec.getUIAdapter());
            }

        } else if (src == editCondButton) {
            // Edit the selected condition.
            ConditionUI cui = (ConditionUI) conditionList.getSelectedValue();
            if (cui != null) {
                ExprCondition cond = (ExprCondition) cui.getCondition();
                String expr = editCondition(this, cond.getExprString());
                if (expr != null) {
                    // Save changes to condition.
                    cond.setExprString(expr);
                    DefaultListModel model = (DefaultListModel)
                        conditionList.getModel();
                    // Force an update to the list.
                    model.set(model.indexOf(cui), cui);
                }
            }

        } else if (src == deleteCondButton) {
            // Delete the selected condition.
            Object o = conditionList.getSelectedValue();
            if (o != null) {
                DefaultListModel model = (DefaultListModel)
                    conditionList.getModel();
                model.removeElement(o);
            }

        } else if (src == addMonButton) {
            // Add a new monitor.
            String command = editMonitor(this, null);
            if (command != null && command.length() > 0) {
                Monitor mon = new CommandMonitor(command);
                DefaultListModel model = (DefaultListModel)
                    monitorList.getModel();
                model.addElement(mon.getUIAdapter());
            }

        } else if (src == editMonButton) {
            // Edit the selected monitor.
            MonitorUI mui = (MonitorUI) monitorList.getSelectedValue();
            if (mui != null) {
                CommandMonitor mon = (CommandMonitor) mui.getMonitor();
                String command = editMonitor(this, mon.getCommand());
                if (command != null) {
                    // Save changes to monitor.
                    mon.setCommand(command);
                    DefaultListModel model = (DefaultListModel)
                        monitorList.getModel();
                    // Force an update to the list.
                    model.set(model.indexOf(mui), mui);
                }
            }

        } else if (src == deleteMonButton) {
            // Delete the selected monitor.
            Object o = monitorList.getSelectedValue();
            if (o != null) {
                DefaultListModel model = (DefaultListModel)
                    monitorList.getModel();
                model.removeElement(o);
            }

        } else if (src == okayButton) {
            // Save the breakpoint changes.
            uiAdapter.commit();
            try {
                resolveBreakpoint();
            } catch (ResolveException re) {
                // Argh, report and roll back.
                resolveError(re);
                undoChanges();
                // don't dispose of this dialog
                return;
            }
            // Save the conditions and monitors.
            commitConditionsAndMonitors();
            // Signal that the breakpoint has changed.
            Session session =
                targetBreakpoint.getBreakpointGroup().getSession();
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            brkman.fireChange(targetBreakpoint,
                              BreakpointEvent.TYPE_MODIFIED);
            dispose();

        } else if (src == cancelButton) {
            // Abandon the breakpoint changes.
            dispose();
        }
    } // actionPerformed

    /**
     * Put the target breakpoint's conditions into a new list model.
     *
     * @return  new list model.
     */
    protected DefaultListModel buildConditionList() {
        DefaultListModel model = new DefaultListModel();
        Iterator iter = targetBreakpoint.conditions();
        while (iter.hasNext()) {
            Condition cond = (Condition) iter.next();
            ConditionUI condui = cond.getUIAdapter();
            model.addElement(condui);
        }
        return model;
    } // buildConditionList

    /**
     * Put the target breakpoint's monitors into a new list model.
     *
     * @return  new list model.
     */
    protected DefaultListModel buildMonitorList() {
        DefaultListModel model = new DefaultListModel();
        Iterator iter = targetBreakpoint.monitors();
        while (iter.hasNext()) {
            Monitor mon = (Monitor) iter.next();
            MonitorUI monui = mon.getUIAdapter();
            model.addElement(monui);
        }
        return model;
    } // buildMonitorList

    /**
     * Save the current conditions and monitors to the breakpoint.
     */
    protected void commitConditionsAndMonitors() {
        // First, remove all the existing conditions.
        ListIterator iter = targetBreakpoint.conditions();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        // Next, add the conditions that we know about.
        DefaultListModel model = (DefaultListModel) conditionList.getModel();
        Enumeration conditions = model.elements();
        while (conditions.hasMoreElements()) {
            ConditionUI cui = (ConditionUI) conditions.nextElement();
            targetBreakpoint.addCondition(cui.getCondition());
        }

        // First, remove all the existing monitors.
        iter = targetBreakpoint.monitors();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        // Next, add the monitors that we know about.
        model = (DefaultListModel) monitorList.getModel();
        Enumeration monitors = model.elements();
        while (monitors.hasMoreElements()) {
            MonitorUI mui = (MonitorUI) monitors.nextElement();
            targetBreakpoint.addMonitor(mui.getMonitor());
        }
    } // commitConditionsAndMonitors

    /**
     * Inform the user of an error.
     *
     * @param  msg  error message.
     */
    protected void displayError(String msg) {
        Session session = targetBreakpoint.getBreakpointGroup().getSession();
        UIAdapter adapter = session.getUIAdapter();
        adapter.showMessage(UIAdapter.MESSAGE_ERROR, msg);
    } // displayError

    /**
     * Edit a condition expression by presenting the user with a dialog
     * for editing the expression.
     *
     * @param  parent      parent Window for the input dialog.
     * @param  expression  condition expression to be edited.
     * @return  new condition expression, or null if user cancelled.
     */
    protected String editCondition(Window parent, String expression) {
        // Ask the user for variable name and value to test for.
        Object[] messages = {
            Bundle.getString("EditCondition.expr"),
            new JTextField(expression == null ? "" : expression, 30)
        };

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog to get user input.
            int response = JOptionPane.showOptionDialog(
                parent, messages, Bundle.getString("EditCondition.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                // user cancelled
                return null;
            }

            expression = ((JTextField) messages[1]).getText();
            if (expression == null || expression.length() == 0) {
                displayError(Bundle.getString("EditCondition.missingExpr"));
            } else {
                responseOkay = true;
            }
        }
        return expression;
    } // editCondition

    /**
     * Edits a command monitor command string.
     *
     * @param  parent   parent Window for the input dialog.
     * @param  command  command to be edited.
     * @return  new command, or null if user cancelled.
     */
    protected String editMonitor(Window parent, String command) {
        // Ask the user for command to run.
        Object[] messages = {
            Bundle.getString("EditMonitor.command"),
            new JTextField(command == null ? "" : command, 30)
        };

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog to get user input.
            int response = JOptionPane.showOptionDialog(
                parent, messages,
                Bundle.getString("EditMonitor.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                // user cancelled
                return null;
            }

            command = ((JTextField) messages[1]).getText();
            if (command == null || command.length() == 0) {
                displayError(Bundle.getString("EditMonitor.missingCommand"));
            } else {
                responseOkay = true;
            }
        }
        return command;
    } // editMonitor

    /**
     * Try to resolve the breakpoint if it is a resolvable breakpoint
     * and it is currently unresolved.
     *
     * @throws  ResolveException
     *          if there was a problem resolving.
     */
    protected void resolveBreakpoint() throws ResolveException {
        if ((targetBreakpoint instanceof ResolvableBreakpoint)
            && (!targetBreakpoint.isResolved())) {

            // Try to resolve the breakpoint.
            Session session =
                targetBreakpoint.getBreakpointGroup().getSession();
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            brkman.resolveBreakpoint((ResolvableBreakpoint) targetBreakpoint);
        }
    } // resolveBreakpoint

    /**
     * An error occurred trying to resolve a breakpoint. Need to inform
     * the user of the problem via a dialog.
     *
     * @param  re  resolve exception.
     */
    protected void resolveError(ResolveException re) {
        Session session = targetBreakpoint.getBreakpointGroup().getSession();
        UIAdapter adapter = session.getUIAdapter();
        adapter.showMessage(UIAdapter.MESSAGE_ERROR, re.errorMessage());
    } // resolveError

    /**
     * Undo whatever changes the user made to the breakpoint, conditions,
     * or monitors. This method will attempt to resolve the breakpoint
     * again. If this fails, it will silently discard the exception.
     */
    protected void undoChanges() {
        try {
            uiAdapter.undo();
        } catch (UnsupportedOperationException uoe) {
            // Argh, foiled again!
        }
        try {
            resolveBreakpoint();
        } catch (ResolveException re) {
            // Oy, maybe it was never resolved in the first place.
        }
    } // undoChanges
} // EditorDialog
