/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import java.util.EventObject;

/**
 * An event which indicates that a watch has changed status.
 *
 * @author  Nathan Fiedler
 */
public class WatchEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The watch that changed. */
    private transient Watch watch;
    /** The type of watch change. */
    private Type type;

    /**
     * Type of watch event.
     */
    public static enum Type {
        /** Watch was added (to the WatchManager). */
        ADDED {
            public void fireEvent(WatchEvent e, WatchListener l) {
                l.watchAdded(e);
            }
        },
        /** Watch was removed (from the WatchManager). */
        REMOVED {
            public void fireEvent(WatchEvent e, WatchListener l) {
                l.watchRemoved(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(WatchEvent e, WatchListener l);
    }

    /**
     * Constructs a new WatchEvent.
     *
     * @param  watch  watch that changed (source of event).
     * @param  type   type of watch change.
     */
    public WatchEvent(Watch watch, Type type) {
        super(watch);
        this.watch = watch;
        this.type = type;
    }

    /**
     * Get the watch that changed.
     *
     * @return  watch.
     */
    public Watch getWatch() {
        return watch;
    }

    /**
     * Get the watch event type.
     *
     * @return  watch event type.
     */
    public Type getType() {
        return type;
    }
}
