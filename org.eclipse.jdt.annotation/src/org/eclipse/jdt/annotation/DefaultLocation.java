/*******************************************************************************
 * Copyright (c) 2014 Stephan Herrmann and others.
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
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.annotation;

/**
 * Locations that can be affected by a {@link NonNullByDefault} annotation.
 * Each constant of this enum describes a specific kind of type use.
 * Wildcards and the use of type variables are always excluded from {@link NonNullByDefault}.
 * @since 2.0
 */
public enum DefaultLocation {
	
	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * parameters of any method or constructor within the scope of the annotated declaration.
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(PARAMETER)
	 * interface X {
	 *     void print(Number n);
	 * }</pre>
	 * <p>
	 * Here <code>Number</code> will be interpreted as <code>@NonNull Number</code>.
	 * </p>
	 */
	PARAMETER,
	
	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * method return types within the scope of the annotated declaration.
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(RETURN_TYPE)
	 * interface X {
	 *     Number getNumber();
	 * }</pre>
	 * <p>
	 * Here <code>Number</code> will be interpreted as <code>@NonNull Number</code>.
	 * </p>
	 */
	RETURN_TYPE,
	
	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * field types within the scope of the annotated declaration.
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(FIELD)
	 * class X {
	 *     Number number = Integer.MAX_VALUE;
	 * }</pre>
	 * <p>
	 * Here <code>Number</code> will be interpreted as <code>@NonNull Number</code>.
	 * </p>
	 */
	FIELD,
	
	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * type parameter declarations within the scope of the annotated declaration.
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(TYPE_PARAMETER)
	 * class X {
	 *     &lt;T&gt; T identity(T t) { return t; }
	 * }</pre>
	 * <p>
	 * Here <code>&lt;T&gt;</code> will be interpreted as <code>&lt;@NonNull T&gt;</code>.
	 * </p>
	 */
	TYPE_PARAMETER,
	
	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * explicit type bounds within the scope of the annotated declaration.
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(TYPE_BOUND)
	 * interface X {
	 *     &lt;T extends Number&gt; void process(T t, List&lt;? super Number&gt; l);
	 * }</pre>
	 * <p>
	 * Here both occurrences of <code>Number</code> will be interpreted as <code>@NonNull Number</code>.
	 * </p>
	 */
	TYPE_BOUND,
	
	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * type arguments within the scope of the annotated declaration (except wildcards and
	 * type variables).
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(TYPE_ARGUMENT)
	 * interface X&lt;T&gt; {
	 *     void process(List&lt;T&gt; tl, List&lt;Number&gt; nl);
	 * }</pre>
	 * <p>
	 * Here <code>Number</code> will be interpreted as <code>@NonNull Number</code>,
	 * but the use of type variable <code>T</code> is not affected.
	 * </p>
	 */
	TYPE_ARGUMENT,

	/**
	 * Defines that a given {@link NonNullByDefault} annotation should affect all unannotated
	 * array components within the scope of the annotated declaration.
	 * 
	 * <h2>Example</h2>
	 * <pre> @NonNullByDefault(ARRAY_CONTENTS)
	 * interface X {
	 *     Number[] n1;
	 *     Number[][] n2;
	 * }</pre>
	 * <p>
	 * These declarations are interpreted as:
	 * <pre>    &#64;NonNull Number [] n1;
	 *    &#64;NonNull Number [] @NonNull[] n2;</pre>
	 * I.e., both fields can still be <code>null</code> (see the unannotated left-most pair
	 * of brackets) but none of the <em>contents</em> of these arrays is allowed to be 
	 * <code>null</code> (at any dimension).
	 * </p>
	 */
	ARRAY_CONTENTS
}
