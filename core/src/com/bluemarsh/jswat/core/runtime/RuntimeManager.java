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

import java.util.Iterator;

/**
 * A RuntimeManager handles the creation and persistence of Runtime instances.
 * Concrete implementations of this interface are acquired from the
 * <code>RuntimeProvider</code> class.
 *
 * @author Nathan Fiedler
 */
public interface RuntimeManager {

    /**
     * Add the given runtime to this manager's list.
     *
     * @param  runtime  runtime to be added.
     */
    void add(JavaRuntime runtime);

    /**
     * Add an event listener to this manager object.
     *
     * @param  listener  new listener to add to notification list.
     */
    void addRuntimeListener(RuntimeListener listener);

    /**
     * Find the runtime with the given base path. If multiple runtimes
     * exist with the same base path, and one is valid, that one will be
     * returned. Otherwise, if only one, invalid runtime exists, it will
     * be returned. The caller may then attempt to fix the runtime.
     *
     * @param  base  base directory of runtime to locate.
     * @return  matching runtime instance, or null if not found.
     */
    JavaRuntime findByBase(String base);

    /**
     * Find the runtime with the given identifier.
     *
     * @param  id  runtime identifier.
     * @return  matching runtime instance, or null if not found.
     */
    JavaRuntime findById(String id);

    /**
     * Generate a new, unique runtime identifier.
     *
     * @return  new runtime identifier.
     */
    String generateIdentifier();

    /**
     * Create an iterator of the currently available runtimes.
     *
     * @return  runtime iterator.
     */
    Iterator<JavaRuntime> iterateRuntimes();

    /**
     * Load all of the runtimes previously saved to persistent storage.
     *
     * @param  factory  RuntimeFactory with which to create instances.
     */
    void loadRuntimes(RuntimeFactory factory);

    /**
     * Remove the given runtime, deleting any stored data.
     *
     * @param  runtime  runtime to be removed.
     */
    void remove(JavaRuntime runtime);

    /**
     * Remove an event listener from this manager object.
     *
     * @param  listener  listener to remove from notification list.
     */
    void removeRuntimeListener(RuntimeListener listener);

    /**
     * Save all of the runtimes to persistent storage.
     */
    void saveRuntimes();
}
