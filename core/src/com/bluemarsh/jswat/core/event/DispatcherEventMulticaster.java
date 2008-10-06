/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DispatcherEventMulticaster.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;
import java.util.Iterator;

/**
 * Class JdiEventMulticaster implements a thread-safe list of
 * JdiEventListeners. It is technically a tree but it grows only
 * in one direction, which makes it more like a linked list. This
 * class behaves like a listener but it simply forwards the events
 * to the contained JdiEventListeners.
 *
 * <pre><code>
 * JdiEventListener eventListener = null;
 *
 * public synchronized void addJdiEventListener(JdiEventListener l) {
 *     eventListener = JdiEventMulticaster.add(eventListener, l);
 * }
 * public synchronized void removeJdiEventListener(JdiEventListener l) {
 *     eventListener = JdiEventMulticaster.remove(eventListener, l);
 * }
 * protected void fireEvent(JdiEvent e) {
 *     JdiEventListener listener = eventListener;
 *     if (listener != null) {
 *         listener.eventOccurred(e);
 *     }
 * }
 * </code></pre>
 *
 * <p>This marvelous design was originally put to code by Amy Fowler and
 * John Rose in the form of the <code>AWTEventMulticaster</code> class
 * in the <code>java.awt</code> package. This implementation is based on
 * the description given in <u>Taming Java Threads</u> by Allen Holub.</p>
 */
public class DispatcherEventMulticaster implements DispatcherListener {
    /** An event listener. */
    protected final DispatcherListener listener1;
    /** An event listener. */
    protected final DispatcherListener listener2;

    /**
     * Adds the second listener to the first listener and returns the
     * resulting multicast listener.
     *
     * @param  l1  an event listener.
     * @param  l2  the event listener being added.
     * @return  event multicast listener.
     */
    public static DispatcherListener add(DispatcherListener l1,
                                       DispatcherListener l2) {
        return (l1 == null) ? l2 :
               (l2 == null) ? l1 : new DispatcherEventMulticaster(l1, l2);
    }

    /**
     * Removes the second listener from the first listener and returns
     * the resulting multicast listener.
     *
     * @param  l1  an event listener.
     * @param  l2  the listener being removed.
     * @return  event multicast listener.
     */
    public static DispatcherListener remove(DispatcherListener l1,
                                          DispatcherListener l2) {
        if (l1 == l2 || l1 == null) {
            return null;
        } else if (l1 instanceof DispatcherEventMulticaster) {
            return ((DispatcherEventMulticaster) l1).remove(l2);
        } else {
            return l1;
        }
    }

    /**
     * Creates an event multicaster instance which chains listener l1 with
     * listener l2.
     *
     * @param  l1  an event listener.
     * @param  l2  an event listener.
     */
    protected DispatcherEventMulticaster(DispatcherListener l1, DispatcherListener l2) {
        listener1 = l1;
        listener2 = l2;
    }

    /**
     * Removes an event listener from this multicaster and returns the
     * resulting multicast listener.
     *
     * @param  l  the listener to be removed.
     * @return  the other listener.
     */
    protected DispatcherListener remove(DispatcherListener l) {
        if (l == listener1) {
            return listener2;
        }
        if (l == listener2) {
            return listener1;
        }
        // Recursively seek out the target listener.
        DispatcherListener l1 = remove(listener1, l);
        DispatcherListener l2 = remove(listener2, l);
        return (l1 == listener1 && l2 == listener2) ? this : add(l1, l2);
    }

    public boolean eventOccurred(DispatcherEvent e) {
        // True only if both listeners return true.
        return listener1.eventOccurred(e) && listener2.eventOccurred(e);
    }

    public Iterator<Class> eventTypes() {
        // This will never be called by the event handler.
        return null;
    }
}
