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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionFactory.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

/**
 * A SessionFactory constructs Session instances. A concrete implementation
 * can be acquired from the <code>SessionProvider</code> class.
 *
 * <p>It is the responsibility of the SessionFactory to call the
 * <code>init()</code> method of the new Session instances.</p>
 *
 * @author Nathan Fiedler
 */
public interface SessionFactory {

    /**
     * Create a new Session whose identifier is the one given.
     *
     * @param  id  unique identifier to assign to the new Session.
     * @return  newly created Session.
     */
    Session createSession(String id);
}
