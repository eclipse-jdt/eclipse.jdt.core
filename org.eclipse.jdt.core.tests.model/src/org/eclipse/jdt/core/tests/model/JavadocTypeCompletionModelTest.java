/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in Javadoc comment of a type declaration.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavadocTypeCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocTypeCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_RANGE = new int[] { 22, -1 };
//	TESTS_NUMBERS = new int[] { 20 };
}
public static Test suite() {
	return buildModelTestSuite(JavadocTypeCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
}

/**
 * @category Tests for tag names completion
 */
public void test001() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on empty tag name:
		 * 	@
		 */
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@");
	assertResults(
		"author[JAVADOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on impossible tag name:
		 * 	@par
		 */
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@par");
	assertResults("");
}

public void test003() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on one letter:
		 * 	@v
		 */
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@v");
	assertResults(
		"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion with several letters:
		 * 	@deprec
		 */
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@deprec");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on full tag name:
		 * 	@link
		 */
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@link");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on full tag name:
		 * 	@link
		 */
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@li");
	assertResults(
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test007() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_3);
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on empty tag name:
		 * 	@
		 */
		// Note: this test should be done using compliance 1.3
		public class Test {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@");
	assertResults(
		"author[JAVADOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test008() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion on empty tag name:
		 * 	@
		 */
		// Note: this test should be done using compliance 1.5
		public class Test<T> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/Test.java", source, true, "@");
	assertResults(
		"author[JAVADOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"code[JAVADOC_INLINE_TAG]{{@code}, null, null, code, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"literal[JAVADOC_INLINE_TAG]{{@literal}, null, null, literal, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for types completion
 */
public void test010() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see Obj
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "Obj");
	assertResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test011() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTest
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTest");
	assertResults(
		"BasicTestTypes[TYPE_REF]{BasicTestTypes, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestReferences[TYPE_REF]{org.eclipse.jdt.core.tests.BasicTestReferences, org.eclipse.jdt.core.tests, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test012() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestTypes
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTest");
	assertResults(
		"BasicTestTypes[TYPE_REF]{BasicTestTypes, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestReferences[TYPE_REF]{org.eclipse.jdt.core.tests.BasicTestReferences, org.eclipse.jdt.core.tests, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test013() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTest
		 */
		public class BasicTestTypes<TPARAM> {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTest");
	assertResults(
		"BasicTestTypes<TPARAM>[TYPE_REF]{BasicTestTypes, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes<TTPARAM;>;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestReferences[TYPE_REF]{org.eclipse.jdt.core.tests.BasicTestReferences, org.eclipse.jdt.core.tests, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test014() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see javadoc.types.tags.BasicTest
		 * 		Note: JDT-UI failed on this one
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "javadoc.types.tags.BasicTest");
	assertResults(
		"BasicTestTypes[TYPE_REF]{BasicTestTypes, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test015() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see javadoc.types.tags.BasicTest
		 * 		Note: JDT-UI failed on this one
		 */
		public class BasicTestTypes<TPARAM> {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "javadoc.types.tags.BasicTest");
	assertResults(
		"BasicTestTypes<TPARAM>[TYPE_REF]{BasicTestTypes, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes<TTPARAM;>;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test016() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.la
		 * 		Note: JDT-UI fails on this one
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.la");
	assertResults(
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test017() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.lang
		 * 		Note: JDT-UI fails on this one
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.la");
	assertResults(
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test018() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see pack.Bin
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "pack.Bin");
	assertSortedResults(
		"Bin1[TYPE_REF]{pack.Bin1, pack, Lpack.Bin1;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin2[TYPE_REF]{pack.Bin2, pack, Lpack.Bin2;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin3[TYPE_REF]{pack.Bin3, pack, Lpack.Bin3;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin4[TYPE_REF]{pack.Bin4, pack, Lpack.Bin4;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin5[TYPE_REF]{pack.Bin5, pack, Lpack.Bin5;, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test019() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see pack.Bin2
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "pack.Bin");
	assertSortedResults(
		"Bin1[TYPE_REF]{pack.Bin1, pack, Lpack.Bin1;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin2[TYPE_REF]{pack.Bin2, pack, Lpack.Bin2;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin3[TYPE_REF]{pack.Bin3, pack, Lpack.Bin3;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin4[TYPE_REF]{pack.Bin4, pack, Lpack.Bin4;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin5[TYPE_REF]{pack.Bin5, pack, Lpack.Bin5;, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test020() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see pack.Bin2
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "pack.Bin2");
	assertSortedResults(
		"Bin2[TYPE_REF]{pack.Bin2, pack, Lpack.Bin2;, null, null, "+this.positions+R_DRICENQNR+"}"
	);
}

public void test021() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see I
		 * 		Note: completion list shoud not include base types.
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "I");
	assertSortedResults(
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test022() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.lang.
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.lang.");
	assertSortedResults(
		"java.lang.annotation[PACKAGE_REF]{java.lang.annotation, java.lang.annotation, null, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"CharSequence[TYPE_REF]{CharSequence, java.lang, Ljava.lang.CharSequence;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Comparable[TYPE_REF]{Comparable, java.lang, Ljava.lang.Comparable;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Enum[TYPE_REF]{Enum, java.lang, Ljava.lang.Enum;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Error[TYPE_REF]{Error, java.lang, Ljava.lang.Error;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"RuntimeException[TYPE_REF]{RuntimeException, java.lang, Ljava.lang.RuntimeException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test023() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.");
	assertResults(
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"java.io[PACKAGE_REF]{java.io, java.io, null, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test024() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.lang
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.");
	assertResults(
		"java.util[PACKAGE_REF]{java.util, java.util, null, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"java.lang.annotation[PACKAGE_REF]{java.lang.annotation, java.lang.annotation, null, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"java.io[PACKAGE_REF]{java.io, java.io, null, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test025() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.lang.Obj
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.lang.");
	assertSortedResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Error[TYPE_REF]{Error, java.lang, Ljava.lang.Error;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"RuntimeException[TYPE_REF]{RuntimeException, java.lang, Ljava.lang.RuntimeException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test026() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see java.lang.Objec
		 */
		public class BasicTestTypes {
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "java.lang.Ob");
	assertResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

/**
 * @category Tests for member types completion
 */
public void test030() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestTypesM
		 */
		public class BasicTestTypes {
			class BasicTestTypesMember {}
		}
		class BasicTestTypesTestSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTestTypesM");
	assertResults(
		"BasicTestTypes.BasicTestTypesMember[TYPE_REF]{BasicTestTypesMember, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes$BasicTestTypesMember;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test031() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestTypesMember
		 */
		public class BasicTestTypes {
			class BasicTestTypesMember {}
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTestTypesM");
	assertResults(
		"BasicTestTypes.BasicTestTypesMember[TYPE_REF]{BasicTestTypesMember, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes$BasicTestTypesMember;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test032() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestTypes.BasicTestTypesM
		 */
		public class BasicTestTypes {
			class BasicTestTypesMember {}
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTestTypesM");
	assertResults(
		"BasicTestTypes.BasicTestTypesMember[TYPE_REF]{BasicTestTypesMember, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes$BasicTestTypesMember;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test033() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see javadoc.types.tags.BasicTestTypes.BasicTestTypesM
		 */
		public class BasicTestTypes {
			class BasicTestTypesMember {}
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTestTypesM");
	assertResults(
		"BasicTestTypes.BasicTestTypesMember[TYPE_REF]{BasicTestTypesMember, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes$BasicTestTypesMember;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test034() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestTypesS
		 */
		public class BasicTestTypes {
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BasicTestTypesS");
	assertResults(
		"BasicTestTypesSecondary[TYPE_REF]{BasicTestTypesSecondary, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypesSecondary;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test035() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see javadoc.types.tags.BasicTestTypesS
		 */
		public class BasicTestTypes {
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "javadoc.types.tags.BasicTestTypesS");
	assertResults(
		"BasicTestTypesSecondary[TYPE_REF]{BasicTestTypesSecondary, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypesSecondary;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test036() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see javadoc.types.tags.BasicTestTypesSecondary
		 */
		public class BasicTestTypes {
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "javadoc.types.tags.BasicTestTypesS");
	assertResults(
		"BasicTestTypesSecondary[TYPE_REF]{BasicTestTypesSecondary, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypesSecondary;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test037() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see javadoc.types.tags.BasicTestTypes.BasicTestTypes
		 */
		public class BasicTestTypes {
		}
		class BasicTestTypesSecondary {}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "javadoc.types.tags.BasicTestTypes.BasicTestTypes");
	assertResults("");
}


/**
 * @category Tests for fields completion
 */
public void test040() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestReferences#FIE
		 * 		Note: JDT/UI create one proposal on this one
		 */
		public class BasicTestTypes {
		}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "FIE");
	assertResults("");
}

public void test041() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see org.eclipse.jdt.core.tests.BasicTestReferences#FIE
		 */
		public class BasicTestTypes {
		}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "FIE");
	assertResults(
		"FIELD[FIELD_REF]{FIELD, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, I, FIELD, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test042() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see org.eclipse.jdt.core.tests.BasicTestReferences#FIELD
		 */
		public class BasicTestTypes {
		}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "FIE");
	assertResults(
		"FIELD[FIELD_REF]{FIELD, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, I, FIELD, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test043() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see #fo
		 */
		public class BasicTestTypes {
			int foo;
		}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.types.tags.BasicTestTypes;, I, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test044() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see #foo
		 */
		public class BasicTestTypes {
			int foo;
		}""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.types.tags.BasicTestTypes;, I, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test045() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/types/tags/BasicTestTypes.java",
			"""
				package javadoc.types.tags;
				/**
				 * Completion after:
				 * 	@see OtherTypes#fo
				 */
				public class BasicTestTypes {
				}""",
		"/Completion/src/javadoc/types/tags/OtherTypes.java",
			"""
				package javadoc.types.tags;
				public class OtherTypes {
					int foo;
				}"""
	};
	completeInJavadoc(sources, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.types.tags.OtherTypes;, I, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}


/**
 * @category Tests for methods completion
 */
public void test050() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestMethod.meth
		 * 		Note that test result may change if bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=26814 was fixed
		 */
		public class BasicTestTypes {
			void method() {}
			void paramMethod(String str, int x, Object obj) {}
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "meth");
	assertResults("");
}

public void test051() throws JavaModelException {
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@see BasicTestMethod#unknown
		 * 		- completion list shoud be empty
		 */
		public class BasicTestTypes {
			void method() {}
			void paramMethod(String str, int x, Object obj) {}
		}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "unknown");
	assertResults("");
}

public void test052() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/types/tags/BasicTestTypes.java",
			"""
				package javadoc.types.tags;
				/**
				 * Completion after:
				 * 	@see OtherTypes#meth
				 */
				public class BasicTestTypes {
				}""",
		"/Completion/src/javadoc/types/tags/OtherTypes.java",
			"""
				package javadoc.types.tags;
				public class OtherTypes {
					void method() {};
				}"""
	};
	completeInJavadoc(sources, true, "meth");
	assertResults(
		"method[METHOD_REF]{method(), Ljavadoc.types.tags.OtherTypes;, ()V, method, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test053() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/types/tags/BasicTestTypes.java",
			"""
				package javadoc.types.tags;
				/**
				 * Completion after:
				 * 	@see OtherTypes#method
				 */
				public class BasicTestTypes {
				}""",
		"/Completion/src/javadoc/types/tags/OtherTypes.java",
			"""
				package javadoc.types.tags;
				public class OtherTypes {
					void method() {};
				}"""
	};
	completeInJavadoc(sources, true, "meth");
	assertResults(
		"method[METHOD_REF]{method(), Ljavadoc.types.tags.OtherTypes;, ()V, method, null, "+this.positions+R_DRICNRNS+"}"
	);
}

/**
 * @category Tests for type parameters completion
 */
public void test060() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param\s
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "@param ", 0); // empty token
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test061() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <
		 * 	Note:
		 * 		JDT/UI fails on this one (no proposal)
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<");
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test062() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <TPA
		 * 	Note:
		 * 		JDT/UI fails on this one (no proposal)
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<TPA");
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test063() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <TPARAM
		 * 	Note:
		 * 		JDT/UI fails on this one (no proposal)
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<TPA");
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test064() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <TPARAM
		 * 	Note:
		 * 		JDT/UI fails on this one (no proposal)
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<TPARAM");
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test065() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <TPARAM>
		 * 	Note:
		 * 		JDT/UI fails on this one (no proposal)
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<TPARAM");
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test066() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <TPARAM>
		 * 	Note:
		 * 		JDT/UI fails on this one (no proposal)
		 */
		public class BasicTestTypes<TPARAM> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<TPARAM>");
	assertResults(
		"TPARAM[JAVADOC_PARAM_REF]{<TPARAM>, null, null, TPARAM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test067() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <T1>
		 * 	@param <T1>
		 */
		public class BasicTestTypes<T1, T2, T3> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<T1>");
	assertResults("");
}

public void test068() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <T1>
		 * 	@param <T1>
		 */
		public class BasicTestTypes<T1, T2, T3> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<T1>", 2); //2nd position
	assertResults("");
}

public void test069() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <T1>
		 ** 	@param\s
		 * 	@param <T3>
		 */
		public class BasicTestTypes<T1, T2, T3> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "** 	@param ", 0); // empty token
	assertResults(
		"T2[JAVADOC_PARAM_REF]{<T2>, null, null, T2, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test070() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <T1>
		 * 	@param <T2>
		 * 	@param <T3>
		 */
		public class BasicTestTypes<T1, T2, T3> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "<T", 3); // 3rd position
	assertResults(
		"T3[JAVADOC_PARAM_REF]{<T3>, null, null, T3, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test071() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"""
		package javadoc.types.tags;
		/**
		 * Completion after:
		 * 	@param <T1>
		 * 	@param <T2>
		 * 	@param <T3>
		 ** 	@param\s
		 */
		public class BasicTestTypes<T1, T2, T3> {}
		""";
	completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "** 	@param ", 0); // empty token
	assertResults("");
}

/**
 * tests Tests for camel case completion
 */
public void test080() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		String source =
			"""
			package javadoc.types.tags;
			/**
			 * Completion after:
			 * 	@see BTT
			 */
			public class BasicTestTypes {}
			""";
		completeInJavadoc("/Completion/src/javadoc/types/tags/BasicTestTypes.java", source, true, "BTT");
		assertResults("BasicTestTypes[TYPE_REF]{BasicTestTypes, javadoc.types.tags, Ljavadoc.types.tags.BasicTestTypes;, null, null, "+this.positions+ (R_DEFAULT + 17) +"}");
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
/**
 * @category Tests for filtered completion
 */
public void test100() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasi bla
		 */
		public class ZBasicTestTypes {}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasi",
			1,
			new int[]{});
	assertResults(
			"ZBasicTestTypes[TYPE_REF]{ZBasicTestTypes, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICUNR+"}\n" +
			"ZBasicTestTypes[JAVADOC_TYPE_REF]{{@link ZBasicTestTypes}, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICUNRIT+"}"
	);
}
public void test101() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasi bla
		 */
		public class ZBasicTestTypes {}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasi",
			1,
			new int[]{CompletionProposal.JAVADOC_TYPE_REF});
	assertResults(
			"ZBasicTestTypes[TYPE_REF]{ZBasicTestTypes, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void test102() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasi bla
		 */
		public class ZBasicTestTypes {}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasi",
			1,
			new int[]{CompletionProposal.TYPE_REF});
	assertResults(
			"ZBasicTestTypes[JAVADOC_TYPE_REF]{{@link ZBasicTestTypes}, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICUNRIT+"}"
	);
}
public void test103() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasicTestTypes#fo bla
		 */
		public class ZBasicTestTypes {
		  public void foo() {}
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasicTestTypes#fo",
			1,
			new int[]{});
	assertResults(
			"foo[JAVADOC_METHOD_REF]{{@link ZBasicTestTypes#foo()}, Ljavadoc.types.ZBasicTestTypes;, ()V, foo, null, "+this.positions+R_DRICNRNSIT+"}"
	);
}
public void test104() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasicTestTypes#fo bla
		 */
		public class ZBasicTestTypes {
		  public void foo() {}
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasicTestTypes#fo",
			1,
			new int[]{CompletionProposal.JAVADOC_METHOD_REF});
	assertResults(
			""
	);
}
public void test105() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasicTestTypes#fo bla
		 */
		public class ZBasicTestTypes {
		  public void foo() {}
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasicTestTypes#fo",
			1,
			new int[]{CompletionProposal.METHOD_REF});
	assertResults(
			"foo[JAVADOC_METHOD_REF]{{@link ZBasicTestTypes#foo()}, Ljavadoc.types.ZBasicTestTypes;, ()V, foo, null, "+this.positions+R_DRICNRNSIT+"}"
	);
}
public void test106() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasicTestTypes#fo bla
		 */
		public class ZBasicTestTypes {
		  public int foo;
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasicTestTypes#fo",
			1,
			new int[]{});
	assertResults(
			"foo[JAVADOC_FIELD_REF]{{@link ZBasicTestTypes#foo}, Ljavadoc.types.ZBasicTestTypes;, I, foo, null, "+this.positions+R_DRICNRNSIT+"}"
	);
}
public void test107() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasicTestTypes#fo bla
		 */
		public class ZBasicTestTypes {
		  public int foo;
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasicTestTypes#fo",
			1,
			new int[]{CompletionProposal.JAVADOC_FIELD_REF});
	assertResults(
			""
	);
}
public void test108() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla ZBasicTestTypes#fo bla
		 */
		public class ZBasicTestTypes {
		  public int foo;
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"ZBasicTestTypes#fo",
			1,
			new int[]{CompletionProposal.FIELD_REF});
	assertResults(
			"foo[JAVADOC_FIELD_REF]{{@link ZBasicTestTypes#foo}, Ljavadoc.types.ZBasicTestTypes;, I, foo, null, "+this.positions+R_DRICNRNSIT+"}"
	);
}
public void test109() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla javadoc.types.ZBasi bla
		 */
		public class ZBasicTestTypes {}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"javadoc.types.ZBasi",
			1,
			new int[]{});
	assertResults(
			"ZBasicTestTypes[TYPE_REF]{ZBasicTestTypes, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICNR+"}\n" +
			"ZBasicTestTypes[JAVADOC_TYPE_REF]{{@link ZBasicTestTypes}, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICNRIT+"}"
	);
}
public void test110() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla javadoc.types.ZBasi bla
		 */
		public class ZBasicTestTypes {}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"javadoc.types.ZBasi",
			1,
			new int[]{CompletionProposal.JAVADOC_TYPE_REF});
	assertResults(
			"ZBasicTestTypes[TYPE_REF]{ZBasicTestTypes, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICNR+"}"
	);
}
public void test111() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla javadoc.types.ZBasi bla
		 */
		public class ZBasicTestTypes {}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"javadoc.types.ZBasi",
			1,
			new int[]{CompletionProposal.TYPE_REF});
	assertResults(
			"ZBasicTestTypes[JAVADOC_TYPE_REF]{{@link ZBasicTestTypes}, javadoc.types, Ljavadoc.types.ZBasicTestTypes;, null, null, "+this.positions+R_DRICNRIT+"}"
	);
}
public void test112() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla javadoc.types.ZBasicTestTypes.Inn bla
		 */
		public class ZBasicTestTypes {
		  public class Inner {}
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"javadoc.types.ZBasicTestTypes.Inn",
			1,
			new int[]{});
	assertResults(
			"ZBasicTestTypes.Inner[TYPE_REF]{Inner, javadoc.types, Ljavadoc.types.ZBasicTestTypes$Inner;, null, null, "+this.positions+R_DRICNR+"}\n" +
			"ZBasicTestTypes.Inner[JAVADOC_TYPE_REF]{{@link Inner}, javadoc.types, Ljavadoc.types.ZBasicTestTypes$Inner;, null, null, "+this.positions+R_DRICNRIT+"}"
	);
}
public void test113() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla javadoc.types.ZBasicTestTypes.Inn bla
		 */
		public class ZBasicTestTypes {
		  public class Inner {}
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"javadoc.types.ZBasicTestTypes.Inn",
			1,
			new int[]{CompletionProposal.JAVADOC_TYPE_REF});
	assertResults(
			"ZBasicTestTypes.Inner[TYPE_REF]{Inner, javadoc.types, Ljavadoc.types.ZBasicTestTypes$Inner;, null, null, "+this.positions+R_DRICNR+"}"
	);
}
public void test114() throws JavaModelException {
	String source =
		"""
		package javadoc.types;
		/**
		 * Completion after:
		 * 	bla javadoc.types.ZBasicTestTypes.Inn bla
		 */
		public class ZBasicTestTypes {
		  public class Inner {}
		}
		""";
	completeInJavadoc(
			"/Completion/src/javadoc/types/ZBasicTestTypes.java",
			source,
			true,
			"javadoc.types.ZBasicTestTypes.Inn",
			1,
			new int[]{CompletionProposal.TYPE_REF});
	assertResults(
			"ZBasicTestTypes.Inner[JAVADOC_TYPE_REF]{{@link Inner}, javadoc.types, Ljavadoc.types.ZBasicTestTypes$Inner;, null, null, "+this.positions+R_DRICNRIT+"}"
	);
}
}
