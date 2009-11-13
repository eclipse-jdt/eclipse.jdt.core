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

/**
 * Description of an extended annotation structure as described in the JVM specifications
 * (added in JavaSE-1.7).
 *
 * This interface may be implemented by clients.
 *
 * @since 3.6
 */
public interface IExtendedAnnotation extends IAnnotation {
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

	/**
	 * Answer back the target type as described in the JVM specifications.
	 *
	 * @return the target type
	 */
	int getTargetType();
	
	/**
	 * Answer back the offset.
	 * 
	 * For a target_type value equals to:
	 * <table border="1">
	 * <tr>
	 * <th>target_type</th>
	 * <th>offset description</th>
	 * </tr>
	 * <tr>
	 * <td>0x00, 0x02, 0x04, 0x1E</td>
	 * <td>The offset within the bytecodes of the containing method of the <code>checkcast</code> 
	 * bytecode emitted for a typecast, the <code>instanceof</code> bytecode for the type tests, 
	 * the <code>new</code> bytecode emitted for the object creation expression, the <code>ldc(_w)</code>
	 * bytecode emitted for class literal, or the <code>getstatic</code> bytecode emitted for primitive
	 * class literals.</td>
	 * </tr>
	 * <tr>
	 * <td>0x18, 0x1A</td>
	 * <td>The offset within the bytecodes of the containing method of the <code>new</code> 
	 * bytecode emitted for a constructor call, or the <code>invoke{interface|special|static|virtual}</code>
	 * bytecode emitted for a method invocation.</td>
	 * </tr>
	 * </table>
	 * 
	 * 
	 * @return the offset
	 */
	int getOffset();
	
	/**
	 * Answer back the local variable reference info table length of this entry as specified in
	 * the JVM specifications.
	 * 
	 * <p>This is defined only for annotations related a local variable.</p>
	 *
	 * @return the local variable reference info table length of this entry as specified in
	 * the JVM specifications
	 */
	int getLocalVariableRefenceInfoLength();
	
	/**
	 * Answer back the local variable reference info table of this entry as specified in
	 * the JVM specifications. Answer an empty array if none.
	 * 
	 * <p>This is defined only for annotations related a local variable.</p>
	 *
	 * @return the local variable reference info table of this entry as specified in
	 * the JVM specifications. Answer an empty array if none
	 */
	ILocalVariableReferenceInfo[] getLocalVariableTable();
	
	/**
	 * Answer back the method parameter index.
	 * 
	 * <p>The index is 0-based.</p>
	 * 
	 * @return the method parameter index
	 */
	int getParameterIndex();

	/**
	 * Answer back the method type parameter index.
	 * 
	 * <p>The index is 0-based.</p>
	 * 
	 * @return the method type parameter index
	 */
	int getTypeParameterIndex();

	/**
	 * Answer back the method type parameter bound index.
	 * 
	 * <p>The index is 0-based.</p>
	 * 
	 * @return the method type parameter bound index
	 */
	int getTypeParameterBoundIndex();

	/**
	 * Answer back the index in the given different situations.
	 * 
	 * <p>The index is 0-based.</p>
	 * 
	 * <table border="1">
	 * <tr>
	 * <th>target_type</th>
	 * <th>offset description</th>
	 * </tr>
	 * <tr>
	 * <td>0x18, 0x1A</td>
	 * <td>the type argument index in the expression</td>
	 * </tr>
	 * <tr>
	 * <td>0x14</td>
	 * <td>the index of the type in the clause: <code>-1 (255)</code> is used if the annotation is on 
	 * the superclass type, and the value <code>i</code> is used if the annotation is on the <code>i</code>th
	 * superinterface type (counting from zero).</td>
	 * </tr>
	 * <tr>
	 * <td>0x16</td>
	 * <td>the index of the exception type in the clause: the value <code>i</code> denotes an annotation of the 
	 * <code>i</code>th exception type (counting from zero).</td>
	 * </tr>
	 * </table>
	 * @return the index in the given different situations
	 */
	int getAnnotationTypeIndex();
	
	/**
	 * Answer back the target type of the location of the wildcard as described in the JVM specifications.
	 *
	 * @return the target type of the location of the wildcard
	 */
	int getWildcardLocationType();
	
	/**
	 * Answer back the locations of the wildcard type as described in the JVM specifications.
	 *
	 * @return the locations of the wildcard type
	 */
	int[] getWildcardLocations();
	
	/**
	 * Answer back the locations of the annotated type as described in the JVM specifications.
	 * 
	 * <p>This is used for parameterized and array types.</p>
	 *
	 * @return the locations of the annotated type
	 */
	int[] getLocations();
}
