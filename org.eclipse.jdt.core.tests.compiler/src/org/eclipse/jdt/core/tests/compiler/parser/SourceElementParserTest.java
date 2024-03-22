/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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

import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SourceElementParserTest extends AbstractCompilerTest implements ISourceElementRequestor {
	private SourceType currentType;
	private SourceMethod currentMethod;
	private SourceField currentField;
	private SourceField currentRecordComp;
	private SourceInitializer currentInitializer;
	private char[] source;
	private SourcePackage currentPackage;
	private SourceImport[] currentImports;
	private int numberOfImports;
public SourceElementParserTest(String testName) {
	super(testName);
}
public SourceElementParserTest(String testName, char[] source) {
	super(testName);
	this.source = source;
}
static {
//	TESTS_NUMBERS = new int[] { 81 };
//	TESTS_NAMES = new String[] {"test63"};
}
public static Test suite() {
	return buildAllCompliancesTestSuite(SourceElementParserTest.class);
}
/**
 * acceptAnnotationTypeReference method comment.
 */
public void acceptAnnotationTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptAnnotationTypeReference method comment.
 */
public void acceptAnnotationTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptConstructorReference method comment.
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {
	if (this.currentMethod == null) {
		if (this.currentType != null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(typeName).append("(").append(argCount).append(")\n");
			this.currentType.setDefaultConstructor(buffer.toString());
		}
		return;
	}
	if (this.currentMethod.isConstructor()) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(typeName).append("(").append(argCount).append(")\n");
		this.currentMethod.setExplicitConstructorCall(buffer.toString());
	} else {
		if (this.currentType != null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(typeName).append("(").append(argCount).append(")\n");
			this.currentType.setDefaultConstructor(buffer.toString());
		}
	}
}
/**
 * acceptFieldReference method comment.
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * acceptImport method comment.
 */
public void acceptImport(
	int declarationStart,
	int declarationEnd,
	int nameStart,
	int nameEnd,
	char[][] tokens,
	boolean onDemand,
	int modifiers) {

	addImport(
		new SourceImport(declarationStart, declarationEnd, CharOperation.concatWith(tokens, '.'), onDemand, modifiers, this.source));
}
/**
 * acceptLineSeparatorPositions method comment.
 */
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * acceptMethodReference method comment.
 */
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {}
/**
 * acceptPackage method comment.
 */
public void acceptPackage(ImportReference importReference) {

	this.currentPackage =
		new SourcePackage(importReference.declarationSourceStart, importReference.declarationSourceEnd, CharOperation.concatWith(importReference.getImportName(), '.'), this.source);
}
/**
 * acceptProblem method comment.
 */
