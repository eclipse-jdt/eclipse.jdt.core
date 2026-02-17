/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
*
https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class ResolveTests21 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	 TESTS_NAMES = new String[] { "testBug577508_4" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests21.class);
}
public ResolveTests21(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("Resolve", "21", false);
	waitUntilIndexesReady();
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	super.tearDownSuite();
}

@Override
protected void tearDown() throws Exception {
	if (this.wc != null) {
		this.wc.discardWorkingCopy();
	}
	super.tearDown();
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2572
// [code select] ClassCastException when hovering in switch case yield
public void testIssue2572() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/Test.java",
			"""
			public class Test {
				public static void main(String[] args) {
					test(new Bar(0));
					test(new FooBar<>("0"));
				}

				public static void test(final Foo foo) {
					final String str = switch (foo) {
					case Bar(Number number) -> {
						yield number.toString();
					}
					case BarFoo(String data) -> {
						yield data;
					}
					case final FooBar<?> fooBar -> {
						yield fooBar.object.toString();
					}
					};
					System.out.println(str);
				}

				private static sealed interface Foo {
				}

				private record Bar(Number number) implements Foo {
				}

				private record BarFoo(String data) implements Foo {
				}

				private record FooBar<T>(T object) implements Foo {
				}
			}
			"""
			);
	String str = this.wc.getSource();
	String selection = "toString";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"toString() [in Object [in Object.class [in java.lang [in " + getExternalPath() + "jclMin21.jar]]]]",
		elements
	);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3050
// [code select] Unexpected runtime error while computing a text hover: java.lang.NegativeArraySizeException
public void testIssue3050() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/Hover.java",
			"""
			interface Function<T, R> {
			    R apply(T t);
			}

			public class Hover {

				void d() {
					Function<Object, Object> f = d -> 2;
					switch (f) {
						case Function<?, ?> s -> {}
					}
				}
			}
			"""
			);
	String str = this.wc.getSource();
	String selection = "Function";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Function [in [Working copy] Hover.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3050
// [code select] Unexpected runtime error while computing a text hover: java.lang.NegativeArraySizeException
public void testIssue3050_2() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/Hover.java",
			"""
			interface Function<T, R> {
			    R apply(T t);
			}

			public class Hover {

				void d() {
					Function<Object, Object> f = d -> 2;
					switch (f) {
						case Function<Object, Object> _, Function<?, ?> _ -> {}
					}
				}
			}
			"""
			);
	String str = this.wc.getSource();
	String selection = "Function";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Function [in [Working copy] Hover.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4217
// NPE: Cannot invoke org.eclipse.jdt.internal.compiler.lookup.BlockScope.getBinding(char[], int, org.eclipse.jdt.internal.compiler.lookup.InvocationSite, boolean) because scope is null" on hyperlink request
public void testIssue4217() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/Test.java",
	"""
	@FunctionalInterface
	interface Callable<V> {
	    /**
	     * Computes a result, or throws an exception if unable to do so.
	     *
	     * @return computed result
	     * @throws Exception if unable to compute a result
	     */
	    V call() throws Exception;
	}

	public class Test {

		public static <T> void createObjectBinding(final Callable<T> func) {
			return;
		}

		sealed interface Index {
			enum SS implements Index {}
			enum TS implements Index {}
		}

		public abstract sealed class Entity<S extends Index> permits Struct, Time {}

		final class Struct extends Entity<Index.SS> {}

		final class Time extends Entity<Index.TS> {

			Struct getStruct() {
				return null;
			}
		}

		private void setMaterials(Time entity) {
			ObjectBinding<Entity<?>> selfIllumImage = Bindings.<Entity<?>>createObjectBinding(() -> {
					var entity2 = entity.getStruct() == null ? entity : entity.getStruct();
					return createSlefIlluminationMap(entity.getSpells());
			});
		}
	}
	""");
	String str = this.wc.getSource();
	String selection = "entity2";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] selected = this.wc.codeSelect(start, length);
	assertEquals(0, selected.length);
}
}
