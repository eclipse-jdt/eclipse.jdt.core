package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class MessageSend extends Expression implements InvocationSite {
	public Expression receiver;
	public char[] selector;
	public Expression[] arguments;
	public MethodBinding binding;

	public long nameSourcePosition; //(start<<32)+end

	MethodBinding syntheticAccessor;

	public MessageSend() {

	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		flowInfo =
			receiver
				.analyseCode(currentScope, flowContext, flowInfo, !binding.isStatic())
				.unconditionalInits();
		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				flowInfo =
					arguments[i]
						.analyseCode(currentScope, flowContext, flowInfo)
						.unconditionalInits();
			}
		}
		ReferenceBinding[] thrownExceptions;
		if ((thrownExceptions = binding.thrownExceptions) != NoExceptions) {
			// must verify that exceptions potentially thrown by this expression are caught in the method
			flowContext.checkExceptionHandlers(
				thrownExceptions,
				this,
				flowInfo,
				currentScope);
		}
		// if invoking through an enclosing instance, then must perform the field generation -- only if reachable
		manageEnclosingInstanceAccessIfNecessary(currentScope);
		manageSyntheticAccessIfNecessary(currentScope);
		return flowInfo;
	}

	/**
	 * MessageSend code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;

		// generate receiver/enclosing instance access
		boolean isStatic = binding.isStatic();
		// outer access ?
		if (!isStatic && ((bits & DepthMASK) != 0)) {
			// outer method can be reached through emulation
			Object[] path =
				currentScope.getExactEmulationPath(
					currentScope.enclosingSourceType().enclosingTypeAt(
						(bits & DepthMASK) >> DepthSHIFT));
			if (path == null) {
				// emulation was not possible (should not happen per construction)
				currentScope.problemReporter().needImplementation();
			} else {
				codeStream.generateOuterAccess(path, this, currentScope);
			}
		} else {
			receiver.generateCode(currentScope, codeStream, !isStatic);
		}
		// generate arguments
		if (arguments != null) {
			for (int i = 0, max = arguments.length; i < max; i++) {
				arguments[i].generateCode(currentScope, codeStream, true);
			}
		}
		// actual message invocation
		if (syntheticAccessor == null) {
			if (isStatic) {
				codeStream.invokestatic(binding);
			} else {
				if ((receiver.isSuper()) || binding.isPrivate()) {
					codeStream.invokespecial(binding);
				} else {
					if (binding.declaringClass.isInterface()) {
						codeStream.invokeinterface(binding);
					} else {
						codeStream.invokevirtual(binding);
					}
				}
			}
		} else {
			codeStream.invokestatic(syntheticAccessor);
		}
		// operation on the returned value
		if (valueRequired) {
			// implicit conversion if necessary
			codeStream.generateImplicitConversion(implicitConversion);
		} else {
			// pop return value if any
			switch (binding.returnType.id) {
				case T_long :
				case T_double :
					codeStream.pop2();
					break;
				case T_void :
					break;
				default :
					codeStream.pop();
			}
		}
		codeStream.recordPositionsFrom(pc, this);
	}

	public boolean isSuperAccess() {
		return receiver.isSuper();
	}

	public boolean isTypeAccess() {
		return receiver != null && receiver.isTypeReference();
	}

	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {
		if (((bits & DepthMASK) != 0)
			&& (!binding.isStatic())
			&& (receiver == ThisReference.ThisImplicit)) {
			ReferenceBinding compatibleType = currentScope.enclosingSourceType();
			// the declaringClass of the target binding must be compatible with the enclosing
			// type at <depth> levels outside
			for (int i = 0, depth = (bits & DepthMASK) >> DepthSHIFT; i < depth; i++) {
				compatibleType = compatibleType.enclosingType();
			}
			currentScope.emulateOuterAccess((SourceTypeBinding) compatibleType, false);
			// request cascade of accesses
		}
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {

		if (((bits & DepthMASK) != 0)
			|| currentScope.enclosingSourceType() != binding.declaringClass) {
			// implicit only have a depth set
			if (binding.isPrivate()) { // private access 
				syntheticAccessor =
					((SourceTypeBinding) binding.declaringClass).addSyntheticMethod(binding);
				currentScope.problemReporter().needToEmulateMethodAccess(binding, this);
				return;
			}
			if (receiver == ThisReference.ThisImplicit
				&& binding.isProtected()
				&& (bits & DepthMASK) != 0 // only if outer access			
				&& binding.declaringClass.getPackage()
					!= currentScope.enclosingSourceType().getPackage()) {
				// protected access (implicit access only)
				syntheticAccessor =
					(
						(SourceTypeBinding) currentScope.enclosingSourceType().enclosingTypeAt(
							(bits & DepthMASK) >> DepthSHIFT)).addSyntheticMethod(
						binding);
				currentScope.problemReporter().needToEmulateMethodAccess(binding, this);
			}
			if (receiver instanceof QualifiedSuperReference) { // qualified super
				SourceTypeBinding destinationType =
					(SourceTypeBinding) (((QualifiedSuperReference) receiver)
						.currentCompatibleType);
				syntheticAccessor = destinationType.addSyntheticMethod(binding);
				currentScope.problemReporter().needToEmulateMethodAccess(binding, this);
			}
		}
	}

	public TypeBinding resolveType(BlockScope scope) {
		// Answer the signature return type
		// Base type promotion

		constant = NotAConstant;
		TypeBinding receiverType = receiver.resolveType(scope);
		// will check for null after args are resolved
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			boolean argHasError = false; // typeChecks all arguments 
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++)
				if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null)
					argHasError = true;
			if (argHasError)
				return null;
		}
		if (receiverType == null)
			return null;

		// base type cannot receive any message
		if (receiverType.isBaseType()) {
			scope.problemReporter().errorNoMethodFor(this, receiverType, argumentTypes);
			return null;
		}

		binding =
			receiver == ThisReference.ThisImplicit
				? scope.getImplicitMethod(selector, argumentTypes, this)
				: scope.getMethod(receiverType, selector, argumentTypes, this);
		if (!binding.isValidBinding()) {
			if (binding.declaringClass == null) {
				if (receiverType instanceof ReferenceBinding) {
					binding.declaringClass = (ReferenceBinding) receiverType;
				} else { // really bad error ....
					scope.problemReporter().errorNoMethodFor(this, receiverType, argumentTypes);
					return null;
				}
			}
			scope.problemReporter().invalidMethod(this, binding);
			return null;
		}
		if (!binding.isStatic()) {
			// the "receiver" must not be a type, i.e. a NameReference that the TC has bound to a Type
			if (receiver instanceof NameReference) {
				if ((((NameReference) receiver).bits & BindingIds.TYPE) != 0) {
					scope.problemReporter().mustUseAStaticMethod(this, binding);
					return null;
				}
			}
		}
		if (arguments != null)
			for (int i = 0; i < arguments.length; i++)
				arguments[i].implicitWidening(binding.parameters[i], argumentTypes[i]);

		//-------message send that are known to fail at compile time-----------
		if (binding.isAbstract()) {
			if (receiver.isSuper()) {
				scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, binding);
				return null;
			}
			// abstract private methods cannot occur nor abstract static............
		}
		if (isMethodUseDeprecated(binding, scope))
			scope.problemReporter().deprecatedMethod(binding, this);
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		if (binding.declaringClass != receiverType
			&& !binding.declaringClass.canBeSeenBy(scope))
			binding = new MethodBinding(binding, (ReferenceBinding) receiverType);
		return binding.returnType;
	}

	public void setDepth(int depth) {
		if (depth > 0) {
			bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
		}
	}

	public void setFieldIndex(int depth) {
		// ignore for here
	}

	public String toStringExpression() {
		/*slow code*/

		String s = "";
		if (receiver != ThisReference.ThisImplicit)
			s = s + receiver.toStringExpression() + ".";
		s = s + new String(selector) + "(";
		if (arguments != null)
			for (int i = 0; i < arguments.length; i++) {
				s = s + arguments[i].toStringExpression();
				if (i != arguments.length - 1)
					s = s + " , ";
			};
		;
		s = s + ")";
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			receiver.traverse(visitor, blockScope);
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					arguments[i].traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}

}
