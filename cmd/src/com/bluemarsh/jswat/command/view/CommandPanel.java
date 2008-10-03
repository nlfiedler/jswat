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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandPanel.java 29 2008-06-30 00:41:09Z nfiedler $
 */

package com.bluemarsh.jswat.command.view;

import com.bluemarsh.jswat.command.CommandParser;
import com.bluemarsh.jswat.core.actions.Actions;
import com.bluemarsh.jswat.core.actions.ClearAction;
import com.bluemarsh.jswat.core.actions.CopyAction;
import com.bluemarsh.jswat.core.actions.CutAction;
import com.bluemarsh.jswat.core.actions.PasteAction;
import com.bluemarsh.jswat.core.util.Threads;
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.concurrent.Future;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Displays an input field for receiving commands and a text area for
 * showing the results of the command processing.
 *
 * @author  Nathan Fiedler
 */
public class CommandPanel extends JPanel implements Runnable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The future reading from the reader and printing to the text area. */
    private transient Future readerFuture;

    /**
     * Creates new form CommandPanel.
     */
    public CommandPanel() {
        initComponents();
        // Set the DefaultCaret update policy to 'never'.
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        outputTextArea.setCaret(caret);
        new TextEnhancer(outputScrollPane, outputTextArea);

        // Set the actions for the output text area.
        Action[] actions = new Action[] {
            new CutAction(),
            new CopyAction(),
            new PasteAction(),
            new ClearAction(),
        };
        Actions.attachActions(actions, outputTextArea, inputTextField);
        Actions.attachShortcuts(actions, this);

        EventQueue.invokeLater(this);
    }

    /**
     * This component is being closed.
     */
    void closing() {
        // Interrupt the running task to make it stop.
        readerFuture.cancel(true);
    }

    /**
     * Run in event queue to connect the input/output readers.
     */
    public void run() {
        // Perform lookup to find the CommandParser instance.
        CommandParser parser = Lookup.getDefault().lookup(CommandParser.class);
        if (parser != null) {
            PipedWriter pwriter = new PipedWriter();
            PipedReader preader = new PipedReader();
            try {
                preader.connect(pwriter);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return;
            }
            PrintWriter printWriter = new PrintWriter(pwriter);
            parser.setOutput(printWriter);
            new CommandInputAdapter(inputTextField, parser, printWriter);

            OutputReader or = new OutputReader(preader);
            readerFuture = Threads.getThreadPool().submit(or);
        } else {
            ErrorManager.getDefault().log(ErrorManager.ERROR,
                    "No CommandParser defined!");
        }
    }

    /**
     * Adds a few additional features to a plain JTextArea.
     *
     * @author  Nathan Fiedler
     */
    private static class TextEnhancer implements ComponentListener {
        /** Vertical scrollbar for the text area for auto-scrolling. */
        private JScrollBar verticalScrollBar;
        /** Horizontal scrollbar for the text area for auto-scrolling. */
        private JScrollBar horizontalScrollBar;
        /** Runnable to scroll the text area down. */
        private Runnable downScroller;

        /**
         * Creates a new instance of TextEnhancer.
         *
         * @param  scrollPane  scroll pane to manage.
         * @param  textArea    text area to watch.
         */
        public TextEnhancer(JScrollPane scrollPane, JTextArea textArea) {
            super();
            verticalScrollBar = scrollPane.getVerticalScrollBar();
            horizontalScrollBar = scrollPane.getHorizontalScrollBar();
            // Become a component listener so that we can scroll.
            textArea.addComponentListener(this);
            downScroller = new Runnable() {
                    public void run() {
                        if (verticalScrollBar != null) {
                            verticalScrollBar.setValue(
                                verticalScrollBar.getMaximum());
                        }
                        if (horizontalScrollBar != null) {
                            horizontalScrollBar.setValue(
                                horizontalScrollBar.getMinimum());
                        }
                    }
                };
        }

        /**
         * Invoked when the component has been made invisible.
         *
         * @param  e component event.
         */
        public void componentHidden(ComponentEvent e) {
        }

        /**
         * Invoked when the component's position changes.
         *
         * @param  e component event.
         */
        public void componentMoved(ComponentEvent e) {
        }

        /**
         * Invoked when the component's size changes.
         *
         * @param  e component event.
         */
        public void componentResized(ComponentEvent e) {
            // Scroll the text area once it has physically grown larger.
            EventQueue.invokeLater(downScroller);
        }

        /**
         * Invoked when the componenthas been made visible.
         *
         * @param  e component event.
         */
        public void componentShown(ComponentEvent e) {
        }
    }

    /**
     * Reads from a Reader and writes to a text area.
     *
     * @author  Nathan Fiedler
     */
    private class OutputReader implements Runnable {
        /** Reader from whence output is read. */
        private Reader reader;

        /**
         * Creates a new instance of OutputReader.
         *
         * @param  reader  from which output is read.
         */
        public OutputReader(Reader reader) {
            this.reader = reader;
        }

        /**
         * Reads from the output reader and sends everything to the text area.
         */
        public void run() {
            char[] buf = new char[8192];
            // Run until we are interrupted.
            while (true) {
                try {
                    int len = reader.read(buf);
                    if (len == -1) {
                        // The writer has closed.
                        break;
                    } else if (len > 0) {
                        String str = new String(buf, 0, len);
                        outputTextArea.append(str);
                    }
                } catch (IOException ioe) {
                    // This includes being interrupted.
                    break;
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        inputLabel = new javax.swing.JLabel();
        inputTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        outputScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        outputTextArea.setEditable(false);
        outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 11));
        outputScrollPane.setViewportView(outputTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        add(outputScrollPane, gridBagConstraints);

        inputLabel.setLabelFor(inputTextField);
        inputLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/command/view/Form").getString("LBL_Command_Input"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 12);
        add(inputLabel, gridBagConstraints);

        inputTextField.setFont(new java.awt.Font("Monospaced", 0, 11));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        add(inputTextField, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel inputLabel;
    private javax.swing.JTextField inputTextField;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    // End of variables declaration//GEN-END:variables
}
