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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

import java.awt.Button;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Class that creates a window containing a button and waits for actions.
 *
 * @author  Nathan Fiedler
 */
public class ClickMe implements ActionListener {
    /** Number of times the button was pushed. */
    protected int pushCount;

    /**
     * A button was pressed.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        pushCount++;
        Button button = (Button) e.getSource();
        StringBuffer label = new StringBuffer("Pushed ");
        label.append(Integer.toString(pushCount));
        label.append(" times");
        button.setLabel(label.toString());
    }

    /**
     * Build the main window.
     *
     * @param  title  window title.
     * @return  new window.
     */
    protected Frame buildWindow(String title) {
	Frame fr = new Frame(title);
        fr.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
        fr.setSize(180, 120);
        fr.setLocation(100, 100);
        return fr;
    }

    /**
     * Invoked from command-line.
     *
     * @param  args  command-line arguments.
     */
    public static void main(String[] args) {
        ClickMe me = new ClickMe();
        final Frame frame = me.buildWindow("Click me!");
        Button button = new Button("Click me!");
        button.addActionListener(me);
        frame.add(button);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }
}
