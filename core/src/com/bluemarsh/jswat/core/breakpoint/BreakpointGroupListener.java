/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointGroupListener.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import java.beans.PropertyChangeListener;

/**
 * The listener interface for receiving changes to breakpoint groups.
 *
 * @author  Nathan Fiedler
 */
public interface BreakpointGroupListener extends PropertyChangeListener {

    /**
     * An exception occurred during the processing of a group.
     *
     * @param  event  group event, provides exception and group.
     */
    void errorOccurred(BreakpointGroupEvent event);

    /**
     * Invoked when a group has been added.
     *
     * @param  event  group change event
     */
    void groupAdded(BreakpointGroupEvent event);

    /**
     * Invoked when a group has been removed.
     *
     * @param  event  group change event
     */
    void groupRemoved(BreakpointGroupEvent event);
}
