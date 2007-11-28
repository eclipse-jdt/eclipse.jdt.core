/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * An ast visitor used to refine match locating to some specified fine grained flags.
 */
class FineGrainVisitor extends ASTVisitor implements IJavaSearchConstants {

	/**
	 * Mask of flags implying the usage of this visitor. For other ones, the refining
	 * is done during the parse of the syntax (see {@link MatchLocatorParser}).
	 */
	final static int FINE_GRAIN_MASK =
		MatchLocatorParser.UNSPECIFIED_REFERENCE_FINE_GRAIN_MASK |
		MatchLocatorParser.FORMAL_PARAMETER_FINE_GRAIN_MASK |
		MatchLocatorParser.GENERIC_FINE_GRAIN_MASK |
		ALLOCATION_EXPRESSION_TYPE_REFERENCE |
		FIELD_DECLARATION_TYPE_REFERENCE |
		LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE |
		IMPORT_DECLARATION_TYPE_REFERENCE |
		THROWS_CLAUSE_TYPE_REFERENCE |
		RETURN_TYPE_REFERENCE;

	final MatchLocator matchLocator;

	IJavaElement element;
	Binding binding;
	MatchingNodeSet nodeSet;
	ASTNode[] matchingNodes;
	PatternLocator locator;
	int length;
	final int fineGrain;
	boolean completed = false;

	int parameterizedTypeRef = 0;
	int wildcardTypeRef = 0;

FineGrainVisitor(IJavaElement enclosingElement, Binding binding, MatchingNodeSet set, ASTNode[] nodes, PatternLocator patternLocator, MatchLocator matchLocator) {
	this.matchLocator = matchLocator;
	this.element = enclosingElement;
	this.binding = binding;
	this.nodeSet = set;
	this.matchingNodes = nodes;
	this.locator = patternLocator;
	this.length = nodes.length;
	this.fineGrain = patternLocator.fineGrain();
}

protected void report(ASTNode node, Scope scope) {
	for (int i = 0; i < this.length; i++) {
		if (this.matchingNodes[i] == node) {
			Integer level = (Integer) this.nodeSet.matchingNodes.removeKey(node);
			if (level != null) {
				try {
					this.locator.matchReportReference(node, this.element, this.binding, scope, level.intValue(), this.matchLocator);
				} catch (CoreException e) {
					// skip
				}
				this.completed = this.nodeSet.matchingNodes.elementSize == 0;
			}
			return;
		}
	}
}

public boolean visit(AllocationExpression allocationExpression, BlockScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & ALLOCATION_EXPRESSION_TYPE_REFERENCE) != 0) {
        report(allocationExpression.type, scope);
	}
	return !this.completed;
}

public boolean visit(CastExpression castExpression, BlockScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & CAST_TYPE_REFERENCE) != 0) {
		if (castExpression.type instanceof TypeReference) {
	        report(castExpression.type, scope);
        }
		else if (castExpression.type instanceof NameReference) {
	        report(castExpression.type, scope);
        }
	}
	return !this.completed;
}

public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & IMPORT_DECLARATION_TYPE_REFERENCE) != 0) {
        report(importRef, scope);
	}
	return !this.completed;
}

public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE) != 0) {
        report(localDeclaration.type, scope);
	}
	return !this.completed;
}

public boolean visit(SingleNameReference singleNameReference, BlockScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & IMPLICIT_THIS_REFERENCE) != 0) {
        report(singleNameReference, scope);
	}
	return !completed;
}

public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & PARAMETER_DECLARATION_TYPE_REFERENCE) != 0) {
		Argument[] arguments = methodDeclaration.arguments;
		if (arguments != null) {
			int argLength = arguments.length;
			for (int i=0; i<argLength; i++) {
				report(arguments[i].type, scope);
			}
		}
	}
	if ((this.fineGrain & THROWS_CLAUSE_TYPE_REFERENCE) != 0) {
		TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
		if (thrownExceptions != null) {
			int thrownLength = thrownExceptions.length;
			for (int i=0; i<thrownLength; i++) {
				report(thrownExceptions[i], scope);
				if (this.completed) return false;
			}
		}
	}
	if ((this.fineGrain & RETURN_TYPE_REFERENCE) != 0) {
		report(methodDeclaration.returnType, scope);
	}
	return !this.completed;
}

public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & PARAMETER_DECLARATION_TYPE_REFERENCE) != 0) {
		Argument[] arguments = constructorDeclaration.arguments;
		if (arguments != null) {
			int argLength = arguments.length;
			for (int i=0; i<argLength; i++) {
				report(arguments[i].type, scope);
				if (this.completed) return false;
			}
		}
	}
	if ((this.fineGrain & THROWS_CLAUSE_TYPE_REFERENCE) != 0) {
		TypeReference[] thrownExceptions = constructorDeclaration.thrownExceptions;
		if (thrownExceptions != null) {
			int thrownLength = thrownExceptions.length;
			for (int i=0; i<thrownLength; i++) {
				report(thrownExceptions[i], scope);
				if (this.completed) return false;
			}
		}
	}
	return !this.completed;
}

