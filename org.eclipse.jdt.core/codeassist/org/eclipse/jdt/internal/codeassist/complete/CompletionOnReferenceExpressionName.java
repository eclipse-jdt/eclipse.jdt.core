/*******************************************************************************
 * Copyright (c) 2014, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnReferenceExpressionName extends ReferenceExpression implements CompletionNode {

	@Override
	public TypeBinding resolveType(BlockScope scope) {

		TypeBinding lhsType;
		boolean typeArgumentsHaveErrors;

		this.constant = Constant.NotAConstant;
		lhsType = this.lhs.resolveType(scope);
		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			typeArgumentsHaveErrors = false;
			this.resolvedTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				TypeReference typeReference = this.typeArguments[i];
				if ((this.resolvedTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
					typeArgumentsHaveErrors = true;
				}
				if (typeArgumentsHaveErrors && typeReference instanceof Wildcard) { // resolveType on wildcard always return null above, resolveTypeArgument is the real workhorse.
					scope.problemReporter().illegalUsageOfWildcard(typeReference);
				}
			}
			if (typeArgumentsHaveErrors || lhsType == null)
				throw new CompletionNodeFound();
		}

		if (lhsType != null && lhsType.isValidBinding())
			throw new CompletionNodeFound(this, lhsType, scope);
		throw new CompletionNodeFound();
	}

	@Override
	public StringBuilder printExpression(int tab, StringBuilder output) {
		output.append("<CompletionOnReferenceExpressionName:"); //$NON-NLS-1$
		super.printExpression(tab, output);
		return output.append('>');
	}

	@Override
	public char[] toCharArray() {
		return super.printExpression(0, new StringBuilder(30)).toString().toCharArray();
	}
}
