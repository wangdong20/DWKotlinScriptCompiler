package com.github.wangdong20.kotlinscriptcompiler.codegen;

import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeMutableList;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class CodeGenerator {
    private static final String EMPTY_VOID = "()V";
    private final String outputClassName;
    private final String outputFunctionName;

    private final Map<String, FunctionDeclareStmt> functionTable;
    private final ClassWriter classWriter;
    private Map<Variable, VariableEntry> variables;
    private int nextIndex;
    private MethodVisitor methodVisitor;

    public CodeGenerator(final String outputClassName,
                         final String outputFunctionName) {
        this.outputClassName = outputClassName;
        this.outputFunctionName = outputFunctionName;
        classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        variables = null;
        nextIndex = 0;

        functionTable = new HashMap<>();
        classWriter.visit(V1_8, // Java 1.8 in my laptop
                ACC_PUBLIC, // public
                outputClassName, // class name
                null, // signature (null means not generic)
                "java/lang/Object", // superclass
                new String[0]); // interfaces (none)

        // ---BEGIN CONSTRUCTOR DEFINITION---
        final MethodVisitor constructor =
                classWriter.visitMethod(ACC_PUBLIC, // access modifier
                        "<init>", // method name (constructor)
                        EMPTY_VOID, // descriptor
                        null, // signature (null means not generic)
                        null); // exceptions
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0); // load "this"
        constructor.visitMethodInsn(INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                EMPTY_VOID,
                false); // super()
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        // ---END CONSTRUCTOR DEFINITION---

        // ---BEGIN MAIN DEFINITION---
        final MethodVisitor main =
                classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC,
                        "main",
                        "([Ljava/lang/String;)V",
                        null,
                        null);
        main.visitCode();
        main.visitMethodInsn(INVOKESTATIC,
                outputClassName,
                outputFunctionName,
                EMPTY_VOID,
                false);
        main.visitInsn(RETURN);
        main.visitMaxs(0, 0);
        // ---END MAIN DEFINITION---

        methodVisitor = null;
    } // CodeGenerator

    private void functionStart(final FunctionDeclareStmt function) throws CodeGeneratorException {
        functionStart(function,
                Descriptor.toDescriptorString(function));
    } // functionStart

    private void functionStart(final FunctionDeclareStmt function,
                               final String descriptor)
            throws CodeGeneratorException {
        assert(variables == null);
        assert(nextIndex == 0);
        assert(methodVisitor == null);

        variables = new HashMap<>();
        if(function.getParameterList() != null) {
            for (Map.Entry<Exp, Type> entry : function.getParameterList().entrySet()) {
                addEntry((Variable) entry.getKey(), entry.getValue());
            }
        }

        methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC,
                function.getFuncName().getName(),
                descriptor,
                null,
                null);
        methodVisitor.visitCode();
    } // functionStart

    private void functionEnd() {
        assert(variables != null);
        assert(methodVisitor != null);

        methodVisitor.visitMaxs(0, 0);
        nextIndex = 0;
        variables = null;
        methodVisitor = null;
    } // functionEnd

    private VariableEntry getEntryFor(final Variable variable) throws CodeGeneratorException {
        final VariableEntry entry = variables.get(variable);
        if (entry != null) {
            return entry;
        } else if(variable instanceof ArrayWithIndexExp) {
            VariableEntry arrayEntry = variables.get(((ArrayWithIndexExp) variable).getVariableExp());
            return new VariableEntry(variable, arrayEntry.type, arrayEntry.index);
        } else {
            // should be caught by typechecker
            throw new CodeGeneratorException("no such variable declared: " + variable);
        }
    } // getEntryFor

    private VariableEntry addEntry(final Variable variable, final Type type) throws CodeGeneratorException {
        if (variables.containsKey(variable)) {
            // should be caught by typechecker
            throw new CodeGeneratorException("Variable already in scope: " + variable);
        } else if(variable instanceof ArrayWithIndexExp) {
            if(variables.containsKey(((ArrayWithIndexExp) variable).getVariableExp())) {
                throw new CodeGeneratorException("Should not add ArrayWithIndexExp into entry.");
            } else {
                throw new CodeGeneratorException("Array is not in scope: " + ((ArrayWithIndexExp) variable).getVariableExp());
            }
        } else {
            final VariableEntry entry = new VariableEntry(variable, type, nextIndex++);
            variables.put(variable, entry);
            return entry;
        }
    } // addEntry

    private void writeIntLiteral(final int value) {
        switch (value) {
            case -1:
                methodVisitor.visitInsn(ICONST_M1);
                break;
            case 0:
                methodVisitor.visitInsn(ICONST_0);
                break;
            case 1:
                methodVisitor.visitInsn(ICONST_1);
                break;
            case 2:
                methodVisitor.visitInsn(ICONST_2);
                break;
            case 3:
                methodVisitor.visitInsn(ICONST_3);
                break;
            case 4:
                methodVisitor.visitInsn(ICONST_4);
                break;
            case 5:
                methodVisitor.visitInsn(ICONST_5);
                break;
            default:
                methodVisitor.visitLdcInsn(value);
        }
    } // writeIntLiteral

    private void writeComparableOp(final ComparableOp op) throws CodeGeneratorException {
        // there is no direct instruction for these, but instead a branching
        // version IF.  Basic idea (with <):
        //   push left (assumed written already)
        //   push right (assumed written  already)
        //   iflt is_less_than
        //   push 0
        //   goto after_less_than
        // is_less_than:
        //   push 1
        // after_less_than:
        final Label conditionTrue = new Label();
        final Label afterCondition = new Label();
        switch (op) {
            case OP_LESS_THAN:
                methodVisitor.visitJumpInsn(IF_ICMPLT, conditionTrue);
                break;
            case OP_EQUAL_EQUAL:
                methodVisitor.visitJumpInsn(IF_ICMPEQ, conditionTrue);
                break;
            case OP_LESS_EQUAL:
                methodVisitor.visitJumpInsn(IF_ICMPLE, conditionTrue);
                break;
            case OP_GREATER_THAN:
                methodVisitor.visitJumpInsn(IF_ICMPGT, conditionTrue);
                break;
            case OP_GREATER_EQUAL:
                methodVisitor.visitJumpInsn(IF_ICMPGE, conditionTrue);
                break;
            case OP_NOT_EQUAL:
                methodVisitor.visitJumpInsn(IF_ICMPNE, conditionTrue);
                break;
            default:
                assert (false);
                throw new CodeGeneratorException("Unrecognized operation: " + op);
        }
        writeIntLiteral(0);
        methodVisitor.visitJumpInsn(GOTO, afterCondition);
        methodVisitor.visitLabel(conditionTrue);
        writeIntLiteral(1);
        methodVisitor.visitLabel(afterCondition);
    } // writeComparableOp

    private void writeComparableExp(ComparableExp exp) throws CodeGeneratorException {
        Exp left = exp.getLeft();
        Exp right = exp.getRight();
        writeExp(left);
        writeExp(right);
        writeComparableOp(exp.getOp());
    }

    private void writeNotExp(NotExp exp) throws CodeGeneratorException {
        Exp e = exp.getValue();
        writeExp(e);
        Label ifTrue = new Label();
        Label endIf = new Label();
        methodVisitor.visitJumpInsn(IFEQ, ifTrue);
        writeIntLiteral(0);
        methodVisitor.visitJumpInsn(GOTO, endIf);
        methodVisitor.visitLabel(ifTrue);
        writeIntLiteral(1);
        methodVisitor.visitLabel(endIf);
    }

    private VariableEntry loadVariable(Variable variable) throws CodeGeneratorException {
        final VariableEntry entry = getEntryFor(variable);
        entry.load(this, methodVisitor);
        return entry;
    }


    private void writeBiLogicalExp(BiLogicalExp exp) throws CodeGeneratorException {
        Exp left = exp.getLeft();
        Exp right = exp.getRight();
        Label ifFalse = new Label();
        Label endIf = new Label();

        switch (exp.getOp()) {
            case OP_AND:
                if(left instanceof BooleanExp) {
                    writeIntLiteral(((BooleanExp) left).getValue() ? 1 : 0);
                } else if(left instanceof ComparableExp) {
                    writeComparableExp((ComparableExp) left);
                } else if(left instanceof BiLogicalExp) {
                    writeBiLogicalExp((BiLogicalExp) left);
                } else if(left instanceof NotExp) {
                    writeNotExp((NotExp) left);
                } else if(left instanceof Variable) {
                    loadVariable((Variable) left);
                } else {
                    throw new CodeGeneratorException("Bilogical expression's left value should be boolean type");
                }

                methodVisitor.visitJumpInsn(IFEQ, ifFalse);

                if(right instanceof BooleanExp) {
                    writeIntLiteral(((BooleanExp) right).getValue() ? 1 : 0);
                } else if(right instanceof ComparableExp) {
                    writeComparableExp((ComparableExp) right);
                } else if(right instanceof BiLogicalExp) {
                    writeBiLogicalExp((BiLogicalExp) right);
                } else if(right instanceof NotExp) {
                    writeNotExp((NotExp) right);
                } else if(right instanceof Variable) {
                    loadVariable((Variable) right);
                }else {
                    throw new CodeGeneratorException("Bilogical expression's right value should be boolean type");
                }
                methodVisitor.visitJumpInsn(IFEQ, ifFalse);
                writeIntLiteral(1);
                methodVisitor.visitJumpInsn(GOTO, endIf);
                methodVisitor.visitLabel(ifFalse);
                writeIntLiteral(0);
                methodVisitor.visitLabel(endIf);
                break;
            case OP_OR:
                if(left instanceof BooleanExp) {
                    writeIntLiteral(((BooleanExp) left).getValue() ? 1 : 0);
                } else if(left instanceof ComparableExp) {
                    writeComparableExp((ComparableExp) left);
                } else if(left instanceof BiLogicalExp) {
                    writeBiLogicalExp((BiLogicalExp) left);
                } else if(left instanceof NotExp) {
                    writeNotExp((NotExp) left);
                } else if(left instanceof Variable) {
                    loadVariable((Variable) left);
                } else {
                    throw new CodeGeneratorException("Bilogical expression's left value should be boolean type");
                }

                methodVisitor.visitJumpInsn(IFNE, ifFalse);

                if(right instanceof BooleanExp) {
                    writeIntLiteral(((BooleanExp) right).getValue() ? 1 : 0);
                } else if(right instanceof ComparableExp) {
                    writeComparableExp((ComparableExp) right);
                } else if(right instanceof BiLogicalExp) {
                    writeBiLogicalExp((BiLogicalExp) right);
                } else if(right instanceof NotExp) {
                    writeNotExp((NotExp) right);
                } else if(right instanceof Variable) {
                    loadVariable((Variable) right);
                } else {
                    throw new CodeGeneratorException("Bilogical expression's right value should be boolean type");
                }
                methodVisitor.visitJumpInsn(IFNE, ifFalse);
                writeIntLiteral(0);
                methodVisitor.visitJumpInsn(GOTO, endIf);
                methodVisitor.visitLabel(ifFalse);
                writeIntLiteral(1);
                methodVisitor.visitLabel(endIf);
                break;
        }
    }

    private void writeSelfOperationExp(SelfOperationExp exp) throws CodeGeneratorException {
        int index = getEntryFor(exp.getVariableExp()).index;
        if(exp.getPreOrder()) {
            // ++i case
            if(exp.getOp() == SelfOp.OP_SELF_INCREASE) {
                methodVisitor.visitIincInsn(index, 1);
            } else {
                methodVisitor.visitIincInsn(index, -1);
            }
        } else {
            // i++ case
            // Did not figure out how to do i++ case
            if(exp.getOp() == SelfOp.OP_SELF_INCREASE) {
                methodVisitor.visitIincInsn(index, 1);
            } else {
                methodVisitor.visitIincInsn(index, -1);
            }
        }

        // load the variable after self operation
        final VariableEntry entry = getEntryFor(exp.getVariableExp());
        if(entry.type == BasicType.TYPE_INT) {
            entry.load(this, methodVisitor);
        } else {
            assert (false);
            throw new CodeGeneratorException("Variable in AdditiveExp should be TYPE_INT.");
        }
    }

    private void writeForStatement(final ForStmt forStmt) throws CodeGeneratorException {
        final Label head = new Label();
        final Label afterFor = new Label();

        Map<Variable, VariableEntry> gammaBefore = newCopy(variables);
        VariableEntry entry;
        if(forStmt.getArrayExp() != null) {
            loadVariable(forStmt.getArrayExp());
            String arrayLength = forStmt.hashCode() + forStmt.getArrayExp().getName() + ".length";
            Type type = typeOf(forStmt.getArrayExp());
            BasicType basicType;
            if(type instanceof TypeArray) {
                basicType = ((TypeArray) type).getBasicType();
            } else if(type instanceof TypeMutableList) {
                basicType = ((TypeMutableList) type).getBasicType();
            } else {
                throw new CodeGeneratorException("For in variable should be array or list");
            }
            entry = addEntry(new VariableExp(arrayLength), BasicType.TYPE_INT);
            methodVisitor.visitInsn(ARRAYLENGTH);
            entry.store(this, methodVisitor);
            String index = forStmt.hashCode() + forStmt.getArrayExp().getName() + ".index";
            writeIntLiteral(0);
            entry = addEntry(new VariableExp(index), BasicType.TYPE_INT);
            entry.store(this, methodVisitor);
            methodVisitor.visitLabel(head);
            loadVariable(new VariableExp(index));
            loadVariable(new VariableExp(arrayLength));
            methodVisitor.visitJumpInsn(IF_ICMPGE, afterFor);
            loadVariable(forStmt.getArrayExp());
            loadVariable(new VariableExp(index));
            int opcode = 0;
            switch (basicType) {
                case TYPE_INT:
                    opcode = IALOAD;
                    break;
                case TYPE_BOOLEAN:
                    opcode = BALOAD;
                    break;
                case TYPE_STRING: case TYPE_ANY:
                    opcode = AALOAD;
                    break;
                case TYPE_UNIT:
                    throw new CodeGeneratorException("Void type only from return in function");
            }
            methodVisitor.visitInsn(opcode);
            entry = addEntry(forStmt.getIteratorExp(), basicType);
            entry.store(this, methodVisitor);

            writeBlockStmtInLoop(forStmt.getBlockStmt(), head, afterFor);
            entry = getEntryFor(new VariableExp(index));
            methodVisitor.visitIincInsn(entry.index, 1);
            methodVisitor.visitJumpInsn(GOTO, head);
            methodVisitor.visitLabel(afterFor);
        } else {    // for in range case
            RangeExp rangeExp = forStmt.getRangeExp();
            writeExp(rangeExp.getStart());
            entry = addEntry(forStmt.getIteratorExp(), BasicType.TYPE_INT);
            entry.store(this, methodVisitor);
            String end = forStmt.hashCode() + rangeExp.getEnd().toString();
            writeExp(rangeExp.getEnd());
            entry = addEntry(new VariableExp(end), BasicType.TYPE_INT);
            entry.store(this, methodVisitor);

            String step = null;
            if(forStmt.getStepExp() != null) {
                step = forStmt.hashCode() + forStmt.getStepExp().toString();
                writeExp(forStmt.getStepExp());
                entry = addEntry(new VariableExp(step), BasicType.TYPE_INT);
                entry.store(this, methodVisitor);
            }
            methodVisitor.visitLabel(head);
            loadVariable(forStmt.getIteratorExp());
            loadVariable(new VariableExp(end));
            methodVisitor.visitJumpInsn(IF_ICMPGE, afterFor);
            writeBlockStmtInLoop(forStmt.getBlockStmt(), head, afterFor);
            loadVariable(forStmt.getIteratorExp());
            if(forStmt.getStepExp() != null) {
                loadVariable(new VariableExp(step));
                methodVisitor.visitInsn(IADD);
                entry = getEntryFor(forStmt.getIteratorExp());
                entry.store(this, methodVisitor);
            } else {
                writeIntLiteral(1);
                methodVisitor.visitInsn(IADD);
                entry = getEntryFor(forStmt.getIteratorExp());
                entry.store(this, methodVisitor);
            }
            methodVisitor.visitJumpInsn(GOTO, head);
            methodVisitor.visitLabel(afterFor);
        }

        // After for loop
        variables = gammaBefore;
    }

    private void writeBlockStmtInLoop(BlockStmt blockStmt, Label head, Label afterLoop) throws CodeGeneratorException {
        for(Stmt s : blockStmt.getStmtList()) {
            if(s instanceof ControlLoopStmt) {
                if(s == ControlLoopStmt.STMT_BREAK) {
                    methodVisitor.visitJumpInsn(GOTO, afterLoop);
                } else {
                    methodVisitor.visitJumpInsn(GOTO, head);
                }
            } else {
                writeStatement(s);
            }
        }
    }

    private void writeBlockStmt(BlockStmt blockStmt) throws CodeGeneratorException {
        if(blockStmt != null && blockStmt.getStmtList() != null) {
            // Copy gamma before block
            Map<Variable, VariableEntry> gammaBefore = newCopy(variables);
            writeStatements(blockStmt.getStmtList());
            // restore gamma after block
            variables = gammaBefore;
        }
    }

    private Map<Variable, VariableEntry> newCopy(final Map<Variable, VariableEntry> table) {
        return new HashMap<>(table);
    }

    private void writeIfStatement(final IfStmt ifStmt) throws CodeGeneratorException {
        // if false, jump to the else branch.  If true, fall through to true branch.
        // true branch needs to jump after the false.  Looks like this:
        //
        //   condition_expression
        //   if !condition, jump to false
        //   true stuff
        //   goto after_false
        // false:
        //   false stuff
        // after_false:

        // condition is a boolean, which is represented with an integer which is either
        // 0 or 1.  IFEQ jumps if the value on top of the operand stack is 0, so this naturally
        // ends up giving us the if !condition (as odd as it looks)
        final Label falseLabel = new Label();
        final Label afterFalseLabel = new Label();
        writeExp(ifStmt.getCondition());
        methodVisitor.visitJumpInsn(IFEQ, falseLabel);
        writeBlockStmt(ifStmt.getTrueBranch());
        methodVisitor.visitJumpInsn(GOTO, afterFalseLabel);
        methodVisitor.visitLabel(falseLabel);
        writeBlockStmt(ifStmt.getFalseBranch());
        methodVisitor.visitLabel(afterFalseLabel);
    } // writeIfStatement

    private void writeWhileStatement(final WhileStmt whileStmt) throws CodeGeneratorException {
        // head:
        //   condition_expression
        //   if !condition, jump to after_while
        //   body
        //   goto head
        // after_while
        Map<Variable, VariableEntry> gammaBefore = newCopy(variables);
        final Label head = new Label();
        final Label afterWhile = new Label();
        methodVisitor.visitLabel(head);
        writeExp(whileStmt.getCondition());
        methodVisitor.visitJumpInsn(IFEQ, afterWhile);
        writeBlockStmtInLoop(whileStmt.getBlockStmt(), head, afterWhile);
        methodVisitor.visitJumpInsn(GOTO, head);
        methodVisitor.visitLabel(afterWhile);
        variables = gammaBefore;
    } // whileWhileStatement

    private void writeStatements(final List<Stmt> stmts) throws CodeGeneratorException {
        for (final Stmt statement : stmts) {
            writeStatement(statement);
        }
    } // writeStatements

    private void writeStatement(final Stmt stmt) throws CodeGeneratorException {
        if (stmt instanceof VariableDeclareStmt) {
            // Do nothing here until initialized in AssignStmt
        } else if (stmt instanceof AssignStmt) {
            final AssignStmt asAssign = (AssignStmt)stmt;
            Type type = typeOf(asAssign.getExpression());
            final VariableEntry entry;
            if(((AssignStmt) stmt).isNew()) {
                writeExp(asAssign.getExpression());
                if(asAssign.getExpression() instanceof ArrayWithIndexExp) {
                    entry = addEntry(asAssign.getVariable(), ((TypeArray) type).getBasicType());
                } else {
                    entry = addEntry(asAssign.getVariable(), type);
                }
            } else {
                entry = getEntryFor(((AssignStmt) stmt).getVariable());
                if(entry.variable instanceof ArrayWithIndexExp) {
                    methodVisitor.visitVarInsn(ALOAD, entry.index);
                    writeExp(((ArrayWithIndexExp) entry.variable).getIndexExp());
                    writeExp(asAssign.getExpression());
                }
            }
            entry.store(this, methodVisitor);
        } else if(stmt instanceof CompoundAssignStmt) {
            // support Int += first, then think about string +
            final CompoundAssignStmt asAssign = (CompoundAssignStmt)stmt;
            final VariableEntry entry = getEntryFor(asAssign.getVariable());
            boolean isStringAppend = false;
            if(asAssign.getVariable() instanceof ArrayWithIndexExp) {
                methodVisitor.visitVarInsn(ALOAD, entry.index);
                writeExp(((ArrayWithIndexExp) entry.variable).getIndexExp());
            }
            Type type = typeOf((Exp)asAssign.getVariable());
            if(type == BasicType.TYPE_STRING) {
                isStringAppend = true;
            } else if(type instanceof TypeArray && asAssign.getVariable() instanceof ArrayWithIndexExp && ((TypeArray) type).getBasicType() == BasicType.TYPE_STRING) {
                isStringAppend = true;
            } else {
                entry.load(this, methodVisitor);
            }

            switch (((CompoundAssignStmt) stmt).getOp()) {
                case EXP_PLUS_EQUAL:
                    if(isStringAppend) {
                        writeExp(new AdditiveExp((Exp)asAssign.getVariable(), asAssign.getExpression(), AdditiveOp.EXP_PLUS));
                    } else {
                        writeExp(asAssign.getExpression());
                        methodVisitor.visitInsn(IADD);
                    }
                    break;
                case EXP_MINUS_EQUAL:
                    writeExp(asAssign.getExpression());
                    methodVisitor.visitInsn(ISUB);
                    break;
                case EXP_MULTIPLY_EQUAL:
                    writeExp(asAssign.getExpression());
                    methodVisitor.visitInsn(IMUL);
                    break;
                case EXP_DIVIDE_EQUAL:
                    writeExp(asAssign.getExpression());
                    methodVisitor.visitInsn(IDIV);
                    break;
            }

            entry.store(this, methodVisitor);
        }
        else if (stmt instanceof PrintStmt || stmt instanceof PrintlnStmt) {
            if(stmt instanceof PrintStmt) {
                writePrint( ((PrintStmt)stmt).getValue(), false);
            } else {
                writePrint(((PrintlnStmt)stmt).getValue(), true);
            }
        } else if (stmt instanceof IfStmt) {
            writeIfStatement((IfStmt)stmt);
        } else if (stmt instanceof WhileStmt) {
            writeWhileStatement((WhileStmt)stmt);
        } else if(stmt instanceof ReturnStmt) {
            writeExp(((ReturnStmt) stmt).getReturnExp());
        } else if(stmt instanceof BlockStmt) {
            writeBlockStmt((BlockStmt) stmt);
        } else if(stmt instanceof FunctionInstanceStmt) {
            writeFunctionInstance(((FunctionInstanceStmt) stmt).getFunctionInstanceExp());
        } else if(stmt instanceof ForStmt) {
            writeForStatement((ForStmt) stmt);
        } else if(stmt instanceof SelfOperationStmt) {
            writeExp(((SelfOperationStmt) stmt).getSelfOperationExp());
        }
        else {
//            assert(false);
            throw new CodeGeneratorException("Unrecognized statement so far: " + stmt);
        }
    } // writeStatement

    private void writeReturnFor(final Type type) {
        if(type == BasicType.TYPE_INT ||
                type == BasicType.TYPE_BOOLEAN) {
            methodVisitor.visitInsn(IRETURN);
        } else if(type == BasicType.TYPE_UNIT) {
            methodVisitor.visitInsn(RETURN);
        } else {
            methodVisitor.visitInsn(ARETURN);
        }
    } // writeReturnFor

    private void writeFunction(final FunctionDeclareStmt function) throws CodeGeneratorException {
        functionStart(function);
        if(function.getBlockStmt() != null) {
            writeStatements(function.getBlockStmt().getStmtList());
        }
        writeReturnFor(function.getReturnType());
        functionEnd();
    } // writeFunction

    private void writeEntryPoint(final Program program) throws CodeGeneratorException {
        functionStart(new FunctionDeclareStmt(new VariableExp(outputFunctionName), BasicType.TYPE_UNIT, null, null),
                EMPTY_VOID);
        for(Stmt s : program.getStmtList()) {
            if(!(s instanceof FunctionDeclareStmt)) {
                writeStatement(s);
            }
        }
        methodVisitor.visitInsn(RETURN);
        functionEnd();
    } // writeEntryPoint

    private Type writeFunctionInstance(final FunctionInstanceExp call) throws CodeGeneratorException {
        final FunctionDeclareStmt function = functionTable.get(call.getFuncName().getName());
        if (function == null) {
            throw new CodeGeneratorException("Call to nonexistent function.");
        }

        for (final Exp param : call.getParameterList()) {
            writeExp(param);
        }
        methodVisitor.visitMethodInsn(INVOKESTATIC,
                outputClassName,
                call.getFuncName().getName(),
                Descriptor.toDescriptorString(function),
                false);
        return function.getReturnType();
    } // writeFunctionInstance

    private Type typeOfVariable(Variable variable) throws CodeGeneratorException {
        return getEntryFor(variable).type;
    }

    private Type typeOfFunctionInstance(FunctionInstanceExp exp) throws CodeGeneratorException {
        final FunctionDeclareStmt function = functionTable.get(exp.getFuncName().getName());
        if (function == null) {
            throw new CodeGeneratorException("Call to nonexistent function.");
        }
        return function.getReturnType();
    }

    private Type typeOf(Exp temp) throws CodeGeneratorException {
        Type type;
        if(temp instanceof IntExp || temp instanceof SelfOperationExp) {
            type = BasicType.TYPE_INT;
        } else if(temp instanceof BinaryIntExp) {
            if(temp instanceof AdditiveExp) {
                Type left = typeOf(((BinaryIntExp) temp).getLeft());
                if(((AdditiveExp) temp).getOp() == AdditiveOp.EXP_PLUS) {
                    if(left == BasicType.TYPE_STRING) {
                        type = BasicType.TYPE_STRING;
                    } else {
                        type = BasicType.TYPE_INT;
                    }
                } else {
                    type = BasicType.TYPE_INT;
                }
            } else {
                type = BasicType.TYPE_INT;
            }
        } else if(temp instanceof StringExp) {
            type = BasicType.TYPE_STRING;
        } else if(temp instanceof BooleanExp || temp instanceof ComparableExp ||
                temp instanceof NotExp || temp instanceof BiLogicalExp) {
            type = BasicType.TYPE_BOOLEAN;
        } else if(temp instanceof VariableExp || temp instanceof ArrayWithIndexExp) {
            type = typeOfVariable((Variable) temp);
        } else if(temp instanceof FunctionInstanceExp) {
            type = typeOfFunctionInstance((FunctionInstanceExp) temp);
        } else if(temp instanceof ArrayOfExp) {
            if(((ArrayOfExp) temp).getExpList().size() > 0) {
                Exp t = ((ArrayOfExp) temp).getExpList().get(0);
                BasicType basicType = (BasicType) typeOf(t);
                boolean isAny = false;
                for (Exp e : ((ArrayOfExp) temp).getExpList()) {
                    if(basicType != typeOf(e)) {
                        isAny = true;
                        break;
                    }
                }
                if(isAny) {
                    basicType = BasicType.TYPE_ANY;
                }
                type = new TypeArray(basicType);
            } else {
                throw new CodeGeneratorException("arrayOf(exp*) should has at least one parameter");
            }
        } else if(temp instanceof ArrayExp) {
            Type genericType = typeOf(((ArrayExp) temp).getLambdaExp().getReturnExp());
            type = new TypeArray((BasicType) genericType);
        }
        else {
            throw new CodeGeneratorException("Unrecognized expression type");
        }
        return type;
    }

    private void writeStringExp(StringExp s) throws CodeGeneratorException {
        if(s.getStrWithoutInterpolation() == null) {
            throw new CodeGeneratorException("Null is StringExp!");
        } else if(s.getInterpolationExp() == null || s.getInterpolationExp().size() == 0) {
            methodVisitor.visitLdcInsn(s.getStrWithoutInterpolation());
        } else {
            Integer[] indexs = new Integer[s.getInterpolationExp().size()];
            Exp[] exps = new Exp[s.getInterpolationExp().size()];
            s.getInterpolationExp().keySet().toArray(indexs);
            s.getInterpolationExp().values().toArray(exps);
            Type type;
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            for (int i = 0; i < indexs.length; i++) {
                methodVisitor.visitLdcInsn(s.getStrWithoutInterpolation());
                if (i == 0) {
                    writeIntLiteral(0);
                } else {
                    writeIntLiteral(indexs[i - 1]);
                }
                writeIntLiteral(indexs[i]);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;", false);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                writeExp(exps[i]);
                type = typeOf(exps[i]);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + Descriptor.toDescriptorString(type) + ")Ljava/lang/StringBuilder;", false);
            }
            methodVisitor.visitLdcInsn(s.getStrWithoutInterpolation());
            writeIntLiteral(indexs[indexs.length - 1]);
            writeIntLiteral(s.getStrWithoutInterpolation().length());
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        }
    }

    // assume array is already created.
    private void writeValueToInitArrayExp(int opcode, VariableExp iteratorExp, Exp initExp, String arrayPrefix, String sizeVar) throws CodeGeneratorException {
        final Label head = new Label();
        final Label afterFor = new Label();
        VariableEntry entry;
        writeIntLiteral(0);
        entry = addEntry(new VariableExp(arrayPrefix + ".index"), BasicType.TYPE_INT);
        entry.store(this, methodVisitor);
        methodVisitor.visitLabel(head);
        loadVariable(new VariableExp(arrayPrefix + ".index"));
        loadVariable(new VariableExp(sizeVar));
        methodVisitor.visitJumpInsn(IF_ICMPGE, afterFor);
        methodVisitor.visitInsn(DUP);
        loadVariable(new VariableExp(arrayPrefix + ".index"));
        writeExp(initExp);
        methodVisitor.visitInsn(opcode);
        entry = getEntryFor(new VariableExp(arrayPrefix + ".index"));
        methodVisitor.visitIincInsn(entry.index, 1);
        if(iteratorExp != null) {
            entry = getEntryFor(iteratorExp);
            methodVisitor.visitIincInsn(entry.index, 1);
        }
        methodVisitor.visitJumpInsn(GOTO, head);
        methodVisitor.visitLabel(afterFor);
    }

    private void writeArrayExp(ArrayExp arrayExp) throws CodeGeneratorException {
        Map<Variable, VariableEntry> gammaBefore = newCopy(variables);
        VariableEntry entry;
        LinkedHashMap<VariableExp, Type> parameters = arrayExp.getLambdaExp().getParameterList();
        VariableExp[] variableExps = new VariableExp[1];
        if(parameters != null && parameters.size() == 1) {
            variableExps = new VariableExp[1];
            parameters.keySet().toArray(variableExps);
            entry = addEntry(variableExps[0], BasicType.TYPE_INT);
            writeIntLiteral(0);
            entry.store(this, methodVisitor);
        }

        Exp size = arrayExp.getSize();
        String arrayPrefix = arrayExp.hashCode() + "";
        String sizeVar = arrayExp.hashCode() + ".size";
        Exp returnExp = arrayExp.getLambdaExp().getReturnExp();
        writeExp(size);
        entry = addEntry(new VariableExp(sizeVar), BasicType.TYPE_INT);
        entry.store(this, methodVisitor);
        Type type = typeOf(returnExp);
        loadVariable(new VariableExp(sizeVar));

        switch ((BasicType) type) {
            case TYPE_INT:
                methodVisitor.visitIntInsn(NEWARRAY, T_INT);
                writeValueToInitArrayExp(IASTORE, variableExps[0], returnExp, arrayPrefix, sizeVar);
                break;
            case TYPE_STRING:
                methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
                writeValueToInitArrayExp(AASTORE, variableExps[0], returnExp, arrayPrefix, sizeVar);
                break;
            case TYPE_BOOLEAN:
                methodVisitor.visitIntInsn(NEWARRAY, T_BOOLEAN);
                writeValueToInitArrayExp(BASTORE, variableExps[0], returnExp, arrayPrefix, sizeVar);
                break;
            case TYPE_ANY:
                methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                writeValueToInitArrayExp(AASTORE, variableExps[0], returnExp, arrayPrefix, sizeVar);
                break;
            case TYPE_UNIT:
                throw new CodeGeneratorException("Void type only from return in function");
        }

        variables = gammaBefore;
    }

    private void writeValueToArray(BasicType type, List<Exp> exps) throws CodeGeneratorException {
        int size = exps.size();
        writeIntLiteral(size);
        switch (type) {
            case TYPE_INT:
                methodVisitor.visitIntInsn(NEWARRAY, T_INT);
                for(int i = 0; i < size; i++) {
                    methodVisitor.visitInsn(DUP);
                    writeIntLiteral(i);
                    writeExp(exps.get(i));
                    methodVisitor.visitInsn(IASTORE);
                }
                break;
            case TYPE_BOOLEAN:
                methodVisitor.visitIntInsn(NEWARRAY, T_BOOLEAN);
                for(int i = 0; i < size; i++) {
                    methodVisitor.visitInsn(DUP);
                    writeIntLiteral(i);
                    writeExp(exps.get(i));
                    methodVisitor.visitInsn(BASTORE);
                }
                break;
            case TYPE_STRING:
                methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
                for(int i = 0; i < size; i++) {
                    methodVisitor.visitInsn(DUP);
                    writeIntLiteral(i);
                    writeExp(exps.get(i));
                    methodVisitor.visitInsn(AASTORE);
                }
                break;
            case TYPE_ANY:
                methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                for(int i = 0; i < size; i++) {
                    methodVisitor.visitInsn(DUP);
                    writeIntLiteral(i);
                    writeExp(exps.get(i));
                    Type t = typeOf(exps.get(i));
                    if(t == BasicType.TYPE_INT) {
                        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    } else if(t == BasicType.TYPE_BOOLEAN) {
                        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                    }
                    methodVisitor.visitInsn(AASTORE);
                }
                break;
            case TYPE_UNIT:
                throw new CodeGeneratorException("Void type only use for return from function!");
        }
    }

    public Type writeExp(Exp exp) throws CodeGeneratorException {
        if(exp instanceof IntExp) {
            writeIntLiteral(((IntExp) exp).getValue());
            return BasicType.TYPE_INT;
        } else if(exp instanceof BooleanExp) {
            writeIntLiteral(((BooleanExp) exp).getValue() ? 1 : 0);
            return BasicType.TYPE_BOOLEAN;
        } else if(exp instanceof StringExp) {
            writeStringExp((StringExp) exp);
            return BasicType.TYPE_STRING;
        } else if(exp instanceof VariableExp) {
            return loadVariable((VariableExp)exp).type;
        } else if(exp instanceof ArrayWithIndexExp) {
            return loadVariable((ArrayWithIndexExp)exp).type;
        } else if(exp instanceof SelfOperationExp) {
            writeSelfOperationExp((SelfOperationExp) exp);
            return BasicType.TYPE_INT;
        } else if(exp instanceof BinaryIntExp) {
            if(writeAdditiveExpOrMultplicativeExp((BinaryIntExp) exp)) {
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            }
            return typeOf(exp);
        } else if(exp instanceof ComparableExp) {
            writeComparableExp((ComparableExp) exp);
            return BasicType.TYPE_BOOLEAN;
        } else if(exp instanceof NotExp) {
            writeNotExp((NotExp) exp);
            return BasicType.TYPE_BOOLEAN;
        } else if(exp instanceof BiLogicalExp) {
            writeBiLogicalExp((BiLogicalExp) exp);
            return BasicType.TYPE_BOOLEAN;
        } else if(exp instanceof FunctionInstanceExp) {
            return writeFunctionInstance((FunctionInstanceExp) exp);
        } else if(exp instanceof ArrayOfExp) {
            if(((ArrayOfExp) exp).getExpList().size() > 0) {
                Exp temp = ((ArrayOfExp) exp).getExpList().get(0);
                BasicType type = (BasicType) typeOf(temp);
                boolean isAny = false;
                for (Exp e : ((ArrayOfExp) exp).getExpList()) {
                    if(type != typeOf(e)) {
                        isAny = true;
                        break;
                    }
                }
                if(isAny) {
                    type = BasicType.TYPE_ANY;
                }

                writeValueToArray(type, ((ArrayOfExp) exp).getExpList());
                return new TypeArray(type);
            } else {
                throw new CodeGeneratorException("arrayOf(exp*) should has at least one parameter");
            }
        } else if(exp instanceof ArrayExp) {
            Type returnGenericType = typeOf(((ArrayExp) exp).getLambdaExp().getReturnExp());
            writeArrayExp((ArrayExp) exp);
            return new TypeArray((BasicType) returnGenericType);
        }
        else {
            throw new CodeGeneratorException("Unsupported expression so far!");
        }
    }

    // return whether is String append operation or not.
    private boolean writeAdditiveExpOrMultplicativeExp(BinaryIntExp exp) throws CodeGeneratorException {
        Exp left = exp.getLeft();
        Exp right = exp.getRight();
        boolean isStringAppend = false;
        if(left instanceof IntExp) {
            writeIntLiteral(((IntExp) left).getValue());
        } else if(left instanceof VariableExp) {
            final VariableEntry entry = getEntryFor((VariableExp) left);
            if(entry.type == BasicType.TYPE_INT) {
                entry.load(this, methodVisitor);
            } else if(entry.type == BasicType.TYPE_STRING) {
                isStringAppend = true;
                methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                entry.load(this, methodVisitor);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            }
            else {
                assert (false);
                throw new CodeGeneratorException("Variable in AdditiveExp should be TYPE_INT.");
            }
        } else if(left instanceof StringExp) {
            isStringAppend = true;
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            writeStringExp((StringExp) left);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        } else if(left instanceof BinaryIntExp) {
            isStringAppend = writeAdditiveExpOrMultplicativeExp((BinaryIntExp)left);
        } else if(left instanceof SelfOperationExp) {
            writeSelfOperationExp((SelfOperationExp) left);
        } else if(left instanceof ArrayWithIndexExp) {
            final VariableEntry entry = getEntryFor((ArrayWithIndexExp) left);
            BasicType basicType = ((TypeArray) entry.type).getBasicType();
            if(basicType == BasicType.TYPE_STRING) {
                isStringAppend = true;
                methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                entry.load(this, methodVisitor);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            } else {
                entry.load(this, methodVisitor);
            }
        } else {
            assert (false);
            throw new CodeGeneratorException("IllTypedException should be handled in typechecker.");
        }

        // Do it again to load right Exp on stack
        if(right instanceof IntExp) {
            if(isStringAppend) {
                writeIntLiteral(((IntExp) right).getValue());
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
            } else {
                writeIntLiteral(((IntExp) right).getValue());
            }
        } else if(right instanceof VariableExp) {
            final VariableEntry entry = getEntryFor((VariableExp) right);
            if(entry.type == BasicType.TYPE_INT) {
                if(isStringAppend) {
                    entry.load(this, methodVisitor);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
                } else {
                    entry.load(this, methodVisitor);
                }
            } else if(entry.type == BasicType.TYPE_STRING) {
                if(!isStringAppend) {
                    throw new CodeGeneratorException("String Concatenation left value must be a string");
                } else {
                    entry.load(this, methodVisitor);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                }
            } else if(entry.type == BasicType.TYPE_BOOLEAN) {
                if(isStringAppend) {
                    entry.load(this, methodVisitor);
                    methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;", false);
                } else {
                    throw new CodeGeneratorException("Variable in AdditiveExp should be TYPE_INT or TYPE_STRING.");
                }
            }
            else {
                assert (false);
                throw new CodeGeneratorException("Variable in AdditiveExp should be TYPE_INT or TYPE_STRING.");
            }
        } else if(right instanceof BinaryIntExp) {
            writeAdditiveExpOrMultplicativeExp((BinaryIntExp)right);
        } else if(right instanceof SelfOperationExp) {
            if(isStringAppend) {
                writeSelfOperationExp((SelfOperationExp) right);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
            } else {
                writeSelfOperationExp((SelfOperationExp) right);
            }
        } else if(right instanceof BooleanExp) {
            if(isStringAppend) {
                writeIntLiteral(((BooleanExp) right).getValue() ? 1 : 0);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;", false);
            } else {
                throw new CodeGeneratorException("Variable in AdditiveExp should be TYPE_INT or TYPE_STRING.");
            }
        }
        else if(right instanceof ArrayWithIndexExp) {
            final VariableEntry entry = getEntryFor((ArrayWithIndexExp) right);
            if(isStringAppend) {
                BasicType basicType = ((TypeArray) entry.type).getBasicType();
                entry.load(this, methodVisitor);
                switch (basicType) {
                    case TYPE_INT:
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
                        break;
                    case TYPE_STRING:
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                        break;
                    case TYPE_BOOLEAN:
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;", false);
                        break;
                    case TYPE_ANY:
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
                        break;
                    case TYPE_UNIT:
                        throw new CodeGeneratorException("Void type should only return from function declaration");
                }
            } else {
                entry.load(this, methodVisitor);
            }
        } else if(right instanceof StringExp) {
            if(isStringAppend) {
                writeStringExp((StringExp) right);
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            } else {
                throw new CodeGeneratorException("String can be only in string + string operation.");
            }
        }
        else {
            assert (false);
            throw new CodeGeneratorException("IllTypedException should be handled in typechecker.");
        }

        if(exp instanceof AdditiveExp) {
            AdditiveOp op = ((AdditiveExp) exp).getOp();
            if(op == AdditiveOp.EXP_PLUS) {
                if(!isStringAppend) {
                    methodVisitor.visitInsn(IADD);
                }
            } else {
                if(isStringAppend) {
                    throw new CodeGeneratorException("String concatenation is only allow in +");
                }
                methodVisitor.visitInsn(ISUB);
            }
        } else if(exp instanceof MultiplicativeExp) {
            if(isStringAppend) {
                throw new CodeGeneratorException("String concatenation is only allow in +");
            }
            MultiplicativeOp op = ((MultiplicativeExp) exp).getOp();
            if(op == MultiplicativeOp.OP_MULTIPLY) {
                methodVisitor.visitInsn(IMUL);
            } else if(op == MultiplicativeOp.OP_DIVIDE) {
                methodVisitor.visitInsn(IDIV);
            } else if(op == MultiplicativeOp.OP_MOD) {
                methodVisitor.visitInsn(IREM);
            }
        }
        return isStringAppend;
    }

    private void writePrint(final Exp exp, boolean isNewLine) throws CodeGeneratorException {
        Type type = typeOf(exp);
        final String descriptor;
        if (type == BasicType.TYPE_INT) {
            descriptor = "(I)V";
        } else if (type == BasicType.TYPE_BOOLEAN) {
            descriptor = "(Z)V";
        } else if(type == BasicType.TYPE_STRING) {
            descriptor = "(Ljava/lang/String;)V";
        } else if(type instanceof TypeArray || type instanceof TypeMutableList || type == BasicType.TYPE_ANY) {
            if(exp instanceof ArrayWithIndexExp) {
                BasicType basicType = ((TypeArray)type).getBasicType();
                if (basicType == BasicType.TYPE_INT) {
                    descriptor = "(I)V";
                } else if (basicType == BasicType.TYPE_BOOLEAN) {
                    descriptor = "(Z)V";
                } else if(basicType == BasicType.TYPE_STRING) {
                    descriptor = "(Ljava/lang/String;)V";
                } else {
                    throw new CodeGeneratorException("Unrecognized type; " + type);
                }
            } else {
                descriptor = "(Ljava/lang/Object;)V";
            }
        } else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized type; " + type);
        }

        methodVisitor.visitFieldInsn(GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
        writeExp(exp);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                "java/io/PrintStream",
                isNewLine ? "println" : "print",
                descriptor,
                false);
    } // writePrint

    private void loadFunctionTable(final Program program) throws CodeGeneratorException {
        for (final Stmt s : program.getStmtList()) {
            if(s instanceof FunctionDeclareStmt) {
                if (functionTable.containsKey(((FunctionDeclareStmt)s).getFuncName().getName())) {
                    throw new CodeGeneratorException("Duplicate function name: " + ((FunctionDeclareStmt)s).getFuncName().getName());
                }
                functionTable.put(((FunctionDeclareStmt)s).getFuncName().getName(), (FunctionDeclareStmt)s);
            }
        }
    } // loadFunctionTable

    public void writeProgram(final Program program) throws CodeGeneratorException, IOException {
        loadFunctionTable(program);
        for (final FunctionDeclareStmt function : functionTable.values()) {
            writeFunction(function);
        }
        writeEntryPoint(program);
        classWriter.visitEnd();

        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(new File(outputClassName + ".class")));
        output.write(classWriter.toByteArray());
        output.close();
    } // writeProgram

}
