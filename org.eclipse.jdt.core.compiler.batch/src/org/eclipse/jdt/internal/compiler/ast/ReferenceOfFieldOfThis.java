/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* This is an implementation of an early-draft specification developed under the Java
* Community Process (JCP) and is made available for testing and evaluation purposes
* only. The code is not compatible with any specification of the JCP.
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/** An abstraction to wrap what used to be a FieldReference modeling `this.someField` to present it
 *  as a SingleNameReference instead
 */
public class ReferenceOfFieldOfThis extends SingleNameReference {

	private FieldReference fieldReference;
	public long nameSourcePosition;

    public ReferenceOfFieldOfThis(char[] source, long pos, FieldReference fieldReference) {
        super(source, pos);
        this.fieldReference = fieldReference;
        this.nameSourcePosition = pos;
        this.sourceStart = fieldReference.sourceStart;
    }

    public FieldReference fieldReference() {
        return this.fieldReference;
    }

    @Override
    public StringBuilder printExpression(int indent, StringBuilder output) {
    	return this.fieldReference.printExpression(indent, output);
    }

    @Override
    public String toString() {
        return this.fieldReference.toString();
    }

    @Override
    public Constant optimizedBooleanConstant() {
    	return this.fieldReference.optimizedBooleanConstant();
    }

    @Override
    public boolean isEquivalent(Reference reference) {
    	return reference.isEquivalent(this.fieldReference); // switch receiver to ensure both sides get unwrapped as needed
    }

    @Override
    public void traverse(ASTVisitor visitor, BlockScope scope) {
    	if (visitor.visit(this, scope)) {
    		this.fieldReference.traverse(visitor, scope);
    	}
    	visitor.endVisit(this, scope);
    }

    @Override
    public boolean isReferenceOfFieldOfThis() {
    	return true;
    }

    /* We resolve the underlying field reference and copy over relevant state, rather than resolve this as a SingleNameReference.
     * The reason is two folds. All semantics constraints validation continue to happen as before without having to be replicated.
     * This also allows us to expose the wrapped (resolved) FieldReference to clients who don't care about early construction context.
     * Really only flow analysis and code generation phases of the compiler ought to care about uninitialized `this`.
     */
    @Override
    public TypeBinding resolveType(BlockScope scope) {
    	this.fieldReference.bits |= this.bits & (ASTNode.IsStrictlyAssigned | ASTNode.IsCompoundAssigned);
    	TypeBinding type = this.fieldReference.resolveType(scope);
    	this.bits &= ~ASTNode.RestrictiveFlagMASK;
    	this.binding = this.fieldReference.binding;
    	if (this.binding != null && this.binding.hasProxyLocal())
    		this.binding = scope.getProxy((FieldBinding) this.binding);
    	if (this.binding instanceof LocalVariableBinding) // don't assume a swap; scope changes would inhibit substitution.
    		this.bits |= Binding.LOCAL;
    	else
        	this.bits |= Binding.FIELD;
    	this.resolvedType = this.fieldReference.resolvedType;
    	this.constant = this.fieldReference.constant;
    	this.actualReceiverType = this.fieldReference.actualReceiverType;
    	return type;
    }
}
