/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     René Brandstetter <Rene.Brandstetter@gmx.net> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

/**
 * ExtensionPoint-Factory to access the singleton instance of
 * the {@link AptCompilationParticipant} during ExtensionPoint processing.
 */
public class AptCompilationParticipantExtensionFactory implements
		IExecutableExtensionFactory {

	public Object create() throws CoreException {
		return AptCompilationParticipant.getInstance();
	}

}
