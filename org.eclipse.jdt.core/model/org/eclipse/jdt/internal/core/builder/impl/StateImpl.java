package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.*;

import org.eclipse.core.resources.*;
import org.eclipse.jdt.internal.core.builder.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.core.IClassFile;

import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.lookup.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.compiler.ClassFile;

/**
 * The concrete representation of a built state.
 *
 * @see IState
 */
public class StateImpl implements IState {

	/**
	 * The development context corresponding to this state
	 */
	private JavaDevelopmentContextImpl fDevelopmentContext;

	/**
	 * The build context.  Only packages in the build context
	 * are actually built
	 */
	private IImageContext fBuildContext;

	/**
	 * The project built by this state.
	 */
	private IProject fProject;

	/**
	 * The name of the project built by this state.
	 */
	private String fProjectName;

	/**
	 * The paths of the package fragment roots in the class path.
	 */
	private IPackageFragmentRoot[] fPackageFragmentRootsInClassPath;

	/**
	 * The binary output.
	 */
	private BinaryOutput fBinaryOutput;

	/**
	 * The package map.  A hashtable of package handles to PackageMapEntry objects.
	 * The package map entries store the collection of package fragments that
	 * make up the given builder package.
	 */
	private PackageMap fPackageMap;

	/**
	 * The path map.  A table that maps from paths to package handles.  This
	 * is essentially a reverse index of the package map.  Note that this
	 * table can always be regenerated from the package map, so it does
	 * not need to be serialized or incrementally updated.
	 */
	private PathMap fPathMap;

	/**
	 * The source element table.  This table holds a source fragment for
	 * all workspace elements.
	 */
	private SourceElementTable fSourceElementTable;

	/**
	 * The principal structure table.  A table of type handles to TypeStructureEntry objects.
	 * This is where build results are stored.  This table only contains types that
	 * have been compiled.
	 */
	private Hashtable fPrincipalStructureTable;

	/**
	 * Table of IPackage to TypeStructureEntry[] for all types in package (lazy).
	 */
	private Hashtable fPrincipalStructureByPackageTable;

	/**
	 * The problem reporter.  All problems for this state are stored in this problem
	 * reporter.
	 */
	private IProblemReporter fProblemReporter;

	/**
	 * The graph of source element dependencies, used for
	 * incremental compilation
	 */
	private DependencyGraph fGraph= null;

	/**
	 * The table of subtypes.  Maps from IType to TypeStructureEntry[].  Absence in table implies no subtypes.
	 */
	private Hashtable fSubtypesTable;
	private IImageContext fSubtypesTableImageContext;

	/**
	 * The image corresponding to this state
	 */
	private IImage fImage;

	/**
	 * The compiler options that were used to build this state.
	 */
	private ConfigurableOption[] fCompilerOptions;

	/**
	 * Unique state number
	 */
	private int fStateNumber;

	/**
	 * Fingerprint bytes
	 */
	private byte[] fFingerprint;

	/* primitive type handles */
	final IType fVoidType;
	final IType fIntType;
	final IType fByteType;
	final IType fCharType;
	final IType fDoubleType;
	final IType fFloatType;
	final IType fLongType;
	final IType fShortType;
	final IType fBooleanType;

	/**
	 * Counter for unique state numbers (not the fingerprint).
	 */
	private static int fgStateCounter= 0;

	/**
	 * Namespace flag indicating CU has parse error.
	 * Must not conflict with modifiers or other flags in IConstants.
	 */
	private static final int F_HAS_PARSE_ERROR= 0x10000000;

	/**
	 * Name for namespace node representing unknown dependencies.
	 */
	private static final String UNKNOWN_DEPENDENCIES= "$UNKNOWN_DEPENDENCIES$"/*nonNLS*/;

	/**
	 * Random number generator, used for generating fingerprints.
	 */
	private static final Random fgRandom= new Random();

