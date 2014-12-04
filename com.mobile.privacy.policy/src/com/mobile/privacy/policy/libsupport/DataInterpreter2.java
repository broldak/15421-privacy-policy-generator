package com.mobile.privacy.policy.libsupport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

public class DataInterpreter2 extends Interpreter<DataValue> implements Opcodes {

    private Stack<DataValue> args = null;
    private Stack<DataValue> returnValues;
    private Stack<Handle> callStack;
    private MethodResolver methodResolver;
    private FieldResolver fieldResolver;
    private boolean isSafe;
    
    public DataInterpreter2(MethodResolver methodResolver, FieldResolver fieldResolver, MethodNode method, String className) {
        super(ASM5);
        this.methodResolver = methodResolver;
        this.fieldResolver = fieldResolver;
        this.returnValues = new Stack<DataValue>();
        this.callStack = new Stack<Handle>();
        
        Handle stackHandle = new Handle(Opcodes.H_INVOKESPECIAL,className,method.name,method.desc);
        callStack.push(stackHandle);
        returnValues.push(null);
    }

    protected DataInterpreter2(final int api) {
        super(api);
    }
    
    //Pass in a set of arguments to the function as if
    //we were actually invoking this method
    public void setArgs(Stack<DataValue> args) {
        isSafe = true;
        this.args = args;
        args.push(null); //The return type is pushed in first
    }
    
    public static boolean isAndroidName(String name) {
        if(name.startsWith("java/") ||
           name.startsWith("javax/") ||
           name.startsWith("android/") ||
           name.startsWith("sun/") ||
           name.startsWith("com/android") ||
           name.startsWith("org/apache")) {
                return true;
            }
            
            return false;
    }
    
    public static boolean isInternetName(String name) {
        if(name.startsWith("java/net") ||
           name.startsWith("org/apache/http")) {
            return true;
           }
        
        return false;
    }
    
    public static boolean isAndroidType(Type type) {
        return isAndroidName(type.getInternalName());
    }

    @Override
    public DataValue newValue(Type type)  {
        if(args != null && ! args.empty()) {
            if(args.peek() != null)
                return args.pop();
            else
                args.pop(); //Pop off the dummy value and continue
        }
        
        if(type == null) {
            return NativeValue.EMPTY_VALUE;
        }
        
        if(type.getSort() == Type.ARRAY && type.getElementType().getSort() == Type.OBJECT) {
            type = type.getElementType();
        }
        
        switch (type.getSort()) {
        case Type.VOID:
            return null;
        case Type.BOOLEAN:
        case Type.BYTE:
        case Type.CHAR:
        case Type.FLOAT:
        case Type.INT:
        case Type.SHORT:
            return NativeValue.WORD_VALUE;
        case Type.LONG:
        case Type.DOUBLE:
            return NativeValue.DOUBLE_VALUE;
        case Type.ARRAY:
            return new SimpleObjectValue(type);
        case Type.OBJECT:
            if(isAndroidType(type)) {
                return new SimpleObjectValue(type);
            } else {
                return new ComplexObjectValue(type);
            }
        default:
            System.out.println("Invalid call to new Value with type METHOD");
            return null;
        }   
    }

