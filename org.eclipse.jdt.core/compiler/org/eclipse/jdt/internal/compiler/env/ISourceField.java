package org.eclipse.jdt.internal.compiler.env;

public interface ISourceField extends IGenericField {
/**
 * Answer the source end position of the field's declaration.
 */

int getDeclarationSourceEnd();
/**
 * Answer the source start position of the field's declaration.
 */

int getDeclarationSourceStart();
/**
 * Answer the source end position of the field's name.
 */

int getNameSourceEnd();
/**
 * Answer the source start position of the field's name.
 */

int getNameSourceStart();
/**
 * Answer the type name of the field.
 *
 * The name is a simple name or a qualified, dot separated name.
 * For example, Hashtable or java.util.Hashtable.
 */

char[] getTypeName();
}
