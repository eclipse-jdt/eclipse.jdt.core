/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/
package targets.dispatch;

import org.eclipse.jdt.compiler.apt.tests.annotations.CheckArgs;
import org.eclipse.jdt.compiler.apt.tests.annotations.GenClass;

/**
 * Target for annotation processor tests.
 * @since 3.3
 */
@CheckArgs
@GenClass(clazz="gen.TwoAnnotationsGen", method="foo")
public class TwoAnnotations {

}
