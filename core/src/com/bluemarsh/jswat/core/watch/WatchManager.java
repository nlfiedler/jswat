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
 * $Id: WatchManager.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import java.util.Iterator;

/**
 * A WatchManager maintains the watch expressions within the application.
 * Concrete implementations of this interface are acquired from the
 * <code>WatchProvider</code> class.
 *
 * @author Nathan Fiedler
 */
public interface WatchManager {

    /**
     * Adds the given watch to the watch list.
     *
     * @param  watch  watch to be added.
     */
    void addWatch(Watch watch);

    /**
     * Add an event listener to this manager object.
     *
     * @param  listener  new listener to add to notification list.
     */
    void addWatchListener(WatchListener listener);

    /**
     * Remove the given watch from the managed list.
     *
     * @param  watch  watch to be removed.
     */
    void removeWatch(Watch watch);

    /**
     * Remove an event listener from this manager object.
     *
     * @param  listener  listener to remove from notification list.
     */
    void removeWatchListener(WatchListener listener);

    /**
     * Iterates the managed watches.
     *
     * @return  a watch iterator.
     */
    Iterator<Watch> watchIterator();
}
