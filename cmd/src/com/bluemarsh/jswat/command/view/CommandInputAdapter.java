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
 * are Copyright (C) 2001-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command.view;

import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.CommandParser;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import javax.swing.JTextField;
import org.openide.util.NbBundle;

/**
 * Class CommandInputAdapter reads the input from a text field and sends
 * it to a CommandParser to be processed.
 *
 * @author  Nathan Fiedler
 */
public class CommandInputAdapter implements ActionListener, KeyListener {
    /** Where command input comes from. */
    private JTextField inputField;
    /** Where error messages are written, if needed. */
    private PrintWriter outputWriter;
    /** Where command input is sent. */
    private CommandParser commandParser;
    /** The text in the command input field when the user started
     * scrolling through the command history. Used to restore the
     * original command input. */
    private String inputBeforeScroll;

    /**
     * Creates a new instance of CommandInputAdapter.
     *
     * @param  input   input field to read from.
     * @param  parser  CommandParser to which input is sent.
     * @param  writer  where input is echoed and error messages are shown.
     */
    public CommandInputAdapter(JTextField input, CommandParser parser,
            PrintWriter writer) {
        inputField = input;
        commandParser = parser;
        outputWriter = writer;
        input.addActionListener(this);
        input.addKeyListener(this);
    }

    /**
     * Text field was activated by user. Parse the input and execute the
     * command, then clear the input field to receive more input.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        String input = inputField.getText().trim();
        if (input.length() > 0) {
            // Echoing the input is useful for separating the output
            // from one command to the next.
            outputWriter.print("# ");
            outputWriter.println(input);
            try {
                commandParser.parseInput(input);
            } catch (MissingArgumentsException mae) {
                outputWriter.println(mae.getMessage());
                outputWriter.println(NbBundle.getMessage(CommandInputAdapter.class,
                        "ERR_CommandInputAdapter_HelpCommand"));
            } catch (CommandException ce) {
                outputWriter.println(ce.getMessage());
                Throwable cause = ce.getCause();
                if (cause != null) {
                    String cmsg = cause.getMessage();
                    if (cmsg != null) {
                        outputWriter.println(cmsg);
                    }
                }
            }
        }
        inputField.setText("");
    }

    /**
     * Invoked when a key has been pressed in the input field. We take this
     * opportunity to implement the "previous" and "next" command feature.
     *
     * @param  event  key event.
     */
    public void keyPressed(KeyEvent event) {
        if (event.isConsumed()
            || !(event.isControlDown() || event.isActionKey())) {
            // Already consumed or not a control or action key.
            return;
        }

        int ch = event.getKeyCode();
        if (ch == KeyEvent.VK_N || ch == KeyEvent.VK_DOWN) {
            // Save the command input, if necessary.
            if (inputBeforeScroll == null) {
                inputBeforeScroll = inputField.getText();
            }

            // Show the next command in the history.
            String next = commandParser.getHistoryNext();
            if (next == null) {
                inputField.setText(inputBeforeScroll);
                inputBeforeScroll = null;
            } else {
                inputField.setText(next);
            }
            event.consume();

        } else if (ch == KeyEvent.VK_P || ch == KeyEvent.VK_UP) {
            // Save the command input, if necessary.
            if (inputBeforeScroll == null) {
                inputBeforeScroll = inputField.getText();
            }

            // Show the previous command in the history.
            String prev = commandParser.getHistoryPrev();
            if (prev == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                inputField.setText(prev);
            }
            event.consume();
        }
    }

    /**
     * Invoked when a key has been released.
     *
     * @param  event  key event.
     */
    public void keyReleased(KeyEvent event) {
    }

    /**
     * Invoked when a key has been pressed and released.
     *
     * @param  event  key event.
     */
    public void keyTyped(KeyEvent event) {
    }
}
