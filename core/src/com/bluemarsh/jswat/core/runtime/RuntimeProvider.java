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
 * $Id: RuntimeProvider.java 15 2007-06-03 00:01:17Z nfiedler $
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
            rtFactory = (RuntimeFactory) Lookup.getDefault().lookup(RuntimeFactory.class);
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
            rtManager = (RuntimeManager) Lookup.getDefault().lookup(RuntimeManager.class);
            // Load the persisted runtimes.
            RuntimeFactory rf = getRuntimeFactory();
            rtManager.loadRuntimes(rf);
            // Make sure the default runtime exists.
            String base = rf.getDefaultBase();
            JavaRuntime rt = rtManager.findByBase(base);
            if (rt == null) {
                // No matching runtime, we must create it.
                String id = rtManager.generateIdentifier();
                rt = rf.createRuntime(base, id);
                rtManager.add(rt);
            }
        }
        return rtManager;
    }
}
