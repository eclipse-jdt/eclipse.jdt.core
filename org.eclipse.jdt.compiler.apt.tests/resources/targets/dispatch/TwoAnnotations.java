/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
