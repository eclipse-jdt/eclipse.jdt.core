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
 *******************************************************************************/

package targets.infrastructure;

import org.eclipse.jdt.apt.pluggable.tests.annotations.Message6;

/**
 * A simple class with no annotations, to test compilation of vanilla projects
 */
public class NoAnno {
	// This is here to verify that we have access to the annotations jar from within the test project.
	Class<?> _annoClass = Message6.class;
}

