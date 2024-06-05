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
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;

/**
 * The base of all eclipse type system object
 * @author tyeung
 */
public interface EclipseMirrorObject {

	public enum MirrorKind
    {
        CONSTRUCTOR,
        METHOD,
        ANNOTATION_ELEMENT,
        FIELD,
        ENUM_CONSTANT,
        ANNOTATION_VALUE,
        ANNOTATION_MIRROR,
        TYPE_ANNOTATION,
        TYPE_INTERFACE,
        TYPE_CLASS,
        TYPE_ENUM,
        TYPE_ARRAY,
        TYPE_WILDCARD,
        TYPE_VOID,
        TYPE_PRIMITIVE,
        TYPE_PARAMETER_VARIABLE,
        TYPE_ERROR,
        FORMAL_PARAMETER,
        PACKAGE
    }

    public MirrorKind kind();

	/**
	 * @return the processor environment associated with the object.
	 * return null for primitive, void and error type.
	 */
	public BaseProcessorEnv getEnvironment();


}
