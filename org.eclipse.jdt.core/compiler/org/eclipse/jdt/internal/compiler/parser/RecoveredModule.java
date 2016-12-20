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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleStatement;
import org.eclipse.jdt.internal.compiler.ast.OpensStatement;
import org.eclipse.jdt.internal.compiler.ast.ProvidesStatement;
import org.eclipse.jdt.internal.compiler.ast.RequiresStatement;
import org.eclipse.jdt.internal.compiler.ast.UsesStatement;

public class RecoveredModule extends RecoveredType {
	
	public RecoveredExportsStatement[] exports;
	public int exportCount;
	public RecoveredOpensStatement[] opens;
	public int opensCount;
	public RecoveredRequiresStatement[] requires;
	public int requiresCount;
	public RecoveredUsesStatement[] uses;
	public int usesCount;
	public RecoveredProvidesStatement[] services;
	public int servicesCount;

	public RecoveredModule(ModuleDeclaration moduleDeclaration, RecoveredElement parent, int bracketBalance) {
		super(moduleDeclaration, parent, bracketBalance);
	}
	public RecoveredElement add(ModuleStatement moduleStatement, int bracketBalanceValue) {
		
		// TODO: can't we do away with all these additions except for ProvidesStatement - to check
		// if there are any corner cases that uses these.
		if (moduleStatement instanceof ExportsStatement) {
			return add((ExportsStatement) moduleStatement, bracketBalanceValue);
		}
		if (moduleStatement instanceof OpensStatement) {
			return add((OpensStatement) moduleStatement, bracketBalanceValue);
		}
		if (moduleStatement instanceof RequiresStatement) {
			return add((RequiresStatement) moduleStatement, bracketBalanceValue);
		}
		if (moduleStatement instanceof ProvidesStatement) {
			return add((ProvidesStatement) moduleStatement, bracketBalanceValue);
		}
		if (moduleStatement instanceof UsesStatement) {
			return add((UsesStatement) moduleStatement, bracketBalanceValue);
		}
		
		return this;
	}

