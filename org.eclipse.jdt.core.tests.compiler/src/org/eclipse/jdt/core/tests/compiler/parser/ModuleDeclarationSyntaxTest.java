/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.IOException;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class ModuleDeclarationSyntaxTest extends AbstractSyntaxTreeTest {

	public ModuleDeclarationSyntaxTest(String name, String referenceCompiler,
			String referenceCompilerTestsScratchArea) {
		super(name, referenceCompiler, referenceCompilerTestsScratchArea);
	}
	public static Class<?> testClass() {
		return ModuleDeclarationSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	static {
		//		TESTS_NAMES = new String[] { "test0009" };
		//		TESTS_NUMBERS = new int[] { 133, 134, 135 };
	}
	public ModuleDeclarationSyntaxTest(String testName){
		super(testName, null, null);
	}
	public void test0001() throws IOException {
		String source =
				"module com.greetings {\n" +
				"}\n";
		String expectedUnitToString =
				"module com.greetings {\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0002() throws IOException {
		String source =
				"""
			module com.greetings {
			requires org.astro;\
			}
			""";
		String expectedUnitToString =
				"""
			module com.greetings {
			  requires org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0003() throws IOException {
		String source =
				"""
			module org.astro {
			    exports org.astro;
			}
			""";
		String expectedUnitToString =
				"""
			module org.astro {
			  exports org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0004() throws IOException {
		String source =
				"""
			module org.astro {
			    exports org.astro to com.greetings, com.example1, com.example2;
			}
			""";
		String expectedUnitToString =
				"""
			module org.astro {
			  exports org.astro to com.greetings, com.example1, com.example2;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0005() throws IOException {
		String source =
				"""
			module com.socket {
			    exports com.socket;
			    exports com.socket.spi;
			    uses com.socket.spi.NetworkSocketProvider;
			}
			""";
		String expectedUnitToString =
				"""
			module com.socket {
			  exports com.socket;
			  exports com.socket.spi;
			  uses com.socket.spi.NetworkSocketProvider;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0006() throws IOException {
		String source =
				"""
			module org.fastsocket {
			    requires com.socket;
			    provides com.socket.spi.NetworkSocketProvider
			      with org.fastsocket.FastNetworkSocketProvider;
			}
			""";
		String expectedUnitToString =
				"""
			module org.fastsocket {
			  requires com.socket;
			  provides com.socket.spi.NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0007() throws IOException {
		String source =
				"""
			module org.fastsocket {
			    requires com.socket;
			    provides com.socket.spi.NetworkSocketProvider;
			}
			""";
		String expectedErrorString =
				"""
			----------
			1. ERROR in module-info (at line 3)
				provides com.socket.spi.NetworkSocketProvider;
				                       ^
			Syntax error on token ".", with expected
			----------
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), expectedErrorString, "module-info", null, null, options);
	}
	public void test0008() throws IOException {
		String source =
				"""
			module @Marker com.greetings {
				requires org.astro;\
			}
			""";
		String errorMsg = """
				----------
				1. ERROR in module-info (at line 1)
					module @Marker com.greetings {
					^^^^^^
				Syntax error on token "module", delete this token
				----------
				2. ERROR in module-info (at line 1)
					module @Marker com.greetings {
					       ^^^^^^^^^^^^^^^^^^^^^
				Syntax error on tokens, ModuleHeader expected instead
				----------
				""";

		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0009() throws IOException {
		String source =
				"""
			module com.greetings {
				requires @Marker org.astro;
			}
			""";
		String errorMsg = """
				----------
				1. ERROR in module-info (at line 1)
					module com.greetings {
					requires @Marker org.astro;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in module-info (at line 2)
					requires @Marker org.astro;
					                    ^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in module-info (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0010() throws IOException {
		String source =
				"""
			module com.greetings {
				requires private org.astro;
			}
			""";
		String errorMsg =
				"""
			----------
			1. ERROR in module-info (at line 2)
				requires private org.astro;
				         ^^^^^^^
			Syntax error on token "private", delete this token
			----------
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0011() throws IOException {
		String source =
				"""
			module com.greetings {
				exports @Marker com.greetings;
			}
			""";
		String errorMsg = """
				----------
				1. ERROR in module-info (at line 1)
					module com.greetings {
					exports @Marker com.greetings;
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in module-info (at line 2)
					exports @Marker com.greetings;
					                   ^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in module-info (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0012() throws IOException {
		String source =
				"""
			module com.greetings {
				exports com.greetings to @Marker org.astro;
			}
			""";
		String errorMsg =
				"""
			----------
			1. ERROR in module-info (at line 2)
				exports com.greetings to @Marker org.astro;
				                         ^^^^^^^
			Syntax error on tokens, delete these tokens
			----------
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0013() throws IOException {
		String source =
				"""
			module com.greetings {
				uses @Marker org.astro.World;
			}
			""";
		String errorMsg =
				"""
			----------
			1. ERROR in module-info (at line 2)
				uses @Marker org.astro.World;
				     ^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0014() throws IOException {
		String source =
				"""
			module com.greetings {
				provides @Marker org.astro.World with @Marker com.greetings.Main;
			}
			""";
		String errorMsg =
				"""
			----------
			1. ERROR in module-info (at line 2)
				provides @Marker org.astro.World with @Marker com.greetings.Main;
				         ^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			2. ERROR in module-info (at line 2)
				provides @Marker org.astro.World with @Marker com.greetings.Main;
				                                      ^^^^^^^
			Syntax error, type annotations are illegal here
			----------
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), errorMsg, "module-info", null, null, options);
	}
	public void test0015() throws IOException {
		String source =
				"""
			module com.greetings {
			requires transitive org.astro;\
			}
			""";
		String expectedUnitToString =
				"""
			module com.greetings {
			  requires transitive org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0016() throws IOException {
		String source =
				"""
			module com.greetings {
			requires static org.astro;\
			}
			""";
		String expectedUnitToString =
				"""
			module com.greetings {
			  requires static org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0017() throws IOException {
		String source =
				"""
			module com.greetings {
			requires transitive static org.astro;\
			}
			""";
		String expectedUnitToString =
				"""
			module com.greetings {
			  requires transitive static org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0018() throws IOException {
		String source =
				"""
			import com.socket.spi.NetworkSocketProvider;
			module org.fastsocket {
			    requires com.socket;
			    provides NetworkSocketProvider
			      with org.fastsocket.FastNetworkSocketProvider;
			}
			""";
		String expectedUnitToString =
				"""
			import com.socket.spi.NetworkSocketProvider;
			module org.fastsocket {
			  requires com.socket;
			  provides NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0019() throws IOException {
		String source =
				"""
			import com.socket.spi.*;
			module org.fastsocket {
			    requires com.socket;
			    provides NetworkSocketProvider
			      with org.fastsocket.FastNetworkSocketProvider;
			}
			""";
		String expectedUnitToString =
				"""
			import com.socket.spi.*;
			module org.fastsocket {
			  requires com.socket;
			  provides NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0020() throws IOException {
		String source =
				"""
			open module com.greetings {
			requires transitive static org.astro;\
			}
			""";
		String expectedUnitToString =
				"""
			open module com.greetings {
			  requires transitive static org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0021() throws IOException {
		String source =
				"""
			module org.fastsocket {
			    requires com.socket;
			    provides com.socket.spi.NetworkSocketProvider
			      with org.fastsocket.FastNetworkSocketProvider, org.fastSocket.SlowNetworkSocketProvider;
			}
			""";
		String expectedUnitToString =
				"""
			module org.fastsocket {
			  requires com.socket;
			  provides com.socket.spi.NetworkSocketProvider with org.fastsocket.FastNetworkSocketProvider, org.fastSocket.SlowNetworkSocketProvider;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0022() throws IOException {
		String source =
				"""
			module org.astro {
			    opens org.astro;
			}
			""";
		String expectedUnitToString =
				"""
			module org.astro {
			  opens org.astro;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0023() throws IOException {
		String source =
				"""
			module org.astro {
			    opens org.astro to com.greetings, com.example1, com.example2;
			}
			""";
		String expectedUnitToString =
				"""
			module org.astro {
			  opens org.astro to com.greetings, com.example1, com.example2;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0024() throws IOException {
		String source =
				"""
			module org.astro {
			    exports org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		String expectedUnitToString =
				"""
			module org.astro {
			  exports org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0025() throws IOException {
		String source =
				"""
			@Foo
			module org.astro {
			    exports org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		String expectedUnitToString =
				"""
			@Foo
			module org.astro {
			  exports org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0026() throws IOException {
		String source =
				"""
			@Foo
			open module org.astro {
			    exports org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		String expectedUnitToString =
				"""
			@Foo
			open module org.astro {
			  exports org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}

	public void test0027() throws IOException {
		String source =
				"""
			@Foo @Bar(x = 2) @Baz("true")
			open module org.astro {
			    exports org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro to com.greetings, com.example1, com.example2;
			    opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		String expectedUnitToString =
				"""
			@Foo @Bar(x = 2) @Baz("true")
			open module org.astro {
			  exports org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro to com.greetings, com.example1, com.example2;
			  opens org.astro.galaxy to com.greetings, com.example1, com.example2;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}

	public void testBug518626() throws IOException {
		String source =
				"""
			module module.test {
			    provides X with Y;
			}
			""";
		String expectedUnitToString =
				"""
			module module.test {
			  provides X with Y;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void testbug488541() throws IOException {
		String source =
				"""
			module module {
			   requires requires;
			   exports to to exports;
			   uses module;
			   provides uses with to;
			}
			""";
		String expectedUnitToString =
				"""
			module module {
			  requires requires;
			  exports to to exports;
			  uses module;
			  provides uses with to;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void testbug488541a() throws IOException {
		String source =
			"""
			import module.pack1.exports.pack2;
			import module.open.pack1.opens.pack2;
			@open @module(true)
			open module module.module.module {
			   requires static transitive requires;
			   requires transitive static transitive;
			   exports to to exports;
			   opens module.to.pack1 to to.exports;
			   uses module;
			   provides uses with to;
			}
			""";
		String expectedUnitToString =
			"""
			import module.pack1.exports.pack2;
			import module.open.pack1.opens.pack2;
			@open @module(true)
			open module module.module.module {
			  requires transitive static requires;
			  requires transitive static transitive;
			  exports to to exports;
			  opens module.to.pack1 to to.exports;
			  uses module;
			  provides uses with to;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void testbug488541b() throws IOException {
		String source =
				"""
			module module {
			   requires requires;
			   exports to to exports, module;
			   uses module;
			   provides uses with to, open, module;
			}
			""";
		String expectedUnitToString =
				"""
			module module {
			  requires requires;
			  exports to to exports, module;
			  uses module;
			  provides uses with to, open, module;
			}
			""";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK9;
		options.sourceLevel = ClassFileConstants.JDK9;
		options.targetJDK = ClassFileConstants.JDK9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}

}
