package com.mobile.privacy.policy.libsupport;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Type;

public class SimpleObjectValue implements DataValue {
    public Set<String> stringValues; //possible string value associated with this 
    public Set<String> dataValues; //possible private data associated with this
    public Set<Integer> argValues; //Used for stubbing methods
    public boolean internet;
    public Set<Type> types; //possible runtime types for this object
    
    public SimpleObjectValue() {
        internet = false;
        types = new HashSet<Type>();
        dataValues = new HashSet<String>();
        stringValues = new HashSet<String>();
        argValues = new HashSet<Integer>();
    }
    public SimpleObjectValue(Type type) {
        internet = false;
        types = new HashSet<Type>();
        dataValues = new HashSet<String>();
        stringValues = new HashSet<String>();
        
        if(type != null)
            types.add(type);
    }
    
    
    @Override
    public int getSize() {
        return 1;
    }
    
    @Override
    public void addArg(int idx){
        argValues.add(idx);
    }
    
    @Override
    publi
    
    @Override
    public void merge(DataValue v) {
        if(v instanceof SimpleObjectValue) {
            SimpleObjectValue obj = (SimpleObjectValue) v;
            stringValues.addAll(obj.stringValues);
            dataValues.addAll(obj.dataValues);
            internet = internet || obj.internet;
        } else {
            throw new RuntimeException("BAD VALUE " + v + " EXPECTED " + this);
        }
    }
    
    @Override
    public Set<String> getStringValues() {
        return stringValues;
    }
    
    @Override
    public Set<String> getDataValues() {
        return dataValues;
    }
    
    @Override
    public Set<Type> getTypes() {
        return types;
    }

}
