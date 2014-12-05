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

import com.mobile.privacy.policy.parser.ClassResolver;

public class DataInterpreter extends Interpreter<DataValue> implements Opcodes {

    private MethodResolver methodResolver;
    private FieldPool fieldPool;
    private int idx;
    
    public DataInterpreter(MethodResolver methodResolver, FieldPool fieldPool) {
        super(ASM5);
        this.methodResolver = methodResolver;
        this.fieldPool = fieldPool;
        idx = 0;
    }
    
    protected DataInterpreter(final int api) {
        super(api);
    }
    
    @Override
    public DataValue newValue(final Type type) {
        DataValue newVal = typeToDataValue(type);
        if(type!= null && newVal != null)
            newVal.argValues.add(idx++);
        return newVal;
    }
    
    public static DataValue typeToDataValue(final Type type) {
        if (type == null) {
            return DataValue.EMPTY_VALUE;
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
            return DataValue.WORD_VALUE;
        case Type.LONG:
        case Type.DOUBLE:
            return DataValue.DOUBLE_VALUE;
        case Type.ARRAY:
        case Type.OBJECT:
            return new DataValue();
        default:
            throw new Error("Internal error");
        }
    }
    
    @Override
    public DataValue newOperation(final AbstractInsnNode insn)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
        case ACONST_NULL:
            return new DataValue();
        case ICONST_M1:
        case ICONST_0:
        case ICONST_1:
        case ICONST_2:
        case ICONST_3:
        case ICONST_4:
        case ICONST_5:
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
            return  DataValue.WORD_VALUE;
        case LCONST_0:
        case LCONST_1:
        case DCONST_0:
        case DCONST_1:
            return  DataValue.DOUBLE_VALUE;
        case BIPUSH:
        case SIPUSH:
            return DataValue.WORD_VALUE;
        case LDC:
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Integer) {
                return DataValue.WORD_VALUE;
            } else if (cst instanceof Float) {
                return DataValue.WORD_VALUE;
            } else if (cst instanceof Long) {
                return DataValue.DOUBLE_VALUE;
            } else if (cst instanceof Double) {
                return DataValue.DOUBLE_VALUE;
            } else if (cst instanceof String) {
                DataValue val = new DataValue();
                val.stringValues.add((String)cst);
                return val;
            } else if (cst instanceof Type) {
                return new DataValue();
            } else if (cst instanceof Handle) {
                return new DataValue();
            } else {
                throw new IllegalArgumentException("Illegal LDC constant "
                        + cst);
            }
        case JSR:
            return null;
        case GETSTATIC:
            FieldInsnNode node = (FieldInsnNode) insn;
            Handle handle = new Handle(Opcodes.H_GETSTATIC, node.owner, node.name, node.desc);
            return fieldPool.getField(handle);
        case NEW:
            return new DataValue();
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
            return DataValue.WORD_VALUE;
        case LNEG:
        case I2L:
        case F2L:
        case D2L:
        case DNEG:
        case I2D:
        case L2D:
        case F2D:
            return DataValue.DOUBLE_VALUE;
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
            return null;
        case PUTSTATIC:
            FieldInsnNode node = (FieldInsnNode) insn;
            Handle handle = new Handle(Opcodes.H_GETSTATIC, node.owner, node.name, node.desc);
            fieldPool.setField(handle, value);
            return null;
        case GETFIELD:
            FieldInsnNode nodeF = (FieldInsnNode) insn;
            Handle handleF = new Handle(Opcodes.H_GETFIELD, nodeF.owner, nodeF.name, nodeF.desc);
            return fieldPool.getField(handleF);
        case NEWARRAY:
            return new DataValue();
        case ANEWARRAY:
            return new DataValue();
        case ARRAYLENGTH:
            return DataValue.WORD_VALUE;
        case ATHROW:
            return null;
        case CHECKCAST:
            return value;
        case INSTANCEOF:
            return DataValue.WORD_VALUE;
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
            return DataValue.WORD_VALUE;
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
            return DataValue.DOUBLE_VALUE;
        case AALOAD:
            return value1;
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
            return DataValue.WORD_VALUE;
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
            return null;
        case PUTFIELD:
            FieldInsnNode node = (FieldInsnNode) insn;
            Handle handle = new Handle(Opcodes.H_GETFIELD, node.owner, node.name, node.desc);
            fieldPool.setField(handle, value2);
            if(value1.isRef() && value2.isRef())
                value1.merge(value2);
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
            return typeToDataValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else if (opcode == INVOKEDYNAMIC) {
            System.out.println("Warning, unsupported operation INVOKEDYNAMIC");
            return typeToDataValue(Type.getReturnType(((InvokeDynamicInsnNode) insn).desc));
        } else {
            MethodInsnNode node = (MethodInsnNode) insn;
            Handle handle;
            switch(insn.getOpcode()) {
            case INVOKEVIRTUAL:
                handle = new Handle(Opcodes.H_INVOKEVIRTUAL,node.owner,node.name,node.desc);
                break;
            case INVOKESPECIAL:
                handle = new Handle(Opcodes.H_INVOKESPECIAL,node.owner,node.name,node.desc);
                break;
            case INVOKESTATIC:
                handle = new Handle(Opcodes.H_INVOKESTATIC,node.owner,node.name,node.desc);
                break;
            case INVOKEINTERFACE:
                handle = new Handle(Opcodes.H_INVOKEINTERFACE,node.owner,node.name,node.desc);
                break;
            default:
                System.out.println("Invalid Instructions");
                handle = null;
            }
            DataValue ret = methodResolver.execute(handle,(List<DataValue>) values);
            return ret;
        }
        
    }
    
    @Override
    public void returnOperation(final AbstractInsnNode insn,
            final DataValue value, final DataValue expected)
            throws AnalyzerException {
    }
    
    @Override
    public DataValue merge(final DataValue v, final DataValue w) {
        if(w == DataValue.EMPTY_VALUE)
            return v;
        if(v == DataValue.EMPTY_VALUE)
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
