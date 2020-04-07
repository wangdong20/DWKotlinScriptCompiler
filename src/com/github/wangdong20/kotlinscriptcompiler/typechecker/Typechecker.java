package com.github.wangdong20.kotlinscriptcompiler.typechecker;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;

import java.util.HashMap;
import java.util.Map;

public class Typechecker {

    public static Type typeOf(final Map<Variable, Pair<Type, Boolean>> gamma, final Exp e) throws IllTypedException {
        if(e instanceof IntExp) {
            return BasicType.TYPE_INT;
        } else if(e instanceof BooleanExp) {
            return BasicType.TYPE_BOOLEAN;
        } else if(e instanceof StringExp) {
            return BasicType.TYPE_STRING;
        } else if(e instanceof AdditiveExp) {
            final Type leftType = typeOf(gamma, ((AdditiveExp) e).getLeft());
            final Type rightType = typeOf(gamma, ((AdditiveExp) e).getRight());
            if (leftType == BasicType.TYPE_INT && rightType == BasicType.TYPE_INT) {
                return BasicType.TYPE_INT;
            }
            final AdditiveOp op = ((AdditiveExp) e).getOp();
            if (op == AdditiveOp.EXP_PLUS) {
                if (leftType == BasicType.TYPE_STRING && rightType == BasicType.TYPE_INT) {
                    return BasicType.TYPE_STRING;
                } else if (leftType == BasicType.TYPE_STRING && rightType == BasicType.TYPE_STRING) {
                    return BasicType.TYPE_STRING;
                }
            }
            throw new IllTypedException("Only Int + Int, Int - Int, String + Int, String + String accept!");
        } else if(e instanceof MultiplicativeExp) {
            final Type leftType = typeOf(gamma, ((MultiplicativeExp) e).getLeft());
            final Type rightType = typeOf(gamma, ((MultiplicativeExp) e).getRight());
            if (leftType == BasicType.TYPE_INT && rightType == BasicType.TYPE_INT) {
                return BasicType.TYPE_INT;
            } else {
                throw new IllTypedException("Only Int * Int and Int / Int accept!");
            }
        } else if(e instanceof ComparableExp) {
            final Type leftType = typeOf(gamma, ((ComparableExp) e).getLeft());
            final Type rightType = typeOf(gamma, ((ComparableExp) e).getRight());
            if (leftType == BasicType.TYPE_INT && rightType == BasicType.TYPE_INT) {
                return BasicType.TYPE_BOOLEAN;
            } else {
                throw new IllTypedException("Only Int can compare with Int!");
            }
        } else if(e instanceof BiLogicalExp) {
            final Type leftType = typeOf(gamma, ((BiLogicalExp) e).getLeft());
            final Type rightType = typeOf(gamma, ((BiLogicalExp) e).getRight());
            if (leftType == BasicType.TYPE_BOOLEAN && rightType == BasicType.TYPE_BOOLEAN) {
                return BasicType.TYPE_BOOLEAN;
            } else {
                throw new IllTypedException("Only Boolean && Boolean and Boolean || Boolean supported!");
            }
        } else if(e instanceof VariableExp) {
            if(gamma.containsKey(e)) {
                return gamma.get(e).getFirst();
            } else {
                throw new IllTypedException("Not in scope " + ((VariableExp) e).getName());
            }
        } else if(e instanceof ArrayExp) {
            LambdaExp lambdaExp = ((ArrayExp) e).getLambdaExp();
            if(lambdaExp.getParameterList().size() == 1) {  // ArrayExp only support Array(Int, {i - > exp})
                Exp[] variables = new Exp[1];
                Type[] types = new Type[1];
                lambdaExp.getParameterList().keySet().toArray(variables);
                lambdaExp.getParameterList().values().toArray(types);
                if(variables[0] instanceof VariableExp) {
                    if(types[0] == null) {
                        gamma.put((VariableExp)variables[0], new Pair<>(BasicType.TYPE_INT, false));
                    } else {
                        if(types[0] == BasicType.TYPE_INT) {
                            gamma.put((VariableExp)variables[0], new Pair<>(types[0], false));
                        } else {
                            throw new IllTypedException("Expected parameter type of Int!");
                        }
                    }
                }
                Type returnType = typeOf (gamma, ((ArrayExp) e).getLambdaExp().getReturnExp());
                gamma.remove(variables[0]);
                if(returnType instanceof BasicType) {
                    return new TypeArray((BasicType) returnType);
                } else {
                    throw new IllTypedException("Unsupported generic type: " + returnType);
                }
            } else {
                throw new IllTypedException("Parameter size should be 1");
            }

        } else if(e instanceof ArrayOfExp) {
            if(((ArrayOfExp) e).getExpList().size() > 0) {
                Type type = typeOf(gamma, ((ArrayOfExp) e).getExpList().get(0));
                Boolean isAny = false;
                for (Exp exp : ((ArrayOfExp) e).getExpList()) {
                    if(type != typeOf(gamma, exp)) {
                        isAny = true;
                    }
                }
                if(isAny) {
                    return new TypeArray(BasicType.TYPE_ANY);
                } else {
                    if(type instanceof BasicType)
                        return new TypeArray((BasicType) type);
                    else
                        throw new IllTypedException("Unsupported generic type: " + type);
                }
            } else {
                throw new IllTypedException("arrayOf(exp*) should have at least one expression in parameter");
            }
        } else if(e instanceof ArrayWithIndexExp) {
            if(gamma.containsKey(((ArrayWithIndexExp) e).getVariableExp())) {
                return gamma.get(e).getFirst();
            } else {
                throw new IllTypedException("Not in scope " + ((ArrayWithIndexExp) e).getVariableExp().getName());
            }
        } else if(e instanceof FunctionInstanceExp) {
            // TODO
            return null;
        }
        else {
            throw new IllTypedException("Unknown type!");
        }
    }

