/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class CompletionTests extends AbstractJavaModelTests implements RelevanceConstants {

public CompletionTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	IJavaProject project = setUpJavaProject("Completion");
	
	// dummy query for waiting until the indexes are ready
	SearchEngine engine = new SearchEngine();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
	try {
		engine.searchAllTypeNames(
			project.getProject().getWorkspace(),
			null,
			"!@$#!@".toCharArray(),
			IJavaSearchConstants.PATTERN_MATCH,
			IJavaSearchConstants.CASE_SENSITIVE,
			IJavaSearchConstants.CLASS,
			scope, 
			new ITypeNameRequestor() {
				public void acceptClass(
					char[] packageName,
					char[] simpleTypeName,
					char[][] enclosingTypeNames,
					String path) {}
				public void acceptInterface(
					char[] packageName,
					char[] simpleTypeName,
					char[][] enclosingTypeNames,
					String path) {}
			},
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
	} catch (CoreException e) {
	}
}
public void tearDownSuite() throws Exception {
	deleteProject("Completion");
	
	super.tearDownSuite();
}


public static Test suite() {
	TestSuite suite = new Suite(CompletionTests.class.getName());
	
	// completion tests
	suite.addTest(new CompletionTests("testCompletionCaseInsensitive"));
	suite.addTest(new CompletionTests("testCompletionNullRequestor"));
	suite.addTest(new CompletionTests("testCompletionFindExceptions1"));
	suite.addTest(new CompletionTests("testCompletionFindExceptions2"));
	suite.addTest(new CompletionTests("testCompletionFindClass"));
	suite.addTest(new CompletionTests("testCompletionFindClass2"));
	suite.addTest(new CompletionTests("testCompletionFindClassDefaultPackage"));
	suite.addTest(new CompletionTests("testCompletionFindConstructor"));
	suite.addTest(new CompletionTests("testCompletionFindField1"));
	suite.addTest(new CompletionTests("testCompletionFindField2"));
	suite.addTest(new CompletionTests("testCompletionFindField3"));
	suite.addTest(new CompletionTests("testCompletionFindImport1"));
	suite.addTest(new CompletionTests("testCompletionFindImport2"));
	suite.addTest(new CompletionTests("testCompletionFindLocalVariable"));
	suite.addTest(new CompletionTests("testCompletionFindMemberType1"));
	suite.addTest(new CompletionTests("testCompletionFindMemberType2"));
	suite.addTest(new CompletionTests("testCompletionFindMethod1"));
	suite.addTest(new CompletionTests("testCompletionFindMethod2"));
	suite.addTest(new CompletionTests("testCompletionFindMethodInThis"));
	suite.addTest(new CompletionTests("testCompletionFindMethodWhenInProcess"));
	suite.addTest(new CompletionTests("testCompletionFindThisDotField"));
	suite.addTest(new CompletionTests("testCompletionEndOfCompilationUnit"));
	suite.addTest(new CompletionTests("testCompletionOutOfBounds"));
	suite.addTest(new CompletionTests("testCompletionRepeatedType"));
	suite.addTest(new CompletionTests("testCompletionOnClassFile"));
	suite.addTest(new CompletionTests("testCompletionCaseInsensitivePackage"));
	suite.addTest(new CompletionTests("testCompletionFindSuperInterface"));
	suite.addTest(new CompletionTests("testCompletionVisibilityCheckEnabled"));
	suite.addTest(new CompletionTests("testCompletionVisibilityCheckDisabled"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName2"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName3"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName4"));
	suite.addTest(new CompletionTests("testCompletionPrefixFieldName1"));
	suite.addTest(new CompletionTests("testCompletionPrefixFieldName2"));
	suite.addTest(new CompletionTests("testCompletionPrefixMethodName1"));
	suite.addTest(new CompletionTests("testCompletionPrefixMethodName2"));
	suite.addTest(new CompletionTests("testCompletionPrefixMethodName3"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration2"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration3"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration4"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration5"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration6"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration7"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration8"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration9"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration10"));
	suite.addTest(new CompletionTests("testCompletionFieldName"));
	suite.addTest(new CompletionTests("testCompletionLocalName"));
	suite.addTest(new CompletionTests("testCompletionArgumentName"));
	suite.addTest(new CompletionTests("testCompletionCatchArgumentName"));
	suite.addTest(new CompletionTests("testCompletionCatchArgumentName2"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousType"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousType2"));
	suite.addTest(new CompletionTests("testCompletionWithBinaryFolder"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray1"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray2"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray3"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray4"));
	suite.addTest(new CompletionTests("testCompletionVariableNameUnresolvedType"));
	suite.addTest(new CompletionTests("testCompletionSameSuperClass"));
	suite.addTest(new CompletionTests("testCompletionSuperType"));
	suite.addTest(new CompletionTests("testCompletionSuperType2"));
	suite.addTest(new CompletionTests("testCompletionSuperType3"));
	suite.addTest(new CompletionTests("testCompletionSuperType4"));
	suite.addTest(new CompletionTests("testCompletionSuperType5"));
	suite.addTest(new CompletionTests("testCompletionSuperType6"));
	suite.addTest(new CompletionTests("testCompletionSuperType7"));
	suite.addTest(new CompletionTests("testCompletionSuperType8"));
	suite.addTest(new CompletionTests("testCompletionMethodThrowsClause"));
	suite.addTest(new CompletionTests("testCompletionMethodThrowsClause2"));
	suite.addTest(new CompletionTests("testCompletionThrowStatement"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedReturnType"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedParameterType"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedFieldType"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedEnclosingType"));
	suite.addTest(new CompletionTests("testCompletionObjectsMethodWithInterfaceReceiver"));
	suite.addTest(new CompletionTests("testCompletionConstructorForAnonymousType"));
	suite.addTest(new CompletionTests("testCompletionAbstractMethodRelevance1"));
	suite.addTest(new CompletionTests("testCompletionAbstractMethodRelevance2"));
	suite.addTest(new CompletionTests("testCompletionReturnInInitializer"));
	suite.addTest(new CompletionTests("testCompletionVariableName1"));
	suite.addTest(new CompletionTests("testCompletionVariableName2"));
	suite.addTest(new CompletionTests("testCompletionVariableName3"));
	suite.addTest(new CompletionTests("testCompletionOnStaticMember1"));
	suite.addTest(new CompletionTests("testCompletionOnStaticMember2"));
	suite.addTest(new CompletionTests("testCompletionMemberType2"));
	suite.addTest(new CompletionTests("testCompletionAfterCase1"));
	suite.addTest(new CompletionTests("testCompletionAfterCase2"));
	suite.addTest(new CompletionTests("testCompletionToplevelType1"));
	suite.addTest(new CompletionTests("testCompletionLocalType1"));
	
	// completion expectedTypes tests
	suite.addTest(new CompletionTests("testCompletionReturnStatementIsParent1"));
	suite.addTest(new CompletionTests("testCompletionReturnStatementIsParent2"));
	suite.addTest(new CompletionTests("testCompletionCastIsParent1"));
	suite.addTest(new CompletionTests("testCompletionCastIsParent2"));
	suite.addTest(new CompletionTests("testCompletionMessageSendIsParent1"));
	suite.addTest(new CompletionTests("testCompletionMessageSendIsParent2"));
	suite.addTest(new CompletionTests("testCompletionMessageSendIsParent3"));
	suite.addTest(new CompletionTests("testCompletionMessageSendIsParent4"));
	suite.addTest(new CompletionTests("testCompletionMessageSendIsParent5"));
	suite.addTest(new CompletionTests("testCompletionMessageSendIsParent6"));
	suite.addTest(new CompletionTests("testCompletionAllocationExpressionIsParent1"));
	suite.addTest(new CompletionTests("testCompletionAllocationExpressionIsParent2"));
	suite.addTest(new CompletionTests("testCompletionAllocationExpressionIsParent3"));
	suite.addTest(new CompletionTests("testCompletionAllocationExpressionIsParent4"));
	suite.addTest(new CompletionTests("testCompletionAllocationExpressionIsParent5"));
	suite.addTest(new CompletionTests("testCompletionAllocationExpressionIsParent6"));
	suite.addTest(new CompletionTests("testCompletionFieldInitializer1"));
	suite.addTest(new CompletionTests("testCompletionFieldInitializer2"));
	suite.addTest(new CompletionTests("testCompletionFieldInitializer3"));
	suite.addTest(new CompletionTests("testCompletionFieldInitializer4"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInInitializer1"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInInitializer2"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInInitializer3"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInInitializer4"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInMethod1"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInMethod2"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInMethod3"));
	suite.addTest(new CompletionTests("testCompletionVariableInitializerInMethod4"));
	suite.addTest(new CompletionTests("testCompletionAssignmentInMethod1"));
	suite.addTest(new CompletionTests("testCompletionAssignmentInMethod2"));
	suite.addTest(new CompletionTests("testCompletionAssignmentInMethod3"));
	suite.addTest(new CompletionTests("testCompletionAssignmentInMethod4"));
	suite.addTest(new CompletionTests("testCompletionEmptyTypeName1"));
	suite.addTest(new CompletionTests("testCompletionEmptyTypeName2"));
	suite.addTest(new CompletionTests("testCompletionExpectedTypeIsNotValid"));
	suite.addTest(new CompletionTests("testCompletionMemberType"));
	suite.addTest(new CompletionTests("testCompletionVoidMethod"));
	suite.addTest(new CompletionTests("testCompletionQualifiedExpectedType"));
	suite.addTest(new CompletionTests("testCompletionUnaryOperator1"));
	suite.addTest(new CompletionTests("testCompletionUnaryOperator2"));
	suite.addTest(new CompletionTests("testCompletionBinaryOperator1"));
	suite.addTest(new CompletionTests("testCompletionBinaryOperator2"));
	suite.addTest(new CompletionTests("testCompletionBinaryOperator3"));
	suite.addTest(new CompletionTests("testCompletionInstanceofOperator1"));
	suite.addTest(new CompletionTests("testCompletionConditionalExpression1"));
	suite.addTest(new CompletionTests("testCompletionConditionalExpression2"));
	suite.addTest(new CompletionTests("testCompletionConditionalExpression3"));
	suite.addTest(new CompletionTests("testCompletionArrayAccess1"));
	suite.addTest(new CompletionTests("testCompletionFindSecondaryType1"));
	
	// completion keywords tests
	suite.addTest(new CompletionTests("testCompletionKeywordThis1"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis2"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis3"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis4"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis5"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis6"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis7"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis8"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis9"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis10"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis11"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis12"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis13"));
	suite.addTest(new CompletionTests("testCompletionKeywordThis14"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper1"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper2"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper3"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper4"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper5"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper6"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper7"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper8"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper9"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper10"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper11"));
	suite.addTest(new CompletionTests("testCompletionKeywordSuper12"));
	suite.addTest(new CompletionTests("testCompletionKeywordTry1"));
	suite.addTest(new CompletionTests("testCompletionKeywordTry2"));
	suite.addTest(new CompletionTests("testCompletionKeywordTry3"));
	suite.addTest(new CompletionTests("testCompletionKeywordTry4"));
	suite.addTest(new CompletionTests("testCompletionKeywordTry5"));
	suite.addTest(new CompletionTests("testCompletionKeywordTry6"));
	suite.addTest(new CompletionTests("testCompletionKeywordDo1"));
	suite.addTest(new CompletionTests("testCompletionKeywordDo2"));
	suite.addTest(new CompletionTests("testCompletionKeywordDo3"));
	suite.addTest(new CompletionTests("testCompletionKeywordDo4"));
	suite.addTest(new CompletionTests("testCompletionKeywordDo5"));
	suite.addTest(new CompletionTests("testCompletionKeywordDo6"));
	suite.addTest(new CompletionTests("testCompletionKeywordFor1"));
	suite.addTest(new CompletionTests("testCompletionKeywordFor2"));
	suite.addTest(new CompletionTests("testCompletionKeywordFor3"));
	suite.addTest(new CompletionTests("testCompletionKeywordFor4"));
	suite.addTest(new CompletionTests("testCompletionKeywordFor5"));
	suite.addTest(new CompletionTests("testCompletionKeywordFor6"));
	suite.addTest(new CompletionTests("testCompletionKeywordIf1"));
	suite.addTest(new CompletionTests("testCompletionKeywordIf2"));
	suite.addTest(new CompletionTests("testCompletionKeywordIf3"));
	suite.addTest(new CompletionTests("testCompletionKeywordIf4"));
	suite.addTest(new CompletionTests("testCompletionKeywordIf5"));
	suite.addTest(new CompletionTests("testCompletionKeywordIf6"));
	suite.addTest(new CompletionTests("testCompletionKeywordReturn1"));
	suite.addTest(new CompletionTests("testCompletionKeywordReturn2"));
	suite.addTest(new CompletionTests("testCompletionKeywordReturn3"));
	suite.addTest(new CompletionTests("testCompletionKeywordReturn4"));
	suite.addTest(new CompletionTests("testCompletionKeywordReturn5"));
	suite.addTest(new CompletionTests("testCompletionKeywordReturn6"));
	suite.addTest(new CompletionTests("testCompletionKeywordSwitch1"));
	suite.addTest(new CompletionTests("testCompletionKeywordSwitch2"));
	suite.addTest(new CompletionTests("testCompletionKeywordSwitch3"));
	suite.addTest(new CompletionTests("testCompletionKeywordSwitch4"));
	suite.addTest(new CompletionTests("testCompletionKeywordSwitch5"));
	suite.addTest(new CompletionTests("testCompletionKeywordSwitch6"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrow1"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrow2"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrow3"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrow4"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrow5"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrow6"));
	suite.addTest(new CompletionTests("testCompletionKeywordAssert1"));
	suite.addTest(new CompletionTests("testCompletionKeywordAssert2"));
	suite.addTest(new CompletionTests("testCompletionKeywordAssert3"));
	suite.addTest(new CompletionTests("testCompletionKeywordAssert4"));
	suite.addTest(new CompletionTests("testCompletionKeywordAssert5"));
	suite.addTest(new CompletionTests("testCompletionKeywordAssert6"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse1"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse2"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse3"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse4"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse5"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse6"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse7"));
	suite.addTest(new CompletionTests("testCompletionKeywordElse8"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch1"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch2"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch3"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch4"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch5"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch6"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch7"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch8"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch8"));
	suite.addTest(new CompletionTests("testCompletionKeywordCatch10"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally1"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally2"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally3"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally4"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally5"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally6"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally7"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally8"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally9"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally10"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally11"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally12"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally13"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinally14"));
	suite.addTest(new CompletionTests("testCompletionKeywordContinue1"));
	suite.addTest(new CompletionTests("testCompletionKeywordContinue2"));
	suite.addTest(new CompletionTests("testCompletionKeywordContinue3"));
	suite.addTest(new CompletionTests("testCompletionKeywordContinue4"));
	suite.addTest(new CompletionTests("testCompletionKeywordBreak1"));
	suite.addTest(new CompletionTests("testCompletionKeywordBreak2"));
	suite.addTest(new CompletionTests("testCompletionKeywordBreak3"));
	suite.addTest(new CompletionTests("testCompletionKeywordBreak4"));
	suite.addTest(new CompletionTests("testCompletionKeywordBreak5"));
	suite.addTest(new CompletionTests("testCompletionKeywordBreak6"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile1"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile2"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile3"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile4"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile5"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile6"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile7"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile8"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile9"));
	suite.addTest(new CompletionTests("testCompletionKeywordWhile10"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends1"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends2"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends3"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends4"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends5"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends6"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends7"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends8"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends9"));
	suite.addTest(new CompletionTests("testCompletionKeywordExtends10"));
	suite.addTest(new CompletionTests("testCompletionKeywordImplements1"));
	suite.addTest(new CompletionTests("testCompletionKeywordImplements2"));
	suite.addTest(new CompletionTests("testCompletionKeywordImplements3"));
	suite.addTest(new CompletionTests("testCompletionKeywordImplements4"));
	suite.addTest(new CompletionTests("testCompletionKeywordImplements5"));
	suite.addTest(new CompletionTests("testCompletionKeywordImplements6"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage1"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage2"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage3"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage4"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage5"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage6"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage7"));
	suite.addTest(new CompletionTests("testCompletionKeywordPackage8"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport1"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport2"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport3"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport4"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport5"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport6"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport7"));
	suite.addTest(new CompletionTests("testCompletionKeywordImport8"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase1"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase2"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase3"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase4"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase5"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase6"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase7"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase8"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase9"));
	suite.addTest(new CompletionTests("testCompletionKeywordCase10"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault1"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault2"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault3"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault4"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault5"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault6"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault7"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault8"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault9"));
	suite.addTest(new CompletionTests("testCompletionKeywordDefault10"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass1"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass2"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass3"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass4"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass5"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass6"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass7"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass8"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass9"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass10"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass11"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass12"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass13"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass14"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass15"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass16"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass17"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass18"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass19"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass20"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass21"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass22"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass23"));
	suite.addTest(new CompletionTests("testCompletionKeywordClass24"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface1"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface2"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface3"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface4"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface5"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface6"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface7"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface8"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface9"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface10"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface11"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface12"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface13"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface14"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface15"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface16"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface17"));
	suite.addTest(new CompletionTests("testCompletionKeywordInterface18"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows1"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows2"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows3"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows4"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows5"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows6"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows7"));
	suite.addTest(new CompletionTests("testCompletionKeywordThrows8"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized1"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized2"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized3"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized4"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized5"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized6"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized7"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized8"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized9"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized10"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized11"));
	suite.addTest(new CompletionTests("testCompletionKeywordSynchronized12"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative1"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative2"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative3"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative4"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative5"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative6"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative7"));
	suite.addTest(new CompletionTests("testCompletionKeywordNative8"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp1"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp2"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp3"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp4"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp5"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp6"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp7"));
	suite.addTest(new CompletionTests("testCompletionKeywordStrictfp8"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile1"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile2"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile3"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile4"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile5"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile6"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile7"));
	suite.addTest(new CompletionTests("testCompletionKeywordVolatile8"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient1"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient2"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient3"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient4"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient5"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient6"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient7"));
	suite.addTest(new CompletionTests("testCompletionKeywordTransient8"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew1"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew2"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew3"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew4"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew5"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew6"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew7"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew8"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew9"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew10"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew11"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew12"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew13"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew14"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew15"));
	suite.addTest(new CompletionTests("testCompletionKeywordNew16"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic1"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic2"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic3"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic4"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic5"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic6"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic7"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic8"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic9"));
	suite.addTest(new CompletionTests("testCompletionKeywordStatic10"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic1"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic2"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic3"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic4"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic5"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic6"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic7"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic8"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic9"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic10"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic11"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic12"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic13"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic14"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic15"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic16"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic17"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic18"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic19"));
	suite.addTest(new CompletionTests("testCompletionKeywordPublic20"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate1"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate2"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate3"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate4"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate5"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate6"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate7"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate8"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate9"));
	suite.addTest(new CompletionTests("testCompletionKeywordPrivate10"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected1"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected2"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected3"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected4"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected5"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected6"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected7"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected8"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected9"));
	suite.addTest(new CompletionTests("testCompletionKeywordProtected10"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal1"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal2"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal3"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal4"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal5"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal6"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal7"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal8"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal9"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal10"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal11"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal12"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal13"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal14"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal15"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal16"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal17"));
	suite.addTest(new CompletionTests("testCompletionKeywordFinal18"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract1"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract2"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract3"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract4"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract5"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract6"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract7"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract8"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract9"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract10"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract11"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract12"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract13"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract14"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract15"));
	suite.addTest(new CompletionTests("testCompletionKeywordAbstract16"));
	suite.addTest(new CompletionTests("testCompletionKeywordTrue1"));
	suite.addTest(new CompletionTests("testCompletionKeywordTrue2"));
	suite.addTest(new CompletionTests("testCompletionKeywordTrue3"));
	suite.addTest(new CompletionTests("testCompletionKeywordTrue4"));
	suite.addTest(new CompletionTests("testCompletionKeywordFalse1"));
	suite.addTest(new CompletionTests("testCompletionKeywordFalse2"));
	suite.addTest(new CompletionTests("testCompletionKeywordFalse3"));
	suite.addTest(new CompletionTests("testCompletionKeywordFalse4"));
	suite.addTest(new CompletionTests("testCompletionKeywordNull1"));
	suite.addTest(new CompletionTests("testCompletionKeywordNull2"));
	suite.addTest(new CompletionTests("testCompletionKeywordNull3"));
	suite.addTest(new CompletionTests("testCompletionKeywordNull4"));
	suite.addTest(new CompletionTests("testCompletionKeywordInstanceof1"));
	suite.addTest(new CompletionTests("testCompletionKeywordInstanceof2"));
	suite.addTest(new CompletionTests("testCompletionKeywordInstanceof3"));
	suite.addTest(new CompletionTests("testCompletionKeywordInstanceof4"));
	suite.addTest(new CompletionTests("testCompletionKeywordInstanceof5"));
	suite.addTest(new CompletionTests("testCompletionKeywordInstanceof6"));
	
	// completion tests with position
	suite.addTest(new CompletionTests("testCompletionNonEmptyToken1"));
	suite.addTest(new CompletionTests("testCompletionEmptyToken1"));
	
	return suite;
}
/**
 * Ensures that completion is not case sensitive
 */
public void testCompletionCaseInsensitive() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src", "", "CompletionCaseInsensitive.java");
	
	String str = cu.getSource();
	String completeBehind = "Field";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:field    completion:field    relevance:"+(R_DEFAULT + R_INTERESTING + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}
/**
 * Complete a package in a case insensitive way
 */
public void testCompletionCaseInsensitivePackage() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCaseInsensitivePackage.java");

	String str = cu.getSource();
	String completeBehind = "Ja";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have package completions",
		"element:jarpack1    completion:jarpack1    relevance:"+(R_DEFAULT + R_INTERESTING)+"\n" +
		"element:jarpack2    completion:jarpack2    relevance:"+(R_DEFAULT + R_INTERESTING)+"\n" +
		"element:java    completion:java    relevance:"+(R_DEFAULT + R_INTERESTING)+"\n" +
		"element:java.lang    completion:java.lang    relevance:"+(R_DEFAULT + R_INTERESTING),
		requestor.getResults());
}

/**
 * Complete at end of file.
 */
public void testCompletionEndOfCompilationUnit() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src", "", "CompletionEndOfCompilationUnit.java");
	cu.codeComplete(cu.getSourceRange().getOffset() + cu.getSourceRange().getLength(), requestor);
	assertEquals(
		"should have two methods of 'foo'", 
		"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED)+"\n" +
		"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED),
		requestor.getResults());	
}

/**
 * Complete the type "A" from "new A".
 */
public void testCompletionFindClass() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindClass.java");

	String str = cu.getSource();
	String completeBehind = "A";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one class",
		"element:A    completion:A    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED)+"\n" +
		"element:A1    completion:A1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:A2    completion:A2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:A3    completion:A3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:ABC    completion:p1.ABC    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:ABC    completion:p2.ABC    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());	
}

/**
 * The same type must be find only once
 */
public void testCompletionFindClass2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindClass2.java");

	String str = cu.getSource();
	String completeBehind = "PX";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one classe", 
		"element:PX    completion:pack1.PX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
		requestor.getResults());
}


/**
 * Complete the type "Default" in the default package example.
 */
public void testCompletionFindClassDefaultPackage() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionDefaultPackage.java");

	String str = cu.getSource();
	String completeBehind = "Def";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one class", 
		"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());	
}

/**
 * Complete the constructor "CompletionFindConstructor" from "new CompletionFindConstructor(".
 */
public void testCompletionFindConstructor() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindConstructor.java");

	String str = cu.getSource();
	String completeBehind = "CompletionFindConstructor(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have two constructor (a constructor and an anonymous type)", 
		"element:CompletionFindConstructor    completion:)    relevance:"+(R_DEFAULT + R_INTERESTING)+"\n" +
		"element:CompletionFindConstructor    completion:)    relevance:"+(R_DEFAULT + R_INTERESTING),
		requestor.getResults());
}

/**
 * Complete the exception "Exception" in a catch clause.
 */
public void testCompletionFindExceptions1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindException1.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals(
		"should have one class", 
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXCEPTION + R_UNQUALIFIED),
		requestor.getResults());
}

/**
 * Complete the exception "Exception" in a throws clause.
 */
public void testCompletionFindExceptions2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindException2.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one class",
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXCEPTION + R_UNQUALIFIED),
		requestor.getResults());
}

/**
 * Complete the field "var" from "va";
 */
public void testCompletionFindField1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindField1.java");

	String str = cu.getSource();
	String completeBehind = "va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals(
		"should have one field: 'var' and one variable: 'var'", 
		"element:var    completion:this.var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE)+"\n"+
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());	
}

/**
 * Complete the field "var" from "this.va";
 */
public void testCompletionFindField2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindField2.java");

	String str = cu.getSource();
	String completeBehind = "va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals(
		"should have 1 field of starting with 'va'",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionFindField3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindField3.java");

	String str = cu.getSource();
	String completeBehind = "b.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:bar    completion:bar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

/**
 * Complete the import, "import pac"
 */
public void testCompletionFindImport1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindImport1.java");

	String str = cu.getSource();
	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have three imports \"pack1\" & \"pack1\" & \"pack1.pack3\" & \"pack2\"", 
		"element:pack    completion:pack.*;    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:pack1    completion:pack1.*;    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:pack1.pack3    completion:pack1.pack3.*;    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:pack2    completion:pack2.*;    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionFindImport2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindImport2.java");

	String str = cu.getSource();
	String completeBehind = "pack1.P";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have six completions",
		"element:PX    completion:pack1.PX;    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:pack1.pack3    completion:pack1.pack3.*;    relevance:"+(R_DEFAULT + R_INTERESTING),
		requestor.getResults());
}

/**
 * Complete the local variable "var";
 */
public void testCompletionFindLocalVariable() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindLocalVariable.java");

	String str = cu.getSource();
	String completeBehind = "va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one local variable of 'var'", 
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());	
}

/**
 * Complete the method call "a.foobar" from "a.fooba";
 */
public void testCompletionFindMethod1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethod1.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have two methods of 'foobar'", 
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED)+"\n" +
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());		
}


/**
 * Too much Completion match on interface
 */
public void testCompletionFindMethod2() throws JavaModelException {
	
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethod2.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED)+"\n" +
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());	
}


/**
 * Complete the method call "foobar" from "fooba";
 */
public void testCompletionFindMethodInThis() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethodInThis.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one method of 'foobar'", 
		"element:foobar    completion:foobar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());		
}

/**
 * Complete the method call "foobar" from "fooba".  The compilation
 * unit simulates typing in process; ie it has incomplete structure/syntax errors.
 */
public void testCompletionFindMethodWhenInProcess() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethodInProcess.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have a method of 'foobar'", 
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
	cu.close();
}

public void testCompletionFindSuperInterface() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindSuperInterface.java");

	String str = cu.getSource();
	String completeBehind = "Super";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
		
	assertEquals(
		"element:SuperClass    completion:SuperClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:SuperInterface    completion:SuperInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED),
		requestor.getResults());
}

/**
 * Complete the field "bar" from "this.ba"
 */
public void testCompletionFindThisDotField() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindThisDotField.java");

	String str = cu.getSource();
	String completeBehind = "this.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one result of 'bar'", 
		"element:bar    completion:bar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}

/**
 * Attempt to do completion with a null requestor
 */
public void testCompletionNullRequestor() throws JavaModelException {
	try {
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindThisDotField.java");
		cu.codeComplete(5, (ICompletionRequestor)null);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Should not be able to do completion with a null requestor", false);
}

/**
 * Ensures that the code assist features works on class files with associated source.
 */
public void testCompletionOnClassFile() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cu = getClassFile("Completion", "zzz.jar", "jarpack1", "X.class");
	
	String str = cu.getSource();
	String completeBehind = "Obj";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one class", 
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

/**
 * Test that an out of bounds index causes an exception.
 */
public void testCompletionOutOfBounds() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionOutOfBounds.java");
	try {
		cu.codeComplete(cu.getSource().length() + 1, requestor);
	} catch (JavaModelException e) {
		assertTrue("Should be out of bounds", e.getStatus().getCode() == IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		return;
	}
	assertTrue("should have failed", false);
}

/**
 * Complete the type "Repeated", "RepeatedOtherType from "Repeated".
 */
public void testCompletionRepeatedType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionRepeatedType.java");

	String str = cu.getSource();
	String completeBehind = "/**/CompletionRepeated";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have two types",
		"element:CompletionRepeatedOtherType    completion:CompletionRepeatedOtherType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionRepeatedType    completion:CompletionRepeatedType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());	
}


public void testCompletionVisibilityCheckEnabled() throws JavaModelException {
	String visibilityCheckID = "org.eclipse.jdt.core.codeComplete.visibilityCheck";
	Hashtable options = JavaCore.getOptions();
	Object visibilityCheckPreviousValue = options.get(visibilityCheckID);
	options.put(visibilityCheckID,"enabled");
	JavaCore.setOptions(options);
	
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVisibilityCheck.java");

	String str = cu.getSource();
	String completeBehind = "x.p";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	options.put(visibilityCheckID,visibilityCheckPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"should have two methods", 
		"element:protectedFoo    completion:protectedFoo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED)+"\n" +
		"element:publicFoo    completion:publicFoo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}


public void testCompletionVisibilityCheckDisabled() throws JavaModelException {
	String visibilityCheckID = "org.eclipse.jdt.core.codeComplete.visibilityCheck";
	Hashtable options = JavaCore.getOptions();
	Object visibilityCheckPreviousValue = options.get(visibilityCheckID);
	options.put(visibilityCheckID,"disabled");
	JavaCore.setOptions(options);
	
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVisibilityCheck.java");

	String str = cu.getSource();
	String completeBehind = "x.p";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	options.put(visibilityCheckID,visibilityCheckPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"should have three methods", 
		"element:privateFoo    completion:privateFoo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED)+"\n" +
		"element:protectedFoo    completion:protectedFoo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED)+"\n" +
		"element:publicFoo    completion:publicFoo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAmbiguousFieldName() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:this.xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAmbiguousFieldName2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName2.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:CompletionAmbiguousFieldName2.this.xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAmbiguousFieldName3() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName3.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:ClassFoo.this.xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}


public void testCompletionAmbiguousFieldName4() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName4.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}


public void testCompletionPrefixFieldName1() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixFieldName1.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:CompletionPrefixFieldName1.this.xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}


public void testCompletionPrefixFieldName2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixFieldName2.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}


public void testCompletionPrefixMethodName1() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixMethodName1.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:CompletionPrefixMethodName1.this.xBar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:xBar    completion:xBar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}


public void testCompletionPrefixMethodName2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixMethodName2.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:xBar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionPrefixMethodName3() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixMethodName3.java");

	String str = cu.getSource();
	String completeBehind = "xBar(1,";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED)+"\n" +
		"element:xBar    completion:CompletionPrefixMethodName3.this.xBar(1,    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
		requestor.getResults());
}

public void testCompletionFindMemberType1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMemberType1.java");

	String str = cu.getSource();
	String completeBehind = "Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:B1.Inner1    completion:Inner1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionFindMemberType2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMemberType2.java");

	String str = cu.getSource();
	String completeBehind = "Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:B2.Inner2    completion:Inner2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:equals    completion:public boolean equals(Object obj)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration2.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:equals    completion:public boolean equals(Object obj)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

/**
 * Completion should not propose declarations of method already locally implemented
 */
public void testCompletionMethodDeclaration3() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration3.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:equals    completion:public boolean equals(Object obj)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionMethodDeclaration4() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration4.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_ABSTRACT_METHOD)+"\n"+
		"element:equals    completion:public boolean equals(Object obj)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration5() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration5.java");

	String str = cu.getSource();
	String completeBehind = "new CompletionSuperClass() {";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CompletionMethodDeclaration5    completion:CompletionMethodDeclaration5    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:equals    completion:public boolean equals(Object obj)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:finalize    completion:protected void finalize() throws Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:hashCode    completion:public int hashCode()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:toString    completion:public String toString()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration6() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration6.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMethodDeclaration7() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration7.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration8() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration8.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration9() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration9.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration10() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration10.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionFieldName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFieldName.java");

	String str = cu.getSource();
	String completeBehind = "ClassWithComplexName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions",
		"element:classWithComplexName    completion:classWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:complexName2    completion:complexName2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:withComplexName    completion:withComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionLocalName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionLocalName.java");

	String str = cu.getSource();
	String completeBehind = "ClassWithComplexName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:classWithComplexName    completion:classWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:complexName2    completion:complexName2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:withComplexName    completion:withComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionArgumentName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionArgumentName.java");

	String str = cu.getSource();
	String completeBehind = "ClassWithComplexName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:classWithComplexName    completion:classWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:complexName2    completion:complexName2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:withComplexName    completion:withComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionCatchArgumentName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCatchArgumentName.java");

	String str = cu.getSource();
	String completeBehind = "ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:exception    completion:exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionAmbiguousType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousType.java");

	String str = cu.getSource();
	String completeBehind = "ABC";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:ABC    completion:p1.ABC    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME)+"\n" +
		"element:ABC    completion:p2.ABC    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
		requestor.getResults());
}


public void testCompletionAmbiguousType2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousType2.java");

	String str = cu.getSource();
	String completeBehind = "ABC";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:ABC    completion:ABC    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED)+"\n" +
		"element:ABC    completion:p2.ABC    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
		requestor.getResults());
}


public void testCompletionWithBinaryFolder() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionWithBinaryFolder.java");

	String str = cu.getSource();
	String completeBehind = "My";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions",
		"element:MyClass    completion:MyClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:mypackage    completion:mypackage    relevance:"+(R_DEFAULT + R_INTERESTING),
		requestor.getResults());
}


public void testCompletionVariableNameOfArray1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray1.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion",
		"element:objects    completion:objects    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionVariableNameOfArray2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray2.java");

	String str = cu.getSource();
	String completeBehind = "cl";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion",
		"element:classes    completion:classes    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionVariableNameOfArray3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray3.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion",
		"element:objects    completion:objects    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}


public void testCompletionVariableNameOfArray4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray4.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have no completion",
		"",
		requestor.getResults());
}


public void testCompletionVariableNameUnresolvedType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameUnresolvedType.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have no completion",
		"",
		requestor.getResults());
}


public void testCompletionSameSuperClass() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSameSuperClass.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have five completions",
		"element:bar    completion:CompletionSameSuperClass.this.bar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME)+"\n"+
		"element:bar    completion:CompletionSameSuperClass.this.bar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME)+"\n"+
		"element:bar    completion:bar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED)+"\n"+
		"element:bar    completion:bar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED)+"\n"+
		"element:bar    completion:this.bar    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
		requestor.getResults());
}

public void testCompletionSuperType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperClass.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass.Inner    completion:Inner    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS),
		requestor.getResults());
}