public boolean visit(QualifiedNameReference qualifiedNameReference, BlockScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & QUALIFIED_REFERENCE) != 0) {
        report(qualifiedNameReference, scope);
	}
	return !this.completed;
}

public boolean visit(MessageSend messageSend, BlockScope scope) {
	if (this.completed) return false;
	if (messageSend.isSuperAccess()) {
		if ((this.fineGrain & SUPER_REFERENCE) != 0) {
			report(messageSend, scope);
		}
	} else if (messageSend.receiver.isImplicitThis()) {
		if ((this.fineGrain & IMPLICIT_THIS_REFERENCE) != 0) {
			report(messageSend, scope);
		}
	} else if (messageSend.receiver.isThis()) {
		if ((this.fineGrain & THIS_REFERENCE) != 0) {
			report(messageSend, scope);
		}
	} else {
		if ((this.fineGrain & QUALIFIED_REFERENCE) != 0) {
			report(messageSend, scope);
		}
	}
	if (messageSend.typeArguments !=  null && messageSend.typeArguments.length > 0) {
		this.parameterizedTypeRef++;
	}
	return !this.completed;
}

public void endVisit(MessageSend messageSend, BlockScope scope) {
	if (messageSend.typeArguments !=  null && messageSend.typeArguments.length > 0) {
		this.parameterizedTypeRef--;
	}
	super.endVisit(messageSend, scope);
}

public boolean visit(TryStatement tryStatement, BlockScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & CATCH_TYPE_REFERENCE) != 0) {
		Argument[] catchArguments = tryStatement.catchArguments;
		if (catchArguments != null) {
			int argLength = catchArguments.length;
			for (int i=0; i<argLength; i++) {
				report(catchArguments[i].type, scope);
				if (this.completed) return false;
			}
		}
	}
	return !this.completed;
}

public boolean visit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, BlockScope scope) {
	if ((this.fineGrain & TYPE_ARGUMENT_TYPE_REFERENCE) != 0) {
		report(parameterizedQualifiedTypeReference, scope);
	}
	this.parameterizedTypeRef++;
	return !this.completed;
}

public boolean visit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, ClassScope scope) {
	if ((this.fineGrain & TYPE_ARGUMENT_TYPE_REFERENCE) != 0) {
		report(parameterizedQualifiedTypeReference, scope);
	}
	this.parameterizedTypeRef++;
	return !this.completed;
}

public boolean visit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, BlockScope scope) {
	if ((this.fineGrain & TYPE_ARGUMENT_TYPE_REFERENCE) != 0) {
		report(parameterizedSingleTypeReference, scope);
	}
	this.parameterizedTypeRef++;
	return !this.completed;
}

public boolean visit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, ClassScope scope) {
	if ((this.fineGrain & TYPE_ARGUMENT_TYPE_REFERENCE) != 0) {
		report(parameterizedSingleTypeReference, scope);
	}
	this.parameterizedTypeRef++;
	return !this.completed;
}

public void endVisit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, BlockScope scope) {
	this.parameterizedTypeRef--;
}

public void endVisit(ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference, ClassScope scope) {
	this.parameterizedTypeRef--;
}

public void endVisit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, BlockScope scope) {
	this.parameterizedTypeRef--;
}

public void endVisit(ParameterizedSingleTypeReference parameterizedSingleTypeReference, ClassScope scope) {
	this.parameterizedTypeRef--;
}

public boolean visit(SingleTypeReference singleTypeReference, BlockScope scope) {
	if (((this.fineGrain & TYPE_ARGUMENT_TYPE_REFERENCE) != 0 && this.parameterizedTypeRef > 0 && this.wildcardTypeRef == 0) ||
		((this.fineGrain & WILDCARD_BOUND_TYPE_REFERENCE) != 0 && this.wildcardTypeRef > 0)) {
		report(singleTypeReference, scope);
	}
	return !this.completed;
}

public boolean visit(Wildcard wildcard, BlockScope scope) {
	this.wildcardTypeRef++;
	return !this.completed;
}

public boolean visit(Wildcard wildcard, ClassScope scope) {
	this.wildcardTypeRef++;
	return !this.completed;
}

public void endVisit(Wildcard wildcard, BlockScope scope) {
	this.wildcardTypeRef--;
}

public void endVisit(Wildcard wildcard, ClassScope scope) {
	this.wildcardTypeRef--;
}

public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
	if (this.completed) return false;
	if ((this.fineGrain & FIELD_DECLARATION_TYPE_REFERENCE) != 0) {
        report(fieldDeclaration.type, scope);
	}
	return !this.completed;
}

