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
 * are Copyright (C) 2006-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.watch;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Class WatchProvider manages a set of WatchManager instances, one for each
 * unique Session passed to the <code>getWatchManager()</code> method.
 *
 * @author Nathan Fiedler
 */
public class WatchProvider {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            WatchProvider.class.getName());
    /** Used to control access to the instance maps. */
    private static final Object mapsLock;
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
            factory = Lookup.getDefault().lookup(WatchFactory.class);
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
                WatchManager prototype = Lookup.getDefault().lookup(WatchManager.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class<? extends WatchManager> protoClass = prototype.getClass();
                try {
                    inst = protoClass.newInstance();
                } catch (InstantiationException ie) {
                    logger.log(Level.SEVERE, null, ie);
                    return null;
                } catch (IllegalAccessException iae) {
                    logger.log(Level.SEVERE, null, iae);
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
