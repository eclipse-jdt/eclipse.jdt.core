package org.eclipse.jdt.internal.core.newbuilder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;

import org.eclipse.jdt.internal.core.JavaModelManager;

public class State {

IJavaProject javaProject;
ClasspathLocation[] classpathLocations;
String outputLocationString;
// keyed by location (the full filesystem path "d:/xyz/eclipse/Test/p1/p2/A.java"), value is a ReferenceCollection or an AdditionalTypeCollection
SimpleLookupTable references;

int buildNumber;
int lastStructuralBuildNumber;
SimpleLookupTable structuralBuildNumbers;

protected State(JavaBuilder javaBuilder) {
	this.javaProject = javaBuilder.javaProject;
	this.classpathLocations = javaBuilder.classpath;
	this.outputLocationString = javaBuilder.outputFolder.getLocation().toString();
	this.references = new SimpleLookupTable(13);

	this.buildNumber = 0; // indicates a full build
	this.lastStructuralBuildNumber = this.buildNumber;
	this.structuralBuildNumbers = new SimpleLookupTable(3);
}

void cleanup() {
	for (int i = 0, length = classpathLocations.length; i < length; i++)
		classpathLocations[i].clear();
}

void copyFrom(State lastState) {
	try {
		this.references = (SimpleLookupTable) lastState.references.clone();
		this.buildNumber = lastState.buildNumber + 1;
		this.lastStructuralBuildNumber = lastState.lastStructuralBuildNumber;
	} catch (CloneNotSupportedException e) {
		this.references = new SimpleLookupTable(31);

		Object[] keyTable = lastState.references.keyTable;
		Object[] valueTable = lastState.references.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				this.references.put(keyTable[i], valueTable[i]);
	}
}

char[][] getAdditionalTypeNamesFor(String location) {
	Object c = references.get(location);
	if (c instanceof AdditionalTypeCollection)
		return ((AdditionalTypeCollection) c).additionalTypeNames;
	return null;
}

boolean isStructurallyChanged(IProject prereqProject, State prereqState) {
	Object o = structuralBuildNumbers.get(prereqProject.getName());
	if (prereqState != null) {
		int previous = o == null ? 0 : ((Integer) o).intValue();
		if (previous == prereqState.lastStructuralBuildNumber) return false;
	}
	return true;
}

void hasStructuralChanges() {
	this.lastStructuralBuildNumber = this.buildNumber;
}

void record(String location, char[][][] qualifiedRefs, char[][] simpleRefs, char[][] typeNames) {
	references.put(location,
		(typeNames != null && typeNames.length > 0)
			? new AdditionalTypeCollection(typeNames, qualifiedRefs, simpleRefs)
			: new ReferenceCollection(qualifiedRefs, simpleRefs));
}

void recordLastStructuralChanges(IProject prereqProject, int prereqBuildNumber) {
	structuralBuildNumbers.put(prereqProject.getName(), new Integer(prereqBuildNumber));
}

void remove(IPath filePath) {
	references.removeKey(filePath.toString().toCharArray());
}

void removePackage(IResourceDelta sourceDelta) {
	IPath location = sourceDelta.getResource().getLocation();
	String extension = location.getFileExtension();
	if (extension == null) { // no extension indicates a folder
		IResourceDelta[] children = sourceDelta.getAffectedChildren();
		for (int i = 0, length = children.length; i < length; ++i)
			removePackage(children[i]);
	} else if (JavaBuilder.JAVA_EXTENSION.equalsIgnoreCase(extension)) {
		remove(location);
	}
}

/**
 * Returns a string representation of the receiver.
 */
public String toString() {
	return "State for " + javaProject.getElementName() //$NON-NLS-1$
		+ " (" + buildNumber //$NON-NLS-1$
			+ " : " + lastStructuralBuildNumber //$NON-NLS-1$
				+ ")"; //$NON-NLS-1$
}

/* Debug helper
void dump() {
	System.out.println("State for " + javaProject.getElementName() + " (" + buildNumber + " : " + lastStructuralBuildNumber + ")");
	System.out.println("\tClass path locations:");
	for (int i = 0, length = classpathLocations.length; i < length; ++i)
		System.out.println("\t\t" + classpathLocations[i]);
	System.out.println("\tOutput location:");
	System.out.println("\t\t" + outputLocationString);

	System.out.print("\tStructural build numbers table:");
	if (structuralBuildNumbers.size() == 0) {
		System.out.print(" <empty>");
	} else {
		Object[] keyTable = structuralBuildNumbers.keyTable;
		Object[] valueTable = structuralBuildNumbers.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				System.out.print("\n\t\t" + keyTable[i].toString() + " -> " + valueTable[i].toString());
	}

	System.out.print("\n\tReferences table:");
	if (references.size() == 0) {
		System.out.print(" <empty>");
	} else {
		Object[] keyTable = references.keyTable;
		Object[] valueTable = references.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				System.out.print("\n\t\t" + keyTable[i].toString());
				ReferenceCollection c = (ReferenceCollection) valueTable[i];
				char[][][] qRefs = c.qualifiedReferences;
				System.out.print("\n\t\t\tqualified:");
				if (qRefs.length == 0)
					System.out.print(" <empty>");
				else for (int j = 0, k = qRefs.length; j < k; j++)
						System.out.print("  '" + org.eclipse.jdt.internal.compiler.util.CharOperation.toString(qRefs[j]) + "'");
				char[][] sRefs = c.simpleNameReferences;
				System.out.print("\n\t\t\tsimple:");
				if (sRefs.length == 0)
					System.out.print(" <empty>");
				else for (int j = 0, k = sRefs.length; j < k; j++)
						System.out.print("  " + new String(sRefs[j]));
				if (c instanceof AdditionalTypeCollection) {
					char[][] names = ((AdditionalTypeCollection) c).additionalTypeNames;
					System.out.print("\n\t\t\tadditional type names:");
					for (int j = 0, k = names.length; j < k; j++)
						System.out.print("  " + new String(names[j]));
				}
			}
		}
	}
	System.out.print("\n\n");
}
*/
}