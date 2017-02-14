/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.internal.compiler.lookup.ModuleEnvironment;

public class NameEnvironmentAnswer {

	// only one of the three can be set
	IBinaryType binaryType;
	ICompilationUnit compilationUnit;
	ISourceType[] sourceTypes;
	AccessRestriction accessRestriction;
	char[] module;
	String externalAnnotationPath; // should be an absolute file system path

	public NameEnvironmentAnswer(IBinaryType binaryType, AccessRestriction accessRestriction) {
		this.binaryType = binaryType;
		this.accessRestriction = accessRestriction;
		this.module = binaryType.getModule();
	}

	public NameEnvironmentAnswer(IBinaryType binaryType, AccessRestriction accessRestriction, char[] module) {
		this.binaryType = binaryType;
		this.accessRestriction = accessRestriction;
		this.module = module;
	}
	public NameEnvironmentAnswer(ICompilationUnit compilationUnit, AccessRestriction accessRestriction) {
		this(compilationUnit, accessRestriction, compilationUnit.module());
	}
	public NameEnvironmentAnswer(ICompilationUnit compilationUnit, AccessRestriction accessRestriction, char[] module) {
		this.compilationUnit = compilationUnit;
		this.accessRestriction = accessRestriction;
		this.module = module;
	}

	public NameEnvironmentAnswer(ISourceType[] sourceTypes, AccessRestriction accessRestriction, String externalAnnotationPath) {
		this(sourceTypes, accessRestriction, externalAnnotationPath, ModuleEnvironment.UNNAMED);
	}

	public NameEnvironmentAnswer(ISourceType[] sourceTypes, AccessRestriction accessRestriction, String externalAnnotationPath, char[] module) {
		this.sourceTypes = sourceTypes;
		this.accessRestriction = accessRestriction;
		this.externalAnnotationPath = externalAnnotationPath;
		this.module = module;
	}
	
	@Override
	public String toString() {
		String baseString = ""; //$NON-NLS-1$
		if (this.binaryType != null) {
			char[] fileNameChars = this.binaryType.getFileName();
			String fileName = fileNameChars == null ? "" : new String(fileNameChars); //$NON-NLS-1$
			baseString = "IBinaryType " + fileName; //$NON-NLS-1$
		}
		if (this.compilationUnit != null) {
			baseString = "ICompilationUnit " + this.compilationUnit.toString(); //$NON-NLS-1$
		}
		if (this.sourceTypes != null) {
			baseString = this.sourceTypes.toString();
		}
		if (this.accessRestriction != null) {
			baseString += " " + this.accessRestriction.toString(); //$NON-NLS-1$
		}
		if (this.externalAnnotationPath != null) {
			baseString += " extPath=" + this.externalAnnotationPath.toString(); //$NON-NLS-1$
		}
		return baseString;
	}
	
	/**
	 * Returns the associated access restriction, or null if none.
	 */
	public AccessRestriction getAccessRestriction() {
		return this.accessRestriction;
	}

	public void setBinaryType(IBinaryType newType) {
		this.binaryType = newType;
	}

	/**
	 * Answer the resolved binary form for the type or null if the receiver represents a compilation unit or source
	 * type.
	 */
	public IBinaryType getBinaryType() {
		return this.binaryType;
	}

	/**
	 * Answer the compilation unit or null if the
	 * receiver represents a binary or source type.
	 */
	public ICompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}

	public String getExternalAnnotationPath() {
		return this.externalAnnotationPath;
	}

	/**
	 * Answer the unresolved source forms for the type or null if the
	 * receiver represents a compilation unit or binary type.
	 *
	 * Multiple source forms can be answered in case the originating compilation unit did contain
	 * several type at once. Then the first type is guaranteed to be the requested type.
	 */
	public ISourceType[] getSourceTypes() {
		return this.sourceTypes;
	}

	/**
	 * Answer whether the receiver contains the resolved binary form of the type.
	 */
	public boolean isBinaryType() {
		return this.binaryType != null;
	}

	/**
	 * Answer whether the receiver contains the compilation unit which defines the type.
	 */
	public boolean isCompilationUnit() {
		return this.compilationUnit != null;
	}

	/**
	 * Answer whether the receiver contains the unresolved source form of the type.
	 */
	public boolean isSourceType() {
		return this.sourceTypes != null;
	}

	public boolean ignoreIfBetter() {
		return this.accessRestriction != null && this.accessRestriction.ignoreIfBetter();
	}
	public char[] moduleName() {
		return this.module;
	}

	/*
	 * Returns whether this answer is better than the other awswer.
	 * (accessible is better than discouraged, which is better than
	 * non-accessible)
	 */
	public boolean isBetter(NameEnvironmentAnswer otherAnswer) {
		if (otherAnswer == null) return true;
		if (this.accessRestriction == null) return true;
		return otherAnswer.accessRestriction != null
			&& this.accessRestriction.getProblemId() < otherAnswer.accessRestriction.getProblemId();
	}
}
