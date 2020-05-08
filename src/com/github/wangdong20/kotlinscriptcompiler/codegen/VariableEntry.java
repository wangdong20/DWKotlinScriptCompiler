package com.github.wangdong20.kotlinscriptcompiler.codegen;

import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.ArrayWithIndexExp;
import com.github.wangdong20.kotlinscriptcompiler.parser.expressions.Variable;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.BasicType;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.Type;
import com.github.wangdong20.kotlinscriptcompiler.parser.type.TypeArray;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class VariableEntry {
    public final Variable variable;
    public final Type type;
    public final int index;

    public VariableEntry(Variable variable, Type type, int index) {
        assert(index >= 0);
        this.variable = variable;
        this.type = type;
        this.index = index;
    }

    public void load(CodeGenerator codeGenerator, final MethodVisitor visitor) throws CodeGeneratorException {
        // both are treated as integers at the bytecode level
        if (type == BasicType.TYPE_INT ||
                type == BasicType.TYPE_BOOLEAN) {
            visitor.visitVarInsn(ILOAD, index);
        } else if (type == BasicType.TYPE_STRING) {
            visitor.visitVarInsn(ALOAD, index);
        } else if(type instanceof TypeArray) {
            visitor.visitVarInsn(ALOAD, index);
            if(variable instanceof ArrayWithIndexExp) {
                codeGenerator.writeExp(((ArrayWithIndexExp) variable).getIndexExp());
                int opcode;
                switch (((TypeArray) type).getBasicType()) {
                    case TYPE_INT:
                        opcode = IALOAD;
                        break;
                    case TYPE_BOOLEAN:
                        opcode = BALOAD;
                        break;
                    case TYPE_STRING:
                    case TYPE_ANY:
                        opcode = AALOAD;
                        break;
                    default: throw new CodeGeneratorException("Unsupported type in array: " + type);
                }
                visitor.visitInsn(opcode);
            }
        }
        else {
            throw new CodeGeneratorException("Unsupported load type: " + type);
        }
    } // load

    public void store(CodeGenerator codeGenerator, final MethodVisitor visitor) throws CodeGeneratorException {
        // both are treated as integers at the bytecode level
        if (type == BasicType.TYPE_INT ||
                type == BasicType.TYPE_BOOLEAN) {
            visitor.visitVarInsn(ISTORE, index);
        } else if (type == BasicType.TYPE_STRING) {
            visitor.visitVarInsn(ASTORE, index);
        } else if(type instanceof TypeArray) {
            if(variable instanceof ArrayWithIndexExp) {
                int opcode;
                switch (((TypeArray) type).getBasicType()) {
                    case TYPE_INT:
                        opcode = IASTORE;
                        break;
                    case TYPE_BOOLEAN:
                        opcode = BASTORE;
                        break;
                    case TYPE_STRING:
                    case TYPE_ANY:
                        opcode = AASTORE;
                        break;
                    default: throw new CodeGeneratorException("Unsupported type in array: " + type);
                }
                visitor.visitInsn(opcode);
            } else {
                visitor.visitVarInsn(ASTORE, index);
            }
        } else {
            throw new CodeGeneratorException("Unsupported store type: " + type);
        }
    } // store
}
