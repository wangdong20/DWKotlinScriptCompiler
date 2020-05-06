import com.github.wangdong20.kotlinscriptcompiler.codegen.CodeGenerator;
import com.github.wangdong20.kotlinscriptcompiler.codegen.CodeGeneratorException;
import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.AssignStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.PrintStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.PrintlnStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.Stmt;
import com.sun.tools.javac.jvm.Code;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


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
}
