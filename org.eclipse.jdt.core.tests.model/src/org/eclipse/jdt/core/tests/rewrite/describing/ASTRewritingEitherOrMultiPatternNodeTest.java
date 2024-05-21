/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.GuardedPattern;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritingEitherOrMultiPatternNodeTest extends ASTRewritingTest {

	static {
		//TESTS_NAMES = new String[] {"test007_c"};
	}

	public ASTRewritingEitherOrMultiPatternNodeTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingEitherOrMultiPatternNodeTest.class, 22);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS22 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}
	//@SuppressWarnings({ })
	//replacing : by ->
	//removing guard from multipatternNode
	public void test0001() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String code = """
					public class X {
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					    	case PushNotification _, EmailNotification _, SMSNotification(String _) when notification instanceof Object : System.out.println("Eclipse");
					    	default : System.out.println("Unknown notification type");
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
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", code, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();
		{
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
			assertTrue("Number of statements not 4", switchStatement.statements().size() == 4);
			for (int i = 0, l = switchStatement.statements().size(); i < l; ++i) {
				Statement stmt = (Statement) switchStatement.statements().get(i);
				if (stmt instanceof SwitchCase) {
					SwitchCase switchCase = (SwitchCase) stmt;
					assertTrue("Switch case has arrow", switchCase.isSwitchLabeledRule() == false);
					rewrite.set(switchCase, SwitchCase.SWITCH_LABELED_RULE_PROPERTY, Boolean.TRUE, null);
				}
			}
		}
		String preview= evaluateRewrite(cu, rewrite);
		String ASTConverterCode = """
					public class X {
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					    	case PushNotification _, EmailNotification _, SMSNotification(String _) when notification instanceof Object -> System.out.println("Eclipse");
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
		assertEqualString(preview, ASTConverterCode);

		{
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
			List<?> statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(0);
			List<?> firstCaseExpression = caseStatement.expressions();
			GuardedPattern guardedPattern = (GuardedPattern) firstCaseExpression.get(0);
//			InfixExpression infixExpression = ast.newInfixExpression();
//			guardedPattern.setExpression(infixExpression);
			guardedPattern.setExpression(null);
			rewrite.replace((ASTNode) caseStatement.expressions().get(0),guardedPattern, null);
//			Expression instanceOfExpression = guardedPattern.getExpression();
//			instanceOfExpression.r
			System.out.println("sasi");
			//EitherOrMultiPattern node = (EitherOrMultiPattern) switchCase.getExpression();
		}

		String newlyCreatedAST = """
					public class X {
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					    	case PushNotification _, EmailNotification _, SMSNotification(String _) when true -> System.out.println("Eclipse");
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
		assertEqualString(preview, newlyCreatedAST);
	}
}