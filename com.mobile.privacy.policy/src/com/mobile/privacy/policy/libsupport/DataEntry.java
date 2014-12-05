package com.mobile.privacy.policy.libsupport;

import java.util.HashSet;
import java.util.Set;

public class DataEntry {
    public Set<String> data;
    public String details;
    
    public DataEntry() {
        data = new HashSet<String>();
        details = null;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof DataEntry))
            return false;
        
        if(((DataEntry)o).data.equals(data)) {
            return true;
        }
        
        return false;
    }
    
    
}
