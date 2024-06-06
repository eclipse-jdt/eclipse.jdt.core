/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EitherOrMultiPattern;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.GuardedPattern;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Pattern;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

public class ASTConverterEitherOrMultiPatternTest extends ConverterTestSetup {
	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getASTLatest(), true);
		this.currentProject = getJavaProject("Converter_22");
		if (this.ast.apiLevel() == AST.JLS22) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_22);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_22);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_22);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		}
	}

	public ASTConverterEitherOrMultiPatternTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverterEitherOrMultiPatternTest.class);
	}

	static int getASTLatest() {
		return AST.getJLSLatest();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	private void printJREError() {
		System.err.println("Test "+getName()+" requires a JRE 22");
	}

	public void test001() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
					public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _) when notification instanceof Object :
							System.out.println("Eclipse");
					    	  break;
					      case EmailNotification email:
					    	  System.out.println("Sending push notification: " + email.eMessage);
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
					class PushNotification {
					  String pMessage;
					  public PushNotification(String message) {
					    this.pMessage = message;
					  }
					}
					class EmailNotification {
						String eMessage;
						public EmailNotification(String eMessage) {
							this.eMessage = eMessage;
						}
					}
					record SMSNotification(Object smsMessage) {}
				""";
		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		BodyDeclaration bodyDeclaration = (BodyDeclaration) getASTNode(compilationUnit, 0, 0);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertEquals("statements size", block.statements().size(), 2);

		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);

		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("variable declaration statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);

		List<ASTNode> blockStatements= block.statements();
		assertTrue("Number of statements not 2", blockStatements.size() == 2);

		SwitchStatement switchDeclarationStatement = (SwitchStatement)blockStatements.get(1);
		List<ASTNode> statements = switchDeclarationStatement.statements();
		assertTrue("Number of statements not 9", statements.size() == 9);

		SwitchCase firstSwitchCase = (SwitchCase) statements.get(0);
		GuardedPattern guardedPattern = (GuardedPattern) firstSwitchCase.expressions().get(0);
		assertEquals("Guarded Pattern", guardedPattern.getNodeType(), ASTNode.GUARDED_PATTERN);

		EitherOrMultiPattern pattern = (EitherOrMultiPattern) guardedPattern.getPattern();
		assertEquals("Either OR Multipattern", pattern.getNodeType(), ASTNode.EitherOr_MultiPattern);

		List<Pattern> listPatterns = pattern.patterns();
		assertTrue("Number of patterns not 3", listPatterns.size() == 3);
		assertEquals("TypePattern", listPatterns.get(0).getNodeType(), ASTNode.TYPE_PATTERN);
		assertEquals("TypePattern", listPatterns.get(1).getNodeType(), ASTNode.TYPE_PATTERN);
		assertEquals("RecordPattern", listPatterns.get(2).getNodeType(), ASTNode.RECORD_PATTERN);

		Expression conditionalExpression = guardedPattern.getExpression();
		assertEquals("Instance of Expression", conditionalExpression.getNodeType(), ASTNode.INSTANCEOF_EXPRESSION);


	}


	public void test002() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
					public class X {
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					    	case PushNotification _, EmailNotification _, SMSNotification(String _) when notification instanceof Object -> System.out.println("Eclipse");
					    	case EmailNotification email -> System.out.println("Sending push notification: " + email.eMessage);
					    	default -> System.out.println("Unknown notification type");
					    }
					  }
					}
					class PushNotification {
					  String pMessage;
					  public PushNotification(String message) {
					    this.pMessage = message;
					  }
					}
					class EmailNotification {
						String eMessage;
						public EmailNotification(String eMessage) {
							this.eMessage = eMessage;
						}
					}
					record SMSNotification(Object smsMessage) {}
				""";
		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		BodyDeclaration bodyDeclaration = (BodyDeclaration) getASTNode(compilationUnit, 0, 0);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertEquals("statements size", block.statements().size(), 2);

		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);

		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("variable declaration statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);

		List<ASTNode> blockStatements= block.statements();
		assertTrue("Number of statements not 2", blockStatements.size() == 2);

		SwitchStatement switchDeclarationStatement = (SwitchStatement)blockStatements.get(1);
		List<ASTNode> statements = switchDeclarationStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);

		SwitchCase firstSwitchCase = (SwitchCase) statements.get(0);
		GuardedPattern guardedPattern = (GuardedPattern) firstSwitchCase.expressions().get(0);
		assertEquals("Guarded Pattern", guardedPattern.getNodeType(), ASTNode.GUARDED_PATTERN);

		EitherOrMultiPattern pattern = (EitherOrMultiPattern) guardedPattern.getPattern();
		assertEquals("Either OR Multipattern", pattern.getNodeType(), ASTNode.EitherOr_MultiPattern);

		List<Pattern> listPatterns = pattern.patterns();
		assertTrue("Number of patterns not 3", listPatterns.size() == 3);
		assertEquals("TypePattern", listPatterns.get(0).getNodeType(), ASTNode.TYPE_PATTERN);
		assertEquals("TypePattern", listPatterns.get(1).getNodeType(), ASTNode.TYPE_PATTERN);
		assertEquals("RecordPattern", listPatterns.get(2).getNodeType(), ASTNode.RECORD_PATTERN);

		Expression conditionalExpression = guardedPattern.getExpression();
		assertEquals("Instance of Expression", conditionalExpression.getNodeType(), ASTNode.INSTANCEOF_EXPRESSION);


	}

	public void test003() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}

		String contents = """
				public class X {
				  public static void main(String[] args) {
				    Object notification = "Email notification";
				    switch (notification) {
				    	case PushNotification _, EmailNotification _, SMSNotification(String _) : System.out.println("Eclipse");
				    	default: System.out.println("Unknown notification type");
				    }
				  }
				}
				class PushNotification {
				  String pMessage;
				  public PushNotification(String message) {
				    this.pMessage = message;
				  }
				}
				class EmailNotification {
					String eMessage;
					public EmailNotification(String eMessage) {
						this.eMessage = eMessage;
					}
				}
				record SMSNotification(Object smsMessage) {}
			""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		BodyDeclaration bodyDeclaration = (BodyDeclaration) getASTNode(compilationUnit, 0, 0);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertEquals("statements size", block.statements().size(), 2);

		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);

		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("variable declaration statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);

		List<ASTNode> blockStatements= block.statements();
		assertTrue("Number of statements not 2", blockStatements.size() == 2);

		SwitchStatement switchDeclarationStatement = (SwitchStatement)blockStatements.get(1);
		List<ASTNode> statements = switchDeclarationStatement.statements();
		assertTrue("Number of statements not 4", statements.size() == 4);

		SwitchCase firstSwitchCase = (SwitchCase) statements.get(0);
		List<EitherOrMultiPattern> switchExpression = firstSwitchCase.expressions();
		assertEquals("Either OR Multipattern", switchExpression.get(0).getNodeType(), ASTNode.EitherOr_MultiPattern);

		List<Pattern> listPatterns = switchExpression.get(0).patterns();
		assertTrue("Number of patterns not 3", listPatterns.size() == 3);
		assertEquals("TypePattern", listPatterns.get(0).getNodeType(), ASTNode.TYPE_PATTERN);
		assertEquals("TypePattern", listPatterns.get(1).getNodeType(), ASTNode.TYPE_PATTERN);
		assertEquals("RecordPattern", listPatterns.get(2).getNodeType(), ASTNode.RECORD_PATTERN);
	}

	public void test004() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}

		String contents = """
				public class X {
				  public static void main(String[] args) {
				    Object notification = "Email notification";
				    switch (notification) {
				    	case PushNotification _, EmailNotification _, SMSNotification(String _) : System.out.println("Eclipse");
				    	default: System.out.println("Unknown notification type");
				    }
				  }
				}
				class PushNotification {
				  String pMessage;
				  public PushNotification(String message) {
				    this.pMessage = message;
				  }
				}
				class EmailNotification {
					String eMessage;
					public EmailNotification(String eMessage) {
						this.eMessage = eMessage;
					}
				}
				record SMSNotification(Object smsMessage) {}
			""";
		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		ASTNode nodePush = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		ASTNode nodeEmail = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		ASTNode nodeSMS = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Node Type Decleration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		assertEquals("Node Type Decleration - PushNotification", ASTNode.TYPE_DECLARATION, nodePush.getNodeType());
		assertEquals("Node Type Decleration - EmailNotification", ASTNode.TYPE_DECLARATION, nodeEmail.getNodeType());
		assertEquals("Node Type Decleration - SMSNotification", ASTNode.TYPE_DECLARATION, nodeSMS.getNodeType());
		ASTParser parser= ASTParser.newParser(getASTLatest());
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		parser.setProject(javaProject);
		IBinding[] bindings = parser.createBindings(new IJavaElement[] { this.workingCopy.findPrimaryType() }, null);
		IMethodBinding methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[0];
		assertEquals("constructor name", "X", methodBinding.getName());
		methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[1];
		assertEquals("method name", "main", methodBinding.getName());
	}
}