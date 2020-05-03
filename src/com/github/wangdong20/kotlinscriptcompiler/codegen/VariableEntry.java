package com.github.wangdong20.kotlinscriptcompiler.codegen;

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

    public void load(final MethodVisitor visitor) throws CodeGeneratorException {
        // both are treated as integers at the bytecode level
        if (type == BasicType.TYPE_INT ||
                type == BasicType.TYPE_BOOLEAN) {
            visitor.visitVarInsn(ILOAD, index);
        } else if (type instanceof TypeArray || type == BasicType.TYPE_STRING) {
            visitor.visitVarInsn(ALOAD, index);
        } else {
            throw new CodeGeneratorException("Unsupported load type: " + type);
        }
    } // load

    public void store(final MethodVisitor visitor) throws CodeGeneratorException {
        // both are treated as integers at the bytecode level
        if (type == BasicType.TYPE_INT ||
                type == BasicType.TYPE_BOOLEAN) {
            visitor.visitVarInsn(ISTORE, index);
        } else if (type instanceof TypeArray || type == BasicType.TYPE_STRING) {
            visitor.visitVarInsn(ASTORE, index);
        } else {
            throw new CodeGeneratorException("Unsupported store type: " + type);
        }
    } // store
}
