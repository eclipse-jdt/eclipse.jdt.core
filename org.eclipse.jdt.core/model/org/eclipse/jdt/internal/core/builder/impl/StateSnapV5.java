package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IDevelopmentContext;
import org.eclipse.jdt.internal.core.builder.IImageContext;
import org.eclipse.jdt.internal.core.builder.IPackage;
import org.eclipse.jdt.internal.core.builder.IProblemReporter;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.util.LookupTable;

/** 
 * StateSnap for state file format version 5.
 * @see StateSnap
 */
public class StateSnapV5 {
	/**
	 * Table from IType to IBuilderType, used only when reading.
	 */
	Hashtable fBuilderTypeTable;
	IPackage currentPackage;
	static final int MAGIC = 0x53544154; // magic = "STAT"e
	static final int VERSION = 0x0005;
/** 
 * Add pool constants for the build context.
 */
protected void addBuildContextToPool(IState state, StateSnapConstantPool pool) {
	IImageContext ctx = state.getBuildContext();
	if (ctx != null) {
		IPackage[] pkgs = ctx.getPackages();
		for (int i = 0; i < pkgs.length; ++i) {
			pool.add(pkgs[i]);
		}
	}
}
/** 
 * Add pool constants for the dependency graph.
 */
protected void addDependencyGraphToPool(StateImpl state, StateSnapConstantPool pool) {
	DependencyGraph graph = state.getInternalDependencyGraph();
	for (Enumeration e = graph.getNodes(); e.hasMoreElements(); ) {
		INode node = (INode)e.nextElement();
		switch (node.getKind()) {
			case INode.JCU_NODE:
				JCUNode jcuNode = (JCUNode) node;
				pool.add(state.getSourceEntry(jcuNode.getPackageElement()));
				IType[] types = jcuNode.getTypes();
				for (int i = 0; i < types.length; ++i) {
					pool.add(types[i]);
				}
				break;
			case INode.TYPE_NODE:
				TypeNode typeNode = (TypeNode)node;
				pool.add(state.getSourceEntry(typeNode.getPackageElement()));
				break;
			case INode.NAMESPACE_NODE:
				pool.add(((NamespaceNode)node).getPackage());
				break;
			case INode.ZIP_NODE:
				pool.add(((ZipNode)node).getZipFile());
				break;
			default:
				Assert.isTrue(false, "Unexpected kind of node"/*nonNLS*/);
		}
		// Don't need to process node dependents here, since they're nodes as well
		// and will have their info added to the pool above.
	}
}
/** 
 * Add pool constants for the package map.
 */
protected void addPackageMapToPool(StateImpl state, StateSnapConstantPool pool) {
	PackageMap map = state.getPackageMap();
	for (Enumeration e = map.getAllPackages(); e.hasMoreElements(); ) {
		IPackage pkg = (IPackage)e.nextElement();
		pool.add(pkg);
		IPath[] fragments = map.getFragments(pkg);
		for (int i = 0; i < fragments.length; ++i) {
			pool.add(fragments[i]);
		}
	}
}
/** 
 * Add pool constants for the principal structure table.
 */
protected void addPrincipalStructureTableToPool(StateImpl state, StateSnapConstantPool pool) {
	Hashtable table = state.getPrincipalStructureTable();
	for (Enumeration e = table.elements(); e.hasMoreElements(); ) {
		TypeStructureEntry tsEntry = (TypeStructureEntry) e.nextElement();
		addTypeStructureEntryToPool(tsEntry, pool);
	}
}
/** 
 * Add pool constants for the problem table.
 */
protected void addProblemTableToPool(StateImpl state, StateSnapConstantPool pool) {
	IProblemReporter reporter = state.getProblemReporter();
	if (reporter instanceof ProblemTable) {
		for (Enumeration e = reporter.getAllProblems(); e.hasMoreElements();) {
			addProblemToPool((ProblemDetailImpl) e.nextElement(), pool);
		}
		for (Enumeration e = reporter.getImageProblems(); e.hasMoreElements();) {
			addProblemToPool((ProblemDetailImpl) e.nextElement(), pool);
		}
	}
}
/** 
 * Add pool constants for a problem.
 */
protected void addProblemToPool(ProblemDetailImpl pb, StateSnapConstantPool pool) {
	SourceEntry sourceEntry = pb.getSourceEntry();
	if (sourceEntry != null) {
		pool.add(sourceEntry);
	}
	pool.add(pb.getMessage());
}
/** 
 * Add pool constants for the source element table.
 */
protected void addSourceElementTableToPool(StateImpl state, StateSnapConstantPool pool) {
	SourceElementTable table = state.getSourceElementTable();
	for (Enumeration e = state.getPackageMap().getAllPackages(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		// Assume pkg has already been added to the pool
		LookupTable entryTable = table.getPackageTable(pkg);
		for (Enumeration keys = entryTable.keys(); keys.hasMoreElements();) {
			String fileName = (String) keys.nextElement();
			pool.add(fileName);
			SourceEntry sEntry = (SourceEntry) entryTable.get(fileName);
			pool.add(sEntry);
		}
	}
}
/** 
 * Add pool constants for a type structure entry.
 */
protected void addTypeStructureEntryToPool(TypeStructureEntry tsEntry, StateSnapConstantPool pool) {
	pool.add(tsEntry.getType());
}
protected void badFormat() throws IOException {
	throw new IOException("Error in format"/*nonNLS*/);
}
/**
 * Check that the next section has the given name.
 */
protected void checkSection(DataInputStream in, String name) throws IOException {
	String toCheck = in.readUTF();
	if (!toCheck.equals(name))
		badFormat();
}
protected PackageElement getPackageElement(int entryNum, PackageElement[] packageElements, StateSnapConstantPool pool, StateImpl state) throws IOException {
	if (packageElements[entryNum] == null) {
		SourceEntry sourceEntry = pool.getSourceEntry(entryNum);
		packageElements[entryNum] = state.packageElementFromSourceEntry(sourceEntry);
	}
	return packageElements[entryNum];
}
/** 
 * Reads and reconstructs a state from the given input stream.
 */
public StateImpl read(JavaDevelopmentContextImpl dc, IProject project, DataInputStream in) throws IOException {

	/* magic number and version have already been read */
	StateImpl state = new StateImpl(dc, project);
	int fingerprintLen = in.readShort();
	byte[] fingerprint = new byte[fingerprintLen];
	in.readFully(fingerprint);
	state.setFingerprint(fingerprint);

	// Read pool.
	StateSnapConstantPool pool = new StateSnapConstantPool(dc, in, this);
	state.readClassPath(); // Read classpath from the project
	state.setBuildContext(readBuildContext(dc, pool, in));
	PackageMap packageMap = readPackageMap(pool, in);
	state.setPackageMap(packageMap);
	state.setSourceElementTable(readSourceElementTable(pool, in));
	state.setPrincipalStructureTable(readPrincipalStructureTable(pool, in, state));
	state.setProblemReporter(readProblemReporter(project, pool, in));
	state.setInternalDependencyGraph(readDependencyGraph(pool, in, state));
	state.buildPrincipalStructureByPackageTable();

	// We don't need the project any more
	//state.resetProject();
	return state;
}
/** 
 * Read the build context.
 */
protected IImageContext readBuildContext(IDevelopmentContext dc, StateSnapConstantPool pool, DataInputStream in) throws IOException {
	checkSection(in, "BuildContext"/*nonNLS*/);
	int numPkgs = in.readInt();
	if (numPkgs == -1) {
		return null;
	}
	IPackage[] pkgs = new IPackage[numPkgs];
	for (int i = 0; i < pkgs.length; ++i) {
		pkgs[i] = pool.getPackage(in.readInt());
	}
	return dc.getImage().createImageContext(pkgs);
}
/** 
 * Read the dependency graph.
 * The state is needed to create CompilationUnit objects from SourceEntry objects.
 */
protected DependencyGraph readDependencyGraph(StateSnapConstantPool pool, DataInputStream in, StateImpl state) throws IOException {
	checkSection(in, "DependencyGraph"/*nonNLS*/);
	DependencyGraph graph = new DependencyGraph();
	
	// Avoid creating package elements for each node and dependency.
	PackageElement[] packageElements = new PackageElement[pool.size()];
	while (in.readBoolean()) {
		JCUNode jcu = null;
		try {
			int entryNum = in.readInt();
			PackageElement unit = getPackageElement(entryNum, packageElements, pool, state);
			jcu = (JCUNode)graph.getNodeFor(unit);
			int numTypes = in.readByte() & 0xFF;
			IType[] types = new IType[numTypes];
			for (int j = 0; j < numTypes; ++j) {
				types[j] = pool.getType(in.readInt());
			}
			jcu.setTypes(types);
			}
		catch (ClassCastException e) {  // the source entry should be a CU
			badFormat();
		}

		// Read dependencies
		int numDep = in.readShort() & 0xFFFF;
		INode[] dependencies = new INode[numDep];
		for (int j = 0; j < numDep; ++j) {
			int entryNum = in.readInt();
			Object obj = pool.getObject(entryNum);
			if (obj instanceof SourceEntry) {
				PackageElement unit = getPackageElement(entryNum, packageElements, pool, state);
				dependencies[j] = graph.getNodeFor(unit);
			} else if (obj instanceof IPackage) {
				dependencies[j] = graph.getNodeFor((IPackage) obj);
			} else if (obj instanceof IPath) {
				dependencies[j] = graph.getNodeFor((IPath) obj);
			} else {
				// Unexpected referrent
				badFormat();
			}
		}
		jcu.setDependencies(dependencies);
	}
	skipAttributes(in);
	graph.integrityCheck();
	return graph;
}
/** 
 * Read the package map.
 */
protected PackageMap readPackageMap(StateSnapConstantPool pool, DataInputStream in) throws IOException {
	checkSection(in, "PackageMap"/*nonNLS*/);
	PackageMap map = new PackageMap();
	int size = in.readInt();
	for (int i = 0; i < size; ++i) {
		IPackage pkg = pool.getPackage(in.readInt());
		int numFragments = in.readShort() & 0xFFFF;
		IPath[] fragments = new IPath[numFragments];
		for (int j = 0; j < numFragments; ++j) {
			fragments[j] = pool.getPath(in.readInt());
		}
		map.putFragments(pkg, fragments);
	}
	return map;
}
/** 
 * Read the principal structure table.
 */
protected Hashtable readPrincipalStructureTable(StateSnapConstantPool pool, DataInputStream in, StateImpl state) throws IOException {
	checkSection(in, "PrincipalStructureTable"/*nonNLS*/);
	int numEntries = in.readInt();
	Hashtable table = new Hashtable(numEntries * 2 + 1);
	for (int i = 0; i < numEntries; ++i) {
		TypeStructureEntry entry = readTypeStructureEntry(pool, in, state);
		table.put(entry.getType(), entry);
	}
	return table;
}
/** 
 * Read a problem.
 */
protected ProblemDetailImpl readProblem(StateSnapConstantPool pool, DataInputStream in) throws IOException {
	String msg = pool.getString(in.readInt());
	int id = in.readInt();
	int severity = in.readInt();
	int temp = in.readInt();
	SourceEntry sourceEntry = (temp == 0) ? null : pool.getSourceEntry(temp);
	int startPos = in.readInt();
	int endPos = in.readInt();
	int lineNumber = in.readInt();
	skipAttributes(in);
	return new ProblemDetailImpl(msg, id, severity, sourceEntry, startPos, endPos, lineNumber);
}
/** 
 * Read the problem reporter.
 */
protected IProblemReporter readProblemReporter(IProject project, StateSnapConstantPool pool, DataInputStream in) throws IOException {
	checkSection(in, "Problems"/*nonNLS*/);
	IProblemReporter reporter = null;
	boolean isProblemTable = in.readBoolean();
	if (isProblemTable) {
		reporter = new ProblemTable();
		int numProblems = in.readInt();
		for (int i = 0; i < numProblems; ++i) {
			ProblemDetailImpl pb = readProblem(pool, in);
			reporter.putProblem(pb.getSourceEntry(), pb);
		}
	} else {
		String className = in.readUTF();
		Class clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IOException("Class "/*nonNLS*/ + className + " was not found."/*nonNLS*/);
		}
		try {
			reporter = (IProblemReporter) clazz.newInstance();
		} catch (InstantiationException e) {
			throw new IOException("Could not instanciate "/*nonNLS*/ + clazz.getName());
		} catch (IllegalAccessException e) {
			throw new IOException("Could not instanciate "/*nonNLS*/ + clazz.getName());
		}
		reporter.initialize(project, JavaModelManager.getJavaModelManager().getDevelopmentContext(project));
	}
	return reporter;
}
/** 
 * Read the source element table.
 */
protected SourceElementTable readSourceElementTable(StateSnapConstantPool pool, DataInputStream in) throws IOException {
	checkSection(in, "SourceElementTable"/*nonNLS*/);
	SourceElementTable table = new SourceElementTable();
	int numPackages = in.readInt();
	for (int i = 0; i < numPackages; ++i) {
		IPackage pkg = pool.getPackage(in.readInt());
		int numEntries = in.readInt();
		LookupTable entryTable = new LookupTable(numEntries);
		for (int j = 0; j < numEntries; ++j) {
			String fileName = pool.getString(in.readInt());
			SourceEntry sEntry = pool.getSourceEntry(in.readInt());
			entryTable.put(fileName, sEntry);
		}
		table.putPackageTable(pkg, entryTable);
	}
	return table;
}
/** 
 * Read the next source entry.
 */
protected SourceEntry readSourceEntry(StateSnapConstantPool pool, DataInputStream in) throws IOException {
	IPath path = pool.getPath(in.readInt());
	String zipEntryName = pool.getStringOrNull(in.readInt());
	String zipEntryPath = null, zipEntryFileName = null;
	if (zipEntryName != null) {
		int pos = zipEntryName.lastIndexOf('/');
		if (pos != -1) {
			zipEntryPath = zipEntryName.substring(0, pos);
			zipEntryFileName = zipEntryName.substring(pos + 1);
		} else {
			zipEntryPath = null;
			zipEntryFileName = zipEntryName;
		}
	}
	return new SourceEntry(path, zipEntryPath, zipEntryFileName);
}
/** 
 * Read a type handle.
 * Allow null if allowNull is true.
 */
IType readTypeHandle(StateSnapConstantPool pool, DataInputStream in, boolean allowNull) throws IOException {
	int index = in.readInt();
	if (index == 0) {
		if (!allowNull)
			badFormat();
		return null;
	}
	return pool.getType(index);
}
/** 
 * Read a class or interface type handle and return its name.
 * Allow null if allowNull is true.
 */
String readTypeHandleAsName(StateSnapConstantPool pool, DataInputStream in, boolean allowNull) throws IOException {
	IType typeHandle = readTypeHandle(pool, in, allowNull);
	if (typeHandle == null) {
		if (!allowNull)
			badFormat();
		return null;
	}
	Assert.isTrue(!typeHandle.isPrimitive() && !typeHandle.isArray()); // must not be a class or interface handle
	return typeHandle.getName(); //.intern();  // intern it
}
/** 
 * Read a class or interface type handle and return its name.
 * Allow null if allowNull is true.
 */
String readTypeHandleAsSignature(StateSnapConstantPool pool, DataInputStream in) throws IOException {
	IType typeHandle = readTypeHandle(pool, in, false);
	return ((TypeImpl) typeHandle).getVMSignature(); //.intern(); // intern it
}
/** 
 * Read a type structure entry.
 */
protected TypeStructureEntry readTypeStructureEntry(StateSnapConstantPool pool, DataInputStream in, StateImpl state) throws IOException {
	SourceEntry sEntry = pool.getSourceEntry(in.readInt());
	IType type = readTypeHandle(pool, in, false);
	return new TypeStructureEntry(sEntry, type);
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
	out.writeShort(VERSION);
	
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
/** 
 * Skip over the attributes on read.
 */
protected void skipAttributes(DataInputStream in) throws IOException {
	int numAttributes = in.readShort() & 0xFFFF;
	for (int i = 0; i < numAttributes; ++i) {
		int nameIndex = in.readInt();
		int len = in.readShort() & 0xFFFF;
		in.skipBytes(len);
	}
}
/** 
 * Write the build context.
 */
protected void writeBuildContext(StateImpl state, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeUTF("BuildContext"/*nonNLS*/);
	IImageContext ctx = state.getBuildContext();
	if (ctx == null) {
		out.writeInt(-1);
	}
	else {
		IPackage[] pkgs = ctx.getPackages();
		out.writeInt(pkgs.length);
		for (int i = 0; i < pkgs.length; ++i) {
			out.writeInt(pool.index(pkgs[i]));
		}
	}
}
/** 
 * Write the dependency graph.
 */
protected void writeDependencyGraph(StateImpl state, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeUTF("DependencyGraph"/*nonNLS*/);
	DependencyGraph graph = state.getInternalDependencyGraph();
	graph.integrityCheck();
	/**
	 * We only care about serializing JCU nodes, since they
	 * are the only nodes with dependency information.  All
	 * other nodes will be serialized indirectly as dependencies
	 * of JCU nodes.
	 * Do we care about Type/Zip/Namespace nodes that have no dependents???
	 */
	for (Enumeration e = graph.getJCUNodes(); e.hasMoreElements();) {
		JCUNode jcu = (JCUNode) e.nextElement();
		out.writeBoolean(true);
		SourceEntry sEntry = state.getSourceEntry(jcu.getPackageElement());
		if (sEntry == null) {
			System.out.println("Warning: StatSnap: Attempt to serialize dependency graph node for missing JCU: "/*nonNLS*/ + jcu.getPackageElement() + ". Skipping..."/*nonNLS*/);
		} else {
			Assert.isNotNull(sEntry);
			out.writeInt(pool.index(sEntry));
			IType[] types = jcu.getTypes();
			Assert.isTrue(types.length < 256);
			out.writeByte(types.length);
			for (int i = 0; i < types.length; ++i) {
				out.writeInt(pool.index(types[i]));
			}

			// Write dependencies
			INode[] dependencies = jcu.getDependencies();
			int numDep = dependencies.length;
			Assert.isTrue(numDep < 65536);
			out.writeShort(numDep);
			for (int i = 0; i < numDep; ++i) {
				INode dep = dependencies[i];
				switch (dep.getKind()) {
					case INode.JCU_NODE :
						{
							PackageElement element = ((JCUNode) dep).getPackageElement();
							SourceEntry depEntry = state.getSourceEntry(element);
							out.writeInt(pool.index(depEntry));
							break;
						}
					case INode.TYPE_NODE :
						{
							PackageElement element = ((TypeNode) dep).getPackageElement();
							SourceEntry depEntry = state.getSourceEntry(element);
							out.writeInt(pool.index(depEntry));
							break;
						}
					case INode.NAMESPACE_NODE :
						{
							IPackage pkg = ((NamespaceNode) dep).getPackage();
							out.writeInt(pool.index(pkg));
							break;
						}
					case INode.ZIP_NODE :
						{
							IPath path = ((ZipNode) dep).getZipFile();
							out.writeInt(pool.index(path));
							break;
						}
					default :
						badFormat();
				}
			}
		}
	}
	out.writeBoolean(false);
	writeEmptyAttributes(out);
}
/** 
 * Write an empty set of attributes.
 */
protected void writeEmptyAttributes(DataOutputStream out) throws IOException {
	out.writeShort(0);
}
/** 
 * Write the package map.
 */
protected void writePackageMap(StateImpl state, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeUTF("PackageMap"/*nonNLS*/);
	PackageMap map = state.getPackageMap();
	out.writeInt(map.size());
	int count = 0;
	for (Enumeration e = map.getAllPackages(); e.hasMoreElements(); ) {
		++count;
		IPackage pkg = (IPackage)e.nextElement();
		IPath[] fragments = map.getFragments(pkg);
		out.writeInt(pool.index(pkg));
		out.writeShort(fragments.length);
		for (int i = 0; i < fragments.length; ++i) {
			out.writeInt(pool.index(fragments[i]));
		}
	}
	Assert.isTrue(count == map.size()); // Sanity check
}
/** 
 * Write the principal structure table.
 */
protected void writePrincipalStructureTable(StateImpl state, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeUTF("PrincipalStructureTable"/*nonNLS*/);
	Hashtable table = state.getPrincipalStructureTable();
	int num = table.size();
	out.writeInt(num);
	int count = 0;
	for (Enumeration e = table.elements(); e.hasMoreElements();) {
		++count;
		TypeStructureEntry tsEntry = (TypeStructureEntry) e.nextElement();
		writeTypeStructureEntry(tsEntry, pool, out);
	}
	Assert.isTrue(count == num); // Sanity check
}
/** 
 * Write a problem.
 */
protected void writeProblem(ProblemDetailImpl pb, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeInt(pool.index(pb.getMessage()));
	out.writeInt(pb.getID());
	out.writeInt(pb.getSeverity());
	out.writeInt(pool.index(pb.getSourceEntry()));
	out.writeInt(pb.getStartPos());
	out.writeInt(pb.getEndPos());
	out.writeInt(pb.getLineNumber());
	writeEmptyAttributes(out);
}
/** 
 * Write the problem reporter.
 */
protected void writeProblemReporter(StateImpl state, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeUTF("Problems"/*nonNLS*/);
	IProblemReporter reporter = state.getProblemReporter();
	if (reporter instanceof ProblemTable) {
		out.writeBoolean(true);
		Vector problems = new Vector();
		for (Enumeration e = reporter.getAllProblems(); e.hasMoreElements();) {
			problems.addElement(e.nextElement());
		}
		for (Enumeration e = reporter.getImageProblems(); e.hasMoreElements();) {
			problems.addElement(e.nextElement());
		}
		out.writeInt(problems.size());
		for (Enumeration e = problems.elements(); e.hasMoreElements();) {
			writeProblem((ProblemDetailImpl) e.nextElement(), pool, out);
		}
	} else {
		out.writeBoolean(false);
		out.writeUTF(reporter.getClass().getName());
	}
}
/** 
 * Write the source element table.
 */
protected void writeSourceElementTable(StateImpl state, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeUTF("SourceElementTable"/*nonNLS*/);
	SourceElementTable table = state.getSourceElementTable();
	int num = table.numPackages();
	out.writeInt(num);
	for (Enumeration e = state.getPackageMap().getAllPackages(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		out.writeInt(pool.index(pkg));
		LookupTable entryTable = table.getPackageTable(pkg);
		out.writeInt(entryTable.size());
		for (Enumeration keys = entryTable.keys(); keys.hasMoreElements();) {
			String fileName = (String) keys.nextElement();
			out.writeInt(pool.index(fileName));
			SourceEntry sEntry = (SourceEntry) entryTable.get(fileName);
			out.writeInt(pool.index(sEntry));
		}
	}
}
/** 
 * Write a type structure entry.
 */
protected void writeTypeStructureEntry(TypeStructureEntry tsEntry, StateSnapConstantPool pool, DataOutputStream out) throws IOException {
	out.writeInt(pool.index(tsEntry.getSourceEntry()));
	out.writeInt(pool.index(tsEntry.getType()));
}
}
