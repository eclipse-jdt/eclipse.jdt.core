package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

/**
 * The selector is unused, the constructor name is specified by the type simple name.
 */
public class ConstructorReferencePattern extends MethodReferencePattern {

	private char[] decodedTypeName;
	public ConstructorReferencePattern(
		char[] declaringSimpleName,
		int matchMode,
		boolean isCaseSensitive,
		char[] declaringQualification,
		char[][] parameterQualifications,
		char[][] parameterSimpleNames) {
		super(
			null,
			matchMode,
			isCaseSensitive,
			declaringQualification,
			declaringSimpleName,
			null,
			null,
			parameterQualifications,
			parameterSimpleNames);
	}

	public void decodeIndexEntry(IEntryResult entryResult) {

		char[] word = entryResult.getWord();
		int size = word.length;
		int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);

		decodedParameterCount =
			Integer.parseInt(
				new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
		decodedTypeName =
			CharOperation.subarray(word, CONSTRUCTOR_REF.length, lastSeparatorIndex);
	}

	/**
	 * see SearchPattern.feedIndexRequestor
	 */
	public void feedIndexRequestor(
		IIndexSearchRequestor requestor,
		int detailLevel,
		int[] references,
		IndexInput input,
		IJavaSearchScope scope)
		throws IOException {
		for (int i = 0, max = references.length; i < max; i++) {
			IndexedFile file = input.getIndexedFile(references[i]);
			String path;
			if (file != null
				&& scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
				requestor.acceptConstructorReference(
					path,
					decodedTypeName,
					decodedParameterCount);
			}
		}
	}

	/**
	 * @see SearchPattern#indexEntryPrefix
	 */
	public char[] indexEntryPrefix() {

		return AbstractIndexer.bestConstructorReferencePrefix(
			declaringSimpleName,
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length,
			matchMode,
			isCaseSensitive);
	}

	/**
	 * Returns whether this constructor pattern  matches the given allocation expression.
	 * Look at resolved information only if specified.
	 */
	private boolean matches(AllocationExpression allocation, boolean resolve) {

		// constructor name is simple type name
		char[][] typeName = allocation.type.getTypeName();
		if (this.declaringSimpleName != null
			&& !this.matchesName(this.declaringSimpleName, typeName[typeName.length - 1]))
			return false;

		// declaring type
		MethodBinding binding = allocation.binding;
		if (resolve && binding != null) {
			ReferenceBinding declaringBinding = binding.declaringClass;
			if (!this
				.matchesType(
					this.declaringSimpleName,
					this.declaringQualification,
					declaringBinding))
				return false;
		}

		// argument types
		int argumentCount =
			this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (argumentCount > -1) {
			int parameterCount =
				allocation.arguments == null ? 0 : allocation.arguments.length;
			if (parameterCount != argumentCount)
				return false;

			if (resolve && binding != null) {
				for (int i = 0; i < parameterCount; i++) {
					char[] qualification = this.parameterQualifications[i];
					char[] type = this.parameterSimpleNames[i];
					if (!this.matchesType(type, qualification, binding.parameters[i]))
						return false;
				}
			}
		}

		return true;
	}

	/**
	 * @see SearchPattern#matches(AstNode, boolean)
	 */
	protected boolean matches(AstNode node, boolean resolve) {
		if (node instanceof AllocationExpression) {
			return this.matches((AllocationExpression) node, resolve);
		} else
			if (node instanceof ExplicitConstructorCall) {
				return this.matches((ExplicitConstructorCall) node, resolve);
			}
		return false;
	}

	/**
	 * Returns whether this constructor pattern  matches the given explicit constructor call.
	 * Look at resolved information only if specified.
	 */
	private boolean matches(ExplicitConstructorCall call, boolean resolve) {
		// TBD: constructor name is super simple type name

		// declaring type
		MethodBinding binding = call.binding;
		if (resolve && binding != null) {
			ReferenceBinding declaringBinding = binding.declaringClass;
			if (!this
				.matchesType(
					this.declaringSimpleName,
					this.declaringQualification,
					declaringBinding))
				return false;
		}

		// argument types
		int argumentCount =
			this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (argumentCount > -1) {
			int parameterCount = call.arguments == null ? 0 : call.arguments.length;
			if (parameterCount != argumentCount)
				return false;

			if (resolve && binding != null) {
				for (int i = 0; i < parameterCount; i++) {
					char[] qualification = this.parameterQualifications[i];
					char[] type = this.parameterSimpleNames[i];
					if (!this.matchesType(type, qualification, binding.parameters[i]))
						return false;
				}
			}
		}

		return true;
	}

	/**
	 * @see SearchPattern#matchIndexEntry
	 */
	protected boolean matchIndexEntry() {

		/* check selector matches */
		if (declaringSimpleName != null) {
			switch (matchMode) {
				case EXACT_MATCH :
					if (!CharOperation
						.equals(declaringSimpleName, decodedTypeName, isCaseSensitive)) {
						return false;
					}
					break;
				case PREFIX_MATCH :
					if (!CharOperation
						.prefixEquals(declaringSimpleName, decodedTypeName, isCaseSensitive)) {
						return false;
					}
					break;
				case PATTERN_MATCH :
					if (!CharOperation
						.match(declaringSimpleName, decodedTypeName, isCaseSensitive)) {
						return false;
					}
			}
		}
		if (parameterSimpleNames != null) {
			if (parameterSimpleNames.length != decodedParameterCount)
				return false;
		}
		return true;
	}

	public String toString() {

		StringBuffer buffer = new StringBuffer(20);
		buffer.append("ConstructorReferencePattern: ");
		if (declaringQualification != null)
			buffer.append(declaringQualification).append('.');
		if (declaringSimpleName != null)
			buffer.append(declaringSimpleName);
		else
			if (declaringQualification != null)
				buffer.append("*");
		buffer.append('(');
		if (parameterSimpleNames == null) {
			buffer.append("...");
		} else {
			for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
				if (i > 0)
					buffer.append(", ");
				if (parameterQualifications[i] != null)
					buffer.append(parameterQualifications[i]).append('.');
				if (parameterSimpleNames[i] == null)
					buffer.append('*');
				else
					buffer.append(parameterSimpleNames[i]);
			}
		}
		buffer.append(')');
		buffer.append(", ");
		switch (matchMode) {
			case EXACT_MATCH :
				buffer.append("exact match, ");
				break;
			case PREFIX_MATCH :
				buffer.append("prefix match, ");
				break;
			case PATTERN_MATCH :
				buffer.append("pattern match, ");
				break;
		}
		if (isCaseSensitive)
			buffer.append("case sensitive");
		else
			buffer.append("case insensitive");
		return buffer.toString();
	}

}
