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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ConsoleBreakpointFactory.java 137 2009-04-29 00:05:39Z nathanfiedler $
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.breakpoint.DefaultLineBreakpoint;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.Location;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import org.openide.util.NbBundle;

/**
 * Console-mode implementation of
 * {@link com.bluemarsh.jswat.core.breakpoint.LineBreakpoint}.
 * Emits JDB-compatible output if requested.
 *
 * @author  Steve Yegge
 */
public class ConsoleLineBreakpoint extends DefaultLineBreakpoint {

    @Override
    public String describe(Event e) {
        return Main.emulateJDB()
                ? generateJDBDescription(e)
                : super.describe(e);
    }

    // Emulate JDB and show fully qualified classname.  Sadly,
    // the superclass throws away all the information we could
    // have used -- class name, package name, full path.  Argh.
    // It's all packed into the "url" field, so we have to
    // unpack it.
    @Override
    public String getDescription() {
        if (!Main.emulateJDB() || url == null) {
            return super.getDescription();
        }
        String path = url;
        String typename = classnameFromUrl(url);
        String prefix = "file://root/";
        if (url.startsWith(prefix)) {
            path = url.substring(prefix.length());
        } else if (url.startsWith("/")) {
            path = url.substring(1);
        }
        if (path.endsWith(".java")) {
            path = path.substring(0, path.length() - ".java".length());
        }
        path = path.replace('/', '.');
        return NbBundle.getMessage(ConsoleLineBreakpoint.class,
                "Line.description", path, lineNumber);
    }

    /**
     * Parse the internal {@code LineBreakpoint} {@code url} field
     * to retrieve the fully qualified type name.
     * @param url e.g. "file://root/com/bluemarsh/jswat/console/Main.java"
     * @return a typename such as {@code com.bluemarsh.jswat.console.Main}
     * Returns {@code null} if we could not parse out a type.
     */
    public static String classnameFromUrl(String url) {
        String prefix = "file://root/";
        if (!url.startsWith(prefix)) {
            return null;
        }
        String result = url.substring(prefix.length());
        if (result.endsWith(".java")) {
            result = result.substring(0, result.length() - ".java".length());
        }
        return result.replace('/', '.');
    }

    /**
     * Generate a "Breakpoint hit" description in the exact format
     * that JDB emits.
     */
    private String generateJDBDescription(Event e) {
        if (!(e instanceof LocatableEvent)) {
            return generateFallbackJDBDesc(e);
        }
        LocatableEvent le = (LocatableEvent) e;
        Location loc = le.location();
        if (loc == null) {
            return generateFallbackJDBDesc(e);
        }

        try {
            String type = loc.declaringType().name();
            String method = loc.method().name();
            // We could print the method arg types with
            //    Strings.listToString(loc.method().argumentTypeNames())
            // However, JDB apparently doesn't emit the arg types, and Emacs
            // gets confused if you include them.

            String desc = type + "." + method + "()";

            String line = String.valueOf(loc.lineNumber());
            String thread = Threads.getIdentifier(le.thread());

            return NbBundle.getMessage(ConsoleLineBreakpoint.class,
                    "Line.description.stop", thread, desc, line);
        } catch (Exception x) {
            return generateFallbackJDBDesc(e);
        }
    }

    /**
     * Generate a description that should at least satisfy Emacs gud-jdb,
     * which only cares about the classname and line number.
     */
    private String generateFallbackJDBDesc(Event e) {
        // I should try harder to get reasonable values from the event.
        String classDesc = sourceName;
        if (classDesc == null) {
            classDesc = "";
        }
        // strip extension
        if (classDesc.endsWith(".java")) {
            classDesc = classDesc.substring(0, classDesc.length() - 5);
        }
        // convert to qualified name
        classDesc = classDesc.replace('/', '.');

        // Emacs looks for "\.[a-zA-Z0-9$_<>(),]+" -- just append anything
        // human-readable that matches this pattern, and Emacs will skip it
        // and move on to match the line=XXX field.
        classDesc += ".<unknown_method>()";

        String thread = "???";  // should try to get it from e

        return NbBundle.getMessage(ConsoleLineBreakpoint.class,
                "Line.description.stop", thread, classDesc, lineNumber);
    }
}
