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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
