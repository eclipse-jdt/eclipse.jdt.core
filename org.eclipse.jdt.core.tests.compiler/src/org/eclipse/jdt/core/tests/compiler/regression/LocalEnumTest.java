/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contributions for
 *								Bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								Bug 265744 - Enum switch should warn about missing default
 *								Bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalEnumTest extends AbstractComparableTest {

	String reportMissingJavadocComments = null;

	public LocalEnumTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test004" };
//		TESTS_NUMBERS = new int[] { 185 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
//		return buildComparableTestSuite(testClass());
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}

	public static Class testClass() {
		return LocalEnumTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16); // FIXME
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.reportMissingJavadocComments = null;
	}

	@Override
	protected void runConformTest(String[] testFiles) {
		runConformTest(testFiles, "", getCompilerOptions());
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}
//	@Override
//	protected void runConformTest(String[] testFiles, String expectedOutput, Map customOptions) {
//		Runner runner = new Runner();
//		runner.testFiles = testFiles;
//		runner.expectedOutputString = expectedOutput;
//		runner.vmArguments = new String[] {"--enable-preview"};
//		runner.customOptions = customOptions;
//		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("16");
//		runner.runConformTest();
//	}
//
//	@Override
//	protected void runConformTest(
//			// test directory preparation
//			boolean shouldFlushOutputDirectory,
//			String[] testFiles,
//			//compiler options
//			String[] classLibraries /* class libraries */,
//			Map customOptions /* custom options */,
//			// compiler results
//			String expectedCompilerLog,
//			// runtime results
//			String expectedOutputString,
//			String expectedErrorString,
//			// javac options
//			JavacTestOptions javacTestOptions) {
//		Runner runner = new Runner();
//		runner.testFiles = testFiles;
//		runner.expectedOutputString = expectedOutputString;
//		runner.expectedCompilerLog = expectedCompilerLog;
//		runner.expectedErrorString = expectedErrorString;
//		runner.vmArguments = new String[] {"--enable-preview"};
//		runner.customOptions = customOptions;
//		runner.javacTestOptions = javacTestOptions == null ? JavacTestOptions.forReleaseWithPreview("16") : javacTestOptions;
//		runner.runConformTest();
//	}
//
//	@Override
//	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
//		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview("16"));
//	}
//	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
//		runWarningTest(testFiles, expectedCompilerLog, null);
//	}
//	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
//		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
//	}
//	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
//			Map<String, String> customOptions, String javacAdditionalTestOptions) {
//
//		Runner runner = new Runner();
//		runner.testFiles = testFiles;
//		runner.expectedCompilerLog = expectedCompilerLog;
//		runner.customOptions = customOptions;
//		runner.vmArguments = new String[] {"--enable-preview"};
//		runner.javacTestOptions = javacAdditionalTestOptions == null ? JavacTestOptions.forReleaseWithPreview("15") :
//			JavacTestOptions.forReleaseWithPreview("16", javacAdditionalTestOptions);
//		runner.runWarningTest();
//	}

	private void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean positive) throws IOException, ClassFormatException {
		String result = getClassFileContents(classFileName, mode);
		verifyOutput(result, expectedOutput, positive);
	}
	private String getClassFileContents( String classFileName, int mode) throws IOException,
	ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		return result;
	}
	private void verifyOutput(String result, String expectedOutput, boolean positive) {
		int index = result.indexOf(expectedOutput);
		if (positive) {
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
				System.out.println("...");
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} else {
			if (index != -1) {
				assertEquals("Unexpected contents", "", result);
			}
		}
	}

// test simple valid enum and its usage
public void test000() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				          enum Role { M, D }
				 enum T {
				       PHILIPPE(37) {
				               public boolean isManager() {
				                       return true;
				               }
				       },
				       DAVID(27),
				       JEROME(33),
				       OLIVIER(35),
				       KENT(40),
				       YODA(41),
				       FREDERIC;
				       final static int OLD = 41;
				
				
				   int age;
				       Role role;
				
				       T() { this(OLD); }
				       T(int age) {
				               this.age = age;
				       }
				       public int age() { return this.age; }
				       public boolean isManager() { return false; }
				       void setRole(boolean mgr) {
				               this.role = mgr ? Role.M : Role.D;
				       }
				}
				       System.out.print("JDTCore team:");
				       T oldest = null;
				       int maxAge = Integer.MIN_VALUE;
				       for (T t : T.values()) {
				            if (t == T.YODA) continue;// skip YODA
				            t.setRole(t.isManager());
				                        if (t.age() > maxAge) {
				               oldest = t;
				               maxAge = t.age();
				            }
				                      Location l = switch(t) {
				                         case PHILIPPE, DAVID, JEROME, FREDERIC-> Location.SNZ;
				                         case OLIVIER, KENT -> Location.OTT;
				                         default-> throw new AssertionError("Unknown team member: " + t);
				                       };
				
				            System.out.print(" "+ t + ':'+t.age()+':'+l+':'+t.role);
				        }
				        System.out.println(" WINNER is:" + T.valueOf(oldest.name()));
				    }
				
				   private enum Location { SNZ, OTT }
				}"""
		},
		"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:41:SNZ:D WINNER is:FREDERIC"
	);
}
// check assignment to enum constant is disallowed
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				    enum Y {\s
					   BLEU,\s
					   BLANC,\s
					   ROUGE;
					   static {
					   	 BLEU = null;
					   }
					 }
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				BLEU = null;
				^^^^
			The final field Y.BLEU cannot be assigned
			----------
			""");
}
// check diagnosis for duplicate enum constants
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				    enum Y {\s
					\t
						BLEU,\s
						BLANC,\s
						ROUGE,
						BLEU;
					 }
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				BLEU,\s
				^^^^
			Duplicate field Y.BLEU
			----------
			2. ERROR in X.java (at line 8)
				BLEU;
				^^^^
			Duplicate field Y.BLEU
			----------
			""");
}
// check properly rejecting enum constant modifiers
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				    enum Y {\s
				\t
						public BLEU,\s
						transient BLANC,\s
						ROUGE,\s
						abstract RED {
							void _test() {}
						}
					 }
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public BLEU,\s
				       ^^^^
			Illegal modifier for the enum constant BLEU; no modifier is allowed
			----------
			2. ERROR in X.java (at line 6)
				transient BLANC,\s
				          ^^^^^
			Illegal modifier for the enum constant BLANC; no modifier is allowed
			----------
			3. ERROR in X.java (at line 8)
				abstract RED {
				         ^^^
			Illegal modifier for the enum constant RED; no modifier is allowed
			----------
			""");
}
// check using an enum constant
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				     enum Y {\s
					\t
						BLEU,
						BLANC,
						ROUGE;
					\t
						public static void main(String[] a) {
							System.out.println(BLEU);
						}
					\t
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"BLEU");
}
// check method override diagnosis (with no enum constants)
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				     enum Y {\s
						;
						protected Object clone() { return this; }
					  }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				protected Object clone() { return this; }
				                 ^^^^^^^
			Cannot override the final method from Enum<Y>
			----------
			2. WARNING in X.java (at line 5)
				protected Object clone() { return this; }
				                 ^^^^^^^
			The method clone() of type Y should be tagged with @Override since it actually overrides a superclass method
			----------
			""");
}
// check generated #values() method
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				     enum Y {\s
					\t
						BLEU,
						BLANC,
						ROUGE;
					\t
						public static void main(String[] args) {
							for(Y y: Y.values()) {
								System.out.print(y);
							}
						}
					\t
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"BLEUBLANCROUGE");
}
// tolerate user definition for $VALUES
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				     enum Y {\s
					\t
						BLEU,
						BLANC,
						ROUGE;
					\t
				      int $VALUES;
						public static void main(String[] args) {
								for(Y y: Y.values()) {
									System.out.print(y);
							}
						}
					\t
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"BLEUBLANCROUGE");
}
// reject user definition for #values()
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				     enum Y {\s
					\t
						BLEU,
						BLANC,
						ROUGE;
					\t
				      void dup() {}\s
				      void values() {}\s
				      void dup() {}\s
				      void values() {}\s
				      Missing dup() {}\s
						public static void main(String[] args) {
								for(Y y: Y.values()) {
									System.out.print(y);
						}
					}
				\t
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				void dup() {}\s
				     ^^^^^
			Duplicate method dup() in type Y
			----------
			2. ERROR in X.java (at line 10)
				void values() {}\s
				     ^^^^^^^^
			The enum Y already defines the method values() implicitly
			----------
			3. ERROR in X.java (at line 11)
				void dup() {}\s
				     ^^^^^
			Duplicate method dup() in type Y
			----------
			4. ERROR in X.java (at line 12)
				void values() {}\s
				     ^^^^^^^^
			The enum Y already defines the method values() implicitly
			----------
			5. ERROR in X.java (at line 13)
				Missing dup() {}\s
				^^^^^^^
			Missing cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 13)
				Missing dup() {}\s
				        ^^^^^
			Duplicate method dup() in type Y
			----------
			7. WARNING in X.java (at line 14)
				public static void main(String[] args) {
				                                 ^^^^
			The parameter args is hiding another local variable defined in an enclosing scope
			----------
			""");
}
// switch on enum
public void test009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				     enum Y {\s
				\t
						BLEU,
						BLANC,
						ROUGE;
					\t
						//void values() {}
					\t
						public static void main(String[] args) {
							Y y = BLEU;
							switch(y) {
								case BLEU :
									System.out.println("SUCCESS");
									break;
								case BLANC :
								case ROUGE :
									System.out.println("FAILED");
									break;
					           default: // nop
				   		}
						}
					\t
					  }
					  Y.main(args);
					}
				}"""
		},
		"SUCCESS");
}
// duplicate switch case
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				     enum Y {\s
					\t
						BLEU,
						BLANC,
						ROUGE;
					\t
						//void values() {}
					\t
						public static void main(String[] args) {
							Y y = BLEU;
							switch(y) {
								case BLEU :
									break;
								case BLEU :
								case BLANC :
								case ROUGE :
									System.out.println("FAILED");
									break;
				              default: // nop
							}
						}
				\t
					  }
					  Y.main(args);
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 11)
				public static void main(String[] args) {
				                                 ^^^^
			The parameter args is hiding another local variable defined in an enclosing scope
			----------
			2. ERROR in X.java (at line 14)
				case BLEU :
				^^^^^^^^^
			Duplicate case
			----------
			3. ERROR in X.java (at line 16)
				case BLEU :
				^^^^^^^^^
			Duplicate case
			----------
			""");
}
// reject user definition for #values()
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				     enum Y {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
				   void values() {}\s
				   void values() {}\s
					public static void main(String[] args) {
						for(Y y: Y.values()) {
							System.out.print(y);
						}
					}
				\t
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				void values() {}\s
				     ^^^^^^^^
			The enum Y already defines the method values() implicitly
			----------
			2. ERROR in X.java (at line 10)
				void values() {}\s
				     ^^^^^^^^
			The enum Y already defines the method values() implicitly
			----------
			3. WARNING in X.java (at line 11)
				public static void main(String[] args) {
				                                 ^^^^
			The parameter args is hiding another local variable defined in an enclosing scope
			----------
			""");
}
// check abstract method diagnosis
public void testNPE012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
					  enum Y implements Runnable {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
					  }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum Y implements Runnable {\s
				     ^
			The type Y must implement the inherited abstract method Runnable.run()
			----------
			""");
}
// check enum constants with wrong arguments
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
					  enum Y  {\s
				\t
					BLEU(10),
					BLANC(20),
					ROUGE(30);
					  }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				BLEU(10),
				^^^^
			The constructor Y(int) is undefined
			----------
			2. ERROR in X.java (at line 6)
				BLANC(20),
				^^^^^
			The constructor Y(int) is undefined
			----------
			3. ERROR in X.java (at line 7)
				ROUGE(30);
				^^^^^
			The constructor Y(int) is undefined
			----------
			""");
}
// check enum constants with extra arguments
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
					  enum Y  {\s
				\t
					BLEU(10),
					BLANC(20),
					ROUGE(30);
				
					int val;
					Y(int val) {
						this.val = val;
					}
				
					public static void main(String[] args) {
						for(Y y: values()) {
							System.out.print(y.val);
						}
					}
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"102030");
}
// check enum constants with wrong arguments
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
					  enum Y  {\s
				\t
					BLEU(10),
					BLANC(),
					ROUGE(30);
				
					int val;
					Y(int val) {
						this.val = val;
					}
				
					public static void main(String[] a) {
						for(Y y: values()) {
							System.out.print(y.val);
						}
					}
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				BLANC(),
				^^^^^
			The constructor Y() is undefined
			----------
			""");
}
// check enum constants with wrong arguments
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
					  enum Y  {\s
				\t
					BLEU(10) {
						String foo() { // inner
							return super.foo() + this.val;
						}
					},
					BLANC(20),
					ROUGE(30);
				
					int val;
					Y(int val) {
						this.val = val;
					}
					String foo() {  // outer
						return this.name();
					}
					public static void main(String[] args) {
						for(Y y: values()) {
							System.out.print(y.foo());
						}
					}
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"BLEU10BLANCROUGE");
}
// check enum constants with empty arguments
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
					  enum Y  {\s
				\t
					BLEU()
					  }
					}
				}
				"""
		},
		"");
}
// cannot extend enums
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
					  enum Y  {\s
					    BLEU()
					  }
					\t
					  class XX extends Y implements Y {
					  }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				class XX extends Y implements Y {
				                 ^
			The type Y cannot be the superclass of XX; a superclass must be a class
			----------
			2. ERROR in X.java (at line 7)
				class XX extends Y implements Y {
				                              ^
			The type Y cannot be a superinterface of XX; a superinterface must be an interface
			----------
			""");
}
// 74851
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				 enum MonthEnum {
				    JANUARY   (30),
				    FEBRUARY  (28),
				    MARCH     (31),
				    APRIL     (30),
				    MAY       (31),
				    JUNE      (30),
				    JULY      (31),
				    AUGUST    (31),
				    SEPTEMBER (31),
				    OCTOBER   (31),
				    NOVEMBER  (30),
				    DECEMBER  (31);
				   \s
				    private final int days;
				   \s
				    MonthEnum(int days) {
				        this.days = days;
				    }
				   \s
				    public int getDays() {
				    	boolean leapYear = true;
				    	switch(this) {
				    		case FEBRUARY: if(leapYear) return days+1;
				           default: return days;
				    	}
				    }
				   \s
				    public static void main(String[] args) {
				    	System.out.println(JANUARY.getDays());
				    }
				   \s
					  }
					  MonthEnum.main(args);
					}
				}
				""",
		},
		"30");
}
// 74226
public void test020() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo{
					 public static void main(String[] args) {
				       enum Rank {FIRST,SECOND,THIRD}
					 }
				}
				""",
		},
		"");
}
// 74226 variation - check nested enum is implicitly static
public void test021() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {
					 public static void main(String[] args) {
				      enum Rank {FIRST,SECOND,THIRD;
				            void bar() { foo(); }\s
				      }
					 }
				    void foo() {}
				}
				""",
		},
		"""
			----------
			1. ERROR in Foo.java (at line 4)
				void bar() { foo(); }\s
				             ^^^
			Cannot make a static reference to the non-static method foo() from the type Foo
			----------
			""");
}
// 77151 - cannot use qualified name to denote enum constants in switch case label
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
				\t
					void foo() {
					    enum MX { BLEU, BLANC, ROUGE }
						MX e = MX.BLEU;\s
						switch(e) {
							case MX.BLEU : break;
							case MX.BLANC : break;
							case MX.ROUGE : break;
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				case MX.BLEU : break;
				     ^^^^^^^
			The qualified case label MX.BLEU must be replaced with the unqualified enum constant BLEU
			----------
			2. ERROR in X.java (at line 9)
				case MX.BLANC : break;
				     ^^^^^^^^
			The qualified case label MX.BLANC must be replaced with the unqualified enum constant BLANC
			----------
			3. ERROR in X.java (at line 10)
				case MX.ROUGE : break;
				     ^^^^^^^^
			The qualified case label MX.ROUGE must be replaced with the unqualified enum constant ROUGE
			----------
			""");
}

