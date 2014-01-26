/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        JSwatPanel.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/24/99        Initial version
 *      nf      09/01/01        Removed the unused getPeer()
 *      nf      12/31/01        Removed getParentWindow()
 *
 * DESCRIPTION:
 *      Defines the abstract class for display panels in JSwat.
 *
 * $Id: JSwatPanel.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.*;
import java.awt.*;
import javax.swing.*;

/**
 * Abstract class for all JSwat panels to extend.
 *
 * @author  Nathan Fiedler
 */
public abstract class JSwatPanel implements SessionListener {
    /** Instance of JSwat. */
    protected static JSwat swat = JSwat.instanceOf();

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
    } // activate

    /**
     * Called when the Session is closing down this panel, generally
     * just after the panel has been removed from the Session.
     *
     * @param  session  Session closing the panel.
     */
    public void close(Session session) {
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
    } // deactivate

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public abstract JComponent getUI();

    /**
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
    } // init

    /**
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
    } // refresh
} // JSwatPanel
