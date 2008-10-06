// Contributed breakpoint setting test case (bug 668)
// Should be able to set breakpoints at every line with
// Logger in it. Should resolve to the correct inner class.
// $Id: innerbrk.java 14 2007-06-02 23:50:55Z nfiedler $

import java.util.logging.Logger;

public class innerbrk {

    interface D {
        void function();
    }

    static public void main(String[] args) {
        new innerbrk();
        new innerbrk2();
        new innerbrk3();
    }

    public innerbrk() {
        new A().new C().function();
        new B().new C().function();
    }

    static class A {
        A() {
            Logger.global.info("innerbrk$A()");
            function();
            D d = new D() {
                    public void function() {
                        Logger.global.info("innerbrk$1.function");
                    }
                };
            d.function();

        }
        void function() {
            Logger.global.info("innerbrk$A.function");
        }
        class C {
            void function() {
                Logger.global.info("innerbrk$A$C.function");
            }

        }
    }

    static class B {
        B() {
            Logger.global.info("innerbrk$B()");
            function();
            D d = new D() {
                    public void function() {
                        Logger.global.info("innerbrk$2.function");
                    }
                };
            d.function();
        }

        void function() {
            Logger.global.info("innerbrk$B.function");
        }

        class C {
            void function() {
                Logger.global.info("innerbrk$B$C.function");
            }
        }
    }
}

class innerbrk2 {

    interface D {
        void function();
    }

    static public void main(String[] args) {
        new innerbrk2();
    }

    public innerbrk2() {
        new A().new C().function();
        new B().new C().function();
    }

    static class A {
        A() {
            Logger.global.info("innerbrk2$A()");
            function();
            D d = new D() {
                    public void function() {
                        Logger.global.info("innerbrk2$1.function");
                    }
                };
            d.function();

        }
        void function() {
            Logger.global.info("innerbrk2$A.function");
        }
        class C {
            void function() {
                Logger.global.info("innerbrk2$A$C.function");
            }

        }
    }

    static class B {
        B() {
            Logger.global.info("innerbrk2$B()");
            function();
            D d = new D() {
                    public void function() {
                        Logger.global.info("innerbrk2$2.function");
                    }
                };
            d.function();
        }

        void function() {
            Logger.global.info("innerbrk2$B.function");
        }

        class C {
            void function() {
                Logger.global.info("innerbrk2$B$C.function");
            }
        }
    }
}

class innerbrk3 {

    static class A {
        void start() {
            Logger.global.info("innerbrk3$A.start");
        }
    }

    static public void main(String[] args) {
        new A().start();
    }

    public innerbrk3() {
        new A().start();
    }
}
