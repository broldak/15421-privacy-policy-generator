package com.mobile.privacy.policy.libsupport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.analysis.Analyzer;

import com.mobile.privacy.policy.libsupport.MethodResolver.MethodResult;
import com.mobile.privacy.policy.parser.ClassResolver;

public class FieldResolver {
    private MethodResolver methodResolver;
    private Map<Handle, DataValue> staticFields;
    
    public FieldResolver(ClassResolver classResolver) {
        this.methodResolver = new MethodResolver(classResolver);
        staticFields = new HashMap <Handle,DataValue>();
    }
    
    public void setField(Handle handle, DataValue value) {
        if(!staticFields.containsKey(handle)) {
            staticFields.put(handle, value);
        } else {
            staticFields.get(handle).merge(value);
        }
        
    }
    
    public DataValue resolveField(Handle handle){
        if(staticFields.containsKey(handle)) {
            return staticFields.get(handle);
        }
        System.out.println("CALLED");
        Type type = Type.getType(handle.getDesc());
        try{
            //Invoke the class initializer
            Handle inithandle = new Handle(Opcodes.H_INVOKESTATIC,handle.getOwner(),"<clinit>","()V");
            MethodResult res = methodResolver.resolveMethod(inithandle,null);
            if(res == null) {
                System.out.println("ISSUE");
                return null;
            }
            DataInterpreter2 interp = new DataInterpreter2(methodResolver, this, res.method, res.className);
            Analyzer<DataValue> analyzer = new Analyzer<DataValue>(interp);
            analyzer.analyze(res.className, res.method);
            if(staticFields.containsKey(handle)) {
                return staticFields.get(handle);
            } else {
                return interp.newValue(type);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
