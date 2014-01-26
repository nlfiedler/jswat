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
 * $Id: EditorDialog.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.CommandMonitor;
import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.breakpoint.ResolvableBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.ValueCondition;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
 * Class EditorDialog is responsible for allowing the user to edit
 * the properties of a particular breakpoint. This includes presenting
 * the user interface of the breakpoint as well as the interface for
 * the conditions and monitors of the breakpoint. This dialog allows
 * the user to add conditions and monitors to a breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class EditorDialog extends JDialog {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Breakpoint that we are editing. */
    protected Breakpoint targetBreakpoint;
    /** UI adapter for the breakpoint we are editing. */
    protected BreakpointUI uiAdapter;
    /** List showing the conditions, if any. */
    protected JList conditionList;
    /** List showing the monitors, if any. */
    protected JList monitorList;

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

        JButton button;
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
        button = new JButton(Bundle.getString("addCondLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ValueCondition vc = createCondition(EditorDialog.this);
                    if (vc != null) {
                        DefaultListModel model = (DefaultListModel)
                            conditionList.getModel();
                        model.addElement(vc.getUIAdapter());
                    }
                }
            });
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipady = 0;
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        // Remove Condition button to remove selected condition.
        button = new JButton(Bundle.getString("delCondLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object o = conditionList.getSelectedValue();
                    if (o != null) {
                        DefaultListModel model = (DefaultListModel)
                            conditionList.getModel();
                        model.removeElement(o);
                    }
                }
            });
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        // xxx - need edit condition

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
        button = new JButton(Bundle.getString("addMonLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    CommandMonitor cm = createMonitor(EditorDialog.this);
                    if (cm != null) {
                        DefaultListModel model = (DefaultListModel)
                            monitorList.getModel();
                        model.addElement(cm.getUIAdapter());
                    }
                }
            });
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.ipady = 0;
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        // Remove Monitor button to remove selected monitor.
        button = new JButton(Bundle.getString("delMonLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object o = monitorList.getSelectedValue();
                    if (o != null) {
                        DefaultListModel model = (DefaultListModel)
                            monitorList.getModel();
                        model.removeElement(o);
                    }
                }
            });
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        // xxx - need edit monitor

        // Ok button to save the changes and close the dialog.
        button = new JButton(Bundle.getString("okayLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                }
            });
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        // Cancel button to close without saving.
        button = new JButton(Bundle.getString("cancelLabel"));
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Nothing to do, abandon the user input.
                    dispose();
                }
            });
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        // Set up the dialog's closing procedure.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // Nothing to do, abandon the user input.
                    dispose();
                }
            });

        // Size the dialog.
        pack();
        // Let the caller position and display us.
    } // EditorDialog

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
        Enumeration enmr = model.elements();
        while (enmr.hasMoreElements()) {
            ConditionUI cui = (ConditionUI) enmr.nextElement();
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
        enmr = model.elements();
        while (enmr.hasMoreElements()) {
            MonitorUI mui = (MonitorUI) enmr.nextElement();
            targetBreakpoint.addMonitor(mui.getMonitor());
        }
    } // commitConditionsAndMonitors

    /**
     * Creates a new value condition after presenting a dialog
     * to the user to get their input.
     *
     * @param  parent  parent Window for the input dialog.
     * @return  new value condition, or null if user cancelled.
     */
    protected ValueCondition createCondition(Window parent) {
        // Ask the user for variable name and value to test for.
        Object messages[] = {
            Bundle.getString("AddCondition.varname"),
            new JTextField(30),
            Bundle.getString("AddCondition.value"),
            new JTextField(30)
        };

        String varname = null;
        String varvalue = null;

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog to get user input.
            int response = JOptionPane.showOptionDialog(
                parent, messages,
                Bundle.getString("AddCondition.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                // user cancelled
                return null;
            }

            varname = ((JTextField) messages[1]).getText();
            varvalue = ((JTextField) messages[3]).getText();
            if ((varname == null) || (varname.length() == 0)) {
                displayError(
                    Bundle.getString("AddCondition.missingVarName"));
            } else if ((varvalue == null) || (varvalue.length() == 0)) {
                displayError(
                    Bundle.getString("AddCondition.missingValue"));
            } else {
                responseOkay = true;
            }
        }

        // Construct a new ValueCondition.
        return new ValueCondition(varname, varvalue);
    } // createCondition

    /**
     * Creates a new command monitor after presenting a dialog
     * to the user to get their input.
     *
     * @param  parent  parent Window for the input dialog.
     * @return  new command monitor, or null if user cancelled.
     */
    protected CommandMonitor createMonitor(Window parent) {
        // Ask the user for command to run.
        Object messages[] = {
            Bundle.getString("AddMonitor.command"),
            new JTextField(30)
        };

        String command = null;

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog to get user input.
            int response = JOptionPane.showOptionDialog(
                parent, messages,
                Bundle.getString("AddMonitor.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                // user cancelled
                return null;
            }

            command = ((JTextField) messages[1]).getText();
            if ((command == null) || (command.length() == 0)) {
                displayError(
                    Bundle.getString("AddMonitor.missingCommand"));
            } else {
                responseOkay = true;
            }
        }

        // Construct a new CommandMonitor.
        return new CommandMonitor(command);
    } // createMonitor

    /**
     * Inform the user of an error.
     *
     * @param  msg  error message.
     */
    protected void displayError(String msg) {
        JOptionPane.showMessageDialog(
            this, msg, Bundle.getString("Dialog.errorTitle"),
            JOptionPane.ERROR_MESSAGE);
    } // displayError

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
     * Try to resolve the breakpoint if it is a resolvable breakpoint
     * and it is currently unresolved.
     *
     * @exception  ResolveException
     *             Thrown if there was a problem resolving.
     */
    protected void resolveBreakpoint() throws ResolveException {
        if ((targetBreakpoint instanceof ResolvableBreakpoint) &&
            (!targetBreakpoint.isResolved())) {

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
        JOptionPane.showMessageDialog(
            this, re.errorMessage(),
            Bundle.getString("ResolveError.title"),
            JOptionPane.ERROR_MESSAGE);
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
