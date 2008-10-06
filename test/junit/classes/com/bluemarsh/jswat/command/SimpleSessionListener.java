/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: SimpleSessionListener.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;

/**
 * Useful for see if the Session has suspended, resumed, etc.
 */
public class SimpleSessionListener implements SessionListener {
    /** True if session is open. */
    private volatile boolean open;
    /** True if session is active. */
    private volatile boolean active;
    /** True if session is running. */
    private volatile boolean running;

    /**
     * Session is being activated.
     *
     * @param  sevt  session event.
     */
    public synchronized void activated(SessionEvent sevt) {
        active = true;
        notifyAll();
    }

    /**
     * Session is being closed.
     *
     * @param  sevt  session event.
     */
    public synchronized void closing(SessionEvent sevt) {
        open = false;
        notifyAll();
    }

    /**
     * Session is being deactivated.
     *
     * @param  sevt  session event.
     */
    public synchronized void deactivated(SessionEvent sevt) {
        active = false;
        running = false;
        notifyAll();
    }

    /**
     * Session is being initialized.
     *
     * @param  session  session.
     */
    public synchronized void opened(Session session) {
        open = true;
        notifyAll();
    }

    /**
     * Is session active?
     *
     * @return  true or false.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Is session open?
     *
     * @return  true or false.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Is session running?
     *
     * @return  true or false.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Session is resuming.
     *
     * @param  sevt  session event.
     */
    public synchronized void resuming(SessionEvent sevt) {
        running = true;
        notifyAll();
    }

    /**
     * Session is suspending.
     *
     * @param  sevt  session event.
     */
    public synchronized void suspended(SessionEvent sevt) {
        running = false;
        notifyAll();
    }
}
