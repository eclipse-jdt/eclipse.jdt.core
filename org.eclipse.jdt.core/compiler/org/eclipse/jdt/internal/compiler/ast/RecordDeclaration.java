/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class RecordDeclaration extends TypeDeclaration {

	public int nRecordComponents;

	public RecordDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}
	public RecordDeclaration(TypeDeclaration t) {
		super(t.compilationResult);
		this.modifiers = t.modifiers;
		this.modifiersSourceStart = t.modifiersSourceStart;
		this.annotations = t.annotations;
		this.name = t.name;
		this.superInterfaces = t.superInterfaces;
		this.fields = t.fields;
		this.methods = t.methods;
		this.memberTypes = t.memberTypes;
		this.binding = t.binding;
		this.scope = t.scope;
		this.initializerScope = t.initializerScope;
		this.staticInitializerScope = t.staticInitializerScope;
		this.ignoreFurtherInvestigation = t.ignoreFurtherInvestigation;
		this.maxFieldCount = t.maxFieldCount;
		this.declarationSourceStart = t.declarationSourceStart;
		this.declarationSourceEnd = t.declarationSourceEnd;
		this.bodyStart = t.bodyStart;
		this.bodyEnd = t.bodyEnd;
		this.missingAbstractMethods = t.missingAbstractMethods; // TODO: Investigate whether this is relevant.
		this.javadoc = t.javadoc;
		this.allocation = t.allocation;
		this.enclosingType = t.enclosingType;
		this.typeParameters = t.typeParameters;
		this.sourceStart = t.sourceStart;
		this.sourceEnd = t.sourceEnd;
	}
	@Override
	public StringBuffer printHeader(int indent, StringBuffer output) {
		printModifiers(this.modifiers & ~ClassFileConstants.AccRecord, output); // mask record alias volatile
		if (this.annotations != null) {
			printAnnotations(this.annotations, output);
			output.append(' ');
		}

		output.append("record "); //$NON-NLS-1$
		output.append(this.name);
		output.append('(');
		if (this.nRecordComponents > 0 && this.fields != null) {
			for (int i = 0; i < this.nRecordComponents; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				output.append(this.fields[i].type.getTypeName()[0]);
				output.append(' ');
				output.append(this.fields[i].name);
			}
		}
		output.append(')');
		if (this.typeParameters != null) {
			output.append("<");//$NON-NLS-1$
			for (int i = 0; i < this.typeParameters.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				this.typeParameters[i].print(0, output);
			}
			output.append(">");//$NON-NLS-1$
		}
		if (this.superInterfaces != null && this.superInterfaces.length > 0) {
			output.append(" implements "); //$NON-NLS-1$
			for (int i = 0; i < this.superInterfaces.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				this.superInterfaces[i].print(0, output);
			}
		}
		return output;
	}
	@Override
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(" {"); //$NON-NLS-1$
		if (this.memberTypes != null) {
			for (int i = 0; i < this.memberTypes.length; i++) {
				if (this.memberTypes[i] != null) {
					output.append('\n');
					this.memberTypes[i].print(indent + 1, output);
				}
			}
		}
		if (this.fields != null) {
			for (int fieldI = this.nRecordComponents; fieldI < this.fields.length; fieldI++) {
				if (this.fields[fieldI] != null) {
					output.append('\n');
					this.fields[fieldI].print(indent + 1, output);
				}
			}
		}
		if (this.methods != null) {
			for (int i = 0; i < this.methods.length; i++) {
				if (this.methods[i] != null) {
					output.append('\n');
					this.methods[i].print(indent + 1, output);
				}
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}
