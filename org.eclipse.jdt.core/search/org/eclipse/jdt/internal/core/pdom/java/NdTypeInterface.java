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

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * Represents one interface implemented by a specific type. This is an intermediate object between a PDOMType
 * and the PDOMTypeIds corresponding to its interfaces, which is necessary in order to implement the many-to-many
 * relationship between them.
 *  
 * @since 3.12
 */
public class NdTypeInterface extends NdNode {
	public static final FieldManyToOne<NdType> APPLIES_TO;
	public static final FieldManyToOne<NdTypeSignature> IMPLEMENTS;

	@SuppressWarnings("hiding")
	public static StructDef<NdTypeInterface> type;

	static {
		type = StructDef.create(NdTypeInterface.class, NdNode.type);
		APPLIES_TO = FieldManyToOne.createOwner(type, NdType.INTERFACES);
		IMPLEMENTS = FieldManyToOne.create(type, NdTypeSignature.IMPLEMENTATIONS);
		type.done();
	}
	
	public NdTypeInterface(Nd pdom, long record) {
		super(pdom, record);
	}

	public NdTypeInterface(Nd pdom, NdType targetType, NdTypeSignature makeTypeId) {
		super(pdom);

		APPLIES_TO.put(pdom, this.address, targetType);
		IMPLEMENTS.put(pdom, this.address, makeTypeId);
	}

	public NdType getImplementation() {
		return APPLIES_TO.get(getPDOM(), this.address);
	}

	public NdTypeSignature getInterface() {
		return IMPLEMENTS.get(getPDOM(), this.address);
	}
}
