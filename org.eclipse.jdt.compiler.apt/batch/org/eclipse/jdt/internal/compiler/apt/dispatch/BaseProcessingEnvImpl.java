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

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.model.ElementsImpl;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

/**
 * Implementation of ProcessingEnvironment that is common to batch and IDE environments.
 */
public abstract class BaseProcessingEnvImpl implements ProcessingEnvironment {

	// Initialized in subclasses:
	protected Filer _filer;
	protected Messager _messager;
	protected Map<String, String> _processorOptions;
	protected Compiler _compiler;
	
	// Initialized in this base class:
	protected Elements _elementUtils;
	protected Types _typeUtils;
	private List<ICompilationUnit> _addedUnits;
	private List<ICompilationUnit> _deletedUnits;

	public BaseProcessingEnvImpl() {
		_addedUnits = new ArrayList<ICompilationUnit>();
		_deletedUnits = new ArrayList<ICompilationUnit>();
		_elementUtils = new ElementsImpl(this);
		//TODO: _typeUtils = new TypesImpl(this);
	}

	public void addNewUnit(ICompilationUnit unit) {
		_addedUnits.add(unit);
	}

	public ICompilationUnit[] getDeletedUnits() {
		ICompilationUnit[] result = new ICompilationUnit[_deletedUnits.size()];
		_deletedUnits.toArray(result);
		return result;
	}

	public ICompilationUnit[] getNewUnits() {
		ICompilationUnit[] result = new ICompilationUnit[_addedUnits.size()];
		_addedUnits.toArray(result);
		return result;
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
	public Messager getMessager() {
		return _messager;
	}
	
	@Override
	public Map<String, String> getOptions() {
		return _processorOptions;
	}

	@Override
	public Types getTypeUtils() {
		return _typeUtils;
	}

	public LookupEnvironment getLookupEnvironment() {
		return _compiler.lookupEnvironment;
	}

	@Override
	public SourceVersion getSourceVersion() {
		// As of this writing, RELEASE_6 is the highest level available.
		// It is also the lowest level for which this code can possibly
		// be called.  When Java 7 is released, this method will need to
		// return a value based on _compiler.options.sourceLevel.
		return SourceVersion.RELEASE_6;
	}

	/**
	 * Called when AnnotationProcessorManager has retrieved the list of 
	 * newly generated compilation units.
	 */
	public void reset() {
		_addedUnits.clear();
		_deletedUnits.clear();
	}

}
