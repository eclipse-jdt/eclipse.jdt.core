/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.internal.tools.unicode;

import java.util.HashSet;
import java.util.Set;

public class PartEnvironment extends Environment {
	private static final String RESOURCE_FILE_NAME = "part"; //$NON-NLS-1$

	private static enum Category {
		Lu, Ll, Lt, Lm, Lo, Nl, Sc, Pc, Nd, Mc, Mn, Cf;
	}

	private static final Set<String> categories = new HashSet<>();
	static {
		for (Category c : Category.values()) {
			categories.add(c.name());
		}
	}

	@Override
	public boolean hasCategory(String value) {
		return categories.contains(value);
	}

	@Override
	public String getResourceFileName() {
		return RESOURCE_FILE_NAME;
	}

}
