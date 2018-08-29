/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package targets.model8;

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;

public class Z2 {
	public <@Type("mp1") KKK, @Type("mp2") VVV> void foo() {}
}
