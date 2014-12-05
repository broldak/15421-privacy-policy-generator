package com.mobile.privacy.policy.libsupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import com.mobile.privacy.policy.parser.ClassResolver;

public class MethodResolver {
    
    public class MethodSummary {
        public List<DataValue> transforms;
        public DataValue retVal;
        public Type retType;
        
        public MethodSummary() {
            transforms = null;
            retVal = null;
        }
        
    }
    
    public class MethodResult {
        public MethodNode method;
        public String className;
        
        public MethodResult(MethodNode method, String className) {
            this.method = method;
            this.className = className;
        }
    }
    
    private ClassResolver classResolver; 
    private Map<Handle, MethodSummary> methods;
    private Stack<Handle> callStack; 
    private FieldPool fieldPool;
    
    
    public DataValue execute(Handle handle, List<DataValue> args) {
        if(methods.containsKey(handle)) {
            return execute(methods.get(handle),args);
        }
        
        MethodSummary s = getSummary(handle);
        if(s == null)
            return stubMethod(handle, args);
        
        return execute(s,args);
    }
    
    private void addAll(String data, List<DataValue> args, DataValue ret) {
        for(DataValue arg : args)
            arg.dataValues.add(data);
        
        if(ret != null)
            ret.dataValues.add(data);
    }
    
    private DataValue stubMethod(Handle handle, List<DataValue> args) {
        DataValue ret = DataInterpreter.typeToDataValue(Type.getReturnType(handle.getDesc()));
        String name = handle.getName();
        String owner = handle.getOwner();
        if(name.contains("getExternalStorage") || name.contains("getExternalFilesDir")) {
            addAll("EXTERNAL_STORAGE",args,ret);
        }
        if(owner.startsWith("java/net") ||
           owner.startsWith("org/apache/http")) {
           addAll("INTERNET",args,ret);
        }
        if(owner.equals("android/hardware/Camera") || owner.contains("android/hardware/camera2")) {
            addAll("CAMERA",args,ret);
           
        } else if(owner.equals("android/location/LocationManager")) {
            addAll("LOCATION",args,ret);
        } else if(owner.equals("android/telephony/SmsManager")) {
            addAll("SMS_WRITE",args,ret);
        }
            
        if(ret != null) {
            if(ret.isRef()) {
                for(DataValue v : args) {
                    if(v.isRef())
                        ret.merge(v);
                }
            }
        }
        if(handle.getTag() != Opcodes.INVOKESTATIC) {
            for(int i = 1; i< args.size(); i++) {
                if(args.get(i).isRef())
                    args.get(0).merge(args.get(i));
            }
        }
        
        return ret;
    }
    
    private DataValue execute(MethodSummary summary, List<DataValue> args) {
        DataValue ret = DataInterpreter.typeToDataValue(summary.retType);
        for(int i = 0; i< args.size(); i++) {
            if(ret != null && ret.isRef()) {
                if(summary.retVal.argValues.contains(i))
                   ret.merge(args.get(i));
            }
            
            args.get(i).merge(summary.transforms.get(i));
            args.get(i).argValues.clear();
        }
        
        if(ret != null && ret.isRef()) {
            ret.merge(summary.retVal);
            ret.argValues.clear();
        }
        
        return ret;
    }
    
    public MethodSummary getSummary(Handle handle) {
        MethodSummary summary = new MethodSummary();
        summary.retType = Type.getReturnType(handle.getDesc());
        summary.retVal = DataInterpreter.typeToDataValue(summary.retType);
        summary.transforms = new ArrayList<DataValue>();
        if(handle.getTag() != Opcodes.INVOKESTATIC) {
            summary.transforms.add(new DataValue());
        }
        for(Type t : Type.getArgumentTypes(handle.getDesc())) {
            summary.transforms.add(DataInterpreter.typeToDataValue(t));
        }
        
        if(callStack.contains(handle))
            return null;
        
        callStack.push(handle);
        List<MethodResult> results = null;
        try {
            results = resolveMethod(handle);
            if(results.isEmpty())
                return null;
            for(MethodResult res : results) {
                Frame<DataValue>[] frames= null;
            
                frames = new Analyzer<DataValue>(new DataInterpreter(this,fieldPool)).analyze(res.className, res.method);
                for(int i = 0; i < frames.length; i++) {
                    if(frames[i] == null)
                        continue;
                    switch(res.method.instructions.get(i).getOpcode()) {
                        case Opcodes.IRETURN:
                        case Opcodes.LRETURN:
                        case Opcodes.FRETURN:
                        case Opcodes.DRETURN:
                        case Opcodes.ARETURN:
                            summary.retVal.merge(frames[i].pop());
                            break;
                        default :
                            break;
                    }
                    for(int j = 0; j< frames[i].getLocals(); j++) {
                        for(int k = 0; k < summary.transforms.size(); k++) {
                            if(frames[i].getLocal(j).argValues.contains(k)) {
                                summary.transforms.get(k).merge(frames[i].getLocal(j));
                            }
                        }
                    }
                    for(int j = 0; j < frames[i].getStackSize(); j++) {
                        for(int k = 0; k < summary.transforms.size(); k++) {
                            if(frames[i].getStack(j).argValues.contains(k)) {
                                summary.transforms.get(k).merge(frames[i].getStack(j));
                            }
                        }
                    }
                }
                for(int i = 0; i < summary.transforms.size(); i++) {
                    summary.transforms.get(i).argValues.clear();
                }
            }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        callStack.pop();
        methods.put(handle, summary);
        return summary;
    }
    
    public MethodResolver(ClassResolver classResolver, FieldPool fieldPool) {
        this.classResolver = classResolver;
        this.fieldPool = fieldPool;
        this.methods = new HashMap<Handle, MethodSummary>();
        this.callStack = new Stack<Handle>();
    }
    
    public List<MethodResult> resolveMethod(Handle handle) throws JavaModelException, IOException, ClassNotFoundException {
        List<MethodResult> results = new ArrayList<MethodResult>();
        MethodResult parentMethod;
        switch(handle.getTag()) {
        case Opcodes.H_INVOKEINTERFACE:
        case Opcodes.H_INVOKEVIRTUAL:
             parentMethod = resolveMethod(handle,handle.getOwner(),true);
             if(parentMethod != null) {
                 results.add(parentMethod);
             }
             break;
        case Opcodes.H_INVOKESPECIAL:
        case Opcodes.H_INVOKESTATIC:
            parentMethod = resolveMethod(handle,handle.getOwner(),true);
            if(parentMethod != null) {
                results.add(parentMethod);
            }
           break;
        default:
            System.out.println("Invalid handle");
            return null;
        }
        
        return results;
    }
    
    
    private MethodResult resolveMethod(Handle handle, String className, boolean recurse) throws JavaModelException, IOException, ClassNotFoundException {
        if(Util.isAndroidName(className)) {
            return null;
        }
        System.out.println("Searching for method: " + handle.getName() + " in " + className);
        byte[] classFile = classResolver.resolveBytecodeClass(className);
        ClassReader reader = new ClassReader(classFile);
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        reader.accept(classNode, 0);
        for(MethodNode methodNode: classNode.methods) {
            if(methodNode.name.equals(handle.getName()) && methodNode.desc.equals(handle.getDesc())) {
                return new MethodResult(methodNode, className);
            }
        }
       
        return resolveMethod(handle, classNode.superName,true);
    }
}
