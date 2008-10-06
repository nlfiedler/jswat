/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License 
 * Version 1.0 (the "License"); you may not use this file except in 
 * compliance with the License. A copy of the License is available at 
 * http://www.sun.com/
 *
 * The Original Code is JSwat Installer. The Initial Developer of the 
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Main.java 15 2007-06-03 00:01:17Z nfiedler $
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
