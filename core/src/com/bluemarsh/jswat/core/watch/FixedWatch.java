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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: FixedWatch.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import com.sun.jdi.ObjectReference;

/**
 * A FixedWatch represents a watch based on a fixed object.
 *
 * @author Nathan Fiedler
 */
public interface FixedWatch extends Watch {

    /**
     * Retrieves the object reference to be displayed.
     *
     * @return  watched object.
     */
    ObjectReference getObjectReference();

    /**
     * Sets the object reference to be displayed.
     *
     * @param  obj  object to be watched.
     */
    void setObjectReference(ObjectReference obj);
}
