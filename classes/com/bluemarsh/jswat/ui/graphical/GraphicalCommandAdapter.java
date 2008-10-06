/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * $Id: GraphicalCommandAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.ui.Bundle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class GraphicalCommandAdapter reads the input from a text field and
 * sends it to the CommandManager to be processed.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalCommandAdapter implements ActionListener, KeyListener {
    /** Where command input comes from. */
    private JTextField inputField;
    /** Where command input goes to. */
    private CommandManager commandManager;
    /** Prompt label beside the input field. */
    private JLabel inputLabel;
    /** The command input prompt string, must not be null. */
    private String inputPrompt;
    /** The text in the command input field when the user started
     * scrolling through the command history. Used to restore the
     * original command input. */
    private String inputBeforeScroll;
    /** Log to which we print the typed commands for reference. */
    private Log outputLog;

    /**
     * Constructs a GraphicalCommandAdapter that connects the given
     * input field to the given command manager.
     *
     * @param  input   input field to read from.
     * @param  prompt  label for displaying input prompt.
     * @param  cmdman  CommandManager to send input to.
     * @param  log     Log to print to.
     */
    public GraphicalCommandAdapter(JTextField input, JLabel prompt,
                                   CommandManager cmdman, Log log) {
        inputField = input;
        inputLabel = prompt;
        commandManager = cmdman;
        outputLog = log;
        input.addActionListener(this);
        input.addKeyListener(this);

        updateInputPrompt(null);
        inputLabel.setLabelFor(inputField);
    } // GraphicalCommandAdapter

    /**
     * Text field was activated by user. Parse the input and execute the
     * command. Clears the input field.
     *
     * @param  event  ActionEvent
     */
    public void actionPerformed(ActionEvent event) {
        String inputStr = inputField.getText().trim();
        if (inputStr.length() > 0) {
            // Write out the given command, for user reference.
            outputLog.writeln(inputPrompt + "> " + inputStr);
            // Send input to command manager.
            try {
                commandManager.handleInput(inputStr);
            } catch (Exception e) {
                outputLog.writeStackTrace(e);
            }
        }
        // Clear the input text field for the next command.
        inputField.setText("");
    } // actionPerformed

    /**
     * Change the prompt displayed beside the command input field.
     *
     * @param  prompt  new input prompt, or null to display default.
     */
    public void updateInputPrompt(String prompt) {
        if (prompt == null) {
            inputPrompt = "";
            prompt = Bundle.getString("commandField");
            inputLabel.setText(prompt + ':');
            String mnemonic = Bundle.getString("commandFieldMnemonic");
            if (mnemonic != null && mnemonic.length() > 0) {
                inputLabel.setDisplayedMnemonic(mnemonic.charAt(0));
            }
        } else {
            inputPrompt = prompt;
            inputLabel.setText(prompt + ':');
        }
    } // updateInputPrompt

    /**
     * Invoked when a key has been pressed in the input field. We take
     * this opportunity to implement the "previous" and "next" command
     * feature.
     *
     * @param  event  Key event.
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
            String next = commandManager.getHistoryNext();
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
     * Invoked when a key has been released.
     *
     * @param  event  key event.
     */
    public void keyReleased(KeyEvent event) {
        // ignored
    } // keyReleased

    /**
     * Invoked when a key has been pressed and released.
     *
     * @param  event  key event.
     */
    public void keyTyped(KeyEvent event) {
        // ignored
    } // keyTyped
} // GraphicalCommandAdapter
