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
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class ClassScope extends Scope {
	public TypeDeclaration referenceContext;
	private TypeReference superTypeReference;

	private final static char[] IncompleteHierarchy = new char[] {'h', 'a', 's', ' ', 'i', 'n', 'c', 'o', 'n', 's', 'i', 's', 't', 'e', 'n', 't', ' ', 'h', 'i', 'e', 'r', 'a', 'r', 'c', 'h', 'y'};

	public ClassScope(Scope parent, TypeDeclaration context) {
		super(CLASS_SCOPE, parent);
		this.referenceContext = context;
	}
	
	void buildAnonymousTypeBinding(SourceTypeBinding enclosingType, ReferenceBinding supertype) {
		
		LocalTypeBinding anonymousType = buildLocalType(enclosingType, enclosingType.fPackage);

		SourceTypeBinding sourceType = referenceContext.binding;
		if (supertype.isInterface()) {
			sourceType.superclass = getJavaLangObject();
			sourceType.superInterfaces = new ReferenceBinding[] { supertype };
		} else {
			sourceType.superclass = supertype;
			sourceType.superInterfaces = TypeConstants.NoSuperInterfaces;
		}
		connectMemberTypes();
		buildFieldsAndMethods();
		anonymousType.faultInTypesForFieldsAndMethods();
		sourceType.verifyMethods(environment().methodVerifier());
	}
	
	private void buildFields() {
		boolean hierarchyIsInconsistent = referenceContext.binding.isHierarchyInconsistent();
		if (referenceContext.fields == null) {
			if (hierarchyIsInconsistent) { // 72468
				referenceContext.binding.fields = new FieldBinding[1];
				referenceContext.binding.fields[0] =
					new FieldBinding(IncompleteHierarchy, VoidBinding, AccPrivate, referenceContext.binding, null);
			} else {
				referenceContext.binding.fields = NoFields;
			}
			return;
		}
		// count the number of fields vs. initializers
		FieldDeclaration[] fields = referenceContext.fields;
		int size = fields.length;
		int count = 0;
		for (int i = 0; i < size; i++)
			if (fields[i].isField())
				count++;

		if (hierarchyIsInconsistent)
			count++;
		// iterate the field declarations to create the bindings, lose all duplicates
		FieldBinding[] fieldBindings = new FieldBinding[count];
		HashtableOfObject knownFieldNames = new HashtableOfObject(count);
		boolean duplicate = false;
		count = 0;
		for (int i = 0; i < size; i++) {
			FieldDeclaration field = fields[i];
			if (!field.isField()) {
				if (referenceContext.binding.isInterface())
					problemReporter().interfaceCannotHaveInitializers(referenceContext.binding, field);
			} else {
				FieldBinding fieldBinding = new FieldBinding(field, null, field.modifiers | AccUnresolved, referenceContext.binding);
				// field's type will be resolved when needed for top level types
				checkAndSetModifiersForField(fieldBinding, field);

				if (knownFieldNames.containsKey(field.name)) {
					duplicate = true;
					FieldBinding previousBinding = (FieldBinding) knownFieldNames.get(field.name);
					if (previousBinding != null) {
						for (int f = 0; f < i; f++) {
							FieldDeclaration previousField = fields[f];
							if (previousField.binding == previousBinding) {
								problemReporter().duplicateFieldInType(referenceContext.binding, previousField);
								previousField.binding = null;
								break;
							}
						}
					}
					knownFieldNames.put(field.name, null); // ensure that the duplicate field is found & removed
					problemReporter().duplicateFieldInType(referenceContext.binding, field);
					field.binding = null;
				} else {
					knownFieldNames.put(field.name, fieldBinding);
					// remember that we have seen a field with this name
					if (fieldBinding != null)
						fieldBindings[count++] = fieldBinding;
				}
			}
		}
		// remove duplicate fields
		if (duplicate) {
			FieldBinding[] newFieldBindings = new FieldBinding[knownFieldNames.size() - 1];
			// we know we'll be removing at least 1 duplicate name
			size = count;
			count = 0;
			for (int i = 0; i < size; i++) {
				FieldBinding fieldBinding = fieldBindings[i];
				if (knownFieldNames.get(fieldBinding.name) != null)
					newFieldBindings[count++] = fieldBinding;
			}
			fieldBindings = newFieldBindings;
		}
		if (hierarchyIsInconsistent)
			fieldBindings[count++] = new FieldBinding(IncompleteHierarchy, VoidBinding, AccPrivate, referenceContext.binding, null);

		if (count != fieldBindings.length)
			System.arraycopy(fieldBindings, 0, fieldBindings = new FieldBinding[count], 0, count);
		for (int i = 0; i < count; i++)
			fieldBindings[i].id = i;
		referenceContext.binding.fields = fieldBindings;
	}
	
	void buildFieldsAndMethods() {
		buildFields();
		buildMethods();

		SourceTypeBinding sourceType = referenceContext.binding;
		if (sourceType.isMemberType() && !sourceType.isLocalType())
			 ((MemberTypeBinding) sourceType).checkSyntheticArgsAndFields();

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++)
			 ((SourceTypeBinding) memberTypes[i]).scope.buildFieldsAndMethods();
	}
	
	private LocalTypeBinding buildLocalType(
		SourceTypeBinding enclosingType,
		PackageBinding packageBinding) {
	    
		referenceContext.scope = this;
		referenceContext.staticInitializerScope = new MethodScope(this, referenceContext, true);
		referenceContext.initializerScope = new MethodScope(this, referenceContext, false);

		// build the binding or the local type
		LocalTypeBinding localType = new LocalTypeBinding(this, enclosingType, this.switchCase());
		referenceContext.binding = localType;
		checkAndSetModifiers();
		buildTypeVariables();
		
		// Look at member types
		ReferenceBinding[] memberTypeBindings = NoMemberTypes;
		if (referenceContext.memberTypes != null) {
			int size = referenceContext.memberTypes.length;
			memberTypeBindings = new ReferenceBinding[size];
			int count = 0;
			nextMember : for (int i = 0; i < size; i++) {
				TypeDeclaration memberContext = referenceContext.memberTypes[i];
				if (memberContext.isInterface()) {
					problemReporter().nestedClassCannotDeclareInterface(memberContext);
					continue nextMember;
				}
				ReferenceBinding type = localType;
				// check that the member does not conflict with an enclosing type
				do {
					if (CharOperation.equals(type.sourceName, memberContext.name)) {
						problemReporter().hidingEnclosingType(memberContext);
						continue nextMember;
					}
					type = type.enclosingType();
				} while (type != null);
				// check the member type does not conflict with another sibling member type
				for (int j = 0; j < i; j++) {
					if (CharOperation.equals(referenceContext.memberTypes[j].name, memberContext.name)) {
						problemReporter().duplicateNestedType(memberContext);
						continue nextMember;
					}
				}
				ClassScope memberScope = new ClassScope(this, referenceContext.memberTypes[i]);
				LocalTypeBinding memberBinding = memberScope.buildLocalType(localType, packageBinding);
				memberBinding.setAsMemberType();
				memberTypeBindings[count++] = memberBinding;
			}
			if (count != size)
				System.arraycopy(memberTypeBindings, 0, memberTypeBindings = new ReferenceBinding[count], 0, count);
		}
		localType.memberTypes = memberTypeBindings;
		return localType;
	}
	
	void buildLocalTypeBinding(SourceTypeBinding enclosingType) {

		LocalTypeBinding localType = buildLocalType(enclosingType, enclosingType.fPackage);
		connectTypeHierarchy();
		buildFieldsAndMethods();
		localType.faultInTypesForFieldsAndMethods();

		referenceContext.binding.verifyMethods(environment().methodVerifier());
	}
	
	private void buildMemberTypes() {
	    SourceTypeBinding sourceType = referenceContext.binding;
		ReferenceBinding[] memberTypeBindings = NoMemberTypes;
		if (referenceContext.memberTypes != null) {
			int length = referenceContext.memberTypes.length;
			memberTypeBindings = new ReferenceBinding[length];
			int count = 0;
			nextMember : for (int i = 0; i < length; i++) {
				TypeDeclaration memberContext = referenceContext.memberTypes[i];
				if (memberContext.isInterface()
					&& sourceType.isNestedType()
					&& sourceType.isClass()
					&& !sourceType.isStatic()) {
					problemReporter().nestedClassCannotDeclareInterface(memberContext);
					continue nextMember;
				}
				ReferenceBinding type = sourceType;
				// check that the member does not conflict with an enclosing type
				do {
					if (CharOperation.equals(type.sourceName, memberContext.name)) {
						problemReporter().hidingEnclosingType(memberContext);
						continue nextMember;
					}
					type = type.enclosingType();
				} while (type != null);
				// check that the member type does not conflict with another sibling member type
				for (int j = 0; j < i; j++) {
					if (CharOperation.equals(referenceContext.memberTypes[j].name, memberContext.name)) {
						problemReporter().duplicateNestedType(memberContext);
						continue nextMember;
					}
				}

				ClassScope memberScope = new ClassScope(this, memberContext);
				memberTypeBindings[count++] = memberScope.buildType(sourceType, sourceType.fPackage);
			}
			if (count != length)
				System.arraycopy(memberTypeBindings, 0, memberTypeBindings = new ReferenceBinding[count], 0, count);
		}
		sourceType.memberTypes = memberTypeBindings;
	}
	
	private void buildMethods() {
		if (referenceContext.methods == null) {
			referenceContext.binding.methods = NoMethods;
			return;
		}

		// iterate the method declarations to create the bindings
		AbstractMethodDeclaration[] methods = referenceContext.methods;
		int size = methods.length;
		int clinitIndex = -1;
		for (int i = 0; i < size; i++) {
			if (methods[i] instanceof Clinit) {
				clinitIndex = i;
				break;
			}
		}
		MethodBinding[] methodBindings = new MethodBinding[clinitIndex == -1 ? size : size - 1];

		int count = 0;
		for (int i = 0; i < size; i++) {
			if (i != clinitIndex) {
				MethodScope scope = new MethodScope(this, methods[i], false);
				MethodBinding methodBinding = scope.createMethod(methods[i]);
				if (methodBinding != null) // is null if binding could not be created
					methodBindings[count++] = methodBinding;
			}
		}
		if (count != methodBindings.length)
			System.arraycopy(methodBindings, 0, methodBindings = new MethodBinding[count], 0, count);

		referenceContext.binding.methods = methodBindings;
		referenceContext.binding.modifiers |= AccUnresolved; // until methods() is sent
	}
	
	SourceTypeBinding buildType(SourceTypeBinding enclosingType, PackageBinding packageBinding) {
		// provide the typeDeclaration with needed scopes
		referenceContext.scope = this;
		referenceContext.staticInitializerScope = new MethodScope(this, referenceContext, true);
		referenceContext.initializerScope = new MethodScope(this, referenceContext, false);

		if (enclosingType == null) {
			char[][] className = CharOperation.arrayConcat(packageBinding.compoundName, referenceContext.name);
			referenceContext.binding = new SourceTypeBinding(className, packageBinding, this);
		} else {
			char[][] className = CharOperation.deepCopy(enclosingType.compoundName);
			className[className.length - 1] =
				CharOperation.concat(className[className.length - 1], referenceContext.name, '$');
			referenceContext.binding = new MemberTypeBinding(className, this, enclosingType);
		}

		SourceTypeBinding sourceType = referenceContext.binding;
		sourceType.fPackage.addType(sourceType);
		checkAndSetModifiers();
		buildTypeVariables();
		buildMemberTypes();
		return sourceType;
	}
	
	private void buildTypeVariables() {
	    
	    SourceTypeBinding sourceType = referenceContext.binding;
		TypeParameter[] typeParameters = referenceContext.typeParameters;
		
	    // do not construct type variables if source < 1.5
		if (typeParameters == null || environment().options.sourceLevel < ClassFileConstants.JDK1_5) {
		    sourceType.typeVariables = NoTypeVariables;
		    return;
		}
		sourceType.typeVariables = NoTypeVariables; // safety

		if (sourceType.id == T_Object) { // handle the case of redefining java.lang.Object up front
			problemReporter().objectCannotBeGeneric(referenceContext);
			return; 
		}
		sourceType.typeVariables = createTypeVariables(typeParameters, sourceType);
		sourceType.modifiers |= AccGenericSignature;
	}
	
	private void checkAndSetModifiers() {
		SourceTypeBinding sourceType = referenceContext.binding;
		int modifiers = sourceType.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0)
			problemReporter().duplicateModifierForType(sourceType);

		ReferenceBinding enclosingType = sourceType.enclosingType();
		boolean isMemberType = sourceType.isMemberType();
		
		if (isMemberType) {
			// checks for member types before local types to catch local members
			if (enclosingType.isStrictfp())
				modifiers |= AccStrictfp;
			if (enclosingType.isViewedAsDeprecated() && !sourceType.isDeprecated())
				modifiers |= AccDeprecatedImplicitly;
			if (enclosingType.isInterface())
				modifiers |= AccPublic;
		} else if (sourceType.isLocalType()) {
			if (sourceType.isAnonymousType()) 
			    modifiers |= AccFinal;
			Scope scope = this;
			do {
				switch (scope.kind) {
					case METHOD_SCOPE :
						MethodScope methodScope = (MethodScope) scope;
						if (methodScope.isInsideInitializer()) {
							SourceTypeBinding type = ((TypeDeclaration) methodScope.referenceContext).binding;
			
							// inside field declaration ? check field modifier to see if deprecated
							if (methodScope.initializedField != null) {
									// currently inside this field initialization
								if (methodScope.initializedField.isViewedAsDeprecated() && !sourceType.isDeprecated()){
									modifiers |= AccDeprecatedImplicitly;
								}
							} else {
								if (type.isStrictfp())
									modifiers |= AccStrictfp;
								if (type.isViewedAsDeprecated() && !sourceType.isDeprecated()) 
									modifiers |= AccDeprecatedImplicitly;
							}					
						} else {
							MethodBinding method = ((AbstractMethodDeclaration) methodScope.referenceContext).binding;
							if (method != null){
								if (method.isStrictfp())
									modifiers |= AccStrictfp;
								if (method.isViewedAsDeprecated() && !sourceType.isDeprecated())
									modifiers |= AccDeprecatedImplicitly;
							}
						}
						break;
					case CLASS_SCOPE :
						// local member
						if (enclosingType.isStrictfp())
							modifiers |= AccStrictfp;
						if (enclosingType.isViewedAsDeprecated() && !sourceType.isDeprecated())
							modifiers |= AccDeprecatedImplicitly;
						break;
				}
				scope = scope.parent;
			} while (scope != null);
		}
		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & AccJustFlag;

		if ((realModifiers & AccInterface) != 0) {
			// detect abnormal cases for interfaces
			if (isMemberType) {
				int unexpectedModifiers =
					~(AccPublic | AccPrivate | AccProtected | AccStatic | AccAbstract | AccInterface | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForMemberInterface(sourceType);
				/*
				} else if (sourceType.isLocalType()) { //interfaces cannot be defined inside a method
					int unexpectedModifiers = ~(AccAbstract | AccInterface | AccStrictfp);
					if ((realModifiers & unexpectedModifiers) != 0)
						problemReporter().illegalModifierForLocalInterface(sourceType);
				*/
			} else {
				int unexpectedModifiers = ~(AccPublic | AccAbstract | AccInterface | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForInterface(sourceType);
			}
			modifiers |= AccAbstract;
		} else {
			// detect abnormal cases for types
			if (isMemberType) { // includes member types defined inside local types
				int unexpectedModifiers =
					~(AccPublic | AccPrivate | AccProtected | AccStatic | AccAbstract | AccFinal | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForMemberClass(sourceType);
			} else if (sourceType.isLocalType()) {
				int unexpectedModifiers = ~(AccAbstract | AccFinal | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForLocalClass(sourceType);
			} else {
				int unexpectedModifiers = ~(AccPublic | AccAbstract | AccFinal | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForClass(sourceType);
			}

			// check that Final and Abstract are not set together
			if ((realModifiers & (AccFinal | AccAbstract)) == (AccFinal | AccAbstract))
				problemReporter().illegalModifierCombinationFinalAbstractForClass(sourceType);
		}

		if (isMemberType) {
			// test visibility modifiers inconsistency, isolate the accessors bits
			if (enclosingType.isInterface()) {
				if ((realModifiers & (AccProtected | AccPrivate)) != 0) {
					problemReporter().illegalVisibilityModifierForInterfaceMemberType(sourceType);

					// need to keep the less restrictive
					if ((realModifiers & AccProtected) != 0)
						modifiers ^= AccProtected;
					if ((realModifiers & AccPrivate) != 0)
						modifiers ^= AccPrivate;
				}
			} else {
				int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
				if ((accessorBits & (accessorBits - 1)) > 1) {
					problemReporter().illegalVisibilityModifierCombinationForMemberType(sourceType);

					// need to keep the less restrictive
					if ((accessorBits & AccPublic) != 0) {
						if ((accessorBits & AccProtected) != 0)
							modifiers ^= AccProtected;
						if ((accessorBits & AccPrivate) != 0)
							modifiers ^= AccPrivate;
					}
					if ((accessorBits & AccProtected) != 0)
						if ((accessorBits & AccPrivate) != 0)
							modifiers ^= AccPrivate;
				}
			}

			// static modifier test
			if ((realModifiers & AccStatic) == 0) {
				if (enclosingType.isInterface())
					modifiers |= AccStatic;
			} else {
				if (!enclosingType.isStatic())
					// error the enclosing type of a static field must be static or a top-level type
					problemReporter().illegalStaticModifierForMemberType(sourceType);
			}
		}

		sourceType.modifiers = modifiers;
	}
	
	/* This method checks the modifiers of a field.
	*
	* 9.3 & 8.3
	* Need to integrate the check for the final modifiers for nested types
	*
	* Note : A scope is accessible by : fieldBinding.declaringClass.scope
	*/
	private void checkAndSetModifiersForField(FieldBinding fieldBinding, FieldDeclaration fieldDecl) {
		int modifiers = fieldBinding.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0)
			problemReporter().duplicateModifierForField(fieldBinding.declaringClass, fieldDecl);

		if (fieldBinding.declaringClass.isInterface()) {
			int expectedValue = AccPublic | AccStatic | AccFinal;
			// set the modifiers
			modifiers |= expectedValue;

			// and then check that they are the only ones
			if ((modifiers & AccJustFlag) != expectedValue)
				problemReporter().illegalModifierForInterfaceField(fieldBinding.declaringClass, fieldDecl);
			fieldBinding.modifiers = modifiers;
			return;
		}

		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & AccJustFlag;
		int unexpectedModifiers =
			~(AccPublic | AccPrivate | AccProtected | AccFinal | AccStatic | AccTransient | AccVolatile);
		if ((realModifiers & unexpectedModifiers) != 0)
			problemReporter().illegalModifierForField(fieldBinding.declaringClass, fieldDecl);

		int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
		if ((accessorBits & (accessorBits - 1)) > 1) {
			problemReporter().illegalVisibilityModifierCombinationForField(
				fieldBinding.declaringClass,
				fieldDecl);

			// need to keep the less restrictive
			if ((accessorBits & AccPublic) != 0) {
				if ((accessorBits & AccProtected) != 0)
					modifiers ^= AccProtected;
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
			}
			if ((accessorBits & AccProtected) != 0)
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
		}

		if ((realModifiers & (AccFinal | AccVolatile)) == (AccFinal | AccVolatile))
			problemReporter().illegalModifierCombinationFinalVolatileForField(
				fieldBinding.declaringClass,
				fieldDecl);

		if (fieldDecl.initialization == null && (modifiers & AccFinal) != 0) {
			modifiers |= AccBlankFinal;
		}
		fieldBinding.modifiers = modifiers;
	}

	private void checkForInheritedMemberTypes(SourceTypeBinding sourceType) {
		// search up the hierarchy of the sourceType to see if any superType defines a member type
		// when no member types are defined, tag the sourceType & each superType with the HasNoMemberTypes bit
		// assumes super types have already been checked & tagged
		ReferenceBinding currentType = sourceType;
		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;
		do {
			if (currentType.hasMemberTypes()) // avoid resolving member types eagerly
				return;

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (interfacesToVisit == null)
					interfacesToVisit = new ReferenceBinding[5][];
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		} while ((currentType = currentType.superclass()) != null && (currentType.tagBits & HasNoMemberTypes) == 0);

		if (interfacesToVisit != null) {
			// contains the interfaces between the sourceType and any superclass, which was tagged as having no member types
			boolean needToTag = false;
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & HasNoMemberTypes) == 0) { // skip interface if it already knows it has no member types
						if (anInterface.hasMemberTypes()) // avoid resolving member types eagerly
							return;

						needToTag = true;
						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {
							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			if (needToTag) {
				for (int i = 0; i <= lastPosition; i++) {
					ReferenceBinding[] interfaces = interfacesToVisit[i];
					for (int j = 0, length = interfaces.length; j < length; j++)
						interfaces[j].tagBits |= HasNoMemberTypes;
				}
			}
		}

		// tag the sourceType and all of its superclasses, unless they have already been tagged
		currentType = sourceType;
		do {
			currentType.tagBits |= HasNoMemberTypes;
		} while ((currentType = currentType.superclass()) != null && (currentType.tagBits & HasNoMemberTypes) == 0);
	}
	// Perform deferred bound checks for parameterized type references (only done after hierarchy is connected)
	private void  checkParameterizedTypeBounds() {
		TypeReference superclass = referenceContext.superclass;
		if (superclass != null) {
			superclass.checkBounds(this);
		}
		TypeReference[] superinterfaces = referenceContext.superInterfaces;
		if (superinterfaces != null) {
			for (int i = 0, length = superinterfaces.length; i < length; i++) {
				superinterfaces[i].checkBounds(this);
			}
		}
		TypeParameter[] typeParameters = referenceContext.typeParameters;
		if (typeParameters != null) {
			for (int i = 0, paramLength = typeParameters.length; i < paramLength; i++) {
				TypeParameter typeParameter = typeParameters[i];
				TypeReference typeRef = typeParameter.type;
				if (typeRef != null) {
					typeRef.checkBounds(this);

					TypeReference[] boundRefs = typeParameter.bounds;
					if (boundRefs != null)
						for (int j = 0, k = boundRefs.length; j < k; j++)
							boundRefs[j].checkBounds(this);
				}
			}
		}
	}

	private void connectMemberTypes() {
		SourceTypeBinding sourceType = referenceContext.binding;
		if (sourceType.memberTypes != NoMemberTypes)
			for (int i = 0, size = sourceType.memberTypes.length; i < size; i++)
				 ((SourceTypeBinding) sourceType.memberTypes[i]).scope.connectTypeHierarchy();
	}
	/*
		Our current belief based on available JCK tests is:
			inherited member types are visible as a potential superclass.
			inherited interfaces are not visible when defining a superinterface.
	
		Error recovery story:
			ensure the superclass is set to java.lang.Object if a problem is detected
			resolving the superclass.
	
		Answer false if an error was reported against the sourceType.
	*/
	private boolean connectSuperclass() {
		SourceTypeBinding sourceType = referenceContext.binding;
		if (sourceType.id == T_Object) { // handle the case of redefining java.lang.Object up front
			sourceType.superclass = null;
			sourceType.superInterfaces = NoSuperInterfaces;
			if (referenceContext.superclass != null || referenceContext.superInterfaces != null)
				problemReporter().objectCannotHaveSuperTypes(sourceType);
			return true; // do not propagate Object's hierarchy problems down to every subtype
		}
		if (referenceContext.superclass == null) {
			sourceType.superclass = getJavaLangObject();
			return !detectCycle(sourceType, sourceType.superclass, null);
		}
		TypeReference superclassRef = referenceContext.superclass;
		ReferenceBinding superclass = findSupertype(superclassRef);
		if (superclass != null) { // is null if a cycle was detected cycle or a problem
			if (superclass.isInterface()) {
				problemReporter().superclassMustBeAClass(sourceType, superclassRef, superclass);
			} else if (superclass.isFinal()) {
				problemReporter().classExtendFinalClass(sourceType, superclassRef, superclass);
			} else if ((superclass.tagBits & TagBits.HasWildcard) != 0) {
				problemReporter().superTypeCannotUseWildcard(sourceType, superclassRef, superclass);
			} else {
				// only want to reach here when no errors are reported
				sourceType.superclass = superclass;
				return true;
			}
		}
		sourceType.tagBits |= HierarchyHasProblems;
		sourceType.superclass = getJavaLangObject();
		if ((sourceType.superclass.tagBits & BeginHierarchyCheck) == 0)
			detectCycle(sourceType, sourceType.superclass, null);
		return false; // reported some error against the source type
	}

	/*
		Our current belief based on available JCK 1.3 tests is:
			inherited member types are visible as a potential superclass.
			inherited interfaces are visible when defining a superinterface.
	
		Error recovery story:
			ensure the superinterfaces contain only valid visible interfaces.
	
		Answer false if an error was reported against the sourceType.
	*/
	private boolean connectSuperInterfaces() {
		SourceTypeBinding sourceType = referenceContext.binding;
		sourceType.superInterfaces = NoSuperInterfaces;
		if (referenceContext.superInterfaces == null)
			return true;
		if (sourceType.id == T_Object) // already handled the case of redefining java.lang.Object
			return true;

		boolean noProblems = true;
		int length = referenceContext.superInterfaces.length;
		ReferenceBinding[] interfaceBindings = new ReferenceBinding[length];
		int count = 0;
		nextInterface : for (int i = 0; i < length; i++) {
		    TypeReference superInterfaceRef = referenceContext.superInterfaces[i];
			ReferenceBinding superInterface = findSupertype(superInterfaceRef);
			if (superInterface == null) { // detected cycle
				sourceType.tagBits |= HierarchyHasProblems;
				noProblems = false;
				continue nextInterface;
			}
			superInterfaceRef.resolvedType = superInterface; // hold onto the problem type
			// Check for a duplicate interface once the name is resolved, otherwise we may be confused (ie : a.b.I and c.d.I)
			for (int k = 0; k < count; k++) {
				if (interfaceBindings[k] == superInterface) {
					// should this be treated as a warning?
					problemReporter().duplicateSuperinterface(sourceType, referenceContext, superInterface);
					continue nextInterface;
				}
			}
			if (superInterface.isClass()) {
				problemReporter().superinterfaceMustBeAnInterface(sourceType, superInterfaceRef, superInterface);
				sourceType.tagBits |= HierarchyHasProblems;
				noProblems = false;
				continue nextInterface;
			}
			if ((superInterface.tagBits & TagBits.HasWildcard) != 0) {
				problemReporter().superTypeCannotUseWildcard(sourceType, superInterfaceRef, superInterface);
				sourceType.tagBits |= HierarchyHasProblems;
				noProblems = false;
				continue nextInterface;
			}
			ReferenceBinding invalid = findAmbiguousInterface(superInterface, sourceType);
			if (invalid != null) {
				ReferenceBinding generic = null;
				if (superInterface.isParameterizedType())
					generic = ((ParameterizedTypeBinding) superInterface).type;
				else if (invalid.isParameterizedType())
					generic = ((ParameterizedTypeBinding) invalid).type;
				problemReporter().superinterfacesCollide(generic, referenceContext, superInterface, invalid);
				sourceType.tagBits |= HierarchyHasProblems;
				noProblems = false;
				continue nextInterface;
			}

			// only want to reach here when no errors are reported
			interfaceBindings[count++] = superInterface;
		}
		// hold onto all correctly resolved superinterfaces
		if (count > 0) {
			if (count != length)
				System.arraycopy(interfaceBindings, 0, interfaceBindings = new ReferenceBinding[count], 0, count);
			sourceType.superInterfaces = interfaceBindings;
		}
		return noProblems;
	}
	
	void connectTypeHierarchy() {
		SourceTypeBinding sourceType = referenceContext.binding;
		if ((sourceType.tagBits & BeginHierarchyCheck) == 0) {
			sourceType.tagBits |= BeginHierarchyCheck;
			boolean noProblems = connectTypeVariables(referenceContext.typeParameters);
			noProblems &= connectSuperclass();
			noProblems &= connectSuperInterfaces();
			sourceType.tagBits |= EndHierarchyCheck;
			if (noProblems && sourceType.isHierarchyInconsistent())
				problemReporter().hierarchyHasProblems(sourceType);
		}
		// Perform deferred bound checks for parameterized type references (only done after hierarchy is connected)
		checkParameterizedTypeBounds();
		connectMemberTypes();
		try {
			checkForInheritedMemberTypes(sourceType);
		} catch (AbortCompilation e) {
			e.updateContext(referenceContext, referenceCompilationUnit().compilationResult);
			throw e;
		}
	}
	
	private void connectTypeHierarchyWithoutMembers() {
		// must ensure the imports are resolved
		if (parent instanceof CompilationUnitScope) {
			if (((CompilationUnitScope) parent).imports == null)
				 ((CompilationUnitScope) parent).checkAndSetImports();
		} else if (parent instanceof ClassScope) {
			// ensure that the enclosing type has already been checked
			 ((ClassScope) parent).connectTypeHierarchyWithoutMembers();
		}

		// double check that the hierarchy search has not already begun...
		SourceTypeBinding sourceType = referenceContext.binding;
		if ((sourceType.tagBits & BeginHierarchyCheck) != 0)
			return;

		sourceType.tagBits |= BeginHierarchyCheck;
		boolean noProblems = connectTypeVariables(referenceContext.typeParameters);
		noProblems &= connectSuperclass();
		noProblems &= connectSuperInterfaces();
		sourceType.tagBits |= EndHierarchyCheck;
		if (noProblems && sourceType.isHierarchyInconsistent())
			problemReporter().hierarchyHasProblems(sourceType);
	}

	public boolean detectCycle(TypeBinding superType, TypeReference reference, TypeBinding[] argTypes) {
		if (!(superType instanceof ReferenceBinding)) return false;

		if (argTypes != null) {
			for (int i = 0, l = argTypes.length; i < l; i++) {
				TypeBinding argType = argTypes[i].leafComponentType();
				if ((argType.tagBits & BeginHierarchyCheck) == 0 && argType instanceof SourceTypeBinding)
			    	// ensure if this is a source argument type that it has already been checked
			    	((SourceTypeBinding) argType).scope.connectTypeHierarchyWithoutMembers();
			}
		}

		if (reference == this.superTypeReference) { // see findSuperType()
			if (superType.isTypeVariable())
				return false; // error case caught in resolveSuperType()
			// abstract class X<K,V> implements java.util.Map<K,V>
			//    static abstract class M<K,V> implements Entry<K,V>
			if (superType.isParameterizedType())
				superType = ((ParameterizedTypeBinding) superType).type;
			compilationUnitScope().recordSuperTypeReference(superType); // to record supertypes
			return detectCycle(referenceContext.binding, (ReferenceBinding) superType, reference);
		}

		if ((superType.tagBits & BeginHierarchyCheck) == 0 && superType instanceof SourceTypeBinding)
			// ensure if this is a source superclass that it has already been checked
			((SourceTypeBinding) superType).scope.connectTypeHierarchyWithoutMembers();
		return false;
	}

	// Answer whether a cycle was found between the sourceType & the superType
	private boolean detectCycle(SourceTypeBinding sourceType, ReferenceBinding superType, TypeReference reference) {
		if (superType.isRawType())
			superType = ((RawTypeBinding) superType).type;
		// by this point the superType must be a binary or source type

		if (sourceType == superType) {
			problemReporter().hierarchyCircularity(sourceType, superType, reference);
			sourceType.tagBits |= HierarchyHasProblems;
			return true;
		}

		if (superType.isMemberType()) {
			ReferenceBinding current = superType.enclosingType();
			do {
				if (current.isHierarchyBeingConnected()) {
					problemReporter().hierarchyCircularity(sourceType, current, reference);
					sourceType.tagBits |= HierarchyHasProblems;
					current.tagBits |= HierarchyHasProblems;
					return true;
				}
			} while ((current = current.enclosingType()) != null);
		}

		if (superType.isBinaryBinding()) {
			// force its superclass & superinterfaces to be found... 2 possibilities exist - the source type is included in the hierarchy of:
			//		- a binary type... this case MUST be caught & reported here
			//		- another source type... this case is reported against the other source type
			boolean hasCycle = false;
			if (superType.superclass() != null) {
				if (sourceType == superType.superclass()) {
					problemReporter().hierarchyCircularity(sourceType, superType, reference);
					sourceType.tagBits |= HierarchyHasProblems;
					superType.tagBits |= HierarchyHasProblems;
					return true;
				}
				ReferenceBinding parentType = superType.superclass();
				if (parentType.isParameterizedType())
					parentType = ((ParameterizedTypeBinding) parentType).type;
				hasCycle |= detectCycle(sourceType, parentType, reference);
				if ((parentType.tagBits & HierarchyHasProblems) != 0) {
					sourceType.tagBits |= HierarchyHasProblems;
					parentType.tagBits |= HierarchyHasProblems; // propagate down the hierarchy
				}
			}

			ReferenceBinding[] itsInterfaces = superType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				for (int i = 0, length = itsInterfaces.length; i < length; i++) {
					ReferenceBinding anInterface = itsInterfaces[i];
					if (sourceType == anInterface) {
						problemReporter().hierarchyCircularity(sourceType, superType, reference);
						sourceType.tagBits |= HierarchyHasProblems;
						superType.tagBits |= HierarchyHasProblems;
						return true;
					}
					if (anInterface.isParameterizedType())
						anInterface = ((ParameterizedTypeBinding) anInterface).type;
					hasCycle |= detectCycle(sourceType, anInterface, reference);
					if ((anInterface.tagBits & HierarchyHasProblems) != 0) {
						sourceType.tagBits |= HierarchyHasProblems;
						superType.tagBits |= HierarchyHasProblems;
					}
				}
			}
			return hasCycle;
		}

		if (superType.isHierarchyBeingConnected()) {
			if (((SourceTypeBinding) superType).scope.superTypeReference != null) { // if null then its connecting its type variables
				problemReporter().hierarchyCircularity(sourceType, superType, reference);
				sourceType.tagBits |= HierarchyHasProblems;
				superType.tagBits |= HierarchyHasProblems;
				return true;
			}
		}
		if ((superType.tagBits & BeginHierarchyCheck) == 0)
			// ensure if this is a source superclass that it has already been checked
			((SourceTypeBinding) superType).scope.connectTypeHierarchyWithoutMembers();
		if ((superType.tagBits & HierarchyHasProblems) != 0)
			sourceType.tagBits |= HierarchyHasProblems;
		return false;
	}

	private ReferenceBinding findAmbiguousInterface(ReferenceBinding newInterface, ReferenceBinding currentType) {
		TypeBinding newErasure = newInterface.erasure();
		if (newInterface == newErasure) return null;

		ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
		int lastPosition = -1;
		do {
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		} while ((currentType = currentType.superclass()) != null);

		for (int i = 0; i <= lastPosition; i++) {
			ReferenceBinding[] interfaces = interfacesToVisit[i];
			for (int j = 0, length = interfaces.length; j < length; j++) {
				currentType = interfaces[j];
				if (currentType.erasure() == newErasure)
					if (currentType != newInterface)
						return currentType;

				ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
				if (itsInterfaces != NoSuperInterfaces) {
					if (++lastPosition == interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
					interfacesToVisit[lastPosition] = itsInterfaces;
				}
			}
		}
		return null;
	}

	private ReferenceBinding findSupertype(TypeReference typeReference) {
		try {
			typeReference.aboutToResolve(this); // allows us to trap completion & selection nodes
			compilationUnitScope().recordQualifiedReference(typeReference.getTypeName());
			this.superTypeReference = typeReference;
			ReferenceBinding superType = (ReferenceBinding) typeReference.resolveSuperType(this);
			this.superTypeReference = null;
			return superType;
		} catch (AbortCompilation e) {
			e.updateContext(typeReference, referenceCompilationUnit().compilationResult);
			throw e;
		}			
	}

	/* Answer the problem reporter to use for raising new problems.
	*
	* Note that as a side-effect, this updates the current reference context
	* (unit, type or method) in case the problem handler decides it is necessary
	* to abort.
	*/
	public ProblemReporter problemReporter() {
		MethodScope outerMethodScope;
		if ((outerMethodScope = outerMostMethodScope()) == null) {
			ProblemReporter problemReporter = referenceCompilationUnit().problemReporter;
			problemReporter.referenceContext = referenceContext;
			return problemReporter;
		}
		return outerMethodScope.problemReporter();
	}

	/* Answer the reference type of this scope.
	* It is the nearest enclosing type of this scope.
	*/
	public TypeDeclaration referenceType() {
		return referenceContext;
	}
	
	public String toString() {
		if (referenceContext != null)
			return "--- Class Scope ---\n\n"  //$NON-NLS-1$
							+ referenceContext.binding.toString();
		return "--- Class Scope ---\n\n Binding not initialized" ; //$NON-NLS-1$
	}
}
