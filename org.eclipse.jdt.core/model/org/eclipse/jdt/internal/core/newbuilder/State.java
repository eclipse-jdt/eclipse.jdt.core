package org.eclipse.jdt.internal.core.newbuilder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.*;

import java.util.*;

public class State {

IJavaProject javaProject;

ClasspathLocation[] classpathLocations;

String outputLocationString;

// keyed by filename "p1/p2/X", value is an array of additional type names "p1/p2/Y"
HashtableOfObject additionalTypeNames;

// keyed by filename "p1/p2/X", value is a ReferenceCollection
HashtableOfObject references;

private int stateNumber;
private static int stateCounter= 0;

protected State(JavaBuilder javaBuilder) {
	this.javaProject = javaBuilder.javaProject;
	this.classpathLocations = javaBuilder.classpath;
	this.outputLocationString = javaBuilder.outputFolder.getLocation().toString();

	this.stateNumber = stateCounter++;
	this.additionalTypeNames = new HashtableOfObject(11);
	this.references = new HashtableOfObject(31);
}

void cleanup() {
	for (int i = 0, length = classpathLocations.length; i < length; i++)
		classpathLocations[i].clear();
}

void copyFrom(State lastState) {
	try {
		this.additionalTypeNames = (HashtableOfObject) lastState.additionalTypeNames.clone();
		this.references = (HashtableOfObject) lastState.references.clone();
	} catch (CloneNotSupportedException e) {
		this.additionalTypeNames = lastState.additionalTypeNames;
		this.references = lastState.references;
	}
}

public void recordDependencies(char[] fileId, char[][][] qualifiedRefs, char[][] simpleRefs) {
	references.put(fileId, new ReferenceCollection(qualifiedRefs, simpleRefs));
}

public void rememberAdditionalTypes(char[] fileId, char[][] typeNames) {
	if (typeNames != null && typeNames.length > 0)
		additionalTypeNames.put(fileId, typeNames);
}

/**
 * Returns a string representation of the receiver.
 */
public String toString() {
	return "State(" //$NON-NLS-1$
		+ stateNumber + ")"; //$NON-NLS-1$
}
}