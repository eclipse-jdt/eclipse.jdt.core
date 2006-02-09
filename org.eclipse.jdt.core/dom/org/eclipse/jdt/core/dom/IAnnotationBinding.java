/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *    IBM Corporation - changed interface to extend IBinding
 *    IBM Corporation - renamed from IResolvedAnnotation to IAnnotationBinding
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

/**
 * Represents an resolved annotation. Resolved annotation are computed along with other
 * bindings; they correspond to {@link Annotation} nodes.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.2
 */
public interface IAnnotationBinding extends IBinding {

	/**
	 * Returns the complete list of member value pairs for this annotation, including
	 * ones explicitly listed in the annotation as well as entries for 
	 * annotation type members with default values that are implied.
	 * 
	 * @return a possibly empty list of resolved member value pairs
	 */
	IMemberValuePairBinding[] getAllMemberValuePairs();
	
	/**
	 * Returns the type of the annotation. The resulting type binding will always
	 * return <code>true</code>	to <code>ITypeBinding.isAnnotation()</code>.
	 * 
	 * @return the type of the annotation
	 */
	ITypeBinding getAnnotationType();
	
	/**
	 * Returns the list of declared member value pairs for this annotation.
	 * Returns an empty list for a <code>MarkerAnnotation</code>, a one element
	 * list for a <code>SingleMemberAnnotation</code>, and one entry for each
	 * of the explicitly listed values in a <code>NormalAnnotation</code>.
	 * <p>
	 * Note that the list only includes entries for annotation type members that are
	 * explicitly mentioned in the annotation. The list does not include any 
	 * annotation type members with default values that are merely implied.
	 * Use {@link #getAllMemberValuePairs()} to get those as well.
	 * </p>
	 * 
	 * @return a possibly empty list of resolved member value pairs
	 */
	IMemberValuePairBinding[] getDeclaredMemberValuePairs();
	
	/**
	 * Returns the name of the annotation type.
	 * 
	 * @return the name of the annotation type
	 */
	public String getName();
	
}
