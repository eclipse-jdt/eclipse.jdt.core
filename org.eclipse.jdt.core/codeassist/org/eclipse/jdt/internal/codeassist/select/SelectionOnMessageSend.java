/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnMessageSend extends MessageSend {

	/*
	 * Cannot answer default abstract match, iterate in superinterfaces of declaring class
	 * for a better match (default abstract match came from scope lookups).
	 */
	private MethodBinding findNonDefaultAbstractMethod(MethodBinding binding) {

		ReferenceBinding[] itsInterfaces = binding.declaringClass.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
			int lastPosition = 0;
			interfacesToVisit[lastPosition] = itsInterfaces;
			
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];

				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding currentType = interfaces[j];

					if ((currentType.tagBits & TagBits.InterfaceVisited) == 0) {
						// if interface as not already been visited
						currentType.tagBits |= TagBits.InterfaceVisited;

						MethodBinding[] methods = currentType.getMethods(binding.selector);;
						if(methods != null) {
							for (int k = 0; k < methods.length; k++) {
								if(binding.areParametersEqual(methods[k])) {
									return methods[k];
								}
							}
						}

						itsInterfaces = currentType.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {

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
		return binding;
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
	
	public String toStringExpression() {

		String s = "<SelectOnMessageSend:"; //$NON-NLS-1$
		if (!receiver.isImplicitThis())
			s = s + receiver.toStringExpression() + "."; //$NON-NLS-1$
		s = s + new String(selector) + "("; //$NON-NLS-1$
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toStringExpression();
				if (i != arguments.length - 1) {
					s += ", "; //$NON-NLS-1$
				}
			};
		}
		s = s + ")>"; //$NON-NLS-1$
		return s;
	}
}
