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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ModuleDeclaration extends TypeDeclaration {

	public ExportsStatement[] exports;
	public RequiresStatement[] requires;
	public UsesStatement[] uses;
	public ProvidesStatement[] services;
	public OpensStatement[] opens;
	public int exportsCount;
	public int requiresCount;
	public int usesCount;
	public int servicesCount;
	public int opensCount;
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
		Set<ModuleBinding> requiredModules = new HashSet<ModuleBinding>();
		for(int i = 0; i < this.requiresCount; i++) {
			RequiresStatement ref = this.requires[i];
			if (ref != null && ref.resolve(this.scope) != null) {
				if (!requiredModules.add(ref.resolvedBinding)) {
					this.scope.problemReporter().duplicateModuleReference(IProblem.DuplicateRequires, ref.module);
				}
				Collection<ModuleBinding> deps = ref.resolvedBinding.dependencyGraphCollector().get();
				if (deps.contains(this.moduleBinding))
					this.scope.problemReporter().cyclicModuleDependency(this.moduleBinding, ref.module);
			}
		}
		Set<PackageBinding> exportedPkgs = new HashSet<>();
		for (int i = 0; i < this.exportsCount; i++) {
			ExportsStatement ref = this.exports[i];
 			if (ref != null && ref.resolve(this.scope)) {
				if (!exportedPkgs.add(ref.resolvedPackage)) {
					this.scope.problemReporter().invalidPackageReference(IProblem.DuplicateExports, ref);
				}
			}
		}
		Set<PackageBinding> openedPkgs = new HashSet<>();
		for (int i = 0; i < this.opensCount; i++) {
			OpensStatement ref = this.opens[i];
			if (isOpen()) {
				this.scope.problemReporter().invalidOpensStatement(ref, this);
			} else {
				if (ref.resolve(this.scope)) {
					if (!openedPkgs.add(ref.resolvedPackage)) {
						this.scope.problemReporter().invalidPackageReference(IProblem.DuplicateOpens, ref);
					}
				}
			}
		}
		Set<TypeBinding> allTypes = new HashSet<TypeBinding>();
		for(int i = 0; i < this.usesCount; i++) {
			TypeBinding serviceBinding = this.uses[i].serviceInterface.resolveType(this.scope);
			if (serviceBinding != null && serviceBinding.isValidBinding()) {
				if (!(serviceBinding.isClass() || serviceBinding.isInterface() || serviceBinding.isAnnotationType())) {
					this.scope.problemReporter().invalidServiceRef(IProblem.InvalidServiceIntfType, this.uses[i].serviceInterface);
				}
				if (!allTypes.add(this.uses[i].serviceInterface.resolvedType)) {
					this.scope.problemReporter().duplicateTypeReference(IProblem.DuplicateUses, this.uses[i].serviceInterface);
				}
			}
		}
		Set<TypeBinding> interfaces = new HashSet<>();
		for(int i = 0; i < this.servicesCount; i++) {
			this.services[i].resolve(this.scope);
			TypeBinding infBinding = this.services[i].serviceInterface.resolvedType;
			if (infBinding != null && infBinding.isValidBinding()) {
				if (!interfaces.add(this.services[i].serviceInterface.resolvedType)) { 
					this.scope.problemReporter().duplicateTypeReference(IProblem.DuplicateServices,
							this.services[i].serviceInterface);
				}
			}
		}
	}

	
	public StringBuffer printHeader(int indent, StringBuffer output) {
		if (isOpen()) {
			output.append("open "); //$NON-NLS-1$
		}
		output.append("module "); //$NON-NLS-1$
		output.append(CharOperation.charToString(this.moduleName));
		return output;
	}
	private boolean isOpen() {
		return (this.modifiers & ClassFileConstants.ACC_OPEN) != 0;
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
		if (this.opens != null) {
			for(int i = 0; i < this.opensCount; i++) {
				output.append('\n');
				this.opens[i].print(indent + 1, output);
			}
		}
		if (this.uses != null) {
			for(int i = 0; i < this.usesCount; i++) {
				output.append('\n');
				this.uses[i].print(indent + 1, output);
			}
		}
		if (this.servicesCount != 0) {
			for(int i = 0; i < this.servicesCount; i++) {
				output.append('\n');
				this.services[i].print(indent + 1, output);
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}
