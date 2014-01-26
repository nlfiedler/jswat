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
 * $Id: ManagerDialog.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.panel.BreakPanel;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

/**
 * Class ManagerDialog is responsible for building a dialog that
 * presents all of the breakpoint groups and breakpoints. It allows
 * the user to add new breakpoints, as well as edit or delete the
 * existing breakpoint groups and breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class ManagerDialog extends JDialog {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Panel showing the breakpoints. */
    protected BreakPanel breakpointsPanel;
    /** Session this dialog is associated with. */
    protected Session owningSession;

    /**
     * Constructs the breakpoint managing dialog.
     *
     * @param  session  Owning session.
     * @param  window   Owning window.
     */
    public ManagerDialog(Session session, Frame owner) {
        super(owner, Bundle.getString("ManagerDialog.title"));
        owningSession = session;

        Container pane = getContentPane();
        breakpointsPanel = new BreakPanel();
        session.addListener(breakpointsPanel);
        // Have to manually refresh the panel.
        breakpointsPanel.refresh(session);
        pane.add(breakpointsPanel.getUI(), "Center");

        // Set up the dialog's closing procedure.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // Close the breakpoints panel and this dialog.
                    owningSession.removeListener(breakpointsPanel);
                    dispose();
                }
            });

        // Size and position the dialog.
        pack();
        if (getWidth() < 400) {
            setSize(400, getHeight());
        }
        setLocationRelativeTo(owner);
        setVisible(true);
    } // ManagerDialog
} // ManagerDialog
