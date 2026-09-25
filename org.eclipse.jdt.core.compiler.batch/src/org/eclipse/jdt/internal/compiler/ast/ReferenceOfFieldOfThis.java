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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
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
    public String toString() {
        return this.fieldReference.toString();
    }

    @Override
    public TypeBinding resolveType(BlockScope scope) {
    	this.actualReceiverType = this.fieldReference.receiver.resolveType(scope);
    	TypeBinding type = super.resolveType(scope);
    	FieldBinding fieldBinding = (FieldBinding) this.binding;
    	if (fieldBinding.isStatic()) {
    		scope.problemReporter().nonStaticAccessToStaticField(this, fieldBinding);
    		ReferenceBinding declaringClass = fieldBinding.declaringClass;
    		if (TypeBinding.notEquals(declaringClass, this.actualReceiverType)
    				&& declaringClass.canBeSeenBy(scope)) {
    			scope.problemReporter().indirectAccessToStaticField(this, fieldBinding);
    		}
    		// check if accessing enum static field in initializer
    		if (declaringClass.isEnum() && scope.kind != Scope.MODULE_SCOPE) {
    			MethodScope methodScope = scope.methodScope();
    			SourceTypeBinding sourceType = scope.enclosingSourceType();
    			if (this.constant == Constant.NotAConstant
    					&& !methodScope.isStatic
    					&& (TypeBinding.equalsEquals(sourceType, declaringClass) || TypeBinding.equalsEquals(sourceType.superclass, declaringClass)) // enum constant body
    					&& methodScope.isInsideInitializerOrConstructor()) {
    				scope.problemReporter().enumStaticFieldUsedDuringInitialization(fieldBinding, this);
    			}
    		}
    	}
    	return type;
    }
}