public void testCompletionSuperType2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType2.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuper";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass    completion:CompletionSuperClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperClass2    completion:CompletionSuperClass2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperInterface    completion:CompletionSuperInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperInterface2    completion:CompletionSuperInterface2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType    completion:CompletionSuperType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType2    completion:CompletionSuperType2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType3    completion:CompletionSuperType3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType4    completion:CompletionSuperType4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType5    completion:CompletionSuperType5    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType6    completion:CompletionSuperType6    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType7    completion:CompletionSuperType7    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType8    completion:CompletionSuperType8    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionSuperType3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType3.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuper";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass    completion:CompletionSuperClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperClass2    completion:CompletionSuperClass2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperInterface    completion:CompletionSuperInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperInterface2    completion:CompletionSuperInterface2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType    completion:CompletionSuperType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType2    completion:CompletionSuperType2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType3    completion:CompletionSuperType3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType4    completion:CompletionSuperType4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType5    completion:CompletionSuperType5    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType6    completion:CompletionSuperType6    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType7    completion:CompletionSuperType7    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType8    completion:CompletionSuperType8    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionSuperType4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType4.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperClass2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperClass2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

public void testCompletionSuperType5() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType5.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperInterface2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperInterface2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:CompletionSuperInterface2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionSuperType6() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType6.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuper";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass    completion:CompletionSuperClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperClass2    completion:CompletionSuperClass2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperInterface    completion:CompletionSuperInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperInterface2    completion:CompletionSuperInterface2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType    completion:CompletionSuperType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType2    completion:CompletionSuperType2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType3    completion:CompletionSuperType3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType4    completion:CompletionSuperType4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType5    completion:CompletionSuperType5    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType6    completion:CompletionSuperType6    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType7    completion:CompletionSuperType7    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED)+"\n" +
		"element:CompletionSuperType8    completion:CompletionSuperType8    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionSuperType7() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType7.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperClass2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:CompletionSuperClass2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionSuperType8() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType8.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperInterface2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperInterface2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:CompletionSuperInterface2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionMethodThrowsClause() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodThrowsClause.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXCEPTION + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMethodThrowsClause2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodThrowsClause2.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Exception    completion:java.lang.Exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXCEPTION),
		requestor.getResults());
}

