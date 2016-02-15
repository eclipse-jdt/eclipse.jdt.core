/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class ExportReference extends ASTNode {
	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public ModuleReference[] targets;
	public char[] pkgName;

	public ExportReference(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length - 1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
		this.pkgName = CharOperation.concatWith(tokens, '.');
	}
	
	public boolean isTargeted() {
		return this.targets != null && this.targets.length > 0;
	}
	
	public ModuleReference[] getTargetedModules() {
		return this.targets;
	}

	public boolean resolve(Scope scope) {
		boolean errorsExist = false;
		if (this.isTargeted()) {
			Set<ModuleBinding> modules = new HashSet<ModuleBinding>();
			for (int i = 0; i < this.targets.length; i++) {
				ModuleReference ref = this.targets[i];
				if (ref.resolve(scope) != null) {
					if (!modules.add(ref.binding)) {
						scope.problemReporter().duplicateModuleReference(IProblem.DuplicateExports, ref);
						errorsExist = true;
					}
				}
			}
		}
		return !errorsExist;
	}
	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output);
		output.append("exports "); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		if (this.isTargeted()) {
			output.append(" to "); //$NON-NLS-1$
			for (int i = 0; i < this.targets.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.targets[i].print(0, output);
			}
		}
		output.append(";"); //$NON-NLS-1$
		return output;
	}

}
