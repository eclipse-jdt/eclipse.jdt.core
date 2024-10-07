/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
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


package org.eclipse.jdt.apt.tests.annotations.nestedhelloworld;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.apt.tests.annotations.BaseFactory;

public class NestedHelloWorldAnnotationProcessorFactory extends BaseFactory
{
	public NestedHelloWorldAnnotationProcessorFactory() {
		super(NestedHelloWorldAnnotation.class.getName());
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> atds,
			AnnotationProcessorEnvironment env)
	{
		return new NestedHelloWorldAnnotationProcessor( env );
	}

	@Override
	public Collection<String> supportedOptions() {
		return Collections.singletonList(AptPreferenceConstants.RTTG_ENABLED_OPTION);
	}

}
