/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.parser;

/**
 * Javadoc tag constants.
 *
 * @since 3.2
 */
public interface JavadocTagConstants {

	// recognized tags
	public static final char[] TAG_DEPRECATED = "deprecated".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_PARAM = "param".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_RETURN = "return".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_THROWS = "throws".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_EXCEPTION = "exception".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SEE = "see".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LINK = "link".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LINKPLAIN = "linkplain".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_INHERITDOC = "inheritDoc".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_VALUE = "value".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_AUTHOR = "author".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_CODE = "code".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_DOC_ROOT = "docRoot".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LITERAL = "literal".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SERIAL = "serial".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SERIAL_DATA = "serialData".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SERIAL_FIELD = "serialField".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SINCE = "since".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_VERSION = "version".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_CATEGORY = "category".toCharArray(); //$NON-NLS-1$

	// tags value
	public static final int NO_TAG_VALUE = 0;
	public static final int TAG_DEPRECATED_VALUE = 1;
	public static final int TAG_PARAM_VALUE = 2;
	public static final int TAG_RETURN_VALUE = 3;
	public static final int TAG_THROWS_VALUE = 4;
	public static final int TAG_EXCEPTION_VALUE = 5;
	public static final int TAG_SEE_VALUE = 6;
	public static final int TAG_LINK_VALUE = 7;
	public static final int TAG_LINKPLAIN_VALUE = 8;
	public static final int TAG_INHERITDOC_VALUE = 9;
	public static final int TAG_VALUE_VALUE = 10;
	public static final int TAG_CATEGORY_VALUE = 11;
	public static final int TAG_OTHERS_VALUE = 100;
	
	// tags expected positions
	public final static int ORDERED_TAGS_NUMBER = 3;
	public final static int PARAM_TAG_EXPECTED_ORDER = 0;
	public final static int THROWS_TAG_EXPECTED_ORDER = 1;
	public final static int SEE_TAG_EXPECTED_ORDER = 2;

	/*
	 * Tag kinds indexes
	 */
	public final static int BLOCK_IDX = 0;
	public final static int INLINE_IDX = 1;

	/*
	 * Tags versions
	 */
	public static final char[][][] BLOCK_TAGS = {
		// since 1.0
		{ TAG_AUTHOR, TAG_DEPRECATED, TAG_EXCEPTION, TAG_PARAM, TAG_RETURN, TAG_SEE, TAG_VERSION },
		// since 1.1
		{ TAG_SINCE },
		// since 1.2
		{ TAG_SERIAL, TAG_SERIAL_DATA, TAG_SERIAL_FIELD , TAG_THROWS },
		// since 1.3
		{},
		// since 1.4
		{},
		// since 1.5
		{},
		// since 1.6
		{ TAG_CATEGORY },
	};
	public static final char[][][] INLINE_TAGS = {
		// since 1.0
		{},
		// since 1.1
		{},
		// since 1.2
		{ TAG_LINK },
		// since 1.3
		{ TAG_DOC_ROOT },
		// since 1.4
		{ TAG_INHERITDOC, TAG_LINKPLAIN, TAG_VALUE },
		// since 1.5
		{ TAG_CODE, TAG_LITERAL },
	};
	public final static int INLINE_TAGS_LENGTH = INLINE_TAGS.length;
	public final static int BLOCK_TAGS_LENGTH = BLOCK_TAGS.length;
	public final static int ALL_TAGS_LENGTH = BLOCK_TAGS_LENGTH+INLINE_TAGS_LENGTH;

	/*
	 * Tags usage
	 */
	public static final char[][] PACKAGE_TAGS = {
		TAG_SEE,
		TAG_SINCE,
		TAG_SERIAL,
		TAG_AUTHOR,
		TAG_VERSION,
		TAG_CATEGORY,
		TAG_LINK,
		TAG_LINKPLAIN,
		TAG_DOC_ROOT,
	};
	public static final char[][] CLASS_TAGS = {
		TAG_SEE,
		TAG_SINCE,
		TAG_DEPRECATED,
		TAG_SERIAL,
		TAG_AUTHOR,
		TAG_VERSION,
		TAG_CATEGORY,
		TAG_LINK,
		TAG_LINKPLAIN,
		TAG_DOC_ROOT,
		TAG_CODE,
		TAG_LITERAL
	};
	public static final char[][] FIELD_TAGS = {
		TAG_SEE,
		TAG_SINCE,
		TAG_DEPRECATED,
		TAG_SERIAL,
		TAG_SERIAL_FIELD,
		TAG_CATEGORY,
		TAG_LINK,
		TAG_LINKPLAIN,
		TAG_DOC_ROOT,
		TAG_VALUE,
		TAG_CODE,
		TAG_LITERAL
	};
	public static final char[][] METHOD_TAGS = {
		TAG_SEE,
		TAG_SINCE,
		TAG_DEPRECATED,
		TAG_PARAM,
		TAG_RETURN,
		TAG_THROWS,
		TAG_EXCEPTION,
		TAG_SERIAL_DATA,
		TAG_CATEGORY,
		TAG_LINK,
		TAG_LINKPLAIN,
		TAG_INHERITDOC,
		TAG_DOC_ROOT,
		TAG_CODE,
		TAG_LITERAL
	};
}