public void testCompletionThrowStatement() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionThrowStatement.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXCEPTION + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionUnresolvedReturnType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedReturnType.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:barPlus    completion:barPlus()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionUnresolvedParameterType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedParameterType.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:barPlus    completion:barPlus()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionUnresolvedFieldType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedFieldType.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:barPlus    completion:barPlus()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
		requestor.getResults());
}
/*
 * bug : http://dev.eclipse.org/bugs/show_bug.cgi?id=24440
 */
public void testCompletionUnresolvedEnclosingType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedEnclosingType.java");

	String str = cu.getSource();
	String completeBehind = "new ZZZ(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertTrue(
		requestor.getResults().length() == 0);
}
public void testCompletionReturnStatementIsParent1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionReturnStatementIsParent1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zz00    completion:zz00    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz00M    completion:zz00M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz01    completion:zz01    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz01M    completion:zz01M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz02    completion:zz02    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz02M    completion:zz02M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz10    completion:zz10    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz10M    completion:zz10M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz11    completion:zz11    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz11M    completion:zz11M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz12    completion:zz12    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz12M    completion:zz12M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz20    completion:zz20    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz20M    completion:zz20M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz21    completion:zz21    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz21M    completion:zz21M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz22    completion:zz22    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz22M    completion:zz22M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzOb    completion:zzOb    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzObM    completion:zzObM()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionReturnStatementIsParent2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionReturnStatementIsParent2.java");

	String str = cu.getSource();
	String completeBehind = "xx";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:XX00    completion:XX00    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX01    completion:XX01    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX02    completion:XX02    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX10    completion:XX10    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX11    completion:XX11    relevance:"+(R_DEFAULT + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:XX12    completion:XX12    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX20    completion:XX20    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX21    completion:XX21    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX22    completion:XX22    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionCastIsParent1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCastIsParent1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zz00    completion:zz00    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz00M    completion:zz00M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz01    completion:zz01    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz01M    completion:zz01M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz02    completion:zz02    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz02M    completion:zz02M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz10    completion:zz10    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz10M    completion:zz10M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz11    completion:zz11    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz11M    completion:zz11M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz12    completion:zz12    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz12M    completion:zz12M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz20    completion:zz20    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz20M    completion:zz20M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz21    completion:zz21    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz21M    completion:zz21M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zz22    completion:zz22    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zz22M    completion:zz22M()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzOb    completion:zzOb    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzObM    completion:zzObM()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionCastIsParent2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCastIsParent2.java");

	String str = cu.getSource();
	String completeBehind = "xx";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:XX00    completion:XX00    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX01    completion:XX01    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX02    completion:XX02    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX10    completion:XX10    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX11    completion:XX11    relevance:"+(R_DEFAULT + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:XX12    completion:XX12    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX20    completion:XX20    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX21    completion:XX21    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n" +
		"element:XX22    completion:XX22    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMessageSendIsParent1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMessageSendIsParent1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMessageSendIsParent2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMessageSendIsParent2.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMessageSendIsParent3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMessageSendIsParent3.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMessageSendIsParent4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMessageSendIsParent4.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMessageSendIsParent5() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMessageSendIsParent5.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionMessageSendIsParent6() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMessageSendIsParent6.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAllocationExpressionIsParent1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAllocationExpressionIsParent1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAllocationExpressionIsParent2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAllocationExpressionIsParent2.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAllocationExpressionIsParent3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAllocationExpressionIsParent3.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAllocationExpressionIsParent4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAllocationExpressionIsParent4.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAllocationExpressionIsParent5() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAllocationExpressionIsParent5.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAllocationExpressionIsParent6() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAllocationExpressionIsParent6.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionFieldInitializer1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFieldInitializer1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionFieldInitializer2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFieldInitializer2.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionFieldInitializer3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFieldInitializer3.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionFieldInitializer4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFieldInitializer4.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}
