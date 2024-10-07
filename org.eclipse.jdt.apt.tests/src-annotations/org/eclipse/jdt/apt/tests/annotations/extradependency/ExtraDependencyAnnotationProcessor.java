/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.extradependency;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

public class ExtraDependencyAnnotationProcessor extends BaseProcessor {

	public ExtraDependencyAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process() {
		_env.getTypeDeclaration( "p1.p2.p3.p4.C" ); //$NON-NLS-1$
	}
}
