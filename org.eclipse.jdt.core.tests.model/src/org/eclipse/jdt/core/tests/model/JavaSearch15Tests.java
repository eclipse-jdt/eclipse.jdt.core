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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SourceRefElement;

/**
 * Tests the Java search engine where results are JavaElements and source positions.
 */
public class JavaSearch15Tests extends AbstractJavaModelTests implements IJavaSearchConstants {
	
	/**
	 * Collects results as a string.
	 */
	public static class JavaSearchResultCollector extends SearchRequestor {
		public StringBuffer results = new StringBuffer();
		public boolean showAccuracy = true;
		public boolean showContext;
		public boolean showInsideDoc;
		public boolean showProject;
		public boolean showResource = true;
		public boolean showSynthetic;
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			try {
				if (results.length() > 0) results.append("\n");
				IResource resource = match.getResource();
				IJavaElement element = (IJavaElement) match.getElement();
				boolean lineStarted = false;
				if (this.showResource) {
					results.append(getPathString(resource, element));
					lineStarted = true;
				}
				if (this.showProject) {
					IProject project = element.getJavaProject().getProject();
					if (lineStarted) results.append(" ");
					results.append("[in ");
					results.append(project.getName());
					results.append("]");
					lineStarted = true;
				}
				ICompilationUnit unit = null;
				if (element instanceof IMethod) {
					if (lineStarted) results.append(" ");
					IMethod method = (IMethod)element;
					append(method);
					unit = method.getCompilationUnit();
					lineStarted = true;
				} else if (element instanceof IType) {
					if (lineStarted) results.append(" ");
					IType type = (IType)element;
					append(type);
					unit = type.getCompilationUnit();
					lineStarted = true;
				} else if (element instanceof IField) {
					if (lineStarted) results.append(" ");
					IField field = (IField)element;
					append(field);
					unit = field.getCompilationUnit();
					lineStarted = true;
				} else if (element instanceof IInitializer) {
					if (lineStarted) results.append(" ");
					IInitializer initializer = (IInitializer)element;
					append(initializer);
					unit = initializer.getCompilationUnit();
					lineStarted = true;
				} else if (element instanceof IPackageFragment) {
					if (lineStarted) results.append(" ");
					append((IPackageFragment)element);
					lineStarted = true;
				} else if (element instanceof ILocalVariable) {
					if (lineStarted) results.append(" ");
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
					lineStarted = true;
				}
				if (resource instanceof IFile) {
					char[] contents = getSource(resource, element, unit);
					int start = match.getOffset();
					int end = start + match.getLength();
					if (start == -1 || (contents != null && contents.length > 0)) { // retrieving attached source not implemented here
						if (lineStarted) results.append(" ");
						results.append("[");
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
						lineStarted = true;
					}
				}
				if (this.showAccuracy) {
					switch (match.getAccuracy()) {
						case SearchMatch.A_ACCURATE:
							if (lineStarted) results.append(" ");
							results.append("EXACT_MATCH");
							lineStarted = true;
							break;
						case SearchMatch.A_INACCURATE:
							if (lineStarted) results.append(" ");
							results.append("POTENTIAL_MATCH");
							lineStarted = true;
							break;
					}
				}
				if (this.showInsideDoc) {
					if (match.isInsideDocComment()) {
						if (lineStarted) results.append(" ");
						results.append("INSIDE_JAVADOC");
						lineStarted = true;
					} else {
						if (lineStarted) results.append(" ");
						results.append("OUTSIDE_JAVADOC");
						lineStarted = true;
					}
				}
				if (this.showSynthetic) {
					if (match instanceof MethodReferenceMatch) {
						MethodReferenceMatch methRef = (MethodReferenceMatch) match;
						if (methRef.isSynthetic()) {
							if (lineStarted) results.append(" ");
							results.append("SYNTHETIC");
							lineStarted = true;
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
			for (int i = 0; i < parameters.length; i++) {
				results.append(Signature.toString(parameters[i]));
				if (i < parameters.length-1) {
					results.append(", ");
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
		
	protected IJavaProject javaProject;
		
		
	public JavaSearch15Tests(String name) {
		super(name);
	}
	public static Test suite() {
//		return buildTestSuite(JavaSearch15Tests.class, "testGenericFieldReference", null);
		return buildTestSuite(JavaSearch15Tests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "testGenericFieldReferenceAC04" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 8 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { -1, -1 };
	}
	IJavaSearchScope getJavaSearchScope15() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch15")});
	}
	IJavaSearchScope getJavaSearchPackageScope(String packageName) throws JavaModelException {
		return getJavaSearchPackageScope(packageName, false);
	}
	IJavaSearchScope getJavaSearchPackageScope(String packageName, boolean addSubpackages) throws JavaModelException {
		String projectName = "JavaSearch15";
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
	IJavaSearchScope getJavaSearchPackageScope(String packageName, String unitName) throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", packageName, unitName);
		if (unit == null) return null;
		IJavaElement[] units = new IJavaElement[1];
		units[0] = unit;
		return SearchEngine.createJavaSearchScope(units);
	}
	protected void search(SearchPattern searchPattern, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
		new SearchEngine().search(
			searchPattern, 
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
	public void setUpSuite() throws Exception {
		super.setUpSuite();
	
		this.javaProject = setUpJavaProject("JavaSearch15", "1.5");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearch15");
	
		super.tearDownSuite();
	}

	/**
	 * Generic constructor reference
	 */
	public void testConstructorReference09() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/p2/X.java").getType("X");
		IMethod method = type.getMethod("X", new String[] {"QE;"});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			method, 
			REFERENCES, 
			getJavaSearchScope15(), 
			resultCollector);
		assertSearchResults(
			"src/p2/Y.java Object p2.Y.foo() [new X<Object>(this)] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Declaration of referenced types test.
	 * (Regression test for bug 68862 [1.5] ClassCastException when moving a a java file 
	)
	 */
	public void testDeclarationOfReferencedTypes09() throws CoreException {
		ICompilationUnit cu = getCompilationUnit("JavaSearch15/src/p3/X.java");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector() {
		    public void beginReporting() {
		        results.append("Starting search...");
	        }
		    public void endReporting() {
		        results.append("\nDone searching.");
	        }
		};
		searchDeclarationsOfReferencedTypes(
			cu, 
			resultCollector
		);
		assertSearchResults(
			"Starting search...\n" + 
			getExternalJCLPathString() + " java.lang.Object EXACT_MATCH\n" + 
			"Done searching.",
			resultCollector);
	}

	/**
	 * Generic method reference.
	 */
	public void testMethodReference16() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/p2/X.java").getType("X");
		IMethod method = type.getMethod("foo", new String[] {"QE;"});
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(method, REFERENCES, getJavaSearchScope15(), resultCollector);
		assertSearchResults(
			"src/p2/Y.java void p2.Y.bar() [foo(this)] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Type declaration test.
	 * (generic type)
	 */
	public void testTypeDeclaration02() throws CoreException {
		IPackageFragment pkg = this.getPackageFragment("JavaSearch15", "src", "p1");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			"Y", 
			TYPE,
			DECLARATIONS,
			scope, 
			resultCollector);
		assertSearchResults(
			"src/p1/Y.java p1.Y [Y] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Type reference test
	 * (in a generic type)
	 */
	public void testTypeReference06() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/p1/X.java").getType("X");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			type, 
			REFERENCES, 
			getJavaSearchPackageScope("p1"), 
			resultCollector);
		assertSearchResults(
			"src/p1/Y.java Object p1.Y.foo() [X] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Type reference for 1.5.
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73336)
	 */
	public void testTypeReferenceBug73336() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336/A.java").getType("A");
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(type,
			REFERENCES,
			getJavaSearchPackageScope("bug73336"), 
			resultCollector);
		assertSearchResults(
			"src/bug73336/AA.java bug73336.AA [A] EXACT_MATCH\n" + 
			"src/bug73336/B.java bug73336.B [A] EXACT_MATCH\n" + 
			"src/bug73336/B.java bug73336.B [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java bug73336.C [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java void bug73336.C.foo() [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java void bug73336.C.foo() [A] EXACT_MATCH",
			resultCollector);
	}
	public void testTypeReferenceBug73336b() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336b/A.java").getType("A");
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(type,
			REFERENCES,
			getJavaSearchPackageScope("bug73336b"), 
			resultCollector);
		assertSearchResults(
			"src/bug73336b/B.java bug73336b.B [A] EXACT_MATCH\n" + 
			"src/bug73336b/B.java bug73336b.B [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C() [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C() [A] EXACT_MATCH",
			resultCollector);
	}
	// Verify that no NPE was raised on following case
	public void testTypeReferenceBug73336c() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336c/A.java").getType("A");
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(type,
			REFERENCES,
			getJavaSearchPackageScope("bug73336c"), 
			resultCollector);
		assertSearchResults(
				"src/bug73336c/B.java bug73336c.B [A] EXACT_MATCH\n" + 
				"src/bug73336c/B.java bug73336c.B [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Search references for generics.
	 * Bug 73277: [1.5][Search] Search should work with generics
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73277)
	 */
	/*
	 * Following functionalities are tested:
	 * I) Generic types search
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic types
	 * 			GenericTypeReferenceAA* tests
	 * 		b) multiple parameters generic types
	 * 			GenericTypeReferenceAB* tests
	 */
	// Search reference to a generic type
	public void testGenericTypeReferenceAA01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceAA02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testGenericTypeReferenceAA03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testGenericTypeReferenceAA04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceAB01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceAB02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testGenericTypeReferenceAB03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testGenericTypeReferenceAB04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH",
			resultCollector);
	}

	/*
	 * Following functionalities are tested:
	 * I) Generic types search
	 * 	B) Search using a not parameterized string pattern
	 * 		a) single name
	 * 			GenericTypeReferenceBA* tests
	 * 		b) any string characters
	 * 			GenericTypeReferenceBB* tests
	 */
	// Search reference to a generic type
	public void testGenericTypeReferenceBA01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.s.ref");
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBA02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.m.ref");
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceBA03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testGenericTypeReferenceBA04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testGenericTypeReferenceBA05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBB01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.s.ref");
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBB02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.m.ref");
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceBB03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.s.ref");
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBB04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.m.ref");
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH",
			resultCollector);
	}

	/*
	 * Following functionalities are tested:
	 * I) Generic types search
	 * 	C) Search using a single parameterized string pattern
	 * 		a) no wildcard
	 * 			GenericTypeReferenceCA* tests
	 * 		b) wildcard extends
	 * 			GenericTypeReferenceCB* tests
	 * 		c) wildcard super
	 * 			GenericTypeReferenceCC* tests
	 * 		d) wildcard unbound
	 * 			GenericTypeReferenceCD* tests
	 */
	// Search reference to a generic type
	public void testGenericTypeReferenceCA01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceCA02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceCA03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceCA04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCA05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCA06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceCA07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceCB01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceCB02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceCB03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceCB04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCB05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCB06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceCB07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceCC01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceCC02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceCC03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceCC04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCC05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCC06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceCC07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceCD01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceCD02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceCD03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceCD04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCD05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceCD06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceCD07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}

	/*
	 * Following functionalities are tested:
	 * I) Generic types search
	 * 	D) Search using a multiple parameterized string pattern
	 * 		a) no wildcard
	 * 			GenericTypeReferenceDA* tests
	 * 		b) wildcard extends
	 * 			GenericTypeReferenceDB* tests
	 * 		c) wildcard super
	 * 			GenericTypeReferenceDC* tests
	 * 		d) wildcard unbound
	 * 			GenericTypeReferenceDD* tests
	 */
	// Search reference to a generic type
	public void testGenericTypeReferenceDA01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceDA02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceDA03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceDA04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDA05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDA06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceDA07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceDB01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceDB02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceDB03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceDB04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDB05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDB06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceDB07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceDC01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceDC02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceDC03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceDC04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDC05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDC06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceDC07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>.", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceDD01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ? >", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceDD02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceDD03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceDD04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDD05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceDD06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceDD07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}

	/*
	 * Following functionalities are tested:
	 * II) Field search
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic type field
	 * 			GenericFieldReferenceAA* tests
	 * 		b) multiple parameters generic type field
	 * 			GenericFieldReferenceAB* tests
	 * 		c) single parameterized type field
	 * 			GenericFieldReferenceAC* tests
	 * 		d) mutliple parameterized type field
	 * 			GenericFieldReferenceAD* tests
	 */
	// Search reference to a field of generic type
	public void testGenericFieldReferenceAA01() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getField("t");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of field of member type declared in a generic type
	public void testGenericFieldReferenceAA02() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member").getField("m");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of field of generic member type declared in a generic type
	public void testGenericFieldReferenceAA03() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric").getField("v");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic member type declared in a non-generic type
	public void testGenericFieldReferenceAA04() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember").getField("t");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic type
	public void testGenericFieldReferenceAB01() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getField("t1");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of member type declared in a generic type
	public void testGenericFieldReferenceAB02() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member").getField("m");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic member type declared in a generic type
	public void testGenericFieldReferenceAB03() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric").getField("u2");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic member type declared in a non-generic type
	public void testGenericFieldReferenceAB04() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember").getField("t3");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a single parameterized field
	public void testGenericFieldReferenceAC01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type field of single parameterized type
	public void testGenericFieldReferenceAC02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a single parameterized member type field of a single parameterized type
	public void testGenericFieldReferenceAC03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a single parameterized member type field
	public void testGenericFieldReferenceAC04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a multiple parameterized field
	public void testGenericFieldReferenceAD01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type field of multiple parameterized type
	public void testGenericFieldReferenceAD02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a multiple parameterized member type field of a multiple parameterized type
	public void testGenericFieldReferenceAD03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a multiple parameterized member type field
	public void testGenericFieldReferenceAD04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}

	/*
	 * Following functionalities are tested:
	 * II) Field search
	 * 	B) Search using a string pattern
	 * 		a) single name
	 * 			GenericFieldReferenceBA* tests
	 * 		b) any char characters
	 * 			GenericFieldReferenceBB* tests
	 * 		b) any string characters
	 * 			GenericFieldReferenceBB* tests
	 */
	// Search reference to a single name pattern
	public void testGenericFieldReferenceBA01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchScope15();
		search("gen", FIELD, ALL_OCCURRENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericFieldReferenceBA02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchScope15();
		search("gen_???", FIELD, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [gen_run] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericFieldReferenceBA03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchScope15();
		search("gen_*", FIELD, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_run] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericFieldReferenceBA04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchScope15();
		search("?gen_*", FIELD, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericFieldReferenceBA05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchScope15();
		search("qgen_*", FIELD, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
}
