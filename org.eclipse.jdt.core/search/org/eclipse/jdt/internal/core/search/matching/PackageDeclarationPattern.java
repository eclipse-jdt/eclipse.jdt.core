package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.index.IEntryResult;

public class PackageDeclarationPattern extends SearchPattern {
	char[] pkgName;
	public PackageDeclarationPattern(
		char[] pkgName,
		int matchMode,
		boolean isCaseSensitive) {
		super(matchMode, isCaseSensitive);
		this.pkgName = pkgName;
	}

	/**
	 * @see SearchPattern#decodeIndexEntry
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		// not used
	}

	/**
	 * @see SearchPattern#feedIndexRequestor
	 */
	public void feedIndexRequestor(
		IIndexSearchRequestor requestor,
		int detailLevel,
		int[] references,
		IndexInput input,
		IJavaSearchScope scope)
		throws java.io.IOException {
		// not used
	}

	/**
	 * see SearchPattern#findMatches
	 */
	public void findIndexMatches(
		IndexInput input,
		IIndexSearchRequestor requestor,
		int detailLevel,
		IProgressMonitor progressMonitor,
		IJavaSearchScope scope)
		throws IOException {
		// package declarations are not indexed
	}

	/**
	 * @see SearchPattern#indexEntryPrefix
	 */
	public char[] indexEntryPrefix() {
		// not used
		return null;
	}

	/**
	 * @see SearchPattern#matchContainer
	 */
	protected int matchContainer() {
		// used only in the case of a OrPattern
		return 0;
	}

	/**
	 * @see SearchPattern#matches(AstNode, boolean)
	 */
	protected boolean matches(AstNode node, boolean resolve) {
		// used only in the case of a OrPattern
		return true;
	}

	/**
	 * @see SearchPattern#matchIndexEntry
	 */
	protected boolean matchIndexEntry() {
		// used only in the case of a OrPattern
		return true;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("PackageDeclarationPattern: <");
		if (this.pkgName != null)
			buffer.append(this.pkgName);
		buffer.append(">, ");
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
