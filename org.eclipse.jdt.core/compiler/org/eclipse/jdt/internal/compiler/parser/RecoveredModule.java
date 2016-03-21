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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ExportReference;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class RecoveredModule extends RecoveredType {

	public RecoveredExport[] exports;
	public int exportCount;
	public RecoveredModuleReference[] requires;
	public int requiresCount;
	public RecoveredTypeReference[] uses;
	public int usesCount;
	public RecoveredTypeReference[] interfaces;
	public RecoveredTypeReference[] implementations;
	public int servicesCount;
	private TypeReference pendingInterface;

	public RecoveredModule(ModuleDeclaration moduleDeclaration, RecoveredElement parent, int bracketBalance) {
		super(moduleDeclaration, parent, bracketBalance);
	}
	public RecoveredElement add(ExportReference exportReference, int bracketBalanceValue) {
		resetPendingModifiers();

		if (this.exports == null) {
			this.exports = new RecoveredExport[5];
			this.exportCount = 0;
		} else {
			if (this.exportCount == this.exports.length) {
				System.arraycopy(
					this.exports,
					0,
					(this.exports = new RecoveredExport[2 * this.exportCount]),
					0,
					this.exportCount);
			}
		}
		RecoveredExport element = new RecoveredExport(exportReference, this, bracketBalanceValue);
		this.exports[this.exportCount++] = element;

		/* if export not finished, then export becomes current */
		if (exportReference.declarationSourceEnd == 0) return element;
		return this;
	}
	public RecoveredElement add(ModuleReference moduleReference, int bracketBalanceValue) {
		if (this.requires == null) {
			this.requires = new RecoveredModuleReference[5];
			this.requiresCount = 0;
		} else {
			if (this.requiresCount == this.requires.length) {
				System.arraycopy(
					this.requires,
					0,
					(this.requires = new RecoveredModuleReference[2 * this.requiresCount]),
					0,
					this.requiresCount);
			}
		}
		RecoveredModuleReference element = new RecoveredModuleReference(moduleReference, this, bracketBalanceValue);
		this.requires[this.requiresCount++] = element;

		if (moduleReference.declarationSourceEnd == 0) return element;
		return this;
	}
	public RecoveredElement addUses(TypeReference typeReference, int bracketBalanceValue) {
		resetPendingModifiers();

		if (this.uses == null) {
			this.uses = new RecoveredTypeReference[5];
			this.usesCount = 0;
		} else {
			if (this.usesCount == this.uses.length) {
				System.arraycopy(
					this.uses,
					0,
					(this.uses = new RecoveredTypeReference[2 * this.usesCount]),
					0,
					this.usesCount);
			}
		}
		RecoveredTypeReference element = new RecoveredTypeReference(typeReference, this, bracketBalanceValue);
		this.uses[this.usesCount++] = element;
		return this;
	}
	public RecoveredElement addProvidesInterfaces(TypeReference typeReference, int bracketBalanceValue) {
		resetPendingModifiers();
		checkMemServices();
		RecoveredTypeReference element = new RecoveredTypeReference(typeReference, this, bracketBalanceValue);
		this.interfaces[this.servicesCount++] = element;
		return this;
	}
	public RecoveredElement addProvidesImplementations(TypeReference thatPendingInterface, TypeReference typeReference, int bracketBalanceValue) {
		resetPendingModifiers();
		checkMemServices();
		RecoveredTypeReference element = new RecoveredTypeReference(typeReference, this, bracketBalanceValue);
		this.pendingInterface = thatPendingInterface;
		this.implementations[this.servicesCount++] = element;
		return this;
	}
	private void checkMemServices() {
		if (this.interfaces == null) {
			this.interfaces = new RecoveredTypeReference[5];
			this.implementations = new RecoveredTypeReference[5];
			this.servicesCount = 0;
		} else {
			if (this.servicesCount == this.interfaces.length) {
				System.arraycopy(
					this.interfaces,
					0,
					(this.interfaces = new RecoveredTypeReference[2 * this.servicesCount]),
					0,
					this.servicesCount);
				System.arraycopy(
						this.implementations,
						0,
						(this.implementations = new RecoveredTypeReference[2 * this.servicesCount]),
						0,
						this.servicesCount);
			}
		}
	}
	public ModuleDeclaration updatedModuleDeclaration() {

		ModuleDeclaration moduleDeclaration = (ModuleDeclaration) this.typeDeclaration;
		/* update exports */
		if (this.exportCount > 0) {
			int existingCount = moduleDeclaration.exportsCount, actualCount = 0;
			ExportReference[] exports1 = new ExportReference[existingCount + this.exportCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.exports, 0, exports1, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.exportCount; i++){
				exports1[actualCount++] = this.exports[i].updatedExportReference();
			}
			moduleDeclaration.exports = exports1;
			moduleDeclaration.exportsCount = actualCount;
		}
		/* update uses */
		updateUses(moduleDeclaration);
		updateServices(moduleDeclaration);
		return moduleDeclaration;
	}
	private void updateUses(ModuleDeclaration moduleDeclaration) {
		if (this.usesCount > 0) {
			int existingCount = moduleDeclaration.usesCount, actualCount = 0;
			TypeReference[] ref1 = new TypeReference[existingCount + this.usesCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.uses, 0, ref1, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.usesCount; ++i) {
				ref1[actualCount++] = this.uses[i].updateTypeReference();
			}
			moduleDeclaration.uses = ref1;
			moduleDeclaration.usesCount = actualCount;
  			
		}
	}
	private void updateServices(ModuleDeclaration moduleDeclaration) {
		if (this.servicesCount > 0) {
			int existingCount = moduleDeclaration.servicesCount, actualCount = 0;
			int totalCount = existingCount + this.servicesCount;
			TypeReference[] ref1 = new TypeReference[totalCount];
			TypeReference[] ref2 = new TypeReference[totalCount];
			if (existingCount > 0) {
				System.arraycopy(moduleDeclaration.interfaces, 0, ref1, 0, existingCount);
				System.arraycopy(moduleDeclaration.implementations, 0, ref2, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.servicesCount; ++i) {
				TypeReference interfaceRef = this.interfaces[i] != null ? this.interfaces[i].updateTypeReference() : this.pendingInterface;
				if (interfaceRef == null) break; // something wrong
				ref1[actualCount] = interfaceRef;
				ref2[actualCount] = this.implementations[i] != null ? this.implementations[i].updateTypeReference() : null;
				++actualCount;
			}
			moduleDeclaration.interfaces = ref1;
			moduleDeclaration.implementations = ref2;
			moduleDeclaration.servicesCount = actualCount;
  			
		}
	}
	public void updateParseTree(){
		updatedModuleDeclaration();
	}

}
