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
 * are Copyright (C) 2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NodeFactory.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.nodes.breakpoints.BreakpointGroupChildren;
import com.bluemarsh.jswat.nodes.breakpoints.BreakpointGroupNode;
import com.bluemarsh.jswat.nodes.breakpoints.BreakpointNode;
import com.bluemarsh.jswat.nodes.breakpoints.DefaultBreakpointGroupNode;
import com.bluemarsh.jswat.nodes.breakpoints.DefaultBreakpointNode;
import com.bluemarsh.jswat.nodes.classes.ClassChildren;
import com.bluemarsh.jswat.nodes.classes.ClassLoaderNode;
import com.bluemarsh.jswat.nodes.classes.ClassNode;
import com.bluemarsh.jswat.nodes.classes.DefaultClassLoaderNode;
import com.bluemarsh.jswat.nodes.classes.DefaultClassNode;
import com.bluemarsh.jswat.nodes.classes.PackageChildren;
import com.bluemarsh.jswat.nodes.sessions.DefaultSessionNode;
import com.bluemarsh.jswat.nodes.sessions.SessionNode;
import com.bluemarsh.jswat.nodes.stack.DefaultStackFrameNode;
import com.bluemarsh.jswat.nodes.stack.StackFrameNode;
import com.bluemarsh.jswat.nodes.threads.DefaultThreadGroupNode;
import com.bluemarsh.jswat.nodes.threads.DefaultThreadNode;
import com.bluemarsh.jswat.nodes.threads.ThreadGroupChildren;
import com.bluemarsh.jswat.nodes.threads.ThreadGroupNode;
import com.bluemarsh.jswat.nodes.threads.ThreadNode;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import org.openide.nodes.Children;

/**
 * A factory for all of the nodes available in the nodes module.
 *
 * @author Nathan Fiedler
 */
public abstract class NodeFactory {
    /** The default instance of the NodeFactory. */
    private static NodeFactory defaultFactory;

    /**
     * Retrieves the default implementation of the NodeFactory.
     *
     * @return  node factory implementation.
     */
    public static synchronized NodeFactory getDefault() {
        if (defaultFactory == null) {
            defaultFactory = new DefaultNodeFactory();
        }
        return defaultFactory;
    }

    /**
     * Creates a Node to represent the given BreakpointGroup.
     *
     * @param  group  breakpoint group to be represented.
     * @return  node for the breakpoint group.
     */
    public abstract BreakpointGroupNode createBreakpointGroupNode(
            BreakpointGroup group);

    /**
     * Creates a Node to represent the given Breakpoint.
     *
     * @param  bp  breakpoint to be represented.
     * @return  node for the breakpoint.
     */
    public abstract BreakpointNode createBreakpointNode(Breakpoint bp);

    /**
     * Creates a Node to represent a class loader.
     *
     * @param  vm      virtual machine that owns the class loader.
     * @param  loader  class loader to be represented.
     * @return  node for the class loader.
     */
    public abstract ClassLoaderNode createClassLoaderNode(VirtualMachine vm,
            ClassLoaderReference loader);

    /**
     * Creates a Node to represent a reference type.
     *
     * @param  type  reference type to be represented.
     * @return  node for the reference type.
     */
    public abstract ClassNode createClassNode(ReferenceType type);

    /**
     * Creates a Node to represent the given Session.
     *
     * @param  session  Session to be represented.
     * @return  node for the Session.
     */
    public abstract SessionNode createSessionNode(Session session);

    /**
     * Creates a Node to represent a stack frame.
     *
     * @param  index  the frame index.
     * @param  frame  the stack frame.
     */
    public abstract StackFrameNode createStackFrameNode(int index,
            StackFrame frame);

    /**
     * Create a node for the given thread group.
     *
     * @param  group    thread group.
     * @param  context  debugging context.
     * @return  thread group node.
     */
    public abstract ThreadGroupNode createThreadGroupNode(
            ThreadGroupReference group, DebuggingContext context);

    /**
     * Create a node for the given thread.
     *
     * @param  thread   thread reference.
     * @param  context  debugging context.
     * @return  thread node.
     */
    public abstract ThreadNode createThreadNode(ThreadReference thread,
            DebuggingContext context);

    /**
     * Default implementation of NodeFactory.
     */
    private static class DefaultNodeFactory extends NodeFactory {

        @Override
        public BreakpointGroupNode createBreakpointGroupNode(
                BreakpointGroup group) {
            Children ch = new BreakpointGroupChildren(group);
            return new DefaultBreakpointGroupNode(ch, group);
        }

        @Override
        public BreakpointNode createBreakpointNode(Breakpoint bp) {
            return new DefaultBreakpointNode(bp);
        }

        @Override
        public ClassLoaderNode createClassLoaderNode(VirtualMachine vm,
                ClassLoaderReference loader) {
            Children ch = new PackageChildren(vm);
            return new DefaultClassLoaderNode(ch, loader);
        }

        @Override
        public ClassNode createClassNode(ReferenceType type) {
            Children ch = new ClassChildren(type);
            return new DefaultClassNode(ch, type);
        }

        @Override
        public SessionNode createSessionNode(Session session) {
            return new DefaultSessionNode(session);
        }

        @Override
        public StackFrameNode createStackFrameNode(int index, StackFrame frame) {
            return new DefaultStackFrameNode(index, frame);
        }

        @Override
        public ThreadGroupNode createThreadGroupNode(ThreadGroupReference group,
                DebuggingContext context) {
            Children ch = new ThreadGroupChildren(group, context);
            return new DefaultThreadGroupNode(group, ch, context);
        }

        @Override
        public ThreadNode createThreadNode(ThreadReference thread,
                DebuggingContext context) {
            return new DefaultThreadNode(thread, context);
        }
    }
}
