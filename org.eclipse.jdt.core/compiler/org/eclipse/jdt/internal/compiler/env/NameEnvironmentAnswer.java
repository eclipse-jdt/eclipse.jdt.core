package org.eclipse.jdt.internal.compiler.env;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class NameEnvironmentAnswer {
	
	// only one of the three can be set
	IBinaryType binaryType;
	ICompilationUnit compilationUnit;
	ISourceType[] sourceTypes;
	
	public NameEnvironmentAnswer(IBinaryType binaryType) {
		this.binaryType = binaryType;
	}

	public NameEnvironmentAnswer(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public NameEnvironmentAnswer(ISourceType[] sourceTypes) {
		this.sourceTypes = sourceTypes;
	}

	/**
	 * Answer the resolved binary form for the type or null if the
	 * receiver represents a compilation unit or source type.
	 */
	public IBinaryType getBinaryType() {
		return binaryType;
	}

	/**
	 * Answer the compilation unit or null if the
	 * receiver represents a binary or source type.
	 */
	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	/**
	 * Answer the unresolved source forms for the type or null if the
	 * receiver represents a compilation unit or binary type.
	 * 
	 * Multiple source forms can be answered in case the originating compilation unit did contain
	 * several type at once. Then the first type is guaranteed to be the requested type.
	 */
	public ISourceType[] getSourceTypes() {
		return sourceTypes;
	}

	/**
	 * Answer whether the receiver contains the resolved binary form of the type.
	 */
	public boolean isBinaryType() {
		return binaryType != null;
	}

	/**
	 * Answer whether the receiver contains the compilation unit which defines the type.
	 */
	public boolean isCompilationUnit() {
		return compilationUnit != null;
	}

	/**
	 * Answer whether the receiver contains the unresolved source form of the type.
	 */
	public boolean isSourceType() {
		return sourceTypes != null;
	}
}