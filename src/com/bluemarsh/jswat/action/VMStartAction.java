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
 * $Id: VMStartAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.util.JVMArguments;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import java.awt.Frame;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

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
     * Performs the virtual machine start action. Gets the name
     * of the class to be debugged and starts a virtual machine.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame win = getFrame(event);
        Session session = getSession(event);

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
            javaHome = ((Connector.Argument) args.get("home")).value();;
        }
        if (jvmExecutable == null || jvmExecutable.length() == 0) {
            jvmExecutable = ((Connector.Argument) args.get("vmexec")).value();;
        }

        // Ask the user for name of class to launch, along
        // with arguments to that class, and args to JVM.
        Object messages[] = {
            Bundle.getString("VMStart.javaHome"),
            new JTextField(javaHome, 30),
            Bundle.getString("VMStart.jvmExecutable"),
            new JTextField(jvmExecutable, 30),
            Bundle.getString("VMStart.jvmArgsField"),
            new JTextField(jvmOptions, 30),
            Bundle.getString("VMStart.nameAndArgsField"),
            new JTextField(mainClass, 30),
            new JCheckBox(Bundle.getString("VMStart.startSuspended"),
                          startSuspended)
        };

        boolean responseOkay = false;
        while (!responseOkay) {
            // Show dialog to get user input.
            int response = JOptionPane.showOptionDialog
                (win, messages,
                 Bundle.getString("VMStart.title"),
                 JOptionPane.OK_CANCEL_OPTION,
                 JOptionPane.QUESTION_MESSAGE,
                 null, null, null);
            if (response != JOptionPane.OK_OPTION) {
                // user cancelled
                return;
            }

            mainClass = ((JTextField) messages[7]).getText();
            if (mainClass == null || mainClass.length() == 0) {
                // Missing the classname to load.
                displayError(event, Bundle.getString("VMStart.missingClass"));
            } else {
                responseOkay = true;
            }
        }

        javaHome = ((JTextField) messages[1]).getText();
        jvmExecutable = ((JTextField) messages[3]).getText();
        jvmOptions = ((JTextField) messages[5]).getText();
        JVMArguments jvmArgs = new JVMArguments(jvmOptions);
        jvmOptions = jvmArgs.normalizedOptions(session);
        startSuspended = ((JCheckBox) messages[8]).isSelected();

        if (session.isActive()) {
            // Deactivate current session.
            session.deactivate(false);
        }

        VMConnection connection = VMConnection.buildConnection
            (javaHome, jvmExecutable, jvmOptions, mainClass);

        // Display the options and classname that the launcher is
        // actually going to use.
        Log out = session.getStatusLog();
        StringBuffer buf = new StringBuffer(
            swat.getResourceString("vmLoading"));
        buf.append('\n');
        buf.append(connection.getConnectArg("home"));
        buf.append(File.separator);
        buf.append("bin");
        buf.append(File.separator);
        buf.append(connection.getConnectArg("vmexec"));
        buf.append(' ');
        buf.append(connection.getConnectArg("options"));
        buf.append('\n');
        buf.append(connection.getConnectArg("main"));
        out.writeln(buf.toString());

        // Save the values to the session settings for later reuse.
        session.setProperty("javaHome", javaHome);
        session.setProperty("jvmExecutable", jvmExecutable);
        session.setProperty("jvmOptions", jvmArgs.parsedOptions());
        session.setProperty("mainClass", mainClass);
        session.setProperty("startSuspended",
                            String.valueOf(startSuspended));

        // Show a busy cursor while we launch the debuggee.
        win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean launched = connection.launchDebuggee(session);
        win.setCursor(Cursor.getDefaultCursor());

        if (launched) {
            if (!startSuspended) {
                // Now that the Session has completely activated,
                // we may resume the debuggee VM.
                try {
                    session.resumeVM();
                } catch (NotActiveException nae) { }
            }
        } else {
            if (!startSuspended) {
                messages = new Object[2];
                messages[0] = swat.getResourceString("vmLoadFailed");
                messages[1] = Bundle.getString("VMStart.tryStartingSuspended");
            } else {
                messages = new Object[1];
                messages[0] = swat.getResourceString("vmLoadFailed");
            }
            JOptionPane.showMessageDialog
                (win, messages,
                 swat.getResourceString("Dialog.Error.title"),
                 JOptionPane.ERROR_MESSAGE);
        }
    } // actionPerformed
} // VMStartAction
