/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ExtraFlags;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.LambdaExpression;
import org.eclipse.jdt.internal.core.Member;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessConstructorRequestor;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessMethodRequestor;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;

/**
 * Abstract class for Java Search tests.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class AbstractJavaSearchTests extends ModifyingResourceTests implements IJavaSearchConstants {

	public static List JAVA_SEARCH_SUITES = null;
	protected static IJavaProject JAVA_PROJECT;
	protected static boolean COPY_DIRS = true;
	protected final static int EXACT_RULE = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
	protected final static int EQUIVALENT_RULE = EXACT_RULE | SearchPattern.R_EQUIVALENT_MATCH;
	protected final static int ERASURE_RULE = EXACT_RULE | SearchPattern.R_ERASURE_MATCH;
	protected final static int RAW_RULE = EXACT_RULE | SearchPattern.R_ERASURE_MATCH | SearchPattern.R_EQUIVALENT_MATCH;

//	ICompilationUnit[] workingCopies;
//	boolean discard;

	/**
	 * Flags for the default search result collector
	 */
	static protected final int SHOW_ACCURACY		= 0x0001;
	static protected final int SHOW_SELECTION		= 0x0002;
	static protected final int SHOW_RULE					= 0x0004;
	static protected final int SHOW_INSIDE_DOC	= 0x0008;
	static protected final int SHOW_POTENTIAL		= 0x0010;
	static protected final int SHOW_PROJECT			= 0x0020;
	static protected final int SHOW_SYNTHETIC		= 0x0040;
	static protected final int SHOW_OFFSET			= 0x0080;
	static protected final int SHOW_ACCESS			= 0x0100;
	static protected final int SHOW_MATCH_KIND	= 0x0200;
	static protected final int SHOW_JAR_FILE			= 0x0400;

	public static class ConstructorDeclarationsCollector implements IRestrictedAccessConstructorRequestor {
		List<String> results = new ArrayList<>();

		public void acceptConstructor(
				int modifiers,
				char[] simpleTypeName,
				int parameterCount,
				char[] signature,
				char[][] parameterTypes,
				char[][] parameterNames,
				int typeModifiers,
				char[] packageName,
				int extraFlags,
				String path,
				AccessRestriction access) {
			StringBuilder buffer = new StringBuilder();

			boolean isMemberType = (extraFlags & ExtraFlags.IsMemberType) != 0;

			buffer.append(packageName == null ? CharOperation.NO_CHAR : packageName);
			if (isMemberType) {
				buffer.append('.');
				buffer.append('?'); // enclosing type names are not stored in the indexes
				buffer.append('?');
				buffer.append('?');
			}
			buffer.append('.');
			buffer.append(simpleTypeName);
			buffer.append('#');
			buffer.append(simpleTypeName);
			buffer.append('(');

			parameterTypes = signature == null ? parameterTypes : Signature.getParameterTypes(signature);

			for (int i = 0; i < parameterCount; i++) {
				if (i != 0) buffer.append(',');

				if (parameterTypes != null) {
					char[] parameterType;
					if (signature != null) {
						parameterType = Signature.toCharArray(parameterTypes[i]);
						CharOperation.replace(parameterType, '/', '.');
					} else {
						parameterType = parameterTypes[i];
					}
					buffer.append(parameterType);
				} else {
					buffer.append('?'); // parameter type names are not stored in the indexes
					buffer.append('?');
					buffer.append('?');
				}
				buffer.append(' ');
				if (parameterNames != null) {
					buffer.append(parameterNames[i]);
				} else {
					buffer.append("arg"+i);
				}
			}
			buffer.append(')');

			if (parameterCount < 0) {
				buffer.append('*');
			}

			this.results.add(buffer.toString());
		}

		@Override
		public String toString(){
			int length = this.results.size();
			String[] strings = new String[length];
			this.results.toArray(strings);
			org.eclipse.jdt.internal.core.util.Util.sort(strings);
			StringBuilder buffer = new StringBuilder(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
		public int size() {
			return this.results.size();
		}
	}

	static void checkAndAddtoBuffer(StringBuffer buffer, char[] precond, char c) {
		if (precond == null || precond.length == 0) return;
		buffer.append(precond);
		buffer.append(c);
	}
	public static class MethodDeclarationsCollector implements IRestrictedAccessMethodRequestor {
		List<String> results = new ArrayList<>();

		@Override
		public void acceptMethod(
				char[] methodName,
				int parameterCount,
				char[] declaringQualifier,
				char[] simpleTypeName,
				int typeModifiers,
				char[] packageName,
				char[] signature,
				char[][] parameterTypes,
				char[][] parameterNames,
				char[] returnType,
				int modifiers,
				String path,
				AccessRestriction access,
				int methodIndex) {

			StringBuffer buffer = new StringBuffer();
			char c = '.';
			char[] noname = new String("<NONAME>").toCharArray();
			buffer.append(path);
			buffer.append(' ');
			buffer.append(returnType == null ? CharOperation.NO_CHAR: returnType);
			buffer.append(' ');
			checkAndAddtoBuffer(buffer, packageName, c);
			checkAndAddtoBuffer(buffer, declaringQualifier, c);
			checkAndAddtoBuffer(buffer, simpleTypeName == null ? noname : simpleTypeName, c);
			buffer.append(methodName);
			buffer.append('(');
			parameterTypes = signature == null ? parameterTypes : Signature.getParameterTypes(signature);

			for (int i = 0; i < parameterCount; i++) {
				if (parameterTypes != null) {
					char[] parameterType;
					if (parameterTypes.length != parameterCount) {
						System.out.println("Error");
					}
					if (signature != null) {
						parameterType = Signature.toCharArray(Signature.getTypeErasure(parameterTypes[i]));
						CharOperation.replace(parameterType, '/', '.');
					} else {
						parameterType = this.getTypeErasure(parameterTypes[i]);
					}
					buffer.append(parameterType);
				} else {
					buffer.append('?'); // parameter type names are not stored in the indexes
					buffer.append('?');
					buffer.append('?');
				}
				buffer.append(' ');
				if (parameterNames != null) {
					buffer.append(parameterNames[i]);
				} else {
					buffer.append("arg"+i);
				}
				if (parameterCount > 1 && i < parameterCount - 1) buffer.append(',');
			}
			buffer.append(')');
			this.results.add(buffer.toString());
		}
		private char[] getTypeErasure(char[] typeName) {
			int index;
			if ((index = CharOperation.indexOf('<', typeName)) == -1) return typeName;

			int length = typeName.length;
			char[] typeErasurename = new char[length - 2];

			System.arraycopy(typeName, 0, typeErasurename, 0, index);

			int depth = 1;
			for (int i = index + 1; i < length; i++) {
				switch (typeName[i]) {
					case '<':
						depth++;
						break;
					case '>':
						depth--;
						break;
					default:
						if (depth == 0) {
							typeErasurename[index++] = typeName[i];
						}
						break;
				}
			}

			System.arraycopy(typeErasurename, 0, typeErasurename = new char[index], 0, index);
			return typeErasurename;
		}
		@Override
		public String toString(){
			int length = this.results.size();
			String[] strings = new String[length];
			this.results.toArray(strings);
			org.eclipse.jdt.internal.core.util.Util.sort(strings);
			StringBuilder buffer = new StringBuilder(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
		public int size() {
			return this.results.size();
		}
	}
	/**
	 * Collects results as a string.
	 */
	public static class JavaSearchResultCollector extends SearchRequestor {
		int flags = SHOW_POTENTIAL; // default
		protected SearchMatch match;
		public StringBuffer results = new StringBuffer(), line;
		public int showFlavors = 0;
		public int count = 0;
		List lines = new ArrayList();
		boolean sorted;
		public JavaSearchResultCollector() {
			this(false);
		}
		public JavaSearchResultCollector(boolean sorted) {
			this.sorted = sorted;
		}
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			this.count++;
			this.match = searchMatch;
			writeLine();
			if (this.line != null && (this.match.getAccuracy() == SearchMatch.A_ACCURATE || (this.flags & SHOW_POTENTIAL) != 0)) {
				this.lines.add(this.line);
			}
		}
		protected void addLine(String text) {
			this.lines.add(text);
		}
		protected void writeLine() throws CoreException {
			try {
				IResource resource = this.match.getResource();
				IJavaElement element = getElement(this.match);
				this.line = new StringBuffer();
				if ((this.flags & SHOW_MATCH_KIND) != 0) {
					String matchClassName = this.match.getClass().getName();
					this.line.append(matchClassName.substring(matchClassName.lastIndexOf('.')+1));
					this.line.append(": ");
				}
				this.line.append(getPathString(resource, element));
				if ((this.flags & SHOW_PROJECT) != 0) {
					IProject project = element.getJavaProject().getProject();
					this.line.append(" [in ");
					this.line.append(project.getName());
					this.line.append("]");
				}
				ICompilationUnit unit = null;
				if (element instanceof IModuleDescription) {
					IModuleDescription md = (IModuleDescription) element;
					this.line.append(" ");
					append(md);
					unit = md.getCompilationUnit();
				} else if (element instanceof IMethod) {
					this.line.append(" ");
					IMethod method = (IMethod)element;
					append(method);
					unit = method.getCompilationUnit();
				} else if (element instanceof IType) {
					this.line.append(" ");
					IType type = (IType)element;
					append(type);
					unit = type.getCompilationUnit();
				} else if (element instanceof IField) {
					this.line.append(" ");
					IField field = (IField)element;
					append(field);
					unit = field.getCompilationUnit();
				} else if (element instanceof IInitializer) {
					this.line.append(" ");
					IInitializer initializer = (IInitializer)element;
					append(initializer);
					unit = initializer.getCompilationUnit();
				} else if (element instanceof IPackageFragment) {
					this.line.append(" ");
					append((IPackageFragment)element);
				} else if (element instanceof ILocalVariable) {
					this.line.append(" ");
					ILocalVariable localVar = (ILocalVariable)element;
					IJavaElement parent = localVar.getDeclaringMember();
					if (parent instanceof IInitializer) {
						IInitializer initializer = (IInitializer)parent;
						append(initializer);
					} else { // IMethod
						IMethod method = (IMethod)parent;
						append(method);
					}
					this.line.append(".");
					this.line.append(localVar.getElementName());
					unit = (ICompilationUnit)localVar.getAncestor(IJavaElement.COMPILATION_UNIT);
				} else if (element instanceof ITypeParameter) {
					this.line.append(" ");
					ITypeParameter typeParam = (ITypeParameter)element;
					IJavaElement parent = typeParam.getParent();
					if (parent instanceof IType) {
						IType type = (IType)parent;
						append(type);
						unit = type.getCompilationUnit();
					} else if (parent instanceof IMethod) {
						IMethod method = (IMethod)parent;
						append(method);
						unit = method.getCompilationUnit();
					} else {
						this.line.append("<Unexpected kind of parent for type parameter>");
						unit = (ICompilationUnit)typeParam.getAncestor(IJavaElement.COMPILATION_UNIT);
					}
					this.line.append(".");
					this.line.append(typeParam.getElementName());
				} else if (element instanceof IImportDeclaration) {
					IImportDeclaration importDeclaration = (IImportDeclaration)element;
					unit = (ICompilationUnit)importDeclaration.getAncestor(IJavaElement.COMPILATION_UNIT);
				} else if (element instanceof IPackageDeclaration) {
					IPackageDeclaration packageDeclaration = (IPackageDeclaration)element;
					unit = (ICompilationUnit)packageDeclaration.getAncestor(IJavaElement.COMPILATION_UNIT);
				} else if (element instanceof IAnnotation) {
					this.line.append(" ");
					append((IAnnotation)element);
					unit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
				}
				if (resource instanceof IFile) {
					char[] contents = getSource(resource, element, unit);
					int start = this.match.getOffset();
					int end = start + this.match.getLength();
					if (start == -1 || (contents != null && contents.length > 0)) { // retrieving attached source not implemented here
						this.line.append(" [");
						if (start > -1) {
							if ((this.flags & SHOW_SELECTION) != 0) {
								int lineStart1 = CharOperation.lastIndexOf('\n', contents, 0, start);
								int lineStart2 = CharOperation.lastIndexOf('\r', contents, 0, start);
								int lineStart = Math.max(lineStart1, lineStart2) + 1;
								this.line.append(CharOperation.subarray(contents, lineStart, start));
								this.line.append("!|");
							}
							this.line.append(CharOperation.subarray(contents, start, end));
							if ((this.flags & SHOW_SELECTION) != 0) {
								this.line.append("|!");
								int lineEnd1 = CharOperation.indexOf('\n', contents, end);
								int lineEnd2 = CharOperation.indexOf('\r', contents, end);
								int lineEnd = lineEnd1 > 0 && lineEnd2 > 0 ? Math.min(lineEnd1, lineEnd2) : Math.max(lineEnd1, lineEnd2);
								if (lineEnd == -1) lineEnd = contents.length;
								this.line.append(CharOperation.subarray(contents, end, lineEnd));
							}
							if ((this.flags & SHOW_OFFSET) != 0) {
								this.line.append('@');
								this.line.append(start);
							}
						} else {
							this.line.append("No source");
						}
						this.line.append("]");
					}
				}
				if ((this.flags & SHOW_ACCURACY) != 0) {
					this.line.append(" ");
					if (this.match.getAccuracy() == SearchMatch.A_ACCURATE) {
						if ((this.flags & SHOW_RULE) != 0) {
							if (this.match.isExact()) {
								this.line.append("EXACT_");
							} else if (this.match.isEquivalent()) {
								this.line.append("EQUIVALENT_");
							} else if (this.match.isErasure()) {
								this.line.append("ERASURE_");
							} else {
								this.line.append("INVALID_RULE_");
							}
							if (this.match.isRaw()) {
								this.line.append("RAW_");
							}
						} else {
							this.line.append("EXACT_");
						}
						this.line.append("MATCH");
					} else {
						this.line.append("POTENTIAL_MATCH");
					}
				}
				if ((this.flags & SHOW_INSIDE_DOC) != 0) {
					this.line.append(" ");
					if (this.match.isInsideDocComment()) {
						this.line.append("INSIDE_JAVADOC");
					} else {
						this.line.append("OUTSIDE_JAVADOC");
					}
				}
				if ((this.flags & SHOW_SYNTHETIC) != 0) {
					if (this.match instanceof MethodReferenceMatch) {
						MethodReferenceMatch methRef = (MethodReferenceMatch) this.match;
						if (methRef.isSynthetic()) {
							this.line.append(" SYNTHETIC");
						}
					}
				}
				if (this.showFlavors > 0) {
					if (this.match instanceof MethodReferenceMatch) {
						MethodReferenceMatch methRef = (MethodReferenceMatch) this.match;
						if (methRef.isSuperInvocation() && showSuperInvocation()) {
							this.line.append(" SUPER INVOCATION");
						}
					}
				}
				if ((this.flags & SHOW_ACCESS) != 0) {
					if (this.match instanceof FieldReferenceMatch) {
						FieldReferenceMatch fieldRef = (FieldReferenceMatch) this.match;
						if (fieldRef.isReadAccess()) {
							this.line.append(" READ");
							if (fieldRef.isWriteAccess()) this.line.append("/WRITE");
							this.line.append(" ACCESS");
						} else if (fieldRef.isWriteAccess()) {
							this.line.append(" WRITE ACCESS");
						}
					} else if (this.match instanceof LocalVariableReferenceMatch) {
						LocalVariableReferenceMatch variableRef = (LocalVariableReferenceMatch) this.match;
						if (variableRef.isReadAccess()) {
							this.line.append(" READ");
							if (variableRef.isWriteAccess()) this.line.append("/WRITE");
							this.line.append(" ACCESS");
						} else if (variableRef.isWriteAccess()) {
							this.line.append(" WRITE ACCESS");
						}
					}
				}
			} catch (JavaModelException e) {
				this.line.append("\n");
				this.line.append(e.toString());
			}
		}
		private boolean showSuperInvocation() {
			return (this.showFlavors & PatternLocator.SUPER_INVOCATION_FLAVOR) != 0;
		}
		public void showAccess() {
			this.flags |= SHOW_ACCESS;
		}
		public void showAccuracy(boolean on) {
			if (on) {
				this.flags |= SHOW_ACCURACY;
			} else {
				this.flags &= ~SHOW_ACCURACY;
			}
		}
		public void showInsideDoc() {
			this.flags |= SHOW_INSIDE_DOC;
		}
		public void showJarFile() {
			this.flags |= SHOW_JAR_FILE;
		}
		public void showMatchKind() {
			this.flags |= SHOW_MATCH_KIND;
		}
		public void showOffset() {
			this.flags |= SHOW_OFFSET;
		}
		public void showPotential(boolean on) {
			if (on) {
				this.flags |= SHOW_POTENTIAL;
			} else {
				this.flags &= ~SHOW_POTENTIAL;
			}
		}
		public void showProject() {
			this.flags |= SHOW_PROJECT;
		}
		public void showRule() {
			this.flags |= SHOW_RULE;
		}
		public void showSelection() {
			this.flags |= SHOW_SELECTION;
		}
		public void showSynthetic() {
			this.flags |= SHOW_SYNTHETIC;
		}
		protected void append(IAnnotation annotation) throws JavaModelException {
			this.line.append("@");
			this.line.append(annotation.getElementName());
			this.line.append('(');
			IMemberValuePair[] pairs = annotation.getMemberValuePairs();
			int length = pairs == null ? 0 : pairs.length;
			for (int i=0; i<length; i++) {
				this.line.append(pairs[i].getMemberName());
				this.line.append('=');
				Object value = pairs[i].getValue();
				switch (pairs[i].getValueKind()) {
					case IMemberValuePair.K_CLASS:
						this.line.append(value);
						this.line.append(".class");
						break;
					default:
						this.line.append(value);
					break;
				}
			}
			this.line.append(')');
		}
		protected void append(IField field) throws JavaModelException {
			append(field.getDeclaringType());
			this.line.append(".");
			this.line.append(field.getElementName());
		}
		private void append(IInitializer initializer) throws JavaModelException {
			append(initializer.getDeclaringType());
			this.line.append(".");
			if (Flags.isStatic(initializer.getFlags())) {
				this.line.append("static ");
			}
			this.line.append("{}");
		}
		private void append(IMethod method) throws JavaModelException {
			if (!method.isConstructor()) {
				this.line.append(Signature.toString(method.getReturnType()));
				this.line.append(" ");
			}
			append(method.getDeclaringType());
			if (!method.isConstructor()) {
				this.line.append(".");
				this.line.append(method.getElementName());
			}
			this.line.append("(");
			String[] parameters = method.getParameterTypes();
			boolean varargs = Flags.isVarargs(method.getFlags());
			for (int i = 0, length=parameters.length; i<length; i++) {
				if (i < length - 1) {
					this.line.append(Signature.toString(parameters[i]));
					this.line.append(", "); //$NON-NLS-1$
				} else if (varargs) {
					// remove array from signature
					String parameter = parameters[i].substring(1);
					this.line.append(Signature.toString(parameter));
					this.line.append(" ..."); //$NON-NLS-1$
				} else {
					this.line.append(Signature.toString(parameters[i]));
				}
			}
			this.line.append(")");
		}
		private void append(IModuleDescription md) {
			this.line.append(md.getElementName());
		}
		private void append(IPackageFragment pkg) {
			this.line.append(pkg.getElementName());
		}
		private void append(IType type) throws JavaModelException {
			IJavaElement parent = type.getParent();
			boolean isLocal = false;
			switch (parent.getElementType()) {
				case IJavaElement.COMPILATION_UNIT:
					IPackageFragment pkg = type.getPackageFragment();
					append(pkg);
					if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						this.line.append(".");
					}
					break;
				case IJavaElement.CLASS_FILE:
					IType declaringType = type.getDeclaringType();
					if (declaringType != null) {
						append(type.getDeclaringType());
						this.line.append("$");
					} else {
						pkg = type.getPackageFragment();
						append(pkg);
						if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
							this.line.append(".");
						}
					}
					break;
				case IJavaElement.TYPE:
					append((IType)parent);
					this.line.append("$");
					break;
				case IJavaElement.FIELD:
					append((IField)parent);
					isLocal = true;
					break;
				case IJavaElement.INITIALIZER:
					append((IInitializer)parent);
					isLocal = true;
					break;
				case IJavaElement.METHOD:
					append((IMethod)parent);
					isLocal = true;
					break;
			}
			if (isLocal) {
				this.line.append(":");
			}
			String typeName = type.getElementName();
			boolean anonymous = false;
			try {
				anonymous = type.isAnonymous();
			} catch(JavaModelException jme) {
			}
			if (anonymous) {
				this.line.append("<anonymous>");
			} else if (type.isLambda()) {
				((LambdaExpression) type).toStringName(this.line);
			} else {
				this.line.append(typeName);
			}
			if (isLocal && !(type instanceof LambdaExpression)) { // don't want occurrence counts for lambdas. it can be confusing at best, as not all are built.
				this.line.append("#");
				this.line.append(((SourceRefElement)type).occurrenceCount);
			}
		}
		protected IJavaElement getElement(SearchMatch searchMatch) {
			return (IJavaElement) searchMatch.getElement();
		}
		protected String getPathString(IResource resource, IJavaElement element) {
			String pathString;
			if (resource != null) {
				IPath path = resource.getProjectRelativePath();
				if ((this.flags & SHOW_JAR_FILE) != 0 && element instanceof Member) {
					IPackageFragmentRoot pkgFragmentRoot = null;
					try {
		                pkgFragmentRoot = element.getJavaProject().findPackageFragmentRoot(resource.getFullPath());
	                } catch (JavaModelException e) {
		                // ignore
	                }
					if (pkgFragmentRoot != null && pkgFragmentRoot.isArchive()) {
						if (pkgFragmentRoot.isExternal()) {
							pathString = pkgFragmentRoot.getPath().toOSString();
						} else {
							pathString = path.toString();
						}
						return pathString + "|" + ((Member)element).getTypeRoot().getElementName();
					}
				}
				if (path.segmentCount() == 0) {
					IJavaElement root = element;
					while (root != null && !(root instanceof IPackageFragmentRoot)) {
						root = root.getParent();
					}
					if (root != null) {
						IPackageFragmentRoot pkgFragmentRoot = (IPackageFragmentRoot)root;
						if (pkgFragmentRoot.isExternal()) {
							pathString = pkgFragmentRoot.getPath().toOSString();
						} else {
							pathString = pkgFragmentRoot.getPath().toString();
						}
					} else {
						pathString = "";
					}
				} else {
					pathString = path.toString();
				}
			} else {
				pathString = element.getPath().toString();
			}
			return pathString;
		}
		protected char[] getSource(IResource resource, IJavaElement element, ICompilationUnit unit) throws CoreException {
			char[] contents = CharOperation.NO_CHAR;
			if ("java".equals(resource.getFileExtension())) {
				ICompilationUnit cu = (ICompilationUnit)element.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (cu != null && cu.isWorkingCopy()) {
					// working copy
					contents = unit.getBuffer().getCharacters();
				} else {
					IFile file = ((IFile) resource);
					try {
						contents = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
							null,
							file.getLocation().toFile().getPath(),
							file.getCharset()).getContents();
					} catch(AbortCompilationUnit e) {
						// TODO (philippe) occured with a FileNotFoundException
						// ignore
					}
				}
			}
			return contents;
		}
		public void clear() {
			this.lines.clear();
			this.match = null;
			this.count = 0;
		}
		public String toString() {
	    	StringBuilder buffer = new StringBuilder();
	    	List displayedLines = new ArrayList(this.lines);
	    	if (this.sorted) {
	    		Collections.sort(displayedLines, new Comparator() {
					@Override
					public int compare(Object o1, Object o2) {
						return o1.toString().compareTo(o2.toString());
				    }
				});
	    	}
	    	int size = displayedLines.size();
	    	for (int i=0; i<size; i++) {
	    		if (i > 0) buffer.append('\n');
	    		buffer.append(displayedLines.get(i).toString());
	    	}
	        return buffer.toString();
	    }
	}

	static class MethodNameMatchCollector extends MethodNameMatchRequestor {
		List matches = new ArrayList();
		public void acceptMethodNameMatch(MethodNameMatch match) {
			IMethod method = match.getMethod();
			if (method != null) {
				this.matches.add(method);
			}
		}
		public int size() {
			return this.matches.size();
		}
		private String toString(boolean withTypeName) {
			int size = size();
			if (size == 0) return "";
			String[] strings = new String[size];
			for (int i=0; i<size; i++) {
				IMethod method = (IMethod) this.matches.get(i);
				IType type = method.getDeclaringType();
				String path = type.getPath().toPortableString();
				String declaringTypeName = type.getFullyQualifiedName('.');
				String[] parameterTypes = method.getParameterTypes();
				String[] parameterNames;
				try {
					parameterNames = method.getParameterNames();
				} catch (JavaModelException e1) {
					parameterNames = new String[] {Util.EMPTY_STRING};
				}
				int nParameterNames = parameterNames.length;

				StringBuilder buf = new StringBuilder();
				buf.append(path);
				buf.append(' ');
				try {
					buf.append(Signature.toString(Signature.getTypeErasure(method.getReturnType())));
					buf.append(' ');
				} catch (JavaModelException e) {
					// do nothing
				}
				if (withTypeName) {
					buf.append(declaringTypeName);
					buf.append('.');
				}
				buf.append(method.getElementName());
				buf.append('(');
				int l = parameterTypes.length;
				if (l > 0) {
					buf.append(Signature.toString(Signature.getTypeErasure(parameterTypes[0])));
					if (nParameterNames > 0) {
						buf.append(' ');
						buf.append(parameterNames[0]);
					}
					for (int j = 1; j < l; ++j) {
						buf.append(',');
						buf.append(Signature.toString(Signature.getTypeErasure(parameterTypes[j])));
						if (j < nParameterNames) {
							buf.append(' ');
							buf.append(parameterNames[j]);
						}
					}
				}
				buf.append(')');
				if (i < size - 1) buf.append('\n');
				strings[i] = buf.toString();
			}
			StringBuilder buffer = new StringBuilder();
			for (int i=0; i<size; i++) {
				buffer.append(strings[i]);
			}
			return buffer.toString();
		}
		public String toString() {
			return toString(false);
		}
		public String toFullyQualifiedNamesString() {
			return toString(true);
		}
	}

	static class TypeNameMatchCollector extends TypeNameMatchRequestor {
		List matches = new ArrayList();
		public void acceptTypeNameMatch(TypeNameMatch match) {
			IType type = match.getType();
			if (type != null) {
				this.matches.add(type);
			}
		}
		public int size() {
			return this.matches.size();
		}
		private String toString(int kind) {
			int size = size();
			if (size == 0) return "";
			String[] strings = new String[size];
			for (int i=0; i<size; i++) {
				IType type = (IType) this.matches.get(i);
				switch (kind) {
					case 1: // fully qualified name
						strings[i] = type.getFullyQualifiedName();
						break;
					case 0:
					default:
						strings[i] = type.toString();
				}
			}
			Arrays.sort(strings);
			StringBuilder buffer = new StringBuilder();
			for (int i=0; i<size; i++) {
				if (i>0) buffer.append('\n');
				buffer.append(strings[i]);
			}
			return buffer.toString();
		}
		public String toString() {
			return toString(0);
		}
		public String toFullyQualifiedNamesString() {
			return toString(1);
		}
	}

