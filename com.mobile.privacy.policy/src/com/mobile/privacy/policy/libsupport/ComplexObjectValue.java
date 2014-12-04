package com.mobile.privacy.policy.libsupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

public class ComplexObjectValue extends SimpleObjectValue {
    public Map<String,DataValue> fields;
    
    public ComplexObjectValue(Type type) {
        super(type);
        fields = new HashMap<String,DataValue>();
    }

    @Override
    public int getSize() {
        return 1;
    }
    
    public void setField(String name, DataValue v) {
        if(fields.containsKey(name)) {
            fields.get(name).merge(v);
        } else {
            fields.put(name, v);
        }
    }
    
    public DataValue getField(String name) {
        return fields.get(name);
    }
    
    @Override
    public void merge(DataValue v) {
        if(v instanceof ComplexObjectValue) {
            ComplexObjectValue obj = (ComplexObjectValue) v;
            types.addAll(obj.types);
            for(Map.Entry<String, DataValue> e : obj.fields.entrySet()) {
                if(fields.containsKey(e.getKey())) {
                    fields.get(e.getKey()).merge(e.getValue());
                } else {
                    fields.put(e.getKey(), e.getValue());
                }
            }
        }
        if(v instanceof SimpleObjectValue) {
            super.merge(v);
        }
    }
    
    @Override
    public Set<String> getStringValues() {
        Set<String> results = new HashSet<String>(this.stringValues);
        for(DataValue v : fields.values()) {
            results.addAll(v.getStringValues());
        }
        
        return results;
    }
    
    @Override
    public Set<String> getDataValues() {
        Set<String> results = new HashSet<String>(this.dataValues);
        for(DataValue v : fields.values()) {
            results.addAll(v.getDataValues());
        }
        
        return results;
    }
    
    @Override
    public Set<Type> getTypes() {
        return types;
    }
    
    @Override
    public String toString() {
        return "Complex object :\n Types: " + types.toString() + "\n + Fields: " + fields.toString(); 
    }
    
}
