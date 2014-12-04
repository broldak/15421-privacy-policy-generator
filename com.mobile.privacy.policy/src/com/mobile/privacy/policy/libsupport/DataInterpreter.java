package com.mobile.privacy.policy.libsupport;

import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

public class DataInterpreter extends Interpreter<DataValue> implements Opcodes {

    public DataInterpreter() {
        super(ASM5);
    }
    
    protected DataInterpreter(final int api) {
        super(api);
    }
    
    @Override
    public DataValue newValue(final Type type) {
        return typeToDataValue(type);
    }
    
    public static DataValue typeToDataValue(final Type type) {
        if (type == null) {
            return NativeValue.EMPTY_VALUE;
        }
        switch (type.getSort()) {
        case Type.VOID:
            return null;
        case Type.BOOLEAN:
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
        case Type.FLOAT:
            return NativeValue.WORD_VALUE;
        case Type.LONG:
        case Type.DOUBLE:
            return NativeValue.DOUBLE_VALUE;
        case Type.ARRAY:
        case Type.OBJECT:
            return new SimpleObjectValue();
        default:
            throw new Error("Internal error");
        }
    }
    
    @Override
    public DataValue newOperation(final AbstractInsnNode insn)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
        case ACONST_NULL:
            return new SimpleObjectValue();
        case ICONST_M1:
            return new NativeValue(1, new Integer(-1));
        case ICONST_0:
            return new NativeValue(1, new Integer(0));
        case ICONST_1:
            return new NativeValue(1, new Integer(1));
        case ICONST_2:
            return new NativeValue(1, new Integer(2));
        case ICONST_3:
            return new NativeValue(1, new Integer(3));
        case ICONST_4:
            return new NativeValue(1, new Integer(4));
        case ICONST_5:
            return new NativeValue(1, new Integer(5));
        case LCONST_0:
            return new NativeValue(2, new Long(0));
        case LCONST_1:
            return new NativeValue(2, new Long(1));
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
            return  NativeValue.WORD_VALUE;
        case DCONST_0:
        case DCONST_1:
            return  NativeValue.DOUBLE_VALUE;
        case BIPUSH:
        case SIPUSH:
            return new NativeValue(1,new Integer(((IntInsnNode) insn).operand));
        case LDC:
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Integer) {
                return new NativeValue(1,cst);
            } else if (cst instanceof Float) {
                return NativeValue.WORD_VALUE;
            } else if (cst instanceof Long) {
                return new NativeValue(2,cst);
            } else if (cst instanceof Double) {
                return NativeValue.DOUBLE_VALUE;
            } else if (cst instanceof String) {
                SimpleObjectValue val = new SimpleObjectValue();
                val.stringValues.add((String)cst);
                return val;
            } else if (cst instanceof Type) {
                return new SimpleObjectValue();
            } else if (cst instanceof Handle) {
                return new SimpleObjectValue();
            } else {
                throw new IllegalArgumentException("Illegal LDC constant "
                        + cst);
            }
        case JSR:
            return null;
        case GETSTATIC:
            FieldInsnNode node = (FieldInsnNode) insn;
            Handle handle = new Handle(Opcodes.H_GETSTATIC, node.owner, node.name, node.desc);
            //todo
            return newValue(Type.getType(((FieldInsnNode) insn).desc));
        case NEW:
            return new SimpleObjectValue();
        default:
            throw new Error("Internal error.");
        }
    }
    
    @Override
    public DataValue copyOperation(final AbstractInsnNode insn,
            final DataValue value) throws AnalyzerException {
        return value;
    }
    
    @Override
    public DataValue unaryOperation(final AbstractInsnNode insn,
            final DataValue value) throws AnalyzerException {
        switch (insn.getOpcode()) {
        case INEG:
        case IINC:
        case L2I:
        case F2I:
        case D2I:
        case I2B:
        case I2C:
        case I2S:
        case FNEG:
        case I2F:
        case L2F:
        case D2F:
            return NativeValue.WORD_VALUE;
        case LNEG:
        case I2L:
        case F2L:
        case D2L:
        case DNEG:
        case I2D:
        case L2D:
        case F2D:
            return NativeValue.DOUBLE_VALUE;
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case TABLESWITCH:
        case LOOKUPSWITCH:
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:
        case PUTSTATIC:
            //TODO put static
            return null;
        case GETFIELD:
            //TODO get static
            return new SimpleObjectValue();
        case NEWARRAY:
            return new SimpleObjectValue();
        case ANEWARRAY:
            return new SimpleObjectValue();
        case ARRAYLENGTH:
            return NativeValue.WORD_VALUE;
        case ATHROW:
            return null;
        case CHECKCAST:
            return value;
        case INSTANCEOF:
            return NativeValue.WORD_VALUE;
        case MONITORENTER:
        case MONITOREXIT:
        case IFNULL:
        case IFNONNULL:
            return null;
        default:
            throw new Error("Internal error.");
        }
    }
    
    @Override
    public DataValue binaryOperation(final AbstractInsnNode insn,
            final DataValue value1, final DataValue value2)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
        case IALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case IADD:
        case ISUB:
        case IMUL:
        case IDIV:
        case IREM:
        case ISHL:
        case ISHR:
        case IUSHR:
        case IAND:
        case IOR:
        case IXOR:
        case FALOAD:
        case FADD:
        case FSUB:
        case FMUL:
        case FDIV:
        case FREM:
            return NativeValue.WORD_VALUE;
        case LALOAD:
        case LADD:
        case LSUB:
        case LMUL:
        case LDIV:
        case LREM:
        case LSHL:
        case LSHR:
        case LUSHR:
        case LAND:
        case LOR:
        case LXOR:
        case DALOAD:
        case DADD:
        case DSUB:
        case DMUL:
        case DDIV:
        case DREM:
            return NativeValue.DOUBLE_VALUE;
        case AALOAD:
            System.out.println("CALLED");
            System.out.println(value1);
            return value1;
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
            return NativeValue.WORD_VALUE;
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case PUTFIELD:
            //TODO do something with this
            return null;
        default:
            throw new Error("Internal error.");
        }
    }
    
    @Override
    public DataValue ternaryOperation(final AbstractInsnNode insn,
            final DataValue value1, final DataValue value2,
            final DataValue value3) throws AnalyzerException {
        switch(insn.getOpcode()){
        case AASTORE:
            value1.merge(value3);
        case IASTORE:
        case LASTORE:
        case FASTORE:
        case DASTORE:
        case BASTORE:
        case CASTORE:
        case SASTORE:
            return null;
        default:
            throw new Error("Internal error.");
        }
    }
    
    @Override
    public DataValue naryOperation(final AbstractInsnNode insn,
            final List<? extends DataValue> values) throws AnalyzerException {
        int opcode = insn.getOpcode();
        if (opcode == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else if (opcode == INVOKEDYNAMIC) {
            return newValue(Type
                    .getReturnType(((InvokeDynamicInsnNode) insn).desc));
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
        }
    }
    
    @Override
    public void returnOperation(final AbstractInsnNode insn,
            final DataValue value, final DataValue expected)
            throws AnalyzerException {
    }
    
    @Override
    public DataValue merge(final DataValue v, final DataValue w) {
        if(w == NativeValue.EMPTY_VALUE)
            return v;
        if(v == NativeValue.EMPTY_VALUE)
            return w;
        
        if(v != null)
            v.merge(w);
        if(w != null)
            w.merge(v);

        if(v != null)
            return v;
        
        return w;
    }

}
