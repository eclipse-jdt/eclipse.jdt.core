package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.*;

public abstract class MethodPattern extends SearchPattern {

	// selector	
	protected char[] selector;
	
	// declaring type
	protected char[] declaringQualification;
	protected char[] declaringSimpleName;

	// return type
	protected char[] returnQualification;
	protected char[] returnSimpleName;

	// parameter types
	protected char[][] parameterQualifications;
	protected char[][] parameterSimpleNames;

	protected char[] decodedSelector;
	protected int decodedParameterCount;	
public MethodPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
public abstract String getPatternName();
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check selector matches */
	if (selector != null){
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(selector, decodedSelector, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(selector, decodedSelector, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(selector, decodedSelector, isCaseSensitive)){
					return false;
				}
		}
	}
	if (parameterSimpleNames != null){
		if (parameterSimpleNames.length != decodedParameterCount) return false;
	}
	return true;
}
/**
 * Returns whether a method declaration or message send will need to be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean needsResolve() {

	// declaring type
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	if (returnSimpleName != null || returnQualification != null) return true;

	// parameter types
	if (parameterSimpleNames != null){
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			if (parameterQualifications[i] != null || parameterSimpleNames[i] != null) return true;
		}
	}
	return false;
}
public String toString(){

	StringBuffer buffer = new StringBuffer(20);
	buffer.append(this.getPatternName());
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*.");
	if (selector != null) {
		buffer.append(selector);
	} else {
		buffer.append("*");
	}
	buffer.append('(');
	if (parameterSimpleNames == null) {
		buffer.append("...");
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			if (i > 0) buffer.append(", ");
			if (parameterQualifications[i] != null) buffer.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) buffer.append('*'); else buffer.append(parameterSimpleNames[i]);
		}
	}
	buffer.append(')');
	if (returnQualification != null) 
		buffer.append(" --> ").append(returnQualification).append('.');
	else if (returnSimpleName != null) buffer.append(" --> ");
	if (returnSimpleName != null) 
		buffer.append(returnSimpleName);
	else if (returnQualification != null) buffer.append("*");
	buffer.append(", ");
	switch(matchMode){
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
