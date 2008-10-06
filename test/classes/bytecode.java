// Test class for bytecodes command.
// $Id: bytecode.java 14 2007-06-02 23:50:55Z nfiedler $

import java.util.*;

public class bytecode {

    public int doThat(int v) {
        int u = 2;
        return v * u;
    }

    public void doIt() {
        int i = 5;
        int j = doThat(i);
        String s = "stringy thingy";
        char c = s.charAt(j);
        Map m = new HashMap();
        m.put(new Integer(j), new Character(c));
        i += 2;
        char ca[] = new char[1];
        ca[0] = c;
        int mia[][] = new int[3][3];
        mia[1][1] = 10;
        mia[1][2] = 255;

        // This will be a lookupswitch
        switch (i) {
        case 5:
            System.out.println("i = 5");
            break;
        case 7:
            System.out.println("i = 7");
            break;
        default:
            System.out.println("i = " + i);
            break;
        }

        if (i == 5) {
            i = 10;
        } else if (i == 7) {
            i = 14;
        }

        // This will be a tableswitch
        switch (i) {
        case 1: i = 2; break;
        case 2: i = 4; break;
        case 3: i = 6; break;
        case 4: i = 8; break;
        case 5: i = 10; break;
        default:
            System.out.println("i = " + i);
            break;
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        bytecode bc = new bytecode();
        bc.doIt();
    }
}

// Bytecode for the doIt() method as reported by jclasslib bytecode viewer
//   0 iconst_5
//   1 istore_1
//   2 aload_0
//   3 iload_1
//   4 invokevirtual #2 <bytecode.doThat>
//   7 istore_2
//   8 ldc #3 <stringy thingy>
//  10 astore_3
//  11 aload_3
//  12 iload_2
//  13 invokevirtual #4 <java/lang/String.charAt>
//  16 istore 4
//  18 new #5 <java/util/HashMap>
//  21 dup
//  22 invokespecial #6 <java/util/HashMap.<init>>
//  25 astore 5
//  27 aload 5
//  29 new #7 <java/lang/Integer>
//  32 dup
//  33 iload_2
//  34 invokespecial #8 <java/lang/Integer.<init>>
//  37 new #9 <java/lang/Character>
//  40 dup
//  41 iload 4
//  43 invokespecial #10 <java/lang/Character.<init>>
//  46 invokeinterface #11 <java/util/Map.put> count 3
//  51 pop
//  52 iinc 1 by 2
//  55 iconst_1
//  56 newarray 5 (char)
//  58 astore 6
//  60 aload 6
//  62 iconst_0
//  63 iload 4
//  65 castore
//  66 iconst_3
//  67 iconst_3
//  68 multianewarray #12 <[[I> dim 2
//  72 astore 7
//  74 aload 7
//  76 iconst_1
//  77 aaload
//  78 iconst_1
//  79 bipush 10
//  81 iastore
//  82 aload 7
//  84 iconst_1
//  85 aaload
//  86 iconst_2
//  87 sipush 255
//  90 iastore
//  91 iload_1
//  92 lookupswitch 2
//            5:  120 (+28)
//            7:  131 (+39)
//            default:  142 (+50)
// 120 getstatic #13 <java/lang/System.out>
// 123 ldc #14 <i = 5>
// 125 invokevirtual #15 <java/io/PrintStream.println>
// 128 goto 167 (+39)
// 131 getstatic #13 <java/lang/System.out>
// 134 ldc #16 <i = 7>
// 136 invokevirtual #15 <java/io/PrintStream.println>
// 139 goto 167 (+28)
// 142 getstatic #13 <java/lang/System.out>
// 145 new #17 <java/lang/StringBuffer>
// 148 dup
// 149 invokespecial #18 <java/lang/StringBuffer.<init>>
// 152 ldc #19 <i = >
// 154 invokevirtual #20 <java/lang/StringBuffer.append>
// 157 iload_1
// 158 invokevirtual #21 <java/lang/StringBuffer.append>
// 161 invokevirtual #22 <java/lang/StringBuffer.toString>
// 164 invokevirtual #15 <java/io/PrintStream.println>
// 167 iload_1
// 168 iconst_5
// 169 if_icmpne 178 (+9)
// 172 bipush 10
// 174 istore_1
// 175 goto 187 (+12)
// 178 iload_1
// 179 bipush 7
// 181 if_icmpne 187 (+6)
// 184 bipush 14
// 186 istore_1
// 187 iload_1
// 188 tableswitch 1 to 5
//            1:  224 (+36)
//            2:  229 (+41)
//            3:  234 (+46)
//            4:  240 (+52)
//            5:  246 (+58)
//            default:  252 (+64)
// 224 iconst_2
// 225 istore_1
// 226 goto 277 (+51)
// 229 iconst_4
// 230 istore_1
// 231 goto 277 (+46)
// 234 bipush 6
// 236 istore_1
// 237 goto 277 (+40)
// 240 bipush 8
// 242 istore_1
// 243 goto 277 (+34)
// 246 bipush 10
// 248 istore_1
// 249 goto 277 (+28)
// 252 getstatic #13 <java/lang/System.out>
// 255 new #17 <java/lang/StringBuffer>
// 258 dup
// 259 invokespecial #18 <java/lang/StringBuffer.<init>>
// 262 ldc #19 <i = >
// 264 invokevirtual #20 <java/lang/StringBuffer.append>
// 267 iload_1
// 268 invokevirtual #21 <java/lang/StringBuffer.append>
// 271 invokevirtual #22 <java/lang/StringBuffer.toString>
// 274 invokevirtual #15 <java/io/PrintStream.println>
// 277 return