// 77212
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					    enum RuleType{ SUCCESS, FAILURE }
						System.out.print(RuleType.valueOf(RuleType.SUCCESS.name()));
					}
				}""",
		},
		"SUCCESS");
}

// 77244 - cannot declare final enum
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				     final enum Y {
					    FOO() {}
				     }
					}
				}
				
				""",
		},
	"""
		----------
		1. ERROR in X.java (at line 3)
			final enum Y {
			           ^
		Illegal modifier for local enum Y; no explicit modifier is permitted
		----------
		""");
}

// values is using arraycopy instead of clone
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				      enum Y {
					SUC, CESS;
					public static void main(String[] args) {
						for (Y y : values()) {
							System.out.print(y.name());
						}
					}
					}
					Y.main(args);
				}
				}""",
		},
		"SUCCESS");
}

// check enum name visibility
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					   enum Couleur { BLEU, BLANC, ROUGE }
				   }
				  class Y {
					void foo(Couleur c) {
						switch (c) {
							case BLEU :
								break;
							case BLANC :
								break;
							case ROUGE :
								break;
						}
					}
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				void foo(Couleur c) {
				         ^^^^^^^
			Couleur cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 8)
				case BLEU :
				     ^^^^
			BLEU cannot be resolved to a variable
			----------
			3. ERROR in X.java (at line 10)
				case BLANC :
				     ^^^^^
			BLANC cannot be resolved to a variable
			----------
			4. ERROR in X.java (at line 12)
				case ROUGE :
				     ^^^^^
			ROUGE cannot be resolved to a variable
			----------
			""");
}
// check enum name visibility
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum Couleur { BLEU, BLANC, ROUGE }
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				  }
				}
				""",
		},
		"");
}
// check enum name visibility
public void test028() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum Couleur {\s
						BLEU, BLANC, ROUGE;
						static int C = 0;
						static void FOO() {}
					}
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
							FOO();
							C++;
						}\t
					}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 19)
				FOO();
				^^^
			The method FOO() is undefined for the type Y
			----------
			2. ERROR in X.java (at line 20)
				C++;
				^
			C cannot be resolved to a variable
			----------
			""");
}
// check enum name visibility
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum Couleur {\s
						BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type
					}
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				  }
				}
				
				class BLEU {}
				""",
		},
		"");
}
// check enum name visibility
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum Couleur {\s
						BLEU, BLANC, ROUGE; // take precedence over sibling constant from Color
					}
					enum Color {\s
						BLEU, BLANC, ROUGE;
					}
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				  }
				}
				
				class BLEU {}
				""",
		},
		"");
}
// check enum name visibility
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum Couleur {\s
						BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type
					}
					class Y implements IX, JX {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				  }
				}
				
				interface IX {
					int BLEU = 1;
				}
				interface JX {
					int BLEU = 2;
				}
				class BLEU {}
				
				""",
		},
		"");
}

// check Enum cannot be used as supertype (explicitly)
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     class Y extends Enum {
					}
				  }
				}""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				class Y extends Enum {
				                ^^^^
			Enum is a raw type. References to generic type Enum<E> should be parameterized
			----------
			2. ERROR in X.java (at line 3)
				class Y extends Enum {
				                ^^^^
			The type Y may not subclass Enum explicitly
			----------
			""");
}

// Javadoc in enum (see bug 78018)
public void test033() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
						/**
						 * Valid javadoc
						 * @author ffr
						 */
					 enum E {
						/** Valid javadoc */
						TEST,
						/** Valid javadoc */
						VALID;
						/** Valid javadoc */
						public void foo() {}
						}
					  }
					}
					"""
		}
	);
}
public void test034() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
						/**
						 * Invalid javadoc
						 * @exception NullPointerException Invalid tag
						 * @throws NullPointerException Invalid tag
						 * @return Invalid tag
						 * @param x Invalid tag
						 */
						 enum E { TEST, VALID }
					  }
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* @exception NullPointerException Invalid tag
				   ^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 6)
				* @throws NullPointerException Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			3. ERROR in X.java (at line 7)
				* @return Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			4. ERROR in X.java (at line 8)
				* @param x Invalid tag
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test035() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
						/**
						 * @see "Valid normal string"
						 * @see <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</a>
						 * @see Object
						 * @see #TEST
						 * @see E
						 * @see E#TEST
						 */
					    enum E { TEST, VALID }
					  }
					}"""
		}
	);
}
public void test036() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
						/**
						 * @see "invalid" no text allowed after the string
						 * @see <a href="invalid">invalid</a> no text allowed after the href
						 * @see
						 * @see #VALIDE
						 */
					    enum E { TEST, VALID }
					  }
					}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				* @see "invalid" no text allowed after the string
				                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			2. ERROR in X.java (at line 5)
				* @see <a href="invalid">invalid</a> no text allowed after the href
				                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			3. ERROR in X.java (at line 6)
				* @see
				   ^^^
			Javadoc: Missing reference
			----------
			4. ERROR in X.java (at line 7)
				* @see #VALIDE
				        ^^^^^^
			Javadoc: VALIDE cannot be resolved or is not a field
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test037() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
						/**
						 * Value test: {@value #TEST}
						 * or: {@value E#TEST}
						 */
					    enum E { TEST, VALID }
					  }
					}"""
		}
	);
}
public void test038() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    enum E { TEST, VALID;
					  public void foo() {}
				  }
				}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X {
				             ^
			Javadoc: Missing comment for public declaration
			----------
			2. ERROR in X.java (at line 2)
				public static void main(String[] args) {
				                   ^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test039() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    enum E {
						/**
						 * @exception NullPointerException Invalid tag
						 * @throws NullPointerException Invalid tag
						 * @return Invalid tag
						 * @param x Invalid tag
						 */
						TEST,
						VALID;
					  }
					}
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* @exception NullPointerException Invalid tag
				   ^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 6)
				* @throws NullPointerException Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			3. ERROR in X.java (at line 7)
				* @return Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			4. ERROR in X.java (at line 8)
				* @param x Invalid tag
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test040() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    enum E {
						/**
						 * @see E
						 * @see #VALID
						 */
						TEST,
						/**
						 * @see E#TEST
						 * @see E
						 */
						VALID;
						/**
						 * @param x the object
						 * @return String
						 * @see Object
						 */
						public String val(Object x) { return x.toString(); }
					  }
					}
					}
					"""
		}
	);
}
public void test041() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    enum E {
						/**
						 * @see e
						 * @see #VALIDE
						 */
						TEST,
						/**
						 * @see E#test
						 * @see EUX
						 */
						VALID;
						/**
						 * @param obj the object
						 * @return
						 * @see Objet
						 */
						public String val(Object x) { return x.toString(); }
					  }
					}
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* @see e
				       ^
			Javadoc: e cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 6)
				* @see #VALIDE
				        ^^^^^^
			Javadoc: VALIDE cannot be resolved or is not a field
			----------
			3. ERROR in X.java (at line 10)
				* @see E#test
				         ^^^^
			Javadoc: test cannot be resolved or is not a field
			----------
			4. ERROR in X.java (at line 11)
				* @see EUX
				       ^^^
			Javadoc: EUX cannot be resolved to a type
			----------
			5. ERROR in X.java (at line 15)
				* @param obj the object
				         ^^^
			Javadoc: Parameter obj is not declared
			----------
			6. ERROR in X.java (at line 16)
				* @return
				   ^^^^^^
			Javadoc: Description expected after @return
			----------
			7. ERROR in X.java (at line 17)
				* @see Objet
				       ^^^^^
			Javadoc: Objet cannot be resolved to a type
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test042() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    enum E {
						/**
						 * Test value: {@value #TEST}
						 */
						TEST,
						/**
						 * Valid value: {@value E#VALID}
						 */
						VALID;
						/**
						 * Test value: {@value #TEST}
						 * Valid value: {@value E#VALID}
						 * @param x the object
						 * @return String
						 */
						public String val(Object x) { return x.toString(); }
					  }
					}
					}
					"""
		}
	);
}

// External javadoc references to enum
public void _NAtest043() {
	this.runConformTest(
		new String[] {
			"test/E.java",
			"package test;\n" +
				"public enum E { TEST, VALID }\n",
			"test/X.java",
			"""
				import static test.E.TEST;
					/**
					 * @see test.E
					 * @see test.E#VALID
					 * @see #TEST
					 */
				public class X {}
				"""
		}
	);
}
public void _NAtest044() {
	this.runConformTest(
		new String[] {
			"test/E.java",
			"package test;\n" +
				"public enum E { TEST, VALID }\n",
			"test/X.java",
			"""
				import static test.E.TEST;
					/**
					 * Valid value = {@value test.E#VALID}
					 * Test value = {@value #TEST}
					 */
				public class X {}
				"""
		}
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78321
 */
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    enum Y {\n" +
			"      FIRST,\n" +
			"      SECOND,\n" +
			"      THIRD;\n" +
			"\n" +
			"      static {\n" +
			"        for (Y t : values()) {\n" +
			"          System.out.print(t.name());\n" +
			"        }\n" +
			"      }\n" +
			"\n" +
			"      Y() {}\n" +
			"      static void foo(){}\n" +
			"\n" +
			"     }\n" +
			"     Y.foo();\n" + // trigger the static block with a static method invocation
			"   }\n" +
			"}"
		},
		"FIRSTSECONDTHIRD"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78464
 */
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    enum Y{
				  a(1);
				  Y(int i) {
				  }
				  }
				  }
				}"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914
 */
public void test047() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    enum Y{\s
					;
					Y() {
						super();
					}
				  }
				   }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				super();
				^^^^^^^^
			Cannot invoke super constructor from enum constructor Y()
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77211
 */
public void test048() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    enum StopLight {
					    RED{
					        public StopLight next(){ return GREEN; }
					    },
					    GREEN{
					        public StopLight next(){ return YELLOW; }
					    },
					    YELLOW{
					        public StopLight next(){ return RED; }
					    };
					
					   public abstract StopLight next();
					   }
					   }
					}"""
		},
		""
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78915
 */
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    public abstract enum Y {}
				   }
				}"""
		},
	"""
		----------
		1. ERROR in X.java (at line 3)
			public abstract enum Y {}
			                     ^
		Illegal modifier for local enum Y; no explicit modifier is permitted
		----------
		"""
	);
}

public void test050() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					     enum Y {}
					   }
					}"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
					BLEU (0) {
					}
					;
					Y() {
						this(0);
					}
					Y(int i) {
					}
				   }
				   }
				}
				"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916
 */
public void test052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
					A
					;
				\t
					public abstract void foo();
				   }
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A
				^
			The enum constant A must implement the abstract method foo()
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test053() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
					A () { public void foo() {} }
					;
				\t
					public abstract void foo();
				   }
				   }
				}
				"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test054() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
					A() {}
					;
				\t
					public abstract void foo();
				   }
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A() {}
				^
			The enum constant A must implement the abstract method foo()
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test055() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
					;
				\t
					public abstract void foo();
				   }
				   }
				}
				"""
		},
	"""
		----------
		1. ERROR in X.java (at line 6)
			public abstract void foo();
			                     ^^^^^
		The enum Y can only define the abstract method foo() if it also defines enum constants with corresponding implementations
		----------
		"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test056() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
				    PLUS {
				        double eval(double x, double y) { return x + y; }
				    };
				
				    // Perform the arithmetic X represented by this constant
				    abstract double eval(double x, double y);
				   }
				   }
				}"""
		},
		""
	);
	String expectedOutput =
			"// Signature: Ljava/lang/Enum<LX$1Y;>;\n" +
			"abstract static enum X$1Y {\n" ;

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430
 */
public void test057() {
	this.runConformTest(
		new String[] {
			"Enum2.java",
			"""
				public class Enum2 {
				    public static void main(String[] args) {
				    	enum Color { RED, GREEN };
				        Color c= Color.GREEN;
				        switch (c) {
				        case RED:
				            System.out.println(Color.RED);
				            break;
				        case GREEN:
				            System.out.println(c);
				            break;
				        default: // nop
				        }
				    }
				}
				"""
		},
		"GREEN"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430 - variation
 */
public void test058() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A {
					public static void main(String[] args) {
					enum X { a }
					class B {
					 void _test(X x, int a) {
						if (x == a) a++; // incomparable types: X and int
						switch(x) {
							case a : System.out.println(a); // prints '9'
				           default: // nop
						}
					}
					 void _test2(X x, final int aa) {
						switch(x) {
							case aa : // unqualified enum constant error
								System.out.println(a); // cannot find a
				           default: // nop
						}
					}
					}
						new B()._test(X.a, 9);
						new B()._test2(X.a, 3);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (x == a) a++; // incomparable types: X and int
				    ^^^^^^
			Incompatible operand types X and int
			----------
			2. ERROR in X.java (at line 14)
				case aa : // unqualified enum constant error
				     ^^
			aa cannot be resolved or is not a field
			----------
			3. ERROR in X.java (at line 15)
				System.out.println(a); // cannot find a
				                   ^
			a cannot be resolved to a variable
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81262
 */
public void test059() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				     enum Y {
					  	MONDAY {
							public void foo() {
						}
					  };
						private Y() {}
						public static void main(String[] ags) {
					  		System.out.println("SUCCESS");
						}
					  }
					  Y.main(args);
					}
				}
				"""
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81589
 */
public void _NAtest060() {
	this.runNegativeTest(
		new String[] {
			"com/flarion/test/a/MyEnum.java",
			"""
				package com.flarion.test.a;
				public enum MyEnum {
				
				    First, Second;
				   \s
				}
				""",
			"com/flarion/test/b/MyClass.java",
			"package com.flarion.test.b;\n" +
			"import com.flarion.test.a.MyEnum;\n" +
			"import static com.flarion.test.a.MyEnum.First;\n" +
			"import static com.flarion.test.a.MyEnum.Second;\n" +
			"public class MyClass {\n" +
			"\n" +
			"    public void myMethod() {\n" +
			"        MyEnum e = MyEnum.First;\n" +
			"        switch (e) {\n" +
			"        case First:\n" +
			"            break;\n" +
			"        case Second:\n" +
			"            break;\n" +
			"        default: // nop\n" +
			"        }\n" +
			"        throw new Exception();\n" + // fake error to cause dump of unused import warnings
			"    }\n" +
			"}\n",
		},
		"""
			----------
			1. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 3)
				import static com.flarion.test.a.MyEnum.First;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The import com.flarion.test.a.MyEnum.First is never used
			----------
			2. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 4)
				import static com.flarion.test.a.MyEnum.Second;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The import com.flarion.test.a.MyEnum.Second is never used
			----------
			3. ERROR in com\\flarion\\test\\b\\MyClass.java (at line 16)
				throw new Exception();
				^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type Exception
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217
 */
