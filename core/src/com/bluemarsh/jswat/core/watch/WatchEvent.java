/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchEvent.java 6 2007-05-16 07:14:24Z nfiedler $
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
