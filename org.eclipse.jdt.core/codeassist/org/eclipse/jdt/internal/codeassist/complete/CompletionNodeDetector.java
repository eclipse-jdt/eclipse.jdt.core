/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Detect the presence of a node in expression
 */
public class CompletionNodeDetector extends AbstractSyntaxTreeVisitorAdapter {
	private AstNode searchedNode;
	private AstNode parent;
	private boolean result;
	
	public CompletionNodeDetector(AstNode searchedNode, AstNode visitedAst){
		this.searchedNode = searchedNode;
		this.result = false;
		
		if(searchedNode != null && visitedAst != null) {
			visitedAst.traverse(this, null);
		}
	}
	
	public boolean containsCompletionNode() {
		return result;
	}
	
	public AstNode getCompletionNodeParent() {
		return parent;
	}
	public void endVisit(AllocationExpression allocationExpression, BlockScope scope) {
		endVisit(allocationExpression);
	}
	public void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		endVisit(and_and_Expression);
	}
	public void endVisit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope) {
		endVisit(arrayAllocationExpression);
	}
	public void endVisit(ArrayInitializer arrayInitializer, BlockScope scope) {
		endVisit(arrayInitializer);
	}
	public void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope) {
		endVisit(arrayQualifiedTypeReference);
	}
	public void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope) {
		endVisit(arrayQualifiedTypeReference);
	}
	public void endVisit(ArrayReference arrayReference, BlockScope scope) {
		endVisit(arrayReference);
	}
	public void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		endVisit(arrayTypeReference);
	}
	public void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		endVisit(arrayTypeReference);
	}
	public void endVisit(Assignment assignment, BlockScope scope) {
		endVisit(assignment);
	}
	public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
		endVisit(binaryExpression);
	}
	public void endVisit(CastExpression castExpression, BlockScope scope) {
		endVisit(castExpression);
	}
	public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
		endVisit(compoundAssignment);
	}
	public void endVisit(ConditionalExpression conditionalExpression, BlockScope scope) {
		endVisit(conditionalExpression);
	}
	public void endVisit(EqualExpression equalExpression, BlockScope scope) {
		endVisit(equalExpression);
	}
	public void endVisit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
		endVisit(explicitConstructor);
	}
	public void endVisit(FieldReference fieldReference, BlockScope scope) {
		endVisit(fieldReference);
	}
	public void endVisit(InstanceOfExpression instanceOfExpression, BlockScope scope) {
		endVisit(instanceOfExpression);
	}
	public void endVisit(MessageSend messageSend, BlockScope scope) {
		endVisit(messageSend);
	}
	public void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		endVisit(or_or_Expression);
	}
	public void endVisit(PostfixExpression postfixExpression, BlockScope scope) {
		endVisit(postfixExpression);
	}
	public void endVisit(PrefixExpression prefixExpression, BlockScope scope) {
		endVisit(prefixExpression);
	}
	public void endVisit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
		endVisit(qualifiedAllocationExpression);
	}
	public void endVisit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
		endVisit(qualifiedNameReference);
	}
	public void endVisit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope) {
		endVisit(qualifiedSuperReference);
	}
	public void endVisit(QualifiedThisReference qualifiedThisReference, BlockScope scope) {
		endVisit(qualifiedThisReference);
	}
	public void endVisit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope) {
		endVisit(qualifiedTypeReference);
	}
	public void endVisit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
		endVisit(qualifiedTypeReference);
	}
	public void endVisit(SingleNameReference singleNameReference, BlockScope scope) {
		endVisit(singleNameReference);
	}
	public void endVisit(SingleTypeReference singleTypeReference, BlockScope scope) {
		endVisit(singleTypeReference);
	}
	public void endVisit(SingleTypeReference singleTypeReference, ClassScope scope) {
		endVisit(singleTypeReference);
	}
	public void endVisit(SuperReference superReference, BlockScope scope) {
		endVisit(superReference);
	}
	public void endVisit(ThisReference thisReference, BlockScope scope) {
		endVisit(thisReference);
	}
	public void endVisit(UnaryExpression unaryExpression, BlockScope scope) {
		endVisit(unaryExpression);
	}
	public boolean visit(AllocationExpression allocationExpression, BlockScope scope) {
		return this.visit(allocationExpression);
	}
	public boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		return this.visit(and_and_Expression);
	}
	public boolean visit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope) {
		return this.visit(arrayAllocationExpression);
	}
	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		return this.visit(arrayInitializer);
	}
	public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope) {
		return this.visit(arrayQualifiedTypeReference);
	}
	public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope) {
		return this.visit(arrayQualifiedTypeReference);
	}
	public boolean visit(ArrayReference arrayReference, BlockScope scope) {
		return this.visit(arrayReference);
	}
	public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		return this.visit(arrayTypeReference);
	}
	public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		return this.visit(arrayTypeReference);
	}
	public boolean visit(Assignment assignment, BlockScope scope) {
		return this.visit(assignment);
	}
	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		return this.visit(binaryExpression);
	}
	public boolean visit(CastExpression castExpression, BlockScope scope) {
		return this.visit(castExpression);
	}
	public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
		return this.visit(compoundAssignment);
	}
	public boolean visit(ConditionalExpression conditionalExpression, BlockScope scope) {
		return this.visit(conditionalExpression);
	}
	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		return this.visit(equalExpression);
	}
	public boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
		return this.visit(explicitConstructor);
	}
	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		return this.visit(fieldReference);
	}
	public boolean visit(InstanceOfExpression instanceOfExpression, BlockScope scope) {
		return this.visit(instanceOfExpression);
	}
	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return this.visit(messageSend);
	}
	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		return this.visit(or_or_Expression);
	}
	public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
		return this.visit(postfixExpression);
	}
	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		return this.visit(prefixExpression);
	}
	public boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
		return this.visit(qualifiedAllocationExpression);
	}
	public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
		return this.visit(qualifiedNameReference);
	}
	public boolean visit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope) {
		return this.visit(qualifiedSuperReference);
	}
	public boolean visit(QualifiedThisReference qualifiedThisReference, BlockScope scope) {
		return this.visit(qualifiedThisReference);
	}
	public boolean visit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope) {
		return this.visit(qualifiedTypeReference);
	}
	public boolean visit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
		return this.visit(qualifiedTypeReference);
	}
	public boolean visit(SingleNameReference singleNameReference, BlockScope scope) {
		return this.visit(singleNameReference);
	}
	public boolean visit(SingleTypeReference singleTypeReference, BlockScope scope) {
		return this.visit(singleTypeReference);
	}
	public boolean visit(SingleTypeReference singleTypeReference, ClassScope scope) {
		return this.visit(singleTypeReference);
	}
	public boolean visit(SuperReference superReference, BlockScope scope) {
		return this.visit(superReference);
	}
	public boolean visit(ThisReference thisReference, BlockScope scope) {
		return this.visit(thisReference);
	}
	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		return this.visit(unaryExpression);
	}
	
	private void endVisit(AstNode astNode) {
		if(result && parent == null && astNode != searchedNode) {
			if(!(astNode instanceof AllocationExpression && ((AllocationExpression) astNode).type == searchedNode)
				&& !(astNode instanceof ConditionalExpression && ((ConditionalExpression) astNode).valueIfTrue == searchedNode)
				&& !(astNode instanceof ConditionalExpression && ((ConditionalExpression) astNode).valueIfFalse == searchedNode)) {
				parent = astNode;	
			}
		}
	}
	private boolean visit(AstNode astNode) {
		if(astNode == searchedNode) {
			result = true;
		}
		return !result;
	}
}
