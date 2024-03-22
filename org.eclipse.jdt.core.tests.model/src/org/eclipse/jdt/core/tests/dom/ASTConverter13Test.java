/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

@SuppressWarnings("rawtypes")
public class ASTConverter13Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
	private static final String jclLib = "CONVERTER_JCL13_LIB";

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST13(), false);
	}

	public ASTConverter13Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"_test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter13Test.class);
	}

	@SuppressWarnings("deprecation")
	static int getAST13() {
		return AST.JLS13;
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
	public void _test0001() throws JavaModelException {
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void _test0004() throws JavaModelException {
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	@Deprecated
	public void __test0005() throws JavaModelException {
		String contents =
				"""
			public class X {
				public String _test001() {
					int i = 0;
					String ret = switch(i%2) {
					case 0 -> "odd";
					case 1 -> "even";
					default -> "";
					};
					return ret;
				}\
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void _test0006() throws JavaModelException {
		String contents =
				"""
			public class X {
				public String _test001() {
					int i = 0;
					String ret = switch(i%2) {
					case 0 -> {return "odd"; }
					case 1 -> "even";
					default -> "";
					};
					return ret;
				}\
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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
			List statements = ((SwitchExpression) initializer).statements();
			assertEquals("incorrect no of statements", 6, statements.size());
			Block block = (Block) statements.get(1);
			statements = block.statements();
			assertEquals("incorrect no of statements", 1, statements.size());
			Statement stmt = (Statement) statements.get(0);
			assertEquals("incorrect node type", ASTNode.RETURN_STATEMENT, stmt.getNodeType());

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// Moved over from ASTConverter9Test
	@SuppressWarnings("deprecation")
	public void _testBug531714_015() throws CoreException {
		// saw NPE in SwitchExpression.resolveType(SwitchExpression.java:423)
		if (!isJRE13) {
			System.err.println("Test "+getName()+" requires a JRE 13");
			return;
		}
		IJavaProject p =  createJavaProject("Foo", new String[] {"src"}, new String[] {jclLib}, "bin", "13"); // FIXME jcl12?
		p.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		p.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		try {
			String source =
				"""
				import java.util.*;
				public class X {
					void testForeach1(int i, List<String> list) {
						for (String s : switch(i) { case 1 -> list; default -> ; }) {
						\t
						}
						Throwable t = switch (i) {
							case 1 -> new Exception();
							case 2 -> new RuntimeException();
							default -> missing;
						};
					}
					void testForeach0(int i, List<String> list) {
						for (String s : switch(i) { case 1 -> ; default -> list; }) {
						\t
						}
						Throwable t = switch (i) {
							case 0 -> missing;
							case 1 -> new Exception();
							default -> new RuntimeException();
						};
					}
					void testForeachAll(int i) {
						Throwable t = switch (i) {
							case 0 -> missing;
							default -> absent;
						};
					}
				}
				""";
			createFile("Foo/src/X.java", source);
			ICompilationUnit cuD = getCompilationUnit("/Foo/src/X.java");

			ASTParser parser = ASTParser.newParser(AST_INTERNAL_JLS13);
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

	public void _test0007() throws JavaModelException {
		String contents =
				"""
			public class X {
				public String _test001() {
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Text block statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
			List fragments = ((VariableDeclarationStatement) node).fragments();
			assertEquals("Incorrect no of fragments", 1, fragments.size());
			node = (ASTNode) fragments.get(0);
			assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
			Expression initializer = fragment.getInitializer();
			assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
			String escapedValue = ((TextBlock) initializer).getEscapedValue();

			assertTrue("String should not be empty", escapedValue.length() != 0);
			assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));

			String literal = ((TextBlock) initializer).getLiteralValue();
			assertEquals("literal value not correct",
					"""
						      	<html>
						        <body>
						            <p>Hello, world</p>
						        </body>
						    	</html>
						    	\
						""",
					literal);

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void _test0008() throws JavaModelException {
		String contents =
				"""
			public class X {
				public String _test001() {
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			try {
			buildAST(
					contents,
					this.workingCopy);
			} catch(UnsupportedOperationException e) {
				fail("Should not throw UnsupportedOperationException");
			} catch(AssertionFailedError e) {
				e.printStackTrace();
				return;
			}
			fail("Compilation should not succeed");

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void _test0009() throws JavaModelException {
		String contents =
				"""
			public class X {
				public String _test001() {
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
		this.workingCopy = getWorkingCopy("/Converter13/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Text block statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
			List fragments = ((VariableDeclarationStatement) node).fragments();
			assertEquals("Incorrect no of fragments", 1, fragments.size());
			node = (ASTNode) fragments.get(0);
			assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
			Expression initializer = fragment.getInitializer();
			assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
			ITypeBinding binding = initializer.resolveTypeBinding();
			assertNotNull("No binding", binding);
			assertEquals("Wrong qualified name", "java.lang.String", binding.getQualifiedName());

			String escapedValue = ((TextBlock) initializer).getEscapedValue();

			assertTrue("String should not be empty", escapedValue.length() != 0);
			assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));
			assertEquals("escaped value not correct",
					"""
						\"""
						      	<html>
						        <body>
						            <p>Hello, world</p>
						        </body>
						    	</html>
						    	\"\"\"""",
					escapedValue);

			String literal = ((TextBlock) initializer).getLiteralValue();
			assertEquals("literal value not correct",
					"""
						      	<html>
						        <body>
						            <p>Hello, world</p>
						        </body>
						    	</html>
						    	\
						""",
					literal);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void _test0010() throws JavaModelException {
		String contents =
				"""
			public class test13 {
				public static void main(String[] args) {
					String s = \"""
							nadknaks vgvh\s
							\""";
			
					int m = 10;
					m = m* 6;
					System.out.println(s);
				}
			}""" ;
		this.workingCopy = getWorkingCopy("/Converter13/src/test13.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("wrong line number", 3, compilationUnit.getLineNumber(node.getStartPosition()));
			node = getASTNode(compilationUnit, 0, 0, 1);
			assertEquals("wrong line number", 7, compilationUnit.getLineNumber(node.getStartPosition()));
			node = getASTNode(compilationUnit, 0, 0, 2);
			assertEquals("wrong line number", 8, compilationUnit.getLineNumber(node.getStartPosition()));
			node = getASTNode(compilationUnit, 0, 0, 3);
			assertEquals("wrong line number", 9, compilationUnit.getLineNumber(node.getStartPosition()));
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

		public void _test0011() throws CoreException {
			// saw NPE in SwitchExpression.resolveType(SwitchExpression.java:423)
			if (!isJRE13) {
				System.err.println("Test "+getName()+" requires a JRE 13");
				return;
			}
			String source =
				"""
				public class Switch {
					public static void main(String[] args) {
						foo(Day.TUESDAY);
					}
				
					@SuppressWarnings("preview")
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
			this.workingCopy = getWorkingCopy("/Converter13/src/Switch.java", true/*resolve*/);
			IJavaProject javaProject = this.workingCopy.getJavaProject();
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
				javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
				try {
				buildAST(
						source,
						this.workingCopy);
				} catch(UnsupportedOperationException e) {
					fail("Should not throw UnsupportedOperationException");
				} catch(AssertionFailedError e) {
					e.printStackTrace();
					return;
				}

				} finally {
					javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
				}
		}
}
