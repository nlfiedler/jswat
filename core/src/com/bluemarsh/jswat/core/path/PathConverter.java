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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PathConverter.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.path;

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
     * Splits the given string by the File.pathSeparator character and
     * returns a list of FileObjects that represent each path entry.
     * Non-existent paths are not returned.
     *
     * @param  path  path entries separated by File.pathSeparator characters.
     * @return  list of FileObjects.
     */
    public static List<FileObject> toFileObject(String path) {
        List<String> paths = Strings.stringToList(path, File.pathSeparator);
        List<FileObject> spath = new LinkedList<FileObject>();
        // Convert the strings into FileObjects on the local system.
        for (String apath : paths) {
            File file = new File(apath);
            if (file.exists()) {
                file = FileUtil.normalizeFile(file);
                FileObject fo = FileUtil.toFileObject(file);
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
    public static String toString(List<FileObject> path) {
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
        return Strings.listToString(paths, File.pathSeparator);
    }
}
