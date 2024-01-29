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
 * This annotation allows to specify absence of responsibility for certain objects as they are passed from one method to another.
 * <p>This annotation is the direct inverse of {@link Owning} and can be used in all locations, where that annotation is used.
 *  {@code @NotOwning} can be used do specify a shared responsibility rather than the clear separation between sources
 *  and sinks of resources given by {@link Owning}.
 * </p>
 * <p>
 *  In particular the annotation {@code @NotOwning} makes explicit that absence of {@code @Owning} is by intention.
 * </p>
 * <p>Additionally, placing {@code @NotOwning} on a class that implements {@link AutoCloseable} specifies that instances of this
 *  class do <em>not</em> hold any gc-resistant resources and therefore calling {@link AutoCloseable#close()} is not necessary.
 * </p>
 *
 * @since 2.3
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({ ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
public @interface NotOwning {
	// no details
}
