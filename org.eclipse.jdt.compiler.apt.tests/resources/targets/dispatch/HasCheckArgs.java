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

/**
 * Target for annotation processing test.  Processing this
 * has no effect, but invokes a processor that complains
 * if it does not see expected environment variables.
 * @see org.eclipse.jdt.compiler.apt.tests.processors.checkargs.CheckArgsProc.
 */
@CheckArgs
public class HasCheckArgs {
}