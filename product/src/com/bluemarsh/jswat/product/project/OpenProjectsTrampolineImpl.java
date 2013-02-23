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
 * are Copyright (C) 2006-2013. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.product.project;

import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Satisfy a requirement of the projectuiapi module that there be a trampoline
 * for open projects, even though we don't support projects.
 * <p/>
 * @author Nathan Fiedler
 */
public class OpenProjectsTrampolineImpl implements OpenProjectsTrampoline {

    /**
     * Our single fake project instance.
     */
    private static final JSwatProject theProject = new JSwatProject();

    @Override
    public void closeAPI(Project[] project) {
    }

    @Override
    public Project getMainProject() {
        return theProject;
    }

    @Override
    public Project[] getOpenProjectsAPI() {
        return new Project[]{theProject};
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
        return new Future<Project[]>() {
            @Override
            public boolean cancel(boolean bln) {
                return true;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Project[] get() throws InterruptedException, ExecutionException {
                return new Project[]{theProject};
            }

            @Override
            public Project[] get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
                return new Project[]{theProject};
            }
        };
    }

    @Override
    public void openAPI(Project[] arg0, boolean arg1, boolean arg2) {
    }

    /**
     * A fake project used to avoid null pointer exceptions in NetBeans.
     */
    private static class JSwatProject implements Project {

        @Override
        public FileObject getProjectDirectory() {
            // return a dummy path for now
            return FileUtil.getConfigRoot();
        }

        @Override
        public Lookup getLookup() {
            return Lookup.getDefault();
        }
    }
}
