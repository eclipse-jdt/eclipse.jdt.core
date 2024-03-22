/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] follow up bug for comments - https://bugs.eclipse.org/458208
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;

public class FormatterBugs18Tests extends FormatterRegressionTests {

public static Test suite() {
	return buildModelTestSuite(FormatterBugs18Tests.class);
}

public FormatterBugs18Tests(String name) {
	super(name);
}

/**
 * Create project and set the jar placeholder.
 */
@Override
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterBugs", "1.8"); //$NON-NLS-1$
	}
	super.setUpSuite();
}

/**
 * bug 426520: [1.8][formatter] inserts spaces into annotated qualified type
 * test Ensure that formatting does not change the qualified type formatting for c and it
 * it removes the spaces for s.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=426520"
 */
public void testBug426520a() throws JavaModelException {
	String source =
		"""
		import java.lang.annotation.*;\
		@Target(ElementType.TYPE_USE)
		@interface T {}
		public class X {
			@SuppressWarnings("rawtypes")
			java.util.concurrent.@T Callable c;
			java.  util.  @T Set<java.lang.@T String> s;
		}
		""";
	formatSource(source,
			"""
				import java.lang.annotation.*;
				
				@Target(ElementType.TYPE_USE)
				@interface T {
				}
				
				public class X {
					@SuppressWarnings("rawtypes")
					java.util.concurrent.@T Callable c;
					java.util.@T Set<java.lang.@T String> s;
				}
				""");
}
public void testBug426520b() throws JavaModelException {
	String source =
		"""
		import java.lang.annotation.*;\
		@Target(ElementType.TYPE_USE)
		@interface T {}
		public class X {
			@SuppressWarnings("rawtypes")
			java.util.concurrent.@T()Callable c;
			java.util.@T()Set<java.lang.@T()String> s;
		}
		""";
	formatSource(source,
			"""
				import java.lang.annotation.*;
				
				@Target(ElementType.TYPE_USE)
				@interface T {
				}
				
				public class X {
					@SuppressWarnings("rawtypes")
					java.util.concurrent.@T() Callable c;
					java.util.@T() Set<java.lang.@T() String> s;
				}
				""");
}
public void testBug425040() throws JavaModelException {
	String source =
			"""
		import java.lang.annotation.*;
		
		public class X extends @Annot1 Object {
			@Deprecated	@Annot3 public @Annot2	int b;
		
			@SuppressWarnings("unused")
			public @Annot3() int foo(@Annot4 C<@Annot5() Object> a) {
				@Annot1 int @Annot2 [] i;
				return 0;
			}
		}
		class C<T> {}
		@Documented
		@Target(ElementType.TYPE_USE)
		@interface Annot1 {}
		@Target(ElementType.TYPE_USE)
		@interface Annot2 {}
		@Target(ElementType.TYPE_USE)
		@interface Annot3 {}
		@Target(ElementType.TYPE_USE)
		@interface Annot4 {}
		@Target(ElementType.TYPE_USE)
		@interface Annot5 {}
		""";
	formatSource(source,
			"""
				import java.lang.annotation.*;
				
				public class X extends @Annot1 Object {
					@Deprecated
					@Annot3
					public @Annot2 int b;
				
					@SuppressWarnings("unused")
					public @Annot3() int foo(@Annot4 C<@Annot5() Object> a) {
						@Annot1
						int @Annot2 [] i;
						return 0;
					}
				}
				
				class C<T> {
				}
				
				@Documented
				@Target(ElementType.TYPE_USE)
				@interface Annot1 {
				}
				
				@Target(ElementType.TYPE_USE)
				@interface Annot2 {
				}
				
				@Target(ElementType.TYPE_USE)
				@interface Annot3 {
				}
				
				@Target(ElementType.TYPE_USE)
				@interface Annot4 {
				}
				
				@Target(ElementType.TYPE_USE)
				@interface Annot5 {
				}
				"""
			);
}
public void testBug433177() throws Exception {
	String source =
			"""
		interface Function<T, R> {
			R apply(T t);
		}
		
		public class X {
		
			  public Function<String, String> testOK() {
			    return foo((s) -> {
			      // nothing
			      System.out.println("");
			      return "";
			    });
			  }
		
			  public Function<String, String> testBad() {
			    return this.foo((s) -> {
			      // nothing
			        System.out.println("");
			        return "";
			      });
			  }
		
			  public Function<String, String> foo(Function<String, String> f) {
			    return null;
			  }
		
			}
		""";
	String expected = """
		interface Function<T, R> {
			R apply(T t);
		}
		
		public class X {
		
			public Function<String, String> testOK() {
				return foo((s) -> {
					// nothing
					System.out.println("");
					return "";
				});
			}
		
			public Function<String, String> testBad() {
				return this.foo((s) -> {
					// nothing
					System.out.println("");
					return "";
				});
			}
		
			public Function<String, String> foo(Function<String, String> f) {
				return null;
			}
		
		}
		""";

	formatSource(source,expected);
}
public void testBug434821() throws Exception {
	String source ="""
		public class FormatterTest {
			public void doNothing() {
				new Thread(() -> {
					synchronized (this) {
						try {
							Thread.sleep(0); // blah
					} catch (final InterruptedException e2) {
						e2.printStackTrace();
					}
				}
		
			}	).start();
			}
		}
		""";
	String expected = """
		public class FormatterTest {
			public void doNothing() {
				new Thread(() -> {
					synchronized (this) {
						try {
							Thread.sleep(0); // blah
						} catch (final InterruptedException e2) {
							e2.printStackTrace();
						}
					}
		
				}).start();
			}
		}
		""";

	formatSource(source,expected);
}

}