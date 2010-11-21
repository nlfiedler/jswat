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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.ui.components;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Class DebuggeeInfoPanel displays various bits of information about the
 * debuggee to which the Session is connected.
 *
 * @author  Nathan Fiedler
 */
public class DebuggeeInfoPanel extends javax.swing.JPanel {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Emtpy list of Value objects. */
    private static List<Value> EMTPY_ARGUMENTS = new LinkedList<Value>();

    /**
     * Creates new form DebuggeeInfoPanel.
     */
    public DebuggeeInfoPanel() {
        initComponents();
    }

    /**
     * Constructs a dialog and displays this panel as its primary component.
     *
     * @param  session  Session to be displayed.
     */
    public void display(Session session) {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        String str = String.valueOf(vmm.majorInterfaceVersion()) + '.'
                + String.valueOf(vmm.minorInterfaceVersion());
        jdiVersionTextField.setText(str);
        if (session.isConnected()) {
            VirtualMachine vm = session.getConnection().getVM();
            jvmVersionTextField.setText(vm.version());
            jvmDescriptionTextArea.setText(vm.description());
            displaySupport(vm);
        }

        displayProperties(session);

        NotifyDescriptor desc = new NotifyDescriptor.Message(this);
        desc.setTitle(NbBundle.getMessage(DebuggeeInfoPanel.class,
                "LBL_DebuggeeInfo_Title"));
        DialogDisplayer.getDefault().notify(desc);
    }

    /**
     * Retrieves the system properties defined in the debuggee to which the
     * given session is connected.
     *
     * @param  session  Session for which to get properties.
     */
    private void displayProperties(Session session) {
        StringBuilder sb = new StringBuilder(1024);
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        if (thread == null) {
            sb.append(NbBundle.getMessage(DebuggeeInfoPanel.class,
                    "ERR_DebuggeeInfo_IncompatibleThread"));
        } else {
            VirtualMachine vm = session.getConnection().getVM();
            List<ReferenceType> classes = vm.classesByName("java.lang.System");
            // We assume it exists and is a real class.
            ClassType type = (ClassType) classes.get(0);
            List<Method> methods = type.methodsByName("getProperties");
            if (methods.isEmpty()) {
                // KVM does not have a System.getProperties() method.
                sb.append(NbBundle.getMessage(DebuggeeInfoPanel.class,
                        "ERR_DebuggeeInfo_NoPropertiesMethod"));
            } else {
                Method method = methods.get(0);
                try {
                    ObjectReference props = (ObjectReference) Classes.invokeMethod(
                            null, type, thread, method, EMTPY_ARGUMENTS);

                    // Get the property names enumerator.
                    type = (ClassType) props.referenceType();
                    methods = type.methodsByName("propertyNames", "()Ljava/util/Enumeration;");
                    if (methods.isEmpty()) {
                        throw new IllegalArgumentException("no propertyNames() method");
                    }
                    method = methods.get(0);
                    ObjectReference iter = (ObjectReference) Classes.invokeMethod(
                            props, type, thread, method, EMTPY_ARGUMENTS);

                    ClassType iterType = (ClassType) iter.referenceType();
                    methods = iterType.methodsByName("hasMoreElements", "()Z");
                    if (methods.isEmpty()) {
                        throw new IllegalArgumentException("no hasMoreElements() method");
                    }
                    Method hasNextMeth = methods.get(0);

                    methods = iterType.methodsByName("nextElement", "()Ljava/lang/Object;");
                    if (methods.isEmpty()) {
                        throw new IllegalArgumentException("no nextElement() method");
                    }
                    Method nextMeth = methods.get(0);

                    BooleanValue bool = (BooleanValue) Classes.invokeMethod(
                            iter, iterType, thread, hasNextMeth, EMTPY_ARGUMENTS);

                    // Enumerate the property names, and then sort them.
                    List<String> propNames = new LinkedList<String>();
                    while (bool != null && bool.value()) {
                        StringReference sr = (StringReference) Classes.invokeMethod(
                                iter, iterType, thread, nextMeth, EMTPY_ARGUMENTS);
                        propNames.add(sr.value());
                        bool = (BooleanValue) Classes.invokeMethod(
                                iter, iterType, thread, hasNextMeth, EMTPY_ARGUMENTS);
                    }
                    Collections.sort(propNames);

                    // Display the property values.
                    type = (ClassType) props.referenceType();
                    methods = type.methodsByName("getProperty",
                            "(Ljava/lang/String;)Ljava/lang/String;");
                    if (methods.isEmpty()) {
                        throw new IllegalArgumentException("no getProperty() method");
                    }
                    method = methods.get(0);
                    List<Value> args = new ArrayList<Value>(1);
                    args.add(vm.mirrorOf("dummy"));
                    for (String prop : propNames) {
                        args.set(0, vm.mirrorOf(prop));
                        StringReference sr = (StringReference) Classes.invokeMethod(
                                props, type, thread, method, args);
                        sb.append(prop);
                        sb.append(" = ");
                        sb.append(sr.value());
                        sb.append('\n');
                    }
                } catch (ExecutionException ee) {
                    Throwable t = ee.getCause();
                    if (t instanceof IncompatibleThreadStateException) {
                        sb.append(NbBundle.getMessage(DebuggeeInfoPanel.class,
                                "ERR_DebuggeeInfo_IncompatibleThread"));
                    } else {
                        sb.append(t.toString());
                    }
                } catch (Exception e) {
                    sb.append(e.toString());
                }
            }
        }
        propsTextArea.setText(sb.toString());
    }

