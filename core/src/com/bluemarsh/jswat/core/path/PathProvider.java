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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.path;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Class PathProvider manages a set of PathManager instances, one for each
 * unique Session passed to the <code>getPathManager()</code> method.
 *
 * @author Nathan Fiedler
 */
public class PathProvider {
    /** Used to control access to the instance maps. */
    private static final Object mapsLock;
    /** Map of PathManager instances, keyed by Session instance. */
    private static Map<Session, PathManager> instanceMap;
    /** Map of Session instances, keyed by PathManager instance. */
    private static Map<PathManager, Session> reverseMap;

    static {
        mapsLock = new Object();
        instanceMap = new HashMap<Session, PathManager>();
        reverseMap = new HashMap<PathManager, Session>();
    }

    /**
     * Creates a new instance of PathProvider.
     */
    private PathProvider() {
    }

    /**
     * Retrieve the PathManager instance for the given Session, creating
     * one if necessary.
     *
     * @param  session  Session for which to get PathManager.
     * @return  path manager instance.
     */
    public static PathManager getPathManager(Session session) {
        synchronized (mapsLock) {
            PathManager inst = instanceMap.get(session);
            if (inst == null) {
                // Perform lookup to find a PathManager instance.
                PathManager prototype = Lookup.getDefault().lookup(PathManager.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class protoClass = prototype.getClass();
                try {
                    inst = (PathManager) protoClass.newInstance();
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
     * Retrieve the Session instance associated with the given PathManager.
     *
     * @param  pm  PathManager for which to find Session.
     * @return  Session, or null if none is mapped to the given PathManager.
     */
    public static Session getSession(PathManager pm) {
        synchronized (mapsLock) {
            return reverseMap.get(pm);
        }
    }
}
