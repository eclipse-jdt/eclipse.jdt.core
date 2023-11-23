/*******************************************************************************
 * Copyright (c) 2007, 2018 BEA Systems, Inc. and others
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
			IJavaProject jproject, Compiler compiler, boolean isTestCode) {
		super(dispatchManager, jproject, compiler, isTestCode);
	}

	@Override
	public Phase getPhase() {
		return Phase.RECONCILE;
	}

}
