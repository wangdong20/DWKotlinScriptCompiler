import com.github.wangdong20.kotlinscriptcompiler.token.*;

import java.util.ArrayList;
import java.util.List;

public class TokenizerTest {

    // input: "("
    // output: BracketsToken.TK_LPAREN
    public static void testLeftParen() throws TokenizerException {
        testTokenizes("(", BracketsToken.TK_LPAREN);
    }

    // input: ")"
    // output: BracketsToken.TK_RPAREN
    public static void testRightParen() throws TokenizerException {
        testTokenizes(")", BracketsToken.TK_RPAREN);
    }

    public static void testTokenizes(final String input,
                                     final Token... expectedTokens) throws TokenizerException {
        final Tokenizer tokenizer = new Tokenizer(input);
        final List<Token> receivedTokens = tokenizer.tokenize();
        final List<Token> expected = new ArrayList<Token>();
        for(final Token token : expectedTokens) {
            expected.add(token);
        }
        System.out.println("Expected: " + expected);
        System.out.println("Received: " + receivedTokens);
        assert(receivedTokens.equals(expected));
    }

    public static void testVariableAlone() throws TokenizerException {
        testTokenizes("x", new VariableToken("x"));
    }

    public static void testVariableWithWhitespaceBefore() throws TokenizerException {
        testTokenizes(" x", new VariableToken("x"));
    }

    public static void testVariableWithWhitespaceAfter() throws TokenizerException {
        testTokenizes("x ", new VariableToken("x"));
    }

    public static void testVariableContainKeywords() throws TokenizerException {
        testTokenizes("ifelse", new VariableToken("ifelse"));
    }

    public static void testIfElseKeywords() throws TokenizerException {
        testTokenizes("if else", KeywordToken.TK_IF, KeywordToken.TK_ELSE);
    }

    public static void testIntToken() throws TokenizerException {
        testTokenizes("-123", new IntToken(-123));
    }

    public static void testKeywordToken() throws TokenizerException {
        testTokenizes("while continue break return for in val var print println fun true false main arrayOf mutableListOf",
                KeywordToken.TK_WHILE, KeywordToken.TK_CONTINUE, KeywordToken.TK_BREAK, KeywordToken.TK_RETURN,
                KeywordToken.TK_FOR, KeywordToken.TK_IN, KeywordToken.TK_VAL, KeywordToken.TK_VAR,
                KeywordToken.TK_PRINT, KeywordToken.TK_PRINTLN, KeywordToken.TK_FUN, KeywordToken.TK_TRUE, KeywordToken.TK_FALSE,
                KeywordToken.TK_MAIN, KeywordToken.TK_ARRAY_OF, KeywordToken.TK_MUTABLE_LIST_OF);
    }

    public static void testBinopToken() throws TokenizerException {
        testTokenizes("+ - */ % || && < > <= >= += -= *= /= == = !=",
                BinopToken.TK_PLUS, BinopToken.TK_MINUS, BinopToken.TK_MULTIPLY, BinopToken.TK_DIVIDE,
                BinopToken.TK_MOD, BinopToken.TK_OR, BinopToken.TK_AND, BinopToken.TK_LESS_THAN, BinopToken.TK_GREATER_THAN,
                BinopToken.TK_LESS_OR_EQUAL, BinopToken.TK_GREATER_OR_EQUAL, BinopToken.TK_PLUS_EQUAL, BinopToken.TK_MINUS_EQUAL,
                BinopToken.TK_MULTIPLY_EQUAL, BinopToken.TK_DIVIDE_EQUAL, BinopToken.TK_EQUAL_EQUAL, BinopToken.TK_EQUAL,
                BinopToken.TK_NOT_EQUAL);
    }

    public static void testUnopToken() throws TokenizerException {
        testTokenizes("!++--", UnopToken.TK_NOT, UnopToken.TK_PLUS_PLUS, UnopToken.TK_MINUS_MINUS);
    }

    public static void testBracketsToken() throws TokenizerException {
        testTokenizes("(){ }][", BracketsToken.TK_LPAREN, BracketsToken.TK_RPAREN,
                BracketsToken.TK_LCURLY, BracketsToken.TK_RCURLY, BracketsToken.TK_RBRACKET, BracketsToken.TK_LBRACKET);
    }

    public static void testSymbolToken() throws TokenizerException {
        testTokenizes(":;,->. ..$\n", SymbolToken.TK_COLON, SymbolToken.TK_SEMICOLON, SymbolToken.TK_COMMA,
                SymbolToken.TK_ARROW, SymbolToken.TK_DOT, SymbolToken.TK_DOT_DOT, SymbolToken.TK_DOLLAR_MARK, SymbolToken.TK_LINE_BREAK);
    }

