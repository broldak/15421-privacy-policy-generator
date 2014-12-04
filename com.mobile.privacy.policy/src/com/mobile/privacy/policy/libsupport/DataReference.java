package com.mobile.privacy.policy.libsupport;

import java.util.Set;

import org.objectweb.asm.Type;

public interface DataReference {
    public int REF_NODE = 0x1;
    public int REF_LEAF = 0x2;
    public int REF_ROOT = 0x3;
    
    public int getRefType();
    public Type getType();
    public Set<DataReference> getField(String fieldName);
    public void setField(String name, DataReference value);
    
    public Set<String> getPrivateData();
}
