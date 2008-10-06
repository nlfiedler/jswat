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
 * $Id: PathManager.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.path;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.filesystems.FileObject;

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
 * <p>The sourcepath is represented as a list of FileObjects since the
 * sources must be locally accessible to the debugger. Therefore, they
 * must be representible using FileObject instances.</p>
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
    FileObject findByteCode(ReferenceType clazz);

    /**
     * Scan the sourcepath and classpath to find the source file for the
     * named class. Any inner-class names will be removed and the default
     * extension will be appended in order to locate the source file.
     *
     * @param  name  name of the class to find source for.
     * @return  source file object, or null if not found.
     */
    FileObject findSource(String name);

    /**
     * Find the source file for the declaring type of the given location.
     *
     * @param  location  location for which to locate source.
     * @return  source file object, or null if not found.
     */
    FileObject findSource(Location location);

    /**
     * Find the source file for the given class.
     *
     * @param  clazz  class for which to locate source.
     * @return  source file object, or null if not found.
     */
    FileObject findSource(ReferenceType clazz);

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
    List<FileObject> getSourcePath();

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
    void setSourcePath(List<FileObject> roots);
}
