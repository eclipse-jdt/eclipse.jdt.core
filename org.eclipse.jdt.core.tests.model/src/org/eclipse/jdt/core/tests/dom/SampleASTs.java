/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SampleASTs {
	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;
	/**
	 * Internal synonym for deprecated constant AST.JSL4
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS4_INTERNAL = AST.JLS4;

	/**
	 * @deprecated
	 */
	static int getJLS8() {
		return AST.JLS8;
	}
	/**
	 * Returns a subtree of sample of AST nodes. The sample includes
	 * one of each kind (except for BlockComment and LineComment,
     * which cannot be connected directly to a CompilationUnit),
     * but otherwise does not make sense.
	 */
	public static ASTNode oneOfEach(AST target) {
		CompilationUnit cu = target.newCompilationUnit();
		PackageDeclaration pd = target.newPackageDeclaration();
		cu.setPackage(pd);

		ImportDeclaration im = target.newImportDeclaration();
		cu.imports().add(im);

		TypeDeclaration td = target.newTypeDeclaration();
		cu.types().add(td);
		Javadoc javadoc = target.newJavadoc();
		td.setJavadoc(javadoc);
		TagElement tg = target.newTagElement();
		javadoc.tags().add(tg);
		tg.fragments().add(target.newTextElement());
		tg.fragments().add(target.newMemberRef());
		MethodRef mr = target.newMethodRef();
		tg.fragments().add(mr);
		mr.parameters().add(target.newMethodRefParameter());

		VariableDeclarationFragment variableDeclarationFragment = target.newVariableDeclarationFragment();
		FieldDeclaration fd =
			target.newFieldDeclaration(variableDeclarationFragment);
		td.bodyDeclarations().add(fd);

		Initializer in = target.newInitializer();
		td.bodyDeclarations().add(in);

		if (target.apiLevel() >= JLS3_INTERNAL) {
			EnumDeclaration ed = target.newEnumDeclaration();
			td.bodyDeclarations().add(ed);
			EnumConstantDeclaration ec = target.newEnumConstantDeclaration();
			ed.enumConstants().add(ec);
		}

		MethodDeclaration md = target.newMethodDeclaration();
		SingleVariableDeclaration singleVariableDeclaration = target.newSingleVariableDeclaration();
		md.parameters().add(singleVariableDeclaration);
		td.bodyDeclarations().add(md);

		SimpleName sn1 = target.newSimpleName("one"); //$NON-NLS-1$
		SimpleName sn2 =target.newSimpleName("two"); //$NON-NLS-1$
		QualifiedName qn = target.newQualifiedName(sn1, sn2);

		PrimitiveType pt = target.newPrimitiveType(PrimitiveType.INT);
		ArrayType at = target.newArrayType(pt);
		fd.setType(at);

		if (target.apiLevel() >= JLS3_INTERNAL) {
			SimpleType st = target.newSimpleType(qn);
			QualifiedType qt = target.newQualifiedType(st, target.newSimpleName("x")); //$NON-NLS-1$
			WildcardType wt = target.newWildcardType();
			ParameterizedType pmt = target.newParameterizedType(target.newSimpleType(target.newSimpleName("y"))); //$NON-NLS-1$
			pmt.typeArguments().add(wt);
			pmt.typeArguments().add(qt);
			md.setReturnType2(pmt);
		}
		if (target.apiLevel() >= getJLS8()) {
			Dimension ed = target.newDimension();
			md.extraDimensions().add(ed);
		}

		Block b = target.newBlock();
		md.setBody(b);

		// all statements (in alphabetic order of statement type)
		AssertStatement assertStatement = target.newAssertStatement();
		b.statements().add(assertStatement);
		Block block = target.newBlock();
		b.statements().add(block);
		BreakStatement breakStatement = target.newBreakStatement();
		b.statements().add(breakStatement);
		ContinueStatement continueStatement = target.newContinueStatement();
		b.statements().add(continueStatement);
		ConstructorInvocation constructorInvocation = target.newConstructorInvocation();
		b.statements().add(constructorInvocation);
		DoStatement doStatement = target.newDoStatement();
		b.statements().add(doStatement);
		EmptyStatement emptyStatement = target.newEmptyStatement();
		b.statements().add(emptyStatement);
		NullLiteral nullLiteral = target.newNullLiteral();
		ExpressionStatement expressionStatement = target.newExpressionStatement(nullLiteral);
		b.statements().add(expressionStatement);
		ForStatement forStatement = target.newForStatement();
		b.statements().add(forStatement);
		if (target.apiLevel() >= JLS3_INTERNAL) {
			EnhancedForStatement foreachStatement = target.newEnhancedForStatement();
			b.statements().add(foreachStatement);
		}
		IfStatement ifStatement = target.newIfStatement();
		b.statements().add(ifStatement);
		LabeledStatement labeledStatement = target.newLabeledStatement();
		b.statements().add(labeledStatement);
		ReturnStatement returnStatement = target.newReturnStatement();
		b.statements().add(returnStatement);
		SuperConstructorInvocation superConstructorInvocation = target.newSuperConstructorInvocation();
		b.statements().add(superConstructorInvocation);
		SwitchStatement ss = target.newSwitchStatement();
		SwitchCase switchCase = target.newSwitchCase();
		ss.statements().add(switchCase);
		b.statements().add(ss);
		SwitchStatement switchStatement = target.newSwitchStatement();
		b.statements().add(switchStatement);
		SwitchCase switchCase2 = target.newSwitchCase();
		b.statements().add(switchCase2);
		SynchronizedStatement synchronizedStatement = target.newSynchronizedStatement();
		b.statements().add(synchronizedStatement);
		ThrowStatement throwStatement = target.newThrowStatement();
		b.statements().add(throwStatement);
		TryStatement tr = target.newTryStatement();
		CatchClause catchClause = target.newCatchClause();
		tr.catchClauses().add(catchClause);
		b.statements().add(tr);
		if (target.apiLevel() >= JLS4_INTERNAL) {
			UnionType ut = target.newUnionType();
			catchClause.getException().setType(ut);
		}

		TypeDeclaration typeDeclaration = target.newTypeDeclaration();
		TypeDeclarationStatement typeDeclarationStatement = target.newTypeDeclarationStatement(typeDeclaration);
		b.statements().add(typeDeclarationStatement);
		VariableDeclarationFragment variableDeclarationFragment2 = target.newVariableDeclarationFragment();
		VariableDeclarationStatement variableDeclarationStatement = target.newVariableDeclarationStatement(variableDeclarationFragment2);
		b.statements().add(variableDeclarationStatement);
		WhileStatement whileStatement = target.newWhileStatement();
		b.statements().add(whileStatement);

		// all expressions (in alphabetic order of expressions type)
		MethodInvocation inv = target.newMethodInvocation();
		ExpressionStatement expressionStatement2 = target.newExpressionStatement(inv);
		b.statements().add(expressionStatement2);
		List z = inv.arguments();
		ArrayAccess arrayAccess = target.newArrayAccess();
		z.add(arrayAccess);
		ArrayCreation arrayCreation = target.newArrayCreation();
		z.add(arrayCreation);
		ArrayInitializer arrayInitializer = target.newArrayInitializer();
		z.add(arrayInitializer);
		Assignment assignment = target.newAssignment();
		z.add(assignment);
		BooleanLiteral booleanLiteral = target.newBooleanLiteral(true);
		z.add(booleanLiteral);
		CastExpression castExpression = target.newCastExpression();
		z.add(castExpression);
		if (target.apiLevel() >= getJLS8()) {
			IntersectionType it = target.newIntersectionType();
			castExpression.setType(it);
		}
		CharacterLiteral characterLiteral = target.newCharacterLiteral();
		z.add(characterLiteral);
		ClassInstanceCreation cic = target.newClassInstanceCreation();
		AnonymousClassDeclaration anonymousClassDeclaration = target.newAnonymousClassDeclaration();
		cic.setAnonymousClassDeclaration(anonymousClassDeclaration);
		z.add(cic);
		ConditionalExpression conditionalExpression = target.newConditionalExpression();
		z.add(conditionalExpression);
		FieldAccess fieldAccess = target.newFieldAccess();
		z.add(fieldAccess);
		InfixExpression infixExpression = target.newInfixExpression();
		z.add(infixExpression);
		InstanceofExpression instanceofExpression = target.newInstanceofExpression();
		z.add(instanceofExpression);
		if (target.apiLevel() >= getJLS8()) {
			LambdaExpression lambdaExpression = target.newLambdaExpression();
			z.add(lambdaExpression);
		}
		MethodInvocation methodInvocation = target.newMethodInvocation();
		z.add(methodInvocation);
		Name name = target.newName(new String[]{"a", "b"}); //$NON-NLS-1$ //$NON-NLS-2$
		z.add(name);
		NullLiteral nullLiteral2 = target.newNullLiteral();
		z.add(nullLiteral2);
		NumberLiteral numberLiteral = target.newNumberLiteral("1024"); //$NON-NLS-1$
		z.add(numberLiteral);
		ParenthesizedExpression parenthesizedExpression = target.newParenthesizedExpression();
		z.add(parenthesizedExpression);
		PostfixExpression postfixExpression = target.newPostfixExpression();
		z.add(postfixExpression);
		PrefixExpression prefixExpression = target.newPrefixExpression();
		z.add(prefixExpression);
		StringLiteral stringLiteral = target.newStringLiteral();
		z.add(stringLiteral);
		SuperFieldAccess superFieldAccess = target.newSuperFieldAccess();
		z.add(superFieldAccess);
		SuperMethodInvocation superMethodInvocation = target.newSuperMethodInvocation();
		z.add(superMethodInvocation);
		ThisExpression thisExpression = target.newThisExpression();
		z.add(thisExpression);
		TypeLiteral typeLiteral = target.newTypeLiteral();
		z.add(typeLiteral);
		VariableDeclarationFragment variableDeclarationFragment3 = target.newVariableDeclarationFragment();
		VariableDeclarationExpression variableDeclarationExpression = target.newVariableDeclarationExpression(variableDeclarationFragment3);
		z.add(variableDeclarationExpression);

		// annotations
		if (target.apiLevel() >= JLS3_INTERNAL) {
			AnnotationTypeDeclaration atd = target.newAnnotationTypeDeclaration();
			cu.types().add(atd);
			atd.bodyDeclarations().add(target.newAnnotationTypeMemberDeclaration());
			td.modifiers().add(target.newMarkerAnnotation());
			td.modifiers().add(target.newSingleMemberAnnotation());
			NormalAnnotation an0 = target.newNormalAnnotation();
			td.modifiers().add(an0);
			an0.values().add(target.newMemberValuePair());
			td.modifiers().add(target.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		}
		return cu;
	}

	/**
	 * Returns a flat list of sample nodes of each type.
	 * The sample includes one of each kind, including
	 * BlockComment and LineComment.
	 */
	public static List oneOfEachList(AST target) {
		List result = new ArrayList(100);
		for (int nodeType = 0; nodeType < 100; nodeType++) {
			Class nodeClass = null;
			try {
				nodeClass = ASTNode.nodeClassForType(nodeType);
			} catch (IllegalArgumentException e) {
				// oops - guess that's not valid
			}
			if (nodeClass != null) {
				result.add(target.createInstance(nodeClass));
			}
		}
		return result;
	}

}
