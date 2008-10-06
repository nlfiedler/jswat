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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BeepMonitor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import java.awt.Toolkit;

/**
 * Produces a beep sound.
 *
 * @author  Nathan Fiedler
 */
public class BeepMonitor implements Monitor {
    /** The instance of this class. */
    private static BeepMonitor theInstance;

    static {
        theInstance = new BeepMonitor();
    }

    /**
     * Default constructor for deserialization.
     */
    private BeepMonitor() {
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the singleton instance.
     */
    public static BeepMonitor getInstance() {
        return theInstance;
    }

    public void perform(BreakpointEvent event) {
        // This does not always make a sound, even though audio may be
        // configured on the system (e.g. Fedora Core Linux).
        Toolkit.getDefaultToolkit().beep();
    }

    public boolean requiresThread() {
        return false;
    }
}
