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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: FieldBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
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
