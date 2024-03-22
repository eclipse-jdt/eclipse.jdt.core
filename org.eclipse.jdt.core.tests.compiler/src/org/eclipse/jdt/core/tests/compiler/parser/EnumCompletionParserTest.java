/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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


import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;



@SuppressWarnings({ "unchecked", "rawtypes" })
public class EnumCompletionParserTest extends AbstractCompletionTest {
public EnumCompletionParserTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(EnumCompletionParserTest.class);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83321
 */
public void test0001(){
	String str =
		"""
		public class Completion {
			/*here*/
		}
		enum Natural {
			ONE;
		}
		""";

	String completeBehind = "/*here*/";
	int cursorLocation = str.indexOf("/*here*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		public class Completion {
		  <CompleteOnType:>;
		  public Completion() {
		  }
		}
		enum Natural {
		  ONE(),
		  Natural() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0002(){
	String str =
		"""
		public class Test {
			void foo() {
			  switch(c) {
			  	case FOO :
			  	  break;
			  }
			}
		}
		""";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"""
			switch (c) {
			case <CompleteOnName:FOO> :
			    break;
			}""";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    switch (c) {
			    case <CompleteOnName:FOO> :
			        break;
			    }
			  }
			}
			""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0003(){
	String str =
		"""
		public class Test {
			void foo() {
			  switch(c) {
			  	case BAR :
			  	case FOO :
			  	  break;
			  }
			}
		}
		""";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"""
			switch (c) {
			case BAR :
			case <CompleteOnName:FOO> :
			    break;
			}""";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    switch (c) {
			    case BAR :
			    case <CompleteOnName:FOO> :
			        break;
			    }
			  }
			}
			""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0004(){
	String str =
		"""
		public class Test {
			void foo() {
			  switch(c) {
			  	case BAR :
			  	  break;
			  	case FOO :
			  	  break;
			  }
			}
		}
		""";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"""
			switch (c) {
			case BAR :
			    break;
			case <CompleteOnName:FOO> :
			    break;
			}""";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    switch (c) {
			    case BAR :
			        break;
			    case <CompleteOnName:FOO> :
			        break;
			    }
			  }
			}
			""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0005(){
	String str =
		"""
		public class Test {
			void foo() {
			  switch(c) {
			  	case BAR :
			  	  break;
			  	case FOO :
			  }
			}
		}
		""";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"""
			switch (c) {
			case BAR :
			    break;
			case <CompleteOnName:FOO> :
			}""";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    switch (c) {
			    case BAR :
			        break;
			    case <CompleteOnName:FOO> :
			    }
			  }
			}
			""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0006(){
	String str =
		"""
		public class Test {
			void foo() {
			  switch(c) {
			  	case BAR :
			  	  break;
			  	case FOO
			  }
			}
		}
		""";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"""
			switch (c) {
			case BAR :
			    break;
			case <CompleteOnName:FOO> :
			}""";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    {
			      switch (c) {
			      case BAR :
			          break;
			      case <CompleteOnName:FOO> :
			      }
			    }
			  }
			}
			""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0007(){
	String str =
		"""
		public class Test {
			void foo() {
			  switch(c) {
			  	case BAR0 :
			      switch(c) {
			        case BAR :
			  	      break;
			  	    case FOO
			      }
			  	  break;
			  	case BAR2 :
			  	  break;
			  }
			}
		}
		""";

	String completeBehind = "FOO";
	int cursorLocation = str.indexOf("FOO") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");

	expectedCompletionNodeToString = "<CompleteOnName:FOO>";
	expectedParentNodeToString =
		"""
			switch (c) {
			case BAR :
			    break;
			case <CompleteOnName:FOO> :
			}""";
	completionIdentifier = "FOO";
	expectedReplacedSource = "FOO";
	expectedUnitDisplayString =
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    {
			      {
			        switch (c) {
			        case BAR :
			            break;
			        case <CompleteOnName:FOO> :
			        }
			      }
			    }
			  }
			}
			""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0008(){
	String str =
		"""
		public enum Test {
			A() {
			  void foo() {
			    zzz
			  }
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public enum Test {
		  A() {
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  },
		  public Test() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0009(){
	String str =
		"""
		public enum Test {
			B,
			A() {
			  void foo() {
			    zzz
			  }
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public enum Test {
		  B(),
		  A() {
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  },
		  public Test() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0010(){
	String str =
		"""
		public enum Test {
			#
			B,
			A() {
			  void foo() {
			    zzz
			  }
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public enum Test {
		  B(),
		  A() {
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  },
		  public Test() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0011(){
	String str =
		"""
		public enum Test {
			B() {
			  void foo() {
			  }
			},
			A() {
			  void foo() {
			    zzz
			  }
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public enum Test {
		  B() {
		    void foo() {
		    }
		  },
		  A() {
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  },
		  public Test() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0012(){
	String str =
		"""
		public enum Test {
			#
			B() {
			  void foo() {
			  }
			},
			A() {
			  void foo() {
			    zzz
			  }
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public enum Test {
		  B() {
		    void foo() {
		    }
		  },
		  A() {
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  },
		  public Test() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0013(){
	String str =
		"""
		public enum Test {
			#
			B() {
			  void foo() {
			    #
			  }
			},
			A() {
			  void foo() {
			    zzz
			  }
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public enum Test {
		  B() {
		    void foo() {
		    }
		  },
		  A() {
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  },
		  public Test() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100868
public void test0014(){
	String str =
		"""
		public enum Enum1 {
		  A {
		    tos
		  };
		}
		""";

	String completeBehind = "tos";
	int cursorLocation = str.indexOf("tos") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:tos>";
	String expectedParentNodeToString =
			"""
		() {
		  <CompleteOnType:tos>;
		}""";
	String completionIdentifier = "tos";
	String expectedReplacedSource = "tos";
	String expectedUnitDisplayString =
		"""
		public enum Enum1 {
		  A() {
		    <CompleteOnType:tos>;
		  },
		  public Enum1() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
}
