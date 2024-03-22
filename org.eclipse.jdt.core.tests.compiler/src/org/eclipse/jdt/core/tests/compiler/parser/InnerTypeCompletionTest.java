/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import junit.framework.Test;

/**
 * Completion is expected to be in an inner type
 */
public class InnerTypeCompletionTest extends AbstractCompletionTest {
public InnerTypeCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(InnerTypeCompletionTest.class);
}
/*
 * Test completion in the first method of an anonymous inner class
 */
public void testAnonymousFirstMethod() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					Object o = new Object() {			\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
						void fuzz() {					\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    Object o = new Object() {
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			      void fuzz() {
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class in first method>"
	);
}
/*
 * Test completion in anonymous inner class with no statement defined before.
 */
public void testAnonymousNoStatementBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					Object o = new Object() {			\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    Object o = new Object() {
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class with no statement before>"
	);
}
/*
 * Test completion in anonymous inner class with one field defined before
 * the method containing the completion.
 */
public void testAnonymousOneFieldBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					Object o = new Object() {			\t
						int field = 1;					\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    Object o = new Object() {
			      int field;
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class with one field before>"
	);
}
/*
 * Test completion in anonymous inner class with one statement defined before.
 */
public void testAnonymousOneStatementBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					int i = 1;							\t
					Object o = new Object() {			\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    Object o = new Object() {
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class with one statement before>"
	);
}
/*
 * Test completion in the second method of an anonymous inner class
 */
public void testAnonymousSecondMethod() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					Object o = new Object() {			\t
						void fuzz() {					\t
						}								\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    Object o = new Object() {
			      void fuzz() {
			      }
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in anonymous inner class in second method>"
	);
}
/*
 * Test completion in the first method of a local type declaration
 */
public void testLocalTypeFirstMethod() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					class InnerBar {					\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
						void fuzz() {					\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    class InnerBar {
			      InnerBar() {
			      }
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			      void fuzz() {
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration in first method>"
	);
}
/*
 * Test completion in local type declaration with no statement defined before.
 */
public void testLocalTypeNoStatementBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					class InnerBar {					\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    class InnerBar {
			      InnerBar() {
			      }
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration with no statement before>"
	);
}
/*
 * Test completion in local type declaration with one field defined before
 * the method containing the completion.
 */
public void testLocalTypeOneFieldBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					class InnerBar {					\t
						int field = 1;					\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    class InnerBar {
			      int field;
			      InnerBar() {
			      }
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration with one field before>"
	);
}
/*
 * Test completion in local type declaration with one statement defined before.
 */
public void testLocalTypeOneStatementBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					int i = 1;							\t
					class InnerBar {					\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    class InnerBar {
			      InnerBar() {
			      }
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration with 1 statement before>"
	);
}
/*
 * Test completion in the second method of a local type declaration
 */
public void testLocalTypeSecondMethod() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					class InnerBar {					\t
						void fuzz() {					\t
						}								\t
						void buzz() {					\t
							int i = fred().xyz;			\t
						}								\t
					};									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    class InnerBar {
			      InnerBar() {
			      }
			      void fuzz() {
			      }
			      void buzz() {
			        int i = <CompleteOnMemberAccess:fred().x>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete in local type declaration in second method>"
	);
}
}