public void testCompletionVariableInitializerInInitializer1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInInitializer1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionVariableInitializerInInitializer2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInInitializer2.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionVariableInitializerInInitializer3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInInitializer3.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionVariableInitializerInInitializer4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInInitializer4.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}
public void testCompletionVariableInitializerInMethod1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInMethod1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionVariableInitializerInMethod2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInMethod2.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionVariableInitializerInMethod3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInMethod3.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionVariableInitializerInMethod4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableInitializerInMethod4.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}
public void testCompletionAssignmentInMethod1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAssignmentInMethod1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAssignmentInMethod2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAssignmentInMethod2.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzObject    completion:zzObject    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:zzboolean    completion:zzboolean    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzdouble    completion:zzdouble    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzint    completion:zzint    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:zzlong    completion:zzlong    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAssignmentInMethod3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAssignmentInMethod3.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}

public void testCompletionAssignmentInMethod4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAssignmentInMethod4.java");

	String str = cu.getSource();
	String completeBehind = "Objec";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=24565
*/
public void testCompletionObjectsMethodWithInterfaceReceiver() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionObjectsMethodWithInterfaceReceiver.java");

	String str = cu.getSource();
	String completeBehind = "hash";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:hashCode    completion:hashCode()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=24939
*/
public void testCompletionConstructorForAnonymousType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionConstructorForAnonymousType.java");

	String str = cu.getSource();
	String completeBehind = "TypeWithConstructor(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:TypeWithConstructor    completion:)    relevance:"+(R_DEFAULT + R_INTERESTING),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25221
