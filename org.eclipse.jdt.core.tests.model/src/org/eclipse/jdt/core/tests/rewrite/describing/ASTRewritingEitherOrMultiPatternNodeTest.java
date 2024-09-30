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
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingEitherOrMultiPatternNodeTest extends ASTRewritingTest {

	static {
		//TESTS_NAMES = new String[] {"test0005"};
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
		this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_22);
		this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_22);
		this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_22);
	}
	//@SuppressWarnings({ })
	//replacing : by ->
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
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", code, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

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
		String ASTConvertedCode = """
					public class X {
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					    	case PushNotification _, EmailNotification _, SMSNotification(String _) when notification instanceof Object -> System.out.println("Eclipse");
					    	default -> System.out.println("Unknown notification type");
					    }
					  }
					}
				""";
		assertEqualString(preview, ASTConvertedCode);
	}

	//removing guard
	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String code = """
				public class X {
				  public static void main(String[] args) {
				    Object notification = "Email notification";
				    switch (notification) {
				    	case PushNotification _, EmailNotification _, SMSNotification(String _) when notification instanceof Object -> System.out.println("Eclipse");
				    	default -> System.out.println("Unknown notification type");
				    }
				  }
				}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", code, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		{
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
			List<?> statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(0);
			List<Expression> firstCaseExpression = caseStatement.expressions();
			for (Expression expression : firstCaseExpression) {
				if (expression instanceof GuardedPattern) {
					GuardedPattern guardedPattern = (GuardedPattern) expression;
					EitherOrMultiPattern newPattern = (EitherOrMultiPattern) guardedPattern.getPattern();
					rewrite.replace(guardedPattern, newPattern, null);
				}
			}
		}
		String preview= evaluateRewrite(cu, rewrite);
		String ASTConvertedCode = """
				public class X {
				  public static void main(String[] args) {
				    Object notification = "Email notification";
				    switch (notification) {
				    	case PushNotification _, EmailNotification _, SMSNotification(String _) -> System.out.println("Eclipse");
				    	default -> System.out.println("Unknown notification type");
				    }
				  }
				}
			""";
		assertEqualString(preview, ASTConvertedCode);
	}
	//generated the entire AST from scratch
	public void test0003() throws Exception {
		AST ast = AST.newAST(AST.JLS22, true);
        CompilationUnit compilationUnit = ast.newCompilationUnit();
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newName("test1"));
        compilationUnit.setPackage(packageDeclaration);

        /*-------- Class X START----------*/
        // Create ClassDeclaration - class X
        TypeDeclaration classDeclaration = ast.newTypeDeclaration();
        classDeclaration.setName(ast.newSimpleName("X"));
        classDeclaration.setInterface(false);
        classDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

        MethodDeclaration mainMethod = ast.newMethodDeclaration();
        mainMethod.setName(ast.newSimpleName("main"));
        mainMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        mainMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        mainMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

        //main param
        SingleVariableDeclaration argvParameter = ast.newSingleVariableDeclaration();
        argvParameter.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String"))));
        argvParameter.setName(ast.newSimpleName("argv"));
        mainMethod.parameters().add(argvParameter);
        classDeclaration.bodyDeclarations().add(mainMethod);

        Block mainblock = ast.newBlock();
        VariableDeclarationFragment mainDeclarationfragment = ast.newVariableDeclarationFragment();
        mainDeclarationfragment.setName(ast.newSimpleName("notification"));

        VariableDeclarationStatement mainDeclarationStatement = ast.newVariableDeclarationStatement(mainDeclarationfragment);
        mainDeclarationStatement.setType(ast.newSimpleType(ast.newSimpleName("Object")));

        SwitchStatement switchStatement = ast.newSwitchStatement();
        switchStatement.setExpression(ast.newSimpleName("notification"));


        SwitchCase firstCase = ast.newSwitchCase();
        GuardedPattern guardedPattern = ast.newGuardedPattern();

        //GuardedPattern Expression
        InstanceofExpression instanceOfExpression = ast.newInstanceofExpression();
        instanceOfExpression.setLeftOperand(ast.newSimpleName("notification"));
        instanceOfExpression.setRightOperand(ast.newSimpleType(ast.newSimpleName("Object")));

        //guardedPattern Pattern
        EitherOrMultiPattern eitherOrMultiPattern = ast.newEitherOrMultiPattern();

        //TypePattern1
        TypePattern typePattern1 = ast.newTypePattern();
        SingleVariableDeclaration variableDeclarationPattern1 = ast.newSingleVariableDeclaration();
        variableDeclarationPattern1.setType(ast.newSimpleType(ast.newSimpleName("PushNotification")));
        variableDeclarationPattern1.setName(ast.newSimpleName("_"));
        typePattern1.setPatternVariable(variableDeclarationPattern1);

        //TypePattern2
        TypePattern typePattern2 = ast.newTypePattern();
        SingleVariableDeclaration variableDeclarationPattern2 = ast.newSingleVariableDeclaration();
        variableDeclarationPattern2.setType(ast.newSimpleType(ast.newSimpleName("EmailNotification")));
        variableDeclarationPattern2.setName(ast.newSimpleName("_"));
        typePattern2.setPatternVariable(variableDeclarationPattern2);

        //RecordPattern
        RecordPattern recordPattern = ast.newRecordPattern();
        TypePattern patterns = ast.newTypePattern();
        SingleVariableDeclaration RecordVariableDeclaration = ast.newSingleVariableDeclaration();
        RecordVariableDeclaration.setType(ast.newSimpleType(ast.newName("String")));
        RecordVariableDeclaration.setName(ast.newSimpleName("_"));
        patterns.setPatternVariable(RecordVariableDeclaration);

        recordPattern.setPatternType(ast.newSimpleType(ast.newSimpleName("SMSNotification")));
        recordPattern.patterns().add(patterns);

        eitherOrMultiPattern.patterns().add(typePattern1);
        eitherOrMultiPattern.patterns().add(typePattern2);
        eitherOrMultiPattern.patterns().add(recordPattern);

        guardedPattern.setExpression(instanceOfExpression);
        guardedPattern.setPattern(eitherOrMultiPattern);

        firstCase.expressions().add(guardedPattern);

        MethodInvocation methodInvocation = ast.newMethodInvocation();
        QualifiedName qualifiedName = ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out"));
        methodInvocation.setExpression(qualifiedName);
        methodInvocation.setName(ast.newSimpleName("println"));
        StringLiteral stringLiteral1 = ast.newStringLiteral();
		stringLiteral1.setLiteralValue("Eclipse");
        methodInvocation.arguments().add(stringLiteral1);
        ExpressionStatement expressionStatement1 = ast.newExpressionStatement(methodInvocation);

        BreakStatement breakStatement1 = ast.newBreakStatement();

        switchStatement.statements().add(firstCase);
        switchStatement.statements().add(expressionStatement1);
        switchStatement.statements().add(breakStatement1);

        SingleVariableDeclaration secondCaseVariableDec = ast.newSingleVariableDeclaration();
        secondCaseVariableDec.setName(ast.newSimpleName("email"));
        secondCaseVariableDec.setType(ast.newSimpleType(ast.newName("EmailNotification")));
        TypePattern secondCaseTypePattern = ast.newTypePattern();
        secondCaseTypePattern.setPatternVariable(secondCaseVariableDec);


        MethodInvocation methodInvocation2 = ast.newMethodInvocation();
        QualifiedName qualifiedName2 = ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out"));
        methodInvocation2.setExpression(qualifiedName2);
        methodInvocation2.setName(ast.newSimpleName("println"));
        StringLiteral stringLiteral2 = ast.newStringLiteral();
		stringLiteral2.setLiteralValue("Sending Email notification");
        methodInvocation2.arguments().add(stringLiteral2);//Email notification

        ExpressionStatement expressionStatement2 = ast.newExpressionStatement(methodInvocation2);
        BreakStatement breakStatement2 = ast.newBreakStatement();

        SwitchCase secondCase = ast.newSwitchCase();
        secondCase.expressions().add(secondCaseTypePattern);
        switchStatement.statements().add(secondCase);
        switchStatement.statements().add(expressionStatement2);
        switchStatement.statements().add(breakStatement2);

        SwitchCase defaultCaseStatement = ast.newSwitchCase();

		MethodInvocation methodInvocation3 = ast.newMethodInvocation();
        QualifiedName qualifiedName3 = ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out"));
        methodInvocation3.setExpression(qualifiedName3);
        methodInvocation3.setName(ast.newSimpleName("println"));
        StringLiteral stringLiteral3 = ast.newStringLiteral();
		stringLiteral3.setLiteralValue("Unknown notification type");
        methodInvocation3.arguments().add(stringLiteral3);//Email notification

        ExpressionStatement expressionStatement3 = ast.newExpressionStatement(methodInvocation3);
        BreakStatement breakStatement3 = ast.newBreakStatement();

        switchStatement.statements().add(defaultCaseStatement);
        switchStatement.statements().add(expressionStatement3);
        switchStatement.statements().add(breakStatement3);


        mainblock.statements().add(mainDeclarationStatement);
        mainblock.statements().add(switchStatement);

        mainMethod.setBody(mainblock);


        /*-----------Class X Stop -----------------------------*/

        /*  -----------PushNotification START-----------------*/
        //Create ClassDeclaration - PushNotification class
        TypeDeclaration classDeclarationPush = ast.newTypeDeclaration();
        classDeclarationPush.setName(ast.newSimpleName("PushNotification"));
        classDeclarationPush.setInterface(false);

        //Field declaration
        SimpleType pMessageType = ast.newSimpleType(ast.newSimpleName("String"));
        VariableDeclarationFragment pMessageFragment = ast.newVariableDeclarationFragment();
        pMessageFragment.setName(ast.newSimpleName("pMessage"));
        FieldDeclaration pMessage = ast.newFieldDeclaration(pMessageFragment);
        pMessage.setType(pMessageType);

        //Constructor
        MethodDeclaration pushNotificationConstructor = ast.newMethodDeclaration();
        pushNotificationConstructor.setName(ast.newSimpleName("PushNotification"));
        pushNotificationConstructor.setConstructor(true);

        //Constructor Param
        SingleVariableDeclaration pushConstructorParam= ast.newSingleVariableDeclaration();
        pushConstructorParam.setType(ast.newSimpleType(ast.newSimpleName("String")));
        pushConstructorParam.setName(ast.newSimpleName("message"));
        pushNotificationConstructor.parameters().add(pushConstructorParam);

        //Constructor Body
        Block pushConstructorBlock= ast.newBlock();
        FieldAccess pushfieldAccess = ast.newFieldAccess();
        pushfieldAccess.setExpression(ast.newThisExpression());
        pushfieldAccess.setName(ast.newSimpleName("pMessage"));

        Assignment pushAssignment = ast.newAssignment();
        pushAssignment.setLeftHandSide(pushfieldAccess);
        pushAssignment.setRightHandSide(ast.newSimpleName("message"));

        ExpressionStatement pushConstructorExpression = ast.newExpressionStatement(pushAssignment);
        pushConstructorBlock.statements().add(pushConstructorExpression);

        //add Push Constructor body to constructor
        pushNotificationConstructor.setBody(pushConstructorBlock);

        //add field and constructor to class
        classDeclarationPush.bodyDeclarations().add(pMessage);
        classDeclarationPush.bodyDeclarations().add(pushNotificationConstructor);
        /*  -----------PushNotification END-----------------*/

        /*  -----------EmailNotification START-----------------*/
        //Create ClassDeclatation - EmailNotification class
        TypeDeclaration classDeclarationEmail = ast.newTypeDeclaration();
        classDeclarationEmail.setName(ast.newSimpleName("EmailNotification"));
        classDeclarationEmail.setInterface(false);

        //Field declaration
        SimpleType eMessageType = ast.newSimpleType(ast.newSimpleName("String"));
        VariableDeclarationFragment eMessageFragment = ast.newVariableDeclarationFragment();
        eMessageFragment.setName(ast.newSimpleName("eMessage"));
        FieldDeclaration eMessage = ast.newFieldDeclaration(eMessageFragment);
        eMessage.setType(eMessageType);

        //Constructor
        MethodDeclaration emailNotificationConstructor = ast.newMethodDeclaration();
        emailNotificationConstructor.setName(ast.newSimpleName("EmailNotification"));
        emailNotificationConstructor.setConstructor(true);

        //Constructor Param
        SingleVariableDeclaration emailConstructorParam= ast.newSingleVariableDeclaration();
        emailConstructorParam.setType(ast.newSimpleType(ast.newSimpleName("String")));
        emailConstructorParam.setName(ast.newSimpleName("message"));
        emailNotificationConstructor.parameters().add(emailConstructorParam);

        //Constructor Body
        Block emailConstructorBlock= ast.newBlock();
        FieldAccess emailfieldAccess = ast.newFieldAccess();
        emailfieldAccess.setExpression(ast.newThisExpression());
        emailfieldAccess.setName(ast.newSimpleName("eMessage"));

        Assignment emailAssignment = ast.newAssignment();
        emailAssignment.setLeftHandSide(emailfieldAccess);
        emailAssignment.setRightHandSide(ast.newSimpleName("eMessage"));

        ExpressionStatement emailConstructorExpression = ast.newExpressionStatement(emailAssignment);
        emailConstructorBlock.statements().add(emailConstructorExpression);

        //add Push Constructor body to constructor
        emailNotificationConstructor.setBody(emailConstructorBlock);

        //add field and constructor to class
        classDeclarationEmail.bodyDeclarations().add(eMessage);
        classDeclarationEmail.bodyDeclarations().add(emailNotificationConstructor);

        /*  -----------EmailNotification END-----------------*/

        //Create recordDeclaration - SMSNotification record
        RecordDeclaration recordDeclarationSMS = ast.newRecordDeclaration();
        recordDeclarationSMS.setName(ast.newSimpleName("SMSNotification"));
        SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
        newParam.setType(ast.newSimpleType(ast.newSimpleName("Object")));
		newParam.setName(ast.newSimpleName("smsMessage"));
		recordDeclarationSMS.recordComponents().add(newParam);

		compilationUnit.types().add(classDeclaration);
		compilationUnit.types().add(classDeclarationPush);
		compilationUnit.types().add(classDeclarationEmail);
		compilationUnit.types().add(recordDeclarationSMS);
		String code = """
				package test1;
				public class X {
				  public static void main(  String[] argv){
				    Object notification;
				switch (notification) {
				case       PushNotification _,       EmailNotification _, SMSNotification(      String _) when notification instanceof Object:      System.out.println("Eclipse");
				    break;
				case   EmailNotification email:  System.out.println("Sending Email notification");
				break;
				default:System.out.println("Unknown notification type");
				break;
				}
				}
				}
				class PushNotification {
				String pMessage;
				PushNotification(String message){
				this.pMessage=message;
				}
				}
				class EmailNotification {
				String eMessage;
				EmailNotification(String message){
				this.eMessage=eMessage;
				}
				}
				record SMSNotification  (Object smsMessage){}
				""";
		assertEqualString(compilationUnit.toString(), code);
	}
	/*
	 * Following test cases will be handled in this test
	 * Insert TypePattern at the beginning
	 * Insert two TypePatterns at the beginning
	 * Insert non-consecutive nodes
	 */
	public void test0004_a() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 3", patterns.size() == 3);
		//insert TypePattern at the beginning
		{
			TypePattern typePattern = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern.setType(ast.newSimpleType(ast.newSimpleName("PagerNotification")));
	        variableDeclarationPattern.setName(ast.newSimpleName("_"));
	        typePattern.setPatternVariable(variableDeclarationPattern);

	        ListRewrite listRewrite= rewrite.getListRewrite(eitherOrMultiPattern, EitherOrMultiPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(typePattern, 0, null);

		}
		String typePatternAtBeginning = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PagerNotification _, PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, typePatternAtBeginning);

		//Insert multiple TypePatterns at the beginning
		{
			TypePattern typePattern1 = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern1 = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern1.setType(ast.newSimpleType(ast.newSimpleName("PagerNotification1")));
	        variableDeclarationPattern1.setName(ast.newSimpleName("_"));
	        typePattern1.setPatternVariable(variableDeclarationPattern1);

	        TypePattern typePattern2 = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern2 = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern2.setType(ast.newSimpleType(ast.newSimpleName("SampleNotification")));
	        variableDeclarationPattern2.setName(ast.newSimpleName("_"));
	        typePattern2.setPatternVariable(variableDeclarationPattern2);

	        ListRewrite listRewrite= rewrite.getListRewrite(eitherOrMultiPattern, EitherOrMultiPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(typePattern1, 0, null);
			listRewrite.insertAt(typePattern2, 0, null);
		}
		String typePatternMultipleAtBeginning = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, PagerNotification _, PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, typePatternMultipleAtBeginning);

		//Insert non-consecutive nodes
		{
			TypePattern typePattern1 = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern1 = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern1.setType(ast.newSimpleType(ast.newSimpleName("XNotification1")));
	        variableDeclarationPattern1.setName(ast.newSimpleName("_"));
	        typePattern1.setPatternVariable(variableDeclarationPattern1);

	        TypePattern typePattern2 = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern2 = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern2.setType(ast.newSimpleType(ast.newSimpleName("YNotification")));
	        variableDeclarationPattern2.setName(ast.newSimpleName("_"));
	        typePattern2.setPatternVariable(variableDeclarationPattern2);

	        ListRewrite listRewrite= rewrite.getListRewrite(eitherOrMultiPattern, EitherOrMultiPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(typePattern1, 2, null);
			listRewrite.insertAt(typePattern2, 4, null);
		}

		String typePatternInsertNonConsicutive = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, XNotification1 _, PagerNotification _, YNotification _, PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, typePatternInsertNonConsicutive);
	}
	/*
	 * Following test cases will be handled in this test
	 * Insert RecordPattern middle
	 * Insert multiple RecrodPattern
	 * Insert TypePattern and RecordPattern
	 * Insert multiple RecordPattern at the end
	 */
	public void test0004_b() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 3", patterns.size() == 3);
		//add one RecordPattern
		{
			RecordPattern recordPattern = ast.newRecordPattern();
	        TypePattern typePatterns = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration.setName(ast.newSimpleName("_"));
	        typePatterns.setPatternVariable(RecordVariableDeclaration);

	        recordPattern.setPatternType(ast.newSimpleType(ast.newSimpleName("ANotification")));
	        recordPattern.patterns().add(typePatterns);

	        ListRewrite listRewrite= rewrite.getListRewrite(eitherOrMultiPattern, EitherOrMultiPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(recordPattern, 3, null);
		}
		String singleRecordPatternCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _), ANotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, singleRecordPatternCode);
		//add multiple RecordPattern
		{
			RecordPattern recordPattern1 = ast.newRecordPattern();
	        TypePattern typePatterns1 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration1 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration1.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration1.setName(ast.newSimpleName("_"));
	        typePatterns1.setPatternVariable(RecordVariableDeclaration1);
	        recordPattern1.setPatternType(ast.newSimpleType(ast.newSimpleName("BNotification")));
	        recordPattern1.patterns().add(typePatterns1);


	        RecordPattern recordPattern2 = ast.newRecordPattern();
	        TypePattern typePatterns2 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration2 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration2.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration2.setName(ast.newSimpleName("_"));
	        typePatterns2.setPatternVariable(RecordVariableDeclaration2);
	        recordPattern2.setPatternType(ast.newSimpleType(ast.newSimpleName("CNotification")));
	        recordPattern2.patterns().add(typePatterns2);

	        ListRewrite listRewrite= rewrite.getListRewrite(eitherOrMultiPattern, EitherOrMultiPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(recordPattern1, 4, null);
			listRewrite.insertAt(recordPattern2, 5, null);
		}

		String multipleRecordPatternCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _), ANotification(String _), BNotification(String _), CNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, multipleRecordPatternCode);

		//Insert TypePattern and RecordPattern
		{
			TypePattern typePattern = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern.setType(ast.newSimpleType(ast.newSimpleName("YNotification1")));
	        variableDeclarationPattern.setName(ast.newSimpleName("_"));
	        typePattern.setPatternVariable(variableDeclarationPattern);

			RecordPattern recordPattern = ast.newRecordPattern();
	        TypePattern typePatterns = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration.setName(ast.newSimpleName("_"));
	        typePatterns.setPatternVariable(RecordVariableDeclaration);
	        recordPattern.setPatternType(ast.newSimpleType(ast.newSimpleName("ZNotification")));
	        recordPattern.patterns().add(typePatterns);

	        ListRewrite listRewrite= rewrite.getListRewrite(eitherOrMultiPattern, EitherOrMultiPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(typePattern, 4, null);
			listRewrite.insertAt(recordPattern, 5, null);
		}

		String typeAndRecordPatterns = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _), ANotification(String _), YNotification1 _, ZNotification(String _), BNotification(String _), CNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, typeAndRecordPatterns);

	}

	/*
	 * Following test cases will be handled in this test
	 * Delete one TypePattern at the end
	 * Delete one TypePatterns at the beginning
	 */
	public void test0005_a() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _), ANotification(String _), YNotification1 _, ZNotification(String _), BNotification(String _), CNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 8", patterns.size() == 8);
		//remove one TypePattern at the end
		{
			rewrite.remove(patterns.get(7), null);
		}
		String TypePatternDeleteAtEnd = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _), ANotification(String _), YNotification1 _, ZNotification(String _), BNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, TypePatternDeleteAtEnd);
		//remove one TypePattern at the beginning
		{
			rewrite.remove(patterns.get(0), null);
		}

		String TypePatternDeleteAtBeginning = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case EmailNotification _, SMSNotification(String _), ANotification(String _), YNotification1 _, ZNotification(String _), BNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, TypePatternDeleteAtBeginning);
	}
	/*
	 * Following test cases will be handled in this test
	 * Delete multiple(3) TypePatterns at the beginning
	 */
	public void test0005_b() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, PagerNotification _, PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		//remove multiple TypePattern at the beginning
		{
			rewrite.remove(patterns.get(0), null);
			rewrite.remove(patterns.get(1), null);
			rewrite.remove(patterns.get(2), null);
		}
		String TypePatternDeleteAtBeginning = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, TypePatternDeleteAtBeginning);
	}
	/*
	 * Delete multiple(2) TypePatterns at the end
	 */
	public void test0005_c() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, PagerNotification _, PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			rewrite.remove(patterns.get(4), null);
			rewrite.remove(patterns.get(5), null);
		}
		String TypePatternDeleteAtTheEnd = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, PagerNotification _, PushNotification _:
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";

		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, TypePatternDeleteAtTheEnd);
	}
	/*
	 * Delete a RecordPattern
	 */
	public void test0005_d() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			rewrite.remove(patterns.get(1), null);
		}
		String recordPatternDelete = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, recordPatternDelete);
	}
	/*
	 * Delete multiple RecordPatterns
	 */
	public void test0005_e() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			rewrite.remove(patterns.get(1), null);
			rewrite.remove(patterns.get(2), null);
		}
		String recordPatternDelete = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, recordPatternDelete);
	}
	/*
	 * delete a combination of type patterns and record patterns
	 */
	public void test0005_f() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			rewrite.remove(patterns.get(2), null);
			rewrite.remove(patterns.get(3), null);
		}
		String recordPatternDelete = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, recordPatternDelete);
	}
	/*
	 * Delete non-consecutive nodes
	 */
	public void test0005_g() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			rewrite.remove(patterns.get(2), null);
			rewrite.remove(patterns.get(4), null);
		}
		String recordPatternDelete = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PushNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, recordPatternDelete);
	}
	/*
	 * Delete a node in the middle
	 */
	public void test0005_h() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			rewrite.remove(patterns.get(2), null);
		}
		String recordPatternDelete = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, recordPatternDelete);
	}
	/*
	 * Following test cases will be handled in this test
	 * Replace node at beginning
	 * Replace node at middle
	 * Replace node at end
	 * Replace multiple nodes at beginning
	 * Replace a multiple nodes at middle(one TypePattern and one RecordPattern
	 * Replace a multiple nodes at end
	 */
	public void test0006_a() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();

		SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(1);
		List<?> statements= switchStatement.statements();
		assertTrue("Number of statements not 6", statements.size() == 6);
		SwitchCase firstCase = (SwitchCase) statements.get(0);
		List<Expression> firstExpressions = (firstCase.expressions());
		assertTrue("Number of Expressions not 1", firstExpressions.size() == 1);
		EitherOrMultiPattern eitherOrMultiPattern = (EitherOrMultiPattern) firstExpressions.get(0);
		List<Pattern> patterns = eitherOrMultiPattern.patterns();
		assertTrue("Number of patterns not 6", patterns.size() == 6);
		{
			TypePattern typePattern = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern.setType(ast.newSimpleType(ast.newSimpleName("SampleNotification")));
	        variableDeclarationPattern.setName(ast.newSimpleName("_"));
	        typePattern.setPatternVariable(variableDeclarationPattern);

	        rewrite.replace(patterns.get(0),typePattern, null);
		}
		String replaceFirstCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, replaceFirstCode);

		{
			TypePattern typePattern = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern.setType(ast.newSimpleType(ast.newSimpleName("PagerNotification1")));
	        variableDeclarationPattern.setName(ast.newSimpleName("_"));
	        typePattern.setPatternVariable(variableDeclarationPattern);

	        rewrite.replace(patterns.get(1),typePattern, null);
		}
		String replaceMiddleCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, replaceMiddleCode);

		//replace node at the end
		{
			TypePattern typePattern = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern.setType(ast.newSimpleType(ast.newSimpleName("SMSNotification")));
	        variableDeclarationPattern.setName(ast.newSimpleName("_"));
	        typePattern.setPatternVariable(variableDeclarationPattern);

	        rewrite.replace(patterns.get(5),typePattern, null);
		}

		String replaceEndCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification _, PagerNotification1 _, PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification _:
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, replaceEndCode);
		//Replace multiple nodes at beginning
		{
			RecordPattern recordPattern1 = ast.newRecordPattern();
	        TypePattern typePatterns1 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration1 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration1.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration1.setName(ast.newSimpleName("_"));
	        typePatterns1.setPatternVariable(RecordVariableDeclaration1);
	        recordPattern1.setPatternType(ast.newSimpleType(ast.newSimpleName("SampleNotification")));
	        recordPattern1.patterns().add(typePatterns1);

	        RecordPattern recordPattern2 = ast.newRecordPattern();
	        TypePattern typePatterns2 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration2 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration2.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration2.setName(ast.newSimpleName("_"));
	        typePatterns2.setPatternVariable(RecordVariableDeclaration2);

	        recordPattern2.setPatternType(ast.newSimpleType(ast.newSimpleName("PagerNotification1")));
	        recordPattern2.patterns().add(typePatterns2);

	        rewrite.replace(patterns.get(0),recordPattern1, null);
	        rewrite.replace(patterns.get(1),recordPattern2, null);
		}

		String replaceMultipleNodesBeginningCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), PagerNotification(String _), PushNotification _, EmailNotification _, SMSNotification _:
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, replaceMultipleNodesBeginningCode);
		//Replace a multiple nodes at middle
		{
			TypePattern typePattern1 = ast.newTypePattern();
	        SingleVariableDeclaration variableDeclarationPattern1 = ast.newSingleVariableDeclaration();
	        variableDeclarationPattern1.setType(ast.newSimpleType(ast.newSimpleName("XNotification")));
	        variableDeclarationPattern1.setName(ast.newSimpleName("_"));
	        typePattern1.setPatternVariable(variableDeclarationPattern1);


			RecordPattern recordPattern2 = ast.newRecordPattern();
	        TypePattern typePatterns2 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration2 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration2.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration2.setName(ast.newSimpleName("_"));
	        typePatterns2.setPatternVariable(RecordVariableDeclaration2);

	        recordPattern2.setPatternType(ast.newSimpleType(ast.newSimpleName("YNotification1")));
	        recordPattern2.patterns().add(typePatterns2);

	        rewrite.replace(patterns.get(2),typePattern1, null);
	        rewrite.replace(patterns.get(3),recordPattern2, null);
		}

		String replaceMultipleNodesMiddleCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), XNotification _, YNotification1(String _), EmailNotification _, SMSNotification _:
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, replaceMultipleNodesMiddleCode);
		//Replace a multiple nodes at end
		{
			RecordPattern recordPattern1 = ast.newRecordPattern();
	        TypePattern typePatterns1 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration1 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration1.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration1.setName(ast.newSimpleName("_"));
	        typePatterns1.setPatternVariable(RecordVariableDeclaration1);

	        recordPattern1.setPatternType(ast.newSimpleType(ast.newSimpleName("EmailNotification")));
	        recordPattern1.patterns().add(typePatterns1);

	        RecordPattern recordPattern2 = ast.newRecordPattern();
	        TypePattern typePatterns2 = ast.newTypePattern();
	        SingleVariableDeclaration RecordVariableDeclaration2 = ast.newSingleVariableDeclaration();
	        RecordVariableDeclaration2.setType(ast.newSimpleType(ast.newName("String")));
	        RecordVariableDeclaration2.setName(ast.newSimpleName("_"));
	        typePatterns2.setPatternVariable(RecordVariableDeclaration2);

	        recordPattern2.setPatternType(ast.newSimpleType(ast.newSimpleName("SMSNotification")));
	        recordPattern2.patterns().add(typePatterns2);

	        rewrite.replace(patterns.get(4),recordPattern1, null);
	        rewrite.replace(patterns.get(5),recordPattern2, null);
		}
		String replaceMultipleNodesEndCode = """
				package x;
				public class X{
					  public static void main(String[] args) {
					    Object notification = "Email notification";
					    switch (notification) {
					      case SampleNotification(String _), PagerNotification1(String _), XNotification _, YNotification1(String _), EmailNotification(String _), SMSNotification(String _):
							System.out.println("Eclipse");
					    	  break;
					      default:
					    	  System.out.println("Unknown notification type");
					    	  break;
					    }
					  }
					}
				""";
		preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, replaceMultipleNodesEndCode);
	}

	//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2623
	public void test0007_a() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X {
					public static void main(Object object) {
						if (object instanceof Path(Pos(int x1, int y1),Pos _)) {
								System.out.printf("object is a path starting at x = %d, y = %d%n", x1, y1);
						}
					}
				}
				record Pos(int x, int y) {}
				record Path(Pos p1, Pos p2) {}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast = AST.newAST(AST.JLS22, true);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();
		IfStatement ifStatement = (IfStatement) blockStatements.get(0);
		PatternInstanceofExpression expression = (PatternInstanceofExpression) ifStatement.getExpression();
		RecordPattern recordPattern = (RecordPattern) expression.getPattern();
		List<?> patterns = recordPattern.patterns();
		TypePattern typePattern = (TypePattern) patterns.get(1);
		{
			VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
			vdf.setName(ast.newSimpleName("_"));
			TypePattern newTypePattern = ast.newTypePattern();
			newTypePattern.setPatternVariable(vdf);
			rewrite.replace(typePattern, newTypePattern, null);
		}
		String ASTConvertedCode = """
				package x;
				public class X {
					public static void main(Object object) {
						if (object instanceof Path(Pos(int x1, int y1),_)) {
								System.out.printf("object is a path starting at x = %d, y = %d%n", x1, y1);
						}
					}
				}
				record Pos(int x, int y) {}
				record Path(Pos p1, Pos p2) {}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, ASTConvertedCode);

	}

	//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2623
	public void test0007_b() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String baseCode = """
				package x;
				public class X {
					public static void main(Object object) {
						if (object instanceof Path(Pos(int x1, int y1), _)) {
								System.out.printf("object is a path starting at x = %d, y = %d%n", x1, y1);
						}
					}
				}
				record Pos(int x, int y) {}
				record Path(Pos p1, Pos p2) {}
				""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", baseCode, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast = AST.newAST(AST.JLS22, true);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "main");
		Block block= methodDecl.getBody();
		List<?> blockStatements= block.statements();
		IfStatement ifStatement = (IfStatement) blockStatements.get(0);
		PatternInstanceofExpression expression = (PatternInstanceofExpression) ifStatement.getExpression();
		RecordPattern recordPattern = (RecordPattern) expression.getPattern();
		List<?> patterns = recordPattern.patterns();
		TypePattern typePattern = (TypePattern) patterns.get(1);
		{
			SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
			svd.setName(ast.newSimpleName("_"));
			svd.setType(ast.newSimpleType(ast.newSimpleName("Pos")));


			TypePattern newTypePattern = ast.newTypePattern();
			newTypePattern.setPatternVariable(svd);
			newTypePattern.setSourceRange(typePattern.getStartPosition(), 1);
			rewrite.replace(typePattern, newTypePattern, null);
		}
		String ASTConvertedCode = """
				package x;
				public class X {
					public static void main(Object object) {
						if (object instanceof Path(Pos(int x1, int y1), Pos _)) {
								System.out.printf("object is a path starting at x = %d, y = %d%n", x1, y1);
						}
					}
				}
				record Pos(int x, int y) {}
				record Path(Pos p1, Pos p2) {}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, ASTConvertedCode);

	}
}