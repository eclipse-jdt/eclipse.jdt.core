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
import org.eclipse.jface.text.Document;
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
	public void testEmptyFirstRegionAtDocumentStart() throws Exception {
		String source = "\nclass A {\n\tvoid foo() {\n\t}\n}\n";
		assertEmptyLeadingRegionIsNeutral(source, 1, "\n");
	}

	public void testEmptyFirstRegionAtDocumentStartWithCrLf() throws Exception {
		String source = "\r\nclass A {\r\n\tvoid foo() {\r\n\t}\r\n}\r\n";
		assertEmptyLeadingRegionIsNeutral(source, 2, "\r\n");
	}

	public void testSingleEmptyRegionAtDocumentStartDoesNotChangeSource() throws Exception {
		String source = "\nclass A {\n\tvoid foo() {\n\t}\n}\n";

		assertEquals(source, formatAndApply(source, new IRegion[] { new Region(0, 0) }, "\n"));
	}

	public void testSingleEmptyRegionAfterDocumentStartDoesNotChangeSource() throws Exception {
		String source = "class A {\n\tvoid foo() {\n\t}\n}\n";
		int offset = source.indexOf("void");

		assertTrue(offset > 0);
		assertEquals(source, formatAndApply(source, new IRegion[] { new Region(offset, 0) }, "\n"));
	}

	private void assertEmptyLeadingRegionIsNeutral(String source, int nextRegionOffset, String lineSeparator) throws Exception {
		IRegion followingRegion = new Region(nextRegionOffset, source.length() - nextRegionOffset);
		String expected = formatAndApply(source, new IRegion[] { followingRegion }, lineSeparator);
		String actual = formatAndApply(source, new IRegion[] { new Region(0, 0), followingRegion }, lineSeparator);

		assertEquals(expected, actual);
		assertTrue(actual.startsWith(lineSeparator));
	}

	private String formatAndApply(String source, IRegion[] regions, String lineSeparator) throws Exception {
		TextEdit edit = new DefaultCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, source, regions, 0, lineSeparator);
		assertNotNull(edit);
		Document document = new Document(source);
		edit.apply(document);
		return document.get();
	}
}
