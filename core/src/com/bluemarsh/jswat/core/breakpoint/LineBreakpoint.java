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
 * $Id: LineBreakpoint.java 6 2007-05-16 07:14:24Z nfiedler $
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
