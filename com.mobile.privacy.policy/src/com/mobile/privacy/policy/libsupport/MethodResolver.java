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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
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
    private Analyzer<DataValue> analyzer; 
    private Map<Handle, MethodSummary> methods;
    private Stack<Handle> callStack;
    
    public DataValue execute(MethodSummary summary, List<DataValue> args) {
       
        DataValue ret = DataInterpreter.typeToDataValue(summary.retType);
        
        for(int i = 0; i< args.size(); i++) {
            if(summary.retVal.argValues().contains(i))
               ret.merge(args.get(i));
            
            args.get(i).merge(summary.transforms.get(i));
            args.get(i).argValues().clear();
        }
        
        return ret;
    }
    
    public MethodSummary getSummary(Handle handle) {
        MethodSummary summary = new MethodSummary();
        summary.retType = Type.getReturnType(handle.getDesc());
        summary.retVal = DataInterpreter.typeToDataValue(summary.retType);
        summary.transforms = new ArrayList<DataValue>();
        for(Type t : Type.getArgumentTypes(handle.getDesc())) {
            summary.transforms.add(new SimpleObjectValue());
        }
        
        List<MethodResult> results = resolveMethod(handle);
        for(MethodResult res : results) {
            Frame<DataValue>[] frames = analyzer.analyze(res.className, res.method);
            for(int i = 0; i < frames.length; i++) {
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
                        if(frames[i].getLocal(j).getArgs().contains(k)) {
                            summary.transforms.get(k).merge(frames[i].getLocal(j));
                        }
                    }
                }
            }
        }
        
        methods.put(handle, summary);
        return summary;
    }
    
    public MethodResolver(ClassResolver classResolver) {
        this.classResolver = classResolver;
        this.methods = new HashMap<Handle, MethodSummary>();
        this.analyzer = new Analyzer<DataValue>(new DataInterpreter());
    }
    
    public List<MethodResult> resolveMethod(Handle handle) throws JavaModelException, IOException, ClassNotFoundException {
        switch(handle.getTag()) {
        case Opcodes.H_INVOKEINTERFACE:
        case Opcodes.H_INVOKEVIRTUAL:
            MethodResult parentMethod = resolveMethod(handle,handle.getOwner(),true);
        case Opcodes.H_INVOKESPECIAL:
        case Opcodes.H_INVOKESTATIC:
            return resolveMethod(handle,handle.getOwner(),false);
        default:
            System.out.println("Invalid handle");
            return null;
        }
    }
    
    
    private MethodResult resolveMethod(Handle handle, String className, boolean recurse) throws JavaModelException, IOException, ClassNotFoundException {
        if(DataInterpreter2.isAndroidName(className)) {
            return null;
        }
        System.out.println("Searching for method: " + handle.getName() + " in " + className);
        lastMethodAnalyzed = handle.getName();
        byte[] classFile = classResolver.resolveBytecodeClass(className);
        ClassReader reader = new ClassReader(classFile);
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        reader.accept(classNode, 0);
        for(MethodNode methodNode: classNode.methods) {
            if(methodNode.name.equals(handle.getName()) && methodNode.desc.equals(handle.getDesc())) {
                return new MethodResult(methodNode, className);
            }
        }
        if(recurse && !classNode.superName.equals("java/lang/Object")) {
            return resolveMethod(handle, classNode.superName,true);
        }
        
        return null;
    }
}
