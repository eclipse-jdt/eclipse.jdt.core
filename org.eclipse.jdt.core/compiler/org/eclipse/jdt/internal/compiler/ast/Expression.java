package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class Expression extends Statement {
	//some expression may not be used - from a java semantic point
	//of view only - as statements. Other may. In order to avoid the creation
	//of wrappers around expression in order to tune them as expression
	//Expression is a subclass of Statement. See the message isValidJavaStatement()
	
	public int implicitConversion;
	public Constant constant;

public Expression() {
	super();
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	return analyseCode(currentScope, flowContext, flowInfo);
}
public Constant conditionalConstant(){

		return constant;
}
/* Dislike this name
*/

public static final boolean convertToTypeFromTypeValue(int left , int right, Constant cst){
	//true if there is no loose of information while casting.
	//right is constant's type.id

	//a faster solution would be to use combinaison of 
	//some range.........????


	/*
	org.eclipse.jdt.internal.compiler.util.Constant cst ;
	cst = org.eclipse.jdt.internal.compiler.util.Constant.fromValue((float)898565456.0) ;
	org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding.convertToTypeFromTypeValue(
	org.eclipse.jdt.internal.compiler.ast.TypeReference.T_int,
	org.eclipse.jdt.internal.compiler.ast.TypeReference.T_float,
	cst)
	*/
	
	if (left == right) return true ;
	switch(left){
		case T_char 	:	
				switch (right) {
					case T_char	: return true ;
					case T_double : return cst.doubleValue() 	== cst.charValue() ;
					case T_float  : return cst.floatValue() 	== cst.charValue() ;
					case T_int    : return cst.intValue() 	== cst.charValue() ;
					case T_short  : return cst.shortValue() 	== cst.charValue() ;
					case T_byte   : return cst.byteValue() 	== cst.charValue() ;
					case T_long   : return cst.longValue() 	== cst.charValue() ;
					default : return false ;} //boolean

		case T_float 	:	
				switch (right) {
					case T_char	: return cst.charValue() 	== cst.floatValue() ;
					case T_double : return cst.doubleValue() 	== cst.floatValue() ;
					case T_float  : return true;
					case T_int    : return cst.intValue() 	== cst.floatValue() ;
					case T_short  : return cst.shortValue() 	== cst.floatValue() ;
					case T_byte   : return cst.byteValue() 	== cst.floatValue() ;
					case T_long   : return cst.longValue() 	== cst.floatValue() ;
					default : return false ;} //boolean
	case T_double 	:	
				switch (right) {
					case T_char	: return cst.charValue() 	== cst.doubleValue() ;
					case T_double : return true ;
					case T_float  : return cst.floatValue() 	== cst.doubleValue() ;
					case T_int    : return cst.intValue() 	== cst.doubleValue() ;
					case T_short  : return cst.shortValue() 	== cst.doubleValue() ;
					case T_byte   : return cst.byteValue() 	== cst.doubleValue() ;
					case T_long   : return cst.longValue() 	== cst.doubleValue() ;
					default : return false ;} //boolean
	case T_byte 	:	
				switch (right) {
					case T_char	: return cst.charValue() 	== cst.byteValue() ;
					case T_double : return cst.doubleValue() 	== cst.byteValue() ;
					case T_float  : return cst.floatValue() 	== cst.byteValue() ;
					case T_int    : return cst.intValue() 	== cst.byteValue() ;
					case T_short  : return cst.shortValue() 	== cst.byteValue() ;
					case T_byte   : return true ; 
					case T_long   : return cst.longValue() 	== cst.byteValue() ;
					default : return false ;} //boolean
	case T_short 	:	
				switch (right) {
					case T_char	: return cst.charValue() 	== cst.shortValue() ;
					case T_double : return cst.doubleValue() 	== cst.shortValue() ;
					case T_float  : return cst.floatValue() 	== cst.shortValue() ;
					case T_int    : return cst.intValue() 	== cst.shortValue() ;
					case T_short  : return true ;
					case T_byte   : return cst.byteValue() 	== cst.shortValue() ;
					case T_long   : return cst.longValue() 	== cst.shortValue() ;
					default : return false ;} //boolean
	case T_int 	:	
				switch (right) {
					case T_char	: return cst.charValue() 	== cst.intValue() ;
					case T_double : return cst.doubleValue() 	== cst.intValue() ;
					case T_float  : return cst.floatValue() 	== cst.intValue() ;
					case T_int    : return true ; 
					case T_short  : return cst.shortValue() 	== cst.intValue() ;
					case T_byte   : return cst.byteValue() 	== cst.intValue() ;
					case T_long   : return cst.longValue() 	== cst.intValue() ;
					default : return false ;} //boolean
	case T_long 	:	
				switch (right) {
					case T_char	: return cst.charValue() 	== cst.longValue() ;
					case T_double : return cst.doubleValue() 	== cst.longValue() ;
					case T_float  : return cst.floatValue() 	== cst.longValue() ;
					case T_int    : return cst.intValue() 	== cst.longValue() ;
					case T_short  : return cst.shortValue() 	== cst.longValue() ;
					case T_byte   : return cst.byteValue() 	== cst.longValue() ;
					case T_long   : return true ;
					default : return false ;} //boolean
	default : return false ; } //boolean
}
/**
 * Expression statements are plain expressions, however they generate like
 * normal expressions with no value required.
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream 
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	generateCode(currentScope, codeStream, false);
}
/**
 * Every expression is responsible for generating its implicit conversion when necessary.
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	if (constant != NotAConstant) {
		// generate a constant expression
		int pc = codeStream.position;
		codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this);
	} else {
		// actual non-constant code generation
		throw new ShouldNotImplement(Util.bind("ast.missingCode")); //$NON-NLS-1$
	}
}
/**
 * Default generation of a boolean value
 */
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {

	// a label valued to nil means: by default we fall through the case... 
	// both nil means we leave the value on the stack

	if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean)) {
		int pc = codeStream.position;
		if (constant.booleanValue() == true) {
			// constant == true
			if (valueRequired) {
				if (falseLabel == null) {
					// implicit falling through the FALSE case
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
			}
		} else {
			if (valueRequired) {
				if (falseLabel != null) {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.goto_(falseLabel);
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this);
		return;
	}
	generateCode(currentScope, codeStream, valueRequired);
	// branching
	int position = codeStream.position;
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// Implicit falling through the FALSE case
				codeStream.ifne(trueLabel);
			}
		} else {
			if (trueLabel == null) {
				// Implicit falling through the TRUE case
				codeStream.ifeq(falseLabel);
			} else {
				// No implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	// reposition the endPC
	codeStream.updateLastRecordedEndPC(position);
}
public void generateOptimizedStringBuffer(BlockScope blockScope, org.eclipse.jdt.internal.compiler.codegen.CodeStream codeStream, int typeID) {
	/* Optimized (java) code generation for string concatenations that involve StringBuffer
	 * creation: going through this path means that there is no need for a new StringBuffer
	 * creation, further operands should rather be only appended to the current one.
	 * By default: no optimization.
	 */

	generateCode(blockScope, codeStream, true);
	codeStream.invokeStringBufferAppendForType(typeID);
}
public void generateOptimizedStringBufferCreation(BlockScope blockScope, CodeStream codeStream, int typeID) {
	/* Optimized (java) code generation for string concatenations that involve StringBuffer
	 * creation: going through this path means that there is no need for a new StringBuffer
	 * creation, further operands should rather be only appended to the current one.
	 */

	// Optimization only for integers and strings

	if (typeID == T_Object) {
		// in the case the runtime value of valueOf(Object) returns null, we have to use append(Object) instead of directly valueOf(Object)
		// append(Object) returns append(valueOf(Object)), which means that the null case is handled by append(String).
		codeStream.newStringBuffer();
		codeStream.dup();
		codeStream.invokeStringBufferDefaultConstructor();
		generateCode(blockScope, codeStream, true);
		codeStream.invokeStringBufferAppendForType(T_Object);	
		return;
	}
	codeStream.newStringBuffer();
	codeStream.dup();
	if ((typeID == T_String) || (typeID == T_null)) {
		if (constant != NotAConstant) {
			codeStream.ldc(constant.stringValue());
		} else {
			generateCode(blockScope, codeStream, true);
			codeStream.invokeStringValueOf(T_Object);
		}
	} else {
		generateCode(blockScope, codeStream, true);
		codeStream.invokeStringValueOf(typeID);
	}
	codeStream.invokeStringBufferStringConstructor();
}
public void implicitWidening(TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
	// Base types need that the widening is explicitly done by the compiler using some bytecode like i2f

	if (runtimeTimeType == null || compileTimeType == null)
		return;

	if (compileTimeType.id == T_null) {
		// this case is possible only for constant null
		// The type of runtime is a reference type
		// The code gen use the constant id thus any value
		// for the runtime id (akak the <<4) could be used.
		// T_Object is used as some general T_reference
		implicitConversion = (T_Object << 4) + T_null;
		return;
	}

	switch (runtimeTimeType.id) {
		case T_byte :
		case T_short :
		case T_char :
			implicitConversion = (T_int << 4) + compileTimeType.id;
			break;
		case T_String :
		case T_float :
		case T_boolean :
		case T_double :
		case T_int : //implicitConversion may result in i2i which will result in NO code gen
		case T_long :
			implicitConversion = (runtimeTimeType.id << 4) + compileTimeType.id;
			break;
		default : //nothing on regular object ref
	}
}
public boolean isCompactableOperation() {
	return false;
}
public boolean isConstantValueOfTypeAssignableToType(TypeBinding constantType, TypeBinding targetType) {
	//Return true if the conversion is done AUTOMATICALLY by the vm
	//while the javaVM is an int based-machine, thus for example pushing
	//a byte onto the stack , will automatically creates a int on the stack
	//(this request some work d be done by the VM on signed numbers)

	if (constant == Constant.NotAConstant)
		return false;
	if (constantType == targetType)
		return true;
	if (constantType.isBaseType() && targetType.isBaseType()) {
		//No free assignment conversion from anything but to integral ones.
		if ((constantType == IntBinding || BaseTypeBinding.isWidening(T_int, constantType.id))
			&& (BaseTypeBinding.isNarrowing(targetType.id, T_int))) {
				//use current explicit conversion in order to get some new value to compare with current one
				return convertToTypeFromTypeValue(targetType.id, constantType.id, constant);
		}
	}
	return false;
}
public boolean isTypeReference() {
	return false;
}
public void resolve(BlockScope scope) {
	// drops the returning expression's type whatever the type is.

	this.resolveType(scope);
	return;
}
public TypeBinding resolveType(BlockScope scope) {
	// by default... subclasses should implement a better TC if required.

	return null;
}
public TypeBinding resolveTypeExpecting(BlockScope scope, TypeBinding expectedTb) {
	TypeBinding thisTb = this.resolveType(scope);
	if (thisTb == null)
		return null;
	if (!scope.areTypesCompatible(thisTb, expectedTb)) {
		scope.problemReporter().typeMismatchError(thisTb, expectedTb, this);
		return null;
	}
	return thisTb;
}
public String toString(int tab) {

	//Subclass re-define toStringExpression

	String s = tabString(tab);
	if (constant != null)
		//before TC has runned
		if (constant != NotAConstant)
			//after the TC has runned
			s += " /*cst:" + constant.toString() + "*/ "; //$NON-NLS-1$ //$NON-NLS-2$
	return s + toStringExpression(tab);
}
public String toStringExpression() {

	//Subclass re-define toStringExpression
	//This method is abstract and should never be called
	//but we provide some code that is running.....just in case
	//of developpement time (while every  thing is not built)
	
	return super.toString(0);}
public String toStringExpression(int tab) {
	// default is regular toString expression (qualified allocation expressions redifine this method)
	return this.toStringExpression();
}
public Expression toTypeReference(){
	//by default undefined

	//this method is meanly used by the parser in order to transform
	//an expression that is used as a type reference in a cast ....
	//--appreciate the fact that castExpression and ExpressionWithParenthesis
	//--starts with the same pattern.....
	
	return this; }
}
