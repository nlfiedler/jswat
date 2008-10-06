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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InstanceWatchBreakpoint.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * An InstanceWatchBreakpoint is a watch breakpoint that is set to watch
 * a field within a particular instance of a class, using only references
 * to the JDI Field and ObjectReference, rather than field names and
 * class names. By its nature, this type of breakpoint is short-lived
 * and will delete itself when the session disconnects.
 *
 * @author Nathan Fiedler
 */
public interface InstanceWatchBreakpoint extends FieldBreakpoint,
        InstanceBreakpoint, WatchBreakpoint {

    /**
     * Return the name of the class containing the field being watched.
     *
     * @return  fully-qualified class name.
     */
    String getClassName();
}
