package org.eclipse.jdt.internal.compiler.env;

public class NameEnvironmentAnswer {
	// only one of the three can be set
	IBinaryType binaryType;
	ICompilationUnit compilationUnit;
	ISourceType sourceType;
public NameEnvironmentAnswer(IBinaryType binaryType) {
	this.binaryType = binaryType;
}
public NameEnvironmentAnswer(ICompilationUnit compilationUnit) {
	this.compilationUnit = compilationUnit;
}
public NameEnvironmentAnswer(ISourceType sourceType) {
	this.sourceType = sourceType;
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
 * Answer the unresolved source form for the type or null if the
 * receiver represents a compilation unit or binary type.
 */

public ISourceType getSourceType() {
	return sourceType;
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

public  boolean isSourceType() {
	return sourceType != null;
}
}
