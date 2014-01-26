/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      JSwat UI
 * FILE:        GraphicalCommandAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/12/01        Moved from GraphicalAdapter.java
 *
 * DESCRIPTION:
 *      The user interface adapter class for graphical interface.
 *
 * $Id: GraphicalCommandAdapter.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.Log;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class GraphicalCommandAdapter adapts the input from a text field
 * and sends that input to the CommandManager.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalCommandAdapter implements ActionListener, KeyListener {
    /** Where input is sent. */
    protected CommandManager commandManager;
    /** Where input comes from. */
    protected JTextField inputField;
    /** Prompt label beside the input field. */
    protected JLabel inputPrompt;
    /** The text in the command input field when the user started
     * scrolling through the command history. Used to restore the
     * original command input. */
    protected String inputBeforeScroll;
    /** Log to which we print the typed commands for reference. */
    protected Log outputLog;
    /** Previous return value from commandManager.getInputPrompt(); */
    private String lastInputPrompt;

    /**
     * Constructs a GraphicalCommandAdapter that connects the given
     * input field to the given command manager.
     *
     * @param  input   input field.
     * @param  cmdman  CommandManager to send input to.
     * @param  log     Log to print to.
     */
    public GraphicalCommandAdapter(JTextField input,
                                   JLabel prompt,
                                   CommandManager cmdman,
                                   Log log) {
        inputField = input;
        inputPrompt = prompt;
        commandManager = cmdman;
        outputLog = log;
        input.addActionListener(this);
        input.addKeyListener(this);

        // force input prompt to be set for the first time
        lastInputPrompt = "";
        updatePrompt();
    } // GraphicalCommandAdapter

    /**
     * Text field was activated by user. Parse the input and
     * execute the command. Also clears the input field and
     * resets the "current history" value.
     *
     * @param  event  ActionEvent
     */
    public void actionPerformed(ActionEvent event) {
        String inputStr = inputField.getText().trim();
        if (inputStr.length() > 0) {
            // Write out the given command, for user reference.
            String prompt = commandManager.getInputPrompt();
            if(prompt == null) {
                prompt = "";
            }
            outputLog.writeln(prompt + "> " + inputStr);
            // Send input to command manager.
            commandManager.handleInput(inputStr);
        }
        // Clear the input text field for the next command.
        inputField.setText("");
        // Call updatePrompt to update the prompt, if necessary
        updatePrompt();
    } // actionPerformed

    /**
     * Change the prompt displayed beside the input field, if necessary.
     */
    protected void updatePrompt() {
        String newPrompt = commandManager.getInputPrompt();
        if (newPrompt != lastInputPrompt) {
            lastInputPrompt = newPrompt;
            if (newPrompt == null) {
                newPrompt = Bundle.getString("commandField");
            }
            inputPrompt.setText(newPrompt+":");
        }
    } // updatePrompt

    /**
     * Invoked when a key has been pressed in the input field.
     * We take this opportunity to implement the "previous" and
     * "next" command feature.
     *
     * @param  event  Key event.
     */
    public void keyPressed(KeyEvent event) {
        if (event.isConsumed() ||
            !(event.isControlDown() || event.isActionKey())) {
            // Already consumed or not a control or action key.
            return;
        }

        int ch = event.getKeyCode();
        if ((ch == KeyEvent.VK_N) || (ch == KeyEvent.VK_DOWN)) {
            // Save the command input, if necessary.
            if (inputBeforeScroll == null) {
                inputBeforeScroll = inputField.getText();
            }

            // Show the next command in the history.
            String next = commandManager.getHistoryNext();
            if (next == null) {
                inputField.setText(inputBeforeScroll);
                inputBeforeScroll = null;
            } else {
                inputField.setText(next);
            }
            event.consume();

        } else if ((ch == KeyEvent.VK_P) || (ch == KeyEvent.VK_UP)) {
            // Save the command input, if necessary.
            if (inputBeforeScroll == null) {
                inputBeforeScroll = inputField.getText();
            }

            // Show the previous command in the history.
            String prev = commandManager.getHistoryPrev();
            if (prev == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                inputField.setText(prev);
            }
            event.consume();
        }
    } // keyPressed

    /**
     * Ignored.
     */
    public void keyReleased(KeyEvent event) {}

    /**
     * Ignored.
     */
    public void keyTyped(KeyEvent event) {}
} // GraphicalCommandAdapter
