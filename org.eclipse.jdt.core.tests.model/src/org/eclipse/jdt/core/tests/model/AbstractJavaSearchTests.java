/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.PackageFragment;
//import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
//import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.core.SourceRefElement;

/**
 * Abstract class for Java Search tests.
 */
public class AbstractJavaSearchTests extends AbstractJavaModelTests implements IJavaSearchConstants {

	public static List TEST_SUITES = null;
	protected static IJavaProject JAVA_PROJECT;
	protected static boolean COPY_DIRS = true;
	protected static int EXACT_RULE = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
	protected static int EQUIVALENT_RULE = EXACT_RULE | SearchPattern.R_EQUIVALENT_MATCH;
	protected static int ERASURE_RULE = EXACT_RULE | SearchPattern.R_ERASURE_MATCH;
	protected static int RAW_RULE = EXACT_RULE | SearchPattern.R_ERASURE_MATCH | SearchPattern.R_EQUIVALENT_MATCH;

//	ICompilationUnit[] workingCopies;
//	boolean discard;

	/**
	 * Collects results as a string.
	 */
	public static class JavaSearchResultCollector extends SearchRequestor {
		public StringBuffer results = new StringBuffer(), line;
		public boolean showAccuracy;
		public boolean showContext;
		public boolean showRule;
		public boolean showInsideDoc;
		public boolean showPotential = true;
		public boolean showProject;
		public boolean showSynthetic;
		public int count = 0;
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			count++;
			try {
				IResource resource = match.getResource();
				IJavaElement element = (IJavaElement) match.getElement();
				line = new StringBuffer(getPathString(resource, element));
				if (this.showProject) {
					IProject project = element.getJavaProject().getProject();
					line.append(" [in ");
					line.append(project.getName());
					line.append("]");
				}
				ICompilationUnit unit = null;
				if (element instanceof IMethod) {
					line.append(" ");
					IMethod method = (IMethod)element;
					append(method);
					unit = method.getCompilationUnit();
				} else if (element instanceof IType) {
					line.append(" ");
					IType type = (IType)element;
					append(type);
					unit = type.getCompilationUnit();
				} else if (element instanceof IField) {
					line.append(" ");
					IField field = (IField)element;
					append(field);
					unit = field.getCompilationUnit();
				} else if (element instanceof IInitializer) {
					line.append(" ");
					IInitializer initializer = (IInitializer)element;
					append(initializer);
					unit = initializer.getCompilationUnit();
				} else if (element instanceof IPackageFragment) {
					line.append(" ");
					append((IPackageFragment)element);
				} else if (element instanceof ILocalVariable) {
					line.append(" ");
					ILocalVariable localVar = (ILocalVariable)element;
					IJavaElement parent = localVar.getParent();
					if (parent instanceof IInitializer) {
						IInitializer initializer = (IInitializer)parent;
						append(initializer);
					} else { // IMethod
						IMethod method = (IMethod)parent;
						append(method);
					}
					line.append(".");
					line.append(localVar.getElementName());
					unit = (ICompilationUnit)localVar.getAncestor(IJavaElement.COMPILATION_UNIT);
				} else if (element instanceof IImportDeclaration) {
					IImportDeclaration importDeclaration = (IImportDeclaration)element;
					unit = (ICompilationUnit)importDeclaration.getAncestor(IJavaElement.COMPILATION_UNIT);
				}
				if (resource instanceof IFile) {
					char[] contents = getSource(resource, element, unit);
					int start = match.getOffset();
					int end = start + match.getLength();
					if (start == -1 || (contents != null && contents.length > 0)) { // retrieving attached source not implemented here
						line.append(" [");
						if (start > -1) {
							if (this.showContext) {
								int lineStart1 = CharOperation.lastIndexOf('\n', contents, 0, start);
								int lineStart2 = CharOperation.lastIndexOf('\r', contents, 0, start);
								int lineStart = Math.max(lineStart1, lineStart2) + 1;
								line.append(CharOperation.subarray(contents, lineStart, start));
								line.append("<");
							}
							line.append(CharOperation.subarray(contents, start, end));
							if (this.showContext) {
								line.append(">");
								int lineEnd1 = CharOperation.indexOf('\n', contents, end);
								int lineEnd2 = CharOperation.indexOf('\r', contents, end);
								int lineEnd = lineEnd1 > 0 && lineEnd2 > 0 ? Math.min(lineEnd1, lineEnd2) : Math.max(lineEnd1, lineEnd2);
								if (lineEnd == -1) lineEnd = contents.length;
								line.append(CharOperation.subarray(contents, end, lineEnd));
							}
						} else {
							line.append("No source");
						}
						line.append("]");
					}
				}
				if (this.showAccuracy) {
					line.append(" ");
					if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
						if (this.showRule) {
							if (match.isExact()) {
								line.append("EXACT_");
							} else if (match.isEquivalent()) {
								line.append("EQUIVALENT_");
							} else if (match.isErasure()) {
								line.append("ERASURE_");
							} else {
								line.append("INVALID_RULE_");
							}
							if (match.isRaw()) {
								line.append("RAW_");
							}
						} else {
							line.append("EXACT_");
						}
						line.append("MATCH");
					} else {
						line.append("POTENTIAL_MATCH");
					}
				}
				if (this.showInsideDoc) {
					line.append(" ");
					if (match.isInsideDocComment()) {
						line.append("INSIDE_JAVADOC");
					} else {
						line.append("OUTSIDE_JAVADOC");
					}
				}
				if (this.showSynthetic) {
					if (match instanceof MethodReferenceMatch) {
						MethodReferenceMatch methRef = (MethodReferenceMatch) match;
						if (methRef.isSynthetic()) {
							line.append(" SYNTHETIC");
						}
					}
				}
				if (match.getAccuracy() == SearchMatch.A_ACCURATE || showPotential) {
					if (results.length() > 0) results.append("\n");
					results.append(line);
				}
			} catch (JavaModelException e) {
				results.append("\n");
				results.append(e.toString());
			}
		}
		private void append(IField field) throws JavaModelException {
			append(field.getDeclaringType());
			line.append(".");
			line.append(field.getElementName());
		}
		private void append(IInitializer initializer) throws JavaModelException {
			append(initializer.getDeclaringType());
			line.append(".");
			if (Flags.isStatic(initializer.getFlags())) {
				line.append("static ");
			}
			line.append("{}");
		}
		private void append(IMethod method) throws JavaModelException {
			if (!method.isConstructor()) {
				line.append(Signature.toString(method.getReturnType()));
				line.append(" ");
			}
			append(method.getDeclaringType());
			if (!method.isConstructor()) {
				line.append(".");
				line.append(method.getElementName());
			}
			line.append("(");
			String[] parameters = method.getParameterTypes();
			boolean varargs = Flags.isVarargs(method.getFlags());
			for (int i = 0, length=parameters.length; i<length; i++) {
				if (i < length - 1) {
					line.append(Signature.toString(parameters[i]));
					line.append(", "); //$NON-NLS-1$
				} else if (varargs) {
					// remove array from signature
					String parameter = parameters[i].substring(1);
					line.append(Signature.toString(parameter));
					line.append(" ..."); //$NON-NLS-1$
				} else {
					line.append(Signature.toString(parameters[i]));
				}
			}
			line.append(")");
		}
		private void append(IPackageFragment pkg) {
			line.append(pkg.getElementName());
		}
		private void append(IType type) throws JavaModelException {
			IJavaElement parent = type.getParent();
			boolean isLocal = false;
			switch (parent.getElementType()) {
				case IJavaElement.COMPILATION_UNIT:
					IPackageFragment pkg = type.getPackageFragment();
					append(pkg);
					if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						line.append(".");
					}
					break;
				case IJavaElement.CLASS_FILE:
					IType declaringType = type.getDeclaringType();
					if (declaringType != null) {
						append(type.getDeclaringType());
						line.append("$");
					} else {
						pkg = type.getPackageFragment();
						append(pkg);
						if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
							line.append(".");
						}
					}
					break;
				case IJavaElement.TYPE:
					append((IType)parent);
					line.append("$");
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
				line.append(":");
			}
			String typeName = type.getElementName();
			if (typeName.length() == 0) {
				line.append("<anonymous>");
			} else {
				line.append(typeName);
			}
			if (isLocal) {
				line.append("#");
				line.append(((SourceRefElement)type).occurrenceCount);
			}
		}
		protected String getPathString(IResource resource, IJavaElement element) {
			String pathString;
			if (resource != null) {
				IPath path = resource.getProjectRelativePath();
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
					contents = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
						null, 
						file.getLocation().toFile().getPath(),
						file.getCharset()).getContents();
				}
			}
			return contents;
		}
		public String toString() {
			return results.toString();
		}
	}
	
	protected JavaSearchResultCollector resultCollector;

	public AbstractJavaSearchTests(String name) {
		this(name, 3);
	}
	public AbstractJavaSearchTests(String name, int tabs) {
		super(name, tabs);
		this.displayName = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertSearchResults(java.lang.String, java.lang.Object)
	 */
	protected void assertSearchResults(String expected) {
		assertSearchResults(expected, resultCollector);
	}
	protected void assertSearchResults(String expected, JavaSearchResultCollector collector) {
		assertSearchResults("Unexpected search results", expected, collector);
	}
	protected void assertSearchResults(String message, String expected, JavaSearchResultCollector collector) {
		String actual = collector.toString();
		if (!expected.equals(actual)) {
			if (this.displayName) {
				System.out.print(getName());
				System.out.print(" expects ");
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
			if (!displayName || collector.count>0) {
				System.out.print(displayString(actual, this.tabs));
				System.out.println(",");
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
		search(element, limitTo, EXACT_RULE, scope, resultCollector);
	}
	protected void search(IJavaElement element, int limitTo, int matchRule, IJavaSearchScope scope) throws CoreException {
		search(element, limitTo, matchRule, scope, resultCollector);
	}
	protected void search(IJavaElement element, int limitTo, int matchRule, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(element, limitTo, matchRule);
		assertNotNull("Pattern should not be null", pattern);
		new SearchEngine(workingCopies).search(
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
		search(patternString, searchFor, limitTo, EXACT_RULE, scope, resultCollector);
	}
	protected void search(String patternString, int searchFor, int limitTo, int matchRule, IJavaSearchScope scope) throws CoreException {
		search(patternString, searchFor, limitTo, matchRule, scope, resultCollector);
	}
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
		new SearchEngine(workingCopies).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
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
//
//	/*
//	 * Search several occurences of a selection in a compilation unit source and returns its start and length.
//	 * If occurence is negative, then perform a backward search from the end of file.
//	 * If selection starts or ends with a comment (to help identification in source), it is removed from returned selection info.
//	 */
//	protected int[] selectionInfo(ICompilationUnit cu, String selection, int occurences) throws JavaModelException {
//		String source = cu.getSource();
//		int index = occurences < 0 ? source.lastIndexOf(selection) : source.indexOf(selection);
//		int max = Math.abs(occurences)-1;
//		for (int n=0; index >= 0 && n<max; n++) {
//			index = occurences < 0 ? source.lastIndexOf(selection, index) : source.indexOf(selection, index+selection.length());
//		}
//		StringBuffer msg = new StringBuffer("Selection '");
//		msg.append(selection);
//		if (index >= 0) {
//			if (selection.startsWith("/**")) { // comment is before
//				int start = source.indexOf("*/", index);
//				if (start >=0) {
//					return new int[] { start+2, selection.length()-(start+2-index) };
//				} else {
//					msg.append("' starts with an unterminated comment");
//				}
//			} else if (selection.endsWith("*/")) { // comment is after
//				int end = source.lastIndexOf("/**", index+selection.length());
//				if (end >=0) {
//					return new int[] { index, index-end };
//				} else {
//					msg.append("' ends with an unstartted comment");
//				}
//			} else { // no comment => use whole selection
//				return new int[] { index, selection.length() };
//			}
//		} else {
//			msg.append("' was not found in ");
//		}
//		msg.append(cu.getElementName());
//		msg.append(":\n");
//		msg.append(source);
//		assertTrue(msg.toString(), false);
//		return null;
//	}
//
//	/**
//	 * Select a local variable in a compilation unit identified with the first occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @return IType
//	 * @throws JavaModelException
//	 */
//	protected ILocalVariable selectLocalVariable(ICompilationUnit unit, String selection) throws JavaModelException {
//		return selectLocalVariable(unit, selection, 1);
//	}
//
//	/**
//	 * Select a local variable in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @param occurences
//	 * @return IType
//	 * @throws JavaModelException
//	 */
//	protected ILocalVariable selectLocalVariable(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
//		return (ILocalVariable) selectJavaElement(unit, selection, occurences, IJavaElement.LOCAL_VARIABLE);
//	}
//
//	/**
//	 * Select a method in a compilation unit identified with the first occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @return IMethod
//	 * @throws JavaModelException
//	 */
//	protected IMethod selectMethod(ICompilationUnit unit, String selection) throws JavaModelException {
//		return selectMethod(unit, selection, 1);
//	}
//
//	/**
//	 * Select a method in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @param occurences
//	 * @return IMethod
//	 * @throws JavaModelException
//	 */
//	protected IMethod selectMethod(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
//		return (IMethod) selectJavaElement(unit, selection, occurences, IJavaElement.METHOD);
//	}
//
//	/**
//	 * Select a parameterized source method in a compilation unit identified with the first occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @return ParameterizedSourceMethod
//	 * @throws JavaModelException
//	 */
//	protected ResolvedSourceMethod selectParameterizedMethod(ICompilationUnit unit, String selection) throws JavaModelException {
//		return selectParameterizedMethod(unit, selection, 1);
//	}
//	
//	/**
//	 * Select a parameterized source method in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @param occurences
//	 * @return ParameterizedSourceMethod
//	 * @throws JavaModelException
//	 */
//	protected ResolvedSourceMethod selectParameterizedMethod(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
//		IMethod type = selectMethod(unit, selection, occurences);
//		assertTrue("Not a parameterized source type: "+type.getElementName(), type instanceof ResolvedSourceMethod);
//		return (ResolvedSourceMethod) type;
//	}
//
//	/**
//	 * Select a parameterized source type in a compilation unit identified with the first occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @return ParameterizedSourceType
//	 * @throws JavaModelException
//	 */
//	protected ResolvedSourceType selectParameterizedType(ICompilationUnit unit, String selection) throws JavaModelException {
//		return selectParameterizedType(unit, selection, 1);
//	}
//	
//	/**
//	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @param occurences
//	 * @return ParameterizedSourceType
//	 * @throws JavaModelException
//	 */
//	protected ResolvedSourceType selectParameterizedType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
//		IType type = selectType(unit, selection, occurences);
//		assertTrue("Not a parameterized source type: "+type.getElementName(), type instanceof ResolvedSourceType);
//		return (ResolvedSourceType) type;
//	}
//
//	/**
//	 * Select a type in a compilation unit identified with the first occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @return IType
//	 * @throws JavaModelException
//	 */
//	protected IType selectType(ICompilationUnit unit, String selection) throws JavaModelException {
//		return selectType(unit, selection, 1);
//	}
//
//	/**
//	 * Select a type in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @param occurences
//	 * @return IType
//	 * @throws JavaModelException
//	 */
//	protected IType selectType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
//		return (IType) selectJavaElement(unit, selection, occurences, IJavaElement.TYPE);
//	}
//
//	/**
//	 * Select a type parameter in a compilation unit identified with the first occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @return IType
//	 * @throws JavaModelException
//	 */
//	protected ITypeParameter selectTypeParameter(ICompilationUnit unit, String selection) throws JavaModelException {
//		return selectTypeParameter(unit, selection, 1);
//	}
//
//	/**
//	 * Select a type parameter in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * @param unit
//	 * @param selection
//	 * @param occurences
//	 * @return IType
//	 * @throws JavaModelException
//	 */
//	protected ITypeParameter selectTypeParameter(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
//		return (ITypeParameter) selectJavaElement(unit, selection, occurences, IJavaElement.TYPE_PARAMETER);
//	}
//
//	/**
//	 * Select a java element in a compilation unit identified with the nth occurence in the source of a given selection.
//	 * Do not allow subclasses to call this method as we want to verify IJavaElement kind.
//	 */
//	private IJavaElement selectJavaElement(ICompilationUnit unit, String selection, int occurences, int elementType) throws JavaModelException {
//		int[] selectionPositions = selectionInfo(unit, selection, occurences);
//		IJavaElement[] elements = unit.codeSelect(selectionPositions[0], selectionPositions[1]);
//		assertEquals("Invalid selection number", 1, elements.length);
//		assertEquals("Invalid java element type: "+elements[0].getElementName(), elements[0].getElementType(), elementType);
//		return elements[0];
//	}
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new JavaSearchResultCollector();
//		if (discard) workingCopies = null;
//		discard = true;
	}
//	protected void tearDown() throws Exception {
//		if (discard && workingCopies != null) {
//			discardWorkingCopies(workingCopies);
//		}
//	}
}
