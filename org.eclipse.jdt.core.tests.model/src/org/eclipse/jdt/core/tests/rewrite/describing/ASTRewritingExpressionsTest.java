/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingExpressionsTest extends ASTRewritingTest {
	private static final Class THIS= ASTRewritingExpressionsTest.class;

	public ASTRewritingExpressionsTest(String name) {
		super(name);
	}
	public ASTRewritingExpressionsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return createSuite(ASTRewritingExpressionsTest.class);
	}

	public void testArrayAccess() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    int[] o= new int[] { 1, 2, 3 };\n");
		buf.append("    public void foo() {\n");
		buf.append("        o[3 /* comment*/ - 1]= this.o[3 - 1];\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{	// replace left hand side index, replace right hand side index by left side index
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			ArrayAccess left= (ArrayAccess) assignment.getLeftHandSide();
			ArrayAccess right= (ArrayAccess) assignment.getRightHandSide();

			NumberLiteral name= ast.newNumberLiteral("1");
			rewrite.replace(left.getIndex(), name, null);

			ASTNode placeHolder= rewrite.createCopyTarget(left.getIndex());
			rewrite.replace(right.getIndex(), placeHolder, null);

			SimpleName newName= ast.newSimpleName("o");
			rewrite.replace(right.getArray(), newName, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    int[] o= new int[] { 1, 2, 3 };\n");
		buf.append("    public void foo() {\n");
		buf.append("        o[1]= o[3 /* comment*/ - 1];\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}


	public void testArrayCreation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new int[] { 1, 2, 3 },\n");
		buf.append("        new int[] { 1, 2, 3 },\n");
		buf.append("        new int[2][][],\n");
		buf.append("        new int[2][][],\n");
		buf.append("        new int[2][][],\n");
		buf.append("        new int[2][][]);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		ExpressionStatement statement= (ExpressionStatement) statements.get(0);
		MethodInvocation invocation= (MethodInvocation) statement.getExpression();
		List args= invocation.arguments();
		assertTrue("Number of arguments not 6", args.size() == 6);

		{	// replace the element type and increase the dimension
			ArrayCreation arrayCreation= (ArrayCreation) args.get(0);
			ArrayType arrayType= arrayCreation.getType();

			PrimitiveType floatType= ast.newPrimitiveType(PrimitiveType.FLOAT);
			ArrayType newArrayType= ast.newArrayType(floatType, 2);

			rewrite.replace(arrayType, newArrayType, null);
		}
		{	// remove the initializer, add a dimension expression
			ArrayCreation arrayCreation= (ArrayCreation) args.get(1);
			rewrite.remove(arrayCreation.getInitializer(), null);

			List dimensions= arrayCreation.dimensions();
			assertTrue("Number of dimension expressions not 0", dimensions.size() == 0);

			NumberLiteral literal= ast.newNumberLiteral("10");

			rewrite.getListRewrite(arrayCreation, ArrayCreation.DIMENSIONS_PROPERTY).insertLast(literal, null);
		}
		{	// remove all dimension except one, no dimension expression
			// insert the initializer: formatter problems
			ArrayCreation arrayCreation= (ArrayCreation) args.get(2);
			ArrayType arrayType= arrayCreation.getType();
			PrimitiveType intType= ast.newPrimitiveType(PrimitiveType.INT);
			ArrayType newArrayType= ast.newArrayType(intType, 1);

			rewrite.replace(arrayType, newArrayType, null);

			List dimensions= arrayCreation.dimensions();
			assertTrue("Number of dimension expressions not 1", dimensions.size() == 1);

			rewrite.remove((ASTNode) dimensions.get(0), null);

			ArrayInitializer initializer= ast.newArrayInitializer();
			List expressions= initializer.expressions();
			expressions.add(ast.newNumberLiteral("10"));
		}
		{	// add 2 dimension expressions
			ArrayCreation arrayCreation= (ArrayCreation) args.get(3);

			List dimensions= arrayCreation.dimensions();
			assertTrue("Number of dimension expressions not 1", dimensions.size() == 1);

			NumberLiteral literal1= ast.newNumberLiteral("10");
			rewrite.getListRewrite(arrayCreation, ArrayCreation.DIMENSIONS_PROPERTY).insertLast(literal1, null);

			NumberLiteral literal2= ast.newNumberLiteral("11");
			rewrite.getListRewrite(arrayCreation, ArrayCreation.DIMENSIONS_PROPERTY).insertLast(literal2, null);

		}
		{	// add 2 empty dimensions
			ArrayCreation arrayCreation= (ArrayCreation) args.get(4);
			ArrayType arrayType= arrayCreation.getType();
			assertTrue("Number of dimension not 3", arrayType.getDimensions() == 3);

			PrimitiveType intType= ast.newPrimitiveType(PrimitiveType.INT);
			ArrayType newArrayType= ast.newArrayType(intType, 5);

			rewrite.replace(arrayType, newArrayType, null);
		}
		{	// replace dimension expression, add a dimension expression
			ArrayCreation arrayCreation= (ArrayCreation) args.get(5);

			List dimensions= arrayCreation.dimensions();
			assertTrue("Number of dimension expressions not 1", dimensions.size() == 1);

			NumberLiteral literal1= ast.newNumberLiteral("10");
			rewrite.replace((ASTNode) dimensions.get(0), literal1, null);

			NumberLiteral literal2= ast.newNumberLiteral("11");
			rewrite.getListRewrite(arrayCreation, ArrayCreation.DIMENSIONS_PROPERTY).insertLast(literal2, null);

		}
		{	// add a new ArrayCreation
			ArrayCreation arrayCreation= ast.newArrayCreation();
			arrayCreation.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Object")), 3));
			arrayCreation.dimensions().add(ast.newNumberLiteral("1"));
			arrayCreation.dimensions().add(ast.newNumberLiteral("2"));

			rewrite.getListRewrite(invocation, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(arrayCreation, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new float[][] { 1, 2, 3 },\n");
		buf.append("        new int[10],\n");
		buf.append("        new int[],\n");
		buf.append("        new int[2][10][11],\n");
		buf.append("        new int[2][][][][],\n");
		buf.append("        new int[10][11][], new Object[1][2][]);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testArrayCreation2_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		this.project1.setOption(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		ExpressionStatement statement= (ExpressionStatement) statements.get(0);
		MethodInvocation invocation= (MethodInvocation) statement.getExpression();

		{	// add a new ArrayCreation with annotations
			ArrayCreation arrayCreation= ast.newArrayCreation();
			SimpleType elementType= ast.newSimpleType(ast.newName("java.lang.String"));

			ArrayType arrayType= ast.newArrayType(elementType);
			NormalAnnotation annotationC= ast.newNormalAnnotation();
			annotationC.setTypeName(ast.newSimpleName("C"));
			MemberValuePair memberValuePair= ast.newMemberValuePair();
			memberValuePair.setName(ast.newSimpleName("v"));
			memberValuePair.setValue(ast.newNumberLiteral("99"));
			annotationC.values().add(memberValuePair);
			Dimension dim0 = (Dimension) arrayType.dimensions().get(0);
			dim0.annotations().add(annotationC);

			SingleMemberAnnotation annotationB= ast.newSingleMemberAnnotation();
			annotationB.setTypeName(ast.newSimpleName("B"));
			annotationB.setValue(ast.newNumberLiteral("0"));
			Dimension dim1 = ast.newDimension();
			dim1.annotations().add(annotationB);
			arrayType.dimensions().add(0, dim1);

			MarkerAnnotation annotationA= ast.newMarkerAnnotation();
			annotationA.setTypeName(ast.newSimpleName("A"));
			Dimension dim2 = ast.newDimension();
			dim2.annotations().add(annotationA);
			arrayType.dimensions().add(0, dim2);

			arrayCreation.dimensions().add(ast.newNumberLiteral("1"));
			arrayCreation.dimensions().add(ast.newNumberLiteral("2"));
			arrayCreation.setType(arrayType);

			rewrite.getListRewrite(invocation, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(arrayCreation, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new java.lang.String @A [1] @B(0) [2] @C(v = 99) []);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	public void testArrayInitializer() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new int[] { 1, 2, 3 },\n");
		buf.append("        new int[] { 1, 2, 3 },\n");
		buf.append("        new int[] { 1, 2, 3 });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		ExpressionStatement statement= (ExpressionStatement) statements.get(0);
		MethodInvocation invocation= (MethodInvocation) statement.getExpression();
		List args= invocation.arguments();

		{	// remove first and last initializer expression
			ArrayCreation arrayCreation= (ArrayCreation) args.get(0);
			ArrayInitializer initializer= arrayCreation.getInitializer();

			List expressions= initializer.expressions();
			assertTrue("Number of initializer expressions not 3", expressions.size() == 3);

			rewrite.remove((ASTNode) expressions.get(0), null);
			rewrite.remove((ASTNode) expressions.get(2), null);
		}
		{	// insert at second and last position
			ArrayCreation arrayCreation= (ArrayCreation) args.get(1);
			ArrayInitializer initializer= arrayCreation.getInitializer();

			List expressions= initializer.expressions();
			assertTrue("Number of initializer expressions not 3", expressions.size() == 3);

			NumberLiteral literal1= ast.newNumberLiteral("10");
			rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY).insertAfter(literal1, (ASTNode) expressions.get(0), null);

			NumberLiteral literal2= ast.newNumberLiteral("11");
			rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY).insertLast(literal2, null);

		}
		{	// replace first and last initializer expression
			ArrayCreation arrayCreation= (ArrayCreation) args.get(2);
			ArrayInitializer initializer= arrayCreation.getInitializer();

			List expressions= initializer.expressions();
			assertTrue("Number of initializer expressions not 3", expressions.size() == 3);

			NumberLiteral literal1= ast.newNumberLiteral("10");
			NumberLiteral literal2= ast.newNumberLiteral("11");

			rewrite.replace((ASTNode) expressions.get(0), literal1, null);
			rewrite.replace((ASTNode) expressions.get(2), literal2, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new int[] { 2 },\n");
		buf.append("        new int[] { 1, 10, 2, 3, 11 },\n");
		buf.append("        new int[] { 10, 2, 11 });\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testArrayInitializer2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new int[] { 1, 2, 3, },\n");
		buf.append("        new int[] { 1, 2, 3, },\n");
		buf.append("        new int[] { 1, 2, 3 });\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		ExpressionStatement statement= (ExpressionStatement) statements.get(0);
		MethodInvocation invocation= (MethodInvocation) statement.getExpression();
		List args= invocation.arguments();

		{	// remove first and last initializer expression
			ArrayCreation arrayCreation= (ArrayCreation) args.get(0);
			ArrayInitializer initializer= arrayCreation.getInitializer();

			List expressions= initializer.expressions();
			assertTrue("Number of initializer expressions not 3", expressions.size() == 3);

			rewrite.remove((ASTNode) expressions.get(0), null);
			rewrite.remove((ASTNode) expressions.get(2), null);
		}
		{	// insert at second and last position
			ArrayCreation arrayCreation= (ArrayCreation) args.get(1);
			ArrayInitializer initializer= arrayCreation.getInitializer();

			List expressions= initializer.expressions();
			assertTrue("Number of initializer expressions not 3", expressions.size() == 3);

			NumberLiteral literal2= ast.newNumberLiteral("11");
			rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY).insertLast(literal2, null);

		}
		{	// replace first and last initializer expression
			ArrayCreation arrayCreation= (ArrayCreation) args.get(2);
			ArrayInitializer initializer= arrayCreation.getInitializer();

			List expressions= initializer.expressions();
			assertTrue("Number of initializer expressions not 3", expressions.size() == 3);

			NumberLiteral literal1= ast.newNumberLiteral("10");
			NumberLiteral literal2= ast.newNumberLiteral("11");

			rewrite.replace((ASTNode) expressions.get(0), literal1, null);
			rewrite.replace((ASTNode) expressions.get(2), literal2, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(new int[] { 2, },\n");
		buf.append("        new int[] { 1, 2, 3, 11, },\n");
		buf.append("        new int[] { 10, 2, 11 });\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testAssignment() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i, j;\n");
		buf.append("        i= 0;\n");
		buf.append("        i-= j= 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 3);
		{ // change left side & right side
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			Assignment assignment= (Assignment) stmt.getExpression();

			SimpleName name= ast.newSimpleName("j");
			rewrite.replace(assignment.getLeftHandSide(), name, null);

			MethodInvocation invocation= ast.newMethodInvocation();
			invocation.setName(ast.newSimpleName("goo"));
			invocation.setExpression(ast.newSimpleName("other"));

			rewrite.replace(assignment.getRightHandSide(), invocation, null);
		}
		{ // change operator and operator of inner
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			Assignment assignment= (Assignment) stmt.getExpression();

			rewrite.set(assignment, Assignment.OPERATOR_PROPERTY, Assignment.Operator.DIVIDE_ASSIGN, null);

			Assignment inner= (Assignment) assignment.getRightHandSide();

			rewrite.set(inner, Assignment.OPERATOR_PROPERTY, Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int i, j;\n");
		buf.append("        j= other.goo();\n");
		buf.append("        i/= j>>>= 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testCastExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        x= (E) clone();\n");
		buf.append("        z= y.toList();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 2", statements.size() == 2);
		{ // change cast type and cast expression
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			CastExpression expression= (CastExpression) assignment.getRightHandSide();
			SimpleType newType= ast.newSimpleType(ast.newSimpleName("SuperE"));
			rewrite.replace(expression.getType(), newType, null);

			SimpleName newExpression= ast.newSimpleName("a");
			rewrite.replace(expression.getExpression(), newExpression, null);
		}
		{ // create cast
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			Assignment assignment= (Assignment) stmt.getExpression();

			Expression rightHand= assignment.getRightHandSide();

			Expression placeholder= (Expression) rewrite.createCopyTarget(rightHand);

			CastExpression newCastExpression= ast.newCastExpression();
			newCastExpression.setType(ast.newSimpleType(ast.newSimpleName("List")));
			newCastExpression.setExpression(placeholder);

			rewrite.replace(rightHand, newCastExpression, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        x= (SuperE) a;\n");
		buf.append("        z= (List) y.toList();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testCastExpression_bug28824() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        z= foo().y.toList();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // create cast
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			Expression rightHand= assignment.getRightHandSide();

			String rightHandString= cu.getBuffer().getText(rightHand.getStartPosition(), rightHand.getLength());
			assertEqualString(rightHandString, "foo().y.toList()");

			Expression placeholder= (Expression) rewrite.createCopyTarget(rightHand);

			CastExpression newCastExpression= ast.newCastExpression();
			newCastExpression.setType(ast.newSimpleType(ast.newSimpleName("List")));
			newCastExpression.setExpression(placeholder);

			rewrite.replace(rightHand, newCastExpression, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        z= (List) foo().y.toList();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testCatchClause() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("        } catch (IOException e) {\n");
		buf.append("        } catch (CoreException e) {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 1);
		List catchClauses= ((TryStatement) statements.get(0)).catchClauses();
		assertTrue("Number of catchClauses not 2", catchClauses.size() == 2);
		{ // change exception type
			CatchClause clause= (CatchClause) catchClauses.get(0);

			SingleVariableDeclaration exception= clause.getException();

			SingleVariableDeclaration newException= ast.newSingleVariableDeclaration();

			newException.setType(ast.newSimpleType(ast.newSimpleName("NullPointerException")));
			newException.setName(ast.newSimpleName("ex"));

			rewrite.replace(exception, newException, null);
		}
		{ // change body
			CatchClause clause= (CatchClause) catchClauses.get(1);
			Block body= clause.getBody();

			Block newBody= ast.newBlock();
			ReturnStatement returnStatement= ast.newReturnStatement();
			newBody.statements().add(returnStatement);

			rewrite.replace(body, newBody, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        try {\n");
		buf.append("        } catch (NullPointerException ex) {\n");
		buf.append("        } catch (CoreException e) {\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testClassInstanceCreation2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E<A> {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new Inner();\n");
		buf.append("        new <A>Inner();\n");
		buf.append("        new<A>Inner();\n");
		buf.append("        new <A, A>Inner();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 4);
		{ // add type argument
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();

			Type newTypeArg= ast.newSimpleType(ast.newSimpleName("A"));
			ListRewrite listRewrite= rewrite.getListRewrite(creation, ClassInstanceCreation.TYPE_ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(newTypeArg, null);

		}
		{ // remove type argument
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();

			List typeArgs= creation.typeArguments();
			rewrite.remove((ASTNode) typeArgs.get(0), null);
		}
		{ // remove type argument
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();

			List typeArgs= creation.typeArguments();
			rewrite.remove((ASTNode) typeArgs.get(0), null);
		}
		{ // add type argument to existing
			ExpressionStatement stmt= (ExpressionStatement) statements.get(3);
			ClassInstanceCreation creation= (ClassInstanceCreation) stmt.getExpression();

			Type newTypeArg= ast.newSimpleType(ast.newSimpleName("String"));

			ListRewrite listRewrite= rewrite.getListRewrite(creation, ClassInstanceCreation.TYPE_ARGUMENTS_PROPERTY);
			listRewrite.insertLast(newTypeArg, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E<A> {\n");
		buf.append("    public void foo() {\n");
		buf.append("        new <A> Inner();\n");
		buf.append("        new Inner();\n");
		buf.append("        new Inner();\n");
		buf.append("        new <A, A, String>Inner();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}



	public void testConditionalExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= (k == 0) ? 1 : 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // change compare expression, then expression & else expression
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();
			ConditionalExpression condExpression= (ConditionalExpression) assignment.getRightHandSide();

			BooleanLiteral literal= ast.newBooleanLiteral(true);
			rewrite.replace(condExpression.getExpression(), literal, null);

			SimpleName newThenExpre= ast.newSimpleName("x");
			rewrite.replace(condExpression.getThenExpression(), newThenExpre, null);

			InfixExpression infixExpression= ast.newInfixExpression();
			infixExpression.setLeftOperand(ast.newNumberLiteral("1"));
			infixExpression.setRightOperand(ast.newNumberLiteral("2"));
			infixExpression.setOperator(InfixExpression.Operator.PLUS);

			rewrite.replace(condExpression.getElseExpression(), infixExpression, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= true ? x : 1 + 2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testFieldAccess() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo().i= goo().i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace field expression, replace field name
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();
			FieldAccess leftFieldAccess= (FieldAccess) assignment.getLeftHandSide();
			FieldAccess rightFieldAccess= (FieldAccess) assignment.getRightHandSide();

			MethodInvocation invocation= ast.newMethodInvocation();
			invocation.setName(ast.newSimpleName("xoo"));
			rewrite.replace(leftFieldAccess.getExpression(), invocation, null);

			SimpleName newName= ast.newSimpleName("x");
			rewrite.replace(leftFieldAccess.getName(), newName, null);

			SimpleName rightHand= ast.newSimpleName("b");
			rewrite.replace(rightFieldAccess.getExpression(), rightHand, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        xoo().x= b.i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testInfixExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= 1 + 2;\n");
		buf.append("        j= 1 + 2 + 3 + 4 + 5;\n");
		buf.append("        k= 1 + 2 + 3 + 4 + 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 3);
		{ // change left side & right side & operand
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			SimpleName leftOp= ast.newSimpleName("k");
			rewrite.replace(expr.getLeftOperand(), leftOp, null);

			SimpleName rightOp= ast.newSimpleName("j");
			rewrite.replace(expr.getRightOperand(), rightOp, null);

			// change operand
			rewrite.set(expr, InfixExpression.OPERATOR_PROPERTY, InfixExpression.Operator.MINUS, null);
		}

		{ // remove an ext. operand, add one and replace one
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			List extendedOperands= expr.extendedOperands();
			assertTrue("Number of extendedOperands not 3", extendedOperands.size() == 3);

			rewrite.remove((ASTNode) extendedOperands.get(0), null);

			SimpleName newOp1= ast.newSimpleName("k");
			rewrite.replace((ASTNode) extendedOperands.get(1), newOp1, null);

			SimpleName newOp2= ast.newSimpleName("n");
			rewrite.getListRewrite(expr, InfixExpression.EXTENDED_OPERANDS_PROPERTY).insertLast(newOp2, null);

		}

		{ // change operand
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			rewrite.set(expr, InfixExpression.OPERATOR_PROPERTY, InfixExpression.Operator.TIMES, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= k - j;\n");
		buf.append("        j= 1 + 2 + k + 5 + n;\n");
		buf.append("        k= 1 * 2 * 3 * 4 * 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testInfixExpression2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= 0 + 2;\n");
		buf.append("        j= 1 + 0;\n");
		buf.append("        k= 0 + 2 + 3 + 4 + 5;\n");
		buf.append("        l= 1 + 0 + 3 + 4 + 5;\n");
		buf.append("        m= 0 + 0 + 0 + 4 + 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		{ // remove left side
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			rewrite.remove(expr.getLeftOperand(), null);
		}

		{ // remove right side
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			rewrite.remove(expr.getRightOperand(), null);
		}

		{ // remove left side (with extended operands)
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			rewrite.remove(expr.getLeftOperand(), null);
		}

		{ // remove right side (with extended operands)
			ExpressionStatement stmt= (ExpressionStatement) statements.get(3);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			rewrite.remove(expr.getRightOperand(), null);
		}

		{ // remove left, right, and extended operand
			ExpressionStatement stmt= (ExpressionStatement) statements.get(4);
			Assignment assignment= (Assignment) stmt.getExpression();
			InfixExpression expr= (InfixExpression) assignment.getRightHandSide();

			rewrite.remove(expr.getLeftOperand(), null);
			rewrite.remove(expr.getRightOperand(), null);
			rewrite.remove((ASTNode) expr.extendedOperands().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= 2;\n");
		buf.append("        j= 1;\n");
		buf.append("        k= 2 + 3 + 4 + 5;\n");
		buf.append("        l= 1 + 3 + 4 + 5;\n");
		buf.append("        m= 4 + 5;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	/** @deprecated using deprecated code */
	public void testInstanceofExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(k instanceof Vector);\n");
		buf.append("        goo(k()instanceof Vector);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 2", statements.size() == 2);
		{ // change left side & right side
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();

			List arguments= invocation.arguments();
			InstanceofExpression expr= (InstanceofExpression) arguments.get(0);

			SimpleName name= ast.newSimpleName("x");
			rewrite.replace(expr.getLeftOperand(), name, null);

			Type newCastType= ast.newSimpleType(ast.newSimpleName("List"));

			rewrite.replace(expr.getRightOperand(), newCastType, null);
		}
		{ // change left side
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();

			List arguments= invocation.arguments();
			InstanceofExpression expr= (InstanceofExpression) arguments.get(0);

			SimpleName name= ast.newSimpleName("x");
			rewrite.replace(expr.getLeftOperand(), name, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(x instanceof List);\n");
		buf.append("        goo(x instanceof Vector);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodInvocation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo(1, 2).goo();\n");
		buf.append("        foo(1, 2).goo();\n");
		buf.append("        foo(1, 2).goo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 3);
		{ // remove expression, add param, change name
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();

			rewrite.remove(invocation.getExpression(), null);

			SimpleName name= ast.newSimpleName("x");
			rewrite.replace(invocation.getName(), name, null);

			ASTNode arg= ast.newNumberLiteral("1");
			rewrite.getListRewrite(invocation, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(arg, null);

		}
		{ // insert expression, delete params
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();

			MethodInvocation leftInvocation= (MethodInvocation) invocation.getExpression();

			SimpleName newExpression= ast.newSimpleName("x");
			rewrite.set(leftInvocation, MethodInvocation.EXPRESSION_PROPERTY, newExpression, null);

			List args= leftInvocation.arguments();
			rewrite.remove((ASTNode) args.get(0), null);
			rewrite.remove((ASTNode) args.get(1), null);
		}
		{ // remove expression, add it as parameter
			ExpressionStatement stmt= (ExpressionStatement) statements.get(2);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();

			ASTNode placeHolder= rewrite.createCopyTarget(invocation.getExpression());

			rewrite.set(invocation, MethodInvocation.EXPRESSION_PROPERTY, null, null);

			rewrite.getListRewrite(invocation, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(placeHolder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        x(1);\n");
		buf.append("        x.foo().goo();\n");
		buf.append("        goo(foo(1, 2));\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void _testMethodParamsRenameReorder() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void m(boolean y, int a) {\n");
		buf.append("        m(y, a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "m");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{
			//params
			List params= methodDecl.parameters();
			SingleVariableDeclaration firstParam= (SingleVariableDeclaration) params.get(0);
			SingleVariableDeclaration secondParam= (SingleVariableDeclaration) params.get(1);

			//args
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();
			List arguments= invocation.arguments();
			SimpleName first= (SimpleName) arguments.get(0);
			SimpleName second= (SimpleName) arguments.get(1);


			//rename args
			SimpleName newFirstArg= methodDecl.getAST().newSimpleName("yyy");
			SimpleName newSecondArg= methodDecl.getAST().newSimpleName("bb");
			rewrite.replace(first, newFirstArg, null);
			rewrite.replace(second, newSecondArg, null);


			//rename params
			SimpleName newFirstName= methodDecl.getAST().newSimpleName("yyy");
			SimpleName newSecondName= methodDecl.getAST().newSimpleName("bb");
			rewrite.replace(firstParam.getName(), newFirstName, null);
			rewrite.replace(secondParam.getName(), newSecondName, null);

			//reoder params
			ASTNode paramplaceholder1= rewrite.createCopyTarget(firstParam);
			ASTNode paramplaceholder2= rewrite.createCopyTarget(secondParam);

			rewrite.replace(firstParam, paramplaceholder2, null);
			rewrite.replace(secondParam, paramplaceholder1, null);

			//reorder args
			ASTNode placeholder1= rewrite.createCopyTarget(first);
			ASTNode placeholder2= rewrite.createCopyTarget(second);

			rewrite.replace(first, placeholder2, null);
			rewrite.replace(second, placeholder1, null);


		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void m(int bb, boolean yyy) {\n");
		buf.append("        m(bb, yyy);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodInvocation1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo(foo(1, 2), 3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // remove expression, add param, change name
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();

			List arguments= invocation.arguments();
			MethodInvocation first= (MethodInvocation) arguments.get(0);
			ASTNode second= (ASTNode) arguments.get(1);

			ASTNode placeholder1= rewrite.createCopyTarget(first);
			ASTNode placeholder2= rewrite.createCopyTarget(second);

			rewrite.replace(first, placeholder2, null);
			rewrite.replace(second, placeholder1, null);

			List innerArguments= first.arguments();
			ASTNode innerFirst= (ASTNode) innerArguments.get(0);
			ASTNode innerSecond= (ASTNode) innerArguments.get(1);

			ASTNode innerPlaceholder1= rewrite.createCopyTarget(innerFirst);
			ASTNode innerPlaceholder2= rewrite.createCopyTarget(innerSecond);

			rewrite.replace(innerFirst, innerPlaceholder2, null);
			rewrite.replace(innerSecond, innerPlaceholder1, null);



		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo(3, foo(2, 1));\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMethodInvocation2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        this.foo(3);\n");
		buf.append("        this.<String>foo(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 2", statements.size() == 2);
		{ // add type arguments
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();
			SimpleType newType= ast.newSimpleType(ast.newSimpleName("String"));
			ListRewrite listRewriter= rewrite.getListRewrite(invocation, MethodInvocation.TYPE_ARGUMENTS_PROPERTY);
			listRewriter.insertFirst(newType, null);
		}
		{ // remove type arguments
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();
			rewrite.remove((ASTNode) invocation.typeArguments().get(0), null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        this.<String>foo(3);\n");
		buf.append("        this.foo(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testParenthesizedExpression() throws Exception {
		//System.out.println(getClass().getName()+"::" + getName() +" disabled (bug 23362)");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= (1 + 2) * 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace expression
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			InfixExpression multiplication= (InfixExpression) assignment.getRightHandSide();

			ParenthesizedExpression parenthesizedExpression= (ParenthesizedExpression) multiplication.getLeftOperand();

			SimpleName name= ast.newSimpleName("x");
			rewrite.replace(parenthesizedExpression.getExpression(), name, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= (x) * 3;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());


	}

	public void testPrefixExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= --x;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // modify operand and operation
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			PrefixExpression preExpression= (PrefixExpression) assignment.getRightHandSide();

			NumberLiteral newOperation= ast.newNumberLiteral("10");
			rewrite.replace(preExpression.getOperand(), newOperation, null);

			rewrite.set(preExpression, PrefixExpression.OPERATOR_PROPERTY, PrefixExpression.Operator.COMPLEMENT, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= ~10;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testPostfixExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= x--;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // modify operand and operation
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			PostfixExpression postExpression= (PostfixExpression) assignment.getRightHandSide();

			NumberLiteral newOperation= ast.newNumberLiteral("10");
			rewrite.replace(postExpression.getOperand(), newOperation, null);

			rewrite.set(postExpression, PostfixExpression.OPERATOR_PROPERTY, PostfixExpression.Operator.INCREMENT, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i= 10++;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSuperConstructorInvocation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("    }\n");
		buf.append("    public E(int i) {\n");
		buf.append("        foo(i + i).super(i);\n");
		buf.append("    }\n");
		buf.append("    public E(int i, int k) {\n");
		buf.append("        Outer.super(foo(goo(x)), 1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List bodyDeclarations= type.bodyDeclarations();
		assertTrue("Number of bodyDeclarations not 3", bodyDeclarations.size() == 3);
		{ // add expresssion & parameter
			MethodDeclaration methodDecl= (MethodDeclaration) bodyDeclarations.get(0);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);

			SimpleName newExpression= ast.newSimpleName("x");
			rewrite.set(invocation, SuperConstructorInvocation.EXPRESSION_PROPERTY, newExpression, null);

			ASTNode arg= ast.newNumberLiteral("1");
			rewrite.getListRewrite(invocation, SuperConstructorInvocation.ARGUMENTS_PROPERTY).insertLast(arg, null);

		}
		{ // remove expression, replace argument with argument of expression
			MethodDeclaration methodDecl= (MethodDeclaration) bodyDeclarations.get(1);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);

			MethodInvocation expression= (MethodInvocation) invocation.getExpression();
			rewrite.remove(expression, null);

			ASTNode placeHolder= rewrite.createCopyTarget((ASTNode) expression.arguments().get(0));

			ASTNode arg1= (ASTNode) invocation.arguments().get(0);

			rewrite.replace(arg1, placeHolder, null);
		}
		{ // remove argument, replace expression with part of argument
			MethodDeclaration methodDecl= (MethodDeclaration) bodyDeclarations.get(2);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);

			MethodInvocation arg1= (MethodInvocation) invocation.arguments().get(0);
			rewrite.remove(arg1, null);

			ASTNode placeHolder= rewrite.createCopyTarget((ASTNode) arg1.arguments().get(0));

			rewrite.replace(invocation.getExpression(), placeHolder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        x.super(1);\n");
		buf.append("    }\n");
		buf.append("    public E(int i) {\n");
		buf.append("        super(i + i);\n");
		buf.append("    }\n");
		buf.append("    public E(int i, int k) {\n");
		buf.append("        goo(x).super(1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSuperConstructorInvocation2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        x.super();\n");
		buf.append("    }\n");
		buf.append("    public E(int i) {\n");
		buf.append("        x.<String>super(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		assertTrue("Number of methods not 2", type.bodyDeclarations().size() == 2);
		{ // add type arguments
			MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(0);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);
			SimpleType newType= ast.newSimpleType(ast.newSimpleName("String"));
			ListRewrite listRewriter= rewrite.getListRewrite(invocation, SuperConstructorInvocation.TYPE_ARGUMENTS_PROPERTY);
			listRewriter.insertFirst(newType, null);
		}
		{ // remove type arguments
			MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(1);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);

			rewrite.remove((ASTNode) invocation.typeArguments().get(0), null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        x.<String>super();\n");
		buf.append("    }\n");
		buf.append("    public E(int i) {\n");
		buf.append("        x.super(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSuperConstructorInvocation4_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        x.super();\n");
		buf.append("    }\n");
		buf.append("    public E(int i) {\n");
		buf.append("        x.<String>super(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		assertTrue("Number of methods not 2", type.bodyDeclarations().size() == 2);
		{ // add type arguments
			MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(0);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);
			rewrite.remove(invocation.getExpression(), null);
			SimpleType newType= ast.newSimpleType(ast.newSimpleName("String"));
			ListRewrite listRewriter= rewrite.getListRewrite(invocation, SuperConstructorInvocation.TYPE_ARGUMENTS_PROPERTY);
			listRewriter.insertFirst(newType, null);
		}
		{ // remove type arguments
			MethodDeclaration methodDecl= (MethodDeclaration) type.bodyDeclarations().get(1);
			SuperConstructorInvocation invocation= (SuperConstructorInvocation) methodDecl.getBody().statements().get(0);

			rewrite.remove(invocation.getExpression(), null);
			rewrite.remove((ASTNode) invocation.typeArguments().get(0), null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public E() {\n");
		buf.append("        <String>super();\n");
		buf.append("    }\n");
		buf.append("    public E(int i) {\n");
		buf.append("        super(i);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	public void testSuperFieldInvocation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        super.x= Outer.super.y;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // insert qualifier, replace field name, delete qualifier
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();
			SuperFieldAccess leftFieldAccess= (SuperFieldAccess) assignment.getLeftHandSide();
			SuperFieldAccess rightFieldAccess= (SuperFieldAccess) assignment.getRightHandSide();

			SimpleName newQualifier= ast.newSimpleName("X");
			rewrite.set(leftFieldAccess, SuperFieldAccess.QUALIFIER_PROPERTY, newQualifier, null);

			SimpleName newName= ast.newSimpleName("y");
			rewrite.replace(leftFieldAccess.getName(), newName, null);

			rewrite.remove(rightFieldAccess.getQualifier(), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        X.super.y= super.y;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}
	public void testSuperMethodInvocation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        super.foo();\n");
		buf.append("        Outer.super.foo(i);\n");
		buf.append("        Outer.super.foo(foo(X.goo()), 1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 3", statements.size() == 3);
		{ // add qualifier & parameter
			ExpressionStatement statement= (ExpressionStatement) statements.get(0);
			SuperMethodInvocation invocation= (SuperMethodInvocation) statement.getExpression();

			SimpleName newExpression= ast.newSimpleName("X");
			rewrite.set(invocation, SuperMethodInvocation.QUALIFIER_PROPERTY, newExpression, null);

			ASTNode arg= ast.newNumberLiteral("1");
			rewrite.getListRewrite(invocation, SuperMethodInvocation.ARGUMENTS_PROPERTY).insertLast(arg, null);
		}
		{ // remove qualifier, replace argument with argument of expression
			ExpressionStatement statement= (ExpressionStatement) statements.get(1);
			SuperMethodInvocation invocation= (SuperMethodInvocation) statement.getExpression();

			Name qualifier= invocation.getQualifier();
			rewrite.remove(qualifier, null);

			Name placeHolder= (Name) rewrite.createCopyTarget(qualifier);

			FieldAccess newFieldAccess= ast.newFieldAccess();
			newFieldAccess.setExpression(placeHolder);
			newFieldAccess.setName(ast.newSimpleName("count"));

			ASTNode arg1= (ASTNode) invocation.arguments().get(0);
			rewrite.replace(arg1, newFieldAccess, null);
		}
		{ // remove argument, replace qualifier with part argument qualifier
			ExpressionStatement statement= (ExpressionStatement) statements.get(2);
			SuperMethodInvocation invocation= (SuperMethodInvocation) statement.getExpression();

			MethodInvocation arg1= (MethodInvocation) invocation.arguments().get(0);
			rewrite.remove(arg1, null);

			MethodInvocation innerArg= (MethodInvocation) arg1.arguments().get(0);

			ASTNode placeHolder= rewrite.createCopyTarget(innerArg.getExpression());

			rewrite.replace(invocation.getQualifier(), placeHolder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        X.super.foo(1);\n");
		buf.append("        super.foo(Outer.count);\n");
		buf.append("        X.super.foo(1);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSuperMethodInvocation2_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        X.super.foo(3);\n");
		buf.append("        X.super.<String>foo(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 2", statements.size() == 2);
		{ // add type arguments
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			SuperMethodInvocation invocation= (SuperMethodInvocation) stmt.getExpression();
			SimpleType newType= ast.newSimpleType(ast.newSimpleName("String"));
			ListRewrite listRewriter= rewrite.getListRewrite(invocation, SuperMethodInvocation.TYPE_ARGUMENTS_PROPERTY);
			listRewriter.insertFirst(newType, null);
		}
		{ // remove type arguments
			ExpressionStatement stmt= (ExpressionStatement) statements.get(1);
			SuperMethodInvocation invocation= (SuperMethodInvocation) stmt.getExpression();
			rewrite.remove((ASTNode) invocation.typeArguments().get(0), null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        X.super.<String>foo(3);\n");
		buf.append("        X.super.foo(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testThisExpression() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        return this;\n");
		buf.append("        return Outer.this;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 2", statements.size() == 2);
		{ // add qualifier
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			ThisExpression thisExpression= (ThisExpression) returnStatement.getExpression();

			SimpleName newExpression= ast.newSimpleName("X");
			rewrite.set(thisExpression, ThisExpression.QUALIFIER_PROPERTY, newExpression, null);
		}
		{ // remove qualifier
			ReturnStatement returnStatement= (ReturnStatement) statements.get(1);

			ThisExpression thisExpression= (ThisExpression) returnStatement.getExpression();

			rewrite.remove(thisExpression.getQualifier(), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        return X.this;\n");
		buf.append("        return this;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testTypeLiteral() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        return E.class;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace type
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			TypeLiteral typeLiteral= (TypeLiteral) returnStatement.getExpression();

			Type newType= ast.newPrimitiveType(PrimitiveType.VOID);

			rewrite.replace(typeLiteral.getType(), newType, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        return void.class;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testSimpleName() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int hello) {\n");
		buf.append("        return hello;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace identifier
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			SimpleName simpleName= (SimpleName) returnStatement.getExpression();
			rewrite.set(simpleName, SimpleName.IDENTIFIER_PROPERTY, "changed", null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo(int hello) {\n");
		buf.append("        return changed;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testNumberLiteral() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace number
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			NumberLiteral literal= (NumberLiteral) returnStatement.getExpression();
			rewrite.set(literal, NumberLiteral.TOKEN_PROPERTY, "11", null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public int foo() {\n");
		buf.append("        return 11;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testBooleanLiteral() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo() {\n");
		buf.append("        return true;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace number
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			BooleanLiteral literal= (BooleanLiteral) returnStatement.getExpression();
			rewrite.set(literal, BooleanLiteral.BOOLEAN_VALUE_PROPERTY, Boolean.FALSE, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo() {\n");
		buf.append("        return false;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testStringLiteral() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo() {\n");
		buf.append("        return \"Hello\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace number
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			StringLiteral literal= (StringLiteral) returnStatement.getExpression();
			rewrite.set(literal, StringLiteral.ESCAPED_VALUE_PROPERTY, "\"Eclipse\"", null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public String foo() {\n");
		buf.append("        return \"Eclipse\";\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testCharacterLiteral() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public char foo() {\n");
		buf.append("        return 'x';\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace number
			ReturnStatement returnStatement= (ReturnStatement) statements.get(0);

			CharacterLiteral literal= (CharacterLiteral) returnStatement.getExpression();
			rewrite.set(literal, CharacterLiteral.ESCAPED_VALUE_PROPERTY, "'y'", null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public char foo() {\n");
		buf.append("        return 'y';\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404251
	public void testBug404251() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("       total = 0;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast = astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		{ // add infix expression with operand that is a prefix expression
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			Assignment assignment= (Assignment) stmt.getExpression();

			PrefixExpression prefix = ast.newPrefixExpression();
			prefix.setOperator(PrefixExpression.Operator.INCREMENT);
			prefix.setOperand(ast.newSimpleName("c"));

			InfixExpression infix= ast.newInfixExpression();
			infix.setLeftOperand(ast.newSimpleName("a"));
			infix.setRightOperand(ast.newSimpleName("b"));
			infix.extendedOperands().add(prefix);
			rewrite.set(assignment, Assignment.RIGHT_HAND_SIDE_PROPERTY, infix, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("       total = a + b + ++c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ADDITIVE_OPERATOR, JavaCore.DO_NOT_INSERT);
		this.project1.setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ADDITIVE_OPERATOR, JavaCore.DO_NOT_INSERT);

		preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(int a, int b, int c) {\n");
		buf.append("       total = a+b+ ++c;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testIntersectionCastExpression_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		this.project1.setOption(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		String content =
				"import java.io.Serializable;\n" +
				"public class X {\n" +
				"      public Serializable main(Object obj) {\n" +
				"         Serializable o = ((@Marker1 @Marker2 Serializable & I) () -> {});\n" +
				"    	  Serializable oo = obj;\n" +
				"         Serializable ooo = (Serializable) obj;\n" +
				"      }\n" +
				"}\n" +
				"interface I {\n" +
				"  public void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n" +
				"interface K {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", content, false, null);

		CompilationUnit unit= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(unit.getAST());
		AST ast= unit.getAST();

		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		ASTNode node = (ASTNode) type.bodyDeclarations().get(0);
		MethodDeclaration method = (MethodDeclaration) node;

		VariableDeclarationStatement statement = (VariableDeclarationStatement) method.getBody().statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		CastExpression cast = (CastExpression) ((ParenthesizedExpression)fragment.getInitializer()).getExpression();
		Type castType = cast.getType();
		assertEquals("Not an intersection cast type", ASTNode.INTERSECTION_TYPE, castType.getNodeType());

		{
			// Modify intersection cast types by adding new types and removing and adding annotations
			ListRewrite listRewrite = rewrite.getListRewrite(castType, IntersectionType.TYPES_PROPERTY);
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("J"));
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker3"));
			simpleType.annotations().add(annot);
			listRewrite.insertLast(simpleType, null);

			simpleType = (SimpleType) ((IntersectionType) castType).types().get(0);
			listRewrite = rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY);
			listRewrite.remove((ASTNode) simpleType.annotations().get(0), null);
			listRewrite.remove((ASTNode) simpleType.annotations().get(1), null);
		}
		{
			// Create new intersection cast types
			IntersectionType intersection = ast.newIntersectionType();
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("I"));
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker1"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);

			simpleType = ast.newSimpleType(ast.newSimpleName("J"));
			annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker2"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);

			simpleType = ast.newSimpleType(ast.newSimpleName("Serializable"));
			annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker3"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);

			 statement = (VariableDeclarationStatement) method.getBody().statements().get(1);
			fragment = (VariableDeclarationFragment) statement.fragments().get(0);
			CastExpression castExp = ast.newCastExpression();
			castExp.setType(intersection);
			castExp.setExpression(ast.newSimpleName("obj"));
			rewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, castExp, null);
		}
		{
			IntersectionType intersection = ast.newIntersectionType();
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("I"));
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker1"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);

			simpleType = ast.newSimpleType(ast.newSimpleName("Serializable"));
			annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker2"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);

			statement = (VariableDeclarationStatement) method.getBody().statements().get(2);
			fragment = (VariableDeclarationFragment) statement.fragments().get(0);
			CastExpression castExp = (CastExpression) fragment.getInitializer();
			rewrite.set(castExp, CastExpression.TYPE_PROPERTY, intersection, null);
		}
		{ // Add a new cast expression
			cast = ast.newCastExpression();
			IntersectionType intersection = ast.newIntersectionType();
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("I"));
			MarkerAnnotation annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker1"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);

			simpleType = ast.newSimpleType(ast.newSimpleName("Serializable"));
			annot = ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("Marker2"));
			simpleType.annotations().add(annot);
			intersection.types().add(simpleType);
			cast.setType(intersection);
			cast.setExpression(ast.newSimpleName("obj"));

			ReturnStatement returnSt = ast.newReturnStatement();
			returnSt.setExpression(cast);
			ListRewrite listRewrite = rewrite.getListRewrite(method.getBody(), Block.STATEMENTS_PROPERTY);
			listRewrite.insertLast(returnSt, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		content =
				"import java.io.Serializable;\n" +
				"public class X {\n" +
				"      public Serializable main(Object obj) {\n" +
				"         Serializable o = ((Serializable & I & @Marker3 J) () -> {});\n" +
				"    	  Serializable oo = (@Marker1 I & @Marker2 J & @Marker3 Serializable) obj;\n" +
				"         Serializable ooo = (@Marker1 I & @Marker2 Serializable) obj;\n" +
				"        return (@Marker1 I & @Marker2 Serializable) obj;\n" +
				"      }\n" +
				"}\n" +
				"interface I {\n" +
				"  public void foo();\n" +
				"}\n" +
				"interface J {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n" +
				"interface K {\n" +
				"  public void foo();\n" +
				"  public void bar();\n" +
				"}\n";
		assertEqualString(preview, content);
	}

}
