package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Member is a mixin interface that represents identifying information about
 * a single member (a field or method), a constructor, or a nested class.
 *
 * @see IType
 * @see IField
 * @see IMethod
 * @see IConstructor
 */
public interface IMember extends IHandle {


	/**
	 * Returns the Type object representing the class or interface
	 * that declares the the member represented by this object.
	 * Derived from java.lang.reflect.Member.getDeclaringClass();
	 * This is a handle-only method.
	 */
	IType getDeclaringClass();
	/**
	 * Returns the Java language modifiers for the member 
	 * represented by this object, as an integer.  
	 * The Flags class should be used to decode the modifiers in
	 * the integer.
	 * 
	 * @exception NotPresentException if the member is not present.
	 *
	 * @see Flags
	 */
	int getModifiers() throws NotPresentException;
	/**
	 * Returns the simple name of the member represented by this object.
	 * If this Member represents a constructor, this returns 
	 * the simple name of its declaring class.
	 * This is a handle-only method.
	 */
	String getName();
	/**
	 * Return <code>true</code> if this represents a binary member,
	 * <code>false</code> otherwise.
	 * A binary member is one for which the declaring class is in 
	 * .class file format in the workspace.
	 *
	 * @return  <code>true</code> if this object represents a binary member;
	 *          <code>false</code> otherwise.
	 * @exception NotPresentException if this type is not present.
	 */
	boolean isBinary() throws NotPresentException;
	/**
	 * Return <code>true</code> if this represents a deprecated member,
	 * <code>false</code> otherwise.
	 * A deprecated object is one that has a 'deprecated' tag in 
	 * its doc comment.
	 *
	 * @return  <code>true</code> if this object represents a deprecated member;
	 *          <code>false</code> otherwise.
	 * @exception NotPresentException if this type is not present.
	 */
	boolean isDeprecated() throws NotPresentException;
	/**
	 * Returns true if the member represented by this object is
	 * synthetic, false otherwise.  A synthetic object is one that
	 * was invented by the compiler, but was not declared in the source.
	 * See <em>The Inner Classes Specification</em>.
	 * A synthetic object is not the same as a fictional object.
	 * 
	 * @exception NotPresentException if the member is not present.
	 *
	 * @see IHandle#isFictional
	 */
	boolean isSynthetic() throws NotPresentException;
}
