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

	char[][][] keepers = new char[length][][];
	int index = 0;
	next : for (int i = 0; i < length; i++) {
		char[][] qualifiedName = qualifiedNames[i];
		int qLength = qualifiedName.length;
		for (int j = 0, k = WellKnownQualifiedNames.length; j < k; j++) {
			char[][] wellKnownName = WellKnownQualifiedNames[j];
			if (qLength > wellKnownName.length)
				break; // all remaining well known names are shorter
			if (qLength == wellKnownName.length && CharOperation.equals(qualifiedName, wellKnownName))
				continue next;
		}
		for (int j = 0, k = InternedQualifiedNames.size(); j < k; j++) {
			char[][] internedName = (char[][]) InternedQualifiedNames.get(j);
			if (qLength == internedName.length && CharOperation.equals(qualifiedName, internedName)) {
				keepers[index++] = internedName;
				continue next;
			}
		}
		qualifiedName = internSimpleNames(qualifiedName, false);
		InternedQualifiedNames.add(qualifiedName);
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
			if (sLength == wellKnownName.length && CharOperation.equals(name, wellKnownName)) {
				if (!removeWellKnown)
					keepers[index++] = WellKnownSimpleNames[j];
				continue next;
			}
		}
		for (int j = 0, k = InternedSimpleNames.size(); j < k; j++) {
			char[] internedName = (char[]) InternedSimpleNames.get(j);
			if (sLength == internedName.length && CharOperation.equals(name, internedName)) {
				keepers[index++] = internedName;
				continue next;
			}
		}
		InternedSimpleNames.add(name);
		keepers[index++] = name;
	}
	if (length > index) {
		if (index == 0) return EmptySimpleNames;
		System.arraycopy(keepers, 0, keepers = new char[index][], 0, index);
	}
	return keepers;
}
}