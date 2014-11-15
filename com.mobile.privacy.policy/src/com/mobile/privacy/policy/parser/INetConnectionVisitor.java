package com.mobile.privacy.policy.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class INetConnectionVisitor extends ASTVisitor {
    private boolean dataSink;
    private Set<IBinding> vars = null;
    private Set<IBinding> inetVars;
    
    public INetConnectionVisitor() {
        inetVars = new HashSet<IBinding>();
        
    }
    
    public Set<IBinding> getInetVars() {
        return inetVars;
    }
    
    @Override
    public boolean visit(Assignment node) {
        vars = new HashSet<IBinding>();
        //Do the traversal in the reverse order
        node.getRightHandSide().accept(this);
        node.getLeftHandSide().accept(this);
        
        if(dataSink) {
            for(IBinding var : vars) {
               inetVars.add(var);
            }
        }
        vars = null;
        dataSink = false;
        return false;
    }
    
    @Override 
    public boolean visit(VariableDeclarationFragment node) {
        if(node.getInitializer() != null) {
            vars = new HashSet<IBinding>();
            //Do the traversal in the reverse order
            node.getInitializer().accept(this);
            node.getName().accept(this);
            
            if(dataSink) {
                for(IBinding var : vars) {
                   inetVars.add(var);
                }
            }
            vars = null;
            dataSink = false;
        }
        
        return false;
    }
    
    @Override
    public boolean visit(MethodInvocation method) {
        System.out.println(method.getName());
        if(method.getName().toString().equals("openConnection")) {
            dataSink = true;
        }
        return true;
    }
    
    @Override
    public boolean visit(SimpleName var) {
        if(vars != null) {
            if(inetVars.contains(var.resolveBinding())) {
                dataSink = true;
            } else {
                vars.add(var.resolveBinding());
            }
        }
        return true;
    }
    
    @Override
    public boolean visit(FieldAccess fieldVar) {
        if(vars != null) {
            if(inetVars.contains(fieldVar.resolveFieldBinding())) {
                dataSink = true;
            } else {
                vars.add(fieldVar.resolveFieldBinding());
            }
        }
        return true;
    }
}