    public static void testTypeToken() throws TokenizerException {
        testTokenizes(" Int String Boolean Unit Array MutableList Any", TypeToken.TK_TYPE_INT,
                TypeToken.TK_TYPE_STRING, TypeToken.TK_TYPE_BOOLEAN, TypeToken.TK_TYPE_UNIT,
                TypeToken.TK_ARRAY, TypeToken.TK_MUTABLE_LIST, TypeToken.TK_ANY);
    }

    public static void testBooleanToken() throws TokenizerException {
        testTokenizes("var b : Boolean = false; b = true;", KeywordToken.TK_VAR,
                new VariableToken("b"), SymbolToken.TK_COLON, TypeToken.TK_TYPE_BOOLEAN,
                BinopToken.TK_EQUAL, KeywordToken.TK_FALSE, SymbolToken.TK_SEMICOLON, new VariableToken("b"),
                BinopToken.TK_EQUAL, KeywordToken.TK_TRUE, SymbolToken.TK_SEMICOLON);
    }

    public static void testStringToken() throws TokenizerException {
        testTokenizes("println(\"This is test!\")", KeywordToken.TK_PRINTLN, BracketsToken.TK_LPAREN,
                new StringToken("This is test!"), BracketsToken.TK_RPAREN);
    }

    public static void testBreakString() throws TokenizerException {
        testTokenizes("\"fsdf", new StringToken("fsdf"));
    }

    public static void testForLoopToken() throws TokenizerException {
        testTokenizes("for(i in a) {print(i)}", KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("i"), KeywordToken.TK_IN, new VariableToken("a"),
                BracketsToken.TK_RPAREN, BracketsToken.TK_LCURLY, KeywordToken.TK_PRINT,
                BracketsToken.TK_LPAREN, new VariableToken("i"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_RCURLY);
    }

    public static void testSingleLineComment() throws TokenizerException {
        testTokenizes("// For loop \nfor(i in a) {print(i)}", SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("i"), KeywordToken.TK_IN, new VariableToken("a"),
                BracketsToken.TK_RPAREN, BracketsToken.TK_LCURLY, KeywordToken.TK_PRINT,
                BracketsToken.TK_LPAREN, new VariableToken("i"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_RCURLY);
    }

    public static void testMultipleLineComment() throws TokenizerException {
        testTokenizes("/** For loop \n Mutiple line comment\n*/\nfor(i in a) {print(i)}", SymbolToken.TK_LINE_BREAK, KeywordToken.TK_FOR, BracketsToken.TK_LPAREN,
                new VariableToken("i"), KeywordToken.TK_IN, new VariableToken("a"),
                BracketsToken.TK_RPAREN, BracketsToken.TK_LCURLY, KeywordToken.TK_PRINT,
                BracketsToken.TK_LPAREN, new VariableToken("i"), BracketsToken.TK_RPAREN,
                BracketsToken.TK_RCURLY);
    }

    public static void testOperator() throws TokenizerException {
        testTokenizes("var a = 1// define a\n a += 2; a++; a--; a*=2; a /= 2; a = a % 2; if(a > 1) return true;",
            KeywordToken.TK_VAR, new VariableToken("a"), BinopToken.TK_EQUAL, new IntToken(1), SymbolToken.TK_LINE_BREAK,
                new VariableToken("a"), BinopToken.TK_PLUS_EQUAL, new IntToken(2), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), UnopToken.TK_PLUS_PLUS, SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), UnopToken.TK_MINUS_MINUS, SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_MULTIPLY_EQUAL, new IntToken(2), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_DIVIDE_EQUAL, new IntToken(2), SymbolToken.TK_SEMICOLON,
                new VariableToken("a"), BinopToken.TK_EQUAL, new VariableToken("a"),
                BinopToken.TK_MOD, new IntToken(2), SymbolToken.TK_SEMICOLON,
                KeywordToken.TK_IF, BracketsToken.TK_LPAREN, new VariableToken("a"), BinopToken.TK_GREATER_THAN,
                new IntToken(1), BracketsToken.TK_RPAREN, KeywordToken.TK_RETURN, KeywordToken.TK_TRUE, SymbolToken.TK_SEMICOLON);
    }

    public static void main(String[] args) throws TokenizerException {
        // write your code here
        testLeftParen();
        testRightParen();
        testVariableAlone();
        testVariableWithWhitespaceBefore();
        testVariableWithWhitespaceAfter();
        testVariableContainKeywords();
        testIfElseKeywords();
        testIntToken();
        testKeywordToken();
        testBinopToken();
        testUnopToken();
        testBracketsToken();
        testTypeToken();
        testSymbolToken();
        testBooleanToken();
        testStringToken();
//        testBreakString();
        testForLoopToken();
        testSingleLineComment();
        testMultipleLineComment();
        testOperator();
    }

}
