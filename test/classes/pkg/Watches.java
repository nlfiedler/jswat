// JSwat test class for watchpoints.
// $Id: Watches.java 14 2007-06-02 23:50:55Z nfiedler $

package pkg;

import java.util.ArrayList;

public class Watches {

    public void runtest() {
        // Populate the test list.
        ArrayList list = new ArrayList();
        for (int ii = 0; ii < 20; ii++) {
            MutableInteger mi = new MutableInteger(ii);
            list.add(mi); // unit tests stop here
        }

        // Modify the values to cause modification events.
        for (int ii = 0; ii < list.size(); ii++) {
            MutableInteger mi = (MutableInteger) list.get(ii);
            mi.setValue(mi.getValue() * 2);
        }
    }

    public static void main(String[] args) {
        Watches me = new Watches();
        me.runtest();
    }

}

// This is defined outside of Watches, and is non-public, of course,
// for the sake of testing the PathManager's find source function.
class MutableInteger {
    private int value;

    public MutableInteger(int i) {
        value = i;
    }

    public int getValue() {
        return value; // unit tests stop here
    }

    public void setValue(int i) {
        value = i;
    }
}
