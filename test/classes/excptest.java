// An exception test for JSwat.
// $Id: excptest.java 14 2007-06-02 23:50:55Z nfiedler $

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import javax.swing.*;

public class excptest implements ActionListener {
    JButton npeButton;
    JButton rteButton;
    JButton meButton;
    JButton uceButton;
    JButton iteButton;

    public excptest() {
	JFrame fr = new JFrame("exception test");
        Container pane = fr.getContentPane();
        pane.setLayout(new GridLayout(5, 1));

        npeButton = new JButton("Null ptr exception");
        pane.add(npeButton);
        npeButton.addActionListener(this);

        rteButton = new JButton("Runtime Exception w/o msg");
        pane.add(rteButton);
        rteButton.addActionListener(this);

        meButton = new JButton("Exception w/o msg");
        pane.add(meButton);
        meButton.addActionListener(this);

        uceButton = new JButton("Uncaught exception");
        pane.add(uceButton);
        uceButton.addActionListener(this);

        iteButton = new JButton("Invocation exception");
        pane.add(iteButton);
        iteButton.addActionListener(this);

        fr.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
        fr.setSize(240, 200);
        fr.setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            Object src = ae.getSource();
            if (src == npeButton) {
                throw new NullPointerException("test exception");
            } else if (src == rteButton) {
                throw new MyRuntimeException();
            } else if (src == meButton) {
                throw new MyException();
            } else if (src == iteButton) {
                Target t = new Target();
                Method m = Target.class.getMethod("doIt", null);
                Object o = m.invoke(t, null);
                System.out.println(o);
            } else {
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        throw new NullPointerException("uncaught exception");
                    }
                });
                th.start();
            }
        } catch (Throwable t) {
            System.out.println(t);
            if (t.getCause() != null) {
                System.out.println("Caused by: " + t.getCause());
            }
        }
    }

    public static void main(String[] args) {
        new excptest();
    }
}

class MyRuntimeException extends RuntimeException {

    public String getMessage() {
        return null;
    }
}

class MyException extends Exception {

    public String getMessage() {
        return null;
    }
}

class Target {
    
    public int doIt() {
        // intentionally cause an exception
        return 10 / 0;
    }
}
