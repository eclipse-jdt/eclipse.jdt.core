/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/** Base environment to be used during reconcile or build */ 
public abstract class CompilationProcessorEnv 
	extends BaseProcessorEnv 
	implements EclipseAnnotationProcessorEnvironment{
	
	/** regex to identify substituted token in path variables */
	private static final String PATHVAR_TOKEN = "^%[^%/\\\\ ]+%.*"; //$NON-NLS-1$
	/** path variable meaning "workspace root" */
	private static final String PATHVAR_ROOT = "%ROOT%"; //$NON-NLS-1$
	/** path variable meaning "project root" */
	private static final String PATHVAR_PROJECTROOT = "%PROJECT.DIR%"; //$NON-NLS-1$

	private Set<AnnotationProcessorListener> _listeners = null;
	
	protected List<APTProblem> _problems = new ArrayList<APTProblem>();
	/**
	 * Processor options, including -A options.
	 * Set in ctor and then not changed.
	 */
	private Map<String, String> _options;
	private boolean _isClosed = false;
	
	public static ReconcileProcessorEnv newReconcileEnv(
			ReconcileContext reconcileContext,
			final IJavaProject javaProj ){
		
		assert reconcileContext != null : "reconcile context is null"; //$NON-NLS-1$
		return ReconcileProcessorEnv.newEnv(reconcileContext);
	}
    
    public static ProcessorEnvImpl newBuildEnv(
    		BuildContext[] filesWithAnnotation,
    		final BuildContext[] additionalFiles,
    		IJavaProject javaProj )
    {
    	assert filesWithAnnotation != null : "missing files"; //$NON-NLS-1$    	
    
		// note, we are not reading any files.
		return new ProcessorEnvImpl(filesWithAnnotation, additionalFiles, javaProj);
    }
	
	CompilationProcessorEnv(
			CompilationUnit compilationUnit,
			IFile file,
			IJavaProject javaProj,
			Phase phase)
	{
		super(compilationUnit, file, javaProj, phase);
		initOptions(javaProj);
	}
	
	public void addListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if(_listeners == null )
			_listeners = new HashSet<AnnotationProcessorListener>();
		_listeners.add(listener);
    }

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
		return Collections.unmodifiableSet(_listeners);
	}
	
	/**
     * Set the _options map based on the current project/workspace settings.
     * There is a bug in Sun's apt implementation: it parses the command line 
     * incorrectly, such that -Akey=value gets added to the options map as 
     * key "-Akey=value" and value "".  In order to support processors written 
     * to run on Sun's apt as well as processors written without this bug
     * in mind, we populate the map with two copies of every option, one the
     * expected way ("key" / "value") and the other the Sun way 
     * ("-Akey=value" / "").  We make exceptions for the non-dash-A options
     * that we set automatically, such as -classpath, -target, and so forth;
     * since these wouldn't have come from a -A option we don't construct a
     * -Akey=value variant.
     * 
     * Called from constructor.  A new Env is constructed for each build pass,
     * so this will always be up to date with the latest settings.
	 */
	private void initOptions(IJavaProject jproj) {
		Map<String, String> procOptions = AptConfig.getProcessorOptions(jproj);
		_options = new HashMap<String, String>(procOptions.size() * 2);
		
		// Add configured options
		for (Map.Entry<String, String> entry : procOptions.entrySet()) {
			String value = resolveVarPath(jproj, entry.getValue());
			String key = entry.getKey();
			_options.put(key, value);
			if (!AptConfig.isAutomaticProcessorOption(key)) {
				String sunStyle;
				if (value != null) {
					sunStyle = "-A" + entry.getKey() + "=" + value; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					sunStyle = "-A" + entry.getKey(); //$NON-NLS-1$
				}
				_options.put(sunStyle, ""); //$NON-NLS-1$
			}
		}
	}

	/**
	 * If the value starts with a path variable such as %ROOT%, replace it with
	 * the absolute path.
	 * @param value the value of a -Akey=value command option
	 */
	private String resolveVarPath(IJavaProject jproj, String value) {
		if (value == null) {
			return null;
		}
		// is there a token to substitute?
		if (!Pattern.matches(PATHVAR_TOKEN, value)) {
			return value;
		}
		IPath path = new Path(value);
		String firstToken = path.segment(0);
		// If it matches %ROOT%/project, it is a project-relative path.
		if (PATHVAR_ROOT.equals(firstToken)) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource proj = root.findMember(path.segment(1));
			if (proj == null) {
				return value;
			}
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(2);
			IPath absoluteProjPath = proj.getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}
		
		// If it matches %PROJECT.DIR%/project, the path is relative to the current project.
		if (jproj != null && PATHVAR_PROJECTROOT.equals(firstToken)) {
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(1);
			IPath absoluteProjPath = jproj.getProject().getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}
		
		// otherwise it's a classpath-var-based path.
		String cpvName = firstToken.substring(1, firstToken.length() - 1);
		IPath cpvPath = JavaCore.getClasspathVariable(cpvName);
		if (cpvPath != null) {
			IPath resolved = cpvPath.append(path.removeFirstSegments(1));
			return resolved.toOSString();
		}
		else {
			return value;
		}
	}
	
	public Map<String, String> getOptions()
    {
        final HashMap<String, String> options = new HashMap<String, String>(_options);
		options.put("phase", getPhase().toString()); //$NON-NLS-1$
		return options;
    }
	
	abstract public CompilationUnit getASTFrom(final IFile file);
	
	public CompilationUnit getAST(){
		return _astRoot;
	}
	
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
        	new APTProblem(msg, severity, resource, start, end-1, line, arguments);
    	return newProblem;
    }
	
	public abstract Filer getFiler();
	
	/**
	 * @return all annotation types in the current compilation unit.
	 */
	public Map<String, AnnotationTypeDeclaration> getAnnotationTypes()
    {
    	checkValid();
    	final List<Annotation> instances = new ArrayList<Annotation>();
		final Map<String, AnnotationTypeDeclaration> decls = 
			new HashMap<String, AnnotationTypeDeclaration>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		_astRoot.accept(visitor);
			
		for (int instanceIndex=0, size = instances.size(); instanceIndex < size; instanceIndex++) {
			final Annotation instance = instances.get(instanceIndex);
			final ITypeBinding annoType = instance.resolveTypeBinding();
			if (annoType == null)
				continue;
			final TypeDeclarationImpl decl = 
				Factory.createReferenceType(annoType, this);
			if (decl.kind() == EclipseMirrorImpl.MirrorKind.TYPE_ANNOTATION){
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
	
	public void close(){
		if (isClosed()) 
			return; 
		if(_listeners != null)
			_listeners.clear();
		_problems = null;
		_isClosed = true;
	}
	
	boolean isClosed(){ return _isClosed; }
}
