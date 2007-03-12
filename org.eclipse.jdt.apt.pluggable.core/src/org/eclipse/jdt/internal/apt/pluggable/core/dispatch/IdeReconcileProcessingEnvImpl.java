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

package org.eclipse.jdt.internal.apt.pluggable.core.dispatch;

import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.Compiler;

/**
 * ProcessingEnvironment for reconciles in IDE.
 * @since 3.3
 */
public class IdeReconcileProcessingEnvImpl extends IdeProcessingEnvImpl {

	public IdeReconcileProcessingEnvImpl(IdeAnnotationProcessorManager dispatchManager,
			IJavaProject jproject, Compiler compiler) {
		super(dispatchManager, jproject, compiler);
	}

	@Override
	public Phase getPhase() {
		return Phase.RECONCILE;
	}

}
