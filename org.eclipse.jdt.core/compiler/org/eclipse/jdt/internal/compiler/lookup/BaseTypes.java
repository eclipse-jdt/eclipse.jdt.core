package org.eclipse.jdt.internal.compiler.lookup;

public interface BaseTypes {
	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, "int".toCharArray(), new char[] {'I'});
	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, "byte".toCharArray(), new char[] {'B'});
	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, "short".toCharArray(), new char[] {'S'});
	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, "char".toCharArray(), new char[] {'C'});
	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, "long".toCharArray(), new char[] {'J'});
	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, "float".toCharArray(), new char[] {'F'});
	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, "double".toCharArray(), new char[] {'D'});
	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, "boolean".toCharArray(), new char[] {'Z'});
	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, "null".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used
	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, "void".toCharArray(), new char[] {'V'});
}
