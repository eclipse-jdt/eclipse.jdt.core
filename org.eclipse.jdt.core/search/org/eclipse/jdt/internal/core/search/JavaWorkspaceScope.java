package org.eclipse.jdt.internal.core.search;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.*;

import java.util.*;

/**
 * A Java-specific scope for searching the entire workspace.
 * The scope can be configured to not search binaries. By default, binaries
 * are included.
 */
public class JavaWorkspaceScope extends JavaSearchScope {
public JavaWorkspaceScope() {
	JavaCore javaCore = JavaCore.getJavaCore();
	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	for (int i = 0, length = projects.length; i < length; i++) {
		IProject project = projects[i];
		if (project.isAccessible()) {
			try {
				this.add(javaCore.create(project), false);
			} catch (JavaModelException e) {
			}
		}
	}
}
}
