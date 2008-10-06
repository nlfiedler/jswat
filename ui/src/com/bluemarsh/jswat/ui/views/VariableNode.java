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
 * $Id: VariableNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Arrays;
import com.bluemarsh.jswat.core.watch.Watch;
import com.bluemarsh.jswat.core.watch.WatchFactory;
import com.bluemarsh.jswat.core.watch.WatchManager;
import com.bluemarsh.jswat.core.watch.WatchProvider;
import com.bluemarsh.jswat.ui.nodes.ReadOnlyProperty;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.VirtualMachine;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 * Represents either a local variable or a field of an object.
 *
 * @author Nathan Fiedler
 */
public class VariableNode extends AbstractNode implements
        Comparable<VariableNode>, Node.Cookie {
    /** What kind of variable this is. */
    public static enum Kind {
        FIELD, LOCAL, STATIC_FIELD, THIS
    }
    /** Name of the name property. */
    protected static final String PROP_NAME = "name";
    /** Name of the type property. */
    protected static final String PROP_TYPE = "type";
    /** Name of the value property. */
    protected static final String PROP_VALUE = "value";
    /** Display modifiers. */
    private String modifiers;
    /** The name of the variable. */
    private String name;
    /** The type of the variable. */
    private String type;
    /** The kind of variable this node represents. */
    private Kind kind;
    /** The Field this variable represents, if it is not a local variable. */
    private Field field;
    /** The object reference, if this node represents a field of an object. */
    private ObjectReference object;
    /** Array of actions for our node. */
    private Action[] nodeActions;

    /**
     * Constructs a VariableNode to represent the given class loader.
     *
     * @param  kids   children of this node.
     * @param  name   name of the variable.
     * @param  type   type of the variable.
     * @param  kind   kind of variable.
     */
    public VariableNode(Children kids, String name, String type, Kind kind) {
        this(kids, name, type, kind, null);
    }

    /**
     * Constructs a VariableNode to represent the given class loader.
     *
     * @param  kids   children of this node.
     * @param  name   name of the variable.
     * @param  type   type of the variable.
     * @param  kind   kind of variable.
     * @param  mods   display modifiers.
     */
    public VariableNode(Children kids, String name, String type, Kind kind,
            String mods) {
        super(kids);
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.modifiers = mods;
        nodeActions = new Action[] {
            SystemAction.get(BreakpointAction.class),
            SystemAction.get(WatchAction.class),
        };
        getCookieSet().add(this);
    }

    public int compareTo(VariableNode o) {
        // Keep the 'this' object above all of the others.
        if (name.equals("this")) {
            return -1;
        } else if (o.name.equals("this")) {
            return 1;
        } else {
            return name.compareTo(o.name);
        }
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  value  display value.
     * @return  new property.
     */
    protected Node.Property createProperty(String key, String value) {
        String desc = NbBundle.getMessage(
                VariableNode.class, "CTL_VariablesView_Column_Desc_" + key);
        String name = NbBundle.getMessage(
                VariableNode.class, "CTL_VariablesView_Column_Name_" + key);
        return new ReadOnlyProperty(key, String.class, name, desc, value);
    }

    /**
     * Create the properties for this node.
     *
     * @return  property sheet.
     */
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, name));
        set.put(createProperty(PROP_TYPE, type));
        return sheet;
    }

    public Action[] getActions(boolean context) {
        Action[] retValue = super.getActions(context);
        retValue = (Action[]) Arrays.join(retValue, nodeActions);
        return retValue;
    }

    public String getDisplayName() {
        // Just return the name of the variable.
        return name;
    }

    public Image getIcon(int type) {
        String url = null;
        switch (kind) {
            case LOCAL:
                url = NbBundle.getMessage(VariableNode.class, "IMG_Variables_LocalNode");
                break;
            case FIELD:
                url = NbBundle.getMessage(VariableNode.class, "IMG_Variables_FieldNode");
                break;
            case STATIC_FIELD:
                url = NbBundle.getMessage(VariableNode.class, "IMG_Variables_StaticFieldNode");
                break;
            case THIS:
                url = NbBundle.getMessage(VariableNode.class, "IMG_Variables_ThisNode");
                break;
        }
        if (url != null) {
            return Utilities.loadImage(url);
        } else {
            return null;
        }
    }

    /**
     * Returns the field this node represents, if it is not a local variable.
     *
     * @return  field, or null if local variable.
     */
    public Field getField() {
        return field;
    }

    /**
     * Returns the display modifiers, or null if none.
     *
     * @return  display modifiers (may be null).
     */
    protected String getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the object reference this node is associated with.
     *
     * @return  object reference.
     */
    public ObjectReference getObjectReference() {
        return object;
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     * Sets the field that this node represents.
     *
     * @param  field  field for this node.
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Sets the object reference this node is associated with.
     *
     * @param  object  object reference.
     */
    public void setObjectReference(ObjectReference object) {
        this.object = object;
    }

    /**
     * Implements the action of creating a watchpoint for the
     * selected fields.
     *
     * @author  Nathan Fiedler
     */
    public static class BreakpointAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length > 0) {
                boolean enable = true;
                for (Node n : activatedNodes) {
                    if (n instanceof VariableNode) {
                        VariableNode vn = (VariableNode) n;
                        Field f = vn.getField();
                        if (f == null) {
                            enable = false;
                            break;
                        } else {
                            VirtualMachine vm = f.virtualMachine();
                            if (!vm.canWatchFieldModification() ||
                                    vn.getObjectReference() != null &&
                                    !vm.canUseInstanceFilters()) {
                                enable = false;
                                break;
                            }
                        }
                    }
                }
                return enable;
            } else {
                return false;
            }
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(BreakpointAction.class,
                    "LBL_BreakpointAction_Name");
        }

        protected void performAction(Node[] activatedNodes) {
            Session session = SessionProvider.getCurrentSession();
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
            for (Node n : activatedNodes) {
                VariableNode vn = (VariableNode) n.getCookie(VariableNode.class);
                if (vn != null) {
                    Field field = vn.getField();
                    if (field != null) {
                        ObjectReference obj = vn.getObjectReference();
                        Breakpoint bp = null;
                        if (obj != null) {
                            bp = bf.createWatchBreakpoint(
                                    field, obj, false, true);
                        } else {
                            try {
                                bp = bf.createWatchBreakpoint(
                                        field.declaringType().name(),
                                        field.name(), false, true);
                            } catch (MalformedClassNameException mcne) {
                                // This can't happen.
                            } catch (MalformedMemberNameException mmne) {
                                // This can't happen.
                            }
                        }
                        if (bp != null) {
                            bm.addBreakpoint(bp);
                        }
                    }
                }
            }
        }
    }

    /**
     * Implements the action of adding the selected fields to the
     * Watches window.
     *
     * @author  Nathan Fiedler
     */
    public static class WatchAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length > 0) {
                boolean enable = true;
                // For now, just allow watching objects.
                for (Node n : activatedNodes) {
                    if (!(n instanceof VariableNode)) {
                        enable = false;
                        break;
                    }
                }
                return enable;
            } else {
                return false;
            }
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(WatchAction.class,
                    "LBL_WatchAction_Name");
        }

        protected void performAction(Node[] activatedNodes) {
            Session session = SessionProvider.getCurrentSession();
            WatchManager wm = WatchProvider.getWatchManager(session);
            WatchFactory wf = WatchProvider.getWatchFactory();
            for (Node n : activatedNodes) {
                if (n instanceof VariableNode) {
                    VariableNode vn = (VariableNode) n;
                    StringBuilder name = new StringBuilder();
                    if (vn.kind == Kind.THIS) {
                        // Shortcut for 'this' node.
                        name.append("this");
                    } else {
                        // Otherwise, build out a complete reference.
                        name.insert(0, vn.name);
                        Node node = vn.getParentNode();
                        while (node != null && node instanceof VariableNode &&
                                ((VariableNode) node).kind != Kind.THIS) {
                            // Add period separator for all but array access.
                            if (name.charAt(0) != '[') {
                                name.insert(0, '.');
                            }
                            name.insert(0, node.getName());
                            node = node.getParentNode();
                        }
                    }
                    Watch w = wf.createExpressionWatch(name.toString());
                    wm.addWatch(w);
                }
            }
        }
    }
}
