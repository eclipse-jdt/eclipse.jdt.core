package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import java.util.ArrayList;

public class ReferenceCollection {

char[][][] qualifiedReferences;
char[][] simpleNameReferences;

public ReferenceCollection(char[][][] qualifiedReferences, char[][] simpleNameReferences) {
	this.qualifiedReferences = internQualifiedNames(qualifiedReferences);
	this.simpleNameReferences = internSimpleNames(simpleNameReferences);
}

boolean includes(char[][][] qualifiedNames, char[][] simpleNames) {
	for (int i = 0, a = simpleNames.length; i < a; i++) {
		char[] simpleName = simpleNames[i];
		for (int j = 0, b = simpleNameReferences.length; j < b; j++) {
			if (simpleName == simpleNameReferences[j]) {
				for (int k = 0, c = qualifiedNames.length; k < c; k++) {
					char[][] qualifiedName = qualifiedNames[k];
					for (int l = 0, d = qualifiedReferences.length; l < d; l++) {
						if (qualifiedName == qualifiedReferences[l]) {
							if (JavaBuilder.DEBUG)
								System.out.println("  found match with " //$NON-NLS-1$
									+ new String(simpleName) + " in " + CharOperation.toString(qualifiedName)); //$NON-NLS-1$
							return true;
						}
					}
				}
				return false;
			}
		}
	}
	return false;
}


static char[][][] EmptyQualifiedNames = new char[0][][];
static char[][] EmptySimpleNames = new char[0][];
static ArrayList InternedQualifiedNames = new ArrayList(333); // contains qualified char[][]
static ArrayList InternedSimpleNames = new ArrayList(111); // contains simple char[]

static char[][][] internQualifiedNames(ArrayList qualifiedStrings) {
	if (qualifiedStrings == null) return EmptyQualifiedNames;
	int length = qualifiedStrings.size();
	if (length == 0) return EmptyQualifiedNames;

	char[][][] result = new char[length][][];
	for (int i = 0; i < length; i++)
		result[i] = CharOperation.splitOn('/', ((String) qualifiedStrings.get(i)).toCharArray());
	return internQualifiedNames(result);
}

static char[][][] internQualifiedNames(char[][][] qualifiedNames) {
	if (qualifiedNames == null) return EmptyQualifiedNames;
	int length = qualifiedNames.length;
	if (length == 0) return EmptyQualifiedNames;

	next : for (int i = 0; i < length; i++) {
		char[][] qualifiedName = qualifiedNames[i];
		for (int j = 0, k = InternedQualifiedNames.size(); j < k; j++) {
			char[][] internedName = (char[][]) InternedQualifiedNames.get(j);
			if (CharOperation.equals(qualifiedName, internedName)) {
				qualifiedNames[i] = internedName;
				continue next;
			}
		}
		InternedQualifiedNames.add(internSimpleNames(qualifiedName));
	}
	return qualifiedNames;
}

static char[][] internSimpleNames(ArrayList simpleStrings) {
	if (simpleStrings == null) return EmptySimpleNames;
	int length = simpleStrings.size();
	if (length == 0) return EmptySimpleNames;

	char[][] result = new char[length][];
	for (int i = 0; i < length; i++)
		result[i] = ((String) simpleStrings.get(i)).toCharArray();
	return internSimpleNames(result);
}

static char[][] internSimpleNames(char[][] simpleNames) {
	if (simpleNames == null) return EmptySimpleNames;
	int length = simpleNames.length;
	if (length == 0) return EmptySimpleNames;

	next : for (int i = 0; i < length; i++) {
		char[] name = simpleNames[i];
		for (int j = 0, k = InternedSimpleNames.size(); j < k; j++) {
			char[] internedName = (char[]) InternedSimpleNames.get(j);
			if (CharOperation.equals(name, internedName)) {
				simpleNames[i] = internedName;
				continue next;
			}
		}
		InternedSimpleNames.add(name);
	}
	return simpleNames;
}
}