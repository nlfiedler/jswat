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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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

    @Override
    public String getHelp() {
        String name = getName();
        // This will load the bundle from the command's package, whatever
        // that may be (i.e. not necessary 'this' package).
        return NbBundle.getMessage(getClass(), name + "_Help");
    }

    @Override
    public String getDescription() {
        String name = getName();
        // This will load the bundle from the command's package, whatever
        // that may be (i.e. not necessary 'this' package).
        return NbBundle.getMessage(getClass(), name + "_Description");
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }

    @Override
    public boolean requiresDebuggee() {
        return false;
    }

    @Override
    public boolean requiresThread() {
        return false;
    }
}