	/**
	 * State constructor comment.  The build context, fingerprint, and internal tables 
	 * are not instantiated and must be filled in before use.
	 */
	protected StateImpl(JavaDevelopmentContextImpl dc, IProject project) {
		fDevelopmentContext= dc;
		fProject= project;
		fStateNumber= fgStateCounter++;
		fImage= new ImageImplSWH(this, (ImageImpl) dc.getImage());
		fVoidType= new PrimitiveTypeHandleImplSWH(this, dc.fVoidType);
		fIntType= new PrimitiveTypeHandleImplSWH(this, dc.fIntType);
		fByteType= new PrimitiveTypeHandleImplSWH(this, dc.fByteType);
		fCharType= new PrimitiveTypeHandleImplSWH(this, dc.fCharType);
		fDoubleType= new PrimitiveTypeHandleImplSWH(this, dc.fDoubleType);
		fFloatType= new PrimitiveTypeHandleImplSWH(this, dc.fFloatType);
		fLongType= new PrimitiveTypeHandleImplSWH(this, dc.fLongType);
		fShortType= new PrimitiveTypeHandleImplSWH(this, dc.fShortType);
		fBooleanType= new PrimitiveTypeHandleImplSWH(this, dc.fBooleanType);
		fFingerprint= generateFingerprint();
	}
	/**
	 * State constructor comment.
	 */
	protected StateImpl(JavaDevelopmentContextImpl dc, IProject project, IImageContext buildContext) {
		this(dc, project);
		fBuildContext= buildContext;

		/* state tables */
		fPackageMap= new PackageMap();
		fSourceElementTable= new SourceElementTable();
		fPrincipalStructureTable= new Hashtable();
		fProblemReporter= new ProblemTable();
		fGraph= new DependencyGraph();
	}
	/** 
	 * @see IState
	 */
	public IImageBuilder applySourceDelta(IProject newProject, IResourceDelta projectDelta, IImageContext buildContext) {
		/* create and return builder */
		IncrementalImageBuilder builder= new IncrementalImageBuilder(this, newProject, buildContext);
		builder.applySourceDelta(projectDelta);
		return builder;
	}
	/**
	 * Creates state tables that are dependent only on the workspace, and not
	 * on build results.  This includes the package map, source element table,
	 * and the namespace table.
	 */
	protected void buildInitialPackageMap() {
		fPackageMap= new PackageMap();

		/* do for each package fragment root in (classpath INTERSECT workspace) */
		try {
			IPackageFragmentRoot[] roots= getPackageFragmentRootsInClassPath();
			IPath outputLocation= getJavaProject().getOutputLocation();
			for (int i= 0; i < roots.length; ++i) {
				PackageFragmentRoot root= (PackageFragmentRoot)roots[i];
				if (root.exists0()) {
					root.refreshChildren();
					IJavaElement[] frags= root.getChildren();
					for (int j= 0; j < frags.length; ++j) {
						IPackageFragment frag= (IPackageFragment) frags[j];
//						if (frag.exists()) {
							String pkgName= frag.getElementName();
							IPackage pkg= pkgName.length() == 0 ? defaultPackageForProject() : fDevelopmentContext.getImage().getPackageHandle(pkgName, false);
							IPath path= root.isArchive() ? root.getPath() : frag.getUnderlyingResource().getFullPath();
							fPackageMap.putFragment(pkg, path);
//						}
					}
				}
			}
		} catch (JavaModelException e) {
			throw internalException(e);
		}

		/* build the reverse index -- the path map */
		fPathMap= new PathMap(fPackageMap);
	}
	protected void buildPrincipalStructureByPackageTable() {
		IPackage[] pkgs= fBuildContext != null ? fBuildContext.getPackages() : fPackageMap.getAllPackagesAsArray();
		Hashtable principalStructureByPackageTable= new Hashtable(pkgs.length * 2 + 1);
		for (int i= 0; i < pkgs.length; ++i) {
			principalStructureByPackageTable.put(pkgs[i], new Vector(20));
		}
		for (Enumeration e= fPrincipalStructureTable.elements(); e.hasMoreElements();) {
			TypeStructureEntry tsEntry= (TypeStructureEntry) e.nextElement();
			IPackage pkg= tsEntry.getType().getPackage();
			Vector v= (Vector) principalStructureByPackageTable.get(pkg);
			/* Be careful to only gather types for packages in the given list.
			   Other packages may only be partially built. */
			if (v != null) {
				v.addElement(tsEntry);
			}
		}
		/* Convert vectors to arrays */
		for (Enumeration e= principalStructureByPackageTable.keys(); e.hasMoreElements();) {
			IPackage pkg= (IPackage) e.nextElement();
			Vector v= (Vector) principalStructureByPackageTable.get(pkg);
			TypeStructureEntry[] tsEntries= new TypeStructureEntry[v.size()];
			v.copyInto(tsEntries);
			principalStructureByPackageTable.put(pkg, tsEntries);
		}
		setPrincipalStructureByPackageTable(principalStructureByPackageTable);
	}
	/**
	 * Returns the type structure entry for the given type handle.  Performs lazy
	 * analysis and compilation as necessary.  Throws
	 * a NotPresentException if no element exists in the workspace that 
	 * matches the given handle.
	 */
	protected TypeStructureEntry buildTypeStructureEntry(IType handle) throws NotPresentException {
		TypeStructureEntry tsEntry= getTypeStructureEntry(handle, true);
		if (tsEntry == null) {
			throw new NotPresentException();
		}
		return tsEntry;
	}
	/**
	 * Canonicalize a package handle.
	 */
	protected IPackage canonicalize(IPackage pkg) {
		PackageMapEntry entry= fPackageMap.getEntry(pkg);
		return entry != null ? entry.getPackage() : pkg;
	}
	/**
	 * Canonicalize a type handle. 
	 */
	protected IType canonicalize(IType type) {
		TypeStructureEntry tsEntry= (TypeStructureEntry) fPrincipalStructureTable.get(type);
		if (tsEntry != null) {
			return tsEntry.getType();
		} else {
			return type;
		}
	}
	/**
	 * Canonicalizes the build context of this state through the package map.
	 */
	protected void canonicalizeBuildContext() {
		fBuildContext= canonicalizeBuildContext(fBuildContext);
	}
	/**
	 * Returns a new build context that contains package handles
	 * that have been canonicalized in this state.
	 */
	protected IImageContext canonicalizeBuildContext(IImageContext oldContext) {
		if (oldContext == null) {
			return null;
		}
		IPackage[] oldPackages= oldContext.getPackages();
		int pkgCount= oldPackages.length;
		IPackage[] newPackages= new IPackage[pkgCount];

		/* canonicalize packages through package map */
		for (int i= 0; i < pkgCount; i++) {
			newPackages[i]= canonicalize(oldPackages[i]);
		}

		return new ImageContextImpl(fDevelopmentContext, newPackages);
	}
	/**
	 * Clean up after a build.
	 */
	protected void cleanUp() {
		//resetProject(); - no longer clear the project back pointer
	}
	/**
	 * Adds all binary broker keys still in use by this state 
	 * to the given set.
	 */
	protected void collectBinaryBrokerKeys(Hashtable keysInUse) {
		for (Enumeration e= fPrincipalStructureTable.elements(); e.hasMoreElements();) {
			TypeStructureEntry tsEntry= (TypeStructureEntry) e.nextElement();
			if (!tsEntry.isBinary()) {
				int crc= tsEntry.getCRC32();
				if (crc != 0) {
					IType type= tsEntry.getType();
					BinaryBrokerKey key= new BinaryBrokerKey(type, crc);
					keysInUse.put(key, key);
				}
			}
		}
	}
	/**
	 * Returns true if the given source entry contributes to the state, false otherwise.
	 */
	protected boolean contains(SourceEntry sEntry) {
		IPackage pkg= packageFromSourceEntry(sEntry);
		return getSourceElementTable().getSourceEntry(pkg, sEntry.getFileName()) != null;
	}
	/**
	 * Converts a compilation result from the compiler's representation
	 * to the builder's representation.  This version is to be called
	 * by the batch builder.  The goal is to be able to accomplish
	 * this without having to parse the binary for principal structure
	 * information.
	 */
	protected ConvertedCompilationResult convertCompilationResult(CompilationResult result, IPackage defaultPackage) {
		String fileName= new String(result.getFileName());
		SourceEntry sEntry= SourceEntry.fromPathWithZipEntryName(fileName);
		PackageElement resultUnit= packageElementFromSourceEntry(sEntry);

		/**
		 * Make sure the CU exists.  May be null if unit is unavailable 
		 * (e.g. package is not included in package map due to class path omission).
		 * If this is the case, we shouldn't have reached this point.
		 */
		IPackage resultPkg= resultUnit.getPackage();
		IProblem[] compilerProblems= result.getProblems();
		Vector vProblems= new Vector(compilerProblems == null ? 0 : compilerProblems.length);

		/* convert type names to type handles for the produced types */
		ClassFile[] classFiles= result.getClassFiles();
		Vector vTSEntries= new Vector(classFiles.length);
		boolean reportedPackageConflict= false;
		for (int i= 0; i < classFiles.length; ++i) {
			ClassFile classFile= classFiles[i];
			String className= Util.toString(classFile.getCompoundName());
			if (classFile == null) {
				// Could not discover principal structure
				String msg= Util.bind("build.errorParsingBinary"/*nonNLS*/, className);
				ProblemDetailImpl problem= new ProblemDetailImpl(msg, sEntry);
				vProblems.addElement(problem);
				// skip it
				continue;
			}
			IType typeHandle= typeNameToHandle(resultPkg, className);
			IPackage typePkg= typeHandle.getPackage();
			if (!resultPkg.equals(typePkg)) {
				if (!reportedPackageConflict) {
					// Fix for 1FW88LE: ITPJCORE:WIN2000 - What does the error mean (package declaration/package)
					// and 1FW88DS: ITPJUI:WIN2000 - Go to file from task doesn't show line in editor
					IPath path= sEntry.getPath().removeLastSegments(1);
					if (!resultPkg.isUnnamed()) {
						path= path.removeLastSegments(new Path(resultPkg.getName().replace('.', IPath.SEPARATOR)).segmentCount());
					}
					if (!typePkg.isUnnamed()) {
						path= path.append(typePkg.getName().replace('.', IPath.SEPARATOR));
					}
					String msg= Util.bind("build.packageMismatch"/*nonNLS*/, path.toString());
					ProblemDetailImpl problem= new ProblemDetailImpl(msg, 0, IProblemDetail.S_ERROR, sEntry, 0, 0, 1);
					vProblems.addElement(problem);
					// Only report the conflict once (there may be several types, but there's only one package declaration).
					reportedPackageConflict= true;
				}
				// toss type result
				continue;
			}
			TypeStructureEntry tsEntry= new TypeStructureEntry(sEntry, typeHandle);

			/* squirrel the binary away */
			byte[] binary= classFile.getBytes();
			// as a side effect, the following sets the crc32 for the type structure entry
			getBinaryOutput().putBinary(tsEntry, binary);
			vTSEntries.addElement(tsEntry);
		}
		TypeStructureEntry[] tsEntries= new TypeStructureEntry[vTSEntries.size()];
		vTSEntries.copyInto(tsEntries);

		/* convert dependencies */
		Vector dependencies= resolveDependencies(resultUnit, result);

		/* convert problems */
		if (compilerProblems != null) {
			for (int i= 0; i < compilerProblems.length; i++) {
				// The problem factory created the compiler problems as ProblemDetailImpl objects
				// without the source entry set.  Fill it in here.
				ProblemDetailImpl problem= (ProblemDetailImpl) compilerProblems[i];
				problem.setSourceEntry(sEntry);
				vProblems.addElement(problem);
			}
		}
		IProblemDetail[] problems= new IProblemDetail[vProblems.size()];
		vProblems.copyInto(problems);
		return new ConvertedCompilationResult(resultUnit, dependencies, problems, tsEntries);
	}
	/** 
	 * Returns a copy of the state with the given workspace and build context.
	 */
	protected StateImpl copy(IProject newProject, IImageContext context) {
		StateImpl newState= new StateImpl(this.fDevelopmentContext, newProject);
		newState.fPackageMap= this.fPackageMap.copy();
		newState.fPathMap= this.fPathMap;
		newState.fSourceElementTable= this.fSourceElementTable.copy();
		newState.fPrincipalStructureTable= (Hashtable) this.fPrincipalStructureTable.clone();
		newState.fProblemReporter= this.fProblemReporter.copy();
		newState.fGraph= this.fGraph.copy();
		newState.fBuildContext= context;
		newState.fCompilerOptions= this.fCompilerOptions;
		return newState;
	}
	/**
	 * Returns the default package which is visible by the given source entry,
	 * or null if there is no visible default package.
	 * Named projects should be able to see the default package
	 * in the same project, if any.
	 * See 1PQ9DWH: LFRE:ALL - default package not found in name lookup.
	 */
	protected IPackage defaultPackageFor(IPackage pkg) {
		return pkg.isUnnamed() ? pkg : defaultPackageForProject();
	}
	/**
	 * Returns the default package for the project.
	 */
	protected IPackage defaultPackageForProject() {
		return fDevelopmentContext.getImage().getPackageHandle(PackageImpl.DEFAULT_PACKAGE_PREFIX + getProject().getName(), true);
	}
	/**
	 * The IBinaryType for this TypeStructureEntry has been flushed from
	 * the cache.  Rebuild the IBinaryType from binary.  May or may not force
	 * a lazy build, depending on the parameter lazyBuildCU.
	 */
	protected IBinaryType forceBinaryType(TypeStructureEntry tsEntry, boolean lazyBuildCU) {
		IType type= tsEntry.getType();
		SourceEntry sEntry= tsEntry.getSourceEntry();
		IBinaryType binaryType= null;

		/* if its a class file, get descriptor from binary index */
		if (sEntry.isBinary()) {
			try {
				byte[] bytes= getElementContentBytes(sEntry);
				binaryType= new ClassFileReader(bytes, sEntry.getPathWithZipEntryName().toCharArray());
			} catch (ClassFormatException e) {
				/* problem will be generated below */
			}
		} else {
			/* entry is source in workspace; get binary from broker */
			IType typeSS= (IType) type.inState(this);
			byte[] bytes= getBinary(typeSS, lazyBuildCU);
			if (bytes != null) {
				try {
					binaryType= new ClassFileReader(bytes, sEntry.getPathWithZipEntryName().toCharArray());
				} catch (ClassFormatException e) {
					/* problem will be generated below */
				}
			}
		}
		if (lazyBuildCU && binaryType == null) {
			/* couldn't parse the class file */
			ProblemDetailImpl problem= new ProblemDetailImpl(Util.bind("build.errorParsingBinary"/*nonNLS*/, type.getName()), sEntry);
			fProblemReporter.putProblem(sEntry, problem);
		}
		return binaryType;
	}
	/**
	 * Generate a unique fingerprint for a state.
	 */
	protected static byte[] generateFingerprint() {
		// TBD: Better to use a UUID, but we don't have a convenient implementation.
		// Chances are the fingerprint will be used only on one machine, so this should suffice.
		byte[] fingerprint= new byte[32];
		fgRandom.nextBytes(fingerprint);
		return fingerprint;
	}
	/**
	 * Returns an array of all packages in the state (non-state-specific handles).
	 */
	protected IPackage[] getAllPackagesAsArray() {
		return fPackageMap.getAllPackagesAsArray();
	}
	/**
	 * Returns an enumeration of TypeStructureEntry objects for all types 
	 * in the given package, including nested types.  Returns null if the package does not exist.
	 */
	protected TypeStructureEntry[] getAllTypesForPackage(IPackage pkg) {
		if (fPrincipalStructureByPackageTable == null) {
			fPrincipalStructureByPackageTable= new Hashtable(11);
		}
		Object o= fPrincipalStructureByPackageTable.get(pkg);
		if (o != null) {
			return (TypeStructureEntry[]) o;
		}
		if (!fPackageMap.containsPackage(pkg)) {
			return null;
		}

		// TBD: Doesn't support lazy builds.

		int max= 30;
		TypeStructureEntry[] list= new TypeStructureEntry[max];
		int count= 0;
		for (Enumeration e= fPrincipalStructureTable.elements(); e.hasMoreElements();) {
			TypeStructureEntry tsEntry= (TypeStructureEntry) e.nextElement();
			if (tsEntry.getType().getPackage().equals(pkg)) {
				if (count == max)
					System.arraycopy(list, 0, list= new TypeStructureEntry[max= max * 2], 0, count);
				list[count++]= tsEntry;
			}
		}
		if (count < max)
			System.arraycopy(list, 0, list= new TypeStructureEntry[count], 0, count);
		fPrincipalStructureByPackageTable.put(pkg, list);
		return list;
	}
	/**
	 * Returns the binary for a given type handle.  The type handle must
	 * be state specific.
	 * @param type The type handle to get binaries for
	 * @param lazyBuildCU whether or not to perform a lazy build to get the binary.
	 */
	protected byte[] getBinary(IType type, boolean lazyBuildCU) throws NotPresentException {
		if (!type.isStateSpecific()) {
			throw new StateSpecificException();
		}
		IType nssHandle= (IType) type.nonStateSpecific();
		TypeStructureEntry tsEntry= buildTypeStructureEntry(nssHandle);

		/* Attempt to retrieve binary from binary output */
		byte[] binary= getBinaryOutput().getBinary(tsEntry, nssHandle);
		if (binary != null)
			return binary;

		/*
		 * We have a built entry, but couldn't get the bytes from the binary output.
		 * Need to recompile.
		 */
		if (!lazyBuildCU) {
			return null;
		}

		/* make sure the entry is a compilation unit */
		PackageElement unit= packageElementFromSourceEntry(tsEntry.getSourceEntry());
		new BatchImageBuilder(this).lazyBuild(unit);
		tsEntry= getTypeStructureEntry(nssHandle, false);
		if (tsEntry == null) {
			return null;
		} else {
			return getBinaryOutput().getBinary(tsEntry, nssHandle);
		}
	}
	/**
	 * Returns the binary output for this state.
	 */
	protected BinaryOutput getBinaryOutput() {
		BinaryOutput output= fDevelopmentContext.getBinaryOutput();
		if (output != null) {
			return output;
		} else {
			return fBinaryOutput;
		}
	}
	/**
	 * Returns the IBinaryType for the given type structure entry.
	 * Performs lazy builds as necessary.
	 * If there is no possible way of creating the IBinaryType (because
	 * blobs are not available or the class file is corrupt), a NotPresentException
	 * is thrown.
	 */
	protected IBinaryType getBinaryType(TypeStructureEntry tsEntry) throws NotPresentException {

		/* rebuild descriptor from indexes or binary */
		IBinaryType binaryType= forceBinaryType(tsEntry, false); // Use false for 1FVQGL1: ITPJCORE:WINNT - SEVERE - Error saving java file
		if (binaryType == null) {
			throw new NotPresentException(Util.bind("build.errorBuildingType"/*nonNLS*/, tsEntry.getSourceEntry().getFileName()));
		}
		return binaryType;
	}
	/**
	 * Returns the IBinaryType for the given type structure entry if it
	 * is available.  Parses the binary if necessary, but returns null if
	 * the binary is not available (doesn't invoke a lazy build)
	 */
	protected IBinaryType getBinaryTypeOrNull(TypeStructureEntry tsEntry) {

		/* rebuild descriptor from binary */
		return forceBinaryType(tsEntry, false);
	}
	/**
	 * Returns the ImageContext representing the subset of the
	 * image which is important to have built as early
	 * as possible.  Although all parts of the image can be navigated
	 * to and queried, possibly using other ImageContexts, the builder
	 * gives higher priority to maintaining the build context subset
	 * than to other parts of the image.
	 * 
	 * @see #applySourceDelta
	 */
	public IImageContext getBuildContext() {
		return fBuildContext;
	}
	/**
	 * Returns the projects mentioned in the class path, including the one for this state.
	 */
	public IProject[] getClassPathProjects() {
		Vector projects = new Vector();
		IPackageFragmentRoot[] roots = fPackageFragmentRootsInClassPath;
		for (int i = 0; i < roots.length; ++i) {
			IJavaProject javaProject = roots[i].getJavaProject();
			if (javaProject != null) {
				IProject project = javaProject.getProject();
				if (!projects.contains(project)) {
					projects.add(project);
				}
			}
		}
		IProject[] result = new IProject[projects.size()];
		projects.copyInto(result);
		return result;
	}
	/**
	 * Given a compilation unit name from the compiler, and the default package which
	 * was active when it was produced, answer the corresponding CompilationUnit.
	 */
	protected PackageElement getCompilationUnitFromName(String name, IPackage defaultPackage) {
		int i= name.lastIndexOf('.');
		IPackage pkg;
		String simpleName;
		if (i == -1) {
			pkg= defaultPackage;
			simpleName= name;
		} else {
			pkg= fDevelopmentContext.getImage().getPackageHandle(name.substring(0, i), false);
			simpleName= name.substring(i + 1);
		}
		pkg= canonicalize(pkg);
		if (!fSourceElementTable.containsPackage(pkg)) {
			getSourceEntries(pkg);
		}
		SourceEntry entry= fSourceElementTable.getSourceEntry(pkg, simpleName + ".java"/*nonNLS*/);
		if (entry == null)
			return null;
		return new PackageElement(pkg, entry);
	}
	/**
	 * Returns the compiler options used to build this state.
	 */
	public ConfigurableOption[] getCompilerOptions() {
		return fCompilerOptions;
	}
	/**
	 * Returns an enumeration of TypeStructureEntry objects for all top-level types 
	 * in the given package.  Returns null if the package does not exist.
	 */
	protected TypeStructureEntry[] getDeclaredTypesForPackage(IPackage pkg) {
		TypeStructureEntry[] all= getAllTypesForPackage(pkg);
		TypeStructureEntry[] declared= new TypeStructureEntry[all.length];
		int count= 0;
		for (int i= 0, len= all.length; i < len; ++i) {
			if (BinaryStructure.isPackageMember(getBinaryType(all[i]))) {
				declared[count++]= all[i];
			}
		}
		if (count < declared.length) {
			System.arraycopy(declared, 0, declared= new TypeStructureEntry[count], 0, count);
		}
		return declared;
	}
	/**
	 * Returns the dependency graph for this state, if supported,
	 * or null if the dependency graph is unknown.
	 */
	public IDependencyGraph getDependencyGraph() {
		return new DependencyGraphImpl(this);
	}
	/**
	 * Returns this state's development context.
	 */
	public IDevelopmentContext getDevelopmentContext() {
		return fDevelopmentContext;
	}
	/**
	 * Returns the byte contents for a source entry.
	 */
	protected byte[] getElementContentBytes(SourceEntry entry) {
		if (entry.fZipEntryFileName != null) {
			ZipFile zipFile= null;
			try {
				IPath path= entry.getPath();
				JavaModelManager manager= (JavaModelManager) JavaModelManager.getJavaModelManager();
				zipFile= manager.getZipFile(path);
				if (zipFile == null) {
					return new byte[0];
				}
				ZipEntry zipEntry= zipFile.getEntry(entry.getZipEntryName());
				if (zipEntry == null) {
					return new byte[0];
				}
				InputStream input= zipFile.getInputStream(zipEntry);
				if (input == null){
					return new byte[0];
				}
				byte[] contents= Util.readContentsAsBytes(input);
				if (contents == null) {
					return new byte[0];
				}
				return contents;
			} catch (CoreException e) {
				return new byte[0];
			} catch (IOException e) {
				return new byte[0];
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch(IOException e) {
						// ignore
					}
				}
			}
		} else {
			IFile file= getFile(entry);
			try {
				// Fix for 1FVTLHB: ITPCORE:WINNT - Importing a project does not import class files
				JavaModelManager.getJavaModelManager().ensureLocal(file);
				return Util.readContentsAsBytes(file.getContents(true));
			} catch (CoreException e) {
				return fDevelopmentContext.getBinaryFromFileSystem(file);
			} catch (IOException e) {
				String message= e.getMessage();
				message= (message == null ? "."/*nonNLS*/ : " due to "/*nonNLS*/ + message + "."/*nonNLS*/);
				return new byte[0];
			}
		}
	}
	/**
	 * Returns the contents for a source entry as a char array.
	 */
	protected char[] getElementContentCharArray(SourceEntry entry) {
		// TBD: need proper byte->char conversion
		byte[] bytes= getElementContentBytes(entry);
		BufferedReader reader = null;
		try {
			reader= new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
			int length= bytes.length;
			char[] contents= new char[length];
			int len= 0;
			int readSize= 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize= reader.read(contents, len, length - len);
			}
			reader.close();
			// See PR 1FMS89U
			// Now we need to resize in case the default encoding used more than one byte for each
			// character
			if (len != length)
				System.arraycopy(contents, 0, (contents= new char[len]), 0, len);
			return contents;
		} catch (IOException e) {
			if (reader != null) {
				try {
					reader.close();
				} catch(IOException ioe) {
				}
			}
			return new char[0];
		}
	}
	/**
	 * Returns the file for a workspace-relative path.
	 */
	protected IFile getFile(IPath path) {
		return getProject().getWorkspace().getRoot().getFile(makeAbsolute(path));
	}
	/**
	 * Returns the file for a source entry.
	 */
	protected IFile getFile(SourceEntry sEntry) {
		return getFile(sEntry.getPath());
	}
	/**
	 * Answer a unique fingerprint for this state.
	 * It is guaranteed that no other state can have the same 
	 * fingerprint.
	 * The result should not be modified.
	 */
	public byte[] getFingerprint() {
		return (byte[]) fFingerprint.clone(); // Trust no one.
	}
	/**
	 * Returns the folder for a project-relative path.
	 */
	protected IFolder getFolder(IPath path) {
		return getProject().getFolder(makeAbsolute(path));
	}
	/**
	 * Returns the image described by this state.  The result is state-specific. 
	 *
	 * @see IDevelopmentContext
	 * @see IDevelopmentContext#getImage
	 */
	public IImage getImage() {
		return fImage;
	}
	/**
	 * Returns the dependency graph for the state.  Used by the incremental builder.
	 */
	protected DependencyGraph getInternalDependencyGraph() {
		return fGraph;
	}
	/**
	 * Returns the Java Model element for a source entry.
	 */
	protected IJavaElement getJavaElement(SourceEntry sEntry) {
		try {
			JavaProject javaProject= (JavaProject)getJavaProject();
			String zipEntryFileName= sEntry.fZipEntryFileName;
			IPackageFragment frag= null;
			if (zipEntryFileName != null) {
				IPackageFragmentRoot root;
				IPath path = sEntry.getPath();
				if (!path.isAbsolute() || javaProject.getWorkspace().getRoot().findMember(path) != null) {
					root= javaProject.getPackageFragmentRoot(getFile(path));
				} else {
					root= javaProject.getPackageFragmentRoot(path.toOSString());
				}
				String zipEntryPath = sEntry.fZipEntryPath;
				String pkgName= zipEntryPath == null ? IPackageFragment.DEFAULT_PACKAGE_NAME : zipEntryPath.replace('/', '.');
				frag= root.getPackageFragment(pkgName);
			} else {
				IPackageFragmentRoot[] roots= getPackageFragmentRootsInClassPath();
				for (int i= 0; i < roots.length; ++i) {
					IPackageFragmentRoot root= roots[i];
					if (!root.isArchive() && root.exists()) {
						IPath rootPath= root.getUnderlyingResource().getFullPath();
						if (rootPath.isPrefixOf(sEntry.getPath())) {
							String pkgName= sEntry.getPath().removeLastSegments(1).removeFirstSegments(rootPath.segmentCount()).toString().replace('/', '.');
							frag= root.getPackageFragment(pkgName);
							break;
						}
					}
				}
			}
			if (frag == null) {
				throw internalException(Util.bind("build.missingFile"/*nonNLS*/, sEntry.toString()));
			}
			String fileName= sEntry.getPath().lastSegment();
			if (sEntry.isSource()) {
				return frag.getCompilationUnit(fileName);
			} else {
				if (zipEntryFileName != null) {
					return frag.getClassFile(zipEntryFileName);
				} else {
					return frag.getClassFile(fileName);
				}
			}
		} catch (JavaModelException e) {
			throw internalException(e);
		}
	}
	/**
	 * Returns the Java Model element for the project.
	 */
	protected IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}
	/**
	 * For debugging only.
	 */
	protected INode getNode(String qualifiedNameWithSuffix) {
		PackageElement element= getPackageElement(qualifiedNameWithSuffix);
		if (element == null)
			return null;
		return fGraph.getNodeFor(element, false);
	}
	/**
	 * Returns the output location for this state.
	 * The binary output must be a ProjectBinaryOutput.
	 */
	protected IPath getOutputLocation() {
		BinaryOutput output= getBinaryOutput();
		if (output instanceof ProjectBinaryOutput) {
			return ((ProjectBinaryOutput) output).getOutputPath();
		} else {
			return null;
		}
	}
