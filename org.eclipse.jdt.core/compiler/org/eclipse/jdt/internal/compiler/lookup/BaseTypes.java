package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public interface BaseTypes {
	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, "int"/*nonNLS*/.toCharArray(), new char[] {'I'});
	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, "byte"/*nonNLS*/.toCharArray(), new char[] {'B'});
	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, "short"/*nonNLS*/.toCharArray(), new char[] {'S'});
	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, "char"/*nonNLS*/.toCharArray(), new char[] {'C'});
	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, "long"/*nonNLS*/.toCharArray(), new char[] {'J'});
	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, "float"/*nonNLS*/.toCharArray(), new char[] {'F'});
	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, "double"/*nonNLS*/.toCharArray(), new char[] {'D'});
	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, "boolean"/*nonNLS*/.toCharArray(), new char[] {'Z'});
	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, "null"/*nonNLS*/.toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used
	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, "void"/*nonNLS*/.toCharArray(), new char[] {'V'});
}