protected JavaSearchResultCollector resultCollector;

	public AbstractJavaSearchTests(String name) {
		this(name, 2);
	}
	public AbstractJavaSearchTests(String name, int tabs) {
		super(name, tabs);
		this.displayName = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertSearchResults(java.lang.String, java.lang.Object)
	 */
	protected void assertSearchResults(String expected) {
		assertSearchResults(expected, this.resultCollector);
	}
	protected void assertSearchResults(String expected, JavaSearchResultCollector collector) {
		assertSearchResults("Unexpected search results", expected, collector);
	}
	protected void assertSearchResults(String message, String expected, JavaSearchResultCollector collector) {
		String actual = collector.toString();
		if (!expected.equals(actual)) {
			if (this.displayName) {
				System.out.print(getName());
				System.out.print(" got ");
				if (collector.count==0)
					System.out.println("no result!");
				else {
					System.out.print(collector.count);
					System.out.print(" result");
					if (collector.count==1)
						System.out.println(":");
					else
						System.out.println("s:");
				}
			}
			if (!this.displayName || collector.count>0) {
				System.out.print(displayString(actual, this.tabs));
				System.out.println(this.endChar);
			}
			if (this.workingCopies != null) {
				int length = this.workingCopies.length;
				String[] sources = new String[length*2];
				for (int i=0; i<length; i++) {
					sources[i*2] = this.workingCopies[i].getPath().toString();
					try {
						sources[i*2+1] = this.workingCopies[i].getSource();
					} catch (JavaModelException e) {
						// ignore
					}
				}
				System.out.println("--------------------------------------------------------------------------------");
				length *= 2;
				for (int i=0; i<length; i+=2) {
					System.out.println(sources[i]);
					System.out.println(sources[i+1]);
				}
			}
		}
		assertEquals(
			message,
			expected,
			actual
		);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#copyDirectory(java.io.File, java.io.File)
	 */
	@Override
	protected void copyDirectory(File sourceDir, File targetDir) throws IOException {
		if (COPY_DIRS) {
			super.copyDirectory(sourceDir, targetDir);
		} else {
			targetDir.mkdirs();
			File sourceFile = new File(sourceDir, ".project");
			File targetFile = new File(targetDir, ".project");
			targetFile.createNewFile();
			copy(sourceFile, targetFile);
			sourceFile = new File(sourceDir, ".classpath");
			targetFile = new File(targetDir, ".classpath");
			targetFile.createNewFile();
			copy(sourceFile, targetFile);
		}
	}
	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch")});
	}
	IJavaSearchScope getJavaSearchScope15() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch15")});
	}
	IJavaSearchScope getJavaSearchScope15(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScope15();
		return getJavaSearchPackageScope("JavaSearch15", packageName, addSubpackages);
	}
	IJavaSearchScope getJavaSearchPackageScope(String projectName, String packageName, boolean addSubpackages) throws JavaModelException {
		IPackageFragment fragment = getPackageFragment(projectName, "src", packageName);
		if (fragment == null) return null;
		IJavaElement[] searchPackages = null;
		if (addSubpackages) {
			// Create list of package with first found one
			List packages = new ArrayList();
			packages.add(fragment);
			// Add all possible subpackages
			IJavaElement[] children= ((IPackageFragmentRoot)fragment.getParent()).getChildren();
			String[] names = ((PackageFragment)fragment).names;
			int namesLength = names.length;
			nextPackage: for (int i= 0, length = children.length; i < length; i++) {
				PackageFragment currentPackage = (PackageFragment) children[i];
				String[] otherNames = currentPackage.names;
				if (otherNames.length <= namesLength) continue nextPackage;
				for (int j = 0; j < namesLength; j++) {
					if (!names[j].equals(otherNames[j]))
						continue nextPackage;
				}
				packages.add(currentPackage);
			}
			searchPackages = new IJavaElement[packages.size()];
			packages.toArray(searchPackages);
		} else {
			searchPackages = new IJavaElement[1];
			searchPackages[0] = fragment;
		}
		return SearchEngine.createJavaSearchScope(searchPackages);
	}
	IJavaSearchScope getJavaSearchCUScope(String projectName, String packageName, String cuName) throws JavaModelException {
		ICompilationUnit cu = getCompilationUnit(projectName, "src", packageName, cuName);
		return SearchEngine.createJavaSearchScope(new ICompilationUnit[] { cu });
	}
	protected void search(IJavaElement element, int limitTo, IJavaSearchScope scope) throws CoreException {
		search(element, limitTo, EXACT_RULE, scope, this.resultCollector);
	}
	IJavaSearchScope getJavaSearchWorkingCopiesScope(ICompilationUnit workingCopy) throws JavaModelException {
		return SearchEngine.createJavaSearchScope(new ICompilationUnit[] { workingCopy });
	}
	IJavaSearchScope getJavaSearchWorkingCopiesScope() throws JavaModelException {
		return SearchEngine.createJavaSearchScope(this.workingCopies);
	}
	protected void search(IJavaElement element, int limitTo, int matchRule, IJavaSearchScope scope) throws CoreException {
		search(element, limitTo, matchRule, scope, this.resultCollector);
	}
	@Override
	protected void search(IJavaElement element, int limitTo, int matchRule, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(element, limitTo, matchRule);
		assertNotNull("Pattern should not be null", pattern);
		new SearchEngine(this.workingCopies).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null
		);
	}
	protected void search(SearchPattern searchPattern, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		new SearchEngine().search(
			searchPattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
	}
	protected void search(String patternString, int searchFor, int limitTo, IJavaSearchScope scope) throws CoreException {
		search(patternString, searchFor, limitTo, EXACT_RULE, scope, this.resultCollector);
	}
	protected void search(String patternString, int searchFor, int limitTo, SearchRequestor requestor) throws CoreException {
		search(patternString, searchFor, limitTo, EXACT_RULE, getJavaSearchScope(), requestor);
	}
	protected void search(String patternString, int searchFor, int limitTo, int matchRule, IJavaSearchScope scope) throws CoreException {
		search(patternString, searchFor, limitTo, matchRule, scope, this.resultCollector);
	}
	@Override
	protected void search(String patternString, int searchFor, int limitTo, int matchRule, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		if (patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1) {
			matchRule |= SearchPattern.R_PATTERN_MATCH;
		}
		SearchPattern pattern = SearchPattern.createPattern(
			patternString,
			searchFor,
			limitTo,
			matchRule);
		assertNotNull("Pattern should not be null", pattern);
		new SearchEngine(this.workingCopies).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
	}
	protected void search(IJavaElement element, int limitTo) throws CoreException {
		search(element, limitTo, EXACT_RULE, getJavaSearchScope(), this.resultCollector);
	}
	protected void search(IJavaElement element, int limitTo, int matchRule) throws CoreException {
		search(element, limitTo, matchRule, getJavaSearchScope(), this.resultCollector);
	}
	protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
		search(patternString, searchFor, limitTo, EXACT_RULE, getJavaSearchScope(), this.resultCollector);
	}
	protected void search(String patternString, int searchFor, int limitTo, int matchRule) throws CoreException {
		search(patternString, searchFor, limitTo, matchRule, getJavaSearchScope(), this.resultCollector);
	}
	protected void searchAllConstructorDeclarations(String pattern, int matchRule, IRestrictedAccessConstructorRequestor requestor) throws JavaModelException {
		new BasicSearchEngine(this.workingCopies).searchAllConstructorDeclarations(
				null,
				pattern.toCharArray(),
				matchRule,
				SearchEngine.createWorkspaceScope(),
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
	}
	protected void searchAllMethodNames(String pattern, int matchRule, IRestrictedAccessMethodRequestor requestor) throws JavaModelException {
		new BasicSearchEngine(this.workingCopies).searchAllMethodNames(
				null, SearchPattern.R_EXACT_MATCH,
				null, SearchPattern.R_EXACT_MATCH,
				null, SearchPattern.R_EXACT_MATCH,
				pattern.toCharArray(), matchRule,
				getJavaSearchScope(),
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
	}
	protected void searchAllMethodNames(String pattern, int matchRule, IJavaSearchScope scope, IRestrictedAccessMethodRequestor requestor) throws JavaModelException {
		searchAllMethodNames(
				null, SearchPattern.R_EXACT_MATCH,
				pattern, matchRule,
				scope,
				requestor);
	}
	protected void searchAllMethodNames(
			String declSimpleNamePattern, int declSimpleNameMatchRule,
			String patternMethod, int methodMatchRule,
			IJavaSearchScope scope,
			IRestrictedAccessMethodRequestor requestor) throws JavaModelException {
		searchAllMethodNames(
				null, SearchPattern.R_EXACT_MATCH,
				declSimpleNamePattern, declSimpleNameMatchRule,
				patternMethod, methodMatchRule,
				scope,
				requestor);
	}
	protected void searchAllMethodNames(
			String declQualificationPattern, int declQualificationMatchRule,
			String declSimpleNamePattern, int declSimpleNameMatchRule,
			String patternMethod, int methodMatchRule,
			IJavaSearchScope scope,
			IRestrictedAccessMethodRequestor requestor) throws JavaModelException {
		searchAllMethodNames(
				null, SearchPattern.R_EXACT_MATCH,
				declQualificationPattern, declQualificationMatchRule,
				declSimpleNamePattern, declSimpleNameMatchRule,
				patternMethod, methodMatchRule,
				scope,
				requestor);
	}
	protected void searchAllMethodNames(
			String patternPackage, int pkgMatchRule,
			String declQualificationPattern, int declQualificationMatchRule,
			String declSimpleNamePattern, int declSimpleNameMatchRule,
			String patternMethod, int methodMatchRule,
			IJavaSearchScope scope,
			IRestrictedAccessMethodRequestor requestor) throws JavaModelException {
		new BasicSearchEngine(this.workingCopies).searchAllMethodNames(
				patternPackage == null ? null : patternPackage.toCharArray(), pkgMatchRule,
				declQualificationPattern == null ? null : declQualificationPattern.toCharArray(), declQualificationMatchRule,
				declSimpleNamePattern == null ? null : declSimpleNamePattern.toCharArray(), declSimpleNameMatchRule,
				patternMethod == null ? null : patternMethod.toCharArray(), methodMatchRule,
				scope,
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
	}

	public void searchAllMethodNames(
			String packageName,
			final int pkgMatchRule,
			String declaringQualification,
			final int declQualificationMatchRule,
			String declaringSimpleName,
			final int declSimpleNameMatchRule,
			String methodName,
			final int methodMatchRule,
			IJavaSearchScope scope,
			final MethodNameMatchRequestor nameRequestor) {
		try {
			new SearchEngine(this.workingCopies).searchAllMethodNames(
					packageName != null ? packageName.toCharArray() : null, pkgMatchRule,
					declaringQualification != null ? declaringQualification.toCharArray() : null, declQualificationMatchRule,
					declaringSimpleName != null ? declaringSimpleName.toCharArray() : null, declSimpleNameMatchRule,
					methodName != null ? methodName.toCharArray() : null, methodMatchRule,
					scope, nameRequestor,
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
					null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

	}

	public void searchAllMethodNames(
			String qualifier,
			final int qualifierMatchRule,
			String methodName,
			final int methodMatchRule,
			IJavaSearchScope scope,
			final MethodNameMatchRequestor nameRequestor) {
		try {
			new SearchEngine(this.workingCopies).searchAllMethodNames(
					qualifier != null ? qualifier.toCharArray() : null, qualifierMatchRule,
					methodName != null ? methodName.toCharArray() : null, methodMatchRule,
					scope, nameRequestor,
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
					null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

	}

	protected void searchAllTypeNames(String pattern, int matchRule, TypeNameRequestor requestor) throws JavaModelException {
		new SearchEngine(this.workingCopies).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			pattern.toCharArray(),
			matchRule,
			TYPE,
			getJavaSearchScope(),
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
	}
	protected void searchAllTypeNames(String pattern, int matchRule, TypeNameMatchCollector collector) throws JavaModelException {
		searchAllTypeNames(null, pattern, matchRule, collector);
	}
	protected void searchAllTypeNames(String packagePattern, String typePattern, int matchRule, TypeNameMatchCollector collector) throws JavaModelException {
		new SearchEngine(this.workingCopies).searchAllTypeNames(
			packagePattern==null ? null : packagePattern.toCharArray(),
			SearchPattern.R_EXACT_MATCH,
			typePattern==null ? null : typePattern.toCharArray(),
			matchRule,
			TYPE,
			getJavaSearchScope(),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
	}
	protected void searchDeclarationsOfAccessedFields(IJavaElement enclosingElement, SearchRequestor requestor) throws JavaModelException {
		new SearchEngine().searchDeclarationsOfAccessedFields(enclosingElement, requestor, null);
	}
	protected void searchDeclarationsOfReferencedTypes(IJavaElement enclosingElement, SearchRequestor requestor) throws JavaModelException {
		new SearchEngine().searchDeclarationsOfReferencedTypes(enclosingElement, requestor, null);
	}
	protected void searchDeclarationsOfSentMessages(IJavaElement enclosingElement, SearchRequestor requestor) throws JavaModelException {
		new SearchEngine().searchDeclarationsOfSentMessages(enclosingElement, requestor, null);
	}
	protected void buildAndExpectNoProblems(IJavaProject... javaProjects) throws CoreException {
		List<IProject> projects = new ArrayList<>();
		if (javaProjects != null) {
			Arrays.stream(javaProjects).forEach(jp -> projects.add(jp.getProject()));
		}
		for (IProject project : projects) {
			project.build(IncrementalProjectBuilder.AUTO_BUILD, new NullProgressMonitor());
		}
		waitForAutoBuild();
		waitUntilIndexesReady();

		for (IProject project : projects) {
			assertProblemMarkers("Expected no build problems on project: " + project, "", project);
		}
	}
	protected void buildAndExpectProblems(IJavaProject javaProject, String expectedMarkers) throws CoreException {
		IProject project = javaProject.getProject();
		project.build(IncrementalProjectBuilder.AUTO_BUILD, new NullProgressMonitor());
		waitForAutoBuild();
		waitUntilIndexesReady();
		assertProblemMarkers("Expected build problems on project due to undefined type",
				expectedMarkers, project);
	}
	@Override
	protected void setUp () throws Exception {
		this.indexDisabledForTest = false;
		super.setUp();
		this.resultCollector = new JavaSearchResultCollector();
	}
}
