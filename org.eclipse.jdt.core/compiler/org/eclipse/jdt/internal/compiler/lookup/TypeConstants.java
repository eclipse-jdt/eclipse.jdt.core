package org.eclipse.jdt.internal.compiler.lookup;

public interface TypeConstants {
	final char[] JAVA = new char[] {'j', 'a', 'v', 'a'};
	final char[] LANG = new char[] {'l', 'a', 'n', 'g'};
	final char[] IO = new char[] {'i', 'o'};
	final char[] REFLECT = new char[] {'r', 'e', 'f', 'l', 'e', 'c', 't'};
	final char[] CharArray_JAVA_LANG_OBJECT = new char[] {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'};
	final char[] LENGTH = new char[] {'l', 'e', 'n', 'g', 't', 'h'};
	final char[] CLONE = new char[] {'c', 'l', 'o', 'n', 'e'};

	// Constant compound names
	final char[][] JAVA_LANG = new char[][] {JAVA, LANG};
	final char[][] JAVA_IO = new char[][] {JAVA, IO};
	final char[][] JAVA_LANG_CLASS = new char[][] {JAVA, LANG, {'C', 'l', 'a', 's', 's'}};
	final char[][] JAVA_LANG_CLASSNOTFOUNDEXCEPTION = new char[][] {JAVA, LANG, {'C', 'l', 'a', 's', 's', 'N', 'o', 't', 'F', 'o', 'u', 'n', 'd', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n'}};
	final char[][] JAVA_LANG_CLONEABLE = new char[][] {JAVA, LANG, {'C', 'l', 'o', 'n', 'e', 'a', 'b', 'l', 'e'}};
	final char[][] JAVA_LANG_EXCEPTION = new char[][] {JAVA, LANG, {'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n'}};
	final char[][] JAVA_LANG_ERROR = new char[][] {JAVA, LANG, {'E', 'r', 'r', 'o', 'r'}};
	final char[][] JAVA_LANG_NOCLASSDEFERROR = new char[][] {JAVA, LANG, {'N', 'o', 'C', 'l', 'a', 's', 's', 'D', 'e', 'f', 'E', 'r', 'r', 'o', 'r'}};
	final char[][] JAVA_LANG_OBJECT = new char[][] {JAVA, LANG, {'O', 'b', 'j', 'e', 'c', 't'}};
	final char[][] JAVA_LANG_STRING = new char[][] {JAVA, LANG, {'S', 't', 'r', 'i', 'n', 'g'}};
	final char[][] JAVA_LANG_STRINGBUFFER = new char[][] {JAVA, LANG, {'S', 't', 'r', 'i', 'n', 'g', 'B', 'u', 'f', 'f', 'e', 'r'}};
	final char[][] JAVA_LANG_SYSTEM = new char[][] {JAVA, LANG, {'S', 'y', 's', 't', 'e', 'm'}};
	final char[][] JAVA_LANG_RUNTIMEEXCEPTION = new char[][] {JAVA, LANG, {'R', 'u', 'n', 't', 'i', 'm', 'e', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n'}};
	final char[][] JAVA_LANG_THROWABLE = new char[][] {JAVA, LANG, {'T', 'h', 'r', 'o', 'w', 'a', 'b', 'l', 'e'}};
	final char[][] JAVA_LANG_REFLECT_CONSTRUCTOR = new char[][] {JAVA, LANG, REFLECT, {'C', 'o', 'n', 's', 't', 'r', 'u', 'c', 't', 'o', 'r'}};
	final char[][] JAVA_IO_PRINTSTREAM = new char[][] {JAVA, IO, {'P', 'r', 'i', 'n', 't', 'S', 't', 'r', 'e', 'a', 'm'}};
	final char[][] JAVA_IO_SERIALIZABLE = new char[][] {JAVA, IO, {'S', 'e', 'r', 'i', 'a', 'l', 'i', 'z', 'a', 'b', 'l', 'e'}};
	final char[][] JAVA_LANG_BYTE = new char[][] {JAVA, LANG, "Byte".toCharArray()};
	final char[][] JAVA_LANG_SHORT = new char[][] {JAVA, LANG, "Short".toCharArray()};
	final char[][] JAVA_LANG_CHARACTER = new char[][] {JAVA, LANG, "Character".toCharArray()};
	final char[][] JAVA_LANG_INTEGER = new char[][] {JAVA, LANG, "Integer".toCharArray()};
	final char[][] JAVA_LANG_LONG = new char[][] {JAVA, LANG, "Long".toCharArray()};
	final char[][] JAVA_LANG_FLOAT = new char[][] {JAVA, LANG, "Float".toCharArray()};
	final char[][] JAVA_LANG_DOUBLE = new char[][] {JAVA, LANG, "Double".toCharArray()};
	final char[][] JAVA_LANG_BOOLEAN = new char[][] {JAVA, LANG, "Boolean".toCharArray()};
	final char[][] JAVA_LANG_VOID = new char[][] {JAVA, LANG, "Void".toCharArray()};

	// Constants used by the flow analysis
	final int EqualOrMoreSpecific = -1;
	final int NotRelated = 0;
	final int MoreGeneric = 1;

	// Empty Collection which can later assign to null if performance is an issue.
	final char[] NoChar = new char[0];
	final char[][] NoCharChar = new char[0][];
	// Method collections
	final TypeBinding[] NoParameters = new TypeBinding[0];
	final ReferenceBinding[] NoExceptions = new ReferenceBinding[0];
	// Type collections
	final FieldBinding[] NoFields = new FieldBinding[0];
	final MethodBinding[] NoMethods = new MethodBinding[0];
	final ReferenceBinding[] NoSuperInterfaces = new ReferenceBinding[0];
	final ReferenceBinding[] NoMemberTypes = new ReferenceBinding[0];
}
