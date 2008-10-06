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
 * $Id: AbstractRuntimeManager.java 15 2007-06-03 00:01:17Z nfiedler $
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
                break;
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
}
