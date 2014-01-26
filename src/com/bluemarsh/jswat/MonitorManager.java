/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * FILE:        MonitorManager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/14/02        Initial version
 *
 * $Id: MonitorManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class MonitorManager is responsible for managing the set of common
 * monitors. Monitors are executable objects that are executed each time
 * the debuggee VM halts execution.
 *
 * <p>The monitors managed by this manager are not associated with any
 * particular breakpoint. Instead, they are standalone monitors,
 * mananged by the monitor and unmonitor commands.</p>
 *
 * @author  Nathan Fiedler
 */
public class MonitorManager implements Manager {
    /** Logger. */
    private static Logger logger;
    /** List of monitors this breakpoint executes when it stops. */
    private List monitorList;
    /** Table of all monitors, keyed by a unique number. The number is
     * assigned at the time the monitor is created and will be unique
     * among the set of monitors.
     * @see #lastMonitorNumber
     */
    private Hashtable monitorsTable;
    /** Value representing the last number assigned to a new monitor.
     * Used to key monitors in a table so they may be referred to by a
     * unique number.
     * @see #monitorsTable
     */
    private int lastMonitorNumber;
    /** True if the debuggee was running at one point. */
    private boolean hasResumed;

    static {
        // Initialize the logger.
        logger = Logger.getLogger("com.bluemarsh.jswat.monitor");
        com.bluemarsh.jswat.logging.Logging.setInitialState(logger);
    }

    /**
     * Constructs a MonitorManager instance.
     */
    public MonitorManager() {
        monitorList = new LinkedList();
        monitorsTable = new Hashtable();
    } // MonitorManager

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
    } // activated

    /**
     * Add the given monitor to this breakpoint. That is, when the
     * <code>stopped()</code> method is called, this breakpoint will
     * execute this monitor.
     *
     * @param  monitor  monitor for this breakpoint to execute.
     */
    public void addMonitor(Monitor monitor) {
        synchronized (monitorList) {
            monitorList.add(monitor);
            // Keep track of the monitor numbers.
            monitorsTable.put(new Integer(++lastMonitorNumber), monitor);
            monitor.setNumber(lastMonitorNumber);
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("added monitor " + monitor);
        }
    } // addMonitor

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        // Persist the monitors to the preferences.
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/monitors");
        // First completely obliterate this node.
        try {
            prefs.removeNode();
        } catch (BackingStoreException bse) {
            // Just overwrite the data then.
        }
        // Then recreate it from scratch.
        prefs = Preferences.userRoot().node("com/bluemarsh/jswat/monitors");

        // Save out the monitors.
        logger.info("saving monitors to preferences");
        int n = 1;
        Iterator iter = monitorList.iterator();
        while (iter.hasNext()) {
            Monitor m = (Monitor) iter.next();
            m.writeObject(prefs.node("monitor" + n));
            n++;
        }
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
     * Retrieve the nth monitor. Each and every monitor managed by this
     * monitor manager has a unique, invariant number assigned to it.
     * The monitors can be retrieved by referring to this number.
     *
     * @param  n   monitor number.
     * @return  Monitor, or null if there was no monitor referenced
     *          by the <code>n</code> value.
     */
    public Monitor getMonitor(int n) {
        return (Monitor) monitorsTable.get(new Integer(n));
    } // getMonitor

    /**
     * Returns an iterator of the monitors associated with this
     * breakpoint.
     *
     * @return  ListIterator of <code>Monitor</code> objects.
     */
    public ListIterator monitors() {
        return monitorList.listIterator();
    } // monitors

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        // Recreate the monitors from the persistent store.
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/monitors");
        logger.info("loading monitors from preferences");

        // Look for persisted monitors.
        try {
            int n = 1;
            String subname = "monitor" + n;
            while (prefs.nodeExists(subname)) {
                // We have monitors.
                Preferences subprefs = prefs.node(subname);
                String cname = subprefs.get("class", null);
                if (cname != null) {
                    Class bclass = Class.forName(cname);
                    Monitor m = (Monitor) bclass.newInstance();
                    addMonitor(m);
                    if (!m.readObject(subprefs)) {
                        String msg = MessageFormat.format(
                            Bundle.getString("Monitor.errorRead"),
                            new Object[] { String.valueOf(n) });
                        session.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_WARNING, msg);
                        removeMonitor(m);
                    }
                } else {
                    String msg = MessageFormat.format(
                        Bundle.getString("Monitor.errorNoClass"),
                        new Object[] { String.valueOf(n) });
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING, msg);
                }
                n++;
                subname = "monitor" + n;
            }

        } catch (ClassNotFoundException cnfe) {
            String msg = MessageFormat.format(
                Bundle.getString("Monitor.errorRead"), new Object[] { cnfe });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
        } catch (IllegalAccessException iae) {
            String msg = MessageFormat.format(
                Bundle.getString("Monitor.errorRead"), new Object[] { iae });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
        } catch (InstantiationException ie) {
            String msg = MessageFormat.format(
                Bundle.getString("Monitor.errorRead"), new Object[] { ie });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
        } catch (BackingStoreException bse) {
            String msg = MessageFormat.format(
                Bundle.getString("Monitor.errorRead"), new Object[] { bse });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
        }

        // Iterate the monitors and get their 'number' property, and use
        // that value to add the monitor to the table.
        Iterator iter = monitors();
        while (iter.hasNext()) {
            Monitor mon = (Monitor) iter.next();
            int num = mon.getNumber();
            monitorsTable.put(new Integer(num), mon);
            if (num > lastMonitorNumber) {
                lastMonitorNumber = num;
            }
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("last monitor # = " + lastMonitorNumber);
        }
    } // opened

    /**
     * Remove the given monitor from this breakpoint. This monitor
     * should no longer be associated with this breakpoint. If the
     * monitor is not a part of this breakpoint, nothing happens.
     *
     * @param  monitor  monitor to remove from this breakpoint.
     */
    public void removeMonitor(Monitor monitor) {
        synchronized (monitorList) {
            monitorList.remove(monitor);
            // Remove the monitor from our number table.
            monitorsTable.remove(new Integer(monitor.getNumber()));
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("removed monitor " + monitor);
        }
    } // removeMonitor

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
        // This only needs to be set the one time, and never has to be
        // reset. We only need to know that the VM was running at some
        // point, since suspended() gets called on activation of a
        // suspended VM.
        hasResumed = true;
    } // resuming

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
        if (hasResumed) {
            // Invoke each of the monitors, in an unsychronized fashion.
            // We are not expecting multiple threads to modify this
            // list, but if it does happen, an exception will be thrown.
            logger.info("executing monitors");
            Iterator iter = monitorList.iterator();
            while (iter.hasNext()) {
                Monitor monitor = (Monitor) iter.next();
                monitor.perform(sevt.getSession());
            }
        }
    } // suspended
} // MonitorManager