public void test061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				
				class A {
					public void foo() {
						 enum X {
							A, B, C;
							public static final X D = null;
						}
						X x = X.A;
						switch (x) {
							case D:
						}
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The enum constant A needs a corresponding case label in this enum switch on X
			----------
			2. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The enum constant B needs a corresponding case label in this enum switch on X
			----------
			3. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The enum constant C needs a corresponding case label in this enum switch on X
			----------
			4. ERROR in X.java (at line 10)
				case D:
				     ^
			The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217 - variation with qualified name
 */
public void test062() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				
				class A {
					private void foo() {
						enum X {
							A, B, C;
							public static final X D = null;
						}
						X x = X.A;
						switch (x) {
							case X.D:
						}
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The switch over the enum type X should have a default case
			----------
			2. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The enum constant A needs a corresponding case label in this enum switch on X
			----------
			3. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The enum constant B needs a corresponding case label in this enum switch on X
			----------
			4. WARNING in X.java (at line 9)
				switch (x) {
				        ^
			The enum constant C needs a corresponding case label in this enum switch on X
			----------
			5. ERROR in X.java (at line 10)
				case X.D:
				       ^
			The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch
			----------
			""",
		null, // classlibs
		true, // flush
		options);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81945
 */
public void test063() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				  public static void main(String[] args) {
				  enum Option { ALPHA, BRAVO  };
				    Option item = Option.ALPHA;
				    switch (item) {
				    case ALPHA:      break;
				    case BRAVO:      break;
				    default:         break;
				    }
				  }
				}
				""",
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82590
 */
public void test064() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				interface B {
					public void _test();
				\t
					}
					 enum Y implements B {
				
						C1 {
							public void _test() {};
						},
						C2 {
							public void _test() {};
						}
					}
				
					}
				}
				""",
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83847
 */
public void test065() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum Y  {
				 		A;
				  		private void foo() {
				    		Y e= new Y() {
				    		};
				  		}
				  	  }
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				Y e= new Y() {
				         ^
			Cannot instantiate the type Y
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83860
 */
public void test066() {
    this.runConformTest(
        new String[] {
            "X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum Y  {
				    	SUCCESS (0) {};
				    	private Y(int i) {}
				    	public static void main(String[] args) {
				       	for (Y y : values()) {
				           	System.out.print(y);
				       	}
				   	}
				   }
				   Y.main(args);
				  }
				}""",
        },
        "SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83219
 */
public void test067() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum Y  {
				    	ONE, TWO, THREE;
				    	abstract int getSquare();
				    	abstract int getSquare();
				    }
				  }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 4)
				ONE, TWO, THREE;
				^^^
			The enum constant ONE must implement the abstract method getSquare()
			----------
			2. ERROR in X.java (at line 4)
				ONE, TWO, THREE;
				     ^^^
			The enum constant TWO must implement the abstract method getSquare()
			----------
			3. ERROR in X.java (at line 4)
				ONE, TWO, THREE;
				          ^^^^^
			The enum constant THREE must implement the abstract method getSquare()
			----------
			4. ERROR in X.java (at line 5)
				abstract int getSquare();
				             ^^^^^^^^^^^
			Duplicate method getSquare() in type Y
			----------
			5. ERROR in X.java (at line 6)
				abstract int getSquare();
				             ^^^^^^^^^^^
			Duplicate method getSquare() in type Y
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test068() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum Y  {
				    	A(1, 3), B(1, 3), C(1, 3) { }
				   	;
				    	public Y(int i, int j) { }
				    }
				  }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 6)
				public Y(int i, int j) { }
				       ^^^^^^^^^^^^^^^
			Illegal modifier for the enum constructor; only private is permitted.
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test069() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum Y  {
				    	A(1, 3), B(1, 3), C(1, 3) { }
				   	;
				    	protected Y(int i, int j) { }
				    }
				  }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 6)
				protected Y(int i, int j) { }
				          ^^^^^^^^^^^^^^^
			Illegal modifier for the enum constructor; only private is permitted.
			----------
			""");
}

public void test070() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum Y  {
				    	PLUS {
				        	double eval(double x, double y) { return x + y; }
				    	};
				
				    	// Perform the arithmetic X represented by this constant
				    	abstract double eval(double x, double y);
				    }
				  }
				}"""
		},
		""
	);
	String expectedOutput =
			"""
		  // Method descriptor #18 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X$1Y(java.lang.String arg0, int arg1);
		    0  aload_0 [this]
		    1  aload_1 [arg0]
		    2  iload_2 [arg1]
		    3  invokespecial java.lang.Enum(java.lang.String, int) [25]
		    6  return
		""";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test071() {
	this.runConformTest( // no methods to implement
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    enum X1 implements I {
					;
				    }
				  }
				}
				interface I {}
				"""
		},
		""
	);
	this.runConformTest( // no methods to implement with constant
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum X1a implements I {
						A;
				    }
				  }
				}
				interface I {}
				"""
		},
		""
	);
	this.runConformTest( // no methods to implement with constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				  	enum X1b implements I {
						A() { void random() {} };
					}
				  }
				}
				interface I {}
				"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test072() {
	this.runConformTest( // implement inherited method
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				  	enum X2 implements I {
					;
						public void _test() {}
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		""
	);
	this.runConformTest( // implement inherited method with constant
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					 enum X2a implements I {
					 	A;
					 public void _test() {}
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		""
	);
	this.runConformTest( // implement inherited method with constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
						enum X2b implements I {
							A() { public void _test() {} };
						public void _test() {}
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		""
	);
	this.runConformTest( // implement inherited method with random constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				 	 enum X2c implements I {
						A() { void random() {} };
						public void _test() {}
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test073() {
	this.runNegativeTest( // implement inherited method but as abstract
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
						enum X3 implements I {
					;
						public abstract void _test();
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public abstract void _test();
				                     ^^^^^^^
			The enum X3 can only define the abstract method _test() if it also defines enum constants with corresponding implementations
			----------
			"""
	);
	this.runNegativeTest( // implement inherited method as abstract with constant
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				  	enum X3a implements I {
						A;
						public abstract void _test();
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A;
				^
			The enum constant A must implement the abstract method _test()
			----------
			"""
	);
	this.runConformTest( // implement inherited method as abstract with constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum X3b implements I {
						A() { public void _test() {} };
						public abstract void _test();
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		""
	);
	this.runNegativeTest( // implement inherited method as abstract with random constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				 	 enum X3c implements I {
						A() { void random() {} };
					 public abstract void _test();
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A() { void random() {} };
				^
			The enum constant A must implement the abstract method _test()
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test074() {
	this.runNegativeTest( // define abstract method
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				  	enum X4 {
					;
						public abstract void _test();
					}
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public abstract void _test();
				                     ^^^^^^^
			The enum X4 can only define the abstract method _test() if it also defines enum constants with corresponding implementations
			----------
			"""
	);
	this.runNegativeTest( // define abstract method with constant
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				 	enum X4a {
						A;
						public abstract void _test();
					}
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A;
				^
			The enum constant A must implement the abstract method _test()
			----------
			"""
	);
	this.runConformTest( // define abstract method with constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum X4b {
						A() { public void _test() {} };
						public abstract void _test();
					}
				  }
				}
				"""
		},
		""
	);
	this.runNegativeTest( // define abstract method with random constant body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
						enum X4c {
							A() { void random() {} };
						public abstract void _test();
					}
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A() { void random() {} };
				^
			The enum constant A must implement the abstract method _test()
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void testNPE075() {
	this.runNegativeTest( // do not implement inherited method
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				  	enum X5 implements I {
					;
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum X5 implements I {
				     ^^
			The type X5 must implement the inherited abstract method I._test()
			----------
			"""
	);
	this.runNegativeTest( // do not implement inherited method & have constant with no body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum X5a implements I {
						A;
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum X5a implements I {
				     ^^^
			The type X5a must implement the inherited abstract method I._test()
			----------
			"""
	);
	this.runConformTest( // do not implement inherited method & have constant with body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				  	enum X5b implements I {
						A() { public void _test() {} };
					;
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		""
	);
	this.runNegativeTest( // do not implement inherited method & have constant with random body
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
					enum X5c implements I {
						A() { void random() {} };
						;
					private X5c() {}
					}
				  }
				}
				interface I { void _test(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A() { void random() {} };
				^
			The enum constant A must implement the abstract method _test()
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
public void test076() { // bridge method needed
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				enum E implements I {
					A;
					public E foo() {
						System.out.println("SUCCESS");
						return null;
					}
				}
					public static void main(String[] args) { ((I) E.A).foo(); }
				}
				interface I { I foo(); }
				"""
		},
		"SUCCESS"
	);
}

public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
				enum E {
					A {
						void bar() {
							new M();
						}
					};
					abstract void bar();
				\t
					class M {
						M() {
							System.out.println("SUCCESS");
						}
					}
				}
						E.A.bar();
					}
				}
				"""
		},
		"SUCCESS"
	);
}

public void test078() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
				enum E {
					A {
						void bar() {
							new X(){
								void baz() {
									new M();
								}
							}.baz();
						}
					};
					abstract void bar();
				\t
					class M {
						M() {
							System.out.println("SUCCESS");
						}
					}
				}
						E.A.bar();
					}
				}
				"""
		},
		"SUCCESS"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85397
public void test079() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
				     enum Y {
						A, B;
						private strictfp Y() {}
					  }
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				private strictfp Y() {}
				                 ^^^
			Illegal modifier for the constructor in type Y; only public, protected & private are permitted
			----------
			"""
	);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						strictfp enum Y {
						A, B;
						private Y() {}
					  }
					}
				}
				"""
		},
		""
	);

	String[] expectedOutputs = new String[] {
		"  private strictfp X$1Y(java.lang.String arg0, int arg1);\n",
		"  public static strictfp new X(){}[] values();\n",
		"  public static strictfp new X(){} valueOf(java.lang.String arg0);\n"
	};

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	for (int i = 0, max = expectedOutputs.length; i < max; i++) {
		String expectedOutput = expectedOutputs[i];
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87064
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface TestInterface {
					int test();
				}
				
				public class X {
					public static void main(String[] args) {
				   	enum Y implements TestInterface {
						TEST {
							public int test() {
								return 42;
							}
						},
						ENUM {
							public int test() {
								return 37;
							}
						};
					  }
					}
				}\s
				"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87818
public void test081() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {}
					void foo() {
						enum E {}
					}
				}"""
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88223
public void test082() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					class Y {
						enum E {}
					}
					}
				}"""
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					 class Y {
						enum E {}
					}
				}
				}"""
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {}
					void foo() {
						class Local {
							enum E {}
						}
					}
				}"""
		},
		"");
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check no emulation warning
public void test083() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				 		enum Y {
					INPUT {
						@Override
						public Y getReverse() {
							return OUTPUT;
						}
					},
					OUTPUT {
						@Override
						public Y getReverse() {
							return INPUT;
						}
					},
					INOUT {
						@Override
						public Y getReverse() {
							return INOUT;
						}
					};
					Y(){}
				  Zork z;
					public abstract Y getReverse();
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 23)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check private constructor generation
public void test084() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				 		enum Y {
					INPUT {
						@Override
						public Y getReverse() {
							return OUTPUT;
						}
					},
					OUTPUT {
						@Override
						public Y getReverse() {
							return INPUT;
						}
					},
					INOUT {
						@Override
						public Y getReverse() {
							return INOUT;
						}
					};
					Y(){}
					public abstract Y getReverse();
						}
						}
				}
				""",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #20 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X$1Y(java.lang.String arg0, int arg1);
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88625
public void test085() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					enum Test1 {
						test11, test12
					};
					enum Test2 {
						test21, test22
					};
				
				  class Y {
					void foo1(Test1 t1, Test2 t2) {
						boolean b = t1 == t2;
					}
					void foo2(Test1 t1, Object t2) {
						boolean b = t1 == t2;
					}
					void foo3(Test1 t1, Enum t2) {
						boolean b = t1 == t2;
					}
					public void foo() {
						boolean booleanTest = (Test1.test11 == Test2.test22);
					}
					}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				boolean b = t1 == t2;
				            ^^^^^^^^
			Incompatible operand types Test1 and Test2
			----------
			2. WARNING in X.java (at line 17)
				void foo3(Test1 t1, Enum t2) {
				                    ^^^^
			Enum is a raw type. References to generic type Enum<E> should be parameterized
			----------
			3. ERROR in X.java (at line 21)
				boolean booleanTest = (Test1.test11 == Test2.test22);
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Incompatible operand types Test1 and Test2
			----------
			""");
}
public void test086() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					enum Test1 {
						V;
						static int foo = 0;
					}
					}
				}
				""",
		},
		"");
}
public void _test087_more_meaningful_error_msg_required() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
					enum Test1 {
						V;
						interface Foo {}
					}
					}
				}
				""",
		},
		"");
}
public void test088() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				
					enum Test1 {
						V;
					}
					}
					Object foo() {
						return this;
					}
				
				}
				""",
		},
		"");
}
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
				
					enum Test1 {
						V;
						protected final Test1 clone() { return V; }
					}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				protected final Test1 clone() { return V; }
				                      ^^^^^^^
			Cannot override the final method from Enum<Test1>
			----------
			2. WARNING in X.java (at line 6)
				protected final Test1 clone() { return V; }
				                      ^^^^^^^
			The method clone() of type Test1 should be tagged with @Override since it actually overrides a superclass method
			----------
			""");
}
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
					enum Test1 {
						V;
						public Test1 foo() { return V; }
					}
					}
					Zork z;
				}
				""",
			"java/lang/Object.java",
			"""
				package java.lang;
				public class Object {
					public Object foo() { return this; }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				public Test1 foo() { return V; }
				             ^^^^^
			The method foo() of type Test1 should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 9)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
