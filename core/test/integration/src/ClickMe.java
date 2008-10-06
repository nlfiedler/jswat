/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClickMe.java 15 2007-06-03 00:01:17Z nfiedler $
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
