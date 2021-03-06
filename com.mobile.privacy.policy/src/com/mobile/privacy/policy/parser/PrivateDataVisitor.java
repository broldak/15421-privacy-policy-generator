package com.mobile.privacy.policy.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class PrivateDataVisitor extends ASTVisitor {

    public Set<String> dataUse;
    private Set<String> dataSource = null;
    private Set<IBinding> vars = null;
    private Map<IBinding, Set<String>> privateVars;
    
    public PrivateDataVisitor() {
        privateVars = new HashMap<IBinding, Set<String>>();
        dataUse = new HashSet<String>();
    }
    
    public Map<IBinding, Set<String>> getPrivateVars() {
        return privateVars;
    }
    
    @Override
    public boolean visit(Assignment node) {
        vars = new HashSet<IBinding>();
        dataSource = new HashSet<String>();
        //Do the traversal in the reverse order
        node.getRightHandSide().accept(this);
        node.getLeftHandSide().accept(this);
        
        if(!dataSource.isEmpty()) {
            for(IBinding var : vars) {
               privateVars.put(var, dataSource);
            }
        }
        vars = null;
        dataSource = null;
        return false;
    }
    
    @Override 
    public boolean visit(VariableDeclarationFragment node) {
        if(node.getInitializer() != null) {
            vars = new HashSet<IBinding>();
            dataSource = new HashSet<String>();
            //Do the traversal in the reverse order
            node.getInitializer().accept(this);
            node.getName().accept(this);
            
            if(!dataSource.isEmpty()) {
                for(IBinding var : vars) {
                   privateVars.put(var, dataSource);
                }
            }
            vars = null;
            dataSource = null;
        }
        
        return false;
    }
    @Override
    public boolean visit(StringLiteral s) {
        if(s.getLiteralValue().contains("content://sms/inbox")) {
            dataUse.add("SMS_READ");
            if(dataSource != null) {
                dataSource.add("SMS_READ");
            }
        }
        return true;
    }
    
    @Override
    public boolean visit(MethodInvocation method) {
        boolean parsePrivateData = dataSource == null;
        if(parsePrivateData) {
            vars = new HashSet<IBinding>();
            dataSource = new HashSet<String>();
        }
        /*
        System.out.println(method.resolveMethodBinding().getName());
        System.out.println(method.resolveMethodBinding().getDeclaringClass().getName());
        System.out.println(method.resolveMethodBinding().getDeclaringClass().getPackage().getName());
        System.out.println(method.getName().getFullyQualifiedName());
        */
        //Check for methods
        if(method.getName().getFullyQualifiedName().equals("getSystemService") &&
            method.arguments().size() > 0 && (method.arguments().get(0).toString().equals("location") || 
            method.arguments().get(0).toString().equals("Context.LOCATION_SERVICE"))){
             dataSource.add("LOCATION");
             dataUse.add("LOCATION");
             return false;
        }
        if(method.resolveMethodBinding() != null && method.resolveMethodBinding().getDeclaringClass().getName().contains("SmsManager")) {
               dataSource.add("SMS_WRITE");
               dataUse.add("SMS_WRITE");
        }
        if(method.getExpression() != null)
            method.getExpression().accept(this);
        
        for(Object o : method.arguments()) {
            ((Expression) o).accept(this);
        }
        
        if(parsePrivateData) {
            if(!dataSource.isEmpty()) {
                for(IBinding var : vars) {
                   privateVars.put(var, dataSource);
                }
            }
            vars = null;
            dataSource = null;
        }
        return false;
    }
    
    @Override
    public boolean visit(SimpleName var) {
        if(vars != null && dataSource != null) {
            if(privateVars.containsKey(var.resolveBinding())) {
                dataSource.addAll(privateVars.get(var.resolveBinding()));
            } else {
                vars.add(var.resolveBinding());
            }
        }
        return true;
    }
    
    @Override
    public boolean visit(FieldAccess fieldVar) {
        if(fieldVar.resolveFieldBinding().getDeclaringClass().getName().contains("Camera")) {
            dataUse.add("CAMERA");
        }
        if(fieldVar.resolveFieldBinding().getDeclaringClass().getPackage().getName().contains("ContactsContract")) {
            dataUse.add("CONTACTS");
        }
        if(fieldVar.resolveFieldBinding().getDeclaringClass().getPackage().getName().contains("MediaStore.Images")) {
            dataUse.add("IMAGE_SEND");
        }
        
        if(vars != null && dataSource != null) {
            if(privateVars.containsKey(fieldVar.resolveFieldBinding())) {
                dataSource.addAll(privateVars.get(fieldVar.resolveFieldBinding()));
            } else {
                vars.add(fieldVar.resolveFieldBinding());
            }
        }
        return true;
    }
}
