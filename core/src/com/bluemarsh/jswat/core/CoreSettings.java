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
 * are Copyright (C) 2004-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CoreSettings.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.Collections;
import java.util.List;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Manages the core debugger options.
 *
 * @author  Nathan Fiedler
 */
public class CoreSettings extends SystemOption {
    /** serial version UID - DO NOT CHANGE */
    private static final long serialVersionUID = 5094910772009622985L;
    /** Name of the connection timeout setting. */
    public static final String PROP_CONNECT_TIMEOUT = "connectionTimeout";
    /** Name of the method invocation timeout setting. */
    public static final String PROP_INVOKE_TIMEOUT = "invocationTimeout";
    /** Name of the show all threads setting. */
    public static final String PROP_SHOW_ALL_THREADS = "showAllThreads";
    /** Name of the show hidden files setting. */
    public static final String PROP_SHOW_HIDDEN_FILES = "showHiddenFiles";
    /** Name of the skip synthetics setting. */
    public static final String PROP_SKIP_SYNTHETICS = "skipSynthetics";
    /** Name of the source file extension setting. */
    public static final String PROP_SOURCE_EXTENSION = "sourceExtension";
    /** Name of the single-stepping excludes setting. */
    public static final String PROP_STEPPING_EXCLUDES = "steppingExcludes";

    /**
     * Get the display name of this system option.
     *
     * @return  display name.
     */
    public String displayName() {
        return NbBundle.getMessage(getClass(), "CTL_CoreSettings_name");
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the instance.
     */
    public static CoreSettings getDefault() {
        return (CoreSettings) findObject(CoreSettings.class, true);
    }

    /**
     * Returns the help context for these options.
     *
     * @return  help context.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Retrieves the attaching connector timeout value.
     *
     * @return  connection timeout (in milliseconds).
     */
    public int getConnectionTimeout() {
        return ((Integer) getProperty(PROP_CONNECT_TIMEOUT)).intValue();
    }

    /**
     * Retrieves the method invocation timeout value.
     *
     * @return  invocation timeout (in milliseconds).
     */
    public int getInvocationTimeout() {
        return ((Integer) getProperty(PROP_INVOKE_TIMEOUT)).intValue();
    }

    /**
     * Retrieves the show all threads value.
     *
     * @return  true if showing all threads, false to hide weird threads.
     */
    public boolean getShowAllThreads() {
        return ((Boolean) getProperty(PROP_SHOW_ALL_THREADS)).booleanValue();
    }

    /**
     * Retrieves the show hidden files value.
     *
     * @return  true if showing hidden files, false otherwise.
     */
    public boolean getShowHiddenFiles() {
        return ((Boolean) getProperty(PROP_SHOW_HIDDEN_FILES)).booleanValue();
    }

    /**
     * Retrieves the skip synthetics value.
     *
     * @return  true if skipping synthetics, false otherwise.
     */
    public boolean getSkipSynthetics() {
        return ((Boolean) getProperty(PROP_SKIP_SYNTHETICS)).booleanValue();
    }

    /**
     * Retrieves the source file extension value.
     *
     * @return  the file extension for locating source files.
     */
    public String getSourceExtension() {
        return (String) getProperty(PROP_SOURCE_EXTENSION);
    }

    /**
     * Retrieves the single-stepping exclusions value.
     *
     * @return  list of packages (type String) to skip while single-stepping;
     *          if there no excludes, the returned list is empty.
     */
    public List getSteppingExcludes() {
        return (List) getProperty(PROP_STEPPING_EXCLUDES);
    }

    protected void initialize() {
        super.initialize();
        setDefaults();
    }

    /**
     * The object implements the readExternal method to restore its contents
     * by calling the methods of DataInput for primitive types and readObject
     * for objects, strings and arrays.
     *
     * @param  in  the stream to read data from in order to restore the object.
     * @throws  IOException
     *          if I/O errors occur.
     * @throws  ClassNotFoundException
     *          if the class for an object being restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws
            IOException, ClassNotFoundException {
        super.readExternal(in);
        // Upgrade the restored instance to include the latest settings.
        setDefaults();
    }

    /**
     * Set the attaching connector timeout value.
     *
     * @param  timeout  connection timeout (in milliseconds).
     */
    public void setConnectionTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout cannot be negative");
        }
        putProperty(PROP_CONNECT_TIMEOUT, timeout, true);
    }

    /**
     * For those properties that have null values, set them to the default.
     */
    private void setDefaults() {
        if (getProperty(PROP_CONNECT_TIMEOUT) == null) {
            putProperty(PROP_CONNECT_TIMEOUT, 5000);
        }
        if (getProperty(PROP_INVOKE_TIMEOUT) == null) {
            putProperty(PROP_INVOKE_TIMEOUT, 5000);
        }
        if (getProperty(PROP_SOURCE_EXTENSION) == null) {
            putProperty(PROP_SOURCE_EXTENSION, ".java");
        }
        if (getProperty(PROP_STEPPING_EXCLUDES) == null) {
            List<String> empty = Collections.emptyList();
            putProperty(PROP_STEPPING_EXCLUDES, empty);
        }
        if (getProperty(PROP_SHOW_ALL_THREADS) == null) {
            putProperty(PROP_SHOW_ALL_THREADS, Boolean.FALSE);
        }
        if (getProperty(PROP_SHOW_HIDDEN_FILES) == null) {
            putProperty(PROP_SHOW_HIDDEN_FILES, Boolean.FALSE);
        }
        if (getProperty(PROP_SKIP_SYNTHETICS) == null) {
            putProperty(PROP_SKIP_SYNTHETICS, Boolean.FALSE);
        }
    }

    /**
     * Sets the method invocation timeout value.
     *
     * @param  timeout  invocation timeout (in milliseconds).
     */
    public void setInvocationTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout cannot be negative");
        }
        putProperty(PROP_INVOKE_TIMEOUT, timeout, true);
    }

    /**
     * Sets the show all threads value.
     *
     * @param  show  true to show all threads, false to hide weird threads.
     */
    public void setShowAllThreads(boolean show) {
        putProperty(PROP_SHOW_ALL_THREADS, Boolean.valueOf(show));
    }

    /**
     * Sets the show hidden files value.
     *
     * @param  show  true to show hidden files, false otherwise.
     */
    public void setShowHiddenFiles(boolean show) {
        putProperty(PROP_SHOW_HIDDEN_FILES, Boolean.valueOf(show));
    }

    /**
     * Sets the skip synthetics value.
     *
     * @param  skip  true to skip synthetics, false otherwise.
     */
    public void setSkipSynthetics(boolean skip) {
        putProperty(PROP_SKIP_SYNTHETICS, Boolean.valueOf(skip));
    }

    /**
     * Sets the source file extension value.
     *
     * @param  extension  default file extension for source files.
     */
    public void setSourceExtension(String extension) {
        if (extension.length() == 0) {
            throw new IllegalArgumentException("extension not specified");
        }
        if (extension.charAt(0) != '.') {
            // Add the dot if not already included.
            extension = '.' + extension;
        }
        putProperty(PROP_SOURCE_EXTENSION, extension, true);
    }

    /**
     * Sets the single-stepping exclusions value.
     *
     * @param  excludes  list of packags to skip while single-stepping;
     *                   null is converted to an empty list.
     */
    public void setSteppingExcludes(List<String> excludes) {
        if (excludes == null) {
            excludes = Collections.emptyList();
        }
        putProperty(PROP_STEPPING_EXCLUDES, excludes, true);
    }
}
