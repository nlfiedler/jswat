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
 * $Id: RuntimeFactory.java 6 2007-05-16 07:14:24Z nfiedler $
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
