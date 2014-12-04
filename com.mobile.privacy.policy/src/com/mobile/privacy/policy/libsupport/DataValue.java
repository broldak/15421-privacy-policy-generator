package com.mobile.privacy.policy.libsupport;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

public class DataValue implements Value{
    public Set<String> stringValues; //possible string value associated with this 
    public Set<String> dataValues; //possible private data associated with this
    public Set<Integer> argValues; //Used for stubbing methods
    private int size;
    
    public DataValue() {
        dataValues = new HashSet<String>();
        stringValues = new HashSet<String>();
        argValues = new HashSet<Integer>();
        size = 1;
    }
    public DataValue(int size) {
        this();
        this.size = size;
    }
    
    
    public int getSize() {
        return size;
    }

    public void merge(DataValue v) {
        stringValues.addAll(v.stringValues);
        dataValues.addAll(v.dataValues);
        argValues.addAll(v.argValues);
        
        if(v.size != size) {
            throw new RuntimeException("BAD SIZES IN MERGE");
        }
    }
}
