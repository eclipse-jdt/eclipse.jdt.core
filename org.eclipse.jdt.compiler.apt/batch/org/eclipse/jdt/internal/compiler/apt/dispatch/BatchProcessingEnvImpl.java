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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;

import org.eclipse.jdt.internal.compiler.apt.model.ElementsImpl;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

/**
 * The implementation of ProcessingEnvironment that is used when compilation is
 * driven by the command line or by the Tool interface.  This environment uses
 * the JavaFileManager provided by the compiler.
 */
public class BatchProcessingEnvImpl implements ProcessingEnvironment {

	protected final AnnotationProcessorManager _dispatchManager;
	protected final JavaFileManager _fileManager;
	protected final Main _compiler;
	protected final Filer _filer;
	protected final Messager _messager;
	protected final Elements _elementUtils;

	public BatchProcessingEnvImpl(AnnotationProcessorManager dispatchManager, Main batchCompiler,
			String[] commandLineArguments) 
	{
		_compiler = batchCompiler;
		_dispatchManager = dispatchManager;
		if (batchCompiler instanceof EclipseCompiler) {
			_fileManager = ((EclipseCompiler) batchCompiler).fileManager;
		} else {
			String encoding = (String) batchCompiler.options.get(CompilerOptions.OPTION_Encoding);
			Charset charset = encoding != null ? Charset.forName(encoding) : null;
			JavaFileManager manager = new EclipseFileManager(batchCompiler, batchCompiler.compilerLocale, charset);
			ArrayList<String> options = new ArrayList<String>();
			for (String argument : commandLineArguments) {
				options.add(argument);
			}
    		for (Iterator<String> iterator = options.iterator(); iterator.hasNext(); ) {
    			manager.handleOption(iterator.next(), iterator);
    		}
			_fileManager = manager;
		}
		//TODO: parse processor -A options
		_filer = new BatchFilerImpl(_dispatchManager, this);
		_messager = new BatchMessagerImpl(_compiler.batchCompiler.problemReporter);
		_elementUtils = new ElementsImpl(this);
	}
	
	public JavaFileManager getFileManager() {
		return _fileManager;
	}

	@Override
	public Elements getElementUtils() {
		return _elementUtils;
	}

	@Override
	public Filer getFiler() {
		return _filer;
	}

	@Override
	public Locale getLocale() {
		return _compiler.compilerLocale;
	}
	
	public LookupEnvironment getLookupEnvironment() {
		return _compiler.batchCompiler.lookupEnvironment;
	}

	@Override
	public Messager getMessager() {
		return _messager;
	}

	@Override
	public Map<String, String> getOptions() {
		// TODO return options
		return null;
	}

	@Override
	public SourceVersion getSourceVersion() {
		// TODO get source version from compiler options
		return null;
	}

	@Override
	public Types getTypeUtils() {
		// TODO implement type utilities object
		return null;
	}

}
