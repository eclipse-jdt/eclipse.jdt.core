package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

import java.util.*;

public class ReferenceCollection {

char[][][] qualifiedReferences; // contains no simple names as in just 'a' which is kept in simpleNameReferences instead
char[][] simpleNameReferences;

public ReferenceCollection(char[][][] qualifiedReferences, char[][] simpleNameReferences) {
	this.qualifiedReferences = internQualifiedNames(qualifiedReferences);
	this.simpleNameReferences = internSimpleNames(simpleNameReferences, true);
}

boolean includes(char[] simpleName) {
	for (int i = 0, l = simpleNameReferences.length; i < l; i++)
		if (simpleName == simpleNameReferences[i]) return true;
	return false;
}

boolean includes(char[][] qualifiedName) {
	for (int i = 0, l = qualifiedReferences.length; i < l; i++)
		if (qualifiedName == qualifiedReferences[i]) return true;
	return false;
}

boolean includes(char[][][] qualifiedNames, char[][] simpleNames) {
	// if either collection of names is null, it means it contained a well known name so we know it already has a match
	if (simpleNames == null) {
		if (JavaBuilder.DEBUG) System.out.println("  found well known match"); //$NON-NLS-1$
		return true;
	} else if (qualifiedNames == null) {
		for (int i = 0, l = simpleNames.length; i < l; i++) {
			if (includes(simpleNames[i])) {
				if (JavaBuilder.DEBUG) System.out.println("  found match in well known package to " + new String(simpleNames[i])); //$NON-NLS-1$
				return true;
			}
		}
	} else {
		for (int i = 0, l = simpleNames.length; i < l; i++) {
			if (includes(simpleNames[i])) {
				for (int j = 0, m = qualifiedNames.length; j < m; j++) {
					char[][] qualifiedName = qualifiedNames[j];
					if (qualifiedName.length == 1 ? includes(qualifiedName[0]) : includes(qualifiedName)) {
						if (JavaBuilder.DEBUG)
							System.out.println("  found match in " + CharOperation.toString(qualifiedName) //$NON-NLS-1$
								+ " to " + new String(simpleNames[i])); //$NON-NLS-1$
						return true;
					}
				}
				return false;
			}
		}
	}
	return false;
}


// When any type is compiled, its methods are verified for certain problems
// the MethodVerifier requests 3 well known types which end up in the reference collection
// having WellKnownQualifiedNames & WellKnownSimpleNames, saves every type 40 bytes
// NOTE: These collections are sorted by length
static final char[][][] WellKnownQualifiedNames = new char[][][] {
	TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION,
	TypeConstants.JAVA_LANG_THROWABLE,
	TypeConstants.JAVA_LANG_OBJECT,
	TypeConstants.JAVA_LANG,
	new char[][] {TypeConstants.JAVA},
	new char[][] {new char[] {'o', 'r', 'g'}},
	new char[][] {new char[] {'c', 'o', 'm'}},
	TypeConstants.NoCharChar}; // default package
static final char[][] WellKnownSimpleNames = new char[][] {
	TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION[2],
	TypeConstants.JAVA_LANG_THROWABLE[2],
	TypeConstants.JAVA_LANG_OBJECT[2],
	TypeConstants.JAVA,
	TypeConstants.LANG,
	new char[] {'o', 'r', 'g'},
	new char[] {'c', 'o', 'm'}};

static final char[][][] EmptyQualifiedNames = new char[0][][];
static final char[][] EmptySimpleNames = new char[0][];

static ArrayList[] InternedQualifiedNames = new ArrayList[] { // each array contains qualified char[][]
	new ArrayList(37), // for qualified names of size 2
	new ArrayList(37), // for qualified names of size 3
	new ArrayList(37), // for qualified names of size 4
	new ArrayList(37), // for qualified names of size 5
	new ArrayList(37), // for qualified names of size 6
	new ArrayList(37), // for qualified names of size 7
	new ArrayList(37) // for all others
};
static ArrayList[] InternedSimpleNames = new ArrayList[] { // each array contains simple char[]
	new ArrayList(37), // for simple names up to 7 characters
	new ArrayList(37), // for simple names of size 8-11
	new ArrayList(37), // for simple names of size 12-15
	new ArrayList(37), // for simple names of size 16-19
	new ArrayList(37), // for simple names of size 20-24
	new ArrayList(37) // for all others
};

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

