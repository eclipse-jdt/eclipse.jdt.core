/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.stream.Stream;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest9;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.UnnamedClass;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.junit.Test;

public class UnnamedClassTest extends AbstractRegressionTest9 {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK21; //$NON-NLS-1$

	public UnnamedClassTest(String testName){
		super(testName);
	}

	public static Class<?> testClass() {
		return UnnamedClassTest.class;
	}

	public static junit.framework.Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}

	private CompilationUnitDeclaration parse(String source, String testName) {
		this.complianceLevel = ClassFileConstants.JDK21;
		/* using regular parser in DIET mode */
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.enablePreviewFeatures = true;
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					options,
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		ICompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		return parser.parse(sourceUnit, compilationResult);
	}

	private UnnamedClass unnamedClass(CompilationUnitDeclaration cu) {
		return cu == null || cu.types == null ? null :
			Stream.of(cu.types).filter(UnnamedClass.class::isInstance).map(UnnamedClass.class::cast).findAny().orElse(null);
	}

	@Test
	public void testParseExplicitClass() {
		CompilationUnitDeclaration res = parse("public class A {}", "A.java");
		assertFalse(res.compilationResult.hasErrors());
		assertNull(unnamedClass(res));
	}

	@Test
	public void testParseOnlyMain() {
		CompilationUnitDeclaration res = parse("void main() {}", "A.java");
		assertFalse(res.hasErrors());
		UnnamedClass unnamedClass = unnamedClass(res);
		assertNotNull(unnamedClass);
		assertTrue(Stream.of(unnamedClass.methods).anyMatch(m -> m instanceof MethodDeclaration method && "main".equals(new String(method.selector))));
		// should generated A.class (unnamed)
	}

	@Test
	public void testParseMixedMethodAndTypes() {
		CompilationUnitDeclaration res = parse("""
			void hello() {}
			public class B {}
			void main() {}
			""", "A.java");
		assertFalse(res.compilationResult.hasErrors());
		// hello, main, and the implicit constructor
		assertEquals(3, unnamedClass(res).methods.length);
		// should generated A.class (unnamed) and A$B.class
		assertEquals(1, res.types[0].memberTypes.length);
	}
}
