/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.hierarchy;

import org.eclipse.jdt.internal.compiler.env.IGenericType;

public interface IHierarchyRequestor {
/**
 * Connect the supplied type to its superclass & superinterfaces.
 * The superclass & superinterfaces are the identical binary or source types as
 * supplied by the name environment.
 */

public void connect(IGenericType suppliedType, IGenericType superclass, IGenericType[] superinterfaces);
}
