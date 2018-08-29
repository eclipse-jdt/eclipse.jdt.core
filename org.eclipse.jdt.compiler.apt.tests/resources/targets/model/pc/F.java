/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// CAUTION!  THIS FILE CONTAINS SPECIFIC COMBINATIONS OF TABS AND SPACES,
// FOR TESTING WHITESPACE HANDLING IN JAVADOC.  DO NOT REFORMAT OR MODIFY
// WHITESPACE IN ANY JAVADOC IN THIS FILE.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

package targets.model.pc;

/**
 * Javadoc on element F
 * @param <T1> a type parameter
 */
@AnnoY("on F")
public class F<T1> {
	/**
	 * Javadoc on nested element FChild
	 */  
	public class FChild {
	}
	
	/**
	 * Javadoc on nested enum FEnum
	 * Two lines long
	 */
	enum FEnum { FEnum1, FEnum2 }

	/**
	 * Javadoc on nested interface FChildI
	 *	this line has tab after asterisk and ends with another tab	
	 *	this one too	
	 *   this line has three spaces after asterisk and ends with three spaces   
 * this line has only one space before the asterisk
	 */
	public interface FChildI {}
	
	/** Javadoc on field _fieldT1_protected, inline format */
	protected T1 _fieldT1_protected;
	
	// Next line has a space at the end, after the double asterisks
	/** 
	 * Javadoc on _fieldT1_private
  this line starts with two spaces, no asterisk
	This line starts, contains	and ends with a tab	
 	 	 This line starts with a space, tab, space, tab, space
	 */
	private T1 _fieldT1_private;
	
	int fieldInt;
	
	/**
	 * Javadoc on F.method_T1
	 */
	@AnnoY("on F.method_T1")
	T1 method_T1(T1 param1) 
	{
		return null;
	}
	
	String method_String(T1 param1)
	{
		_fieldT1_private = param1;
		return _fieldT1_private.toString();
	}
	
	// not hidden by G.staticMethod() - private
	private static void staticMethod()
	{
	}

	// not hidden by G.staticMethod - different name
	public static void staticMethod2()
	{
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	void deprecatedMethod()
	{
	}
	
	// unrelated to the same-signature methods in Overriding.java
	void f() {}
	
	/************************/
	/*** Abstract methods ***/
	/************************/

	public Object getAnonymousObjectAbstract() {
		return null;
	}

	/** 
	 *	@add(int)
	 */
	int add (int j) {
		return j + j;
	} 

	/** 
	 * Creates a new instance of AllChecks 
	 */
	public void foo() {
	}

	/**
	 * @bar	(int)
	 */
	int bar(int i) { return i; }
	
	/**
	 *	@bar2(int)
	 */
	int bar2(int i) { return i; }
	
/**
		Method	m
 */
	void m() {}
	
    /** This is a comment for the method m1,
     *  it is on two lines
     */
	void m1() {}
	
	/** Another comment - starts on first line and
    continue on the second line */
	void m2() {}
	
	/**
	 * One more test case that.
	 * needs
	 * to be verified.
	 *
	 * An empty line with no spaces need to be seen as an empty line.
	 *
	 * End of the comment.
	 */
	void m3() {}
}