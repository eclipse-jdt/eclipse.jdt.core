package org.eclipse.jdt.core;

/**
 * A callback interface for receiving java problem correction.
 * 
 * @since 2.0
 */
public interface ICorrectionRequestor {
/**
 * Notification of a class correction.
 * 
 * @return void - Nothing is answered back to correction engine
 *
 * @param packageName char[] - Declaring package name of the class.
 * @param className char[] - Name of the class.
 * @param correctionName char[] - The correction for the class.
 * @param modifiers int - The modifiers of the class.
 * @param correctionStart int - The start position of insertion of the correction of the class.
 * @param correctionEnd int - The end position of insertion of the correction of the class.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptClass(
	char[] packageName,
	char[] className,
	char[] correctionName,
	int modifiers,
	int correctionStart,
	int correctionEnd);
/**
 * Notification of a field correction.
 *
 * @return void - Nothing is answered back to correction engine
 *
 * @param declaringTypePackageName char[] - Name of the package in which the type that contains this field is declared.
 * @param declaringTypeName char[] - Name of the type declaring this field.
 * @param name char[] - Name of the field.
 * @param typePackageName char[] - Name of the package in which the type of this field is declared.
 * @param typeName char[] - Name of the type of this field.
 * @param correctionName char[] - The correction for the field.
 * @param modifiers int - The modifiers of this field.
 * @param correctionStart int - The start position of insertion of the correction of this field.
 * @param correctionEnd int - The end position of insertion of the correction of this field.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptField(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] name,
	char[] typePackageName,
	char[] typeName,
	char[] correctionName,
	int modifiers,
	int correctionStart,
	int correctionEnd);
/**
 * Notification of an interface correction.
 *
 * @return void - Nothing is answered back to correction engine
 *
 * @param packageName char[] - Declaring package name of the interface.
 * @param className char[] - Name of the interface.
 * @param correctionName char[] - The correction for the interface.
 *   Can include ';' for imported interfaces.
 * @param modifiers int - The modifiers of the interface.
 * @param correctionStart int - The start position of insertion of the correction of the interface.
 * @param correctionEnd int - The end position of insertion of the correction of the interface.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptInterface(
	char[] packageName,
	char[] interfaceName,
	char[] correctionName,
	int modifiers,
	int correctionStart,
	int correctionEnd);
/**
 * Notification of a local variable correction.
 *
 * @return void - Nothing is answered back to correction engine
 *
 * @param name char[] - Name of the local variable.
 * @param typePackageName char[] - Name of the package in which the type of this local variable is declared.
 * @param typeName char[] - Name of the type of this local variable.
 * @param modifiers int - The modifiers of this local variable.
 * @param correctionStart int - The start position of insertion of the correction of this local variable.
 * @param correctionEnd int - The end position of insertion of the correction of this local variable.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
void acceptLocalVariable(
	char[] name,
	char[] typePackageName,
	char[] typeName,
	int modifiers,
	int correctionStart,
	int correctionEnd);
/**
 * Notification of a method correction.
 *
 * @return void - Nothing is answered back to correction engine
 *
 * @param declaringTypePackageName char[] - Name of the package in which the type that contains this method is declared.
 * @param declaringTypeName char[] - Name of the type declaring this method.
 * @param selector char[] - Name of the method.
 * @param parameterPackageNames char[][] -  Names of the packages in which the parameter types are declared.
 *    Should contain as many elements as parameterTypeNames.
 * @param parameterTypeNames char[][] - Names of the parameters types.
 *    Should contain as many elements as parameterPackageNames.
 * @param returnTypePackageName char[] - Name of the package in which the return type is declared.
 * @param returnTypeName char[] - Name of the return type of this method, should be <code>null</code> for a constructor.
 * @param correctionName char[] - The correction for the method.
 *   Can include zero, one or two brackets. If the closing bracket is included, then the cursor should be placed before it.
 * @param modifiers int - The modifiers of this method.
 * @param correctionStart int - The start position of insertion of the correction of this method.
 * @param correctionEnd int - The end position of insertion of the correction of this method.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Base types are in the form "int" or "boolean".
 *    Array types are in the qualified form "M[]" or "int[]".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 *
 * NOTE: parameter names can be retrieved from the source model after the user selects a specific method.
 */
void acceptMethod(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] selector,
	char[][] parameterPackageNames,
	char[][] parameterTypeNames,
	char[][] parameterNames,
	char[] returnTypePackageName,
	char[] returnTypeName,
	char[] correctionName,
	int modifiers,
	int correctionStart,
	int correctionEnd);
/**
 * Notification of a package correction.
 *
 * @return void - Nothing is answered back to correction engine
 *
 * @param packageName char[] - The package name.
 * @param correctionName char[] - The correction for the package.
 *   Can include '.*;' for imports.
 * @param correctionStart int - The start position of insertion of the correction of this package.
 * @param correctionEnd int - The end position of insertion of the correction of this package.
 *
 * NOTE - All package names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    The default package is represented by an empty array.
 */
void acceptPackage(
	char[] packageName,
	char[] correctionName,
	int correctionStart,
	int correctionEnd);
}
