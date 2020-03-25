package com.github.wangdong20.kotlinscriptcompiler.parser;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.Stmt;
import com.github.wangdong20.kotlinscriptcompiler.token.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Parser {

    private final Token[] tokens;

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

    private void checkTokenIs(final int position, final Token token) throws ParseException {
        final Token tokenHere = readToken(position);
        if (!tokenHere.equals(token)) {
            throw new ParseException("Expected: " + token.toString() +
                    "\nReceived: " + tokens[position].toString());
        }
    }

    private Token checkTokenIsOr(final int position, final Token... token) throws ParseException {
        for(Token t : token) {
            final Token tokenHere = readToken(position);
            if (tokenHere.equals(t)) {
               return t;
            }
        }
        throw new ParseException("Expected: " + token.toString() +
                "\nReceived: " + tokens[position].toString());
    }

    private Token readToken(final int position) throws ParseException {
        if (position < tokens.length) {
            return tokens[position];
        } else {
            throw new ParseException("Position out of bounds: " + position);
        }
    } // readToken

    public ParseResult<Exp> parseAdditiveExpHelper(final int startPos, final Exp leftExp) {
        int curPos = startPos;
        Exp resultExp = leftExp;

        while(curPos < tokens.length) {
            try {
                Token t = checkTokenIsOr(curPos, BinopToken.TK_PLUS, BinopToken.TK_MINUS);
                final ParseResult<Exp> curPrimary = parseNotExp(curPos + 1);
                curPos = curPrimary.nextPos;
                resultExp = new AdditiveExp(resultExp, curPrimary.result, (t == BinopToken.TK_PLUS) ? AdditiveOp.EXP_PLUS :
                        AdditiveOp.EXP_MINUS);
            } catch (ParseException e) {
                break;
            }
        }
        return new ParseResult<Exp>(resultExp, curPos);
    }

    public ParseResult<Exp> parseMultiplicativeExpHelper(final int startPos, final Exp leftExp) {
        int curPos = startPos;
        Exp resultExp = leftExp;
        MultiplicativeOp op = null;

        while(curPos < tokens.length) {
            try {
                Token t = checkTokenIsOr(curPos, BinopToken.TK_MULTIPLY, BinopToken.TK_DIVIDE, BinopToken.TK_MOD);
                final ParseResult<Exp> curPrimary = parsePrimary(curPos + 1);
                curPos = curPrimary.nextPos;
                switch ((BinopToken)t) {
                    case TK_MULTIPLY:
                        op = MultiplicativeOp.OP_MULTIPLY;
                        break;
                    case TK_DIVIDE:
                        op = MultiplicativeOp.OP_DIVIDE;
                        break;
                    case TK_MOD:
                        op = MultiplicativeOp.OP_MOD;
                        break;
                }
                resultExp = new MultiplicativeExp(resultExp, curPrimary.result, op);
            } catch (ParseException e) {
                break;
            }
        }

        return new ParseResult<Exp>(resultExp, curPos);
    }

    public ParseResult<Exp> parseAdditiveExp(final int startPos, Exp resultExp) {
        return parseAdditiveExpHelper(startPos, resultExp);
    }

    public ParseResult<Exp> parseMultiplicative(final int startPos) throws ParseException {
        final ParseResult<Exp> starting = parsePrimary(startPos);
        Exp resultExp = starting.result;
        return parseMultiplicativeExpHelper(starting.nextPos, resultExp);
    }

    public ParseResult<Exp> parseNotExp(final int startPos) throws ParseException {
        Token t = readToken(startPos);
        if(t == UnopToken.TK_NOT) {
            ParseResult<Exp> result = parseMultiplicative(startPos + 1);
            Exp resultExp = new NotExp(result.result);
            return new ParseResult<>(resultExp, result.nextPos);
        } else {
            return parseMultiplicative(startPos);
        }
    }

    public ParseResult<Exp> parseComparableExp(final int startPos, Exp leftExp) throws ParseException{
        int curPos = startPos;
        ParseResult<Exp> result = null;
        ComparableOp op = null;

        if(curPos < tokens.length) {
            try {
                Token t = checkTokenIsOr(curPos, BinopToken.TK_GREATER_THAN, BinopToken.TK_LESS_THAN,
                        BinopToken.TK_GREATER_OR_EQUAL, BinopToken.TK_LESS_OR_EQUAL, BinopToken.TK_EQUAL_EQUAL,
                        BinopToken.TK_NOT_EQUAL);

                switch ((BinopToken)t) {
                    case TK_GREATER_THAN:
                        op = ComparableOp.OP_GREATER_THAN;
                        break;
                    case TK_LESS_THAN:
                        op = ComparableOp.OP_LESS_THAN;
                        break;
                    case TK_GREATER_OR_EQUAL:
                        op = ComparableOp.OP_GREATER_EQUAL;
                        break;
                    case TK_LESS_OR_EQUAL:
                        op = ComparableOp.OP_LESS_EQUAL;
                        break;
                    case TK_EQUAL_EQUAL:
                        op = ComparableOp.OP_EQUAL_EQUAL;
                        break;
                    case TK_NOT_EQUAL:
                        op = ComparableOp.OP_NOT_EQUAL;
                        break;
                }
                result =  parseNotExp(curPos + 1);
                if(result.nextPos - startPos >= 1) {    // at least parse primary
                    result = parseAdditiveExp(result.nextPos, result.result);
                }

            } catch (ParseException e) {
                if(op != null) {
                    throw new ParseException("Unable to parse right value in comparable expression!");
                }
            }
        }
        if(result == null || result.nextPos - startPos < 1) {    // at least parse primary
            return new ParseResult<>(leftExp, startPos);
        } else {
            return new ParseResult<>(new ComparableExp(leftExp, result.result, op), result.nextPos);
        }
    }

    public ParseResult<Exp> parseBilogicalExp(final int startPos, Exp leftExp) throws ParseException{
        int curPos = startPos;
        ParseResult<Exp> result = null;
        BiLogicalOp op = null;

        while(curPos < tokens.length) {
            try {
                Token t = checkTokenIsOr(curPos, BinopToken.TK_AND, BinopToken.TK_OR);

                switch ((BinopToken)t) {
                    case TK_AND:
                        op = BiLogicalOp.OP_AND;
                        break;
                    case TK_OR:
                        op = BiLogicalOp.OP_OR;
                        break;
                }
                result =  parseNotExp(curPos + 1);
                if(result.nextPos - startPos >= 1) {    // at least parse primary
                    result = parseAdditiveExp(result.nextPos, result.result);
                    result = parseComparableExp(result.nextPos, result.result);
                }
                curPos = result.nextPos;
            } catch (ParseException e) {
                break;
            }
        }
        if(result == null || result.nextPos - startPos < 1) {    // at least parse primary
            return new ParseResult<>(leftExp, startPos);
        } else {
            return new ParseResult<>(new BiLogicalExp(leftExp, result.result, op), result.nextPos);
        }
    }

    public ParseResult<Exp> parsePrimary(final int startPos) throws ParseException {
        final Token tokenHere = readToken(startPos);
        if (tokenHere instanceof VariableToken) {
            final VariableToken asVar = (VariableToken)tokenHere;
            return new ParseResult<>(new VariableExp(asVar.getName()), startPos + 1);
        } else if(tokenHere instanceof IntToken) {
            final IntToken asInt = (IntToken) tokenHere;
            return new ParseResult<>(new IntExp(asInt.getValue()), startPos + 1);
        } else if(tokenHere instanceof StringToken) {
            return parseString(tokenHere, startPos);
        } else if(tokenHere == KeywordToken.TK_TRUE || tokenHere == KeywordToken.TK_FALSE) {
            if(tokenHere == KeywordToken.TK_TRUE) {
                return new ParseResult<>(new BooleanExp(true),startPos + 1);
            } else {
                return new ParseResult<>(new BooleanExp(false), startPos + 1);
            }
        } else {
            checkTokenIs(startPos, BracketsToken.TK_LPAREN);
            final ParseResult<Exp> inner = parseExp(startPos + 1);
            checkTokenIs(inner.nextPos, BracketsToken.TK_RPAREN);
            return new ParseResult<>(inner.result,
                    inner.nextPos + 1);
        }
    }

    public ParseResult<Exp> parseString(final Token token, final int startPos) throws ParseException {
        String value = ((StringToken)token).getValue();
        String temp = value;
        int index = 0;
        int location = 0;
        LinkedHashMap<Integer, Exp> map = new LinkedHashMap<>();
        int length = value.length();
        String interpolation = "";
        boolean isBlockInterpolation = false;

        while(index < value.length()) {
            if(value.charAt(index) == '$') {
                if(index + 1 < length && value.charAt(index + 1) == '{') {
                    index += 2;
                    for(int i = index; i < length; i++) {
                        if(value.charAt(i) == '}') {
                            isBlockInterpolation = true;
                            break;
                        }
                    }
                    if(isBlockInterpolation) {
                        while (index < value.length() && value.charAt(index) != '}') {
                            interpolation += value.charAt(index);
                            index++;
                        }
                        index++;
//                        location++;
                    } else {
                        throw new ParseException("Invalid string interpolation! Expect }");
                    }
                } else if(index + 1 < length && Character.isLetter(value.charAt(index + 1))) {
                    index++;
                    interpolation += value.charAt(index);
                    index++;
                    while(index < length && Character.isLetterOrDigit(value.charAt(index))) {
                        interpolation += value.charAt(index);
                        index++;
                    }
                } else if(index + 1 < length && Character.isWhitespace(value.charAt(index + 1))){
                    index++;
                    location += 2;   // Now $ count
                }
                if(interpolation.length() > 0) {
                    try {
                        Tokenizer tokenizer = new Tokenizer(interpolation);
                        Token[] tokens = tokenizer.tokenize().stream().toArray(Token[]::new);
                        Parser parser = new Parser(tokens);
                        ParseResult<Exp> result = parser.parseExp(0);
                        map.put(location, result.result);
                        temp = temp.replace(isBlockInterpolation ? "${" + interpolation + "}" : "$" + interpolation, "");
                        isBlockInterpolation = false;
                        interpolation = "";
                    } catch (TokenizerException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                index++;
                location++;
            }
        }
        return new ParseResult<Exp>(map.size() > 0 ? new StringExp(temp, map) : new StringExp(temp, null), startPos + 1);
    }

    public  ParseResult<Exp> parseExp(final int startPos) throws ParseException {
        final Token tokenHere = readToken(startPos);
        if(tokenHere == KeywordToken.TK_IF) {
            checkTokenIs(startPos + 1, BracketsToken.TK_LPAREN);
            final ParseResult<Exp> condition = parseExp(startPos + 2);
            checkTokenIs(condition.nextPos, BracketsToken.TK_RPAREN);
            final ParseResult<Exp> ifTrue = parseExp(condition.nextPos + 1);
            if(tokens.length > ifTrue.nextPos && tokens[ifTrue.nextPos] == KeywordToken.TK_ELSE) {
                final ParseResult<Exp> ifFalse = parseExp(ifTrue.nextPos + 1);
                return new ParseResult<Exp>(new IfExp(condition.result, ifTrue.result, ifFalse.result),
                        ifFalse.nextPos);
            } else {
                return new ParseResult<Exp>(new IfExp(condition.result, ifTrue.result),
                        ifTrue.nextPos);
            }

        } else if(tokenHere == KeywordToken.TK_ARRAY_OF || tokenHere == KeywordToken.TK_MUTABLE_LIST_OF) {
            checkTokenIs(startPos + 1, BracketsToken.TK_LPAREN);
            int pos = startPos + 2;
            List<Exp> expList = new ArrayList<>();
            ParseResult<Exp> temp;
            while(readToken(pos) != BracketsToken.TK_RPAREN) {
                temp = parsePrimary(pos);
                expList.add(temp.result);
                if(readToken(temp.nextPos) == SymbolToken.TK_COMMA) {
                    pos = temp.nextPos + 1;
                } else {
                    pos = temp.nextPos;
                    break;
                }
            }
            checkTokenIs(pos, BracketsToken.TK_RPAREN);
            if(expList.size() <= 0) {
                throw new ParseException("At least one element in " + (tokenHere == KeywordToken.TK_ARRAY_OF ? "arrayOf!" : "listOf!"));
            }
            return new ParseResult<>(tokenHere == KeywordToken.TK_ARRAY_OF ? new ArrayOfExp(expList) : new MutableListOfExp(expList), pos + 1);
        } else if(tokenHere == TypeToken.TK_ARRAY) {
            return null;
        }
        else {
            ParseResult<Exp> result =  parseNotExp(startPos);
            if(result.nextPos - startPos >= 1) {    // at least parse primary
                result = parseAdditiveExp(result.nextPos, result.result);
                result = parseComparableExp(result.nextPos, result.result);
                result = parseBilogicalExp(result.nextPos, result.result);
                return result;
            } else {
                return result;
            }
        }
    }

    public Exp parseToplevelExp() throws ParseException {
        final ParseResult<Exp> result = parseExp(0);

        if(result.nextPos == tokens.length) {
            return result.result;
        } else {
            throw new ParseException("Extra tokens at end");
        }
    }

    public ParseResult<Stmt> parseStmt(final int startPos) throws ParseException {
        return null;
    }

    public ParseResult<Program> parseProgram(final int startPos) throws ParseException {
        return null;
    }
}
