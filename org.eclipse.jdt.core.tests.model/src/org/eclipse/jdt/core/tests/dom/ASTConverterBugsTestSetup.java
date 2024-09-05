/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.Map;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import junit.framework.Test;

public class ASTConverterBugsTestSetup extends ConverterTestSetup {

@Override
public void setUpSuite() throws Exception {
//	PROJECT_SETUP = true; // do not copy Converter* directories
	super.setUpSuite();
//	setUpJCLClasspathVariables("1.5");
	waitUntilIndexesReady();
}

public ASTConverterBugsTestSetup(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(ASTConverterBugsTestSetup.class);
}

protected void checkParameterAnnotations(String message, String expected, IMethodBinding methodBinding) {
	ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
	int size = parameterTypes == null ? 0 : parameterTypes.length;
	StringBuilder buffer = new StringBuilder();
	for (int i=0; i<size; i++) {
		buffer.append("----- param ");
		buffer.append(i+1);
		buffer.append("-----\n");
		IAnnotationBinding[] bindings= methodBinding.getParameterAnnotations(i);
		int length = bindings.length;
		for (int j=0; j<length; j++) {
			buffer.append(bindings[j].getKey());
			buffer.append('\n');
		}
	}
	assertEquals(message, expected, buffer.toString());
}

@Override
public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
	return runConversion(this.testLevel, unit, resolveBindings);
}

@Override
public ASTNode runConversion(ICompilationUnit unit, int position, boolean resolveBindings) {
	return runConversion(this.testLevel, unit, position, resolveBindings);
}

@Override
public ASTNode runConversion(IClassFile classFile, int position, boolean resolveBindings) {
	return runConversion(this.testLevel, classFile, position, resolveBindings);
}

@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
	return runConversion(this.testLevel, source, unitName, project);
}

@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project, boolean resolveBindings) {
	return runConversion(this.testLevel, source, unitName, project, resolveBindings);
}

@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map<String, String> options, boolean resolveBindings) {
	return runConversion(this.testLevel, source, unitName, project, options, resolveBindings);
}
@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map<String, String> options) {
	return runConversion(this.testLevel, source, unitName, project, options);
}

public ASTNode runConversion(
		ICompilationUnit unit,
		boolean resolveBindings,
		boolean statementsRecovery,
		boolean bindingsRecovery) {
	ASTParser parser = createASTParser();
	parser.setSource(unit);
	parser.setResolveBindings(resolveBindings);
	parser.setStatementsRecovery(statementsRecovery);
	parser.setBindingsRecovery(bindingsRecovery);
	parser.setWorkingCopyOwner(this.wcOwner);
	return parser.createAST(null);
}

@Override
protected void resolveASTs(ICompilationUnit[] cus, String[] bindingKeys, ASTRequestor requestor, IJavaProject project, WorkingCopyOwner owner) {
	ASTParser parser = createASTParser();
	parser.setResolveBindings(true);
	parser.setProject(project);
	parser.setWorkingCopyOwner(owner);
	parser.createASTs(cus, bindingKeys,  requestor, null);
}
}