public void acceptProblem(CategorizedProblem problem) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {}
protected void addImport(SourceImport sourceImport) {
	if (this.currentImports == null) {
		this.currentImports = new SourceImport[4];
	}

	if (this.numberOfImports == this.currentImports.length) {
		System.arraycopy(
			this.currentImports,
			0,
			this.currentImports = new SourceImport[this.numberOfImports * 2],
			0,
			this.numberOfImports);
	}
	this.currentImports[this.numberOfImports++] = sourceImport;
}
public void dietParse(String s, String testName) {
	this.dietParse(s, testName, false);
}
public void dietParse(String s, String testName, boolean recordLocalDeclaration) {

	this.source = s.toCharArray();
	reset();
	SourceElementParser parser =
		new SourceElementParser(
			this,
			new DefaultProblemFactory(Locale.getDefault()),
			new CompilerOptions(getCompilerOptions()),
			recordLocalDeclaration/*don't record local declarations*/,
			true/*optimize string literals*/);

	ICompilationUnit sourceUnit = new CompilationUnit(this.source, testName, null);

	parser.parseCompilationUnit(sourceUnit, false, null);

}
public static String displayModifiers(int modifiers) {
	StringBuilder buffer = new StringBuilder();

	if ((modifiers & ClassFileConstants.AccPublic) != 0)
		buffer.append("public ");
	if ((modifiers & ClassFileConstants.AccProtected) != 0)
		buffer.append("protected ");
	if ((modifiers & ClassFileConstants.AccPrivate) != 0)
		buffer.append("private ");
	if ((modifiers & ClassFileConstants.AccFinal) != 0)
		buffer.append("final ");
	if ((modifiers & ClassFileConstants.AccStatic) != 0)
		buffer.append("static ");
	if ((modifiers & ClassFileConstants.AccAbstract) != 0)
		buffer.append("abstract ");
	if ((modifiers & ClassFileConstants.AccNative) != 0)
		buffer.append("native ");
	if ((modifiers & ClassFileConstants.AccSynchronized) != 0)
		buffer.append("synchronized ");
	return buffer.toString();
}
public void enterType(TypeInfo typeInfo) {
	if (this.currentType == null) {
		// top level type
		this.currentType =
			new SourceType(
				null,
				typeInfo.declarationStart,
				typeInfo.modifiers,
				typeInfo.name,
				typeInfo.nameSourceStart,
				typeInfo.nameSourceEnd,
				typeInfo.superclass,
				typeInfo.superinterfaces,
				this.source);
		this.currentType.setPackage(this.currentPackage);
		setImports();
	} else {
		// member type
		SourceType memberType;
		this.currentType.addMemberType(
			memberType =
				new SourceType(
					this.currentType.getName(),
					typeInfo.declarationStart,
					typeInfo.modifiers,
					typeInfo.name,
					typeInfo.nameSourceStart,
					typeInfo.nameSourceEnd,
					typeInfo.superclass,
					typeInfo.superinterfaces,
					this.source));
		memberType.parent = this.currentType;
		this.currentType = memberType;
	}
	if (typeInfo.typeParameters != null) {
		for (int i = 0, length = typeInfo.typeParameters.length; i < length; i++) {
			TypeParameterInfo typeParameterInfo = typeInfo.typeParameters[i];
			addTypeParameterToType(typeParameterInfo);
		}
	}
}
public void enterCompilationUnit() {
}
public void enterConstructor(MethodInfo methodInfo) {
	enterAbtractMethod(methodInfo);
}
public void enterField(FieldInfo fieldInfo) {
	if (fieldInfo.isRecordComponent) {
		this.currentType.addRecordComponent(
				this.currentRecordComp =
					new SourceField(
							fieldInfo.declarationStart,
							fieldInfo.modifiers,
							fieldInfo.type,
							fieldInfo.name,
							fieldInfo.nameSourceStart,
							fieldInfo.nameSourceEnd,
							this.source));
	} else {
		this.currentType.addField(
				this.currentField =
				new SourceField(
						fieldInfo.declarationStart,
						fieldInfo.modifiers,
						fieldInfo.type,
						fieldInfo.name,
						fieldInfo.nameSourceStart,
						fieldInfo.nameSourceEnd,
						this.source));
	}
}
public void enterInitializer(int declarationSourceStart, int modifiers) {
	this.currentType.addField(
		this.currentInitializer = new SourceInitializer(
			declarationSourceStart,
			modifiers));
}
public void exitInitializer(int declarationSourceEnd) {
	this.currentInitializer.setDeclarationSourceEnd(declarationSourceEnd);
}
public void enterMethod(MethodInfo methodInfo) {
	enterAbtractMethod(methodInfo);
}
protected void enterAbtractMethod(MethodInfo methodInfo) {
	this.currentType.addMethod(
		this.currentMethod =
			new SourceMethod(
				methodInfo.declarationStart,
				methodInfo.modifiers,
				methodInfo.returnType,
				methodInfo.name, // null for constructors
				methodInfo.nameSourceStart,
				methodInfo.nameSourceEnd,
				methodInfo.parameterTypes,
				methodInfo.parameterNames,
				methodInfo.exceptionTypes,
				this.source));

	if (methodInfo.typeParameters != null) {
		for (int i = 0, length = methodInfo.typeParameters.length; i < length; i++) {
			TypeParameterInfo typeParameterInfo = methodInfo.typeParameters[i];
			addTypeParameterToMethod(typeParameterInfo);
		}
	}
}
public void addTypeParameterToMethod(TypeParameterInfo typeParameterInfo) {
	if (this.currentMethod.typeParameterNames == null) {
		this.currentMethod.typeParameterNames = new char[][] {typeParameterInfo.name};
		this.currentMethod.typeParameterBounds = new char[][][] {typeParameterInfo.bounds};
	} else {
		int length = this.currentMethod.typeParameterNames.length;
		System.arraycopy(this.currentMethod.typeParameterNames, 0, this.currentMethod.typeParameterNames = new char[length+1][],0, length);
		this.currentMethod.typeParameterNames[length] = typeParameterInfo.name;
		System.arraycopy(this.currentMethod.typeParameterBounds, 0, this.currentMethod.typeParameterBounds = new char[length+1][][],0, length);
		this.currentMethod.typeParameterBounds[length] = typeParameterInfo.bounds;
	}
}
public void addTypeParameterToType(TypeParameterInfo typeParameterInfo) {
	if (this.currentType.typeParameterNames == null) {
		this.currentType.typeParameterNames = new char[][] {typeParameterInfo.name};
		this.currentType.typeParameterBounds = new char[][][] {typeParameterInfo.bounds};
	} else {
		int length = this.currentType.typeParameterNames.length;
		System.arraycopy(this.currentType.typeParameterNames, 0, this.currentType.typeParameterNames = new char[length+1][],0, length);
		this.currentMethod.typeParameterNames[length] = typeParameterInfo.name;
		System.arraycopy(this.currentType.typeParameterBounds, 0, this.currentType.typeParameterBounds = new char[length+1][][],0, length);
		this.currentType.typeParameterBounds[length] = typeParameterInfo.bounds;
	}
}
public void exitType(int declarationEnd) {
	this.currentType.setDeclarationSourceEnd(declarationEnd);
	if (this.currentType.parent != null) {
		this.currentType = this.currentType.parent;
	}
}
public void exitCompilationUnit(int declarationEnd) {}
public void exitConstructor(int declarationEnd) {
	exitAbstractMethod(declarationEnd);
}
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {
	this.currentField.setDeclarationSourceEnd(declarationEnd);
}
public void exitRecordComponent(int declarationEnd, int declarationSourceEnd) {
	this.currentRecordComp.setDeclarationSourceEnd(declarationEnd);
}
public void exitMethod(int declarationEnd, Expression defaultValue) {
	exitAbstractMethod(declarationEnd);
}
protected void exitAbstractMethod(int declarationEnd) {
	this.currentMethod.setDeclarationSourceEnd(declarationEnd);
}
public void fullParse(String s, String testName) {
	this.fullParse(s, testName, false);
}
public void fullParse(String s, String testName, Map options) {
	this.fullParse(s, testName, false, options);
}
public void fullParse(String s, String testName, boolean recordLocalDeclaration, Map options) {
	this.source = s.toCharArray();
	reset();
	SourceElementParser parser =
		new SourceElementParser(
			this, new DefaultProblemFactory(Locale.getDefault()),
			new CompilerOptions(options),
			recordLocalDeclaration/*don't record local declarations*/,
			true/*optimize string literals*/);

	ICompilationUnit sourceUnit = new CompilationUnit(this.source, testName, null);

	parser.parseCompilationUnit(sourceUnit, true, null);
}
public void fullParse(String s, String testName, boolean recordLocalDeclaration) {
	this.fullParse(s, testName, recordLocalDeclaration, getCompilerOptions());
}
public void reset() {
	this.currentType = null;
	this.currentMethod = null;
	this.currentField = null;
	this.currentPackage = null;
	this.currentImports = null;
	this.numberOfImports = 0;
}
public void setImports() {
	if (this.currentImports == null)
		return;
	if (this.numberOfImports != this.currentImports.length) {
		System.arraycopy(
			this.currentImports,
			0,
			this.currentImports = new SourceImport[this.numberOfImports],
			0,
			this.numberOfImports);
	}
	this.currentType.setImports(this.currentImports);
}
public void test01() {

	String s =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		
		public class X {
		void foo() {
		System.out.println();
		
		public int h;
		public int[] i = { 0, 1 };
		
		int bar\
		\\\
		u0065(){
		void truc(){
		}
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			public int[] i;
			java.lang.Object(0)
			void foo() {}
			int bare() {}
			void truc() {}
		}""";

	String testName = "test01: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		52,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		178,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 105, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 117, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 119, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 144, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 69, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 103, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bare", 147, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bare", 163, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 164, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 177, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bare", """
		bar\
		\\\
		u0065""", methods[1].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test02() {

	String s =
			"""
		/** javadoc comment */
		public class X {
		}
		""";

	String expectedUnitToString =
			"""
		public class X {
			java.lang.Object(0)
		}""";

	String testName = "test02: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		40,
		this.currentType.getDeclarationSourceEnd());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test03() {

	String s =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		
		public class X {
		void foo() {
		System.out.println();
		
		public int h;
		public int[] i = { 0, 1 };
		
		int bar\
		\\\
		u0065(){
		void truc(){
		}
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			public int[] i;
			void foo() {}
			int bare() {}
			void truc() {}
		}""";

	String testName = "test03: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		52,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		178,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 105, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 117, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 119, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 144, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 69, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 103, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 147, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 163, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 164, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 177, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bare", """
		bar\
		\\\
		u0065""", methods[1].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test04() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L {							\t
					void baz(){}					\t
				}									\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			public int[] i;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";
	String testName = "test04: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		372,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 248, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 260, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 271, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 296, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 115, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 222, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 317, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 337, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 339, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 360, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test05() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L {							\t
					void baz(){}					\t
				}									\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			public int[] i;
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test05: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		372,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 248, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 260, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 271, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 296, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 115, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 222, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 317, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 337, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 339, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 360, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test06() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
		 X x;									\t
		 Object a, b = null;						\t
			void foo() {							\t
				System.out.println();				\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			X x;
			Object a;
			Object b;
			public int h;
			public int[] i;
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test06: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		347,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 5, fields.length);

	assertEquals("Invalid declaration source start for field x", 115, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 118, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field a", 131, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field a", 139, fields[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field b", 131, fields[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field b", 149, fields[2].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field h", 223, fields[3].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 235, fields[3].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 246, fields[4].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 271, fields[4].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 159, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 221, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 292, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 312, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 314, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 335, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test07() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
		 X x;									\t
		 Object a, b = null;						\t
			void foo() {							\t
				System.out.println();				\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			X x;
			Object a;
			Object b;
			public int h;
			public int[] i;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test07: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		347,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 5, fields.length);

	assertEquals("Invalid declaration source start for field xh", 115, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 118, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field a", 131, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field a", 139, fields[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field b", 131, fields[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field b", 149, fields[2].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field h", 223, fields[3].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 235, fields[3].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 246, fields[4].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 271, fields[4].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 159, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 221, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 292, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 312, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 314, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 335, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test08() {

	String s =
		"""
		public class X {								\t
			void foo() {							\t
				System.out.println();				\t
		 	void baz(){}						\t
		 }										\t
													\t
			void bar(){								\t
		 }										\t
			void truc(){							\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test08: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		198,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 100, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 127, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 149, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 163, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 185, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test09() {

	String s =
		"""
		public class X {								\t
			void foo() {							\t
				System.out.println();				\t
		 	void baz(){}						\t
		 }										\t
													\t
			void bar(){								\t
		 }										\t
			void truc(){							\t
		 }										\t
		}										\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test09: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		198,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 100, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 127, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 149, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 163, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 185, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());


	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test10() {

	String s =
		"""
		public class X {								\t
			void foo() {							\t
				System.out.println();				\t
		 	void baz(){}						\t
		 }										\t
			/** comment                                \s
		  *                                         \s
		  *                                         \s
		  */                                        \s
		                                            \s
			void bar(){								\t
		 }										\t
			void truc(){							\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test10: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		415,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 100, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 114, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 366, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 380, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 402, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test11() {

	String s =
		"""
		public class X {								\t
			void foo() {							\t
				System.out.println();				\t
		 	void baz(){}  						\t
			/** comment                                \s
		  *                                         \s
		  *                                         \s
		  */                                        \s
		  int[][] j[] = null, k; // comment         \s
		                                            \s
			void bar(){								\t
		 }										\t
			void truc(){							\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			 """
		public class X {
			int[][][] j;
			int[][] k;
			java.lang.Object(0)
			void foo() {}
			void baz() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test11: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		449,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);

	assertEquals("Invalid declaration source start for field j", 102, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field j", 305, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field k", 102, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field k", 308, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 4, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 76, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 79, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 90, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 378, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 400, methods[2].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 414, methods[3].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 436, methods[3].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[1].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[2].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[3].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test12() {

	String s =
			"""
		import java.util.Enumeration;
		import java.util.Hashtable;\
		
		/** comment */
		public class A2 {
			void foo() {
				System.out.println();
		 	void baz(){}
			/** comment
		  *
		  *
		  */
		  static { } // comment
		 \s
		
			void bar(){
		 }
			void truc(){
		 }
		}
		""";

	String expectedUnitToString =
			"""
		import java.util.Enumeration;
		import java.util.Hashtable;
		public class A2 {
			static {}
			java.lang.Object(0)
			void foo() {}
			void baz() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test12: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		58,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		231,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 1, fields.length);

	assertEquals("Invalid declaration source start for initializer", 145, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 181, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 4, methods.length);

	assertEquals("Invalid declaration source start for method foo", 92, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 128, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 131, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 142, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 183, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 212, methods[2].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 215, methods[3].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 229, methods[3].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[1].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[2].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[3].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test13() {

	String s =
			"""
		import java.util.Enumeration;
		import java.util.Hashtable;
		
		public class A2 {
			void foo() {
				System.out.println();
		 	void baz(){}
		  static { }
		 \s
		
			void bar(){
		 }
			void truc(){
		 }
		}
		""";

	String expectedUnitToString =
			"""
		import java.util.Enumeration;
		import java.util.Hashtable;
		public class A2 {
			static {}
			java.lang.Object(0)
			void foo() {}
			void baz() {}
			void bar() {}
			void truc() {}
		}""";
	String testName = "test13: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		59,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		180,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);

	assertEquals(" invalid fields length", 1, fields.length);

	assertEquals("Invalid declaration source start for initializer", 132, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 141, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 4, methods.length);

	assertEquals("Invalid declaration source start for method foo", 78, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 114, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 117, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 128, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 148, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 161, methods[2].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 164, methods[3].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 178, methods[3].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[1].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[2].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[3].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test14() {

	String s =
			"""
		import java.util.Enumeration;
		import java.util.Hashtable;
		
		public class A2 {
			void foo() {
				System.out.println();
		 	void baz(){}
		  static { }
		 }
		
			void bar(){
		 }
			void truc(){
		 }
		}
		""";

	String expectedUnitToString =
			"""
		import java.util.Enumeration;
		import java.util.Hashtable;
		public class A2 {
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test14: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		59,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		180,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("Invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 78, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 144, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 148, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 161, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 164, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 178, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test15() {

	String s =
			"""
		public class X {							\t
		 class Y {								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			class Y {
				public int h;
				public int[] i;
				java.lang.Object(0)
				void foo() {}
				void bar() {}
				void baz() {}
			}
			java.lang.Object(0)
		}""";

	String testName = "test15: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		227,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" invalid members length ", 1, members.length);

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 117, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 129, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 140, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 163, fields[1].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 48, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 103, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 171, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 191, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 193, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 215, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[2].getActualName());

	SourceType member = members[0];
	assertEquals(
		"Invalid class declarationSourceStart ",
		26,
		member.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		227,
		member.getDeclarationSourceEnd());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test16() {

	String s =
			"""
		public class X {							\t
		 class Y {								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			class Y {
				public int h;
				public int[] i;
				void foo() {}
				void bar() {}
				void baz() {}
			}
		}""";

	String testName = "test16: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		227,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" invalid members length ", 1, members.length);

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 117, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 129, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 140, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 163, fields[1].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid methods length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 48, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 103, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 171, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 191, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 193, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 215, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[2].getActualName());

	SourceType member = members[0];
	assertEquals(
		"Invalid class declarationSourceStart ",
		26,
		member.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		227,
		member.getDeclarationSourceEnd());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test17() {

	String s =
			"""
		public class X {							\t
		 class Y {								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			class Y {
				java.lang.Object(0)
				void foo() {}
			}
			public int h;
			public int[] i;
			java.lang.Object(0)
			void bar() {}
			void baz() {}
		}""";

	String testName = "test17: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		241,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 131, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 143, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 154, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 177, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 2, methods.length);

	assertEquals("Invalid declaration source start for method bar", 185, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 205, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 207, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 229, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method bar", "bar", methods[0].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" invalid members length ", 1, members.length);

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields == null);

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid methods length ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 48, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 103, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType member = members[0];
	assertEquals(
		"Invalid class declarationSourceStart ",
		26,
		member.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		117,
		member.getDeclarationSourceEnd());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test18() {

	String s =
			"""
		public class X {							\t
		 class Y {								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			class Y {
				void foo() {}
			}
			public int h;
			public int[] i;
			void bar() {}
			void baz() {}
		}""";

	String testName = "test18: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		241,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for field h", 131, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 143, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 154, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 177, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 2, methods.length);

	assertEquals("Invalid declaration source start for method bar", 185, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 205, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 207, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 229, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method bar", "bar", methods[0].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" invalid members length ", 1, members.length);

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields == null);

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid methods length ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 48, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 103, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType member = members[0];
	assertEquals(
		"Invalid class declarationSourceStart ",
		26,
		member.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		117,
		member.getDeclarationSourceEnd());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test19() {

	String s =
			"""
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
		 }										\t
		}										\t
			void bar(){								\t
		  int x;									\t
			void baz(){								\t
		 }										\t
		 int y;									\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			{}
			int y;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void baz() {}
		}""";

	String testName = "test19: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		197,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 2, fields.length);

	assertEquals("Invalid declaration source start for initializer", 90, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 90, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 181, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 186, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 26, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 77, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 104, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 143, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 145, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 167, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test20() {

	String s =
			"""
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
		 }										\t
		}										\t
			void bar(){								\t
		 public int x;							\t
			void baz(){								\t
		 }										\t
		 int y;									\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			{}
			public int x;
			int y;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void baz() {}
		}""";

	String testName = "test20: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		201,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 3, fields.length);

	assertEquals("Invalid declaration source start for initializer", 90, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 90, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field x", 126, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 138, fields[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 185, fields[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 190, fields[2].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 26, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 77, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 104, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 124, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 149, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 171, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test21() {

	String s =
			"""
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
		 }										\t
		}										\t
			void bar(){								\t
		 public int x;							\t
			void baz(){								\t
		 }										\t
		 int y;									\t
		""";


	String expectedUnitToString =
			"""
		public class X {
			{}
			public int x;
			int y;
			void foo() {}
			void bar() {}
			void baz() {}
		}""";

	String testName = "test21: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		201,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" invalid fields length ", 3, fields.length);

	assertEquals("Invalid declaration source start for initializer", 90, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 90, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field x", 126, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 138, fields[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 185, fields[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 190, fields[2].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 26, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 77, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 104, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 124, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method baz", 149, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method baz", 171, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method baz", "baz", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test22() {

	String s =
			"""
		public class X extends {					\t
			void foo() {							\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			java.lang.Object(0)
			void foo() {}
		}""";

	String testName = "test22: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		67,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 32, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 54, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test23() {

	String s =
			"""
		public class X extends Thread {			\t
			void foo() throws						\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X extends Thread {
			Thread(0)
			void foo() {}
			void bar() {}
		}""";

	String testName = "test23: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		98,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 37, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 61, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 63, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 85, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test24() {

	String s =
			"""
		public class X implements 					\t
			void foo() 								\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			java.lang.Object(0)
			void foo() {}
			void bar() {}
		}""";

	String testName = "test24: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		91,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 34, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 54, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 56, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 78, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test25() {

	String s =
			"""
		public class X implements Y,				\t
			void foo() 								\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X implements Y, {
			java.lang.Object(0)
			void foo() {}
			void bar() {}
		}""";

	String testName = "test25: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		92,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("contains superinterfaces " , this.currentType.getInterfaceNames() != null);
	assertEquals(" invalid superinterfaces length ", 1, this.currentType.getInterfaceNames().length);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" invalid fields length ", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 35, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 55, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 57, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 79, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test26() {

	String s =
			"""
		public class X implements 					\t
		 class Y { 								\t
			 void bar() 							\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			class Y {
				java.lang.Object(0)
				void bar() {}
			}
			java.lang.Object(0)
		}""";

	String testName = "test26: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		102,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		34,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		90,
		members[0].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method bar", 56, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 77, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method bar", "bar", methods[0].getActualName());

	members = members[0].getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test27() {

	String s =
		"""
		public class X 		 					\t
		 fieldX;									\t
		 class Y { 								\t
			 void bar() 							\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			class Y {
				java.lang.Object(0)
				void bar() {}
			}
			java.lang.Object(0)
		}""";

	String testName = "test27: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		113,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		45,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		101,
		members[0].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method bar", 67, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 88, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method bar", "bar", methods[0].getActualName());

	members = members[0].getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test28() {

	String s =
			"""
		public class X 		 					\t
		 fieldX;									\t
		 class Y  								\t
		 }										\t
		}										\t
		""";

	String expectedUnitToString =
			"""
		public class X {
			class Y {
				java.lang.Object(0)
			}
			java.lang.Object(0)
		}""";

	String testName = "test28: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		78,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		45,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		65,
		members[0].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods == null);

	members = members[0].getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test29() {

	String s =
		"""
		package a;									\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L extends {					\t
					public int l;					\t
					void baz(){}					\t
				}									\t
													\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test29: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		88,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		357,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field h", 276, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 288, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains 3 methods ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 114, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 250, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 312, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 332, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 334, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 355, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test30() {

	String s =
		"""
		package a;									\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L extends {					\t
					public int l;					\t
					void baz(){}					\t
				}									\t
													\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test30: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		88,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		357,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field h", 276, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 288, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains 3 methods ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 114, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 250, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 312, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 332, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 334, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 355, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test31() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() {}					\t
				}.baz();							\t
													\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test31: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		334,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field h", 253, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 265, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains 3 methods ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 115, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 251, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 289, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 309, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 311, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 332, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test32() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() {}					\t
													\t
				public int h;						\t
													\t
				void bar(){							\t
				void truc(){						\t
		}\t
		""";


	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			java.lang.Object(0)
			void foo() {}
		}""";

	String testName = "test32: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		315,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 115, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 315, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test33() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() {}					\t
													\t
				public int h;						\t
													\t
				void bar(){							\t
				void truc(){						\t
		}\t
		""";


	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			void foo() {}
		}""";

	String testName = "test33: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		89,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		315,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 115, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 315, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test34() {

	String s =
		"""
		package a;									\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() 						\t
			    }									\t
			}										\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}											\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			public int h;
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test34: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		88,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		342,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field h", 250, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field h", 262, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains 3 methods ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 114, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 236, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 286, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 306, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 308, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 329, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test35() {

	String s =
		"""
		package a;						\t
		import java.lang.*;			\t
		import java.util.*;			\t
										\t
		public class X {				\t
			void foo() {				\t
				System.out.println();	\t
										\t
				class L extends {		\t
					public int l;		\t
					void baz(){}		\t
				}						\t
										\t
				int h;					\t
										\t
			void bar(){					\t
			void truc(){				\t
		}								\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			java.lang.Object(0)
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test35: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		76,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		309,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains 3 methods ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 99, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 260, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 262, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 279, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 281, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 299, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test36() {

	String s =
		"""
		package a;						\t
		import java.lang.*;			\t
		import java.util.*;			\t
										\t
		public class X {				\t
			void foo() {				\t
				System.out.println();	\t
										\t
				class L extends {		\t
					public int l;		\t
					void baz(){}		\t
				}						\t
										\t
				int h;					\t
										\t
			void bar(){					\t
			void truc(){				\t
		}								\t
		""";

	String expectedUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
			void foo() {}
			void bar() {}
			void truc() {}
		}""";

	String testName = "test36: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		76,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		309,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains 3 methods ", 3, methods.length);

	assertEquals("Invalid declaration source start for method foo", 99, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 260, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 262, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 279, methods[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method truc", 281, methods[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method truc", 299, methods[2].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	assertEquals(" Invalid actual name for method truc", "truc", methods[2].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test37() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  {			\t
		    int y;				\t
		}						\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			class Y {
				int y;
				java.lang.Object(0)
			}
			int x;
			java.lang.Object(0)
			int foo() {}
		}""";

	String testName = "test37: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		112,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 23, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 28, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 46, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 57, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals("contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		73,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		104,
		members[0].getDeclarationSourceEnd());

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field y", 92, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 97, fields[0].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test38() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  {			\t
		    int y;				\t
		}						\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			class Y {
				int y;
			}
			int x;
			int foo() {}
		}""";

	String testName = "test38: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		112,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 23, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 28, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 46, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 57, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals("contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		73,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		104,
		members[0].getDeclarationSourceEnd());

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field y", 92, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 97, fields[0].getDeclarationSourceEnd());

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test39() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  			\t
		}						\t
		  int y;				\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			class Y {
				java.lang.Object(0)
			}
			int x;
			int y;
			java.lang.Object(0)
			int foo() {}
		}""";

	String testName = "test39: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		109,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 2, fields.length);

	assertEquals("Invalid declaration source start for field x", 23, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 28, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 98, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 103, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 46, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 57, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals("contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		73,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		87,
		members[0].getDeclarationSourceEnd());

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields == null);

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test40() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  			\t
		}						\t
		  int y;				\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			class Y {
			}
			int x;
			int y;
			int foo() {}
		}""";

	String testName = "test40: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		109,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("contains one field ", 2, fields.length);

	assertEquals("Invalid declaration source start for field x", 23, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 28, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 98, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 103, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 46, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 57, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals("contains one member ", 1, members.length);

	assertEquals(
		"Invalid class declarationSourceStart ",
		73,
		members[0].getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		87,
		members[0].getDeclarationSourceEnd());

	fields = members[0].getFields();
	assertTrue(" invalid fields ", fields == null);

	methods = members[0].getMethods();
	assertTrue(" invalid methods ", methods == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test41() {

	String s =
		"""
		public class X {			\t
			void hell\
		\\\
		u006f()
			static void foo() {		\t
				X x;				\t
				x = new X(23);		\t
				System.out.println();\t
									\t
		}							\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			java.lang.Object(0)
			void hello() {}
			static void foo() {}
		}""";

	String testName = "test41: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		139,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals(" contains one methods ", 2, methods.length);

	assertEquals("Invalid declaration source start for method hello", 22, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method hello", 39, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method foo", 41, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 130, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method hello", """
		hell\
		\\\
		u006f""", methods[0].getActualName());

	assertEquals(" Invalid actual name for method foo", "foo", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test42() {

	String s =
		"public class X {		\n" +
		"	int x				\n";

	String expectedUnitToString =
		"""
		public class X {
			int x;
			java.lang.Object(0)
		}""";

	String testName = "test42: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		29,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" contains one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 20, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 24, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test43() {

	String s =
		"public class X {		\n" +
		"	int x				\n";

	String expectedUnitToString =
		"""
		public class X {
			int x;
		}""";

	String testName = "test43: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		29,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" contains one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 20, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 24, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test44() {

	String s =
		"public class X {		\n" +
		"	int x, y			\n";

	String expectedUnitToString =
		"""
		public class X {
			int x;
			int y;
			java.lang.Object(0)
		}""";

	String testName = "test44: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		31,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" contains one field", 2, fields.length);

	assertEquals("Invalid declaration source start for field x", 20, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 25, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 20, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 27, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test45() {

	String s =
		"public class X {		\n" +
		"	int x, y			\n";

	String expectedUnitToString =
		"""
		public class X {
			int x;
			int y;
		}""";

	String testName = "test45: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		31,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" contains one field", 2, fields.length);

	assertEquals("Invalid declaration source start for field x", 20, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 25, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field y", 20, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 27, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test46() {

	String s =
		"public class X {		\n" +
		"	String s = \"		\n";

	String expectedUnitToString =
		"""
		public class X {
			String s;
			java.lang.Object(0)
		}""";

	String testName = "test46: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		34,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" contains one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field s", 20, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 34, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test47() {

	String s =
		"public class X {		\n" +
		"	String s = \"		\n";

	String expectedUnitToString =
		"""
		public class X {
			String s;
		}""";

	String testName = "test47: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		34,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("doesn't contain superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals(" contains one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field s", 20, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 34, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test48() {

	String s =
		"public class X implements Y, 		\n" +
		"	String s = \"					\n";

	String expectedUnitToString =
		"""
		public class X implements Y, String, {
			java.lang.Object(0)
		}""";

	String testName = "test48: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		50,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has 2 superinterfaces " , this.currentType.getInterfaceNames() != null);
	assertEquals("2 superinterfaces " , 2, this.currentType.getInterfaceNames().length);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test49() {

	String s =
		"public class X implements Y, 		\n" +
		"	String s = \"					\n";

	String expectedUnitToString =
		"public class X implements Y, String, {\n"
		+ "}";

	String testName = "test49: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		50,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has 2 superinterfaces " , this.currentType.getInterfaceNames() != null);
	assertEquals("2 superinterfaces " , 2, this.currentType.getInterfaceNames().length);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test50() {

	String s =
		"""
		public class X implements 	\t
		int x						\t
		}							\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			int x;
			java.lang.Object(0)
		}""";

	String testName = "test50: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		42,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 29, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 33, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test51() {

	String s =
		"""
		public class X implements 	\t
		int x						\t
		}							\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			int x;
		}""";

	String testName = "test51: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		42,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 29, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 33, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test52() {

	String s =
		"public class X public int foo(int bar(static String s";

	String expectedUnitToString =
		"""
		public class X {
			static String s;
			java.lang.Object(0)
			public int foo() {}
			int bar() {}
		}""";

	String testName = "test52: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		52,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field s", 38, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 52, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);

	assertEquals("Invalid declaration source start for method foo", 15, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 29, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 30, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 37, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test53() {

	String s =
		"public class X public int foo(int x, int bar public String s;";

	String expectedUnitToString =
		"""
		public class X {
			public String s;
			java.lang.Object(0)
			public int foo(int x, int bar, ) {}
		}""";

	String testName = "test53: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		60,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field ", 1, fields.length);

	assertEquals("Invalid declaration source start for field s", 45, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 60, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has one method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 15, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 44, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test54() {

	String s =
		"""
		public class X 		\t
			public int foo(		\t
			int bar(			\t
		 	static String s, int x\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			static String s;
			int x;
			java.lang.Object(0)
			public int foo() {}
			int bar() {}
		}""";

	String testName = "test54: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		78,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has 2 fields ", 2, fields.length);

	assertEquals("Invalid declaration source start for field s", 55, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 70, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field x", 72, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 76, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 2 methods ", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 20, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 38, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 40, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 52, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test55() {

	String s =
		"""
		public class X 		\t
			public int foo(		\t
			int bar(			\t
		 	static String s, int x\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			static String s;
			int x;
			public int foo() {}
			int bar() {}
		}""";

	String testName = "test55: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		78,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has 2 fields ", 2, fields.length);

	assertEquals("Invalid declaration source start for field s", 55, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 70, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field x", 72, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 76, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 2 methods ", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 20, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 38, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method bar", 40, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method bar", 52, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method bar", "bar", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test56() {

	String s =
		"""
		class X {				\t
			String s;			\t
								\t
			public void foo(	\t
				static int x	\t
		}						\t
		""";


	String expectedUnitToString =
		"""
		class X {
			String s;
			static int x;
			java.lang.Object(0)
			public void foo() {}
		}""";

	String testName = "test56: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		75,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has 2 fields ", 2, fields.length);

	assertEquals("Invalid declaration source start for field s", 16, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 24, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field x", 60, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 71, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 1 method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 39, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 57, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test57() {

	String s =
		"""
		class X {				\t
			String s;			\t
								\t
			public void foo(	\t
				static int x	\t
		}						\t
		""";


	String expectedUnitToString =
		"""
		class X {
			String s;
			static int x;
			public void foo() {}
		}""";

	String testName = "test57: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		75,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has 2 fields ", 2, fields.length);

	assertEquals("Invalid declaration source start for field s", 16, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field s", 24, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field x", 60, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 71, fields[1].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 1 method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 39, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 57, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test58() {

	String s =
		"""
		public class X {		\t
			int foo(){			\t
				String s = "	\t
			}					\t
		}						\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			java.lang.Object(0)
			int foo() {}
		}""";

	String testName = "test58: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		62,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 1 method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 21, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 54, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test59() {

	String s =

		"""
		class X {								\t
			int foo(AA a, BB b, IOEx			\t
												\t
		""";

	String expectedUnitToString =
		"""
		class X {
			java.lang.Object(0)
			int foo(AA a, BB b, ) {}
		}""";

	String testName = "test59: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		60,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 1 method ", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 20, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 60, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test60() {

	String s =
		"""
		public class X {						\t
			final static int foo(){ 			\t
				return "1; 					\t
			} 									\t
			public static void main(String argv[]){\s
				foo();							\t
			} 									\t
		}										\t
		""";

	String expectedUnitToString =
		"""
		public class X {
			java.lang.Object(0)
			final static int foo() {}
			public static void main(String[] argv, ) {}
		}""";

	String testName = "test60: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		161,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has 1 method ", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 25, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 75, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method main", 89, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method main", 148, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method main", "main", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test61() {

	String s =
		"""
		public class X {						\t
			{									\t
		     int x;""";

	String expectedUnitToString =
		"""
		public class X {
			{}
			java.lang.Object(0)
		}""";

	String testName = "test61: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		47,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);

	assertEquals("Invalid declaration source start for initializer", 25, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 47, fields[0].getDeclarationSourceEnd());


	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test62() {

	String s =
		"""
		public class X {						\t
		   int foo(){							\t
			  if(true){							\t
		     	int x;""";

	String expectedUnitToString =
		"""
		public class X {
			java.lang.Object(0)
			int foo() {}
		}""";

	String testName = "test62: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		78,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has one method", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 78, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
//invalid tests (would be rejected by compiler. Disable for now
public void _test63() {

	String s =
		"""
		public class X {						\t
		   int foo(){}							\t
		}										\t
		int x;
		""";

	String expectedUnitToString =
		"""
		final class test63$Implicit {
			public class X {
				java.lang.Object(0)
				int foo() {}
			}
			int x;
			java.lang.Object(0)
		}""";

	String testName = "test63";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		47,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 60, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 65, fields[0].getDeclarationSourceEnd());

	SourceType[] types = this.currentType.getMemberTypes();
	assertTrue("invalid member types", types != null);
	assertEquals("has one member type", 1, types.length);

	assertEquals("Invalid member type declaration start", 0, types[0].getDeclarationSourceStart());
	assertEquals("Invalid member type declaration start", 47, types[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMemberTypes()[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has one method", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 37, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
// invalid tests (would be rejected by compiler. Disable for now
public void _test64() {

	String s =
		"""
		public class X {						\t
		   int foo(){}							\t
		}										\t
		int x;
		""";

	String expectedUnitToString =
			"""
		final class test64$Implicit {
			public class X {
				int foo() {}
			}
			int x;
		}""";


	String testName = "test64";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		66,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field x", 60, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field x", 65, fields[0].getDeclarationSourceEnd());

	SourceType[] types = this.currentType.getMemberTypes();
	assertTrue("invalid member types", types != null);
	assertEquals("has one member type", 1, types.length);

	assertEquals("Invalid member type declaration start", 0, types[0].getDeclarationSourceStart());
	assertEquals("Invalid member type declaration start", 47, types[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMemberTypes()[0].getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has one method", 1, methods.length);

	assertEquals("Invalid declaration source start for method foo", 27, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 37, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test65() {

	String s =
		"""
		public class X {						\t
		   int foo(){}							\t
		}										\t
		int x();
		""";

	String expectedUnitToString =
		"""
		final class test65 {
			public class X {
				int foo() {}
			}
			int x() {}
		}""";

	String testName = "test65";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		68,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has one method", 1, methods.length);

	assertEquals("Invalid declaration source start for method x", 60, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 67, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method x", "x", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" has one member type ", 1, members.length);

	SourceMethod[] memberMethods = members[0].getMethods();
	assertTrue(" invalid member methods ", memberMethods != null);
	assertEquals(" has one member method ", 1, memberMethods.length);

	assertEquals("Invalid declaration source start for method foo", 27, memberMethods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 37, memberMethods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", memberMethods[0].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test66() {

	String s =
		"""
		public interface X {					\t
		   int foo() {};						\t
		}										\t
		int x();
		""";

	String expectedUnitToString =
		"""
		final class test66 {
			public interface X {
				int foo() {}
			}
			int x() {}
		}""";

	String testName = "test66";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		72,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has one method", 1, methods.length);

	assertEquals("Invalid declaration source start for method x", 64, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 71, methods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method x", "x", methods[0].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members != null);
	assertEquals(" has 1 member type ", 1, members.length);

	SourceMethod[] memberMethods = members[0].getMethods();
	assertTrue("invalid member type methods", memberMethods != null);
	assertEquals("member type has one method", 1, memberMethods.length);

	assertEquals("Invalid declaration source start for method foo", 30, memberMethods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 41, memberMethods[0].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", memberMethods[0].getActualName());

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test67() {

	String s =
		"""
		public interface X {					\t
		   int foo() {};						\t
		   int x();							\t
		}""";

	String expectedUnitToString =
		"""
		public interface X {
			int foo() {}
			int x() {}
		}""";

	String testName = "test67: diet parse";
	dietParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		71,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has two methods", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 30, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 41, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method x", 54, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 61, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method x", "x", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test68() {

	String s =
		"""
		public interface X {					\t
		   int foo() {};						\t
		   int x();							\t
		}""";

	String expectedUnitToString =
		"""
		public interface X {
			int foo() {}
			int x() {}
		}""";

	String testName = "test68: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		71,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has two methods", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 30, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 41, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method x", 54, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 61, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method x", "x", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test69() {

	String s =
		"""
		public interface X {					\t
		float y;								\t
		   int foo()	;						\t
		   int x();							\t
		}""";

	String expectedUnitToString =
		"""
		public interface X {
			float y;
			int foo() {}
			int x() {}
		}""";

	String testName = "test69: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		87,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field y", 27, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field y", 34, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has two methods", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 48, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 58, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method x", 70, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 77, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method x", "x", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test70() {

	String s =
		"""
		public interface X {					\t
		   int foo();							\t
		   int x();							\t
		}""";

	String expectedUnitToString =
		"""
		public interface X {
			int foo() {}
			int x() {}
		}""";

	String testName = "test70: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		69,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields == null);

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has two methods", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 30, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 39, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method x", 52, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 59, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method x", "x", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test71() {

	String s =
		"""
		public interface X {					\t
		   int[] i = ;							\t
		   int foo() {}						\t
		   int x();							\t
		}""";

	String expectedUnitToString =
		"""
		public interface X {
			int[] i;
			int foo() {}
			int x() {}
		}""";

	String testName = "test71: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		93,
		this.currentType.getDeclarationSourceEnd());

	assertTrue("has no superinterfaces " , this.currentType.getInterfaceNames() == null);

	SourceField[] fields = this.currentType.getFields();
	assertTrue(" invalid fields ", fields != null);
	assertEquals("has one field", 1, fields.length);

	assertEquals("Invalid declaration source start for field i", 30, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field i", 49, fields[0].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods != null);
	assertEquals("has two methods", 2, methods.length);

	assertEquals("Invalid declaration source start for method foo", 53, methods[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method foo", 64, methods[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for method x", 76, methods[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for method x", 83, methods[1].getDeclarationSourceEnd());

	assertEquals(" Invalid actual name for method foo", "foo", methods[0].getActualName());

	assertEquals(" Invalid actual name for method x", "x", methods[1].getActualName());

	SourceType[] members = this.currentType.getMemberTypes();
	assertTrue(" invalid members ", members == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test72() {

	String s =
		"""
		public class X {					\t
		   X() {							\t
		   	this();						\t
			}								\t
		}""";

	String expectedUnitToString =
		"""
		public class X {
			X() {
				X(0)
			}
		}""";

	String testName = "test72: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test73() {

	String s =
		"""
		public class X extends Toto {		\t
		   X() {							\t
		   	this();						\t
			}								\t
		   X(int i) {						\t
		   	super();					\t
			}								\t
		   X() {							\t
		   	this(0);					\t
			}								\t
		}""";

	String expectedUnitToString =
		"""
		public class X extends Toto {
			X() {
				X(0)
			}
			X(int i, ) {
				Toto(0)
			}
			X() {
				X(1)
			}
		}""";

	String testName = "test73: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test74() {

	String s =
		"""
		public class X extends Toto {		\t
			class Y extends Throwable {		\t
			}								\t
		   X() {							\t
		   	this();						\t
			}								\t
		   X(int i) {						\t
		   	super();					\t
			}								\t
		   X() {							\t
		   	this(0);					\t
			}								\t
			public Object foo(int i) {		\t
				return new Object() {};		\t
			}								\t
		}""";

	String expectedUnitToString =
		"""
		public class X extends Toto {
			class Y extends Throwable {
				Throwable(0)
			}
			X() {
				X(0)
			}
			X(int i, ) {
				Toto(0)
			}
			X() {
				X(1)
			}
			public Object foo(int i, ) {}
		}""";

	String testName = "test74: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
/*
 * bugs  16126
 */
public void test75() {
	String s =
		"""
		public class P#AField {
			public void setP#A(String P#A) {
				this.P#A = P#A;
			}
		}""";

	String expectedUnitToString =
		"""
		public class P {
			{}
			public void setP;
			java.lang.Object(0)
			A(String P, ) {}
		}""";

	String testName = "test75: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test76() {

	String s =
		"""
		class X {
		  public static int j = 0;
		  /* static */ {
		  }\
		  public static int i = 9;
		}
		""";

	String expectedUnitToString =
		"""
		class X {
			public static int j;
			{}
			public static int i;
			java.lang.Object(0)
		}""";

	String testName = "test76: full parse";
	fullParse(s,testName);

	assertEquals(
		"Invalid class declarationSourceStart ",
		0,
		this.currentType.getDeclarationSourceStart());

	assertEquals(
		"Invalid class declarationSourceEnd ",
		84,
		this.currentType.getDeclarationSourceEnd());

	SourceField[] fields = this.currentType.getFields();
	assertTrue("invalid fields ", fields != null);
	assertEquals("Invalid fields length ", 3, fields.length);

	assertEquals("Invalid declaration source start for field j", 12, fields[0].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for field j", 35, fields[0].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for initializer", 39, fields[1].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end for initializer", 56, fields[1].getDeclarationSourceEnd());

	assertEquals("Invalid declaration source start for field i", 59, fields[2].getDeclarationSourceStart());
	assertEquals("Invalid declaration source end field i", 82, fields[2].getDeclarationSourceEnd());

	SourceMethod[] methods = this.currentType.getMethods();
	assertTrue(" invalid methods ", methods == null);

	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
/**
 * Bug 99662:[1.5] JavaModel returns inexistent IType for package-info ICompilationUnits
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99662"
 */
public void testBug99662() {

	String s =
		"@Deprecated\n" +
		"package p;\n";

	String testName = "package-info.java";
	fullParse(s,testName);

	assertNull("package-info.java file should not have ANY type!",  this.currentType);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
public void _test77() {

	String s =
		"""
		public class X {
			void foo() {
				class Y {
					String s = null;
					{
						class Z {
						}
					}
				}
			}
		}""";

	String expectedUnitToString =
		"public class X implements Y, String, {\n"
		+ "}";

	String testName = "test77: diet parse";
	dietParse(s,testName, true);
	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
public void _test78() {

	String s =
		"""
		public class X {
			void foo() {
				class Y {
					String s = null;
					{
						class Z {
						}
					}
				}
			}
		}""";

	String expectedUnitToString =
		"public class X implements Y, String, {\n"
		+ "}";

	String testName = "test78: full parse";
	fullParse(s,testName, true);
	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
public void _test79() {

	String s =
		"""
		public class X {
			void foo() {
				class Y {
					{
						class Z {
						}
					}
					String s = null;
				}
			}
		}""";

	String expectedUnitToString =
		"public class X implements Y, String, {\n"
		+ "}";

	String testName = "test79: diet parse";
	dietParse(s,testName, true);
	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
public void _test80() {

	String s =
		"""
		public class X {
			void foo() {
				class Y {
					{
						class Z {
						}
					}
					String s = null;
				}
			}
		}""";

	String expectedUnitToString =
		"public class X implements Y, String, {\n"
		+ "}";

	String testName = "test80: full parse";
	fullParse(s,testName, true);
	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
public void test81() {

	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
	options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);

	String s =
		"""
		import java.util.Collection;
		
		public class X {
			public abstract class AbstractData {}
		\t
			public interface IScalarData<T extends AbstractData> {}
		
			private static interface ValueObjectPropertyIterator {
				public <T extends AbstractData> void iterateOnValueObjectProperty(IScalarData<T> scalarObject, T valueObject, Class<?> valueObjectType, final String name, final Class<?> scalarType) throws Exception;
			}
		
			private static <T extends AbstractData> void iterateOnValueObjectProperties(IScalarData<T> scalarObject, T valueObject, ValueObjectPropertyIterator valueObjectPropertyIterator) {}
		\t
			public static <T extends AbstractData> void loadScalarFromValueObject(IScalarData<T> scalarObject, T valueObject) {
				iterateOnValueObjectProperties(scalarObject, valueObject, new ValueObjectPropertyIterator() {
					public <T extends AbstractData> void iterateOnValueObjectProperty(IScalarData<T> scalarObject, T valueObject, Class<?> valueObjectType, String name, Class<?> scalarType) throws Exception {
						if (true) {
							if (true) {
								if (true) {
									final Collection<IScalarData<AbstractData>> lazyCollection = createLazyCollection(
											name, scalarType, null, null,
											new CollectionProviderForTargetCollection<IScalarData<AbstractData>>() {
												@Override
												public Collection<IScalarData<AbstractData>> provideCollection(
														final Collection<IScalarData<AbstractData> targetCollection, final Class<IScalarData<AbstractData>> scalarCollectionType) {
													return null;
												}
											});
								}
							}
						}
					}
		
					abstract class CollectionProviderForTargetCollection<S> {
						abstract public Collection<S> provideCollection(Collection<S> targetCollection, Class<S> scalarCollectionType);
					}
		
					private <S> Collection<S> createLazyCollection(String name,
							Class<?> scalarType, final Collection<AbstractData> valueObjectCollection,
							final Class<S> scalarCollectionType, CollectionProviderForTargetCollection<S> collectionProvider) {
						return null;
					}
				});
			}
		}""";

	String expectedUnitToString =
		"""
		import java.util.Collection;
		public class X {
			public abstract class AbstractData {
				java.lang.Object(0)
			}
			public interface IScalarData {
			}
			private static interface ValueObjectPropertyIterator {
				public void iterateOnValueObjectProperty(IScalarData<T> scalarObject, T valueObject, Class<?> valueObjectType, String name, Class<?> scalarType, ) throws Exception, {}
			}
			java.lang.Object(0)
			private static void iterateOnValueObjectProperties(IScalarData<T> scalarObject, T valueObject, ValueObjectPropertyIterator valueObjectPropertyIterator, ) {}
			public static void loadScalarFromValueObject(IScalarData<T> scalarObject, T valueObject, ) {}
		}""";

	String testName = "test81: full parse";
	fullParse(s,testName, options);
	assertEquals(
		"Invalid source " + testName,
		expectedUnitToString,
		this.currentType.toString());
}
}
