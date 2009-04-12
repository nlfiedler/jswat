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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core;

import com.bluemarsh.jswat.core.util.Strings;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages the core debugger options. Delegates to NbPreferences.
 *
 * @author  Nathan Fiedler
 */
public class CoreSettings {
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
    /** Singleton instance. */
    private static CoreSettings theInstance;
    /** The Preferences instance where our settings are stored. */
    private Preferences preferences;

    /**
     * Singleton constructor.
     */
    private CoreSettings() {
        PlatformService platform = PlatformProvider.getPlatformService();
        preferences = platform.getPreferences(this.getClass());
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the instance.
     */
    public static synchronized CoreSettings getDefault() {
        if (theInstance == null) {
            theInstance = new CoreSettings();
        }
        return theInstance;
    }

    /**
     * Retrieves the attaching connector timeout value.
     *
     * @return  connection timeout (in milliseconds).
     */
    public int getConnectionTimeout() {
        return preferences.getInt(PROP_CONNECT_TIMEOUT, 30000);
    }

    /**
     * Retrieves the method invocation timeout value.
     *
     * @return  invocation timeout (in milliseconds).
     */
    public int getInvocationTimeout() {
        return preferences.getInt(PROP_INVOKE_TIMEOUT, 5000);
    }

    /**
     * Retrieves the show all threads value.
     *
     * @return  true if showing all threads, false to hide weird threads.
     */
    public boolean getShowAllThreads() {
        return preferences.getBoolean(PROP_SHOW_ALL_THREADS, false);
    }

    /**
     * Retrieves the show hidden files value.
     *
     * @return  true if showing hidden files, false otherwise.
     */
    public boolean getShowHiddenFiles() {
        return preferences.getBoolean(PROP_SHOW_HIDDEN_FILES, false);
    }

    /**
     * Retrieves the skip synthetics value.
     *
     * @return  true if skipping synthetics, false otherwise.
     */
    public boolean getSkipSynthetics() {
        return preferences.getBoolean(PROP_SKIP_SYNTHETICS, false);
    }

    /**
     * Retrieves the source file extension value.
     *
     * @return  the file extension for locating source files.
     */
    public String getSourceExtension() {
        return preferences.get(PROP_SOURCE_EXTENSION, ".java");
    }

    /**
     * Retrieves the single-stepping exclusions value.
     *
     * @return  list of packages to skip while single-stepping;
     *          empty list if there are no excludes.
     */
    public List<String> getSteppingExcludes() {
        String exc = preferences.get(PROP_STEPPING_EXCLUDES, "");
        return Strings.stringToList(exc, ",");
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
        preferences.putInt(PROP_CONNECT_TIMEOUT, timeout);
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
        preferences.putInt(PROP_INVOKE_TIMEOUT, timeout);
    }

    /**
     * Sets the show all threads value.
     *
     * @param  show  true to show all threads, false to hide weird threads.
     */
    public void setShowAllThreads(boolean show) {
        preferences.putBoolean(PROP_SHOW_ALL_THREADS, show);
    }

    /**
     * Sets the show hidden files value.
     *
     * @param  show  true to show hidden files, false otherwise.
     */
    public void setShowHiddenFiles(boolean show) {
        preferences.putBoolean(PROP_SHOW_HIDDEN_FILES, show);
    }

    /**
     * Sets the skip synthetics value.
     *
     * @param  skip  true to skip synthetics, false otherwise.
     */
    public void setSkipSynthetics(boolean skip) {
        preferences.putBoolean(PROP_SKIP_SYNTHETICS, skip);
    }

    /**
     * Sets the source file extension value.
     *
     * @param  extension  default file extension for source files.
     */
    public void setSourceExtension(String extension) {
        if (extension == null || extension.trim().length() == 0) {
            throw new IllegalArgumentException("extension cannot be blank");
        }
        // Ensure extension includes the leading period.
        if (extension.charAt(0) != '.') {
            extension = '.' + extension;
        }
        preferences.put(PROP_SOURCE_EXTENSION, extension);
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
        String exc = Strings.listToString(excludes, ",");
        preferences.put(PROP_STEPPING_EXCLUDES, exc);
    }
}
