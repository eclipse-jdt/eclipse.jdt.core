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
import org.eclipse.jdt.internal.compiler.util.*;

public class FieldReference extends Reference implements InvocationSite {
	public Expression receiver;
	public char[] token;
	public FieldBinding binding;

	public long nameSourcePosition ; //(start<<32)+end
	

	MethodBinding syntheticReadAccessor, syntheticWriteAccessor;
	public TypeBinding receiverType;

public FieldReference(char[] source , long pos) {
		token = source ;
		nameSourcePosition = pos;
		//by default the position are the one of the field (not true for super access)
		sourceStart = (int) (pos>>>32) ;
		sourceEnd = (int) (pos & 0x00000000FFFFFFFFL);

	
}
public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {

	// compound assignment extra work
	if (isCompound) { // check the variable part is initialized if blank final
		if (binding.isFinal() && receiver.isThis() && currentScope.allowBlankFinalFieldAssignment(binding) && (!flowInfo.isDefinitelyAssigned(binding))) {
			currentScope.problemReporter().uninitializedBlankFinalField(binding, this);
			// we could improve error msg here telling "cannot use compound assignment on final blank field"
		}
		manageSyntheticReadAccessIfNecessary(currentScope);
	}
	if (assignment.expression != null) {
		flowInfo = assignment.expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
	}
	flowInfo = receiver.analyseCode(currentScope, flowContext, flowInfo, !binding.isStatic()).unconditionalInits();
	manageSyntheticWriteAccessIfNecessary(currentScope);

	// check if assigning a final field 
	if (binding.isFinal()) {
		// in a context where it can be assigned?
		if (receiver.isThis() && currentScope.allowBlankFinalFieldAssignment(binding)) {
			if (flowInfo.isPotentiallyAssigned(binding)) {
				currentScope.problemReporter().duplicateInitializationOfBlankFinalField(binding, this);
			}
			flowInfo.markAsDefinitelyAssigned(binding);
			flowContext.recordSettingFinal(binding, this);
		} else {
			// assigning a final field outside an initializer or constructor
			currentScope.problemReporter().cannotAssignToFinalField(binding, this);
		}
	}
	return flowInfo;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return analyseCode(currentScope, flowContext, flowInfo, true);
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	receiver.analyseCode(currentScope, flowContext, flowInfo, !binding.isStatic());
	if (valueRequired) {
		manageSyntheticReadAccessIfNecessary(currentScope);
	}
	return flowInfo;
}
public FieldBinding fieldBinding() {
	//FLOW ANALYSIS
	
	return binding ; }
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {

	receiver.generateCode(currentScope, codeStream, !binding.isStatic());
	assignment.expression.generateCode(currentScope, codeStream, true);
	fieldStore(codeStream, binding, syntheticWriteAccessor, valueRequired);
	if (valueRequired){
		codeStream.generateImplicitConversion(assignment.implicitConversion);
	}
}
/**
 * Field reference code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (constant != NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(constant, implicitConversion);
		}
	} else {
		boolean isStatic = binding.isStatic();
		receiver.generateCode(currentScope, codeStream, valueRequired && (!isStatic) && (binding.constant == NotAConstant));
		if (valueRequired) {
			if (binding.constant == NotAConstant) {
				if (binding.declaringClass == null) { // array length
					codeStream.arraylength();
				} else {
					if (syntheticReadAccessor == null) {
						if (isStatic) {
							codeStream.getstatic(binding);
						} else {
							codeStream.getfield(binding);
						}
					} else {
						codeStream.invokestatic(syntheticReadAccessor);
					}
				}
				codeStream.generateImplicitConversion(implicitConversion);
			} else {
				codeStream.generateConstant(binding.constant, implicitConversion);
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this);
}
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	boolean isStatic;
	receiver.generateCode(currentScope, codeStream, !(isStatic = binding.isStatic()));
	if (isStatic) {
		if (syntheticReadAccessor == null) {
			codeStream.getstatic(binding);
		} else {
			codeStream.invokestatic(syntheticReadAccessor);
		}
	} else {
		codeStream.dup();
		if (syntheticReadAccessor == null) {
			codeStream.getfield(binding);
		} else {
			codeStream.invokestatic(syntheticReadAccessor);
		}
	}
	int operationTypeID;
	if ((operationTypeID = implicitConversion >> 4) == T_String) {
		codeStream.generateStringAppend(currentScope, null, expression);
	} else {
		// promote the array reference to the suitable operation type
		codeStream.generateImplicitConversion(implicitConversion);
		// generate the increment value (will by itself  be promoted to the operation value)
		if (expression == IntLiteral.One){ // prefix operation
			codeStream.generateConstant(expression.constant, implicitConversion);			
		} else {
			expression.generateCode(currentScope, codeStream, true);
		}		
		// perform the operation
		codeStream.sendOperator(operator, operationTypeID);
		// cast the value back to the array reference type
		codeStream.generateImplicitConversion(assignmentImplicitConversion);
	}
		fieldStore(codeStream, binding, syntheticWriteAccessor, valueRequired);
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	boolean isStatic;
	receiver.generateCode(currentScope, codeStream, !(isStatic = binding.isStatic()));
	if (isStatic) {
		if (syntheticReadAccessor == null) {
			codeStream.getstatic(binding);
		} else {
			codeStream.invokestatic(syntheticReadAccessor);
		}
	} else {
		codeStream.dup();
		if (syntheticReadAccessor == null) {
			codeStream.getfield(binding);
		} else {
			codeStream.invokestatic(syntheticReadAccessor);
		}
	}
	if (valueRequired) {
		if (isStatic) {
			if ((binding.type == LongBinding) || (binding.type == DoubleBinding)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
		} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
			if ((binding.type == LongBinding) || (binding.type == DoubleBinding)) {
				codeStream.dup2_x1();
			} else {
				codeStream.dup_x1();
			}
		}
	}
	codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
	codeStream.sendOperator(postIncrement.operator, binding.type.id);
	codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
	fieldStore(codeStream, binding, syntheticWriteAccessor, false);
}
public static final Constant getConstantFor(
	FieldBinding binding, 
	boolean implicitReceiver, 
	Reference ref, 
	int indexInQualification) {
	//propagation of the constant.

	//ref can be a FieldReference, a SingleNameReference or a QualifiedNameReference
	//indexInQualification may have a value greater than zero only for QualifiednameReference
	//if ref==null then indexInQualification==0 AND implicitReceiver == false. This case is a 
	//degenerated case where a fake reference field (null) 
	//is associted to a real FieldBinding in order 
	//to allow its constant computation using the regular path (i.e. find the fieldDeclaration
	//and proceed to its type resolution). As implicitReceiver is false, no error reporting
	//against ref will be used ==> no nullPointerException risk .... 

	//special treatment for langage-built-in  field (their declaring class is null)
	if (binding.declaringClass == null) {
		//currently only one field "length" : the constant computation is never done
		return NotAConstant;
	}
	if (!binding.isFinal()) {
		return binding.constant = NotAConstant;
	}
	if (binding.constant != null) {
		if (indexInQualification == 0) {
			return binding.constant;
		}
		//see previous comment for the (sould-always-be) valid cast
		QualifiedNameReference qnr = (QualifiedNameReference) ref;
		if (indexInQualification == (qnr.indexOfFirstFieldBinding - 1)) {
			return binding.constant;
		}
		return NotAConstant;
	}
	//The field has not been yet type checked.
	//It also means that the field is not coming from a class that
	//has already been compiled. It can only be from a class within
	//compilation units to process. Thus the field is NOT from a BinaryTypeBinbing

	SourceTypeBinding tb = (SourceTypeBinding) binding.declaringClass;
	TypeDeclaration typeDecl = tb.scope.referenceContext;

	//fetch the field declaration
	FieldDeclaration fieldDecl = null;
	int index = 0;
	FieldDeclaration[] fields = typeDecl.fields;
	while (fieldDecl == null) {
		if ((fields[index].isField())
			&& (CharOperation.equals(fields[index].name, binding.name)))
			fieldDecl = fields[index];
		else
			index++;
	}
	//what scope to use (depend on the staticness of the field binding)
	MethodScope fieldScope = 
		binding.isStatic()
			? typeDecl.staticInitializerScope
			: typeDecl.initializerScope; 
	if (implicitReceiver) { //Determine if the ref is legal in the current class of the field
		//i.e. not a forward reference .... (they are allowed when the receiver is explicit ! ... Please don't ask me why !...yet another java mystery...)
		if (fieldScope.fieldDeclarationIndex == MethodScope.NotInFieldDecl) {
			// no field is currently being analysed in typeDecl
			fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
			return binding.constant;
		}
		//We are re-entering the same class fields analysing
		if (((ref == null) || ((ref.bits & DepthMASK) == 0)) // not implicit ref to enclosing field
			&& (binding.id > fieldScope.fieldDeclarationIndex)) {
			//forward reference. The declaration remains unresolved.
			tb.scope.problemReporter().forwardReference(ref, indexInQualification, tb);
			return NotAConstant;
		}
		fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
		return binding.constant;
	}
	//the field reference is explicity. It has to be a "simple" like field reference to get the
	//constant propagation. For example in Packahe.Type.field1.field2 , field1 may have its
	//constant having a propagation where field2 is always not propagating its
	if (indexInQualification == 0) {
		fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
		return binding.constant;
	}
	// Side-effect on the field binding may not be propagated out for the qualified reference
	// unless it occurs in first place of the name sequence
	fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
	//see previous comment for the cast that should always be valid
	QualifiedNameReference qnr = (QualifiedNameReference) ref;
	if (indexInQualification == (qnr.indexOfFirstFieldBinding - 1)) {
		return binding.constant;
	} else {
		return NotAConstant;
	}
}
public boolean isFieldReference() {

	return true ;
}
public boolean isSuperAccess() {

	return receiver.isSuper();
}
public boolean isTypeAccess() {	
	return receiver != null && receiver.isTypeReference();
}
/*
 * No need to emulate access to protected fields since not implicitly accessed
 */
public void manageSyntheticReadAccessIfNecessary(BlockScope currentScope){
	if (binding.isPrivate() 
		&& (currentScope.enclosingSourceType() != binding.declaringClass)
		&& (binding.constant == NotAConstant)) {
		syntheticReadAccessor = binding.getSyntheticReadAccess();
	}
}
/*
 * No need to emulate access to protected fields since not implicitly accessed
 */
public void manageSyntheticWriteAccessIfNecessary(BlockScope currentScope){
	if (binding.isPrivate() && (currentScope.enclosingSourceType() != binding.declaringClass)) {
		syntheticWriteAccessor = binding.getSyntheticWriteAccess();
	}
}
public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature type of the field.
	// constants are propaged when the field is final
	// and initialized with a (compile time) constant 

