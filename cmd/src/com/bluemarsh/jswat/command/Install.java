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

package com.bluemarsh.jswat.command;

import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

/**
 * Manages the command module's lifecycle.
 *
 * @author  Nathan Fiedler
 */
public class Install extends ModuleInstall {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * The IDE is starting up.
     */
    public void restored() {
        CommandParser parser = Lookup.getDefault().lookup(CommandParser.class);
        parser.loadSettings();
    }

    /**
     * The IDE is exiting, shut down now.
     */
    public void close() {
        CommandParser parser = Lookup.getDefault().lookup(CommandParser.class);
        parser.saveSettings();
    }
}
