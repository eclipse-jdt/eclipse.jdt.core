/*******************************************************************************
 * Copyright (c) 2023 GK Software SE and others.
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
 * <dd>Here {@link Owning} denotes that responsibility to close a resource passed into this parameter will lie with the
 * receiving method, on this particular flow.
 * </dd>
 * <dt>Method ({@link ElementType#METHOD}) - as a way to refer to the method return value:</dt>
 * <dd>Here {@link Owning} denotes that responsibility to close a resource received from a call to this method will lie
 * with the receiving code.
 * </dd></dd></dt>
 * <p>Responsibility to close a resource may <strong>initially arise</strong> from these situations:</p>
 * <ul>
 * <li>Instantiating a class that is a subtype of {@link AutoCloseable}.</li>
 * <li>Receiving a method argument via a parameter of type {@link AutoCloseable} (or subtype) that is marked as {@code @Owning}.</li>
 * <li>Receiving a result from a call to method that is marked as {@code @Owning} and returns {@link AutoCloseable} (or a subtype).
 * 	<ul><li>If the {@code @Owning} annotation is absent in this situation, the receiving method is <em>potentially responsible</em>.</li></ul>
 * </li>
 * <li>
 * <p>Responsibility to close a resource may be <strong>fulfilled</strong> by ensuring any of these measures on every possible path:</p>
 * <ul>
 * <li>Invoking {@link AutoCloseable#close()}.
 * <li>Passing the resource into a method where the receiving parameter has type {@link AutoCloseable} (or subtype)
 *  and is marked as {@code @Owning}.</li>
 * <li>Returning the resource to the caller, provided that the current method is marked as {@code @Owning} and has a declared return type
 * 	of {@link AutoCloseable} (or a subtype).</li>
 * </ul>
 *
 * @since 2.3
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({ ElementType.PARAMETER, ElementType.METHOD })
public @interface Owning {
	// no details
}
