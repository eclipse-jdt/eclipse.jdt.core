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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.APTDispatch.APTResult;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.env.EclipseRoundCompleteEvent;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.apt.core.util.ScannerUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/*package*/ class APTDispatchRunnable implements IWorkspaceRunnable
{
	private final IFile _file;
	private final ICompilationUnit _compilationUnit;
	private final IJavaProject _javaProject;
	private final List<AnnotationProcessorFactory> _factories;
	private final String _phaseName;
	private final String _fileName;
	
	private  APTResult _result;

	/*package*/ APTDispatchRunnable( IFile file, IJavaProject javaProject, List<AnnotationProcessorFactory> factories)
	{
		_compilationUnit = null;
		_file = file;
		_javaProject = javaProject;
		_factories = factories;
		_phaseName =  "build"; //$NON-NLS-1$
		_fileName =  _file.toString();
	}

	/*package*/ APTDispatchRunnable( ICompilationUnit cu, IJavaProject javaProject, List<AnnotationProcessorFactory> factories)
	{
		_compilationUnit = cu;
		_file = null;
		_javaProject = javaProject;
		_factories = factories;
		_phaseName =  "reconcile"; //$NON-NLS-1$
		_fileName =  _compilationUnit.getResource().toString();
	}
	
	public APTResult getResult() { return _result; }
	
	public void run(IProgressMonitor monitor) 
	{
		assert( _file == null || _compilationUnit == null ) : "Either _file should be null or _compilationUnit should be null."; //$NON-NLS-1$	
		
		//
		//  bail-out early if there aren't factories, or if there aren't any annotation instances
		// 
		if ( _factories == null || _factories.size() == 0 || 
				( _compilationUnit != null && ! ScannerUtil.hasAnnotationInstance( _compilationUnit ) ) ||
				( _file!= null && !  ScannerUtil.hasAnnotationInstance( _file ) ) )
		{
			// tracing
			if ( AptPlugin.DEBUG ) 
			{			
				String msg;
				if ( _factories == null || _factories.size() == 0 )
					msg = "no AnnotationProcessoryFactory instances registered."; //$NON-NLS-1$
				else
					msg = "no annotation instances in file."; //$NON-NLS-1$
				trace( "run():  leaving early because there are " + msg ); //$NON-NLS-1$
			}
			
			
			IFile f;
			if ( _file != null )
				f = _file;
			else
				f = (IFile)_compilationUnit.getResource();
			
			Set<IFile> deletedFiles = cleanupAllGeneratedFilesForParent( f, _compilationUnit );
			
			if ( deletedFiles.size() == 0 )
				_result =  EMPTY_APT_RESULT;
			else
				_result = new APTResult( Collections.<IFile>emptySet(), 
										 deletedFiles, 
										 Collections.<String>emptySet(),
										 Collections.<IFile, List<IProblem>>emptyMap(), false );
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
	
	private APTResult runAPT(
			final List<AnnotationProcessorFactory> factories,
			final ProcessorEnvImpl processorEnv) 
	{
		try {
			if (factories.size() == 0)
			{
				if ( AptPlugin.DEBUG ) trace( "runAPT: leaving early because there are no factories"); //$NON-NLS-1$
				return EMPTY_APT_RESULT;
			}
				
			if ( ! processorEnv.getFile().exists() )
			{
				if ( AptPlugin.DEBUG ) trace( "runAPT: leaving early because file doesn't exist"); //$NON-NLS-1$
				return EMPTY_APT_RESULT;
			}				
		
			final Map<String, AnnotationTypeDeclaration> annotationDecls = getAnnotationTypeDeclarations(
					processorEnv.getAstCompilationUnit(), processorEnv);
			
			if (annotationDecls.isEmpty())
			{
				if ( AptPlugin.DEBUG ) trace( "runAPT:  leaving early because annotationDecls is empty" ); //$NON-NLS-1$
				return EMPTY_APT_RESULT;
			}

			GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( processorEnv.getJavaProject().getProject() );
			Set<IFile> lastGeneratedFiles = gfm.getGeneratedFilesForParent( processorEnv.getFile() );
			
			for (int i = 0, size = factories.size(); i < size; i++) {
				final AnnotationProcessorFactory factory = factories.get(i);
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
						if ( AptPlugin.DEBUG ) trace( "runAPT: invoking processor " + processor.getClass().getName() ); //$NON-NLS-1$
                        processorEnv.setLatestProcessor(processor);
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
			
			// BUGZILLA 103183 - reconcile-path disabled until type-generation in reconcile is turned on
			Set<IFile> deletedFiles = cleanupNoLongerGeneratedFiles( processorEnv.getFile(), processorEnv.getCompilationUnit(), lastGeneratedFiles, allGeneratedFiles, gfm );
			
			APTResult result = new APTResult( modifiedFiles, 
											  deletedFiles, 
											  processorEnv.getTypeDependencies(), 
											  processorEnv.getProblems(), processorEnv.getSourcePathChanged() );
			processorEnv.close();
			return result;

			// log unclaimed annotations.
		} catch (Throwable t) {
			AptPlugin.log(t, "Unexpected failure running APT " + _file); //$NON-NLS-1$
		}
		return EMPTY_APT_RESULT;
	}

	private Set<IFile> cleanupAllGeneratedFilesForParent( IFile parent, ICompilationUnit parentCompilationUnit )
	{
		GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( parent.getProject() );
		Set<IFile> lastGeneratedFiles = gfm.getGeneratedFilesForParent( parent );
		return cleanupNoLongerGeneratedFiles( parent, parentCompilationUnit, lastGeneratedFiles, Collections.<IFile>emptySet(), gfm );
	}
	
	private Set<IFile> cleanupNoLongerGeneratedFiles( 
		IFile parentFile, ICompilationUnit parentCompilationUnit, Set<IFile> lastGeneratedFiles, Set<IFile> newGeneratedFiles,
		GeneratedFileManager gfm )
	{
		HashSet<IFile> deletedFiles = new HashSet<IFile>();
			
		// make a copy into an array to avoid concurrent modification exceptions
		IFile[] files = lastGeneratedFiles.toArray( new IFile[ lastGeneratedFiles.size() ] );
		for ( int i = 0; i< files.length; i++ )
		{
			IFile f = files[i];
			if ( ! newGeneratedFiles.contains( f ) )
			{
				if ( AptPlugin.DEBUG ) trace( "runAPT:  File " + f + " is no longer a generated file for " + parentFile ); //$NON-NLS-1$ //$NON-NLS-2$
				try
				{
					if ( parentCompilationUnit == null )
					{
						if ( gfm.deleteGeneratedFile( f, parentFile, null ) )
							deletedFiles.add( f );
					}
					else 
					{
						if ( gfm.deleteGeneratedTypeInMemory( f, parentCompilationUnit, null ) )
							deletedFiles.add( f );
					}
				}
				catch ( CoreException ce )
				{
					AptPlugin.log(ce, "Could not clean up generated files"); //$NON-NLS-1$
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
			final AnnotationProcessorFactory factory = factories.get(i);
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
			CompilationUnit astCompilationUnit, BaseProcessorEnv env) {
		final List<Annotation> instances = new ArrayList<Annotation>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		astCompilationUnit.accept(visitor);
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
	
	private void trace( String s )
	{
		if (AptPlugin.DEBUG)
		{
			s = "[ phase = " + _phaseName + ", file = " + _fileName +" ]  " + s; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			System.out.println( "[" + APTDispatch.class.getName() + "][ thread= " + Thread.currentThread().getName() + " ]"+ s ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	
	public static final APTResult EMPTY_APT_RESULT = new APTResult();
	
}
