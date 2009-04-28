/*******************************************************************************
 * Copyright (c) 2009 Walter Harley 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
