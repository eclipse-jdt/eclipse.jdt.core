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
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/** Base environment to be used during reconcile or build */
public abstract class AbstractCompilationEnv
	extends BaseProcessorEnv
	implements EclipseAnnotationProcessorEnvironment {

	// Bugzilla 188185: accept "-AenableTypeGenerationInEditor" as well as "enableTypeGenerationInEditor".
	private final static String RTTG_ENABLED_DASH_A_OPTION = "-A" + AptPreferenceConstants.RTTG_ENABLED_OPTION; //$NON-NLS-1$
	private final static String PROCESSING_IN_EDITOR_DISABLED_DASH_A_OPTION = "-A" + AptPreferenceConstants.PROCESSING_IN_EDITOR_DISABLED_OPTION; //$NON-NLS-1$

	private Set<AnnotationProcessorListener> _listeners = null;

	protected List<APTProblem> _problems = new ArrayList<>();
	private boolean _isClosed = false;

	EnvCallback _callback;

    private Set<IFile> _allGeneratedSourceFiles = new HashSet<>();
    private Set<IFile> _modifiedGeneratedSourceFiles = new HashSet<>();

    /**
	 * Currently open dom pipeline, used to request type bindings.
	 */
	protected ASTRequestor _requestor;

	/**
	 * The processor that is currently being executed, or null if processing is not underway.
	 */
	private AnnotationProcessorFactory _currentProcessorFactory = null;

	/**
	 * True if the currently active processor will be called during reconcile as well as build.
	 * Takes into account project settings, factory path, and processor options.
	 */
	private boolean _currentProcessorFactoryWillReconcile;

	public static interface EnvCallback {
		public void run(AbstractCompilationEnv env);
	}

	public static void newReconcileEnv(ReconcileContext reconcileContext,  EnvCallback callback)
	{
		assert reconcileContext != null : "reconcile context is null"; //$NON-NLS-1$
		ReconcileEnv env = ReconcileEnv.newEnv(reconcileContext);
		env._callback = callback;
		env.openPipeline();
	}

    public static void newBuildEnv(
    		BuildContext[] filesWithAnnotations,
    		final BuildContext[] additionalFiles,
    		IJavaProject javaProj,
    		EnvCallback callback)
    {
    	assert filesWithAnnotations != null : "missing files"; //$NON-NLS-1$
	    boolean isTestCode;
		if (filesWithAnnotations != null && filesWithAnnotations.length > 0) {
			isTestCode = filesWithAnnotations[0].isTestCode();
		} else if (additionalFiles != null && additionalFiles.length > 0) {
			isTestCode = additionalFiles[0].isTestCode();
		} else {
			isTestCode = false;
		}
		// note, we are not reading any files.
		BuildEnv env = new BuildEnv(filesWithAnnotations, additionalFiles, javaProj, isTestCode);
		env._callback = callback;
		env.createASTs(filesWithAnnotations);
    }

    /**
     * Determine whether a processor wants to be called during reconcile. By default
     * processors are called during both build and reconcile, but a processor can choose
     * not to be called during reconcile by reporting
     * {@link AptPreferenceConstants#PROCESSING_IN_EDITOR_DISABLED_OPTION}
     * in its supportedOptions() method.
     * @return false if the processor reports PROCESSING_IN_EDITOR_DISABLED_OPTION.
     * This does not consider project or factory path settings.
     */
    public static boolean doesFactorySupportReconcile(AnnotationProcessorFactory factory) {
    	Collection<String> options = factory.supportedOptions();
    	return options == null ||
    		(!options.contains(AptPreferenceConstants.PROCESSING_IN_EDITOR_DISABLED_OPTION) &&
    		!options.contains(PROCESSING_IN_EDITOR_DISABLED_DASH_A_OPTION));
    }

	AbstractCompilationEnv(
			CompilationUnit compilationUnit,
			IFile file,
			IJavaProject javaProj,
			Phase phase, boolean isTestCode)
	{
		super(compilationUnit, file, javaProj, phase, isTestCode);
	}

	@Override
	protected IBinding getBindingFromKey(String key, ICompilationUnit unit) {
		return _requestor.createBindings(new String[] {key})[0];
	}

	@Override
	public void addListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if(_listeners == null )
			_listeners = new HashSet<>();
		_listeners.add(listener);
    }

    @Override
	public void removeListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if( _listeners == null ) return;
		_listeners.remove(listener);
    }

	public Set<AnnotationProcessorListener> getProcessorListeners()
	{
		if( _listeners == null )
			return Collections.emptySet();
		// Return a copy, to avoid ConcurrentModificationException if a listener
		// removes itself in response to the callback.
		return new HashSet<>(_listeners);
	}

	@Override
	public Map<String, String> getOptions()
    {
        final HashMap<String, String> options = new HashMap<>(_options);
		options.put("phase", getPhase().toString()); //$NON-NLS-1$
		return options;
    }

	abstract public CompilationUnit getASTFrom(final IFile file);

	@Override
	public CompilationUnit getAST(){
		return _astRoot;
	}

	@Override
	public EclipseMessager getMessager()
    {
		checkValid();
		return new MessagerImpl(this);
	}

	abstract void addMessage(
			IFile resource,
		    int start,
			int end,
            Severity severity,
            String msg,
            int line,
            String[] arguments);

	public List<? extends CategorizedProblem> getProblems(){
		checkValid();
		if( !_problems.isEmpty() )
			EnvUtil.updateProblemLength(_problems, getAstCompilationUnit());
		return _problems;
	}

	APTProblem createProblem(
	    		IFile resource,
			    int start,
				int end,
	            Severity severity,
	            String msg,
	            int line,
	            String[] arguments)
    {
    	// end-1 since IProblem ending offsets are inclusive but DOM layer
    	// ending offsets are exclusive.
    	final APTProblem newProblem =
        	new APTProblem(msg, severity, resource, start, end-1, line, arguments, !_currentProcessorFactoryWillReconcile);
    	return newProblem;
    }

	@Override
	public abstract Filer getFiler();

	public void addGeneratedSourceFile( IFile f, boolean contentsChanged ) {
		if (!f.toString().endsWith(".java")) { //$NON-NLS-1$
			throw new IllegalArgumentException("Source files must be java source files, and end with .java"); //$NON-NLS-1$
		}

		boolean addedToAll = _allGeneratedSourceFiles.add(f);
		boolean addedToMod = false;
		if (contentsChanged)
			addedToMod = _modifiedGeneratedSourceFiles.add(f);
		if (AptPlugin.DEBUG_COMPILATION_ENV) {
			AptPlugin.trace("add generated file " + f + " to env " + this + //$NON-NLS-1$ //$NON-NLS-2$
					"; addToAll = " + addedToAll + "; addToMod = " + addedToMod + //$NON-NLS-1$ //$NON-NLS-2$
					"; contentsChanged = " + contentsChanged); //$NON-NLS-1$
		}
	}

	public void addGeneratedNonSourceFile(final IFile file) {
		_allGeneratedSourceFiles.add(file);
	}

    public Set<IFile> getAllGeneratedFiles() {
    	return _allGeneratedSourceFiles;
    }

    public Set<IFile> getModifiedGeneratedFiles() {
    	return _modifiedGeneratedSourceFiles;
    }

	/**
	 * @return true iff source files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedSourceFiles(){ return !_allGeneratedSourceFiles.isEmpty();  }


	/**
	 * @return all annotation types in the current compilation unit.
	 */
	public Map<String, AnnotationTypeDeclaration> getAnnotationTypes()
    {
    	checkValid();
    	final List<Annotation> instances = new ArrayList<>();
		final Map<String, AnnotationTypeDeclaration> decls =
			new HashMap<>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		_astRoot.accept(visitor);

		for (int instanceIndex=0, size = instances.size(); instanceIndex < size; instanceIndex++) {
			final Annotation instance = instances.get(instanceIndex);
			final ITypeBinding annoType = instance.resolveTypeBinding();
			if (annoType == null)
				continue;
			final TypeDeclarationImpl decl =
				Factory.createReferenceType(annoType, this);
			if (decl != null && decl.kind() == EclipseMirrorObject.MirrorKind.TYPE_ANNOTATION){
				final AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration)decl;
				decls.put(annoDecl.getQualifiedName(), annoDecl);
			}
		}

		return decls;
    }

	/* package */ void checkValid()
	{
		if( _isClosed )
			throw new IllegalStateException("Environment has expired"); //$NON-NLS-1$
	}

	// Call this after each file; cf. BuildEnv#beginFileProcessing()
	protected void completedProcessing() {
		_modifiedGeneratedSourceFiles.clear();
	}

	public void close(){
		if (isClosed())
			return;
		if(_listeners != null)
			_listeners.clear();
		_problems = null;
		_typeCache.clear();
		_packageRootsCache = null;
		_isClosed = true;
		_callback = null;
		_requestor = null;
		_allGeneratedSourceFiles = null;
		_modifiedGeneratedSourceFiles = null;
		if (AptPlugin.DEBUG_COMPILATION_ENV) AptPlugin.trace(
				"closed env " + this); //$NON-NLS-1$
	}

	boolean isClosed(){ return _isClosed; }

	/**
	 * Check typeName to ensure it doesn't contain any bogus characters.
	 */
	public void validateTypeName(String typeName) throws CoreException
	{
        Map<String, String> options = getJavaProject().getOptions(true);
        String sourceLevel = options.get(JavaCore.COMPILER_SOURCE);
        String complianceLevel = options.get(JavaCore.COMPILER_COMPLIANCE);
        String preview = options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES);
        IStatus status = JavaConventions.validateJavaTypeName(typeName, sourceLevel, complianceLevel, preview);
        if (status.matches(IStatus.ERROR)) {
        	throw new CoreException(status);
        }
	}

	public AnnotationProcessorFactory getCurrentProcessorFactory() {
		return _currentProcessorFactory;
	}

	/**
	 * @param factory a processor factory, or null to indicate processing is over.
	 * @param willReconcile true if the processor will be called during reconcile as well as during build,
	 * taking into account project settings, factory path, and processor options.
	 */
	public void setCurrentProcessorFactory(AnnotationProcessorFactory factory, boolean willReconcile)
	{
		_currentProcessorFactory = factory;
		_currentProcessorFactoryWillReconcile = willReconcile;
	}

	public boolean currentProcessorSupportsRTTG()
	{
		AnnotationProcessorFactory factory = getCurrentProcessorFactory();
		if (null == factory) {
			return false;
		}
		Collection<String> options = factory.supportedOptions();
		if (null == options) {
			return false;
		}
		return options.contains(AptPreferenceConstants.RTTG_ENABLED_OPTION) ||
			options.contains(RTTG_ENABLED_DASH_A_OPTION);
	}
}
