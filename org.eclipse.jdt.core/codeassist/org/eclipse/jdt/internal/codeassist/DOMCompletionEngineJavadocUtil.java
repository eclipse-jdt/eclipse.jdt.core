/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;

class DOMCompletionEngineJavadocUtil {

	// block tags

	private static final List<char[]> JAVA_8_BLOCK_TAGS;
	static {
		JAVA_8_BLOCK_TAGS = new ArrayList<>();
		for (int i = 0 ; i < 4 ; i++) {
			for (char[] entry : JavadocTagConstants.BLOCK_TAGS_RAW[i].tags()) {
				JAVA_8_BLOCK_TAGS.add(entry);
			}
		}
	}
	private static final List<char[]> JAVA_9_BLOCK_TAGS;
	static {
		JAVA_9_BLOCK_TAGS = new ArrayList<>();
		JAVA_9_BLOCK_TAGS.addAll(JAVA_8_BLOCK_TAGS);
		for (char[] entry : JavadocTagConstants.BLOCK_TAGS_RAW[4].tags()) {
			JAVA_9_BLOCK_TAGS.add(entry);
		}
	}

	// inline tags

	private static final List<char[]> JAVA_8_INLINE_TAGS;
	static {
		JAVA_8_INLINE_TAGS = new ArrayList<>();
		for (int i = 0 ; i < 4 ; i++) {
			for (char[] entry : JavadocTagConstants.INLINE_TAGS_RAW[i].tags()) {
				JAVA_8_INLINE_TAGS.add(entry);
			}
		}
	}

	private static final List<char[]> JAVA_9_INLINE_TAGS;
	static {
		JAVA_9_INLINE_TAGS = new ArrayList<>();
		JAVA_9_INLINE_TAGS.addAll(JAVA_8_INLINE_TAGS);
		for (char[] entry : JavadocTagConstants.INLINE_TAGS_RAW[4].tags()) {
			JAVA_9_INLINE_TAGS.add(entry);
		}
	}

	private static final List<char[]> JAVA_10_INLINE_TAGS;
	static {
		JAVA_10_INLINE_TAGS = new ArrayList<>();
		JAVA_10_INLINE_TAGS.addAll(JAVA_9_INLINE_TAGS);
		for (char[] entry : JavadocTagConstants.INLINE_TAGS_RAW[5].tags()) {
			JAVA_10_INLINE_TAGS.add(entry);
		}
	}

	private static final List<char[]> JAVA_12_INLINE_TAGS;
	static {
		JAVA_12_INLINE_TAGS = new ArrayList<>();
		JAVA_12_INLINE_TAGS.addAll(JAVA_10_INLINE_TAGS);
		for (char[] entry : JavadocTagConstants.INLINE_TAGS_RAW[6].tags()) {
			JAVA_12_INLINE_TAGS.add(entry);
		}
	}

	private static final List<char[]> JAVA_16_INLINE_TAGS;
	static {
		JAVA_16_INLINE_TAGS = new ArrayList<>();
		JAVA_16_INLINE_TAGS.addAll(JAVA_12_INLINE_TAGS);
		for (char[] entry : JavadocTagConstants.INLINE_TAGS_RAW[7].tags()) {
			JAVA_16_INLINE_TAGS.add(entry);
		}
	}

	private static final List<char[]> JAVA_18_INLINE_TAGS;
	static {
		JAVA_18_INLINE_TAGS = new ArrayList<>();
		JAVA_18_INLINE_TAGS.addAll(JAVA_16_INLINE_TAGS);
		for (char[] entry : JavadocTagConstants.INLINE_TAGS_RAW[8].tags()) {
			JAVA_18_INLINE_TAGS.add(entry);
		}
	}

	public static List<char[]> getJavadocBlockTags(IJavaProject project, TagElement tagNode) {
		List<char[]> tagsForVersion;
		String projectVersion = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if (projectVersion.contains(".") || Integer.parseInt(projectVersion) < 9) { //$NON-NLS-1$
			tagsForVersion = JAVA_8_BLOCK_TAGS;
		} else {
			tagsForVersion = JAVA_9_BLOCK_TAGS;
		}

		return tagsForNode(tagsForVersion, tagNode);
	}

	public static List<char[]> getJavadocInlineTags(IJavaProject project, TagElement tagNode) {
		List<char[]> tagsForVersion;
		String projectVersion = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if (projectVersion.contains(".")) { //$NON-NLS-1$
			tagsForVersion = JAVA_8_INLINE_TAGS;
		} else {
			int versionNumber = Integer.parseInt(projectVersion);
			if (versionNumber < 9) {
				tagsForVersion = JAVA_8_INLINE_TAGS;
			} else if (versionNumber < 10) {
				tagsForVersion = JAVA_9_INLINE_TAGS;
			} else if (versionNumber < 12) {
				tagsForVersion = JAVA_10_INLINE_TAGS;
			} else if (versionNumber < 16) {
				tagsForVersion = JAVA_12_INLINE_TAGS;
			} else if (versionNumber < 18) {
				tagsForVersion = JAVA_16_INLINE_TAGS;
			} else {
				tagsForVersion = JAVA_18_INLINE_TAGS;
			}
		}
		return tagsForNode(tagsForVersion, tagNode);
	}

	private static List<char[]> tagsForNode(List<char[]> tagsForVersion, TagElement tagNode) {
		boolean isField = DOMCompletionUtil.findParent(tagNode, new int[]{ ASTNode.FIELD_DECLARATION }) != null;
		if (isField) {
			return Stream.of(JavadocTagConstants.FIELD_TAGS) //
					.filter(tag -> tagsForVersion.contains(tag)) //
					.toList();
		}

		ASTNode astNode = DOMCompletionUtil.findParent(tagNode, new int[] {
				ASTNode.METHOD_DECLARATION,
				ASTNode.TYPE_DECLARATION,
				ASTNode.ENUM_DECLARATION,
				ASTNode.RECORD_DECLARATION,
				ASTNode.ANNOTATION_TYPE_DECLARATION,
				ASTNode.TYPE_DECLARATION_STATEMENT});

		boolean isMethod = astNode != null && astNode.getNodeType() == ASTNode.METHOD_DECLARATION;
		if (isMethod) {
			return Stream.of(JavadocTagConstants.METHOD_TAGS) //
					.filter(tag -> tagsForVersion.contains(tag)) //
					.toList();
		}

		boolean isType = astNode != null;
		if (isType) {
			return Stream.of(JavadocTagConstants.CLASS_TAGS) //
					.filter(tag -> tagsForVersion.contains(tag)) //
					.toList();
		}

		boolean isPackage = DOMCompletionUtil.findParent(tagNode, new int[] {ASTNode.PACKAGE_DECLARATION}) != null;
		if (isPackage) {
			return Stream.of(JavadocTagConstants.PACKAGE_TAGS) //
					.filter(tag -> tagsForVersion.contains(tag)) //
					.toList();
		}

		throw new IllegalStateException("I was expecting one of the above nodes to be documented"); //$NON-NLS-1$
	}

}
