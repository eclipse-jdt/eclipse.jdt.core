/*******************************************************************************
 * Copyright (c) 2011 Stephan Herrmann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation 
 *******************************************************************************/
package org.eclipse.jdt.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;
 
/**
 * <p>
 * This annotation can be applied to a package, type, method or constructor in order to 
 * define that all contained entities for which a null annotation is otherwise lacking
 * should be considered as {@link NonNull @NonNull}.
 * <dl>
 * <dt>Canceling a default</dt>
 * <dd>By using a <code>@NonNullByDefault</code> annotation with the parameter <code>false</code>
 * a default from any enclosing scope can be canceled for the element being annotated.
 * <dt>Nested defaults</dt>
 * <dd>If a <code>@NonNullByDefault</code>
 * annotation is used within the scope of another <code>@NonNullByDefault</code>
 * annotation or a project wide default setting the inner most annotation defines the
 * default applicable at any given position (depending on the parameter {@link #value()}).</dd>
 * </dl>
 * Note that for applying an annotation to a package a file by the name
 * <code>package-info.java</code> is used.
 * 
 * @version 1.0
 * @author Stephan Herrmann
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({PACKAGE,TYPE,METHOD,CONSTRUCTOR})
public @interface NonNullByDefault {
	/**
	 * When parameterized with <code>false</code> the annotation specifies that the current element
	 * should not apply any default to un-annotated types.
	 */
	boolean value() default true;
}
