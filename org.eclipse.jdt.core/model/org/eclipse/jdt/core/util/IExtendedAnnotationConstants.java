/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;

/**
 * Description of an extended annotation target types constants as described in the JVM specifications
 * (added in JavaSE-1.7).
 *
 * @since 3.9
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IExtendedAnnotationConstants {
	int METHOD_RECEIVER = AnnotationTargetTypeConstants.METHOD_RECEIVER;
	int METHOD_RECEIVER_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.METHOD_RECEIVER_GENERIC_OR_ARRAY;
	int METHOD_RETURN_TYPE = AnnotationTargetTypeConstants.METHOD_RETURN_TYPE;
	int METHOD_RETURN_TYPE_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.METHOD_RETURN_TYPE_GENERIC_OR_ARRAY;
	int METHOD_PARAMETER = AnnotationTargetTypeConstants.METHOD_PARAMETER;
	int METHOD_PARAMETER_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.METHOD_PARAMETER_GENERIC_OR_ARRAY;
	int FIELD = AnnotationTargetTypeConstants.FIELD;
	int FIELD_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.FIELD_GENERIC_OR_ARRAY;
	int CLASS_TYPE_PARAMETER_BOUND = AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND;
	int CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY;
	int METHOD_TYPE_PARAMETER_BOUND = AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND;
	int METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY;
	int CLASS_EXTENDS_IMPLEMENTS = AnnotationTargetTypeConstants.CLASS_EXTENDS_IMPLEMENTS;
	int CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY;
	int THROWS = AnnotationTargetTypeConstants.THROWS;
	int THROWS_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.THROWS_GENERIC_OR_ARRAY;
	int WILDCARD_BOUND = AnnotationTargetTypeConstants.WILDCARD_BOUND;
	int WILDCARD_BOUND_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.WILDCARD_BOUND_GENERIC_OR_ARRAY;
	int METHOD_TYPE_PARAMETER = AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER;
	int METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY;
	int CLASS_TYPE_PARAMETER = AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER;
	int CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY;
	int TYPE_CAST = AnnotationTargetTypeConstants.TYPE_CAST;
	int TYPE_CAST_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.TYPE_CAST_GENERIC_OR_ARRAY;
	int TYPE_INSTANCEOF = AnnotationTargetTypeConstants.TYPE_INSTANCEOF;
	int TYPE_INSTANCEOF_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.TYPE_INSTANCEOF_GENERIC_OR_ARRAY;
	int OBJECT_CREATION = AnnotationTargetTypeConstants.OBJECT_CREATION;
	int OBJECT_CREATION_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.OBJECT_CREATION_GENERIC_OR_ARRAY;
	int LOCAL_VARIABLE = AnnotationTargetTypeConstants.LOCAL_VARIABLE;
	int LOCAL_VARIABLE_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.LOCAL_VARIABLE_GENERIC_OR_ARRAY;
	int TYPE_ARGUMENT_CONSTRUCTOR_CALL = AnnotationTargetTypeConstants.TYPE_ARGUMENT_CONSTRUCTOR_CALL;
	int TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY;
	int TYPE_ARGUMENT_METHOD_CALL = AnnotationTargetTypeConstants.TYPE_ARGUMENT_METHOD_CALL;
	int TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY;
	int CLASS_LITERAL = AnnotationTargetTypeConstants.CLASS_LITERAL;
	int CLASS_LITERAL_GENERIC_OR_ARRAY = AnnotationTargetTypeConstants.CLASS_LITERAL_GENERIC_OR_ARRAY;
}
