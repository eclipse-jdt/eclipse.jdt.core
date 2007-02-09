/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import org.eclipse.core.runtime.CoreException;

class ClassServiceFactory implements IServiceFactory {
	private final Class<?> _clazz;
	
	public ClassServiceFactory(Class<?> clazz) {
		_clazz = clazz;
	}

	public Object newInstance() throws CoreException {
		try {
			return _clazz.newInstance();
		} catch (InstantiationException e) {
			throw new CoreException(AptPlugin.createWarningStatus(e, 
					"Unable to create instance of annotation processor " + _clazz.getName())); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			throw new CoreException(AptPlugin.createWarningStatus(e, 
					"Unable to create instance of annotation processor " + _clazz.getName())); //$NON-NLS-1$
		}
	}
	
	public String toString() {
		if (_clazz == null) {
			return "unknown (null)"; //$NON-NLS-1$
		}
		else {
			return _clazz.getName();
		}
	}
}