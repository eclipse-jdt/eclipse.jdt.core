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
	protected boolean needsInitialize;
	
public JavaWorkspaceScope() {
	JavaModelManager.getJavaModelManager().rememberScope(this);
}
public boolean encloses(IJavaElement element) {
	if (this.needsInitialize) {
		this.initialize();
	}
	return super.encloses(element);
}
public boolean encloses(String resourcePathString) {
	if (this.needsInitialize) {
		this.initialize();
	}
	return super.encloses(resourcePathString);
}
public IPath[] enclosingProjectsAndJars() {
	if (this.needsInitialize) {
		this.initialize();
	}
	return super.enclosingProjectsAndJars();
}
public void initialize() {
	super.initialize();
	JavaCore javaCore = JavaCore.getJavaCore();
	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	for (int i = 0, length = projects.length; i < length; i++) {
		IProject project = projects[i];
		if (project.isAccessible()) {
			try {
				this.add(javaCore.create(project), false, new HashSet(2));
			} catch (JavaModelException e) {
			}
		}
	}
	this.needsInitialize = false;
}
public void processDelta(IJavaElementDelta delta) {
	if (this.needsInitialize) return;
	IJavaElement element = delta.getElement();
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				IJavaElementDelta child = children[i];
				this.processDelta(child);
			}
			break;
		case IJavaElement.JAVA_PROJECT:
			int kind = delta.getKind();
			switch (kind) {
				case IJavaElementDelta.ADDED:
				case IJavaElementDelta.REMOVED:
					this.needsInitialize = true;
					break;
				case IJavaElementDelta.CHANGED:
					children = delta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++) {
						IJavaElementDelta child = children[i];
						this.processDelta(child);
					}
					break;
			}
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			kind = delta.getKind();
			switch (kind) {
				case IJavaElementDelta.ADDED:
				case IJavaElementDelta.REMOVED:
					this.needsInitialize = true;
					break;
				case IJavaElementDelta.CHANGED:
					int flags = delta.getFlags();
					if ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) > 0
						|| (flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0) {
						this.needsInitialize = true;
					}
					break;
			}
			break;
	}
}
}
