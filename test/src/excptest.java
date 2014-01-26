// An exception test for JSwat.
// $Id: excptest.java 1814 2005-07-17 05:56:32Z nfiedler $

import java.awt.*;
import java.awt.event.*;

public class excptest implements ActionListener {
    Button npeButton;
    Button nmeButton;
    Button uceButton;
    Button sizeButton;
    Frame fr;

    /**
     * Tests exception handling.
     */
    public excptest(int width, int height) {
	fr = new Frame("tester");
        fr.setLayout(new GridLayout(4, 1));

        npeButton = new Button("Null ptr exception");
        fr.add(npeButton);
        npeButton.addActionListener(this);

        nmeButton = new Button("Exception w/o msg");
        fr.add(nmeButton);
        nmeButton.addActionListener(this);

        uceButton = new Button("Uncaught exception");
        fr.add(uceButton);
        uceButton.addActionListener(this);

        sizeButton = new Button("Window dimensions");
        fr.add(sizeButton);
        sizeButton.addActionListener(this);

        fr.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
                    System.out.print("w = ");
                    System.out.print(fr.getWidth());
                    System.out.print(", h = ");
                    System.out.println(fr.getHeight());
		    System.exit(0);
		}
	    });
        fr.setSize(width, height);
        fr.setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == npeButton) {
            throw new NullPointerException("test exception");
        } else if (src == sizeButton) {
            Insets insets = fr.getInsets();
            System.out.print("Insets (t,l,b,r): ");
            System.out.print(insets.top);
            System.out.print(", ");
            System.out.print(insets.left);
            System.out.print(", ");
            System.out.print(insets.bottom);
            System.out.print(", ");
            System.out.println(insets.right);

            System.out.print("Dimensions (x,y,w,h): ");
            System.out.print(fr.getX());
            System.out.print(", ");
            System.out.print(fr.getY());
            System.out.print(", ");
            System.out.print(fr.getWidth());
            System.out.print(", ");
            System.out.println(fr.getHeight());
        } else if (src == nmeButton) {
            throw new MyException();
        } else {
            Thread th = new Thread(new Runnable() {
                    public void run() {
                        throw new NullPointerException("uncaught exception");
                    }
                });
            th.start();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            new excptest(200, 200);
        } else {
            try {
                int w = Integer.parseInt(args[0]);
                int h = Integer.parseInt(args[1]);
                new excptest(w, h);
            } catch (NumberFormatException nfe) {
                System.err.println("The first two arguments must be "+
                                   "integers, specifying width and height.");
            }
        }
    }
}

class MyException extends RuntimeException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    public String getMessage() {
        return null;
    }
}
