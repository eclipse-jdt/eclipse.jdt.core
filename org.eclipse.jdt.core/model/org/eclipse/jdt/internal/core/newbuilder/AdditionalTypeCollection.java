package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class AdditionalTypeCollection extends ReferenceCollection {

char[][] definedTypeNames;

protected AdditionalTypeCollection(char[][] definedTypeNames, char[][][] qualifiedReferences, char[][] simpleNameReferences) {
	super(qualifiedReferences, simpleNameReferences);
	this.definedTypeNames = definedTypeNames; // do not bother interning member type names (ie. 'A$M')
}
}

