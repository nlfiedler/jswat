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
 * $Id: WatchProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Class WatchProvider manages a set of WatchManager instances, one for each
 * unique Session passed to the <code>getWatchManager()</code> method.
 *
 * @author Nathan Fiedler
 */
public class WatchProvider {
    /** Used to control access to the instance maps. */
    private static Object mapsLock;
    /** Map of WatchManager instances, keyed by Session instance. */
    private static Map<Session, WatchManager> instanceMap;
    /** Map of Session instances, keyed by WatchManager instance. */
    private static Map<WatchManager, Session> reverseMap;
    /** The WatchFactory instance, if it has already been retrieved. */
    private static WatchFactory factory;

    static {
        mapsLock = new Object();
        instanceMap = new HashMap<Session, WatchManager>();
        reverseMap = new HashMap<WatchManager, Session>();
    }

    /**
     * Creates a new instance of WatchProvider.
     */
    private WatchProvider() {
    }

    /**
     * Retrieve the WatchFactory instance, creating one if necessary.
     *
     * @return  WatchFactory instance.
     */
    public static synchronized WatchFactory getWatchFactory() {
        if (factory == null) {
            // Perform lookup to find a WatchFactory instance.
            factory = (WatchFactory) Lookup.getDefault().lookup(
                    WatchFactory.class);
        }
        return factory;
    }

    /**
     * Retrieve the WatchManager instance for the given Session, creating
     * one if necessary.
     *
     * @param  session  Session for which to get WatchManager.
     * @return  watch manager instance.
     */
    public static WatchManager getWatchManager(Session session) {
        synchronized (mapsLock) {
            WatchManager inst = instanceMap.get(session);
            if (inst == null) {
                // Perform lookup to find a WatchManager instance.
                WatchManager prototype = (WatchManager)
                    Lookup.getDefault().lookup(WatchManager.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class protoClass = prototype.getClass();
                try {
                    inst = (WatchManager) protoClass.newInstance();
                } catch (InstantiationException ie) {
                    ErrorManager.getDefault().notify(ie);
                    return null;
                } catch (IllegalAccessException iae) {
                    ErrorManager.getDefault().notify(iae);
                    return null;
                }
                instanceMap.put(session, inst);
                reverseMap.put(inst, session);
                if (inst instanceof SessionListener) {
                    session.addSessionListener((SessionListener) inst);
                }
            }
            return inst;
        }
    }

    /**
     * Retrieve the Session instance associated with the given WatchManager.
     *
     * @param  wm  WatchManager for which to find Session.
     * @return  Session, or null if none is mapped to the given WatchManager.
     */
    public static Session getSession(WatchManager wm) {
        synchronized (mapsLock) {
            return reverseMap.get(wm);
        }
    }
}
