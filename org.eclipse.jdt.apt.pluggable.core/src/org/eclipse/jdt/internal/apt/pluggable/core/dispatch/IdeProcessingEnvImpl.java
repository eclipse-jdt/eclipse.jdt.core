/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.dispatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.Element;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptCompilationParticipant;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.generatedfile.FileGenerationResult;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.apt.pluggable.core.filer.IdeFilerImpl;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.apt.model.IElementInfo;

/**
 * Implementation of ProcessingEnvironment when running inside IDE.
 * The lifetime of this object corresponds to the lifetime of the
 * {@link IdeAnnotationProcessorManager} that owns it.
 * @see org.eclipse.jdt.internal.compiler.apt.dispatch.BatchProcessingEnvImpl
 */
public abstract class IdeProcessingEnvImpl extends BaseProcessingEnvImpl {
	
	private final IdeAnnotationProcessorManager _dispatchManager;
	private final IJavaProject _javaProject;
	protected final AptProject _aptProject;

	public IdeProcessingEnvImpl(IdeAnnotationProcessorManager dispatchManager,
			IJavaProject jproject, Compiler compiler) 
	{
		_dispatchManager = dispatchManager;
		_javaProject = jproject;
		_compiler = compiler;
		_aptProject = AptPlugin.getAptProject(jproject);
		_filer = new IdeFilerImpl(_dispatchManager, this);
		_messager = new IdeMessagerImpl(_dispatchManager, this);
	}
	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.ProcessingEnvironment#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
	public Map<String, String> getOptions() {
		if (null == _processorOptions) {
			// Java 5 processor options include items on the command line such as -s,
			// -classpath, etc., but Java 6 options only include the options specified
			// with -A, which will have been parsed into key/value pairs with no dash.
			Map<String, String> allOptions = AptConfig.getProcessorOptions(_javaProject);
			Map<String, String> procOptions = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : allOptions.entrySet()) {
				if (!entry.getKey().startsWith("-")) { //$NON-NLS-1$
					procOptions.put(entry.getKey(), entry.getValue());
				}
			}
			procOptions.put("phase", getPhase().toString()); //$NON-NLS-1$
			_processorOptions = Collections.unmodifiableMap(procOptions);
		}
		return _processorOptions;
	}
	
	public AptProject getAptProject() {
		return _aptProject;
	}
	
	public IJavaProject getJavaProject() {
		return _javaProject;
	}
	
	public IProject getProject() {
		return _javaProject.getProject();
	}

	/**
	 * @return whether this environment supports building or reconciling.
	 */
	public abstract Phase getPhase();

	/**
	 * Get the IFile that contains or represents the specified source element.
	 * If the element is a package, get the IFile corresponding to its
	 * package-info.java file.  If the element is a top-level type, get the
	 * IFile corresponding to its type.  If the element is a nested element
	 * of some sort (nested type, method, etc.) then get the IFile corresponding
	 * to the containing top-level type.
	 * If the element is not a source type at all, then return null.
	 * @param elem
	 * @return may be null
	 */
	public IFile getEnclosingIFile(Element elem) {
		// if this cast fails it could be that a non-Eclipse element got passed in somehow.
		IElementInfo impl = (IElementInfo)elem;
		String name = impl.getFileName();
		if (name == null) {
			return null;
		}
		// The name will be workspace-relative, e.g., /project/src/packages/File.java.
		IFile file = _javaProject.getProject().getParent().getFile(new Path(name));
		return file;
	}

	/**
	 * Inform the environment that a new Java file has been generated.
	 * @param result must be non-null
	 */
	public void addNewUnit(FileGenerationResult result) {
		AptCompilationParticipant.getInstance().addJava6GeneratedFile(result.getFile());
		addNewUnit(_dispatchManager.findCompilationUnit(result.getFile()));
	}
	
	/**
	 * Inform the environment that a new non-Java file has been generated.
	 * This file will not be submitted to a subsequent round of processing in
	 * the current build, even if the file happens to be in a source location
	 * and named with a Java-like name.  However, its dependencies will be
	 * tracked in the same manner as Java files, e.g., it will be deleted
	 * if changes in source cause it to no longer be generated.
	 * @param file must be non-null
	 */
	public void addNewResource(IFile file) {
		AptCompilationParticipant.getInstance().addJava6GeneratedFile(file);
	}

	public boolean currentProcessorSupportsRTTG()
	{
		// Reconcile time type generation is not currently enabled for Java 6 processors
		return false;
	}

}
