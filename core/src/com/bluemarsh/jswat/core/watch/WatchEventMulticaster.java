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
 * are Copyright (C) 2006-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.watch;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Class WatchEventMulticaster implements a thread-safe list of watch listeners.
 * In addition, it acts as a listener such that events can be sent to the
 * multicaster and it will dispatch the events to all registered listeners.
 *
 * @author Nathan Fiedler
 */
public class WatchEventMulticaster implements WatchListener {

    /**
     * A set of unique watch listeners.
     */
    private final Set<WatchListener> listeners;

    /**
     * Creates a new instance of WatchEventMulticaster.
     */
    public WatchEventMulticaster() {
        // Use CopyOnWriteArraySet so that our listeners are unique,
        // that the average case of iterating the list is kept fast
        // and efficient, and that the unusual case of adding/removing
        // from the list is still thread-safe (albeit via copying).
        listeners = new CopyOnWriteArraySet<WatchListener>();
    }

    /**
     * Adds the given listener to the set of listeners.
     *
     * @param l a watch listener.
     */
    public void add(WatchListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    /**
     * Removes the given listener from the set of listeners.
     *
     * @param l a watch listener.
     */
    public void remove(WatchListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    @Override
    public void watchAdded(WatchEvent e) {
        for (WatchListener l : listeners) {
            l.watchAdded(e);
        }
    }

    @Override
    public void watchRemoved(WatchEvent e) {
        for (WatchListener l : listeners) {
            l.watchRemoved(e);
        }
    }
}
