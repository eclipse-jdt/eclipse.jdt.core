package org.eclipse.jdt.internal.codeassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * A completion requestor accepts results as they are computed and is aware
 * of source positions to complete the various different results.
 */

import org.eclipse.jdt.internal.compiler.*;

public interface ICompletionRequestor {
	/**
	 * Code assist notification of a class completion.
	 * 
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param packageName char[] - Declaring package name of the class.
	 * @param className char[] - Name of the class.
	 * @param completionName char[] - The completion for the class.
	 *   Can include ';' for imported classes.
	 * @param modifiers int - The modifiers of the class.
	 *	 	@see com.ibm.compiler.java.ast.Modifiers
	 * @param completionStart int - The start position of insertion of the name of the class.
	 * @param completionEnd int - The end position of insertion of the name of the class.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	void acceptClass(
		char[] packageName,
		char[] className,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of a compilation error detected during completion.
	 *
	 *  @return void - Nothing is answered back to code assist engine
	 *
	 *  @param error com.ibm.compiler.java.api.problem.IProblem
	 *      Only problems which are categorized as errors are notified to the requestor,
	 *		warnings are silently ignored.
	 *		In case an error got signaled, no other completions might be available,
	 *		therefore the problem message should be presented to the user.
	 *		The source positions of the problem are related to the source where it was
	 *		detected (might be in another compilation unit, if it was indirectly requested
	 *		during the code assist process).
	 *      Note: the problem knows its originating file name.
	 */
	void acceptError(IProblem error);
	/**
	 * Code assist notification of a field completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param declaringTypePackageName char[] - Name of the package in which the type that contains this field is declared.
	 * @param declaringTypeName char[] - Name of the type declaring this new field.
	 * @param name char[] - Name of the field.
	 * @param typePackageName char[] - Name of the package in which the type of this field is declared.
	 * @param typeName char[] - Name of the type of this field.
	 * @param completionName char[] - The completion for the field.
	 * @param modifiers int - The modifiers of this field.
	 * @param completionStart int - The start position of insertion of the name of this field.
	 * @param completionEnd int - The end position of insertion of the name of this field.
	 * @see com.ibm.compiler.java.ast.Modifiers
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
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of an interface completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param packageName char[] - Declaring package name of the interface.
	 * @param className char[] - Name of the interface.
	 * @param completionName char[] - The completion for the interface.
	 *   Can include ';' for imported interfaces.
	 * @param modifiers int - The modifiers of the interface.
	 *	 	@see com.ibm.compiler.java.ast.Modifiers
	 * @param completionStart int - The start position of insertion of the name of the interface.
	 * @param completionEnd int - The end position of insertion of the name of the interface.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	void acceptInterface(
		char[] packageName,
		char[] interfaceName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of a keyword completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param keywordName char[] - The keyword source.
	 * @param completionStart int - The start position of insertion of the name of this keyword.
	 * @param completionEnd int - The end position of insertion of the name of this keyword.
	 */
	void acceptKeyword(char[] keywordName, int completionStart, int completionEnd);
	/**
	 * Code assist notification of a label completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param labelName char[] - The label source.
	 * @param completionStart int - The start position of insertion of the name of this label.
	 * @param completionEnd int - The end position of insertion of the name of this label.
	 */
	void acceptLabel(char[] labelName, int completionStart, int completionEnd);
	/**
	 * Code assist notification of a local variable completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param name char[] - Name of the new local variable.
	 * @param typePackageName char[] - Name of the package in which the type of this new local variable is declared.
	 * @param typeName char[] - Name of the type of this new local variable.
	 * @param modifiers int - The modifiers of this new local variable.
	 * @param completionStart int - The start position of insertion of the name of this new local variable.
	 * @param completionEnd int - The end position of insertion of the name of this new local variable.
	 * @see com.ibm.compiler.java.ast.Modifiers
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
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of a method completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param declaringTypePackageName char[] - Name of the package in which the type that contains this new method is declared.
	 * @param declaringTypeName char[] - Name of the type declaring this new method.
	 * @param selector char[] - Name of the new method.
	 * @param parameterPackageNames char[][] -  Names of the packages in which the parameter types are declared.
	 *    Should contain as many elements as parameterTypeNames.
	 * @param parameterTypeNames char[][] - Names of the parameters types.
	 *    Should contain as many elements as parameterPackageNames.
	 * @param returnTypePackageName char[] - Name of the package in which the return type is declared.
	 * @param returnTypeName char[] - Name of the return type of this new method, should be null for a constructor.
	 * @param completionName char[] - The completion for the method.
	 *   Can include zero, one or two brackets. If the closing bracket is included, then the cursor should be placed before it.
	 * @param modifiers int - The modifiers of this new method.
	 * @param completionStart int - The start position of insertion of the name of this new method.
	 * @param completionEnd int - The end position of insertion of the name of this new method.
	 * @see com.ibm.compiler.java.ast.Modifiers
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
		char[] returnTypePackageName,
		char[] returnTypeName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of a modifier completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param modifierName char[] - The new modifier.
	 * @param completionStart int - The start position of insertion of the name of this new modifier.
	 * @param completionEnd int - The end position of insertion of the name of this new modifier.
	 */
	void acceptModifier(
		char[] modifierName,
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of a package completion.
	 *
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param packageName char[] - The package name.
	 * @param completionName char[] - The completion for the package.
	 *   Can include '.*;' for imports.
	 * @param completionStart int - The start position of insertion of the name of this new package.
	 * @param completionEnd int - The end position of insertion of the name of this new package.
	 *
	 * NOTE - All package names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    The default package is represented by an empty array.
	 */
	void acceptPackage(
		char[] packageName,
		char[] completionName,
		int completionStart,
		int completionEnd);
	/**
	 * Code assist notification of a type completion.
	 * 
	 * @return void - Nothing is answered back to code assist engine
	 *
	 * @param packageName char[] - Declaring package name of the type.
	 * @param typeName char[] - Name of the type.
	 * @param completionName char[] - The completion for the type.
	 *   Can include ';' for imported types.
	 * @param completionStart int - The start position of insertion of the name of the type.
	 * @param completionEnd int - The end position of insertion of the name of the type.
	 *
	 * NOTE - All package and type names are presented in their readable form:
	 *    Package names are in the form "a.b.c".
	 *    Nested type names are in the qualified form "A.M".
	 *    The default package is represented by an empty array.
	 */
	void acceptType(
		char[] packageName,
		char[] typeName,
		char[] completionName,
		int completionStart,
		int completionEnd);
}
