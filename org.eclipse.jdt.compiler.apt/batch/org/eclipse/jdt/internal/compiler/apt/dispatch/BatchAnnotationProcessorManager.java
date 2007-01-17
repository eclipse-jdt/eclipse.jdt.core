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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * Java 6 annotation processor manager used when compiling from the command line
 * or via the javax.tools.JavaCompiler interface.
 * @see org.eclipse.jdt.internal.compiler.apt.ide.dispatch.IdeAnnotationProcessorManager
 */
public class BatchAnnotationProcessorManager extends BaseAnnotationProcessorManager
{
	
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
	private Iterator<Processor> _serviceLoaderIter;
	
	private ClassLoader _procLoader;
	
	/**
	 * Zero-arg constructor so this object can be easily created via reflection.
	 * A BatchAnnotationProcessorManager cannot be used until its
	 * {@link #configure(Main, String[])} method has been called.
	 */
	public BatchAnnotationProcessorManager() 
	{
	}

	@Override
	public void configure(Main batchCompiler, String[] commandLineArguments) {
		if (null != _processingEnv) {
			throw new IllegalStateException(
					"Calling configure() more than once on an AnnotationProcessorManager is not supported");
		}
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
	public void reportProcessorException(Processor p, Exception e) {
		// TODO: if (verbose) report the processor
		throw new AbortCompilation(null, e);
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
