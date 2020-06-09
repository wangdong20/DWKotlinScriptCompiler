import com.github.wangdong20.kotlinscriptcompiler.codegen.CodeGenerator;
import com.github.wangdong20.kotlinscriptcompiler.codegen.CodeGeneratorException;
import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodeGeneratorTest {
    // ---BEGIN STATICS---
    public static final String CLASS_NAME_PREFIX = "Compiled";
    public static final String METHOD_NAME = "compiledProgram";
    // ---END STATICS---

    private String currentClassName = null;

    // each element of the array is a separate line
    public static String[] readUntilClose(final InputStream stream) throws IOException {
        return readUntilClose(new BufferedReader(new InputStreamReader(stream)));
    } // readUntilClose

    public static String[] readUntilClose(final BufferedReader reader) throws IOException {
        final List<String> buffer = new ArrayList<>();

        try {
            String currentLine = "";
            while ((currentLine = reader.readLine()) != null) {
                buffer.add(currentLine);
            }
            return buffer.toArray(new String[buffer.size()]);
        } finally {
            reader.close();
        }
    } // readUntilClose

    public String[] runTest(final Program program, String testName)
            throws CodeGeneratorException, IOException {
        currentClassName = CLASS_NAME_PREFIX + testName;
        final CodeGenerator generator = new CodeGenerator(currentClassName, METHOD_NAME);
        generator.writeProgram(program);
        final ProcessBuilder builder = new ProcessBuilder("java", currentClassName);
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        try {
            return readUntilClose(process.getInputStream());
        } finally {
            process.getErrorStream().close();
            process.getOutputStream().close();
        }
    } // runTest

    public void assertOutput(String testName, final Program program,
                             final String... expectedOutput)
            throws CodeGeneratorException, IOException {
        assertArrayEquals(expectedOutput,
                runTest(program, testName));
        new File(currentClassName + ".class").delete();
    } // runTest

    public void assertOutputExpectedException (String testName, final Program program,
                                               final String... expectedOutput) {
        Throwable exception = assertThrows(CodeGeneratorException.class,
                ()-> {
                    assertOutput(testName, program, expectedOutput);
                });
        System.out.println(exception.getMessage());
    }

    public static List<Stmt> stmts(final Stmt... statements) {
        final List<Stmt> list = new ArrayList<Stmt>();
        for (final Stmt statement : statements) {
            list.add(statement);
        }
        return list;
    } // stmts

    public static Program makeProgram(final Stmt... statements) {
        return new Program(stmts(statements));
    } // makeProgram

    @Test
    // var x = 1
    // print(x)
    public void testAssign(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new IntExp(1), new VariableExp("x"), false, true),
                        new PrintStmt(new VariableExp("x"))),
                "1");
    }

    @Test
    // var x = 1
    // x = 2
    // print(x)
    public void testReAssign(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new IntExp(1), new VariableExp("x"), false, true),
                        new AssignStmt(new IntExp(2), new VariableExp("x"), false, false),
                        new PrintStmt(new VariableExp("x"))),
                "2");
    }

    @Test
    // var b = true
    // print(!b)
    public void testNotExp(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new BooleanExp(true), new VariableExp("b"), false, true),
                        new PrintStmt(new NotExp(new VariableExp("b")))), "false");
    }

    @Test
    // var b = false
    // print(!b)
    public void testFalseNotExp(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new BooleanExp(false), new VariableExp("b"), false, true),
                        new PrintStmt(new NotExp(new VariableExp("b")))), "true");
    }

    @Test
    // var x = 1 + 2 * 3 / 6 - 2 * 3
    // print(x)
    public void testComplicateNumAssign(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new AdditiveExp(new AdditiveExp(
                        new IntExp(1), new MultiplicativeExp(new MultiplicativeExp(new IntExp(2), new IntExp(3), MultiplicativeOp.OP_MULTIPLY),
                        new IntExp(6), MultiplicativeOp.OP_DIVIDE), AdditiveOp.EXP_PLUS),
                        new MultiplicativeExp(new IntExp(2), new IntExp(3), MultiplicativeOp.OP_MULTIPLY), AdditiveOp.EXP_MINUS
                ), new VariableExp("x"), false, true),
                        new PrintStmt(new VariableExp("x"))),
                "-4");
    }

    @Test
    // var i  = 3;
    // var j = ++i * 3;
    // println(i)
    // println(j)
    public void testSelfIncreaseMultiply(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new IntExp(3), new VariableExp("i"), false, true),
                        new AssignStmt(new MultiplicativeExp(new SelfOperationExp(new VariableExp("i"),
                                SelfOp.OP_SELF_INCREASE, true), new IntExp(3), MultiplicativeOp.OP_MULTIPLY),
                                new VariableExp("j"), false, true),
                        new PrintlnStmt(new VariableExp("i")),
                        new PrintlnStmt(new VariableExp("j"))), "4", "12");
    }

    @Test
    // var a = 2
    // var b = (2 * 3 > 5) && (a == 2)
    // print(b)
    public void testCompareAnd(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new IntExp(2), new VariableExp("a"), false, true),
                        new AssignStmt(new BiLogicalExp(new ComparableExp(new MultiplicativeExp(new IntExp(2), new IntExp(3), MultiplicativeOp.OP_MULTIPLY),
                                new IntExp(5), ComparableOp.OP_GREATER_THAN), new ComparableExp(new VariableExp("a"), new IntExp(2),
                                ComparableOp.OP_EQUAL_EQUAL), BiLogicalOp.OP_AND), new VariableExp("b"), false, true),
                        new PrintStmt(new VariableExp("b"))), "true");
    }

    @Test
    // var a = 2
    // var b = (2 * 3 > 5) && (a > 2)
    // print(b)
    public void testCompareAndFalse(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(),
                makeProgram(new AssignStmt(new IntExp(2), new VariableExp("a"), false, true),
                        new AssignStmt(new BiLogicalExp(new ComparableExp(new MultiplicativeExp(new IntExp(2), new IntExp(3), MultiplicativeOp.OP_MULTIPLY),
                                new IntExp(5), ComparableOp.OP_GREATER_THAN), new ComparableExp(new VariableExp("a"), new IntExp(2),
                                ComparableOp.OP_GREATER_THAN), BiLogicalOp.OP_AND), new VariableExp("b"), false, true),
                        new PrintStmt(new VariableExp("b"))), "false");
    }

    @Test
    // var a = 2
    // var b = 3
    // var c = 4
    // var d = 5
    // var e = 7
    // a += 3
    // b *= 3
    // c /= 2
    // d -= a
    // e /= a
    // println(a)
    // println(b)
    // println(c)
    // println(d)
    // println(e)
    public void testAllCompoundAssignment(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new IntExp(2), new VariableExp("a"), false, true),
                new AssignStmt(new IntExp(3), new VariableExp("b"), false, true),
                new AssignStmt(new IntExp(4), new VariableExp("c"), false, true),
                new AssignStmt(new IntExp(5), new VariableExp("d"), false, true),
                new AssignStmt(new IntExp(7), new VariableExp("e"), false, true),
                new CompoundAssignStmt(new IntExp(3), new VariableExp("a"), CompoundAssignOp.EXP_PLUS_EQUAL),
                new CompoundAssignStmt(new IntExp(3), new VariableExp("b"), CompoundAssignOp.EXP_MULTIPLY_EQUAL),
                new CompoundAssignStmt(new IntExp(2), new VariableExp("c"), CompoundAssignOp.EXP_DIVIDE_EQUAL),
                new CompoundAssignStmt(new VariableExp("a"), new VariableExp("d"), CompoundAssignOp.EXP_MINUS_EQUAL),
                new CompoundAssignStmt(new VariableExp("a"), new VariableExp("e"), CompoundAssignOp.EXP_DIVIDE_EQUAL),
                new PrintlnStmt(new VariableExp("a")),
                new PrintlnStmt(new VariableExp("b")),
                new PrintlnStmt(new VariableExp("c")),
                new PrintlnStmt(new VariableExp("d")),
                new PrintlnStmt(new VariableExp("e"))
        ), "5", "9", "2", "0", "1");
    }

    @Test
    // var a = arrayOf(1, 2, 3)
    // for(i in a) {
    //      println(i)
    // }
    public void testForInArray(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new IntExp(1));
        exps.add(new IntExp(2));
        exps.add(new IntExp(3));
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInFor))
        ), "1", "2", "3");
    }

    @Test
    // var a = arrayOf("a", "b", "c")
    // for(i in a) {
    //      println(i)
    // }
    public void testForInStringArray(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new StringExp("a", null));
        exps.add(new StringExp("b", null));
        exps.add(new StringExp("c", null));
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInFor))
        ), "a", "b", "c");
    }

    @Test
    // var a = arrayOf("a", 1, true)
    // for(i in a) {
    //      println(i)
    // }
    public void testForInAnyArray(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new StringExp("a", null));
        exps.add(new IntExp(1));
        exps.add(new BooleanExp(true));
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInFor))
        ), "a", "1", "true");
    }

    @Test
    // for(i in 1..3) {
    //      println(i)
    // }
    public void testForInRange(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new IntExp(3)), new BlockStmt(stmtsInFor))
        ), "1", "2");
    }

    @Test
    // for(i in 1..10) {
    //      if(i == 4) {
    //          break
    //      }
    //      println(i)
    // }
    public void testForBreak(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        List<Stmt> stmtsInIf = new ArrayList<>();
        stmtsInIf.add(ControlLoopStmt.STMT_BREAK);
        stmtsInFor.add(new IfStmt(new ComparableExp(new VariableExp("i"), new IntExp(4), ComparableOp.OP_EQUAL_EQUAL),
                new BlockStmt(stmtsInIf)));
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new IntExp(10)),
                        new BlockStmt(stmtsInFor))
        ), "1", "2", "3");
    }

    @Test
    // for(i in 1..10) {
    //      if(i % 2 == 0) {
    //          continue
    //      }
    //      println(i)
    // }
    public void testForContinue(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        List<Stmt> stmtsInIf = new ArrayList<>();
        stmtsInIf.add(ControlLoopStmt.STMT_CONTINUE);
        stmtsInFor.add(new IfStmt(new ComparableExp(new MultiplicativeExp(new VariableExp("i"), new IntExp(2), MultiplicativeOp.OP_MOD), new IntExp(0), ComparableOp.OP_EQUAL_EQUAL),
                new BlockStmt(stmtsInIf)));
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new IntExp(10)),
                        new BlockStmt(stmtsInFor))
        ), "1", "3", "5", "7", "9");
    }

    @Test
    // for(i in 1..10 step 2) {
    //      println(i)
    // }
    public void testForInRangeWithStep2(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new IntExp(10)), new IntExp(2), new BlockStmt(stmtsInFor))
        ), "1", "3", "5", "7", "9");
    }

    @Test
    // var s = 2
    // for(i in 1..10 step s) {
    //      println(i)
    // }
    public void testForInRangeWithStepS(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(new AssignStmt(new IntExp(2), new VariableExp("s"), false, true),
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new IntExp(10)), new VariableExp("s"), new BlockStmt(stmtsInFor))
        ), "1", "3", "5", "7", "9");
    }

    @Test
    // var s = 2
    // var end = 10
    // for(i in 1..end step s) {
    //      println(i)
    // }
    public void testForInRangeWithEndStepS(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(new AssignStmt(new IntExp(2), new VariableExp("s"), false, true),
                new AssignStmt(new IntExp(10), new VariableExp("end"), false, true),
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new VariableExp("end")), new VariableExp("s"), new BlockStmt(stmtsInFor))
        ), "1", "3", "5", "7", "9");
    }

    @Test
    // var s = 2
    // var a = arrayOf(1, 5, 10)
    // for(i in 1..a[2] step s) {
    //      println(i)
    // }
    public void testForInRangeWithEndArrayIndexStepS(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInFor = new ArrayList<>();
        List<Exp> exps = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        exps.add(new IntExp(1));
        exps.add(new IntExp(5));
        exps.add(new IntExp(10));
        assertOutput(testInfo.getDisplayName(), makeProgram(new AssignStmt(new IntExp(2), new VariableExp("s"), false, true),
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(1), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(2))), new VariableExp("s"), new BlockStmt(stmtsInFor))
        ), "1", "3", "5", "7", "9");
    }

    @Test
    // var a = Array(10, {i -> 2 * i})
    // for(i in a) {
    //      println(i)
    // }
    public void testArrayExp(TestInfo testInfo) throws CodeGeneratorException, IOException {
        LinkedHashMap<VariableExp, Type> parameters = new LinkedHashMap<>();
        parameters.put(new VariableExp("i"), null);
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayExp(new IntExp(10), new LambdaExp(parameters, new MultiplicativeExp(new IntExp(2), new VariableExp("i"), MultiplicativeOp.OP_MULTIPLY))),
                        new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInFor))),
                "0", "2", "4", "6", "8", "10", "12", "14", "16", "18"
        );
    }

    @Test
    // var a = Array(10, {i -> i % 2 == 0})
    // for(i in a) {
    //      println(i)
    // }
    public void testBoolArrayExp(TestInfo testInfo) throws CodeGeneratorException, IOException {
        LinkedHashMap<VariableExp, Type> parameters = new LinkedHashMap<>();
        parameters.put(new VariableExp("i"), null);
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayExp(new IntExp(10), new LambdaExp(parameters, new ComparableExp(new MultiplicativeExp(new VariableExp("i"), new IntExp(2), MultiplicativeOp.OP_MOD), new IntExp(0), ComparableOp.OP_EQUAL_EQUAL))),
                        new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInFor))),
                "true", "false", "true", "false", "true", "false", "true", "false", "true", "false"
        );
    }

    @Test
    // var size = 10
    // var a = Array(size, {i -> 2 * i})
    // for(i in a) {
    //      println(i)
    // }
    public void testArrayExpWithSizeVar(TestInfo testInfo) throws CodeGeneratorException, IOException {
        LinkedHashMap<VariableExp, Type> parameters = new LinkedHashMap<>();
        parameters.put(new VariableExp("i"), null);
        List<Stmt> stmtsInFor = new ArrayList<>();
        stmtsInFor.add(new PrintlnStmt(new VariableExp("i")));
        assertOutput(testInfo.getDisplayName(), makeProgram(new AssignStmt(new IntExp(10), new VariableExp("size"), false, true),
                new AssignStmt(new ArrayExp(new VariableExp("size"), new LambdaExp(parameters, new MultiplicativeExp(new IntExp(2), new VariableExp("i"), MultiplicativeOp.OP_MULTIPLY))),
                        new VariableExp("a"), false, true),
                new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInFor))),
                "0", "2", "4", "6", "8", "10", "12", "14", "16", "18"
        );
    }

    @Test
    // var i = 10
    // var b = true
    // var s = "S" + i + b
    // print(s)
    public void testStringConcatenation(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new IntExp(10), new VariableExp("i"), false, true),
                new AssignStmt(new BooleanExp(true), new VariableExp("b"), false, true),
                new AssignStmt(new AdditiveExp(new AdditiveExp(new StringExp("S", null),
                        new VariableExp("i"), AdditiveOp.EXP_PLUS), new VariableExp("b"), AdditiveOp.EXP_PLUS),
                        new VariableExp("s"), false, true),
                new PrintStmt(new VariableExp("s"))
        ), "S10true");
    }

    @Test
    // var i = 10
    // var b = true
    // var s = i + "S" + b
    // print(s)
    public void testStringConcatenationInitWithInt(TestInfo testInfo) throws IOException {
        assertOutputExpectedException(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new IntExp(10), new VariableExp("i"), false, true),
                new AssignStmt(new BooleanExp(true), new VariableExp("b"), false, true),
                new AssignStmt(new AdditiveExp(new AdditiveExp(new VariableExp("i"), new StringExp("S", null),
                        AdditiveOp.EXP_PLUS), new VariableExp("b"), AdditiveOp.EXP_PLUS),
                        new VariableExp("s"), false, true),
                new PrintStmt(new VariableExp("s"))
        ), "S10true");
    }

    @Test
    // var i = "10"
    // var b = true
    // var s = "S" + i + b
    // print(s)
    public void testStringStringConcatenation(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new StringExp("10", null), new VariableExp("i"), false, true),
                new AssignStmt(new BooleanExp(true), new VariableExp("b"), false, true),
                new AssignStmt(new AdditiveExp(new AdditiveExp(new StringExp("S", null),
                        new VariableExp("i"), AdditiveOp.EXP_PLUS), new VariableExp("b"), AdditiveOp.EXP_PLUS),
                        new VariableExp("s"), false, true),
                new PrintStmt(new VariableExp("s"))
        ), "S10true");
    }

    @Test
    // var i = "10"
    // var b = true
    // var s = "S"
    // print(s + i + b)
    public void testStringStringPrintExp(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new StringExp("10", null), new VariableExp("i"), false, true),
                new AssignStmt(new BooleanExp(true), new VariableExp("b"), false, true),
                new AssignStmt(new StringExp("S", null),
                        new VariableExp("s"), false, true),
                new PrintStmt(new AdditiveExp(new AdditiveExp(new VariableExp("s"),
                        new VariableExp("i"), AdditiveOp.EXP_PLUS), new VariableExp("b"), AdditiveOp.EXP_PLUS))
        ), "S10true");
    }

    @Test
    // var a = 200
    // var b = 10
    // var c = 3
    // var d = "a is $a, b is $b, c is $c, a + b * c is ${a + b * c}"
    // print(d)
    public void testStringInterpolation(TestInfo testInfo) throws CodeGeneratorException, IOException {
        LinkedHashMap<Integer, Exp> interpolation = new LinkedHashMap<>();
        interpolation.put(5, new VariableExp("a"));
        interpolation.put(12, new VariableExp("b"));
        interpolation.put(19, new VariableExp("c"));
        interpolation.put(34, new AdditiveExp(new VariableExp("a"), new MultiplicativeExp(
                new VariableExp("b"), new VariableExp("c"), MultiplicativeOp.OP_MULTIPLY
        ), AdditiveOp.EXP_PLUS));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new IntExp(200), new VariableExp("a"), false, true),
                new AssignStmt(new IntExp(10), new VariableExp("b"), false, true),
                new AssignStmt(new IntExp(3), new VariableExp("c"), false, true),
                new AssignStmt(new StringExp("a is , b is , c is , a + b * c is ", interpolation), new VariableExp("d"), false, true),
                new PrintStmt(new VariableExp("d"))
        ), "a is 200, b is 10, c is 3, a + b * c is 230");
    }

    @Test
    // fun bubbleSort(arr : Array<Int>, length: Int): Unit {
    //  for (i in 0..length - 1)
    //     for (j in 0..length-i-1)
    //         if (arr[j] > arr[j+1])
    //         {
    //             // swap arr[j+1] and arr[i]
    //             var temp = arr[j];
    //             arr[j] = arr[j+1];
    //             arr[j+1] = temp;
    //         }
    // }
    // var a = arrayOf(3, 2, 5, 6, 8, 9, 2, 4)
    // bubbleSort(a, 8)
    // for(i in a) {
    //      println("i")
    // }
    public void testFunctionWithBubbleSort(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Stmt> stmtsInForForIf = new ArrayList<>();
        stmtsInForForIf.add(new AssignStmt(new ArrayWithIndexExp(new VariableExp("arr"), new VariableExp("j")),
                new VariableExp("temp"), false, true));
        stmtsInForForIf.add(new AssignStmt(new ArrayWithIndexExp(new VariableExp("arr"), new AdditiveExp(new VariableExp("j"), new IntExp(1), AdditiveOp.EXP_PLUS)),
                new ArrayWithIndexExp(new VariableExp("arr"), new VariableExp("j")), false, false));
        stmtsInForForIf.add(new AssignStmt(new VariableExp("temp"), new ArrayWithIndexExp(new VariableExp("arr"), new AdditiveExp(new VariableExp("j"), new IntExp(1), AdditiveOp.EXP_PLUS)),
                false, false));
        List<Stmt> stmtsInForFor = new ArrayList<>();
        List<Stmt> stmtsInFor = new ArrayList<>();
        List<Stmt> stmtsInFun = new ArrayList<>();
        stmtsInForFor.add(new IfStmt(new ComparableExp(new ArrayWithIndexExp(new VariableExp("arr"), new VariableExp("j")),
                new ArrayWithIndexExp(new VariableExp("arr"), new AdditiveExp(new VariableExp("j"), new IntExp(1), AdditiveOp.EXP_PLUS)), ComparableOp.OP_GREATER_THAN),
                new BlockStmt(stmtsInForForIf)));
        ForStmt forStmtInFor = new ForStmt(new VariableExp("j"), new RangeExp(new IntExp(0),
                new AdditiveExp(new AdditiveExp(new VariableExp("length"), new VariableExp("i"), AdditiveOp.EXP_MINUS),
                        new IntExp(1), AdditiveOp.EXP_MINUS)), new BlockStmt(stmtsInForFor));
        stmtsInFor.add(forStmtInFor);
        ForStmt forStmt = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0),
                new AdditiveExp(new VariableExp("length"), new IntExp(1), AdditiveOp.EXP_MINUS)), new BlockStmt(stmtsInFor));
        LinkedHashMap<Exp, Type> parameters = new LinkedHashMap<>();
        parameters.put(new VariableExp("arr"), new TypeArray(BasicType.TYPE_INT));
        parameters.put(new VariableExp("length"), BasicType.TYPE_INT);
        stmtsInFun.add(forStmt);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("bubbleSort"),
                BasicType.TYPE_UNIT, parameters, new BlockStmt(stmtsInFun));

        List<Stmt> forPrintArray = new ArrayList<>();
        forPrintArray.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt printArray = new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(forPrintArray));
        List<Exp> arrayPara = new ArrayList<>();
        arrayPara.add(new IntExp(3));
        arrayPara.add(new IntExp(2));
        arrayPara.add(new IntExp(5));
        arrayPara.add(new IntExp(6));
        arrayPara.add(new IntExp(8));
        arrayPara.add(new IntExp(9));
        arrayPara.add(new IntExp(2));
        arrayPara.add(new IntExp(4));
        List<Exp> funPara = new ArrayList<>();
        funPara.add(new VariableExp("a"));
        funPara.add(new IntExp(8));

        assertOutput(testInfo.getDisplayName(), makeProgram(
                functionDeclareStmt, new AssignStmt(new ArrayOfExp(arrayPara), new VariableExp("a"), false, true),
                new FunctionInstanceStmt(new FunctionInstanceExp(new VariableExp("bubbleSort"), funPara)),
                printArray
        ), "2", "2", "3", "4", "5", "6", "8", "9");
    }

    @Test
    // var a = arrayOf(1, 2, 3)
    // a[1] = 5
    // print(a[1])
    public void testArrayIndexAssign(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new IntExp(1));
        exps.add(new IntExp(2));
        exps.add(new IntExp(3));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new AssignStmt(new IntExp(5), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), false, false),
                new PrintStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)))
        ), "5");
    }

    @Test
    // var a = arrayOf(1, 2, 3)
    // a[1] = a[2]
    // print(a[1])
    public void testArrayIndexAssignArrayIndex(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new IntExp(1));
        exps.add(new IntExp(2));
        exps.add(new IntExp(3));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new AssignStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(2)), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), false, false),
                new PrintStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)))
        ), "3");
    }

    @Test
    // var a = arrayOf("1", "2", "3")
    // a[1] += 5
    // print(a[1])
    public void testArrayIndexCompoundAssign(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new StringExp("1", null));
        exps.add(new StringExp("2", null));
        exps.add(new StringExp("3", null));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new CompoundAssignStmt(new IntExp(5), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), CompoundAssignOp.EXP_PLUS_EQUAL),
                new PrintStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)))
        ), "25");
    }

    @Test
    // var a = arrayOf("1", "2", "3")
    // a[1] += a[2]
    // print(a[1])
    public void testArrayIndexCompoundAssignArrayIndex(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new StringExp("1", null));
        exps.add(new StringExp("2", null));
        exps.add(new StringExp("3", null));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new CompoundAssignStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(2)), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), CompoundAssignOp.EXP_PLUS_EQUAL),
                new PrintStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)))
        ), "23");
    }

    @Test
    // var a = arrayOf("1", "2", "3")
    // var b = "S"
    // b += a[2]
    // print(a[1])
    public void testVarCompoundAssignArrayIndex(TestInfo testInfo) throws CodeGeneratorException, IOException {
        List<Exp> exps = new ArrayList<>();
        exps.add(new StringExp("1", null));
        exps.add(new StringExp("2", null));
        exps.add(new StringExp("3", null));
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new ArrayOfExp(exps), new VariableExp("a"), false, true),
                new AssignStmt(new StringExp("S", null), new VariableExp("b"), false, true),
                new CompoundAssignStmt(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(2)), new VariableExp("b"), CompoundAssignOp.EXP_PLUS_EQUAL),
                new PrintStmt(new VariableExp("b"))
        ), "S3");
    }

    @Test
    // var a = 3
    // var b = 5
    // a -= b
    // print(a)
    public void testVarMinusEVar(TestInfo testInfo) throws CodeGeneratorException, IOException {
        assertOutput(testInfo.getDisplayName(), makeProgram(
                new AssignStmt(new IntExp(3), new VariableExp("a"), false, true),
                new AssignStmt(new IntExp(5), new VariableExp("b"), false, true),
                new CompoundAssignStmt(new VariableExp("b"), new VariableExp("a"), CompoundAssignOp.EXP_MINUS_EQUAL),
                new PrintStmt(new VariableExp("a"))
        ), "-2");
    }

}
