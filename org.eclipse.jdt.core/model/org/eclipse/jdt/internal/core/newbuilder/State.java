package org.eclipse.jdt.internal.core.newbuilder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class State {

IJavaProject javaProject;

ClasspathLocation[] classpathLocations;

String outputLocationString;

// keyed by fileId (the full filesystem path "d:/xyz/eclipse/Test/p1/p2/A.java"), value is a ReferenceCollection or an AdditionalTypeCollection
HashtableOfObject references;

private int stateNumber;
private static int stateCounter= 0;

protected State(JavaBuilder javaBuilder) {
	this.javaProject = javaBuilder.javaProject;
	this.classpathLocations = javaBuilder.classpath;
	this.outputLocationString = javaBuilder.outputFolder.getLocation().toString();

	this.stateNumber = stateCounter++;
	this.references = new HashtableOfObject(31);
}

void cleanup() {
	for (int i = 0, length = classpathLocations.length; i < length; i++)
		classpathLocations[i].clear();
}

void copyFrom(State lastState) {
	try {
		this.references = (HashtableOfObject) lastState.references.clone();
	} catch (CloneNotSupportedException e) {
		this.references = new HashtableOfObject(31);

		char[][] keyTable = lastState.references.keyTable;
		Object[] valueTable = lastState.references.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++)
			if (keyTable[i] != null)
				this.references.put(keyTable[i], valueTable[i]);
	}
}

char[][] getAdditionalTypeNamesFor(char[] fileId) {
	Object c = references.get(fileId);
	if (c instanceof AdditionalTypeCollection)
		return ((AdditionalTypeCollection) c).additionalTypeNames;
	return null;
}

void record(char[] fileId, char[][][] qualifiedRefs, char[][] simpleRefs, char[][] typeNames) {
	references.put(fileId,
		(typeNames != null && typeNames.length > 0)
			? new AdditionalTypeCollection(typeNames, qualifiedRefs, simpleRefs)
			: new ReferenceCollection(qualifiedRefs, simpleRefs));
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
	return "State(" //$NON-NLS-1$
		+ stateNumber + ")"; //$NON-NLS-1$
}

/* Debug helper
void dump() {
	System.out.println("State for " + javaProject.getElementName() + " (" + stateNumber + ")");
	System.out.println("\tClass path locations:");
	for (int i = 0, length = classpathLocations.length; i < length; ++i)
		System.out.println("\t\t" + classpathLocations[i]);
	System.out.println("\tOutput location:");
	System.out.println("\t\t" + outputLocationString);

	System.out.print("\tReferences table:");
	if (references.size() == 0) {
		System.out.print(" <empty>");
	} else {
		char[][] keyTable = references.keyTable;
		Object[] valueTable = references.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			if (keyTable[i] != null) {
				System.out.print("\n\t\t" + new String(keyTable[i]));
				ReferenceCollection c = (ReferenceCollection) valueTable[i];
				char[][][] qRefs = c.qualifiedReferences;
				System.out.print("\n\t\t\tqualified:");
				if (qRefs.length == 0)
					System.out.print(" <empty>");
				else for (int j = 0, k = qRefs.length; j < k; j++)
						System.out.print("  '" + CharOperation.toString(qRefs[j]) + "'");
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