public class brktest { // *
    static int staticInt;
    static { // *
        staticInt = 10; // *
    }
    static public void main(String[] args) {
        NotMuch myIFace = new NotMuch() { // *
                public void doNothingMuch() { // $1 *
                    System.out.println("Hello again"); // $1 *
                    NotMuch myIFace2 = new NotMuch() { // $1 *
                            public void doNothingMuch() { // $2 *
                                System.out.println("Hello once more"); // $2 *
                            } // $2 *
                        };
                    myIFace2.doNothingMuch(); // $1 *
                } // $1 *
            };
        System.out.println("Hello"); // *
        myIFace.doNothingMuch(); // *
        brktest2.test("blah"); // *
    } // *
    static interface NotMuch {
        public void doNothingMuch();
    }
}

class brktest2 { // *
    public static void test(String s) {
        System.out.println("brktest2 was run: (" + s + ")"); // *
    } // *
}

// Java parser and breakpoints setting test case (see bug #521).
// Lines of code are indicated by comments. Should be able to
// set breakpoints at every line of code. The $N in the comment
// indicates where the inner classes are located.
