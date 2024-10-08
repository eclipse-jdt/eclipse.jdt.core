 /*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    het@google.com - Bug 459601 - [clean up] Use CompilationParticipant.buildFinished() in AptCompilationParticipant
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.TestCodeUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;

/**
 * A singleton object, created by callback through the
 * org.eclipse.jdt.core.compilationParticipants extension point.
 */
public class AptCompilationParticipant extends CompilationParticipant
{
	/**
	 * Batch factories that claimed some annotation in a previous round of APT processing.
	 * This currently only apply to the build case since are only generating types during build
	 * and hence cause APT rounding.
	 * The set is an order preserving. The order is determined by their first invocation.
	 */
	private final Set<AnnotationProcessorFactory> _previousRoundsBatchFactories = new LinkedHashSet<>();
	private int _buildRound = 0;
	private boolean _isBatch = false;
	private static final AptCompilationParticipant INSTANCE = new AptCompilationParticipant();
	/**
	 * Files that has been processed by apt during the current build.
	 * Files that has been compiled may need re-compilation (from jdt's perspective)
	 * because of newly generated types. APT only process each file once during a build and
	 * this set will prevent unnecessary/incorrect compilation of already processed files.
	 */
	private Map<IFile, CategorizedProblem[]> _processedFiles = null;

	/**
	 * Files generated by Java 6 annotation processing during the current build.
	 * These must be stored here in order to communicate between the Java 6 annotation
	 * processing phase, which runs during Java compilation, and the Java 5 phase,
	 * which runs afterwards.  This member is reinitialized during aboutToBuild()
	 * and cleared in buildComplete().
	 *
	 * Doing it this way implies that files can only be generated by Java 6 processing
	 * during a build.  That is true as of 8/07.  If that changes, then this may need
	 * to be stored as a thread local, or elsewhere entirely.
	 */
	private HashSet<IFile> _java6GeneratedFiles = null;

	public static AptCompilationParticipant getInstance() {
		return INSTANCE;
	}

	/**
	 * This class is constructed indirectly, by registering an extension to the
	 * org.eclipse.jdt.core.compilationParticipants extension point.  Other
	 * clients should NOT construct this object.
	 */
	private AptCompilationParticipant()	{
	}

	@Override
	public boolean isAnnotationProcessor(){
		return true;
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch){
		// this gets called multiple times during a build.
		// This gets called:
		// 1) after "aboutToBuild" is called.
        // 2) everytime an incremental build occur because of newly generated files
        // this gets called.
		if( _buildRound == 0 )
			_isBatch = isBatch;
	}

	@Override
	public void buildFinished(IJavaProject project) {
		buildComplete();
	}

	@Override
	public void processAnnotations(BuildContext[] allfiles) {
		// This should not happen. There should always be file that that needs
		// building when
		final int total = allfiles == null ? 0 : allfiles.length;
		if( total == 0 )
			return;

		final IProject project = allfiles[0].getFile().getProject();
		final IJavaProject javaProject = JavaCore.create(project);
		// Don't dispatch on pre-1.5 project. They cannot legally have annotations
		String javaVersion = javaProject.getOption("org.eclipse.jdt.core.compiler.source", true); //$NON-NLS-1$
		// Check for 1.3 or 1.4, as we don't want this to break in the future when 1.6
		// is a possibility
		if ("1.3".equals(javaVersion) || "1.4".equals(javaVersion)) { //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		if ( _isBatch && _buildRound == 0 ) {
			AnnotationProcessorFactoryLoader.getLoader().resetBatchProcessors(javaProject);
			_previousRoundsBatchFactories.clear();
		}

		try {

			// split up the list of files with annotations from those that don't
			// also exclude files that has already been processed.
			int annoFileCount = 0;
			int noAnnoFileCount = 0;
			boolean test = false;
			for( int i=0; i<total; i++ ){
				BuildContext bc = allfiles[i];
				if(bc.isTestCode()) {
					test = true;
				}
				if( _buildRound > 0 && _processedFiles.containsKey( bc.getFile() )){
					// We've already processed this file; we'll skip reprocessing it, on
					// the assumption that nothing would change, but we need to re-report
					// any problems we reported earlier because JDT will have cleared them.
					CategorizedProblem[] problems = _processedFiles.get(bc.getFile());
					if (null != problems && problems.length > 0) {
						bc.recordNewProblems(problems);
					}
					continue;
				}
				if( bc.hasAnnotations() )
					annoFileCount ++;
				else
					noAnnoFileCount ++;
			}
			// apt has already processed all files
			// files that are reported at this point is triggered by
			// dependencies introduced by type creation.
			if( annoFileCount == 0 && noAnnoFileCount == 0 )
				return;

			BuildContext[] withAnnotation = null;
			BuildContext[] withoutAnnotation = null;

			if( annoFileCount != 0 )
				withAnnotation = new BuildContext[annoFileCount];
			if(noAnnoFileCount != 0 )
				withoutAnnotation = new BuildContext[noAnnoFileCount];
			int wIndex = 0; // index for 'withAnnotation' array
			int woIndex = 0; // index of 'withoutAnnotation' array
			for( int i=0; i<total; i++ ){
				if( _processedFiles.containsKey( allfiles[i].getFile() ) )
					continue;
				if( allfiles[i].hasAnnotations() )
					withAnnotation[wIndex ++] = allfiles[i];
				else
					withoutAnnotation[woIndex ++] = allfiles[i];
			}

			for( BuildContext file : allfiles )
				_processedFiles.put(file.getFile(), null);

			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories =
				AnnotationProcessorFactoryLoader.getLoader().getJava5FactoriesAndAttributesForProject(javaProject);

			AptProject aptProject = AptPlugin.getAptProject(javaProject);
			Set<AnnotationProcessorFactory> dispatchedBatchFactories =
				APTDispatchRunnable.runAPTDuringBuild(
						withAnnotation,
						withoutAnnotation,
						_processedFiles,
						aptProject,
						factories,
						_previousRoundsBatchFactories,
						_isBatch,
						test);
			_previousRoundsBatchFactories.addAll(dispatchedBatchFactories);
		}
		finally {
			_buildRound ++;
		}
	}

