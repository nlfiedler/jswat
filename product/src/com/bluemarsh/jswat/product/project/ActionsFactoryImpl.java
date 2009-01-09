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
 * are Copyright (C) 2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.product.project;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.project.uiapi.ActionsFactory;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;

/**
 * Satisfy requirements of the projectuiapi module. We do not support
 * projects so the implementation is empty.
 *
 * @author Nathan Fiedler
 */
public class ActionsFactoryImpl implements ActionsFactory {

    public Action setAsMainProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action customizeProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action openSubprojectsAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action closeProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action newFileAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action deleteProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action copyProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action moveProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action newProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action projectCommandAction(String arg0, String arg1, Icon arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action projectSensitiveAction(ProjectActionPerformer arg0, String arg1, Icon arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action mainProjectCommandAction(String arg0, String arg1, Icon arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action mainProjectSensitiveAction(ProjectActionPerformer arg0, String arg1, Icon arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action fileCommandAction(String arg0, String arg1, Icon arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action renameProjectAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Action setProjectConfigurationAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
