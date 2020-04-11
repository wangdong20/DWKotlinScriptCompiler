import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.AssignStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.PrintlnStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.Stmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.IllTypedException;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.Typechecker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TypecheckerTest {

    private static void assertTypecheckProgram(final Program program) throws IllTypedException {
        Typechecker.typecheckProgram(program);
    }

    private static void assertTypecheckProgramExpectedException(final Program program) {
        Throwable exception = assertThrows(IllTypedException.class,
                ()->{
                    Typechecker.typecheckProgram(program);
                });
        System.out.println(exception.getMessage());
    }

    @Test
    // a = x + 1
    public void varNotDefine() {
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new AdditiveExp(new VariableExp("X"), new IntExp(1), AdditiveOp.EXP_PLUS),
                new VariableExp("a"), null, false, false));
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // var a = x + 1
    public void xNotDefine() {
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new AdditiveExp(new VariableExp("x"), new IntExp(1), AdditiveOp.EXP_PLUS),
                new VariableExp("a"), null, false, true));
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // var x = 2
    // var a = x + 1
    public void varDefine() throws IllTypedException {
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new IntExp(2), new VariableExp("x"), null, false, true));
        stmtList.add(new AssignStmt(new AdditiveExp(new VariableExp("x"), new IntExp(1), AdditiveOp.EXP_PLUS),
                new VariableExp("a"), null, false, true));
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // var x = true
    // var a = x + 1
    public void booleanPlusInt() {
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new BooleanExp(true), new VariableExp("x"), null, false, true));
        stmtList.add(new AssignStmt(new AdditiveExp(new VariableExp("x"), new IntExp(1), AdditiveOp.EXP_PLUS),
                new VariableExp("a"), null, false, true));
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // var a = "test" + 1
    public void stringPlusInt() throws IllTypedException{
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new AdditiveExp(new StringExp("test", null), new IntExp(1), AdditiveOp.EXP_PLUS),
                new VariableExp("a"), false, true));
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // val a = 2
    // a = 1
    public void readOnlyVariable() {
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new IntExp(2), new VariableExp("a"), true, true));
        stmtList.add(new AssignStmt(new IntExp(1), new VariableExp("a"), false, false));
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // val s = {a: Int, b: Int -> a + b}
    // println(s(2,3))
    public void printlnLambdaFunction() throws IllTypedException {
        List<Stmt> stmtList = new ArrayList<>();
        LinkedHashMap<VariableExp, Type> map = new LinkedHashMap<>();
        map.put(new VariableExp("a"), BasicType.TYPE_INT);
        map.put(new VariableExp("b"), BasicType.TYPE_INT);
        stmtList.add(new AssignStmt(new LambdaExp(map, new AdditiveExp(new VariableExp("a"), new VariableExp("b"), AdditiveOp.EXP_PLUS)),
                new VariableExp("s"), true, true));
        List<Exp> exps = new ArrayList<>();
        exps.add(new IntExp(2));
        exps.add(new IntExp(3));
        stmtList.add(new PrintlnStmt(new FunctionInstanceExp(new VariableExp("s"), exps)));
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // val s = {a: Int, b: Int -> a + b}
    // var x = true
    // println(s(x,3))
    public void functionWithWrongParameterType() {
        List<Stmt> stmtList = new ArrayList<>();
        LinkedHashMap<VariableExp, Type> map = new LinkedHashMap<>();
        map.put(new VariableExp("a"), BasicType.TYPE_INT);
        map.put(new VariableExp("b"), BasicType.TYPE_INT);
        stmtList.add(new AssignStmt(new LambdaExp(map, new AdditiveExp(new VariableExp("a"), new VariableExp("b"), AdditiveOp.EXP_PLUS)),
                new VariableExp("s"), true, true));
        stmtList.add(new AssignStmt(new BooleanExp(true), new VariableExp("x"), false, true));
        List<Exp> exps = new ArrayList<>();
        exps.add(new VariableExp("x"));
        exps.add(new IntExp(3));
        stmtList.add(new PrintlnStmt(new FunctionInstanceExp(new VariableExp("s"), exps)));
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

}
