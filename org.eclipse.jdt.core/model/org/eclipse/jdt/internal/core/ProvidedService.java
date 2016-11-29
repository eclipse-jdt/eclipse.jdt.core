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
import org.eclipse.jdt.internal.core.util.Util;

public class ProvidedService extends SourceRefElement implements IModuleDescription.IProvidedService {

	private String service;
	private String[] impl;
	public ProvidedService(JavaElement parent, String service, String[] impl) {
		super(parent);
		this.service = service;
		this.impl = impl;
	}
	@Override
	public String getServiceName() {
		return this.service;
	}
	@Override
	public String[] getImplementationNames() {
		return this.impl;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("provides "); //$NON-NLS-1$
		buffer.append(this.service);
		buffer.append(" with "); //$NON-NLS-1$
		for (int i = 0; i < this.impl.length; i++) {
			buffer.append(this.impl[i]);
			if (i < this.impl.length - 1)
				buffer.append(", "); //$NON-NLS-1$
		}
		buffer.append(';');
		return buffer.toString();
	}
	@Override
	public int getElementType() {
		return SERVICE;
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
		hash = Util.combineHashCodes(hash, this.service.hashCode());
		return Util.combineHashCodes(hash, this.impl.hashCode());
	}
	public boolean equals(Object o) {
		if (!(o instanceof ProvidedService)) 
			return false;
		ProvidedService other = (ProvidedService) o;
		return super.equals(o) && 
				this.service.equals(other.service)
				&& this.impl.equals(other.impl);
	}
}
