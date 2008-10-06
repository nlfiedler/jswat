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
 * $Id: DefaultRuntime.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import com.bluemarsh.jswat.core.util.Processes;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.openide.ErrorManager;

/**
 * The default implementation of a JavaRuntime.
 *
 * @author Nathan Fiedler
 */
public class DefaultRuntime implements JavaRuntime {
    /** Unique identifier for this instance. */
    private String runtimeIdentifier;
    /** Base directory of this runtime. */
    private File baseDirectory;
    /** Contains the source code for the Java core classes. */
    private List<String> sourceCodes;
    /** Name of runtime executable. */
    private String runtimeExec;
    /** Name of this runtime; not to be persisted. */
    private transient String cachedName;

    public File findExecutable(File base, String exec) {
        if (base == null) {
            return null;
        }
        if (exec == null) {
            return null;
        }
        File bindir = new File(base, "bin");
        if (!bindir.exists()) {
            // Fall back to the base directory in the hopes that it
            // will suffice for this particular runtime.
            bindir = base;
        }
        File execf = new File(bindir, exec);
        if (!execf.exists()) {
            return null;
        }
        return execf;
    }

    public String getBase() {
        if (baseDirectory == null) {
            return null;
        } else {
            return baseDirectory.getAbsolutePath();
        }
    }

    public String getExec() {
        return runtimeExec;
    }

    public String getName() {
        //
        // Typical output from the Sun JVM:
        //
        // java version "1.5.0_01"
        // Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_01-b08)
        // Java HotSpot(TM) Client VM (build 1.5.0_01-b08, mixed mode, sharing)
        //
        if (cachedName == null) {
            File exec = findExecutable(baseDirectory, runtimeExec);
            if (exec != null && exec.exists()) {
                String[] cmd = new String[] {
                    exec.getAbsolutePath(), "-version"
                };
                java.lang.Runtime rt = java.lang.Runtime.getRuntime();
                try {
                    Process proc = rt.exec(cmd);
                    cachedName = Processes.waitFor(proc);
                    // Take the first line of output as the runtime name.
                    String lt = System.getProperty("line.separator");
                    int idx = cachedName.indexOf(lt);
                    if (idx > -1) {
                        cachedName = cachedName.substring(0, idx);
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }
        return cachedName;
    }

    public String getIdentifier() {
        return runtimeIdentifier;
    }

    public List<String> getSources() {
        return sourceCodes;
    }

    public boolean isValid() {
        if (baseDirectory != null && runtimeExec != null) {
            // Try to find the executable.
            File exec = findExecutable(baseDirectory, runtimeExec);
            return exec != null && exec.exists();
        }
        return false;
    }

    public void setBase(String base) {
        if (base == null || base.length() == 0) {
            throw new IllegalArgumentException("invalid base value");
        }
        // Must leave validation for manager to perform, otherwise we mess
        // up the deserialization process.
        baseDirectory = new File(base);
    }

    public void setExec(String exec) {
        if (exec == null || exec.length() == 0) {
            throw new IllegalArgumentException("invalid exec value");
        }
        runtimeExec = exec;
        // Must leave validation for manager to perform, otherwise we mess
        // up the deserialization process.
    }

    public void setIdentifier(String id) {
        runtimeIdentifier = id;
    }

    public void setSources(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            sourceCodes = null;
        } else {
            sourceCodes = sources;
        }
    }
}
