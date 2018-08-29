/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
package targets.inherited;

import java.awt.Point;

import org.eclipse.jdt.compiler.apt.tests.annotations.ArgsConstructor;

@ArgsConstructor({ Point.class })
public class TestGeneric<K extends Point> {

	public TestGeneric(K k) {
	}
}
