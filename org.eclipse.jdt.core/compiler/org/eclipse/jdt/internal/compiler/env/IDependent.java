package org.eclipse.jdt.internal.compiler.env;

public interface IDependent {
/**
 * Answer the file name which defines the type.
 *
 * The path part (optional) must be separated from the actual
 * file proper name by a java.io.File.separator.
 *
 * The proper file name includes the suffix extension (e.g. ".java")
 *
 * e.g. "c:/com/ibm/compiler/java/api/Compiler.java" 
 */

char[] getFileName();
}
