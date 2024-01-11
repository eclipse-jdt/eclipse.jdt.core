/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows to specify responsibility for certain objects as they are passed from one method to another.
 * <p>
 * Based on this annotation, flow oriented analysis tools can be made more precise without the need to perform
 * full-system analysis, as the {@code @Owning} annotation describes a contract, consisting of two sides that can be
 * checked independently.
 * </p>
 * <p>
 * The Eclipse Compiler for Java&trade; (version 3.37 and greater) interprets this annotation when placed on a value of
 * type {@link AutoCloseable} or any subtype. This annotation is interpreted specifically in these locations:
 * </p>
 * <dl>
 * <dt>Method parameter ({@link ElementType#PARAMETER}):</dt>
 * <dd>Here {@code @Owning} denotes that the responsibility to close a resource passed into this parameter will lie with the
 * receiving method, on this particular flow.
 * </dd>
 * <dt>Method ({@link ElementType#METHOD}) - as a way to refer to the method return value:</dt>
 * <dd>Here {@code @Owning} denotes that responsibility to close a resource received from a call to this method will lie
 * with the receiving code.
 * </dd>
 * <dt>Field ({@link ElementType#FIELD})</dt>
 * <dd>This annotation marks that the lifecycle of a resource stored in the field is tied to the lifecycle of the
 *  enclosing object. In order to allow for precise analysis in this situation, it is recommended that the enclosing
 *  class of such a field implements {@link AutoCloseable}. In that case, it will be the responsibility of the class's
 *  {@code close()} implementation to also close all resources stored in {@code @Owning} fields.
 * <dt>Method receiver (via {@link ElementType#TYPE_USE})</dt>
 * <dd>Annotating a method receiver (explicit {@code this} parameter) as owning signals that the method is responsible
 *  for closing all contained resources. The method will be treated as a "custom close method".<br/>
 *  The annotation is not evaluated in any other TYPE_USE locations.
 * </dl>
 * <p><strong>Responsibility</strong> to close a resource may <strong>initially arise</strong> from these situations:</p>
 * <ul>
 * <li>Instantiating a class that is a subtype of {@link AutoCloseable}.</li>
 * <li>Receiving a method argument via a parameter of type {@link AutoCloseable} (or subtype) that is marked as {@code @Owning}.</li>
 * <li>Receiving a result from a call to method that is marked as {@code @Owning} and returns {@link AutoCloseable} (or a subtype).
 * 	<ul><li>If the {@code @Owning} annotation is absent in this situation, the receiving method is <em>potentially responsible</em>.</li></ul>
 * </li>
 * <li>Within the {@code close()} method of a class implementing {@link AutoCloseable}, and within each "custom close method",
 *  each resource field annotated as {@code @Owning} must be closed.</li>
 * </ul>
 * <p><strong>Responsibility</strong> to close a resource may be <strong>fulfilled</strong> by ensuring any of these measures on every possible path:</p>
 * <ul>
 * <li>Invoking {@link AutoCloseable#close()} or a "custom close method".</li>
 * <li>Passing the resource into a method where the receiving parameter has type {@link AutoCloseable} (or subtype)
 *  and is marked as {@code @Owning}.</li>
 * <li>Returning the resource to the caller, provided that the current method is marked as {@code @Owning} and has a declared return type
 * 	of {@link AutoCloseable} (or a subtype).</li>
 * <li>Assigning the resource to a field annotated as {@code @Owning}.
 * <li>Within the {@code close()} method of a class implementing {@link AutoCloseable} and within a "custom close method"
 *  closing the resource held by a field tagged as {@code @Owning} can happen either directly, or for inherited fields
 *  by invoking {@code super.close()}, or the super version of a "custom close method".
 * </ul>
 *
 * @since 2.3
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE })
public @interface Owning {
	// no details
}
