/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DebuggeeInfoPanel.java 15 2007-06-03 00:01:17Z nfiedler $
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
        String str = String.valueOf(vmm.majorInterfaceVersion()) + '.' +
            String.valueOf(vmm.minorInterfaceVersion());
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
            if (methods.size() == 0) {
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
                    if (methods.size() == 0) {
                        throw new IllegalArgumentException("no propertyNames() method");
                    }
                    method = methods.get(0);
                    ObjectReference iter = (ObjectReference) Classes.invokeMethod(
                        props, type, thread, method, EMTPY_ARGUMENTS);

                    ClassType iterType = (ClassType) iter.referenceType();
                    methods = iterType.methodsByName("hasMoreElements", "()Z");
                    if (methods.size() == 0) {
                        throw new IllegalArgumentException("no hasMoreElements() method");
                    }
                    Method hasNextMeth = methods.get(0);

                    methods = iterType.methodsByName("nextElement", "()Ljava/lang/Object;");
                    if (methods.size() == 0) {
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
                    if (methods.size() == 0) {
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
        getBytecodesCheckBox.setSelected(vm.canGetBytecodes());
        getCurrentContendedMonitorCheckBox.setSelected(vm.canGetCurrentContendedMonitor());
        getMonitorInfoCheckBox.setSelected(vm.canGetMonitorInfo());
        getOwnedMonitorInfoCheckBox.setSelected(vm.canGetOwnedMonitorInfo());
        getSourceDebugExtensionCheckBox.setSelected(vm.canGetSourceDebugExtension());
        getSyntheticAttributeCheckBox.setSelected(vm.canGetSyntheticAttribute());
        popFramesCheckBox.setSelected(vm.canPopFrames());
        redefineClassesCheckBox.setSelected(vm.canRedefineClasses());
        requestVMDeathEventCheckBox.setSelected(vm.canRequestVMDeathEvent());
        unrestrictedlyRedefineClassesCheckBox.setSelected(vm.canUnrestrictedlyRedefineClasses());
        useInstanceFiltersCheckBox.setSelected(vm.canUseInstanceFilters());
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
        java.awt.GridBagConstraints gridBagConstraints;

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
        getBytecodesCheckBox = new javax.swing.JCheckBox();
        getCurrentContendedMonitorCheckBox = new javax.swing.JCheckBox();
        getMonitorInfoCheckBox = new javax.swing.JCheckBox();
        getOwnedMonitorInfoCheckBox = new javax.swing.JCheckBox();
        getSourceDebugExtensionCheckBox = new javax.swing.JCheckBox();
        getSyntheticAttributeCheckBox = new javax.swing.JCheckBox();
        popFramesCheckBox = new javax.swing.JCheckBox();
        redefineClassesCheckBox = new javax.swing.JCheckBox();
        requestVMDeathEventCheckBox = new javax.swing.JCheckBox();
        unrestrictedlyRedefineClassesCheckBox = new javax.swing.JCheckBox();
        useInstanceFiltersCheckBox = new javax.swing.JCheckBox();
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

        org.jdesktop.layout.GroupLayout versionPanelLayout = new org.jdesktop.layout.GroupLayout(versionPanel);
        versionPanel.setLayout(versionPanelLayout);
        versionPanelLayout.setHorizontalGroup(
            versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(versionPanelLayout.createSequentialGroup()
                .add(versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jdiVersionLabel)
                    .add(jvmVersionLabel)
                    .add(jvmDescriptionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(versionPanelLayout.createSequentialGroup()
                        .add(versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(versionPanelLayout.createSequentialGroup()
                                .add(jvmVersionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(versionPanelLayout.createSequentialGroup()
                                .add(jdiVersionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .addContainerGap())
                    .add(jvmDescriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)))
        );
        versionPanelLayout.setVerticalGroup(
            versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(versionPanelLayout.createSequentialGroup()
                .add(versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jdiVersionLabel)
                    .add(jdiVersionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jvmVersionLabel)
                    .add(jvmVersionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(versionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(versionPanelLayout.createSequentialGroup()
                        .add(jvmDescriptionLabel)
                        .addContainerGap(184, Short.MAX_VALUE))
                    .add(jvmDescriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)))
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_VersionsTab"), versionPanel);

        propertiesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        propsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        propsTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
        propsTextArea.setColumns(40);
        propsTextArea.setEditable(false);
        propsTextArea.setRows(10);
        propsScrollPane.setViewportView(propsTextArea);

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_PropsTab"), propertiesPanel);

        supportPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addMethodCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanAddMethod"));
        addMethodCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanAddMethod"));
        addMethodCheckBox.setEnabled(false);

        beModifiedCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanBeModified"));
        beModifiedCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanBeModified"));
        beModifiedCheckBox.setEnabled(false);

        getBytecodesCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetBytecodes"));
        getBytecodesCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetBytecodes"));
        getBytecodesCheckBox.setEnabled(false);

        getCurrentContendedMonitorCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetCurrentContentedMonitor"));
        getCurrentContendedMonitorCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetCurrentContentedMonitor"));
        getCurrentContendedMonitorCheckBox.setEnabled(false);

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

        requestVMDeathEventCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanGetVMDeathEvent"));
        requestVMDeathEventCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanGetVMDeathEvent"));
        requestVMDeathEventCheckBox.setEnabled(false);

        unrestrictedlyRedefineClassesCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanUnrestrictedlyRedefineClasses"));
        unrestrictedlyRedefineClassesCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanUnrestrictedlyRedefineClasses"));
        unrestrictedlyRedefineClassesCheckBox.setEnabled(false);

        useInstanceFiltersCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanUseInstanceFilters"));
        useInstanceFiltersCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanUseInstanceFilters"));
        useInstanceFiltersCheckBox.setEnabled(false);

        watchFieldAccessCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanWatchFieldAccess"));
        watchFieldAccessCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanWatchFieldAccess"));
        watchFieldAccessCheckBox.setEnabled(false);

        watchFieldModificationCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_CanWatchFieldModification"));
        watchFieldModificationCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("HINT_DebuggeeInfo_CanWatchFieldModification"));
        watchFieldModificationCheckBox.setEnabled(false);

        org.jdesktop.layout.GroupLayout supportPanelLayout = new org.jdesktop.layout.GroupLayout(supportPanel);
        supportPanel.setLayout(supportPanelLayout);
        supportPanelLayout.setHorizontalGroup(
            supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(supportPanelLayout.createSequentialGroup()
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(supportPanelLayout.createSequentialGroup()
                        .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(addMethodCheckBox)
                            .add(beModifiedCheckBox)
                            .add(getBytecodesCheckBox)
                            .add(getCurrentContendedMonitorCheckBox)
                            .add(getMonitorInfoCheckBox)
                            .add(getOwnedMonitorInfoCheckBox)
                            .add(getSourceDebugExtensionCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(popFramesCheckBox)
                            .add(redefineClassesCheckBox)
                            .add(requestVMDeathEventCheckBox)
                            .add(unrestrictedlyRedefineClassesCheckBox)
                            .add(useInstanceFiltersCheckBox)
                            .add(watchFieldAccessCheckBox)
                            .add(watchFieldModificationCheckBox)))
                    .add(getSyntheticAttributeCheckBox))
                .addContainerGap())
        );
        supportPanelLayout.setVerticalGroup(
            supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(supportPanelLayout.createSequentialGroup()
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addMethodCheckBox)
                    .add(popFramesCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(beModifiedCheckBox)
                    .add(redefineClassesCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(getBytecodesCheckBox)
                    .add(requestVMDeathEventCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(getCurrentContendedMonitorCheckBox)
                    .add(unrestrictedlyRedefineClassesCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(getMonitorInfoCheckBox)
                    .add(useInstanceFiltersCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(getOwnedMonitorInfoCheckBox)
                    .add(watchFieldAccessCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(supportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(getSourceDebugExtensionCheckBox)
                    .add(watchFieldModificationCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(getSyntheticAttributeCheckBox)
                .addContainerGap())
        );
        tabbedPane.addTab(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/components/Forms").getString("LBL_DebuggeeInfo_SupportTab"), supportPanel);

        add(tabbedPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addMethodCheckBox;
    private javax.swing.JCheckBox beModifiedCheckBox;
    private javax.swing.JCheckBox getBytecodesCheckBox;
    private javax.swing.JCheckBox getCurrentContendedMonitorCheckBox;
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
    private javax.swing.JCheckBox requestVMDeathEventCheckBox;
    private javax.swing.JPanel supportPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox unrestrictedlyRedefineClassesCheckBox;
    private javax.swing.JCheckBox useInstanceFiltersCheckBox;
    private javax.swing.JPanel versionPanel;
    private javax.swing.JCheckBox watchFieldAccessCheckBox;
    private javax.swing.JCheckBox watchFieldModificationCheckBox;
    // End of variables declaration//GEN-END:variables
}
