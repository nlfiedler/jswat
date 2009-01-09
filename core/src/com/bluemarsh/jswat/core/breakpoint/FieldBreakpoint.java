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
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.Field;

/**
 * A FieldBreakpoint applies only to a particular field of a class. This
 * is typically combined with other types of a breakpoints, such as
 * WatchBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public interface FieldBreakpoint extends Breakpoint {
    /** Name of 'field' property. */
    public static final String PROP_FIELD = "field";

    /**
     * Returns the field this breakpoint is watching.
     *
     * @return  field.
     */
    public Field getField();

    /**
     * Sets the field this breakpoint should watch.
     *
     * @param  field  the field to watch.
     */
    public void setField(Field field);
}
