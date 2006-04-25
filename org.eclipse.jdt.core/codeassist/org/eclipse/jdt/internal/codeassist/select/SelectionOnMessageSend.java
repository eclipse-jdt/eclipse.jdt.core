/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a message send containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      this.[start]bar[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnMessageSend:this.bar(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnMessageSend extends MessageSend {

	/*
	 * Cannot answer default abstract match, iterate in superinterfaces of declaring class
	 * for a better match (default abstract match came from scope lookups).
	 */
	private MethodBinding findNonDefaultAbstractMethod(MethodBinding methodBinding) {

		ReferenceBinding[] itsInterfaces = methodBinding.declaringClass.superInterfaces();
		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
			int lastPosition = 0;
			interfacesToVisit[lastPosition] = itsInterfaces;
			org.eclipse.jdt.internal.core.util.SimpleSet interfacesSeen = new org.eclipse.jdt.internal.core.util.SimpleSet(itsInterfaces.length * 2);

			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];

				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding currentType = interfaces[j];

					if (!interfacesSeen.includes(currentType)) {
						// if interface as not already been visited
						interfacesSeen.add(currentType);

						MethodBinding[] methods = currentType.getMethods(methodBinding.selector);
						if(methods != null) {
							for (int k = 0; k < methods.length; k++) {
								if(methodBinding.areParametersEqual(methods[k])) {
									return methods[k];
								}
							}
						}

						itsInterfaces = currentType.superInterfaces();
						if (itsInterfaces != Binding.NO_SUPERINTERFACES) {

							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}
		}
		return methodBinding;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {

		output.append("<SelectOnMessageSend:"); //$NON-NLS-1$
		if (!receiver.isImplicitThis()) receiver.printExpression(0, output).append('.');
		output.append(this.selector).append('(');
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				arguments[i].printExpression(0, output);
			}
		}
		return output.append(")>"); //$NON-NLS-1$
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		super.resolveType(scope);

		// tolerate some error cases
		if(binding == null ||
					!(binding.isValidBinding() || 
						binding.problemId() == ProblemReasons.NotVisible
						|| binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
						|| binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
						|| binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext)) {
			throw new SelectionNodeFound();
		} else {
			if(binding.isDefaultAbstract()) {
				throw new SelectionNodeFound(findNonDefaultAbstractMethod(binding)); // 23594
			} else {
				throw new SelectionNodeFound(binding);
			}
		}
	}
}
