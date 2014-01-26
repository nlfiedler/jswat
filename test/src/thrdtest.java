// A threading test for JSwat.
// $Id: thrdtest.java 1814 2005-07-17 05:56:32Z nfiedler $

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Thread test bed.
 *
 * @author  Nathan Fiedler
 */
public class thrdtest implements ActionListener, Runnable {

    /**
     * Create the thread test window.
     */
    public thrdtest() {
        JFrame frame = new JFrame("thread tester");
        Container pane = frame.getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        pane.setLayout(gbl);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        JButton button = new JButton("Start 10 threads");
        button.setActionCommand("makeTen");
        button.addActionListener(this);
        gbl.setConstraints(button, gbc);
        pane.add(button);

        button = new JButton("Start 'ThrdA' thread");
        button.setActionCommand("makeThrdA");
        button.addActionListener(this);
        gbl.setConstraints(button, gbc);
        pane.add(button);

        button = new JButton("Start 'ThrdB' thread");
        button.setActionCommand("makeThrdB");
        button.addActionListener(this);
        gbl.setConstraints(button, gbc);
        pane.add(button);

//         button = new JButton("Stop test");
//         button.setActionCommand("stopTest");
//         button.addActionListener(this);
//         gbl.setConstraints(button, gbc);
//         pane.add(button);

        frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("makeTen")) {
            // Create 10 threads to run for a while.
            for (int i = 0; i < 10; i++) {
                Thread th = new Thread(this);
                th.start();
            }
        } else if (cmd.equals("makeThrdA")) {
            Thread th = new Thread(this, "ThrdA");
            th.start();
        } else if (cmd.equals("makeThrdB")) {
            Thread th = new Thread(this, "ThrdB");
            th.start();
//         } else if (cmd.equals("stopTest")) {
//             Thread th = new Thread(this, "Victim");
//             th.start();
//             th.stop();
        }
    }

    /**
     * Sleep for somewhere between 1 and 20 seconds.
     */
    public void run() {
        // Pick random time from 1 to 20 seconds and sleep that long.
        long delay = (long) (Math.random() * 19000) + 1000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) { }
    }

    /**
     * Main method.
     *
     * @param  args  command-line arguments.
     */
    public static void main(String[] args) {
        new thrdtest();
    }
}
