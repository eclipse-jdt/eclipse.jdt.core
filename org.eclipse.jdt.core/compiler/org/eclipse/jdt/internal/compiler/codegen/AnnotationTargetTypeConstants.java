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
package org.eclipse.jdt.internal.compiler.codegen;

public interface AnnotationTargetTypeConstants {
	int METHOD_RECEIVER = 0x06;
	int METHOD_RECEIVER_GENERIC_OR_ARRAY = 0x07;
	int METHOD_RETURN_TYPE = 0x0A;
	int METHOD_RETURN_TYPE_GENERIC_OR_ARRAY = 0x0B;
	int METHOD_PARAMETER = 0x0C;
	int METHOD_PARAMETER_GENERIC_OR_ARRAY = 0x0D;
	int FIELD = 0x0E;
	int FIELD_GENERIC_OR_ARRAY = 0x0F;
	int CLASS_TYPE_PARAMETER_BOUND = 0x10;
	int CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = 0x11;
	int METHOD_TYPE_PARAMETER_BOUND = 0x12;
	int METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = 0x13;
	int CLASS_EXTENDS_IMPLEMENTS = 0x14;
	int CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY = 0x15;
	int THROWS = 0x16;
	int THROWS_GENERIC_OR_ARRAY = 0x17;
	int WILDCARD_BOUND = 0x1C;
	int WILDCARD_BOUND_GENERIC_OR_ARRAY = 0x1D;
	int METHOD_TYPE_PARAMETER = 0x20;
	int METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY = 0x21;
	int CLASS_TYPE_PARAMETER = 0x22;
	int CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY = 0x23;
	int TYPE_CAST = 0x00;
	int TYPE_CAST_GENERIC_OR_ARRAY = 0x01;
	int TYPE_INSTANCEOF = 0x02;
	int TYPE_INSTANCEOF_GENERIC_OR_ARRAY = 0x03;
	int OBJECT_CREATION = 0x04;
	int OBJECT_CREATION_GENERIC_OR_ARRAY = 0x05;
	int LOCAL_VARIABLE = 0x08;
	int LOCAL_VARIABLE_GENERIC_OR_ARRAY = 0x09;
	int TYPE_ARGUMENT_CONSTRUCTOR_CALL = 0x18;
	int TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY = 0x19;
	int TYPE_ARGUMENT_METHOD_CALL = 0x1A;
	int TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY = 0x1B;
	int CLASS_LITERAL = 0x1E;
	int CLASS_LITERAL_GENERIC_OR_ARRAY = 0x1F;
}
