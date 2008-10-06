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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ContextEventMulticaster.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.context;

/**
 * Class ContextEventMulticaster implements a thread-safe list of
 * context listeners. It is technically a tree but it grows only
 * in one direction, which makes it more like a linked list. This
 * class behaves like a listener but it simply forwards the events
 * to the contained listeners.
 *
 * <p>This marvelous design was originally put to code by Amy Fowler and
 * John Rose in the form of the <code>AWTEventMulticaster</code> class
 * in the <code>java.awt</code> package. This implementation is based on
 * the description given in <u>Taming Java Threads</u> by Allen Holub.</p>
 */
public class ContextEventMulticaster implements ContextListener {
    /** A session listener. */
    protected final ContextListener listener1;
    /** A session listener. */
    protected final ContextListener listener2;

    /**
     * Adds the second listener to the first listener and returns the
     * resulting multicast listener.
     *
     * @param  l1  a session listener.
     * @param  l2  the session listener being added.
     * @return  session multicast listener.
     */
    public static ContextListener add(ContextListener l1,
                                      ContextListener l2) {
        return (l1 == null) ? l2 :
               (l2 == null) ? l1 : new ContextEventMulticaster(l1, l2);
    }

    /**
     * Removes the second listener from the first listener and returns
     * the resulting multicast listener.
     *
     * @param  l1  a session listener.
     * @param  l2  the listener being removed.
     * @return  session multicast listener.
     */
    public static ContextListener remove(ContextListener l1,
                                         ContextListener l2) {
        if (l1 == l2 || l1 == null) {
            return null;
        } else if (l1 instanceof ContextEventMulticaster) {
            return ((ContextEventMulticaster) l1).remove(l2);
        } else {
            return l1;
        }
    }

    /**
     * Creates a session event multicaster instance which chains
     * listener l1 with listener l2.
     *
     * @param  l1  a session listener.
     * @param  l2  a session listener.
     */
    protected ContextEventMulticaster(ContextListener l1, ContextListener l2) {
        listener1 = l1;
        listener2 = l2;
    }

    /**
     * Removes a session listener from this multicaster and returns the
     * resulting multicast listener.
     *
     * @param  l  the listener to be removed.
     * @return  the other listener.
     */
    protected ContextListener remove(ContextListener l) {
        if (l == listener1) {
            return listener2;
        }
        if (l == listener2) {
            return listener1;
        }
        // Recursively seek out the target listener.
        ContextListener l1 = remove(listener1, l);
        ContextListener l2 = remove(listener2, l);
        return (l1 == listener1 && l2 == listener2) ? this : add(l1, l2);
    }

    public void changedFrame(ContextEvent ce) {
        listener1.changedFrame(ce);
        listener2.changedFrame(ce);
    }

    public void changedLocation(ContextEvent ce) {
        listener1.changedLocation(ce);
        listener2.changedLocation(ce);
    }

    public void changedThread(ContextEvent ce) {
        listener1.changedThread(ce);
        listener2.changedThread(ce);
    }
}
