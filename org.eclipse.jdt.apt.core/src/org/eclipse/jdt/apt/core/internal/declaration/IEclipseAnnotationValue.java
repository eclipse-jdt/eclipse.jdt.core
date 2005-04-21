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
package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;

import com.sun.mirror.declaration.AnnotationValue;

/**
 * Represents an annotation member value. 
 * The value may have come from source or from binary.
 */
public interface IEclipseAnnotationValue extends AnnotationValue, EclipseMirrorImpl 
{

}
