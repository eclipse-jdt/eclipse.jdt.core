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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class CompletionOnMethodReturnType extends MethodDeclaration {
	public CompletionOnMethodReturnType(TypeReference returnType, CompilationResult compilationResult){
		super(compilationResult);
		this.returnType = returnType;
		this.sourceStart = returnType.sourceStart;
		this.sourceEnd = returnType.sourceEnd;
	}
	
	public void resolveStatements() {
			throw new CompletionNodeFound(this, this.scope);
	}
	
	public String toString(int tab) {
		return returnType.toString(tab);
	}

}
