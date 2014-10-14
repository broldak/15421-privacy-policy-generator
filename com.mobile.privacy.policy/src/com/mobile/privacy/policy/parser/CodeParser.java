package com.mobile.privacy.policy.parser;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class CodeParser {

    public static void parse(IProject project) {

        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {

                IPackageFragment[] packages = JavaCore.create(project)
                        .getPackageFragments();
                for (IPackageFragment mypackage : packages) {
                    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        for (ICompilationUnit unit : mypackage
                                .getCompilationUnits()) {
                            CompilationUnit parse = parse(unit);
                             AndroidMethodVisitor visitor = new AndroidMethodVisitor();
                             parse.accept(visitor);
                             for(Map.Entry<IBinding, Set<String>> var : visitor.getSensitiveVariables().entrySet()) {
                                 System.out.println("Variable: " + var.getKey().getName());
                                 System.out.println(var.getValue());
                             }
                        }
                    }

                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a ICompilationUnit and creates the AST DOM for manipulating the
     * Java source file
     * 
     * @param unit
     * @return
     */

    private static CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null);
    }
}