static Comparator getPackageComparator() {
	return new Comparator() {
		public int compare(Object o1, Object o2) {
			IPackage p1 = (IPackage) o1;
			IPackage p2 = (IPackage) o2;
			return p1.getName().compareTo(p2.getName());
		}
	};
}
	/**
	 * For debugging only.
	 */
	protected PackageElement getPackageElement(String qualifiedNameWithSuffix) {
		SourceEntry sEntry= getSourceEntry(qualifiedNameWithSuffix);
		if (sEntry == null)
			return null;
		return packageElementFromSourceEntry(sEntry);
	}
	/**
	 * Returns the package fragment roots to build in classpath order.
	 */
	protected IPackageFragmentRoot[] getPackageFragmentRootsInClassPath() {
		return fPackageFragmentRootsInClassPath;
	}
	/**
	 * Returns the package map
	 */
	protected PackageMap getPackageMap() {
		return fPackageMap;
	}
static Comparator getPathComparator() {
	return new Comparator() {
		public int compare(Object o1, Object o2) {
			IPath p1 = (IPath) o1;
			IPath p2 = (IPath) o2;
			return p1.toString().compareTo(p2.toString());
		}
	};
}
	/**
	 * Returns the path map.
	 */
	protected PathMap getPathMap() {
		return fPathMap;
	}
	/**
	 * Returns the principal structure by package table.
	 */
	protected Hashtable getPrincipalStructureByPackageTable() {
		if (fPrincipalStructureByPackageTable == null) {
			fPrincipalStructureByPackageTable= new Hashtable(11);
		}
		return fPrincipalStructureByPackageTable;
	}
	/**
	 * Returns the principal structure table
	 */
	protected Hashtable getPrincipalStructureTable() {
		return fPrincipalStructureTable;
	}
	/**
	 * Returns the problem reporter
	 */
	protected IProblemReporter getProblemReporter() {
		return fProblemReporter;
	}
	/**
	 * Returns the problems that were reported against the image itself
	 */
	protected Enumeration getProblems() {
		return fProblemReporter.getImageProblems();
	}
	/**
	 * For debugging only.
	 */
	protected Vector getProblems(String qualifiedNameWithSuffix) {
		SourceEntry sEntry= getSourceEntry(qualifiedNameWithSuffix);
		return fProblemReporter.getProblemVector(sEntry);
	}
	/**
	 * @see IState
	 */
	public IProject getProject() {
		return fProject;
	}
	/**
	 * Returns the name of the project built by this state.
	 */
	protected String getProjectName() {
		if (fProjectName == null)
			fProjectName= getProject().getName();
		return fProjectName;
	}
	/**
	 * Returns an array of Package objects representing all other
	 * packages which this package directly references.
	 * This is the union of all packages directly referenced by all 
	 * classes and interfaces in this package, including packages
	 * mentioned in import declarations.
	 * <p>
	 * A direct reference in source code is a use of a package's
	 * name other than as a prefix of another package name.
	 * For example, 'java.lang.Object' contains a direct reference
	 * to the package 'java.lang', but not to the package 'java'.
	 * Also note that every package that declares at least one type
	 * contains a direct reference to java.lang in virtue of the
	 * automatic import of java.lang.*.
	 * The result does not include this package (so contrary to the note
	 * above, the result for package java.lang does not include java.lang).
	 * In other words, the result is non-reflexive and typically
	 * non-transitive.
	 * <p>
	 * The resulting packages may or may not be present in the image,
	 * since the classes and interfaces in this package may refer to missing
	 * packages.
	 * The resulting packages are in no particular order.
	 */
	protected IPackage[] getReferencedPackages(IPackage pkgHandle) {

		/* set of referenced builder packages */
		Hashtable pkgTable= getTableOfReferencedPackages(pkgHandle);

		/* convert to array and return */
		IPackage[] results= new IPackage[pkgTable.size()];
		int i= 0;
		for (Enumeration e= pkgTable.elements(); e.hasMoreElements(); i++) {
			results[i]= (IPackage) e.nextElement();
		}
		return results;
	}
	/**
	 * Returns the references for the given package element.  Returns null
	 * if the references could not be found.
	 */
	protected ReferenceInfo getReferencesForPackageElement(PackageElement builderUnit) {
		if (!builderUnit.isSource()) {
			return null;
		}
		SourceEntry sEntry= getSourceEntry(builderUnit);
		IJavaElement javaElement= getJavaElement(sEntry);
		ICompilationUnit unit= (ICompilationUnit) javaElement;
		try {
			return ((CompilationUnit)unit).getReferenceInfo();
		} catch (JavaModelException e) {
			return null;
		}
	}
	/**
	 * Returns an array of Package objects representing all packages
	 * in the given image context which directly reference this package.
	 * The result does not include this package.
	 * In other words, the result is non-transitive and non-reflexive.
	 * <p>
	 * The intersection of all packages in the image and those in the
	 * image context are considered, so the resulting packages are 
	 * guaranteed to be present in the image.
	 * The resulting packages are in no particular order.
	 *
	 * This is an extremely slow implementation (n^3?).  Avoid using it if possible.
	 */
	protected IPackage[] getReferencingPackages(IPackage pkgHandle, IImageContext context) {

		/* the results */
		Vector vResults= new Vector();
		IImage image= fDevelopmentContext.getImage();

		/* do for each package in the image context */
		IPackage[] pkgs= context.getPackages();
		for (int i= 0; i < pkgs.length; i++) {
			/* skip the package we are looking for */
			if (pkgs[i].equals(pkgHandle)) {
				continue;
			}
			Hashtable table= getTableOfReferencedPackages(pkgs[i]);
			/* if the package references this package */
			if (table.contains(pkgHandle)) {
				/* add it to results */
				vResults.addElement(pkgs[i]);
			}
		}
		IPackage[] results= new IPackage[vResults.size()];
		vResults.copyInto(results);
		return results;
	}
	/**
	 * Returns a report card for this state., restricted to
	 * the given image context.
	 * Problems are organized by workspace element identifiers.
	 * This method is on <code>IState</code> rather than 
	 * <code>IImage</code> to make it clear that
	 * the result is inherently state-specific.
	 * @param imageContext the image context in which to 
	 *    restrict the report card.
	 */
	public IReportCard getReportCard(IImageContext imageContext) {
		return new ReportCardImpl(this, imageContext);
	}
	/**
	 * Iterates through the children of a package fragment and adds all visible source element entries to the table.
	 */
	protected void getSourceElementEntries(IPackage pkg, IPath path, LookupTable entryTable) {
		try {
			IPackageFragmentRoot root= null;
			IPackageFragment frag= null;
			boolean isDefault= pkg.isUnnamed();
			String pkgName= isDefault ? IPackageFragment.DEFAULT_PACKAGE_NAME : pkg.getName();
			String pkgPath= pkgName.replace('.', '/');
			if (isZipElement(path)) {
				IResource member= null;
				if (!path.isAbsolute() || getProject().getWorkspace().getRoot().findMember(path) != null) {
					root= getJavaProject().getPackageFragmentRoot(getFile(path));
				} else {
					root= getJavaProject().getPackageFragmentRoot(path.toOSString());
				}
				frag= root.getPackageFragment(pkgName);
			} else {
				IPackageFragmentRoot[] roots= getPackageFragmentRootsInClassPath();
				for (int i= 0; i < roots.length; ++i) {
					IPackageFragmentRoot testRoot= roots[i];
					if (!testRoot.isArchive() && testRoot.getUnderlyingResource().getFullPath().isPrefixOf(path)) {
						root= testRoot;
						frag= testRoot.getPackageFragment(pkgName);
						break;
					}
				}
			}
			boolean isArchive= root.isArchive();
			if (!isArchive && !root.exists()) {
				return;
			}
			if (isArchive && root.getUnderlyingResource() != null && !root.getUnderlyingResource().isLocal(IResource.DEPTH_ZERO)) {
				return;
			}
			if (frag == null || !frag.exists()) {
				return;
			}
			IPath entryPath= null;
			String zipEntryPath = null;
			String zipEntryFileName= null;
			((PackageFragment) frag).refreshChildren();
			ICompilationUnit[] units= frag.getCompilationUnits();
			for (int i= 0; i < units.length; ++i) {
				ICompilationUnit unit= units[i];
				String fileName= unit.getElementName();
				// get the corresponding .class file name
				String classFileName = ""/*nonNLS*/;
				if (Util.isJavaFileName(fileName)) { // paranoia check
					classFileName = fileName.substring(0, fileName.length()-5).concat(".class"/*nonNLS*/);
				}
				// see if a source entry exists for this file name
				// or for the corresponding .class file
				if (entryTable.get(fileName) == null && entryTable.get(classFileName) == null) {
					if (isArchive) {
						entryPath= path;
						zipEntryPath = isDefault || pkgPath.length() == 0 ? null : pkgPath;
						zipEntryFileName= fileName;
					} else {
						entryPath= unit.getUnderlyingResource().getFullPath();
						zipEntryPath = null;
						zipEntryFileName= null;
					}
					SourceEntry sEntry= new SourceEntry(entryPath, zipEntryPath, zipEntryFileName);
					entryTable.put(fileName, sEntry);
				}
			}
			IClassFile[] classFiles= frag.getClassFiles();
			for (int i= 0; i < classFiles.length; ++i) {
				IClassFile classFile= classFiles[i];
				String fileName= classFile.getElementName();
				// get the corresponding .java file name
				// note: this handles nested types, but not secondary types (e.g. class B defined in A.java)
				String javaFileName = ""/*nonNLS*/;
				if (Util.isClassFileName(fileName)) { // paranoia check
					// strip off any nested types
					javaFileName = fileName.substring(0, fileName.length()-6);
					int dol = javaFileName.indexOf('$');
					if (dol != -1) {
						javaFileName = javaFileName.substring(0, dol);
					}
					javaFileName = javaFileName.concat(".java"/*nonNLS*/);
				}
				// see if a source entry exists for this file name
				// or for the corresponding .java file
				if (entryTable.get(fileName) == null && entryTable.get(javaFileName) == null) {
					if (isArchive) {
						entryPath= path;
						zipEntryPath = isDefault || pkgPath.length() == 0 ? null : pkgPath;
						zipEntryFileName= fileName;
					} else {
						if (!classFile.getUnderlyingResource().isLocal(IResource.DEPTH_ZERO))
							continue;
						entryPath= classFile.getUnderlyingResource().getFullPath();
						zipEntryPath = null;
						zipEntryFileName= null;
					}
					SourceEntry sEntry= new SourceEntry(entryPath, zipEntryPath, zipEntryFileName);
					entryTable.put(fileName, sEntry);
				}
			}
		} catch (JavaModelException e) {
			throw internalException(e);
		}
	}
	/**
	 * Returns the source element table
	 */
	protected SourceElementTable getSourceElementTable() {
		return fSourceElementTable;
	}
	/**
	 * Returns the source entries in the given package.  Does lazy analysis
	 * of source entry table as necessary.  Returns null if the package is not present.
	 */
	protected SourceEntry[] getSourceEntries(IPackage pkg) {
		SourceEntry[] entries= fSourceElementTable.getSourceEntries(pkg);
		if (entries != null) {
			return entries;
		}

		/* Need to build the table for the package */

		/* go through package fragments and compute all source entries */
		IPath[] frags= fPackageMap.getFragments(pkg);
		if (frags == null) {
			return null; // package not present
		}

		/* build a table of source entries, keyed by filename */
		LookupTable entryTable= new LookupTable(20);
		for (int i= 0; i < frags.length; i++) {
			getSourceElementEntries(pkg, frags[i], entryTable);
		}
		fSourceElementTable.putPackageTable(pkg, entryTable);
		return fSourceElementTable.getSourceEntries(pkg);
	}
	/**
	 * For debugging only.
	 */
	protected SourceEntry getSourceEntry(String qualifiedNameWithSuffix) {
		int dot= qualifiedNameWithSuffix.lastIndexOf('.');
		dot= qualifiedNameWithSuffix.lastIndexOf('.', dot - 1);
		String pkgName= (dot == -1 ? ".default"/*nonNLS*/ : qualifiedNameWithSuffix.substring(0, dot));
		String fileName= (dot == -1 ? qualifiedNameWithSuffix : qualifiedNameWithSuffix.substring(dot + 1));
		IPackage pkg= fDevelopmentContext.getImage().getPackageHandle(pkgName, false);
		getSourceEntries(pkg); // force
		return fSourceElementTable.getSourceEntry(pkg, fileName);
	}
	/**
	 * Looks up and returns the source entry for a source element in the source element table.
	 * Returns null if the type does not exist.
	 */
	protected SourceEntry getSourceEntry(PackageElement element) {
		IPackage pkg= element.getPackage();
		// lazy build if necessary
		if (!fSourceElementTable.containsPackage(pkg)) {
			getSourceEntries(pkg);
		}
		return fSourceElementTable.getSourceEntry(pkg, element.getFileName());
	}
	/**
	 * Returns the source entry for a type.  Returns null if the type
	 * does not exist
	 */
	protected SourceEntry getSourceEntry(IType type) {
		IPackage pkg= type.getPackage();
		// lazy build if necessary
		if (!fSourceElementTable.containsPackage(pkg)) {
			getSourceEntries(pkg);
		}
		String simpleName= type.getSimpleName();
		SourceEntry entry= fSourceElementTable.getSourceEntry(pkg, simpleName + ".java"/*nonNLS*/);
		if (entry == null) {
			entry= fSourceElementTable.getSourceEntry(pkg, simpleName + ".class"/*nonNLS*/);
			if (entry == null) {
				int firstDollar= simpleName.indexOf('$');
				if (firstDollar != -1) {
					simpleName= simpleName.substring(0, firstDollar);
					entry= fSourceElementTable.getSourceEntry(pkg, simpleName + ".java"/*nonNLS*/);
					if (entry == null) {
						entry= fSourceElementTable.getSourceEntry(pkg, simpleName + ".class"/*nonNLS*/);
					}
				}
			}
		}
		return entry;
	}
	/**
	 * Returns the table of subtypes which covers the given image context.
	 * All types in the table are state-specific
	 */
	protected Hashtable getSubtypesTable(IImageContext imageContext) {
		if (fSubtypesTable != null) {
			if (imageContext == null) {
				if (fSubtypesTableImageContext == null) {
					return fSubtypesTable;
				}
			} else {
				if (fSubtypesTableImageContext == null || ((ImageContextImpl) imageContext).isSubsetOf((ImageContextImpl) fSubtypesTableImageContext)) {
					return fSubtypesTable;
				}
			}
		}
		IPackage[] pkgs= (imageContext == null ? fPackageMap.getAllPackagesAsArray() : imageContext.getPackages());
		Hashtable table= new Hashtable(Math.max(pkgs.length * 5, 1));
		for (int i= 0; i < pkgs.length; ++i) {
			IPackage pkg= pkgs[i];
			TypeStructureEntry[] tsEntries= getAllTypesForPackage(pkg);
			if (tsEntries != null) {
				for (int j= 0; j < tsEntries.length; ++j) {
					IType type= (IType) tsEntries[j].getType().inState(this);
					if (!type.isInterface()) {
						IType superclass= type.getSuperclass();
						if (superclass != null) {
							Vector vSubtypes= (Vector) table.get(superclass);
							if (vSubtypes == null) {
								vSubtypes= new Vector(5);
								table.put(superclass, vSubtypes);
							}
							vSubtypes.addElement(type);
						}
					}
					IType[] interfaces= type.getInterfaces();
					for (int k= 0; k < interfaces.length; ++k) {
						IType intf= interfaces[k];
						Vector vSubtypes= (Vector) table.get(intf);
						if (vSubtypes == null) {
							vSubtypes= new Vector(5);
							table.put(intf, vSubtypes);
						}
						vSubtypes.addElement(type);
					}
				}
			}
		}
		for (Enumeration e= table.keys(); e.hasMoreElements();) {
			IType type= (IType) e.nextElement();
			Vector vSubtypes= (Vector) table.get(type);
			IType[] subtypes= new IType[vSubtypes.size()];
			vSubtypes.copyInto(subtypes);
			table.put(type, subtypes);
		}
		fSubtypesTable= table;
		fSubtypesTableImageContext= imageContext;
		return table;
	}
	/**
	 * Returns a hashtable of IPackage objects representing all other
	 * packages which this package directly references.
	 */
	protected Hashtable getTableOfReferencedPackages(IPackage pkgHandle) {
		/* set of referenced builder packages */
		Hashtable pkgTable= new Hashtable();

		/* do for each type in this package */
		TypeStructureEntry[] types= getAllTypesForPackage(pkgHandle);
		if (types != null) {
			for (int i= 0; i < types.length; i++) {
				PackageElement element= packageElementFromSourceEntry(types[i].getSourceEntry());
				IPackage[] deps= getInternalDependencyGraph().getNamespaceDependencies(element);

				/* make sure namespaces are actually packages */
				for (int j= 0; j < deps.length; j++) {
					if (fPackageMap.getEntry(deps[j]) != null) {
						pkgTable.put(deps[j], deps[j]);
					}
				}
			}
		}

		/* remove this package */
		pkgTable.remove(pkgHandle);
		return pkgTable;
	}
	/**
	 * For debugging only.
	 */
	protected TypeStructureEntry getTypeStructureEntry(String qualifiedNameWithoutSuffix) {
		int dot= qualifiedNameWithoutSuffix.lastIndexOf('.');
		String pkgName= (dot == -1 ? ".default"/*nonNLS*/ : qualifiedNameWithoutSuffix.substring(0, dot));
		String typeName= (dot == -1 ? qualifiedNameWithoutSuffix : qualifiedNameWithoutSuffix.substring(dot + 1));
		IPackage pkg= fDevelopmentContext.getImage().getPackageHandle(pkgName, false);
		IType type= pkg.getClassHandle(typeName);
		return (TypeStructureEntry) fPrincipalStructureTable.get(type);
	}
	/**
	 * Returns the type structure entry for the given type handle.  
	 * If lazyBuildCU is true, performs lazy building of compilation units if necessary.
	 * Always performs lazy building of class files if necessary.
	 * Returns null if no type descriptor can be found.
	 */
	protected TypeStructureEntry getTypeStructureEntry(IType handle, boolean lazyBuildCU) {
		TypeStructureEntry tsEntry= (TypeStructureEntry) fPrincipalStructureTable.get(handle);
		if (tsEntry != null) {
			return tsEntry;
		}

		// TBD: Doesn't handle lazy builds.

		/* get the source element */
		IPackage pkg= handle.getPackage();
		SourceEntry sEntry= getSourceEntry(handle);
		if (sEntry == null) {
			return null;
		}

		/* if its a class file, parse it */
		if (sEntry.isBinary()) {
			//byte[] bytes = getElementContentBytes(sEntry);
			// Canonicalize package part of type handle
			handle= canonicalize(pkg).getClassHandle(handle.getSimpleName());
			tsEntry= new TypeStructureEntry(sEntry, handle);
			//tsEntry.setCRC32(getBinaryOutput().crc32(bytes));
			fPrincipalStructureTable.put(handle, tsEntry);
		} else if (lazyBuildCU) {
			if (fProblemReporter.hasProblems(sEntry)) {
				// If the entry has problems, that's a sure sign it has already been compiled.
				// Don't try again.
				return null;
			}

			// make sure the entry is a compilation unit
			PackageElement unit= packageElementFromSourceEntry(sEntry);

			// compile it 
			new BatchImageBuilder(this).lazyBuild(unit);

			// try to get the entry again; may still be null 
			tsEntry= (TypeStructureEntry) fPrincipalStructureTable.get(handle);
		}
		return tsEntry;
	}
	/**
	 * Returns the (non-state-specific) package handle representing the namespace
	 * for unknown dependencies.  All compilation units which have unknown dependencies
	 * depend on this namespace.
	 */
	protected IPackage getUnknownDependenciesNamespace() {
		return fDevelopmentContext.getImage().getPackageHandle(UNKNOWN_DEPENDENCIES, true);
	}
	/**
	 * Process an internal exception: if we're being called by the compiler, throw an AbortCompilation
	 * otherwise throw an internal image builder exception.
	 */
	protected RuntimeException internalException(String message) {
		return fDevelopmentContext.internalException(message);
	}
	/**
	 * Process an internal exception: if we're being called by the compiler, throw an AbortCompilation
	 * otherwise throw an internal image builder exception.
	 */
	protected RuntimeException internalException(Throwable t) {
		return fDevelopmentContext.internalException(t);
	}
	/**
	 * Returns whether the given path represents a zip file.
	 */
	protected static boolean isZipElement(IPath path) {
		String extension= path.getFileExtension();
		return extension != null && (extension.equalsIgnoreCase("zip"/*nonNLS*/) || extension.equalsIgnoreCase("jar"/*nonNLS*/));
	}
	/**
	 * Given a project-relative path, returns an absolute path.
	 */
	protected IPath makeAbsolute(IPath path) {
		if (path.isAbsolute()) {
			return path;
		}
		IProject project= getProject();
		return project.getFullPath().append(path);
	}
	/**
	 * Returns a new image delta representing the differences between this
	 * state (the new state) and another one (the old state).  The delta is naive in that no delta information is initially provided.
	 * Only the portion of the states within the given image context are examined.
	 */
	public IDelta newNaiveDeltaWith(IState oldState, IImageContext imgCtx) {
		return new DeltaImpl(oldState, this, imgCtx);
	}
	/**
	 * Returns a package element corresponding to the given source entry. 
	 * The source entry may or may not be present.
	 */
	protected PackageElement packageElementFromSourceEntry(SourceEntry entry) {
		IPackage pkgHandle= packageFromSourceEntry(entry);
		return new PackageElement(pkgHandle, entry);
	}
	/**
	 * Returns a package handle corresponding to the given source entry's package. 
	 * The source entry may or may not be present.
	 */
	protected IPackage packageFromSourceEntry(SourceEntry entry) {
		IPath path= entry.getPath();
		IPackage pkgHandle;

		/* if it's a zip file */
		String zipEntryFileName = entry.fZipEntryFileName;
		if (zipEntryFileName != null) {
			/* compute filename and package from zip name */
			String zipEntryPath = entry.fZipEntryPath;
			if (zipEntryPath == null) {
				/* default unnamed package */
				pkgHandle= defaultPackageForProject();
			} else {
				String pkgName= zipEntryPath.replace('/', '.');
				pkgHandle= fDevelopmentContext.getImage().getPackageHandle(pkgName, false);
			}
		} else {
			/* compute filename and package from element id */
			IPath parent= path.removeLastSegments(1);
			pkgHandle= fPathMap.packageHandleFromPath(parent);
		}
		return canonicalize(pkgHandle);
	}
	/**
	 * Stores the result of a compilation in the state tables
	 */
	protected void putCompilationResult(ConvertedCompilationResult result) {
		PackageElement unit= result.getPackageElement();

		/* get source entry for result */
		SourceEntry sEntry= getSourceEntry(unit);

		/* record problems */
		fProblemReporter.removeNonSyntaxErrors(sEntry);
		IProblemDetail[] problems= result.getProblems();
		for (int i= 0; i < problems.length; ++i) {
			fProblemReporter.putProblem(sEntry, problems[i]);
		}

		/* This records the types actually contributed, */
		/* to record in the dependency graph. */
		TypeStructureEntry[] tsEntries= result.getTypes();
		IType[] types= new IType[tsEntries.length];
		int count= 0;

		/* record type structure */
		for (int i= 0; i < tsEntries.length; i++) {
			TypeStructureEntry tsEntry= tsEntries[i];
			IType typeHandle= tsEntry.getType();
			// Sanity check before putting in table
			TypeStructureEntry tsExisting= (TypeStructureEntry) fPrincipalStructureTable.get(typeHandle);
			if (tsExisting != null) {
				if (!tsExisting.getSourceEntry().getFileName().equals(sEntry.getFileName())) {
					// Same type provided by different files
					String msg= Util.bind("build.duplicateType"/*nonNLS*/, typeHandle.getName(), tsExisting.getSourceEntry().getFileName());
					ProblemDetailImpl problem= new ProblemDetailImpl(msg, sEntry);
					fProblemReporter.putProblem(sEntry, problem);
					// skip it
					continue;
				}
			}

			// Finally, put it in table.
			fPrincipalStructureTable.put(typeHandle, tsEntry);
			types[count++]= typeHandle;
		}

		/* Update the dependency graph. */
		if (count < types.length) {
			System.arraycopy(types, 0, types= new IType[count], 0, count);
		}
		fGraph.add(unit, types, result.getDependencies());
	}
	/**
	 * Stores the results of a compilation in the state tables
	 */
	protected void putCompilationResults(ConvertedCompilationResult[] results) {
		for (int i= 0; i < results.length; i++) {
			putCompilationResult(results[i]);
		}
	}
	/**
	 * Adds one source entry in the source element table
	 */
	public void putSourceEntry(IPackage pkg, SourceEntry sourceEntry) {
		this.fSourceElementTable.putSourceEntry(pkg, sourceEntry);
	}
	/**
	 * Reads the class path.
	 */
	protected void readClassPath() {
		try {
			JavaProject jp= (JavaProject) getJavaProject();
			fPackageFragmentRootsInClassPath= jp.getBuilderRoots(null);
			fBinaryOutput= new ProjectBinaryOutput(getProject(), jp.getOutputLocation(), fDevelopmentContext);
		} catch (JavaModelException e) {
			throw internalException(e);
		}
	}
	/**
	 * Reads the class path.
	 */
	protected void readClassPath(IResourceDelta delta) {
		try {
			JavaProject jp= (JavaProject) getJavaProject();
			fPackageFragmentRootsInClassPath= jp.getBuilderRoots(delta);
			fBinaryOutput= new ProjectBinaryOutput(getProject(), jp.getOutputLocation(), fDevelopmentContext);
		} catch (JavaModelException e) {
			throw internalException(e);
		}
	}
	/**
	 * Remove one source entry from the source element table
	 */
	public void removeSourceEntry(IPackage pkg, IType handle, String fileName) {
		this.fSourceElementTable.removeSourceEntry(pkg, fileName);
		this.getPrincipalStructureTable().remove(handle);
	}
	protected void resetProject() {
		// Remembers the name of the project before reseting it
		if (fProjectName == null)
			fProjectName= fProject.getName();
		fProject= null;
	}
	/**
	 * This method recalculates the dependency info to refer to the source element 
	 * each type came from.  Stand-alone sources and binaries are represented by 
	 * PackageElement objects, zips are represented by the IPath of the zip file,
	 * and namespaces by IPackage objects.
	 */
	protected Vector resolveDependencies(PackageElement resultUnit, CompilationResult result) {
		IPackage resultPackage= resultUnit.getPackage();
		SourceEntry resultSourceEntry= getSourceEntry(resultUnit);
		char[][] fileDependencies= result.getFileDependencies();
		char[][] namespaceDependencies= result.getNamespaceDependencies();
		Vector vSourceDeps= new Vector();
		if (namespaceDependencies != null) {
			for (int i= 0; i < namespaceDependencies.length; i++) {
				String namespace= Util.toString(namespaceDependencies[i]);
				if (namespace.length() == 0) {
					IPackage defaultPkg= defaultPackageFor(resultUnit.getPackage());
					if (defaultPkg != null && !vSourceDeps.contains(defaultPkg)) {
						vSourceDeps.addElement(defaultPkg);
					}
				} else {
					IPackage pkg= fDevelopmentContext.getImage().getPackageHandle(namespace, false);
					pkg= canonicalize(pkg);
					if (!vSourceDeps.contains(pkg)) {
						vSourceDeps.addElement(pkg);
					}
				}
			}
		}
		if (!vSourceDeps.contains(resultPackage)) {
			vSourceDeps.addElement(resultPackage);
		}
		if (!vSourceDeps.contains(fDevelopmentContext.getDefaultPackage())) {
			vSourceDeps.addElement(fDevelopmentContext.getDefaultPackage());
		}

		/* do for each file dependency */
		if (fileDependencies != null) {
			for (int i= 0; i < fileDependencies.length; i++) {
				if (fileDependencies[i] != null) {
					SourceEntry sEntry= SourceEntry.fromPathWithZipEntryName(Util.toString(fileDependencies[i]));
					if (sEntry.fZipEntryFileName != null) {
						IPath path= sEntry.getPath();
						if (!vSourceDeps.contains(path)) {
							vSourceDeps.addElement(path);
						}
					} else {
						PackageElement element= packageElementFromSourceEntry(sEntry);
						/* Make sure it's a valid ref. */
						if (getSourceEntry(element) != null) {
							if (!vSourceDeps.contains(element.getPackage())) {
								vSourceDeps.addElement(element.getPackage());
							}
							vSourceDeps.addElement(element);
						}
					}
				}
			}
		}
		return vSourceDeps;
	}
	/**
	 * setBuildContext method comment.
	 */
	protected void setBuildContext(IImageContext context) {
		fBuildContext= context;
	}
	/**
	 * Sets the compiler options that were in effect when
	 * this state was built.
	 */
	protected void setCompilerOptions(ConfigurableOption[] options) {
		fCompilerOptions= options;
	}
	/**
	 * Sets the fingerprint for this state.
	 */
	protected void setFingerprint(byte[] fp) {
		fFingerprint= fp;
	}
	/**
	 * Sets the dependency graph for the state.
	 */
	protected void setInternalDependencyGraph(DependencyGraph graph) {
		fGraph= graph;
	}
	/**
	 * Sets the package map
	 */
	protected void setPackageMap(PackageMap map) {
		fPackageMap= map;
		/* build the reverse index -- the path map */
		fPathMap= new PathMap(fPackageMap);
	}
	/**
	 * Sets the path map
	 */
	protected void setPathMap(PathMap map) {
		fPathMap= map;
	}
	/**
	 * Sets the principal structure by package table.
	 */
	protected void setPrincipalStructureByPackageTable(Hashtable table) {
		fPrincipalStructureByPackageTable= table;
	}
	/**
	 * Sets the principal structure table.
	 */
	protected void setPrincipalStructureTable(Hashtable table) {
		fPrincipalStructureTable= table;
	}
	/**
	 * Sets the problem reporter.
	 */
	protected void setProblemReporter(IProblemReporter problemReporter) {
		fProblemReporter= problemReporter;
	}
	/**
	 * Sets the source element table.
	 */
	protected void setSourceElementTable(SourceElementTable table) {
		fSourceElementTable= table;
	}
	/**
	 * Returns a string representation of the receiver.
	 */
	public String toString() {
		return "StateImpl("/*nonNLS*/ + fStateNumber + ")"/*nonNLS*/;
	}
	/**
	 * Returns the type handle for the given type name,
	 * relative to the referring type given by tsEntry.
	 * If typeName is unqualified, the resulting type is in
	 * the default package visible by tsEntry (which may not be
	 * the same package as tsEntry's).
	 */
	protected IType typeNameToHandle(TypeStructureEntry tsEntry, String typeName) {
		return typeNameToHandle(tsEntry.getType().getPackage(), typeName);
	}
	/**
	 * Returns the type handle for the given type name,
	 * relative to the referring package refPkg.
	 * If typeName is unqualified, the resulting type is in
	 * the default package visible by the referring package (which may not be
	 * the same as refPkg).
	 */
	protected IType typeNameToHandle(IPackage refPkg, String typeName) {
		int lastDot= typeName.lastIndexOf('.');
		if (lastDot == -1) {
			IPackage pkg= defaultPackageFor(refPkg);
			if (pkg == null) {
				// typeName is unqualified but there is no visible default package.
				// Should not occur, but be resilient, and assume it's in the same package
				// as tsEntry.
				pkg= refPkg;
			}
			return pkg.getClassHandle(typeName);
		}
		String packageName= typeName.substring(0, lastDot);
		String simpleName= typeName.substring(lastDot + 1);
		if (!refPkg.isUnnamed() && refPkg.getName().equals(packageName)) {
			return refPkg.getClassHandle(simpleName);
		}
		IPackage pkg= fDevelopmentContext.getImage().getPackageHandle(packageName, false);
		pkg= canonicalize(pkg);
		return canonicalize(pkg.getClassHandle(simpleName));
	}
	/**
	 * Convert a type signature to a non-state-specific type handle.
	 */
	protected IType typeSignatureToHandle(TypeStructureEntry tsEntry, String sig) {
		int nestingDepth= 0;
		int i= 0;
		char c= sig.charAt(i);
		while (c == '[') {
			++nestingDepth;
			++i;
			c= sig.charAt(i);
		}

		/* if its a class */
		IType elementType;
		if (c == 'L') {
			/* class or interface */
			int semicolon= sig.indexOf(';', i + 1);
			elementType= typeNameToHandle(tsEntry, sig.substring(i + 1, semicolon));
		} else {
			/* base type or invalid type name */
			elementType= fDevelopmentContext.primitiveTypeFromTypeCode(c);
		}
		if (nestingDepth == 0) {
			return elementType;
		} else {
			return new ArrayTypeHandleImpl((TypeImpl) elementType, nestingDepth);
		}
	}
}