*/
public void testCompletionEmptyTypeName1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionEmptyTypeName1.java");

	String str = cu.getSource();
	String completeBehind = "new ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:A    completion:A    relevance:" +(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:CompletionEmptyTypeName1    completion:CompletionEmptyTypeName1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25221
*/
public void testCompletionEmptyTypeName2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionEmptyTypeName2.java");

	String str = cu.getSource();
	String completeBehind = " = ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:A    completion:A    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:CompletionEmptyTypeName2    completion:CompletionEmptyTypeName2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:a    completion:a    relevance:"+(R_DEFAULT + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:clone    completion:clone()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:equals    completion:equals()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:getClass    completion:getClass()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:hashCode    completion:hashCode()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:toString    completion:toString()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25578
*/
public void testCompletionAbstractMethodRelevance1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAbstractMethodRelevance1.java");

	String str = cu.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:foo1    completion:public void foo1()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:foo2    completion:public void foo2()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_ABSTRACT_METHOD)+"\n" +
		"element:foo3    completion:public void foo3()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25578
*/
public void testCompletionAbstractMethodRelevance2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAbstractMethodRelevance2.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_ABSTRACT_METHOD)+"\n" +
		"element:equals    completion:public boolean equals(Object obj)    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25591
*/
public void testCompletionReturnInInitializer() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionReturnInInitializer.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:equals    completion:equals()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25811
*/
public void testCompletionVariableName1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableName1.java");

	String str = cu.getSource();
	String completeBehind = "TEST_FOO_MyClass ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:class1    completion:class1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:myClass    completion:myClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25811
*/
public void testCompletionVariableName2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableName2.java");

	String str = cu.getSource();
	String completeBehind = "Test_Bar_MyClass ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:bar_MyClass    completion:bar_MyClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:class1    completion:class1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:myClass    completion:myClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n" +
		"element:test_Bar_MyClass    completion:test_Bar_MyClass    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25820
*/
public void testCompletionExpectedTypeIsNotValid() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionExpectedTypeIsNotValid.java");

	String str = cu.getSource();
	String completeBehind = "new ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionExpectedTypeIsNotValid    completion:CompletionExpectedTypeIsNotValid    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25815
*/
public void testCompletionMemberType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMemberType.java");

	String str = cu.getSource();
	String completeBehind = "new ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionMemberType    completion:CompletionMemberType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
		"element:Y    completion:Y    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25815
*/
public void testCompletionVoidMethod() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVoidMethod.java");

	String str = cu.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:foo1    completion:foo1()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED)+"\n" +
		"element:foo3    completion:foo3()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25890
*/
public void testCompletionOnStaticMember1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionOnStaticMember1.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
			requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=25890
