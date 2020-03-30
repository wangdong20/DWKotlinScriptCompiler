package com.github.wangdong20.kotlinscriptcompiler.parser;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.*;
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
        // We will consider variable or function variable instance as primary
        if (tokenHere instanceof VariableToken) {
            final VariableToken asVar = (VariableToken)tokenHere;
            if(startPos + 1 < tokens.length) {  // we dont want throw exception now.
                Token next = readToken(startPos + 1);
                VariableExp name = new VariableExp(asVar.getName());

                if(next == BracketsToken.TK_LPAREN) {
                    Token temp = null;
                    int pos = startPos + 2;
                    List<Exp> parameterList = new ArrayList<>();
                    while((temp = readToken(pos)) != BracketsToken.TK_RPAREN) {
                        if(temp instanceof VariableToken) {
                            parameterList.add(new VariableExp(((VariableToken) temp).getName()));
                        } else if(temp instanceof IntToken) {
                            parameterList.add(new IntExp(((IntToken) temp).getValue()));
                        } else if(temp instanceof StringToken) {
                            parameterList.add(parseString(temp, pos).result);
                        } else if(temp == KeywordToken.TK_TRUE || temp == KeywordToken.TK_FALSE) {
                            parameterList.add(new BooleanExp(temp == KeywordToken.TK_TRUE ? true : false));
                        } else {
                            throw new ParseException("Unsupport function parameter!");
                        }
                        pos++;
                        if(readToken(pos) == SymbolToken.TK_COMMA) {
                            pos++;
                        } else {
                            break;
                        }
                    }
                    checkTokenIs(pos, BracketsToken.TK_RPAREN);
                    return new ParseResult<>(new FunctionInstanceExp(name, parameterList), pos + 1);
                } else if(next == UnopToken.TK_PLUS_PLUS || next == UnopToken.TK_MINUS_MINUS) {
                    return new ParseResult<>(new SelfOperationExp(name,
                            next == UnopToken.TK_PLUS_PLUS ? SelfOp.OP_SELF_INCREASE : SelfOp.OP_SELF_DECREASE,
                            false), startPos + 2);
                }
                else {
                    return new ParseResult<>(new VariableExp(asVar.getName()), startPos + 1);
                }
            }
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
        } else if(tokenHere == UnopToken.TK_PLUS_PLUS || tokenHere == UnopToken.TK_MINUS_MINUS) {
            if(startPos + 1 < tokens.length) {
                Token next = readToken(startPos + 1);
                if(next instanceof VariableToken) {
                    return new ParseResult<>(new SelfOperationExp(new VariableExp(((VariableToken) next).getName()),
                            tokenHere == UnopToken.TK_PLUS_PLUS ? SelfOp.OP_SELF_INCREASE : SelfOp.OP_SELF_DECREASE,
                            true), startPos + 2);
                } else {
                    throw new ParseException("VariableToken expected after self increase or decrease operator!");
                }
            } else {
                throw new ParseException("VariableToken expected after self increase or decrease operator!");
            }
        }
        else {
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

    public ParseResult<Exp> parseLambdaExp(final int startPos) throws ParseException {
        checkTokenIs(startPos, BracketsToken.TK_LCURLY);
        Token tokenHere;
        VariableExp variableExp = null;
        Type type = null;
        int pos = startPos + 1;
        LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
        while((tokenHere = readToken(pos)) != SymbolToken.TK_ARROW) {
            if (tokenHere instanceof VariableToken) {
                variableExp = new VariableExp(((VariableToken)tokenHere).getName());
                pos++;
                tokenHere = readToken(pos);
                if(tokenHere == SymbolToken.TK_COLON) {
                    pos++;
                    tokenHere = readToken(pos);
                    switch ((TypeToken)tokenHere) {
                        case TK_TYPE_INT:
                            type = BasicType.TYPE_INT;
                            pos++;
                            break;
                        case TK_TYPE_STRING:
                            type = BasicType.TYPE_STRING;
                            pos++;
                            break;
                        case TK_TYPE_BOOLEAN:
                            type = BasicType.TYPE_BOOLEAN;
                            pos++;
                            break;
                        case TK_TYPE_UNIT:
                            type = BasicType.TYPE_UNIT;
                            pos++;
                            break;
                        case TK_ANY:
                            type = BasicType.TYPE_ANY;
                            pos++;
                            break;
                        case TK_ARRAY: case TK_MUTABLE_LIST:
                            pos++;
                            ParseResult<BasicType> genericType = parseGenericType(pos);
                            type = tokenHere == TypeToken.TK_ARRAY ? new TypeArray(genericType.result) :
                                    new TypeMutableList(genericType.result);
                            pos = genericType.nextPos;
                            break;
                    }
                }
                parameterList.put(variableExp, type);
                if(readToken(pos) != SymbolToken.TK_COMMA) {
                    break;
                } else {
                    pos++;
                }
            }
            type = null;
            variableExp = null;
        }
        checkTokenIs(pos, SymbolToken.TK_ARROW);
        ParseResult<Exp> returnExp = parseExp(pos + 1);
        checkTokenIs(returnExp.nextPos, BracketsToken.TK_RCURLY);
        return new ParseResult<>(new LambdaExp(parameterList, returnExp.result), returnExp.nextPos + 1);
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
        } else if(tokenHere == TypeToken.TK_ARRAY || tokenHere == TypeToken.TK_MUTABLE_LIST) {
            checkTokenIs(startPos + 1, BracketsToken.TK_LPAREN);
            int pos = startPos + 2;
            IntExp intExp  = null;
            LambdaExp lambdaExp = null;
            final Token t = readToken(pos);
            if(t instanceof IntToken) {
                intExp = new IntExp(((IntToken)t).getValue());
                pos++;
                checkTokenIs(pos, SymbolToken.TK_COMMA);
                pos++;
                ParseResult<Exp> resultParse = parseLambdaExp(pos);
                lambdaExp = (LambdaExp) resultParse.result;
                checkTokenIs(resultParse.nextPos, BracketsToken.TK_RPAREN);
                return new ParseResult<>(tokenHere == TypeToken.TK_ARRAY ? new ArrayExp(intExp, lambdaExp) :
                        new MutableListExp(intExp, lambdaExp), resultParse.nextPos + 1);
            } else {
                throw new ParseException("Integer of size expected in Array initialize operation.");
            }
        } else if(tokenHere == BracketsToken.TK_LCURLY) {
            return parseLambdaExp(startPos);    // Include TK_LCURLY
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

    /**
     * generic type only support basic type
     */
    public ParseResult<BasicType> parseGenericType(final int startPos) throws ParseException {
        checkTokenIs(startPos, BracketsToken.TK_LANGLE);
        final Token tokenHere = readToken(startPos + 1);
        BasicType genericType = null;
        switch ((TypeToken)tokenHere) {
            case TK_TYPE_INT:
                genericType = BasicType.TYPE_INT;
                break;
            case TK_TYPE_STRING:
                genericType = BasicType.TYPE_STRING;
                break;
            case TK_TYPE_BOOLEAN:
                genericType = BasicType.TYPE_BOOLEAN;
                break;
            case TK_TYPE_UNIT:
                genericType = BasicType.TYPE_UNIT;
                break;
            case TK_ANY:
                genericType = BasicType.TYPE_ANY;
                break;
        }
        checkTokenIs(startPos + 2, BracketsToken.TK_RANGLE);
        return new ParseResult<>(genericType, startPos + 3);
    }

    public ParseResult<TypeHighOrderFunction> parseTypeHighOrderFunction(final int startPos) throws ParseException {
        checkTokenIs(startPos, BracketsToken.TK_LPAREN);
        ParseResult<Type> typeResult = null;
        List<Type> parameterTypes = new ArrayList<>();
        int pos = startPos + 1;
        Token temp = null;
        while((temp = readToken(pos)) != BracketsToken.TK_RPAREN) {
            switch ((TypeToken)temp) {
                case TK_TYPE_INT:
                    parameterTypes.add(BasicType.TYPE_INT);
                    pos++;
                    break;
                case TK_TYPE_STRING:
                    parameterTypes.add(BasicType.TYPE_STRING);
                    pos++;
                    break;
                case TK_TYPE_BOOLEAN:
                    parameterTypes.add(BasicType.TYPE_BOOLEAN);
                    pos++;
                    break;
                case TK_TYPE_UNIT:
                    parameterTypes.add(BasicType.TYPE_UNIT);
                    pos++;
                    break;
                case TK_ANY:
                    parameterTypes.add(BasicType.TYPE_ANY);
                    pos++;
                    break;
                case TK_ARRAY: case TK_MUTABLE_LIST:
                    pos++;
                    ParseResult<BasicType> genericType = parseGenericType(pos);
                    parameterTypes.add(temp == TypeToken.TK_ARRAY ? new TypeArray(genericType.result) :
                            new TypeMutableList(genericType.result));
                    pos = genericType.nextPos;
                    break;
            }
            if((temp = readToken(pos)) == SymbolToken.TK_COMMA) {
                pos++;
            }
        }
        pos++;
        checkTokenIs(pos, SymbolToken.TK_ARROW);
        Type retureType = null;
        pos++;
        temp = readToken(pos);
        switch ((TypeToken)temp) {
            case TK_TYPE_INT:
                retureType = BasicType.TYPE_INT;
                pos++;
                break;
            case TK_TYPE_STRING:
                retureType = BasicType.TYPE_STRING;
                pos++;
                break;
            case TK_TYPE_BOOLEAN:
                retureType = BasicType.TYPE_BOOLEAN;
                pos++;
                break;
            case TK_TYPE_UNIT:
                retureType = BasicType.TYPE_UNIT;
                pos++;
                break;
            case TK_ANY:
                retureType = BasicType.TYPE_ANY;
                pos++;
                break;
            case TK_ARRAY: case TK_MUTABLE_LIST:
                pos++;
                ParseResult<BasicType> genericType = parseGenericType(pos);
                retureType = temp == TypeToken.TK_ARRAY ? new TypeArray(genericType.result) :
                        new TypeMutableList(genericType.result);
                pos = genericType.nextPos;
                break;
        }
        return new ParseResult<>(new TypeHighOrderFunction(parameterTypes, retureType), pos);
    }

    public ParseResult<Stmt> parsePrimaryStmt(final int startPos) throws ParseException {
        final Token tokenHere = readToken(startPos);
        ParseResult<Stmt> stmtResult = null;
        if(tokenHere instanceof VariableToken) {
            final VariableToken asVar = (VariableToken)tokenHere;
            if(startPos + 1 < tokens.length) {
                Token next = readToken(startPos + 1);
                if(next == BinopToken.TK_EQUAL) {
                    ParseResult<Exp> expParseResult = parseExp(startPos + 2);
                    stmtResult = new ParseResult<Stmt>(new AssignStmt(expParseResult.result, new VariableExp(asVar.getName()), false), expParseResult.nextPos);
                } else if(next == BinopToken.TK_PLUS_EQUAL || next == BinopToken.TK_MULTIPLY_EQUAL
                    || next == BinopToken.TK_MINUS_EQUAL || next == BinopToken.TK_DIVIDE_EQUAL) {
                    ParseResult<Exp> expParseResult = parseExp(startPos + 2);
                    CompoundAssignOp op = null;
                    switch ((BinopToken)next) {
                        case TK_PLUS_EQUAL:
                            op = CompoundAssignOp.EXP_PLUS_EQUAL;
                            break;
                        case TK_MINUS_EQUAL:
                            op = CompoundAssignOp.EXP_MINUS_EQUAL;
                            break;
                        case TK_MULTIPLY_EQUAL:
                            op = CompoundAssignOp.EXP_MULTIPLY_EQUAL;
                            break;
                        case TK_DIVIDE_EQUAL:
                            op = CompoundAssignOp.EXP_DIVIDE_EQUAL;
                    }
                    if(expParseResult.nextPos == tokens.length) {
                        stmtResult = new ParseResult<Stmt>(new CompoundAssignStmt(expParseResult.result,
                                new VariableExp(asVar.getName()), op), expParseResult.nextPos);
                    } else {
                        checkTokenIsOr(expParseResult.nextPos + 1, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                        stmtResult = new ParseResult<Stmt>(new CompoundAssignStmt(expParseResult.result,
                                new VariableExp(asVar.getName()), op), expParseResult.nextPos + 1);
                    }
                } else if(next == UnopToken.TK_PLUS_PLUS || next == UnopToken.TK_MINUS_MINUS) {
                    if(startPos + 2 == tokens.length) {
                        stmtResult = new ParseResult<Stmt>(new SelfOperationStmt(new SelfOperationExp(new VariableExp(asVar.getName()),
                                next == UnopToken.TK_PLUS_PLUS ? SelfOp.OP_SELF_INCREASE : SelfOp.OP_SELF_DECREASE, false)), startPos + 2);
                    } else {
                        checkTokenIsOr(startPos + 2, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                        stmtResult = new ParseResult<Stmt>(new SelfOperationStmt(new SelfOperationExp(new VariableExp(asVar.getName()),
                                next == UnopToken.TK_PLUS_PLUS ? SelfOp.OP_SELF_INCREASE : SelfOp.OP_SELF_DECREASE, false)), startPos + 3);
                    }
                } else if(next == BracketsToken.TK_LPAREN) {
                    Token temp = null;
                    int pos = startPos + 2;
                    List<Exp> parameterList = new ArrayList<>();
                    while((temp = readToken(pos)) != BracketsToken.TK_RPAREN) {
                        if(temp instanceof VariableToken) {
                            parameterList.add(new VariableExp(((VariableToken) temp).getName()));
                        } else if(temp instanceof IntToken) {
                            parameterList.add(new IntExp(((IntToken) temp).getValue()));
                        } else if(temp instanceof StringToken) {
                            parameterList.add(parseString(temp, pos).result);
                        } else if(temp == KeywordToken.TK_TRUE || temp == KeywordToken.TK_FALSE) {
                            parameterList.add(new BooleanExp(temp == KeywordToken.TK_TRUE ? true : false));
                        } else {
                            throw new ParseException("Unsupport function parameter!");
                        }
                        pos++;
                        if(readToken(pos) == SymbolToken.TK_COMMA) {
                            pos++;
                        } else {
                            break;
                        }
                    }
                    checkTokenIs(pos, BracketsToken.TK_RPAREN);
                    if(pos + 1 == tokens.length) {
                        stmtResult = new ParseResult<>(new FunctionInstanceStmt(new FunctionInstanceExp(new VariableExp(asVar.getName()), parameterList)), pos + 1);
                    } else {
                        checkTokenIsOr(pos + 1, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                        stmtResult = new ParseResult<>(new FunctionInstanceStmt(new FunctionInstanceExp(new VariableExp(asVar.getName()), parameterList)), pos + 2);
                    }
                }
            } else {
                throw new ParseException("Assignment operator expected!");
            }
        } else if(tokenHere == UnopToken.TK_PLUS_PLUS || tokenHere == UnopToken.TK_MINUS_MINUS) {
            if(startPos + 1 < tokens.length) {
                Token next = readToken(startPos + 1);
                if(next instanceof VariableToken) {
                    if(startPos + 2 == tokens.length) {
                        stmtResult = new ParseResult<Stmt>(new SelfOperationStmt(new SelfOperationExp(new VariableExp(((VariableToken) next).getName()),
                                tokenHere == UnopToken.TK_PLUS_PLUS ? SelfOp.OP_SELF_INCREASE : SelfOp.OP_SELF_DECREASE, true)), startPos + 2);
                    } else {
                        checkTokenIsOr(startPos + 2, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                        stmtResult = new ParseResult<Stmt>(new SelfOperationStmt(new SelfOperationExp(new VariableExp(((VariableToken) next).getName()),
                                next == UnopToken.TK_PLUS_PLUS ? SelfOp.OP_SELF_INCREASE : SelfOp.OP_SELF_DECREASE, false)), startPos + 3);
                    }
                }
            } else {
                throw new ParseException("Variable expected after ++, --");
            }
        }
        else if(tokenHere == KeywordToken.TK_VAR || tokenHere == KeywordToken.TK_VAL) {
            Token next = readToken(startPos + 1);
            Type type = null;
            if(next instanceof VariableToken) {
                final VariableToken asVar = (VariableToken)next;
                int pos = startPos + 2;
                if((next = readToken(pos)) == SymbolToken.TK_COLON) {
                    pos++;
                    next = readToken(pos);
                    if(next == BracketsToken.TK_LPAREN) {
                        ParseResult<TypeHighOrderFunction> result = parseTypeHighOrderFunction(pos);
                        type = result.result;
                        pos = result.nextPos;
                    } else {
                        switch ((TypeToken)next) {
                            case TK_TYPE_INT:
                                type = BasicType.TYPE_INT;
                                pos++;
                                break;
                            case TK_TYPE_STRING:
                                type = BasicType.TYPE_STRING;
                                pos++;
                                break;
                            case TK_TYPE_BOOLEAN:
                                type = BasicType.TYPE_BOOLEAN;
                                pos++;
                                break;
                            case TK_TYPE_UNIT:
                                type = BasicType.TYPE_UNIT;
                                pos++;
                                break;
                            case TK_ANY:
                                type = BasicType.TYPE_ANY;
                                pos++;
                                break;
                            case TK_ARRAY: case TK_MUTABLE_LIST:
                                pos++;
                                ParseResult<BasicType> genericType = parseGenericType(pos);
                                type = next == TypeToken.TK_ARRAY ? new TypeArray(genericType.result) :
                                        new TypeMutableList(genericType.result);
                                pos = genericType.nextPos;
                                break;
                        }
                    }
                }
                checkTokenIs(pos, BinopToken.TK_EQUAL);
                ParseResult<Exp> resultExp = parseExp(pos + 1);
                if(resultExp.nextPos == tokens.length) {
                    stmtResult = new ParseResult<>(new AssignStmt(resultExp.result, new VariableExp(asVar.getName()),
                            type, tokenHere == KeywordToken.TK_VAL ? true : false), resultExp.nextPos);
                } else {
                    checkTokenIsOr(resultExp.nextPos, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                    stmtResult = new ParseResult<>(new AssignStmt(resultExp.result, new VariableExp(asVar.getName()),
                            type, tokenHere == KeywordToken.TK_VAL ? true : false), resultExp.nextPos + 1);
                }
            } else {
                throw new ParseException("Variable expected in var, val statement!");
            }
        } else if(tokenHere == KeywordToken.TK_PRINT || tokenHere == KeywordToken.TK_PRINTLN) {
            checkTokenIs(startPos + 1, BracketsToken.TK_LPAREN);
            ParseResult<Exp> resultExp = parseExp(startPos + 2);
            checkTokenIs(resultExp.nextPos, BracketsToken.TK_RPAREN);
            if(resultExp.nextPos + 1 == tokens.length) {
                stmtResult = new ParseResult<>(tokenHere == KeywordToken.TK_PRINT ?
                        new PrintStmt(resultExp.result) : new PrintlnStmt(resultExp.result), resultExp.nextPos + 1);
            } else {
                checkTokenIsOr(resultExp.nextPos + 1, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                stmtResult = new ParseResult<>(tokenHere == KeywordToken.TK_PRINT ?
                        new PrintStmt(resultExp.result) : new PrintlnStmt(resultExp.result), resultExp.nextPos + 2);
            }
        } else if(tokenHere == KeywordToken.TK_BREAK || tokenHere == KeywordToken.TK_CONTINUE) {
            if(startPos + 1 == tokens.length) {
                stmtResult = new ParseResult<>(tokenHere == KeywordToken.TK_BREAK ?
                        ControlLoopStmt.STMT_BREAK : ControlLoopStmt.STMT_CONTINUE, startPos + 1);
            } else {
                checkTokenIsOr(startPos + 1, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                stmtResult = new ParseResult<>(tokenHere == KeywordToken.TK_BREAK ?
                        ControlLoopStmt.STMT_BREAK : ControlLoopStmt.STMT_CONTINUE, startPos + 2);
            }
        } else if(tokenHere == KeywordToken.TK_RETURN) {
            ParseResult<Exp> returnExp = parseExp(startPos + 1);
            if(returnExp.nextPos == tokens.length) {
                stmtResult = new ParseResult<>(new ReturnStmt(returnExp.result), returnExp.nextPos);
            } else {
                checkTokenIsOr(returnExp.nextPos, SymbolToken.TK_LINE_BREAK, SymbolToken.TK_SEMICOLON);
                stmtResult = new ParseResult<>(new ReturnStmt(returnExp.result), returnExp.nextPos + 1);
            }
        }
        return stmtResult;
    }

    private int skipLineBreakOrSemicolon(final int startPos) throws ParseException {
        if(startPos < tokens.length) {
            Token token = readToken(startPos);
            if (token != SymbolToken.TK_LINE_BREAK && token != SymbolToken.TK_SEMICOLON) {
                return startPos;
            } else {
                int pos = startPos + 1;
                if (pos < tokens.length) {
                    token = readToken(pos);
                    while (token == SymbolToken.TK_SEMICOLON || token == SymbolToken.TK_LINE_BREAK) {
                        pos++;
                        if (pos < tokens.length) {
                            token = readToken(pos);
                        } else {
                            break;
                        }
                    }
                }
                return pos;
            }
        } else {
            return startPos;
        }
    }

    public ParseResult<BlockStmt> parseBlockStmt(final int startPos) throws ParseException {
        checkTokenIs(startPos, BracketsToken.TK_LCURLY);
        int pos = startPos + 1;
        Token temp = null;
        ParseResult<BlockStmt> resultStmt;
        List<Stmt> stmtList = new ArrayList<>();
        pos = skipLineBreakOrSemicolon(pos);
        while((temp = readToken(pos)) != BracketsToken.TK_RCURLY) {
            pos = skipLineBreakOrSemicolon(pos);
            if(temp == KeywordToken.TK_FOR) {
                pos++;
                checkTokenIs(pos, BracketsToken.TK_LPAREN);
                pos++;
                if((temp = readToken(pos)) instanceof VariableToken) {
                    VariableExp variableExp = new VariableExp(((VariableToken)temp).getName());
                    pos++;
                    checkTokenIs(pos, KeywordToken.TK_IN);
                    pos++;
                    if((temp = readToken(pos)) instanceof IntToken) {   // RangeExp
                        IntExp startExp = new IntExp(((IntToken)temp).getValue());
                        pos++;
                        checkTokenIs(pos, SymbolToken.TK_DOT_DOT);
                        pos++;
                        if((temp = readToken(pos)) instanceof IntToken) {
                            IntExp endExp = new IntExp(((IntToken)temp).getValue());
                            pos++;
                            checkTokenIs(pos, BracketsToken.TK_RPAREN);
                            pos++;
                            ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                            stmtList.add(new ForStmt(variableExp, new RangeExp(startExp, endExp), blockStmt.result));
                            pos = blockStmt.nextPos;
                            checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                            pos++;
                        } else {
                            throw new ParseException("Integer expected in range expression!");
                        }
                    } else if((temp = readToken(pos)) instanceof VariableToken) {   // ArrayExp
                        pos++;
                        checkTokenIs(pos, BracketsToken.TK_RPAREN);
                        pos++;
                        ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                        stmtList.add(new ForStmt(variableExp, new VariableExp(((VariableToken)temp).getName()), blockStmt.result));
                        pos = blockStmt.nextPos;
                        checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                        pos++;
                    } else {
                        throw new ParseException("Array or range expected in for loop!");
                    }
                }
            } //Function Declaretion cannot be in block
            else if(temp == KeywordToken.TK_IF) {
                pos++;
                checkTokenIs(pos, BracketsToken.TK_LPAREN);
                pos++;
                ParseResult<Exp> resultExp = parseExp(pos);
                pos = resultExp.nextPos;
                checkTokenIs(pos, BracketsToken.TK_RPAREN);
                pos++;
                ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                ParseResult<BlockStmt> elseBlock = null;
                if((temp = readToken(blockStmt.nextPos)) == KeywordToken.TK_ELSE) {
                    elseBlock = parseBlockStmt(blockStmt.nextPos + 1);
                }
                stmtList.add(new IfStmt(resultExp.result, blockStmt.result, elseBlock == null ? null : elseBlock.result));
                pos = elseBlock == null ? blockStmt.nextPos : elseBlock.nextPos;
                checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                pos++;
            } else if(temp == KeywordToken.TK_WHILE) {
                pos++;
                checkTokenIs(pos, BracketsToken.TK_LPAREN);
                pos++;
                ParseResult<Exp> resultExp = parseExp(pos);
                pos = resultExp.nextPos;
                checkTokenIs(pos, BracketsToken.TK_RPAREN);
                pos++;
                ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                stmtList.add(new WhileStmt(resultExp.result, blockStmt.result));
                pos = blockStmt.nextPos;
                checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                pos++;
            } else {
                ParseResult<Stmt> primaryStmt = parsePrimaryStmt(pos);
                stmtList.add(primaryStmt.result);
                pos = primaryStmt.nextPos;
            }
            pos = skipLineBreakOrSemicolon(pos);
        }
        pos++;
        return new ParseResult<>(new BlockStmt(stmtList), pos);
    }

    public ParseResult<Stmt> parseStmt(final int startPos) throws ParseException {
        Token tokenHere = readToken(startPos);
        int pos = startPos;
        if(tokenHere == KeywordToken.TK_FOR) {
            pos++;
            checkTokenIs(pos, BracketsToken.TK_LPAREN);
            pos++;
            if((tokenHere = readToken(pos)) instanceof VariableToken) {
                VariableExp variableExp = new VariableExp(((VariableToken)tokenHere).getName());
                pos++;
                checkTokenIs(pos, KeywordToken.TK_IN);
                pos++;
                if((tokenHere = readToken(pos)) instanceof IntToken) {   // RangeExp
                    IntExp startExp = new IntExp(((IntToken)tokenHere).getValue());
                    pos++;
                    checkTokenIs(pos, SymbolToken.TK_DOT_DOT);
                    pos++;
                    if((tokenHere = readToken(pos)) instanceof IntToken) {
                        IntExp endExp = new IntExp(((IntToken)tokenHere).getValue());
                        pos++;
                        checkTokenIs(pos, BracketsToken.TK_RPAREN);
                        pos++;
                        ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                        pos = blockStmt.nextPos;
                        if(pos < tokens.length) {   // not the end the program
                            checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                            pos++;
                        }
                        return new ParseResult<>(new ForStmt(variableExp, new RangeExp(startExp, endExp), blockStmt.result), pos);
                    } else {
                        throw new ParseException("Integer expected in range expression!");
                    }
                } else if((tokenHere = readToken(pos)) instanceof VariableToken) {   // ArrayExp
                    pos++;
                    checkTokenIs(pos, BracketsToken.TK_RPAREN);
                    pos++;
                    ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                    pos = blockStmt.nextPos;
                    if(pos < tokens.length) {   // not the end the program
                        checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                        pos++;
                    }
                    return new ParseResult<>(new ForStmt(variableExp, new VariableExp(((VariableToken)tokenHere).getName()), blockStmt.result), pos);
                } else {
                    throw new ParseException("Array or range expected in for loop!");
                }
            } else {
                throw new ParseException("Variable expected in for loop!");
            }
        }
        else if(tokenHere == KeywordToken.TK_FUN) {
            pos++;
            if((tokenHere = readToken(pos)) instanceof VariableToken) {
                VariableExp asVar = new VariableExp(((VariableToken) tokenHere).getName());
                VariableExp variableExp = null;
                Type type = null;
                pos++;
                checkTokenIs(pos, BracketsToken.TK_LPAREN);
                pos++;
                LinkedHashMap<Exp, Type> parameterList = new LinkedHashMap<>();
                while((tokenHere = readToken(pos)) != BracketsToken.TK_RPAREN) {
                    if (tokenHere instanceof VariableToken) {
                        variableExp = new VariableExp(((VariableToken)tokenHere).getName());
                        pos++;
                        tokenHere = readToken(pos);
                        if(tokenHere == SymbolToken.TK_COLON) {
                            pos++;
                            tokenHere = readToken(pos);
                            switch ((TypeToken)tokenHere) {
                                case TK_TYPE_INT:
                                    type = BasicType.TYPE_INT;
                                    pos++;
                                    break;
                                case TK_TYPE_STRING:
                                    type = BasicType.TYPE_STRING;
                                    pos++;
                                    break;
                                case TK_TYPE_BOOLEAN:
                                    type = BasicType.TYPE_BOOLEAN;
                                    pos++;
                                    break;
                                case TK_TYPE_UNIT:
                                    type = BasicType.TYPE_UNIT;
                                    pos++;
                                    break;
                                case TK_ANY:
                                    type = BasicType.TYPE_ANY;
                                    pos++;
                                    break;
                                case TK_ARRAY: case TK_MUTABLE_LIST:
                                    pos++;
                                    ParseResult<BasicType> genericType = parseGenericType(pos);
                                    type = tokenHere == TypeToken.TK_ARRAY ? new TypeArray(genericType.result) :
                                            new TypeMutableList(genericType.result);
                                    pos = genericType.nextPos;
                                    break;
                            }
                        }
                        parameterList.put(variableExp, type);
                        if(readToken(pos) != SymbolToken.TK_COMMA) {
                            break;
                        } else {
                            pos++;
                        }
                    } else {
                        if(parameterList.size() > 0) {
                            throw new ParseException("Variable expected after ,");
                        } else {
                            pos++;
                            break;
                        }
                    }
                    type = null;
                    variableExp = null;
                }
                checkTokenIs(pos, BracketsToken.TK_RPAREN);
                Type retureType = null;
                pos++;
                if((tokenHere = readToken(pos)) == SymbolToken.TK_COLON) {
                    pos++;
                    tokenHere = readToken(pos);
                    switch ((TypeToken)tokenHere) {
                        case TK_TYPE_INT:
                            retureType = BasicType.TYPE_INT;
                            pos++;
                            break;
                        case TK_TYPE_STRING:
                            retureType = BasicType.TYPE_STRING;
                            pos++;
                            break;
                        case TK_TYPE_BOOLEAN:
                            retureType = BasicType.TYPE_BOOLEAN;
                            pos++;
                            break;
                        case TK_TYPE_UNIT:
                            retureType = BasicType.TYPE_UNIT;
                            pos++;
                            break;
                        case TK_ANY:
                            retureType = BasicType.TYPE_ANY;
                            pos++;
                            break;
                        case TK_ARRAY: case TK_MUTABLE_LIST:
                            pos++;
                            ParseResult<BasicType> genericType = parseGenericType(pos);
                            retureType = tokenHere == TypeToken.TK_ARRAY ? new TypeArray(genericType.result) :
                                    new TypeMutableList(genericType.result);
                            pos = genericType.nextPos;
                            break;
                    }
                    ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                    pos = blockStmt.nextPos;
                    if(pos < tokens.length) {   // not the end the program
                        checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                        pos++;
                    }
                    return new ParseResult<>(new FunctionDeclareStmt(asVar, retureType, parameterList, blockStmt.result), pos);
                } else if((tokenHere = readToken(pos)) == BracketsToken.TK_LCURLY) {
                    ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
                    pos = blockStmt.nextPos;
                    if(pos < tokens.length) {   // not the end the program
                        checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                        pos++;
                    }
                    return new ParseResult<>(new FunctionDeclareStmt(asVar, BasicType.TYPE_UNIT, parameterList, blockStmt.result), pos);
                } else {
                    throw new ParseException(": or { expected after function parameter!");
                }
            } else {
                throw new ParseException("Function name expected!");
            }
        }
        else if(tokenHere == KeywordToken.TK_IF) {
            pos++;
            checkTokenIs(pos, BracketsToken.TK_LPAREN);
            pos++;
            ParseResult<Exp> resultExp = parseExp(pos);
            pos = resultExp.nextPos;
            checkTokenIs(pos, BracketsToken.TK_RPAREN);
            pos++;
            ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
            ParseResult<BlockStmt> elseBlock = null;
            if((tokenHere = readToken(blockStmt.nextPos)) == KeywordToken.TK_ELSE) {
                elseBlock = parseBlockStmt(blockStmt.nextPos + 1);
            }
            pos = elseBlock == null ? blockStmt.nextPos : elseBlock.nextPos;
            if(pos < tokens.length) {   // not the end the program
                checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                pos++;
            }
            return new ParseResult<>(new IfStmt(resultExp.result, blockStmt.result, elseBlock.result), pos);
        } else if(tokenHere == KeywordToken.TK_WHILE) {
            pos++;
            checkTokenIs(pos, BracketsToken.TK_LPAREN);
            pos++;
            ParseResult<Exp> resultExp = parseExp(pos);
            pos = resultExp.nextPos;
            checkTokenIs(pos, BracketsToken.TK_RPAREN);
            pos++;
            ParseResult<BlockStmt> blockStmt = parseBlockStmt(pos);
            pos = blockStmt.nextPos;
            if(pos < tokens.length) {   // not the end the program
                checkTokenIsOr(pos, SymbolToken.TK_SEMICOLON, SymbolToken.TK_LINE_BREAK);
                pos++;
            }
            return new ParseResult<>(new WhileStmt(resultExp.result, blockStmt.result), pos);
        } else {
            return parsePrimaryStmt(startPos);
        }
    }

    public Stmt parseToplevelStmt() throws ParseException {
        final ParseResult<Stmt> result = parseStmt(0);

        if(result.nextPos == tokens.length) {
            return result.result;
        } else {
            throw new ParseException("Extra tokens at end");
        }
    }

    public ParseResult<Program> parseProgram(final int startPos) throws ParseException {
        List<Stmt> stmtList = new ArrayList<>();
        int pos = startPos;
        while(pos < tokens.length) {
            pos = skipLineBreakOrSemicolon(pos);
            ParseResult<Stmt> stmtParseResult = parseStmt(pos);
            stmtList.add(stmtParseResult.result);
            pos = skipLineBreakOrSemicolon(stmtParseResult.nextPos);
        }
        return new ParseResult<>(new Program(stmtList), pos);
    }

    public Program parseToplevelProgram() throws ParseException {
        final ParseResult<Program> result = parseProgram(0);

        if(result.nextPos == tokens.length) {
            return result.result;
        } else {
            throw new ParseException("Extra tokens at end");
        }
    }
}
