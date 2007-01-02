/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * This class is the central dispatch point for Java 6 annotation processing.
 * This is created and configured by the JDT core; specifics depend on how
 * compilation is being performed, ie from the command line, via the Tool
 * interface, or within the IDE.  This class manages the discovery of annotation
 * processors and other information spanning multiple rounds of processing;
 * context that is valid only within a single round is managed by
 * {@link RoundDispatcher}. 
 *  
 * TODO: do something useful with _supportedOptions and _supportedAnnotationTypes.
 */
public class AnnotationProcessorManager extends AbstractAnnotationProcessorManager
		implements IProcessorProvider
{
	
	private PrintWriter _out;
	private PrintWriter _err;
	private ProcessingEnvironment _processingEnv;
	
	/**
	 * The list of processors that have been loaded so far.  A processor on this
	 * list has been initialized, but may not yet have been called to process().
	 */
	private List<ProcessorInfo> _processors;
	
	/**
	 * Processors that have been set by calling CompilationTask.setProcessors().
	 */
	private List<Processor> _setProcessors = null;
	private Iterator<Processor> _setProcessorIter = null;
	
	/**
	 * Processors named with the -processor option on the command line.
	 */
	private List<String> _commandLineProcessors;
	private Iterator<String> _commandLineProcessorIter = null;
	
	private ServiceLoader<Processor> _serviceLoader = null;
	
	private Main _batchCompiler = null;
	private boolean _isFirstRound = true;
	private ClassLoader _procLoader;
	private Iterator<Processor> _serviceLoaderIter;
	
	/**
	 * An AnnotationProcessorManager cannot be used until its
	 * {@link #configure(Main, String[])} method has been called.
	 */
	public AnnotationProcessorManager() 
	{
	}

	@Override
	public void configure(Main batchCompiler, String[] commandLineArguments) {
		if (null != _batchCompiler) {
			throw new IllegalStateException(
					"Calling configure() more than once on an AnnotationProcessorManager is not supported");
		}
		_batchCompiler  = batchCompiler;
		_processors = new ArrayList<ProcessorInfo>();
		BatchProcessingEnvImpl processingEnv = new BatchProcessingEnvImpl(this, batchCompiler, commandLineArguments);
		_processingEnv = processingEnv;
		_procLoader = processingEnv.getFileManager().getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_PATH);
		_commandLineProcessors = parseCommandLineProcessors(commandLineArguments);
		if (null != _commandLineProcessors) {
			_commandLineProcessorIter = _commandLineProcessors.iterator();
		}
	}
	
	/**
	 * If a -processor option was specified in command line arguments,
	 * parse it into a list of qualified classnames.
	 * @param commandLineArguments contains one string for every space-delimited token on the command line
	 * @return a list of qualified classnames, or null if there was no -processor option.
	 */
	private List<String> parseCommandLineProcessors(String[] commandLineArguments) {
		List<String> result = null;
		for (int i = 0; i < commandLineArguments.length; ++i) {
			String option = commandLineArguments[i];
			if ("-processor".equals(option)) {
				result = new ArrayList<String>();
				String procs = commandLineArguments[++i];
				for (String proc : procs.split(",")) {
					result.add(proc);
				}
				break;
			}
		}
		return result;
	}

	@Override
	public ICompilationUnit[] getNewUnits() {
		return ((BatchFilerImpl)_processingEnv.getFiler()).getNewUnits();
	}

	/**
	 * A single "round" of processing, in the sense implied in
	 * {@link javax.lang.annotation.processing.Processor}.
	 * <p>
	 * The Java 6 Processor spec contains ambiguities about how processors that support "*" are
	 * handled. Eclipse tries to match Sun's implementation in javac. What that actually does is
	 * analogous to inspecting the set of annotions found in the root units and adding an
	 * "imaginary" annotation if the set is empty. Processors are then called in order of discovery;
	 * for each processor, the intersection between the set of root annotations and the set of
	 * annotations the processor supports is calculated, and if it is non-empty, the processor is
	 * called. If the processor returns <code>true</code> then the intersection (including the
	 * imaginary annotation if one exists) is removed from the set of root annotations and the loop
	 * continues, until the set is empty. Of course, the imaginary annotation is not actually
	 * included in the set of annotations passed in to the processor. A processor's process() method
	 * is not called until its intersection set is non-empty, but thereafter it is called on every
	 * round. Note that even if a processor is not called in the first round, if it is called in
	 * subsequent rounds, it will be called in the order in which the processors were discovered,
	 * rather than being added to the end of the list.
	 */
	@Override
	public void processAnnotations(CompilationUnitDeclaration[] units, boolean isLastRound) 
	{
		BatchRoundEnvImpl roundEnv = new BatchRoundEnvImpl(units, isLastRound);
		if (_isFirstRound) {
			_isFirstRound = false;
		}
		RoundDispatcher dispatcher = new RoundDispatcher(this, roundEnv, roundEnv.getRootAnnotations());
		dispatcher.round();
	}

	@Override
	public ProcessorInfo discoverNextProcessor() {
		if (null != _setProcessors) {
			// If setProcessors() was called, use that list until it's empty and then stop.
			if (_setProcessorIter.hasNext()) {
				Processor p = _setProcessorIter.next();
				p.init(_processingEnv);
				ProcessorInfo pi = new ProcessorInfo(p);
				_processors.add(pi);
				return pi;
			}
			return null;
		}
		
		if (null != _commandLineProcessors) {
			// If there was a -processor option, iterate over processor names, 
			// creating and initializing processors, until no more names are found, then stop.
			if (_commandLineProcessorIter.hasNext()) {
				String proc = _commandLineProcessorIter.next();
				try {
					Class<?> clazz = _procLoader.loadClass(proc);
					Object o = clazz.newInstance();
					Processor p = (Processor) o;
					p.init(_processingEnv);
					ProcessorInfo pi = new ProcessorInfo(p);
					_processors.add(pi);
					return pi;
				} catch (Exception e) {
					// TODO: better error handling
					throw new AbortCompilation(null, e);
				}
			}
			return null;
		}
		
		// if no processors were explicitly specified with setProcessors() 
		// or the command line, search the processor path with ServiceLoader.
		if (null == _serviceLoader ) {
			_serviceLoader = ServiceLoader.load(Processor.class, _procLoader);
			_serviceLoaderIter = _serviceLoader.iterator();
		}
		try {
			if (_serviceLoaderIter.hasNext()) {
				Processor p = _serviceLoaderIter.next();
				p.init(_processingEnv);
				ProcessorInfo pi = new ProcessorInfo(p);
				_processors.add(pi);
				return pi;
			}
		} catch (ServiceConfigurationError e) {
			// TODO: better error handling
			throw new AbortCompilation(null, e);
		}
		return null;
	}

	@Override
	public List<ProcessorInfo> getDiscoveredProcessors() {
		return _processors;
	}

	@Override
	public void reset() {
		((BatchFilerImpl)_processingEnv.getFiler()).reset();
	}

	@Override
	public void setErr(PrintWriter err) {
		_err = err;
	}
	
	@Override
	public void setOut(PrintWriter out) {
		_out = out;
	}

	@Override
	public void setProcessors(Object[] processors) {
		if (!_isFirstRound) {
			throw new IllegalStateException("setProcessors() cannot be called after processing has begun");
		}
		// Cast all the processors here, rather than failing later.
		// But don't call init() until the processor is actually needed.
		_setProcessors = new ArrayList<Processor>(processors.length);
		for (Object o : processors) {
			Processor p = (Processor)o;
			_setProcessors.add(p);
		}
		_setProcessorIter = _setProcessors.iterator();

		// processors set this way take precedence over anything on the command line 
		_commandLineProcessors = null;
		_commandLineProcessorIter = null;
	}

}