public boolean visit(FieldReference fieldReference, BlockScope scope) {
	if (fieldReference.isSuperAccess() && (this.fineGrain & SUPER_REFERENCE) != 0) {
		report(fieldReference, scope);
	}
	else if (fieldReference.receiver.isThis() && (this.fineGrain & THIS_REFERENCE) != 0) {
		report(fieldReference, scope);
	}
	// Qualified and Implicit This reference are not field reference AST node
	// They are respectively caught in visit(QualifiedNameReference) and visit(SingleNameReference) methods
	return !this.completed;
}

public boolean visit(QualifiedTypeReference qualifiedTypeReference, BlockScope scope) {
	if (((this.fineGrain & TYPE_ARGUMENT_TYPE_REFERENCE) != 0 && this.parameterizedTypeRef > 0 && this.wildcardTypeRef == 0) ||
		((this.fineGrain & WILDCARD_BOUND_TYPE_REFERENCE) != 0 && this.wildcardTypeRef > 0)) {
		report(qualifiedTypeReference, scope);
	}
	return !this.completed;
}

public boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(AnnotationMethodDeclaration annotationTypeDeclaration, ClassScope scope) {
	return !this.completed;
}

public boolean visit(Argument argument, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Argument argument, ClassScope scope) {
	return !this.completed;
}

public boolean visit(ArrayAllocationExpression arrayAllocationExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(ArrayReference arrayReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(AssertStatement assertStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Assignment assignment, BlockScope scope) {
	return !this.completed;
}

public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Block block, BlockScope scope) {
	return !this.completed;
}

public boolean visit(BreakStatement breakStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(CaseStatement caseStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(CharLiteral charLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ClassLiteralAccess classLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Clinit clinit, ClassScope scope) {
	return !this.completed;
}

public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
	return !this.completed;
}

public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ConditionalExpression conditionalExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ContinueStatement continueStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(DoStatement doStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(EmptyStatement emptyStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(EqualExpression equalExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ExtendedStringLiteral extendedStringLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(FieldReference fieldReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ForeachStatement forStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ForStatement forStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(IfStatement ifStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Initializer initializer, MethodScope methodScope) {
	return !this.completed;
}

public boolean visit(InstanceOfExpression instanceOfExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(IntLiteral intLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Javadoc javadoc, BlockScope scope) {
	return !this.completed;
}

public boolean visit(Javadoc javadoc, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocAllocationExpression expression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocAllocationExpression expression, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocArgumentExpression expression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocArgumentExpression expression, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocArrayQualifiedTypeReference typeRef, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocArrayQualifiedTypeReference typeRef, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocArraySingleTypeReference typeRef, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocArraySingleTypeReference typeRef, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocFieldReference fieldRef, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocFieldReference fieldRef, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocImplicitTypeReference implicitTypeReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocImplicitTypeReference implicitTypeReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocMessageSend messageSend, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocMessageSend messageSend, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocQualifiedTypeReference typeRef, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocQualifiedTypeReference typeRef, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocReturnStatement statement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocReturnStatement statement, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocSingleNameReference argument, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocSingleNameReference argument, ClassScope scope) {
	return !this.completed;
}

public boolean visit(JavadocSingleTypeReference typeRef, BlockScope scope) {
	return !this.completed;
}

public boolean visit(JavadocSingleTypeReference typeRef, ClassScope scope) {
	return !this.completed;
}

public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(LongLiteral longLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
	return !this.completed;
}

public boolean visit(MemberValuePair pair, BlockScope scope) {
	return !this.completed;
}

public boolean visit(NormalAnnotation annotation, BlockScope scope) {
	return !this.completed;
}

public boolean visit(NullLiteral nullLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedNameReference qualifiedNameReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedSuperReference qualifiedSuperReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedSuperReference qualifiedSuperReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedThisReference qualifiedThisReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedThisReference qualifiedThisReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(QualifiedTypeReference qualifiedTypeReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
	return !this.completed;
}

public boolean visit(SingleNameReference singleNameReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(SingleTypeReference singleTypeReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(StringLiteralConcatenation literal, BlockScope scope) {
	return !this.completed;
}

public boolean visit(SuperReference superReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(SwitchStatement switchStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(SynchronizedStatement synchronizedStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ThisReference thisReference, BlockScope scope) {
	return !this.completed;
}

public boolean visit(ThisReference thisReference, ClassScope scope) {
	return !this.completed;
}

public boolean visit(ThrowStatement throwStatement, BlockScope scope) {
	return !this.completed;
}

public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {
	return !this.completed;
}

public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
	return !this.completed;
}

public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
	return !this.completed;
}

public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
	return !this.completed;
}

public boolean visit(TypeParameter typeParameter, BlockScope scope) {
	return !this.completed;
}

public boolean visit(TypeParameter typeParameter, ClassScope scope) {
	return !this.completed;
}

public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
	return !this.completed;
}

public boolean visit(WhileStatement whileStatement, BlockScope scope) {
	return !this.completed;
}

}