/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Corresponds roughly to a JavaTypeSignature, as described in section 4.7.9.1 of the Java VM spec version 4, with the
 * addition of annotations and backpointers to locations where the type is used.
 * <p>
 * Holds back-pointers to all the entities that refer to the name, along with pointers to all classes that have this
 * name. Note that this isn't the class declaration itself. The same index can hold multiple jar files, some of which
 * may contain classes with the same name. All classes that use this fully-qualified name point to the same
 * {@link NdTypeSignature}.
 * <p>
 * Other entities should refer to a type via its TypeId if there is any possiblity that the type may change based on the
 * classpath. It should refer to the type directly if there is no possibility for a type lookup. For example, nested
 * classes refer to their enclosing class directly since they live in the same file and there is no possibility for the
 * enclosing class to change based on the classpath. Classes refer to their base class via its TypeId since the parent
 * class might live in a different jar and need to be resolved on the classpath.
 *
 * @since 3.12
 */
public abstract class NdTypeSignature extends NdNode {
	public static final FieldOneToMany<NdType> SUBCLASSES;
	public static final FieldOneToMany<NdAnnotation> ANNOTATIONS_OF_THIS_TYPE;
	public static final FieldOneToMany<NdTypeInterface> IMPLEMENTATIONS;
	public static final FieldOneToMany<NdVariable> VARIABLES_OF_TYPE;
	public static final FieldOneToMany<NdConstantClass> USED_AS_CONSTANT;
	public static final FieldOneToMany<NdConstantEnum> USED_AS_ENUM_CONSTANT;
	public static final FieldOneToMany<NdTypeArgument> USED_AS_TYPE_ARGUMENT;
	public static final FieldOneToMany<NdTypeBound> USED_AS_TYPE_BOUND;
	public static final FieldOneToMany<NdMethodParameter> USED_AS_METHOD_ARGUMENT;
	public static final FieldOneToMany<NdMethodException> USED_AS_EXCEPTION;
	public static final FieldOneToMany<NdMethod> USED_AS_RETURN_TYPE;

	@SuppressWarnings("hiding")
	public static StructDef<NdTypeSignature> type;

	static {
		type = StructDef.createAbstract(NdTypeSignature.class, NdNode.type);
		SUBCLASSES = FieldOneToMany.create(type, NdType.SUPERCLASS);
		ANNOTATIONS_OF_THIS_TYPE = FieldOneToMany.create(type, NdAnnotation.ANNOTATION_TYPE);
		IMPLEMENTATIONS = FieldOneToMany.create(type, NdTypeInterface.IMPLEMENTS);
		VARIABLES_OF_TYPE = FieldOneToMany.create(type, NdVariable.TYPE);
		USED_AS_CONSTANT = FieldOneToMany.create(type, NdConstantClass.VALUE);
		USED_AS_ENUM_CONSTANT = FieldOneToMany.create(type, NdConstantEnum.ENUM_TYPE);
		USED_AS_TYPE_ARGUMENT = FieldOneToMany.create(type, NdTypeArgument.TYPE_SIGNATURE);
		USED_AS_TYPE_BOUND = FieldOneToMany.create(type, NdTypeBound.TYPE);
		USED_AS_METHOD_ARGUMENT = FieldOneToMany.create(type, NdMethodParameter.ARGUMENT_TYPE);
		USED_AS_EXCEPTION = FieldOneToMany.create(type, NdMethodException.EXCEPTION_TYPE);
		USED_AS_RETURN_TYPE = FieldOneToMany.create(type, NdMethod.RETURN_TYPE);
		type.useStandardRefCounting().done();
	}

	public NdTypeSignature(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdTypeSignature(Nd pdom) {
		super(pdom);
	}

	public List<NdType> getSubclasses() {
		return SUBCLASSES.asList(getNd(), this.address);
	}

	public List<NdTypeInterface> getImplementations() {
		return IMPLEMENTATIONS.asList(getNd(), this.address);
	}

	/**
	 * Returns all subclasses (for classes) and implementations (for interfaces) of this type
	 */
	public List<NdType> getSubTypes() {
		List<NdType> result = new ArrayList<>();
		result.addAll(getSubclasses());

		for (NdTypeInterface next : getImplementations()) {
			result.add(next.getImplementation());
		}

		return result;
	}

	/**
	 * Returns the raw version of this type, if one exists. That is, the version of this type
	 * without any generic arguments or annotations, which the java runtime sees. Returns null
	 * of this signature doesn't have a raw type, for example if it is a type variable.
	 */
	public abstract NdTypeId getRawType();

	public abstract void getSignature(CharArrayBuffer result);

	public final boolean isArrayType() {
		return getRawType().hasSimpleName("["); //$NON-NLS-1$
	}

	public abstract boolean isTypeVariable();
}
