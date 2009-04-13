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

import com.bluemarsh.jswat.core.util.Strings;
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
     * @return  FileObject.
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
     * Check if the file is actually an archive, and get the
     * root of the archive as a file object.
     * 
     * @param  file  file object to possibly be converted.
     * @return  the converted file object.
     */
    public static FileObject convertToRoot(FileObject file) {
        if (FileUtil.isArchiveFile(file)) {
            file = FileUtil.getArchiveRoot(file);
        }
        return file;
    }

    /**
     * Splits the given string by the File.pathSeparator character and
     * returns a list of FileObjects that represent each path entry.
     * Non-existent paths are not returned.
     *
     * @param  path  path entries separated by File.pathSeparator characters.
     * @return  list of FileObjects.
     */
    public static List<FileObject> pathToFileObject(String path) {
        List<String> paths = Strings.stringToList(path, File.pathSeparator);
        List<FileObject> spath = new LinkedList<FileObject>();
        // Convert the strings into FileObjects on the local system.
        for (String apath : paths) {
            FileObject fo = toFileObject(apath);
            if (fo != null) {
                spath.add(fo);
            }
            // Silently leave out any missing path elements.
        }
        return spath;
    }

    /**
     * Converts the given list of FileObject instances into a String of
     * path entries separated by File.pathSeparator.
     *
     * @param  path  list of paths.
     * @return  path as a String separated by File.pathSeparator.
     */
    public static String toStringPath(List<FileObject> path) {
        List<String> paths = toStrings(path);
        return Strings.listToString(paths, File.pathSeparator);
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
            // Check if file is the root of an archive FS -- an archive
            // file will never be the root of a file system.
            if (fo.isRoot()) {
                // It was, get the actual archive file.
                fo = FileUtil.getArchiveFile(fo);
            }
            File file = FileUtil.toFile(fo);
            String fpath = file.getAbsolutePath();
            paths.add(fpath);
        }
        return paths;
    }
}
