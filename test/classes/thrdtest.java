// A threading test for JSwat.
// $Id: thrdtest.java 14 2007-06-02 23:50:55Z nfiedler $

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Thread test bed.
 *
 * @author  Nathan Fiedler
 */
public class thrdtest implements ActionListener {

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

        button = new JButton("Start thread in group");
        button.setActionCommand("makeGrpThrd");
        button.addActionListener(this);
        gbl.setConstraints(button, gbc);
        pane.add(button);

        button = new JButton("Start no-name thread");
        button.setActionCommand("makeNomanThrd");
        button.addActionListener(this);
        gbl.setConstraints(button, gbc);
        pane.add(button);

        button = new JButton("Start many threads");
        button.setActionCommand("makeManyThrds");
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
                Thread th = new Thread(new Sleeper());
                th.start();
            }
        } else if (cmd.equals("makeThrdA")) {
            new Thread(new Sleeper(), "ThrdA").start();
        } else if (cmd.equals("makeThrdB")) {
            new Thread(new Sleeper(), "ThrdB").start();
        } else if (cmd.equals("makeNomanThrd")) {
            new Thread(new Sleeper(), "").start();
        } else if (cmd.equals("makeManyThrds")) {
            manyThreads();
        } else if (cmd.equals("makeGrpThrd")) {
            new Thread(new ThreadGroup("group-1"), new Sleeper(),
                       "thread-1").start();
//         } else if (cmd.equals("stopTest")) {
//             Thread th = new Thread(this, "Victim");
//             th.start();
//             th.stop();
        }
    }

    protected void manyThreads() {
        // Spin this off to another thread.
        Thread th = new Thread(new Runnable() {
                public void run() {
                    // Spawn a bunch of long-lived threads.
                    for (int i = 0; i < 200; i++) {
                        new Thread(new Sleeper(180000, 120000)).start();
                    }
                    // Then create short-lived threads frequently.
                    while (true) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            // ignored
                        }
                        new Thread(new Sleeper(500, 500)).start();
                    }
                }
            });
        th.start();
    }

    public static void main(String[] args) {
        new thrdtest();
    }

    /**
     * Sleeps for some amount of time.
     */
    protected class Sleeper implements Runnable {
        private int base;
        private int duration;

        public Sleeper() {
            // Default to 1 to 20 seconds.
            base = 1000;
            duration = 19000;
        }

        public Sleeper(int base, int duration) {
            this.base = base;
            this.duration = duration;
        }

        public void run() {
            // Pick random time and sleep that long.
            long delay = (long) (Math.random() * duration) + base;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                // ignored
            }
        }
    }
}
