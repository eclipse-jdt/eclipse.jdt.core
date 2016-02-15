/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class ModuleReference extends ASTNode {
	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers = ClassFileConstants.AccDefault;
	public int modifiersSourceStart;
	public char[] moduleName;
	public ModuleBinding binding;

	public ModuleReference(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length - 1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
		this.moduleName = CharOperation.concatWith(tokens, '.');
	}
	
	public boolean isPublic() {
		return (this.modifiers & ClassFileConstants.AccPublic) != 0;
	}
	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output;
	}

	public ModuleBinding resolve(Scope scope) {
		if ((this.binding = scope.environment().getModule(this.moduleName)) == null) {
			scope.problemReporter().invalidModule(this);
		}
		return this.binding;
	}
}
