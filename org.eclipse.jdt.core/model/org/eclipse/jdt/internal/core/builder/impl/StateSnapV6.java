package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;
import java.io.*;

/** 
 * StateSnap for state file format version 6.
 * @see StateSnap
 */
public class StateSnapV6 extends StateSnapV5 {
/** 
 * Read the next source entry.
 */
protected SourceEntry readSourceEntry(StateSnapConstantPool pool, DataInputStream in) throws IOException {
	IPath path = pool.getPath(in.readInt());
	String zipEntryPath = pool.getStringOrNull(in.readInt());
	String zipEntryFileName = pool.getStringOrNull(in.readInt());
	return new SourceEntry(path, zipEntryPath, zipEntryFileName);
}
/** 
 * Saves key information about the given state
 * to the given output stream.  This snapshot can be used
 * subsequently in reconstructing the state.
 */
public void save(StateImpl state, DataOutputStream out) throws IOException {

	// Build up pool.
	IDevelopmentContext dc = state.getDevelopmentContext();
	StateSnapConstantPool pool = new StateSnapConstantPool(dc);
	addBuildContextToPool(state, pool);
	addPackageMapToPool(state, pool);
	addSourceElementTableToPool(state, pool);
	addPrincipalStructureTableToPool(state, pool);
	addProblemTableToPool(state, pool);
	addDependencyGraphToPool(state, pool);
	
	// Write all.
	out.writeInt(MAGIC); 
	out.writeShort(StateSnap.VERSION6);
	
	byte[] fingerprint = state.getFingerprint();
	// regression test for 1F9M2KH: RQIB:ALL - Problem saving incrementally built state
	Assert.isNotNull(fingerprint);
	out.writeShort(fingerprint.length);
	out.write(fingerprint);
	
	pool.write(out);
	writeBuildContext(state, pool, out);
	writePackageMap(state, pool, out);
	writeSourceElementTable(state, pool, out);
	writePrincipalStructureTable(state, pool, out);
	writeProblemReporter(state, pool, out);
	writeDependencyGraph(state, pool, out);

}
}
