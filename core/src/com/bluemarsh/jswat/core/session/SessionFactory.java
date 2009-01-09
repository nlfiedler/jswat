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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
