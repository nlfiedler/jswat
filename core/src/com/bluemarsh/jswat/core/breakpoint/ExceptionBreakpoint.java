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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

/**
 * An ExceptionBreakpoint stops when a named exception is thrown.
 *
 * @author Nathan Fiedler
 */
public interface ExceptionBreakpoint extends ResolvableBreakpoint {

    /** Name of 'stopOnCaught' property. */
    String PROP_STOPONCAUGHT = "stopOnCaught";
    /** Name of 'stopOnUncaught' property. */
    String PROP_STOPONUNCAUGHT = "stopOnUncaught";

    /**
     * Returns the stop-on-caught status.
     *
     * @return  true if stopping when caught exceptions are thrown.
     */
    boolean getStopOnCaught();

    /**
     * Returns the stop-on-uncaught status.
     *
     * @return  true if stopping when uncaught exceptions are thrown.
     */
    boolean getStopOnUncaught();

    /**
     * Sets the stop-on-caught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when caught exceptions are thrown.
     */
    void setStopOnCaught(boolean stop);

    /**
     * Sets the stop-on-uncaught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when uncaught exceptions are thrown.
     */
    void setStopOnUncaught(boolean stop);
}
