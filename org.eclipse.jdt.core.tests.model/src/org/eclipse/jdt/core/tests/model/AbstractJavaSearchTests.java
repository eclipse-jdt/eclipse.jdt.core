/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.jdt.internal.core.ParameterizedSourceMethod;
import org.eclipse.jdt.internal.core.ParameterizedSourceType;
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

	ICompilationUnit[] workingCopies;
	boolean discard;

	/**
	 * Collects results as a string.
	 */
	public static class JavaSearchResultCollector extends SearchRequestor {
		public StringBuffer results = new StringBuffer();
		public boolean showAccuracy;
		public boolean showContext;
		public boolean showRule;
		public boolean showInsideDoc;
		public boolean showProject;
		public boolean showSynthetic;
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			try {
				if (results.length() > 0) results.append("\n");
				IResource resource = match.getResource();
				IJavaElement element = (IJavaElement) match.getElement();
				results.append(getPathString(resource, element));
				if (this.showProject) {
					IProject project = element.getJavaProject().getProject();
					results.append(" [in ");
					results.append(project.getName());
					results.append("]");
				}
				ICompilationUnit unit = null;
				if (element instanceof IMethod) {
					results.append(" ");
					IMethod method = (IMethod)element;
					append(method);
					unit = method.getCompilationUnit();
				} else if (element instanceof IType) {
					results.append(" ");
					IType type = (IType)element;
					append(type);
					unit = type.getCompilationUnit();
				} else if (element instanceof IField) {
					results.append(" ");
					IField field = (IField)element;
					append(field);
					unit = field.getCompilationUnit();
				} else if (element instanceof IInitializer) {
					results.append(" ");
					IInitializer initializer = (IInitializer)element;
					append(initializer);
					unit = initializer.getCompilationUnit();
				} else if (element instanceof IPackageFragment) {
					results.append(" ");
					append((IPackageFragment)element);
				} else if (element instanceof ILocalVariable) {
					results.append(" ");
					ILocalVariable localVar = (ILocalVariable)element;
					IJavaElement parent = localVar.getParent();
					if (parent instanceof IInitializer) {
						IInitializer initializer = (IInitializer)parent;
						append(initializer);
					} else { // IMethod
						IMethod method = (IMethod)parent;
						append(method);
					}
					results.append(".");
					results.append(localVar.getElementName());
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
						results.append(" [");
						if (start > -1) {
							if (this.showContext) {
								int lineStart1 = CharOperation.lastIndexOf('\n', contents, 0, start);
								int lineStart2 = CharOperation.lastIndexOf('\r', contents, 0, start);
								int lineStart = Math.max(lineStart1, lineStart2) + 1;
								results.append(CharOperation.subarray(contents, lineStart, start));
								results.append("<");
							}
							results.append(CharOperation.subarray(contents, start, end));
							if (this.showContext) {
								results.append(">");
								int lineEnd1 = CharOperation.indexOf('\n', contents, end);
								int lineEnd2 = CharOperation.indexOf('\r', contents, end);
								int lineEnd = lineEnd1 > 0 && lineEnd2 > 0 ? Math.min(lineEnd1, lineEnd2) : Math.max(lineEnd1, lineEnd2);
								if (lineEnd == -1) lineEnd = contents.length;
								results.append(CharOperation.subarray(contents, end, lineEnd));
							}
						} else {
							results.append("No source");
						}
						results.append("]");
					}
				}
				if (this.showAccuracy) {
					results.append(" ");
					switch (match.getAccuracy()) {
						case SearchMatch.A_ACCURATE:
							if (this.showRule) {
								int rule = match.getMatchRule();
								if ((rule & SearchPattern.R_EQUIVALENT_MATCH) != 0) {
									this.results.append("EQUIVALENT_");
									if ((rule & SearchPattern.R_ERASURE_MATCH) != 0)
										this.results.append("ERASURE_");
								} else if ((rule & SearchPattern.R_ERASURE_MATCH) != 0) {
									this.results.append("ERASURE_");
								} else {
									results.append("EXACT_");
								}
								results.append("MATCH");
							} else {
								results.append("EXACT_MATCH");
							}
							break;
						case SearchMatch.A_INACCURATE:
							results.append("POTENTIAL_MATCH");
							break;
					}
				}
				if (this.showInsideDoc) {
					results.append(" ");
					if (match.isInsideDocComment()) {
						results.append("INSIDE_JAVADOC");
					} else {
						results.append("OUTSIDE_JAVADOC");
					}
				}
				if (this.showSynthetic) {
					if (match instanceof MethodReferenceMatch) {
						MethodReferenceMatch methRef = (MethodReferenceMatch) match;
						if (methRef.isSynthetic()) {
							results.append(" SYNTHETIC");
						}
					}
				}
			} catch (JavaModelException e) {
				results.append("\n");
				results.append(e.toString());
			}
		}
		private void append(IField field) throws JavaModelException {
			append(field.getDeclaringType());
			results.append(".");
			results.append(field.getElementName());
		}
		private void append(IInitializer initializer) throws JavaModelException {
			append(initializer.getDeclaringType());
			results.append(".");
			if (Flags.isStatic(initializer.getFlags())) {
				results.append("static ");
			}
			results.append("{}");
		}
		private void append(IMethod method) throws JavaModelException {
			if (!method.isConstructor()) {
				results.append(Signature.toString(method.getReturnType()));
				results.append(" ");
			}
			append(method.getDeclaringType());
			if (!method.isConstructor()) {
				results.append(".");
				results.append(method.getElementName());
			}
			results.append("(");
			String[] parameters = method.getParameterTypes();
			boolean varargs = Flags.isVarargs(method.getFlags());
			for (int i = 0, length=parameters.length; i<length; i++) {
				if (i < length - 1) {
					results.append(Signature.toString(parameters[i]));
					results.append(", "); //$NON-NLS-1$
				} else if (varargs) {
					// remove array from signature
					String parameter = parameters[i].substring(1);
					results.append(Signature.toString(parameter));
					results.append(" ..."); //$NON-NLS-1$
				} else {
					results.append(Signature.toString(parameters[i]));
				}
			}
			results.append(")");
		}
		private void append(IPackageFragment pkg) {
			results.append(pkg.getElementName());
		}
		private void append(IType type) throws JavaModelException {
			IJavaElement parent = type.getParent();
			boolean isLocal = false;
			switch (parent.getElementType()) {
				case IJavaElement.COMPILATION_UNIT:
					IPackageFragment pkg = type.getPackageFragment();
					append(pkg);
					if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						results.append(".");
					}
					break;
				case IJavaElement.CLASS_FILE:
					IType declaringType = type.getDeclaringType();
					if (declaringType != null) {
						append(type.getDeclaringType());
						results.append("$");
					} else {
						pkg = type.getPackageFragment();
						append(pkg);
						if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
							results.append(".");
						}
					}
					break;
				case IJavaElement.TYPE:
					append((IType)parent);
					results.append("$");
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
				results.append(":");
			}
			String typeName = type.getElementName();
			if (typeName.length() == 0) {
				results.append("<anonymous>");
			} else {
				results.append(typeName);
			}
			if (isLocal) {
				results.append("#");
				results.append(((SourceRefElement)type).occurrenceCount);
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
	/*
	 * Overrides super method to create parent folders if necessary
	 *
	public ICompilationUnit getWorkingCopy(String fileName, String source) throws JavaModelException {
		IPath folder = new Path(fileName).removeLastSegments(1);
		try {
			createFolder(folder);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ICompilationUnit workingCopy = super.getWorkingCopy(fileName, source, true);
		workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
		return workingCopy;
	}
	/*
	 * Overrides super method to create parent folders if necessary
	 *
	public ICompilationUnit getWorkingCopy(String fileName, String source, WorkingCopyOwner owner, boolean computeProblems) throws JavaModelException {
		ICompilationUnit workingCopy = super.getWorkingCopy(fileName, source, owner, computeProblems);
		workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
		return workingCopy;
	}
	/*
	 * Overrides super method to create parent folders if necessary
	 *
	public ICompilationUnit getWorkingCopy(String fileName, String source, WorkingCopyOwner owner) throws JavaModelException {
		IPath folder = new Path(fileName).removeLastSegments(1);
		try {
			createFolder(folder);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ICompilationUnit workingCopy = super.getWorkingCopy(fileName, source, owner, true);
		workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
		return workingCopy;
	}
	protected void search(SearchPattern searchPattern, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		new SearchEngine().search(
			searchPattern, 
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			requestor,
			null);
	}
	*/
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
		if (patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1)
			matchRule |= SearchPattern.R_PATTERN_MATCH;
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

	/*
	 * Search several occurences of a selection in a compilation unit source and returns its start and length.
	 * If occurence is negative, then perform a backward search from the end of file.
	 * If selection starts or ends with a comment (to help identification in source), it is removed from returned selection info.
	 */
	private int[] selectionInfo(ICompilationUnit cu, String selection, int occurences) throws JavaModelException {
		String source = cu.getSource();
		int index = occurences < 0 ? source.lastIndexOf(selection) : source.indexOf(selection);
		int max = Math.abs(occurences)-1;
		for (int n=0; index >= 0 && n<max; n++) {
			index = occurences < 0 ? source.lastIndexOf(selection, index) : source.indexOf(selection, index+selection.length());
		}
		StringBuffer msg = new StringBuffer("Selection '");
		msg.append(selection);
		if (index >= 0) {
			if (selection.startsWith("/**")) { // comment is before
				int start = source.indexOf("*/", index);
				if (start >=0) {
					return new int[] { start+2, selection.length()-(start+2-index) };
				} else {
					msg.append("' starts with an unterminated comment");
				}
			} else if (selection.endsWith("*/")) { // comment is after
				int end = source.lastIndexOf("/**", index+selection.length());
				if (end >=0) {
					return new int[] { index, index-end };
				} else {
					msg.append("' ends with an unstartted comment");
				}
			} else { // no comment => use whole selection
				return new int[] { index, selection.length() };
			}
		} else {
			msg.append("' was not found in ");
		}
		msg.append(cu.getElementName());
		msg.append(":\n");
		msg.append(source);
		assertTrue(msg.toString(), false);
		return null;
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @return ParameterizedSourceMethod
	 * @throws JavaModelException
	 */
	protected ParameterizedSourceMethod selectParameterizedMethod(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectParameterizedMethod(unit, selection, 1);
	}
	
	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return ParameterizedSourceMethod
	 * @throws JavaModelException
	 */
	protected ParameterizedSourceMethod selectParameterizedMethod(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IMethod type = selectMethod(unit, selection, occurences);
		assertTrue("Not a parameterized source type: "+type.getElementName(), type instanceof ParameterizedSourceMethod);
		return (ParameterizedSourceMethod) type;
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @return ParameterizedSourceType
	 * @throws JavaModelException
	 */
	protected ParameterizedSourceType selectParameterizedType(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectParameterizedType(unit, selection, 1);
	}
	
	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return ParameterizedSourceType
	 * @throws JavaModelException
	 */
	protected ParameterizedSourceType selectParameterizedType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IType type = selectType(unit, selection, occurences);
		assertTrue("Not a parameterized source type: "+type.getElementName(), type instanceof ParameterizedSourceType);
		return (ParameterizedSourceType) type;
	}

	/**
	 * Select a source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @return IMethod
	 * @throws JavaModelException
	 */
	protected IMethod selectMethod(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectMethod(unit, selection, 1);
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return IMethod
	 * @throws JavaModelException
	 */
	protected IMethod selectMethod(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IJavaElement element = selectJavaElement(unit, selection, occurences);
		assertTrue("Not a source method: "+element.getElementName(), element instanceof IMethod);
		return (IMethod) element;
	}

	/**
	 * Select a source type in a compilation unit identified with the first occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @return IType
	 * @throws JavaModelException
	 */
	protected IType selectType(ICompilationUnit unit, String selection) throws JavaModelException {
		return selectType(unit, selection, 1);
	}

	/**
	 * Select a parameterized source type in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return IType
	 * @throws JavaModelException
	 */
	protected IType selectType(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		IJavaElement element = selectJavaElement(unit, selection, occurences);
		assertTrue("Not a source type: "+element.getElementName(), element instanceof IType);
		return (IType) element;
	}

	/**
	 * Select a java element in a compilation unit identified with the nth occurence in the source of a given selection.
	 * @param unit
	 * @param selection
	 * @param occurences
	 * @return IJavaElement
	 * @throws JavaModelException
	 */
	protected IJavaElement selectJavaElement(ICompilationUnit unit, String selection, int occurences) throws JavaModelException {
		int[] selectionPositions = selectionInfo(unit, selection, occurences);
		IJavaElement[] elements = unit.codeSelect(selectionPositions[0], selectionPositions[1]);
		assertEquals("Invalid selection number", 1, elements.length);
		return elements[0];
	}
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new JavaSearchResultCollector();
		if (discard) workingCopies = null;
		discard = true;
	}
	protected void tearDown() throws Exception {
		if (discard && workingCopies != null) {
			discardWorkingCopies(workingCopies);
		}
	}
}
