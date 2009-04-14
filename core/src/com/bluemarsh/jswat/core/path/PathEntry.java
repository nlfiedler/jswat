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
 * are Copyright (C) 2002-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.path;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The PathEntry interface represents a file located within the source
 * or class path as defined by the resident PathManager. This could be
 * a source file or a class file.
 *
 * @author  Nathan Fiedler
 */
public interface PathEntry {

//    /**
//     * Attempts to ensure that the referenced entry actually exists.
//     *
//     * @return  true if entry exists, false if not found.
//     */
//    boolean exists();

    /**
     * Get the input stream for reading the entry object.
     *
     * @return  input stream to the entry object.
     * @throws  IOException  if getting stream fails.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the display name of the entry.
     *
     * @return  displayable name of entry object.
     */
    String getDisplayName();

    /**
     * Returns the file name of the entry.
     *
     * @return  name of entry object.
     */
    String getName();

    /**
     * Returns the path and file name of the entry.
     *
     * @return  file path.
     */
    String getPath();

    /**
     * Returns a URL form of the path entry.
     *
     * @return  URL for the path entry.
     */
    URL getURL();

    /**
     * Determines if the given object is identical to the object used
     * as the basis for this path entry (e.g. for instance if may be
     * a File and this path entry may use File, in which case they are
     * compared for equality).
     *
     * @param  o  object to compare.
     * @return  true if objects are equal.
     */
    boolean isSame(Object o);
}