    /**
     * Displays the supported operations of the given virtual machine.
     *
     * @param  vm  virtual machine to query.
     */
    private void displaySupport(VirtualMachine vm) {
        addMethodCheckBox.setSelected(vm.canAddMethod());
        beModifiedCheckBox.setSelected(vm.canBeModified());
        forceEarlyReturnCheckBox.setSelected(vm.canForceEarlyReturn());
        getBytecodesCheckBox.setSelected(vm.canGetBytecodes());
        getGetClassFileVersionCheckBox.setSelected(vm.canGetClassFileVersion());
        getGetConstantPoolCheckBox.setSelected(vm.canGetConstantPool());
        getCurrentContendedMonitorCheckBox.setSelected(vm.canGetCurrentContendedMonitor());
        getInstanceInfoCheckBox.setSelected(vm.canGetInstanceInfo());
        getMethodReturnCheckBox.setSelected(vm.canGetMethodReturnValues());
        getMonitorFrameInfoCheckBox.setSelected(vm.canGetMonitorFrameInfo());
        getMonitorInfoCheckBox.setSelected(vm.canGetMonitorInfo());
        getOwnedMonitorInfoCheckBox.setSelected(vm.canGetOwnedMonitorInfo());
        getSourceDebugExtensionCheckBox.setSelected(vm.canGetSourceDebugExtension());
        getSyntheticAttributeCheckBox.setSelected(vm.canGetSyntheticAttribute());
        popFramesCheckBox.setSelected(vm.canPopFrames());
        redefineClassesCheckBox.setSelected(vm.canRedefineClasses());
        requestMonitorEventCheckBox.setSelected(vm.canRequestMonitorEvents());
        requestVMDeathEventCheckBox.setSelected(vm.canRequestVMDeathEvent());
        unrestrictedlyRedefineClassesCheckBox.setSelected(vm.canUnrestrictedlyRedefineClasses());
        useInstanceFiltersCheckBox.setSelected(vm.canUseInstanceFilters());
        useSourceFiltersCheckBox.setSelected(vm.canUseSourceNameFilters());
        watchFieldAccessCheckBox.setSelected(vm.canWatchFieldAccess());
        watchFieldModificationCheckBox.setSelected(vm.canWatchFieldModification());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        tabbedPane = new javax.swing.JTabbedPane();
        versionPanel = new javax.swing.JPanel();
        jdiVersionLabel = new javax.swing.JLabel();
        jdiVersionTextField = new javax.swing.JTextField();
        jvmVersionLabel = new javax.swing.JLabel();
        jvmVersionTextField = new javax.swing.JTextField();
        jvmDescriptionLabel = new javax.swing.JLabel();
        jvmDescriptionScrollPane = new javax.swing.JScrollPane();
        jvmDescriptionTextArea = new javax.swing.JTextArea();
        propertiesPanel = new javax.swing.JPanel();
        propsScrollPane = new javax.swing.JScrollPane();
        propsTextArea = new javax.swing.JTextArea();
        supportPanel = new javax.swing.JPanel();
        addMethodCheckBox = new javax.swing.JCheckBox();
        beModifiedCheckBox = new javax.swing.JCheckBox();
        forceEarlyReturnCheckBox = new javax.swing.JCheckBox();
        getBytecodesCheckBox = new javax.swing.JCheckBox();
        getGetClassFileVersionCheckBox = new javax.swing.JCheckBox();
        getGetConstantPoolCheckBox = new javax.swing.JCheckBox();
        getCurrentContendedMonitorCheckBox = new javax.swing.JCheckBox();
        getInstanceInfoCheckBox = new javax.swing.JCheckBox();
        getMethodReturnCheckBox = new javax.swing.JCheckBox();
        getMonitorFrameInfoCheckBox = new javax.swing.JCheckBox();
        getMonitorInfoCheckBox = new javax.swing.JCheckBox();
        getOwnedMonitorInfoCheckBox = new javax.swing.JCheckBox();
        getSourceDebugExtensionCheckBox = new javax.swing.JCheckBox();
        getSyntheticAttributeCheckBox = new javax.swing.JCheckBox();
        popFramesCheckBox = new javax.swing.JCheckBox();
        redefineClassesCheckBox = new javax.swing.JCheckBox();
        requestMonitorEventCheckBox = new javax.swing.JCheckBox();
        requestVMDeathEventCheckBox = new javax.swing.JCheckBox();
        unrestrictedlyRedefineClassesCheckBox = new javax.swing.JCheckBox();
        useInstanceFiltersCheckBox = new javax.swing.JCheckBox();
        useSourceFiltersCheckBox = new javax.swing.JCheckBox();
        watchFieldAccessCheckBox = new javax.swing.JCheckBox();
        watchFieldModificationCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        versionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        jdiVersionLabel.setLabelFor(jdiVersionTextField);
        jdiVersionLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_JdiVersion"));

        jdiVersionTextField.setEditable(false);

        jvmVersionLabel.setLabelFor(jvmVersionTextField);
        jvmVersionLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_JvmVersion"));

        jvmVersionTextField.setEditable(false);

        jvmDescriptionLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_JvmDesc"));

        jvmDescriptionTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
        jvmDescriptionTextArea.setEditable(false);
        jvmDescriptionScrollPane.setViewportView(jvmDescriptionTextArea);

        javax.swing.GroupLayout versionPanelLayout = new javax.swing.GroupLayout(versionPanel);
        versionPanel.setLayout(versionPanelLayout);
        versionPanelLayout.setHorizontalGroup(
            versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(versionPanelLayout.createSequentialGroup()
                .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jdiVersionLabel)
                    .addComponent(jvmVersionLabel)
                    .addComponent(jvmDescriptionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(versionPanelLayout.createSequentialGroup()
                        .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jvmVersionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .addComponent(jdiVersionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))
                        .addGap(0, 0, 0))
                    .addComponent(jvmDescriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)))
        );
        versionPanelLayout.setVerticalGroup(
            versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(versionPanelLayout.createSequentialGroup()
                .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jdiVersionLabel)
                    .addComponent(jdiVersionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jvmVersionLabel)
                    .addComponent(jvmVersionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(versionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(versionPanelLayout.createSequentialGroup()
                        .addComponent(jvmDescriptionLabel)
                        .addContainerGap(211, Short.MAX_VALUE))
                    .addComponent(jvmDescriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)))
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_VersionsTab"), versionPanel);

        propertiesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        propsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        propsTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
        propsTextArea.setColumns(40);
        propsTextArea.setEditable(false);
        propsTextArea.setRows(10);
        propsScrollPane.setViewportView(propsTextArea);

        javax.swing.GroupLayout propertiesPanelLayout = new javax.swing.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(propsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(propsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_PropsTab"), propertiesPanel);

        supportPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addMethodCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanAddMethod"));
        addMethodCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanAddMethod"));
        addMethodCheckBox.setEnabled(false);

        beModifiedCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanBeModified"));
        beModifiedCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanBeModified"));
        beModifiedCheckBox.setEnabled(false);

        forceEarlyReturnCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanForceEarlyReturn"));
        forceEarlyReturnCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanForceEarlyReturn"));
        forceEarlyReturnCheckBox.setEnabled(false);

        getBytecodesCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetBytecodes"));
        getBytecodesCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetBytecodes"));
        getBytecodesCheckBox.setEnabled(false);

        getGetClassFileVersionCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetClassVersion"));
        getGetClassFileVersionCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetClassVersion"));
        getGetClassFileVersionCheckBox.setEnabled(false);

        getGetConstantPoolCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetConstantPool"));
        getGetConstantPoolCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetConstantPool"));
        getGetConstantPoolCheckBox.setEnabled(false);

        getCurrentContendedMonitorCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetCurrentContentedMonitor"));
        getCurrentContendedMonitorCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetCurrentContentedMonitor"));
        getCurrentContendedMonitorCheckBox.setEnabled(false);

        getInstanceInfoCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetInstanceInfo"));
        getInstanceInfoCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetInstanceInfo"));
        getInstanceInfoCheckBox.setEnabled(false);

        getMethodReturnCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetMethodReturn"));
        getMethodReturnCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetMethodReturn"));
        getMethodReturnCheckBox.setEnabled(false);

        getMonitorFrameInfoCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetMonitorFrame"));
        getMonitorFrameInfoCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetMonitorFrame"));
        getMonitorFrameInfoCheckBox.setEnabled(false);

        getMonitorInfoCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetMonitorInfo"));
        getMonitorInfoCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetMonitorInfo"));
        getMonitorInfoCheckBox.setEnabled(false);

        getOwnedMonitorInfoCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetOwnedMonitorInfo"));
        getOwnedMonitorInfoCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetOwnedMonitorInfo"));
        getOwnedMonitorInfoCheckBox.setEnabled(false);

        getSourceDebugExtensionCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetSourceDebugExtension"));
        getSourceDebugExtensionCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetSourceDebugExtension"));
        getSourceDebugExtensionCheckBox.setEnabled(false);

        getSyntheticAttributeCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetSyntheticAttribute"));
        getSyntheticAttributeCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetSyntheticAttribute"));
        getSyntheticAttributeCheckBox.setEnabled(false);

        popFramesCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanPopFrames"));
        popFramesCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanPopFrames"));
        popFramesCheckBox.setEnabled(false);

        redefineClassesCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanRedefineClasses"));
        redefineClassesCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanRedefineClasses"));
        redefineClassesCheckBox.setEnabled(false);

        requestMonitorEventCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetMonitorEvent"));
        requestMonitorEventCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetMonitorEvent"));
        requestMonitorEventCheckBox.setEnabled(false);

        requestVMDeathEventCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetVMDeathEvent"));
        requestVMDeathEventCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetVMDeathEvent"));
        requestVMDeathEventCheckBox.setEnabled(false);

        unrestrictedlyRedefineClassesCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanUnrestrictedlyRedefineClasses"));
        unrestrictedlyRedefineClassesCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanUnrestrictedlyRedefineClasses"));
        unrestrictedlyRedefineClassesCheckBox.setEnabled(false);

        useInstanceFiltersCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanUseInstanceFilters"));
        useInstanceFiltersCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanUseInstanceFilters"));
        useInstanceFiltersCheckBox.setEnabled(false);

        useSourceFiltersCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanUseSourceFilters"));
        useSourceFiltersCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanUseSourceFilters"));
        useSourceFiltersCheckBox.setEnabled(false);

        watchFieldAccessCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanWatchFieldAccess"));
        watchFieldAccessCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanWatchFieldAccess"));
        watchFieldAccessCheckBox.setEnabled(false);

        watchFieldModificationCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanWatchFieldModification"));
        watchFieldModificationCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanWatchFieldModification"));
        watchFieldModificationCheckBox.setEnabled(false);

        javax.swing.GroupLayout supportPanelLayout = new javax.swing.GroupLayout(supportPanel);
        supportPanel.setLayout(supportPanelLayout);
        supportPanelLayout.setHorizontalGroup(
            supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(supportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addMethodCheckBox)
                    .addComponent(beModifiedCheckBox)
                    .addComponent(forceEarlyReturnCheckBox)
                    .addComponent(getBytecodesCheckBox)
                    .addComponent(getGetClassFileVersionCheckBox)
                    .addComponent(getGetConstantPoolCheckBox)
                    .addComponent(getCurrentContendedMonitorCheckBox)
                    .addComponent(getInstanceInfoCheckBox)
                    .addComponent(getMethodReturnCheckBox)
                    .addComponent(getMonitorFrameInfoCheckBox)
                    .addComponent(getMonitorInfoCheckBox)
                    .addComponent(getOwnedMonitorInfoCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(getSourceDebugExtensionCheckBox)
                    .addComponent(getSyntheticAttributeCheckBox)
                    .addComponent(popFramesCheckBox)
                    .addComponent(redefineClassesCheckBox)
                    .addComponent(requestMonitorEventCheckBox)
                    .addComponent(requestVMDeathEventCheckBox)
                    .addComponent(unrestrictedlyRedefineClassesCheckBox)
                    .addComponent(useInstanceFiltersCheckBox)
                    .addComponent(useSourceFiltersCheckBox)
                    .addComponent(watchFieldAccessCheckBox)
                    .addComponent(watchFieldModificationCheckBox))
                .addGap(58, 58, 58))
        );

        supportPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {getSourceDebugExtensionCheckBox, getSyntheticAttributeCheckBox, popFramesCheckBox, redefineClassesCheckBox, requestMonitorEventCheckBox, requestVMDeathEventCheckBox, unrestrictedlyRedefineClassesCheckBox, useInstanceFiltersCheckBox, useSourceFiltersCheckBox, watchFieldAccessCheckBox, watchFieldModificationCheckBox});

        supportPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addMethodCheckBox, beModifiedCheckBox, forceEarlyReturnCheckBox, getBytecodesCheckBox, getCurrentContendedMonitorCheckBox, getGetClassFileVersionCheckBox, getGetConstantPoolCheckBox, getInstanceInfoCheckBox, getMethodReturnCheckBox, getMonitorFrameInfoCheckBox, getMonitorInfoCheckBox, getOwnedMonitorInfoCheckBox});

        supportPanelLayout.setVerticalGroup(
            supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(supportPanelLayout.createSequentialGroup()
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addMethodCheckBox)
                    .addComponent(getSourceDebugExtensionCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(beModifiedCheckBox)
                    .addComponent(getSyntheticAttributeCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(forceEarlyReturnCheckBox)
                    .addComponent(popFramesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getBytecodesCheckBox)
                    .addComponent(redefineClassesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getGetClassFileVersionCheckBox)
                    .addComponent(requestMonitorEventCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getGetConstantPoolCheckBox)
                    .addComponent(requestVMDeathEventCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getCurrentContendedMonitorCheckBox)
                    .addComponent(unrestrictedlyRedefineClassesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getInstanceInfoCheckBox)
                    .addComponent(useInstanceFiltersCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getMethodReturnCheckBox)
                    .addComponent(useSourceFiltersCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getMonitorFrameInfoCheckBox)
                    .addComponent(watchFieldAccessCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(supportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getMonitorInfoCheckBox)
                    .addComponent(watchFieldModificationCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(getOwnedMonitorInfoCheckBox))
        );

        supportPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addMethodCheckBox, beModifiedCheckBox, forceEarlyReturnCheckBox, getBytecodesCheckBox, getCurrentContendedMonitorCheckBox, getGetClassFileVersionCheckBox, getGetConstantPoolCheckBox, getInstanceInfoCheckBox, getMethodReturnCheckBox, getMonitorFrameInfoCheckBox, getMonitorInfoCheckBox, getOwnedMonitorInfoCheckBox, getSourceDebugExtensionCheckBox, getSyntheticAttributeCheckBox, popFramesCheckBox, redefineClassesCheckBox, requestMonitorEventCheckBox, requestVMDeathEventCheckBox, unrestrictedlyRedefineClassesCheckBox, useInstanceFiltersCheckBox, useSourceFiltersCheckBox, watchFieldAccessCheckBox, watchFieldModificationCheckBox});

        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_SupportTab"), supportPanel);

        add(tabbedPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addMethodCheckBox;
    private javax.swing.JCheckBox beModifiedCheckBox;
    private javax.swing.JCheckBox forceEarlyReturnCheckBox;
    private javax.swing.JCheckBox getBytecodesCheckBox;
    private javax.swing.JCheckBox getCurrentContendedMonitorCheckBox;
    private javax.swing.JCheckBox getGetClassFileVersionCheckBox;
    private javax.swing.JCheckBox getGetConstantPoolCheckBox;
    private javax.swing.JCheckBox getInstanceInfoCheckBox;
    private javax.swing.JCheckBox getMethodReturnCheckBox;
    private javax.swing.JCheckBox getMonitorFrameInfoCheckBox;
    private javax.swing.JCheckBox getMonitorInfoCheckBox;
    private javax.swing.JCheckBox getOwnedMonitorInfoCheckBox;
    private javax.swing.JCheckBox getSourceDebugExtensionCheckBox;
    private javax.swing.JCheckBox getSyntheticAttributeCheckBox;
    private javax.swing.JLabel jdiVersionLabel;
    private javax.swing.JTextField jdiVersionTextField;
    private javax.swing.JLabel jvmDescriptionLabel;
    private javax.swing.JScrollPane jvmDescriptionScrollPane;
    private javax.swing.JTextArea jvmDescriptionTextArea;
    private javax.swing.JLabel jvmVersionLabel;
    private javax.swing.JTextField jvmVersionTextField;
    private javax.swing.JCheckBox popFramesCheckBox;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JScrollPane propsScrollPane;
    private javax.swing.JTextArea propsTextArea;
    private javax.swing.JCheckBox redefineClassesCheckBox;
    private javax.swing.JCheckBox requestMonitorEventCheckBox;
    private javax.swing.JCheckBox requestVMDeathEventCheckBox;
    private javax.swing.JPanel supportPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox unrestrictedlyRedefineClassesCheckBox;
    private javax.swing.JCheckBox useInstanceFiltersCheckBox;
    private javax.swing.JCheckBox useSourceFiltersCheckBox;
    private javax.swing.JPanel versionPanel;
    private javax.swing.JCheckBox watchFieldAccessCheckBox;
    private javax.swing.JCheckBox watchFieldModificationCheckBox;
    // End of variables declaration//GEN-END:variables
}
