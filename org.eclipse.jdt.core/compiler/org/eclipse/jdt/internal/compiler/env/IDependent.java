package org.eclipse.jdt.internal.compiler.env;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/**
 * This represents the target (ie. the file) of a type dependency.
 *
 * All implementors of this interface are containers for types or types
 * themselves which must be able to identify their source file name
 * when file dependencies are collected.
 * @see IDependencyInfo
 */
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
