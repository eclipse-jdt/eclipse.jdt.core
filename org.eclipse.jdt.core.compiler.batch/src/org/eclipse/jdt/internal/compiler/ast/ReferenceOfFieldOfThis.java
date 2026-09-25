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
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
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
    public TypeBinding resolveType(BlockScope scope) {
    	this.fieldReference.bits |= this.bits & ASTNode.IsStrictlyAssigned;
    	TypeBinding type = this.fieldReference.resolveType(scope);
    	this.binding = this.fieldReference.binding;
    	this.resolvedType = this.fieldReference.resolvedType;
    	this.constant = this.fieldReference.optimizedBooleanConstant();
    	this.bits &= ~ASTNode.RestrictiveFlagMASK;
    	this.bits |= Binding.FIELD;
    	this.actualReceiverType = this.fieldReference.actualReceiverType;
    	return type;
    }
}
