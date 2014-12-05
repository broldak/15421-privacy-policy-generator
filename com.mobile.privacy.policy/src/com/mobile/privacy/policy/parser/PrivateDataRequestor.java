package com.mobile.privacy.policy.parser;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;

import com.mobile.privacy.policy.libsupport.DataInterpreter;
import com.mobile.privacy.policy.libsupport.DataValue;
import com.mobile.privacy.policy.libsupport.FieldPool;
import com.mobile.privacy.policy.libsupport.MethodResolver;
import com.mobile.privacy.policy.libsupport.PrivateMethodAnalyzer;

public class PrivateDataRequestor extends ASTRequestor {
    INetConnectionVisitor connectionVisitor;
    PrivateDataVisitor dataVisitor;
    ClassResolver classResolver;
    MethodResolver methodResolver;
    FieldPool fieldPool;
    
    public PrivateDataRequestor(ClassResolver classResolver) {
        connectionVisitor = new INetConnectionVisitor();
        dataVisitor = new PrivateDataVisitor();
        fieldPool = new FieldPool(classResolver);
        methodResolver = new MethodResolver(classResolver, fieldPool);
        this.classResolver = classResolver;
        //libCallVisitor = new LibraryCallVisitor(resolver);
        //libCallVisitor = new LibraryCallVisitor("3rdPartyLibraries.txt");
    }
    
    @Override
    public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
        //ast.accept(connectionVisitor);
        //ast.accept(dataVisitor);
        //ast.accept(dataVisitor); //Do this operation twice to propagate field variables
        //ast.accept(libCallVisitor);
       
        IType t = source.findPrimaryType();
        if(t != null) {
            String className = t.getFullyQualifiedName();
            byte[] classSource;
            try {
                classSource = classResolver.resolveBytecodeClass(className.replace('.', '/'));
                PrivateMethodAnalyzer.parseClass(classSource,fieldPool,classResolver);
                ClassReader classReader = new ClassReader(classSource);
                ClassNode visitor = new ClassNode(Opcodes.ASM5);
                classReader.accept(visitor, 0);
                
                for(MethodNode method : visitor.methods) {
                    System.out.println("********************************");
                    DataInterpreter interpreter = new DataInterpreter(methodResolver,fieldPool);//new DataInterpreter(new MethodResolver(resolver), new FieldResolver(resolver), method,visitor.name);
                    Analyzer<DataValue> analyzer = new Analyzer<DataValue>(interpreter);
                    System.out.println(method.name + " " + className);
                    analyzer.analyze(className, method);
                }
                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
    
    public Set<String> getPrivateDataUsed() {
        return dataVisitor.dataUse;
    }
    
    public Map<String,String> getLibraryUses() {
        return new HashMap<String,String>();
    }
    
}
