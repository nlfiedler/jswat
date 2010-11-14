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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Class BreakpointProvider manages a set of BreakpointManager instances, one
 * for each unique Session passed to the <code>getBreakpointManager()</code>
 * method.
 *
 * @author Nathan Fiedler
 */
public class BreakpointProvider {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            BreakpointProvider.class.getName());
    /** Used to control access to the instance maps. */
    private static final Object mapsLock;
    /** Map of BreakpointManager instances, keyed by Session instance. */
    private static Map<Session, BreakpointManager> instanceMap;
    /** Map of Session instances, keyed by BreakpointManager instance. */
    private static Map<BreakpointManager, Session> reverseMap;
    /** Map of BreakpointManager instances, keyed by their root groups. */
    private static Map<BreakpointGroup, BreakpointManager> groupMap;
    /** The BreakpointFactory instance, if it has already been retrieved. */
    private static BreakpointFactory bpFactory;

    static {
        mapsLock = new Object();
        instanceMap = new HashMap<Session, BreakpointManager>();
        reverseMap = new HashMap<BreakpointManager, Session>();
        groupMap = new HashMap<BreakpointGroup, BreakpointManager>();
    }

    /**
     * Creates a new instance of BreakpointProvider.
     */
    private BreakpointProvider() {
    }

    /**
     * Retrieve the BreakpointFactory instance, creating one if necessary.
     *
     * @return  BreakpointFactory instance.
     */
    public static synchronized BreakpointFactory getBreakpointFactory() {
        if (bpFactory == null) {
            // Perform lookup to find a BreakpointFactory instance.
            bpFactory = Lookup.getDefault().lookup(BreakpointFactory.class);
        }
        return bpFactory;
    }

    /**
     * Retrieve the BreakpointManager instance for the given Session, creating
     * one if necessary.
     *
     * @param  session  Session for which to get BreakpointManager.
     * @return  BreakpointManager instance.
     */
    public static BreakpointManager getBreakpointManager(Session session) {
        synchronized (mapsLock) {
            BreakpointManager inst = instanceMap.get(session);
            if (inst == null) {
                // Perform lookup to find the BreakpointManager instance.
                BreakpointManager prototype = Lookup.getDefault().lookup(
                        BreakpointManager.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class<? extends BreakpointManager> protoClass = prototype.getClass();
                try {
                    inst = (BreakpointManager) protoClass.newInstance();
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
                if (inst instanceof SessionManagerListener) {
                    SessionManager sm = SessionProvider.getSessionManager();
                    sm.addSessionManagerListener((SessionManagerListener) inst);
                }
                // Some breakpoint managers do not have a default group
                // until after they have become session listeners.
                BreakpointGroup group = inst.getDefaultGroup();
                groupMap.put(group, inst);
            }
            return inst;
        }
    }

    /**
     * Retrieve the BreakpointManager instance associated with the given
     * BreakpointGroup.
     *
     * @param  bg  BreakpointGroup for which to find BreakpointManager.
     * @return  BreakpointManager, or null if none is mapped to the given
     *          BreakpointGroup.
     */
    public static BreakpointManager getBreakpointManager(BreakpointGroup bg) {
        BreakpointGroup parent = bg;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        synchronized (mapsLock) {
            return groupMap.get(parent);
        }
    }

    /**
     * Retrieve the Session instance associated with the given BreakpointGroup.
     *
     * @param  bg  BreakpointGroup for which to find Session.
     * @return  Session, or null if none is mapped to the given BreakpointGroup.
     */
    public static Session getSession(BreakpointGroup bg) {
        synchronized (mapsLock) {
            BreakpointManager bm = getBreakpointManager(bg);
            return getSession(bm);
        }
    }

    /**
     * Retrieve the Session instance associated with the given BreakpointManager.
     *
     * @param  bm  BreakpointManager for which to find Session.
     * @return  Session, or null if none is mapped to the given BreakpointManager.
     */
    public static Session getSession(BreakpointManager bm) {
        synchronized (mapsLock) {
            return reverseMap.get(bm);
        }
    }
}