public void test091() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
					enum Test1 {
						V;
						void foo() {}
					}
					class Member<E extends Test1> {
						void bar(E e) {
							e.foo();
						}
					}
					}
				}
				""",
		},
		"");
}
public void test092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
					enum Test1 {
						V;
						void foo() {}
					}
					class Member<E extends Object & Test1> {
						void bar(E e) {
							e.foo();
						}
					}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				class Member<E extends Object & Test1> {
				                                ^^^^^
			The type Test1 is not an interface; it cannot be specified as a bounded parameter
			----------
			2. ERROR in X.java (at line 10)
				e.foo();
				  ^^^
			The method foo() is undefined for the type E
			----------
			""");
}
// check wildcard can extend Enum superclass
public void test093() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
					enum Test1 {
						V;
						void foo() {}
					}
					class Member<E extends Test1> {
						E e;
						void bar(Member<? extends Test1> me) {
						}
					}
					}
				}
				""",
		},
		"");
}
// check super bit is set
public void test094() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						enum Y {}
					}
				}
				""",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		// Signature: Ljava/lang/Enum<LX$1Y;>;
		static final enum X$1Y {
		 \s
		  // Field descriptor #6 [LX$1Y;
		  private static final synthetic X$1Y[] ENUM$VALUES;
		 \s
		  // Method descriptor #8 ()V
		  // Stack: 1, Locals: 0
		  static {};
		    0  iconst_0
		    1  anewarray X$1Y [1]
		    4  putstatic X$1Y.ENUM$VALUES : new X(){}[] [10]
		    7  return
		      Line numbers:
		        [pc: 0, line: 3]
		 \s
		  // Method descriptor #15 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X$1Y(java.lang.String arg0, int arg1);
		    0  aload_0 [this]
		    1  aload_1 [arg0]
		    2  iload_2 [arg1]
		    3  invokespecial java.lang.Enum(java.lang.String, int) [16]
		    6  return
		      Line numbers:
		        [pc: 0, line: 3]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: new X(){}
		 \s
		  // Method descriptor #21 ()[LX$1Y;
		  // Stack: 5, Locals: 3
		  public static new X(){}[] values();
		     0  getstatic X$1Y.ENUM$VALUES : new X(){}[] [10]
		     3  dup
		     4  astore_0
		     5  iconst_0
		     6  aload_0
		     7  arraylength
		     8  dup
		     9  istore_1
		    10  anewarray X$1Y [1]
		    13  dup
		    14  astore_2
		    15  iconst_0
		    16  iload_1
		    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [22]
		    20  aload_2
		    21  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		 \s
		  // Method descriptor #29 (Ljava/lang/String;)LX$1Y;
		  // Stack: 2, Locals: 1
		  public static new X(){} valueOf(java.lang.String arg0);
		     0  ldc <Class X$1Y> [1]
		     2  aload_0 [arg0]
		     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [30]
		     6  checkcast X$1Y [1]
		     9  areturn
		""" ;


	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
public void testNPE095() { // check missing abstract cases from multiple interfaces
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y implements I, J {\s
							ROUGE;
						}
					}
				}
				interface I { void foo(); }
				interface J { void foo(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum Y implements I, J {\s
				     ^
			The type Y must implement the inherited abstract method J.foo()
			----------
			""");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y implements I, J {\s
							ROUGE;
							public void foo() {}
						}
					}
				}
				interface I { void foo(int i); }
				interface J { void foo(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum Y implements I, J {\s
				     ^
			The type Y must implement the inherited abstract method I.foo(int)
			----------
			""");
}
public void testNPE096() { // check for raw vs. parameterized parameter types
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y implements I {\s
							ROUGE;
							public void foo(A a) {}
						}
					}
				}
				interface I { void foo(A<String> a); }
				class A<T> {}
				"""
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						 enum Y implements I {\s
							ROUGE { public void foo(A a) {} }
							;
						}
					}
				}
				interface I { void foo(A<String> a); }
				class A<T> {}
				"""
		},
		"");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						 enum Y implements I {\s
							ROUGE;
							public void foo(A<String> a) {}
						}
					}
				}
				interface I { void foo(A a); }
				class A<T> {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum Y implements I {\s
				     ^
			The type Y must implement the inherited abstract method I.foo(A)
			----------
			2. ERROR in X.java (at line 5)
				public void foo(A<String> a) {}
				            ^^^^^^^^^^^^^^^^
			Name clash: The method foo(A<String>) of type Y has the same erasure as foo(A) of type I but does not override it
			----------
			3. WARNING in X.java (at line 9)
				interface I { void foo(A a); }
				                       ^
			A is a raw type. References to generic type A<T> should be parameterized
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982
public void test097() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public class E {
					enum Numbers { ONE, TWO, THREE }
					static final String BLANK = "    ";
					void foo() {
						/**
						 * Enumeration of some basic colors.
						 */
						enum Colors {
							BLACK,
							WHITE,
							RED \s
						}
						Colors color = Colors.BLACK;
						switch (color) {
							case BLUE:
							case RED:
								break;
							default: // nop
						}\s
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in E.java (at line 15)
				case BLUE:
				     ^^^^
			BLUE cannot be resolved or is not a field
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982 - variation
public void test098() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public class E {
					enum Numbers { ONE, TWO, THREE }
					static final String BLANK = "    ";
					void foo() {
						/**
						 * Enumeration of some basic colors.
						 */
						enum Colors {
							BLACK,
							WHITE,
							RED; \s
				  			Zork z;
						}
						Colors color = Colors.BLACK;
						switch (color) {
						}\s
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in E.java (at line 12)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in E.java (at line 15)
				switch (color) {
				        ^^^^^
			The enum constant BLACK needs a corresponding case label in this enum switch on Colors
			----------
			3. WARNING in E.java (at line 15)
				switch (color) {
				        ^^^^^
			The enum constant RED needs a corresponding case label in this enum switch on Colors
			----------
			4. WARNING in E.java (at line 15)
				switch (color) {
				        ^^^^^
			The enum constant WHITE needs a corresponding case label in this enum switch on Colors
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89274
public void _NAtest099() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
					public static void main(String[] args) {
					enum E {
						v1, v2;
					}
					public class X extends A<Integer> {
						void a(A.E e) {
							b(e); // no unchecked warning
						}
				
						void b(E e) {
							A<Integer>.E e1 = e;
						}
					}
				}
				}
				
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				A<Integer>.E e1 = e;
				^^^^^^^^^^^^
			The member type A.E cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type A<Integer>
			----------
			""");
}
/* from JLS
"It is a compile-time error to reference a static field of an enum type
that is not a compile-time constant (15.28) from constructors, instance
initializer blocks, or instance variable initializer expressions of that
type.  It is a compile-time error for the constructors, instance initializer
blocks, or instance variable initializer expressions of an enum constant e1
to refer to itself or an enum constant of the same type that is declared to
the right of e1."
	*/
public void testNPE100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y {
				
							anEnumValue {
								private final Y thisOne = anEnumValue;
				
								@Override String getMessage() {
									return "Here is what thisOne gets assigned: " + thisOne;
								}
							};
				
							abstract String getMessage();
				
							public static void main(String[] arguments) {
								System.out.println(anEnumValue.getMessage());
								System.out.println("SUCCESS");
							}
						}
					}
				
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				private final Y thisOne = anEnumValue;
				                          ^^^^^^^^^^^
			Cannot refer to the static enum field Y.anEnumValue within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91761