*/
public void testCompletionOnStaticMember2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionOnStaticMember2.java");

		String str = cu.getSource();
		String completeBehind = "method";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:method1    completion:method1()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:method2    completion:method2()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_UNQUALIFIED),
			requestor.getResults());
}
/*
* http://dev.eclipse.org/bugs/show_bug.cgi?id=26677
*/
public void testCompletionQualifiedExpectedType() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionQualifiedExpectedType.java");

		String str = cu.getSource();
		String completeBehind = "new ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:CompletionQualifiedExpectedType    completion:CompletionQualifiedExpectedType    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:PX    completion:pack2.PX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE),
			requestor.getResults());
}
public void testCompletionUnaryOperator1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnaryOperator1.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionUnaryOperator2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnaryOperator2.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionBinaryOperator1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionBinaryOperator1.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var4    completion:var4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
			requestor.getResults());
}
public void testCompletionBinaryOperator2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionBinaryOperator2.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionBinaryOperator3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionBinaryOperator3.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionInstanceofOperator1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionInstanceofOperator1.java");

		String str = cu.getSource();
		String completeBehind = "x instanceof WWWCompletionInstanceof";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:WWWCompletionInstanceof1    completion:WWWCompletionInstanceof1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXPECTED_TYPE)+"\n" +
			"element:WWWCompletionInstanceof2    completion:WWWCompletionInstanceof2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:WWWCompletionInstanceof3    completion:WWWCompletionInstanceof3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXPECTED_TYPE)+"\n" +
			"element:WWWCompletionInstanceof4    completion:WWWCompletionInstanceof4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionConditionalExpression1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionConditionalExpression1.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var4    completion:var4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionConditionalExpression2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionConditionalExpression2.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var4    completion:var4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionConditionalExpression3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionConditionalExpression3.java");

		String str = cu.getSource();
		String completeBehind = "var";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:var1    completion:var1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n" +
			"element:var2    completion:var2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var3    completion:var3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n" +
			"element:var4    completion:var4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordThis1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis1.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis2.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis3.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThis4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis4.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis5.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThis6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis6.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis7.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSuper1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper1.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSuper2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper2.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSuper3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper3.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordSuper4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper4.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSuper5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper5.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordSuper6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper6.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE) + "\n" +
			"element:super    completion:super()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTry1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTry1.java");

		String str = cu.getSource();
		String completeBehind = "tr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:try    completion:try    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTry2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTry2.java");

		String str = cu.getSource();
		String completeBehind = "tr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:true    completion:true    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTry3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTry3.java");

		String str = cu.getSource();
		String completeBehind = "try";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordDo1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDo1.java");

		String str = cu.getSource();
		String completeBehind = "do";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:do    completion:do    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME)+"\n"+
			"element:double    completion:double    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDo2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDo2.java");

		String str = cu.getSource();
		String completeBehind = "do";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:double    completion:double    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDo3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDo3.java");

		String str = cu.getSource();
		String completeBehind = "do";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:double    completion:double    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFor1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFor1.java");

		String str = cu.getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
			"element:for    completion:for    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFor2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFor2.java");

		String str = cu.getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFor3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFor3.java");

		String str = cu.getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordIf1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordIf1.java");

		String str = cu.getSource();
		String completeBehind = "if";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:if    completion:if    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
			requestor.getResults());
}
public void testCompletionKeywordIf2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordIf2.java");

		String str = cu.getSource();
		String completeBehind = "if";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordIf3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordIf3.java");

		String str = cu.getSource();
		String completeBehind = "if";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordReturn1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordReturn1.java");

		String str = cu.getSource();
		String completeBehind = "re";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:return    completion:return    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordReturn2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordReturn2.java");

		String str = cu.getSource();
		String completeBehind = "re";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordReturn3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordReturn3.java");

		String str = cu.getSource();
		String completeBehind = "re";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSwitch1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSwitch1.java");

		String str = cu.getSource();
		String completeBehind = "sw";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:switch    completion:switch    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSwitch2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSwitch2.java");

		String str = cu.getSource();
		String completeBehind = "sw";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSwitch3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSwitch3.java");

		String str = cu.getSource();
		String completeBehind = "sw";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThrow1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrow1.java");

		String str = cu.getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Throwable    completion:Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:throw    completion:throw    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrow2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrow2.java");

		String str = cu.getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Throwable    completion:Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordThrow3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrow3.java");

		String str = cu.getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Throwable    completion:Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordAssert1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAssert1.java");

		String str = cu.getSource();
		String completeBehind = "as";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:assert    completion:assert    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAssert2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAssert2.java");

		String str = cu.getSource();
		String completeBehind = "as";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordAssert3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAssert3.java");

		String str = cu.getSource();
		String completeBehind = "as";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordElse1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse1.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:else    completion:else    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordElse2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse2.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordElse3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse3.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordElse4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse4.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch1.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:catch    completion:catch    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCatch2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch2.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch3.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch4.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch5.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:catch    completion:catch    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:catchz    completion:catchz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFinally1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally1.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finally    completion:finally    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinally2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally2.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinally3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally3.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinally4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally4.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinally5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally5.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finally    completion:finally    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinally6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally6.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finally    completion:finally    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finallyz    completion:finallyz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFinally7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally7.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finallyz    completion:finallyz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordContinue1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordContinue1.java");

		String str = cu.getSource();
		String completeBehind = "cont";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:continue    completion:continue    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordContinue2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordContinue2.java");

		String str = cu.getSource();
		String completeBehind = "cont";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordBreak1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordBreak1.java");

		String str = cu.getSource();
		String completeBehind = "bre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:break    completion:break    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordBreak2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordBreak2.java");

		String str = cu.getSource();
		String completeBehind = "bre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordBreak3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordBreak3.java");

		String str = cu.getSource();
		String completeBehind = "bre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:break    completion:break    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordWhile1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile1.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:while    completion:while    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordWhile2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile2.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordWhile3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile3.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordWhile4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile4.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:while    completion:while    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordWhile5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile5.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:while    completion:while    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordExtends1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends1.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:extends    completion:extends    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordExtends2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends2.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordExtends3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends3.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordExtends4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends4.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:extends    completion:extends    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordExtends5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends5.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordImplements1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImplements1.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:implements    completion:implements    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImplements2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImplements2.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:implements    completion:implements    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImplements3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImplements3.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPackage1.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:package    completion:package    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPackage2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "p", "CompletionKeywordPackage2.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPackage3.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPackage4.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordImport1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImport1.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:import    completion:import    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImport2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "p", "CompletionKeywordImport2.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:import    completion:import    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImport3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImport3.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:import    completion:import    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImport4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImport4.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCase1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase1.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase2.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase3.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase4.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase5.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordDefault1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault1.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:default    completion:default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDefault2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault2.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:default    completion:default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDefault3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault3.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:default    completion:default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDefault4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault4.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordDefault5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault5.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordClass1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass1.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass2.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass3.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass4.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass5.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass6.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass7.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass8.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass9.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass10.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass11.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass12.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface1.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface2.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface3.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface4.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface5.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface6.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface7.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface8.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface9.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrows1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows1.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:throws    completion:throws    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrows2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows2.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThrows3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows3.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:throws    completion:throws    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrows4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows4.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:throws    completion:throws    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized1.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:synchronized    completion:synchronized    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized2.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSynchronized3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized3.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:synchronized    completion:synchronized    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized4.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSynchronized5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized5.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:synchronized    completion:synchronized    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized6.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNative1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative1.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:native    completion:native    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNative2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative2.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNative3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative3.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:native    completion:native    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNative4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative4.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordStrictfp1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp1.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:strictfp    completion:strictfp    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStrictfp2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp2.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordStrictfp3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp3.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:strictfp    completion:strictfp    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStrictfp4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp4.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordVolatile1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile1.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:volatile    completion:volatile    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordVolatile2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile2.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordVolatile3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile3.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:volatile    completion:volatile    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordVolatile4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile4.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordTransient1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient1.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:transient    completion:transient    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTransient2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient2.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordTransient3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient3.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:transient    completion:transient    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTransient4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient4.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNew1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew1.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew2.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew3.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew4.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew5.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew6.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew7.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew8.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic1.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic2.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic3.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic4.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordStatic5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic5.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic1.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic2.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic3.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic4.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic5.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic6.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic7.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic8.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic9.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic10.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPrivate1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate1.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:private    completion:private    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate2.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:private    completion:private    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate3.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:private    completion:private    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate4.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPrivate5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate5.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordProtected1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected1.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:protected    completion:protected    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordProtected2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected2.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:protected    completion:protected    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordProtected3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected3.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:protected    completion:protected    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordProtected4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected4.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordProtected5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected5.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinal1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal1.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal2.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinal3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal3.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal4.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal5.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finalize    completion:protected void finalize() throws Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal6.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinal7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal7.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finalize    completion:protected void finalize() throws Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal8.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal9.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finalize    completion:finalize()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE +R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordAbstract1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract1.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract2.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordAbstract3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract3.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract4.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract5.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract6.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordAbstract7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract7.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract8.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTrue1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTrue1.java");

		String str = cu.getSource();
		String completeBehind = "tru";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordTrue2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTrue2.java");

		String str = cu.getSource();
		String completeBehind = "tru";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:true    completion:true    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFalse1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFalse1.java");

		String str = cu.getSource();
		String completeBehind = "fal";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFalse2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFalse2.java");

		String str = cu.getSource();
		String completeBehind = "fal";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:false    completion:false    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNull1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNull1.java");

		String str = cu.getSource();
		String completeBehind = "nul";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNull2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNull2.java");

		String str = cu.getSource();
		String completeBehind = "nul";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:null    completion:null    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInstanceof1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInstanceof1.java");

		String str = cu.getSource();
		String completeBehind = "ins";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:instanceof    completion:instanceof    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInstanceof2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInstanceof2.java");

		String str = cu.getSource();
		String completeBehind = "ins";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordInstanceof3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInstanceof3.java");

		String str = cu.getSource();
		String completeBehind = "ins";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}

