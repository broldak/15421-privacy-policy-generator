package com.mobile.privacy.policy.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 










import com.mobile.privacy.policy.libsupport.DataInterpreter;
import com.mobile.privacy.policy.libsupport.DataValue;
import com.mobile.privacy.policy.libsupport.FieldPool;
import com.mobile.privacy.policy.libsupport.MethodResolver;
import com.mobile.privacy.policy.libsupport.PrivateMethodAnalyzer;

public class CodeParser {
    
    public static void parse(IProject project) {
        List<ICompilationUnit> compUnits = new ArrayList<ICompilationUnit>();
        try {
            if (project.isNatureEnabled(JavaCore.NATURE_ID)) {
                
                IJavaProject javaProject = JavaCore.create(project);
                ClassResolver classResolver = new ClassResolver(javaProject);
                FieldPool fieldPool = new FieldPool(classResolver);
                MethodResolver methodResolver = new MethodResolver(classResolver, fieldPool);
                
                IPackageFragment[] packages = javaProject.getPackageFragments();
      
                for (IPackageFragment mypackage : packages) {
                    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
                            IType t = unit.findPrimaryType();
                            if(t != null) {
                                String className = t.getFullyQualifiedName();
                                byte[] classSource;
                                try {
                                    classSource = classResolver.resolveBytecodeClass(className.replace('.', '/'));
                                    PrivateMethodAnalyzer.parseClass(classSource,fieldPool,classResolver);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                
                for (IPackageFragment mypackage : packages) {
                    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
                            IType t = unit.findPrimaryType();
                            if(t != null) {
                                String className = t.getFullyQualifiedName();
                                byte[] classSource = classResolver.resolveBytecodeClass(className.replace('.', '/'));
                                ClassReader classReader = new ClassReader(classSource);
                                ClassNode visitor = new ClassNode(Opcodes.ASM5);
                                classReader.accept(visitor, 0);
                                for(MethodNode method : visitor.methods) {
                                    //System.out.println("********************************");
                                    DataInterpreter interpreter = new DataInterpreter(methodResolver,fieldPool);//new DataInterpreter(new MethodResolver(resolver), new FieldResolver(resolver), method,visitor.name);
                                    Analyzer<DataValue> analyzer = new Analyzer<DataValue>(interpreter);
                                    //System.out.println(method.name + " " + className);
                                    analyzer.analyze(className, method);
                                }
                            }
                        }
                    }
                }
                
                
                //System.out.println(classResolver.dataUses);
         
                System.out.println("Private data Leaked: ");
                //System.out.println(leaked);
                System.out.println("Private data Used: ");
                //System.out.println(used);
               
                
                WriteXMLFile.write(project.getProject().getLocation().toString() + "/privacy.xml", "Privacy");
            }
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AnalyzerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
