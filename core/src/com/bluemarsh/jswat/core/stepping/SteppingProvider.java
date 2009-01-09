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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.stepping;

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
                Stepper prototype = Lookup.getDefault().lookup(Stepper.class);
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
            }
            return inst;
        }
    }
}
