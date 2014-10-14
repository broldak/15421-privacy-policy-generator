package com.mobile.privacy.policy;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.mobile.privacy.policy.parser.CodeParser;

public class PolicyBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "com.mobile.privacy.policy.PolicyBuilder";
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {
		  
		  //Parse the project
		  CodeParser.parse(getProject());  
		  
		  switch (kind) {  
		  
		  case FULL_BUILD:  
		   break;  
		  
		  case INCREMENTAL_BUILD:  
		   break;  
		  
		  case AUTO_BUILD:  
		   break;
		  }
		  
		  return null;
	}

}
