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
 * $Id: DispatcherProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Class DispatcherProvider manages a set of Dispatcher instances, one for
 * each unique Session passed to the <code>getDispatcher()</code> method.
 *
 * @author Nathan Fiedler
 */
public class DispatcherProvider {
    /** Map of Dispatcher instances, keyed by Session instance. */
    private static Map<Session, Dispatcher> instanceMap;

    static {
        instanceMap = new HashMap<Session, Dispatcher>();
    }

    /**
     * Creates a new instance of PathProvider.
     */
    private DispatcherProvider() {
    }

    /**
     * Retrieve the Dispatcher instance for the given Session, creating
     * one if necessary.
     *
     * @param  session  Session for which to get Dispatcher.
     * @return  Dispatcher instance.
     */
    public static Dispatcher getDispatcher(Session session) {
        synchronized (instanceMap) {
            Dispatcher inst = instanceMap.get(session);
            if (inst == null) {
                // Perform lookup to find a Dispatcher instance.
                Dispatcher prototype = (Dispatcher)
                    Lookup.getDefault().lookup(Dispatcher.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class protoClass = prototype.getClass();
                try {
                    inst = (Dispatcher) protoClass.newInstance();
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
