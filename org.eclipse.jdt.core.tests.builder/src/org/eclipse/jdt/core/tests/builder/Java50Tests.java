/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class Java50Tests extends Tests {

	public Java50Tests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(Java50Tests.class);
	}

	public void _testAnnotation() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"@q.Ann\n" +
			"public class Use {\n" +
			"}"
		);
		env.addClass(projectPath, "q", "Ann",
			"package q;\n" +
			"public @interface Ann {\n" +
			"}"
		); 

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Ann",
			"package q;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD)\n" +
			"public @interface Ann {\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(usePath);
	}
	
}
