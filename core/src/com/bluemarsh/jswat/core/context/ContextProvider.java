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
 * $Id: ContextProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.context;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Class ContextProvider manages a set of DebuggingContext instances, one
 * for each unique Session passed to the <code>getDebuggingContext()</code>
 * method.
 *
 * @author Nathan Fiedler
 */
public class ContextProvider {
    /** Map of DebuggingContext instances, keyed by Session instance. */
    private static Map<Session, DebuggingContext> instanceMap;

    static {
        instanceMap = new HashMap<Session, DebuggingContext>();
    }

    /**
     * Creates a new instance of PathProvider.
     */
    private ContextProvider() {
    }

    /**
     * Retrieve the DebuggingContext instance for the given Session, creating
     * one if necessary.
     *
     * @param  session  Session for which to get DebuggingContext.
     * @return  DebuggingContext instance.
     */
    public static DebuggingContext getContext(Session session) {
        synchronized (instanceMap) {
            DebuggingContext inst = instanceMap.get(session);
            if (inst == null) {
                // Perform lookup to find a DebuggingContext instance.
                DebuggingContext prototype = (DebuggingContext)
                    Lookup.getDefault().lookup(DebuggingContext.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class protoClass = prototype.getClass();
                try {
                    inst = (DebuggingContext) protoClass.newInstance();
                } catch (InstantiationException ie) {
                    ErrorManager.getDefault().notify(ie);
                    return null;
                } catch (IllegalAccessException iae) {
                    ErrorManager.getDefault().notify(iae);
                    return null;
                }
                instanceMap.put(session, inst);
                if (inst instanceof SessionListener) {
                    session.addSessionListener((SessionListener) inst);
                }
            }
            return inst;
        }
    }
}
