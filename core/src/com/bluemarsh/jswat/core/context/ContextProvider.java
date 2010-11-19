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
package com.bluemarsh.jswat.core.context;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Class ContextProvider manages a set of DebuggingContext instances, one
 * for each unique Session passed to the <code>getDebuggingContext()</code>
 * method.
 *
 * @author Nathan Fiedler
 */
public class ContextProvider {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            ContextProvider.class.getName());
    /** Map of DebuggingContext instances, keyed by Session instance. */
    private static final Map<Session, DebuggingContext> instanceMap;

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
                DebuggingContext prototype =
                        Lookup.getDefault().lookup(DebuggingContext.class);
                // Using this prototype, construct a new instance for the
                // given Session, rather than sharing the single instance.
                Class<? extends DebuggingContext> protoClass = prototype.getClass();
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
                if (inst instanceof SessionListener) {
                    session.addSessionListener((SessionListener) inst);
                }
            }
            return inst;
        }
    }
}
