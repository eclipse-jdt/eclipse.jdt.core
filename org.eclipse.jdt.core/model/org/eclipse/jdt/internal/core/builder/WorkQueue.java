/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import java.util.*;

public class WorkQueue {

private HashSet needsCompileList;
private HashSet compiledList;

public WorkQueue() {
	this.needsCompileList = new HashSet(11);
	this.compiledList = new HashSet(11);
}

public void add(SourceFile element) {
	needsCompileList.add(element);
}

public void addAll(SourceFile[] elements) {
	for (int i = 0, l = elements.length; i < l; i++)
		add(elements[i]);
}

public void clear() {
	this.needsCompileList.clear();
	this.compiledList.clear();
}	

public void finished(SourceFile element) {
	needsCompileList.remove(element);
	compiledList.add(element);
}

public boolean isCompiled(SourceFile element) {
	return compiledList.contains(element);
}

public boolean isWaiting(SourceFile element) {
	return needsCompileList.contains(element);
}

public String toString() {
	return "WorkQueue: " + needsCompileList; //$NON-NLS-1$
}
}