public void test101() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Foo {
				  public boolean bar();
				}
				public class X {\s
					public static void main(String[] args) {
				enum BugDemo {
				  CONSTANT(new Foo() {
				    public boolean bar() {
				      Zork z;
				      return true;
				    }
				  });
				  BugDemo(Foo foo) {
				  }
				  }
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90775
public void test102() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.util.*;
				
				public class X <T> {
					public static void main(String[] args) {
					enum SomeEnum {
						A, B;
						static SomeEnum foo() {
							return null;
						}
					}
					Enum<SomeEnum> e = SomeEnum.A;
					\t
					Set<SomeEnum> set1 = EnumSet.of(SomeEnum.A);
					Set<SomeEnum> set2 = EnumSet.of(SomeEnum.foo());
				\t
					Foo<Bar> foo = null;
				}
				}
				class Foo <U extends Foo<U>> {
				}
				class Bar extends Foo {
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 16)
				Foo<Bar> foo = null;
				    ^^^
			Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <U extends Foo<U>> of the type Foo<U>
			----------
			2. WARNING in X.java (at line 21)
				class Bar extends Foo {
				                  ^^^
			Foo is a raw type. References to generic type Foo<U> should be parameterized
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93396
public void test103() {
    this.runNegativeTest(
        new String[] {
            "BadEnum.java",
            """
				public class BadEnum {
				  public interface EnumInterface<T extends Object> {
				    public T getMethod();
				  }
					public static void main(String[] args) {
				   enum EnumClass implements EnumInterface<String> {
				    ENUM1 { public String getMethod() { return "ENUM1";} },
				    ENUM2 { public String getMethod() { return "ENUM2";} };
				  }
				}
				}
				}
				""",
        },
        """
			----------
			1. ERROR in BadEnum.java (at line 12)
				}
				^
			Syntax error on token "}", delete this token
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90215
public void _NA_test104() {
    this.runConformTest(
        new String[] {
            "p/Placeholder.java",
			"package p;\n" +
			"\n" +
			"public class Placeholder {\n" +
			"    public static void main(String... argv) {\n" +
			"        ClassWithBadEnum.EnumClass constant = ClassWithBadEnum.EnumClass.ENUM1;\n" + // forward ref
			"        ClassWithBadEnum.main(argv);\n" +
			"	}\n" +
			"}    \n" +
			"\n",
            "p/ClassWithBadEnum.java",
			"""
				package p;
				
				public class ClassWithBadEnum {
					public interface EnumInterface<T extends Object> {
					    public T getMethod();
					}
				
					public enum EnumClass implements EnumInterface<String> {
						ENUM1 { public String getMethod() { return "ENUM1";} },
						ENUM2 { public String getMethod() { return "ENUM2";} };
					}
					private EnumClass enumVar;\s
					public EnumClass getEnumVar() {
						return enumVar;
					}
					public void setEnumVar(EnumClass enumVar) {
						this.enumVar = enumVar;
					}
				
					public static void main(String... argv) {
						int a = 1;
						ClassWithBadEnum badEnum = new ClassWithBadEnum();
						badEnum.setEnumVar(ClassWithBadEnum.EnumClass.ENUM1);
						// Should fail if bug manifests itself because there will be two getInternalValue() methods
						// one returning an Object instead of a String
						String s3 = badEnum.getEnumVar().getMethod();
						System.out.println(s3);
					}
				} \s
				""",
        },
        "ENUM1");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void _NA_test105() {
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        default: // nop
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black"
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test106() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        }
							 System.out.print("SUCCESS");
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"SUCCESS",
		null,
		false,
		null,
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test107() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"BlackBlack",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color { BLACK }"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"BlackBlack",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test108() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        default:
					            System.out.print("Error");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {RED, GREEN, YELLOW, BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test109() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
							Color c = null;
							 try {
					        	c = BLACK;
							} catch(NoSuchFieldError e) {
								System.out.print("SUCCESS");
								return;
							}
					      	switch(c) {
					       	case BLACK:
					          	System.out.print("Black");
					          	break;
					       	case WHITE:
					          	System.out.print("White");
					          	break;
					      	}
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {RED, GREEN, YELLOW, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"SUCCESS",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NAtest110() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
						public int[] $SWITCH_TABLE$pack$Color;
						public int[] $SWITCH_TABLE$pack$Color() { return null; }
					   public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void  _NA_test111() {
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					@SuppressWarnings("incomplete-switch")
					public class X {
						public int[] $SWITCH_TABLE$pack$Color;
						public int[] $SWITCH_TABLE$pack$Color() { return null; }
					   public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
							 foo();
					    }
					   public static void foo() {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"BlackBlack"
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"BlackBlack",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97247
public void _NA_test112() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"com/annot/X.java",
			"""
				package com.annot;
				import java.lang.annotation.Target;
				import java.lang.annotation.ElementType;
				
				public class X {
				   public static void main(String[] args) {
				  enum TestType {
				    CORRECTNESS,
				    PERFORMANCE
				  }
				  @Target(ElementType.METHOD)
				  @interface Test {
				    TestType type() default TestType.CORRECTNESS;
				  }
				  @Test(type=TestType.PERFORMANCE)
				  public void _testBar() throws Exception {
				    Test annotation = this.getClass().getMethod("testBar").getAnnotation(Test.class);
				    switch (annotation.type()) {
				      case PERFORMANCE:
				        System.out.println(TestType.PERFORMANCE);
				        break;
				      case CORRECTNESS:
				        System.out.println(TestType.CORRECTNESS);
				        break;
				    }  \s
				  }
				  }
				}"""
		},
		"",
		null,
		true,
		new String[] {"--enable-preview"},
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93789
public void _test113() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
				   public static void main(String[] args) {
						enum BugDemo {
							FOO() {
								static int bar;
							}
				  		}
				  	}
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 5)
				static int bar;
				           ^^^
			The field bar cannot be declared static in a non-static inner type, unless initialized with a constant expression
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99428 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=99655
public void test114() {
    this.runConformTest(
        new String[] {
            "LocalEnumTest.java",
			"import java.lang.reflect.*;\n" +
			"import java.lang.annotation.*;\n" +
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"@Retention(RetentionPolicy.RUNTIME)\n" +
			"@interface ExpectedModifiers {\n" +
			"	int value();\n" +
			"}\n" +
			"@ExpectedModifiers(Modifier.FINAL)\n" +
			"public enum LocalEnumTest {\n" +
			"	X(255);\n" +
			"	LocalEnumTest(int r) {}\n" +
			"	public static void main(String argv[]) throws Exception {\n" +
			"		test(\"LocalEnumTest\");\n" +
			"		test(\"LocalEnumTest$1EnumA\");\n" +
			"		test(\"LocalEnumTest$1EnumB\");\n" +
			"		test(\"LocalEnumTest$1EnumB2\");\n" +
			"		test(\"LocalEnumTest$1EnumB3\");\n" +
			// TODO (kent) need verifier to detect when an Enum should be tagged as abstract
			//"		test(\"LocalEnumTest$EnumC\");\n" +
			//"		test(\"LocalEnumTest$EnumC2\");\n" +
			"		test(\"LocalEnumTest$1EnumC3\");\n" +
			"		test(\"LocalEnumTest$1EnumD\");\n" +
			"		@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"		enum EnumA {\n" +
			"			A;\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumB {\n" +
			"			B {\n" +
			"				int value() { return 1; }\n" +
			"			};\n" +
			"			int value(){ return 0; }\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumB2 {\n" +
			"			B2 {};\n" +
			"			int value(){ return 0; }\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"		enum EnumB3 {\n" +
			"			B3;\n" +
			"			int value(){ return 0; }\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumC implements I {\n" +
			"			C {\n" +
			"				int value() { return 1; }\n" +
			"			};\n" +
			"			int value(){ return 0; }\n" +
			"			public void foo(){}\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.STATIC)\n" +
			"		enum EnumC2 implements I {\n" +
			"			C2 {};\n" +
			"			int value(){ return 0; }\n" +
			"			public void foo(){}\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" +
			"		enum EnumC3 implements I {\n" +
			"			C3;\n" +
			"			int value(){ return 0; }\n" +
			"			public void foo(){}\n" +
			"		}\n" +
			"		@ExpectedModifiers(Modifier.ABSTRACT|Modifier.STATIC)\n" +
			"		enum EnumD {\n" +
			"			D {\n" +
			"				int value() { return 1; }\n" +
			"			};\n" +
			"			abstract int value();\n" +
			"		}\n" +
			"	}\n" +
			"	static void test(String className) throws Exception {\n" +
			"		Class c = Class.forName(className);\n" +
			"		ExpectedModifiers em = (ExpectedModifiers) c.getAnnotation(ExpectedModifiers.class);\n" +
			"		if (em != null) {\n" +
			"			int classModifiers = c.getModifiers();\n" +
			"			int expected = em.value();\n" +
			"			if (expected != (classModifiers & (Modifier.ABSTRACT|Modifier.FINAL|Modifier.STATIC))) {\n" +
			"				if ((expected & Modifier.ABSTRACT) != (classModifiers & Modifier.ABSTRACT))\n" +
			"					System.out.println(\"FAILED ABSTRACT: \" + className);\n" +
			"				if ((expected & Modifier.FINAL) != (classModifiers & Modifier.FINAL))\n" +
			"					System.out.println(\"FAILED FINAL: \" + className);\n" +
			"				if ((expected & Modifier.STATIC) != (classModifiers & Modifier.STATIC))\n" +
			"					System.out.println(\"FAILED STATIC: \" + className);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713
public void test115() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						enum Y {
							VALUE;
				
							static int ASD;
							final static int CST = 0;
						\t
							private Y() {
								VALUE = null;
								ASD = 5;
								Y.VALUE = null;
								Y.ASD = 5;
							\t
								System.out.println(CST);
							}
						}
					}
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 10)
				VALUE = null;
				^^^^^
			Cannot refer to the static enum field Y.VALUE within an initializer
			----------
			2. ERROR in X.java (at line 11)
				ASD = 5;
				^^^
			Cannot refer to the static enum field Y.ASD within an initializer
			----------
			3. ERROR in X.java (at line 12)
				Y.VALUE = null;
				  ^^^^^
			Cannot refer to the static enum field Y.VALUE within an initializer
			----------
			4. ERROR in X.java (at line 13)
				Y.ASD = 5;
				  ^^^
			Cannot refer to the static enum field Y.ASD within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						enum Y {
							BLEU,\s
							BLANC,\s
							ROUGE;
							{
								BLEU = null;
							}
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				BLEU = null;
				^^^^
			Cannot refer to the static enum field Y.BLEU within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test117() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						enum Y {
							BLEU,\s
							BLANC,\s
							ROUGE;
							{
								Y x = BLEU.BLANC; // ko
								Y x2 = BLEU; // ko
							}
							static {
								Y x = BLEU.BLANC; // ok
								Y x2 = BLEU; // ok
							}\t
							Y dummy = BLEU; // ko
							static Y DUMMY = BLANC; // ok
							Y() {
								Y x = BLEU.BLANC; // ko
								Y x2 = BLEU; // ko
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				Y x = BLEU.BLANC; // ko
				      ^^^^
			Cannot refer to the static enum field Y.BLEU within an initializer
			----------
			2. ERROR in X.java (at line 8)
				Y x = BLEU.BLANC; // ko
				           ^^^^^
			Cannot refer to the static enum field Y.BLANC within an initializer
			----------
			3. WARNING in X.java (at line 8)
				Y x = BLEU.BLANC; // ko
				           ^^^^^
			The static field Y.BLANC should be accessed in a static way
			----------
			4. ERROR in X.java (at line 9)
				Y x2 = BLEU; // ko
				       ^^^^
			Cannot refer to the static enum field Y.BLEU within an initializer
			----------
			5. WARNING in X.java (at line 12)
				Y x = BLEU.BLANC; // ok
				           ^^^^^
			The static field Y.BLANC should be accessed in a static way
			----------
			6. ERROR in X.java (at line 15)
				Y dummy = BLEU; // ko
				          ^^^^
			Cannot refer to the static enum field Y.BLEU within an initializer
			----------
			7. ERROR in X.java (at line 18)
				Y x = BLEU.BLANC; // ko
				      ^^^^
			Cannot refer to the static enum field Y.BLEU within an initializer
			----------
			8. ERROR in X.java (at line 18)
				Y x = BLEU.BLANC; // ko
				           ^^^^^
			Cannot refer to the static enum field Y.BLANC within an initializer
			----------
			9. WARNING in X.java (at line 18)
				Y x = BLEU.BLANC; // ko
				           ^^^^^
			The static field Y.BLANC should be accessed in a static way
			----------
			10. ERROR in X.java (at line 19)
				Y x2 = BLEU; // ko
				       ^^^^
			Cannot refer to the static enum field Y.BLEU within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102265
public void test118() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
					public static void main(String argv[]) {
						enum Y {
							 one,
							 two;
							\s
							 static ArrayList someList;
							\s
							 private Y() {
							 		 if (someList == null) {
							 		 		 someList = new ArrayList();
							 		 }
							 }
						}
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 9)
				static ArrayList someList;
				       ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			2. ERROR in X.java (at line 12)
				if (someList == null) {
				    ^^^^^^^^
			Cannot refer to the static enum field Y.someList within an initializer
			----------
			3. ERROR in X.java (at line 13)
				someList = new ArrayList();
				^^^^^^^^
			Cannot refer to the static enum field Y.someList within an initializer
			----------
			4. WARNING in X.java (at line 13)
				someList = new ArrayList();
				               ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			""");
}
public void test119() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						enum Y {
							BLEU, BLANC, ROUGE;
							final static int CST = 0;
				          		enum Member {
				          			;
				             	 	Object obj1 = CST;
				              		Object obj2 = BLEU;
				          		}
				       	}
				     }
				}
				"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102213
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						enum Y {
				
							A() {
								final Y a = A;
								final Y a2 = B.A;
								@Override void foo() {
									System.out.println(String.valueOf(a));
									System.out.println(String.valueOf(a2));
								}
							},
							B() {
								@Override void foo(){}
							};
							abstract void foo();
						\t
							public static void main(String[] args) {
								A.foo();
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				final Y a = A;
				            ^
			Cannot refer to the static enum field Y.A within an initializer
			----------
			2. ERROR in X.java (at line 7)
				final Y a2 = B.A;
				             ^
			Cannot refer to the static enum field Y.B within an initializer
			----------
			3. ERROR in X.java (at line 7)
				final Y a2 = B.A;
				               ^
			Cannot refer to the static enum field Y.A within an initializer
			----------
			4. WARNING in X.java (at line 7)
				final Y a2 = B.A;
				               ^
			The static field Y.A should be accessed in a static way
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=92165
public void test121() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {\s
					public static void main(String[] args) {
				enum Y {
				
					UNKNOWN();
				
					private static String error;
				
					{
						error = "error";
					}
				}
				}
				
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				error = "error";
				^^^^^
			Cannot refer to the static enum field Y.error within an initializer
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105592
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo() {
						enum State {
							NORMAL
				}
						State state = State.NORMAL;
						switch (state) {
						case (NORMAL) :
							System.out.println(State.NORMAL);
							break;
				       default: // nop
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				case (NORMAL) :
				     ^^^^^^^^
			Enum constants cannot be surrounded by parenthesis
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110403
public void test123() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {\s
					public static void main(String[] args) {
				enum Y {
				 A(0);
				 Y(int x) {
				    t[0]=x;
				 }
				 private static final int[] t = new int[12];
				}
				}
				
				}
				"""
		},
		"""
			----------
			1. ERROR in Foo.java (at line 6)
				t[0]=x;
				^
			Cannot refer to the static enum field Y.t within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=1101417
public void test124() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				 public class X {
					public void foo() {
						enum Y {
						max {
						{\s
								val=3; \s
							}        \s
							@Override public String toString() {
								return Integer.toString(val);
							}
						};\s
						{
							val=2;
						}
						private int val;\s
						}
				}
				  public static void main(String[] args) {
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				val=3; \s
				^^^
			Cannot make a static reference to the non-static field val
			----------
			2. ERROR in X.java (at line 9)
				return Integer.toString(val);
				                        ^^^
			Cannot make a static reference to the non-static field val
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=112231
public void test125() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				
				public class X {
					interface I {
						default int values(){
						enum E implements I {
							A, B, C;
						}
					}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				enum E implements I {
				     ^
			This static method cannot hide the instance method from X.I
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
public void test126() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				  public class X {
				    public static void main(String[] args) {
				      enum NoValues {}
				      System.out.println("["+NoValues.values().length+"]");
				    }
				  }
				"""
		},
		"[0]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
// Commented and created https://bugs.eclipse.org/bugs/show_bug.cgi?id=570106
public void test127() throws Exception {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String[] args) {
							enum Y {
								VALUE {
									void foo() {
									};
								};
								abstract void foo();
							}
					      System.out.println("["+Y.values().length+"]");
					    }
					}"""
		},
		"[1]");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		// Compiled from X.java (version 16 : 60.0, super bit)
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  getstatic java.lang.System.out : java.io.PrintStream [16]
		     3  invokestatic X$1Y.values() : X$1Y[] [22]
		     6  arraylength
		     7  invokedynamic 0 makeConcatWithConstants(int) : java.lang.String [28]
		    12  invokevirtual java.io.PrintStream.println(java.lang.String) : void [32]
		    15  return
		      Line numbers:
		        [pc: 0, line: 10]
		        [pc: 15, line: 11]
		      Local variable table:
		        [pc: 0, pc: 16] local: args index: 0 type: java.lang.String[]
		
		  Inner classes:
		    [inner class info: #23 X$1Y, outer class info: #0
		     inner name: #52 Y, accessflags: 17416 abstract static],
		    [inner class info: #53 java/lang/invoke/MethodHandles$Lookup, outer class info: #55 java/lang/invoke/MethodHandles
		     inner name: #57 Lookup, accessflags: 25 public static final]
		
		Nest Members:
		   #23 X$1Y,
		   #59 X$1Y$1
		Bootstrap methods:
		  0 : # 48 invokestatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			Method arguments:
				#49 []
		}""";

	int index = actualOutput.indexOf(expectedOutput);

	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}

	disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1Y$1.class"));
	actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	expectedOutput =
		"ENUM$VALUES";

	index = actualOutput.indexOf(expectedOutput);
	if (index != -1) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index != -1) {
		assertTrue("Must not have field ENUM$VALUES", false);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127766
public void test128() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);

	this.runNegativeTest(
         new String[] {
        		 "X.java",
        		 """
					public class X {
						public static void main( String[] args) {
							Enum e = new Enum("foo", 2) {
								public int compareTo( Object o) {
									return 0;
								}
							};
							System.out.println(e);
						}
					}""",
         },
         """
			----------
			1. ERROR in X.java (at line 3)
				Enum e = new Enum("foo", 2) {
				             ^^^^
			The type new Enum(){} may not subclass Enum explicitly
			----------
			""",
         null,
         true,
         options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141155
public void test129() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				  public class X {
				    public void foo(){
						enum Y {
				        A, B, C;
						}
					}
					public static void main(String[] args) {
				    }
				}
				""",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 0, Locals: 1
		  public void foo();
		    0  return
		      Line numbers:
		        [pc: 0, line: 6]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: X
		 \s
		  // Method descriptor #16 ([Ljava/lang/String;)V
		  // Stack: 0, Locals: 1
		  public static void main(java.lang.String[] args);
		    0  return
		      Line numbers:
		        [pc: 0, line: 8]
		      Local variable table:
		        [pc: 0, pc: 1] local: args index: 0 type: java.lang.String[]
		
		  Inner classes:
		    [inner class info: #22 X$1Y, outer class info: #0
		     inner name: #24 Y, accessflags: 16408 static final]
		
		Nest Members:
		   #22 X$1Y
		}""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141810
public void test130() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] args) {
					      enum Action {ONE, TWO}
					      for(Action a : Action.values()) {
					         switch(a) {
					         case ONE:
					            System.out.print("1");
					            break;
					         case TWO:
					            System.out.print("2");
					            break;
					         default:
					            System.out.print("default");
					         }
					      }
					   }
					}""",
			},
			"12"
		);

	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] args) {
					      enum Action {ONE, TWO, THREE}
					      for(Action a : Action.values()) {
					         switch(a) {
					         case ONE:
					            System.out.print("1");
					            break;
					         case TWO:
					            System.out.print("2");
					            break;
					         default:
					            System.out.print("default");
					         }
					      }
					   }
					}""",
			},
			"12default"
		);


}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732
public void test131() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"""
					public class X {
						//A,B
						;
						public static void main(String[] args) {
							enum Y { }
					 \
							try {
								System.out.println(Y.valueOf(null));
							} catch (NullPointerException e) {
								System.out.println("NullPointerException");
							} catch (IllegalArgumentException e) {
								System.out.println("IllegalArgumentException");
							}
						}
					}
					""",
         },
         "NullPointerException");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732 - variation
public void test132() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"""
					public class X {
						public static void main(String[] args) {
							enum Y {
								A,B
								;
							}
							try {
								System.out.println(Y.valueOf(null));
							} catch (NullPointerException e) {
								System.out.println("NullPointerException");
							} catch (IllegalArgumentException e) {
								System.out.println("IllegalArgumentException");
							}
						}
					}
					""",
         },
         "NullPointerException");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147747
public void test133() throws Exception {
	this.runConformTest(
         new String[] {
        		"X.java",
     			"""
					public class X {
						public static void main(String[] args) {
							enum Y {
								A, B, C;
							}
						}
					}
					""",
         },
         "");
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 0, Locals: 1
		  public static void main(java.lang.String[] args);
		    0  return
		      Line numbers:
		        [pc: 0, line: 6]
		      Local variable table:
		        [pc: 0, pc: 1] local: args index: 0 type: java.lang.String[]
		
		  Inner classes:
		    [inner class info: #21 X$1Y, outer class info: #0
		     inner name: #23 Y, accessflags: 16408 static final]
		
		Nest Members:
		   #21 X$1Y
		}""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149042
public void test134() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
					public static void main(String[] args) {
						enum Y {
				    		INITIAL ,
				    		OPENED {
				        	{
				            	System.out.printf("After the %s constructor\\n",INITIAL);
				        	}
						}
				    }
					}
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 7)
				System.out.printf("After the %s constructor\\n",INITIAL);
				                                               ^^^^^^^
			Cannot refer to the static enum field Y.INITIAL within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149562
// a default case is required to consider that b is initialized (in case E
// takes new values in the future)
public void test135() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public class X {
				    boolean foo() {
						enum E {
				    		A,
				    		B
						}
						 E e = E.A;
				        boolean b;
				        switch (e) {
				          case A:
				              b = true;
				              break;
				          case B:
				              b = false;
				              break;
				        }
				        return b;
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 17)
				return b;
				       ^
			The local variable b may not have been initialized. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151368
public void test136() {
 this.runConformTest(
     new String[] {
        "X.java",
        """
			import p.BeanName;
			public class X {
				Object o = BeanName.CreateStepApiOperation;
			}""",
		"p/BeanName.java",
		"""
			package p;
			public enum BeanName {
			
			    //~ Enum constants ---------------------------------------------------------
			
			    AbortAllJobsOperation,
			    AbortJobApiOperation,
			    AbortStepOperation,
			    AclVoter,
			    AcquireNamedLockApiOperation,
			    AuthenticationManager,
			    BeginStepOperation,
			    CloneApiOperation,
			    CommanderDao,
			    CommanderServer,
			    ConfigureQuartzOperation,
			    CreateAclEntryApiOperation,
			    CreateActualParameterApiOperation,
			    CreateFormalParameterApiOperation,
			    CreateProcedureApiOperation,
			    CreateProjectApiOperation,
			    CreateResourceApiOperation,
			    CreateScheduleApiOperation,
			    CreateStepApiOperation,
			    DeleteAclEntryApiOperation,
			    DeleteActualParameterApiOperation,
			    DeleteFormalParameterApiOperation,
			    DeleteJobApiOperation,
			    DeleteProcedureApiOperation,
			    DeleteProjectApiOperation,
			    DeletePropertyApiOperation,
			    DeleteResourceApiOperation,
			    DeleteScheduleApiOperation,
			    DeleteStepApiOperation,
			    DispatchApiRequestOperation,
			    DumpStatisticsApiOperation,
			    ExpandJobStepAction,
			    ExportApiOperation,
			    FinishStepOperation,
			    GetAccessApiOperation,
			    GetAclEntryApiOperation,
			    GetActualParameterApiOperation,
			    GetActualParametersApiOperation,
			    GetFormalParameterApiOperation,
			    GetFormalParametersApiOperation,
			    GetJobDetailsApiOperation,
			    GetJobInfoApiOperation,
			    GetJobStatusApiOperation,
			    GetJobStepDetailsApiOperation,
			    GetJobStepStatusApiOperation,
			    GetJobsApiOperation,
			    GetProcedureApiOperation,
			    GetProceduresApiOperation,
			    GetProjectApiOperation,
			    GetProjectsApiOperation,
			    GetPropertiesApiOperation,
			    GetPropertyApiOperation,
			    GetResourceApiOperation,
			    GetResourcesApiOperation,
			    GetResourcesInPoolApiOperation,
			    GetScheduleApiOperation,
			    GetSchedulesApiOperation,
			    GetStepApiOperation,
			    GetStepsApiOperation,
			    GetVersionsApiOperation,
			    GraphWorkflowApiOperation,
			    HibernateFlushListener,
			    ImportApiOperation,
			    IncrementPropertyApiOperation,
			    InvokeCommandOperation,
			    InvokePostProcessorOperation,
			    LoginApiOperation,
			    LogManager,
			    LogMessageApiOperation,
			    ModifyAclEntryApiOperation,
			    ModifyActualParameterApiOperation,
			    ModifyFormalParameterApiOperation,
			    ModifyProcedureApiOperation,
			    ModifyProjectApiOperation,
			    ModifyPropertyApiOperation,
			    ModifyResourceApiOperation,
			    ModifyScheduleApiOperation,
			    ModifyStepApiOperation,
			    MoveStepApiOperation,
			    PauseSchedulerApiOperation,
			    QuartzQueue,
			    QuartzScheduler,
			    ReleaseNamedLockApiOperation,
			    ResourceInvoker,
			    RunProcedureApiOperation,
			    RunQueryApiOperation,
			    SaxReader,
			    ScheduleStepsOperation,
			    SessionCache,
			    SetJobNameApiOperation,
			    SetPropertyApiOperation,
			    SetStepStatusAction,
			    StartWorkflowOperation,
			    StateRefreshOperation,
			    StepCompletionPrecondition,
			    StepOutcomePrecondition,
			    StepScheduler,
			    TemplateOperation,
			    TimeoutWatchdog,
			    UpdateConfigurationOperation,
			    Workspace,
			    XmlRequestHandler;
			
			    //~ Static fields/initializers ---------------------------------------------
			
			    public static final int MAX_BEAN_NAME_LENGTH = 33;
			
			    //~ Methods ----------------------------------------------------------------
			
			    /**
			     * Get this bean name as a property name, i.e. uncapitalized.
			     *
			     * @return String
			     */
			    public String getPropertyName()
			    {
			        return null;
			    }
			}
			""", // =================,
     },
	"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156540
public void test137() {
 this.runConformTest(
     new String[] {
        "X.java",
        """
			public class X {
			
			    interface Interface {
			        public int value();
			    }
			
			
			    public static void main(String[] args) {
			    	enum MyEnum implements Interface {
			        ;
			
			        	MyEnum(int value) { this.value = value; }       \s
			       	public int value() { return this.value; }
			        	private int value;
			    	}
			        System.out.println(MyEnum.values().length);
			    }
			}""", // =================,
     },
	"0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156591
public void test138() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				    	enum Y {
							PLUS {
								double eval(double x, double y) {
									return x + y;
								}
							},
							MINUS {
								@Override
								abstract double eval(double x, double y);
							};
							abstract double eval(double x, double y);
						}
					}
				}
				
				""", // =================
		 },
		"""
			----------
			1. WARNING in X.java (at line 5)
				double eval(double x, double y) {
				       ^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval(double, double) of type new Y(){} should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 9)
				MINUS {
				^^^^^
			The enum constant MINUS cannot define abstract methods
			----------
			3. ERROR in X.java (at line 11)
				abstract double eval(double x, double y);
				                ^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval cannot be abstract in the enum constant MINUS
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156591 - variation
public void test139() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				    	enum Y {
							PLUS {
								double eval(double x, double y) {
									return x + y;
								}
							},
							MINUS {
								abstract double eval2(double x, double y);
							};
				
							abstract double eval(double x, double y);
						}
					}
				}
				
				""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				double eval(double x, double y) {
				       ^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval(double, double) of type new Y(){} should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 9)
				MINUS {
				^^^^^
			The enum constant MINUS cannot define abstract methods
			----------
			3. ERROR in X.java (at line 9)
				MINUS {
				^^^^^
			The enum constant MINUS must implement the abstract method eval(double, double)
			----------
			4. ERROR in X.java (at line 10)
				abstract double eval2(double x, double y);
				                ^^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval2 cannot be abstract in the enum constant MINUS
			----------
			"""
	);
}
//check final modifier
public void test140() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"""
					public class X {
						void bar(X x) {
							enum Y {
								PLUS {/*ANONYMOUS*/}, MINUS;
							}
							Y y = Y.PLUS;
							Runnable r = (Runnable)y;
						}
					}""", // =================
     },
	"");
}
//check final modifier
public void test141() {
 this.runNegativeTest(
     new String[] {
    	        "X.java",
    			"""
					public class X {
						void bar(X x) {
							enum Y {
								PLUS, MINUS;
							}
							Y y = Y.PLUS;
							Runnable r = (Runnable)y;
						}
					}""", // =================
     },
		"""
			----------
			1. ERROR in X.java (at line 7)
				Runnable r = (Runnable)y;
				             ^^^^^^^^^^^
			Cannot cast from Y to Runnable
			----------
			""");
}
public void test142() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"""
					public class X {
						public static void main(String[] args) {
							enum Week {
								Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
							}
							for (Week w : Week.values())
								System.out.print(w + " ");
							for (Week w : java.util.EnumSet.range(Week.Monday, Week.Friday)) {
								System.out.print(w + " ");
							}
						}
					}
					""", // =================
     },
     "Monday Tuesday Wednesday Thursday Friday Saturday Sunday Monday Tuesday Wednesday Thursday Friday");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=166866
public void test143() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							enum Y {
					  			A {
					    			@Override
					    			public String toString() {
					      				return a();
					    			}
					    			public abstract String a();
					  			}
							}
						}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A {
				^
			The enum constant A cannot define abstract methods
			----------
			2. ERROR in X.java (at line 9)
				public abstract String a();
				                       ^^^
			The method a cannot be abstract in the enum constant A
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test144() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X{
						enum Y<T> {}
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				enum Y<T> {}
				       ^
			Syntax error, enum declaration cannot have type parameters
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test145() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"ClassC.java",
			"""
				public class ClassC {
				  void bar() {
					 enum EnumA {
				  		B1,
				  		B2;
				  		public void foo(){}
					 }
				    EnumA.B1.B1.foo();
				    EnumA.B1.B2.foo();
				  }
				}""",
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in ClassC.java (at line 8)\n" +
		"	EnumA.B1.B1.foo();\n" +
		"	         ^^\n" +
		"The static field EnumA.B1 should be accessed in a static way\n" +
		"----------\n" +
		"2. ERROR in ClassC.java (at line 9)\n" +
		"	EnumA.B1.B2.foo();\n" +
		"	         ^^\n" +
		"The static field EnumA.B2 should be accessed in a static way\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207915
public void test146() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						final String test;
						public X() { // error
							enum MyEnum {
								A, B
							}
							MyEnum e = MyEnum.A;
							switch (e) {
								case A:
									test = "a";
									break;
								case B:
									test = "a";
									break;
								// default: test = "unknown"; // enabling this line fixes above error
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				public X() { // error
				       ^^^
			The blank final field test may not have been initialized. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem
			----------
			""");
}
// normal error when other warning is enabled
public void test146b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						final String test;
						public X() { // error
							enum MyEnum {
								A, B
							}
							MyEnum e = MyEnum.A;
							switch (e) {
								case A:
									test = "a";
									break;
								case B:
									test = "a";
									break;
								// default: test = "unknown"; // enabling this line fixes above error
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				public X() { // error
				       ^^^
			The blank final field test may not have been initialized
			----------
			2. WARNING in X.java (at line 8)
				switch (e) {
				        ^
			The switch over the enum type MyEnum should have a default case
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502
public void test147() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public static void main(String[] args) {
								public abstract enum E {
									SUCCESS;
								}
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 4)
					public abstract enum E {
					                     ^
				Illegal modifier for local enum E; no explicit modifier is permitted
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test148() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public static void main(String[] args) {
								abstract enum E implements Runnable {
									SUCCESS;
								}
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 4)
					abstract enum E implements Runnable {
					              ^
				Illegal modifier for local enum E; no explicit modifier is permitted
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void _NA_test149() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public enum E implements Runnable {
								SUCCESS;
								public void run(){}
							}
							public static void main(String[] args) {
								Class<E> c = E.class;
								System.out.println(c.getName() + ":" + X.E.SUCCESS);
							}
						}
						"""
			},
			"p.X$E:SUCCESS");

	String expectedOutput =
		"// Signature: Ljava/lang/Enum<Lp/X$E;>;Ljava/lang/Runnable;\n" +
		"public static final enum p.X$E implements java.lang.Runnable {\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "X$E.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test150() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public static void main(String[] args) {
								enum E implements Runnable {
									SUCCESS;
									public void run(){}
								}
								Class<E> c = E.class;
								System.out.println(c.getName() + ":" + E.SUCCESS);
							}
						}
						"""
			},
			"p.X$1E:SUCCESS");

}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test151() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public static void main(String[] args) {
								enum E implements Runnable {
									SUCCESS {};
									public void run(){}
								}
								Class<E> c = E.class;
								System.out.println(c.getName() + ":" + E.SUCCESS);
							}
						}
						"""
			},
			"p.X$1E:SUCCESS");

	String expectedOutput =
		"""
		// Signature: Ljava/lang/Enum<Lp/X$1E;>;Ljava/lang/Runnable;
		abstract static enum p.X$1E implements java.lang.Runnable {
		 \s
		  // Field descriptor #8 Lp/X$1E;
		  public static final enum p.X$1E SUCCESS;
		 \s
		  // Field descriptor #10 [Lp/X$1E;
		  private static final synthetic p.X$1E[] ENUM$VALUES;
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 4, Locals: 0
		  static {};
		     0  new p.X$1E$1 [14]
		     3  dup
		     4  ldc <String "SUCCESS"> [16]
		     6  iconst_0
		     7  invokespecial p.X$1E$1(java.lang.String, int) [17]
		    10  putstatic p.X$1E.SUCCESS : new p.X(){} [21]
		    13  iconst_1
		    14  anewarray p.X$1E [1]
		    17  dup
		    18  iconst_0
		    19  getstatic p.X$1E.SUCCESS : new p.X(){} [21]
		    22  aastore
		    23  putstatic p.X$1E.ENUM$VALUES : new p.X(){}[] [23]
		    26  return
		      Line numbers:
		        [pc: 0, line: 5]
		        [pc: 13, line: 4]
		 \s
		  // Method descriptor #20 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X$1E(java.lang.String arg0, int arg1);
		    0  aload_0 [this]
		    1  aload_1 [arg0]
		    2  iload_2 [arg1]
		    3  invokespecial java.lang.Enum(java.lang.String, int) [27]
		    6  return
		      Line numbers:
		        [pc: 0, line: 4]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: new p.X(){}
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 0, Locals: 1
		  public void run();
		    0  return
		      Line numbers:
		        [pc: 0, line: 6]
		      Local variable table:
		        [pc: 0, pc: 1] local: this index: 0 type: new p.X(){}
		 \s
		  // Method descriptor #31 ()[Lp/X$1E;
		  // Stack: 5, Locals: 3
		  public static new p.X(){}[] values();
		     0  getstatic p.X$1E.ENUM$VALUES : new p.X(){}[] [23]
		     3  dup
		     4  astore_0
		     5  iconst_0
		     6  aload_0
		     7  arraylength
		     8  dup
		     9  istore_1
		    10  anewarray p.X$1E [1]
		    13  dup
		    14  astore_2
		    15  iconst_0
		    16  iload_1
		    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [32]
		    20  aload_2
		    21  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		 \s
		  // Method descriptor #39 (Ljava/lang/String;)Lp/X$1E;
		  // Stack: 2, Locals: 1
		  public static new p.X(){} valueOf(java.lang.String arg0);
		     0  ldc <Class p.X$1E> [1]
		     2  aload_0 [arg0]
		     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [40]
		     6  checkcast p.X$1E [1]
		     9  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		
		  Inner classes:
		    [inner class info: #1 p/X$1E, outer class info: #0
		     inner name: #54 E, accessflags: 17416 abstract static],
		    [inner class info: #14 p/X$1E$1, outer class info: #0
		     inner name: #0, accessflags: 16384 default]
		  Enclosing Method: #48  #50 p/X.main([Ljava/lang/String;)V
		
		Nest Host: #48 p/X
		}""";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "X$1E.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test152() {
	this.runConformTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum E implements Runnable {
								SUCCESS {};
								public void run(){}
							}
							System.out.println(E.SUCCESS);
						}
					}
					"""
		},
		"SUCCESS",
		null,
		false,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109
public void test153() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum TestEnum {
								RED, GREEN, BLUE;\s
					    		static int test = 0; \s
					
					    		TestEnum() {
					        		TestEnum.test=10;
					    		}
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 8)
				TestEnum.test=10;
				         ^^^^
			Cannot refer to the static enum field TestEnum.test within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test154() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum TestEnum2 {
								;\s
					   		static int test = 0; \s
								TestEnum2() {
					        		TestEnum2.test=11;
					   		}
							}
						}
					}
					class X {
						static int test = 0;
						X() {
							X.test = 13;
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				TestEnum2.test=11;
				          ^^^^
			Cannot refer to the static enum field TestEnum2.test within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test155() {
	this.runConformTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum TestEnum {
								RED, GREEN, BLUE;\s
					    		static int test = 0; \s
							}
					
							enum TestEnum2 {
								;\s
					    		TestEnum2() {
					        		TestEnum.test=12;
					    		}
							}
						}
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test156() {
	this.runConformTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum TestEnum {
								RED, GREEN, BLUE;\s
					    		static int test = 0; \s
					
					    		TestEnum() {
					       		 new Object() {
									{ TestEnum.test=10; }
									};
								}
							}
					    }
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test157() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum Foo {
								ONE, TWO, THREE;
								static int val = 10;
								Foo () {
									this(Foo.val);
									System.out.println(Foo.val);
								}
								Foo(int i){}
								{
									System.out.println(Foo.val);
								}
								int field = Foo.val;
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				this(Foo.val);
				         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			2. ERROR in Y.java (at line 8)
				System.out.println(Foo.val);
				                       ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			3. ERROR in Y.java (at line 12)
				System.out.println(Foo.val);
				                       ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. ERROR in Y.java (at line 14)
				int field = Foo.val;
				                ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test158() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum Foo {
								ONE, TWO, THREE;
								static int val = 10;
								Foo () {
									this(val);
									System.out.println(val);
								}
								Foo(int i){}
								{
									System.out.println(val);
								}
								int field = val;
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				this(val);
				     ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			2. ERROR in Y.java (at line 8)
				System.out.println(val);
				                   ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			3. ERROR in Y.java (at line 12)
				System.out.println(val);
				                   ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. ERROR in Y.java (at line 14)
				int field = val;
				            ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test159() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
							enum Foo {
								ONE, TWO, THREE;
								static int val = 10;
								Foo () {
									this(get().val);
									System.out.println(get().val);
								}
								Foo(int i){}
								{
									System.out.println(get().val);
								}
								int field = get().val;
								Foo get() { return ONE; }
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				this(get().val);
				     ^^^
			Cannot refer to an instance method while explicitly invoking a constructor
			----------
			2. WARNING in Y.java (at line 7)
				this(get().val);
				           ^^^
			The static field Foo.val should be accessed in a static way
			----------
			3. ERROR in Y.java (at line 7)
				this(get().val);
				           ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. WARNING in Y.java (at line 8)
				System.out.println(get().val);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			5. ERROR in Y.java (at line 8)
				System.out.println(get().val);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			6. WARNING in Y.java (at line 12)
				System.out.println(get().val);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			7. ERROR in Y.java (at line 12)
				System.out.println(get().val);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			8. WARNING in Y.java (at line 14)
				int field = get().val;
				                  ^^^
			The static field Foo.val should be accessed in a static way
			----------
			9. ERROR in Y.java (at line 14)
				int field = get().val;
				                  ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test160() {
	this.runNegativeTest(
		new String[] {
				"Y.java",
				"""
					public class Y {
						public static void main(String[] args) {
						enum Foo {
							ONE, TWO, THREE;
							static int val = 10;
							Foo () {
								this(get().val = 1);
								System.out.println(get().val = 2);
							}
							Foo(int i){}
							{
								System.out.println(get().val = 3);
							}
							int field = get().val = 4;
							Foo get() { return ONE; }
						}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				this(get().val = 1);
				     ^^^
			Cannot refer to an instance method while explicitly invoking a constructor
			----------
			2. WARNING in Y.java (at line 7)
				this(get().val = 1);
				           ^^^
			The static field Foo.val should be accessed in a static way
			----------
			3. ERROR in Y.java (at line 7)
				this(get().val = 1);
				           ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. WARNING in Y.java (at line 8)
				System.out.println(get().val = 2);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			5. ERROR in Y.java (at line 8)
				System.out.println(get().val = 2);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			6. WARNING in Y.java (at line 12)
				System.out.println(get().val = 3);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			7. ERROR in Y.java (at line 12)
				System.out.println(get().val = 3);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			8. WARNING in Y.java (at line 14)
				int field = get().val = 4;
				                  ^^^
			The static field Foo.val should be accessed in a static way
			----------
			9. ERROR in Y.java (at line 14)
				int field = get().val = 4;
				                  ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void _NA_test161() {
	this.runConformTest(
		new String[] {
				"LocalEnumTest1.java",
				"""
					enum LocalEnumTest1 {
						;
						static int foo = LocalEnumTest2.bar;
					}
					enum LocalEnumTest2 {
						;
						static int bar = LocalEnumTest1.foo;
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225
public void test162() {
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"""
					import java.util.HashMap;
					import java.util.Map;
					
					public class X {\s
						public static void main(String[] args) {
							enum Status {
								GOOD((byte) 0x00), BAD((byte) 0x02);
					
								private static Map<Byte, Status> mapping;
					
								private Status(final byte newValue) {
					
									if (Status.mapping == null) {
										Status.mapping = new HashMap<Byte, Status>();
									}
					
									Status.mapping.put(newValue, this);
								}
							}
						}
					}
					""", // =================
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					if (Status.mapping == null) {
					           ^^^^^^^
				Cannot refer to the static enum field Status.mapping within an initializer
				----------
				2. ERROR in X.java (at line 14)
					Status.mapping = new HashMap<Byte, Status>();
					       ^^^^^^^
				Cannot refer to the static enum field Status.mapping within an initializer
				----------
				3. ERROR in X.java (at line 17)
					Status.mapping.put(newValue, this);
					       ^^^^^^^
				Cannot refer to the static enum field Status.mapping within an initializer
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225 - variation
public void test163() {
	this.runConformTest(
			new String[] {
				"X.java", // =================
				"""
					import java.util.HashMap;
					import java.util.Map;
					
					public class X {\s
						public static void main(String[] args) {
							enum Status {
								GOOD((byte) 0x00), BAD((byte) 0x02);
								private byte value;
								private static Map<Byte, Status> mapping;
								private Status(final byte newValue) {
									this.value = newValue;
								}
								static {
									Status.mapping = new HashMap<Byte, Status>();
									for (Status s : values()) {
										Status.mapping.put(s.value, s);
									}
								}
							}
						}
					}
					""", // =================
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251523
public void test164() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y {
							;
							private Y valueOf(String arg0) { return null; }
						}
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				private Y valueOf(String arg0) { return null; }
				          ^^^^^^^^^^^^^^^^^^^^
			The enum Y already defines the method valueOf(String) implicitly
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251523 - variation
public void test165() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				class Other {
					int dupField;//1
					int dupField;//2
					int dupField;//3
					int dupField;//4
					void dupMethod(int i) {}//5
					void dupMethod(int i) {}//6
					void dupMethod(int i) {}//7
					void dupMethod(int i) {}//8
					void foo() {
						int i = dupMethod(dupField);
					}
				}
				
				public class X {\s
					public static void main(String[] args) {
						enum Y {
				       	;
				        	private Y valueOf(String arg0) { return null; }//9
				        	private Y valueOf(String arg0) { return null; }//10
				        	private Y valueOf(String arg0) { return null; }//11
				       	void foo() {
				        		int i = valueOf("");
				        	}
						}
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int dupField;//1
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			2. ERROR in X.java (at line 3)
				int dupField;//2
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			3. ERROR in X.java (at line 4)
				int dupField;//3
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			4. ERROR in X.java (at line 5)
				int dupField;//4
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			5. ERROR in X.java (at line 6)
				void dupMethod(int i) {}//5
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			6. ERROR in X.java (at line 7)
				void dupMethod(int i) {}//6
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			7. ERROR in X.java (at line 8)
				void dupMethod(int i) {}//7
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			8. ERROR in X.java (at line 9)
				void dupMethod(int i) {}//8
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			9. ERROR in X.java (at line 11)
				int i = dupMethod(dupField);
				        ^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from void to int
			----------
			10. ERROR in X.java (at line 19)
				private Y valueOf(String arg0) { return null; }//9
				          ^^^^^^^^^^^^^^^^^^^^
			The enum Y already defines the method valueOf(String) implicitly
			----------
			11. ERROR in X.java (at line 20)
				private Y valueOf(String arg0) { return null; }//10
				          ^^^^^^^^^^^^^^^^^^^^
			The enum Y already defines the method valueOf(String) implicitly
			----------
			12. ERROR in X.java (at line 21)
				private Y valueOf(String arg0) { return null; }//11
				          ^^^^^^^^^^^^^^^^^^^^
			The enum Y already defines the method valueOf(String) implicitly
			----------
			13. ERROR in X.java (at line 23)
				int i = valueOf("");
				        ^^^^^^^^^^^
			Type mismatch: cannot convert from Y to int
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814
public void test166() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y {
				        	;
				       	private int valueOf(String arg0) { return 0; }//11
				        	void foo() {
				        		int i = valueOf("");
				        	}
						}
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				private int valueOf(String arg0) { return 0; }//11
				            ^^^^^^^^^^^^^^^^^^^^
			The enum Y already defines the method valueOf(String) implicitly
			----------
			2. ERROR in X.java (at line 7)
				int i = valueOf("");
				        ^^^^^^^^^^^
			Type mismatch: cannot convert from Y to int
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	// check for presence of #valueOf(...) in problem type
	String expectedPartialOutput =
		"""
		  public static void main(java.lang.String[] arg0);
		     0  new java.lang.Error [16]
		     3  dup
		     4  ldc <String "Unresolved compilation problems: \\n\\tThe enum Y already defines the method valueOf(String) implicitly\\n\\tType mismatch: cannot convert from Y to int\\n"> [18]
		     6  invokespecial java.lang.Error(java.lang.String) [20]
		     9  athrow
		""" ;
	verifyClassFile(expectedPartialOutput, "X.class", ClassFileBytesDisassembler.SYSTEM, true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814 - variation
public void test167() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {\s
					public static void main(String[] args) {
						enum Y {
				    		;
				    		static int valueOf(String arg0) { return 0; }//9
				   		void foo() {
				    			int i = Y.valueOf("");
				   		}
						}
						class Other {
				    		void foo() {
				    			int i = Y.valueOf("");
				    		}
						}
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				static int valueOf(String arg0) { return 0; }//9
				           ^^^^^^^^^^^^^^^^^^^^
			The enum Y already defines the method valueOf(String) implicitly
			----------
			2. ERROR in X.java (at line 7)
				int i = Y.valueOf("");
				        ^^^^^^^^^^^^^
			Type mismatch: cannot convert from Y to int
			----------
			----------
			1. ERROR in X.java (at line 12)
				int i = Y.valueOf("");
				        ^^^^^^^^^^^^^
			Type mismatch: cannot convert from Y to int
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {\s
					public static void main(String[] args) {
						enum BadEnum {
				    		CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers
				    		IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)
				    		private BadEnum(BadEnum self) {
				    		}
						}
						class A {
				    		A x1 = new A(x1);//1 - WRONG
				    		static A X2 = new A(A.X2);//2 - OK
				    		A x3 = new A(this.x3);//3 - OK
				    		A(A x) {}
				    		A(int i) {}
				    		int VALUE() { return 13; }
				    		int value() { return 14; }
						}
						class Y extends A {
				    		A x1 = new A(x1);//6 - WRONG
				    		static A X2 = new A(Y.X2);//7 - OK
				    		A x3 = new A(this.x3);//8 - OK
				    		Y(Y y) { super(y); }
						}
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers
				      ^^^^^
			Cannot reference a field before it is defined
			----------
			2. ERROR in X.java (at line 5)
				IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)
				                   ^^^^^^^^^^
			Cannot reference a field before it is defined
			----------
			3. ERROR in X.java (at line 10)
				A x1 = new A(x1);//1 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			4. WARNING in X.java (at line 19)
				A x1 = new A(x1);//6 - WRONG
				  ^^
			The field Y.x1 is hiding a field from type A
			----------
			5. ERROR in X.java (at line 19)
				A x1 = new A(x1);//6 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			6. WARNING in X.java (at line 20)
				static A X2 = new A(Y.X2);//7 - OK
				         ^^
			The field Y.X2 is hiding a field from type A
			----------
			7. WARNING in X.java (at line 21)
				A x3 = new A(this.x3);//8 - OK
				  ^^
			The field Y.x3 is hiding a field from type A
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452 - variation
public void test169() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {\s
					public static void main(String[] args) {
						enum BadEnum {
				    		NOWAY(BadEnum.NOWAY.CONST),
				    		INVALID(INVALID.CONST),
				    		WRONG(WRONG.VALUE()),
				    		ILLEGAL(ILLEGAL.value());
				    		final static int CONST = 12;
				    		private BadEnum(int i) {
				    		}
				    		int VALUE() { return 13; }
				    		int value() { return 14; }
						}
						class Y {
				    		final static int CONST = 12;
				    		Y x4 = new Y(x4.CONST);//4 - WRONG
				    		Y x5 = new Y(x5.value());//5 - WRONG
				   		Y(int i) {}
				    		int VALUE() { return 13; }
				    		int value() { return 14; }
						}
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				NOWAY(BadEnum.NOWAY.CONST),
				              ^^^^^
			Cannot reference a field before it is defined
			----------
			2. WARNING in X.java (at line 4)
				NOWAY(BadEnum.NOWAY.CONST),
				                    ^^^^^
			The static field BadEnum.CONST should be accessed in a static way
			----------
			3. ERROR in X.java (at line 5)
				INVALID(INVALID.CONST),
				        ^^^^^^^
			Cannot reference a field before it is defined
			----------
			4. WARNING in X.java (at line 5)
				INVALID(INVALID.CONST),
				                ^^^^^
			The static field BadEnum.CONST should be accessed in a static way
			----------
			5. ERROR in X.java (at line 6)
				WRONG(WRONG.VALUE()),
				      ^^^^^
			Cannot reference a field before it is defined
			----------
			6. ERROR in X.java (at line 7)
				ILLEGAL(ILLEGAL.value());
				        ^^^^^^^
			Cannot reference a field before it is defined
			----------
			7. ERROR in X.java (at line 16)
				Y x4 = new Y(x4.CONST);//4 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			8. WARNING in X.java (at line 16)
				Y x4 = new Y(x4.CONST);//4 - WRONG
				                ^^^^^
			The static field Y.CONST should be accessed in a static way
			----------
			9. ERROR in X.java (at line 17)
				Y x5 = new Y(x5.value());//5 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=263877
public void test170() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {
				   public static final int FOO = X.OFFSET + 0;
				   public static final int BAR = OFFSET + 1;
				   public static final int OFFSET = 0;  // cannot move this above, else more errors
					public static void main(String[] args) {
						enum Days {
				    		Monday("Mon", Days.OFFSET + 0),    // should not complain
				    		Tuesday("Tue", Days.Wednesday.hashCode()),   // should complain since enum constant
				    		Wednesday("Wed", OFFSET + 2);   // should complain since unqualified
				    		public static final int OFFSET = 0;  // cannot move this above, else more errors
				   		Days(String abbr, int index) {
				    		}
						}
				
					}
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				public static final int BAR = OFFSET + 1;
				                              ^^^^^^
			Cannot reference a field before it is defined
			----------
			2. ERROR in X.java (at line 8)
				Tuesday("Tue", Days.Wednesday.hashCode()),   // should complain since enum constant
				                    ^^^^^^^^^
			Cannot reference a field before it is defined
			----------
			3. ERROR in X.java (at line 9)
				Wednesday("Wed", OFFSET + 2);   // should complain since unqualified
				                 ^^^^^^
			Cannot reference a field before it is defined
			----------
			4. WARNING in X.java (at line 10)
				public static final int OFFSET = 0;  // cannot move this above, else more errors
				                        ^^^^^^
			The field Days.OFFSET is hiding a field from type X
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious.
public void test171() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				    	enum Colors {
					    	BLEU,
					    	BLANC,
					     	ROUGE
					 	}
						for (Colors c: Colors.values()) {
				           System.out.print(c);
						}
					}
				}
				"""
		},
		null, options,
		"",
		"BLEUBLANCROUGE", null, null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious. This
// test also verifies that while we don't complain about individual enumerators not being used
// we DO complain if the enumeration type itself is not used.
public void test172() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
				   	enum Greet {
					    	HELLO, HOWDY, BONJOUR;\s
				   	}
						enum Colors {
				       	RED, BLACK, BLUE;
				   	}
				   	enum Complaint {\
				       	WARNING, ERROR, FATAL_ERROR, PANIC;
				   	}
						Greet g = Greet.valueOf("HELLO");
						System.out.print(g);
				       Colors c = Enum.valueOf(Colors.class, "RED");
						System.out.print(c);
				   }
				}
				"""
		},
		null, customOptions,
		"""
			----------
			1. WARNING in X.java (at line 9)
				enum Complaint {       	WARNING, ERROR, FATAL_ERROR, PANIC;
				     ^^^^^^^^^
			The type Complaint is never used locally
			----------
			""",
		"HELLORED", null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=273990
public void test173() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static void main(String[] args) {
						enum E {
							A(E.STATIK);
							private static int STATIK = 1;
							private E(final int i) {}
						}
						enum E2 {
							A(E2.STATIK);
							static int STATIK = 1;
							private E2(final int i) {}
						}
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A(E.STATIK);
				    ^^^^^^
			Cannot reference a field before it is defined
			----------
			----------
			1. ERROR in X.java (at line 9)
				A(E2.STATIK);
				     ^^^^^^
			Cannot reference a field before it is defined
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=278562
public void test174() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				public class X {
					public static void main(String[] args) throws Exception {
						interface S {}
						enum A implements S {
							L;
						}
						Enum<? extends S> enumConstant = A.L;
						Map<String, Enum> m = new HashMap<>();
						for (Enum e : enumConstant.getDeclaringClass().getEnumConstants()) {
							m.put(e.name(), e);
						}
						System.out.print(1);
					}
				}
				"""
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=278562
public void test175() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				public class X {
					public static void main(String[] args) throws Exception {
						interface S {}
						enum A implements S {
							L, M, N, O;
						}
						Enum[] tab = new Enum[] {A.L, A.M, A.N, A.O};
						Map m = new HashMap();
						for (Enum s : tab) {
							m.put(s.name(), s);
						}
						System.out.print(1);
					}
				}
				"""
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test176() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) throws Exception {
						enum Y {
							A(""), B("SUCCESS"), C("Hello");
				
							String message;
				
							Y(@Deprecated String s) {
								this.message = s;
							}
							@Override
							public String toString() {
								return this.message;
							}
						}
					}
				}
				"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) throws Exception {
							enum Y {
								A(""), B("SUCCESS"), C("Hello");
					
								String message;
					
								Y(@Deprecated String s) {
									this.message = s;
								}
								@Override
								public String toString() {
									return this.message;
								}
							}
							System.out.println(Y.B);
						}
					}
					"""
		},
		null,
		options,
		"",
		"SUCCESS",
		"",
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test177() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) throws Exception {
						enum Y {
							A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
				
							private String message;
							private int index;
							private String name;
				
							Y(@Deprecated String s, int i, @Deprecated String name) {
								this.message = s;
								this.index = i;
								this.name = name;
							}
							@Override
							public String toString() {
								return this.message + this.name;
							}
						}
					}
				}
				"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) throws Exception {
						enum Y {
							A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
				
							private String message;
							private int index;
							private String name;
				
							Y(@Deprecated String s, int i, @Deprecated String name) {
								this.message = s;
								this.index = i;
								this.name = name;
							}
							@Override
							public String toString() {
								return this.message + this.name;
							}
						}
						System.out.println(Y.B);
					}
				}
				"""
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
// static local enums are not allowed. Removing static is the same as test177
public void _NA_test178() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static enum Y {
						A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
					\t
						private String message;
						private int index;
						private String name;
						Y(@Deprecated String s, int i, @Deprecated String name) {
							this.message = s;
							this.index = i;
							this.name = name;
						}
						@Override
						public String toString() {
							return this.message + this.name;
						}
					}
				}"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Z.java",
			"""
				public class Z {
					public static void main(String[] args) {
						System.out.println(X.Y.B);
					}
				}"""
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test179() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Z.java",
			"""
				public class Z {
					public static void main(String[] args) {
						class X {
							enum Y {
								A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
					\t
								private String message;
								private int index;
								private String name;
								Y(@Deprecated String s, int i, @Deprecated String name) {
									this.message = s;
									this.index = i;
									this.name = name;
								}
								@Override
								public String toString() {
									return this.message + this.name;
								}
							}
						}
						System.out.println(X.Y.B);
					}
				}"""
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		JavacTestOptions.Excuse.JavacHasWarningsEclipseNotConfigured);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289892
public void test180() {
	this.runConformTest(
		new String[] {
			"p/package-info.java",
			"@p.Annot(state=p.MyEnum.BROKEN)\n" +
			"package p;",
			"p/Annot.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public @interface Annot {
					MyEnum state() default MyEnum.KO;
				}""",
			"p/MyEnum.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public enum MyEnum {
					WORKS, OK, KO, BROKEN, ;
				}""",
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"""
				import p.MyEnum;
				import p.Annot;
				public class X {
					public static void main(String[] args) {
						@Annot(state=MyEnum.KO)
						enum LocalEnum {
							A, B, ;
						}
						System.out.print(LocalEnum.class);
					}
				}"""
		},
		null,
		options,
		"",
		"class X$1LocalEnum",
		"",
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=289892
// in interaction with null annotations
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365519#c4 item (6)
public void test180a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"p/package-info.java",
			"@p.Annot(state=p.MyEnum.BROKEN)\n" +
			"package p;",
			"p/Annot.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public @interface Annot {
					MyEnum state() default MyEnum.KO;
				}""",
			"p/MyEnum.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public enum MyEnum {
					WORKS, OK, KO, BROKEN, ;
				}""",
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"""
				import p.MyEnum;
				import p.Annot;
				public class X {
					public static void main(String[] args) {
						@Annot(state=MyEnum.OK)
						enum LocalEnum {
							A, B, ;
						}
						System.out.print(LocalEnum.class);
					}
				}"""
		},
		null,
		options,
		"",
		"class X$1LocalEnum",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=300133
public void test181() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
				"B.java",
				"""
					public class B {
						enum X {
							A {
								@Override
								public Object foo(final String s) {
									class Local {
										public String toString() {
											return s;
										}
									}
									return new Local();
								}
							};
							public abstract Object foo(String s);
						}
						public static void main(String... args) {
							 System.out.println(X.A.foo("SUCCESS"));
						}
					}"""
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test182() throws Exception {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[])   {
						foo();
					}
					public static void foo() {
						enum E {
							a1(1), a2(2);
							static int[] VALUES = { 1, 2 };
							private int value;
							E(int v) {
								this.value = v;
							}
							public int val() {
								return this.value;
							}
						}
						int n = 0;
						for (E e : E.values()) {
							if (e.val() == E.VALUES[n++] ) {
								System.out.print(e.val());
							}
						}
					}
				}"""
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test183() throws Exception {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
					}
					static {
						enum E {
							a1(1), a2(2);
							static int[] VALUES = { 1, 2 };
							private int value;
							E(int v) {
								this.value = v;
							}
							public int val() {
								return this.value;
							}
						}
						int n = 0;
						for (E e : E.values()) {
							if (e.val() == E.VALUES[n++] ) {
								System.out.print(e.val());
							}
						}
					}
				}"""

		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test184() throws Exception {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						new X();
					}
					X() {
						enum E {
							a1(1), a2(2);
							static int[] VALUES = { 1, 2 };
							private int value;
							E(int v) {
								this.value = v;
							}
							public int val() {
								return this.value;
							}
						}
						int n = 0;
						for (E e : E.values()) {
							if (e.val() == E.VALUES[n++] ) {
								System.out.print(e.val());
							}
						}
					}
				}"""
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=
public void test185() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X{
					public static void main(String argv[]) {
						enum Y{
				  			A, B;
				  			private Y() throws Exception {
				  			}
						}
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				A, B;
				^
			Unhandled exception type Exception
			----------
			2. ERROR in X.java (at line 4)
				A, B;
				   ^
			Unhandled exception type Exception
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test186() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    void _test(boolean val) {
						 enum X {
						   A, B;
						 }
						 X x= val? X.A : X.B;
				        switch (x) {
							case A: System.out.println("A"); break;
				 			default : System.out.println("unknown"); break;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in Y.java (at line 7)
				switch (x) {
				        ^
			The enum constant B should have a corresponding case label in this enum switch on X. To suppress this problem, add a comment //$CASES-OMITTED$ on the line above the 'default:'
			----------
			""",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.ERROR);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    void _test(boolean val) {
						 enum X {
						   A, B;
						 }
						 X x= val? X.A : X.B;
				        switch (x) {
							case A: System.out.println("A");
				           //$FALL-THROUGH$
				           //$CASES-OMITTED$
				 			default : System.out.println("unknown"); break;
				        }
				    }
				}
				""",
		},
		"",
		null, // classlibs
		true, // flush
		null, // vmArgs
		options,
		null /*requestor*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187a() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    void _test(boolean val) {\n" +
			"		 enum X {\n" +
			"		   A, B;\n" +
			"		 }\n" +
			"		 X x= val? X.A : X.B;\n" +
			"        switch (x) {\n" +
			"			case A: System.out.println(\"A\"); break;\n" +
			"           //$CASES-OMITTED$\n" + // not strong enough to suppress the warning if default: is missing
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"""
			----------
			1. WARNING in Y.java (at line 7)
				switch (x) {
				        ^
			The enum constant B needs a corresponding case label in this enum switch on X
			----------
			""",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187b() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    @SuppressWarnings("incomplete-switch")
				    void _test(boolean val) {
						 enum X {
						   A, B;
						 }
						 X x= val? X.A : X.B;
				        switch (x) {
							case A: System.out.println("A"); break;
				        }
				    }
				}
				""",
		},
		"",
		null, // classlibs
		true, // flush
		null, // vmArgs
		options,
		null /*requestor*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test188() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    void _test(boolean val) {
						 enum X {
						   A, B;
						 }
						 X x= val? X.A : X.B;
				        switch (x) {
							case A: System.out.println("A"); break;
							case B: System.out.println("B"); break;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in Y.java (at line 7)
				switch (x) {
				        ^
			The switch over the enum type X should have a default case
			----------
			""",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test189() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	//options.put(JavaCore.COMPILER_PB_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				    public int test(boolean val) {
						 enum X {
						   A, B;
						 }
						 X x= val? X.A : X.B;
				        switch (x) {
							case A: return 1;
							case B: return 2;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in Y.java (at line 2)
				public int test(boolean val) {
				           ^^^^^^^^^^^^^^^^^
			This method must return a result of type int. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem
			----------
			""",
		null, // classlibs
		true, // flush
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433060 [1.8][compiler] enum E<T>{I;} causes NPE in AllocationExpression.checkTypeArgumentRedundancy
public void test433060() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y{
					public static void main(String argv[]) {
						enum X<T> {
							OBJ;
						}
					}
				}"""

		},
		"""
			----------
			1. ERROR in Y.java (at line 3)
				enum X<T> {
				       ^
			Syntax error, enum declaration cannot have type parameters
			----------
			""",
		null,
		true,
		options);
}
public void test434442() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(new String[] {
			"Y.java",
			"""
				public class Y {
				  public static void main(String[] args) {
						enum Letter {
				  			A, B;
						}
						interface I {
				  			public default void test(Letter letter) {
				    			switch (letter) {
				      				case A:
				        				System.out.print("A");
				        				break;
				      				case B:
				        				System.out.print("B");
				        				break;
				    			}
				  			}
						}
						class X implements I {
				  		}
						try{
							X x = new X();
							x.test(Letter.A);
					  	}
				    	catch (Exception e) {
				      		e.printStackTrace();
				    	}
				  }
				}\s
				
				"""
	}, "A");
}
public void test476281() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(new String[] {
			"Y.java",
			"""
				public class Y {
				  public static void main(String[] args) {
						enum LambdaEnumLocalClassBug {
				  			A(() -> {
				    			class Foo {
				    			}
				    			new Foo();
				    			System.out.println("Success");
				  			})
							;
				  			private final Runnable runnable;
				  			private LambdaEnumLocalClassBug(Runnable runnable) {
				    			this.runnable = runnable;
				  			}
						}
				    	LambdaEnumLocalClassBug.A.runnable.run();
				  }
				}\s
				"""
			},
			"Success");
}
public void test476281a() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(new String[] {
			"Y.java",
			"""
				public class Y {
				  public static void main(String[] args) {
						enum Test {
				  			B(new Runnable() {
								public void run() {
									//
									class Foo {
								\t
									}
									new Foo();
				   		 		System.out.println("Success");
								}
							});
				  			private final Runnable runnable;
				  			private Test(Runnable runnable) {
				    			this.runnable = runnable;
				  			}
						}
				    	Test.B.runnable.run();
				  }
				}\s
				"""
			},
			"Success");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=566758
public void test566758() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					<T> void m(T t) {
						interface Y {
							T foo(); // T should not be allowed
						}
					}
				\t
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				T foo(); // T should not be allowed
				^
			Cannot make a static reference to the non-static type T
			----------
			""",
		null,
		true);
}
}
