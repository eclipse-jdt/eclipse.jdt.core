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
package org.eclipse.jdt.internal.core.pdom.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Corresponds roughly to a JavaTypeSignature, as described in section 4.7.9.1 of the Java VM spec version 4, with the
 * addition of annotations and backpointers to locations where the type is used.
 * <p>
 * Holds back-pointers to all the entities that refer to the name, along with pointers to all classes that have this
 * name. Note that this isn't the class declaration itself. The same index can hold multiple jar files, some of which
 * may contain classes with the same name. All classes that use this fully-qualified name point to the same
 * {@link PDOMTypeSignature}.
 * <p>
 * Other entities should refer to a type via its TypeId if there is any possiblity that the type may change based on the
 * classpath. It should refer to the type directly if there is no possibility for a type lookup. For example, nested
 * classes refer to their enclosing class directly since they live in the same file and there is no possibility for the
 * enclosing class to change based on the classpath. Classes refer to their base class via its TypeId since the parent
 * class might live in a different jar and need to be resolved on the classpath.
 * 
 * @since 3.12
 */
public abstract class PDOMTypeSignature extends PDOMNode {
	public static final FieldOneToMany<PDOMType> SUBCLASSES;
	public static final FieldOneToMany<PDOMAnnotation> ANNOTATIONS_OF_THIS_TYPE;
//	public static final FieldOneToMany<PDOMType> USED_AS_BOUND;
//	public static final FieldOneToMany<PDOMType> USED_AS_GENERIC_TYPE_OF_WILDCARD_TYPE;
//	public static final FieldOneToMany<PDOMType> USED_AS_COMPONENT_TYPE;
//	public static final FieldOneToMany<PDOMType> USED_AS_ELEMENT_TYPE;
//	public static final FieldOneToMany<PDOMType> USED_AS_ERASURE;
	public static final FieldOneToMany<PDOMTypeInterface> IMPLEMENTATIONS;
//	public static final FieldOneToMany<PDOMTypeArgument> USED_AS_TYPE_ARGUMENT;
//	public static final FieldOneToMany<PDOMTypeBounds> USED_AS_TYPE_BOUNDS;
//	public static final FieldOneToMany<PDOMType> USED_AS_TYPE_DECLARATION;
//	public static final FieldOneToMany<PDOMTypeParameter> USED_AS_TYPE_PARAMETER;
//	public static final FieldOneToMany<PDOMType> USED_AS_WILDCARD;
	public static final FieldOneToMany<PDOMVariable> VARIABLES_OF_TYPE;
	public static final FieldOneToMany<PDOMConstantClass> USED_AS_CONSTANT;
	public static final FieldOneToMany<PDOMConstantEnum> USED_AS_ENUM_CONSTANT;
	public static final FieldOneToMany<PDOMTypeArgument> USED_AS_TYPE_ARGUMENT;

	@SuppressWarnings("hiding")
	public static StructDef<PDOMTypeSignature> type;

	static {
		type = StructDef.createAbstract(PDOMTypeSignature.class, PDOMNode.type);
		SUBCLASSES = FieldOneToMany.create(type, PDOMType.SUPERCLASS);
//		USED_AS_BOUND = FieldOneToMany.create(type, PDOMType.class, PDOMType.BOUND);
		ANNOTATIONS_OF_THIS_TYPE = FieldOneToMany.create(type, PDOMAnnotation.ANNOTATION_TYPE);
//		USED_AS_GENERIC_TYPE_OF_WILDCARD_TYPE = FieldOneToMany.create(type, PDOMType.class, PDOMType.GENERIC_TYPE_OF_WILDCARD_TYPE);
//		USED_AS_COMPONENT_TYPE = FieldOneToMany.create(type, PDOMType.class, PDOMType.COMPONENT_TYPE);
//		USED_AS_ELEMENT_TYPE = FieldOneToMany.create(type, PDOMType.class, PDOMType.ELEMENT_TYPE);
//		USED_AS_ERASURE = FieldOneToMany.create(type, PDOMType.class, PDOMType.ERASURE);
		IMPLEMENTATIONS = FieldOneToMany.create(type, PDOMTypeInterface.IMPLEMENTS);
//		USED_AS_TYPE_ARGUMENT = FieldOneToMany.create(type, PDOMTypeArgument.class, PDOMTypeArgument.ARGUMENT);
//		USED_AS_TYPE_BOUNDS = FieldOneToMany.create(type, PDOMTypeBounds.class, PDOMTypeBounds.BOUNDS);
//		USED_AS_TYPE_DECLARATION = FieldOneToMany.create(type, PDOMType.class, PDOMType.TYPE_DECLARATION);
//		USED_AS_TYPE_PARAMETER = FieldOneToMany.create(type, PDOMTypeParameter.class, PDOMTypeParameter.PARAMETER);
//		USED_AS_WILDCARD = FieldOneToMany.create(type, PDOMType.class, PDOMType.WILDCARD);
		VARIABLES_OF_TYPE = FieldOneToMany.create(type, PDOMVariable.TYPE);
		USED_AS_CONSTANT = FieldOneToMany.create(type, PDOMConstantClass.VALUE);
		USED_AS_ENUM_CONSTANT = FieldOneToMany.create(type, PDOMConstantEnum.ENUM_TYPE);
		USED_AS_TYPE_ARGUMENT = FieldOneToMany.create(type, PDOMTypeArgument.TYPE_SIGNATURE);
		type.useStandardRefCounting().done();
	}

	public PDOMTypeSignature(PDOM pdom, long record) {
		super(pdom, record);
	}

	public PDOMTypeSignature(PDOM pdom) {
		super(pdom);
	}

	public List<PDOMType> getSubclasses() {
		return SUBCLASSES.asList(getPDOM(), this.address);
	}

	public List<PDOMTypeInterface> getImplementations() {
		return IMPLEMENTATIONS.asList(getPDOM(), this.address);
	}

	/**
	 * Returns all subclasses (for classes) and implementations (for interfaces) of this type
	 */
	public List<PDOMType> getSubTypes() {
		List<PDOMType> result = new ArrayList<>();
		result.addAll(getSubclasses());

		for (PDOMTypeInterface next : getImplementations()) {
			result.add(next.getImplementation());
		}

		return result;
	}

	/**
	 * Returns the raw version of this type, if one exists. That is, the version of this type 
	 * without any generic arguments or annotations, which the java runtime sees. Returns null
	 * of this signature doesn't have a raw type, for example if it is a type variable.
	 */
	public abstract PDOMTypeId getRawType();
}
