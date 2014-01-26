// JSwat Tutorial example program.
// $Id: tutorial.java 1989 2005-09-04 07:39:30Z nfiedler $

import java.awt.*;
import java.awt.event.*;

public class tutorial implements ActionListener {
    /** Number of times the button was pushed. */
    protected int pushCount;

    public void actionPerformed(ActionEvent e) {
        pushCount++;
        Button button = (Button) e.getSource();
        StringBuffer label = new StringBuffer("Pushed ");
        label.append(Integer.toString(pushCount));
        label.append(" times");
        button.setLabel(label.toString());
    }

    protected Frame buildWindow(String title) {
	Frame fr = new Frame(title);
        fr.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
        fr.setSize(150, 100);
        return fr;
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        tutorial me = new tutorial();
        Frame frame = me.buildWindow("tutorial");
        Button button = new Button("Push me");
        button.addActionListener(me);
        frame.add(button);
        frame.setVisible(true);
    }
}
