/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * FILE:        AbstractPanel.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/24/99        Initial version
 *      nf      09/01/01        Removed the unused getPeer()
 *      nf      12/31/01        Removed getParentWindow()
 *      nf      04/17/02        Removed JSwat instance.
 *      nf      11/25/02        Added restoreTable(), saveTable()
 *      nf      02/06/03        Renamed class
 *      nf      11/22/03        Renamed again
 *
 * $Id: AbstractPanel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.sun.jdi.VMDisconnectedException;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.prefs.Preferences;

/**
 * Provides an incomplete implementation of a Panel.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractPanel
    implements Panel, Runnable, SessionListener {
    /** True if panel is waiting to be refreshed on AWT event thread. */
    private volatile boolean awaitingUpdate;
    /** Session that we are listening to. */
    protected Session owningSession;

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        owningSession = null;
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
    } // deactivated

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public abstract JComponent getUI();

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        owningSession = session;
    } // opened

    /**
     * Update the display on the screen. Use the given session to fetch
     * the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
    } // refresh

    /**
     * Invoke the refresh() method on the AWT event dispatching thread.
     * Otherwise we run the risk of modifying the data model while the
     * component is being rendered.
     */
    public void refreshLater() {
        if (!awaitingUpdate) {
            awaitingUpdate = true;
            SwingUtilities.invokeLater(this);
        }
    } // refreshLater

    /**
     * Creates table columns with the widths and model indices saved in
     * the user preferences. If the preferences are missing the
     * information, use the given default values.
     *
     * <p>This method returns a new array of the column headers in the
     * preferred column order.</p>
     *
     * @param  colmod   table column model to add columns to.
     * @param  prefs    preferences with settings.
     * @param  widths   list of default column widths.
     * @param  headers  array of column header values.
     * @return  array of column headers in preferred order.
     */
    protected String[] restoreTable(TableColumnModel colmod, Preferences prefs,
                                    int[] widths, String[] headers) {
        String[] newnames = new String[widths.length];
        for (int ii = 0; ii < widths.length; ii++) {
            int index = prefs.getInt("col" + ii + "index", ii);
            int width = prefs.getInt("col" + ii + "width", widths[ii]);
            // Constructor sets the preferred width, too.
            TableColumn tc = new TableColumn(index, width);
            // Must set header value because nobody else will.
            tc.setHeaderValue(headers[index]);
            colmod.addColumn(tc);
            // Put the column header values in the right order.
            newnames[ii] = headers[index];
        }
        return newnames;
    } // restoreTable

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Refresh the tree model and cause the tree to repaint.
     */
    public void run() {
        // Set this false first so we limit the number of events
        // that we may miss during the processing.
        awaitingUpdate = false;
        try {
            refresh(owningSession);
        } catch (VMDisconnectedException vmde) {
            // This is normal.
        } catch (Throwable t) {
            // Catch everything else and report gracefully.
            if (owningSession != null
                && owningSession.getStatusLog() != null) {
                owningSession.getStatusLog().writeStackTrace(t);
            }
        }
    } // run

    /**
     * Save the table column widths, and positions within the column
     * model, to the user preferences.
     *
     * @param  colmod  table column model to be preserved.
     * @param  prefs   preferences to save table model to.
     */
    protected void saveTable(TableColumnModel colmod, Preferences prefs) {
        for (int ii = colmod.getColumnCount() - 1; ii >= 0; ii--) {
            TableColumn col = colmod.getColumn(ii);
            prefs.putInt("col" + ii + "index", col.getModelIndex());
            prefs.putInt("col" + ii + "width", col.getWidth());
        }
    } // saveTable

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // AbstractPanel
