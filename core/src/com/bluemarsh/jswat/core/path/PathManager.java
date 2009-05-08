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

package com.bluemarsh.jswat.core.path;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * A PathManager maintains the classpath and sourcepath values within the
 * application. Concrete implementations of this interface are acquired
 * from the <code>PathProvider</code> class.
 *
 * <p>The classpath is represented as a list of Strings because the roots
 * may only have meaning on the machine running the debuggee. That is, the
 * path roots may refer to files and directories that exist only on the
 * debuggee host.</p>
 *
 * @author Nathan Fiedler
 */
public interface PathManager {
    /** Session property name for the classpath setting. */
    public static final String PROP_CLASSPATH = "ClassPath";
    /** Session property name for the sourcepath setting. */
    public static final String PROP_SOURCEPATH = "SourcePath";
    /** Session property name for ignoring the debuggee classpath. */
    public static final String PROP_IGNORE_DEBUGGEE = "IgnoreDebuggee";

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param  listener  the PropertyChangeListener to be added.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Indicates if the classpath can be modified or not.
     *
     * @return  true if classpath is modifiable, false otherwise.
     */
    boolean canSetClassPath();

    /**
     * Indicates if the sourcepath can be modified or not.
     *
     * @return  true if sourcepath is modifiable, false otherwise.
     */
    boolean canSetSourcePath();

    /**
     * Find the bytecode file (i.e. the .class file) for the given class.
     *
     * @param  clazz  class for which to locate code.
     * @return  .class file object, or null if not found.
     */
    PathEntry findByteCode(ReferenceType clazz);

    /**
     * Find the named file in the sourcepath or classpath. The name
     * is assumed to be a relative path and file name.
     *
     * @param  filename  relative path and name of file to locate.
     * @return  file object, or null if not found.
     */
    PathEntry findFile(String filename);

    /**
     * Scan the sourcepath and classpath to find the source file for the
     * named class. Any inner-class names will be removed and the default
     * extension will be appended in order to locate the source file.
     *
     * @param  name  name of the class to find source for.
     * @return  source file object, or null if not found.
     */
    PathEntry findSource(String name);

    /**
     * Find the source file for the declaring type of the given location.
     *
     * @param  location  location for which to locate source.
     * @return  source file object, or null if not found.
     */
    PathEntry findSource(Location location);

    /**
     * Find the source file for the given class.
     *
     * @param  clazz  class for which to locate source.
     * @return  source file object, or null if not found.
     */
    PathEntry findSource(ReferenceType clazz);

    /**
     * Returns the classpath as it is currently known. If the debugger is
     * attached to the debuggee, this will return the classpath of the
     * debuggee. Otherwise, it will return whatever the user has defined
     * to be the classpath. This list is not modifiable by the caller. To
     * change the sourcepath definition, use the setSourcePath() method.
     *
     * @return  list of classpath roots, may be null.
     */
    List<String> getClassPath();

    /**
     * Returns the list of sourcepath roots, as previously defined by the user.
     * This list is not modifiable by the caller. To change the sourcepath
     * definition, use the setSourcePath() method.
     *
     * @return  list of sourcepath roots, may be null.
     */
    List<String> getSourcePath();

    /**
     * Remove a PropertyChangeListener from the listener list.
     *
     * @param  listener  the PropertyChangeListener to be removed.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Define the classpath for this session. Sends a property change event
     * to the registered listeners.
     *
     * @param  roots  list of classpath roots, may be null.
     */
    void setClassPath(List<String> roots);

    /**
     * Define the sourcepath for this session. Sends a property change event
     * to the registered listeners.
     *
     * @param  roots  list of sourcepath roots, may be null.
     */
    void setSourcePath(List<String> roots);
}
