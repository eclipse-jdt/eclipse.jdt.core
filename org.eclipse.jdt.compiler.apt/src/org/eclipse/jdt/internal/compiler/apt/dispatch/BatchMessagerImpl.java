/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.apt.model.ExecutableElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.TypeElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.VariableElementImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * An implementation of Messager that reports messages via the Compiler
 */
public class BatchMessagerImpl implements Messager {

	private static final String[] NO_ARGUMENTS = new String[0];
	private final Main _compiler;
	private final BaseProcessingEnvImpl _processingEnv;

	public BatchMessagerImpl(BaseProcessingEnvImpl processingEnv, Main compiler) {
		_compiler = compiler;
		_processingEnv = processingEnv;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg) {
		printMessage(kind, msg, null, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e) {
		printMessage(kind, msg, e, null, null);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a) {
		printMessage(kind, msg, e, a, null);

	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Messager#printMessage(javax.tools.Diagnostic.Kind, java.lang.CharSequence, javax.lang.model.element.Element, javax.lang.model.element.AnnotationMirror, javax.lang.model.element.AnnotationValue)
	 */
	@Override
	public void printMessage(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a, AnnotationValue v) {
		ReferenceContext referenceContext = null;
		int startPosition = 0;
		int endPosition = 0;
		CategorizedProblem problem = null;
		if (e != null) {
			switch(e.getKind()) {
				case ANNOTATION_TYPE :
				case INTERFACE :
				case CLASS :
				case ENUM :
					TypeElementImpl typeElementImpl = (TypeElementImpl) e;
					Binding typeBinding = typeElementImpl._binding;
					if (typeBinding instanceof SourceTypeBinding) {
						SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) typeBinding;
						TypeDeclaration typeDeclaration = (TypeDeclaration) sourceTypeBinding.scope.referenceContext();
						referenceContext = typeDeclaration;
						startPosition = typeDeclaration.sourceStart;
						endPosition = typeDeclaration.sourceEnd;
					}
					break;
				case PACKAGE :
					// nothing to do: there is no reference context for a package
					break;
				case CONSTRUCTOR :
				case METHOD :
					ExecutableElementImpl executableElementImpl = (ExecutableElementImpl) e;
					Binding binding = executableElementImpl._binding;
					if (binding instanceof MethodBinding) {
						MethodBinding methodBinding = (MethodBinding) binding;
						AbstractMethodDeclaration sourceMethod = methodBinding.sourceMethod();
						if (sourceMethod != null) {
							referenceContext = sourceMethod;
							startPosition = sourceMethod.sourceStart;
							endPosition = sourceMethod.sourceEnd;
						}
					}
					break;
				case ENUM_CONSTANT :
					break;
				case EXCEPTION_PARAMETER :
					break;
				case FIELD :
					VariableElementImpl variableElementImpl = (VariableElementImpl) e;
					binding = variableElementImpl._binding;
					if (binding instanceof FieldBinding) {
						FieldBinding fieldBinding = (FieldBinding) binding;
						FieldDeclaration fieldDeclaration = fieldBinding.sourceField();
						if (fieldDeclaration != null) {
							ReferenceBinding declaringClass = fieldBinding.declaringClass;
							if (declaringClass instanceof SourceTypeBinding) {
								SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) declaringClass;
								TypeDeclaration typeDeclaration = (TypeDeclaration) sourceTypeBinding.scope.referenceContext();
								referenceContext = typeDeclaration;
							}
							startPosition = fieldDeclaration.sourceStart;
							endPosition = fieldDeclaration.sourceEnd;
						}
					}
					break;
				case INSTANCE_INIT :
				case STATIC_INIT :
					break;
				case LOCAL_VARIABLE :
					break;
				case PARAMETER :
					break;
				case TYPE_PARAMETER :
			}
		}
		StringBuilder builder = new StringBuilder(msg);
		switch(kind) {
			case ERROR :
				_processingEnv.setErrorRaised(true);
				if (referenceContext != null) {
					CompilationResult result = referenceContext.compilationResult();
					int[] lineEnds = null;
					int lineNumber = startPosition >= 0
							? Util.getLineNumber(startPosition, lineEnds = result.getLineSeparatorPositions(), 0, lineEnds.length-1)
							: 0;
					int columnNumber = startPosition >= 0
							? Util.searchColumnNumber(result.getLineSeparatorPositions(), lineNumber,startPosition)
							: 0;
					problem = new BatchAptProblem(
							result.fileName,
							String.valueOf(builder),
							0,
							NO_ARGUMENTS,
							ProblemSeverities.Error,
							startPosition,
							endPosition,
							lineNumber,
							columnNumber);
				} else {
					problem = new BatchAptProblem(
							null,
							String.valueOf(builder),
							0,
							NO_ARGUMENTS,
							ProblemSeverities.Error,
							startPosition,
							endPosition,
							0,
							1);
				}
				break;
			case MANDATORY_WARNING :
			case WARNING :
			case NOTE :
			case OTHER :
				if (referenceContext != null) {
					CompilationResult result = referenceContext.compilationResult();
					int[] lineEnds = null;
					int lineNumber = startPosition >= 0
							? Util.getLineNumber(startPosition, lineEnds = result.getLineSeparatorPositions(), 0, lineEnds.length-1)
							: 0;
					int columnNumber = startPosition >= 0
							? Util.searchColumnNumber(result.getLineSeparatorPositions(), lineNumber,startPosition)
							: 0;
					problem = new BatchAptProblem(
							result.fileName,
							String.valueOf(builder),
							0,
							NO_ARGUMENTS,
							ProblemSeverities.Warning,
							startPosition,
							endPosition,
							lineNumber,
							columnNumber);
				} else {
					problem = new BatchAptProblem(
							null,
							String.valueOf(builder),
							0,
							NO_ARGUMENTS,
							ProblemSeverities.Warning,
							startPosition,
							endPosition,
							0,
							1);
				}
				break;
		}
		if (problem != null) {
			this._compiler.addExtraProblems(problem);
		}
	}
}
