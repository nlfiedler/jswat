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
 * $Id: DefaultBreakpointNode.java 32 2008-06-30 01:23:05Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.breakpoints;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.nodes.BeanPropertySupport;
import com.bluemarsh.jswat.nodes.ExceptionProperty;
import com.bluemarsh.jswat.nodes.ShowSourceAction;
import com.bluemarsh.jswat.nodes.ShowSourceCookie;
import com.bluemarsh.jswat.ui.breakpoint.BreakpointType;
import com.bluemarsh.jswat.ui.breakpoint.EditorPanel;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
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
import org.openide.actions.CustomizeAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Represents a breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class DefaultBreakpointNode extends BreakpointNode implements
        PropertyChangeListener, ShowSourceCookie {
    /** The breakpoint we represent. */
    private Breakpoint breakpoint;

    /**
     * Constructs a new instance of BreakpointNode.
     *
     * @param  bp  breakpoint we represent.
     */
    public DefaultBreakpointNode(Breakpoint bp) {
        super();
        breakpoint = bp;
        bp.addPropertyChangeListener(this);
        if (bp instanceof LineBreakpoint) {
            getLookupContent().add(this);
        }
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        try {
            BeanInfo bi = Introspector.getBeanInfo(breakpoint.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if (prop.isHidden()) {
                    // Ignore the hidden properties.
                    continue;
                }
                String name = prop.getName();
                if (name.equals("class") || name.equals("breakpointGroup")) {
                    // Ignore these useless properties.
                    continue;
                }
                // Ignore the properties that are not supported.
                if (name.equals("classFilter") && !breakpoint.canFilterClass()) {
                    continue;
                }
                if (name.equals("threadFilter") && !breakpoint.canFilterThread()) {
                    continue;
                }
                Class type = prop.getPropertyType();
                Method getter = prop.getReadMethod();
                Method setter = prop.getWriteMethod();
                // Each node property needs its own name, and the display properties
                // that were given to the corresponding table column.
                BeanPropertySupport node = new BeanPropertySupport(
                        breakpoint, type, getter, setter);
                node.setName(name);
                node.setDisplayName(NbBundle.getMessage(BreakpointNode.class,
                        "CTL_BreakpointProperty_Name_" + name));
                node.setShortDescription(NbBundle.getMessage(BreakpointNode.class,
                        "CTL_BreakpointProperty_Desc_" + name));
                Class editor = prop.getPropertyEditorClass();
                if (editor != null) {
                    node.setPropertyEditorClass(editor);
                }
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
        BreakpointGroup bg = breakpoint.getBreakpointGroup();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(bg);
        bm.removeBreakpoint(breakpoint);
        super.destroy();
    }

    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    @Override
    public Component getCustomizer() {
        BreakpointType type = BreakpointType.getType(breakpoint);
        EditorPanel ep = new EditorPanel(type);
        ep.loadParameters(breakpoint);
        return ep;
    }

    @Override
    public String getDisplayName() {
        return breakpoint.toString();
    }

    @Override
    public Image getIcon(int type) {
        String url = NbBundle.getMessage(BreakpointNode.class,
                "IMG_BreakpointNode");
        return ImageUtilities.loadImage(url);
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
    public Action getPreferredAction() {
        if (breakpoint instanceof LineBreakpoint) {
            return SystemAction.get(ShowSourceAction.class);
        } else {
            return SystemAction.get(CustomizeAction.class);
        }
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (name.equals("annotation") || name.equals("breakpointGroup")) {
            // Need to ignore properties that are not in our defined set.
            return;
        }
        firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
        // Always update the display name since the breakpoints often
        // incorporate various property values into their descriptions.
        fireDisplayNameChange(null, null);
    }

    public void showSource() {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) breakpoint;
            String url = lb.getURL();
            int line = lb.getLineNumber();
            EditorSupport es = EditorSupport.getDefault();
            es.showSource(url, line);
        }
    }
}
