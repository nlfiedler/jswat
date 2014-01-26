/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        SessionActionAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/16/02        Initial version
 *
 * DESCRIPTION:
 *      This file defines the Session to SessionAction adapter.
 *
 * $Id: SessionActionAdapter.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionListener;
import com.bluemarsh.jswat.action.SessionAction;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * Class SessionActionAdapter is responsible for enabling and disabling
 * menu items and toolbar buttons when the session becomes active or
 * inactive.
 *
 * @author  Nathan Fiedler
 */
class SessionActionAdapter implements SessionListener {
    /** List of components to enable and disable. */
    protected List componentList;
    /** True if enabled, false if disabled. */
    protected boolean enabled;

    /**
     * Constructs a SessionActionAdapter object.
     */
    public SessionActionAdapter() {
        componentList = new ArrayList();
    } // SessionActionAdapter

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        enabled = true;
        setItemsState();
    } // activate

    /**
     * Add a component to be enabled and disabled with the session.
     *
     * @param  c  component to control.
     */
    public void addComponent(JComponent c) {
        componentList.add(c);
        if (enabled) {
            c.setEnabled(true);
        } else {
            c.setEnabled(false);
        }
    } // addComponent

    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        enabled = false;
        setItemsState();
    } // deactivate

    /**
     * Called after the Session has added this listener to the
     * Session listener list.
     *
     * @param  session  Session adding this listener.
     */
    public void init(Session session) {
    } // init

    /**
     * Set the enabled state of the items to the value of 'enabled'.
     */
    protected void setItemsState() {
        int size = componentList.size();
        for (int ii = 0; ii < size; ii++) {
            JComponent c = (JComponent) componentList.get(ii);
            c.setEnabled(enabled);
        }
    } // setItemsState
} // SessionActionAdapter
