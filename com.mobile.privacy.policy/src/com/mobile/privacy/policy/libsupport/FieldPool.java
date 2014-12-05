package com.mobile.privacy.policy.libsupport;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import com.mobile.privacy.policy.parser.ClassResolver;

public class FieldPool {
    private Map<Handle, DataValue> fields;
    private ClassResolver classResolver;
    
    public FieldPool(ClassResolver classResolver) {
       fields = new HashMap<Handle,DataValue>();
       this.classResolver = classResolver;
    }
    
    public void initField(Handle handle, DataValue value) {
        System.out.println(handle.getName());
        System.out.println(handle.getOwner());
        fields.put(handle, value);
    }
    
    public void setField(Handle handle, DataValue value) {
        Handle h = resolveField(handle);
        if(h != null) {
            fields.get(h).merge(value);
        }
    }
    
    public DataValue getField(Handle handle) {
        Handle h = resolveField(handle);
        if(h == null || fields.get(h)== null)
            return DataInterpreter.typeToDataValue(Type.getType(handle.getDesc()));
        return fields.get(h);
    }
    
    //Returns the most specific type for which we have a handle
    private Handle resolveField(Handle handle) {
        if(Util.isAndroidName(handle.getOwner())) {
            return null;
        }
        
        if(fields.containsKey(handle)) {
            return handle;
        }
        if(classResolver.getParent(handle.getOwner()) == null) {
            return null;
        }
        
        return resolveField(new Handle(handle.getTag(),classResolver.getParent(handle.getOwner()), handle.getName(), handle.getDesc()));
    }
}
