/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.errors.pb;

import target.errors.pa.AnnoZ;

@AnnoZ(
		annoZString = "annoZOnD")
@SuppressWarnings("all")
public class D {
	public enum DEnum { DEnum1, DEnum2, DEnum3 }
	
	@AnnoZ(annoZString = "annoZOnDMethod", annoZint = 31)
	public void methodDvoid(DEnum dEnum1) {
	}
}

