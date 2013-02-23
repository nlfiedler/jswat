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
 * are Copyright (C) 2013. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.nbcore.path;

import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;

/**
 * Class {@code JSwatClassPathProvider} implements the {@code ClassPathProvider}
 * interface to support the syntax highlighting of Java source files. The use of
 * the GlobalPathRegistry in {@code NetBeansPathManager} is the other half of
 * the overall solution.
 * <p/>
 * @author Nathan Fiedler
 */
public class JSwatClassPathProvider implements ClassPathProvider {

    /**
     * Creates a new instance of JSwatClassPathProvider.
     */
    public JSwatClassPathProvider() {
        // Force the path manager to load so the path registry is set up.
        PathProvider.getPathManager(SessionProvider.getCurrentSession());
    }

    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        Set<ClassPath> paths = gpr.getPaths(type);
        if (paths != null) {
            for (ClassPath path : paths) {
                if (path.contains(fo)) {
                    return path;
                }
            }
        }
        return null;
    }
}