	char[][][] keepers = new char[length][][];
	int index = 0;
	next : for (int i = 0; i < length; i++) {
		char[][] qualifiedName = qualifiedNames[i];
		int qLength = qualifiedName.length;
		for (int j = 0, k = WellKnownQualifiedNames.length; j < k; j++) {
			char[][] wellKnownName = WellKnownQualifiedNames[j];
			if (qLength > wellKnownName.length)
				break; // all remaining well known names are shorter
			if (CharOperation.equals(qualifiedName, wellKnownName))
				continue next;
		}

		ArrayList internedNames =
			(qLength >= 2 && qLength <= 7)
				? InternedQualifiedNames[qLength - 2]
				: InternedQualifiedNames[6];
		for (int j = 0, k = internedNames.size(); j < k; j++) {
			char[][] internedName = (char[][]) internedNames.get(j);
			if (CharOperation.equals(qualifiedName, internedName)) {
				keepers[index++] = internedName;
				continue next;
			}
		}
		qualifiedName = internSimpleNames(qualifiedName, false);
		internedNames.add(qualifiedName);
		keepers[index++] = qualifiedName;
	}
	if (length > index) {
		if (length == 0) return EmptyQualifiedNames;
		System.arraycopy(keepers, 0, keepers = new char[index][][], 0, index);
	}
	return keepers;
}

static char[][] internSimpleNames(ArrayList simpleStrings) {
	if (simpleStrings == null) return EmptySimpleNames;
	int length = simpleStrings.size();
	if (length == 0) return EmptySimpleNames;

	char[][] result = new char[length][];
	for (int i = 0; i < length; i++)
		result[i] = ((String) simpleStrings.get(i)).toCharArray();
	return internSimpleNames(result, true);
}

static char[][] internSimpleNames(char[][] simpleNames, boolean removeWellKnown) {
	if (simpleNames == null) return EmptySimpleNames;
	int length = simpleNames.length;
	if (length == 0) return EmptySimpleNames;

	char[][] keepers = new char[length][];
	int index = 0;
	next : for (int i = 0; i < length; i++) {
		char[] name = simpleNames[i];
		int sLength = name.length;
		for (int j = 0, k = WellKnownSimpleNames.length; j < k; j++) {
			char[] wellKnownName = WellKnownSimpleNames[j];
			if (sLength > wellKnownName.length)
				break; // all remaining well known names are shorter
			if (CharOperation.equals(name, wellKnownName)) {
				if (!removeWellKnown)
					keepers[index++] = WellKnownSimpleNames[j];
				continue next;
			}
		}

		ArrayList internedNames = null;
		switch (sLength) {
			case 0 : case 1 : case 2 : case 3 : case 4 : case 5 : case 6 : case 7 :
				internedNames = InternedSimpleNames[0];
				break;
			case 8 : case 9 : case 10 : case 11 :
				internedNames = InternedSimpleNames[1];
				break;
			case 12 : case 13 : case 14 : case 15 :
				internedNames = InternedSimpleNames[2];
				break;
			case 16 : case 17 : case 18 : case 19 :
				internedNames = InternedSimpleNames[3];
				break;
			case 20 : case 21 : case 22 : case 23 : case 24 :
				internedNames = InternedSimpleNames[4];
				break;
			default :
				internedNames = InternedSimpleNames[5];
				break;
		}
		for (int j = 0, k = internedNames.size(); j < k; j++) {
			char[] internedName = (char[]) internedNames.get(j);
			if (CharOperation.equals(name, internedName)) {
				keepers[index++] = internedName;
				continue next;
			}
		}
		internedNames.add(name);
		keepers[index++] = name;
	}
	if (length > index) {
		if (index == 0) return EmptySimpleNames;
		System.arraycopy(keepers, 0, keepers = new char[index][], 0, index);
	}
	return keepers;
}
}