package com.mobile.privacy.policy.parser;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

public class PrivateDataRequestor extends ASTRequestor {
    INetConnectionVisitor connectionVisitor;
    PrivateDataVisitor dataVisitor;
    LibraryCallVisitor libCallVisitor;
    
    public PrivateDataRequestor() {
        connectionVisitor = new INetConnectionVisitor();
        dataVisitor = new PrivateDataVisitor();
        libCallVisitor = new LibraryCallVisitor("3rdPartyLibraries.txt");
    }
    
    @Override
    public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
        ast.accept(connectionVisitor);
        ast.accept(dataVisitor);
        ast.accept(dataVisitor); //Do this operation twice to propagate field variables
        ast.accept(libCallVisitor);
    }
    
    public Set<String> getPrivateDataLeaks() {
        Map<IBinding, Set<String>> privateVars = dataVisitor.getPrivateVars();
        Set<IBinding> inetVars = connectionVisitor.getInetVars();
        Set<String> leakedVars = new HashSet<String>();
        for(IBinding var : inetVars) {
            if(privateVars.containsKey(var)) {
                leakedVars.addAll(privateVars.get(var));
            }
        }
        
        return leakedVars;
    }
    
    public Map<String,String> getLibraryUses() {
        return libCallVisitor.getLibrariesUsed();
    }
    
}
