package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IPackage;

/** 
 * This class takes and restores snapshots of a State.
 *
 * <p> What is saved:
 * <dl> 
 * <dd> - the build context
 * <dd> - information about package fragments
 * <dd> - principal structure of all built types
 * <dd> - any known problems
 * <dd> - information needed to retrieve binaries from the binary output
 * <dd> - dependency graph information
 * </dl>
 * <p> What is not saved:
 * <dl>
 * <dd> - the project
 * <dd> - binaries
 * </dl>
 *
 * <p> This class exists separate from <code>StateImpl</code>,
 * making it is easy to support other saved formats of workspace.
 *
 * <p> Example usage:
<code><pre>
StateImpl state;
DataOutput dout; // should be open
new StateSnap().save(state, dout);
...
DataInput din;
DevelopmentContext devctx;
IProject project;
StateImpl state = new StateSnap().restore(devctx, project, din);
</pre></code>
 * Note that reading in a state snapshot requires a project in hand.
 * The project must correspond to the state's project at the time it was saved.
 *
 * @see StateImpl
 */
public class StateSnap {
	/**
	 * Table from IType to IBuilderType, used only when reading.
	 */
	Hashtable fBuilderTypeTable;

	IPackage currentPackage;

	static final int MAGIC = 0x53544154; // magic = "STAT"e
	static final int VERSION5 = 0x0005;	
	static final int VERSION6 = 0x0006;	
/**
 * Creates a new StateSnap.
 */
public StateSnap() {
	super();
}
/** 
 * Reads and reconstructs a state from the given input stream.
 */
public StateImpl read(JavaDevelopmentContextImpl dc, IProject project, DataInputStream in) throws IOException {

	int magic = in.readInt();
	int version = in.readShort();
	if (magic != MAGIC) { // magic = "STAT"e
		throw new IOException(Util.bind("build.wrongFileFormat")); //$NON-NLS-1$
	}

	/* dispatch to appropriate reader */
	switch (version) {
		case VERSION5:
			return new StateSnapV5().read(dc, project, in);
		case VERSION6:
			return new StateSnapV6().read(dc, project, in);
		default:
			throw new IOException(Util.bind("build.unhandledVersionFormat")); //$NON-NLS-1$
	}
}
/** 
 * Saves key information about the given state
 * to the given output stream.  This snapshot can be used
 * subsequently in reconstructing the state.
 */
public void save(StateImpl state, DataOutputStream out) throws IOException {

	/* current version for writing state is version 6 */
	new StateSnapV6().save(state, out);
}
}
