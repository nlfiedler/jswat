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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
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
import org.openide.ErrorManager;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointNode extends BaseNode implements PropertyChangeListener {
    /** The breakpoint we represent. */
    private Breakpoint breakpoint;

    /**
     * Constructs a new instance of BreakpointNode.
     *
     * @param  bp  breakpoint we represent.
     */
    public BreakpointNode(Breakpoint bp) {
        super();
        breakpoint = bp;
        bp.addPropertyChangeListener(this);
        if (bp instanceof LineBreakpoint) {
            getCookieSet().add(new SourceCookie());
        }
    }

    public boolean canDestroy() {
        return true;
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        try {
            BeanInfo bi = Introspector.getBeanInfo(breakpoint.getClass());
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for(PropertyDescriptor prop : props) {
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

    public void destroy() throws IOException {
        BreakpointGroup bg = breakpoint.getBreakpointGroup();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(bg);
        bm.removeBreakpoint(breakpoint);
        super.destroy();
    }

    /**
     * Returns the Breakpoint this node represents.
     *
     * @return  breakpoint.
     */
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    public Component getCustomizer() {
        BreakpointType type = BreakpointType.getType(breakpoint);
        EditorPanel ep = new EditorPanel(type);
        ep.loadParameters(breakpoint);
        return ep;
    }

    public String getDisplayName() {
        return breakpoint.toString();
    }

    public Image getIcon(int type) {
        String url = NbBundle.getMessage(BreakpointNode.class,
                "IMG_BreakpointNode");
        return Utilities.loadImage(url);
    }

    public boolean hasCustomizer() {
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (name.equals("annotation") || name.equals("breakpointGroup")) {
            // Need to ignore properties that are not in our defined set.
            return;
        }
        propertyChanged(name, evt.getOldValue(), evt.getNewValue());
        // Always update the display name since the breakpoints often
        // incorporate various property values into their descriptions.
        displayNameChanged();
    }

    /**
     * Displays the source code for the corresponding (line) breakpoint.
     */
    private class SourceCookie implements ShowSourceCookie {

        public boolean canShowSource() {
            return breakpoint instanceof LineBreakpoint;
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
}
