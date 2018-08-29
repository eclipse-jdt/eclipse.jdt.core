/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package targets.errors.pb;

import target.errors.pa.AnnoZ;
import target.errors.pa.Outer;
import target.errors.pa.Nested;

@AnnoZ(
		annoZString = "annoZOnD")
@Outer(@Nested())
@SuppressWarnings("all")
public class D {
	public enum DEnum { DEnum1, DEnum2, DEnum3 }
	
	@AnnoZ(annoZString = "annoZOnDMethod", annoZint = 31)
	public void methodDvoid(DEnum dEnum1) {
	}
}

