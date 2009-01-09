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

package com.bluemarsh.jswat.core.runtime;

import org.openide.util.Lookup;

/**
 * RuntimeProvider manages an instance of RuntimeFactory and RuntimeManager.
 *
 * @author Nathan Fiedler
 */
public class RuntimeProvider {
    /** The RuntimeManager instance, if it has already been retrieved. */
    private static RuntimeManager rtManager;
    /** The RuntimeFactory instance, if it has already been retrieved. */
    private static RuntimeFactory rtFactory;

    /**
     * Creates a new instance of RuntimeProvider.
     */
    private RuntimeProvider() {
    }

    /**
     * Retrieve the RuntimeFactory instance, creating one if necessary.
     *
     * @return  RuntimeFactory instance.
     */
    public static synchronized RuntimeFactory getRuntimeFactory() {
        if (rtFactory == null) {
            // Perform lookup to find a RuntimeFactory instance.
            rtFactory = Lookup.getDefault().lookup(RuntimeFactory.class);
        }
        return rtFactory;
    }

    /**
     * Retrieve the RuntimeManager instance, creating one if necessary.
     *
     * @return  RuntimeManager instance.
     */
    public static synchronized RuntimeManager getRuntimeManager() {
        if (rtManager == null) {
            // Perform lookup to find the RuntimeManager instance.
            rtManager = Lookup.getDefault().lookup(RuntimeManager.class);
            // Load the persisted runtimes.
            RuntimeFactory rf = getRuntimeFactory();
            rtManager.loadRuntimes(rf);
            // Make sure the default runtime exists.
            String base = rf.getDefaultBase();
            JavaRuntime rt = rtManager.findByBase(base);
            if (rt == null || !rt.isValid()) {
                // No matching, valid runtime, create a new one.
                String id = rtManager.generateIdentifier();
                rt = rf.createRuntime(base, id);
                rtManager.add(rt);
            }
        }
        return rtManager;
    }
}
