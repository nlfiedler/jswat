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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * A WatchBreakpoint stops each time a particular field is accessed
 * or modified by the debuggee.
 *
 * @author  Nathan Fiedler
 */
public interface WatchBreakpoint extends Breakpoint {
    /** Name of 'fieldName' property. */
    public static final String PROP_FIELDNAME = "fieldName";
    /** Name of 'stopOnAccess' property. */
    public static final String PROP_STOPONACCESS = "stopOnAccess";
    /** Name of 'stopOnModify' property. */
    public static final String PROP_STOPONMODIFY = "stopOnModify";

    /**
     * Returns the name of the field being watched.
     *
     * @return  expression referring to the watched field.
     */
    public String getFieldName();

    /**
     * Returns the stop-on-access status.
     *
     * @return  true if stopping when field is accessed.
     */
    public boolean getStopOnAccess();

    /**
     * Returns the stop-on-modify status.
     *
     * @return  true if stopping when field is modified.
     */
    public boolean getStopOnModify();

    /**
     * Sets the expression referring to the field to watch.
     *
     * @param  name   expression for watched field.
     * @throws  MalformedMemberNameException
     *          if field name is invalid.
     */
    public void setFieldName(String name) throws MalformedMemberNameException;

    /**
     * Sets the stop-on-access status.
     *
     * @param  stop  true to stop when field is accessed.
     */
    public void setStopOnAccess(boolean stop);

    /**
     * Sets the stop-on-modify status.
     *
     * @param  stop  true to stop when field is modified.
     */
    public void setStopOnModify(boolean stop);
}