	// regular receiver reference 
	this.receiverType = receiver.resolveType(scope);
	if (this.receiverType == null){
		constant = NotAConstant;
		return null;
	}
	// the case receiverType.isArrayType and token = 'length' is handled by the scope API
	binding = scope.getField(this.receiverType, token, this);
	if (!binding.isValidBinding()) {
		constant = NotAConstant;
		scope.problemReporter().invalidField(this, this.receiverType);
		return null;
	}

	if (isFieldUseDeprecated(binding, scope))
		scope.problemReporter().deprecatedField(binding, this);

	// check for this.x in static is done in the resolution of the receiver
	constant = FieldReference.getConstantFor(binding, receiver == ThisReference.ThisImplicit, this, 0);
	if (!receiver.isThis())
		constant = NotAConstant;

	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	if (binding.declaringClass != this.receiverType
		&& binding.declaringClass != null // array.length
		&& binding.constant == NotAConstant
		&& !binding.declaringClass.canBeSeenBy(scope))
			binding = new FieldBinding(binding, (ReferenceBinding) this.receiverType);
	return binding.type;
}
public void setDepth(int d) {
}
public void setFieldIndex(int index){}
public String toStringExpression(){
	/* slow code */
	
	return 	receiver.toString()
			+ "."  //$NON-NLS-1$
			+ new String(token);}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		receiver.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
