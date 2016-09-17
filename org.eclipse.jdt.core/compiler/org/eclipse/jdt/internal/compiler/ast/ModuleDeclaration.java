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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ModuleDeclaration extends TypeDeclaration {

	public ExportReference[] exports;
	public ModuleReference[] requires;
	public TypeReference[] uses;
	public TypeReference[] interfaces;
	public TypeReference[] implementations;
	public int exportsCount;
	public int requiresCount;
	public int usesCount;
	public int servicesCount;
	public ModuleBinding moduleBinding;

	public char[][] tokens;
	public char[] moduleName;
	public long[] sourcePositions;

	public ModuleDeclaration(CompilationResult compilationResult, char[][] tokens, long[] positions) {
		super(compilationResult);
		this.compilationResult = compilationResult;
		this.exportsCount = 0;
		this.requiresCount = 0;
		this.tokens = tokens;
		this.moduleName = CharOperation.concatWith(tokens, '.');
		this.sourcePositions = positions;
		this.sourceEnd = (int) (positions[positions.length-1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (positions[0] >>> 32);
	}
	@Override
	public void generateCode(ClassFile enclosingClassFile) {
		if (this.ignoreFurtherInvestigation) {
			return;
		}
		super.generateCode(enclosingClassFile);
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resolve() {
		//
		if (this.binding == null) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
		this.moduleBinding = this.scope.environment().getModule(this.moduleName);
		this.binding.compoundName = CharOperation.arrayConcat(this.tokens, this.name);
		Set<ModuleBinding> requiredModules = new HashSet<ModuleBinding>();
		for(int i = 0; i < this.requiresCount; i++) {
			ModuleReference ref = this.requires[i];
			if (ref.resolve(this.scope) != null) {
				if (!requiredModules.add(ref.binding)) {
					this.scope.problemReporter().duplicateModuleReference(IProblem.DuplicateRequires, ref);
				}
				Collection<ModuleBinding> deps = ref.binding.dependencyGraphCollector().get();
				if (deps.contains(this.moduleBinding))
					this.scope.problemReporter().cyclicModuleDependency(this.moduleBinding, ref);
			}
		}
		for (int i = 0; i < this.exportsCount; i++) {
			this.exports[i].resolve(this.scope);
		}
		for(int i = 0; i < this.usesCount; i++) {
			Set<TypeBinding> allTypes = new HashSet<TypeBinding>();
			if (this.uses[i].resolveType(this.scope) != null) {
				if (!allTypes.add(this.uses[i].resolvedType)) {
					this.scope.problemReporter().duplicateTypeReference(IProblem.DuplicateUses, this.uses[i]);
				}
			}
		}
		Map<TypeBinding, TypeBinding> services = new HashMap<TypeBinding, TypeBinding>(this.servicesCount); 
		for(int i = 0; i < this.servicesCount; i++) {
			if (this.interfaces[i].resolveType(this.scope) != null) {
				TypeBinding inf = this.interfaces[i].resolvedType;
				if (this.implementations[i].resolveType(this.scope) != null) {
					ReferenceBinding imp = (ReferenceBinding) this.implementations[i].resolvedType;
					if (inf.isValidBinding() && imp.isValidBinding()) {
						validate(this.interfaces[i], this.implementations[i]);
						if (services.get(inf) == imp) { //$IDENTITY-COMPARISON$
							this.scope.problemReporter().duplicateTypeReference(IProblem.DuplicateServices,
									this.interfaces[i], this.implementations[i]);
						} else {
							services.put(inf, imp);
						}
					}
				}
			}
		}
	}

	private void validate(TypeReference serviceInf, TypeReference serviceImpl) {
		ReferenceBinding intf = (ReferenceBinding) serviceInf.resolvedType;
		ReferenceBinding impl = (ReferenceBinding) serviceImpl.resolvedType;
		int problemId = ProblemReasons.NoError;
		if (impl.isAbstract()) {
			problemId = ProblemReasons.ServiceImplCannotbeAbstract;
		} else if(impl.findSuperTypeOriginatingFrom(intf) == null) {
			this.scope.problemReporter().typeMismatchError(impl, intf, serviceImpl, null);
		}
		MethodBinding defaultConstructor = impl.getExactConstructor(new TypeBinding[0]);
		if (defaultConstructor == null || !defaultConstructor.isValidBinding()) {
			problemId = ProblemReasons.DefaultConstructorRequiredForServiceImpl;
		} else if (!defaultConstructor.isPublic()) {
			problemId = ProblemReasons.ServiceImplDefaultConstructorNotPublic;
		}
		if (problemId != ProblemReasons.NoError) {
			this.scope.problemReporter().invalidServiceImpl(problemId, serviceImpl);
		}
	}
	public StringBuffer printHeader(int indent, StringBuffer output) {
		output.append("module "); //$NON-NLS-1$
		output.append(CharOperation.charToString(this.moduleName));
		return output;
	}
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(" {"); //$NON-NLS-1$
		if (this.requires != null) {
			for(int i = 0; i < this.requiresCount; i++) {
				output.append('\n');
				printIndent(indent + 1, output);
				output.append("requires "); //$NON-NLS-1$
				this.requires[i].print(0, output);
				output.append(";"); //$NON-NLS-1$
			}
		}
		if (this.exports != null) {
			for(int i = 0; i < this.exportsCount; i++) {
				output.append('\n');
				this.exports[i].print(indent + 1, output);
			}
		}
		if (this.uses != null) {
			for(int i = 0; i < this.usesCount; i++) {
				output.append('\n');
				printIndent(indent + 1, output);
				output.append("uses "); //$NON-NLS-1$
				this.uses[i].print(0, output);
				output.append(";"); //$NON-NLS-1$
			}
		}
		if (this.servicesCount != 0) {
			for(int i = 0; i < this.servicesCount; i++) {
				output.append('\n');
				printIndent(indent + 1, output);
				output.append("provides "); //$NON-NLS-1$
				this.interfaces[i].print(0, output);
				output.append('\n');
				printIndent(indent + 2, output);
				output.append("with "); //$NON-NLS-1$
				this.implementations[i].print(0, output);
				output.append(";"); //$NON-NLS-1$
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}
