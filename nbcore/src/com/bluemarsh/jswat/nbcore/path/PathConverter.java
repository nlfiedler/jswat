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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nbcore.path;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utility class for converting path-related values.
 *
 * @author Nathan Fiedler
 */
public class PathConverter {

    /**
     * Creates a new instance of PathConverter.
     */
    private PathConverter() {
    }

    /**
     * Converts a path and file name to a FileObject.
     *
     * @param  path  path and file name.
     * @return  FileObject, or null if file does not exist.
     */
    public static FileObject toFileObject(String path) {
        File file = new File(path);
        if (file.exists()) {
            file = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(file);
            return fo;
        }
        return null;
    }

    /**
     * Check if the file is actually an archive, and get the root of the
     * archive as a file object. Additionally, if the archive contains a
     * superfluous root directory named "src", that directory is returned
     * (some versions of the JDK ship with a src.jar like this).
     * 
     * @param  file  file object to possibly be converted.
     * @return  the converted file object.
     */
    public static FileObject convertToRoot(FileObject file) {
        if (FileUtil.isArchiveFile(file)) {
            file = FileUtil.getArchiveRoot(file);
            FileObject[] children = file.getChildren();
            if (children.length == 1 && children[0].getName().equals("src")) {
                // There was a "src" parent folder in the archive.
                file = children[0];
            }
        }
        return file;
    }

    /**
     * Converts the given list of FileObject instances into a String of
     * path entries separated by File.pathSeparator.
     *
     * @param  path  list of paths.
     * @return  path as a String separated by File.pathSeparator.
     */
    public static List<String> toStrings(List<FileObject> path) {
        List<String> paths = new LinkedList<String>();
        for (FileObject fo : path) {
            // Check if the entry is inside an archive.
            FileObject arc = FileUtil.getArchiveFile(fo);
            if (arc != null) {
                fo = arc;
            }
            File file = FileUtil.toFile(fo);
            String fpath = file.getAbsolutePath();
            paths.add(fpath);
        }
        return paths;
    }
}
