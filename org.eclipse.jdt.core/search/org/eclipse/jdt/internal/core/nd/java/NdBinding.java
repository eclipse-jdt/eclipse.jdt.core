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

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Base class for bindings in the PDOM.
 *
 * @since 3.12
 */
public abstract class NdBinding extends NdNode implements IAdaptable {
	public static final FieldOneToMany<NdAnnotation> ANNOTATIONS;
	public static final FieldInt MODIFIERS;
	public static final FieldOneToMany<NdTypeParameter> TYPE_PARAMETERS;
	public static final FieldManyToOne<NdResourceFile> FILE;
	public static final FieldOneToMany<NdVariable> VARIABLES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdBinding> type;

	static {
		type = StructDef.create(NdBinding.class, NdNode.type);
		ANNOTATIONS = FieldOneToMany.create(type, NdAnnotation.PARENT_BINDING);
		MODIFIERS = type.addInt();
		TYPE_PARAMETERS = FieldOneToMany.create(type, NdTypeParameter.PARENT);
		FILE = FieldManyToOne.createOwner(type, NdResourceFile.ALL_NODES);
		VARIABLES = FieldOneToMany.create(type, NdVariable.PARENT);
		type.done();
	}

	private static final NdAnnotation[] NO_ANNOTATIONS = new NdAnnotation[0];

	public NdBinding(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdBinding(Nd pdom, NdResourceFile resource) {
		super(pdom);

		FILE.put(pdom, this.address, resource);
	}

	/**
	 * Tests whether this binding has one of the flags defined in {@link Flags}
	 */
	public boolean hasModifier(int toTest) {
		return (MODIFIERS.get(getNd(), this.address) & toTest) != 0;
	}

	/**
	 * Sets the modifiers for this binding (defined in {@link Flags})
	 */
	public void setModifiers(int toSet) {
		MODIFIERS.put(getNd(), this.address, toSet);
	}

	public int getModifiers() {
		return MODIFIERS.get(getNd(), this.address);
	}

	public NdAnnotation[] getAnnotations() {
		int numAnnotations = ANNOTATIONS.size(getNd(), this.address);

		if (numAnnotations == 0) {
			return NO_ANNOTATIONS;
		}

		final NdAnnotation[] result = new NdAnnotation[numAnnotations];

		// If we got this far, the pointer to the linked list is non-null
		ANNOTATIONS.accept(getNd(), this.address, new FieldOneToMany.Visitor<NdAnnotation>() {
			@Override
			public void visit(int index, NdAnnotation toVisit) {
				result[index] = toVisit;
			}
		});

		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(NdBinding.class))
			return this;

		// Any PDOMBinding can have a persistent tag. These tags should be deleted when
		// the PDOMBinding is deleted. However, PDOMBinding's don't get deleted, so there is no way
		// to trigger deleting of the tags. If the implementation is changed so that PDOMBindings
		// do get deleted, then it should call:
		// PDOMTagIndex.setTags(getNd(), pdomBinding.address, Collections.<ITag>emptyList());
		// to clear out all tags for the binding.
		// if (adapter.isAssignableFrom(ITagReader.class))
		// return new PDOMTaggable(getNd(), getRecord());

		return null;
	}

	public final int getBindingConstant() {
		return getNodeType();
	}

	public void setFile(NdResourceFile file) {
		FILE.put(getNd(), this.address, file);
	}

	public NdResourceFile getFile() {
		return FILE.get(getNd(), this.address);
	}

	public char[][] getTypeParameterSignatures() {
		List<NdTypeParameter> parameters = getTypeParameters();
		char[][] result = new char[parameters.size()][];

		int idx = 0;
		for (NdTypeParameter next : parameters) {
			CharArrayBuffer nextArray = new CharArrayBuffer();
			next.getSignature(nextArray);
			result[idx] = nextArray.getContents();
		}
		return result;
	}

	private List<NdTypeParameter> getTypeParameters() {
		return TYPE_PARAMETERS.asList(getNd(), this.address);
	}
}
