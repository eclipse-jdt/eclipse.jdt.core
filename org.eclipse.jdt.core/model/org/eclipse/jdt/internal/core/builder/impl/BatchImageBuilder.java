package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Vector;

import org.eclipse.jdt.internal.compiler.ConfigurableOption;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IDelta;
import org.eclipse.jdt.internal.core.builder.IImageBuilder;
import org.eclipse.jdt.internal.core.builder.IImageContext;
import org.eclipse.jdt.internal.core.builder.IPackage;

/**
 * The batch image builder - builds a state from scratch.
 */
public class BatchImageBuilder extends AbstractImageBuilder implements IImageBuilder, ICompilerRequestor {
	/**
	 * A flag indicating we are doing a batch build rather than
	 * background lazy builds.
	 */
	protected boolean fDoingBatchBuild = false;
/**
 * Creates a new batch image builder on the given new state.
 * The builder will compile everything in the state's project.
 */
public BatchImageBuilder(StateImpl state) {
	this(state, JavaDevelopmentContextImpl.getDefaultCompilerOptions());
}
/**
 * Creates a new batch image builder on the given new state.
 * The batch builder will build all classes within the state's project.
 * This constructor has been created for testing purposes.  This allows
 * tests to control the compiler options used by the batch build.
 */
protected BatchImageBuilder(StateImpl state, ConfigurableOption[] options) {
	fDC = (JavaDevelopmentContextImpl) state.getDevelopmentContext();
	fCompilerOptions = options;
	fNewState = state;
	fWorkQueue = new WorkQueue();
}
/**
 * Builds the entire image from scratch, based on the provided workspace.
 */
public void build() {
	fDoingBatchBuild = true;
	fNotifier = new BuildNotifier(fDC, true);
	getBuilderEnvironment().setNotifier(fNotifier);
	fNotifier.begin();
	try {
		fNewState.readClassPath();
		fNotifier.subTask(Util.bind("build.scrubbingOutput"/*nonNLS*/));
		fNewState.getBinaryOutput().scrubOutput();
		fNotifier.updateProgressDelta(0.05f);
		fNotifier.subTask(Util.bind("build.analyzingPackages"/*nonNLS*/));
		fNewState.buildInitialPackageMap();
		fNotifier.updateProgressDelta(0.05f);

		/* Force build all in build context */
		fNotifier.subTask(Util.bind("build.analyzingSources"/*nonNLS*/));
		IPackage[] pkgs = fNewState.getPackageMap().getAllPackagesAsArray();
		for (int i = 0; i < pkgs.length; ++i) {
			fNotifier.checkCancel();
			SourceEntry[] entries = fNewState.getSourceEntries(pkgs[i]);
			if (entries != null) {
				for (int j = 0; j < entries.length; ++j) {
					SourceEntry sEntry = entries[j];
					if (sEntry.isSource()) {
						PackageElement element = fNewState.packageElementFromSourceEntry(sEntry);
						fWorkQueue.add(element);
					}
				}
			}
		}
		fNotifier.updateProgressDelta(0.05f);
		Vector vToCompile = fWorkQueue.getElementsToCompile();
		if (vToCompile.size() > 0) {
			fNotifier.setProgressPerCompilationUnit(0.75f / vToCompile.size());
			compile(vToCompile);
		}
		/* Copy resources to binary output */
		new ProjectResourceCopier(fNewState.getJavaProject(), fDC, fNotifier, 0.10f).copyAllResourcesOnClasspath();
		
		fNotifier.done();
	} finally {
		cleanUp();
	}
}
/**
 * Returns an image delta between old and new states in the image context.
 * This does not apply to the batch builder.
 * @see IImageBuilder
 */
public IDelta getImageDelta(IImageContext imageContext) {
	return null;
}
/**
 * Builds a given compilation unit.
 */
public void lazyBuild(PackageElement unit) {
	//		String msg = "Attempt to lazy build " + unit.getPackage().getName() + "." + unit.getFileName();
	//		System.err.println(msg + ". " + "Lazy building has been disabled.");
	Assert.isTrue(false, "Internal Error - Lazy building has been disabled"/*nonNLS*/);
}
/**
 * Returns a string describe the builder
 * @see IImageBuilder
 */
public String toString() {
	return "batch image builder for:\n\tnew state: "/*nonNLS*/ + getNewState();
}
}
