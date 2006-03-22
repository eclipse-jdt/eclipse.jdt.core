 /*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
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
import org.eclipse.jdt.apt.core.internal.env.CompilationProcessorEnv;
import org.eclipse.jdt.apt.core.internal.env.EclipseRoundCompleteEvent;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.env.ReconcileProcessorEnv;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.ReconcileContext;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class APTDispatchRunnable implements IWorkspaceRunnable
{
	private static final BuildContext[] NO_FILES_TO_PROCESS = new BuildContext[0];
	private /*final*/ BuildContext[] _filesWithAnnotation = null;
	private /*final*/ BuildContext[] _filesWithoutAnnotation = null;
	private final AptProject _aptProject;
	private final Map<AnnotationProcessorFactory, FactoryPath.Attributes> _factories;
	/** Batch processor dispatched in the previous rounds */
	private final Set<AnnotationProcessorFactory> _dispatchedBatchFactories;
	/** Batch processor dispatched in the current round */
	private Set<AnnotationProcessorFactory> _currentDispatchBatchFactories = Collections.emptySet();
	private final boolean _isFullBuild;
	
	
	public static Set<AnnotationProcessorFactory> runAPTDuringBuild(
			BuildContext[] filesWithAnnotations, 
			BuildContext[] filesWithoutAnnotations,
			AptProject aptProject, 
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			Set<AnnotationProcessorFactory> dispatchedBatchFactories,
			boolean isFullBuild){
		
		 if( filesWithAnnotations == null ){
			 filesWithAnnotations = NO_FILES_TO_PROCESS;
		 }
		// If we're building, types can be generated, so we
		// want to run this as an atomic workspace operation
		 APTDispatchRunnable runnable = 
			 new APTDispatchRunnable( 
					 filesWithAnnotations,
					 filesWithoutAnnotations,
					 aptProject, factories, 
					 dispatchedBatchFactories, isFullBuild );
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
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories)
	{
		// Reconciling, so we do not want to run this as an atomic workspace
		// operation. If we do, it is easy to have locking issues when someone
		// calls a reconcile from within a workspace lock
		APTDispatchRunnable runnable = new APTDispatchRunnable( aptProject, factories );	
		runnable.reconcile(reconcileContext, aptProject.getJavaProject());
	}
	
	/** create a runnable used during build */
	private APTDispatchRunnable( 
			BuildContext[] filesWithAnnotation,
			BuildContext[] filesWithoutAnnotation,
			AptProject aptProject, 
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories,
			Set<AnnotationProcessorFactory> dispatchedBatchFactories,
			boolean isFullBuild)
	{
		assert filesWithAnnotation != null : "missing files"; //$NON-NLS-1$
		_filesWithAnnotation = filesWithAnnotation;
		_filesWithoutAnnotation = filesWithoutAnnotation;
		_aptProject = aptProject;
		_factories = factories;
		_dispatchedBatchFactories = dispatchedBatchFactories;
		_isFullBuild = isFullBuild;
	}	
	/** create a runnable used during reconcile */
	private APTDispatchRunnable(
			AptProject aptProject,
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories)
	{	
		_aptProject = aptProject;
		_factories = factories;
		_isFullBuild = false;
		// does not apply in reconcile case. we don't generate file during
		// reconcile and no apt rounding ever occur as a result.
		_dispatchedBatchFactories = Collections.emptySet();
	}
	
	private void reconcile(ReconcileContext reconcileContext,
			   IJavaProject javaProject)
	{
		if (_factories.size() == 0) {
			if (AptPlugin.DEBUG)
				trace("runAPT: leaving early because there are no factories", //$NON-NLS-1$
						null);
			return;
		}
		ReconcileProcessorEnv processorEnv = CompilationProcessorEnv
				.newReconcileEnv(reconcileContext, javaProject);
		dispatchToFileBasedProcessor(processorEnv);

		final List<? extends CategorizedProblem> problemList = processorEnv
				.getProblems();
		final int numProblems = problemList.size();
		if (numProblems > 0) {
			final CategorizedProblem[] aptCatProblems = new CategorizedProblem[numProblems];
			reconcileContext.putProblems(
					AptPlugin.APT_COMPILATION_PROBLEM_MARKER, problemList
							.toArray(aptCatProblems));
		}
		processorEnv.close();
	}	
	
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
				trace( "run():  leaving early because there are " + msg, //$NON-NLS-1$
					   null);
			}
			cleanupAllGeneratedFiles();
		}
		else
		{
			assert _filesWithAnnotation != null :
				   "should never be invoked unless we are in build mode!"; //$NON-NLS-1$
			ProcessorEnvImpl processorEnv = ProcessorEnvImpl.newBuildEnv( 
					_filesWithAnnotation, 
					_filesWithoutAnnotation, 
					_aptProject.getJavaProject());
			build(processorEnv); 
		}
	}
	
	/**
	 * @param factories
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
	 * @param factories
	 * @param processorEnv
	 * @return <code>true</code> iff batch processors should be dispatched.
	 * Return <code>false</code> otherwise. Return <code>false</code> if
	 * there are no batch processors.
	 */
	private boolean shouldDispatchToBatchProcessor(final CompilationProcessorEnv processorEnv )
	{	
		return ( _isFullBuild && processorEnv.getPhase() == Phase.BUILD && hasBatchFactory() );
	}
	
	private void runAPTInFileBasedMode(final ProcessorEnvImpl processorEnv,
									   final Map<IFile, Set<IFile>> lastGeneratedFiles)
	{
		final BuildContext[] cpResults = processorEnv.getFilesWithAnnotation();
		final GeneratedFileManager gfm = _aptProject.getGeneratedFileManager();
		for (BuildContext curResult : cpResults ) {			
			processorEnv.beginFileProcessing(curResult);
			dispatchToFileBasedProcessor(processorEnv);
			final IFile curFile = curResult.getFile();
			reportResult(
					curResult,
					lastGeneratedFiles.get(curFile),
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
	 * @param curResult
	 * @param lastGeneratedFiles files generated from previous apt run.
	 * @param generatedFiles all files generated from current apt run.
	 * @param modifiedGeneratedFiles new generated files or files differs from those from
	 *        previous run.   
	 * @param problems problems from current apt run.
	 * @param deps
	 * @param gfm
	 * @param processorEnv
	 */
	private void reportResult(
			BuildContext curResult,
			Set<IFile> lastGeneratedFiles,
			Set<IFile> generatedFiles,
			Set<IFile> modifiedGeneratedFiles,
			List<? extends CategorizedProblem> problems,
			Set<String> deps,
			GeneratedFileManager gfm, 
			ProcessorEnvImpl processorEnv){
		
		
		if (lastGeneratedFiles == null)
			lastGeneratedFiles = Collections.emptySet();
		if (generatedFiles == null )
			generatedFiles = Collections.emptySet();
		// figure out exactly what got deleted
		final List<IFile> deletedFiles = new ArrayList<IFile>(); 
		cleanupNoLongerGeneratedFiles(
				curResult, 
				lastGeneratedFiles, 
				generatedFiles, 
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
	 * @param processorEnv
	 * @param currentRoundDispatchedBatchFactories output parameter. At return contains the 
	 * set of batch factories that has been dispatched.
	 */
	private void runAPTInMixedMode(
			final Map<IFile, Set<IFile>> lastGeneratedFiles,
			final ProcessorEnvImpl processorEnv)
	{
		final BuildContext[] cpResults = processorEnv.getFilesWithAnnotation();
		final Map<BuildContext, Set<AnnotationTypeDeclaration>> file2AnnotationDecls = 
			new HashMap<BuildContext, Set<AnnotationTypeDeclaration>>(cpResults.length * 4/3 + 1);
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
			new HashMap<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>>( _factories.size() * 4/3 + 1 );
		
		// batch processing factory to the set of annotations that it 'claims'
		final Map<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>> batchFactory2Annos =
			new HashMap<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>>( _factories.size() * 4/3 + 1 );		
		
		for( Map.Entry<AnnotationProcessorFactory, FactoryPath.Attributes> entry : _factories.entrySet() ){
			AnnotationProcessorFactory factory = entry.getKey();
			Set<AnnotationTypeDeclaration> annotationTypes = getFactorySupportedAnnotations(factory, annotationDecls);
			if( annotationTypes != null ){
				
				boolean batch = entry.getValue().runInBatchMode();
				Map<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration> > factory2Annos = 
					batch ? batchFactory2Annos : fileFactory2Annos;
				if( annotationTypes.size() == 0 ){
					// this factory is claiming all (remaining) annotations. 
					annotationTypes = new HashSet<AnnotationTypeDeclaration>(annotationDecls.values());
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
				_currentDispatchBatchFactories = new LinkedHashSet<AnnotationProcessorFactory>();
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
						processor.process();
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
					processor.process();
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
				final GeneratedFileManager gfm = _aptProject.getGeneratedFileManager();
				reportResult(
						firstResult,  // just put it all in 
						lastGeneratedFiles.get(null), 
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
							processor.process();
						}
					}
				}
				
				final GeneratedFileManager gfm = _aptProject.getGeneratedFileManager();
				final IFile curFile = curResult.getFile();
				reportResult(
						curResult,
						lastGeneratedFiles.get(curFile),
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
	
	private void dispatchToFileBasedProcessor(
			final CompilationProcessorEnv processorEnv){
		
		Map<String, AnnotationTypeDeclaration> annotationDecls = processorEnv.getAnnotationTypes();
		for( Map.Entry<AnnotationProcessorFactory, FactoryPath.Attributes> entry : _factories.entrySet() ){
			if( entry.getValue().runInBatchMode() ) continue;
			AnnotationProcessorFactory factory = entry.getKey();
			Set<AnnotationTypeDeclaration> factoryDecls = getFactorySupportedAnnotations(factory, annotationDecls);
			if( factoryDecls != null ){
				if(factoryDecls.size() == 0 ){
					factoryDecls = new HashSet(annotationDecls.values());
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
					processor.process();						
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
	 * @param processorEnv 
	 * @param filesWithMissingType
	 * @param internalRound
	 * @param result output parameter
	 */
	private Set<AnnotationProcessorFactory> build(final ProcessorEnvImpl processorEnv)
	{
		try {
			final BuildContext[] results = processorEnv.getFilesWithAnnotation();
			GeneratedFileManager gfm = _aptProject.getGeneratedFileManager();
			final Map<IFile,Set<IFile>> lastGeneratedFiles = new HashMap<IFile,Set<IFile>>();
			for( BuildContext result : results ){
				final IFile parentIFile = result.getFile();
				lastGeneratedFiles.put(parentIFile, gfm.getGeneratedFilesForParent(parentIFile));
			}
			
			boolean mixedModeDispatch = shouldDispatchToBatchProcessor(processorEnv);
			if( mixedModeDispatch ){
				runAPTInMixedMode(lastGeneratedFiles, processorEnv);
			}
			else{
				runAPTInFileBasedMode(processorEnv, lastGeneratedFiles);
			}

			// notify the processor listeners
			final Set<AnnotationProcessorListener> listeners = processorEnv
					.getProcessorListeners();
			for (AnnotationProcessorListener listener : listeners) {
				EclipseRoundCompleteEvent event = null;
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
			
			processorEnv.close();

			// log unclaimed annotations.
		} catch (Throwable t) {
			AptPlugin.log(t, "Unexpected failure running APT on the file(s): " + getFileNamesForPrinting(processorEnv)); //$NON-NLS-1$
		}
		
		return Collections.emptySet();
	}
	
	/**
	 * @param one
	 * @param two
	 * @return the set intersect of the two given sets
	 */
	private Set<AnnotationTypeDeclaration> setIntersect(Set<AnnotationTypeDeclaration> one, Set<AnnotationTypeDeclaration> two ){
		Set<AnnotationTypeDeclaration> intersect = null;	
		for( AnnotationTypeDeclaration obj : one ){
			if( two.contains(obj) ){
				if( intersect == null )
					intersect = new HashSet<AnnotationTypeDeclaration>();
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
		final List<IFile> deleted = new ArrayList<IFile>();
		if( cpResults != null ){
			GeneratedFileManager gfm = _aptProject.getGeneratedFileManager();
			for( BuildContext cpResult : cpResults){			
				Set<IFile> lastGeneratedFiles = gfm.getGeneratedFilesForParent( cpResult.getFile() );
				cleanupNoLongerGeneratedFiles( 
						cpResult, 
						lastGeneratedFiles, 
						Collections.<IFile>emptySet(), 
						gfm,
						null, 
						deleted);
				
				if( deleted.size() > 0 ){
					final IFile[] deletedFilesArray = new IFile[deleted.size()];
					cpResult.recordDeletedGeneratedFiles(deleted.toArray(deletedFilesArray));
				}
			}
		}
	}	
	
	// Note: This is written to work only in build phase since we are only generating
	//       types during build phase.
	//       Do not call unless caller is sure this is during build phase.
	private void cleanupNoLongerGeneratedFiles(
			BuildContext parent,
			Set<IFile> lastGeneratedFiles, 
			Set<IFile> newGeneratedFiles,
			GeneratedFileManager gfm,		
			ProcessorEnvImpl processorEnv,
			Collection<IFile> deleted)
	{	
		final int numLastGeneratedFiles = lastGeneratedFiles.size();
		// make a copy into an array to avoid concurrent modification exceptions
		IFile[] files = lastGeneratedFiles.toArray( new IFile[ numLastGeneratedFiles ] );
		
		for ( IFile f : files )
		{
			if ( ! newGeneratedFiles.contains( f ) )
			{
				final IFile parentFile = parent.getFile();
				if ( AptPlugin.DEBUG ) 
					trace( "runAPT:  File " + f + " is no longer a generated file for " + parentFile,  //$NON-NLS-1$ //$NON-NLS-2$
							processorEnv );
				try
				{
					if ( gfm.deleteGeneratedFile( f, parentFile, null ) )
						deleted.add( f );
				}
				catch ( CoreException ce )
				{
					AptPlugin.log(ce, "Could not clean up generated files"); //$NON-NLS-1$
				}
			}
		}
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

		final Set<AnnotationTypeDeclaration> fDecls = new HashSet<AnnotationTypeDeclaration>();

		for (Iterator<String> it = supportedTypes.iterator(); it.hasNext();) {
			final String typeName = it.next();
			if (typeName.equals("*")) { //$NON-NLS-1$
				declarations.clear();
				return Collections.emptySet();
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
	
	private static void trace( String s, CompilationProcessorEnv processorEnv )
	{
		if (AptPlugin.DEBUG)
		{
			if (processorEnv != null) {
				s = "[ phase = " + processorEnv.getPhase() + ", file = " + getFileNamesForPrinting(processorEnv) +" ]  " + s; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			System.out.println( "[" + APTDispatchRunnable.class.getName() + "][ thread= " + Thread.currentThread().getName() + " ]"+ s ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	private static String getFileNamesForPrinting(final CompilationProcessorEnv env){
		if( env instanceof ReconcileProcessorEnv ){
			return env.getFile().getName();
		}
		else{
			return getFileNamesForPrinting((ProcessorEnvImpl)env);
		}
	}
	
	/**
	 * For debugging statements only!!
	 * @return the names of the files that we are currently processing. 
	 */
	private static String getFileNamesForPrinting(final ProcessorEnvImpl processorEnv){
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