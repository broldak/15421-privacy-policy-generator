package com.mobile.privacy.policy.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.mobile.privacy.policy.libsupport.LibraryAnalyser;

public class LibraryCallVisitor extends ASTVisitor {
    
    private LibraryAnalyser libraryAnalyser;
    private Map<String,String> libraryToUse;
    
    public LibraryCallVisitor(String libraryFile) {
        libraryAnalyser = new LibraryAnalyser();
        libraryAnalyser.loadMapping(libraryFile);
        libraryToUse = new HashMap<String,String>();
    }
    
    @Override
    public boolean visit(MethodInvocation method) {
        String pkg = method.resolveMethodBinding().getDeclaringClass().getPackage().getName();
        Map.Entry<String, String> e = libraryAnalyser.lookupPackgeName(pkg);
        if(e != null && e.getKey() != "ANDROID") {
            libraryToUse.put(e.getKey(), e.getValue());
        }
        return false;
    }
    
    public Map<String,String> getLibrariesUsed() {
        return libraryToUse;
    }
    
}
