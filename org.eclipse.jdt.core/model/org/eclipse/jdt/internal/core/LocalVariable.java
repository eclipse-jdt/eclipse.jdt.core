/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;


public class LocalVariable extends JavaElement implements ILocalVariable, ISourceReference {

	public int declarationSourceStart, declarationEnd;
	public int nameStart, nameEnd;
	
	public LocalVariable(
			JavaElement parent, 
			String name, 
			int declarationSourceStart, 
			int declarationEnd,
			int nameStart, 
			int nameEnd) {
		
		super(parent, name);
		this.declarationSourceStart = declarationSourceStart;
		this.declarationEnd = declarationEnd;
		this.nameStart = nameStart;
		this.nameEnd = nameEnd;
	}

	protected void closing(Object info) throws JavaModelException {
		// a local variable has no info
	}

	protected Object createElementInfo() {
		// a local variable has no info
		return null;
	}

	public boolean equals(Object o) {
		if (!(o instanceof LocalVariable)) return false;
		LocalVariable other = (LocalVariable)o;
		return 
			this.declarationSourceStart == other.declarationSourceStart 
			&& this.declarationEnd == other.declarationEnd
			&& this.nameStart == other.nameStart
			&& this.nameEnd == other.nameEnd
			&& super.equals(o);
	}

	protected void generateInfos(Object info, HashMap newElements, IProgressMonitor pm) throws JavaModelException {
		// a local variable has no info
	}

	public IJavaElement getHandleFromMemento(String token, StringTokenizer memento, WorkingCopyOwner owner) {
		switch (token.charAt(0)) {
			case JEM_COUNT:
				return getHandleUpdatingCountFromMemento(memento, owner);
		}
		return this;
	}

	/*
	 * @see JavaElement#getHandleMemento()
	 */
	public String getHandleMemento(){
		StringBuffer buff= new StringBuffer(((JavaElement)getParent()).getHandleMemento());
		buff.append(getHandleMementoDelimiter());
		buff.append(this.name);
		buff.append(JEM_COUNT);
		buff.append(this.declarationSourceStart);
		buff.append(JEM_COUNT);
		buff.append(this.declarationEnd);
		buff.append(JEM_COUNT);
		buff.append(this.nameStart);
		buff.append(JEM_COUNT);
		buff.append(this.nameEnd);
		if (this.occurrenceCount > 1) {
			buff.append(JEM_COUNT);
			buff.append(this.occurrenceCount);
		}
		return buff.toString();
	}

	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_LOCALVARIABLE;
	}

	public IResource getCorrespondingResource() throws JavaModelException {
		return null;
	}

	public int getElementType() {
		return LOCAL_VARIABLE;
	}

	public ISourceRange getNameRange() {
		return new SourceRange(this.nameStart, this.nameEnd-this.nameStart+1);
	}
	
	public IPath getPath() {
		return this.parent.getPath();
	}

	public IResource getResource() {
		return this.parent.getResource();
	}

	/**
	 * @see ISourceReference
	 */
	public String getSource() throws JavaModelException {
		IOpenable openable = getOpenableParent();
		IBuffer buffer = openable.getBuffer();
		if (buffer == null) {
			return null;
		}
		ISourceRange range = getSourceRange();
		int offset = range.getOffset();
		int length = range.getLength();
		if (offset == -1 || length == 0 ) {
			return null;
		}
		try {
			return buffer.getText(offset, length);
		} catch(RuntimeException e) {
			return null;
		}
	}
	
	/**
	 * @see ISourceReference
	 */
	public ISourceRange getSourceRange() throws JavaModelException {
		return new SourceRange(this.declarationSourceStart, this.declarationEnd-this.declarationSourceStart+1);
	}

	public IResource getUnderlyingResource() throws JavaModelException {
		return this.parent.getUnderlyingResource();
	}

	public int hashCode() {
		return Util.combineHashCodes(this.parent.hashCode(), this.nameStart);
	}
}
