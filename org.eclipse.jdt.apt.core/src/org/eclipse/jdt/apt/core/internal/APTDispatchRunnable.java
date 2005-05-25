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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.internal.APTDispatch.APTResult;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.EclipseRoundCompleteEvent;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl.AnnotationVisitor;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.util.AptUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/*package*/ class AptDispatchRunnable implements IWorkspaceRunnable
{
	private final IFile _file;
	private final ICompilationUnit _compilationUnit;
	private final IJavaProject _javaProject;
	private final List<AnnotationProcessorFactory> _factories;
	
	private  APTResult _result;

	/*package*/ AptDispatchRunnable( IFile file, IJavaProject javaProject, List<AnnotationProcessorFactory> factories)
	{
		_compilationUnit = null;
		_file = file;
		_javaProject = javaProject;
		_factories = factories;
	}

	/*package*/ AptDispatchRunnable( ICompilationUnit cu, IJavaProject javaProject, List<AnnotationProcessorFactory> factories)
	{
		_compilationUnit = cu;
		_file = null;
		_javaProject = javaProject;
		_factories = factories;
	}
	
	public APTResult getResult() { return _result; }
	
	public void run(IProgressMonitor monitor) 
	{
		//
		//  bail-out early if there aren't factories, or if there aren't any annotation instances
		// 
		if ( _factories == null || _factories.size() == 0 || 
				( _compilationUnit != null && ! AptUtil.hasAnnotationInstance( _compilationUnit ) ) ||
				( _file!= null && !  AptUtil.hasAnnotationInstance( _file ) ) )
		{
			if ( DEBUG ) trace( "runAPTDuringBuild: leaving early because there are no factories or annotation instances");
			
			
			IFile f;
			if ( _file != null )
				f = _file;
			else
				f = (IFile)_compilationUnit.getResource();
			Set<IFile> deletedFiles = cleanupAllGeneratedFilesForParent( f );
			
			if ( deletedFiles.size() == 0 )
				_result =  EMPTY_APT_RESULT;
			else
				_result = new APTResult( (Set<IFile>)Collections.emptySet(), deletedFiles, (Set<String>)Collections.emptySet() );
		}
		else
		{
			ProcessorEnvImpl processorEnv;
			
			if ( _compilationUnit != null )
			{
				processorEnv = ProcessorEnvImpl
					.newProcessorEnvironmentForReconcile(_compilationUnit, _javaProject);
			}
			else
			{
				processorEnv = ProcessorEnvImpl
					.newProcessorEnvironmentForBuild( _file, _javaProject);
			}
			_result = runAPT(_factories, processorEnv);
		}
	}
	
	private static APTResult runAPT(
			final List<AnnotationProcessorFactory> factories,
			final ProcessorEnvImpl processorEnv) 
	{
		try {
			if (factories.size() == 0)
			{
				if ( DEBUG ) trace( "runAPT: leaving early because there are no factories");
				return EMPTY_APT_RESULT;
			}
				
			if ( ! processorEnv.getFile().exists() )
			{
				if ( DEBUG ) trace( "runAPT: leaving early because file doesn't exist");
				return EMPTY_APT_RESULT;
			}
				
			// clear out all the markers from the previous round.
			final String markerType = processorEnv.getPhase() == ProcessorEnvImpl.Phase.RECONCILE ? ProcessorEnvImpl.RECONCILE_MARKER
					: ProcessorEnvImpl.BUILD_MARKER;
			try {
				processorEnv.getFile().deleteMarkers(markerType, true,
						IResource.DEPTH_INFINITE);

			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}
			final Map<String, AnnotationTypeDeclaration> annotationDecls = getAnnotationTypeDeclarations(
					processorEnv.getAstCompilationUnit(), processorEnv);
			
			if (annotationDecls.isEmpty())
			{
				if ( DEBUG ) trace ( "runAPT:  leaving early because annotationDecls is empty" );
				return EMPTY_APT_RESULT;
			}

			GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( processorEnv.getJavaProject().getProject() );
			Set<IFile> lastGeneratedFiles = gfm.getGeneratedFilesForParent( processorEnv.getFile() );
			
			for (int i = 0, size = factories.size(); i < size; i++) {
				final AnnotationProcessorFactory factory = (AnnotationProcessorFactory) factories.get(i);
				Set<AnnotationTypeDeclaration> factoryDecls = getAnnotations(factory, annotationDecls);
				boolean done = false;
				if( factoryDecls != null ){
					if(factoryDecls.size() == 0 ){
						done = true;
						factoryDecls = new HashSet(annotationDecls.values());
					}
				}
				if (factoryDecls != null && factoryDecls.size() > 0) {
					final AnnotationProcessor processor = factory
							.getProcessorFor(factoryDecls, processorEnv);
					if (processor != null)
					{
						if ( DEBUG ) trace( "runAPT: invoking processor " + processor.getClass().getName() );
						processor.process();
					}
				}

				if (annotationDecls.isEmpty() || done)
					break;
			}
			// TODO: (theodora) log unclaimed annotations.

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

			final Set<IFile> allGeneratedFiles = new HashSet<IFile>();
			Set<IFile> modifiedFiles = new HashSet<IFile>();
			Map<IFile, Boolean> filesMap = processorEnv.getGeneratedFiles();
			for (Map.Entry<IFile, Boolean> entry : filesMap.entrySet()) {
				allGeneratedFiles.add(entry.getKey());
				if (entry.getValue()) {
					modifiedFiles.add(entry.getKey());
				}
			}
			
			// any files that were generated for this parent on the last
			// run, but are no longer generated should be removed
			Set<IFile> deletedFiles = cleanupNoLongerGeneratedFiles( processorEnv.getFile(), lastGeneratedFiles, allGeneratedFiles, gfm );

			APTResult result = new APTResult( modifiedFiles, deletedFiles, processorEnv.getTypeDependencies() );
			processorEnv.close();
			return result;

			// log unclaimed annotations.
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return EMPTY_APT_RESULT;
	}

	private static Set<IFile> cleanupAllGeneratedFilesForParent( IFile parent )
	{
		GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( parent.getProject() );
		Set<IFile> lastGeneratedFiles = gfm.getGeneratedFilesForParent( parent );
		return cleanupNoLongerGeneratedFiles( parent, lastGeneratedFiles, (Set<IFile>)Collections.emptySet(), gfm );
	}
	
	private static Set<IFile> cleanupNoLongerGeneratedFiles( 
		IFile parent, Set<IFile> lastGeneratedFiles, Set<IFile> newGeneratedFiles,
		GeneratedFileManager gfm )
	{
		HashSet<IFile> deletedFiles = new HashSet<IFile>();
		for ( IFile f : lastGeneratedFiles )
		{
			if ( ! newGeneratedFiles.contains( f ) )
			{
				if ( DEBUG ) trace ( "runAPT:  File " + f + " is no longer a generated file for " + parent );
				try
				{
					if ( gfm.deleteGeneratedFile( f, parent, null ) )
						deletedFiles.add( f );
				}
				catch ( CoreException ce )
				{
					// TODO - handle this exception nicely
					ce.printStackTrace();
				}
			}
		}
		return deletedFiles;
	}
	
	/**
	 * invoking annotation processors respecting apt semantics.
	 */
	private static void checkAnnotations(
			final List<AnnotationProcessorFactory> factories,
			final Map<String, AnnotationTypeDeclaration> declarations,
			final ProcessorEnvImpl env) {
		for (int i = 0, size = factories.size(); i < size; i++) {
			final AnnotationProcessorFactory factory = (AnnotationProcessorFactory) factories
					.get(i);
			final Set<AnnotationTypeDeclaration> factoryDecls = getAnnotations(
					factory, declarations);
			final AnnotationProcessor processor = factory.getProcessorFor(
					factoryDecls, env);
			processor.process();
			if (declarations.isEmpty())
				return;
		}
		// log unclaimed annotations.
	}

	private static Map<String, AnnotationTypeDeclaration> getAnnotationTypeDeclarations(
			CompilationUnit astCompilationUnit, ProcessorEnvImpl env) {
		final List<Annotation> instances = new ArrayList<Annotation>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		astCompilationUnit.accept(new AnnotationVisitor(instances));
		final Map<String, AnnotationTypeDeclaration> decls = new HashMap<String, AnnotationTypeDeclaration>();
		for (int i = 0, size = instances.size(); i < size; i++) {
			final Annotation instance = instances.get(i);
			final ITypeBinding annoType = instance.resolveTypeBinding();
			if (annoType == null)
				continue;
			final TypeDeclarationImpl annoDecl = Factory.createReferenceType(
					annoType, env);
			if (annoDecl.kind() == EclipseMirrorImpl.MirrorKind.TYPE_ANNOTATION)
				decls.put(annoDecl.getQualifiedName(),
						(AnnotationTypeDeclaration) annoDecl);
		}
		return decls;
	}

	/**
	 * @return the set of {@link AnnotationTypeDeclaration} that {@link #factory} supports or null
	 *         if the factory doesn't support any of the declarations.
	 *         If the factory supports "*", then the empty set will be returned
	 *
	 * This method will destructively modify {@link #declarations}. Entries will be removed from
	 * {@link #declarations} as the declarations are being added into the returned set.
	 */
	private static Set<AnnotationTypeDeclaration> getAnnotations(
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
			if (typeName.equals("*")) {
				declarations.clear();
				return Collections.emptySet();
			} else if (typeName.endsWith("*")) {
				final String prefix = typeName.substring(0,
						typeName.length() - 2);
				for (Iterator<Map.Entry<String, AnnotationTypeDeclaration>> entries = declarations
						.entrySet().iterator(); entries.hasNext();) {
					final Map.Entry<String, AnnotationTypeDeclaration> entry = entries
							.next();
					final String key = entry.getKey();
					if (key.startsWith(prefix)) {
						fDecls.add((AnnotationTypeDeclaration) entry.getValue());
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
	
	
	public static void trace( String s )
	{
		if (DEBUG)
		{
			System.out.println( "[" + Thread.currentThread().getName() + "][" + APTDispatch.class.getName() + "] " + s );
			System.out.flush();
		}
	}
	
	public static final APTResult EMPTY_APT_RESULT = new APTResult( (Set<IFile>)Collections.emptySet(), (Set<IFile>)Collections.emptySet(), (Set<String>)Collections.emptySet() );
	
	public static final boolean DEBUG = false;
}
