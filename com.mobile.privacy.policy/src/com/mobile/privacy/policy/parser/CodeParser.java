package com.mobile.privacy.policy.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
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
        List<ICompilationUnit> compUnits = new ArrayList<ICompilationUnit>();
        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                
                IPackageFragment[] packages = javaProject.getPackageFragments();
                for (IPackageFragment mypackage : packages) {
                    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        for (ICompilationUnit unit : mypackage
                                .getCompilationUnits()) {
                             compUnits.add(unit);
                        }
                    }

                }
                PrivateDataRequestor requestor = new PrivateDataRequestor();
                ICompilationUnit[] compUnitArray = new ICompilationUnit[compUnits.size()];
                compUnits.toArray(compUnitArray);
                String[] bindings = new String[0];
                ASTParser parser = ASTParser.newParser(AST.JLS4);
                parser.setProject(javaProject);
                parser.setResolveBindings(true);
                parser.createASTs(compUnitArray, bindings, requestor, null);
                System.out.println(requestor.getPrivateDataLeaks());
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}
