/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.ModuleReferenceInfo;
import org.eclipse.jdt.internal.core.util.Util;

public class ModuleRequirement extends SourceRefElement implements IModuleDescription.IModuleReference {
	String name;
	boolean isPublic = false;
	public ModuleRequirement(JavaElement parent, final String name) {
		super(parent);
		this.name = name;
	}
	@Override
	public boolean isPublic() throws JavaModelException {
		ModuleReferenceInfo info = (ModuleReferenceInfo) getElementInfo();
		return info.isTransitive();
	}
	@Override
	public String getModuleName() {
		return this.name;
	}
	@Override
	public int getElementType() {
		return MODULE_REFERENCE;
	}
	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}
	@Override
	protected char getHandleMementoDelimiter() {
		return 0;
	}
	public int hashCode() {
		int hash = super.hashCode();
		return Util.combineHashCodes(hash, this.name.hashCode());
	}
	public boolean equals(Object o) {
		if (!(o instanceof ModuleRequirement)) 
			return false;
		// Don't take isPublic into account?
		return super.equals(o) && this.name.equals(((ModuleRequirement) o).name);
	}
}
