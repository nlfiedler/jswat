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
 * $Id: SteppingProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.stepping;

import com.bluemarsh.jswat.core.event.Dispatcher;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.bluemarsh.jswat.core.event.DispatcherProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Class SteppingProvider manages a set of Stepper instances, one for each
 * unique Session passed to the <code>getStepper()</code> method.
 *
 * @author Nathan Fiedler
 */
public class SteppingProvider {
    /** Map of Stepper instances, keyed by Session instance. */
    private static Map<Session, Stepper> instanceMap;

    static {
        instanceMap = new HashMap<Session, Stepper>();
    }

    /**
     * Creates a new instance of PathProvider.
     */
    private SteppingProvider() {
    }

    /**
     * Retrieve the Stepper instance for the given Session, creating
     * one if necessary.
     *
     * @param  session  Session for which to get Stepper.
     * @return  Stepper instance.
     */
    public static Stepper getStepper(Session session) {
        synchronized (instanceMap) {
            Stepper inst = instanceMap.get(session);
            if (inst == null) {
                // Perform lookup to find a Stepper instance.
                Stepper prototype = (Stepper)
                    Lookup.getDefault().lookup(Stepper.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class protoClass = prototype.getClass();
                try {
                    inst = (Stepper) protoClass.newInstance();
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
                if (inst instanceof DispatcherListener) {
                    Dispatcher disp = DispatcherProvider.getDispatcher(session);
                    disp.addListener((DispatcherListener) inst);
                }
            }
            return inst;
        }
    }
}
