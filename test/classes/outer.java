// An inner class test for JSwat.
// $Id: outer.java 14 2007-06-02 23:50:55Z nfiedler $

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Test for inner class handling.
 *
 * @author  Nathan Fiedler
 */
public class outer implements ActionListener {
    /** inner class instance */
    protected inner inside;

    /**
     * Constructor.
     */
    public outer() {
        inside = new inner();

        JFrame mainWin = new JFrame("inner class tester");
        Container pane = mainWin.getContentPane();

        JButton button1 = new JButton("Push me");
        pane.add(button1);
        button1.addActionListener(this);

        mainWin.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
                    System.out.println("exiting tester...");
		    System.exit(0);
		}
	    });
        mainWin.setSize(200, 100);
        mainWin.setVisible(true);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        inside.pushed();
        outside.amethod(10, 20);
    }

    /**
     * Main method.
     */
    public static void main(String[] args) {
        new outer();
    }

    /**
     * The inner class.
     */
    protected class inner {
        public void pushed() {
            System.out.println("button pushed");
        }
    }; // parser test
}

// Non-public class defined in the same source file to test possible
// bug with regards to the source code single-stepping highlighter.
class outside {

    /**
     * A test method.
     */
    public static int amethod(int i, int j) {
        int k = i * j;
        i++;
        j++;
        k += i + j;
        return k;
    }
}; // parser test
