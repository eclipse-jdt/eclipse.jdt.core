/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

import org.eclipse.jdt.internal.compiler.codegen.AnnotationContext;

/**
 * Description of an extended annotation target types constants as described in the JVM specifications
 * (added in JavaSE-1.7).
 *
 * @since 3.6
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IExtendedAnnotationConstants {
	int METHOD_RECEIVER = AnnotationContext.METHOD_RECEIVER;
	int METHOD_RECEIVER_GENERIC_OR_ARRAY = AnnotationContext.METHOD_RECEIVER_GENERIC_OR_ARRAY;
	int METHOD_RETURN_TYPE = AnnotationContext.METHOD_RETURN_TYPE;
	int METHOD_RETURN_TYPE_GENERIC_OR_ARRAY = AnnotationContext.METHOD_RETURN_TYPE_GENERIC_OR_ARRAY;
	int METHOD_PARAMETER = AnnotationContext.METHOD_PARAMETER;
	int METHOD_PARAMETER_GENERIC_OR_ARRAY = AnnotationContext.METHOD_PARAMETER_GENERIC_OR_ARRAY;
	int FIELD = AnnotationContext.FIELD;
	int FIELD_GENERIC_OR_ARRAY = AnnotationContext.FIELD_GENERIC_OR_ARRAY;
	int CLASS_TYPE_PARAMETER_BOUND = AnnotationContext.CLASS_TYPE_PARAMETER_BOUND;
	int CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = AnnotationContext.CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY;
	int METHOD_TYPE_PARAMETER_BOUND = AnnotationContext.METHOD_TYPE_PARAMETER_BOUND;
	int METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = AnnotationContext.METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY;
	int CLASS_EXTENDS_IMPLEMENTS = AnnotationContext.CLASS_EXTENDS_IMPLEMENTS;
	int CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY = AnnotationContext.CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY;
	int THROWS = AnnotationContext.THROWS;
	int THROWS_GENERIC_OR_ARRAY = AnnotationContext.THROWS_GENERIC_OR_ARRAY;
	int WILDCARD_BOUND = AnnotationContext.WILDCARD_BOUND;
	int WILDCARD_BOUND_GENERIC_OR_ARRAY = AnnotationContext.WILDCARD_BOUND_GENERIC_OR_ARRAY;
	int METHOD_TYPE_PARAMETER = AnnotationContext.METHOD_TYPE_PARAMETER;
	int METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY = AnnotationContext.METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY;
	int CLASS_TYPE_PARAMETER = AnnotationContext.CLASS_TYPE_PARAMETER;
	int CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY = AnnotationContext.CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY;
	int TYPE_CAST = AnnotationContext.TYPE_CAST;
	int TYPE_CAST_GENERIC_OR_ARRAY = AnnotationContext.TYPE_CAST_GENERIC_OR_ARRAY;
	int TYPE_INSTANCEOF = AnnotationContext.TYPE_INSTANCEOF;
	int TYPE_INSTANCEOF_GENERIC_OR_ARRAY = AnnotationContext.TYPE_INSTANCEOF_GENERIC_OR_ARRAY;
	int OBJECT_CREATION = AnnotationContext.OBJECT_CREATION;
	int OBJECT_CREATION_GENERIC_OR_ARRAY = AnnotationContext.OBJECT_CREATION_GENERIC_OR_ARRAY;
	int LOCAL_VARIABLE = AnnotationContext.LOCAL_VARIABLE;
	int LOCAL_VARIABLE_GENERIC_OR_ARRAY = AnnotationContext.LOCAL_VARIABLE_GENERIC_OR_ARRAY;
	int TYPE_ARGUMENT_CONSTRUCTOR_CALL = AnnotationContext.TYPE_ARGUMENT_CONSTRUCTOR_CALL;
	int TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY = AnnotationContext.TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY;
	int TYPE_ARGUMENT_METHOD_CALL = AnnotationContext.TYPE_ARGUMENT_METHOD_CALL;
	int TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY = AnnotationContext.TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY;
	int CLASS_LITERAL = AnnotationContext.CLASS_LITERAL;
	int CLASS_LITERAL_GENERIC_OR_ARRAY = AnnotationContext.CLASS_LITERAL_GENERIC_OR_ARRAY;
}
