package org.eclipse.jdt.core.tests.compiler.parser;

public class SourcePackage {
	int declarationSourceStart;
	int declarationSourceEnd;
	char[] name;
	char[] source;
/**
 * @param declarationSourceStart int
 * @param declarationSourceEnd int
 * @param name char[]
 * @param onDemand boolean
 */
public SourcePackage(
	int declarationSourceStart, 
	int declarationSourceEnd, 
	char[] name,
	char[] source) {

	this.declarationSourceStart = declarationSourceStart;
	this.declarationSourceEnd = declarationSourceEnd; 
	this.name = name;
	this.source = source;
}
/**
 * 
 * @return java.lang.String
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer
		.append(
			source, 
			declarationSourceStart, 
			declarationSourceEnd - declarationSourceStart + 1)
		.append("\n"); 
	return buffer.toString();
}
}
