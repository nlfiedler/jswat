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
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.SessionHelper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the SessionManagerEvent class.
 *
 * @author Nathan Fiedler
 */
public class SessionManagerEventTest {

    @Test
    public void testGetSession() {
        Session session = SessionHelper.getSession();
        SessionManagerEvent instance = new SessionManagerEvent(
                this, session, SessionManagerEvent.Type.ADDED);
        Session result = instance.getSession();
        assertEquals(session, result);
    }

    @Test
    public void testGetType() {
        Session session = SessionHelper.getSession();
        SessionManagerEvent instance = new SessionManagerEvent(
                this, session, SessionManagerEvent.Type.ADDED);
        SessionManagerEvent.Type result = instance.getType();
        assertEquals(SessionManagerEvent.Type.ADDED, result);
    }

    @Test
    public void testToString() {
        Session session = SessionHelper.getSession();
        SessionManagerEvent instance = new SessionManagerEvent(
                this, session, SessionManagerEvent.Type.ADDED);
        String expResult = "SessionManagerEvent=[session=";
        String result = instance.toString();
        assertTrue(result.startsWith(expResult));
    }
}
