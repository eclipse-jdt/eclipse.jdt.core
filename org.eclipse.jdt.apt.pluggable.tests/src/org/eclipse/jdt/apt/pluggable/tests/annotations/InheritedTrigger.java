/*******************************************************************************
 * Copyright (c) 2009 Walter Harley
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Walter Harley - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.annotations;

import java.lang.annotation.Inherited;

/**
 * An annotation that is marked with {@link Inherited}
 * @since 3.5
 */
@Inherited
public @interface InheritedTrigger {
	int value();
}
