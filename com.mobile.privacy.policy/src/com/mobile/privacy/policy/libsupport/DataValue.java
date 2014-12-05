package com.mobile.privacy.policy.libsupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

public class DataValue implements Value{
    public static List<DataEntry> entries = new ArrayList<DataEntry>();
    
    public static void addEntry(DataEntry entry) {
        for(DataEntry e: entries) {
            if(e.equals(entry)) {
                if(entry.details != null) {
                    e.details = entry.details;
                }
                return;
            }
        }
        
        entries.add(entry);
    }
    public static final DataValue WORD_VALUE = new DataValue(1);
    public static final DataValue DOUBLE_VALUE = new DataValue(2);
    public static final DataValue EMPTY_VALUE = new DataValue(1);
    
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
        if(!this.isRef())
            return;
        
        stringValues.addAll(v.stringValues);
        dataValues.addAll(v.dataValues);
        argValues.addAll(v.argValues);
        
        if(dataValues.size() > 0) {
            DataEntry e = new DataEntry();
            e.data.addAll(dataValues);
            if((dataValues).contains("INTERNET")) {
                for(String s: stringValues) {
                    if((s.contains("http://") || s.contains("https://")) && s.contains(".com")) {
                        e.details = s;
                        break;
                    }
                }
            }
            
            if(dataValues.contains("EXTERNAL_WRITE")) {
                for(String s: stringValues) {
                    if((s.startsWith("/") || s.endsWith("/"))) {
                        e.details = s;
                        break;
                    }
                }
            }
            addEntry(e);
        }
        
        if(v!= EMPTY_VALUE && v.size != size) {
            //throw new RuntimeException("BAD SIZES IN MERGE" + this + " " + v);
        }
    }
    
    public boolean isRef() {
        if(this == WORD_VALUE || this == DOUBLE_VALUE || this == EMPTY_VALUE)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        if(this == WORD_VALUE)
            return "WORD VALUE";
        else if(this == DOUBLE_VALUE)
            return "DOUBLE VALUE";
        else if(this == EMPTY_VALUE)
            return "EMPTY VALUE";
        else
            return "String: " + stringValues + " Data: " + dataValues + " Args: " + argValues;
    }
}
