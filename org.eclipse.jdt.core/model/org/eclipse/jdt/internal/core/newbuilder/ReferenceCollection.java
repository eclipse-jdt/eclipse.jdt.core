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
	this.qualifiedReferences = internQualifiedNames(qualifiedReferences, true);
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
	}
	for (int i = 0, l = simpleNames.length; i < l; i++) {
		if (includes(simpleNames[i])) {
			if (qualifiedNames == null) {
				if (JavaBuilder.DEBUG) System.out.println("  found match in well known package to " + new String(simpleNames[i])); //$NON-NLS-1$
				return true;
			}
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
	return false;
}


// When any type is compiled, its methods are verified for certain problems
// the MethodVerifier requests 3 well known types which end up in the reference collection
// having WellKnownQualifiedNames & WellKnownSimpleNames, saves every type 40 bytes
static final char[][][] WellKnownQualifiedNames = new char[][][] {
	TypeConstants.NoCharChar, // default package
	new char[][] {TypeConstants.JAVA},
	TypeConstants.JAVA_LANG,
	TypeConstants.JAVA_LANG_OBJECT,
	TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION,
	TypeConstants.JAVA_LANG_THROWABLE};
static final char[][] WellKnownSimpleNames = new char[][] {
	TypeConstants.JAVA,
	TypeConstants.LANG,
	TypeConstants.JAVA_LANG_OBJECT[2],
	TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION[2],
	TypeConstants.JAVA_LANG_THROWABLE[2]};

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
	return internQualifiedNames(result, true);
}

static char[][][] internQualifiedNames(char[][][] qualifiedNames, boolean removeWellKnown) {
	if (qualifiedNames == null) return EmptyQualifiedNames;
	int length = qualifiedNames.length;
	if (length == 0) return EmptyQualifiedNames;

	ArrayList keepers = new ArrayList(length);
	next : for (int i = 0; i < length; i++) {
		char[][] qualifiedName = qualifiedNames[i];
		for (int j = 0, k = InternedQualifiedNames.size(); j < k; j++) {
			char[][] internedName = (char[][]) InternedQualifiedNames.get(j);
			if (CharOperation.equals(qualifiedName, internedName)) {
				keepers.add(internedName);
				continue next;
			}
		}
		for (int j = 0, k = WellKnownQualifiedNames.length; j < k; j++) {
			if (CharOperation.equals(qualifiedName, WellKnownQualifiedNames[j])) {
				if (!removeWellKnown)
					keepers.add(WellKnownQualifiedNames[j]);
				continue next;
			}
		}
		qualifiedName = internSimpleNames(qualifiedName, false);
		InternedQualifiedNames.add(qualifiedName);
		keepers.add(qualifiedName);
	}
	char[][][] result = new char[keepers.size()][][];
	keepers.toArray(result);
	return result;
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

	ArrayList keepers = new ArrayList(length);
	next : for (int i = 0; i < length; i++) {
		char[] name = simpleNames[i];
		for (int j = 0, k = InternedSimpleNames.size(); j < k; j++) {
			char[] internedName = (char[]) InternedSimpleNames.get(j);
			if (CharOperation.equals(name, internedName)) {
				keepers.add(internedName);
				continue next;
			}
		}
		for (int j = 0, k = WellKnownSimpleNames.length; j < k; j++) {
			if (CharOperation.equals(name, WellKnownSimpleNames[j])) {
				if (!removeWellKnown)
					keepers.add(WellKnownSimpleNames[j]);
				continue next;
			}
		}
		InternedSimpleNames.add(name);
		keepers.add(name);
	}
	char[][] result = new char[keepers.size()][];
	keepers.toArray(result);
	return result;
}
}