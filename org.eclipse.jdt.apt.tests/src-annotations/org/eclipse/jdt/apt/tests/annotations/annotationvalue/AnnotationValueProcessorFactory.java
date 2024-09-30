/*******************************************************************************
 * Copyright (c) 2005, 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    het - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.annotationvalue;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.Set;
import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

public class AnnotationValueProcessorFactory extends BaseFactory {
	public AnnotationValueProcessorFactory() {
		super("trigger.MyTrigger"); //$NON-NLS-1$
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> atds,
			AnnotationProcessorEnvironment env) {
		return new AnnotationValueProcessor(env);
	}
}
