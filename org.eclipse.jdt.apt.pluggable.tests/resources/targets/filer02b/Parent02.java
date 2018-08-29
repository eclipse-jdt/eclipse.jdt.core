/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
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

package targets.filer;

import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;

/**
 * Processing this class should result in removal of the previously
 * generated type and creation of a new one, resulting in a compilation error.
 */
@GenClass6(name="XxxGenerated02", pkg="gen6")
public class Parent02 {
	gen6.Generated02 _gen;
}