public void testCompletionKeywordThis8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis8.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis9.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis10.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThis11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis11.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis12.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThis13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis13.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:this    completion:this    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThis14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThis14.java");

		String str = cu.getSource();
		String completeBehind = "thi";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSuper7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper7.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSuper8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper8.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSuper9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper9.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordSuper10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper10.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSuper11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper11.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordSuper12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSuper12.java");

		String str = cu.getSource();
		String completeBehind = "sup";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:SuperClass    completion:SuperClass    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:SuperInterface    completion:SuperInterface    relevance:" + (R_DEFAULT + R_INTERESTING + R_UNQUALIFIED) + "\n" +
			"element:super    completion:super    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE) + "\n" +
			"element:super    completion:super()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTry4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTry4.java");

		String str = cu.getSource();
		String completeBehind = "tr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:try    completion:try    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTry5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTry5.java");

		String str = cu.getSource();
		String completeBehind = "tr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:true    completion:true    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTry6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTry6.java");

		String str = cu.getSource();
		String completeBehind = "try";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordDo4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDo4.java");

		String str = cu.getSource();
		String completeBehind = "do";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:do    completion:do    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME)+"\n"+
			"element:double    completion:double    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDo5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDo5.java");

		String str = cu.getSource();
		String completeBehind = "do";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:double    completion:double    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDo6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDo6.java");

		String str = cu.getSource();
		String completeBehind = "do";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:double    completion:double    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFor4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFor4.java");

		String str = cu.getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
			"element:for    completion:for    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFor5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFor5.java");

		String str = cu.getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFor6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFor6.java");

		String str = cu.getSource();
		String completeBehind = "fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordIf4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordIf4.java");

		String str = cu.getSource();
		String completeBehind = "if";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:if    completion:if    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
			requestor.getResults());
}
public void testCompletionKeywordIf5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordIf5.java");

		String str = cu.getSource();
		String completeBehind = "if";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordIf6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordIf6.java");

		String str = cu.getSource();
		String completeBehind = "if";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordReturn4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordReturn4.java");

		String str = cu.getSource();
		String completeBehind = "re";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:return    completion:return    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordReturn5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordReturn5.java");

		String str = cu.getSource();
		String completeBehind = "re";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordReturn6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordReturn6.java");

		String str = cu.getSource();
		String completeBehind = "re";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSwitch4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSwitch4.java");

		String str = cu.getSource();
		String completeBehind = "sw";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:switch    completion:switch    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSwitch5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSwitch5.java");

		String str = cu.getSource();
		String completeBehind = "sw";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSwitch6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSwitch6.java");

		String str = cu.getSource();
		String completeBehind = "sw";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThrow4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrow4.java");

		String str = cu.getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Throwable    completion:Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:throw    completion:throw    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrow5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrow5.java");

		String str = cu.getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Throwable    completion:Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordThrow6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrow6.java");

		String str = cu.getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Throwable    completion:Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordAssert4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAssert4.java");

		String str = cu.getSource();
		String completeBehind = "as";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:assert    completion:assert    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAssert5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAssert5.java");

		String str = cu.getSource();
		String completeBehind = "as";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordAssert6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAssert6.java");

		String str = cu.getSource();
		String completeBehind = "as";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordElse5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse5.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:else    completion:else    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordElse6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse6.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordElse7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse7.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordElse8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordElse8.java");

		String str = cu.getSource();
		String completeBehind = "els";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch6.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:catch    completion:catch    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCatch7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch7.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch8.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch9.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordCatch10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCatch10.java");

		String str = cu.getSource();
		String completeBehind = "cat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:catch    completion:catch    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:catchz    completion:catchz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFinally8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally8.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finally    completion:finally    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinally9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally9.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinally10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally10.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinally11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally11.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinally12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally12.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finally    completion:finally    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinally13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally13.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finally    completion:finally    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finallyz    completion:finallyz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFinally14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinally14.java");

		String str = cu.getSource();
		String completeBehind = "finall";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:finallyz    completion:finallyz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordContinue3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordContinue3.java");

		String str = cu.getSource();
		String completeBehind = "cont";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:continue    completion:continue    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordContinue4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordContinue4.java");

		String str = cu.getSource();
		String completeBehind = "cont";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordBreak4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordBreak4.java");

		String str = cu.getSource();
		String completeBehind = "bre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:break    completion:break    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordBreak5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordBreak5.java");

		String str = cu.getSource();
		String completeBehind = "bre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordBreak6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordBreak6.java");

		String str = cu.getSource();
		String completeBehind = "bre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:break    completion:break    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordWhile6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile6.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:while    completion:while    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordWhile7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile7.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordWhile8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile8.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordWhile9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile9.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:while    completion:while    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordWhile10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordWhile10.java");

		String str = cu.getSource();
		String completeBehind = "wh";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:while    completion:while    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordExtends6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends6.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:extends    completion:extends    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordExtends7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends7.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordExtends8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends8.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordExtends9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends9.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:extends    completion:extends    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordExtends10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordExtends10.java");

		String str = cu.getSource();
		String completeBehind = "ext";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordImplements4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImplements4.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:implements    completion:implements    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImplements5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImplements5.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:implements    completion:implements    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImplements6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImplements6.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPackage5.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPackage6.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPackage7.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPackage8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "p", "CompletionKeywordPackage8.java");

		String str = cu.getSource();
		String completeBehind = "pac";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordImport5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImport5.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:import    completion:import    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImport6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImport6.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordImport7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordImport7.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:import    completion:import    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordImport8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "p", "CompletionKeywordImport8.java");

		String str = cu.getSource();
		String completeBehind = "imp";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:import    completion:import    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase6.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase7.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase8.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase9.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:case    completion:case    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordCase10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordCase10.java");

		String str = cu.getSource();
		String completeBehind = "cas";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordDefault6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault6.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:default    completion:default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDefault7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault7.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:default    completion:default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDefault8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault8.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:default    completion:default    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordDefault9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault9.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordDefault10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordDefault10.java");

		String str = cu.getSource();
		String completeBehind = "def";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordClass13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass13.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass14.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass15() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass15.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass16() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass16.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass17() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass17.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass18() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass18.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass19() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass19.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass20() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass20.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass21() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass21.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass22() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass22.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass23() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass23.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordClass24() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordClass24.java");

		String str = cu.getSource();
		String completeBehind = "cla";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:Class    completion:Class    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:ClassWithComplexName    completion:ClassWithComplexName    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
			"element:class    completion:class    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface1.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface11.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface12.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface13.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface14.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface15() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface15.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface16() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface16.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface17() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface17.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInterface18() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInterface18.java");

		String str = cu.getSource();
		String completeBehind = "interf";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:interface    completion:interface    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrows5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows5.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:throws    completion:throws    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrows6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows6.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordThrows7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows7.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:throws    completion:throws    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordThrows8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordThrows8.java");

		String str = cu.getSource();
		String completeBehind = "thro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:throws    completion:throws    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized7.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:synchronized    completion:synchronized    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized8.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSynchronized9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized9.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:synchronized    completion:synchronized    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized10.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordSynchronized11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized11.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:synchronized    completion:synchronized    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordSynchronized12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordSynchronized12.java");

		String str = cu.getSource();
		String completeBehind = "syn";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNative5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative5.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:native    completion:native    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNative6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative6.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNative7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative7.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:native    completion:native    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNative8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNative8.java");

		String str = cu.getSource();
		String completeBehind = "nat";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordStrictfp5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp5.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:strictfp    completion:strictfp    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStrictfp6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp6.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordStrictfp7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp7.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:strictfp    completion:strictfp    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStrictfp8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStrictfp8.java");

		String str = cu.getSource();
		String completeBehind = "stric";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordVolatile5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile5.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:volatile    completion:volatile    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordVolatile6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile6.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordVolatile7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile7.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:volatile    completion:volatile    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordVolatile8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordVolatile8.java");

		String str = cu.getSource();
		String completeBehind = "vol";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordTransient5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient5.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:transient    completion:transient    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTransient6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient6.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordTransient7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient7.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:transient    completion:transient    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTransient8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTransient8.java");

		String str = cu.getSource();
		String completeBehind = "tran";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNew9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew9.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew10.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew11.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew12.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew13.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew14.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew15() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew15.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNew16() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNew16.java");

		String str = cu.getSource();
		String completeBehind = "ne";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:new    completion:new    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic6.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic7.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic8.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordStatic9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic9.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordStatic10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordStatic10.java");

		String str = cu.getSource();
		String completeBehind = "sta";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:static    completion:static    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic20() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic10.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic11.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic12.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic13.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic14.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic15() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic15.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic16() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic16.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPublic17() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic17.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic18() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic18.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPublic19() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPublic19.java");

		String str = cu.getSource();
		String completeBehind = "pub";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:public    completion:public    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate6.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:private    completion:private    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate7.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:private    completion:private    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate8.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:private    completion:private    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordPrivate9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate9.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordPrivate10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordPrivate10.java");

		String str = cu.getSource();
		String completeBehind = "pri";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordProtected6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected6.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:protected    completion:protected    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordProtected7() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected7.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:protected    completion:protected    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordProtected8() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected8.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:protected    completion:protected    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordProtected9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected9.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordProtected10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordProtected10.java");

		String str = cu.getSource();
		String completeBehind = "pro";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinal18() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal18.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finalize    completion:finalize()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionKeywordFinal10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal10.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal11.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinal12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal12.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal13.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal14.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finalize    completion:protected void finalize() throws Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal15() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal15.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFinal16() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal16.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
			"element:finalize    completion:protected void finalize() throws Throwable    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFinal17() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFinal17.java");

		String str = cu.getSource();
		String completeBehind = "fin";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:final    completion:final    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract9() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract9.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract10() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract10.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordAbstract11() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract11.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract12() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract12.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract13() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract13.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract14() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract14.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordAbstract15() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract15.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordAbstract16() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordAbstract16.java");

		String str = cu.getSource();
		String completeBehind = "abs";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:abstract    completion:abstract    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordTrue3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTrue3.java");

		String str = cu.getSource();
		String completeBehind = "tru";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordTrue4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordTrue4.java");

		String str = cu.getSource();
		String completeBehind = "tru";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:true    completion:true    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordFalse3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFalse3.java");

		String str = cu.getSource();
		String completeBehind = "fal";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordFalse4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordFalse4.java");

		String str = cu.getSource();
		String completeBehind = "fal";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:false    completion:false    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordNull3() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNull3.java");

		String str = cu.getSource();
		String completeBehind = "nul";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordNull4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordNull4.java");

		String str = cu.getSource();
		String completeBehind = "nul";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:null    completion:null    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInstanceof4() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInstanceof4.java");

		String str = cu.getSource();
		String completeBehind = "ins";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:instanceof    completion:instanceof    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
			requestor.getResults());
}
public void testCompletionKeywordInstanceof5() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInstanceof5.java");

		String str = cu.getSource();
		String completeBehind = "ins";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionKeywordInstanceof6() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src2", "", "CompletionKeywordInstanceof6.java");

		String str = cu.getSource();
		String completeBehind = "ins";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"",
			requestor.getResults());
}
public void testCompletionMemberType2() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMemberType2.java");

		String str = cu.getSource();
		String completeBehind = "new ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:CompletionMemberType2    completion:CompletionMemberType2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
			"element:CompletionMemberType2.MemberException    completion:MemberException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXCEPTION),
			requestor.getResults());
}
public void testCompletionAfterCase1() throws JavaModelException {
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAfterCase1.java");

		String str = cu.getSource();
		String completeBehind = "zz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:zzz    completion:zzz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionAfterCase2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAfterCase2.java");

		String str = cu.getSource();
		String completeBehind = "zz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:zzz    completion:zzz    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
			requestor.getResults());
}
public void testCompletionToplevelType1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "p3", "CompletionToplevelType1.java");

		String str = cu.getSource();
		String completeBehind = "CompletionToplevelType1";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:CompletionToplevelType1    completion:CompletionToplevelType1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_NAME),
			requestor.getResults());
}
public void testCompletionCatchArgumentName2() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Object argumentPrefixPreviousValue = options.get(JavaCore.CODEASSIST_ARGUMENT_PREFIXES);
	options.put(JavaCore.CODEASSIST_ARGUMENT_PREFIXES,"arg"); //$NON-NLS-1$
	Object localPrefixPreviousValue = options.get(JavaCore.CODEASSIST_LOCAL_PREFIXES);
	options.put(JavaCore.CODEASSIST_LOCAL_PREFIXES,"loc"); //$NON-NLS-1$
	
	JavaCore.setOptions(options);

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCatchArgumentName2.java");

	String str = cu.getSource();
	String completeBehind = "Exception ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	options.put(JavaCore.CODEASSIST_ARGUMENT_PREFIXES,argumentPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_LOCAL_PREFIXES,localPrefixPreviousValue);
	JavaCore.setOptions(options);

	assertEquals(
		"element:exception    completion:exception    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:locException    completion:locException    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX),
		requestor.getResults());
}
public void testCompletionArrayAccess1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionArrayAccess1.java");

	String str = cu.getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzz1    completion:zzz1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:zzz2    completion:zzz2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE +R_UNQUALIFIED),
		requestor.getResults());
}
public void testCompletionVariableName3() throws JavaModelException {
	Hashtable options = JavaCore.getOptions();
	Object argumentPrefixPreviousValue = options.get(JavaCore.CODEASSIST_LOCAL_PREFIXES);
	options.put(JavaCore.CODEASSIST_LOCAL_PREFIXES,"p1,p2"); //$NON-NLS-1$
	Object localPrefixPreviousValue = options.get(JavaCore.CODEASSIST_LOCAL_SUFFIXES);
	options.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES,"s1,s2"); //$NON-NLS-1$
	
	JavaCore.setOptions(options);

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableName3.java");

	String str = cu.getSource();
	String completeBehind = "OneName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	options.put(JavaCore.CODEASSIST_LOCAL_PREFIXES,argumentPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES,localPrefixPreviousValue);
	JavaCore.setOptions(options);

	assertEquals(
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:names1    completion:names1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_SUFFIX)+"\n"+
		"element:names2    completion:names2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_SUFFIX)+"\n"+
		"element:oneName    completion:oneName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE)+"\n"+
		"element:oneNames1    completion:oneNames1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_SUFFIX)+"\n"+
		"element:oneNames2    completion:oneNames2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_SUFFIX)+"\n"+
		"element:p1Name    completion:p1Name    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX)+"\n"+
		"element:p1Names1    completion:p1Names1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX + R_NAME_FIRST_SUFFIX)+"\n"+
		"element:p1Names2    completion:p1Names2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX + R_NAME_SUFFIX)+"\n"+
		"element:p1OneName    completion:p1OneName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX)+"\n"+
		"element:p1OneNames1    completion:p1OneNames1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX + R_NAME_FIRST_SUFFIX)+"\n"+
		"element:p1OneNames2    completion:p1OneNames2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_FIRST_PREFIX + R_NAME_SUFFIX)+"\n"+
		"element:p2Name    completion:p2Name    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_PREFIX)+"\n"+
		"element:p2Names1    completion:p2Names1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_PREFIX + R_NAME_FIRST_SUFFIX)+"\n"+
		"element:p2Names2    completion:p2Names2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_PREFIX + R_NAME_SUFFIX)+"\n"+
		"element:p2OneName    completion:p2OneName    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_PREFIX)+"\n"+
		"element:p2OneNames1    completion:p2OneNames1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_PREFIX + R_NAME_FIRST_SUFFIX)+"\n"+
		"element:p2OneNames2    completion:p2OneNames2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NAME_PREFIX + R_NAME_SUFFIX),
		requestor.getResults());
}
public void testCompletionNonEmptyToken1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionNonEmptyToken1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	int start = cursorLocation - 2;
	int end = start + 4;
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:zzyy    completion:zzyy    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResultsWithPosition());
}
public void testCompletionEmptyToken1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionEmptyToken1.java");

	String str = cu.getSource();
	String completeBehind = "zz";
	// completion is just at start of 'zz'
	int cursorLocation = str.lastIndexOf(completeBehind);
	int start = cursorLocation;
	int end = start + 4;
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionEmptyToken1    completion:CompletionEmptyToken1    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:clone    completion:clone()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:equals    completion:equals()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:finalize    completion:finalize()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:foo    completion:foo()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:getClass    completion:getClass()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:hashCode    completion:hashCode()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:notify    completion:notify()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:notifyAll    completion:notifyAll()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:toString    completion:toString()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:wait    completion:wait()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:wait    completion:wait()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:wait    completion:wait()    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:zzyy    completion:zzyy    position:["+start+","+end+"]    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResultsWithPosition());
}
public void testCompletionFindSecondaryType1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindSecondaryType1.java");

	String str = cu.getSource();
	String completeBehind = "/**/Secondary";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:SecondaryType1    completion:SecondaryType1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:SecondaryType2    completion:SecondaryType2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
public void testCompletionLocalType1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionLocalType1.java");

	String str = cu.getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:ZZZZ    completion:ZZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
}
