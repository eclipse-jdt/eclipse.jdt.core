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
		return buildTestSuite(JavaSearch15Tests.class, "testGenericTypeReference", null);
//		return buildTestSuite(JavaSearch15Tests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "testGenericTypeReferenceBB02" };
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
	 * 
	 * Following functionalities are tested:
	 * I) Generic types search
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic types
	 * 			GenericTypeReferenceAA* tests
	 * 		b) multiple parameters generic types
	 * 			GenericTypeReferenceAB* tests
	 * 	B) Search using a string pattern
	 * 		a) not parameterized pattern
	 * 			GenericTypeReferenceBA* tests
	 * 		b) single parameterized pattern
	 * 			GenericTypeReferenceBB* tests
	 * 		c) single parameterized pattern with wildcard (extends)
	 * 			GenericTypeReferenceBC* tests
	 * 		d) single parameterized pattern with wildcard (super)
	 * 			GenericTypeReferenceBD* tests
	 * 		e) single parameterized pattern with wildcard (unbound)
	 * 			GenericTypeReferenceBE* tests
	 * 		f) multiple parameterized pattern
	 * 			GenericTypeReferenceBF* tests
	 * 		g) multiple parameterized pattern with wildcard (extends)
	 * 			GenericTypeReferenceBG* tests
	 * 		h) multiple parameterized pattern with wildcard (extends)
	 * 			GenericTypeReferenceBH* tests
	 * 		i) multiple parameterized pattern with wildcard (unbound)
	 * 			GenericTypeReferenceBI* tests
	 */
	// Search reference to a generic type
	public void testGenericTypeReferenceAA01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_run [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [g1.t.s.def.Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceAA02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [g1.t.s.def.Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testGenericTypeReferenceAA03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [g1.t.s.def.Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [g1.t.s.def.Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [g1.t.s.def.Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [g1.t.s.def.Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testGenericTypeReferenceAA04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_obj [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH",
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
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_run [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [g1.t.m.def.Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceAB02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testGenericTypeReferenceAB03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [g1.t.m.def.Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testGenericTypeReferenceAB04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_obj [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBA01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.s.ref");
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBA02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.m.ref");
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceBA03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testGenericTypeReferenceBA04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testGenericTypeReferenceBA05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBA06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.s.ref");
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBA07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.m.ref");
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testGenericTypeReferenceBA08() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.s.ref");
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Member] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBA09() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t.m.ref");
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBB01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBB02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBB03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBB04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBB05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBB06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBB07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBC01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBC02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBC03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBC04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBC05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBC06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBC07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBD01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Generic<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBD02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBD03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [GenericMember<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBD04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBD05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBD06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBD07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBE01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBE02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_qmk [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_obj [g1.t.s.def.Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_qmk [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBE03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_qmk [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBE04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBE05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBE06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBE07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_qmk [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBF01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBF02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBF03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBF04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBF05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBF06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBF07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBG01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBG02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBG03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBG04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBG05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBG06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBG07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBH01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBH02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBH03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBH04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBH05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBH06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBH07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>.", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testGenericTypeReferenceBI01() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ? >", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.g_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.g [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testGenericTypeReferenceBI02() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_qmk [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gm_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_qmk [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgm_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gm [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgm [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testGenericTypeReferenceBI03() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("GenericMember<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.ygm_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_qmk [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qygm_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.xgm [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qxgm [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testGenericTypeReferenceBI04() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBI05() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (frederic) try to have a better match selection
	public void testGenericTypeReferenceBI06() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Current limitation of SearchPattern:
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testGenericTypeReferenceBI07() throws CoreException {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaSearchScope scope = getJavaSearchPackageScope("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_qmk [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgmg_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gmg [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgmg [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
}
