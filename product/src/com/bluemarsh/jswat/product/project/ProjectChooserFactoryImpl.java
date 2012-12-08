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
 * are Copyright (C) 2008-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.product.project;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;

/**
 * Satisfy requirements of the projectuiapi module. We do not support projects
 * so the implementation is empty.
 *
 * @author Nathan Fiedler
 */
public class ProjectChooserFactoryImpl implements ProjectChooserFactory {

    @Override
    public File getProjectsFolder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProjectsFolder(File file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JFileChooser createProjectChooser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public org.openide.WizardDescriptor.Panel<org.openide.WizardDescriptor> createSimpleTargetChooser(
            Project prjct, SourceGroup[] sgs,
            org.openide.WizardDescriptor.Panel<org.openide.WizardDescriptor> pnl,
            boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
