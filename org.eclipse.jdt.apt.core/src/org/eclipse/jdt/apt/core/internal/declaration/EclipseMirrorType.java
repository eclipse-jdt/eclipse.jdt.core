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
 *     thanson@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.type.TypeMirror;

/**
 * The base type for all Mirror type objects
 * @author thanson
 */
public interface EclipseMirrorType extends EclipseMirrorObject, TypeMirror {
	public boolean isAssignmentCompatible(EclipseMirrorType left);
	public boolean isSubTypeCompatible(EclipseMirrorType type);

	public ITypeBinding getTypeBinding();
}
