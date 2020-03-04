package com.github.wangdong20.kotlinscriptcompiler.parser;

import com.github.wangdong20.kotlinscriptcompiler.token.*;

public class Parser {

    private final Token[] tokens;
    private int currentPos;

    public Parser(final Token[] tokens) {
        this.tokens = tokens;
    }

    private class ParseResult<A> {
        public final A result;
        public final int nextPos;

        public ParseResult(A result, int nextPos) {
            this.result = result;
            this.nextPos = nextPos;
        }
    }

    private void assertTokenIs(final int position, final Token token) throws ParseException {
        if(!tokens[position].equals(token)) {
            throw new ParseException("Expected: " + token.toString() +
                    "Received: " + tokens[position].toString());
        }
    }

    public ParseResult<Exp> parseAdditiveExp(final int startPos) throws ParseException {

    }

    public ParseResult<Exp> parseMultiplicative(final int startPos) throws ParseException {

    }

    public ParseResult<Exp> parsePrimary(final int startPos) throws ParseException {
        if(tokens[startPos] instanceof VariableToken) {
            final VariableToken asVar = (VariableToken) tokens[startPos];
            return new ParseResult<Exp>(new VariableExp(asVar.getName()), startPos + 1);
        } else if(tokens[startPos] instanceof IntToken) {
            final IntToken asInt = (IntToken) tokens[startPos];
            return new ParseResult<Exp>(new IntExp(asInt.getValue()), startPos + 1);
        } else {
            assertTokenIs(startPos, BracketsToken.TK_LPAREN);
            final ParseResult<Exp> expression = parseExp(startPos + 1);
            assertTokenIs(expression.nextPos, BracketsToken.TK_RPAREN);
            return expression;
        }
    }

//    public ParseResult<E> parseE(final int startPos) throws ParseException {
//        try {
            //final ParseResult<E> alpha = parseAlpha(startPos);
            // return alpha;
//        } catch (ParseException e) {
//            parseBeta(startPos);
//        }
//    }

    public  ParseResult<Exp> parseExp(final int startPos) throws ParseException {
        if(tokens[startPos] == KeywordToken.TK_IF) {
            assertTokenIs(startPos + 1, BracketsToken.TK_LPAREN);
            final ParseResult<Exp> condition = parseExp(startPos + 2);
            assertTokenIs(condition.nextPos, BracketsToken.TK_RPAREN);
            final ParseResult<Exp> ifTrue = parseExp(condition.nextPos + 1);
            if(tokens[ifTrue.nextPos] == KeywordToken.TK_ELSE) {
                final ParseResult<Exp> ifFalse = parseExp(ifTrue.nextPos + 1);
                return new ParseResult<Exp>(new IfExp(condition.result, ifTrue.result, ifFalse.result),
                        ifFalse.nextPos);
            } else {
                return new ParseResult<Exp>(new IfExp(condition.result, ifTrue.result),
                        ifTrue.nextPos);
            }

        } else {
            return parseAdditiveExp(startPos);
        }
    }

    public ParseResult<Stmt> parseStmt(final int startPos) throws ParseException {

    }

    public ParseResult<Program> parseProgram(final int startPos) throws ParseException {

    }
}
