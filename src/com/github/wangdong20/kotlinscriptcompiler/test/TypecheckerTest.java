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
    // Test pair equal
    public void testPairEqual() {
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
    // print(fib(5))
    // fun fib(n : Int) : Int {
    //      if (n <= 1) {
    //       return n;
    //      }
    //    return fib(n-1) + fib(n-2);
    // }
    public void fibRecursion() throws IllTypedException {
        List<Exp> paraInFuncInstance = new ArrayList<>();
        paraInFuncInstance.add(new IntExp(5));
        FunctionInstanceExp functionInstanceExp = new FunctionInstanceExp(new VariableExp("fib"), paraInFuncInstance);
        PrintStmt printStmt = new PrintStmt(functionInstanceExp);
        List<Stmt> stmtsInBlockInBlock = new ArrayList<>();
        stmtsInBlockInBlock.add(new ReturnStmt(new VariableExp("n")));
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("n"), new IntExp(1), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlock));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);

        List<Exp> paraNMinus1 = new ArrayList<>();
        paraNMinus1.add(new AdditiveExp(new VariableExp("n"), new IntExp(1), AdditiveOp.EXP_MINUS));
        FunctionInstanceExp functionInstanceNMinus1 = new FunctionInstanceExp(new VariableExp("fib"), paraNMinus1);

        List<Exp> paraNMinus2 = new ArrayList<>();
        paraNMinus2.add(new AdditiveExp(new VariableExp("n"), new IntExp(2), AdditiveOp.EXP_MINUS));
        FunctionInstanceExp functionInstanceNMinus2 = new FunctionInstanceExp(new VariableExp("fib"), paraNMinus2);

        ReturnStmt returnStmt = new ReturnStmt(new AdditiveExp(functionInstanceNMinus1, functionInstanceNMinus2, AdditiveOp.EXP_PLUS));
        stmtsInBlock.add(returnStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("n"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("fib"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(printStmt);
        stmts.add(functionDeclareStmt);
        Program program = new Program(stmts);
        assertTypecheckProgram(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //          return b
    //      }
    // }
    public void elseNotReturn() {
        List<Stmt> stmtsInBlockInBlock = new ArrayList<>();
        stmtsInBlockInBlock.add(new ReturnStmt(new VariableExp("b")));
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlock));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //      } else {
    //          return a
    //      }
    // }
    public void ifNotReturn() {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        stmtsInBlockInBlockFalse.add(new ReturnStmt(new VariableExp("a")));
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //          if(a < 10) {
    //              return a
    //          }
    //      } else {
    //          return a
    //      }
    // }
    public void ifIfNotReturn() {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlock = new ArrayList<>();
        stmtsInBlockInBlockFalse.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlock.add(new ReturnStmt(new VariableExp("a")));
        IfStmt ifIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlock), null);
        stmtsInBlockInBlockTrue.add(ifIfStmt);
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //          if(a < 10) {
    //              return a
    //          } else {
    //              return b
    //          }
    //      } else {
    //          return a
    //      }
    // }
    public void ifIfReturn() throws IllTypedException {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockFalse = new ArrayList<>();
        stmtsInBlockInBlockFalse.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlockTrue.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlockFalse.add(new ReturnStmt(new VariableExp("b")));
        IfStmt ifIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockInBlockFalse));
        stmtsInBlockInBlockTrue.add(ifIfStmt);
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //          if(a < 10) {
    //              return a
    //          } else {
    //              return b
    //          }
    //      } else {
    //          if(a < 10) {
    //              return a
    //          } else {
    //              return b
    //          }
    //      }
    // }
    public void ifIfElseElseReturn() throws IllTypedException {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInIfIfTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInIfIfFalse = new ArrayList<>();

        stmtsInBlockInBlockInBlockInIfIfTrue.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlockInIfIfFalse.add(new ReturnStmt(new VariableExp("b")));
        IfStmt ifIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlockInIfIfTrue), new BlockStmt(stmtsInBlockInBlockInBlockInIfIfFalse));
        stmtsInBlockInBlockTrue.add(ifIfStmt);

        List<Stmt> stmtsInBlockInBlockInBlockInElseIfTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInElseIfFalse = new ArrayList<>();
        stmtsInBlockInBlockInBlockInElseIfTrue.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlockInElseIfFalse.add(new ReturnStmt(new VariableExp("b")));
        IfStmt elseIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlockInElseIfTrue), new BlockStmt(stmtsInBlockInBlockInBlockInElseIfFalse));
        stmtsInBlockInBlockFalse.add(elseIfStmt);

        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //          if(a < 10) {
    //              return a
    //          } else {
    //              return b
    //          }
    //      } else {
    //          if(a < 10) {
    //              return a
    //          } else {
    //          }
    //      }
    // }
    public void ifIfElseElseNotReturn() {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInIfIfTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInIfIfFalse = new ArrayList<>();

        stmtsInBlockInBlockInBlockInIfIfTrue.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlockInIfIfFalse.add(new ReturnStmt(new VariableExp("b")));
        IfStmt ifIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlockInIfIfTrue), new BlockStmt(stmtsInBlockInBlockInBlockInIfIfFalse));
        stmtsInBlockInBlockTrue.add(ifIfStmt);

        List<Stmt> stmtsInBlockInBlockInBlockInElseIfTrue = new ArrayList<>();
        stmtsInBlockInBlockInBlockInElseIfTrue.add(new ReturnStmt(new VariableExp("a")));
        IfStmt elseIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlockInElseIfTrue), null);
        stmtsInBlockInBlockFalse.add(elseIfStmt);

        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //          if(a < 10) {
    //              return a
    //          } else {
    //              return b
    //          }
    //      } else {
    //          if(a < 10) {
    //          } else {
    //              return b
    //          }
    //      }
    // }
    public void ifIfElseIfNotReturn() {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInIfIfTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockInBlockInIfIfFalse = new ArrayList<>();

        stmtsInBlockInBlockInBlockInIfIfTrue.add(new ReturnStmt(new VariableExp("a")));
        stmtsInBlockInBlockInBlockInIfIfFalse.add(new ReturnStmt(new VariableExp("b")));
        IfStmt ifIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                new BlockStmt(stmtsInBlockInBlockInBlockInIfIfTrue), new BlockStmt(stmtsInBlockInBlockInBlockInIfIfFalse));
        stmtsInBlockInBlockTrue.add(ifIfStmt);

        List<Stmt> stmtsInBlockInBlockInBlockInElseIfFalse = new ArrayList<>();
        stmtsInBlockInBlockInBlockInElseIfFalse.add(new ReturnStmt(new VariableExp("b")));
        IfStmt elseIfStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_LESS_THAN),
                null, new BlockStmt(stmtsInBlockInBlockInBlockInElseIfFalse));
        stmtsInBlockInBlockFalse.add(elseIfStmt);

        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgramExpectedException(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //      } else {
    //      }
    //      return b
    // }
    public void returnOutsideIf() throws IllTypedException {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        stmtsInBlock.add(new ReturnStmt(new VariableExp("b")));
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // fun max(a : Int, b : Int) : Int {
    //      if(a <= b) {
    //      } else {
    //          return a
    //      }
    //      return b
    // }
    public void returnOutsideIfAndReturnInOneIfBranch() throws IllTypedException {
        List<Stmt> stmtsInBlockInBlockTrue = new ArrayList<>();
        List<Stmt> stmtsInBlockInBlockFalse = new ArrayList<>();
        stmtsInBlockInBlockFalse.add(new ReturnStmt(new VariableExp("a")));
        IfStmt ifStmt = new IfStmt(new ComparableExp(new VariableExp("a"), new VariableExp("b"), ComparableOp.OP_LESS_EQUAL),
                new BlockStmt(stmtsInBlockInBlockTrue), new BlockStmt(stmtsInBlockInBlockFalse));
        List<Stmt> stmtsInBlock = new ArrayList<>();
        stmtsInBlock.add(ifStmt);
        stmtsInBlock.add(new ReturnStmt(new VariableExp("b")));
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("max"), BasicType.TYPE_INT,
                parameterList, new BlockStmt(stmtsInBlock));
        List<Stmt> stmtList = new ArrayList<>();
        stmtList.add(functionDeclareStmt);
        Program program = new Program(stmtList);
        assertTypecheckProgram(program);
    }

    @Test
    // fun print() {
    //      print("Hello world")
    // }
    public void voidFunction() throws IllTypedException {
        List<Stmt> stmtsInBlock = new ArrayList<>();
        PrintStmt printStmt = new PrintStmt(new StringExp("Hello world", null));
        stmtsInBlock.add(printStmt);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("print"), BasicType.TYPE_UNIT,
                new LinkedHashMap<>(), new BlockStmt(stmtsInBlock));
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(functionDeclareStmt);
        Program program = new Program(stmts);
        assertTypecheckProgram(program);
    }

    @Test
    // fun print() {
    //      print("Hello world")
    //      return
    // }
    public void voidReturnFunction() throws IllTypedException {
        List<Stmt> stmtsInBlock = new ArrayList<>();
        PrintStmt printStmt = new PrintStmt(new StringExp("Hello world", null));
        stmtsInBlock.add(printStmt);
        stmtsInBlock.add(new ReturnStmt(null));
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("print"), BasicType.TYPE_UNIT,
                new LinkedHashMap<>(), new BlockStmt(stmtsInBlock));
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(functionDeclareStmt);
        Program program = new Program(stmts);
        assertTypecheckProgram(program);
    }

    @Test
    // fun print() {
    //      return
    //      print("Hello world")
    // }
    public void voidReturnBeforeStmtFunction() {
        List<Stmt> stmtsInBlock = new ArrayList<>();
        PrintStmt printStmt = new PrintStmt(new StringExp("Hello world", null));
        stmtsInBlock.add(new ReturnStmt(null));
        stmtsInBlock.add(printStmt);
        FunctionDeclareStmt functionDeclareStmt = new FunctionDeclareStmt(new VariableExp("print"), BasicType.TYPE_UNIT,
                new LinkedHashMap<>(), new BlockStmt(stmtsInBlock));
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(functionDeclareStmt);
        Program program = new Program(stmts);
        assertTypecheckProgramExpectedException(program);
    }

}
