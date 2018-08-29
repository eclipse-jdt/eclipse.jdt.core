/*******************************************************************************
 * Copyright (c) 2009 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Walter Harley (eclipse@cafewalter.com)
 *******************************************************************************/

package targets.filer;

import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;
import gen6.Generated03;

/**
 * Processing this class should result in creation of a source file,
 * thereby allowing this class to compile. Note that unlike in Parent02,
 * the Generated03 class is imported rather than qualified.
 * See https://bugs.eclipse.org/296934.
 */
@GenClass6(name="Generated03", pkg="gen6", options={"forceElementResolution"})
public class Parent03 {
	Generated03 _gen;
}



