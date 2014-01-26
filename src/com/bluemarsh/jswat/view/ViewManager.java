/*********************************************************************
 *
 *      Copyright (C) 2003-2004 Nathan Fiedler
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
 * $Id: ViewManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Manager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.SwingUtilities;

/**
 * The ViewManager class is responsible for maintaining the list of open
 * views. It also handles switching between the different display modes
 * for the views.
 *
 * @author  Nathan Fiedler
 */
public class ViewManager implements Manager, Runnable {
    /** The Session we belong to. */
    private Session owningSession;
    /** Table of open view objects, where the keys are the Views and the
     * values are the SourceSources. */
    private Hashtable viewToSourceMap;
    /** Table of open view objects, where the keys are the SourceSources
     * and the values are the Views. */
    private Hashtable sourceToViewMap;

    /**
     * Constructs a ViewManager.
     */
    public ViewManager() {
        viewToSourceMap = new Hashtable();
        sourceToViewMap = new Hashtable();
    } // ViewManager

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        refreshViews();
    } // activated

    /**
     * Adds the view to this manager's list of open views.
     *
     * @param  view  view to be added to the display.
     * @param  src   source of the view.
     */
    public void addView(View view, SourceSource src) {
        viewToSourceMap.put(view, src);
        sourceToViewMap.put(src, view);
        if (view instanceof SessionListener) {
            owningSession.addListener((SessionListener) view);
        }
    } // addView

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        // Remove all of the open source views as session listeners.
        Enumeration views = viewToSourceMap.keys();
        while (views.hasMoreElements()) {
            View view = (View) views.nextElement();
            if (view instanceof SessionListener) {
                owningSession.removeListener((SessionListener) view);
            }
        }
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
     * Retrieves the view for the given source. If no view has been
     * opened for this source, return null.
     *
     * @param  src  source object.
     * @return  open view for source, or null if none.
     */
    public View getView(SourceSource src) {
        return (View) sourceToViewMap.get(src);
    } // getView

    /**
     * Returns an enumeration over the set of open views.
     *
     * @return  enumeration of open views.
     */
    public Enumeration getViews() {
        return viewToSourceMap.keys();
    } // getViews

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
     * Refresh the open views immediately.
     */
    public void refreshViews() {
        // Manually refresh all of the open views. Better this than
        // having the views do it themselves; in that case our
        // openFile() causes the view refresh() to be invoked twice,
        // which is dumb.
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    } // refreshViews

    /**
     * Remove the given source view from the list.
     *
     * @param  view  source view object to remove.
     */
    public void removeView(View view) {
        if (view instanceof SessionListener) {
            owningSession.removeListener((SessionListener) view);
        }
        sourceToViewMap.remove(viewToSourceMap.remove(view));
    } // removeView

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Refresh the open views.
     */
    public void run() {
        Enumeration views = viewToSourceMap.keys();
        while (views.hasMoreElements()) {
            View view = (View) views.nextElement();
            SourceSource src = (SourceSource) viewToSourceMap.get(view);
            try {
                view.refresh(src, 0);
            } catch (IOException ioe) {
                owningSession.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING, ioe.toString());
            }
        }
    } // run

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // ViewManager
