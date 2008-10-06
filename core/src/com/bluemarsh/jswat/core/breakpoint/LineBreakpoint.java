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
 * $Id: LineBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * A LineBreakpoint stops at a specific line within a particular source file.
 *
 * @author Nathan Fiedler
 */
public interface LineBreakpoint extends ResolvableBreakpoint {
    /** Name of the 'lineNumber' property. */
    public static final String PROP_LINENUMBER = "lineNumber";
    /** Name of the 'packageName' property. */
    public static final String PROP_PACKAGENAME = "packageName";
    /** Name of the 'sourceName' property. */
    public static final String PROP_SOURCENAME = "sourceName";
    /** Name of the 'URL' property. */
    public static final String PROP_URL = "URL";

    /**
     * Retrieve the line number associated with this breakpoint.
     *
     * @return  line number of breakpoint.
     */
    public int getLineNumber();

    /**
     * Returns the name of the package containing the class this breakpoint
     * is set within.
     *
     * @return  package name (may be null).
     */
    public String getPackageName();

    /**
     * Returns the name of the source file containing the class this
     * breakpoint is set within.
     *
     * @return  the source file name.
     */
    public String getSourceName();

    /**
     * Returns the URL of the file this breakpoint was set in.
     *
     * @return  unique file URL.
     */
    public String getURL();

    /**
     * Set the line number at which this breakpoint is set. This method will
     * force the breakpoint to be unresolved. It must be resolved again before
     * it will be effective. The caller should notify breakpoint listeners of
     * the change.
     *
     * @param  line  line number at this this breakpoint is set.
     */
    public void setLineNumber(int line);

    /**
     * Sets the name of the package containing the class this breakpoint
     * is set within.
     *
     * @param  pkg  package name (may be null).
     */
    public void setPackageName(String pkg);

    /**
     * Sets the name of the source file containing the class this
     * breakpoint is set within.
     *
     * @param  name  the source file name.
     */
    public void setSourceName(String name);

    /**
     * Sets the URL of the file this breakpoint is set in.
     *
     * @param  url  unique file URL.
     */
    public void setURL(String url);
}
