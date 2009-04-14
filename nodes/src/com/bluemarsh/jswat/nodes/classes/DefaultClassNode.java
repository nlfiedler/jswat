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

package com.bluemarsh.jswat.nodes.classes;

import com.bluemarsh.jswat.core.path.PathEntry;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.bluemarsh.jswat.nodes.ShowSourceAction;
import com.bluemarsh.jswat.nodes.ShowSourceCookie;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import com.sun.jdi.ReferenceType;
import java.awt.Image;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Represents a class in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class DefaultClassNode extends ClassNode implements ShowSourceCookie {
    /** The class reference we represent. */
    private ReferenceType clazz;
    /** The name of the class. */
    private String name;
    /** The short name of the class. */
    private String shortName;

    /**
     * Creates a new instance of DefaultClassNode.
     *
     * @param  children  the node children.
     * @param  clazz     the reference type.
     */
    public DefaultClassNode(Children children, ReferenceType clazz) {
        super(children);
        this.clazz = clazz;
        name = clazz.name();
        shortName = Names.getShortClassName(name);
        getLookupContent().add(this);
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
        String _name = NbBundle.getMessage(
                ClassNode.class, "CTL_ClassProperty_Name_" + key);
        return new ReadOnlyProperty(key, String.class, _name, desc, value);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, name));
        return sheet;
    }

    @Override
    public String getDisplayName() {
        return shortName;
    }

    @Override
    public ReferenceType getReferenceType() {
        return clazz;
    }

    @Override
    public Image getIcon(int type) {
        String url = NbBundle.getMessage(ClassNode.class, "IMG_ClassNode");
        return ImageUtilities.loadImage(url);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getName() {
        return shortName;
    }

    @Override
    protected Action[] getNodeActions() {
        return new Action[] {
            SystemAction.get(ShowSourceAction.class),
            SystemAction.get(HotSwapAction.class),
            SystemAction.get(TraceAction.class),
        };
    }

    @Override
    public void showSource() {
        Session session = SessionProvider.getCurrentSession();
        PathManager pm = PathProvider.getPathManager(session);
        PathEntry src = pm.findSource(clazz);
        if (src != null) {
            String url = src.getURL().toString();
            EditorSupport.getDefault().showSource(url, 1);
        } else {
            String msg = NbBundle.getMessage(ClassNode.class,
                    "ERR_SourceMissing", clazz.name());
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
    }
}
