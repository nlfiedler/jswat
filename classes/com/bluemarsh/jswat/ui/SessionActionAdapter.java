/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: SessionActionAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.action.SessionAction;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Class SessionActionAdapter is responsible for enabling and disabling
 * menu items and toolbar buttons when the session becomes active or
 * inactive, or has resumed or suspended.
 *
 * @author  Nathan Fiedler
 */
public class SessionActionAdapter implements SessionListener {
    /** Table of session actions and their list of UI components. */
    private Hashtable actionTable;
    /** True if enabled, false if disabled. */
    private boolean enabled;
    /** True if debuggee is running, false otherwise. */
    private boolean vmRunning;

    /**
     * Constructs a SessionActionAdapter object.
     */
    public SessionActionAdapter() {
        actionTable = new Hashtable();
    } // SessionActionAdapter

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        enabled = true;
        setItemsState();
    } // activated

    /**
     * Add a component to be enabled and disabled with the session.
     * The SessionAction dictates how the component should be
     * enabled or disabled while the session is active.
     *
     * @param  a  session action for this component.
     * @param  c  component to control.
     */
    public void addComponent(SessionAction a, Component c) {
        List alist = (List) actionTable.get(a);
        if (alist == null) {
            alist = new ArrayList();
            actionTable.put(a, alist);
        }
        alist.add(c);
        decideStatus(a, c);
    } // addComponent

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        enabled = false;
        vmRunning = false;
        setItemsState();
    } // deactivated

    /**
     * Decide whether the component should be enable or disabled.
     *
     * @param  a  session action for this component.
     * @param  c  component to control.
     */
    protected void decideStatus(SessionAction a, Component c) {
        if (enabled) {
            // Get the default status for this action.
            boolean enable = !a.disableWhenActive();
            if (vmRunning) {
                if (a.disableOnResume()) {
                    enable = false;
                }
            } else {
                if (a.disableOnSuspend()) {
                    enable = false;
                }
            }
            c.setEnabled(enable);
        } else {
            c.setEnabled(a.disableWhenActive());
        }
    } // decideStatus

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
    } // opened

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
        vmRunning = true;
        setItemsState();
    } // resuming

    /**
     * Set the enabled state of the items to the value of 'enabled'.
     */
    protected void setItemsState() {
        Enumeration actions = actionTable.keys();
        while (actions.hasMoreElements()) {
            SessionAction a = (SessionAction) actions.nextElement();
            List list = (List) actionTable.get(a);
            for (int ii = list.size() - 1; ii >= 0; ii--) {
                Component c = (Component) list.get(ii);
                decideStatus(a, c);
            }
        }
    } // setItemsState

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
        vmRunning = false;
        setItemsState();
    } // suspended
} // SessionActionAdapter
