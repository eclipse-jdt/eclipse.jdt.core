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
 * Qualifier for a type in a method signature or a local variable declaration:
 * The entity (return value, parameter, local variable) whose type has this
 * annotation can never have the value <code>null</code> at runtime.
 * <p>
 * This has two consequences:
 * <ol>
 * <li>Dereferencing the entity is safe, i.e., no <code>NullPointerException</code> can occur at runtime.</li>
 * <li>An attempt to bind a <code>null</code> value to the entity is a compile time error.</li>
 * </ol>
 * For the second case diagnostics issued by the compiler should distinguish three situations:
 * <ol>
 * <li>Nullness of the value can be statically determined, the entity is definitely bound from either of:
 *     <ul><li>the value <code>null</code>, or</li>
 *         <li>an entity with a {@link Nullable @Nullable} type.</li></ul></li>
 * <li>Nullness can not definitely be determined, because different code branches yield different results.</li>
 * <li>Nullness can not be determined, because other program elements are involved for which
 *     null annotations are lacking.</li>
 * </ol>
 * </p>
 * @version 1.0
 * @author Stephan Herrmann
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({METHOD,PARAMETER,LOCAL_VARIABLE})
public @interface NonNull {

}