    @Override
    public DataValue newOperation(final AbstractInsnNode insn)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
        case ACONST_NULL:
            return new SimpleObjectValue(null);
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
            return new NativeValue(1, new Long(0));
        case LCONST_1:
            return new NativeValue(1, new Long(1));
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
            return  NativeValue.WORD_VALUE;
        case DCONST_0:
        case DCONST_1:
            return  NativeValue.DOUBLE_VALUE;
        case BIPUSH:
        case SIPUSH:
            return newValue(Type.INT_TYPE);
        case LDC:
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Integer) {
                return new NativeValue(1,cst);
            } else if (cst instanceof Float) {
                return NativeValue.WORD_VALUE;
            } else if (cst instanceof Long) {
                return new NativeValue(1,cst);
            } else if (cst instanceof Double) {
                return NativeValue.DOUBLE_VALUE;
            } else if (cst instanceof String) {
                SimpleObjectValue val = new SimpleObjectValue(Type.getObjectType("java/lang/String"));
                val.stringValues.add((String)cst);
                return val;
            } else if (cst instanceof Type) {
                return newValue((Type)cst);
            } else if (cst instanceof Handle) {
                return newValue(Type
                        .getObjectType("java/lang/invoke/MethodHandle"));
            } else {
                throw new IllegalArgumentException("Illegal LDC constant "
                        + cst);
            }
        case JSR:
            return newValue(Type.VOID_TYPE);
        case GETSTATIC:
            FieldInsnNode node = (FieldInsnNode) insn;
            Handle handle = new Handle(Opcodes.H_GETSTATIC, node.owner, node.name, node.desc);
            return fieldResolver.resolveField(handle);
        case NEW:
            return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
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
    public DataValue unaryOperation(final AbstractInsnNode insn, final DataValue value) throws AnalyzerException {
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
        case PUTSTATIC:
            FieldInsnNode node = (FieldInsnNode) insn;
            Handle handle = new Handle(Opcodes.H_GETSTATIC, node.owner, node.name, node.desc);
            fieldResolver.setField(handle, value);
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
        case GETFIELD:
            if(value instanceof ComplexObjectValue) {
                DataValue val = ((ComplexObjectValue)value).getField(((FieldInsnNode) insn).name);
                if(val != null) {
                    return val;
                }
            }
            return newValue((Type.getType(((FieldInsnNode) insn).desc)));
        case NEWARRAY:
            switch (((IntInsnNode) insn).operand) {
            case T_BOOLEAN:
                return newValue(Type.getType("[Z"));
            case T_CHAR:
                return newValue(Type.getType("[C"));
            case T_BYTE:
                return newValue(Type.getType("[B"));
            case T_SHORT:
                return newValue(Type.getType("[S"));
            case T_INT:
                return newValue(Type.getType("[I"));
            case T_FLOAT:
                return newValue(Type.getType("[F"));
            case T_DOUBLE:
                return newValue(Type.getType("[D"));
            case T_LONG:
                return newValue(Type.getType("[J"));
            default:
                throw new AnalyzerException(insn, "Invalid array type");
            }
        case ANEWARRAY:
            String desc = ((TypeInsnNode) insn).desc;
            System.out.println("ANEW ARRAY");
            return newValue(Type.getObjectType(desc));
        case ARRAYLENGTH:
            return newValue(Type.INT_TYPE);
        case ATHROW:
            return null;
        case CHECKCAST:
            desc = ((TypeInsnNode) insn).desc;
            return newValue(Type.getObjectType(desc));
        case INSTANCEOF:
            return newValue(Type.INT_TYPE);
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
            return newValue(Type.INT_TYPE);
        case FALOAD:
        case FADD:
        case FSUB:
        case FMUL:
        case FDIV:
        case FREM:
            return newValue(Type.FLOAT_TYPE);
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
            return newValue(Type.LONG_TYPE);
        case DALOAD:
        case DADD:
        case DSUB:
        case DMUL:
        case DDIV:
        case DREM:
            return newValue(Type.DOUBLE_TYPE);
        case AALOAD:
            return value1;
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
            return newValue(Type.INT_TYPE);
        case PUTFIELD:
            
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
            return null;
        default:
            throw new Error("Internal error.");
        }
    }

    @Override
    public DataValue ternaryOperation(final AbstractInsnNode insn,
            final DataValue value1, final DataValue value2,
            final DataValue value3) throws AnalyzerException {

        switch (insn.getOpcode()) {
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
    public DataValue naryOperation(final AbstractInsnNode insn, final List<? extends DataValue> values)
            throws AnalyzerException {
        int opcode = insn.getOpcode();
        if (opcode == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else if (opcode == INVOKEDYNAMIC) {
            System.out.println("Warning, unsupported operation INVOKEDYNAMIC");
            return newValue(Type.getReturnType(((InvokeDynamicInsnNode) insn).desc));
        } else {
            MethodInsnNode node = (MethodInsnNode) insn;
            Handle handle;
            Type type = null;
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
            
            Set<Type> runtimeTypes;
            if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                runtimeTypes = new HashSet<Type>(values.get(0).getTypes());
            } else {
                runtimeTypes = new HashSet<Type>();
                runtimeTypes.add(Type.getType(node.owner));
            }
            
            System.out.println(returnValues.size());
            System.out.println(node.name + " " + node.owner);
            System.out.println(runtimeTypes.size());
            returnValues.push(null);
            for(Type t : runtimeTypes) {
                if(isAndroidType(t) || methodResolver.isSafe(handle)) {
                    stubMethod(node,values);
                } else {
                    try{
                        MethodResolver.MethodResult res = methodResolver.resolveMethod(handle, t);
                        if(res == null) {
                            stubMethod(node,values);
                            continue;
                        }
                        Handle stackHandle = new Handle(Opcodes.H_INVOKESPECIAL,res.className,res.method.name,res.method.desc);
                        if(callStack.contains(stackHandle)) {
                            //We uncovered recursion, just use the base case we uncovered previously
                            System.out.println("CALLED");
                            int idx = callStack.indexOf(stackHandle);
                            returnOperation(null,returnValues.get(idx),null);
                        } else {
                            Stack<DataValue> newArgs = new Stack<DataValue>();
                            newArgs.addAll(values);
                            boolean safeBefore = isSafe;
                            this.setArgs(newArgs);
                            callStack.push(stackHandle);
                            Analyzer<DataValue> analyzer = new Analyzer<DataValue>(this);
                            analyzer.analyze(res.className, res.method);
                            callStack.pop();
                            if(isSafe) {
                                methodResolver.setSafe(handle);
                            }
                            this.isSafe = safeBefore && isSafe;
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if(isInternetName(t.getInternalName()) && returnValues.peek() != null && returnValues.peek() instanceof SimpleObjectValue) {
                    ((SimpleObjectValue) returnValues.peek()).internet = true;
                    this.isSafe = false;
                    System.out.println("Internet");
                }
            }
            
            DataValue retVal = returnValues.pop();
            if(retVal != null) {
                if(retVal.getTypes().size() == 0) {
                    System.out.println("BROKEN");
                }
                return retVal;
            }
            
            return newValue(Type.getReturnType(node.desc));
        }
        
    }
    
    private void stubMethod(MethodInsnNode node, List<? extends DataValue> values) {
        if(Type.getReturnType(node.desc).getSort() != Type.VOID) {
            for(DataValue v : values) {
                try {
                    returnOperation(null,v,null);
                } catch (AnalyzerException e) {
                    e.printStackTrace();
                }
             }
        }
        
         if(node.getOpcode() != Opcodes.INVOKESTATIC) {
             for(DataValue v : values) {
                 values.get(0).merge(v);
             }
         }
    }

    @Override
    public void returnOperation(final AbstractInsnNode insn,
            final DataValue value, final DataValue expected)
            throws AnalyzerException {
        if(!returnValues.isEmpty()) {
            returnValues.push(merge(returnValues.pop(),value));
        }
    }

    @Override
    public DataValue merge(final DataValue v, final DataValue w) {
        if(v == NativeValue.EMPTY_VALUE)
            return w;
        if(w == NativeValue.EMPTY_VALUE)
            return v;
        
        DataValue res;
        if(v instanceof ComplexObjectValue || w instanceof ComplexObjectValue)
            res = new ComplexObjectValue(null);
        else if(v instanceof SimpleObjectValue || w instanceof SimpleObjectValue)
            res = new SimpleObjectValue(null);
        else if(v != null)
            res = new NativeValue(v.getSize());
        else if(w != null)
            res = new NativeValue(w.getSize());
        else
            return null;
       
        res.merge(v);
        res.merge(w);
        
        return res;
    }

}
