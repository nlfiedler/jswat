/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Install.java 15 2007-06-03 00:01:17Z nfiedler $
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
        CommandParser parser = (CommandParser) Lookup.getDefault().lookup(
                CommandParser.class);
        parser.loadSettings();
    }

    /**
     * The IDE is exiting, shut down now.
     */
    public void close() {
        CommandParser parser = (CommandParser) Lookup.getDefault().lookup(
                CommandParser.class);
        parser.saveSettings();
    }
}
