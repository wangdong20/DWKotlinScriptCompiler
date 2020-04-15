import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.IllTypedException;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.Pair;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.Typechecker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    // var a = Array(10, {i -> i*2})
    // val b = 3
    // fun search(a: Array<Int>, b: Int): Boolean {
    //      for(i in a) {
    //          if(i == b) {
    //              return true
    //          }
    //      }
    //      return false
    // }
    // print(search(a, b))
    public void searchIntArray() throws IllTypedException {
        List<Stmt> stmtList = new ArrayList<>();
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("i"), null);
        stmtList.add(new AssignStmt(new ArrayExp(new IntExp(10),
                new LambdaExp(parameterList, new MultiplicativeExp(new VariableExp("i"), new IntExp(2), MultiplicativeOp.OP_MULTIPLY))
                ), new VariableExp("a"), false, true));
        stmtList.add(new AssignStmt(new IntExp(3), new VariableExp("b"), true, true));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlock = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlock = new ArrayList<>();
        stmtsInBlockInBlockInBlock.add(new ReturnStmt(new BooleanExp(true)));
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("i"), new VariableExp("b"), ComparableOp.OP_EQUAL_EQUAL),
                new BlockStmt(stmtsInBlockInBlockInBlock), null);
        stmtsInBlockInBlock.add(ifStmt);
        ForStmt forStmt = new ForStmt(new VariableExp("i"), new VariableExp("a"), new BlockStmt(stmtsInBlockInBlock));
        stmtsInBlock.add(forStmt);
        stmtsInBlock.add(new ReturnStmt(new BooleanExp(false)));
        LinkedHashMap<Exp, Type> parameters = new LinkedHashMap<>();
        parameters.put(new VariableExp("a"), new TypeArray(BasicType.TYPE_INT));
        parameters.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("search"), BasicType.TYPE_BOOLEAN,
                parameters, new BlockStmt(stmtsInBlock));
        List<Exp> exps = new ArrayList<>();
        exps.add(new VariableExp("a"));
        exps.add(new VariableExp("b"));
        FunctionInstanceExp functionInstanceExp = new FunctionInstanceExp(new VariableExp("search"), exps);
        PrintStmt printStmt = new PrintStmt(functionInstanceExp);
        stmtList.add(functionDeclareStmt);
        stmtList.add(printStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // Test pair equal
    public void testPairEqual() throws IllTypedException {
        List<Type> para1 = new ArrayList<>();
        para1.add(new TypeArray(BasicType.TYPE_INT));
        para1.add(BasicType.TYPE_INT);
        Pair<Variable, List<Type>> p1 = new Pair<>(new VariableExp("search"), para1);
        List<Type> para2 = new ArrayList<>();
        para2.add(new TypeArray(BasicType.TYPE_INT));
        para2.add(BasicType.TYPE_INT);
        Pair<Variable, List<Type>> p2 = new Pair<>(new VariableExp("search"), para2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
