import com.github.wangdong20.kotlinscriptcompiler.parser.*;
import com.github.wangdong20.kotlinscriptcompiler.token.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    public static void assertParses(final Exp expected,
                                    final Token... tokens) throws ParseException {
        assertEquals(expected, (new Parser(tokens)).parseToplevelExp());
    } // assertParses

    public static void assertExpectedException(final Exp expected, final Token... tokens) {
        assertThrows(ParseException.class,
                ()->{
                    assertEquals(expected, (new Parser(tokens)).parseToplevelExp());
                });
    }

    @Test
    public void emptyDoesNotParse() throws ParseException {
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
    public void ifParses() throws ParseException {
        assertParses(new IfExp(new IntExp(1),
                        new IntExp(2),
                        new IntExp(3)),
                KeywordToken.TK_IF,
                BracketsToken.TK_LPAREN,
                new IntToken(1),
                BracketsToken.TK_RPAREN,
                new IntToken(2),
                KeywordToken.TK_ELSE,
                new IntToken(3));
    }

    @Test
    public void plusParses() throws ParseException {
        assertParses(new AdditiveExp(new IntExp(1), new IntExp(2), AdditiveOp.EXP_PLUS),
                new IntToken(1),
                BinopToken.TK_PLUS,
                new IntToken(2));
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
    public void missingIntegerGivesParseError() throws ParseException {
        assertExpectedException(null,
                new IntToken(1),
                BinopToken.TK_PLUS);
    }
}