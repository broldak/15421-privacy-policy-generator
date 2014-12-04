package com.mobile.privacy.policy.libsupport;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Handle;

public class FieldPool {
    private Map<Handle, DataValue> fields;
    private Map<String, String> classParents;
    
    public FieldPool(Map<String,String> classParents) {
       fields = new HashMap<Handle,DataValue>();
       this.classParents = classParents;
    }
    
    public void initField(Handle handle, DataValue value) {
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
        return fields.get(h);
    }
    
    //Returns the most specific type for which we have a handle
    private Handle resolveField(Handle handle) {
        if(DataInterpreter2.isAndroidName(handle.getOwner())) {
            return null;
        }
        
        if(fields.containsKey(handle)) {
            return handle;
        }
        
        return resolveField(new Handle(handle.getTag(),classParents.get(handle.getOwner()), handle.getName(), handle.getDesc()));
    }
}
