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
 * $Id: JavaRuntime.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import java.io.File;
import java.util.List;

/**
 * A Runtime represents a Java Runtime Environment, in terms of its location
 * on the local machine, and other related information.
 *
 * @author Nathan Fiedler
 */
public interface JavaRuntime extends Cloneable {

    /**
     * Creates a copy of this runtime, replicating the deep structure so
     * it is suitable for modification without affecting the original.
     *
     * @return  duplicate of this runtime.
     */
    JavaRuntime clone();

    /**
     * Use the base directory and executable name to create a file that
     * represents the executable. The file is guaranteed to exist, but
     * no other validation is performed.
     *
     * @param  base  base directory to look in.
     * @param  exec  name of executable to find.
     * @return  runtime executable file, or null if not found.
     */
    File findExecutable(File base, String exec);

    /**
     * Returns the path to the base directory where this runtime is installed
     * (e.g. /opt/jdk1.5.0_04).
     *
     * @return  runtime base directory.
     */
    String getBase();

    /**
     * Returns the name of the runtime executable (e.g. java.exe).
     *
     * @return  name of runtime executable.
     */
    String getExec();

    /**
     * Return a descriptive name for this runtime. This is generally
     * composed of data given by the runtime itself in the form of its
     * version information.
     *
     * @return  runtime name.
     */
    String getName();

    /**
     * Returns the unique, immutable identifier for this runtime. This
     * identifier is appropriate for use in filenames for persisting
     * data that is keyed off of the runtime instance.
     *
     * @return  unique runtime identifier.
     */
    String getIdentifier();

    /**
     * Returns the zip filenames and/or directories where the Java core
     * classes source code is found (e.g. /opt/jdk/src.zip or /opt/jdk/src).
     * The source code may be in a zip file, jar file, or extracted into the
     * original directory structure on disk.
     *
     * @return  runtime source zips and/or directories.
     */
    List<String> getSources();

    /**
     * Determines if this runtime instance refers to a valid Java runtime.
     *
     * @return  true if valid runtime, false otherwise.
     */
    boolean isValid();

    /**
     * Sets the base directory where this runtime is installed.
     *
     * @param  base  runtime base directory.
     */
    void setBase(String base);

    /**
     * Sets the name of the runtime executable.
     *
     * @param  exec  name of runtime executable.
     */
    void setExec(String exec);

    /**
     * Sets the unique, immutable identifier for this runtime. Only the
     * RuntimeManager implementations should ever call this method.
     *
     * @param  id  unique runtime identifier.
     */
    void setIdentifier(String id);

    /**
     * Sets the zip filenames and/or directories where the Java core classes
     * source code is located.
     *
     * @param  sources  runtime source zips and/or directories.
     */
    void setSources(List<String> sources);
}
