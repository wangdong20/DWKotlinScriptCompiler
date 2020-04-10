import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.AssignStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.Stmt;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.IllTypedException;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.Typechecker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    public void booleanPlusIntDefine() throws IllTypedException {
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(new AssignStmt(new BooleanExp(true), new VariableExp("x"), null, false, true));
        stmtList.add(new AssignStmt(new AdditiveExp(new VariableExp("x"), new IntExp(1), AdditiveOp.EXP_PLUS),
                new VariableExp("a"), null, false, true));
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

}