	public RecoveredElement add(ExportsStatement exportsStatement, int bracketBalanceValue) {
		resetPendingModifiers();

		if (this.exports == null) {
			this.exports = new RecoveredExportsStatement[5];
			this.exportCount = 0;
		} else {
			if (this.exportCount == this.exports.length) {
				System.arraycopy(
					this.exports,
					0,
					(this.exports = new RecoveredExportsStatement[2 * this.exportCount]),
					0,
					this.exportCount);
			}
		}
		RecoveredExportsStatement element = new RecoveredExportsStatement(exportsStatement, this, bracketBalanceValue);
		this.exports[this.exportCount++] = element;

		return element;
	}
	public RecoveredElement add(OpensStatement opensStatement, int bracketBalanceValue) {
		resetPendingModifiers();

		if (this.opens == null) {
			this.opens = new RecoveredOpensStatement[5];
			this.opensCount = 0;
		} else {
			if (this.opensCount == this.opens.length) {
				System.arraycopy(
					this.opens,
					0,
					(this.opens = new RecoveredOpensStatement[2 * this.opensCount]),
					0,
					this.opensCount);
			}
		}
		RecoveredOpensStatement element = new RecoveredOpensStatement(opensStatement, this, bracketBalanceValue);
		this.opens[this.opensCount++] = element;

		return element;
	}
	public RecoveredElement add(RequiresStatement requiresStatement, int bracketBalanceValue) {
		if (this.requires == null) {
			this.requires = new RecoveredRequiresStatement[5];
			this.requiresCount = 0;
		} else {
			if (this.requiresCount == this.requires.length) {
				System.arraycopy(
					this.requires,
					0,
					(this.requires = new RecoveredRequiresStatement[2 * this.requiresCount]),
					0,
					this.requiresCount);
			}
		}
		RecoveredRequiresStatement element = new RecoveredRequiresStatement(requiresStatement, this, bracketBalanceValue);
		this.requires[this.requiresCount++] = element;
		return this;
	}
	public RecoveredElement add(ProvidesStatement providesStatement, int bracketBalanceValue) {
		if (this.services == null) {
			this.services = new RecoveredProvidesStatement[5];
			this.servicesCount = 0;
		} else {
			if (this.servicesCount == this.services.length) {
				System.arraycopy(
					this.services,
					0,
					(this.services = new RecoveredProvidesStatement[2 * this.servicesCount]),
					0,
					this.servicesCount);
			}
		}
		RecoveredProvidesStatement element = new RecoveredProvidesStatement(providesStatement, this, bracketBalanceValue);
		this.services[this.servicesCount++] = element;
		return element;
	}
	public RecoveredElement add(UsesStatement usesStatement, int bracketBalanceValue) {
		genAssign(usesStatement, bracketBalanceValue);
		return this;
	}
	private void genAssign(UsesStatement usesStatement, int bracketBalanceValue) {
		if (this.uses == null) {
			this.uses = new RecoveredUsesStatement[5];
			this.usesCount = 0;
		} else {
			if (this.usesCount == this.uses.length) {
				System.arraycopy(
					this.uses,
					0,
					(this.uses = new RecoveredUsesStatement[2 * this.usesCount]),
					0,
					this.usesCount);
			}
		}
		RecoveredUsesStatement element = new RecoveredUsesStatement(usesStatement, this, bracketBalanceValue);
		this.uses[this.usesCount++] = element;
	}
	public String toString(int tab) {
		StringBuffer result = new StringBuffer(tabString(tab));
		result.append("Recovered module:\n"); //$NON-NLS-1$
		result.append("module ");//$NON-NLS-1$
		result.append(CharOperation.charToString(((ModuleDeclaration) this.typeDeclaration).moduleName));
		result.append(" {");//$NON-NLS-1$
		if (this.exportCount > 0) {
			for (int i = 0; i < this.exportCount; ++i) {
				result.append("\n"); //$NON-NLS-1$
				result.append(this.exports[i].toString(tab + 1));
			}
		}
		if (this.requiresCount > 0) {
			for (int i = 0; i < this.requiresCount; ++i) {
				result.append("\n"); //$NON-NLS-1$
				result.append(this.requires[i].toString(tab + 1));
			}
		}
		if (this.usesCount > 0) {
			for (int i = 0; i < this.usesCount; ++i) {
				result.append("\n"); //$NON-NLS-1$
				result.append(this.uses[i].toString(tab + 1));
			}
		}
		if (this.servicesCount > 0) {
			for (int i = 0; i < this.servicesCount; ++i) {
				result.append("\n"); //$NON-NLS-1$
				result.append(this.services[i].toString(tab + 1));
			}
		}
		result.append("\n}");//$NON-NLS-1$
		return result.toString();
	}
	public ModuleDeclaration updatedModuleDeclaration() {

		ModuleDeclaration moduleDeclaration = (ModuleDeclaration) this.typeDeclaration;
		updateExports(moduleDeclaration);
		updateOpens(moduleDeclaration);
		updateRequires(moduleDeclaration);
		updateUses(moduleDeclaration);
		updateServices(moduleDeclaration);
		return moduleDeclaration;
	}
	private void updateExports(ModuleDeclaration moduleDeclaration) {
		if (this.exportCount > 0) {
			int existingCount = moduleDeclaration.exportsCount, actualCount = 0;
			ExportsStatement[] exports1 = new ExportsStatement[existingCount + this.exportCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.exports, 0, exports1, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.exportCount; i++){
				exports1[actualCount++] = (ExportsStatement)this.exports[i].updatedPackageVisibilityStatement();
			}
			moduleDeclaration.exports = exports1;
			moduleDeclaration.exportsCount = actualCount;
		}
	}
	private void updateOpens(ModuleDeclaration moduleDeclaration) {
		if (this.opensCount > 0) {
			int existingCount = moduleDeclaration.opensCount, actualCount = 0;
			OpensStatement[] opens1 = new OpensStatement[existingCount + this.opensCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.exports, 0, opens1, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.opensCount; i++){
				opens1[actualCount++] = (OpensStatement)this.opens[i].updatedPackageVisibilityStatement();
			}
			moduleDeclaration.opens = opens1;
			moduleDeclaration.opensCount = actualCount;
		}
	}
	private void updateRequires(ModuleDeclaration moduleDeclaration) {
		if (this.requiresCount > 0) {
			int existingCount = moduleDeclaration.requiresCount, actualCount = 0;
			RequiresStatement[] requiresStmts = new RequiresStatement[existingCount + this.requiresCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.requires, 0, requiresStmts, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.requiresCount; i++){
				requiresStmts[actualCount++] = this.requires[i].updatedRequiresStatement();
			}
			moduleDeclaration.requires = requiresStmts;
			moduleDeclaration.requiresCount = actualCount;
		}
	}
	private void updateUses(ModuleDeclaration moduleDeclaration) {
		if (this.usesCount > 0) {
			int existingCount = moduleDeclaration.usesCount, actualCount = 0;
			UsesStatement[] usesStmts = new UsesStatement[existingCount + this.usesCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.uses, 0, usesStmts, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.usesCount; ++i) {
				usesStmts[actualCount++] = this.uses[i].updatedUsesStatement();
			}
			moduleDeclaration.uses = usesStmts;
			moduleDeclaration.usesCount = actualCount;
		}
	}
	private void updateServices(ModuleDeclaration moduleDeclaration) {
		if (this.servicesCount > 0) {
			int existingCount = moduleDeclaration.servicesCount, actualCount = 0;
			ProvidesStatement[] providesStmts = new ProvidesStatement[existingCount + this.servicesCount];
			if (existingCount > 0){
				System.arraycopy(moduleDeclaration.services, 0, providesStmts, 0, existingCount);
				actualCount = existingCount;
			}
			for (int i = 0; i < this.servicesCount; ++i) {
				providesStmts[actualCount++] = this.services[i].updatedProvidesStatement();
			}
			moduleDeclaration.services = providesStmts;
			moduleDeclaration.servicesCount = actualCount;  			
		}
	}
	public void updateParseTree(){
		updatedModuleDeclaration();
	}

}
