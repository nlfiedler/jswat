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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InstallerPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.installer;

import javax.swing.JPanel;

/**
 * Defines the required behavior for an installer panel.
 *
 * @author Nathan Fiedler
 */
public abstract class InstallerPanel extends JPanel {

    /**
     * Cancel the in-progress installation and clean up.
     */
    public void cancelInstall() {
        // By default, do nothing; let subclass override if needed.
    }

    /**
     * Indicates if this panel is ready to move on to the next panel.
     * This gives the panel a chance to validate the input and prompt
     * the user indicating any problems.
     *
     * @return  true if we can move on, false to stay on this panel.
     */
    public boolean canProceed() {
        return true;
    }

    /**
     * This panel is being hidden.
     */
    public abstract void doHide();

    /**
     * This panel is being displayed. This is a good opportunity to do any
     * work required for this panel, such as retrieving information from
     * the system or installing files. In such cases, the panel should
     * invoke Controller.markBusy(true) to indicate the panel is working.
     */
    public abstract void doShow();

    /**
     * Determines the name of the panel to show if the user presses the
     * Next button at this particular moment in time.
     *
     * @return  name of panel to go forward to, or null if this is the last.
     */
    public abstract String getNext();

    /**
     * Determines the name of the panel to show if the user presses the
     * Back button at this particular moment in time.
     *
     * @return  name of panel to go back to, or null if this is the first.
     */
    public abstract String getPrevious();
}
