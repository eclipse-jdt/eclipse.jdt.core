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

/**
 * Description of an extended annotation structure as described in the JVM specifications
 * (added in JavaSE-1.7).
 *
 * This interface may be implemented by clients.
 *
 * @since 3.9
 */
public interface IExtendedAnnotation extends IAnnotation {
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
