/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Michael Swartzendruber
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
 * PROJECT:     JSwat
 * MODULE:      Panel
 * FILE:        MethodsPanel.java
 *
 * AUTHOR:      Michael Swartzendruber
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      mjs     01/24/01        Initial version
 *      nf      02/10/01        Tidied up a bit
 *      nf      08/06/01        Changed to use PathManager
 *      nf      09/02/01        Fixed bug #215
 *
 * DESCRIPTION:
 *      Defines the class responsible for displaying methods for the
 *      object currently in 'focus' in the debugger. These methods
 *      are displayed in a JTable.
 *
 * $Id: MethodsPanel.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.*;
import com.bluemarsh.jswat.event.*;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Class MethodsPanel is responsible for displaying the methods in
 * the 'this' object in the current stack frame.
 *
 * @author  Michael Swartzendruber
 */
public class MethodsPanel extends JSwatPanel implements ContextListener, MouseListener  {
    /** Our UI component - scrollable pane */
    protected JScrollPane uicomp;
    /** Our table component - displays the stack frames */
    protected JTable table;
    /** Handy reference to the Session that owns us. */
    protected Session owningSession;
    /** Handy reference to the Session's ContextManager. */
    protected ContextManager contextManager;
    /** 'this' object's class; may be null. */
    protected ReferenceType thisClass;

