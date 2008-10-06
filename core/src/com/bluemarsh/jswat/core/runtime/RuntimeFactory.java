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
 * $Id: RuntimeFactory.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

/**
 * A RuntimeFactory constructs JavaRuntime instances. A concrete
 * implementation can be acquired from the <code>RuntimeProvider</code>
 * class.
 *
 * @author Nathan Fiedler
 */
public interface RuntimeFactory {

    /**
     * Create a new runtime whose identifier is the one given, but which
     * does not have a base directory defined. The runtime will need to
     * be initialized by the client.
     *
     * @param  id  unique identifier to assign to the new runtime.
     * @return  newly created runtime.
     */
    JavaRuntime createRuntime(String id);

    /**
     * Create a new runtime whose base directory is at the given path.
     *
     * @param  base  base directory containing the runtime.
     * @param  id    unique identifier to assign to the new runtime.
     * @return  newly created runtime.
     */
    JavaRuntime createRuntime(String base, String id);

    /**
     * Determine the canonical path to the Java runtime on which this
     * application is running.
     *
     * @return  path to the Java runtime at 'java.home'.
     */
    String getDefaultBase();
}
