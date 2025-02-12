/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImplicitTypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public class ASTConverter_24Test extends ConverterTestSetup {
	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST24(), false);
		this.currentProject = getJavaProject("Converter_24");
		this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_24);
		this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_24);
		this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_24);
		this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
	}

	public ASTConverter_24Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_24Test.class);
	}

	static int getAST24() {
		return AST.JLS24;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}
	public void test001() throws CoreException {
		ASTParser astParser = ASTParser.newParser(getAST24());
	    Map<String, String> options = new HashMap<>();
	    options.put(JavaCore.COMPILER_COMPLIANCE, "24");
	    options.put(JavaCore.COMPILER_SOURCE, "24");
	    options.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);

	    astParser.setCompilerOptions(options);
	    astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
	    astParser.setUnitName("Example.java");
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);

	    String source ="""
		    		String greeting() {
						return "Hello, World!";
					}
					void main() {
					    System.out.println(greeting());
					}
	    		""";

	    astParser.setSource(source.toCharArray());

	    CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(null);
	    ImplicitTypeDeclaration a = (ImplicitTypeDeclaration) compilationUnit.types().get(0);
	    assertTrue(Modifier.isFinal(a.getModifiers()));

	    ITypeBinding aBinding = a.resolveBinding();
	    assertTrue(Modifier.isFinal(aBinding.getModifiers()));
	}
}