    /**
     * Constructs a new MethodsPanel with the default text area.
     */
    public MethodsPanel() {
        String[] columnNames = new String[2];
        columnNames[0] = Bundle.getString("Method.numberColumn");
        columnNames[1] = Bundle.getString("Method.nameColumn");
        table = new JTable(new ViewTableModel(columnNames));
        table.addMouseListener(this);

        // Set the column widths to some hard-coded values.
        TableColumn column = table.getColumnModel().getColumn(0);
        // Give the columns a reasonable maximum width so they
        // aren't resized way too big.
        column.setMaxWidth(120);
        column.setPreferredWidth(60);
        uicomp = new JScrollPane(table);
    } // MethodsPanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        // Add ourselves as a context change listener.
        contextManager.addContextListener(this);
    } // activate

    /**
     * Called when the Session is closing down this panel, generally
     * just after the panel has been removed from the Session.
     *
     * @param  session  Session closing the panel.
     */
    public void close(Session session) {
        owningSession = null;
        contextManager = null;
    } // close

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        refresh(owningSession);
    } // contextChanged

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        // Remove ourselves as a context change listener.
        contextManager.removeContextListener(this);

        // Update the UI to show nothing.
        ViewTableModel model = (ViewTableModel)table.getModel();
        model.clear();
    } // deactivate

    /**
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
        this.owningSession = session;
        contextManager = (ContextManager)
            session.getManager(ContextManager.class);
    } // init

    /**
     * Returns a reference to the peer UI component. In many
     * cases this is a JList, JTree, or JTable, depending on
     * the type of data being displayed in the panel.
     *
     * @return  peer ui component object
     */
    public JComponent getPeer() {
        return table;
    } // getPeer

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return uicomp;
    } // getUI

    /**
     * Invoked when the mouse has been clicked on a component.
     * We use this to take action on the table.
     *
     * @param  e  Mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (thisClass == null) {
            // Nothing to do.
            return;
        }

        if (e.getClickCount() == 2) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());

            // Try to open the source for this frame.
            ViewTableModel model = (ViewTableModel) table.getModel();
            String lineNumber = (String) model.getValueAt(row, 0);
            if (lineNumber != null) {

                // Map the 'this' class to a source file.
                PathManager pathman = (PathManager)
                    owningSession.getManager(PathManager.class);
                SourceSource src = null;
                try {
                    src = pathman.mapSource(thisClass);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                if (src != null) {
                    // Try to show the source line for this location.
                    int line = 0;
                    try {
                        line = Integer.parseInt(lineNumber);
                    } catch (NumberFormatException nfe) { }
                    UIAdapter adapter = owningSession.getUIAdapter();
                    adapter.showFile(src, line, 0);
                } else {
                    JOptionPane.showMessageDialog
                        (null, swat.getResourceString("couldntMapSrcFile"),
                         swat.getResourceString("Dialog.Error.title"),
                         JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    } // mouseClicked

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param  e  Mouse event.
     */
    public void mousePressed(MouseEvent e) {
    } // mousePressed

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseReleased(MouseEvent e) {
    } // mouseReleased

    /**
     * Invoked when the mouse enters a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseEntered(MouseEvent e) {
    } // mouseEntered

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseExited(MouseEvent e) {
    } // mouseExited

    /**
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        ViewTableModel model = (ViewTableModel) table.getModel();
        model.clear();
        ThreadReference currentThread = contextManager.getCurrentThread();
        if (currentThread == null) {
            if (session.isActive()) {
                model.setMessage(Bundle.getString("nothreadInParen"), 1);
            }
            return;
        }

        // Get the stack frame.
        StackFrame frame;
        try {
            frame = currentThread.frame(contextManager.getCurrentFrame());
            if (frame == null) {
                model.setMessage(Bundle.getString("noframeInParen"), 1);
                return;
            }
        } catch (IncompatibleThreadStateException itse) {
            model.setMessage(Bundle.getString("threadRunningInParen"), 1);
            return;
        } catch (IndexOutOfBoundsException ioobe) {
            model.setMessage(Bundle.getString("noframeInParen"), 1);
            return;
        } catch (NativeMethodException nme) {
            model.setMessage(Bundle.getString("nativeInParen"), 1);
            return;
        } catch (VMDisconnectedException vmde) {
            // Do nothing, just return.
            return;
        } catch (Exception e) {
            // All other exceptions result in an immediate abort.
            owningSession.getStatusLog().writeStackTrace(e);
            model.setMessage(Bundle.getString("errorInParen"), 1);
            return;
        }

        ReferenceType refType = frame.location().declaringType();
        thisClass = refType;
        List methods = refType.allMethods();

        // Must synchronize to avoid problems with events that
        // fire off asynchronously.
        synchronized (model) {
            StringBuffer buffer = new StringBuffer(128);
            // Iterate over the methods and build out the table.
            for (int i = 0; i < methods.size(); i++) {
                try {
                    Method aMethod = (Method) methods.get(i);
                    ReferenceType declaringType = aMethod.declaringType();
                    if (!declaringType.name().equals(refType.name())) {
                        // Skip methods in superclasses, as that
                        // gets rather overwhelming.
                        continue;
                    }

                    if (aMethod.isStaticInitializer()) {
                        // Skip static initializers, they're not methods.
                        continue;
                    }

                    // Add a new row to the model.
                    int row = model.addRow();
                    Location location = aMethod.location();
                    if (location != null) {
                        // Set the line number for this entry.
                        model.setValueNoEvent
                            (String.valueOf(location.lineNumber()), row, 0);
                    } else {
                        model.setValueNoEvent("??", row, 0);
                    }

                    // Get the method modifiers.
                    int modifiers = aMethod.modifiers();
                    String modifier = Modifier.toString(modifiers);
                    if (modifier.length() > 0) {
                        buffer.append(modifier);
                        buffer.append(' ');
                    }

                    // Get the method return type.
                    String returnType = aMethod.returnTypeName();
                    returnType = returnType.substring
                        (returnType.lastIndexOf('.') + 1);
                    buffer.append(returnType);
                    buffer.append(' ');

                    // Get the method name.
                    if (aMethod.isConstructor()) {
                        String methodName = refType.name();
                        methodName = methodName.substring
                            (methodName.lastIndexOf('.') + 1);
                        buffer.append(methodName);
                    } else {
                        buffer.append(aMethod.name());
                    }
                    buffer.append('(');

                    // Get the method arguments.
                    List args = aMethod.argumentTypeNames();
                    for (int x = 0; x < args.size(); x++) {
                        String anArg = (String) args.get(x);
                        boolean isArray = false;
                        if (anArg.indexOf('[') != -1) {
                            isArray = true;
                        }
                        anArg = anArg.substring(anArg.lastIndexOf('.') + 1);
                        buffer.append(anArg);
                        if (true == isArray) {
                            buffer.append("[]");
                        }
                        if (x < (args.size() - 1)) {
                            buffer.append(", ");
                        }
                    }
                    buffer.append(')');
                    model.setValueNoEvent(buffer.toString(), row, 1);
                    buffer = new StringBuffer(128);
                } catch (Exception ex) {
                    owningSession.getStatusLog().writeStackTrace(ex);
                }
            }
        }

        // Notify the listeners that the table has changed.
        model.fireTableDataChanged();
    } // refresh
} // MethodsPanel
