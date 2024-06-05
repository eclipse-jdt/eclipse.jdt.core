/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
/*
 * Here we focus on various aspects of the runtime behavior of the generated
 * code.
 */
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SerialVersionUIDTests extends AbstractRegressionTest {

public SerialVersionUIDTests(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "test0001" };
//	 	TESTS_NUMBERS = new int[] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return SerialVersionUIDTests.class;
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.ERROR);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=101476
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				
				public class X implements Serializable {
					private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException {}
					private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {}
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101476
public void test002() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements java.io.Externalizable {
					public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {}
					public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {}
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101476
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements java.io.Serializable {
					private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements java.io.Serializable {
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101476
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X implements java.io.Serializable {
					private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException {}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements java.io.Serializable {
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101476
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements java.io.Serializable {\n" +
			"}"
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements java.io.Serializable {
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101476
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements java.io.Serializable {
					Object writeReplace() throws java.io.ObjectStreamException { return null;}
				}"""
		},
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=203241
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=116733
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=94352
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"abstract class A implements java.io.Serializable {}\n" +
			"public class X extends A {}\n"
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				abstract class A implements java.io.Serializable {}
				               ^
			The serializable class A does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 2)
				public class X extends A {}
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285124
public void _test008() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends javax.rmi.CORBA.Stub {
						public String[] _ids() { return null; }
					}"""
			},
			""
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285124
public void _test009() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends Y {
						public String[] _ids() { return null; }
					}""",
				"Y.java",
				"public abstract class Y extends javax.rmi.CORBA.Stub {\n" +
				"}"
			},
			""
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285124
public void _test010() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X extends Y {
						public String[] _ids() { return null; }
					}""",
				"Y.java",
				"public abstract class Y extends Z {\n" +
				"}",
				"Z.java",
				"public abstract class Z extends javax.rmi.CORBA.Stub {\n" +
				"}"
			},
			""
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=341475
public void test011() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private static final long serialVersionUID = 1L;
				                          ^^^^^^^^^^^^^^^^
			The value of the field X.serialVersionUID is not used
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=341475
public void test012() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					private static final long serialVersionUID = 1L;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private static final long serialVersionUID = 1L;
				                          ^^^^^^^^^^^^^^^^
			The value of the field X<T>.serialVersionUID is not used
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=341475
public void test013() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
					private static final long serialPersistentFields = 1L;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private static final long serialPersistentFields = 1L;
				                          ^^^^^^^^^^^^^^^^^^^^^^
			The value of the field X<T>.serialPersistentFields is not used
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=341475
public void test014() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private static final long serialPersistentFields = 1L;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private static final long serialPersistentFields = 1L;
				                          ^^^^^^^^^^^^^^^^^^^^^^
			The value of the field X.serialPersistentFields is not used
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=341475
public void test015() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.ObjectStreamField;
				public class X<T> implements java.io.Serializable {
					private static final long serialVersionUID = 1L;
					private final static ObjectStreamField[] serialPersistentFields = null;
					private int i;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				private int i;
				            ^
			The value of the field X<T>.i is not used
			----------
			""",
		null,
		true,
		options
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=341475
public void test016() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.ObjectStreamField;
				public class X implements java.io.Serializable {
					private static final long serialVersionUID = 1L;
					private final static ObjectStreamField[] serialPersistentFields = null;
					private int i;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				private int i;
				            ^
			The value of the field X.i is not used
			----------
			""",
		null,
		true,
		options
	);
}
}
