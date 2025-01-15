/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.NameLookup;

class DOMInternalCompletionProposal extends InternalCompletionProposal {

	public DOMInternalCompletionProposal(int kind, int completionLocation) {
		super(kind, completionLocation);
	}

	@Override
	public boolean canUseDiamond(CompletionContext coreContext) {
		// ECJ-based implementation uses a downcast,
		// so re-implement this method with our own downcast
		if (!coreContext.isExtended()) return false;
		if (coreContext instanceof DOMCompletionContext domCompletionContext) {
			char[] name1 = this.declarationPackageName;
			char[] name2 = this.declarationTypeName;
			char[] declarationType = CharOperation.concat(name1, name2, '.');  // fully qualified name
			// even if the type arguments used in the method have been substituted,
			// extract the original type arguments only, since thats what we want to compare with the class
			// type variables (Substitution might have happened when the constructor is coming from another
			// CU and not the current one).
			char[] sign = (this.originalSignature != null)? this.originalSignature : getSignature();
			if (!(sign == null || sign.length < 2)) {
				sign = Signature.removeCapture(sign);
			}
			char[][] types= Signature.getParameterTypes(sign);
			String[] paramTypeNames= new String[types.length];
			for (int i= 0; i < types.length; i++) {
				paramTypeNames[i]= new String(Signature.toCharArray(types[i]));
			}
			if (this.getDeclarationTypeVariables() != null) {
				return domCompletionContext.canUseDiamond(paramTypeNames, this.getDeclarationTypeVariables());
			}
			return domCompletionContext.canUseDiamond(paramTypeNames, declarationType);
		}
		return false;
	}
	
	public void setNameLookup(NameLookup nameLookup) {
		this.nameLookup = nameLookup;
	}
	
	public void setCompletionEngine(CompletionEngine completionEngine) {
		this.completionEngine = completionEngine;
	}
	
	@Override
	protected void setDeclarationPackageName(char[] declarationPackageName) {
		super.setDeclarationPackageName(declarationPackageName);
	}

	@Override
	protected void setTypeName(char[] typeName) {
		super.setTypeName(typeName);
	}
	
	@Override
	protected void setDeclarationTypeName(char[] declarationTypeName) {
		super.setDeclarationTypeName(declarationTypeName);
	}
	
	@Override
	protected void setIsContructor(boolean isConstructor) {
		super.setIsContructor(isConstructor);
	}
	
	@Override
	public void setParameterNames(char[][] parameterNames) {
		super.setParameterNames(parameterNames);
	}
	
	@Override
	protected void setParameterTypeNames(char[][] parameterTypeNames) {
		super.setParameterTypeNames(parameterTypeNames);
	}
	
	@Override
	protected void setParameterPackageNames(char[][] parameterPackageNames) {
		super.setParameterPackageNames(parameterPackageNames);
	}
	
	@Override
	protected void setPackageName(char[] packageName) {
		super.setPackageName(packageName);
	}
	
	@Override
	protected void setModuleName(char[] moduleName) {
		super.setModuleName(moduleName);
	}
	
	@Override
	public boolean isConstructor() {
		return this.isConstructor;
	}

}