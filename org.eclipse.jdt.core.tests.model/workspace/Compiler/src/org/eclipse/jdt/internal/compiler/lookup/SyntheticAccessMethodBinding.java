/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;

public class SyntheticAccessMethodBinding extends MethodBinding {

	public FieldBinding targetReadField;		// read access to a field
	public FieldBinding targetWriteField;		// write access to a field
	public MethodBinding targetMethod;	// method or constructor
	
	public int accessType;

	public final static int FieldReadAccess = 1; 		// field read
	public final static int FieldWriteAccess = 2; 		// field write
	public final static int MethodAccess = 3; 		// normal method 
	public final static int ConstructorAccess = 4; 	// constructor
	public final static int SuperMethodAccess = 5; // super method
	public final static int BridgeMethodAccess = 6; // bridge method

	final static char[] AccessMethodPrefix = { 'a', 'c', 'c', 'e', 's', 's', '$' };

	public int sourceStart = 0; // start position of the matching declaration
	public int index; // used for sorting access methods in the class file
	
	public SyntheticAccessMethodBinding(FieldBinding targetField, boolean isReadAccess, ReferenceBinding declaringClass) {

		this.modifiers = AccDefault | AccStatic | AccSynthetic;
		SourceTypeBinding declaringSourceType = (SourceTypeBinding) declaringClass;
		SyntheticAccessMethodBinding[] knownAccessMethods = declaringSourceType.syntheticAccessMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
		this.selector = CharOperation.concat(AccessMethodPrefix, String.valueOf(methodId).toCharArray());
		if (isReadAccess) {
			this.returnType = targetField.type;
			if (targetField.isStatic()) {
				this.parameters = NoParameters;
			} else {
				this.parameters = new TypeBinding[1];
				this.parameters[0] = declaringSourceType;
			}
			this.targetReadField = targetField;
			this.accessType = FieldReadAccess;
		} else {
			this.returnType = VoidBinding;
			if (targetField.isStatic()) {
				this.parameters = new TypeBinding[1];
				this.parameters[0] = targetField.type;
			} else {
				this.parameters = new TypeBinding[2];
				this.parameters[0] = declaringSourceType;
				this.parameters[1] = targetField.type;
			}
			this.targetWriteField = targetField;
			this.accessType = FieldWriteAccess;
		}
		this.thrownExceptions = NoExceptions;
		this.declaringClass = declaringSourceType;
	
		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				MethodBinding[] methods = declaringSourceType.methods;
				for (int i = 0, length = methods.length; i < length; i++) {
					if (CharOperation.equals(this.selector, methods[i].selector) && this.areParametersEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
				// check for collision with synthetic accessors
				if (knownAccessMethods != null) {
					for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
						if (knownAccessMethods[i] == null) continue;
						if (CharOperation.equals(this.selector, knownAccessMethods[i].selector) && this.areParametersEqual(methods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a selector postfixed by a growing methodId
				this.setSelector(CharOperation.concat(AccessMethodPrefix, String.valueOf(++methodId).toCharArray()));
			}
		} while (needRename);
	
		// retrieve sourceStart position for the target field for line number attributes
		FieldDeclaration[] fieldDecls = declaringSourceType.scope.referenceContext.fields;
		if (fieldDecls != null) {
			for (int i = 0, max = fieldDecls.length; i < max; i++) {
				if (fieldDecls[i].binding == targetField) {
					this.sourceStart = fieldDecls[i].sourceStart;
					return;
				}
			}
		}
	
	/* did not find the target field declaration - it is a synthetic one
		public class A {
			public class B {
				public class C {
					void foo() {
						System.out.println("A.this = " + A.this);
					}
				}
			}
			public static void main(String args[]) {
				new A().new B().new C().foo();
			}
		}	
	*/
		// We now at this point - per construction - it is for sure an enclosing instance, we are going to
		// show the target field type declaration location.
		this.sourceStart = declaringSourceType.scope.referenceContext.sourceStart; // use the target declaring class name position instead
	}

	public SyntheticAccessMethodBinding(MethodBinding targetMethod, boolean isSuperAccess, ReferenceBinding receiverType) {
	
		if (targetMethod.isConstructor()) {
			this.initializeConstructorAccessor(targetMethod);
		} else {
			this.initializeMethodAccessor(targetMethod, isSuperAccess, receiverType);
		}
	}

	/*
	 * Construct a bridge method
	 */
	public SyntheticAccessMethodBinding(MethodBinding overridenMethodToBridge, MethodBinding localTargetMethod) {
	    this.declaringClass = localTargetMethod.declaringClass;
	    this.selector = overridenMethodToBridge.selector;
	    this.modifiers = overridenMethodToBridge.modifiers | AccBridge | AccSynthetic;
	    this.modifiers &= ~(AccAbstract | AccNative);
	    this.returnType = overridenMethodToBridge.returnType;
	    this.parameters = overridenMethodToBridge.parameters;
	    this.thrownExceptions = overridenMethodToBridge.thrownExceptions;
	    this.targetMethod = localTargetMethod;
	    this.accessType = BridgeMethodAccess;
		SyntheticAccessMethodBinding[] knownAccessMethods = ((SourceTypeBinding)this.declaringClass).syntheticAccessMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;	    
	}
	
	/**
	 * An constructor accessor is a constructor with an extra argument (declaringClass), in case of
	 * collision with an existing constructor, then add again an extra argument (declaringClass again).
	 */
	 public void initializeConstructorAccessor(MethodBinding accessedConstructor) {
	
		this.targetMethod = accessedConstructor;
		this.modifiers = AccDefault | AccSynthetic;
		SourceTypeBinding sourceType = (SourceTypeBinding) accessedConstructor.declaringClass; 
		SyntheticAccessMethodBinding[] knownAccessMethods = 
			sourceType.syntheticAccessMethods(); 
		this.index = knownAccessMethods == null ? 0 : knownAccessMethods.length;
	
		this.selector = accessedConstructor.selector;
		this.returnType = accessedConstructor.returnType;
		this.accessType = ConstructorAccess;
		this.parameters = new TypeBinding[accessedConstructor.parameters.length + 1];
		System.arraycopy(
			accessedConstructor.parameters, 
			0, 
			this.parameters, 
			0, 
			accessedConstructor.parameters.length); 
		parameters[accessedConstructor.parameters.length] = 
			accessedConstructor.declaringClass; 
		this.thrownExceptions = accessedConstructor.thrownExceptions;
		this.declaringClass = sourceType;
	
		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				MethodBinding[] methods = sourceType.methods;
				for (int i = 0, length = methods.length; i < length; i++) {
					if (CharOperation.equals(this.selector, methods[i].selector)
						&& this.areParametersEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
				// check for collision with synthetic accessors
				if (knownAccessMethods != null) {
					for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
						if (knownAccessMethods[i] == null)
							continue;
						if (CharOperation.equals(this.selector, knownAccessMethods[i].selector)
							&& this.areParametersEqual(knownAccessMethods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a new extra argument
				int length = this.parameters.length;
				System.arraycopy(
					this.parameters, 
					0, 
					this.parameters = new TypeBinding[length + 1], 
					0, 
					length); 
				this.parameters[length] = this.declaringClass;
			}
		} while (needRename);
	
		// retrieve sourceStart position for the target method for line number attributes
		AbstractMethodDeclaration[] methodDecls = 
			sourceType.scope.referenceContext.methods; 
		if (methodDecls != null) {
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				if (methodDecls[i].binding == accessedConstructor) {
					this.sourceStart = methodDecls[i].sourceStart;
					return;
				}
			}
		}
	}

	/**
	 * An method accessor is a method with an access$N selector, where N is incremented in case of collisions.
	 */
	public void initializeMethodAccessor(MethodBinding accessedMethod, boolean isSuperAccess, ReferenceBinding receiverType) {
		
		this.targetMethod = accessedMethod;
		this.modifiers = AccDefault | AccStatic | AccSynthetic;
		SourceTypeBinding declaringSourceType = (SourceTypeBinding) receiverType;
		SyntheticAccessMethodBinding[] knownAccessMethods = declaringSourceType.syntheticAccessMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
	
		this.selector = CharOperation.concat(AccessMethodPrefix, String.valueOf(methodId).toCharArray());
		this.returnType = accessedMethod.returnType;
		this.accessType = isSuperAccess ? SuperMethodAccess : MethodAccess;
		
		if (accessedMethod.isStatic()) {
			this.parameters = accessedMethod.parameters;
		} else {
			this.parameters = new TypeBinding[accessedMethod.parameters.length + 1];
			this.parameters[0] = declaringSourceType;
			System.arraycopy(accessedMethod.parameters, 0, this.parameters, 1, accessedMethod.parameters.length);
		}
		this.thrownExceptions = accessedMethod.thrownExceptions;
		this.declaringClass = declaringSourceType;
	
		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				MethodBinding[] methods = declaringSourceType.methods;
				for (int i = 0, length = methods.length; i < length; i++) {
					if (CharOperation.equals(this.selector, methods[i].selector) && this.areParametersEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
				// check for collision with synthetic accessors
				if (knownAccessMethods != null) {
					for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
						if (knownAccessMethods[i] == null) continue;
						if (CharOperation.equals(this.selector, knownAccessMethods[i].selector) && this.areParametersEqual(knownAccessMethods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a selector & a growing methodId
				this.setSelector(CharOperation.concat(AccessMethodPrefix, String.valueOf(++methodId).toCharArray()));
			}
		} while (needRename);
	
		// retrieve sourceStart position for the target method for line number attributes
		AbstractMethodDeclaration[] methodDecls = declaringSourceType.scope.referenceContext.methods;
		if (methodDecls != null) {
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				if (methodDecls[i].binding == accessedMethod) {
					this.sourceStart = methodDecls[i].sourceStart;
					return;
				}
			}
		}
	}

	protected boolean isConstructorRelated() {
		return accessType == ConstructorAccess;
	}
}
