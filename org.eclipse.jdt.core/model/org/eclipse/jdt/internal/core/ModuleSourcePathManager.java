/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class ModuleSourcePathManager {

	private Map<String, JavaProject> knownModules = new HashMap<String, JavaProject>(11);

	private JavaProject getModuleRoot0(String name) {
		return this.knownModules.get(name);
	}
	public JavaProject getModuleRoot(String name) {
		JavaProject root = getModuleRoot0(name);
		if (root == null) {
			try {
				seekModule(name.toCharArray(),false, new JavaElementRequestor());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		root = this.knownModules.get(name);
		return root;
	}

	public void seekModule(char[] name, boolean partialMatch, IJavaElementRequestor requestor) throws JavaModelException {
		if (name == null)
			return;
		if (!partialMatch) {
			IJavaProject[] projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
			for (int i = 0; i < projects.length; i++) {
				IJavaProject project = projects[i];
				if (!project.getProject().isAccessible())
					continue;
				if (project instanceof JavaProject) {
					IModule module = ((JavaProject) project).getModule();
					if (module != null) {
						char[] moduleName = module.name();
						if (CharOperation.equals(name, moduleName)) {
							this.knownModules.put(CharOperation.charToString(name), (JavaProject) project);
							requestor.acceptModule(module);
							break;
						}
					}
				}
			}
		} else {
			for (String key : this.knownModules.keySet()) {
				if (CharOperation.prefixEquals(name, key.toCharArray())) {
					requestor.acceptModule(this.knownModules.get(key).getModule());
				}
			}
		}
	}
	public IModule getModule(char[] name) {
		JavaProject root = getModuleRoot0(CharOperation.charToString(name));
		if (root != null)
			try {
				return root.getModule();
			} catch (JavaModelException e1) {
				//
				return null;
			}
		JavaElementRequestor requestor = new JavaElementRequestor();
		try {
			seekModule(name, false, requestor);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
		}
		IModule[] modules = requestor.getModules();
		return modules.length > 0 ? modules[0] : null; 
	}
	public IModule[] getModules() {
		if (this.knownModules.size() == 0) {
			return new IModule[0];
		}
		List<IModule> modules = new ArrayList<IModule>();
		for (JavaProject val : this.knownModules.values()) {
			try {
				modules.add(val.getModule());
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return modules.toArray(new IModule[modules.size()]);
	}
}
