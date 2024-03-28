/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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

package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class MethodReferenceFactory {

	public static MethodReferenceExpression createMethodReferenceExpression(JavaElement parent, org.eclipse.jdt.internal.compiler.ast.ReferenceExpression referenceExpression) {
		return new MethodReferenceExpression(parent, referenceExpression);
	}

	public static MethodReferenceMethod createReferencedMethod(JavaElement parent, org.eclipse.jdt.internal.compiler.ast.ReferenceExpression referenceExpression) {
		int length;
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		String [] parameterTypes = new String[length = referenceExpression.descriptor.parameters.length];
		for (int i = 0; i < length; i++)
			parameterTypes[i] = getTypeSignature(manager, referenceExpression.descriptor.parameters[i]);
		String returnType = getTypeSignature(manager, referenceExpression.descriptor.returnType);
		String selector = manager.intern(new String(referenceExpression.descriptor.selector));
		String key = new String(referenceExpression.descriptor.computeUniqueKey());
		MethodReferenceMethod referencedMethod = createReferencedMethod(parent, selector, key, referenceExpression.sourceStart, referenceExpression.sourceEnd, returnType);
		return referencedMethod;
	}

	public static MethodReferenceMethod createReferencedMethod(JavaElement parent, String selector, String key, int sourceStart, int sourceEnd, String returnType) {
		SourceMethodInfo info = null;
		info = new SourceMethodInfo();
		info.setSourceRangeStart(sourceStart);
		info.setSourceRangeEnd(sourceEnd);
		info.setFlags(0);
		info.setNameSourceStart(sourceStart);
		info.setNameSourceEnd(sourceEnd);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		int length;
		info.setReturnType(manager.intern(Signature.toCharArray(returnType.toCharArray())));
		info.setExceptionTypeNames(CharOperation.NO_CHAR_CHAR);
		info.arguments = null; // will be updated shortly, parent has to come into existence first.
		return new MethodReferenceMethod(parent, selector, key, sourceStart, returnType, info);
	}

	private static String getTypeSignature(JavaModelManager manager, TypeBinding type) {
		char[] signature = type.genericTypeSignature();
		signature = CharOperation.replaceOnCopy(signature, '/', '.');
		return manager.intern(new String(signature));
	}
}
