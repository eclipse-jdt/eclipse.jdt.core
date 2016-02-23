package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Holds a location in the Eclipse workspace where a given resource was found. Note that a given
 * resource might be mapped to multiple locations in the workspace.
 * @since 3.12
 */
public class NdWorkspaceLocation extends NdNode {
	public static final FieldManyToOne<NdResourceFile> RESOURCE;
	public static final FieldString PATH;

	@SuppressWarnings("hiding")
	public static final StructDef<NdWorkspaceLocation> type;

	static {
		type = StructDef.create(NdWorkspaceLocation.class, NdNode.type);
		RESOURCE = FieldManyToOne.createOwner(type, NdResourceFile.WORKSPACE_MAPPINGS);
		PATH = type.addString();
		type.done();
	}

	public NdWorkspaceLocation(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdWorkspaceLocation(Nd pdom, NdResourceFile resource, char[] path) {
		super(pdom);

		RESOURCE.put(getNd(), this.address, resource);
		PATH.put(getNd(), this.address, path);
	}

	public IString getPath() {
		return PATH.get(getNd(), this.address);
	}

	public NdResourceFile getResourceFile() {
		return RESOURCE.get(getNd(), this.address);
	}
}
