package com.mobile.privacy.policy.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AndroidMethodVisitor extends ASTVisitor {
    private Map<IBinding,Set<String>> sensitiveVariables;
    private Set<IBinding> connectionVariables;
    
    public AndroidMethodVisitor() {
        sensitiveVariables = new HashMap<IBinding,Set<String>>();
        connectionVariables = new HashSet<IBinding>();
    }
    
    private class LHSAssignmentVisitor extends ASTVisitor {
        private Set<String> privacyTypes;
        boolean hasConnection;
        
        public LHSAssignmentVisitor(Set<String> privacyTypes, boolean hasConnection) {
            this.privacyTypes = privacyTypes;
            this.hasConnection = hasConnection;
        }
        
        public Set<String> getPrivacyTypes() {
            return privacyTypes;
        }
        
        @Override
        public boolean visit(SimpleName var) {
            addVariable(var.resolveBinding());
            if(hasConnection)
                connectionVariables.add(var.resolveBinding());
            return false;
        }
        
        @Override
        public boolean visit(FieldAccess fieldVar) {
            addVariable(fieldVar.resolveFieldBinding());
            if(hasConnection)
                connectionVariables.add(fieldVar.resolveFieldBinding());
            return false;
        }
        
        private void addVariable(IBinding var) {
            if(var != null) {
                if(sensitiveVariables.containsKey(var)) {
                    sensitiveVariables.get(var).addAll(privacyTypes);
                } else {
                    Set<String> typeSet = new HashSet<String>();
                    typeSet.addAll(privacyTypes);
                    sensitiveVariables.put(var, typeSet);
                }
            }
        }
    }
    
    private class RHSAssignmentVisitor extends ASTVisitor {
        
        private Set<String> privacyTypes;
        private boolean hasConnection;
        
        public RHSAssignmentVisitor(){
            privacyTypes = new HashSet<String>();
        }
        
        public Set<String> getPrivacyTypes() {
            return privacyTypes;
        }
        
        @Override
        public boolean visit(SimpleName var) {
            if(sensitiveVariables.containsKey(var.resolveBinding())) {
                privacyTypes.addAll(sensitiveVariables.get(var.resolveBinding()));
            }
            if(connectionVariables.contains(var.resolveBinding())) {
                hasConnection = true;
            }
                
            return false;
        }
        
        @Override
        public boolean visit(FieldAccess fieldVar) {
            if(sensitiveVariables.containsKey(fieldVar.resolveFieldBinding())) {
                privacyTypes.addAll(sensitiveVariables.get(fieldVar.resolveFieldBinding()));
            }
            return false;
        }
        
        @Override
        public boolean visit(MethodInvocation method) {
            if(method.getName().getFullyQualifiedName().equals("getSystemService") &&
               method.arguments().size() > 0 && (method.arguments().get(0).toString().equals("location") || 
               method.arguments().get(0).toString().equals("Context.LOCATION_SERVICE"))){
                privacyTypes.add("LOCATION");
                return false;
            }
            
            if(method.getName().equals("openConnection")) {
                hasConnection = true;
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * We want to poison any variables that are assigned to a poisoned variable,
     * A method taking as an input a poisoned variable, or one of the core methods used
     * to get access to private data
     */
    @Override
    public boolean visit(Assignment node) {
        RHSAssignmentVisitor rhsVisitor = new RHSAssignmentVisitor();
        node.getRightHandSide().accept(rhsVisitor);
        if(!rhsVisitor.getPrivacyTypes().isEmpty()) {
            LHSAssignmentVisitor lhsVisitor = new LHSAssignmentVisitor(rhsVisitor.getPrivacyTypes(),rhsVisitor.hasConnection);
            node.getLeftHandSide().accept(lhsVisitor);
        }
        
        //We handled the recursion using different handlers
        return false;
    }
    
    @Override 
    public boolean visit(VariableDeclarationFragment node) {
        RHSAssignmentVisitor rhsVisitor = new RHSAssignmentVisitor();
        if(node.getInitializer() != null) {
            node.getInitializer().accept(rhsVisitor);
            if(!rhsVisitor.getPrivacyTypes().isEmpty()) {
                LHSAssignmentVisitor lhsVisitor = new LHSAssignmentVisitor(rhsVisitor.getPrivacyTypes(),rhsVisitor.hasConnection);
                node.getName().accept(lhsVisitor);
            }
        }
        
        return false;
        
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        Set<IBinding> parameters = new HashSet<IBinding>();
        for(Object v: node.parameters()) {
            SingleVariableDeclaration var = (SingleVariableDeclaration) v;
            parameters.add(var.resolveBinding());
        }
        return true;
    }
    
    public Map<IBinding,Set<String>> getSensitiveVariables() {
        return sensitiveVariables;
    }

}
