/*******************************************************************************
 * Copyright (c) 2019, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

/*
 * This test, although originally was created for AST Level 14,
 * during the second iteration of preview feature Records,
 * being migrated to AST 15.
 */
@SuppressWarnings("rawtypes")
public class ASTConverter14Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
	private static final String jclLib = "CONVERTER_JCL14_LIB";

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST14(), false);
		this.currentProject = getJavaProject("Converter14");
		this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
		this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
		this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
	}

	public ASTConverter14Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter14Test.class);
	}

	@SuppressWarnings("deprecation")
	static int getAST14() {
		return AST.JLS14;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}


	/*
	 * Test that a simple switch expression's return type holds the correct type
	 */
	public void test0001() throws JavaModelException {
		String contents =
			"""
				public class X {
			   enum Day
			   {
			   	SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
			   	THURSDAY, FRIDAY, SATURDAY;
				}
				public static void main(String[] args) {
					Day day = Day.SUNDAY;
					int k = switch (day) {
			    	case MONDAY  -> throw new NullPointerException();
			    	case TUESDAY -> 1;
			
				 	case WEDNESDAY -> {yield 10;}
			    	default      -> {
			        	int g = day.toString().length();
			        	yield g;
			   	}};
			   	System.out.println(k);
				}
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		VariableDeclarationStatement vStmt1 = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(1);
		Type type = vStmt1.getType();
		IBinding binding = type.resolveBinding();
		assertTrue("null binding", binding != null);
		assertTrue("binding incorrect", binding.getName().equals("int"));
	}
	/*
	 * Test that a case statement with multiple cases is resolved correctly
	 * and has the correct source range
	 */
	public void _test0002() throws JavaModelException {
		String contents =
			"""
			public class X {
				static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}
				String aa(Day day) throws Exception {
					var today = "";
					switch (day) {
						case SATURDAY,SUNDAY ->
							today="Weekend";
						case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->
							today="Working";
						default ->
							throw new Exception("Invalid day: " + day.name());
					}
					return today;
				}
			\t
				String bb(Day day) throws Exception {
					var today = "";
					switch (day) {
						case SATURDAY,SUNDAY:
							today = "Weekend day";
							break;
						case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:
							today = "Working day";
							break;
						default:
							throw new Exception("Invalid day: " + day.name());
					}
					return today;
				}
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		SwitchStatement switchStatement = (SwitchStatement) node;
		checkSourceRange((Statement) switchStatement.statements().get(0), "case SATURDAY,SUNDAY ->", contents);
		checkSourceRange((Statement) switchStatement.statements().get(2), "case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->", contents);
	}

	/* test implicit break statement */

	public void _test0003() throws JavaModelException {
		String contents =
			"""
			public class X {
				static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}
				String aa(Day day) throws Exception {
					var today = "";
					switch (day) {
						case SATURDAY,SUNDAY ->
							today="Weekend";
						case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->
							today="Working";
						default ->
							throw new Exception("Invalid day: " + day.name());
					}
					return today;
				}
			\t
				String bb(Day day) throws Exception {
					String today = "";
					today = switch (day) {
						case SATURDAY,SUNDAY:
							yield "Weekend day";
						case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:
							yield "Week day";
						default:
							yield "Any day";
					};
					return today;
				}
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		SwitchStatement switchStatement = (SwitchStatement) node;
		checkSourceRange((Statement) switchStatement.statements().get(0), "case SATURDAY,SUNDAY ->", contents);

	}

	public void test0004() throws JavaModelException {
		String contents =
				"""
			public class X {
				static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}
				String bb(Day day) throws Exception {
					String today = switch (day) {
						case SATURDAY,SUNDAY:
							yield "Weekend day";
						case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:
							yield "Week day";
						default:
							yield "Any day";
					};
					return today;
				}
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		Expression expression = ((SwitchExpression) initializer).getExpression();
		assertEquals("incorrect type", ASTNode.SIMPLE_NAME, expression.getNodeType());
		assertEquals("incorrect name", "day", ((SimpleName) expression).getFullyQualifiedName());
		List statements = ((SwitchExpression) initializer).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		YieldStatement brStmt = (YieldStatement) statements.get(1);
		Expression expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

		//default case:
		SwitchCase caseStmt = (SwitchCase) statements.get(4);
		assertTrue("not default", caseStmt.isDefault());
		brStmt = (YieldStatement) statements.get(5);
		expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

	}
	@Deprecated
	public void _test0005() throws JavaModelException {
		String contents =
				"""
			public class X {
				public String test001() {
					int i = 0;
					String ret = switch(i%2) {
					case 0 -> "odd";
					case 1 -> "even";
					default -> "";
					};
					return ret;
				}\
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		Expression expression = ((SwitchExpression) initializer).getExpression();
		assertEquals("incorrect type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		List statements = ((SwitchExpression) initializer).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		BreakStatement brStmt = (BreakStatement) statements.get(1);
		Expression expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

				//default case:
		SwitchCase caseStmt = (SwitchCase) statements.get(4);
		assertTrue("not default", caseStmt.isDefault());
		brStmt = (BreakStatement) statements.get(5);
		expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

	}
	// Moved over from ASTConverter9Test
	@SuppressWarnings("deprecation")
	public void testBug531714_015() throws CoreException {
		// saw NPE in SwitchExpression.resolveType(SwitchExpression.java:423)
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		IJavaProject p =  createJavaProject("Foo", new String[] {"src"}, new String[] {jclLib}, "bin", "14"); // FIXME jcl12?
		try {
			String source =
				"import java.util.*;\n" +
				"public class X {\n" +
				"	void testForeach1(int i, List<String> list) {\n" +
				"		for (String s : switch(i) { case 1 -> list; default -> ; }) {\n" +
				"			\n" +
				"		}\n" +
				"		Throwable t = switch (i) {\n" +
				"			case 1 -> new Exception();\n" +
				"			case 2 -> new RuntimeException();\n" + // trigger !typeUniformAcrossAllArms
				"			default -> missing;\n" +
				"		};\n" +
				"	}\n" +
				"	void testForeach0(int i, List<String> list) {\n" + // errors in first arm
				"		for (String s : switch(i) { case 1 -> ; default -> list; }) {\n" +
				"			\n" +
				"		}\n" +
				"		Throwable t = switch (i) {\n" +
				"			case 0 -> missing;\n" +
				"			case 1 -> new Exception();\n" +
				"			default -> new RuntimeException();\n" + // trigger !typeUniformAcrossAllArms
				"		};\n" +
				"	}\n" +
				"	void testForeachAll(int i) {\n" + // only erroneous arms
				"		Throwable t = switch (i) {\n" +
				"			case 0 -> missing;\n" +
				"			default -> absent;\n" +
				"		};\n" +
				"	}\n" +
				"}\n";
			createFile("Foo/src/X.java", source);
			ICompilationUnit cuD = getCompilationUnit("/Foo/src/X.java");

			ASTParser parser = ASTParser.newParser(AST_INTERNAL_JLS14);
			parser.setProject(p);
			parser.setSource(cuD);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			org.eclipse.jdt.core.dom.CompilationUnit cuAST = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);
			IProblem[] problems = cuAST.getProblems();
			assertProblems("Unexpected problems",
					"""
						1. ERROR in /Foo/src/X.java (at line 4)
							for (String s : switch(i) { case 1 -> list; default -> ; }) {
							                                                    ^^
						Syntax error on token "->", Expression expected after this token
						----------
						2. ERROR in /Foo/src/X.java (at line 10)
							default -> missing;
							           ^^^^^^^
						missing cannot be resolved to a variable
						----------
						3. ERROR in /Foo/src/X.java (at line 14)
							for (String s : switch(i) { case 1 -> ; default -> list; }) {
							                                   ^^
						Syntax error on token "->", Expression expected after this token
						----------
						4. ERROR in /Foo/src/X.java (at line 18)
							case 0 -> missing;
							          ^^^^^^^
						missing cannot be resolved to a variable
						----------
						5. ERROR in /Foo/src/X.java (at line 25)
							case 0 -> missing;
							          ^^^^^^^
						missing cannot be resolved to a variable
						----------
						6. ERROR in /Foo/src/X.java (at line 26)
							default -> absent;
							           ^^^^^^
						absent cannot be resolved to a variable
						----------
						""",
					problems, source.toCharArray());
		} finally {
			deleteProject(p);
		}
	}

	public void test0011() throws CoreException {
		// saw NPE in SwitchExpression.resolveType(SwitchExpression.java:423)
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String source =
			"""
			public class Switch {
				public static void main(String[] args) {
					foo(Day.TUESDAY);
				}
			
				private static void foo(Day day) {
					switch (day) {
					case SUNDAY, MONDAY, FRIDAY -> System.out.println(6);
					case TUESDAY -> System.out.println(7);
					case THURSDAY, SATURDAY -> System.out.println(8);
					}
				}
			}
			
			enum Day {
				MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter14/src/Switch.java", true/*resolve*/);
		try {
		buildAST(
				source,
				this.workingCopy);
		} catch(UnsupportedOperationException e) {
			fail("Should not throw UnsupportedOperationException");
		} catch(AssertionFailedError e) {
			if (e.getMessage().contains("The enum constant WEDNESDAY needs a corresponding case label")) {
				// expected
				return;
			}
			throw e;
		}

	}

	public void testTextBlock002() throws JavaModelException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents =
				"""
			public class X {
				public String test001() {
					String s = \"""
			      	<html>
			        <body>
			            <p>Hello, world</p>
			        </body>
			    	</html>
			    	\""";
			    	System.out.println(s);\
					return s;
				}\
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		try {
		buildAST(
				contents,
				this.workingCopy);
		} catch(UnsupportedOperationException e) {
			fail("Should not throw UnsupportedOperationException");
		} catch(AssertionFailedError e) {
			if (e.getMessage().contains("The Java feature 'Text Blocks' is only available with source level 15 and above")) {
				// expected
				return;
			}
			throw e;
		}
		fail("Compilation should not succeed");

	}

	public void testBugGH949() throws JavaModelException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String sourceCode =
				"""
			public class GH949 {\
			public void test(String s){\
			switch (s){\
			case "1" ->System.out.println("One");\
			case "2" ->System.out.println("Two");\
			case "3" ->System.out.println("Three");\
			}\
			}\
			}""";

		this.workingCopies = new ICompilationUnit[] {
				getWorkingCopy("/Converter14/src/GH949.java", sourceCode, true/* resolve */)
		};
		CompilationUnit cu = (CompilationUnit) buildAST(this.workingCopies[0]);
		String flattened = ASTRewriteFlattener.asString(cu, new RewriteEventStore());
		assertEquals("Flattened AST", sourceCode, flattened);
	}
}
