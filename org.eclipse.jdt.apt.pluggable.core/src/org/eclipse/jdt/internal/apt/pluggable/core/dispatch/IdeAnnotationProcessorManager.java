/*******************************************************************************
 * Copyright (c) 2007, 2018 BEA Systems, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - Bug 478427
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.dispatch;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Processor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.IServiceFactory;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.apt.dispatch.ProcessorInfo;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.builder.ICompilationUnitLocator;

/**
 * Java 6 annotation processor manager used when compiling within the IDE.
 * @see org.eclipse.jdt.internal.compiler.apt.dispatch.BatchAnnotationProcessorManager
 */
public class IdeAnnotationProcessorManager extends BaseAnnotationProcessorManager {

	private IJavaProject _javaProject;
	private ICompilationUnitLocator _cuLocator;
	private Map<IServiceFactory, FactoryPath.Attributes> _processorFactories;
	private Iterator<Entry<IServiceFactory, Attributes>> _processorIter;

	/**
	 * Initialize the processor manager for a particular project.  It is an error
	 * to initialize a manager more than once.
	 *
	 * @param javaProject must be an instanceof IJavaProject.  (But it can't be
	 * prototyped that way because the abstract base class must compile without
	 * Eclipse platform code.)
	 */
	@Override
	public void configureFromPlatform(Compiler compiler, Object compilationUnitLocator, Object javaProject, boolean isTestCode) {
		_javaProject = (IJavaProject) javaProject;
		_cuLocator = (ICompilationUnitLocator) compilationUnitLocator;
		if (null != _processingEnv) {
			throw new IllegalStateException(
					"Calling configure() more than once on an AnnotationProcessorManager is not supported"); //$NON-NLS-1$
		}
		// If it's a CompilationUnitProblemFinder, we're in reconcile phase.  Else it's build.
		if (compiler instanceof CompilationUnitProblemFinder) {
			_processingEnv = new IdeReconcileProcessingEnvImpl(this, _javaProject, compiler, isTestCode);
		} else {
			_processingEnv = new IdeBuildProcessingEnvImpl(this, _javaProject, compiler, isTestCode);
		}
		if (Apt6Plugin.DEBUG) {
			Apt6Plugin.trace("Java 6 annotation processor manager initialized for compiler " +
					compiler.toString() + " on project " + _javaProject.getElementName());
		}
	}

	/**
	 * If this project has a ProcessorPath defined, use it.  Else, construct
	 * one from the classpath.
	 */
	@Override
	public ProcessorInfo discoverNextProcessor() {
		// _processorIter gets initialized the first time through processAnnotations()
		if (_processorIter.hasNext()) {
			Entry<IServiceFactory, Attributes> entry = _processorIter.next();
			Processor p;
			try {
				p = (Processor)entry.getKey().newInstance();
				p.init(_processingEnv);
				ProcessorInfo pi = new ProcessorInfo(p);
				if (Apt6Plugin.DEBUG) {
					Apt6Plugin.trace("Discovered processor " + p.toString());
				}
				_processors.add(pi);
				return pi;
			} catch (CoreException | NoClassDefFoundError e) {
				Apt6Plugin.log(e, "Unable to create instance of annotation processor " + entry.getKey()); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public void reportProcessorException(Processor p, Exception e) {
		Apt6Plugin.log(e, "Exception thrown by Java annotation processor " + p); //$NON-NLS-1$
	}

	/**
	 * @return an ICompilationUnit corresponding to the specified file.  In IDE mode this
	 * will be backed by an org.eclipse.jdt.internal.core.builder.SourceFile.
	 */
	public ICompilationUnit findCompilationUnit(IFile file) {
		return _cuLocator.fromIFile(file);
	}

	/**
	 * In IDE mode, we are able to determine whether there are no processors.  If that's the case,
	 * then we can avoid doing the work of walking the ASTs to search for annotations.  We still
	 * need to clean up no-longer-generated files when the factory path is changed, but the best
	 * way to do that is to force a clean build.
	 * @see BaseAnnotationProcessorManager#processAnnotations(CompilationUnitDeclaration[], ReferenceBinding[], boolean)
	 */
	@Override
	public void processAnnotations(CompilationUnitDeclaration[] units, ReferenceBinding[] referenceBindings, boolean isLastRound) {
		if (null == _processorFactories ) {
			_processorFactories = AnnotationProcessorFactoryLoader.getLoader().getJava6FactoriesAndAttributesForProject(_javaProject);
			_processorIter = _processorFactories.entrySet().iterator();
		}
		if (!_processorFactories.isEmpty()) {
			super.processAnnotations(units, referenceBindings, isLastRound);
		}
	}
}
