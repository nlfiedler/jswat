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
 * $Id: RuntimeManager.java 15 2007-06-03 00:01:17Z nfiedler $
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
     * Find the runtime with the given base path.
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
     * Save all of the runtimes to persistent storage.
     */
    void saveRuntimes();
}
