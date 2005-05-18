/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a type reference containing the completion identifier as a single
 * name reference.
 * e.g.
 *
 *	class X extends Obj[cursor]
 *
 *	---> class X extends <CompleteOnType:Obj>
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
 
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnSingleTypeReference extends SingleTypeReference {
public boolean isCompletionNode;
public boolean isConstructorType;
public CompletionOnFieldType fieldTypeCompletionNode;

public CompletionOnSingleTypeReference(char[] source, long pos) {
	super(source, pos);
	isCompletionNode = true;
}
public void aboutToResolve(Scope scope) {
	getTypeBinding(scope);
}
/*
 * No expansion of the completion reference into an array one
 */
public TypeReference copyDims(int dim){
	return this;
}
protected TypeBinding getTypeBinding(Scope scope) {
    if (this.fieldTypeCompletionNode != null) {
		throw new CompletionNodeFound(this.fieldTypeCompletionNode, scope);
    }
	if(isCompletionNode) {
		throw new CompletionNodeFound(this, scope);
	} else {
		return super.getTypeBinding(scope);
	}
}
public StringBuffer printExpression(int indent, StringBuffer output){
	return output.append("<CompleteOnType:").append(token).append('>'); //$NON-NLS-1$
}
public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
    if (this.fieldTypeCompletionNode != null) {
		throw new CompletionNodeFound(this.fieldTypeCompletionNode, scope);
    }
	if(isCompletionNode) {
		throw new CompletionNodeFound(this, enclosingType, scope);
	} else {
		return super.resolveTypeEnclosing(scope, enclosingType);
	}
}
}
