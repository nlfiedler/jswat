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
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: $
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the PipeProvider class.
 *
 * @author Nathan Fiedler
 */
public class PipeProviderTest {

    @Test
    public void testGetPipedReader() throws IOException {
        Session session = SessionProvider.getCurrentSession();
        PipedReader pr = PipeProvider.getPipedReader(session);
        PipedWriter pw = PipeProvider.getPipedWriter(session);
        PrintWriter writer = new PrintWriter(pw);
        BufferedReader reader = new BufferedReader(pr);
        final String VALUE = "foobar";
        writer.println(VALUE);
        String result = reader.readLine();
        assertEquals(VALUE, result);
    }
}
