/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public interface TypeConstants {
	final char[] JAVA = "java".toCharArray(); //$NON-NLS-1$
	final char[] LANG = "lang".toCharArray(); //$NON-NLS-1$
	final char[] IO = "io".toCharArray(); //$NON-NLS-1$
	final char[] REFLECT = "reflect".toCharArray(); //$NON-NLS-1$
	final char[] CharArray_JAVA_LANG_OBJECT = "java.lang.Object".toCharArray(); //$NON-NLS-1$
	final char[] LENGTH = "length".toCharArray(); //$NON-NLS-1$
	final char[] CLONE = "clone".toCharArray(); //$NON-NLS-1$
	final char[] GETCLASS = "getClass".toCharArray(); //$NON-NLS-1$
	final char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
	final char[] MAIN = "main".toCharArray(); //$NON-NLS-1$
	final char[] SERIALVERSIONUID = "serialVersionUID".toCharArray(); //$NON-NLS-1$
	final char[] SERIALPERSISTENTFIELDS = "serialPersistentFields".toCharArray(); //$NON-NLS-1$ 
	final char[] READRESOLVE = "readResolve".toCharArray(); //$NON-NLS-1$
	final char[] WRITEREPLACE = "writeReplace".toCharArray(); //$NON-NLS-1$
	final char[] READOBJECT = "readObject".toCharArray(); //$NON-NLS-1$
	final char[] WRITEOBJECT = "writeObject".toCharArray(); //$NON-NLS-1$
	final char[] CharArray_JAVA_IO_OBJECTINPUTSTREAM = "java.io.ObjectInputStream".toCharArray(); //$NON-NLS-1$
	final char[] CharArray_JAVA_IO_OBJECTOUTPUTSTREAM = "java.io.ObjectOutputStream".toCharArray(); //$NON-NLS-1$
	final char[] CharArray_JAVA_IO_OBJECTSTREAMFIELD = "java.io.ObjectStreamField".toCharArray(); //$NON-NLS-1$
	final char[] ANONYM_PREFIX = "new ".toCharArray(); //$NON-NLS-1$
	final char[] ANONYM_SUFFIX = "(){}".toCharArray(); //$NON-NLS-1$
    final char[] WILDCARD_NAME = { '?' };
    final char[] WILDCARD_SUPER = " super ".toCharArray(); //$NON-NLS-1$
    final char[] WILDCARD_EXTENDS = " extends ".toCharArray(); //$NON-NLS-1$
    final char[] WILDCARD_MINUS = { '-' };
    final char[] WILDCARD_STAR = { '*' };
    final char[] WILDCARD_PLUS = { '+' };
    
	// Constant compound names
	final char[][] JAVA_LANG = {JAVA, LANG};
	final char[][] JAVA_IO = {JAVA, IO};
	final char[][] JAVA_LANG_ASSERTIONERROR = {JAVA, LANG, "AssertionError".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_CLASS = {JAVA, LANG, "Class".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_CLASSNOTFOUNDEXCEPTION = {JAVA, LANG, "ClassNotFoundException".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_CLONEABLE = {JAVA, LANG, "Cloneable".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_EXCEPTION = {JAVA, LANG, "Exception".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_ERROR = {JAVA, LANG, "Error".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_ITERABLE = {JAVA, LANG, "Iterable".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_NOCLASSDEFERROR = {JAVA, LANG, "NoClassDefError".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_OBJECT = {JAVA, LANG, OBJECT};
	final char[][] JAVA_LANG_STRING = {JAVA, LANG, "String".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_STRINGBUFFER = {JAVA, LANG, "StringBuffer".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_STRINGBUILDER = {JAVA, LANG, "StringBuilder".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_SYSTEM = {JAVA, LANG, "System".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_RUNTIMEEXCEPTION = {JAVA, LANG, "RuntimeException".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_THROWABLE = {JAVA, LANG, "Throwable".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_REFLECT_CONSTRUCTOR = {JAVA, LANG, REFLECT, "Constructor".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_IO_PRINTSTREAM = {JAVA, IO, "PrintStream".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_IO_SERIALIZABLE = {JAVA, IO, "Serializable".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_BYTE = {JAVA, LANG, "Byte".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_SHORT = {JAVA, LANG, "Short".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_CHARACTER = {JAVA, LANG, "Character".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_INTEGER = {JAVA, LANG, "Integer".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_LONG = {JAVA, LANG, "Long".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_FLOAT = {JAVA, LANG, "Float".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_DOUBLE = {JAVA, LANG, "Double".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_BOOLEAN = {JAVA, LANG, "Boolean".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_LANG_VOID = {JAVA, LANG, "Void".toCharArray()}; //$NON-NLS-1$
	final char[][] JAVA_UTIL_ITERATOR = {JAVA, "util".toCharArray(), "Iterator".toCharArray()}; //$NON-NLS-1$//$NON-NLS-2$

	// Constants used by the flow analysis
	final int EqualOrMoreSpecific = -1;
	final int NotRelated = 0;
	final int MoreGeneric = 1;

	// Method collections
	final TypeBinding[] NoParameters = new TypeBinding[0];
	final ReferenceBinding[] NoExceptions = new ReferenceBinding[0];
	final ReferenceBinding[] AnyException = new ReferenceBinding[] { null }; // special handler for all exceptions
	// Type collections
	final FieldBinding[] NoFields = new FieldBinding[0];
	final MethodBinding[] NoMethods = new MethodBinding[0];
	final ReferenceBinding[] NoSuperInterfaces = new ReferenceBinding[0];
	final ReferenceBinding[] NoMemberTypes = new ReferenceBinding[0];
	final TypeVariableBinding[] NoTypeVariables = new TypeVariableBinding[0];
}
