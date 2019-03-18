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

import java.io.IOException;

public class GenerateIdentifierPartResources {

	public static void main(String[] args) throws IOException {
		UnicodeResourceGenerator generator = new UnicodeResourceGenerator(args, true);
		generator.generate();
	}
}
