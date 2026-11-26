/*******************************************************************************
 * Copyright (c) 2026 Eclipse contributors and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ParserReuseTest extends AbstractCompilerTest {

	public static Class testClass() {
		return ParserReuseTest.class;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}

	public ParserReuseTest(String testName) {
		super(testName);
	}

	private Parser parser() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		return new Parser(
				new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						new CompilerOptions(options),
						new DefaultProblemFactory(Locale.getDefault())),
				true);
	}

	private static CompilationUnitDeclaration parse(Parser parser, String name, String source) {
		ICompilationUnit unit = new CompilationUnit(source.toCharArray(), name, null);
		return parser.dietParse(unit, new CompilationResult(unit, 0, 0, 0));
	}

	private static Javadoc javadoc(CompilationUnitDeclaration unit, String selector) {
		for (AbstractMethodDeclaration method : unit.types[0].methods) {
			if (selector.equals(new String(method.selector))) {
				return method.javadoc;
			}
		}
		throw new AssertionError("no method " + selector);
	}

	private static List<String> nodesLeft(Object owner, Class<?> declaringClass, String fieldName) throws Exception {
		Field field = declaringClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		Object[] stack = (Object[]) field.get(owner);
		List<String> nodes = new ArrayList<>();
		if (stack != null) {
			for (Object node : stack) {
				if (node != null) {
					nodes.add(fieldName + ": " + node);
				}
			}
		}
		return nodes;
	}

	public void testInvalidParamReferencesOfAnotherComment() {
		Parser parser = parser();
		CompilationUnitDeclaration x = parse(parser, "X.java",
				"public class X {\n" +
				"	/**\n" +
				"	 * @throws Exception e\n" +
				"	 * @param facture f\n" +
				"	 */\n" +
				"	void m(int facture) throws Exception {}\n" +
				"	/** @param b the b */\n" +
				"	void k(int b) {}\n" +
				"}\n");
		CompilationUnitDeclaration y = parse(parser, "Y.java",
				"public class Y {\n" +
				"	/**\n" +
				"	 * Ok.\n" +
				"	 * @param a the a\n" +
				"	 */\n" +
				"	void n(int a) {}\n" +
				"	/** @return r */\n" +
				"	int o() { return 0; }\n" +
				"}\n");
		Javadoc m = javadoc(x, "m");
		assertNotNull("the @param after @throws of X.m", m.invalidParameters);
		assertEquals(1, m.invalidParameters.length);
		assertEquals("facture", new String(m.invalidParameters[0].token));
		assertNull("X.k, next in the same unit, has no invalid @param", javadoc(x, "k").invalidParameters);
		assertNull("Y.n has no invalid @param", javadoc(y, "n").invalidParameters);
		assertNull("Y.o has no invalid @param", javadoc(y, "o").invalidParameters);
	}

	public void testUsesReferencesOfAnotherComment() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK9) {
			return;
		}
		Parser parser = parser();
		CompilationUnitDeclaration module = parse(parser, "module-info.java",
				"/**\n" +
				" * @uses p.S\n" +
				" * @provides p.S\n" +
				" */\n" +
				"module a {\n" +
				"	uses p.S;\n" +
				"}\n");
		CompilationUnitDeclaration y = parse(parser, "Y.java",
				"public class Y {\n" +
				"	/** @param a the a */\n" +
				"	void n(int a) {}\n" +
				"}\n");
		assertEquals(1, module.javadoc.usesReferences.length);
		assertEquals(1, module.javadoc.providesReferences.length);
		assertEquals("Y.n has no @uses", 0, javadoc(y, "n").usesReferences.length);
		assertEquals("Y.n has no @provides", 0, javadoc(y, "n").providesReferences.length);
		List<String> nodes = new ArrayList<>();
		Class<?> commentParser = parser.javadocParser.getClass().getSuperclass();
		nodes.addAll(nodesLeft(parser.javadocParser, commentParser, "usesReferencesStack"));
		nodes.addAll(nodesLeft(parser.javadocParser, commentParser, "providesReferencesStack"));
		assertEquals("[]", nodes.toString());
	}

	public void testUsesReferencesOfThePreviousCommentOfTheUnit() {
		Parser parser = parser();
		CompilationUnitDeclaration x = parse(parser, "X.java",
				"public class X {\n" +
				"	/**\n" +
				"	 * @uses p.S\n" +
				"	 * @provides p.S\n" +
				"	 */\n" +
				"	void m() {}\n" +
				"	/** @param a the a */\n" +
				"	void n(int a) {}\n" +
				"}\n");
		assertEquals(1, javadoc(x, "m").usesReferences.length);
		assertEquals(1, javadoc(x, "m").providesReferences.length);
		assertEquals("X.n has no @uses", 0, javadoc(x, "n").usesReferences.length);
		assertEquals("X.n has no @provides", 0, javadoc(x, "n").providesReferences.length);
	}

	public void testStacksReleasedForTheNextUnit() throws Exception {
		Parser parser = parser();
		parse(parser, "Y.java",
				"import java.lang.annotation.*;\n" +
				"public class Y<T extends Comparable<T>> {\n" +
				"	@Target(ElementType.TYPE_USE) @interface A {}\n" +
				"	java.util.List<java.util.Map<String, T>> f;\n" +
				"	java.util.List<@A String> g;\n" +
				"	/**\n" +
				"	 * {@link java.util.List}\n" +
				"	 * @see String\n" +
				"	 * @throws Exception e\n" +
				"	 * @param a x\n" +
				"	 */\n" +
				"	void m(int a) throws Exception {}\n" +
				"	/**\n" +
				"	 * @throws Exception e\n" +
				"	 * @param b x\n" +
				"	 * @return r\n" +
				"	 */\n" +
				"	int r(int b) throws Exception { return 0; }\n" +
				"}\n");
		parse(parser, "Z.java", "public class Z {}\n");
		List<String> nodes = new ArrayList<>();
		nodes.addAll(nodesLeft(parser, Parser.class, "genericsStack"));
		nodes.addAll(nodesLeft(parser, Parser.class, "typeAnnotationStack"));
		nodes.addAll(nodesLeft(parser.javadocParser, parser.javadocParser.getClass().getSuperclass(), "astStack"));
		nodes.addAll(nodesLeft(parser.javadocParser, parser.javadocParser.getClass(), "invalidParamReferencesStack"));
		assertEquals("[]", nodes.toString());
		assertNull(parser.javadocParser.docComment);
		Field returnStatement = parser.javadocParser.getClass().getSuperclass().getDeclaredField("returnStatement");
		returnStatement.setAccessible(true);
		assertNull(returnStatement.get(parser.javadocParser));
	}
}
