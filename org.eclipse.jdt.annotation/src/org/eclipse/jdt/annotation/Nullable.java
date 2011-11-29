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
 * annotation is allowed to have the value <code>null</code> at runtime.
 * <p>
 * This has two consequences:
 * <ul>
 * <li>Binding a <code>null</code> value to the entity is legal.</li>
 * <li>Dereferencing the entity is unsafe, i.e., a <code>NullPointerException</code> can occur at runtime.</li>
 * </ul>
 * </p>
 * @version 1.0
 * @author Stephan Herrmann
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({METHOD,PARAMETER,LOCAL_VARIABLE})
public @interface Nullable {

}