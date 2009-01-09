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
 * are Copyright (C) 2004-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.breakpoints;

import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.nodes.BeanPropertySupport;
import com.bluemarsh.jswat.nodes.ExceptionProperty;
import com.bluemarsh.jswat.ui.breakpoint.GroupEditorPanel;
import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.MissingResourceException;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 * Represents a group of breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class DefaultBreakpointGroupNode extends BreakpointGroupNode
        implements PropertyChangeListener {
    /** The breakpoint group we represent. */
    private BreakpointGroup group;

    /**
     * Constructs a new instance of GroupNode.
     *
     * @param  children  children heirarchy for this node.
     * @param  group     breakpoint group we represent.
     */
    public DefaultBreakpointGroupNode(Children children, BreakpointGroup group) {
        super(children);
        this.group = group;
        group.addPropertyChangeListener(this);
    }

    @Override
    public boolean canDestroy() {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(group);
        BreakpointGroup defgrp = bm.getDefaultGroup();
        // Disallow deleting the default group.
        return !defgrp.equals(group);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        try {
            BeanInfo bi = Introspector.getBeanInfo(group.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                String name = prop.getName();
                if (name.equals("class") || name.equals("parent")) {
                    // Ignore these useless properties.
                    continue;
                }
                Class type = prop.getPropertyType();
                Method getter = prop.getReadMethod();
                Method setter = prop.getWriteMethod();
                // Each node property needs its own name, and the display properties
                // that were given to the corresponding table column.
                Property node = new BeanPropertySupport(
                        group, type, getter, setter);
                node.setName(name);
                node.setDisplayName(NbBundle.getMessage(BreakpointGroupNode.class,
                        "CTL_BreakpointProperty_Name_" + name));
                node.setShortDescription(NbBundle.getMessage(BreakpointGroupNode.class,
                        "CTL_BreakpointProperty_Desc_" + name));
                set.put(node);
            }
        }  catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            set.put(new ExceptionProperty(ie));
        }  catch (MissingResourceException mre) {
            ErrorManager.getDefault().notify(mre);
            set.put(new ExceptionProperty(mre));
        }
        return sheet;
    }

    @Override
    public void destroy() throws IOException {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(group);
        bm.removeBreakpointGroup(group);
        super.destroy();
    }

    @Override
    public Component getCustomizer() {
        GroupEditorPanel panel = new GroupEditorPanel();
        panel.loadParameters(group);
        return panel;
    }

    @Override
    public String getDisplayName() {
        return group.getName();
    }

    @Override
    public Image getIcon(int type) {
        String url = NbBundle.getMessage(BreakpointGroupNode.class,
                "IMG_GroupNode");
        return ImageUtilities.loadImage(url);
    }

    public BreakpointGroup getBreakpointGroup() {
        return group;
    }

    @Override
    public NewType[] getNewTypes() {
        NewType[] types = new NewType[] {
            new BreakpointNewType(),
            new BreakpointGroupNewType(group),
        };
        return types;
    }

    protected Action[] getNodeActions() {
        return new Action[] {
            SystemAction.get(NewAction.class),
            SystemAction.get(DeleteAction.class),
            SystemAction.get(EnableAction.class),
            SystemAction.get(DisableAction.class),
        };
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
                evt.getNewValue());
        // Only property of group is the name, so must update this.
        fireDisplayNameChange(null, null);
    }
}