    public static Map<Variable, Pair<Type, Boolean>> typecheckStmt(final Map<Variable, Pair<Type, Boolean>> gamma, Stmt s) throws IllTypedException {
        if(s instanceof VariableDeclareStmt) {
            if(gamma.containsKey(((VariableDeclareStmt) s).getVariableExp())) {
                throw new IllTypedException("Redefined variable " + ((VariableDeclareStmt) s).getVariableExp().getName());
            } else {
                if(((VariableDeclareStmt) s).getType() != null) {
                    final Map<Variable, Pair<Type, Boolean>> copy = newCopy(gamma);
                    copy.put(((VariableDeclareStmt) s).getVariableExp(), new Pair<>(((VariableDeclareStmt) s).getType(), ((VariableDeclareStmt) s).isReadOnly()));
                    return copy;
                } else {
                    throw new IllTypedException("This variable must either have a type annotation or be initialized");
                }
            }
        } else if(s instanceof AssignStmt) {
            if(((AssignStmt) s).isNew()) {      // It means var, val a new variable.
                if(gamma.containsKey(((AssignStmt) s).getVariable())) {
                    throw new IllTypedException(((AssignStmt) s).getVariable() + " redefined!");
                }
                if (((AssignStmt) s).getType() != null) {
                    Type expectedType = ((AssignStmt) s).getType();
                    if (typeOf(gamma, ((AssignStmt) s).getExpression()).equals(expectedType)) {
                        final Map<Variable, Pair<Type, Boolean>> copy = newCopy(gamma);
                        copy.put(((AssignStmt) s).getVariable(), new Pair<>(expectedType, ((AssignStmt) s).isReadOnly()));
                        return copy;
                    } else {
                        throw new IllTypedException(expectedType + "expected!");
                    }
                } else {    // Type inference
                    Type type = typeOf(gamma, ((AssignStmt) s).getExpression());
                    final Map<Variable, Pair<Type, Boolean>> copy = newCopy(gamma);
                    copy.put(((AssignStmt) s).getVariable(), new Pair<>(type, ((AssignStmt) s).isReadOnly()));
                    return copy;
                }
            } else {    // we need to check gamma contain the variable or not in this case
                if(gamma.containsKey(((AssignStmt) s).getVariable())) {
                    if(gamma.get(((AssignStmt) s).getVariable()).getSecond()) { // Read only variable
                        throw new IllTypedException(((AssignStmt) s).getVariable() + " is read only variable!");
                    } else {
                        return gamma;
                    }
                } else {
                    throw new IllTypedException(((AssignStmt) s).getVariable() + " undefined!");
                }
            }
        } else if(s instanceof CompoundAssignStmt) {
            if(gamma.containsKey(((CompoundAssignStmt) s).getVariable())) {
                if(gamma.get(((CompoundAssignStmt) s).getVariable()).getSecond()) {
                    throw new IllTypedException("Read only variable cannot be assigned a new value!");
                }
                Type expected = typeOf(gamma, ((CompoundAssignStmt) s).getExpression());
                Variable variable = ((CompoundAssignStmt) s).getVariable();
                CompoundAssignOp op = ((CompoundAssignStmt) s).getOp();
                if (op == CompoundAssignOp.EXP_DIVIDE_EQUAL || op == CompoundAssignOp.EXP_MULTIPLY_EQUAL
                        || op == CompoundAssignOp.EXP_MINUS_EQUAL) {
                    if(expected == BasicType.TYPE_INT && gamma.get(variable).getFirst() == BasicType.TYPE_INT) {
                        return gamma;
                    } else {
                        throw new IllTypedException("-=, *=, /= only support integer operation!");
                    }
                } else {
                    if((expected == BasicType.TYPE_INT && gamma.get(variable).getFirst() == BasicType.TYPE_INT)
                            || (expected == BasicType.TYPE_STRING && gamma.get(variable).getFirst() == BasicType.TYPE_STRING)
                            || (expected == BasicType.TYPE_INT && gamma.get(variable).getFirst() == BasicType.TYPE_STRING)) {
                        return gamma;
                    } else {
                        throw new IllTypedException("Only Int += Int, String += Int, String += String supported!");
                    }
                }
            } else {
                throw new IllTypedException(((CompoundAssignStmt) s).getVariable() + " undefined!");
            }
        } else if(s instanceof FunctionDeclareStmt) {
            return null;
        }
        else {
            return null;
        }
    }

    private static Map<Variable, Pair<Type, Boolean>> newCopy(final Map<Variable, Pair<Type, Boolean>> gamma) {
        final Map<Variable, Pair<Type, Boolean>> copy = new HashMap<>();
        copy.putAll(gamma);
        return copy;
    }
}
