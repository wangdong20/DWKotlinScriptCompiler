import com.github.wangdong20.kotlinscriptcompiler.parser.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeHighOrderFunction;
import com.github.wangdong20.kotlinscriptcompiler.token.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private static void assertParses(final Exp expected,
                                    final Token... tokens) throws ParseException {
        assertEquals(expected, (new Parser(tokens)).parseToplevelExp());
    } // assertParses

    private static void assertExpectedException(final Exp expected, final Token... tokens) {
        assertThrows(ParseException.class,
                ()->{
                    assertEquals(expected, (new Parser(tokens)).parseToplevelExp());
                });
    }

    private static void assertParseStmts(final Stmt expected,
                                     final Token... tokens) throws ParseException {
        assertEquals(expected, (new Parser(tokens)).parseToplevelStmt());
    } // assertParses

    private static void assertParseStmtsExpectException(final Stmt expected, final Token... tokens) {
        assertThrows(ParseException.class,
                ()->{
                    assertEquals(expected, (new Parser(tokens)).parseToplevelStmt());
                });
    }

    private static void assertParseProgram(final Program expected,
                                         final Token... tokens) throws ParseException {
        assertEquals(expected, (new Parser(tokens)).parseToplevelProgram());
    } // assertParses

    private static void assertParseProgramExpectException(final Program expected, final Token... tokens) {
        assertThrows(ParseException.class,
                ()->{
                    assertEquals(expected, (new Parser(tokens)).parseToplevelProgram());
                });
    }

    @Test
    // return
    public void parseReturnWithoutExp() throws ParseException {
        ReturnStmt stmt = new ReturnStmt(null);
        assertParseStmts(stmt, KeywordToken.TK_RETURN);
    }

    @Test
    // return\n
    public void parseReturnWithoutExpWithLineBreak() throws ParseException {
        ReturnStmt stmt = new ReturnStmt(null);
        assertParseStmts(stmt, KeywordToken.TK_RETURN, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    // return 0
    public void parseReturnWithSingleExp() throws ParseException {
        ReturnStmt stmt = new ReturnStmt(new IntExp(0));
        assertParseStmts(stmt, KeywordToken.TK_RETURN, new IntToken(0));
    }

    @Test
    // return 0;
    public void parseReturnWithSingleExpWithSemicolon() throws ParseException {
        ReturnStmt stmt = new ReturnStmt(new IntExp(0));
        assertParseStmts(stmt, KeywordToken.TK_RETURN, new IntToken(0), SymbolToken.TK_SEMICOLON);
    }

    @Test
    // return a + b;\n
    public void parseReturnWithDoubleExpWithSemicolon() throws ParseException {
        ReturnStmt stmt = new ReturnStmt(new AdditiveExp(new VariableExp("a"), new VariableExp("b"), AdditiveOp.EXP_PLUS));
        assertParseStmts(stmt, KeywordToken.TK_RETURN, new VariableToken("a"), BinopToken.TK_PLUS
                , new VariableToken("b"), SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    // var a : Int;
    public void parseVarDeclareWithType() throws ParseException {
        VariableDeclareStmt stmt = new VariableDeclareStmt(new VariableExp("a"), BasicType.TYPE_INT, false);
        assertParseStmts(stmt, KeywordToken.TK_VAR, new VariableToken("a"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_SEMICOLON);
    }

    @Test
    // val a : Boolean
    public void parseValDeclareWithType() throws ParseException {
        VariableDeclareStmt stmt = new VariableDeclareStmt(new VariableExp("a"), BasicType.TYPE_BOOLEAN, true);
        assertParseStmts(stmt, KeywordToken.TK_VAL, new VariableToken("a"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_BOOLEAN);
    }

    @Test
    // var a;;
    public void parseVarDeclareWithOutType() throws ParseException {
        VariableDeclareStmt stmt = new VariableDeclareStmt(new VariableExp("a"), null, false);
        assertParseStmts(stmt, KeywordToken.TK_VAR, new VariableToken("a"), SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in 0..9) {
     *     for(j in a) {
     *         println(i * j)
     *     }
     * }
     * ;while(true) {
     *     if(a > 10) {
     *         break
     *     }
     *     a++;
     * }
     */
    public void parseTwoLoop() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new MultiplicativeExp(new VariableExp("i"), new VariableExp("j"),
                MultiplicativeOp.OP_MULTIPLY)));
        ForStmt forInside = new ForStmt(new VariableExp("j"), new VariableExp("a"), new BlockStmt(stmtListInside));
        List<Stmt> stmtListOutside = new ArrayList<>();
        stmtListOutside.add(forInside);
        ForStmt forStmt = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)),
                new BlockStmt(stmtListOutside));

        List<Stmt> stmtWhileListInside = new ArrayList<>();
        stmtWhileListInside.add(ControlLoopStmt.STMT_BREAK);
        List<Stmt> stmtWhileListOutside = new ArrayList<>();
        stmtWhileListOutside.add(new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_GREATER_THAN),
                new BlockStmt(stmtWhileListInside)));
        stmtWhileListOutside.add(new SelfOperationStmt(new SelfOperationExp(new VariableExp("a"), SelfOp.OP_SELF_INCREASE, false)));
        WhileStmt whileStmt = new WhileStmt(new BooleanExp(true), new BlockStmt(stmtWhileListOutside));

        List<Stmt> stmtListInProgram = new ArrayList<>();
        stmtListInProgram.add(forStmt);
        stmtListInProgram.add(whileStmt);

        assertParseProgram(new Program(stmtListInProgram),
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("j"), KeywordToken.TK_IN, new VariableToken("a"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BinopToken.TK_MULTIPLY, new VariableToken("j"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK,
                SymbolToken.TK_SEMICOLON,
                KeywordToken.TK_WHILE, BracketsToken.TK_LPAREN, KeywordToken.TK_TRUE, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_IF, BracketsToken.TK_LPAREN,
                new VariableToken("a"), BinopToken.TK_GREATER_THAN, new IntToken(10), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_BREAK, SymbolToken.TK_LINE_BREAK,
                BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, new VariableToken("a"), UnopToken.TK_PLUS_PLUS,
                SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY);
    }

    @Test
    /**
     * for(i in 0..9) {
     *     println(i)
     * }
     */
    public void rangeWithIntInt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in 0..9 step 2) {
     *     println(i)
     * }
     */
    public void rangeWithIntIntWithStepInt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)), new IntExp(2), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), KeywordToken.TK_STEP, new IntToken(2), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in 0..9 step a[i + 1]) {
     *     println(i)
     * }
     */
    public void rangeWithIntIntWithStepArrayIndex() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)), new ArrayWithIndexExp(new VariableExp("a"), new AdditiveExp(new VariableExp("i"), new IntExp(1), AdditiveOp.EXP_PLUS)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), KeywordToken.TK_STEP, new VariableToken("a"), BracketsToken.TK_LBRACKET,
                new VariableToken("i"), BinopToken.TK_PLUS, new IntToken(1), BracketsToken.TK_RBRACKET, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a..9) {
     *     println(i)
     * }
     */
    public void rangeWithExp() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new VariableExp("a"), new IntExp(9)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a..9 step a) {
     *     println(i)
     * }
     */
    public void rangeWithVarStep() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new VariableExp("a"), new IntExp(9)), new VariableExp("a"), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), SymbolToken.TK_DOT_DOT, new IntToken(9), KeywordToken.TK_STEP, new VariableToken("a"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in 0..a step a[1]) {
     *     println(i)
     * }
     */
    public void rangeWithVarStepArrayIndex() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new VariableExp("a")), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new VariableToken("a"), KeywordToken.TK_STEP, new VariableToken("a"),
                BracketsToken.TK_LBRACKET, new IntToken(1), BracketsToken.TK_RBRACKET, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in 0..a[1] step a[1]) {
     *     println(i)
     * }
     */
    public void rangeWithIntVarStepArrayIndex() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1))), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new VariableToken("a"), BracketsToken.TK_LBRACKET,
                new IntToken(1), BracketsToken.TK_RBRACKET, KeywordToken.TK_STEP, new VariableToken("a"),
                BracketsToken.TK_LBRACKET, new IntToken(1), BracketsToken.TK_RBRACKET, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a[0]..a[1] step a[1]) {
     *     println(i)
     * }
     */
    public void rangeWithArrayArrayStepArrayIndex() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(0)), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1))), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new IntToken(0), BracketsToken.TK_RBRACKET, SymbolToken.TK_DOT_DOT, new VariableToken("a"), BracketsToken.TK_LBRACKET,
                new IntToken(1), BracketsToken.TK_RBRACKET, KeywordToken.TK_STEP, new VariableToken("a"),
                BracketsToken.TK_LBRACKET, new IntToken(1), BracketsToken.TK_RBRACKET, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a..b) {
     *     println(i)
     * }
     */
    public void rangeWithTwoExp() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new VariableExp("a"), new VariableExp("b")), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), SymbolToken.TK_DOT_DOT, new VariableToken("b"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a..b step a) {
     *     println(i)
     * }
     */
    public void rangeWithTwoExpWithStepVar() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new VariableExp("a"), new VariableExp("b")), new VariableExp("a"), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), SymbolToken.TK_DOT_DOT, new VariableToken("b"), KeywordToken.TK_STEP,
                new VariableToken("a"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a..b step 2) {
     *     println(i)
     * }
     */
    public void rangeWithTwoExpWithStepInt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new VariableExp("a"), new VariableExp("b")), new IntExp(2), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), SymbolToken.TK_DOT_DOT, new VariableToken("b"), KeywordToken.TK_STEP,
                new IntToken(2), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a..9) {
     *     println(i)
     * }
     */
    public void rangeWithVarAndIntExp() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new VariableExp("a"), new IntExp(9)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a[1]..9) {
     *     println(i)
     * }
     */
    public void rangeWithArrayIndexAndIntExp() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), new IntExp(9)), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new IntToken(1), BracketsToken.TK_RBRACKET, SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in a[1]..a[5]) {
     *     println(i)
     * }
     */
    public void rangeWithArrayIndexAndArrayIndexExp() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new VariableExp("i")));
        ForStmt forInside = new ForStmt(new VariableExp("i"), new RangeExp(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(1)), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(5))), new BlockStmt(stmtListInside));
        assertParseStmts(forInside,
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new IntToken(1), BracketsToken.TK_RBRACKET, SymbolToken.TK_DOT_DOT,
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new IntToken(5), BracketsToken.TK_RBRACKET, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK);
    }

    @Test
    /**
     * for(i in 0..9) {
     *     for(j in a) {
     *         println(i * j)
     *     }
     * }
     */
    public void forInsideForStmt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new MultiplicativeExp(new VariableExp("i"), new VariableExp("j"),
                MultiplicativeOp.OP_MULTIPLY)));
        ForStmt forInside = new ForStmt(new VariableExp("j"), new VariableExp("a"), new BlockStmt(stmtListInside));
        List<Stmt> stmtListOutside = new ArrayList<>();
        stmtListOutside.add(forInside);
        assertParseStmts(new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)),
                new BlockStmt(stmtListOutside)),
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("j"), KeywordToken.TK_IN, new VariableToken("a"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BinopToken.TK_MULTIPLY, new VariableToken("j"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY);
    }

    @Test
    /**
     * for(i in 0..9) {
     *     for(j in 1..a step 2) {
     *         println(i * j)
     *     }
     * }
     */
    public void forRangeStepInsideForStmt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new MultiplicativeExp(new VariableExp("i"), new VariableExp("j"),
                MultiplicativeOp.OP_MULTIPLY)));
        ForStmt forInside = new ForStmt(new VariableExp("j"), new RangeExp(new IntExp(1), new VariableExp("a")), new IntExp(2), new BlockStmt(stmtListInside));
        List<Stmt> stmtListOutside = new ArrayList<>();
        stmtListOutside.add(forInside);
        assertParseStmts(new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)),
                        new BlockStmt(stmtListOutside)),
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("j"), KeywordToken.TK_IN, new IntToken(1), SymbolToken.TK_DOT_DOT, new VariableToken("a"), KeywordToken.TK_STEP, new IntToken(2), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BinopToken.TK_MULTIPLY, new VariableToken("j"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY);
    }

    @Test
    /**
     * for(i in 0..9 step 2) {
     *     for(j in a) {
     *         println(i * j)
     *     }
     * }
     */
    public void forInsideForWithStepStmt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(new PrintlnStmt(new MultiplicativeExp(new VariableExp("i"), new VariableExp("j"),
                MultiplicativeOp.OP_MULTIPLY)));
        ForStmt forInside = new ForStmt(new VariableExp("j"), new VariableExp("a"), new BlockStmt(stmtListInside));
        List<Stmt> stmtListOutside = new ArrayList<>();
        stmtListOutside.add(forInside);
        assertParseStmts(new ForStmt(new VariableExp("i"), new RangeExp(new IntExp(0), new IntExp(9)), new IntExp(2),
                        new BlockStmt(stmtListOutside)),
                KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("i"), KeywordToken.TK_IN,
                new IntToken(0), SymbolToken.TK_DOT_DOT, new IntToken(9), KeywordToken.TK_STEP, new IntToken(2), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("j"), KeywordToken.TK_IN, new VariableToken("a"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new VariableToken("i"), BinopToken.TK_MULTIPLY, new VariableToken("j"), BracketsToken.TK_RPAREN,
                SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY);
    }

    @Test
    /**
     * while(true) {
     *     if(a > 10) {
     *         break
     *     }
     *     a++;
     * }
     */
    public void ifInsidewhileStmt() throws ParseException {
        List<Stmt> stmtListInside = new ArrayList<>();
        stmtListInside.add(ControlLoopStmt.STMT_BREAK);
        List<Stmt> stmtListOutside = new ArrayList<>();
        stmtListOutside.add(new IfStmt(new ComparableExp(new VariableExp("a"), new IntExp(10), ComparableOp.OP_GREATER_THAN),
                new BlockStmt(stmtListInside)));
        stmtListOutside.add(new SelfOperationStmt(new SelfOperationExp(new VariableExp("a"), SelfOp.OP_SELF_INCREASE, false)));
        assertParseStmts(new WhileStmt(new BooleanExp(true), new BlockStmt(stmtListOutside)),
                KeywordToken.TK_WHILE, BracketsToken.TK_LPAREN, KeywordToken.TK_TRUE, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_IF, BracketsToken.TK_LPAREN,
                new VariableToken("a"), BinopToken.TK_GREATER_THAN, new IntToken(10), BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK, KeywordToken.TK_BREAK, SymbolToken.TK_LINE_BREAK,
                BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, new VariableToken("a"), UnopToken.TK_PLUS_PLUS,
                SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY);
    }

    @Test
    /**
     * fun search(i: Int, a: Array<Int>): Boolean {
     *     var index = 0
     *     for(index in a) {
     *         if(i == index) {
     *             return true
     *         }
     *     }
     *     return false
     * }
     */
    public void functionDeclareWithReturnType() throws ParseException {
        List<Stmt> stmtListInIf = new ArrayList<>();
        stmtListInIf.add(new ReturnStmt(new BooleanExp(true)));
        List<Stmt> stmtListInFor = new ArrayList<>();
        stmtListInFor.add(new IfStmt(new ComparableExp(new VariableExp("i"), new VariableExp("index"), ComparableOp.OP_EQUAL_EQUAL),
                new BlockStmt(stmtListInIf)));
        List<Stmt> stmtListInFun = new ArrayList<>();
        stmtListInFun.add(new AssignStmt(new IntExp(0), new VariableExp("index"), false, true));
        stmtListInFun.add(new ForStmt(new VariableExp("index"), new VariableExp("a"),
                new BlockStmt(stmtListInFor)));
        stmtListInFun.add(new ReturnStmt(new BooleanExp(false)));
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("i"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("a"), new TypeArray(BasicType.TYPE_INT));
        assertParseStmts(new FunctionDeclareStmt(new VariableExp("search"), BasicType.TYPE_BOOLEAN,
                parameterList, new BlockStmt(stmtListInFun)),
                KeywordToken.TK_FUN, new VariableToken("search"), BracketsToken.TK_LPAREN,
                new VariableToken("i"), SymbolToken.TK_COLON, TypeToken.TK_TYPE_INT, SymbolToken.TK_COMMA,
                new VariableToken("a"), SymbolToken.TK_COLON, TypeToken.TK_ARRAY, BinopToken.TK_LESS_THAN,
                TypeToken.TK_TYPE_INT, BinopToken.TK_GREATER_THAN, BracketsToken.TK_RPAREN, SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_BOOLEAN, BracketsToken.TK_LCURLY, SymbolToken.TK_LINE_BREAK,
                KeywordToken.TK_VAR, new VariableToken("index"), BinopToken.TK_EQUAL, new IntToken(0),
                SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN, new VariableToken("index"),
                KeywordToken.TK_IN, new VariableToken("a"), BracketsToken.TK_RPAREN, BracketsToken.TK_LCURLY,
                SymbolToken.TK_LINE_BREAK, KeywordToken.TK_IF, BracketsToken.TK_LPAREN, new VariableToken("i"),
                BinopToken.TK_EQUAL_EQUAL, new VariableToken("index"), BracketsToken.TK_RPAREN, BracketsToken.TK_LCURLY,
                SymbolToken.TK_LINE_BREAK, KeywordToken.TK_RETURN, KeywordToken.TK_TRUE, SymbolToken.TK_LINE_BREAK,
                BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY, SymbolToken.TK_LINE_BREAK,
                KeywordToken.TK_RETURN, KeywordToken.TK_FALSE, SymbolToken.TK_LINE_BREAK, BracketsToken.TK_RCURLY);
    }

    @Test
    // a = 1
    public void assignNoVarStmtNoType() throws ParseException {
        assertParseStmts(new AssignStmt(new IntExp(1), new VariableExp("a"), false, false),
                new VariableToken("a"), BinopToken.TK_EQUAL, new IntToken(1));
    }

    @Test
    // a[0] = 1;
    public void assignArrayIndexStmt() throws ParseException {
        assertParseStmts(new AssignStmt(new IntExp(1), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(0)), false, false),
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new IntToken(0),
                BracketsToken.TK_RBRACKET, BinopToken.TK_EQUAL, new IntToken(1), SymbolToken.TK_SEMICOLON);
    }

    @Test
    // var a += 1 Exception expected
    public void varStmtWithCoumpoundAssignment() {
        assertParseStmtsExpectException(new CompoundAssignStmt(new IntExp(1), new VariableExp("a"), CompoundAssignOp.EXP_PLUS_EQUAL),
                KeywordToken.TK_VAR, new VariableToken("a"), BinopToken.TK_PLUS_EQUAL, new IntToken(1));
    }

    @Test
    // a += 1
    public void variableStmtWithCoumpoundAssignment() throws ParseException {
        assertParseStmts(new CompoundAssignStmt(new IntExp(1), new VariableExp("a"), CompoundAssignOp.EXP_PLUS_EQUAL),
                new VariableToken("a"), BinopToken.TK_PLUS_EQUAL, new IntToken(1));
    }

    @Test
    // a[0] += 1;
    public void arrayIndexStmtWithCoumpoundAssignment() throws ParseException {
        assertParseStmts(new CompoundAssignStmt(new IntExp(1), new ArrayWithIndexExp(new VariableExp("a"), new IntExp(0)), CompoundAssignOp.EXP_PLUS_EQUAL),
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new IntToken(0), BracketsToken.TK_RBRACKET, BinopToken.TK_PLUS_EQUAL, new IntToken(1),
                SymbolToken.TK_SEMICOLON);
    }

    @Test
    // a /= 1;;
    public void coumpoundAssignmentParse() throws ParseException {
        assertParseStmts(new CompoundAssignStmt(new IntExp(1), new VariableExp("a"), CompoundAssignOp.EXP_DIVIDE_EQUAL),
                new VariableToken("a"), BinopToken.TK_DIVIDE_EQUAL, new IntToken(1),
                SymbolToken.TK_SEMICOLON, SymbolToken.TK_SEMICOLON);
    }

    @Test
    // val a : String = "abc";
    public void valStmtWithBasicType() throws ParseException {
        assertParseStmts(new AssignStmt(new StringExp("abc", null), new VariableExp("a"), BasicType.TYPE_STRING, true, true),
                KeywordToken.TK_VAL, new VariableToken("a"), SymbolToken.TK_COLON, TypeToken.TK_TYPE_STRING,
                        BinopToken.TK_EQUAL, new StringToken("abc"), SymbolToken.TK_SEMICOLON);
    }

    @Test
    // val a : Array<Int> = Array(10, { -> 0});;
    public void valStmtWithArrayType() throws ParseException {
        assertParseStmts(new AssignStmt(new ArrayExp(new IntExp(10), new LambdaExp(null, new IntExp(0))), new VariableExp("a"), new TypeArray(BasicType.TYPE_INT), true, true),
                KeywordToken.TK_VAL, new VariableToken("a"), SymbolToken.TK_COLON, TypeToken.TK_ARRAY, BinopToken.TK_LESS_THAN,
                TypeToken.TK_TYPE_INT, BinopToken.TK_GREATER_THAN, BinopToken.TK_EQUAL, TypeToken.TK_ARRAY,
                BracketsToken.TK_LPAREN, new IntToken(10), SymbolToken.TK_COMMA, BracketsToken.TK_LCURLY,
                SymbolToken.TK_ARROW, new IntToken(0), BracketsToken.TK_RCURLY, BracketsToken.TK_RPAREN,
                SymbolToken.TK_SEMICOLON, SymbolToken.TK_SEMICOLON);
    }

    @Test
    // val a : Array<Int> = Array(k, {i -> i});;
    public void valStmtWithArrayTypeInitialVar() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameters = new LinkedHashMap<>();
        parameters.put(new VariableExp("i"), null);
        assertParseStmts(new AssignStmt(new ArrayExp(new VariableExp("k"), new LambdaExp(parameters, new VariableExp("i"))), new VariableExp("a"), new TypeArray(BasicType.TYPE_INT), true, true),
                KeywordToken.TK_VAL, new VariableToken("a"), SymbolToken.TK_COLON, TypeToken.TK_ARRAY, BinopToken.TK_LESS_THAN,
                TypeToken.TK_TYPE_INT, BinopToken.TK_GREATER_THAN, BinopToken.TK_EQUAL, TypeToken.TK_ARRAY,
                BracketsToken.TK_LPAREN, new VariableToken("k"), SymbolToken.TK_COMMA, BracketsToken.TK_LCURLY, new VariableToken("i"),
                SymbolToken.TK_ARROW, new VariableToken("i"), BracketsToken.TK_RCURLY, BracketsToken.TK_RPAREN,
                SymbolToken.TK_SEMICOLON, SymbolToken.TK_SEMICOLON);
    }

    @Test
    // var a : (Int, Int)->Int = {a : Int, b: Int -> a + b}
    public void varStmtWithHighOrderFunctionType() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        List<Type> types = new ArrayList<>();
        types.add(BasicType.TYPE_INT);
        types.add(BasicType.TYPE_INT);
        assertParseStmts(new AssignStmt(new LambdaExp(parameterList, new AdditiveExp(new VariableExp("a"),
                        new VariableExp("b"), AdditiveOp.EXP_PLUS)), new VariableExp("a"),
                        new TypeHighOrderFunction(types, BasicType.TYPE_INT), false, true),
                KeywordToken.TK_VAR, new VariableToken("a"), SymbolToken.TK_COLON, BracketsToken.TK_LPAREN,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_COMMA, TypeToken.TK_TYPE_INT, BracketsToken.TK_RPAREN,
                SymbolToken.TK_ARROW, TypeToken.TK_TYPE_INT, BinopToken.TK_EQUAL,
                BracketsToken.TK_LCURLY, new VariableToken("a"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_COMMA, new VariableToken("b"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_ARROW, new VariableToken("a"), BinopToken.TK_PLUS,
                new VariableToken("b"), BracketsToken.TK_RCURLY);
    }

    @Test
    // print("test!")
    public void printStmtWithStringParse() throws ParseException {
        assertParseStmts(new PrintStmt(new StringExp("test!", null)),
                KeywordToken.TK_PRINT, BracketsToken.TK_LPAREN, new StringToken("test!"),
                BracketsToken.TK_RPAREN);
    }

    @Test
    // println("test!" + "plus" + 1)
    public void printlnStmtWithStringPlusParse() throws ParseException {
        assertParseStmts(new PrintlnStmt(new AdditiveExp(new AdditiveExp(new StringExp("test!", null),
                new StringExp("plus", null), AdditiveOp.EXP_PLUS), new IntExp(1), AdditiveOp.EXP_PLUS)),
                KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN, new StringToken("test!"), BinopToken.TK_PLUS,
                new StringToken("plus"), BinopToken.TK_PLUS, new IntToken(1), BracketsToken.TK_RPAREN);
    }

    @Test
    public void emptyDoesNotParse() {
        assertExpectedException(null);
    }

    @Test
    public void integerParses() throws ParseException {
        assertParses(new IntExp(123), new IntToken(123));
    }

    @Test
    public void variableParses() throws ParseException {
        assertParses(new VariableExp("foo"), new VariableToken("foo"));
    }

    @Test
    public void parensParse() throws ParseException {
        assertParses(new VariableExp("foo"),
                BracketsToken.TK_LPAREN,
                new VariableToken("foo"),
                BracketsToken.TK_RPAREN);
    }

    @Test
    // ++i
    public void selfIncreasePreorderParses() throws ParseException {
        assertParses(new SelfOperationExp(new VariableExp("i"), SelfOp.OP_SELF_INCREASE, true),
                UnopToken.TK_PLUS_PLUS, new VariableToken("i"));
    }

    @Test
    // --i
    public void selfDecreasePreorderParses() throws ParseException {
        assertParses(new SelfOperationExp(new VariableExp("i"), SelfOp.OP_SELF_DECREASE, true),
                UnopToken.TK_MINUS_MINUS, new VariableToken("i"));
    }

    @Test
    // ++a[0]
    public void selfIncreasePreorderWithArrayIndex() throws ParseException {
        assertParses(new SelfOperationExp(new ArrayWithIndexExp(new VariableExp("a"), new IntExp(0)), SelfOp.OP_SELF_INCREASE, true),
                UnopToken.TK_PLUS_PLUS, new VariableToken("a"), BracketsToken.TK_LBRACKET,
                new IntToken(0), BracketsToken.TK_RBRACKET);
    }

    @Test
    // i++
    public void selfIncreaseNoPreorderParses() throws ParseException {
        assertParses(new SelfOperationExp(new VariableExp("i"), SelfOp.OP_SELF_INCREASE, false),
                new VariableToken("i"), UnopToken.TK_PLUS_PLUS);
    }

    @Test
    // a[i + 1]++
    public void selfIncreasePostorderWithArrayIndex() throws ParseException {
        assertParses(new SelfOperationExp(new ArrayWithIndexExp(new VariableExp("a"), new AdditiveExp(
                new VariableExp("i"), new IntExp(1), AdditiveOp.EXP_PLUS)), SelfOp.OP_SELF_INCREASE, false),
                new VariableToken("a"), BracketsToken.TK_LBRACKET, new VariableToken("i"), BinopToken.TK_PLUS,
                new IntToken(1), BracketsToken.TK_RBRACKET, UnopToken.TK_PLUS_PLUS);
    }

    @Test
    // i++ + 2
    public void selfDecreaseInAdditiveParses() throws ParseException {
        assertParses(new AdditiveExp(new SelfOperationExp(new VariableExp("i"), SelfOp.OP_SELF_INCREASE, false),
                new IntExp(2), AdditiveOp.EXP_PLUS),
                new VariableToken("i"), UnopToken.TK_PLUS_PLUS, BinopToken.TK_PLUS, new IntToken(2));
    }

    @Test
    // a[i]++ + 2
    public void selfDecreasePostorderArrayElementAdd() throws ParseException {
        assertParses(new AdditiveExp(new SelfOperationExp(new ArrayWithIndexExp(new VariableExp("a"), new VariableExp("i")), SelfOp.OP_SELF_INCREASE, false),
                        new IntExp(2), AdditiveOp.EXP_PLUS), new VariableToken("a"), BracketsToken.TK_LBRACKET,
                new VariableToken("i"), BracketsToken.TK_RBRACKET, UnopToken.TK_PLUS_PLUS, BinopToken.TK_PLUS, new IntToken(2));
    }

    @Test
    // 2 + ++i
    public void selfDecreaseInAdditiveRightParses() throws ParseException {
        assertParses( new AdditiveExp(new IntExp(2), new SelfOperationExp(new VariableExp("i"), SelfOp.OP_SELF_INCREASE, true),
                        AdditiveOp.EXP_PLUS),
                new IntToken(2), BinopToken.TK_PLUS, UnopToken.TK_PLUS_PLUS,  new VariableToken("i"));
    }

    @Test
    // 2 + ++a[i]
    public void selfDecreasePreorderArrayElementAdd() throws ParseException {
        assertParses(new AdditiveExp(new IntExp(2), new SelfOperationExp(new ArrayWithIndexExp(new VariableExp("a"), new VariableExp("i")), SelfOp.OP_SELF_INCREASE, true)
                        , AdditiveOp.EXP_PLUS), new IntToken(2), BinopToken.TK_PLUS,
                UnopToken.TK_PLUS_PLUS, new VariableToken("a"), BracketsToken.TK_LBRACKET,
                new VariableToken("i"), BracketsToken.TK_RBRACKET);
    }

    @Test
    // search(x)
    public void funcInstanceWithSingleParameterStmt() throws ParseException {
        List<Exp> parameterList = new ArrayList<>();
        parameterList.add(new VariableExp("x"));
        assertParseStmts(new FunctionInstanceStmt(new FunctionInstanceExp(new VariableExp("search"), parameterList)),
                new VariableToken("search"), BracketsToken.TK_LPAREN, new VariableToken("x"),
                BracketsToken.TK_RPAREN, SymbolToken.TK_SEMICOLON);
    }

    @Test
    // search(x)
    public void funcInstanceWithSingleParameterParses() throws ParseException {
        List<Exp> parameterList = new ArrayList<>();
        parameterList.add(new VariableExp("x"));
        assertParses(new FunctionInstanceExp(new VariableExp("search"), parameterList),
                new VariableToken("search"), BracketsToken.TK_LPAREN, new VariableToken("x"),
                BracketsToken.TK_RPAREN);
    }

    @Test
    // search()
    public void funcInstanceWithNoParameterParses() throws ParseException {
        List<Exp> parameterList = new ArrayList<>();
        assertParses(new FunctionInstanceExp(new VariableExp("search"), parameterList),
                new VariableToken("search"), BracketsToken.TK_LPAREN,
                BracketsToken.TK_RPAREN);
    }

    @Test
    // search(array, 3)
    public void funcInstanceWithTwoParameterParses() throws ParseException {
        List<Exp> parameterList = new ArrayList<>();
        parameterList.add(new VariableExp("array"));
        parameterList.add(new IntExp(3));
        assertParses(new FunctionInstanceExp(new VariableExp("search"), parameterList),
                new VariableToken("search"), BracketsToken.TK_LPAREN, new VariableToken("array"),
                SymbolToken.TK_COMMA, new IntToken(3), BracketsToken.TK_RPAREN);
    }

    @Test
    // arrayOf(1,2,3,"abc",5)
    public void arrayOfParses() throws ParseException {
        List<Exp> expList = new ArrayList<>();
        expList.add(new IntExp(1));
        expList.add(new IntExp(2));
        expList.add(new IntExp(3));
        expList.add(new StringExp("abc", null));
        expList.add(new IntExp(5));
        assertParses(new ArrayOfExp(expList),
                KeywordToken.TK_ARRAY_OF, BracketsToken.TK_LPAREN, new IntToken(1), SymbolToken.TK_COMMA,
                new IntToken(2), SymbolToken.TK_COMMA, new IntToken(3), SymbolToken.TK_COMMA,
                new StringToken("abc"), SymbolToken.TK_COMMA, new IntToken(5), BracketsToken.TK_RPAREN);
    }

    @Test
    // mutableListOf(1,2,3,"abc",5)
    public void mutableListOfParses() throws ParseException {
        List<Exp> expList = new ArrayList<>();
        expList.add(new IntExp(1));
        expList.add(new IntExp(2));
        expList.add(new IntExp(3));
        expList.add(new StringExp("abc", null));
        expList.add(new IntExp(5));
        assertParses(new MutableListOfExp(expList),
                KeywordToken.TK_MUTABLE_LIST_OF, BracketsToken.TK_LPAREN, new IntToken(1), SymbolToken.TK_COMMA,
                new IntToken(2), SymbolToken.TK_COMMA, new IntToken(3), SymbolToken.TK_COMMA,
                new StringToken("abc"), SymbolToken.TK_COMMA, new IntToken(5), BracketsToken.TK_RPAREN);
    }

    @Test
    // {a: Int, b: Int -> a + b}
    public void lambdaExpParses() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        parameterList.put(new VariableExp("b"), BasicType.TYPE_INT);
        assertParses(new LambdaExp(parameterList, new AdditiveExp(new VariableExp("a"),
                        new VariableExp("b"), AdditiveOp.EXP_PLUS)),
                BracketsToken.TK_LCURLY, new VariableToken("a"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_COMMA, new VariableToken("b"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_ARROW, new VariableToken("a"), BinopToken.TK_PLUS,
                new VariableToken("b"), BracketsToken.TK_RCURLY);
    }

    @Test
    // var b = 3; {a: Int -> a + b}
    public void lambdaExpWithSingleParameterParses() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("a"), BasicType.TYPE_INT);
        assertParses(new LambdaExp(parameterList, new AdditiveExp(new VariableExp("a"),
                        new VariableExp("b"), AdditiveOp.EXP_PLUS)),
                BracketsToken.TK_LCURLY, new VariableToken("a"), SymbolToken.TK_COLON,
                TypeToken.TK_TYPE_INT, SymbolToken.TK_ARROW, new VariableToken("a"), BinopToken.TK_PLUS,
                new VariableToken("b"), BracketsToken.TK_RCURLY);
    }

    @Test
    // val a = 2; var b = 3; { -> a + b}
    public void lambdaExpWithNoParameterParses() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        assertParses(new LambdaExp(parameterList, new AdditiveExp(new VariableExp("a"),
                        new VariableExp("b"), AdditiveOp.EXP_PLUS)),
                BracketsToken.TK_LCURLY,  SymbolToken.TK_ARROW, new VariableToken("a"), BinopToken.TK_PLUS,
                new VariableToken("b"), BracketsToken.TK_RCURLY);
    }

    @Test
    // Array(10, {i -> "s" + i * 2})
    public void arrayExpParses() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("i"), null);
        assertParses(new ArrayExp(new IntExp(10), new LambdaExp(parameterList,
                new AdditiveExp(new StringExp("s", null),
                        new MultiplicativeExp(new VariableExp("i"), new IntExp(2),
                                MultiplicativeOp.OP_MULTIPLY), AdditiveOp.EXP_PLUS))),
                TypeToken.TK_ARRAY, BracketsToken.TK_LPAREN, new IntToken(10),
                SymbolToken.TK_COMMA, BracketsToken.TK_LCURLY, new VariableToken("i"),
                SymbolToken.TK_ARROW, new StringToken("s"), BinopToken.TK_PLUS,
                new VariableToken("i"), BinopToken.TK_MULTIPLY, new IntToken(2),
                BracketsToken.TK_RCURLY, BracketsToken.TK_RPAREN);
    }

    @Test
    // MutableList(10, {i -> "s" + i * 2})
    public void mutableListExpParses() throws ParseException {
        LinkedHashMap<VariableExp, Type> parameterList = new LinkedHashMap<>();
        parameterList.put(new VariableExp("i"), null);
        assertParses(new MutableListExp(new IntExp(10), new LambdaExp(parameterList,
                        new AdditiveExp(new StringExp("s", null),
                                new MultiplicativeExp(new VariableExp("i"), new IntExp(2),
                                        MultiplicativeOp.OP_MULTIPLY), AdditiveOp.EXP_PLUS))),
                TypeToken.TK_MUTABLE_LIST, BracketsToken.TK_LPAREN, new IntToken(10),
                SymbolToken.TK_COMMA, BracketsToken.TK_LCURLY, new VariableToken("i"),
                SymbolToken.TK_ARROW, new StringToken("s"), BinopToken.TK_PLUS,
                new VariableToken("i"), BinopToken.TK_MULTIPLY, new IntToken(2),
                BracketsToken.TK_RCURLY, BracketsToken.TK_RPAREN);
    }

    @Test
    public void plusParses() throws ParseException {
        assertParses(new AdditiveExp(new IntExp(1), new IntExp(2), AdditiveOp.EXP_PLUS),
                new IntToken(1),
                BinopToken.TK_PLUS,
                new IntToken(2));
    }

    @Test
    // !flag
    public void notVariableParses() throws ParseException {
        assertParses(new NotExp(new VariableExp("flag")), UnopToken.TK_NOT, new VariableToken("flag"));
    }

    @Test
    // !(a > 3 + 2 * 4 && false)
    public void notBilogicalParses() throws ParseException {
        assertParses(new NotExp(new BiLogicalExp(new ComparableExp(new VariableExp("a"), new AdditiveExp(new IntExp(3),
                        new MultiplicativeExp(new IntExp(2), new IntExp(4), MultiplicativeOp.OP_MULTIPLY),
                        AdditiveOp.EXP_PLUS), ComparableOp.OP_GREATER_THAN), new BooleanExp(false), BiLogicalOp.OP_AND)),
                UnopToken.TK_NOT, BracketsToken.TK_LPAREN, new VariableToken("a"), BinopToken.TK_GREATER_THAN, new IntToken(3), BinopToken.TK_PLUS,
                new IntToken(2), BinopToken.TK_MULTIPLY, new IntToken(4), BinopToken.TK_AND, KeywordToken.TK_FALSE, BracketsToken.TK_RPAREN);
    }

    @Test
    // 2 + !1 > 3
    public void notPrimaryParses() throws ParseException {
        assertParses(new ComparableExp(new AdditiveExp(new IntExp(2), new NotExp(new IntExp(1)), AdditiveOp.EXP_PLUS),
                new IntExp(3), ComparableOp.OP_GREATER_THAN),
                new IntToken(2), BinopToken.TK_PLUS, UnopToken.TK_NOT, new IntToken(1), BinopToken.TK_GREATER_THAN,
                new IntToken(3));
    }

    @Test
    // flag || 1 + !false
    public void bilogicalNotParses() throws ParseException {
        assertParses(new BiLogicalExp(new VariableExp("flag"), new AdditiveExp(new IntExp(1),
                new NotExp(new BooleanExp(false)), AdditiveOp.EXP_PLUS), BiLogicalOp.OP_OR),
                new VariableToken("flag"), BinopToken.TK_OR, new IntToken(1), BinopToken.TK_PLUS,
                UnopToken.TK_NOT, KeywordToken.TK_FALSE);
    }

    @Test
    // a > 3 + 2 * 4 && false
    public void bilogicalParses() throws ParseException {
        assertParses(new BiLogicalExp(new ComparableExp(new VariableExp("a"), new AdditiveExp(new IntExp(3),
                new MultiplicativeExp(new IntExp(2), new IntExp(4), MultiplicativeOp.OP_MULTIPLY),
                AdditiveOp.EXP_PLUS), ComparableOp.OP_GREATER_THAN), new BooleanExp(false), BiLogicalOp.OP_AND),
                new VariableToken("a"), BinopToken.TK_GREATER_THAN, new IntToken(3), BinopToken.TK_PLUS,
                new IntToken(2), BinopToken.TK_MULTIPLY, new IntToken(4), BinopToken.TK_AND, KeywordToken.TK_FALSE);
    }

    @Test
    // 1 * 2 %3 - 4 / 5
    public void additiveAndMultiplicativeParses() throws ParseException {
        assertParses(new AdditiveExp(new MultiplicativeExp(new MultiplicativeExp(new IntExp(1), new IntExp(2), MultiplicativeOp.OP_MULTIPLY),
                new IntExp(3), MultiplicativeOp.OP_MOD), new MultiplicativeExp(new IntExp(4),
                new IntExp(5), MultiplicativeOp.OP_DIVIDE), AdditiveOp.EXP_MINUS),
                new IntToken(1),
                BinopToken.TK_MULTIPLY,
                new IntToken(2),
                BinopToken.TK_MOD,
                new IntToken(3),
                BinopToken.TK_MINUS,
                new IntToken(4),
                BinopToken.TK_DIVIDE,
                new IntToken(5));
    }

    @Test
    // index > 1 + 2 * 3 + 4
    public void variableCompareParses() throws ParseException {
        assertParses(new ComparableExp(new VariableExp("index"), new AdditiveExp(new AdditiveExp(
                new IntExp(1),
                new MultiplicativeExp(new IntExp(2), new IntExp(3), MultiplicativeOp.OP_MULTIPLY),
                AdditiveOp.EXP_PLUS), new IntExp(4), AdditiveOp.EXP_PLUS), ComparableOp.OP_GREATER_THAN),
                new VariableToken("index"),
                BinopToken.TK_GREATER_THAN,
                new IntToken(1),
                BinopToken.TK_PLUS,
                new IntToken(2),
                BinopToken.TK_MULTIPLY,
                new IntToken(3),
                BinopToken.TK_PLUS,
                new IntToken(4));
    }

    @Test
    public void stringParses() throws ParseException {
        LinkedHashMap<Integer, Exp> map = new LinkedHashMap();
        map.put(5, new AdditiveExp(new VariableExp("a"), new VariableExp("b"), AdditiveOp.EXP_PLUS));
        map.put(12, new VariableExp("beer"));
        map.put(32, new AdditiveExp(new VariableExp("a"), new VariableExp("beer"), AdditiveOp.EXP_PLUS));
        assertParses(new StringExp("a is , b is , c is $ c, sum is ", map),
                new StringToken("a is ${a + b}, b is $beer, c is $ c, sum is ${a + beer}"));
    }

    @Test
    public void plusIsLeftAssociative() throws ParseException {
        assertParses(new AdditiveExp(new AdditiveExp(new IntExp(1),
                        new IntExp(2), AdditiveOp.EXP_PLUS),
                        new IntExp(3), AdditiveOp.EXP_PLUS),
                new IntToken(1),
                BinopToken.TK_PLUS,
                new IntToken(2),
                BinopToken.TK_PLUS,
                new IntToken(3));
    }

    @Test
    public void missingIntegerGivesParseError() {
        assertExpectedException(null,
                new IntToken(1),
                BinopToken.TK_PLUS);
    }
}