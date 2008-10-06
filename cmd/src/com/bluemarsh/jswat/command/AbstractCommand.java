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
 * $Id: AbstractCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import org.openide.util.NbBundle;

/**
 * Partial implementation of the Command interface, providing the basic
 * methods that all commands will likely want to utilize.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractCommand implements Command {

    /**
     * Creates a new instance of AbstractCommand.
     */
    public AbstractCommand() {
    }

    public String getHelp() {
        String name = getName();
        // This will load the bundle from the command's package, whatever
        // that may be (i.e. not necessary 'this' package).
        return NbBundle.getMessage(getClass(), name + "_Help");
    }

    public String getDescription() {
        String name = getName();
        // This will load the bundle from the command's package, whatever
        // that may be (i.e. not necessary 'this' package).
        return NbBundle.getMessage(getClass(), name + "_Description");
    }

    public boolean requiresArguments() {
        return false;
    }

    public boolean requiresDebuggee() {
        return false;
    }

    public boolean requiresThread() {
        return false;
    }
}
