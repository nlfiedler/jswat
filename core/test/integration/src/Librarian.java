/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Librarian.java 15 2007-06-03 00:01:17Z nfiedler $
 */

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;

/**
 * Uses classes in an external library, which requires that the classpath
 * be set appropriately for the NetBeans editor to not show error stripes.
 * Useful for testing the path manager registration of ClassPath.COMPILE.
 *
 * @author Nathan Fiedler
 */
public class Librarian {

    /**
     * @param  args  command-line arguments.
     */
    public static void main(String[] args) {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        System.out.println("version: " + vmm.majorInterfaceVersion() + '.'
                + vmm.minorInterfaceVersion());
    }
}
