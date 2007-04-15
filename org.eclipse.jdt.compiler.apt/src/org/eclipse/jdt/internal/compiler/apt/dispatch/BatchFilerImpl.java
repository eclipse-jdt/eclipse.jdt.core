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

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * Implementation of Filer used when compilation is driven by command line
 * or by Tool interface.  This version does not need to keep track of
 * dependencies.
 */
public class BatchFilerImpl implements Filer {
	
	protected final BaseAnnotationProcessorManager _dispatchManager;
	protected final BaseProcessingEnvImpl _env;
	protected final JavaFileManager _fileManager;

	public BatchFilerImpl(BaseAnnotationProcessorManager dispatchManager, BatchProcessingEnvImpl env) 
	{
		_dispatchManager = dispatchManager;
		_fileManager = env._fileManager;
		_env = env;
	}

	public void addNewUnit(ICompilationUnit unit) {
		_env.addNewUnit(unit);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createClassFile(java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createClassFile(CharSequence name,
			Element... originatingElements) throws IOException 
	{
		//TODO: do we need to check validity of 'name', or can we trust the filemanager to handle that?
		JavaFileObject jfo = _fileManager.getJavaFileForOutput(
				StandardLocation.CLASS_OUTPUT, name.toString(), JavaFileObject.Kind.CLASS, null);
		return jfo;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createResource(javax.tools.JavaFileManager.Location, java.lang.CharSequence, java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public FileObject createResource(Location location, CharSequence pkg,
			CharSequence relativeName, Element... originatingElements)
			throws IOException {
		//TODO: do we need to check validity of 'name', or can we trust the filemanager to handle that?
		FileObject jfo = _fileManager.getFileForOutput(
				location, pkg.toString(), relativeName.toString(), null);
		return jfo;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createSourceFile(java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createSourceFile(CharSequence name,
			Element... originatingElements) throws IOException {
		//TODO: do we need to check validity of 'name', or can we trust the filemanager to handle that?
		JavaFileObject jfo = _fileManager.getJavaFileForOutput(
				StandardLocation.SOURCE_OUTPUT, name.toString(), JavaFileObject.Kind.SOURCE, null);

		// hook the file object's writers to create compilation unit and add to addedUnits()
		return new HookedJavaFileObject(jfo, jfo.getName(), this);
	}
	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#getResource(javax.tools.JavaFileManager.Location, java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public FileObject getResource(Location location, CharSequence pkg,
			CharSequence relativeName) throws IOException {
		//TODO: do we need to check validity of 'name', or can we trust the filemanager to handle that?
		FileObject fo = _fileManager.getFileForInput(
				location, pkg.toString(), relativeName.toString());
		return fo;
	}

}
