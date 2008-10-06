/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Findable.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.components;

/**
 * Those things which can be searched using the FindPanel must provide an
 * implementation of this interface.
 *
 * @author Nathan Fiedler
 */
public interface Findable {

    /**
     * The search is being dismissed by the user, remove the search interface.
     */
    void dismiss();

    /**
     * Locate the next matching element.
     *
     * @param  query  the query string to find.
     * @return  true if found, false otherwise.
     */
    boolean findNext(String query);

    /**
     * Locate the previous matching element.
     *
     * @param  query  the query string to find.
     * @return  true if found, false otherwise.
     */
    boolean findPrevious(String query);
}
