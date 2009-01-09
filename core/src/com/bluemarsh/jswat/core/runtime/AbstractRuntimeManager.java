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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.runtime;

import java.io.File;
import java.util.Iterator;

/**
 * AbstractRuntimeManager provides an abstract RuntimeManager for concrete
 * implementations to subclass.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractRuntimeManager implements RuntimeManager {
    /** The prefix for runtime identifiers. */
    protected static final String ID_PREFIX = "JRE_";
    /** List of runtime listeners. */
    private RuntimeListener runtimeListeners;

    public void addRuntimeListener(RuntimeListener listener) {
        if (listener != null) {
            synchronized (this) {
                runtimeListeners = RuntimeEventMulticaster.add(
                        runtimeListeners, listener);
            }
        }
    }

    public JavaRuntime findByBase(String base) {
        JavaRuntime rt = null;
        Iterator<JavaRuntime> iter = iterateRuntimes();
        File basedir = new File(base);
        while (iter.hasNext()) {
            // See if this runtime's base is what we're looking for.
            JavaRuntime r = iter.next();
            File bd = new File(r.getBase());
            if (bd.equals(basedir)) {
                rt = r;
                // If this one is invalid, keep looking for a valid one.
                // If none is found, the invalid one will be returned.
                if (r.isValid()) {
                    break;
                }
            }
        }
        return rt;
    }

    public JavaRuntime findById(String id) {
        if (id != null && id.length() > 0) {
            Iterator<JavaRuntime> iter = iterateRuntimes();
            while (iter.hasNext()) {
                JavaRuntime runtime = iter.next();
                if (id.equals(runtime.getIdentifier())) {
                    return runtime;
                }
            }
        }
        return null;
    }

    /**
     * Sends the given event to all of the registered listeners.
     *
     * @param  e  event to be dispatched.
     */
    protected void fireEvent(RuntimeEvent e) {
        RuntimeListener listeners;
        synchronized (this) {
            listeners = runtimeListeners;
        }
        if (listeners != null) {
            e.getType().fireEvent(e, listeners);
        }
    }

    public String generateIdentifier() {
        Iterator<JavaRuntime> iter = iterateRuntimes();
        if (!iter.hasNext()) {
            return ID_PREFIX + '1';
        } else {
            int max = 0;
            while (iter.hasNext()) {
                JavaRuntime runtime = iter.next();
                String id = runtime.getIdentifier();
                id = id.substring(ID_PREFIX.length());
                try {
                    int i = Integer.parseInt(id);
                    if (i > max) {
                        max = i;
                    }
                } catch (NumberFormatException nfe) {
                    // This cannot happen as we generate the identifier
                    // and it will always have an integer suffix.
                }
            }
            max++;
            return ID_PREFIX + max;
        }
    }

    public void removeRuntimeListener(RuntimeListener listener) {
        if (listener != null) {
            synchronized (this) {
                runtimeListeners = RuntimeEventMulticaster.remove(
                        runtimeListeners, listener);
            }
        }
    }
}
