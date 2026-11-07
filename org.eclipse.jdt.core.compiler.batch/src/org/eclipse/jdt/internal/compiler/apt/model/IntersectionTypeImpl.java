/*******************************************************************************
 * Copyright (c) 2024 Kamil Krzywanski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kamil Krzywanski - initial creation of Intersection type and Implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * Implementation of IntersectionType, e.g. the upper bound of {@code <T extends Number & Runnable>}
 */
public class IntersectionTypeImpl extends TypeMirrorImpl implements IntersectionType {
	private final List<? extends TypeMirror> bounds;

	IntersectionTypeImpl(BaseProcessingEnvImpl env, IntersectionTypeBinding18 binding) {
		this(env, binding, getBoundBindings(binding));
	}

	/**
	 * @param bounds at least two reference types, in declaration order
	 */
	IntersectionTypeImpl(BaseProcessingEnvImpl env, TypeBinding[] bounds) {
		this(env, createCanonicalBinding(env, bounds), bounds);
	}

	private IntersectionTypeImpl(BaseProcessingEnvImpl env, TypeBinding binding, TypeBinding[] bounds) {
		super(env, binding);
		this.bounds = Arrays.stream(bounds).map(env.getFactory()::newTypeMirror).toList();
	}

	/*
	 * Equivalent intersections share one binding, so that they are the same type for the compiler as well:
	 * with unannotated components, the class or else java.lang.Object first and the interfaces ordered by name.
	 * Only the mirror keeps the bounds as declared.
	 */
	private static TypeBinding createCanonicalBinding(BaseProcessingEnvImpl env, TypeBinding[] bounds) {
		ReferenceBinding superclass = null;
		List<ReferenceBinding> components = new ArrayList<>(bounds.length + 1);
		for (TypeBinding bound : bounds) {
			ReferenceBinding component = (ReferenceBinding) bound.unannotated();
			if (component.isInterface()) {
				components.add(component);
			} else {
				superclass = component;
			}
		}
		// unlike readable names, the names of the erasures are cheap to compute and differ for valid bounds
		components.sort((component1, component2) -> CharOperation.compareTo(component1.erasure().readableName(), component2.erasure().readableName()));
		// with a class first, createIntersectionType18() keeps this order
		components.add(0, superclass != null ? superclass : env.getLookupEnvironment().getType(TypeConstants.JAVA_LANG_OBJECT));
		return env.getLookupEnvironment().createIntersectionType18(components.toArray(new ReferenceBinding[components.size()]));
	}

	/*
	 * Answer the bounds of an intersection recreated from its binding, without the java.lang.Object
	 * that the canonical binding of an intersection of interfaces starts with.
	 */
	private static TypeBinding[] getBoundBindings(IntersectionTypeBinding18 binding) {
		ReferenceBinding[] intersectingTypes = binding.intersectingTypes;
		if (intersectingTypes.length > 2 && intersectingTypes[0].id == TypeIds.T_JavaLangObject) {
			return Arrays.copyOfRange(intersectingTypes, 1, intersectingTypes.length);
		}
		return intersectingTypes;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#getKind()
	 */
	@Override
	public TypeKind getKind() {
		return TypeKind.INTERSECTION;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.TypeMirror#accept(javax.lang.model.type.TypeVisitor, java.lang.Object)
	 */
	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitIntersection(this, p);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.type.IntersectionType#getBounds()
	 */
	@Override
	public List<? extends TypeMirror> getBounds() {
		return this.bounds;
	}

	@Override
	public String toString() {
		return this.bounds.stream().map(TypeMirror::toString).collect(Collectors.joining("&")); //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj) {
		// equivalent intersections share the binding, but not their bounds
		return super.equals(obj) && obj instanceof IntersectionTypeImpl other && this.bounds.equals(other.bounds);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
