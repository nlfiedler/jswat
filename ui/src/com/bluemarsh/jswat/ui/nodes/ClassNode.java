/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClassNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import com.sun.jdi.ReferenceType;
import java.awt.Image;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a class in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class ClassNode extends BaseNode {
    /** The class reference we represent. */
    private ReferenceType clazz;
    /** The name of the class. */
    private String name;
    /** The short name of the class. */
    private String shortName;

    /**
     * Constructs a ClassNode to represent the given class.
     *
     * @param  clazz  the reference type.
     */
    public ClassNode(ReferenceType clazz) {
        super();
        this.clazz = clazz;
        name = clazz.name();
        shortName = Names.getShortClassName(name);
        getCookieSet().add(new SourceCookie());
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  value  display value.
     * @return  new property.
     */
    private Node.Property createProperty(String key, String value) {
        String desc = NbBundle.getMessage(
                ClassNode.class, "CTL_ClassProperty_Desc_" + key);
        String name = NbBundle.getMessage(
                ClassNode.class, "CTL_ClassProperty_Name_" + key);
        return new ReadOnlyProperty(key, String.class, name, desc, value);
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, name));
        return sheet;
    }

    public String getDisplayName() {
        return shortName;
    }

    /**
     * Returns the ReferenceType this node represents.
     *
     * @return  reference type.
     */
    public ReferenceType getReferenceType() {
        return clazz;
    }

    public Image getIcon(int type) {
        String url = NbBundle.getMessage(ClassNode.class, "IMG_ClassNode");
        return Utilities.loadImage(url);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    public String getName() {
        return shortName;
    }

    /**
     * Displays the source code for the corresponding class.
     */
    private class SourceCookie implements ShowSourceCookie {

        public boolean canShowSource() {
            return true;
        }

        public void showSource() {
            Session session = SessionProvider.getCurrentSession();
            PathManager pm = PathProvider.getPathManager(session);
            FileObject src = pm.findSource(clazz);
            if (src != null) {
                try {
                    String url = src.getURL().toString();
                    EditorSupport.getDefault().showSource(url, 1);
                } catch (FileStateInvalidException fsie) {
                    ErrorManager.getDefault().notify(fsie);
                }
            } else {
                String msg = NbBundle.getMessage(SourceCookie.class,
                        "ERR_SourceMissing", clazz.name());
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
}
