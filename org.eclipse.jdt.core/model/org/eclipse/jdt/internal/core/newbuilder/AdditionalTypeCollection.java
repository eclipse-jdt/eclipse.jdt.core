package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class AdditionalTypeCollection extends ReferenceCollection {

char[][] additionalTypeNames;

public AdditionalTypeCollection(char[][] additionalTypeNames, char[][][] qualifiedReferences, char[][] simpleNameReferences) {
	super(qualifiedReferences, simpleNameReferences);
	this.additionalTypeNames = additionalTypeNames; // do not bother interning member type names (ie. 'A$M')
}
}

