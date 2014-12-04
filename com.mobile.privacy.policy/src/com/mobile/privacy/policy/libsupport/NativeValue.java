package com.mobile.privacy.policy.libsupport;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Type;


public class NativeValue implements DataValue{
    //Unknown values
    public static final NativeValue WORD_VALUE = new NativeValue(1);
    public static final NativeValue DOUBLE_VALUE = new NativeValue(2);
    public static final NativeValue EMPTY_VALUE = new NativeValue(1);
    
    public Object value;
    private int size;
    
    public NativeValue(int size) {
        this.size = size;
        this.value = null;
    }
    
    public NativeValue(int size, Object value) {
        this.size = size;
        this.value = value;
    }
    
    public boolean hasValue() {
        return this.value != null;
    }
    
    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void merge(DataValue v) {
        if(v instanceof NativeValue  && v.getSize() == this.getSize()) {
            NativeValue val = (NativeValue) v;
            if(!this.equals(val)) {
                this.value = null;
            }
        } else {
            throw new RuntimeException("BAD MERGE");
        }
    }
    
    @Override
    public Set<String> getStringValues() {
        return new HashSet<String>();
    }
    
    @Override
    public Set<String> getDataValues() {
        return new HashSet<String>();
    }
    
    @Override
    public Set<Type> getTypes() {
        return new HashSet<Type>();
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        
        if(o instanceof NativeValue) {
            NativeValue v2 = (NativeValue) o;
            if(v2.getSize() != size)
                return false;
            if(v2.value == null && value == null)
                return true;
            if(v2.value == null)
                return false;
            
            return v2.value.equals(value);
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        if(value != null) {
            return value.toString();
        }
        
        if(this == WORD_VALUE)
            return "WORD_VALUE";
        if(this == DOUBLE_VALUE)
            return "DOUBLE_VALUE";
        if(this == EMPTY_VALUE)
            return "EMPTY_VALUE";
        
        return "UNKNOWN";
    }

}
