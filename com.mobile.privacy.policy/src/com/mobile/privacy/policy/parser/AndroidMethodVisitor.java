package com.mobile.privacy.policy.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AndroidMethodVisitor extends ASTVisitor {
    private Map<IBinding,Set<String>> sensitiveVariables;
    
    public AndroidMethodVisitor() {
        sensitiveVariables = new HashMap<IBinding,Set<String>>();
    }
    
    private class LHSAssignmentVisitor extends ASTVisitor {
        private Set<String> privacyTypes;
        
        public LHSAssignmentVisitor(Set<String> privacyTypes) {
            this.privacyTypes = privacyTypes;
        }
        
        public Set<String> getPrivacyTypes() {
            return privacyTypes;
        }
        
        @Override
        public boolean visit(SimpleName var) {
            addVariable(var.resolveBinding());
            return false;
        }
        
        @Override
        public boolean visit(FieldAccess fieldVar) {
            addVariable(fieldVar.resolveFieldBinding());
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
            LHSAssignmentVisitor lhsVisitor = new LHSAssignmentVisitor(rhsVisitor.getPrivacyTypes());
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
                LHSAssignmentVisitor lhsVisitor = new LHSAssignmentVisitor(rhsVisitor.getPrivacyTypes());
                node.getName().accept(lhsVisitor);
            }
        }
        
        return false;
        
    }
    
    public Map<IBinding,Set<String>> getSensitiveVariables() {
        return sensitiveVariables;
    }

}
