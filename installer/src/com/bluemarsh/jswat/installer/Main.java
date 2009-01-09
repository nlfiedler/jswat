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
 * The Original Software is JSwat Installer. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.installer;

import java.awt.HeadlessException;

/**
 * Class Main checks the version of the Java platform and opens the installer
 * window, which drives the rest of the installation process.
 *
 * @author Nathan Fiedler
 */
public class Main {

    /**
     * Starts the installer.
     *
     * @param  args  the command line arguments.
     */
    public static void main(String[] args) {
        Controller control = Controller.getDefault();
        try {
            control.start();
        } catch (HeadlessException he) {
            System.out.println(Bundle.getString("ERR_NoGraphics", he.toString()));
        }
    }
}
