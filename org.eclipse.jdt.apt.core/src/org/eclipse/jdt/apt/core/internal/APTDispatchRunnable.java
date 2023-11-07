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
 *    IBM Corporation - modified to split files
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.env.AbstractCompilationEnv;
import org.eclipse.jdt.apt.core.internal.env.BuildEnv;
import org.eclipse.jdt.apt.core.internal.env.EclipseRoundCompleteEvent;
import org.eclipse.jdt.apt.core.internal.env.ReconcileEnv;
import org.eclipse.jdt.apt.core.internal.env.AbstractCompilationEnv.EnvCallback;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class APTDispatchRunnable implements IWorkspaceRunnable
{
	/**
	 * This callback method is passed to a ReconcileEnv to be called within
	 * an AST pipeline in order to process a type during reconcile.
	 * Reconciles involve only one type at a time, but can recurse, so
	 * that multiple instances of this class are on the stack at one time.
	 */
	private final class ReconcileEnvCallback implements EnvCallback {
		private final ReconcileContext _context;
		private final GeneratedFileManager _gfm;

		private ReconcileEnvCallback(ReconcileContext context,
				GeneratedFileManager gfm) {
			_context = context;
			_gfm = gfm;
		}

		@Override
		public void run(AbstractCompilationEnv env) {
			// This is a ReconcileEnvCallback, so we better be dealing with a ReconcileEnv!
			ReconcileEnv reconcileEnv = (ReconcileEnv)env;

			// Dispatch the annotation processors.  Env will keep track of problems and generated types.
			try {
				dispatchToFileBasedProcessor(reconcileEnv, true, true);
			} catch (Throwable t) {
				AptPlugin.log(t, "Processor failure during reconcile"); //$NON-NLS-1$
			}

			// "Remove" any types that were generated in the past but not on this round.
			// Because this is a reconcile, if a file exists on disk we can't really remove
			// it, we can only create a blank WorkingCopy that hides it; thus, we can only
			// remove Java source files, not arbitrary files.
			ICompilationUnit parentWC = _context.getWorkingCopy();
			Set<IFile> newlyGeneratedFiles = reconcileEnv.getAllGeneratedFiles();
			_gfm.deleteObsoleteTypesAfterReconcile(parentWC, newlyGeneratedFiles);

			// Report problems to the ReconcileContext.
			final List<? extends CategorizedProblem> problemList = reconcileEnv.getProblems();
			final int numProblems = problemList.size();
			if (numProblems > 0) {
				final CategorizedProblem[] aptCatProblems = new CategorizedProblem[numProblems];
				_context.putProblems(
				AptPlugin.APT_COMPILATION_PROBLEM_MARKER, problemList
						.toArray(aptCatProblems));
			}

			// Tell the Env that the round is complete.
			// This also calls resetAST() on the context.
			reconcileEnv.close();
		}
	}

	private static final BuildContext[] NO_FILES_TO_PROCESS = new BuildContext[0];
	private static final int MAX_FILES_PER_ITERATION = 1000;
	private /*final*/ BuildContext[] _filesWithAnnotation = null;
	private /*final*/ BuildContext[] _filesWithoutAnnotation = null;
	private /*final*/ Map<IFile, CategorizedProblem[]> _problemRecorder = null;
	private final AptProject _aptProject;
	private final Map<AnnotationProcessorFactory, FactoryPath.Attributes> _factories;
	/** Batch processor dispatched in the previous rounds */
	private final Set<AnnotationProcessorFactory> _dispatchedBatchFactories;
	/** Batch processor dispatched in the current round */
	private Set<AnnotationProcessorFactory> _currentDispatchBatchFactories = Collections.emptySet();
	private final boolean _isFullBuild;
	private static final boolean SPLIT_FILES;
	private static final String SPLIT_FILES_PROPERTY = "org.eclipse.jdt.apt.core.split_files"; //$NON-NLS-1$
	private final boolean _isTestCode;

	static {
		String setting = System.getProperty(SPLIT_FILES_PROPERTY);
		SPLIT_FILES = setting == null || setting.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	public static Set<AnnotationProcessorFactory> runAPTDuringBuild(
			BuildContext[] filesWithAnnotations,
			BuildContext[] filesWithoutAnnotations,
			Map<IFile, CategorizedProblem[]> problemRecorder,
			AptProject aptProject,
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			Set<AnnotationProcessorFactory> dispatchedBatchFactories,
			boolean isFullBuild, boolean isTestCode){

		 if( filesWithAnnotations == null ){
			 filesWithAnnotations = NO_FILES_TO_PROCESS;
		 }
		// If we're building, types can be generated, so we
		// want to run this as an atomic workspace operation
		 APTDispatchRunnable runnable =
			 new APTDispatchRunnable(
					 filesWithAnnotations,
					 filesWithoutAnnotations,
					 problemRecorder,
					 aptProject, factories,
					 dispatchedBatchFactories, isFullBuild, isTestCode );
		 IWorkspace workspace = ResourcesPlugin.getWorkspace();
		 try {
			 workspace.run(runnable, aptProject.getJavaProject().getResource(), IWorkspace.AVOID_UPDATE, null);
		 }
		 catch (CoreException ce) {
			 AptPlugin.log(ce, "Could not run APT"); //$NON-NLS-1$
		 }
		 return runnable._currentDispatchBatchFactories;
	}

	public static void runAPTDuringReconcile(
			ReconcileContext reconcileContext,
			AptProject aptProject,
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			boolean test)
	{
		// Reconciling, so we do not want to run this as an atomic workspace
		// operation. If we do, it is easy to have locking issues when someone
		// calls a reconcile from within a workspace lock
		APTDispatchRunnable runnable = new APTDispatchRunnable( aptProject, factories, test);
		runnable.reconcile(reconcileContext, aptProject.getJavaProject(), test);
	}

	/** create a runnable used during build */
	private APTDispatchRunnable(
			BuildContext[] filesWithAnnotation,
			BuildContext[] filesWithoutAnnotation,
			Map<IFile, CategorizedProblem[]> problemRecorder,
			AptProject aptProject,
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			Set<AnnotationProcessorFactory> dispatchedBatchFactories,
			boolean isFullBuild,
			boolean isTestCode)
	{
		assert filesWithAnnotation != null : "missing files"; //$NON-NLS-1$
		_filesWithAnnotation = filesWithAnnotation;
		_filesWithoutAnnotation = filesWithoutAnnotation;
		_problemRecorder = problemRecorder;
		_aptProject = aptProject;
		_factories = factories;
		_dispatchedBatchFactories = dispatchedBatchFactories;
		_isFullBuild = isFullBuild;
		_isTestCode = isTestCode;
	}
	/** create a runnable used during reconcile */
	private APTDispatchRunnable(
			AptProject aptProject,
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			boolean test)
	{
		_aptProject = aptProject;
		_factories = factories;
		_isTestCode = test;
		_isFullBuild = false;
		// does not apply in reconcile case. we don't generate file during
		// reconcile and no apt rounding ever occur as a result.
		_dispatchedBatchFactories = Collections.emptySet();
	}

	private void reconcile(final ReconcileContext reconcileContext,
			   IJavaProject javaProject, boolean test)
	{
		if (_factories.size() == 0) {
			if (AptPlugin.DEBUG)
				trace("apt leaving project " + javaProject.getProject() +  //$NON-NLS-1$
						" early because there are no factories", //$NON-NLS-1$
						null);
			//TODO: clean up generated working copies here?  I think not necessary. - WSH 10/06
			return;
		}

		// Construct a reconcile time environment. This will invoke
		// dispatch from inside the callback.
		GeneratedFileManager gfm = _aptProject.getGeneratedFileManager(test);
		gfm.reconcileStarted();
		EnvCallback callback = new ReconcileEnvCallback(reconcileContext, gfm);
		AbstractCompilationEnv.newReconcileEnv(reconcileContext, callback);

	}

	@Override
	public void run(IProgressMonitor monitor)
	{
		build();
	}

	/**
	 * Determine whether there are files to be processed.
	 * @return <code>true</code> iff APT processing should occur, return <code>false</code>
	 * otherwise.
	 *
	 * APT should should run one of the following is true
	 * 1) There are files with annotations
	 * 2) There are factories dispatched in an earlier round
	 */
	private boolean shouldBuild()
	{
		if( (_factories == null || _factories.size() == 0) && _dispatchedBatchFactories.isEmpty() )
			return false;

		int totalFiles = _filesWithAnnotation == null ? 0 : _filesWithAnnotation.length;
		// We are required to dispatch even though there are no files with annotations.
		// This is a documented behavior in the mirror spec.
		return totalFiles > 0 || !_dispatchedBatchFactories.isEmpty();
	}

	private void build(){

		if ( !shouldBuild() )
		{
			// tracing
			if ( AptPlugin.DEBUG )
			{
				String msg;
				if ( (_factories == null || _factories.size() == 0) && _dispatchedBatchFactories.isEmpty() )
					msg = "no AnnotationProcessoryFactory instances registered."; //$NON-NLS-1$
				else
					msg = "no files to dispatch to."; //$NON-NLS-1$
				trace( "run():  leaving project " + _aptProject.getJavaProject().getProject() +  //$NON-NLS-1$
						" early because there are " + msg, //$NON-NLS-1$
					   null);
			}
			cleanupAllGeneratedFiles();
		}
		else
		{
			assert _filesWithAnnotation != null :
				   "should never be invoked unless we are in build mode!"; //$NON-NLS-1$

			EnvCallback buildCallback = new EnvCallback() {
				@Override
				public void run(AbstractCompilationEnv env) {
					build((BuildEnv)env);
				}
			};
			boolean split = false;
			if (SPLIT_FILES && !hasBatchFactory()) { // don't split the files if batch processors are present
				split = _filesWithAnnotation.length > MAX_FILES_PER_ITERATION ? true : false;
			}
			if (!split) {
				// Construct build environment, this invokes the build inside a callback
				// in order to keep open the DOM AST pipeline
				BuildEnv.newBuildEnv(
						_filesWithAnnotation,
						_filesWithoutAnnotation,
						_aptProject.getJavaProject(),
						buildCallback);
			} else {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=301894
				for (int index = 0; index < _filesWithAnnotation.length;) {
					int numberToProcess = (index + MAX_FILES_PER_ITERATION) > _filesWithAnnotation.length ? _filesWithAnnotation.length - index : MAX_FILES_PER_ITERATION;
					BuildContext[] filesToProcess = new BuildContext[numberToProcess];
					System.arraycopy(_filesWithAnnotation, index, filesToProcess, 0, numberToProcess);
					// Construct build environment, this invokes the build inside a callback
					// in order to keep open the DOM AST pipeline
					BuildEnv.newBuildEnv(
							filesToProcess,
							_filesWithoutAnnotation,
							_aptProject.getJavaProject(),
							buildCallback);
					 index += numberToProcess;
				}
			}
		}

		// We need to save the file dependency state regardless of whether any Java 5 processing
		// was performed, because it may also contain Java 6 information.
		_aptProject.getGeneratedFileManager(_isTestCode).writeState();
	}

	/**
	 * @return <code>true</code> iff there are factories that can only be run in batch mode.
	 */
	private boolean hasBatchFactory()
	{
		for( FactoryPath.Attributes attr : _factories.values() ){
			if( attr.runInBatchMode() )
				return true;
		}
		return false;

	}

	/**
	 * Batch processor should only be invoked during a clean build.
	 * @return <code>true</code> iff batch processors should be dispatched.
	 * Return <code>false</code> otherwise. Return <code>false</code> if
	 * there are no batch processors.
	 */
	private boolean shouldDispatchToBatchProcessor(final AbstractCompilationEnv processorEnv )
	{
		return ( _isFullBuild && processorEnv.getPhase() == Phase.BUILD && hasBatchFactory() );
	}

	private void runAPTInFileBasedMode(final BuildEnv processorEnv)
	{
		final BuildContext[] cpResults = processorEnv.getFilesWithAnnotation();
		final GeneratedFileManager gfm = _aptProject.getGeneratedFileManager(_isTestCode);
		boolean projectEnablesReconcile = AptConfig.shouldProcessDuringReconcile(_aptProject.getJavaProject());
		for (BuildContext curResult : cpResults ) {
			processorEnv.beginFileProcessing(curResult);
			dispatchToFileBasedProcessor(processorEnv, projectEnablesReconcile, false);
			reportResult(
					curResult,
					processorEnv.getAllGeneratedFiles(),
					processorEnv.getModifiedGeneratedFiles(),
					processorEnv.getProblems(),
					processorEnv.getTypeDependencies(),
					gfm,
					processorEnv);
			processorEnv.completedFileProcessing();
		}
	}

	/**
	 * @param lastGeneratedFiles files generated from previous apt run.
	 * @param generatedFiles all files generated from current apt run.
	 * @param modifiedGeneratedFiles new generated files or files differs from those from
	 *        previous run.
	 * @param problems problems from current apt run.
	 */
	private void reportResult(
			BuildContext curResult,
			Set<IFile> java5GeneratedFiles,
			Set<IFile> modifiedGeneratedFiles,
			List<? extends CategorizedProblem> problems,
			Set<String> deps,
			GeneratedFileManager gfm,
			BuildEnv processorEnv)
	{
		// Combine files generated by Java 5 and Java 6 processing phases
		Set<IFile> allGeneratedFiles = null;
		Set<IFile> java6GeneratedFiles = AptCompilationParticipant.getInstance().getJava6GeneratedFiles();
		if (java5GeneratedFiles == null || java5GeneratedFiles.isEmpty()) {
			if (java6GeneratedFiles.isEmpty()) {
				allGeneratedFiles = Collections.emptySet();
			}
			else {
				allGeneratedFiles = java6GeneratedFiles;
			}
		}
		else {
			if (java6GeneratedFiles.isEmpty()) {
				allGeneratedFiles = java5GeneratedFiles;
			}
			else {
				allGeneratedFiles = new HashSet<>(java6GeneratedFiles);
				allGeneratedFiles.addAll(java5GeneratedFiles);
			}
		}

		// figure out exactly what got deleted
		final List<IFile> deletedFiles = new ArrayList<>();
		IFile parentFile = curResult.getFile();
		cleanupNoLongerGeneratedFiles(
				parentFile,
				allGeneratedFiles,
				gfm,
				processorEnv,
				deletedFiles);
		// report newly created or modified generated files
		int numNewFiles = modifiedGeneratedFiles.size();
		if( numNewFiles > 0 ){
			final IFile[] newFilesArray = new IFile[numNewFiles];
			curResult.recordAddedGeneratedFiles(modifiedGeneratedFiles.toArray(newFilesArray));
		}

		// report deleted file.
		int numDeletedFiles = deletedFiles.size();
		if(numDeletedFiles > 0){
			final IFile[] deletedFilesArray = new IFile[numDeletedFiles];
			curResult.recordDeletedGeneratedFiles(deletedFiles.toArray(deletedFilesArray));
		}

		// report problems
		final int numProblems = problems.size();
		if( numProblems > 0 ){
			final CategorizedProblem[] catProblemsArray = new CategorizedProblem[numProblems];
			curResult.recordNewProblems(problems.toArray(catProblemsArray));
			// Tell compilation participant about the problems, so it can report them
			// again without reprocessing if a file is resubmitted.
			_problemRecorder.put(curResult.getFile(), catProblemsArray);
		}

		// report dependency
		final int numDeps = deps.size();
		if( numDeps > 0 ){
			final String[] depsArray = new String[numDeps];
			curResult.recordDependencies(deps.toArray(depsArray));
		}
	}


	/**
	 * mixed mode - allow batch processor to be run as well as filed based ones.
	 * @param currentRoundDispatchedBatchFactories output parameter. At return contains the
	 * set of batch factories that has been dispatched.
	 */
	private void runAPTInMixedMode(final BuildEnv processorEnv)
	{
		final BuildContext[] cpResults = processorEnv.getFilesWithAnnotation();
		final Map<BuildContext, Set<AnnotationTypeDeclaration>> file2AnnotationDecls =
			new HashMap<>(cpResults.length * 4/3 + 1);
		final Map<String, AnnotationTypeDeclaration> annotationDecls =
			processorEnv.getAllAnnotationTypes(file2AnnotationDecls);

		if (annotationDecls.isEmpty() && _dispatchedBatchFactories.isEmpty() )
		{
			if ( AptPlugin.DEBUG )
				trace( "runAPT:  leaving early because annotationDecls is empty", //$NON-NLS-1$
					   processorEnv);
			return;
		}

		if( AptPlugin.DEBUG )
			trace( "annotations found " + annotationDecls.keySet(), processorEnv); //$NON-NLS-1$

		// file based processing factory to the set of annotations that it 'claims'
		final Map<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>> fileFactory2Annos =
			new HashMap<>( _factories.size() * 4/3 + 1 );

		// batch processing factory to the set of annotations that it 'claims'
		final Map<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>> batchFactory2Annos =
			new HashMap<>( _factories.size() * 4/3 + 1 );

		for( Map.Entry<AnnotationProcessorFactory, FactoryPath.Attributes> entry : _factories.entrySet() ){
			AnnotationProcessorFactory factory = entry.getKey();
			Set<AnnotationTypeDeclaration> annotationTypes = getFactorySupportedAnnotations(factory, annotationDecls);
			if( annotationTypes != null ){

				boolean batch = entry.getValue().runInBatchMode();
				Map<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration> > factory2Annos =
					batch ? batchFactory2Annos : fileFactory2Annos;
				if( annotationTypes.size() == 0 ){
					// this factory is claiming all (remaining) annotations.
					annotationTypes = new HashSet<>(annotationDecls.values());
					factory2Annos.put(factory, annotationTypes);
					annotationDecls.clear();
					break;
				}
				else{
					factory2Annos.put(factory, annotationTypes);
				}
			}
			if( annotationDecls.isEmpty() )
				break;
		}

		if( ! annotationDecls.isEmpty() ){
			// TODO: (theodora) log unclaimed annotations?
		}

		// Dispatch to the batch process factories first.
		// Batch processors only get executed on a full/clean build
		if( !batchFactory2Annos.isEmpty() ||
			(_dispatchedBatchFactories != null && !_dispatchedBatchFactories.isEmpty()) ){

			processorEnv.beginBatchProcessing();
			if( !batchFactory2Annos.isEmpty()){
				// Once we figure out which factory claims what annotation,
				// the order of the factory doesn't matter.
				// But in order to make things consists between runs, will
				// dispatch base on factory order.
				_currentDispatchBatchFactories = new LinkedHashSet<>();
				for(AnnotationProcessorFactory factory : _factories.keySet() ){
					final Set<AnnotationTypeDeclaration> annotationTypes = batchFactory2Annos.get(factory);
					if( annotationTypes == null ) continue;
					final AnnotationProcessor processor =
						factory.getProcessorFor(annotationTypes, processorEnv);
					if( processor != null ){
						if ( AptPlugin.DEBUG )
							trace( "runAPT: invoking batch processor " + processor.getClass().getName(), //$NON-NLS-1$
									processorEnv);
						_currentDispatchBatchFactories.add(factory);
						processorEnv.setCurrentProcessorFactory(factory, false);
						processor.process();
						processorEnv.setCurrentProcessorFactory(null, false);
					}
				}
			}
			// We have to dispatch to factories even though we may not have discovered any annotations.
			// This is a documented APT behavior that we have to observe.
			for( AnnotationProcessorFactory prevRoundFactory : _dispatchedBatchFactories ){
				if(_currentDispatchBatchFactories.contains(prevRoundFactory))
					continue;
				final AnnotationProcessor processor =
					prevRoundFactory.getProcessorFor(Collections.<AnnotationTypeDeclaration>emptySet(), processorEnv);
				if( processor != null ){
					if ( AptPlugin.DEBUG )
						trace( "runAPT: invoking batch processor " + processor.getClass().getName(), //$NON-NLS-1$
								processorEnv);
					processorEnv.setCurrentProcessorFactory(prevRoundFactory, false);
					processor.process();
					processorEnv.setCurrentProcessorFactory(null, false);
				}
			}

			// Currently, we are putting everything in the first file annotations.
			// TODO: Is this correct?
			// Why is it ok (today):
			// 1) Problems are reported as IMarkers and not IProblem thru the
			// BuildContext API.
			// 2) jdt is currently not doing anything about the parent->generated file relation
			//    so it doesn't matter which BuildContext we attach the
			//    creation/modification/deletion of generated files. -theodora
			BuildContext firstResult = null;
			if( cpResults.length > 0 )
				firstResult = cpResults[0];
			else{
				final BuildContext[] others = processorEnv.getFilesWithoutAnnotation();
				if(others != null && others.length > 0 )
					firstResult = others[0];
			}

			// If there are no files to be built, apt will not be involved.
			assert firstResult != null : "don't know where to report results"; //$NON-NLS-1$
			if(firstResult != null ){
				final GeneratedFileManager gfm = _aptProject.getGeneratedFileManager(_isTestCode);
				reportResult(
						firstResult,  // just put it all in
						processorEnv.getAllGeneratedFiles(),
						processorEnv.getModifiedGeneratedFiles(),
						processorEnv.getProblems(),  // this is empty in batch mode.
						processorEnv.getTypeDependencies(),  // this is empty in batch mode.
						gfm,
						processorEnv);
			}
			processorEnv.completedBatchProcessing();
		}

		// Now, do the file based dispatch
		if( !fileFactory2Annos.isEmpty() ){
			boolean projectEnablesReconcile = AptConfig.shouldProcessDuringReconcile(_aptProject.getJavaProject());
			for(BuildContext curResult : cpResults ){
				final Set<AnnotationTypeDeclaration> annotationTypesInFile = file2AnnotationDecls.get(curResult);
				if( annotationTypesInFile == null || annotationTypesInFile.isEmpty() )
					continue;
				for(AnnotationProcessorFactory factory : _factories.keySet() ){
					final Set<AnnotationTypeDeclaration> annotationTypesForFactory = fileFactory2Annos.get(factory);
					if( annotationTypesForFactory == null || annotationTypesForFactory.isEmpty() )
						continue;
					final Set<AnnotationTypeDeclaration> intersect = setIntersect(annotationTypesInFile, annotationTypesForFactory);
					if( intersect != null && !intersect.isEmpty() ){
						processorEnv.beginFileProcessing(curResult);
						final AnnotationProcessor processor =
							factory.getProcessorFor(intersect, processorEnv);
						if( processor != null ){
							if ( AptPlugin.DEBUG )
								trace( "runAPT: invoking file-based processor " + processor.getClass().getName(), //$NON-NLS-1$
										processorEnv );
							//TODO in 3.4: also consider factory path attributes
							boolean willReconcile = projectEnablesReconcile && AbstractCompilationEnv.doesFactorySupportReconcile(factory);
							processorEnv.setCurrentProcessorFactory(factory, willReconcile);
							processor.process();
							processorEnv.setCurrentProcessorFactory(null, false);
						}
					}
				}

				final GeneratedFileManager gfm = _aptProject.getGeneratedFileManager(_isTestCode);
				reportResult(
						curResult,
						processorEnv.getAllGeneratedFiles(),
						processorEnv.getModifiedGeneratedFiles(),
						processorEnv.getProblems(),
						processorEnv.getTypeDependencies(),
						gfm,
						processorEnv);
				processorEnv.completedFileProcessing();
			}
		}
	}

	/**
	 * @param projectEnablesReconcile true if reconcile-time processing is enabled in the current project
	 * @param isReconcile true if this call is during reconcile, e.g., processorEnv is a ReconcileEnv
	 */
	private void dispatchToFileBasedProcessor(
			final AbstractCompilationEnv processorEnv,
			boolean projectEnablesReconcile, boolean isReconcile){

		Map<String, AnnotationTypeDeclaration> annotationDecls = processorEnv.getAnnotationTypes();
		for( Map.Entry<AnnotationProcessorFactory, FactoryPath.Attributes> entry : _factories.entrySet() ){
			if( entry.getValue().runInBatchMode() ) continue;
			AnnotationProcessorFactory factory = entry.getKey();
			//TODO in 3.4: also consider factory path attributes
			boolean reconcileSupported = projectEnablesReconcile &&
				AbstractCompilationEnv.doesFactorySupportReconcile(factory);
			if (isReconcile && !reconcileSupported)
				continue;
			Set<AnnotationTypeDeclaration> factoryDecls = getFactorySupportedAnnotations(factory, annotationDecls);
			if( factoryDecls != null ){
				if(factoryDecls.size() == 0 ){
					factoryDecls = new HashSet<>(annotationDecls.values());
					annotationDecls.clear();
				}
			}
			if (factoryDecls != null && factoryDecls.size() > 0) {
				final AnnotationProcessor processor = factory
						.getProcessorFor(factoryDecls, processorEnv);
				if (processor != null)
				{
					if ( AptPlugin.DEBUG ) {
						trace( "runAPT: invoking file-based processor " + processor.getClass().getName() + " on " + processorEnv.getFile(), //$NON-NLS-1$ //$NON-NLS-2$
								processorEnv);
					}
					processorEnv.setCurrentProcessorFactory(factory, reconcileSupported);
					processor.process();
					processorEnv.setCurrentProcessorFactory(null, false);
				}
			}

			if (annotationDecls.isEmpty())
				break;
		}
		if( ! annotationDecls.isEmpty() ){
			// TODO: (theodora) log unclaimed annotations.
		}
	}

	/**
	 * @param result output parameter
	 */
	private Set<AnnotationProcessorFactory> build(final BuildEnv processorEnv)
	{
		try {
			boolean mixedModeDispatch = shouldDispatchToBatchProcessor(processorEnv);
			if( mixedModeDispatch ){
				runAPTInMixedMode(processorEnv);
			}
			else{
				runAPTInFileBasedMode(processorEnv);
			}

			// notify the processor listeners
			final Set<AnnotationProcessorListener> listeners = processorEnv
					.getProcessorListeners();
			EclipseRoundCompleteEvent event = null;
			for (AnnotationProcessorListener listener : listeners) {
				if (listener instanceof RoundCompleteListener) {
					if (event == null)
						event = new EclipseRoundCompleteEvent(processorEnv);
					final RoundCompleteListener rcListener = (RoundCompleteListener) listener;
					rcListener.roundComplete(event);
				}
			}
			if( _filesWithoutAnnotation != null ){
				cleanupAllGeneratedFilesFrom(_filesWithoutAnnotation);
			}


			// log unclaimed annotations.
		}
		catch (Error t) {
			// Don't catch junit exceptions. This prevents one from unit
			// testing a processor
			if (t.getClass().getName().startsWith("junit.framework")) //$NON-NLS-1$
				throw t;
			AptPlugin.logWarning(t, "Unexpected failure running APT on the file(s): " + getFileNamesForPrinting(processorEnv)); //$NON-NLS-1$
		}
		catch (Throwable t) {
			AptPlugin.logWarning(t, "Unexpected failure running APT on the file(s): " + getFileNamesForPrinting(processorEnv)); //$NON-NLS-1$
		}
		finally {
			processorEnv.close();
		}

		return Collections.emptySet();
	}

	/**
	 * @return the set intersect of the two given sets
	 */
	private Set<AnnotationTypeDeclaration> setIntersect(Set<AnnotationTypeDeclaration> one, Set<AnnotationTypeDeclaration> two ){
		Set<AnnotationTypeDeclaration> intersect = null;
		for( AnnotationTypeDeclaration obj : one ){
			if( two.contains(obj) ){
				if( intersect == null )
					intersect = new HashSet<>();
				intersect.add(obj);
			}
		}
		return intersect;
	}

	private void cleanupAllGeneratedFiles(){
		cleanupAllGeneratedFilesFrom(_filesWithAnnotation);
		cleanupAllGeneratedFilesFrom(_filesWithoutAnnotation);
	}

	private void cleanupAllGeneratedFilesFrom(BuildContext[] cpResults){
		if (cpResults == null) {
			return;
		}
		final Set<IFile> deleted = new HashSet<>();
		GeneratedFileManager gfm = _aptProject.getGeneratedFileManager(_isTestCode);
		Set<IFile> java6GeneratedFiles = AptCompilationParticipant.getInstance().getJava6GeneratedFiles();
		for( BuildContext cpResult : cpResults){
			final IFile parentFile = cpResult.getFile();
			cleanupNoLongerGeneratedFiles(
					parentFile,
					java6GeneratedFiles,
					gfm,
					null,
					deleted);

			if( deleted.size() > 0 ){
				final IFile[] deletedFilesArray = new IFile[deleted.size()];
				cpResult.recordDeletedGeneratedFiles(deleted.toArray(deletedFilesArray));
			}
		}
	}

	/**
	 * Remove all the files that were previously generated
	 * from a particular parent file, but that were not generated
	 * in the most recent build pass.
	 * <p>
	 * Must be called during build phase, not reconcile
	 *
	 * @param parent the BuildContext associated with a single
	 * compiled parent file
	 * @param lastGeneratedFiles the files generated from parent
	 * on the previous build; typically obtained from the GFM just
	 * prior to beginning the current build.
	 * @param newGeneratedFiles the files generated from parent
	 * on the current build; typically stored in the BuildEnv, but
	 * an empty set can be passed in to remove all generated files
	 * of this parent.
	 */
	private void cleanupNoLongerGeneratedFiles(
			IFile parentFile,
			Set<IFile> newGeneratedFiles,
			GeneratedFileManager gfm,
			BuildEnv processorEnv,
			Collection<IFile> deleted)
	{
		deleted.addAll(gfm.deleteObsoleteFilesAfterBuild(parentFile, newGeneratedFiles));
	}

	/**
	 * @return the set of {@link AnnotationTypeDeclaration} that {@link #factory} supports or null
	 *         if the factory doesn't support any of the declarations.
	 *         If the factory supports "*", then the empty set will be returned
	 *
	 * This method will destructively modify {@link #declarations}. Entries will be removed from
	 * {@link #declarations} as the declarations are being added into the returned set.
	 */
	private static Set<AnnotationTypeDeclaration> getFactorySupportedAnnotations(
			final AnnotationProcessorFactory factory,
			final Map<String, AnnotationTypeDeclaration> declarations)

	{
		final Collection<String> supportedTypes = factory
				.supportedAnnotationTypes();

		if (supportedTypes == null || supportedTypes.size() == 0)
			return Collections.emptySet();

		final Set<AnnotationTypeDeclaration> fDecls = new HashSet<>();

		for (Iterator<String> it = supportedTypes.iterator(); it.hasNext();) {
			final String typeName = it.next();
			if (typeName.equals("*")) { //$NON-NLS-1$
				fDecls.addAll(declarations.values());
				declarations.clear();

				// Warn that * was claimed, which is non-optimal
				AptPlugin.logWarning(null, "Processor Factory " + factory +  //$NON-NLS-1$
						" claimed all annotations (*), which prevents any following factories from being dispatched."); //$NON-NLS-1$

			} else if (typeName.endsWith("*")) { //$NON-NLS-1$
				final String prefix = typeName.substring(0,
						typeName.length() - 2);
				for (Iterator<Map.Entry<String, AnnotationTypeDeclaration>> entries = declarations
						.entrySet().iterator(); entries.hasNext();) {
					final Map.Entry<String, AnnotationTypeDeclaration> entry = entries
							.next();
					final String key = entry.getKey();
					if (key.startsWith(prefix)) {
						fDecls.add(entry.getValue());
						entries.remove();
					}
				}
			} else {
				final AnnotationTypeDeclaration decl = declarations
						.get(typeName);
				if (decl != null) {
					fDecls.add(decl);
					declarations.remove(typeName);
				}
			}
		}
		return fDecls.isEmpty() ? null : fDecls;
	}

	private static void trace( String s, AbstractCompilationEnv processorEnv )
	{
		if (AptPlugin.DEBUG)
		{
			if (processorEnv != null) {
				s = "[ phase = " + processorEnv.getPhase() + ", file = " + getFileNamesForPrinting(processorEnv) +" ]  " + s; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			AptPlugin.trace( s );
		}
	}

	private static String getFileNamesForPrinting(final AbstractCompilationEnv env){
		if( env instanceof ReconcileEnv ){
			return env.getFile().getName();
		}
		else{
			return getFileNamesForPrinting((BuildEnv)env);
		}
	}

	/**
	 * For debugging statements only!!
	 * @return the names of the files that we are currently processing.
	 */
	private static String getFileNamesForPrinting(final BuildEnv processorEnv){
		final IFile file = processorEnv.getFile();
		if( file != null )
			return file.getName();
		final BuildContext[] results = processorEnv.getFilesWithAnnotation();
		final int len = results.length;
		switch( len )
		{
		case 0:
			return "no file(s)"; //$NON-NLS-1$
		case 1:
			return results[0].getFile().getName();
		default:
			StringBuilder sb = new StringBuilder();
			boolean firstItem = true;
			for (BuildContext curResult : results) {
				if (firstItem) {
					firstItem = false;
				}
				else {
					sb.append(", "); //$NON-NLS-1$
				}
				sb.append(curResult.getFile().getName());
			}
			return sb.toString();
		}
	}
}
