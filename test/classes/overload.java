// A sample class to test the debugger.
// $Id: overload.java 14 2007-06-02 23:50:55Z nfiedler $

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class overload implements ActionListener {
    private JFrame mainWin;
    private JButton button1;
    private JButton button2;

    public overload() {
        mainWin = new JFrame("b tester");
        Container pane = mainWin.getContentPane();
        pane.setLayout(new BorderLayout());

        button1 = new JButton("Push me");
        pane.add(button1, "North");
        button1.addActionListener(this);

        button2 = new JButton("No, push me instead");
        pane.add(button2, "South");
        button2.addActionListener(this);

        mainWin.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
        mainWin.setSize(300, 100);
        mainWin.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        Object button = e.getSource();
        if (button == button1) {
            overloadedMethod();
        } else {
            overloadedMethod("Second button was pushed");
        }
    }

    public void overloadedMethod() {
        System.out.println("First button was pushed.");
    }

    public void overloadedMethod(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        new overload();
    }
}
