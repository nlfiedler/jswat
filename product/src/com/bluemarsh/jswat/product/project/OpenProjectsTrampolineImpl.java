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
 * are Copyright (C) 2006-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.product.project;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

/**
 * Satisfy a requirement of the projectuiapi module that there be a
 * trampoline for open projects, even though we don't support projects.
 *
 * @author  Nathan Fiedler
 */
public class OpenProjectsTrampolineImpl implements OpenProjectsTrampoline {

    @Override
    public void closeAPI(Project[] project) {
    }

    @Override
    public Project getMainProject() {
        return null;
    }

    @Override
    public Project[] getOpenProjectsAPI() {
        return new Project[0];
    }

    @Override
    public void removePropertyChangeListenerAPI(PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public void setMainProject(Project project) {
    }

    @Override
    public void addPropertyChangeListenerAPI(PropertyChangeListener arg0, Object arg1) {
    }

    @Override
    public Future<Project[]> openProjectsAPI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void openAPI(Project[] arg0, boolean arg1, boolean arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
