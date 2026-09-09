/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Carsten Hammer - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.TextEdit;

public class FormatterRegionTests extends TestCase {

	public static Test suite() {
		return new TestSuite(FormatterRegionTests.class);
	}

	public FormatterRegionTests(String name) {
		super(name);
	}

	/**
	 * Regression test for https://github.com/eclipse-jdt/eclipse.jdt.ui/issues/2499.
	 * <p>
	 * A zero-length changed line at the start of the document followed by another changed
	 * region used to make the formatter pass {@code -1} to
	 * {@code TokenManager.countLineBreaksBetween()}.
	 */
	public void testEmptyFirstRegionAtDocumentStart() {
		String source = "\nclass A {\n\tvoid foo() {\n\t}\n}\n";
		IRegion[] regions = {
				new Region(0, 0),
				new Region(1, source.length() - 1)
		};

		TextEdit edit = new DefaultCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, source, regions, 0, "\n");

		assertNotNull(edit);
	}
}
