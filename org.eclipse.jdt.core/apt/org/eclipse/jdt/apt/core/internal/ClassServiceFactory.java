/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public Object newInstance() throws CoreException {
		try {
			return _clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new CoreException(AptPlugin.createWarningStatus(e,
					"Unable to create instance of annotation processor " + _clazz.getName())); //$NON-NLS-1$
		} 
	}

	@Override
	public String toString() {
		if (_clazz == null) {
			return "unknown (null)"; //$NON-NLS-1$
		}
		else {
			return _clazz.getName();
		}
	}
}