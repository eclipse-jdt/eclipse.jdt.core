/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.javac;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.junit.Test;

public class RegressionTests {

	@Test
	public void testGetDOMForClassWithSource() throws Exception {
		IProject project = importProject("projects/dummy");
		IJavaProject javaProject = JavaCore.create(project);
		IType arrayList = javaProject.findType("java.util.ArrayList");
		IClassFile classFile = (IClassFile)arrayList.getAncestor(IJavaElement.CLASS_FILE);
		var unit = (CompilationUnit)classFile.getWorkingCopy((WorkingCopyOwner)null, null);
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(unit);
		parser.setProject(javaProject);
		var domUnit = parser.createAST(null);
	}

	private IProject importProject(String locationInBundle) throws URISyntaxException, IOException, CoreException {
		File file = new File(FileLocator.toFileURL(getClass().getResource("/projects/dummy/.project")).toURI());
		IPath dotProjectPath = Path.fromOSString(file.getAbsolutePath());
		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace()
				.loadProjectDescription(dotProjectPath);
		projectDescription.setLocation(dotProjectPath.removeLastSegments(1));
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
		project.create(projectDescription, null);
		project.open(null);
		return project;
	}
}
