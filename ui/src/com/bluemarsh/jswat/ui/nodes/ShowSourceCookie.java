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
 * $Id: ShowSourceCookie.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import org.openide.nodes.Node.Cookie;

/**
 * Cookie for displaying the source for an object.
 *
 * @author Nathan Fiedler
 */
public interface ShowSourceCookie extends Cookie {

    /**
     * Indicates if this cookie is able to show source.
     *
     * @return  true if source available, false otherwise.
     */
    boolean canShowSource();

    /**
     * Displays the source code for the corresponding node.
     */
    void showSource();
}
