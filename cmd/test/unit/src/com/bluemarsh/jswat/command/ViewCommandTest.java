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
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the CommandParser class.
 */
public class ViewCommandTest {

    @Test
    public void testViewCommand() {
        // Make sure there is source code for the JDK core classes.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        String base = System.getProperty("java.home");
        JavaRuntime rt = rm.findByBase(base);
        if (rt == null) {
            RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
            rt = rf.createRuntime(base, rm.generateIdentifier());
        }
        assertNotNull(rt);
        List<String> sources = rt.getSources();
        assertNotNull(sources);
        assertTrue(sources.size() > 0);

        // Change the source path to include the JDK source.
        Session session = SessionProvider.getCurrentSession();
        PathManager pm = PathProvider.getPathManager(session);
        pm.setSourcePath(sources);

        // Try viewing source from one of the core classes.
        CommandParser parser = new DefaultCommandParser();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        parser.setOutput(pw);
        try {
            parser.parseInput("view java.lang.String");
        } catch (CommandException ce) {
            fail(ce.toString());
        }
        assertTrue(sw.toString().length() > 0);

        // Test using a relative file path in the local format.
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        String cmd = String.format("view %s%c%s%c%s", "java",
                File.separatorChar, "lang", File.separatorChar, "String.java");
        parser.setOutput(pw);
        try {
            parser.parseInput(cmd);
        } catch (CommandException ce) {
            fail(ce.toString());
        }
        assertTrue(sw.toString().length() > 0);
    }
}
