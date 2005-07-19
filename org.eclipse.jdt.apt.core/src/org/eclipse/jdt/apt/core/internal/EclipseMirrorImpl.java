/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import org.eclipse.jdt.apt.core.env.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;

public interface EclipseMirrorImpl extends EclipseMirrorObject
{	
	/**
	 * @return the processor environment associated with the object.
	 * return null for primitive, void and error type. 
	 */
	public BaseProcessorEnv getEnvironment();

} 
