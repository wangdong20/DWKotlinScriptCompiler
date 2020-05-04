package com.github.wangdong20.kotlinscriptcompiler.codegen;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.*;
import com.github.wangdong20.kotlinscriptcompiler.parser.statements.FunctionDeclareStmt;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
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
                         final String outputFunctionName) throws CodeGeneratorException {
        this.outputClassName = outputClassName;
        this.outputFunctionName = outputFunctionName;
        classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        variables = null;
        nextIndex = 0;

        functionTable = new HashMap<String, FunctionDeclareStmt>();
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

        variables = new HashMap<Variable, VariableEntry>();
        for(Map.Entry<Exp, Type> entry : function.getParameterList().entrySet()) {
            addEntry((Variable) entry.getKey(), entry.getValue());
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
            return new VariableEntry(variable, new TypeArray((BasicType) arrayEntry.type), arrayEntry.index);
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

    private void writeBiLogicalExp(BiLogicalExp exp) throws CodeGeneratorException {
        Exp left = exp.getLeft();
        Exp right = exp.getRight();
        switch (exp.getOp()) {
            case OP_AND:
                if(left instanceof BooleanExp || left instanceof ComparableExp || left instanceof BiLogicalExp || left instanceof NotExp || left instanceof Variable) {

                } else {
                    throw new CodeGeneratorException("Bilogical expression's left value should be boolean type");
                }
                if(right instanceof BooleanExp || right instanceof ComparableExp || right instanceof BiLogicalExp || right instanceof NotExp || right instanceof Variable) {

                } else {
                    throw new CodeGeneratorException("Bilogical expression's right value should be boolean type");
                }
                break;
            case OP_OR:
        }
    }

    public static void writeExp(Exp exp) throws CodeGeneratorException {
        // TODO
    }

    private void writeAdditiveExp(AdditiveExp exp) throws CodeGeneratorException {
        Exp left = exp.getLeft();
        Exp right = exp.getRight();
        if(left instanceof IntExp) {
            writeIntLiteral(((IntExp) left).getValue());
        } else if(left instanceof VariableExp) {
            final VariableEntry entry = getEntryFor((VariableExp) left);
            if(entry.type == BasicType.TYPE_INT) {
                entry.load(methodVisitor);
            } else {
                assert (false);
                throw new CodeGeneratorException("Variable in AdditiveExp should be TYPE_INT.");
            }
        } else if(left instanceof AdditiveExp) {
            writeAdditiveExp((AdditiveExp) left);
        } else if(left instanceof MultiplicativeExp) {
            writeMultiplicativeExp((MultiplicativeExp) left);
        } else if(left instanceof SelfOperationExp) {
            int index = getEntryFor(((SelfOperationExp) left).getVariableExp()).index;
            if(((SelfOperationExp) left).getPreOrder()) {
                // ++i case
                if(((SelfOperationExp) left).getOp() == SelfOp.OP_SELF_INCREASE) {
                    methodVisitor.visitIincInsn(index, 1);
                } else {
                    methodVisitor.visitIincInsn(index, -1);
                }
            } else {
                // i++ case
                // Did not figure out how to do i++ case
                if(((SelfOperationExp) left).getOp() == SelfOp.OP_SELF_INCREASE) {
                    methodVisitor.visitIincInsn(index, 1);
                } else {
                    methodVisitor.visitIincInsn(index, -1);
                }
            }
        } else if(left instanceof ArrayWithIndexExp) {
            final VariableEntry entry = getEntryFor((ArrayWithIndexExp) left);
            entry.load(methodVisitor);
        } else {
            assert (false);
            throw new CodeGeneratorException("IllTypedException should be handled in typechecker.");
        }

        if(right instanceof IntExp) {
            writeIntLiteral(((IntExp) right).getValue());
        } else if(right instanceof VariableExp) {

        } else if(right instanceof AdditiveExp) {
            writeAdditiveExp((AdditiveExp) right);
        } else if(right instanceof MultiplicativeExp) {
            writeMultiplicativeExp((MultiplicativeExp) right);
        } else if(right instanceof SelfOperationExp) {

        } else if(right instanceof ArrayWithIndexExp) {

        } else {
            assert (false);
            throw new CodeGeneratorException("IllTypedException should be handled in typechecker.");
        }
    }

    private void writeMultiplicativeExp(MultiplicativeExp exp) throws CodeGeneratorException {
        Exp left = exp.getLeft();
        Exp right = exp.getRight();
    }

    public void writePrint(final Variable variable, boolean isNewLine) throws CodeGeneratorException {
        final VariableEntry entry = getEntryFor(variable);
        final String descriptor;
        if (entry.type == BasicType.TYPE_INT) {
            descriptor = "(I)V";
        } else if (entry.type == BasicType.TYPE_BOOLEAN) {
            descriptor = "(Z)V";
        } else if(entry.type == BasicType.TYPE_STRING) {
            descriptor = "(Ljava/lang/String;)V";
        }
        else {
            assert(false);
            throw new CodeGeneratorException("Unrecognized type; " + entry.type);
        }

        methodVisitor.visitFieldInsn(GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
        entry.load(methodVisitor);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                "java/io/PrintStream",
                isNewLine ? "println" : "print",
                descriptor,
                false);
    } // writePrint

}