	@Override
	public URI[] getAnnotationProcessorPaths(IJavaProject project, boolean isTest) {
		List<URI> processorPaths = new ArrayList<>();
		FactoryPath factoryPath = FactoryPathUtil.getFactoryPath(project);
		if (factoryPath == null) {
			return null;
		}

		factoryPath.getEnabledContainers().keySet().forEach(container -> {
			if (container instanceof JarFactoryContainer jarContainer) {
				if (jarContainer.exists()) {
					processorPaths.add(jarContainer.getJarFile().toURI());
				}
			}
		});

		if (!processorPaths.isEmpty()) {
			return processorPaths.toArray(new URI[processorPaths.size()]);
		}

		return null;
	}

	@Override
	public IContainer[] getGeneratedSourcePaths(IJavaProject project, boolean isTest) {
		AptProject aptProject = AptPlugin.getAptProject(project);
		if (aptProject == null) {
			return null;
		}

		GeneratedSourceFolderManager generatedSourceFolderManager = aptProject.getGeneratedSourceFolderManager(isTest);
		if (generatedSourceFolderManager == null) {
			return null;
		}

		IFolder folder = generatedSourceFolderManager.getFolder();
		return folder == null? null : new IContainer[] { folder };
	}

	@Override
	public void reconcile(ReconcileContext context){
		final ICompilationUnit workingCopy = context.getWorkingCopy();
		if( workingCopy == null )
			return;
		IJavaProject javaProject = workingCopy.getJavaProject();
		if( javaProject == null )
			return;
		if (!AptConfig.shouldProcessDuringReconcile(javaProject)) {
			AptPlugin.trace("Reconcile-time processing is disabled for project: " + javaProject.getElementName()); //$NON-NLS-1$
			return;
		}
		AptProject aptProject = AptPlugin.getAptProject(javaProject);

		Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories =
			AnnotationProcessorFactoryLoader.getLoader().getJava5FactoriesAndAttributesForProject( javaProject );
		APTDispatchRunnable.runAPTDuringReconcile(context, aptProject, factories, TestCodeUtil.isTestCode(workingCopy));
	}

	@Override
	public void cleanStarting(IJavaProject javaProject){
		IProject p = javaProject.getProject();

		AptPlugin.getAptProject(javaProject).projectClean( true, true, true );
		try{
			// clear out all markers during a clean.
			IMarker[] markers = p.findMarkers(AptPlugin.APT_BATCH_PROCESSOR_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( markers != null ){
				for( IMarker marker : markers )
					marker.delete();
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete batch annotation processor markers"); //$NON-NLS-1$
		}
	}

	/**
	 * Does APT have anything to do for this project?
	 * Even if there are no processors on the factory path, apt may still
	 * be involved during a clean.
	 */
	@Override
	public boolean isActive(IJavaProject project){
		return AptConfig.isEnabled(project);
	}

	@Override
	public int aboutToBuild(IJavaProject project) {
		if (AptConfig.isEnabled(project)) {
			// setup the classpath and make sure the generated source folder is on disk.
			AptPlugin.getAptProject(project).compilationStarted();
		}
		_buildRound = 0; // reset
		// Note that for each project build, we blow away the last project's processed files.
		_processedFiles = new HashMap<>();
		_java6GeneratedFiles = new HashSet<>();
		// TODO: (wharley) if the factory path is different we need a full build
		return CompilationParticipant.READY_FOR_BUILD;
	}

	/**
	 * Called during Java 6 annotation processing phase to register newly-generated files.
	 * This information is then used in the Java 5 (post-compilation) phase when
	 * determining no-longer-generated files to delete.  The list of files is discarded
	 * at the end of each build.
	 */
	public void addJava6GeneratedFile(IFile file) {
		_java6GeneratedFiles.add(file);
	}

	/**
	 * Get the files generated during this build by Java 6 processors.
	 * This is only meaningful in the context of a build, not a reconcile.
	 * @return an unmodifiable, non-null but possibly empty, set of IFiles.
	 */
	public Set<IFile> getJava6GeneratedFiles() {
		if (null == _java6GeneratedFiles) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(_java6GeneratedFiles);
	}

	private void buildComplete() {
		_processedFiles = null;
		_java6GeneratedFiles = null;
	}
}
