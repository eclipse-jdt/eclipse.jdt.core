package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.search.indexing.*;

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;

public class SuperInterfaceReferencePattern extends SuperTypeReferencePattern {
public SuperInterfaceReferencePattern(char[] superQualification, char[] superSimpleName, int matchMode, boolean isCaseSensitive) {
	super(superQualification, superSimpleName, matchMode, isCaseSensitive);
}
/**
 * @see SearchPattern#matches(Binding)
 */
public boolean matches(Binding binding) {
	if (!(binding instanceof ReferenceBinding)) return false;

	ReferenceBinding[] superInterfaces = ((ReferenceBinding)binding).superInterfaces();
	for (int i = 0, max = superInterfaces.length; i < max; i++){
		if (this.matchesType(this.superSimpleName, this.superQualification, superInterfaces[i])){
			return true;
		}
	}
	return false;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	return
		this.decodedSuperClassOrInterface == IIndexConstants.INTERFACE_SUFFIX
		&& super.matchIndexEntry();
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("SuperInterfaceReferencePattern: <");
	if (superSimpleName != null) buffer.append(superSimpleName);
	buffer.append(">, ");
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

/**
 * @see SearchPattern#matchesBinary
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;
	IBinaryType type = (IBinaryType)binaryInfo;

	char[][] superInterfaces = type.getInterfaceNames();
	if (superInterfaces != null) {
		for (int i = 0, max = superInterfaces.length; i < max; i++) {
			char[] superInterfaceName = (char[])superInterfaces[i].clone();
			CharOperation.replace(superInterfaceName, '/', '.');
			if (this.matchesType(this.superSimpleName, this.superQualification, superInterfaceName)){
				return true;
			}
		}
	}
	return false;
}
}
